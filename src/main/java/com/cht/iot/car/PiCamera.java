package com.cht.iot.car;

import java.util.Arrays;

import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cht.iot.util.Future;
import com.github.sarxos.v4l4j.V4L4J;

import au.edu.jcu.v4l4j.CaptureCallback;
import au.edu.jcu.v4l4j.FrameGrabber;
import au.edu.jcu.v4l4j.V4L4JConstants;
import au.edu.jcu.v4l4j.VideoDevice;
import au.edu.jcu.v4l4j.VideoFrame;
import au.edu.jcu.v4l4j.exceptions.V4L4JException;

public class PiCamera {
	static final Logger LOG = LoggerFactory.getLogger(PiCamera.class);
	
	static {
		V4L4J.init();
	}
	
	final VideoDevice device;
	
	int input = 0;	// FIXME - InputInfo.getIndex()
	int standard = V4L4JConstants.STANDARD_WEBCAM; // TODO - NG
	
	final FrameGrabber grabber;
	
	final Future<byte[]> future = new Future<byte[]>();
	
	public PiCamera(String dev, int w, int h, int q) throws V4L4JException {
		device = new VideoDevice(dev);
		grabber = device.getJPEGFrameGrabber(w, h, input, standard, q);
		
        LOG.info("Frame rate: {}", grabber.getFrameInterval());
		
		grabber.setCaptureCallback(new CaptureCallback() {
			@Override
			public void nextFrame(VideoFrame frame) {
				try {
					byte[] snapshot = Arrays.copyOf(frame.getBytes(), frame.getFrameLength()); // FIXME - too much memory usage
					
					if (LOG.isDebugEnabled()) LOG.debug("Next frame - {} bytes", snapshot.length);
				
					future.set(snapshot);
					
				} catch (Exception e) {
					LOG.error(e.getMessage(), e);
				}
				
				frame.recycle();
			}
			
			@Override
			public void exceptionReceived(V4L4JException e) {
				LOG.error(e.getMessage(), e);
				
				future.set(null);
			}
		});
		
		grabber.startCapture();
	}
	
	@PreDestroy
	public void close() {
		grabber.stopCapture();
		
		device.releaseFrameGrabber();
		device.release();
	}
	
	public byte[] capture() throws V4L4JException, InterruptedException {		
		byte[] snapshot = future.get();
		future.reset();
			
		return snapshot;
	}
	
//	public static void main(String[] args) throws Exception {
//		String dev = "/dev/video0";
//		int w = 640;
//		int h = 480;
//		int q = 100;
//		
//		PiCamera pc = new PiCamera(dev, w, h, q);
//		try {		
//			byte[] snapshot = new byte[0];
//			
//			long ctm = System.currentTimeMillis();
//			for (int i = 0;i < 10;i++) {
//				snapshot = pc.capture();
//				
//				LOG.info("Snapshot size is {}", snapshot.length);
//			}
//			LOG.info("elapse: {} ms/frame", (System.currentTimeMillis() - ctm) / 10);
//			
//			java.io.FileOutputStream fos = new java.io.FileOutputStream("/tmp/test.rgb");
//			fos.write(snapshot);
//			fos.flush();
//			fos.close();
//					
//			
//		} finally {
//			pc.close();
//		}
//	}
}
