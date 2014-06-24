package edu.gatech.grits.mdln.interrupt;

import javolution.util.FastList;
import javolution.util.FastMap;
import edu.gatech.grits.mdln.lang.util.InterruptAdapter;
import edu.gatech.grits.mdln.lang.util.SensorId;
import edu.gatech.grits.util.DataVector;
import edu.gatech.grits.util.SensorType;

public class Clear implements InterruptAdapter {

	private final float THRESHOLD = 2f;

	public String getName() {

		return "Clear";
	}

	public boolean isInterrupted(DataVector localData,
			FastMap<Integer, DataVector> neighborData) {
		
		if(localData != null){
			// for now only use sonar
			if(localData.hasDataFrom(SensorType.SONAR)){

				// TODO: WORKS ONLY FOR PIONEER CONFIGURATION!
				FastList<Float> sonarData = localData.getDataFrom(SensorType.SONAR);
				boolean leftClear = false;
				if(sonarData.get(0) > THRESHOLD && sonarData.get(1) > THRESHOLD && sonarData.get(2) > THRESHOLD){
					leftClear = true;
//					System.out.println(sonarData.get(1) + " " + sonarData.get(2));
//					System.out.println("Left clear!");
				}
				
				boolean frontClear = false;
				if(sonarData.get(3) > THRESHOLD && sonarData.get(4) > THRESHOLD){
					frontClear = true;
//					System.out.println(sonarData.get(3) + " " + sonarData.get(4));
//					System.out.println("Front clear!");
				}
				
				boolean rightClear = false;
				if(sonarData.get(5) > THRESHOLD && sonarData.get(6) > THRESHOLD && sonarData.get(7) > THRESHOLD){
					rightClear = true;
//					System.out.println(sonarData.get(5) + " " + sonarData.get(6));
//					System.out.println("Right clear!");
				}

				if(leftClear && frontClear && rightClear){
					System.out.println("All clear!");
					return true;
				}
				
//				if(sonarData.get(1) > THRESHOLD && sonarData.get(2) > THRESHOLD && sonarData.get(3) > THRESHOLD &&
//						sonarData.get(4) > THRESHOLD && sonarData.get(5) > THRESHOLD && sonarData.get(6) > THRESHOLD){
//					return true;
//				}
			}
		}
		return false;
	}

}
