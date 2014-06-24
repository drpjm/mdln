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
public class K3Follow implements ControlAdapter {

	private final float SIGHT_ANGLE = (float) Math.PI / 8;
	private final float MAX_TRANS = 0.18f;
	private final float MAX_ROT = 0.22f;

	public String getName() {
		return "K3Follow";
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
		if(buddyData != null && buddyData.size() != 0){

			if(!myLocMissing){
				float targetX = 0;
				float targetY = 0;
				for(FastMap.Entry<Integer,DataVector> entry = buddyData.head(), end = buddyData.tail(); (entry = entry.getNext())!=end;){

					// 1. get buddy's location

					// 2. get target angle and distance

					// TODO: turn into a CENTROID calculation!

					if(entry.getValue().getDataMap().containsKey(SensorType.LOCAL)){
						// get this buddy's position
						FastList<Float> loc = entry.getValue().getDataFrom(SensorType.LOCAL);
						targetX = loc.get(0);
						targetY = loc.get(1);


						float[] data = Geometry.calcDestination(targetX, targetY, myX, myY);
						float beta = data[0];
						float distance = data[1];
						// similar to GoToGoal behavior...but head toward buddy centroid

						//rotational speed in rad/sec
						float omega = beta - myAngle;
						
						System.out.println("Target: " + targetX + "," + targetY);
						System.out.println("Beta: " + beta);
						float vel = 0;
						if(beta - myAngle < SIGHT_ANGLE && beta - myAngle > -SIGHT_ANGLE){
							// move towards -- max at 1 m/s 

							float velCalc = distance;
							if(velCalc > MAX_TRANS){
								vel = MAX_TRANS;
							}
							else if(velCalc < -MAX_TRANS){
								vel = -MAX_TRANS;
							}
							else{
								vel = distance;
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
						System.out.println("Vel: " + vel + " Omega: " + omega);
						return new ControlParam(vel, omega);
					}
				}
			}
		}
		return new ControlParam(0,0);

	}

}
