package edu.gatech.grits.util;

import java.util.TimerTask;

public class SimpleTask extends TimerTask {

	private String id;
	private TimerListener listener;
	
	public SimpleTask(String id, TimerListener tl){
		this.id = id;
		this.listener = tl;
	}
	@Override
	public void run() {
//		System.out.println("Hello from " +  this.id);
		listener.onTimer(new TimerEvent(this.id, TimerEvent.TIMER_FIRED, System.currentTimeMillis()));

	}

}
