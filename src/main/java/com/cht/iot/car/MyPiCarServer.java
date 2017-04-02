package com.cht.iot.car;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;

import com.cht.iot.util.JsonUtils;
import com.pi4j.wiringpi.Gpio;
import com.pi4j.wiringpi.SoftPwm;

@Service
@ConfigurationProperties(prefix = "car")
public class MyPiCarServer {
	static final Logger LOG = LoggerFactory.getLogger(MyPiCarServer.class);
	
	static final int PWM_MAX = 100;
	
	DatagramSocket socket;
	int port = 10000;
	
	int westForwardPin	= 0;	// pin 11
	int westBackwardPin	= 1;	// pin 12
	int eastForwardPin	= 3;	// pin 15
	int eastBackwardPin	= 4;	// pin 16
	
	ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
	
	int seq = 0;
	Control last;
	
	public MyPiCarServer() throws Exception {
	}
	
	public void setPort(int port) {
		this.port = port;
	}
	
	@PostConstruct
	void start() throws IOException {
		socket = new DatagramSocket(port);
		
		LOG.info("Listen at UDP/{}", port);
		
		Gpio.wiringPiSetup();
		
		SoftPwm.softPwmCreate(westForwardPin, 0, PWM_MAX);
		SoftPwm.softPwmCreate(westBackwardPin, 0, PWM_MAX);
		SoftPwm.softPwmCreate(eastForwardPin, 0, PWM_MAX);
		SoftPwm.softPwmCreate(eastBackwardPin, 0, PWM_MAX);
		
		pause();
		
		ForkJoinPool.commonPool().execute(new Runnable() {
			@Override
			public void run() {
				process();				
			}
		});
	}
	
	@PreDestroy
	void stop() {		
		scheduler.shutdown();
		
		socket.close();
	}
	
	synchronized int newSeq() {
		return ++seq;
	}
	
	void pause() {
		LOG.info("Car is paused");
		
		SoftPwm.softPwmWrite(westForwardPin, 0);
		SoftPwm.softPwmWrite(westBackwardPin, 0);
		SoftPwm.softPwmWrite(eastForwardPin, 0);
		SoftPwm.softPwmWrite(eastBackwardPin, 0);
	}
	
	void exec(Control ctrl) {
		if (ctrl.west > 0) {
			int pwm = Math.min(ctrl.west, PWM_MAX);			
			SoftPwm.softPwmWrite(westForwardPin, pwm);
			
		} else if (ctrl.west < 0) {
			int pwm = Math.min(Math.abs(ctrl.west), PWM_MAX);
			SoftPwm.softPwmWrite(westBackwardPin, pwm);			
		}
		
		if (ctrl.east > 0) {
			int pwm = Math.min(ctrl.east, PWM_MAX);
			SoftPwm.softPwmWrite(eastForwardPin, pwm);
			
		} else if (ctrl.east < 0) {
			int pwm = Math.min(Math.abs(ctrl.east), PWM_MAX);
			SoftPwm.softPwmWrite(eastBackwardPin, pwm);			
		}
	}
	
	void process() {
		byte[] bytes = new byte[4096];
		
		try {		
			for (;;) {
				DatagramPacket pkt = new DatagramPacket(bytes, bytes.length);
				socket.receive(pkt);
				
				try {
					String json = new String(pkt.getData(), 0, pkt.getLength());
					
					final Control ctrl = JsonUtils.fromJson(json, Control.class);
					
					ctrl.seq = newSeq();
					
					if (!ctrl.equals(last)) { // don't pause for duplicated control					
						pause();
					
						LOG.info("Control - {}", json);
					
						exec(ctrl);
					}
					
					last = ctrl;
					
					// stop process after required duration
					scheduler.schedule(new Runnable() {
						@Override
						public void run() {
							if (ctrl.seq == seq) { // no other new command
								pause();
							}						
						}
					}, ctrl.duration, TimeUnit.MILLISECONDS);
					
				} catch (Exception e) {
					LOG.error(e.getMessage(), e);
				}
			}
		} catch (IOException e) {
			LOG.error(e.getMessage()); // socket is closed
		}
	}	
}
