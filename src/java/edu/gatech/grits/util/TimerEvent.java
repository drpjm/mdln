package edu.gatech.grits.util;

public class TimerEvent {
	
	public static int TIMER_DEAD = 0;
	public static int TIMER_FIRED = 1;
	
	private String timerId;
	private int condition;
	private long time;
	
	public TimerEvent(){
		timerId = "null";
	}

	public TimerEvent(String timerId, int condition) {
		super();
		this.condition = condition;
		this.timerId = timerId;
	}

	public TimerEvent(String timerId, int condition, long time) {
		this();
		this.timerId = timerId;
		this.condition = condition;
		this.time = time;
	}

	public int getCondition() {
		return condition;
	}

	public void setCondition(int condition) {
		this.condition = condition;
	}

	public String getTimerId() {
		return timerId;
	}

	public void setTimerId(String timerId) {
		this.timerId = timerId;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

	@Override
	public String toString() {
		String ret = "";
		ret += timerId + " - ";
		switch(condition){
		case 1: ret += "FIRED"; break;
		case 0: ret += "DEAD"; break;
		}
		return ret;
	}
	
	
}
