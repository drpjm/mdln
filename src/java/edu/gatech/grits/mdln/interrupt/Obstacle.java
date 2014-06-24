package edu.gatech.grits.mdln.interrupt;

import javolution.util.FastList;
import javolution.util.FastMap;
import edu.gatech.grits.mdln.lang.util.InterruptAdapter;
import edu.gatech.grits.mdln.lang.util.SensorId;
import edu.gatech.grits.util.DataVector;
import edu.gatech.grits.util.SensorType;

public class Obstacle implements InterruptAdapter {

	private final float THRESHOLD = 0.5f;
	
	public String getName() {
		return "Obstacle";
	}

	public boolean isInterrupted(DataVector localData,
			FastMap<Integer, DataVector> neighborData) {

		if(localData != null){
			// for now only use sonar
			if(localData.hasDataFrom(SensorType.SONAR)){

				FastList<Float> sonarData = localData.getDataFrom(SensorType.SONAR);
				if(sonarData.get(2) < THRESHOLD || sonarData.get(3) < THRESHOLD ||
						sonarData.get(4) < THRESHOLD || sonarData.get(5) < THRESHOLD){
					return true;
				}
				
			}
			
		}
		
		return false;
	}

}
