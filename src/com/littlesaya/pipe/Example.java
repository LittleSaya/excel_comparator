package com.littlesaya.pipe;

import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import layout.TableLayout;
import layout.TableLayoutConstraints;

public class Example {

	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				Receiver receiver = new Receiver();
				List<ReferencePipe<String>> pipe1 = new ArrayList<>();
				pipe1.add(new ReferencePipe<String>(receiver.refRecvLeft));
				Sender sender1 = new Sender(pipe1);
				List<ReferencePipe<String>> pipe2 = new ArrayList<>();
				pipe2.add(new ReferencePipe<String>(receiver.refRecvRight));
				Sender sender2 = new Sender(pipe2);
				
				JFrame f = new JFrame("test");
				f.setSize(800, 400);
				f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				Container container = f.getContentPane();
				double[][] containerSize = {
						{ 0.3, 0.3, 0.3 },
						{ TableLayout.FILL }
				};
				container.setLayout(new TableLayout(containerSize));
				container.add(sender1, new TableLayoutConstraints(0, 0, 0, 0, TableLayout.FULL, TableLayout.FULL));
				container.add(sender2, new TableLayoutConstraints(1, 0, 1, 0, TableLayout.FULL, TableLayout.FULL));
				container.add(receiver, new TableLayoutConstraints(2, 0, 2, 0, TableLayout.FULL, TableLayout.FULL));
				
				f.setVisible(true);
			}
		});
	}

}

@SuppressWarnings("serial")
class Sender extends ReferenceSenderPanel<String> {
	
	private static final double[][] layoutDimension = {
			{ TableLayout.FILL },
			{ TableLayout.FILL }
	};
	
	private JButton button;

	protected Sender(List<ReferencePipe<String>> pipes) {
		super(pipes, new TableLayout(layoutDimension), true);
		button = new JButton("时间");
		button.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				String str = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS").format(new Date());
				sendRef(str);
			}
		});
		this.add(button, new TableLayoutConstraints(0, 0, 0, 0, TableLayout.CENTER, TableLayout.CENTER));
	}
	
}

@SuppressWarnings("serial")
class Receiver extends JPanel {
	
	private static final double[][] layoutDimension = {
			{ 0.5, 0.5 },
			{ TableLayout.FILL }
	};
	
	private JLabel labelLeft;
	private JLabel labelRight;
	
	public Receiver() {
		super(new TableLayout(layoutDimension), true);
		labelLeft = new JLabel();
		this.add(labelLeft, new TableLayoutConstraints(0, 0, 0, 0, TableLayout.CENTER, TableLayout.CENTER));
		labelRight = new JLabel();
		this.add(labelRight, new TableLayoutConstraints(1, 0, 1, 0, TableLayout.CENTER, TableLayout.CENTER));
	}
	
	public ReferenceReceiver<String> refRecvLeft = new ReferenceReceiver<String>() {
		
		@Override
		public void receive(String obj) {
			labelLeft.setText(obj);
		}
	};
	
	public ReferenceReceiver<String> refRecvRight = new ReferenceReceiver<String>() {
		
		@Override
		public void receive(String obj) {
			labelRight.setText(obj);
		}
	};
	
}
