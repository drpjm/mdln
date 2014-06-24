package edu.gatech.grits.util;

import edu.gatech.grits.pancakes.structures.SyrupPacket;
import javolution.util.FastList;
import javolution.util.FastMap;

/**
 * A boolean pooling class that holds the communication status of a network neighbor.
 * @author pmartin
 *
 */
public class NeighborStatus {

	private boolean isBuddy;
	private boolean isSharing;
	private boolean isReceiving;
	private boolean requesting;
	public boolean isRequesting() {
		return requesting;
	}

	public void setRequesting(boolean requesting) {
		this.requesting = requesting;
	}

	private int role;
//	private FastMap<String,FastList<SyrupPacket>> targetMsgTable;
//	private FastMap<String, SyrupPacket> messageMap;
	
	/**
	 * 
	 */
	public NeighborStatus() {
		super();
	}
	
	/**
	 * @param isBuddy
	 * @param isSharing
	 */
	public NeighborStatus(boolean isBuddy, boolean isSharing) {
		super();
		this.isBuddy = isBuddy;
		this.isSharing = isSharing;
//		messageMap = new FastMap<String, SyrupPacket>();
//		targetMsgTable = new FastMap<String,FastList<SyrupPacket>>();
	}

	/**
	 * Tells you if the neighbor has acknowledged it is a buddy.
	 * @return
	 */
	public boolean isBuddy() {
		return isBuddy;
	}
	public void setBuddy(boolean isBuddy) {
		this.isBuddy = isBuddy;
	}
	/**
	 * Tells you if the neighbor is sharing information with current agent.
	 * @return
	 */
	public boolean isSharing() {
		return isSharing;
	}
	public void setSharing(boolean isSharing) {
		this.isSharing = isSharing;
	}

	/**
	 * Tells you if the neighbor is able to receive data from current agent. 
	 * @return
	 */
	public boolean isReceiving() {
		return isReceiving;
	}

	public void setReceiving(boolean isReceiving) {
		this.isReceiving = isReceiving;
	}

//	public FastMap<String, SyrupPacket> getMessageMap() {
//		return messageMap;
//	}

	public int getRole() {
		return role;
	}

	public void setRole(int role) {
		this.role = role;
	}

	@Override
	public String toString() {
		String str = "(";
		str += "role=" + this.role + ", ";
		str += "buddy=" + this.isBuddy + ", receiving=" + this.isReceiving + ", sharing=" + this.isSharing;
		str += ")";
		return str;
	}

	
}
