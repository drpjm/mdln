package edu.gatech.grits.gui;

import java.awt.*;

import javax.swing.*;

import edu.gatech.grits.gui.model.AgentGraphicsState;

import javolution.util.*;

public class AgentPanel extends JPanel {

	private FastMap<Integer, AgentGraphicsState> agentStates;
	private boolean isShowingBuddies = false;
	private boolean isShowingRoles = false;

	private int agentCount = 0;
	private float currAngle = 0;

	private final int AGENT_WIDTH = 35;
	private final int AGENT_HEIGHT = 35;
	private final float ANGLE_STEP = (float)(Math.PI / 3);
	private final int AGENT_OFFSET = 3*this.AGENT_WIDTH;
	private FastList<Float> angles;

	private boolean isAlternate = false;

	private final Color[] COLORS = {Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW, Color.CYAN, Color.MAGENTA};
	private float[] dashes = new float[6];
	
	public AgentPanel(){
		agentStates = new FastMap<Integer, AgentGraphicsState>();
		angles = new FastList<Float>();
		for(int i = 0; i < dashes.length; i++){
			dashes[i] = (float)Math.floor((Math.random()*10))+2;
		}
	}

	public void paintComponent(Graphics g) {
		
		Graphics2D g2 = (Graphics2D)g;
		
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setColor(Color.WHITE);
		g2.fillRect(0, 0, this.getWidth(), this.getHeight());


		// paint ovals
		for (FastMap.Entry<Integer, AgentGraphicsState> e = agentStates.head(), end = agentStates.tail(); (e = e.getNext()) != end;) {

			Integer currId;
			AgentGraphicsState currState;
			if(e.getValue() != null && e.getKey() != null){
				currState = e.getValue();
				currId = e.getKey();

				drawAgent(currId, g2);

				// draw lines for any buddies
				if(isShowingBuddies){
					if(!currState.getBuddyIds().isEmpty()){
						for(Integer buddyId : currState.getBuddyIds()){
							drawBuddyLine(currId, buddyId, g2);
						}
					}
				}

				if(isShowingRoles){
					g2.setColor(Color.WHITE);
					g2.setFont(new Font("Sans Serif", Font.BOLD, 12));
					g2.drawString(currState.getRole().toString(), currState.getXLoc()-4, currState.getYLoc()+5);
				}
			}
		}	

	}

	private final void drawAgent(Integer currId, Graphics2D g2){

		//calculate the position
		int panelCenterX = this.getWidth() / 2;
		int panelCenterY = this.getHeight() / 2;
		// calculated x and y locations
		int xLoc;
		int yLoc;

		/*
		 *  calculate offset from agent size:
		 *  loc = angleprojection - agentwidth/2
		 */
		float currAngle;
		if(!angles.isEmpty()){
			// TODO: this is BAD for out of order agents!!! (i.e. 4 before 1)
			currAngle = angles.get(currId-1);
			// first go to target point
			xLoc = panelCenterX + (int)(this.AGENT_OFFSET * Math.cos((double)currAngle));
			yLoc = panelCenterY + (int)(this.AGENT_OFFSET * Math.sin((double)currAngle));
			agentStates.get(currId).setXLoc(xLoc);
			agentStates.get(currId).setYLoc(yLoc);
			// adjust for Java drawing
			xLoc -= (int)(this.AGENT_WIDTH/2);
			yLoc -= (int)(this.AGENT_WIDTH/2);
			
			
			g2.setColor(Color.BLACK);
			g2.setStroke(new BasicStroke(2.0f));
			g2.drawOval(xLoc, yLoc, this.AGENT_WIDTH, this.AGENT_HEIGHT);
			g2.setColor(COLORS[currId-1]);
			g2.fillOval(xLoc, yLoc, this.AGENT_WIDTH, this.AGENT_HEIGHT);
		}

	}

	private final void drawBuddyLine(Integer id, Integer targetId, Graphics2D g2){

		int thisX = agentStates.get(id).getXLoc();
		int thisY = agentStates.get(id).getYLoc();
		int targetX = agentStates.get(targetId).getXLoc();
		int targetY = agentStates.get(targetId).getYLoc();

		// calculate offsets for x and y
		// TODO: figure out a nice way to do these offsets
//		double toTargetX = targetX - thisX;
//		double toTargetY = targetY - thisY;
//		double theta1 = Math.atan(toTargetY / toTargetX);
//		int startOffsetX = (int) ( (double)this.AGENT_WIDTH / 2 * Math.cos(theta1) );
//		int startOffsetY = (int) ( (double)this.AGENT_WIDTH / 2 * Math.sin(theta1) );
//		
//		double toThisX = thisX-targetX;
//		double toThisY = thisY-targetY;
//		double theta2 = Math.atan(toThisY / toThisX);
//		int endOffsetX = (int) ( (double)this.AGENT_WIDTH / 2 * Math.cos(theta2) );
//		int endOffsetY = (int) ( (double)this.AGENT_WIDTH / 2 * Math.sin(theta2) );
		
		// line color and type
		g2.setColor(COLORS[id-1]);
//		g2.setColor(Color.BLACK);
		float[] dash = {dashes[id]};
		BasicStroke dashedLine = new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER, 
													1.0f, dash, 0.0f);
		
		g2.setStroke(dashedLine);
		// draw line
		g2.drawLine(thisX, thisY, targetX, targetY);
	}
	
	public final void updateAgent(Integer id, Integer role, FastList<Integer> buddies){

//		System.out.println("Add agent " + id + " to AgentPanel");
		if(agentStates.containsKey(id)){
			agentStates.get(id).setRole(role);
			agentStates.get(id).setBuddyIds(buddies);
		}
		else{
			//add the agent
			agentStates.put(id, new AgentGraphicsState(buddies, "Agent" + id, role));
			// adjust angles
			if(angles.isEmpty()){
				angles.add(0f);
			}
			else{
				angles.add(angles.get(agentCount-1) + this.ANGLE_STEP);
			}
			agentCount++;
			
		}
		repaint();
	}


	public void setShowingBuddies(boolean isShowingBuddies) {
		this.isShowingBuddies = isShowingBuddies;
		repaint();
	}

	public void setShowingRoles(boolean isShowingRoles) {
		this.isShowingRoles = isShowingRoles;
		repaint();
	}

	public void notifyChange(ObserverPacket message) {
		// TODO Auto-generated method stub

	}

}
