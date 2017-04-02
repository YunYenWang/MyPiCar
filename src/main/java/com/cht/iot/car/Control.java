package com.cht.iot.car;

public class Control {
	int seq;
	
	public int west = 0;
	public int east = 0;
	public long duration = 0;
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Control) {
			Control ctrl = (Control) obj;
			return (west == ctrl.west) && (east == ctrl.east);
		}
		
		return false;
	}
}
