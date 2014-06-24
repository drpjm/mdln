package edu.gatech.grits.mdln.interrupt;

import javolution.util.FastList;
import javolution.util.FastMap;
import edu.gatech.grits.mdln.lang.util.InterruptAdapter;
import edu.gatech.grits.util.DataVector;
import edu.gatech.grits.util.SensorType;

public class K3Obstacle implements InterruptAdapter {

	private final float THRESHOLD = 0.1f;
	
//	private float ir2_avg = 0;
//	private float ir3_avg = 0;
//	private float ir4_avg = 0;
//	private float ir5_avg = 0;
	
	
	public String getName() {
		return "Obstacle";
	}

	
	public boolean isInterrupted(DataVector localData,
			FastMap<Integer, DataVector> neighborData) {

		if(localData != null){
			// for now only use sonar
			if(localData.hasDataFrom(SensorType.IR)){

				FastList<Float> irData = localData.getDataFrom(SensorType.IR);
				
				// take out negative values in IR
				for(int i = 2; i < 6; i++){
					if(irData.get(i) < 0){
						irData.set(i, 0.25f);
					}
				}
				
				if(irData.get(2) < THRESHOLD || irData.get(3) < THRESHOLD ||
						irData.get(4) < THRESHOLD || irData.get(5) < THRESHOLD){
					
					System.out.println("IR: " + irData.toString());
					
					System.out.println("Obstacle!");
					
					return true;
				}
				
			}
			
		}
		
		return false;
	}

}
