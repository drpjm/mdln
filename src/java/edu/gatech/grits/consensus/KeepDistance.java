package edu.gatech.grits.consensus;
/* This class actually implement the Leader-Follower control laws
 * for one robot.  This dynamic will attempt to keep a distance of
 * k from each other.  If one of the agent is moving through a different
 * direction, the follower will use this dynamics
 * The dynamics can be expressed as follows:
 * x_jdot = Sigma {i~N(j)} (||x_i-x_j||-k)*(x_i-x_j)
 */
import java.lang.System;

class KeepDistance implements Runnable {
	// Robot number assignment
	public int 		number;
	// The robot object
	private Robot	robot;
	// The fiducial count
	private int fiducialcount;
	
	// k is the desired distance between agents
	private static float k = 5f;
	// Note that Delta for these agents = 7
	
	// Simulation Constant
	private static float 	dt = 0.1f;
	
	KeepDistance (int robotnum) {
		number = robotnum;
		robot  = new Robot(number);
	}
	
	public void run (){
		// This function actually start the Leader-follower Dynamics		
		float ftx;		// dx/dt
		float fty;		// dy/dt
		float tx;
		float ty;
		float d2t;		// distance to target ftx^2+fty^2
		float d;
		
		while (true) {
			// get all fudicial values		
			fiducialcount = robot.getFiducials();
			
			if (fiducialcount != 0) {
				System.out.println("Found "+fiducialcount+" fiducials for robot "+(number+1));

				ftx = 0;
				fty = 0;
				for (int j=0; j < fiducialcount; j++){
					System.out.println("Found fiducialID "+robot.fiducials[j].getId()+" for robot "+(number+1));
					//System.out.println("targetx="+robot.fiducials[j].getPose().getPx()+";targety="+robot.fiducials[j].getPose().getPy());
					//System.out.println("Target has angle of "+Math.toDegrees(robot.fiducials[j].getPose().getPyaw()));
					tx = robot.fiducials[j].getPose().getPx();
					ty = robot.fiducials[j].getPose().getPy();
					d = (float)Math.sqrt(tx*tx+ty*ty);
					System.out.println("Distance to fiducial ID "+robot.fiducials[j].getId()+" for robot "+(number+1)+" is "+d);
					ftx = ftx + (d-k) * tx;
					fty = fty + (d-k) * ty;
				}
				System.out.println("ftx="+ftx+";fty="+fty);

				d2t=(float)Math.sqrt(ftx*ftx+fty*fty);

				robot.driveTo(ftx*dt, fty*dt);
			}
			else {
				System.out.println("Found no fiducials for robot "+(number+1));
				robot.stop();
			}
			
			try { Thread.sleep (500); } catch (Exception e) { }
		}
			
		// Consensus succussful, stop movement of robots.
//		robot.stop();
//		robot.missionSuc = true;		
	}
	
}
