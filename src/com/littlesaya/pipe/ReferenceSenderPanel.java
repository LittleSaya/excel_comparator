package com.littlesaya.pipe;

import java.awt.LayoutManager;
import java.util.List;

import javax.swing.JPanel;

// 带有引用发送功能的 JPanel
// 一个 ReferenceSenderPanel 能够对应多个 ReferencePipe
@SuppressWarnings("serial")
public abstract class ReferenceSenderPanel<Type> extends JPanel {

	private List<ReferencePipe<Type>> refPipes;
	
	// 从 JPanel 继承而来的 4 个构造函数
	protected ReferenceSenderPanel(List<ReferencePipe<Type>> pipes) {
		super();
		this.refPipes = pipes;
	}
	
	protected ReferenceSenderPanel(List<ReferencePipe<Type>> pipes, LayoutManager layout, boolean isDoubleBuffered) {
		super(layout, isDoubleBuffered);
		this.refPipes = pipes;
	}
	
	protected ReferenceSenderPanel(List<ReferencePipe<Type>> pipes, LayoutManager layout) {
		super(layout);
		this.refPipes = pipes;
	}
	
	protected ReferenceSenderPanel(List<ReferencePipe<Type>> pipes, boolean isDoubleBuffered) {
		super(isDoubleBuffered);
		this.refPipes = pipes;
	}
	
	// 发送数据
	protected void sendRef(Type obj) {
		for (ReferencePipe<Type> pipe : refPipes) {
			pipe.receive(obj);
		}
	}
}
