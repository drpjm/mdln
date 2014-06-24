package edu.gatech.grits.consensus;
/* This class actually implement the Consensus control laws
 * for one robot.
 * The dynamics can be expressed as follows:
 * x_jdot = sigma{i~N(j)}(x_i-x_j)
 */
import java.lang.System;

class Consensus implements Runnable {
	// Robot number assignment
	public int 		number;
	// The robot object
	private Robot	robot;
	// The fiducial count
	private int fiducialcount;
	// Consensus Success identifier
	public boolean conSuccess;
	// Simulation Constant
	private static float 	dt = 0.1f;
	
	Consensus (int robotnum) {
		number = robotnum;
		robot  = new Robot(number);
		conSuccess = false;
	}
	
	public void run (){
		// This function actually start the Consensus Dynamics		
		float ftx;
		float fty;
		float d2t;
		
		while (!conSuccess) {
			// get all fudicial values		
			fiducialcount = robot.getFiducials();
			
			if (fiducialcount != 0) {
				System.out.println("Found "+fiducialcount+" fiducials for robot "+(number+1));

				ftx = 0;
				fty = 0;
				for (int j=0; j < fiducialcount; j++){
					System.out.println("Found fiducialID "+robot.fiducials[j].getId()+" for robot "+(number+1));
					System.out.println("targetx="+robot.fiducials[j].getPose().getPx()+";targety="+robot.fiducials[j].getPose().getPy());
					System.out.println("Target has angle of "+Math.toDegrees(robot.fiducials[j].getPose().getPyaw()));
					ftx = ftx + robot.fiducials[j].getPose().getPx();
					fty = fty + robot.fiducials[j].getPose().getPy();
				}
				System.out.println("ftx="+ftx+";fty="+fty);

				d2t=(float)Math.sqrt(ftx*ftx+fty*fty);

				if (d2t > 1) 
					robot.driveTo(ftx*dt, fty*dt);
				else
					conSuccess = true;
			}
			else {
				System.out.println("Found no fiducials for robot "+(number+1));
				robot.stop();
			}
			
			try { Thread.sleep (500); } catch (Exception e) { }
		}
			
		// Consensus succussful, stop movement of robots.
		robot.stop();
		robot.missionSuc = true;		
	}
	
}
