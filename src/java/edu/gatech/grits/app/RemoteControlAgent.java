package edu.gatech.grits.app;

import java.util.*;

import javolution.util.*;
import edu.gatech.grits.gui.model.*;
import edu.gatech.grits.mdln.lang.util.*;
import edu.gatech.grits.pancakes.agent.*;
import edu.gatech.grits.pancakes.structures.*;
import edu.gatech.grits.pancakes.util.*;
import edu.gatech.grits.pancakes.util.Properties;
import edu.gatech.grits.util.*;

public class RemoteControlAgent extends PancakesAgent {

	private long lastNetworkUpdate;
	private final long NETWORK_DELAY = 2000;

	private FastMap<Integer,String> agentIdMap;
	private FastList<String> currAgents;
	private FastList<Integer> currNeighborIds;
	private FastMap<Integer,AgentGraphicsState> currNeighborState;
	private int myId;

	private Timer timer;

	public RemoteControlAgent(Properties properties, String s) {
		super(properties, s);
		myId = properties.getID();
		agentIdMap = new FastMap<Integer,String>();
		currAgents = new FastList<String>();
		currNeighborIds = new FastList<Integer>();
		currNeighborState = new FastMap<Integer,AgentGraphicsState>();
		
		timer = new Timer();
		timer.scheduleAtFixedRate(new TimerTask(){

			@Override
			public void run() {
//				System.out.println("RCA: " + Thread.currentThread());
				SyrupPacket msg = getMessage();
				processMessage(msg);

				if((System.currentTimeMillis() - lastNetworkUpdate) > NETWORK_DELAY){
					//check if there are network neighbors yet
					if(getNetworkNeighborhood() != null){
						currNeighborIds = getNeighbors();
						System.out.println(currNeighborIds);
					}
					lastNetworkUpdate = System.currentTimeMillis();
				}
				
			}
			
		}, 500, 100);
		
		AgentProperties myProps = new AgentProperties("controller.prop");
		//hard coded agent map!
		for(FastMap.Entry<String, Integer> e = myProps.getIdMap().head(), end = myProps.getIdMap().tail(); (e = e.getNext())!=end;){
			agentIdMap.put(e.getValue(), e.getKey());
		}

	}

	private final void processMessage(final SyrupPacket msg){
		if(msg != null){
			if(msg.getType().equals(MDLnMessage.UPDATE)){
				// update the current status of this agent
				if(!currNeighborState.containsKey(msg.getSenderID())){
					// TODO: grab buddy list embedded in msg
//					FastList<Integer> buddies = new FastList<Integer>();
//					for(int i = 0; i < msg.getSize(); i++){
//
//					}
					currNeighborState.put(msg.getSenderID(), new AgentGraphicsState());
				}
			}
			else if(msg.getType().equals(MDLnMessage.KILL)){
				System.out.println("Agent" + msg.getSenderID() +  " dead.");
				if(currNeighborState.containsKey(msg.getSenderID())){
					System.out.println("...remove.");
					currNeighborState.remove(msg.getSenderID());
				}				
			}
		}

	}

	public final void sendProgram(final FastList<MDLnMode> program, final Integer role, /*Boolean isRepeating,*/ final Integer targetAgentId){

		SyrupPacket msg = new SyrupPacket(MDLnMessage.PROGRAM, 0);

		//embed the agent's role
		RolePacket rp = new RolePacket("AgentRole");
		rp.addRole(role);
		msg.addPacket(rp);
//		BooleanPacket bp = new BooleanPacket("Repeating");
//		bp.addBoolean(isRepeating);
//		msg.addPacket(bp);
		
		//construct the motion program packet for the target agent
		for(MDLnMode mode : program){
			//			ModePacket pkt = new ModePacket(mode.getId());
			ModePacket pkt = new ModePacket(mode.getClass().getSimpleName());
			pkt.setMode(mode);
			msg.addPacket(pkt);
		}

		//transmit!
		sendMessage(targetAgentId, msg);

	}
	
	public FastMap<Integer, String> getAgentIdMap() {
		return agentIdMap;
	}

	public FastList<String> getCurrAgents() {
		return currAgents;
	}

	public FastList<Integer> getCurrNeighborIds() {
		return currNeighborIds;
	}

	public FastMap<Integer, AgentGraphicsState> getCurrNeighborState() {
		return currNeighborState;
	}

	public int getMyId() {
		return myId;
	}

}
