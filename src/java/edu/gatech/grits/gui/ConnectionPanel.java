package edu.gatech.grits.gui;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

import edu.gatech.grits.gui.model.AgentState;

import javolution.util.*;

/**
 * Panel that allows for the visualization of buddy connections in an MDLn
 * agent network.
 * @author pmartin
 *
 */
public class ConnectionPanel extends MyPanel {

	private FastMap<Integer, AgentState> agentStateMap;
	private AgentPanel agentPanel;
	private JButton showBuddies;
	private JButton showRoles;
	
	public ConnectionPanel(){
		observers = new FastList<PanelObservable>();
		buildContent();
		buildLayout();
	}
	
	@Override
	protected void buildContent() {
		this.setBorder(BorderFactory.createTitledBorder("Agent Connection Status"));
		
		agentPanel = new AgentPanel();
		
		
		showBuddies = new JButton("Show Buddies");
		showBuddies.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				
				if(showBuddies.getText().equals("Show Buddies")){
					showBuddies.setText("Hide Buddies");
//					System.out.println("Show buddy connections.");
					agentPanel.setShowingBuddies(true);
				}
				else {
					showBuddies.setText("Show Buddies");
					agentPanel.setShowingBuddies(false);
				}
				
			}
			
		});
		
		showRoles = new JButton("Show Roles");
		showRoles.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {

				if(showRoles.getText().equals("Show Roles")){
					showRoles.setText("Hide Roles");
					agentPanel.setShowingRoles(true);
				}
				else {
					showRoles.setText("Show Roles");
					agentPanel.setShowingRoles(false);
				}
				
			}
			
		});
		
		// test code
//		FastList<Integer> list1 = new FastList<Integer>();
//		list1.add(3);
//		FastList<Integer> list2 = new FastList<Integer>();
//		list2.add(1);
//		FastList<Integer> list3 = new FastList<Integer>();
//		list3.add(1);
//		FastList<Integer> list4 = new FastList<Integer>();
//		list4.add(1);
//		list4.add(2);
		
//		agentPanel.updateAgent(1, 0, list1);
//		agentPanel.updateAgent(2, 0, list2);
//		agentPanel.updateAgent(3, 0, list3);
//		agentPanel.updateAgent(4, 0, list4);
//		agentPanel.updateAgent(5, 0, list1);
//		agentPanel.updateAgent(6, 0, new FastList<Integer>());
	}

	@Override
	protected void buildLayout() {
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.X_AXIS));
		mainPanel.add(showBuddies);
		mainPanel.add(showRoles);
		
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		this.add(mainPanel);
		this.add(agentPanel);
		
	}

	public void updateAgent(Integer id, Integer role, FastList<Integer> buddies){
		agentPanel.updateAgent(id, role, buddies);
	}
	
	public void notifyChange(ObserverPacket message) {
		// TODO Auto-generated method stub
		
	}
	
}
