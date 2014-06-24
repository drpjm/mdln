package edu.gatech.grits.util;

import java.util.StringTokenizer;

import javolution.util.*;
import edu.gatech.grits.mdln.buddymap.Closest;
import edu.gatech.grits.mdln.lang.util.*;
import edu.gatech.grits.pancakes.structures.Packet;


/**
 * An XML packet used to transmit MDLn modes within a Pancakes system.
 * @author pmartin
 *
 */
public class ModePacket extends Packet {

	private static final long serialVersionUID = 1595579931035106170L;
	
	private final String CTRL_TAG = "control";
	private final String INT_TAG = "interrupt";
	private final String S_BUD_TAG = "s-buddies";
	private final String D_BUD_TAG = "d-buddies";


	public ModePacket(String type) {
		super(type);
	}

	public void setMode(MDLnMode mode){
		this.addAgentId(mode.getAgentId());
		this.addController(mode.getControl());
		this.addInterrupt(mode.getInterrupt());
		this.addTimer(mode.getTimerLength());
		this.addBuddies(mode.getBuddySet());
	}
	
	public MDLnMode extractMode(){
		MDLnMode mode = new MDLnMode();
		
		mode.setAgentId(this.getAgentId());
		mode.setControl(this.getController());
		mode.setInterrupt(this.getInterrupt());
		mode.setTimerLength(this.getTimer());
		
		// get static buddies
		mode.setBuddySet(this.getBuddies());
		
		return mode;
	}
	
	//Agent ID element
	private void addAgentId(String agentId){
		this.add("agentId", agentId);
	}
	private String getAgentId(){
		return this.get("agentId");
	}
	
	//Controller element
	private void addController(ControlAdapter c){
		this.add(this.CTRL_TAG, c.getClass().getName());
	}
	private ControlAdapter getController(){
		ControlAdapter ca = null;
		try {
			ca = (ControlAdapter) Class.forName(this.get(this.CTRL_TAG)).newInstance();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return ca;
		
		 
	}

	//Interrupt element
	private void addInterrupt(InterruptAdapter i){
		this.add(this.INT_TAG, i.getClass().getName());
	}
	private InterruptAdapter getInterrupt(){
		InterruptAdapter ia = null;
		
		try {
			ia = (InterruptAdapter) Class.forName(this.get(this.INT_TAG)).newInstance();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		return ia;
	}
	
	//Timer element
	private void addTimer(long timerVal){
		this.add("timer", Long.valueOf(timerVal));
	}
	private long getTimer(){
		return Long.valueOf(this.get("timer")).longValue();
	}
	
	// Buddies!
	
	private void addBuddies(BuddySet buddies){
		// add static with S tag
		this.add(S_BUD_TAG, buddies.getStaticBuddies().toString());
		
		// add dynamic with D tag
		FastList<String> dynBuddyMaps = new FastList<String>();
		for(BuddyMapAdapter bma : buddies.getDynamicBuddies()){
			dynBuddyMaps.add(bma.getClass().getName());
		}
		this.add(D_BUD_TAG, dynBuddyMaps.toString());
		
	}
	
	private BuddySet getBuddies(){
		BuddySet buddies = new BuddySet();
		
		StringTokenizer st = new StringTokenizer(this.get(S_BUD_TAG), "[],");
		while(st.hasMoreTokens()){
			String next = st.nextToken().trim();
			buddies.getStaticBuddies().add(next);
		}
		
		// dynamic buddies
		StringTokenizer st2 = new StringTokenizer(this.get(D_BUD_TAG), "[],");
		while(st2.hasMoreTokens()){
			String adapterName = st2.nextToken().trim();
			try {
				
				BuddyMapAdapter bma = (BuddyMapAdapter) Class.forName(adapterName).newInstance();
				buddies.getDynamicBuddies().add(bma);
				
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
		
		
		return buddies;
	}
	
	public static void main(String[] args){
		
		MDLnMode testMode = new MDLnMode();
		
		FastList<String> staticBuds = new FastList<String>();
		staticBuds.add("Agent1");
		staticBuds.add("Agent2");
		FastList<BuddyMapAdapter> dynBuds = new FastList<BuddyMapAdapter>();
		dynBuds.add(new Closest());
		
		BuddySet bs = new BuddySet();
		bs.setStaticBuddies(staticBuds);
		bs.setDynamicBuddies(dynBuds);
		ModePacket mp = new ModePacket("mode");
		mp.addBuddies(bs);
		mp.debug();
		System.out.println("Reconstructed mode:");
		System.out.println(mp.getBuddies());
		
	}
}
