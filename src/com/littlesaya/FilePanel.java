package com.littlesaya;

import java.awt.Color;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.poifs.filesystem.NPOIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.littlesaya.pipe.ReferencePipe;
import com.littlesaya.pipe.ReferenceSenderPanel;

import layout.TableLayout;
import layout.TableLayoutConstraints;

@SuppressWarnings("serial")
public class FilePanel extends ReferenceSenderPanel<List<String>> {
	
	// 内部类
	// SwingWorker 定义
	// 打开文件
	private class OpenFileWorker extends
		SwingWorker<String, NameValuePair<String, MsgType>> {
		
		private File newFile;
		
		public OpenFileWorker(File newFile) {
			this.newFile = newFile;
		}

		@Override
		protected String doInBackground() throws Exception {
			try {
				publish(new NameValuePair<String, MsgType>("open: loading...", MsgType.info));
				// 记录 newFile 的信息，创建临时文件路径
				String name = newFile.getName(),
					   tempPath = "~temp-" + UUID.randomUUID().toString() + "-" + name;
				// 执行打开文件的操作将使状态回到最初的状态（ STAGE_EMPTY ）
				returnToStage(STAGE_EMPTY);
				// 创建临时文件
				File source = newFile,
					 target = new File(tempPath);
				try {
					Files.copy(source.toPath(), target.toPath());
				} catch (UnsupportedOperationException e1) {
					publish(new NameValuePair<String, MsgType>("open: copy: unsupported operation", MsgType.error));
					e1.printStackTrace();
					return "";
				} catch (FileAlreadyExistsException e2) {
					publish(new NameValuePair<String, MsgType>("open: copy: temp file already exists", MsgType.error));
					e2.printStackTrace();
					return "";
				} catch (SecurityException e3) {
					publish(new NameValuePair<String, MsgType>("open: copy: can not read or write", MsgType.error));
					e3.printStackTrace();
					return "";
				} catch (IOException e4) {
					publish(new NameValuePair<String, MsgType>("open: copy: i/o exception", MsgType.error));
					e4.printStackTrace();
					return "";
				}
				target.deleteOnExit(); // 退出时删除文件
				file = target; // 类成员 file
				// 识别文件类型，并创建相应的 workbook
				if (name.endsWith(".xlsx")) {
					try {
						xlsxBook = new XSSFWorkbook(file);
						ftype = FType.xlsx;
					} catch (InvalidFormatException e) {
						publish(new NameValuePair<String, MsgType>("open: read xlsx: invalid format", MsgType.error));
						e.printStackTrace();
						return "";
					} catch (IOException e) {
						publish(new NameValuePair<String, MsgType>("open: read xlsx: i/o exception", MsgType.error));
						e.printStackTrace();
						return "";
					}
				} else if (name.endsWith(".xls")) {
					try {
						xlsBook = new HSSFWorkbook(new NPOIFSFileSystem(file));
						ftype = FType.xls;
					} catch (IOException e) {
						publish(new NameValuePair<String, MsgType>("open: read xls: i/o exception", MsgType.error));
						e.printStackTrace();
						return "";
					}
				} else {
					// 不可识别
					publish(new NameValuePair<String, MsgType>("open: read: unknown file ext: " + name.substring(name.lastIndexOf('.')), MsgType.error));
					return "";
				}
				stage = STAGE_FILE;
				return name;
			} finally {
				if (STAGE_FILE != stage) {
					returnToStage(STAGE_EMPTY);
				} else {
					publish(new NameValuePair<String, MsgType>("open: file loaded", MsgType.success));
				}
			}
		}
		
		@Override
		protected void process(List<NameValuePair<String, MsgType>> chunks) {
			state(chunks.get(chunks.size() - 1).getName(), chunks.get(chunks.size() - 1).getValue());
	    }
		
		@Override
		protected void done() {
			String name = "";
			try {
				name = get();
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}
			if ("" != name) {
				// 文件已打开，显示文件名
				fileName.setText(name);
				// 填充 sheetSelect
				String[] sheetList = (FType.xlsx == ftype ? ExcelUtil.getSheetList(xlsxBook) : ExcelUtil.getSheetList(xlsBook));
				for (String s : sheetList) {
					sheetSelect.addItem(s);
				}
			}
			isOpeningFile = false;
			setEnabled(true);
	    }
	};
	
	// 静态常量
	
