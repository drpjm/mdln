package edu.gatech.grits.mdln.lang.util;

import javolution.util.FastList;

public class BuddySet {

	private FastList<String> staticBuddies;
	private FastList<BuddyMapAdapter> dynamicBuddies;
	
	public BuddySet(){
		staticBuddies = new FastList<String>();
		dynamicBuddies = new FastList<BuddyMapAdapter>();
	}

	public void setStaticBuddies(FastList<String> staticBuddies) {
		this.staticBuddies = staticBuddies;
	}

	public void setDynamicBuddies(FastList<BuddyMapAdapter> dynamicBuddies) {
		this.dynamicBuddies = dynamicBuddies;
	}

	public FastList<String> getStaticBuddies() {
		return staticBuddies;
	}

	public FastList<BuddyMapAdapter> getDynamicBuddies() {
		return dynamicBuddies;
	}

	@Override
	public String toString() {
		String out = "Static:\n";
		out += staticBuddies;
		out += "\nDynamic:\n";
		out += dynamicBuddies;
		return out;
	}
	
	
}
