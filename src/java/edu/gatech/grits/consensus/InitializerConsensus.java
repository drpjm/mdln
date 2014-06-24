package edu.gatech.grits.consensus;
/* This class initialize the three robots, and run the threaded version of the dynamics desired
 */

public class InitializerConsensus {
	// Number of robot, this number corresponds to the ribot initialization in .world file
	public static int numRobot = 5;
	
	public static void main(String[] args) {
		// Initialize the robots with the desired dynamics
		// Change dynamics if needed
		
		Consensus[] consensusThreads = new Consensus [numRobot];
		Thread[] newThreads = new Thread [numRobot];
		
		for (int i=0; i < numRobot; i++) {
			consensusThreads[i] = new Consensus(i);
			newThreads[i] = new Thread(consensusThreads[i]);
			newThreads[i].start();
		}

	}

}