	// 阶段
	// STAGE_EMPTY:
	// 文件未输入
	private static final int STAGE_EMPTY = 0;
	// STAGE_FILE:
	// 文件已被正确打开并读取， ftype 被正确赋值， xlsxBook 或 xlsBook 被正确初始化
	// 由于在读取文件的过程中 sheetSelect 中的 item 列表会被重新设置，触发 sheetSelect 上的监听器，
	// 所以从 STAGE_EMPTY 变成 STAGE_FILE （也就是打开并读取文件）会间接导致 xlsxSheet 或 xlsSheet 被初始化为第一个工作表
	private static final int STAGE_FILE = 1;
	// STAGE_DATA:
	// 数据已生成
	private static final int STAGE_DATA = 2;
	
	// 枚举定义
	
	// 文件类型
	private enum FType {
		undefined,
		xlsx,
		xls
	};
	
	// 状态栏消息类型
	private enum MsgType {
		error,
		warning,
		info,
		success
	};
	
	// 成员变量
	
	// 当前状态
	private int stage = STAGE_EMPTY; // 所处阶段
	private boolean isOpeningFile = false; // 是否正在打开文件
	
	// 文件读取阶段
	private File file = null; // 指向文件
	private FType ftype = FType.undefined; // 文件类型
	private XSSFWorkbook xlsxBook = null; // xlsx
	private HSSFWorkbook xlsBook = null; // xls
	
	// 数据选择阶段
	private String sheetName = null;
	private int sheetIndex = -1;
	private XSSFSheet xlsxSheet = null;
	private HSSFSheet xlsSheet = null;
	private int startRowIdx = -1;
	private int startColIdx = -1;
	private int endRowIdx = -1;
	private int endColIdx = -1;
	private List<String> data = new ArrayList<String>();
	
	// 标题
	private String title;
	
	// 控件，布局，监听器

	// 标题栏
	private static final double[][] headPanelLayoutDimension = {
			{ TableLayout.FILL },
			{ TableLayout.FILL }
	};
	private JPanel headPanel;
	private JLabel mainTitle;

	// 文件选择栏
	private static final double[][] fileSelectPanelLayoutDimension = {
			{ 60, TableLayout.FILL },
			{ TableLayout.FILL }
	};
	private JPanel fileSelectPanel;
	private JButton fileSelectButton;
	private JTextField fileName;

	// 文件选择按钮监听器
	private ActionListener fileSelectActionListener = new ActionListener() {
		
		@Override
		public void actionPerformed(ActionEvent e) {
			if (isOpeningFile) {
				return;
			} else {
				isOpeningFile = true;
			}
			// 选择文件
			JFileChooser chooser = new JFileChooser();
			chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			if (chooser.showDialog(null, "确认") == JFileChooser.APPROVE_OPTION) {
				File newFile = chooser.getSelectedFile();
				// 打开
				setEnabled(false);
				new OpenFileWorker(newFile).execute();
			} else {
				// 放弃打开
				isOpeningFile = false;
			}
		}
	};

	// 工作表选择栏
	private static final double[][] sheetSelectPanelLayoutDimension = {
			{ 60, 120, TableLayout.FILL },
			{ TableLayout.FILL }
	};
	private JPanel sheetSelectPanel;
	private JLabel sheetSelectLabel;
	private JComboBox<String> sheetSelect;
	
	// 工作表选择监听器
	private ItemListener sheetSelectItemListener = new ItemListener() {
		
		@Override
		public void itemStateChanged(ItemEvent e) {
			// 检查状态是否大于等于 STAGE_FILE ，确保文件已经读取成功
			if (stage < STAGE_FILE) {
				return;
			}
			if (e.getStateChange() == ItemEvent.SELECTED) {
				@SuppressWarnings("unchecked")
				JComboBox<String> source = (JComboBox<String>)e.getSource();
				// 存储当前所选的工作表名称
				String savedSheetName = (String)source.getSelectedItem();
				// 将阶段状态退回至 STAGE_FILE
				returnToStage(STAGE_FILE);
				// 为变量重新赋值
				sheetName = savedSheetName;
				sheetIndex = (FType.xlsx == ftype ? xlsxBook.getSheetIndex(sheetName) : xlsBook.getSheetIndex(sheetName));
				if (FType.xlsx == ftype) xlsxSheet = xlsxBook.getSheetAt(sheetIndex); else xlsSheet = xlsBook.getSheetAt(sheetIndex);
				state("sheet \"" + sheetName + "\" selected", MsgType.info);
			}
		}
	};
	
