package edu.gatech.grits.mdln.lang.util;

import javolution.util.FastList;
import javolution.xml.*;
import javolution.xml.XMLFormat.OutputElement;
import javolution.xml.stream.*;

public class MDLnMode {

	
//	protected String id;
	protected ControlAdapter control;
	protected long timerLength;
	private String agentId;
	private InterruptAdapter interrupt;
	private BuddySet buddySet;
	
	public MDLnMode(){
//		id = "dummy";
		agentId = "Nobody";
		buddySet = new BuddySet();
		timerLength = AbstractMode.INF_TIMER;
	}
	
//	public String getId() {
//		return id;
//	}
//
//	public void setId(String id) {
//		this.id = id;
//	}

	public ControlAdapter getControl() {
		return control;
	}

	public void setControl(ControlAdapter control) {
		this.control = control;
	}

	public long getTimerLength() {
		return timerLength;
	}

	public void setTimerLength(long timerLength) {
		this.timerLength = timerLength;
	}

	public String getAgentId() {
		return agentId;
	}

	public void setAgentId(String agentId) {
		this.agentId = agentId;
	}

	public InterruptAdapter getInterrupt() {
		return interrupt;
	}
	public void setInterrupt(InterruptAdapter interrupt) {
		this.interrupt = interrupt;
	}
//	public FastList<BuddySet> getBuddyList() {
//		return buddyList;
//	}
//	public void setBuddyList(FastList<BuddySet> buddyList) {
//		this.buddyList = buddyList;
//	}

	public FastList<String> getStaticBuddies(){
		return buddySet.getStaticBuddies();
	}
	
	public BuddySet getBuddySet() {
		return buddySet;
	}

	public void setBuddySet(BuddySet buddySet) {
		this.buddySet = buddySet;
	}

	public FastList<BuddyMapAdapter> getDynamicBuddies(){
		return buddySet.getDynamicBuddies();
	}
	
	@Override
	public String toString() {
		
		String outStr = "(" + agentId + ",";
		
		//check for valid control and interrupt
		if(this.control == null){
			outStr += "NO_CTRL,";
		}
		else{
			outStr += this.control.getClass().getSimpleName() + ",";
		}
		if(this.interrupt == null){
			outStr += "NO_INT";
		}
		else{
			outStr += this.interrupt.getClass().getSimpleName();
		}
		
		//check for timer
		if(this.timerLength != AbstractMode.INF_TIMER){
			outStr += " [" + this.timerLength + "]";
		}
		outStr += "," + this.buddySet;
		outStr += ")";
		
		return outStr;
	}

}
