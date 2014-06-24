package edu.gatech.grits.automaton;

import java.awt.Point;
import java.util.Stack;

import edu.gatech.grits.automaton.NetworkState.StateColor;

import javolution.util.FastList;

import automata.*;
import automata.fsa.FSAAlphabetRetriever;
import automata.fsa.FSATransition;
import automata.fsa.FiniteStateAutomaton;

public class NetworkAutomaton extends FiniteStateAutomaton {

	private final String EMPTY = "e";
	
	private FastList<String> markedLanguage;
	
	public NetworkAutomaton(){
		super();
		markedLanguage = new FastList<String>();
	}

	public NetworkState createState(Point p){
		int i = this.getNextId();
		NetworkState netState = new NetworkState(i, p, this);
		this.addState(netState);
		return netState;
	}
	
	/**
	 * This method performs a DFS algorithm on the NetworkAutomaton, keeping track of the transitions 
	 * that lead to marked states.
	 * @return
	 */
	public final void generateMarkedLanguage(){

		for(State s : this.getStates()){
			NetworkState state = (NetworkState) s;
			// start search from inconsistent states
			if(!state.isConsistent()){
				// special case
				if(state.equals(this.getInitialState())){
					this.markedLanguage.add(this.EMPTY);
				}
				Transition[] priorTrans = this.getTransitionsToState(state);
				for(Transition trans : priorTrans){
					Stack<String> transLabelStack = new Stack<String>();
					transLabelStack.push(((FSATransition)trans).getLabel());
					// follow transition back to FROM state
					traverseBack((NetworkState)trans.getFromState(), transLabelStack);
				}
			}
		}
		System.out.println(this.markedLanguage);
		
	}
	
	private final void traverseBack(NetworkState currState, Stack<String> stack){

		Transition[] prevTrans = this.getTransitionsToState(currState);
		if(prevTrans.length > 0){
			for(Transition t : prevTrans){
				stack.push(((FSATransition)t).getLabel());
				traverseBack((NetworkState)t.getFromState(), stack);
			}
		}
		else{
			// done ... we are at initial state
			// load the stack into marked language
			String transString = "";
			// pull from last index
			for(int i = stack.size()-1; i >= 0; i--){
//				System.out.println(stack.get(i));
				transString += stack.get(i);
				if(i > 0){
					transString += ",";
				}
			}
			this.markedLanguage.add(transString);
//			System.out.println(this.markedLanguage);
		}
		stack.pop();
		
	}

	private final FastList<FSATransition> getStateTransitions(NetworkState state) {
		FastList<FSATransition> outTrans = new FastList<FSATransition>();
		
		// collect outgoing transitions from this state
		for(Transition t : this.getTransitions()){
			if(((FSATransition)t).getFromState().getLabel().equals(state.getLabel())){
				// state "owns" the current transition
				outTrans.add((FSATransition)t);
			}
		}
		
		return outTrans;
		
	}
	
	public FastList<String> getMarkedLanguage() {
		return markedLanguage;
	}
	
}
