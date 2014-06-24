package edu.gatech.grits.gui.model;

public enum AgentState {

	RUNNING(0),
	STOPPED(1),
	ACTIVE(2),
	DEAD(3),
	READY(4);
	
	private int state;
	private AgentState(int s){
		state = s;
	}
	public int getState() {
		return state;
	}
	
}
