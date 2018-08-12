package com.littlesaya;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import layout.TableLayout;
import layout.TableLayoutConstraints;

public class Main {
	
	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				new MyFrame();
			}
		});
	}

}

class MyFrame extends JFrame {
	
	// 文件 1
	private JPanel file1MainContainer;
	private JLabel file1MainTitle;
	private JPanel file1FileSelectContainer;
	private JButton file1FileSelectButton;
	private JTextField file1SelectedFileName;
	private JPanel file1SheetContainer;
	private JLabel file1SheetLabel;
	private JTextField file1SheetTextField;
	private JPanel file1DataSelectContainer;
	private JLabel file1RowTitle;
	private JLabel file1ColTitle;
	private JLabel file1StartTitle;
	private JLabel file1EndTitle;
	private JTextField file1StartRow;
	private JTextField file1StartCol;
	private JTextField file1EndRow;
	private JTextField file1EndCol;
	private JButton file1Update;
	private File file1;
	
	// 文件2
	private JPanel file2MainContainer;
	private JLabel file2MainTitle;
	private JPanel file2FileSelectContainer;
	private JButton file2FileSelectButton;
	private JTextField file2SelectedFileName;
	private JPanel file2SheetContainer;
	private JLabel file2SheetLabel;
	private JTextField file2SheetTextField;
	private JPanel file2DataSelectContainer;
	private JLabel file2RowTitle;
	private JLabel file2ColTitle;
	private JLabel file2StartTitle;
	private JLabel file2EndTitle;
	private JTextField file2StartRow;
	private JTextField file2StartCol;
	private JTextField file2EndRow;
	private JTextField file2EndCol;
	private JButton file2Update;
	private File file2;
	
	// 比较结果
	private JPanel resultMainContainer;
	private JLabel resultMainTitle;
	private JScrollPane resultCompareContainer;
	private JTable resultCompareTable;
	
