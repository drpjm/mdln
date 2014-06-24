package edu.gatech.grits.mdln.lang;

import java.awt.Point;
import java.io.File;
import java.util.StringTokenizer;


import automata.*;
import automata.fsa.*;

import javolution.util.*;
import edu.gatech.grits.automaton.NetworkAutomaton;
import edu.gatech.grits.automaton.NetworkState;
import edu.gatech.grits.mdln.analysis.*;
import edu.gatech.grits.mdln.lang.util.*;
import edu.gatech.grits.mdln.node.*;


public class MDLn extends DepthFirstAdapter {

	/*
	 * Compiler storage objects; used for parser storage for eventual compilation processing.
	 */
	//	private FastMap<String, FastList<MDLnMode>> agentModeMap;
	private FastList<MDLnMode> modes;
	private FastList<String> agentIds;
	private FastMap<String,Integer> agentRoles;

	private MDLnProgram mdlnProgram;

	//mode building objects
	private String currentAgentId;
//	private FastList<String> currBuddyList;
	private FastList<BuddySet> currBuddyList;
	
	private MDLnMode currMode;
	private String controlId;
	private String interruptId;
	private int modeCounter;
	private int transitionCounter = 0;

	private FastMap<String,Integer> modeCounters;

	private final String PATH_ROOT = System.getProperty("user.dir") + "/src/java";
	private final String PATH_CONTROL = "/edu/gatech/grits/mdln/control";
	private final String PATH_INTERRUPT = "/edu/gatech/grits/mdln/interrupt";
	private final String PATH_BUDMAP = "/edu/gatech/grits/mdln/buddymap";

	private final String CLASSPATH_CONTROL = "edu.gatech.grits.mdln.control";
	private final String CLASSPATH_INTERRUPT = "edu.gatech.grits.mdln.interrupt";
	private final String CLASSPATH_BUDMAP = "edu.gatech.grits.mdln.buddymap";

	private FastList<String> availableControls;
	private FastList<String> availableInterrupts;
	private FastList<String> availableBuddymaps;

	public MDLn(){
		//		this.agentModeMap = new FastMap<String, FastList<MDLnMode>>();
		this.agentIds = new FastList<String>();
		this.availableControls = new FastList<String>();
		this.availableInterrupts = new FastList<String>();
		this.availableBuddymaps = new FastList<String>();

		//search control and interrupt paths
		File controlDir = new File(PATH_ROOT + PATH_CONTROL);
		File interruptDir = new File(PATH_ROOT + PATH_INTERRUPT);
		File buddyDir = new File(PATH_ROOT + PATH_BUDMAP);

		for(File f : controlDir.listFiles()){
			if(f.getName().endsWith(".java")){
				StringTokenizer st = new StringTokenizer(f.getName(), ".");
				availableControls.add(st.nextToken());
			}
		}
		for(File f : interruptDir.listFiles()){
			if(f.getName().endsWith(".java")){
				StringTokenizer st = new StringTokenizer(f.getName(), ".");
				availableInterrupts.add(st.nextToken());
			}
		}
		for(File f : buddyDir.listFiles()){
			if(f.getName().endsWith(".java")){
				StringTokenizer st = new StringTokenizer(f.getName(), ".");
				availableBuddymaps.add(st.nextToken());
			}
		}
		
	}

	@Override
	public void inAMdln(AMdln node) {
		System.out.println("Parsing MDLn program...");
		modeCounters = new FastMap<String,Integer>();
		agentRoles = new FastMap<String, Integer>();
		modes = new FastList<MDLnMode>();
		this.mdlnProgram = new MDLnProgram();
	}

	@Override
	public void inADynamicRole(ADynamicRole node) {
		super.inADynamicRole(node);
	}

	@Override
	public void inAStaticRole(AStaticRole node) {		
		agentRoles.put(node.getAgentName().toString().trim(), new Integer(node.getNumber().toString().trim()));
	}

