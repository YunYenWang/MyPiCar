package com.cht.iot.car;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ForkJoinPool;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class PiCameraServer {
	static final Logger LOG = LoggerFactory.getLogger(PiCamera.class);
	
	PiCamera camera;
	String dev = "/dev/video0";
	int width = 800;
	int height = 480;
	int quality = 80;
	
	ServerSocket server;
	int port = 20000;

	public PiCameraServer() {
	}
	
	@PostConstruct
	void start() throws Exception {
		camera = new PiCamera(dev, width, height, quality);
		server = new ServerSocket(port);
		
		LOG.info("Camera - dev: {}, {}x{}, q: {}%", dev, width, height, quality);
		LOG.info("Camera server listens at {}", port);
		
		ForkJoinPool.commonPool().execute(new Runnable() {
			@Override
			public void run() {
				process();				
			}
		});
	}
	
	@PreDestroy
	void stop() throws IOException {
		server.close();
		camera.close();
	}
	
	void process() {
		try {
			for (;;) {
				Socket sck = server.accept();
				
				LOG.info("Connection from {}:{}", sck.getInetAddress().getHostAddress(), sck.getPort());
				
				try {
					try {
						OutputStream os = sck.getOutputStream();
						DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(os));
						
						while (!sck.isInputShutdown()) {
							byte[] snapshot = camera.capture();
							if (snapshot != null) {						
								dos.writeInt(snapshot.length);
								dos.write(snapshot);
								dos.flush();
							}
						}
						
					} finally {
						sck.close();
					}
					
				} catch (Exception e) {
					LOG.error(e.getMessage(), e);
				}
			}
		} catch (IOException e) {
			LOG.error(e.getMessage()); // socket is closed
		}
	}
}
