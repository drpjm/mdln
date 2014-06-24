package edu.gatech.grits.consensus;

/* This is a simple consensus program for playerstage 
*/


import java.lang.System;
import javaclient2.PlayerClient;
import javaclient2.PlayerException;
import javaclient2.Position2DInterface;
import javaclient2.FiducialInterface;
import javaclient2.structures.fiducial.*;
import javaclient2.structures.PlayerConstants;

public class ConsensusCentralized {
	// define the default rotational speed in rad/s
	static float DEF_YAW_SPEED   = 5f;
	
	public static void main (String[] args) {
		// Number of robots, later on this can be inputed by the user.
		int numRobot = 3;
		
		// Simulation constant dt (for approximating velocity)
		// Change this constant based on reality acceleration of the robot. dt->0 <=> a=inf
		float dt = (float) 0.1;
		
		
		PlayerClient        	robot = null;
		Position2DInterface[] 	posi  = new Position2DInterface [numRobot];
		FiducialInterface[]   	fudi  = new FiducialInterface [numRobot];
		PlayerFiducialData[]  	fudiData = new PlayerFiducialData [numRobot];
		PlayerFiducialItem[]  	fiducials = null;
		int fiducialcount;
		float ftx;
		float fty;
		float d2t;
		float[] angles = new float [numRobot];
		float turnspeed;
		float runtime; 
		long runtimems;
		int   conSuc;
		
		
		try {
			// Connect to the Player server and request access to Position and Laser
			robot  = new PlayerClient ("localhost", 6665);
			for (int i = 0; i < numRobot; i++){
				posi[i] = robot.requestInterfacePosition2D (i, PlayerConstants.PLAYER_OPEN_MODE);
				fudi[i] = robot.requestInterfaceFiducial	(i, PlayerConstants.PLAYER_OPEN_MODE);
			}
		} catch (PlayerException e) {
			System.err.println ("Consensus: > Error connecting to Player: ");
			System.err.println ("    [ " + e.toString() + " ]");
			System.exit (1);
		}
		
		robot.runThreaded (-1, -1);
				
		boolean conSuccess=false;
		
		while (!conSuccess) {
			// get all fudicial values		
			robot.readAll();
			fudiData = new PlayerFiducialData [numRobot];
			for (int i=0; i < numRobot; i++){
				if (fudi[i].isDataReady())
					fudiData[i]=fudi[i].getData();
				else{
					while (!fudi[i].isDataReady())
						fudiData[i]=fudi[i].getData();
				}
			}
			
			conSuc = 0;
			for (int i=0; i < numRobot; i++){
				if (fudiData[i]== null) {
					fiducialcount = 0;
					posi[i].setSpeed(0, 0);
				}
				else
					fiducialcount = fudiData[i].getFiducials_count();
				
				if (fiducialcount != 0) {
					fiducials = new PlayerFiducialItem [fiducialcount];
					fiducials = fudiData[i].getFiducials();
					System.out.println("Found "+fiducialcount+" fiducials for robot "+(i+1));
					
					ftx = 0;
					fty = 0;
					for (int j=0; j < fiducialcount; j++){
						System.out.println("Found fiducialID "+fiducials[j].getId()+" for robot "+(i+1));
						System.out.println("targetx="+fiducials[j].getPose().getPx()+";targety="+fiducials[j].getPose().getPy());
						System.out.println("Target has angle of "+Math.toDegrees(fiducials[j].getPose().getPyaw()));
						ftx = ftx + fiducials[j].getPose().getPx();
						fty = fty + fiducials[j].getPose().getPy();
					}
					System.out.println("ftx="+ftx+";fty="+fty);
					
					d2t=(float)Math.sqrt(ftx*ftx+fty*fty);
					
					if (d2t > 1) {
						// Calculate turning angle
						angles[i] = (float)Math.atan2(1*fty,1*ftx);	//this angle go from -pi to pi
						System.out.println("Angle to turn = "+Math.toDegrees(angles[i]));

						
						//System.out.println("Rotating");
						// This command will drive the robot to target in dt sec.
						// The robot will now try to turn to the right location
						
						turnspeed = DEF_YAW_SPEED;
						
						runtime = Math.abs(angles[i]/turnspeed);
						runtimems = (long) (runtime * 1000);
						
						System.out.println("Turning runtime= "+runtime+"s or "+runtimems+"ms");
						// Start turning
						if (angles[i] > 0) 
							posi[i].setSpeed(0, turnspeed);
						else
							posi[i].setSpeed(0, -turnspeed);
						try { Thread.sleep (runtimems); } catch (Exception e) { }
						posi[i].setSpeed(0,0);
						System.out.println("Turning finished");
						
						// Start driving towards the target
						runtimems = (long) (dt * 1000);
						System.out.println("Driving runtime= "+dt+"s or "+runtimems+"ms with speed "+d2t);
						posi[i].setSpeed(d2t, 0);
						try { Thread.sleep (runtimems); } catch (Exception e) { }
						posi[i].setSpeed(0,0);
						System.out.println("Driving finished");
						
						// Consensus not done yet
						conSuc=conSuc+1;
					}
				}
				else {
					System.out.println("Found no fiducials for robot "+(i+1));
					conSuc=1;
				}
			}
			
			try { Thread.sleep (500); } catch (Exception e) { }
			
			if (conSuc == 0) 
				conSuccess=true;
			
		}
		
		// Consensus succussful, stop movement of robots.
		for (int i=0; i < numRobot; i++)
			posi[i].setSpeed(0, 0);
	}
}