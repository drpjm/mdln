package edu.gatech.grits.app;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

import sun.tools.tree.ThisExpression;

import edu.gatech.grits.gui.*;
import edu.gatech.grits.gui.ObserverPacket.MessageType;
import edu.gatech.grits.gui.model.*;
import edu.gatech.grits.mdln.lang.util.MDLnMode;
import edu.gatech.grits.mdln.lang.util.MDLnProgram;
import edu.gatech.grits.pancakes.agent.PancakesAgent;
import edu.gatech.grits.pancakes.structures.SyrupPacket;
import edu.gatech.grits.pancakes.util.AgentProperties;
import edu.gatech.grits.pancakes.util.Properties;
import edu.gatech.grits.util.MDLnMessage;
import edu.gatech.grits.util.ModePacket;
import edu.gatech.grits.util.RolePacket;

import javolution.util.*;

public class MDLFrame extends JFrame implements PanelObservable {

	//network ID
	private final String NAME = "GuiAgent";

	private JPanel mainPanel;
	private ProgramSelectPanel psp;
	private DistributionPanel distPanel;
//	private ConnectionPanel cp;
//	private AgentSpawnPanel asp;

	private RemoteControlAgent agent;
	private FastList<String> currAgents;

	private MDLnProgram currProgram;

	//SWING timer!
	private Timer guiUpdateTimer;

	public MDLFrame(String propFileName) {

		//frame properties
		this.setTitle("MDL Agent Controller!");
		this.setEnabled(true);
		this.setVisible(true);
		this.setSize(new Dimension(450, 500));
		this.setMinimumSize(new Dimension(450, 500));
		this.setSize(new Dimension(450, 300));
		this.setMinimumSize(new Dimension(450, 300));
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		agent = new RemoteControlAgent(new Properties(propFileName), "Gui Agent");
		currAgents = new FastList<String>();
				
		currProgram = null;

		//timer facilities
		guiUpdateTimer = new Timer(1000, new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				//get current numerical ids of neighbors
				FastList<Integer> neighborIds = agent.getCurrNeighborIds();

				for (FastList.Node<Integer> n = neighborIds.head(), end = neighborIds.tail(); (n = n.getNext()) != end;) {
					Integer key = n.getValue();
					String currName = agent.getAgentIdMap().get(key);
//					System.out.println(currName);
					if(!currAgents.contains(currName)){
						currAgents.add(currName);
						//add it locally, but exclude THIS gui agent!
						if(!currName.equals(NAME)){
							distPanel.addNewAgent(currName);
						}
					}
				}

				//check for consistent agent list!
				if(neighborIds.size() != currAgents.size()){
					for (FastMap.Entry<Integer, String> curr = agent.getAgentIdMap().head(), end = agent.getAgentIdMap().tail(); (curr = curr.getNext()) != end;) {
						Integer key = curr.getKey();
						if(!neighborIds.contains(key)){
							String name = agent.getAgentIdMap().get(key);
							//remove this element from the string list
							currAgents.remove(name);
							//remove from gui
							distPanel.removeOldAgent(name);
						}
					}
				}
				
				// update agents in the connection panel
//				if(remoteController.getCurrNeighborState() != null){
//					FastMap<Integer,AgentGraphicsState> states = remoteController.getCurrNeighborState();
//					for(FastMap.Entry<Integer, AgentGraphicsState> curr = states.head(), end = states.tail(); (curr = curr.getNext()) != end;){
//						cp.updateAgent(curr.getKey(), 0, new FastList<Integer>());
//					}
//				}
				
			}

		});
		guiUpdateTimer.start();

		//standard gui configuration stuff
		mainPanel = new JPanel();

		//		asp = new AgentSpawnPanel();
		psp = new ProgramSelectPanel(new MDLnCompileBackend());
		distPanel = new DistributionPanel();