	// 数据选择栏
	private static final double[][] dataSelectPanelLayoutDimension = {
			{ 60, 60, 60, TableLayout.FILL },
			{ 30, 30, 30, TableLayout.FILL }
	};
	private JPanel dataSelectPanel;
	private JLabel rowLabel;
	private JLabel colLabel;
	private JLabel startLabel;
	private JLabel endLabel;
	private JTextField startRowInput;
	private JTextField startColInput;
	private JTextField endRowInput;
	private JTextField endColInput;
	
	// 数据选择监听器
	private DocumentListener inputDocumentListener = new DocumentListener() {
		
		@Override
		public void removeUpdate(DocumentEvent e) {
			onChange();
		}
		
		@Override
		public void insertUpdate(DocumentEvent e) {
			onChange();
		}
		
		@Override
		public void changedUpdate(DocumentEvent e) {
		}
		
		private void onChange() {
			// 在这里， stage 将有可能成为 STAGE_DATA
			// 每一次控件的内容发生改变，程序都会尝试获取数据
			
			// 只有在文件读取完成后才会对起止单元格进行检验
			if (stage < STAGE_FILE) {
				return;
			}
			// 尝试获取 cells
			Cell[] cells = getCells();
			if (null == cells) {
				state("invalid address", MsgType.warning);
				return;
			}
			// 尝试从 cells 中获取数据
			data.clear();
			for (Cell cell : cells) {
				if (null != cell) {
					data.add(cellHandler(cell));
				} else {
					state("invalid address", MsgType.warning);
					return;
				}
			}
			sendRef(data);
			stage = STAGE_DATA;
			state("get data successfully!", MsgType.success);
			for (int i = 0; i < data.size(); ++i) {
				System.out.println("idx: " + i + "\t: " + data.get(i));
			}
		}
		
		private Cell[] getCells() {
			// 获取并检查四个 JTextField 中的数据
			String startRowStr = startRowInput.getText();
			String startColStr = startColInput.getText();
			String endRowStr = endRowInput.getText();
			String endColStr = endColInput.getText();
			// 检查是否为空
			if (startRowStr.length() == 0 || startColStr.length() == 0 || endRowStr.length() == 0 || endColStr.length() == 0) {
				return null;
			}
			// 将行列数据从字符串转为数字（下标）
			startRowIdx = ExcelUtil.strToNum(startRowStr) - 1;
			startColIdx = ExcelUtil.strToNum(startColStr) - 1;
			endRowIdx = ExcelUtil.strToNum(endRowStr) - 1;
			endColIdx = ExcelUtil.strToNum(endColStr) - 1;
			// 检查行下标是否合法
			final int minRowIdx = (FType.xlsx == ftype ? xlsxSheet.getFirstRowNum() : xlsSheet.getFirstRowNum());
			final int maxRowIdx = (FType.xlsx == ftype ? xlsxSheet.getLastRowNum() : xlsSheet.getLastRowNum());
			if (startRowIdx < minRowIdx || startRowIdx > maxRowIdx || endRowIdx < minRowIdx || endRowIdx > maxRowIdx) {
				return null;
			}
			// 检查开始和结束 cell 是否存在
			try {
				if (FType.xlsx == ftype) {
					xlsxSheet.getRow(startRowIdx).getCell(startColIdx);
					xlsxSheet.getRow(endRowIdx).getCell(endColIdx);
				} else {
					xlsSheet.getRow(startRowIdx).getCell(startColIdx);
					xlsSheet.getRow(endRowIdx).getCell(endColIdx);
				}
			} catch (NullPointerException e) {
				// 开始或结束 cell 不存在
				return null;
			}
			// 检查是一行还是一列
			try {
				if (startRowIdx == endRowIdx) {
					// 一行
					int start = startColIdx,
						step = (startColIdx <= endColIdx ? 1 : -1),
						stepNum = Math.abs(endColIdx - startColIdx) + 1; // 多少步，也是 cell 数组的大小
					Cell[] result = new Cell[stepNum];
					for (int i = 0, j = start; i < stepNum; ++i, j += step) {
						result[i] = (FType.xlsx == ftype ?
								xlsxSheet.getRow(startRowIdx).getCell(j) :
								xlsSheet.getRow(startRowIdx).getCell(j));
					}
					return result;
				} else if (startColIdx == endColIdx) {
					// 一列
					int start = startRowIdx,
						step = (startRowIdx <= endRowIdx ? 1 : -1),
						stepNum = Math.abs(endRowIdx - startRowIdx) + 1; // 多少步，也是 cell 数组的大小
					Cell[] result = new Cell[stepNum];
					for (int i = 0, j = start; i < stepNum; ++i, j += step) {
						result[i] = (FType.xlsx == ftype ?
								xlsxSheet.getRow(j).getCell(startColIdx) :
								xlsSheet.getRow(j).getCell(startColIdx));
					}
					return result;
				} else {
					return null;
				}
			} catch (NullPointerException e) {
				return null;
			}
		}
		
		private String cellHandler(Cell cell) {
			String datum = "null";
			switch (cell.getCellTypeEnum()) {
			case _NONE:
				datum = "_NONE";
				break;
			case BLANK:
				datum = "";
				break;
			case BOOLEAN:
				datum = "" + cell.getBooleanCellValue();
				break;
			case ERROR:
				datum = String.valueOf(cell.getErrorCellValue());
				break;
			case FORMULA:
				datum = cell.getCellFormula();
				break;
			case NUMERIC:
				if (HSSFDateUtil.isCellDateFormatted(cell)) {
					double dtNumVal = cell.getNumericCellValue();
					Date dt = cell.getDateCellValue();
					if (dtNumVal < 1.0) {
						// 如果 <1 ，认为它是时间
						datum = new SimpleDateFormat("HH:mm:ss").format(dt);
					} else {
						// 如果 >=1 ，则认为它是日期加时间
						datum = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(dt);
					}
				} else {
					datum = "" + cell.getNumericCellValue();
				}
				break;
			case STRING:
				datum = cell.getStringCellValue();
				break;
			default:
				datum = "";
				break;
			}
			return datum;
		}
	};
	
