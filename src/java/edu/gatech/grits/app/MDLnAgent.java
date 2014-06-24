package edu.gatech.grits.app;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Timer;

import javolution.util.FastList;
import javolution.util.FastMap;

import edu.gatech.grits.mdln.lang.util.AbstractMode;
import edu.gatech.grits.mdln.lang.util.ControlParam;
import edu.gatech.grits.mdln.lang.util.MDLnMode;
import edu.gatech.grits.pancakes.manager.PancakesManager;
import edu.gatech.grits.pancakes.structures.*;
import edu.gatech.grits.pancakes.util.AgentProperties;
import edu.gatech.grits.util.*;

//TODO: DEVELOP MORE FLEXIBLE SENSOR DATA STRUCTURES!

/**
 * Class that encapsulates and MDLn enabled software agent. The MDLnAgent drives the current MDLn string
 * and outputs controls and buddy lists to the Pancakes subsystem.
 * @author pmartin
 *
 */
public class MDLnAgent implements TimerListener {

	private long CTRL_PERIOD = 100;
	private long UPDATE_DELAY = 2500;
	private long REQ_DELAY = 800;
	private long TRANSMIT_DELAY = 500;
	private final String PRIMARY = "Primary";

	private int myId;
	private Integer myRole;
	private DataVector myData;
	private FastMap<Integer,DataVector> buddyData;

	private FastMap<Integer, NeighborStatus> neighborStatusTable;
	private long lastNetworkUpdate;
	private long lastDataTransmit;
	private long lastRequest;

	private AgentProperties myProps;

	// thread timer for pulling data
	private Timer engineTimer;
	//Pancakes handle!
	private PancakesManager robotHandle;
	//for user interface
	private boolean isEnabled;
	//Motion program
	private FastList<MDLnMode> modes;
	private int modeIndex;
	private long modeStartTime;
	private boolean isProgramReady;
	private boolean isNewProgram;
	private boolean isRepeating;

	public MDLnAgent(String propFile){

		myProps = new AgentProperties(propFile);

		this.myId = myProps.getId();
		this.myRole = 0;


		try {
			InetAddress ip = InetAddress.getLocalHost();
			//			robotHandle = new PancakesManager(id, ip.getHostAddress(), netPort, myProps.getBackend(), devPort);
			robotHandle = new PancakesManager(myProps);

			isEnabled = false;
			isProgramReady = false;
			isRepeating = false;
			modes = new FastList<MDLnMode>();
			modeIndex = 0;

			myData = new DataVector(new FastMap<SensorType, FastList<Float>>(), this.myId);
			buddyData = new FastMap<Integer,DataVector>();

			neighborStatusTable = new FastMap<Integer,NeighborStatus>();

			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			engineTimer = new Timer();
			engineTimer.scheduleAtFixedRate(new SimpleTask(this.PRIMARY, this), 0, this.CTRL_PERIOD);

			FastList<Integer> currNet = robotHandle.getNetworkManager().getNeighbors();
			for(Integer neighbor : currNet){
				neighborStatusTable.put(neighbor, new NeighborStatus(false,false));
			}
			lastNetworkUpdate = System.currentTimeMillis();
			lastRequest = System.currentTimeMillis();

			System.out.println("MDLnAgent " + this.myId + " alive.");

		} catch (UnknownHostException e1) {
			e1.printStackTrace();
		}

	}

