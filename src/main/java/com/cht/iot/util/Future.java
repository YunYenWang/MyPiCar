package com.cht.iot.util;

public class Future<T> {
	T object;
	
	public Future() {		
	}
	
	public synchronized void reset() {
		object = null;
	}
	
	public synchronized T get() throws InterruptedException {
		if (object == null) {
			wait();
		}
		
		return object;
	}
	
	public synchronized void set(T object) {
		this.object = object;
		
		notify();
	}
}
