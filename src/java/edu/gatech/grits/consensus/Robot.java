package edu.gatech.grits.consensus;
/* This class defines besic behaviors of the robots
 */

import java.lang.System;
import javaclient2.PlayerClient;
import javaclient2.PlayerException;
import javaclient2.Position2DInterface;
import javaclient2.FiducialInterface;
import javaclient2.structures.fiducial.*;
import javaclient2.structures.PlayerConstants;

public class Robot {
	// Playerclient instance for the robot
	private PlayerClient 			player;
	// Position Interface
	private Position2DInterface 	posi;
	// Fiducial Interface
	private FiducialInterface   	fudi;
	// Fiducial Data (this Data is updated periodically
	private PlayerFiducialData		fudiData;
	// Fiducial items (they are updated when Fiducial Data is updated)
	public PlayerFiducialItem[]  	fiducials;
	
	// The assigned number of the robot
	private int						number;
	
	// Mission Successful
	public 	boolean					missionSuc;
	
	// Static constants
	// define the default rotational speed in rad/s
	static float DEF_YAW_SPEED   = 5f;
	
	Robot (int robotnum){
		// This constructor will request position and fiducial interface through playerclient
		// It will also start a threaded copy of playerclient
		try {
			// Connect to the Player server and request access to Position and Laser
			player  = new PlayerClient ("localhost", 6665);
			posi = player.requestInterfacePosition2D (robotnum, PlayerConstants.PLAYER_OPEN_MODE);
			fudi = player.requestInterfaceFiducial	(robotnum, PlayerConstants.PLAYER_OPEN_MODE);
		} catch (PlayerException e) {
			System.err.println ("Consensus: > Error connecting to Player: ");
			System.err.println ("    [ " + e.toString() + " ]");
			System.exit (1);
		}
		
		number = robotnum;
		
		player.runThreaded (-1, -1);
	}
	
	public int getFiducials (){
		// This method gets all the fiducials, and return fiducial count
		player.readAll();
		int fiducialcount;
		
		if (fudi.isDataReady())
			fudiData=fudi.getData();
		else {
			while (!fudi.isDataReady())
				fudiData=fudi.getData();
		}
		
		if (fudiData == null) {
			fiducialcount = 0;
		}
		else
			fiducialcount = fudiData.getFiducials_count();
		
		if (fiducialcount != 0) {
			fiducials = new PlayerFiducialItem [fiducialcount];
			fiducials = fudiData.getFiducials();
		}
		
		return fiducialcount;
	}
	
	public void rotate (float angles) {
		// This method rotates the robot for the angle(in rad), positive angle = counterclockwise
		// negative angle = clockwise with the DEFAULT turnspeed
		System.out.println("Angle to turn = "+Math.toDegrees(angles));
		
		float turnspeed = DEF_YAW_SPEED;
		float runtime = Math.abs(angles/turnspeed);
		long runtimems = (long) (runtime * 1000);
		
		// Start turning
		if (angles > 0) 
			posi.setSpeed(0, turnspeed);
		else
			posi.setSpeed(0, -turnspeed);
		try { Thread.sleep (runtimems); } catch (Exception e) { }
		posi.setSpeed(0,0);
		System.out.println("Turning finished");
	}
	
	public void rotate (float angles, float turnspeed) {
		// This method rotates the robot for the angle(in rad), positive angle = counterclockwise
		// negative angle = clockwise with the turnspeed
		System.out.println("Angle to turn = "+Math.toDegrees(angles));
		
		float runtime = Math.abs(angles/turnspeed);
		long runtimems = (long) (runtime * 1000);
		
		// Start turning
		if (angles > 0) 
			posi.setSpeed(0, turnspeed);
		else
			posi.setSpeed(0, -turnspeed);
		try { Thread.sleep (runtimems); } catch (Exception e) { }
		posi.setSpeed(0,0);
		System.out.println("Turning finished");
	}
	
	
	public void driveTo (float targetx, float targety) {
		// This method drives the robot to a set point
		// with the DEFAULT turnspeed
		
		float angles = (float)Math.atan2(targety,targetx);	//this angle go from -pi to pi
		System.out.println("Angle to turn = "+Math.toDegrees(angles));
		
		float turnspeed = DEF_YAW_SPEED;
		float runtime = Math.abs(angles/turnspeed);
		long runtimems = (long) (runtime * 1000);
		float d2t=(float)Math.sqrt(targetx*targetx+targety*targety);
		
		// Start turning
		if (angles > 0) 
			posi.setSpeed(0, turnspeed);
		else
			posi.setSpeed(0, -turnspeed);
		try { Thread.sleep (runtimems); } catch (Exception e) { }
		posi.setSpeed(0,0);
		System.out.println("Turning finished");
		
		// Start driving towards the target
		runtimems = (long) (1000);
		System.out.println("Driving robot "+(number+1)+" runtime= 1000 ms with speed "+d2t);
		posi.setSpeed(d2t, 0);
		try { Thread.sleep (runtimems); } catch (Exception e) { }
		posi.setSpeed(0,0);
		System.out.println("Driving finished");
	}
		
	public 	void driveTo (float targetx, float targety, float turnspeed) {
		// This method drives the robot to a set point  with the turnspeed
		
		float angles = (float)Math.atan2(targety,targetx);	//this angle go from -pi to pi
		System.out.println("Angle to turn = "+Math.toDegrees(angles));
		
		float runtime = Math.abs(angles/turnspeed);
		long runtimems = (long) (runtime * 1000);
		float d2t=(float)Math.sqrt(targetx*targetx+targety*targety);
		
		// Start turning
		if (angles > 0) 
			posi.setSpeed(0, turnspeed);
		else
			posi.setSpeed(0, -turnspeed);
		try { Thread.sleep (runtimems); } catch (Exception e) { }
		posi.setSpeed(0,0);
		System.out.println("Turning finished");
		
		// Start driving towards the target
		runtimems = (long) (1 * 1000);
		System.out.println("Driving robot "+(number+1)+" runtime= 1000 ms with speed "+d2t);
		posi.setSpeed(d2t, 0);
		try { Thread.sleep (runtimems); } catch (Exception e) { }
		posi.setSpeed(0,0);
		System.out.println("Driving finished");
	}
	
	public void stop () {
		// Emergency Stop
		posi.setSpeed(0, 0);
	}
	
	public void manualDriveMode (float translation, float rotation, long runtimems){
		// This function manual drive the robot with translational (m/s) and rotational speed (rad/s) given
		// for a period of runtime ms

		System.out.println("Driving robot "+(number+1)+" runtime="+runtimems+"ms with translational speed "+translation+"m/s and rotational speed"+rotation+"rad/s");
		posi.setSpeed(translation, rotation);
		try { Thread.sleep (runtimems); } catch (Exception e) { }
		posi.setSpeed(0,0);
		System.out.println("Driving finished");
	}

	public int getNumber() {
		return number;
	}
	
}
