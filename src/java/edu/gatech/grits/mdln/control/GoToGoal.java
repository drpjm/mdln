package edu.gatech.grits.mdln.control;

import javolution.util.FastList;
import javolution.util.FastMap;
import edu.gatech.grits.mdln.lang.util.ControlAdapter;
import edu.gatech.grits.mdln.lang.util.ControlParam;
import edu.gatech.grits.mdln.lang.util.SensorId;
import edu.gatech.grits.util.DataVector;
import edu.gatech.grits.util.SensorType;

public class GoToGoal implements ControlAdapter {

	private float goalX = -4;
	private float goalY = 8;
	
//	private float goalX = -80; // mm
//	private float goalY = 112;	// mm
	
	private float goalTheta = (float) (-Math.PI / 4);
	private final float SIGHT_ANGLE = (float) Math.PI / 8;
	private final float MAX_TRANS = 1;
	private final float MAX_ROT = 0.5f;
	
	public String getName() {
		return "GoToGoal";
	}

	public ControlParam applyControl(DataVector localData,
			FastMap<Integer, DataVector> neighborData) {
				
		if(localData != null){
			if(localData.hasDataFrom(SensorType.LOCAL)){
				
				// get location
				float myX = localData.getDataFrom(SensorType.LOCAL).get(0);
				float myY = localData.getDataFrom(SensorType.LOCAL).get(1);
				float theta = localData.getDataFrom(SensorType.LOCAL).get(2);
				
//				System.out.println("My orientation: " + myX + " " + myY + " " + Math.toDegrees(theta));
				
				float dx = goalX - myX;
				float dy = goalY - myY;
				// angle between global X axis and the vector to goal
				float beta = (float)Math.atan2(dy, dx);
//				System.out.println(" angle to target: " + Math.toDegrees(beta));
								
				// rotational speed in rad/sec
				float omega = beta - theta;
				
				// translational speed
				float vel = 0;
				// can we "see" the goal?
				if(beta - theta < SIGHT_ANGLE && beta - theta > -SIGHT_ANGLE){
					
					float velCalc = (float)Math.sqrt(Math.pow(dx, 2) + Math.pow(dy,2));
					if(velCalc > MAX_TRANS){
						vel = 1;
					}
					else if(velCalc < -MAX_TRANS){
						vel = -1;
					}
					else{
						vel = velCalc;
					}
					
				}
				else{
					if(omega > MAX_ROT){
						omega = MAX_ROT;
					}
					if(omega < -MAX_ROT){
						omega = -MAX_ROT;
					}
				}
				
				System.out.println("Vel: " + vel + ", Omega: " + omega);
				return new ControlParam(vel, omega);
				
			}
			else
				return new ControlParam(0,0);
		}
		else{
			return new ControlParam(0,0);
		}
	}

}
