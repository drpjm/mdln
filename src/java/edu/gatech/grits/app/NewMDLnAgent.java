package edu.gatech.grits.app;

import edu.gatech.grits.pancakes.agent.PancakesAgent;
import edu.gatech.grits.pancakes.util.AgentProperties;
import edu.gatech.grits.pancakes.util.Properties;

public class NewMDLnAgent extends PancakesAgent {

	private MDLnController mdlnCtrl;
	private int myId;
	private boolean isEnabled;
	private AgentProperties myProps;
	
	public NewMDLnAgent(Properties properties, String s) {
		super(properties, s);
		myId = properties.getID();
		isEnabled = false;
	
		myProps = new AgentProperties(s);
		
		// create the MDLn controller
		mdlnCtrl = new MDLnController(this);
		this.addController(mdlnCtrl);
		
	}
	
	public int getMyId() {
		return myId;
	}

	public boolean isEnabled() {
		return isEnabled;
	}

	public void setEnabled(boolean isEnabled) {
		this.isEnabled = isEnabled;
	}

	public AgentProperties getMyProps() {
		return myProps;
	}

}
