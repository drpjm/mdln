package edu.gatech.grits.util;

import edu.gatech.grits.pancakes.structures.Packet;


public class BooleanPacket extends Packet {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1482527892666848653L;

	public BooleanPacket(String type) {
		super(type);
	}

	public void addBoolean(Boolean b){
		this.add("boolean", Boolean.valueOf(b));
	}
	
	public Boolean getBoolean(){
		return Boolean.parseBoolean(this.get("boolean"));
	}
}
