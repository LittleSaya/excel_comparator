package com.littlesaya.pipe;

// 引用传递管道
public class ReferencePipe<Type> {
	
	// 引用接收器
	private ReferenceReceiver<Type> receiver;
	
	public ReferencePipe(ReferenceReceiver<Type> receiver) {
		this.receiver = receiver;
	}

	public void receive(Type obj) {
		send(obj);
	}
	
	private void send(Type obj) {
		receiver.receive(obj);
	}
}