	/*
	 * This function is the guts of the MDLn Agent. It responds to a scheduled task from a timer.
	 * It executes commands from the PancakesManager for setting control velocities and agent
	 * buddies.
	 * 
	 * @see edu.gatech.ece.grits.util.TimerListener#onTimer(edu.gatech.ece.grits.util.TimerEvent)
	 */
	public final void onTimer(TimerEvent te) {

		//		long time1 = System.currentTimeMillis();
		if(te.getTimerId().equals(this.PRIMARY)){
			getSensorData();
			if(isEnabled){
				//apply MDLn program
				if(isProgramReady){
					executeProgram();
				}
				// send shareable information to all neighbors who are receiving
				long currTime1 = System.currentTimeMillis();
				if((currTime1 - lastDataTransmit) > this.TRANSMIT_DELAY){
					sendShareableInfo();
					lastDataTransmit = System.currentTimeMillis();
				}
			}

			//look for new message
			SyrupPacket msg = robotHandle.getNetworkManager().getPacket();
			processMessage(msg);

			long currTime2 = System.currentTimeMillis();
			if((currTime2 - lastNetworkUpdate) > this.UPDATE_DELAY){
				handleNetworkUpdate();
				if(neighborStatusTable.containsKey(0)){
					updateGui();
				}
				lastNetworkUpdate = currTime2;
				//				System.out.println("MDLnAgent" + this.myId + ": " + this.neighborStatusTable.size() + " neighbor(s)" );
			}
		}
		else {
			System.err.println("MDLnAgent: Unknown timer fired!");
		}
		//		long time2 = System.currentTimeMillis();
		//		System.out.println((time2 - time1));

	}

	private final void setControl(ControlParam param){
		MotorPacket command = new MotorPacket();
		command.setVelocity(param.getTranslationVel());
		command.setRotationalVelocity(param.getRotationVel());

		SyrupPacket msg = new SyrupPacket("request", 1);
		msg.addPacket(command);

		robotHandle.getDeviceManager().processSyrupPacket(msg);
	}

	private final void getSensorData(){
		SyrupPacket query = new SyrupPacket("query", this.myId);
		//add packet request for each available sensor
		for(String sensorName : myProps.getSensors()){
			if(SensorType.toSensorType(sensorName).equals(SensorType.SONAR)){
				query.addPacket(new SonarPacket(8));
			}
			if(SensorType.toSensorType(sensorName).equals(SensorType.IR)){
				query.addPacket(new IRPacket(8));
			}
			if(SensorType.toSensorType(sensorName).equals(SensorType.LOCAL)){
				query.addPacket(new LocalPosePacket());
			}
			//else ... MORE
		}

		SyrupPacket reply = robotHandle.getDeviceManager().processSyrupPacket(query);
		for(int i = 0; i < reply.getSize(); i++){

			if(reply.getPacket(i).getType().toUpperCase().equals(SensorType.SONAR.toString())){
				SonarPacket sonar = (SonarPacket) reply.getPacket(i);
				FastList<Float> sonarRead = new FastList<Float>();
				for(float f : sonar.getSonarReadings()){
					sonarRead.add(f);
				}
				if(!sonarRead.isEmpty()){
					myData.getDataMap().put(SensorType.SONAR, sonarRead);
				}
			}
			else if(reply.getPacket(i).getType().toUpperCase().equals(SensorType.IR.toString())){
				IRPacket ir = (IRPacket) reply.getPacket(i);
				FastList<Float> irRead = new FastList<Float>();
				for(float f : ir.getIRReadings()){
					irRead.add(f);
				}
				myData.getDataMap().put(SensorType.IR, irRead);
			}
			else if(reply.getPacket(i).getType().toUpperCase().equals(SensorType.LOCAL.toString())){
				LocalPosePacket lp = (LocalPosePacket) reply.getPacket(i);
				if(lp.isDataReady()){
					FastList<Float> lpRead = new FastList<Float>();
					lpRead.add(lp.getPositionX());
					lpRead.add(lp.getPositionY());
					lpRead.add(lp.getTheta());
					myData.getDataMap().put(SensorType.LOCAL, lpRead);
				}
			}

		}
	}

