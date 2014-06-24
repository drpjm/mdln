package edu.gatech.grits.mdln.lang.util;

/*
 * This enum lists all implemented sensors on the robot for use in state vectors.
 */
public enum SensorId {
	
	SONAR, 
	AGENT,		//a "sensor" that holds all shared data of a buddy agent 
	IR, 
	GPS;
}