	// 动作栏
	private static final double[][] actionPanelLayoutDimension = {
			{ TableLayout.FILL },
			{ TableLayout.FILL }
	};
	private JPanel actionPanel;
	private JButton confirmButton;
	
	// 确认按钮监听器
	private ActionListener confirmActionListener = new ActionListener() {
		
		@Override
		public void actionPerformed(ActionEvent e) {
			state("you have just clicked a useless button :-)", MsgType.info);
		}
	};

	// 页脚栏（状态栏）
	private static final double[][] footPanelLayoutDimension = {
			{ TableLayout.FILL },
			{ TableLayout.FILL }
	};
	private JPanel footPanel;
	private JTextField statusBar;
	
	// FilePanel 自身
	private static final double[][] filePanelLayoutDimension = {
		{ 5, TableLayout.FILL, 5 },
		{ 5, 30, 30, 30, 90, 30, TableLayout.FILL, 30, 5 }
	};
	
	// Drag and Drop 监听器
	@SuppressWarnings("unused")
	private DropTarget fileDropTarget = new DropTarget(this, DnDConstants.ACTION_COPY_OR_MOVE, new DropTargetAdapter() {
		
		@Override
		public void drop(DropTargetDropEvent dtde) {
			if (isOpeningFile) {
				return;
			} else {
				isOpeningFile = true;
			}
			dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
			Transferable transferable = dtde.getTransferable();
			try {
				// 获取文件列表
				@SuppressWarnings("unchecked")
				List<File> files = (List<File>)transferable.getTransferData(DataFlavor.javaFileListFlavor);
				if (files.size() > 0) {
					// 打开列表中的第一个文件
					File newFile = files.get(0);
					setEnabled(false);
					new OpenFileWorker(newFile).execute();
				} else {
					state("DnD: empty file list", MsgType.warning);
				}
			} catch (UnsupportedFlavorException e) {
				state("DnD: unsupported flavor", MsgType.error);
				e.printStackTrace();
			} catch (IOException e) {
				state("DnD: fail opening file", MsgType.error);
				e.printStackTrace();
			}
		}
	}, true);
	
	// 公有成员函数
	
