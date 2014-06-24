package edu.gatech.grits.util;

/**
 * Enum for abstracting sensor types within DataVectors.
 * @author pmartin
 *
 */
public enum SensorType {

	SONAR(0),
	IR(1),
	GLOBAL(2),
	LOCAL(3);
	
	private int sensor;
	private SensorType(int i){
		
	}
	public int getSensor(){
		return sensor;
	}
	public static SensorType toSensorType(String s){
		if(s.equals(SONAR.toString())){
			return SONAR;
		}
		else if(s.equals(IR.toString())){
			return IR;
		}
		else if(s.equals(GLOBAL.toString())){
			return GLOBAL;
		}
		else if(s.equals(LOCAL.toString())){
			return LOCAL;
		}
		else
			return null;
	}
}
