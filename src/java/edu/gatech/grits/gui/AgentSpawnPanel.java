package edu.gatech.grits.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

import javolution.util.*;

public class AgentSpawnPanel extends MyPanel {

	
	private JLabel device;
	private JLabel network;
	private JLabel id;
	
	private JTextField devPortField;
	private JTextField netPortField;
	private JTextField idField;
	private JButton spawnButton;
	
	public AgentSpawnPanel(){
		observers = new FastList<PanelObservable>();
		
		buildContent();
		buildLayout();
	}
	
	@Override
	protected void buildContent() {
		
		id = new JLabel("Agent Id");
		idField = new JTextField(8);
		
		device = new JLabel("Device Port");
		devPortField = new JTextField(8);
		
		network = new JLabel("Network Port");
		netPortField = new JTextField(8);
		
		spawnButton = new JButton("Spawn!");
		spawnButton.setAlignmentX(CENTER_ALIGNMENT);
		spawnButton.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				
				String id = idField.getText();
				String dev = devPortField.getText();
				String net = netPortField.getText();
				
				System.out.println("Spawn Agent" + id + ": " + dev + ", " + net);
				
				//TODO: HOW DO I IMPLEMENT?
			}
			
		});
		
		this.setBorder(BorderFactory.createTitledBorder("Spawn New Agents"));
	}

	@Override
	protected void buildLayout() {
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		JPanel p1 = new JPanel();
		p1.add(id);
		p1.add(idField);
		this.add(p1);
		
		JPanel p2 = new JPanel();
		p2.add(device);
		p2.add(devPortField);
		this.add(p2);
		
		JPanel p3 = new JPanel();
		p3.add(network);
		p3.add(netPortField);
		this.add(p3);
		
		this.add(spawnButton);
	}

	public void notifyChange(ObserverPacket message) {
		// TODO Auto-generated method stub
		
	}

}
