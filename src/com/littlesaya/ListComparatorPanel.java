package com.littlesaya;

import java.awt.Color;
import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

import com.littlesaya.pipe.ReferenceReceiver;

import layout.TableLayout;
import layout.TableLayoutConstraints;

@SuppressWarnings("serial")
public class ListComparatorPanel extends JPanel {
	
	// 公有成员变量

	public ReferenceReceiver<List<String>> receiverA = new ReferenceReceiver<List<String>>() {

		@Override
		public void receive(List<String> obj) {
			CompareTableModel model = (CompareTableModel)resultTable.getModel();
			model.updateColumnA(obj);
		}
	};
	
	public ReferenceReceiver<List<String>> receiverB = new ReferenceReceiver<List<String>>() {

		@Override
		public void receive(List<String> obj) {
			CompareTableModel model = (CompareTableModel)resultTable.getModel();
			model.updateColumnB(obj);
		}
	};
	
	// 私有成员变量
	
	private DefaultTableCellRenderer compareRenderer = new DefaultTableCellRenderer() {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
        		int row, int column) {
        	Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        	String valueA = (String)resultTable.getModel().getValueAt(row, 0);
        	String valueB = (String)resultTable.getModel().getValueAt(row, 1);
        	if (valueA.compareTo(valueB) == 0) {
            	cell.setBackground(Color.GREEN);
            	cell.setForeground(Color.BLACK);
        	} else {
        		cell.setBackground(Color.RED);
        		cell.setForeground(Color.BLACK);
        	}
	        return cell;
        }
	};
	
	// 控件

	// 标题栏
	private static final double[][] headPanelLayoutDimension = {
			{ TableLayout.FILL },
			{ TableLayout.FILL }
	};
	private JPanel headPanel;
	private JLabel mainTitle;
	
	// 结果栏
	private static final double[][] resultPanelLayoutDimension = {
			{ TableLayout.FILL },
			{ TableLayout.FILL }
	};
	private JPanel resultPanel;
	private JScrollPane scrollPane;
	private JTable resultTable;
	
	// ListComparatorPanel 自身
	private static final double[][] layoutDimension = {
			{ TableLayout.FILL },
			{ 30, TableLayout.FILL }
	};
	
	// 构造函数
	public ListComparatorPanel() {
		super(new TableLayout(layoutDimension), true);
		initHeadPanel();
		this.add(headPanel, new TableLayoutConstraints(0, 0, 0, 0, TableLayout.FULL, TableLayout.FULL));
		initResultPanel();
		this.add(resultPanel, new TableLayoutConstraints(0, 1, 0, 1, TableLayout.FULL, TableLayout.FULL));
	}
	
	// 私有成员函数
	
	// 控件初始化
	
	// 标题栏
	private void initHeadPanel() {
		headPanel = new JPanel(new TableLayout(headPanelLayoutDimension), true);
		
		mainTitle = new JLabel("比较结果");
		headPanel.add(mainTitle, new TableLayoutConstraints(0, 0, 0, 0, TableLayout.FULL, TableLayout.FULL));
	}
	
	// 结果栏
	private void initResultPanel() {
		resultPanel = new JPanel(new TableLayout(resultPanelLayoutDimension), true);
		
		resultTable = new JTable(new CompareTableModel()) {
			@Override
			public TableCellRenderer getCellRenderer(int row, int col) {
				return compareRenderer;
			}
		};
		scrollPane = new JScrollPane(resultTable);
		
		resultPanel.add(scrollPane, new TableLayoutConstraints(0, 0, 0, 0, TableLayout.FULL, TableLayout.FULL));
	}
}

@SuppressWarnings("serial")
class CompareTableModel extends AbstractTableModel {
	
	private List<String> columnNames = new ArrayList<String>();
	private List<String> columnA = new ArrayList<String>();
	private List<String> columnB = new ArrayList<String>();
	
	public CompareTableModel() {
		columnNames.add("文件A");
		columnNames.add("文件B");
	}

	@Override
	public int getRowCount() {
		return columnA.size();
	}

	@Override
	public int getColumnCount() {
		return 2;
	}
	
	@Override
	public String getColumnName(int column) {
		return columnNames.get(column);
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		return (0 == columnIndex ? columnA.get(rowIndex) : columnB.get(rowIndex));
	}
	
	public void updateColumnA(List<String> newColumnA) {
		columnA = newColumnA;
		padding();
		fireTableDataChanged();
	}
	
	public void updateColumnB(List<String> newColumnB) {
		columnB = newColumnB;
		padding();
		fireTableDataChanged();
	}
	
	private void padding() {
		int diff = columnA.size() - columnB.size();
		if (diff < 0) {
			diff = Math.abs(diff);
			for (int i = 0; i < diff; ++i) {
				columnA.add("");
			}
		} else if (diff > 0) {
			for (int i = 0; i < diff; ++i) {
				columnB.add("");
			}
		}
	}
	
}
