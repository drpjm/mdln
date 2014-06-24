package edu.gatech.grits.automaton;

import java.awt.Point;

import javolution.util.FastList;
import javolution.util.FastMap;

import automata.Automaton;
import automata.State;

public class NetworkState extends State {

	private FastMap<String,FastList<String>> agentBuddyMap;
	private FastMap<String,Integer> agentRoleMap;
	private boolean isConsistent;
	// variable for search algorithms
//	private StateColor color;
	
	public NetworkState(int id, Point point, Automaton automaton) {
		super(id, point, automaton);
		isConsistent = true;
		agentRoleMap = new FastMap<String,Integer>();
		agentBuddyMap = new FastMap<String,FastList<String>>();
//		color = StateColor.WHITE;
	}

	public FastMap<String, FastList<String>> getAgentBuddyMap() {
		return agentBuddyMap;
	}

	public void setAgentBuddyMap(FastMap<String, FastList<String>> agentBuddyMap) {
		this.agentBuddyMap = agentBuddyMap;
	}

	public FastMap<String, Integer> getAgentRoleMap() {
		return agentRoleMap;
	}

	public void setAgentRoleMap(FastMap<String, Integer> agentRoleMap) {
		this.agentRoleMap = agentRoleMap;
	}

	public boolean isConsistent() {
		return isConsistent;
	}

	public void setConsistent(boolean isConsistent) {
		this.isConsistent = isConsistent;
	}


//	public StateColor getColor() {
//		return color;
//	}
//
//	public void setColor(StateColor visitValue) {
//		this.color = visitValue;
//	}

	@Override
	public String toString() {
		String str = super.toString();
		str += ", Consistent?";
		if(isConsistent){
			str += " YES";
		}
		else{
			str += " NO";
		}
		return str;
	}
	
	public enum StateColor{
		WHITE(0),
		GRAY(1),
		BLACK(2);
		
		private int value;
		private StateColor(int i){
			value = i;
		}
	}

}
