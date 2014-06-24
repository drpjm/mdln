package edu.gatech.grits.util;

import edu.gatech.grits.pancakes.structures.Packet;



public class RolePacket extends Packet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 488776359425693007L;
	
	public RolePacket(String type) {
		super(type);
	}

	public void addRole(Integer i){
		this.add("role", Integer.valueOf(i));
	}
	public Integer getRole(){
		return Integer.valueOf(this.get("role"));
	}
	
}