	// 构造函数
	public FilePanel(String title, List<ReferencePipe<List<String>>> pipes) {
		super(pipes, new TableLayout(filePanelLayoutDimension), true);
		this.title = title; // 标题
		// 添加标题栏
		initHeadPanel();
		this.add(headPanel, new TableLayoutConstraints(1, 1, 1, 1, TableLayout.FULL, TableLayout.FULL));
		// 添加文件选择栏
		initFileSelectPanel();
		this.add(fileSelectPanel, new TableLayoutConstraints(1, 2, 1, 2, TableLayout.FULL, TableLayout.FULL));
		// 添加工作表选择栏
		initSheetSelectPanel();
		this.add(sheetSelectPanel, new TableLayoutConstraints(1, 3, 1, 3, TableLayout.FULL, TableLayout.FULL));
		// 添加数据选择栏
		initDataSelectPanel();
		this.add(dataSelectPanel, new TableLayoutConstraints(1, 4, 1, 4, TableLayout.FULL, TableLayout.FULL));
		// 添加动作栏
		initActionPanel();
		this.add(actionPanel, new TableLayoutConstraints(1, 5, 1, 5, TableLayout.FULL, TableLayout.FULL));
		// 添加页脚栏
		initFootPanel();
		this.add(footPanel, new TableLayoutConstraints(1, 7, 1, 7, TableLayout.FULL, TableLayout.FULL));
	}
	