	private final void executeProgram(){
		//check for end of program
		if(modeIndex < modes.size()){

			MDLnMode currMode = modes.get(modeIndex);
			// record start time of first mode
			if(modeIndex == 0 && isNewProgram){

				modeStartTime = System.currentTimeMillis();
				System.out.println("--- Agent" + this.myId + " program started ---");
				isNewProgram = false;
			}

			// 1. check for interrupt
			boolean isInterrupted = currMode.getInterrupt().isInterrupted(myData, buddyData);


			// 2. check timer value, if it is NOT an infinite mode
			boolean isTimeUp = false;
			if(currMode.getTimerLength() != AbstractMode.INF_TIMER){
				//check time...
				long execTime = System.currentTimeMillis() - modeStartTime;
				if(execTime > currMode.getTimerLength()*1000){
					isTimeUp = true;
				}
			}

			// if it is not interrupted and time is not up, run current mode
			if(!isInterrupted && !isTimeUp){
				// send new controls
				ControlParam param = currMode.getControl().applyControl(myData, buddyData);
				setControl(param);

				// start buddy requests if there are any buddies
				if(!currMode.getStaticBuddies().isEmpty()){
					requestBuddies(currMode.getStaticBuddies());
				}
			}
			else{
				// index into next mode
				modeIndex++;
				//				System.out.println(modeIndex);
				if(modeIndex < modes.size()){
					// notify any buddies
					notifyBuddies(currMode.getStaticBuddies(), modes.get(modeIndex).getStaticBuddies());
				}
				isTimeUp = false;
				modeStartTime = System.currentTimeMillis();
			}

		}
		//program is done
		else{
			isProgramReady = false;
			stopRobot();
			notifyBuddies(modes.get(modeIndex-1).getStaticBuddies(), null);

			System.out.println("Agent" + this.myId + " completed program.");
		}

	}

	private final void requestBuddies(FastList<String> buddies){
		if((System.currentTimeMillis() - lastRequest) > this.REQ_DELAY){
			// for each buddy, check its status in the neighbor table
			for(String buddyName : buddies){

				Integer buddyId = myProps.getIdMap().get(buddyName);
				if(neighborStatusTable.containsKey(buddyId)){
					if(!neighborStatusTable.get(buddyId).isBuddy()){
						//						System.out.println("Send request to " + buddyName);
						SyrupPacket request = new SyrupPacket(MDLnMessage.BUDDYREQ, this.myId);
						RolePacket rp = new RolePacket("AgentRole");
						rp.addRole(this.myRole);
						request.addPacket(rp);
						robotHandle.getNetworkManager().sendPacket(buddyId, request);
					}
				}

			}
			lastRequest = System.currentTimeMillis();
		}
	}

	private final void notifyBuddies(FastList<String> currBuddies, FastList<String> nextBuddies){
		if(nextBuddies != null){
			for(String buddyName : currBuddies){
				// as long as the buddy is not in next list, send DONE
				Integer buddyId = myProps.getIdMap().get(buddyName);
				if(neighborStatusTable.containsKey(buddyId)){
					if(!nextBuddies.contains(buddyName) && neighborStatusTable.get(buddyId).isBuddy()){
//						System.out.println("Notify " + buddyName + " that mode is done.");
						SyrupPacket done = new SyrupPacket(MDLnMessage.BUDDYDONE, this.myId);
						robotHandle.getNetworkManager().sendPacket(myProps.getIdMap().get(buddyName), done);
						//clear out current buddy data entry
						buddyData.get(buddyId).clearData();
						buddyData.remove(buddyId);
//						System.out.println(buddyData);
					}
				}
			}
		}
		else{
			for(String name : currBuddies){
				Integer buddyId = myProps.getIdMap().get(name);
//				System.out.println("Notify " + name + " that program is done.");
				SyrupPacket done = new SyrupPacket(MDLnMessage.BUDDYDONE, this.myId);
				if(neighborStatusTable.containsKey(buddyId)){
					if(neighborStatusTable.get(buddyId).isBuddy()){
						robotHandle.getNetworkManager().sendPacket(myProps.getIdMap().get(name), done);
						//clear out current buddy data entry
						buddyData.get(buddyId).clearData();
						buddyData.remove(buddyId);
//						System.out.println(buddyData);
					}
				}
			}
		}
	}

