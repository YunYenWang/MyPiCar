package com.cht.iot.car;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class HeartbeatService {
	static final Logger LOG = LoggerFactory.getLogger(HeartbeatService.class);
	
	static final String BROADCAST_ADDRESS = "255.255.255.255";
	static final String BROADCAST_MESSAGE = "HELLO";
	
	DatagramSocket socket;
	InetAddress broadcast;
	int port = 60000;
	
	long period = 2000;
	
	ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
	
	public HeartbeatService() throws Exception {
	}
	
	@PostConstruct
	void start() throws IOException {
		socket = new DatagramSocket();
		socket.setBroadcast(true);
		
		broadcast = InetAddress.getByName(BROADCAST_ADDRESS);
		
		scheduler.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				heartbeat();				
			}
		}, 0, period, TimeUnit.MILLISECONDS);
	}
	
	@PreDestroy
	void stop() {		
		scheduler.shutdown();
		
		socket.close();
	}
	
	void heartbeat() {
		try {		
			byte[] bytes = BROADCAST_MESSAGE.getBytes();
			DatagramPacket pkt = new DatagramPacket(bytes, bytes.length);
			pkt.setAddress(broadcast);
			pkt.setPort(port);
			socket.send(pkt);
			
			if (LOG.isDebugEnabled()) LOG.debug("Send the heartbeat");
			
		} catch (IOException e) {
			LOG.error(e.getMessage()); // socket is closed
		}
	}	
}