	private DefaultTableCellRenderer compareRenderer = new DefaultTableCellRenderer() {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        	Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        	String value1 = (String)resultCompareTable.getModel().getValueAt(row, 0);
        	String value2 = (String)resultCompareTable.getModel().getValueAt(row, 1);
        	if (value1.compareTo(value2) == 0) {
            	cell.setBackground(Color.GREEN);
            	cell.setForeground(Color.BLACK);
        	} else {
        		cell.setBackground(Color.RED);
        		cell.setForeground(Color.BLACK);
        	}
	        return cell;
        }
	};
	
	public MyFrame() {
		super("Excel Comparator");
		setSize(800, 400);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		initLayout();
		initButton();
		setVisible(true);
	}

	private void initLayout() {
		Container container = getContentPane();
		double[][] containerSize = {
				{ 5, 0.3, 5, 0.3, 5, TableLayout.FILL, 5 },
				{ 5, TableLayout.FILL, 5 }
		};
		container.setLayout(new TableLayout(containerSize));
		
		// file1MainContainer
		double[][] file1MainContainerSize = {
				{ TableLayout.FILL },
				{ 30, 30, 30, 100, TableLayout.FILL }
		};
		file1MainContainer = new JPanel(new TableLayout(file1MainContainerSize), true);
		file1MainContainer.setBorder(BorderFactory.createEtchedBorder());
			// file1MainTitle
			file1MainTitle = new JLabel("文件1");
			file1MainContainer.add(file1MainTitle, new TableLayoutConstraints(0, 0, 0, 0, TableLayout.LEFT, TableLayout.CENTER));
			// file1FileSelectContainer
			double[][] file1FileSelectContainerSize = {
					{ 60, TableLayout.FILL },
					{ TableLayout.FILL }
			};
			file1FileSelectContainer = new JPanel(new TableLayout(file1FileSelectContainerSize), true);
				// file1FileSelectButton
				file1FileSelectButton = new JButton("浏览");
				file1FileSelectContainer.add(file1FileSelectButton, new TableLayoutConstraints(0, 0, 0, 0, TableLayout.FULL, TableLayout.CENTER));
				// file1SelectedFileName
				file1SelectedFileName = new JTextField();
				file1SelectedFileName.setEditable(false);
				file1FileSelectContainer.add(file1SelectedFileName, new TableLayoutConstraints(1, 0, 1, 0, TableLayout.FULL, TableLayout.CENTER));
			file1MainContainer.add(file1FileSelectContainer, new TableLayoutConstraints(0, 1, 0, 1, TableLayout.FULL, TableLayout.CENTER));
			// file1SheetContainer
			double[][] file1SheetContainerSize = {
					{ 60, 120, TableLayout.FILL },
					{ TableLayout.FILL }
			};
			file1SheetContainer = new JPanel(new TableLayout(file1SheetContainerSize), true);
				// file1SheetLabel
				file1SheetLabel = new JLabel("工作表");
				file1SheetContainer.add(file1SheetLabel, new TableLayoutConstraints(0, 0, 0, 0, TableLayout.CENTER, TableLayout.CENTER));
				// file1SheetTextField
				file1SheetTextField = new JTextField();
				file1SheetContainer.add(file1SheetTextField, new TableLayoutConstraints(1, 0, 1, 0, TableLayout.FULL, TableLayout.CENTER));
			file1MainContainer.add(file1SheetContainer, new TableLayoutConstraints(0, 2, 0, 2, TableLayout.FULL, TableLayout.CENTER));
			// file1DataSelectContainer
			double[][] file1DataSelectContainerSize = {
					{ 60, 60, 60 },
					{ 30, 30, 30 }
			};
			file1DataSelectContainer = new JPanel(new TableLayout(file1DataSelectContainerSize), true);
				// file1RowTitle
				file1RowTitle = new JLabel("行");
				file1DataSelectContainer.add(file1RowTitle, new TableLayoutConstraints(1, 0, 1, 0, TableLayout.CENTER, TableLayout.CENTER));
				// file1ColTitle
				file1ColTitle = new JLabel("列");
				file1DataSelectContainer.add(file1ColTitle, new TableLayoutConstraints(2, 0, 2, 0, TableLayout.CENTER, TableLayout.CENTER));
				// file1StartTitle
				file1StartTitle = new JLabel("开始");
				file1DataSelectContainer.add(file1StartTitle, new TableLayoutConstraints(0, 1, 0, 1, TableLayout.CENTER, TableLayout.CENTER));
				// file1EndTitle
				file1EndTitle = new JLabel("结束");
				file1DataSelectContainer.add(file1EndTitle, new TableLayoutConstraints(0, 2, 0, 2, TableLayout.CENTER, TableLayout.CENTER));
				// file1StartRow
				file1StartRow = new JTextField();
				file1DataSelectContainer.add(file1StartRow, new TableLayoutConstraints(1, 1, 1, 1, TableLayout.FULL, TableLayout.CENTER));
				// file1StartCol
				file1StartCol = new JTextField();
				file1DataSelectContainer.add(file1StartCol, new TableLayoutConstraints(2, 1, 2, 1, TableLayout.FULL, TableLayout.CENTER));
				// file1EndRow
				file1EndRow = new JTextField();
				file1DataSelectContainer.add(file1EndRow, new TableLayoutConstraints(1, 2, 1, 2, TableLayout.FULL, TableLayout.CENTER));
				// file1EndCol
				file1EndCol = new JTextField();
				file1DataSelectContainer.add(file1EndCol, new TableLayoutConstraints(2, 2, 2, 2, TableLayout.FULL, TableLayout.CENTER));
			file1MainContainer.add(file1DataSelectContainer, new TableLayoutConstraints(0, 3, 0, 3, TableLayout.LEFT, TableLayout.TOP));
			// file1Update
			file1Update = new JButton("更新");
			file1MainContainer.add(file1Update, new TableLayoutConstraints(0, 4, 0, 4, TableLayout.FULL, TableLayout.TOP));
		container.add(file1MainContainer, new TableLayoutConstraints(1, 1, 1, 1, TableLayout.FULL, TableLayout.FULL));
		
		// file2MainContainer
		double[][] file2MainContainerSize = {
				{ TableLayout.FILL },
				{ 30, 30, 30, 100, TableLayout.FILL }
		};
		file2MainContainer = new JPanel(new TableLayout(file2MainContainerSize), true);
		file2MainContainer.setBorder(BorderFactory.createEtchedBorder());
			// file2MainTitle
			file2MainTitle = new JLabel("文件2");
			file2MainContainer.add(file2MainTitle, new TableLayoutConstraints(0, 0, 0, 0, TableLayout.LEFT, TableLayout.CENTER));
			// file2FileSelectContainer
			double[][] file2FileSelectContainerSize = {
					{ 60, TableLayout.FILL},
					{ TableLayout.FILL }
			};
			file2FileSelectContainer = new JPanel(new TableLayout(file2FileSelectContainerSize), true);
				// file2FileSelectButton
				file2FileSelectButton = new JButton("浏览");
				file2FileSelectContainer.add(file2FileSelectButton, new TableLayoutConstraints(0, 0, 0, 0, TableLayout.FULL, TableLayout.CENTER));
				// file2SelectedFileName
				file2SelectedFileName = new JTextField();
				file2SelectedFileName.setEditable(false);
				file2FileSelectContainer.add(file2SelectedFileName, new TableLayoutConstraints(1, 0, 1, 0, TableLayout.FULL, TableLayout.CENTER));
			file2MainContainer.add(file2FileSelectContainer, new TableLayoutConstraints(0, 1, 0, 1, TableLayout.FULL, TableLayout.CENTER));
			// file2SheetContainer
			double[][] file2SheetContainerSize = {
					{ 60, 120, TableLayout.FILL },
					{ TableLayout.FILL }
			};
			file2SheetContainer = new JPanel(new TableLayout(file2SheetContainerSize), true);
				// file2SheetLabel
				file2SheetLabel = new JLabel("工作表");
				file2SheetContainer.add(file2SheetLabel, new TableLayoutConstraints(0, 0, 0, 0, TableLayout.CENTER, TableLayout.CENTER));
				// file2SheetTextField
				file2SheetTextField = new JTextField();
				file2SheetContainer.add(file2SheetTextField, new TableLayoutConstraints(1, 0, 1, 0, TableLayout.FULL, TableLayout.CENTER));
			file2MainContainer.add(file2SheetContainer, new TableLayoutConstraints(0, 2, 0, 2, TableLayout.FULL, TableLayout.CENTER));
			// file2DataSelectContainer
			double[][] file2DataSelectContainerSize = {
					{ 60, 60, 60 },
					{ 30, 30, 30 }
			};
			file2DataSelectContainer = new JPanel(new TableLayout(file2DataSelectContainerSize), true);
				// file2RowTitle
				file2RowTitle = new JLabel("行");
				file2DataSelectContainer.add(file2RowTitle, new TableLayoutConstraints(1, 0, 1, 0, TableLayout.CENTER, TableLayout.CENTER));
				// file2ColTitle
				file2ColTitle = new JLabel("列");
				file2DataSelectContainer.add(file2ColTitle, new TableLayoutConstraints(2, 0, 2, 0, TableLayout.CENTER, TableLayout.CENTER));
				// file2StartTitle
				file2StartTitle = new JLabel("开始");
				file2DataSelectContainer.add(file2StartTitle, new TableLayoutConstraints(0, 1, 0, 1, TableLayout.CENTER, TableLayout.CENTER));
				// file2EndTitle
				file2EndTitle = new JLabel("结束");
				file2DataSelectContainer.add(file2EndTitle, new TableLayoutConstraints(0, 2, 0, 2, TableLayout.CENTER, TableLayout.CENTER));
				// file2StartRow
				file2StartRow = new JTextField();
				file2DataSelectContainer.add(file2StartRow, new TableLayoutConstraints(1, 1, 1, 1, TableLayout.FULL, TableLayout.CENTER));
				// file2StartCol
				file2StartCol = new JTextField();
				file2DataSelectContainer.add(file2StartCol, new TableLayoutConstraints(2, 1, 2, 1, TableLayout.FULL, TableLayout.CENTER));
				// file2EndRow
				file2EndRow = new JTextField();
				file2DataSelectContainer.add(file2EndRow, new TableLayoutConstraints(1, 2, 1, 2, TableLayout.FULL, TableLayout.CENTER));
				// file2EndCol
				file2EndCol = new JTextField();
				file2DataSelectContainer.add(file2EndCol, new TableLayoutConstraints(2, 2, 2, 2, TableLayout.FULL, TableLayout.CENTER));
			file2MainContainer.add(file2DataSelectContainer, new TableLayoutConstraints(0, 3, 0, 3, TableLayout.LEFT, TableLayout.TOP));
			// file2Update
			file2Update = new JButton("更新");
			file2MainContainer.add(file2Update, new TableLayoutConstraints(0, 4, 0, 4, TableLayout.FULL, TableLayout.TOP));
		container.add(file2MainContainer, new TableLayoutConstraints(3, 1, 3, 1, TableLayout.FULL, TableLayout.FULL));
		
		// resultMainContainer
		double[][] resultMainContainerSize = {
				{ TableLayout.FILL },
				{ 30, TableLayout.FILL }
		};
		resultMainContainer = new JPanel(new TableLayout(resultMainContainerSize), true);
		resultMainContainer.setBorder(BorderFactory.createEtchedBorder());
			// resultMainTitle
			resultMainTitle = new JLabel("比较结果");
			resultMainContainer.add(resultMainTitle, new TableLayoutConstraints(0, 0, 0, 0, TableLayout.LEFT, TableLayout.CENTER));
			// resultCompareContainer
				// resultCompareTable
				resultCompareTable = new JTable(new CompareTableModel()) {
					@Override
					public TableCellRenderer getCellRenderer(int row, int col) {
						return compareRenderer;
					}
				};
			resultCompareContainer = new JScrollPane(resultCompareTable);
			resultMainContainer.add(resultCompareContainer, new TableLayoutConstraints(0, 1, 0, 1, TableLayout.FULL, TableLayout.FULL));
		container.add(resultMainContainer, new TableLayoutConstraints(5, 1, 5, 1, TableLayout.FULL, TableLayout.FULL));
	}

	private void initButton() {
		// 文件选择
		MyFrame parent = this;
		file1FileSelectButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser();
				chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				chooser.showDialog(parent, "确认");
				file1 = chooser.getSelectedFile();
				file1SelectedFileName.setText(file1.getName());
			}
		});
		file2FileSelectButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser();
				chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				chooser.showDialog(parent, "确认");
				file2 = chooser.getSelectedFile();
				file2SelectedFileName.setText(file2.getName());
			}
		});
		// 更新数据
		file1Update.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				updateFileData(0);
			}
		});
		file2Update.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				updateFileData(1);
			}
		});
	}

	private void updateFileData(int fileIdx) {
		// 选择文件
		File theFile = (0 == fileIdx ? file1 : file2);
		// 检查文件
		if (theFile == null || !theFile.exists() || !theFile.isFile()) {
			return;
		}
		// 读取工作簿
		XSSFWorkbook book = null;
		try {
			book = new XSSFWorkbook(theFile);
		} catch (IOException | InvalidFormatException e) {
			e.printStackTrace();
			return;
		}
		// 检查工作表
		String sheetName = (0 == fileIdx ? file1SheetTextField.getText() : file2SheetTextField.getText());
		XSSFSheet sheet = null;
		try {
			// 首先尝试将 sheet 名称识别为数字
			int sheetIndex = Integer.parseInt(sheetName) - 1;
			if (sheetIndex >= 0) {
				sheet = book.getSheetAt(sheetIndex);
			}
		} catch (NumberFormatException e) {
			// 无法将 sheetName 识别为数字
			sheet = book.getSheet(sheetName);
		} catch (IllegalArgumentException e2) {
			sheet = null;
		}
		if (sheet == null) {
			try {
				book.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return;
		}
		// 检查行列
		String strStartRow = (0 == fileIdx ? file1StartRow.getText() : file2StartRow.getText());
		String strStartCol = (0 == fileIdx ? file1StartCol.getText() : file2StartCol.getText());
		String strEndRow = (0 == fileIdx ? file1EndRow.getText() : file2EndRow.getText());
		String strEndCol = (0 == fileIdx ? file1EndCol.getText() : file2EndCol.getText());
		// 首先尝试将行列按照数字解析，再尝试将其按照字母解析
		int startRow, startCol, endRow, endCol;
		try {
			startRow = Integer.parseInt(strStartRow);
		} catch (NumberFormatException e) {
			startRow = alphaToDigit(strStartRow);
		}
		try {
			startCol = Integer.parseInt(strStartCol);
		} catch (NumberFormatException e) {
			startCol = alphaToDigit(strStartCol);
		}
		try {
			endRow = Integer.parseInt(strEndRow);
		} catch (NumberFormatException e) {
			endRow = alphaToDigit(strEndRow);
		}
		try {
			endCol = Integer.parseInt(strEndCol);
		} catch (NumberFormatException e) {
			endCol = alphaToDigit(strEndCol);
		}
		if (startRow < 1 || startCol < 1 || endRow < 1 || endCol < 1) {
			// 存在解析失败，或者有非法的行列编号
			try {
				book.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			return;
		}
		// 将行列编号转换为从 0 开始的编号
		--startRow; --startCol; --endRow; --endCol;
		// 检查开始 cell 和结束 cell 是否存在
		try {
			XSSFCell startCell = sheet.getRow(startRow).getCell(startCol);
			XSSFCell endCell = sheet.getRow(endRow).getCell(endCol);
			if (startCell == null || endCell == null) {
				book.close();
				return;
			}
		} catch (NullPointerException e1) {
			// 开始 cell 或结束 cell 不存在
			try {
				book.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return;
		} catch (IOException e2) {
			// startCell == null || endCell == null
			e2.printStackTrace();
			return;
		}
		// 一行，或是一列？
		if (startRow == endRow) {
			// 一行
			int rowIdx = startRow,
				minCol = (startCol < endCol ? startCol : endCol),
				maxCol = (startCol > endCol ? startCol : endCol);
			// 构造数据
			String fileData[] = new String[maxCol - minCol + 1];
			XSSFCell sheetData[] = new XSSFCell[maxCol - minCol + 1];
			for (int i = minCol, k = 0; i <= maxCol; ++i, ++k) {
				sheetData[k] = sheet.getRow(rowIdx).getCell(i);
				CellType type = sheetData[k].getCellTypeEnum();
				if (CellType.NUMERIC != type && CellType.FORMULA != type) {
					// 直接转为字符串
					fileData[k] = sheetData[k].getStringCellValue();
				} else if (CellType.NUMERIC == type) {
					// 从 double 转为字符串
					fileData[k] = Double.toString(sheetData[k].getNumericCellValue());
				} else {
					// 直接显示 formula
					fileData[k] = sheetData[k].getCellFormula();
				}
				fileData[k] = fileData[k].trim();
			}
			// 将数据填充至表格 model
			CompareTableModel model = (CompareTableModel)resultCompareTable.getModel();
			if (0 == fileIdx) {
				model.updateFile1Data(fileData);
			} else if (1 == fileIdx) {
				model.updateFile2Data(fileData);
			}
		} else if (startCol == endCol) {
			// 一列
			int colIdx = startCol,
				minRow = (startRow < endRow ? startRow : endRow),
				maxRow = (startRow > endRow ? startRow : endRow);
			// 构造数据
			String fileData[] = new String[maxRow - minRow + 1];
			XSSFCell sheetData[] = new XSSFCell[maxRow - minRow + 1];
			for (int i = minRow, k = 0; i <= maxRow; ++i, ++k) {
				sheetData[k] = sheet.getRow(i).getCell(colIdx);
				CellType type = sheetData[k].getCellTypeEnum();
				if (CellType.NUMERIC != type && CellType.FORMULA != type) {
					// 直接转为字符串
					fileData[k] = sheetData[k].getStringCellValue();
				} else if (CellType.NUMERIC == type) {
					// 从 double 转为字符串
					fileData[k] = Double.toString(sheetData[k].getNumericCellValue());
				} else {
					// 直接显示 formula
					fileData[k] = sheetData[k].getCellFormula();
				}
				fileData[k] = fileData[k].trim();
			}
			// 将数据填充至表格 model
			CompareTableModel model = (CompareTableModel)resultCompareTable.getModel();
			if (0 == fileIdx) {
				model.updateFile1Data(fileData);
			} else if (1 == fileIdx) {
				model.updateFile2Data(fileData);
			}
		}
		try {
			book.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	// 返回 -1 表示转换失败
	private int alphaToDigit(String alpha) {
		if (alpha.length() == 0) {
			return -1;
		}
		int value = 0;
		for (int chIdx = alpha.length() - 1, i = 0; chIdx >= 0; --chIdx, ++i) {
			int code = alpha.codePointAt(chIdx), digit;
			if (code >= 97 /* a */ && code <= 122 /* z */) {
				digit = code - 97 + 1;
				value += Math.pow(26, i) * digit;
			} else if (code >= 65 /* A */ && code <= 90 /* Z */) {
				digit = code - 65 + 1;
				value += Math.pow(26,  i) * digit;
			} else {
				// 不是字母
				return -1;
			}
		}
		return value;
	}

}

class CompareTableModel extends AbstractTableModel {
	
	private String[] columnNames;
	private String[][] rowData;
	
	public CompareTableModel() {
		columnNames = new String[2];
		columnNames[0] = "文件1";
		columnNames[1] = "文件2";
		rowData = new String[2][];
		rowData[0] = new String[0];
		rowData[1] = new String[0];
	}

	@Override
	public int getRowCount() {
		return rowData[0].length;
	}

	@Override
	public int getColumnCount() {
		return columnNames.length;
	}
	
	@Override
	public String getColumnName(int column) {
		return columnNames[column];
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		return rowData[columnIndex][rowIndex];
	}
	
	public void updateFile1Data(String[] data) {
		if (data.length == rowData[1].length) {
			// 两列大小相等
			rowData[0] = data;
		} else if (data.length < rowData[1].length) {
			// 第一列较小
			rowData[0] = new String[rowData[1].length];
			int i = 0;
			for (; i < data.length; ++i) {
				rowData[0][i] = data[i];
			}
			for (; i < rowData[1].length; ++i) {
				rowData[0][i] = new String("");
			}
		} else {
			// 第二列较小
			rowData[0] = data;
			String[] newData = new String[data.length];
			int i = 0;
			for (; i < rowData[1].length; ++i) {
				newData[i] = rowData[1][i];
			}
			for (; i < newData.length; ++i) {
				newData[i] = new String("");
			}
			rowData[1] = newData;
		}
		fireTableDataChanged();
	}
	
	public void updateFile2Data(String[] data) {
		if (data.length == rowData[0].length) {
			// 两列大小相等
			rowData[1] = data;
		} else if (data.length < rowData[0].length) {
			// 第二列较小
			rowData[1] = new String[rowData[0].length];
			int i = 0;
			for (; i < data.length; ++i) {
				rowData[1][i] = data[i];
			}
			for (; i < rowData[0].length; ++i) {
				rowData[1][i] = new String("");
			}
		} else {
			// 第一列较小
			rowData[1] = data;
			String[] newData = new String[data.length];
			int i = 0;
			for (; i < rowData[0].length; ++i) {
				newData[i] = rowData[0][i];
			}
			for (; i < newData.length; ++i) {
				newData[i] = new String("");
			}
			rowData[0] = newData;
		}
		fireTableDataChanged();
	}
	
}