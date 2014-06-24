package edu.gatech.grits.app;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Timer;

import javax.swing.SwingUtilities;

import javolution.util.FastList;
import javolution.util.FastMap;

import edu.gatech.grits.gui.*;
import edu.gatech.grits.gui.model.*;
import edu.gatech.grits.mdln.lang.util.MDLnMode;
import edu.gatech.grits.mdln.lang.util.MDLnProgram;
import edu.gatech.grits.pancakes.manager.PancakesManager;
import edu.gatech.grits.pancakes.structures.*;
import edu.gatech.grits.pancakes.util.AgentProperties;
import edu.gatech.grits.util.BooleanPacket;
import edu.gatech.grits.util.ModePacket;
import edu.gatech.grits.util.MDLnMessage;
import edu.gatech.grits.util.RolePacket;
import edu.gatech.grits.util.SimpleTask;
import edu.gatech.grits.util.TimerEvent;
import edu.gatech.grits.util.TimerListener;

public class RemoteController implements TimerListener {

	private final String COMM_TIMER = "CommTimer";
	private long lastNetworkUpdate;
	private final long NETWORK_DELAY = 2000;

	//	private final String NET_TIMER = "NetTimer";

	//backend for communicating with pancakes agents
	private PancakesManager pancakes;

	private Timer timer;

//	private FastMap<Integer,NetworkNeighbor> currNeighborhood;
	private FastList<Integer> currNeighborIds;
	private FastMap<Integer,AgentGraphicsState> currNeighborState;


	public RemoteController(AgentProperties props){

		timer = new Timer();
		//gui timer checks for new data from gui
		timer.scheduleAtFixedRate(new SimpleTask(this.COMM_TIMER, this), 500, 100);
		//net timer checks up on the network neighborhood of Pancakes agents
		//		timer.scheduleAtFixedRate(new SimpleTask(this.NET_TIMER, this), 2000, 2000);

		//invoke Pancakes
		try {
			InetAddress ip = InetAddress.getLocalHost();
			pancakes = new PancakesManager(props);
			currNeighborIds = new FastList<Integer>();
			currNeighborState = new FastMap<Integer,AgentGraphicsState>();

		} catch (UnknownHostException e) {
			System.err.println("Error: " + e.getCause());
			System.exit(0);
		}

	}

	public void onTimer(TimerEvent te) {

		if(te.getTimerId().equals(this.COMM_TIMER)){
			//check for new messages
			SyrupPacket msg = pancakes.getNetworkManager().getPacket();
			processMessage(msg);


			if((System.currentTimeMillis() - lastNetworkUpdate) > this.NETWORK_DELAY){
				//check if there are network neighbors yet
				if(pancakes.getNetworkManager().getNetworkNeighborhood() != null){
//					currNeighborhood = pancakes.getNetworkManager().getNetworkNeighborhood();
					currNeighborIds = pancakes.getNetworkManager().getNeighbors();
				}
				lastNetworkUpdate = System.currentTimeMillis();
			}

		}


	}

	public FastList<Integer> getCurrNeighborIds() {
		return currNeighborIds;
	}

	private final void processMessage(SyrupPacket msg){
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
				if(!currNeighborState.containsKey(msg.getSenderID())){
					currNeighborState.remove(msg.getSenderID());
				}				
			}
		}

	}

	public final void sendProgram(FastList<MDLnMode> program, Integer role, /*Boolean isRepeating,*/ Integer targetAgentId){

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
		pancakes.getNetworkManager().sendPacket(targetAgentId, msg);

	}

	public final void sendMessage(String messageType, Integer targetId){

		SyrupPacket msg = new SyrupPacket(messageType, 0);
		pancakes.getNetworkManager().sendPacket(targetId, msg);

	}

	public FastMap<Integer, AgentGraphicsState> getCurrNeighborState() {
		return currNeighborState;
	}


}
