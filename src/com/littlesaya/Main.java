package com.littlesaya;

import java.awt.Container;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import com.littlesaya.pipe.ReferencePipe;

import layout.TableLayout;
import layout.TableLayoutConstraints;

public class Main {
	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				// 设置 comparePanel 为 receiver
				ListComparatorPanel comparePanel = new ListComparatorPanel();
				// filePanelA 通过 pipeA 将输出导向 comparePanel 的 receiverA
				ReferencePipe<List<String>> pipeA = new ReferencePipe<>(comparePanel.receiverA);
				List<ReferencePipe<List<String>>> pipesA = new ArrayList<>();
				pipesA.add(pipeA);
				FilePanel filePanelA = new FilePanel("文件A", pipesA);
				// filePanelB 通过 pipeB 将输出导向 comparePanel 的 receiverB
				ReferencePipe<List<String>> pipeB = new ReferencePipe<>(comparePanel.receiverB);
				List<ReferencePipe<List<String>>> pipesB = new ArrayList<>();
				pipesB.add(pipeB);
				FilePanel filePanelB = new FilePanel("文件B", pipesB);
				
				// 创建 Frame
				JFrame window = new JFrame("Excel Comparator");
				window.setSize(800, 400);
				window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				Container container = window.getContentPane();
				double[][] containerSize = {
						{ 5, 0.3, 5, 0.3, 5, TableLayout.FILL, 5 },
						{ 5, TableLayout.FILL, 5 }
				};
				container.setLayout(new TableLayout(containerSize));
				filePanelA.setBorder(BorderFactory.createEtchedBorder());
				container.add(filePanelA, new TableLayoutConstraints(1, 1, 1, 1, TableLayout.FULL, TableLayout.FULL));
				filePanelB.setBorder(BorderFactory.createEtchedBorder());
				container.add(filePanelB, new TableLayoutConstraints(3, 1, 3, 1, TableLayout.FULL, TableLayout.FULL));
				container.add(comparePanel, new TableLayoutConstraints(5, 1, 5, 1, TableLayout.FULL, TableLayout.FULL));
				
				// 在关闭窗口前释放资源
				window.addWindowListener(new WindowAdapter() {
					@Override
					public void windowClosing(WindowEvent e) {
						filePanelA.releaseResource();
						filePanelB.releaseResource();
						super.windowClosing(e);
					}
				});
				
				window.setVisible(true);
			}
		});
	}

}