	private final void processMessage(SyrupPacket msg){
		if(msg != null){

			// buddy protocols -- only run if the program is running.
			if(isEnabled){
				if(msg.getType().equals(MDLnMessage.BUDDYREQ)){
					handleBuddyReq(msg);
				}
				else if(msg.getType().equals(MDLnMessage.BUDDYDONE)){
					handleBuddyDone(msg);
				}
				else if(msg.getType().equals(MDLnMessage.BUDDYACK)){
					handleBuddyAck(msg);
				}
				else if(msg.getType().equals(MDLnMessage.DATA)){
					handleBuddyData(msg);
				}
				else if(msg.getType().equals(MDLnMessage.FINISHED)){
					handleFinished(msg);
				}
			}

			if(msg.getType().equals(MDLnMessage.PROGRAM)){
				modes = new FastList<MDLnMode>();
				modeIndex = 0;

				//unpack the program
				for(int i = 0; i < msg.getSize(); i++){

					XMLPacket pkt = msg.getPacket(i);
					if(pkt.getType().equals("AgentRole")){
						myRole = ((RolePacket) pkt).getRole();
					}
					//					else if(pkt.getType().equals("Repeating")){
					//						isRepeating = ((BooleanPacket)pkt).getBoolean();
					//					}
					else{
						ModePacket mp = (ModePacket) pkt;
						MDLnMode currMode = mp.extractMode();
						modes.add(currMode);
					}
				}
				isProgramReady = true;
				isNewProgram = true;
			}

			else if(msg.getType().equals(MDLnMessage.STOP)){
				isEnabled = false;
				isProgramReady = false;
				//clear program
				modes = null;
				//clear buddy status
				for (FastMap.Entry<Integer, NeighborStatus> e = neighborStatusTable.head(), end = neighborStatusTable.tail(); (e = e.getNext()) != end;) {
					SyrupPacket finished = new SyrupPacket(MDLnMessage.FINISHED, this.myId);
					// make sure to clean up with buddies and agents receiving data!
					if(e.getValue().isReceiving() || e.getValue().isBuddy()){
						robotHandle.getNetworkManager().sendPacket(e.getKey(), finished);
					}
					e.getValue().setBuddy(false);
					e.getValue().setReceiving(false);
					e.getValue().setSharing(false);
				}
				stopRobot();
			}

			else if(msg.getType().equals(MDLnMessage.START)){
				if(this.isProgramReady){
					isEnabled = true;
				}
			}

			else if(msg.getType().equals(MDLnMessage.KILL)){
				// notify gui agent 0
				SyrupPacket kill = new SyrupPacket(MDLnMessage.KILL, this.myId);
				robotHandle.getNetworkManager().sendPacket(0, kill);
				System.out.println("Agent " + this.myId + " going down...");
				System.exit(0);
			}

			else {
				//				System.err.println("Warning: Message type " + msg.getType() + " not supported.");
			}

		}
	}

	//	private final SyrupPacket assembleBuddyMsg(String msgType){
	//		// send buddy request
	//		SyrupPacket reqMsg = new SyrupPacket(msgType, this.myId);
	//		RolePacket rp = new RolePacket("AgentRole");
	//		rp.addRole(this.myRole);
	//		reqMsg.addPacket(rp);
	//		return reqMsg;
	//	}

	private final void handleBuddyReq(SyrupPacket msg){
		System.out.println("Received request from " + msg.getSenderID());

		Integer requestId = msg.getSenderID();
		RolePacket rp = (RolePacket)msg.getPacket(0);

		boolean sendAck = false;
		if(rp.getRole() >= myRole){
			neighborStatusTable.get(requestId).setReceiving(true);
			sendAck = true;
		}
		else{
			// check to see if this agent is a buddy (R3 in MDLn)
			if(neighborStatusTable.get(requestId).isBuddy()){
				neighborStatusTable.get(requestId).setReceiving(true);
				sendAck = true;
			}
			// otherwise, reject
			else{
				neighborStatusTable.get(requestId).setReceiving(false);
				sendAck = false;
			}
		}

		if(sendAck){
			SyrupPacket ackMsg = new SyrupPacket(MDLnMessage.BUDDYACK, this.myId);
			robotHandle.getNetworkManager().sendPacket(requestId, ackMsg);
		}
	}

