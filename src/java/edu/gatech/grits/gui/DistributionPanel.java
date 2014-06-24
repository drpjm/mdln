package edu.gatech.grits.gui;


import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.*;

import javolution.util.FastList;
import javolution.util.FastMap;

import edu.gatech.grits.gui.ObserverPacket.MessageType;
import edu.gatech.grits.gui.model.AgentState;

public class DistributionPanel extends MyPanel {

	private final String NONE = "None";

	private JButton send;
	private JButton start;
	private JButton stop;
	private JButton kill;
	private JComboBox agentBox;
	private JButton startAll;

	private String selectedAgent;
	private FastMap<String,AgentState> states;

	private Timer timer;

	private boolean isFirstAgent;

	public DistributionPanel(){
		selectedAgent = this.NONE;
		states = new FastMap<String, AgentState>();		
		observers = new FastList<PanelObservable>();
		isFirstAgent = true;

		buildContent();
		buildLayout();

	}

	@Override
	protected void buildContent() {
		agentBox = new JComboBox();
		agentBox.addItemListener(new ItemListener(){

			public void itemStateChanged(ItemEvent e) {

				if(e.getStateChange() == ItemEvent.SELECTED){
					selectedAgent = (String) agentBox.getSelectedItem();

					if(states.get(selectedAgent) == AgentState.ACTIVE){
						start.setEnabled(false);
						send.setEnabled(true);
						kill.setEnabled(true);
						stop.setEnabled(false);
					}
					else if(states.get(selectedAgent) == AgentState.STOPPED){
						start.setEnabled(false);
						send.setEnabled(true);
						kill.setEnabled(true);
						stop.setEnabled(false);
					}
					else if(states.get(selectedAgent) == AgentState.RUNNING){
						send.setEnabled(false);
						start.setEnabled(false);
						stop.setEnabled(true);
						kill.setEnabled(true);
					}
					else if(states.get(selectedAgent) == AgentState.READY){
						send.setEnabled(false);
						start.setEnabled(true);
						stop.setEnabled(false);
						kill.setEnabled(true);
					}
					else {
						disableAll();
					}
				}

			}

		});

		send = new JButton("Send");
		send.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				//tell MDLFrame to send a new program to selectedAgent!
				for(PanelObservable po : observers){
					ObserverPacket message = new ObserverPacket(MessageType.SEND_DATA, selectedAgent);
					po.notifyChange(message);
				}
			}

		});

		start = new JButton("Start");
		start.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				stop.setEnabled(true);
				start.setEnabled(false);
				for(PanelObservable po : observers){
					ObserverPacket message = new ObserverPacket(MessageType.START, selectedAgent);
					po.notifyChange(message);
				}
			}

		});

		stop = new JButton("Stop");
		stop.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				start.setEnabled(true);
				stop.setEnabled(false);
				for(PanelObservable po : observers){
					ObserverPacket message = new ObserverPacket(MessageType.STOPPED, selectedAgent);
					po.notifyChange(message);
				}	
			}

		});

		kill = new JButton("Kill");
		kill.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				for(PanelObservable po : observers){
					po.notifyChange(new ObserverPacket(MessageType.KILLED, selectedAgent));
				}

			}

		});

		startAll = new JButton("Start All");
		startAll.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				for(PanelObservable po : observers){
					po.notifyChange(new ObserverPacket(MessageType.START_ALL, null));
				}

			}


		});

		//timer for updating kill button
		timer = new Timer(1500, new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				if(agentBox.getItemCount() == 0){
					kill.setEnabled(false);
					send.setEnabled(false);
					isFirstAgent = true;
				}
			}

		});
		timer.start();

		agentBox.setEnabled(true);
		send.setEnabled(false);
		start.setEnabled(false);
		stop.setEnabled(false);
		kill.setEnabled(false);
		startAll.setEnabled(false);
	}

	@Override
	protected void buildLayout() {


		JPanel mainPanel = new JPanel();
		mainPanel.setBorder(BorderFactory.createTitledBorder("Agent Controls"));
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

		JPanel top = new JPanel();
		top.setLayout(new BoxLayout(top, BoxLayout.X_AXIS));
		JPanel middle = new JPanel();
		middle.setLayout(new BoxLayout(middle, BoxLayout.X_AXIS));
		JPanel bottom = new JPanel();
		bottom.setLayout(new BoxLayout(bottom, BoxLayout.X_AXIS));

		top.add(Box.createRigidArea(new Dimension(45,0)));
		top.add(agentBox);
		top.add(Box.createRigidArea(new Dimension(45,0)));
		
		middle.add(send);
		middle.add(start);
		middle.add(startAll);
		
		bottom.add(stop);
		bottom.add(kill);

		mainPanel.add(top);
		mainPanel.add(middle);
		mainPanel.add(bottom);

		this.add(mainPanel);

	}

	public void notifyChange(ObserverPacket message) {
		MessageType mt = message.getDataType();

		if(mt == ObserverPacket.MessageType.CANCELLED){
			send.setEnabled(false);
			start.setEnabled(false);
			stop.setEnabled(false);

		}

		if(mt == ObserverPacket.MessageType.NEW_PROGRAM){
			send.setEnabled(true);
			start.setEnabled(true);
		}

		if(mt == ObserverPacket.MessageType.STOPPED){
			start.setEnabled(false);
			stop.setEnabled(false);
		}

	}

	public void addNewAgent(String name){
		agentBox.addItem(name);
		states.put(name, AgentState.ACTIVE);
		if(isFirstAgent){
			send.setEnabled(true);
			kill.setEnabled(true);
			isFirstAgent = false;
		}
	}

	public void removeOldAgent(String name){
		agentBox.removeItem(name);
		states.remove(name);
	}

	public void readyAgent(String name){
		states.put(name, AgentState.READY);
		send.setEnabled(false);
		start.setEnabled(true);
		stop.setEnabled(false);
		kill.setEnabled(true);

	}

	public void runAgent(String name){
		states.put(name, AgentState.RUNNING);
		send.setEnabled(false);
		start.setEnabled(false);
		stop.setEnabled(true);
		kill.setEnabled(true);
	}

	public void stopAgent(String name){
		states.put(name, AgentState.STOPPED);
		start.setEnabled(false);
		send.setEnabled(true);
		kill.setEnabled(true);
		stop.setEnabled(false);

	}
	public void killAgent(String name){
		states.put(name, AgentState.DEAD);
		disableAll();
	}

	private final void disableAll(){
		send.setEnabled(false);
		start.setEnabled(false);
		stop.setEnabled(false);
		kill.setEnabled(false);
	}
}
