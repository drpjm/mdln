package edu.gatech.grits.consensus;
/* This class initialize the three robots, and run the threaded version of the dynamics desired
 */

public class InitializerLeaderFollower {
	// Number of robot, this number corresponds to the ribot initialization in .world file
	public static int numRobot = 3;
	
	public static void main(String[] args) {
		// Initialize the robots with the desired dynamics
		// Change dynamics if needed
		
		Thread[] newThreads = new Thread [numRobot];
		
		// Start the 1st robot, follower
		KeepDistance KeepDistanceThreads1 = new KeepDistance(0);
		newThreads[0] = new Thread(KeepDistanceThreads1);
		newThreads[0].start();
		
		// Start the 2nd robot, leader
		Leader LeaderThread = new Leader(1);
		newThreads[1] = new Thread(LeaderThread);
		newThreads[1].start();
		
//		// Start the 3rd robot, follower
		KeepDistance KeepDistanceThreads2 = new KeepDistance(2);
		newThreads[2] = new Thread(KeepDistanceThreads2);
		newThreads[2].start();
		

	}

}