	private void handleBuddyAck(SyrupPacket msg) {
		//		System.out.println("Received ACK from " + msg.getSenderID());
		neighborStatusTable.get(msg.getSenderID()).setBuddy(true);
		buddyData.put(msg.getSenderID(), new DataVector(msg.getSenderID()));
		//		neighborStatusTable.get(msg.getSenderID()).setSharing(true);
	}


	private final void handleBuddyDone(SyrupPacket msg){
		System.out.println(msg.getSenderID() + " is done!");
		// disable buddy flag, disable receiving data flag
		if(neighborStatusTable.get(msg.getSenderID()).isBuddy()){
			neighborStatusTable.get(msg.getSenderID()).setBuddy(false);
		}
		if(neighborStatusTable.get(msg.getSenderID()).isReceiving()){
			neighborStatusTable.get(msg.getSenderID()).setReceiving(false);
		}

	}

	private final void handleBuddyData(SyrupPacket msg) {
		System.out.println("Receiving data from " + msg.getSenderID());
		// extract incoming data
		Integer buddyId = msg.getSenderID(); 
//		System.out.println(buddyData.get(buddyId));
		if(buddyData.get(buddyId) != null){
			if(buddyData.containsKey(buddyId)){
				for(int i = 0; i < msg.getSize(); i++){
					if(msg.getPacket(i).getType().toUpperCase().equals(SensorType.SONAR.toString())){
						SonarPacket sonar = (SonarPacket) msg.getPacket(i);
						FastList<Float> sonarData = new FastList<Float>();

						for(float f : sonar.getSonarReadings()){
							sonarData.add(f);
						}
						buddyData.get(buddyId).updateData(SensorType.SONAR, sonarData);
					}
					else if(msg.getPacket(i).getType().toUpperCase().equals(SensorType.IR.toString())){
						IRPacket ir = (IRPacket) msg.getPacket(i);
						FastList<Float> irData = new FastList<Float>();
						for(float f : ir.getIRReadings()){
							irData.add(f);
						}
						buddyData.get(buddyId).updateData(SensorType.IR, irData);
					}
					else if(msg.getPacket(i).getType().toUpperCase().equals(SensorType.LOCAL.toString())){
						LocalPosePacket lpp = (LocalPosePacket) msg.getPacket(i);
						FastList<Float> lpData = new FastList<Float>();
						lpData.add(lpp.getPositionX());
						lpData.add(lpp.getPositionY());
						lpData.add(lpp.getTheta());
						buddyData.get(buddyId).updateData(SensorType.LOCAL, lpData);
					}
				}

			}

		}
		else{
			System.out.println("WTF!");
		}
	}

	private final void handleFinished(SyrupPacket msg) {
		//		System.out.println(msg.getSenderID() + " is finished. Clean up!");
		if(neighborStatusTable.get(msg.getSenderID()).isReceiving()){
			neighborStatusTable.get(msg.getSenderID()).setReceiving(false);
		}
		if(neighborStatusTable.get(msg.getSenderID()).isBuddy()){
			neighborStatusTable.get(msg.getSenderID()).setBuddy(false);
		}

	}

