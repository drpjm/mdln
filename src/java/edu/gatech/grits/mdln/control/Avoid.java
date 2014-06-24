package edu.gatech.grits.mdln.control;

import javolution.util.FastList;
import javolution.util.FastMap;
import edu.gatech.grits.mdln.lang.util.ControlAdapter;
import edu.gatech.grits.mdln.lang.util.ControlParam;
import edu.gatech.grits.mdln.lang.util.SensorId;
import edu.gatech.grits.util.DataVector;
import edu.gatech.grits.util.SensorType;

public class Avoid implements ControlAdapter {

	private final float OBSTACLE_CHARGE = 1.0f;
	private final float ROBOT_CHARGE = 1.0f;
	private final float GOAL_CHARGE = 1.0f; // adds a slight forward pull
	
	private final float MAX_TRANS = 1.0f;
	private final float MAX_ROT = 0.5f;

	// sensor locations...
	float[] sensorAngles = {(float)Math.toRadians(90), (float)Math.toRadians(50), (float)Math.toRadians(30), (float)Math.toRadians(10),
			(float)Math.toRadians(-10), (float)Math.toRadians(-30), (float)Math.toRadians(-50), (float)Math.toRadians(-90)};
	
	
	public String getName() {
		return "Avoid";
	}

	/**
	 * Implements a potential function based obstacle avoidance. 
	 */
	public ControlParam applyControl(DataVector localData,
			FastMap<Integer, DataVector> neighborData) {
		
		ControlParam ctrl = new ControlParam();
		if(localData != null){
			
			FastList<Float> data;
			if(localData.getDataMap().containsKey(SensorType.SONAR)){
				data = localData.getDataFrom(SensorType.SONAR);
				float sumX = 0, sumY =0;
				// sum the repulsive forces
				for(int i = 0; i < sensorAngles.length; i++){
					float currVal = data.get(i);
					float fRep = this.OBSTACLE_CHARGE*this.ROBOT_CHARGE / (float)Math.pow(currVal, 2);
					float x = -fRep * (float)Math.cos(sensorAngles[i]);
					float y = -fRep * (float)Math.sin(sensorAngles[i]);
					sumX += x;
					sumY += y;
				}
				// finally add the attractive force of a point 1 meter away
				float fAtt = this.GOAL_CHARGE*this.ROBOT_CHARGE / 1;
				sumX += fAtt * 1;
				
				if(sumX > MAX_TRANS){
					sumX = MAX_TRANS;
				}
				if(sumX < -MAX_TRANS){
					sumX = -MAX_TRANS;
				}
				if(sumY > MAX_ROT){
					sumY = MAX_ROT;
				}
				if(sumY < -MAX_ROT){
					sumY = -MAX_ROT;
				}
				ctrl.setTranslationVel(sumX);
				ctrl.setRotationVel(sumY);
				
			}
			else{
				// no data to use, do not move!
				ctrl.setRotationVel(0);
				ctrl.setTranslationVel(0);
			}			
		}
		return ctrl;
	}

}
