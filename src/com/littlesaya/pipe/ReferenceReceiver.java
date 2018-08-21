package com.littlesaya.pipe;

// 引用接收器
public abstract interface ReferenceReceiver<Type> {
	
	public void receive(Type obj);
}