	private final void sendShareableInfo(){
		//iterate through the neighbor table
		for (FastMap.Entry<Integer, NeighborStatus> e = neighborStatusTable.head(), end = neighborStatusTable.tail(); (e = e.getNext()) != end;) {
			//send information to allowable neighbors
			if(e.getValue().isReceiving()){

				SyrupPacket dataMsg = new SyrupPacket(MDLnMessage.DATA, this.myId);

				// convert data map into new syrup packet
				FastMap<SensorType, FastList<Float>> data = myData.getDataMap();

				//TODO: this is UGLY! :(
				for(FastMap.Entry<SensorType,FastList<Float>> curr = data.head(), fin = data.tail(); (curr = curr.getNext()) != fin;){
					if(curr.getKey().equals(SensorType.SONAR)){
						FastList<Float> sonarData = curr.getValue();
						float[] sonar = new float[sonarData.size()];
						for(int i = 0; i < sonarData.size(); i++){
							sonar[i] = sonarData.get(i);
						}						
						SonarPacket sp = new SonarPacket(0);
						sp.setSonarReadings(sonar);
						dataMsg.addPacket(sp);
					}
					else if(curr.getKey().equals(SensorType.IR)){
						FastList<Float> irData = curr.getValue();
						float[] ir = new float[irData.size()];
						for(int i = 0; i < irData.size(); i++){
							ir[i] = irData.get(i);
						}						
						IRPacket irp = new IRPacket(0);
						irp.setIRReadings(ir);
						dataMsg.addPacket(irp);
					}
					else if(curr.getKey().equals(SensorType.LOCAL)){
						LocalPosePacket lpp = new LocalPosePacket();
						float x = curr.getValue().get(0);
						float y = curr.getValue().get(1);
						float yaw = curr.getValue().get(2);
						lpp.setPose(x, y, yaw);
						dataMsg.addPacket(lpp);
					}
				}
//				System.out.println("Send data to " + e.getKey());
				robotHandle.getNetworkManager().sendPacket(e.getKey(), dataMsg);
			}
		}		
	}

	private final void stopRobot(){
		MotorPacket command = new MotorPacket();
		command.setVelocity(0);
		command.setRotationalVelocity(0);
		SyrupPacket msg = new SyrupPacket("request", 1);
		msg.addPacket(command);
		robotHandle.getDeviceManager().processSyrupPacket(msg);
	}

	private final void handleNetworkUpdate(){
		//update the network
		FastList<Integer> currNet = robotHandle.getNetworkManager().getNeighbors();

		//if the network shrank, we need to update the table
		if(currNet.size() < neighborStatusTable.size()){
			//			FastMap<Integer, NeighborStatus> newTable = new FastMap<Integer, NeighborStatus>();
			FastMap<Integer, NeighborStatus> newTable = new FastMap<Integer, NeighborStatus>();
			for (FastMap.Entry<Integer, NeighborStatus> e = neighborStatusTable.head(), end = neighborStatusTable.tail(); (e = e.getNext()) != end;) {
				if(currNet.contains(e.getKey())){
					newTable.put(e.getKey(), e.getValue());
				}
			}
			neighborStatusTable = newTable;
		}
		//otherwise, business as usual
		else{
			for(Integer neighbor : currNet){
				// check to see if the neighbor is already here
				if(!neighborStatusTable.containsKey(neighbor)){
					neighborStatusTable.put(neighbor, new NeighborStatus(false, false));
				}
			}
		}
	}

	private final void updateGui() {
		SyrupPacket updateMsg = new SyrupPacket(MDLnMessage.UPDATE, this.myId);
		robotHandle.getNetworkManager().sendPacket(0, updateMsg);
	}

//	private final FastList<String> getNextBuddyList(){
//		if(modeIndex+1 < modes.size()){
//			return modes.get(modeIndex+1).getStaticBuddies();
//		}
//		else{
//			return null;
//		}
//	}

	public static void main(String[] args){

		//		Integer id = Integer.parseInt(args[0]);
		//		String robotType = args[1];
		//		Integer devPort = Integer.parseInt(args[2]);
		//		Integer netPort = Integer.parseInt(args[3]);
		MDLnAgent engine = new MDLnAgent(args[0]);

	}

}
