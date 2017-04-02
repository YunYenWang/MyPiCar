package com.cht.iot.car;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import org.junit.Test;

import com.cht.iot.util.JsonUtils;

public class MyPiCarTest {
	
	String host = "192.168.0.174";
	int port = 10000;
	
	void send(Control ctrl) throws IOException {
		DatagramSocket socket = new DatagramSocket();
		
		String json = JsonUtils.toJson(ctrl);
		byte[] bytes = json.getBytes();
		
		DatagramPacket pkt = new DatagramPacket(bytes, bytes.length);
		pkt.setAddress(InetAddress.getByName(host));
		pkt.setPort(port);
		
		socket.send(pkt);
		
		socket.close();
	}
	
	@Test
	public void testForward() throws Exception {
		Control ctrl = new Control();
		ctrl.west = 50;
		ctrl.east = 50;
		ctrl.duration = 3000;
		
		send(ctrl);
	}
	
	@Test
	public void testLoopback() throws Exception {
		Control ctrl = new Control();
		ctrl.west = 50;
		ctrl.east = 50;
		ctrl.duration = 10000;
		send(ctrl);
		
		Thread.sleep(1000);
		
		ctrl.west = 50;
		ctrl.east = 0;
		ctrl.duration = 10000;
		send(ctrl);
		
		Thread.sleep(3000);
		
		ctrl.west = 50;
		ctrl.east = 50;
		ctrl.duration = 3000;
		send(ctrl);
	}
}