	@Override
	public void outAModelist(AModelist node) {
		if(node.getEllipsis() != null){
			mdlnProgram.getRepeatingFlags().put(currentAgentId, true);
		}
		else{
			mdlnProgram.getRepeatingFlags().put(currentAgentId, false);
		}
	}

	@Override
	public void inAMode(AMode node) {
		//initialize buddy list
		this.currentAgentId = node.getAgentName().toString().trim();
		if(!this.agentIds.contains(this.currentAgentId)){
			this.agentIds.add(this.currentAgentId);
			this.modeCounters.put(currentAgentId, 0);
		}
		this.currBuddyList = new FastList<BuddySet>();
		this.currMode = new MDLnMode();
		currMode.setAgentId(currentAgentId);
	}

	@Override
	public void outAMode(AMode node) {
		Integer count = this.modeCounters.get(currentAgentId);
		count++;
		this.modeCounters.put(currentAgentId, count);

		this.modes.add(currMode);

	}

	@Override
	public void outAControl(AControl node) {

		//make control id lowercase for filesystem lookup
		this.controlId = node.getControlName().getText().toLowerCase();
		boolean isCtrlFound = false;

		//search controls
		for(String id : this.availableControls){
			if(id.toLowerCase().equals(controlId)){
				//				System.out.println(controlId + " is available.");
				//create control
				try {
					ControlAdapter ctrl = (ControlAdapter) Class.forName(this.CLASSPATH_CONTROL + "." + id).newInstance();
					this.currMode.setControl(ctrl);
					isCtrlFound = true;

				} catch (InstantiationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				break;
			}
		}
		if(!isCtrlFound){
			System.err.println("ERROR: " + this.controlId + " controller not found!");
		}
	}

	@Override
	public void outAInterrupt(AInterrupt node) {

		//make interrupt id lowercase for filesystem lookup
		this.interruptId = node.getInterruptName().getText().toLowerCase();
		boolean isIntFound = false;

		//search for interrupt function
		for(String id : availableInterrupts){
			if(id.toLowerCase().equals(interruptId)){
				//				System.out.println(interruptId + " is available.");
				try {
					InterruptAdapter interrupt = (InterruptAdapter) Class.forName(this.CLASSPATH_INTERRUPT + "." + id).newInstance();
					this.currMode.setInterrupt(interrupt);
					isIntFound = true;
				} catch (InstantiationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				break;
			}
		}
		if(!isIntFound){
			System.err.println("ERROR: " + this.interruptId + " interrupt not found!");
		}

	}


	@Override
	public void outATimer(ATimer node) {
		String timer = node.getNumber().getText().trim();
		// timer is SPECIFIED in SECONDS
		currMode.setTimerLength(Long.valueOf(timer));
	}

	@Override
	/*
	 * This method processes the data in the buddy list.
	 */
	public void inABuddylist(ABuddylist node) {
		
		BuddySet currSet = new BuddySet();
		for(TIdentifier t : node.getBuddy()){
			
			String buddy = t.getText().trim();
			// check for buddy map match
			for(String mapName : this.availableBuddymaps){
				if(buddy.toLowerCase().equals(mapName.toLowerCase())){
					try {
						BuddyMapAdapter budMap = (BuddyMapAdapter) Class.forName(this.CLASSPATH_BUDMAP + "." + buddy).newInstance();
						currSet.getDynamicBuddies().add(budMap);
						
					} catch (InstantiationException e) {
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					} catch (ClassNotFoundException e) {
						e.printStackTrace();
					}
				}
				else{
					currSet.getStaticBuddies().add(buddy);
					
				}
			}
		}
		currMode.setBuddySet(currSet);
	}

	@Override
	public void outAMdln(AMdln node) {
		System.out.println("Parsing complete...");
		mdlnProgram.setModes(modes);
		mdlnProgram.setAgentRoles(agentRoles);
		mdlnProgram.setAgentIds(agentIds);

	}

	/**
	 * Method that invokes the "compilation" of the MDLn file. It parses the given file and then
	 * checks all generated objects for program consistency.
	 */
	public final void compile(){

//		long time1 = System.currentTimeMillis();
		FastMap<String, NetworkAutomaton> agentAutomataMap = new FastMap<String, NetworkAutomaton>();
		// Construct automata for each agent's program
		for(String id : agentIds){
			FastList<MDLnMode> programModes = mdlnProgram.extractProgram(id);
			// for each mode, look at buddy list and create an automata from it
			NetworkAutomaton na = new NetworkAutomaton();
			NetworkState lastState = null;
			for(MDLnMode mode : programModes){

				FastList<String> buddies = mode.getStaticBuddies();
				NetworkState ns = na.createState(new Point(0,0));
				ns.getAgentBuddyMap().put(id, buddies);
				ns.getAgentRoleMap().put(id, agentRoles.get(id));
				ns.setConsistent(true);
				ns.setLabel(String.valueOf(ns.getID()));
				if(ns.getID() == 0){
					na.setInitialState(ns);
				}
				// add transition
				if(lastState != null){
					FSATransition trans = new FSATransition(lastState, ns, String.valueOf(transitionCounter));
					na.addTransition(trans);
					transitionCounter++;
				}
				lastState = ns;
			}
			agentAutomataMap.put(id, na);
		}

		// With the automata map formed, now perform parallel compositions
		NetworkAutomaton composition = null;
		if(agentAutomataMap.size() > 1){
			for(FastMap.Entry<String, NetworkAutomaton> curr = agentAutomataMap.head(), end = agentAutomataMap.tail(); (curr = curr.getNext()) != end;){
				// if the composition has not started, initialize
				if(composition == null){
					composition = compose(curr.getValue(), curr.getNext().getValue());
				}
				else{
					if(curr.getNext().getValue() != null){
						composition = compose(composition, curr.getNext().getValue());
					}
				}			
			}
//			long time2 = System.currentTimeMillis();
//			System.out.println((time2-time1));
			
			// mark inconsistent states
			findInconsistencies(composition);
			System.out.println(composition);
			composition.generateMarkedLanguage();
			
			if(composition.getMarkedLanguage().isEmpty()){
				System.out.println("Program is consistent!");
			}
			else{
				System.err.println("Program is inconsistent!");
			}
		}
		else{
			// no compiling needed!
		}
	}

	/**
	 * This function performs parallel composition on two NetworkAutomata. However, this is not
	 * a general method for ANY Automata. NetworkAutomata have a sequential structure with single
	 * transitions between states.
	 * @param auto1
	 * @param auto2
	 * @return
	 */
	private final NetworkAutomaton compose(NetworkAutomaton auto1, NetworkAutomaton auto2){

		//TODO: this whole function is ugly!!! Is there a cleaner way?

		State[] auto1States = auto1.getStates();
		State[] auto2States = auto2.getStates();
		Transition[] auto1Transitions = auto1.getTransitions();
		Transition[] auto2Transitions = auto2.getTransitions();

		NetworkAutomaton na = new NetworkAutomaton();

		for(int i = 0; i < auto1States.length; i++){

			NetworkState currAuto1State = (NetworkState) auto1States[i];
			String currLabel = currAuto1State.getLabel();

			for(int j = 0; j < auto2States.length; j++){
				NetworkState currAuto2State = (NetworkState)auto2States[j];
				String nextLabel = currAuto2State.getLabel();

				// generate new state
				NetworkState composedState = na.createState(new Point(0,0));
				if(currAuto1State.getID() == 0 && currAuto2State.getID() == 0){
					na.setInitialState(composedState);
				}

				// load buddy maps
				FastMap<String, FastList<String>> newBuddyMap = new FastMap<String, FastList<String>>(); 
				newBuddyMap.putAll(currAuto1State.getAgentBuddyMap());
				newBuddyMap.putAll(currAuto2State.getAgentBuddyMap());
				composedState.setAgentBuddyMap(newBuddyMap);
				// load roles
				FastMap<String,Integer> newRoleMap = new FastMap<String,Integer>();
				newRoleMap.putAll(currAuto1State.getAgentRoleMap());
				newRoleMap.putAll(currAuto2State.getAgentRoleMap());
				composedState.setAgentRoleMap(newRoleMap);
				// create label to keep track of which automata
				composedState.setLabel(currLabel+","+nextLabel);
			}
		}

		// build transitions from auto1 first -- hence we fix the 2nd token
		for(Transition t : auto1Transitions){
			FSATransition trans1 = (FSATransition)t;

			String fromLabel = trans1.getFromState().getLabel();
			String toLabel = trans1.getToState().getLabel();
			// look for composed automata states that match!
			for(int k = 0; k < na.getStates().length; k++){
				String compStateLabel = na.getStates()[k].getLabel();
				StringTokenizer stFixed = new StringTokenizer(compStateLabel, ",");
				String token1 = stFixed.nextToken();
				String token2 = stFixed.nextToken();

				if(token1.equals(fromLabel)){
					// create a new transition
					State targetState = findStateWithLabel(na, toLabel, token2);
					if(targetState != null){
						FSATransition currTrans = new FSATransition(na.getStates()[k], targetState, trans1.getLabel());
						na.addTransition(currTrans);
					}
				}
			}
		}

		// repeat for the transitions in auto2
		for(Transition t : auto2Transitions){

			FSATransition trans2 = (FSATransition)t;

			String fromLabel = trans2.getFromState().getLabel();
			String toLabel =  trans2.getToState().getLabel();
			for(int k = 0; k < na.getStates().length; k++){

				String compStateLabel = na.getStates()[k].getLabel();
				StringTokenizer stFixed = new StringTokenizer(compStateLabel, ",");
				String token1 = stFixed.nextToken();
				String token2 = stFixed.nextToken();

				if(token2.equals(fromLabel)){
					// create new transition
					State targetState = findStateWithLabel(na, token1, toLabel);
					if(targetState != null){
						FSATransition currTrans = new FSATransition(na.getStates()[k], targetState, trans2.getLabel());
						na.addTransition(currTrans);
					}
				}
			}
		}

		// merge labels of composed automaton
		for(State s : na.getStates()){
			String currLabel = s.getLabel();
			StringTokenizer st = new StringTokenizer(currLabel, ",");
			String newLabel = "";
			while(st.hasMoreTokens()){
				newLabel += st.nextToken();
			}
			s.setLabel(newLabel);
		}

		return na;
	}

	private final State findStateWithLabel(NetworkAutomaton auto, String token1, String token2){

		String label = token1 + "," + token2;
		for(State s : auto.getStates()){
			if(s.getLabel().equals(label)){
				return s;
			}
		}
		return null;

	}

	/**
	 * Marks the inconsistent states of the NetworkAutomaton.
	 * @param na
	 * @return
	 */
	private final NetworkAutomaton findInconsistencies(NetworkAutomaton na){

		for(State s : na.getStates()){

			NetworkState currNs = (NetworkState) s;
			if(currNs.getID() == 1){
				System.out.println(currNs);
			}
			// check consistency...
			for(FastMap.Entry<String, FastList<String>> buddyEntry = currNs.getAgentBuddyMap().head(), t = currNs.getAgentBuddyMap().tail(); (buddyEntry=buddyEntry.getNext())!=t;){
				// test role value
				Integer currRole = currNs.getAgentRoleMap().get(buddyEntry.getKey());
				String currName = buddyEntry.getKey();
				for(String buddyName : buddyEntry.getValue()){
					Integer buddyRole = currNs.getAgentRoleMap().get(buddyName);
					if(currRole < buddyRole){
						if(!currNs.getAgentBuddyMap().get(buddyName).contains(currName)){
							// since we are not in their buddy list -> INCONSISTENT!
							currNs.setConsistent(false);
							na.addFinalState(currNs);
						}
					}
				}
			}
			
		}

		return na;
	}

	public MDLnProgram getMdlnProgram() {
		return mdlnProgram;
	}

}