	// 设置内部控件是否接受用户输入
	@Override
	public void setEnabled(boolean flag) {
		Field[] fields = this.getClass().getDeclaredFields();
		for (Field field : fields) {
			try {
				Object obj = field.get(this);
				if (obj instanceof JButton || obj instanceof JTextField || obj instanceof JComboBox) {
					((JComponent)obj).setEnabled(flag);
				}
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
	}

	// 获取数据
	public String[] getData() {
		return (String[])data.toArray();
	}
	
	// 释放资源（调用 returnToStage(STAGE_EMPTY) ）
	public void releaseResource() {
		returnToStage(STAGE_EMPTY);
	}
	
	// 私有成员函数
	
	// 控件初始化

	// 标题栏
	private void initHeadPanel() {
		headPanel = new JPanel(new TableLayout(headPanelLayoutDimension), true);
		
		mainTitle = new JLabel(title);
		headPanel.add(mainTitle, new TableLayoutConstraints(0, 0, 0, 0, TableLayout.LEFT, TableLayout.CENTER));
	}
	
	// 文件选择栏
	private void initFileSelectPanel() {
		fileSelectPanel = new JPanel(new TableLayout(fileSelectPanelLayoutDimension), true);
		
		fileSelectButton = new JButton("浏览");
		fileSelectButton.addActionListener(fileSelectActionListener);
		fileSelectPanel.add(fileSelectButton, new TableLayoutConstraints(0, 0, 0, 0, TableLayout.CENTER, TableLayout.CENTER));
		
		fileName = new JTextField();
		fileName.setEditable(false);
		fileSelectPanel.add(fileName, new TableLayoutConstraints(1, 0, 1, 0, TableLayout.FULL, TableLayout.CENTER));
	}
	
	// 工作表选择栏
	private void initSheetSelectPanel() {
		sheetSelectPanel = new JPanel(new TableLayout(sheetSelectPanelLayoutDimension), true);
		
		sheetSelectLabel = new JLabel("工作表");
		sheetSelectPanel.add(sheetSelectLabel, new TableLayoutConstraints(0, 0, 0, 0, TableLayout.CENTER, TableLayout.CENTER));
		
		sheetSelect = new JComboBox<String>();
		sheetSelect.addItemListener(sheetSelectItemListener);
		sheetSelectPanel.add(sheetSelect, new TableLayoutConstraints(1, 0, 1, 0, TableLayout.FULL, TableLayout.CENTER));
	}
	
	// 数据选择栏
	private void initDataSelectPanel() {
		dataSelectPanel = new JPanel(new TableLayout(dataSelectPanelLayoutDimension), true);
		
		rowLabel = new JLabel("行");
		dataSelectPanel.add(rowLabel, new TableLayoutConstraints(1, 0, 1, 0, TableLayout.CENTER, TableLayout.CENTER));
		
		colLabel = new JLabel("列");
		dataSelectPanel.add(colLabel, new TableLayoutConstraints(2, 0, 2, 0, TableLayout.CENTER, TableLayout.CENTER));
		
		startLabel = new JLabel("开始");
		dataSelectPanel.add(startLabel, new TableLayoutConstraints(0, 1, 0, 1, TableLayout.CENTER, TableLayout.CENTER));
		
		endLabel = new JLabel("结束");
		dataSelectPanel.add(endLabel, new TableLayoutConstraints(0, 2, 0, 2, TableLayout.CENTER, TableLayout.CENTER));
		
		startRowInput = new JTextField();
		startRowInput.getDocument().addDocumentListener(inputDocumentListener);
		dataSelectPanel.add(startRowInput, new TableLayoutConstraints(1, 1, 1, 1, TableLayout.FULL, TableLayout.CENTER));
		
		startColInput = new JTextField();
		startColInput.getDocument().addDocumentListener(inputDocumentListener);
		dataSelectPanel.add(startColInput, new TableLayoutConstraints(2, 1, 2, 1, TableLayout.FULL, TableLayout.CENTER));
		
		endRowInput = new JTextField();
		endRowInput.getDocument().addDocumentListener(inputDocumentListener);
		dataSelectPanel.add(endRowInput, new TableLayoutConstraints(1, 2, 1, 2, TableLayout.FULL, TableLayout.CENTER));
		
		endColInput = new JTextField();
		endColInput.getDocument().addDocumentListener(inputDocumentListener);
		dataSelectPanel.add(endColInput, new TableLayoutConstraints(2, 2, 2, 2, TableLayout.FULL, TableLayout.CENTER));
	}
	
	// 动作栏
	private void initActionPanel() {
		actionPanel = new JPanel(new TableLayout(actionPanelLayoutDimension), true);
		
		confirmButton = new JButton("确定");
		confirmButton.addActionListener(confirmActionListener);
		actionPanel.add(confirmButton, new TableLayoutConstraints(0, 0, 0, 0, TableLayout.FULL, TableLayout.CENTER));
	}
	
	// 状态栏
	private void initFootPanel() {
		footPanel = new JPanel(new TableLayout(footPanelLayoutDimension), true);
		
		statusBar = new JTextField();
		statusBar.setEditable(false);
		footPanel.add(statusBar, new TableLayoutConstraints(0, 0, 0, 0, TableLayout.FULL, TableLayout.CENTER));
	}
	
	// 工具函数

	// 返回某一状态
	private void returnToStage(int target) {
		if (target != STAGE_EMPTY && target != STAGE_FILE) {
			return;
		}
		if (target < STAGE_DATA) {
			// 回到 STAGE_DATA 之前， STAGE_EMPTY 之后的状态
			// 释放相关资源
			sheetName = null;
			sheetIndex = -1;
			xlsxSheet = null;
			xlsSheet = null;
			startRowIdx = startColIdx = endRowIdx = endColIdx = -1;
			data.clear();
			// 清空相关控件的内容
//			sheetSelect.setSelectedIndex(-1);
			startRowInput.setText("");
			startColInput.setText("");
			endRowInput.setText("");
			endColInput.setText("");
		}
		if (target < STAGE_FILE) {
			// 继续回到 STAGE_FILE ，也就是 STAGE_EMPTY 的状态
			// 释放相关资源
			if (null != xlsxBook) {
				try {
					xlsxBook.close();
				} catch (IOException e) {
					state("return: fail to close xlsx", MsgType.warning);
					e.printStackTrace();
				}
				xlsxBook = null;
			}
			if (null != xlsBook) {
				try {
					xlsBook.close();
				} catch (IOException e) {
					state("return: fail to close xls", MsgType.warning);
					e.printStackTrace();
				}
				xlsBook = null;
			}
			ftype = FType.undefined;
			file = null;
			// 清空相关控件的内容
			sheetSelect.removeAllItems();
			fileName.setText("");
		}
		stage = target;
	}
	
	// 输出消息到状态栏
	
	private void state(String msg, MsgType mtype) {
		switch (mtype) {
		case error:
			statusBar.setForeground(Color.RED);
			break;
		case warning:
			statusBar.setForeground(Color.ORANGE);
			break;
		case info:
			statusBar.setForeground(Color.BLUE);
			break;
		case success:
			statusBar.setForeground(Color.GREEN);
			break;
		}
		statusBar.setText(msg);
	}
	
}

class NameValuePair<Tn, Tv> {

	private Tn name;
	private Tv value;
	
	public NameValuePair(Tn name, Tv value) {
		this.setName(name);
		this.setValue(value);
	}

	public Tn getName() {
		return name;
	}

	public void setName(Tn name) {
		this.name = name;
	}

	public Tv getValue() {
		return value;
	}

	public void setValue(Tv value) {
		this.value = value;
	}
}