//		cp = new ConnectionPanel();
		
		psp.addObserver(this);
		distPanel.addObserver(this);
		psp.addObserver(distPanel);
		distPanel.addObserver(psp);

		buildLayout();
	}

	private final void buildLayout(){

		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		mainPanel.add(psp);
		mainPanel.add(distPanel);
//		JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, mainPanel, cp);
//		this.add(split);
		this.add(mainPanel);

	}

	public void notifyChange(ObserverPacket message) {

		MessageType mt = message.getDataType();

		if(mt == MessageType.SEND_DATA){
			String agentName = (String) message.getData();

			for (FastMap.Entry<Integer, String> curr = agent.getAgentIdMap().head(), end = agent.getAgentIdMap().tail(); (curr = curr.getNext()) != end;) {

				if(curr.getValue().equals(agentName)){
					if(currProgram != null){
						// send the program!
						
						FastMap<String,Integer> roles = currProgram.getAgentRoles();
//						Boolean repeat = currProgram.isProgramRepeating(agentName);
						SyrupPacket msg = new SyrupPacket(MDLnMessage.PROGRAM, 0);

						//embed the agent's role
						RolePacket rp = new RolePacket("AgentRole");
						rp.addRole(roles.get(agentName));
						msg.addPacket(rp);
						//		BooleanPacket bp = new BooleanPacket("Repeating");
						//		bp.addBoolean(isRepeating);
						//		msg.addPacket(bp);

						//construct the motion program packet for the target agent
						for(MDLnMode mode : currProgram.extractProgram(agentName)){
							//			ModePacket pkt = new ModePacket(mode.getId());
							ModePacket pkt = new ModePacket(mode.getClass().getSimpleName());
							pkt.setMode(mode);
							msg.addPacket(pkt);
						}

						//transmit!
						agent.sendMessage(curr.getKey(), msg);
//						pancakes.getNetworkManager().sendPacket(targetAgentId, msg);

						distPanel.readyAgent(agentName);
					}

				}	

			}
		}

		else if(mt == MessageType.NEW_PROGRAM){
			System.out.println("New program ready!");
			currProgram = (MDLnProgram) message.getData();
		}

		else if(mt == MessageType.START){
			String agentName = (String) message.getData();

			for (FastMap.Entry<Integer, String> curr = agent.getAgentIdMap().head(), end = agent.getAgentIdMap().tail(); (curr = curr.getNext()) != end;) {

				if(curr.getValue().equals(agentName)){
					SyrupPacket startMsg = new SyrupPacket(MDLnMessage.START,0);
					agent.sendMessage(curr.getKey(), startMsg);
					distPanel.runAgent(agentName);
				}	

			}
			
		}

		else if(mt == MessageType.STOPPED){
			//stop the current program
			String agentName = (String) message.getData();

			for (FastMap.Entry<Integer, String> curr = agent.getAgentIdMap().head(), end = agent.getAgentIdMap().tail(); (curr = curr.getNext()) != end;) {

				if(curr.getValue().equals(agentName)){
					SyrupPacket stopMsg = new SyrupPacket(MDLnMessage.STOP, 0);
					agent.sendMessage(curr.getKey(), stopMsg);
					distPanel.stopAgent(agentName);
				}	

			}
		}

		else if(mt == MessageType.KILLED){
			//			System.out.print("Kill agent..." + (String) message.getData());
			String agentName = (String) message.getData();

			for (FastMap.Entry<Integer, String> curr = agent.getAgentIdMap().head(), end = agent.getAgentIdMap().tail(); (curr = curr.getNext()) != end;) {

				if(curr.getValue().equals(agentName)){
					SyrupPacket killMsg = new SyrupPacket(MDLnMessage.KILL, 0);
					agent.sendMessage(curr.getKey(), killMsg);
					distPanel.killAgent(agentName);
				}	

			}

		}
		else if(mt == MessageType.START_ALL){
			// send start message to all agents!
			for (FastMap.Entry<Integer, String> curr = agent.getAgentIdMap().head(), end = agent.getAgentIdMap().tail(); (curr = curr.getNext()) != end;) {
				if(curr.getKey() != agent.getMyId()){
					System.out.println("Starting " + curr.getKey() + "...");
//					remoteController.sendMessage(MDLnMessage.START, curr.getKey());
				}
			}

		}

	}

	public static void main(final String[] args){
		SwingUtilities.invokeLater(new Runnable(){

			public void run() {
				MDLFrame guiFrame = new MDLFrame(args[0]);
			}

		});

	}

}
