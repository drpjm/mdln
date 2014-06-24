package edu.gatech.grits.consensus;
/* This class actually implement the Leader control laws
 * for one robot.  This dynamic will attempt to run with 
 * a known velocity v.
 * x_jdot = v
 */
import java.lang.System;

class Leader implements Runnable {
	// Robot number assignment
	public int 		number;
	// The robot object
	private Robot	robot;
	
	// v is the velocity the leader (this robot) run with
	private static float vx = -2f;
	private static float vy = 2f;
	
	
	// Simulation Constant
	private static float 	dt = 0.1f;
	
	Leader (int robotnum) {
		number = robotnum;
		robot  = new Robot(number);
	}
	
	public void run (){
		// This function actually start the Leader Dynamics		
		
		// distance to target vx^2+vy^2
		float d2t;
		d2t=(float)Math.sqrt(vx*vx+vy*vy);
		float angles = (float)Math.atan2(vy,vx);	//this angle go from -pi to pi
		robot.rotate(angles);
		
		while (true) {
			robot.manualDriveMode(d2t*dt, 0, 1000);
			
			System.out.println("Robot "+(number+1)+" is the leader, it's runing with velocity vx="+vx+", vy="+vy);
			try { Thread.sleep (500); } catch (Exception e) { }
		}
			
		// Consensus succussful, stop movement of robots.
//		robot.stop();
//		robot.missionSuc = true;		
	}
	
}
