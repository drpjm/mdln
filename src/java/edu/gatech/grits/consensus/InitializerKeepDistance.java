package edu.gatech.grits.consensus;
/* This class initialize the three robots, and run the threaded version of the dynamics desired
 */

public class InitializerKeepDistance {
	// Number of robot, this number corresponds to the ribot initialization in .world file
	public static int numRobot = 3;
	
	public static void main(String[] args) {
		// Initialize the robots with the desired dynamics
		// Change dynamics if needed
		
		KeepDistance[] KeepDistanceThreads = new KeepDistance [numRobot];
		Thread[] newThreads = new Thread [numRobot];
		
		for (int i=0; i < numRobot; i++) {
			KeepDistanceThreads[i] = new KeepDistance(i);
			newThreads[i] = new Thread(KeepDistanceThreads[i]);
			newThreads[i].start();
		}
		

	}

}
