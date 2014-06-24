package edu.gatech.grits.mdln.control;

import javolution.util.FastList;
import javolution.util.FastMap;
import edu.gatech.grits.mdln.lang.util.ControlAdapter;
import edu.gatech.grits.mdln.lang.util.ControlParam;
import edu.gatech.grits.mdln.lang.util.SensorId;
import edu.gatech.grits.util.DataVector;
import edu.gatech.grits.util.Geometry;
import edu.gatech.grits.util.SensorType;

/**
 * This controller computes the controls for following the centroid of all
 * of the neighbors/buddies.
 * @author pmartin
 *
 */
public class Follow implements ControlAdapter {

	private final float SIGHT_ANGLE = (float) Math.PI / 8;
	private final float MAX_TRANS = 1;
	private final float MAX_ROT = 0.5f;

//	private final float MAX_TRANS = 0.3f;
//	private final float MAX_ROT = 0.2f;

	public String getName() {
		return "Follow";
	}

	public ControlParam applyControl(DataVector localData,
			FastMap<Integer, DataVector> buddyData) {

		// extract my location data
		Float myX = 0f;
		Float myY = 0f;
		Float myAngle = 0f;
		boolean myLocMissing = false;
		if(localData.getDataMap().containsKey(SensorType.LOCAL)){
			myX = localData.getDataFrom(SensorType.LOCAL).get(0);
			myY = localData.getDataFrom(SensorType.LOCAL).get(1);
			myAngle = localData.getDataFrom(SensorType.LOCAL).get(2);
		}
		else{
			myLocMissing = true;
		}

		// make sure there is neighbor data to use
//		System.out.println(buddyData);
		if(buddyData != null && buddyData.size() != 0){

			if(!myLocMissing){
				float targetX = 0;
				float targetY = 0;
				for(FastMap.Entry<Integer,DataVector> entry = buddyData.head(), end = buddyData.tail(); (entry = entry.getNext())!=end;){
					if(entry.getValue().getDataMap().containsKey(SensorType.LOCAL)){
						// get this buddy's position
						FastList<Float> loc = entry.getValue().getDataFrom(SensorType.LOCAL);
						targetX += loc.get(0);
						targetY += loc.get(1);

					}
					// similar to GoToGoal behavior...but head toward buddy centroid
//					System.out.println(entry.getKey() + ": " + centroidX + "," + centroidY);
					float dx = targetX - myX;
					float dy = targetY - myY;

					float[] data = Geometry.calcDestination(targetX, targetY, myX, myY);
					// angle between global X axis and the vector to goal
					float beta = data[0];
					float distance = data[1];

					// rotational speed in rad/sec
					float omega = beta - myAngle;
					float vel = 0;
					if(beta - myAngle < SIGHT_ANGLE && beta - myAngle > -SIGHT_ANGLE){
						// move towards -- max at 1 m/s 
						
						float velCalc = distance;
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
					System.out.println(vel + " " + omega);
					return new ControlParam(vel, omega);
				}
			}
		}

		return new ControlParam(0,0);

	}

}
