package edu.gatech.grits.gui.model;

import java.awt.geom.*;

import javolution.util.FastList;

public class AgentGraphicsState {

	private FastList<Integer> buddyIds;
	private Integer role;
	private int xLoc;
	private int yLoc;
	private String name;
	
	public AgentGraphicsState(){
		buddyIds = new FastList<Integer>();
		role = 0;
		name = "none";
	}
	/**
	 * @param name
	 * @param role
	 */
	public AgentGraphicsState(String name, Integer role) {
		this.name = name;
		this.role = role;
		buddyIds = new FastList<Integer>();
	}

	/**
	 * @param buddyIds
	 * @param name
	 * @param role
	 */
	public AgentGraphicsState(FastList<Integer> buddyIds,
			String name, Integer role) {
		this.buddyIds = buddyIds;
		this.name = name;
		this.role = role;
	}

	public int getXLoc() {
		return xLoc;
	}
	public void setXLoc(int loc) {
		xLoc = loc;
	}
	public int getYLoc() {
		return yLoc;
	}
	public void setYLoc(int loc) {
		yLoc = loc;
	}
	public FastList<Integer> getBuddyIds() {
		return buddyIds;
	}

	public void setBuddyIds(FastList<Integer> buddyIds) {
		this.buddyIds = buddyIds;
	}

	public Integer getRole() {
		return role;
	}

	public void setRole(Integer role) {
		this.role = role;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	
}
