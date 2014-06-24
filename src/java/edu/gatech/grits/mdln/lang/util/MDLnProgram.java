package edu.gatech.grits.mdln.lang.util;

import javolution.util.*;

public class MDLnProgram /*extends AbstractMotionProgram*/ {

	private FastMap<String, Integer> agentRoles;	
	private FastList<String> agentIds;
	protected FastList<MDLnMode> modes;
	private FastMap<String, Boolean> repeatingFlags;

	
	/**
	 * 
	 */
	public MDLnProgram() {
		super();
		modes = new FastList<MDLnMode>();
		agentIds = new FastList<String>();
		agentRoles = new FastMap<String, Integer>();
		repeatingFlags = new FastMap<String,Boolean>();
	}

	/**
	 * @param agentIds
	 * @param agentModes
	 * @param agentRoles
	 */
	public MDLnProgram(FastList<String> agentIds,
			FastList<MDLnMode> modes,
			FastMap<String, Integer> agentRoles) {
		super();
		this.agentIds = agentIds;
		this.agentRoles = agentRoles;
		this.modes = modes;
	}

	public FastList<String> getAgentIds() {
		return agentIds;
	}

	public void setAgentIds(FastList<String> agentIds) {
		this.agentIds = agentIds;
	}

	public FastMap<String, Integer> getAgentRoles() {
		return agentRoles;
	}

	public void setAgentRoles(FastMap<String, Integer> agentRoles) {
		this.agentRoles = agentRoles;
	}

	public FastList<MDLnMode> getModes() {
		return modes;
	}

	public void setModes(FastList<MDLnMode> modes) {
		this.modes = modes;
	}

	/**
	 * Method that pulls out the program associated with the agent "id".
	 * @param id
	 * @return
	 */
	public FastList<MDLnMode> extractProgram(String id){
		FastList<MDLnMode> program = new FastList<MDLnMode>();
		
		for(MDLnMode currMode : modes){
			if(currMode.getAgentId().equals(id)){
				program.add(currMode);
			}
		}
		
		return program;
		
	}

	public Boolean isProgramRepeating(String id){
		System.out.println(repeatingFlags);
		return this.repeatingFlags.get(id);
	}

	public FastMap<String, Boolean> getRepeatingFlags() {
		return repeatingFlags;
	}
	
	
	
//	public static void main(String[] args){
//		MDLnProgram test = new MDLnProgram();
//		AbstractMode am = test.getModes().getFirst();
//		System.out.println(am.getClass());
//	}

}
