package edu.gatech.grits.mdln.control;

import javolution.util.FastMap;
import edu.gatech.grits.mdln.lang.util.ControlAdapter;
import edu.gatech.grits.mdln.lang.util.ControlParam;
import edu.gatech.grits.util.DataVector;
import edu.gatech.grits.util.Geometry;
import edu.gatech.grits.util.SensorType;

public class K3GoToGoal implements ControlAdapter {

	//TODO: make goal NOT hard coded!
	private float goalX = -80; // cm
	private float goalY = 112;	// cm

	private final float SIGHT_ANGLE = (float) Math.PI / 8;

	private final float MAX_TRANS = 0.18f;	// m/s
	private final float MAX_ROT = 0.22f;		// m/s

	public String getName() {
		return "K3GoToGoal";
	}

	public ControlParam applyControl(DataVector localData,
			FastMap<Integer, DataVector> buddyData) {


		if(localData != null){
			if(localData.hasDataFrom(SensorType.LOCAL)){

				// get location
				float myX = localData.getDataFrom(SensorType.LOCAL).get(0);
				float myY = localData.getDataFrom(SensorType.LOCAL).get(1);
				float theta = localData.getDataFrom(SensorType.LOCAL).get(2);

				float[] dest = Geometry.calcDestination(goalX, goalY, myX, myY);
				float targAngle = dest[0];
				float distance = dest[1];
				
//				System.out.println(" angle to target: " + beta + " rad");
//				System.out.println(" distance to target: " + distance);

				// rotational speed in rad/sec
				float omega = targAngle - theta;

				System.out.println(myX + "," + myY);
				// translational speed
				float vel = 0;
				// can we "see" the goal?
				if((targAngle - theta) < SIGHT_ANGLE && (targAngle - theta) > -SIGHT_ANGLE){

					float velCalc = distance / 100;	// scale to m/s
					
					// if so, drive towards it
					if(velCalc > MAX_TRANS){
						vel = MAX_TRANS;
					}
					else if(velCalc < -MAX_TRANS){
						vel = -MAX_TRANS;
					} 
					else {
						vel = velCalc;
					}

				} 
				else {
					
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
