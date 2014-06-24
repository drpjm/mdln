package edu.gatech.grits.app;

import java.util.Timer;
import java.util.TimerTask;

import javolution.util.FastList;
import javolution.util.FastMap;

import edu.gatech.grits.mdln.lang.util.AbstractMode;
import edu.gatech.grits.mdln.lang.util.ControlParam;
import edu.gatech.grits.mdln.lang.util.MDLnMode;
import edu.gatech.grits.pancakes.agent.*;
import edu.gatech.grits.pancakes.structures.*;
import edu.gatech.grits.pancakes.util.AgentProperties;
import edu.gatech.grits.util.*;

public class MDLnController extends PancakesListener {

	private long CTRL_PERIOD = 100;
	private long UPDATE_DELAY = 2500;
	private long REQ_DELAY = 800;
	private long TRANSMIT_DELAY = 500;

	private NewMDLnAgent agent;
	private SyrupPacket cachedPacket;
	private Timer engineTimer;

	private Integer myRole;
	private DataVector myData;
	private FastMap<Integer,DataVector> buddyData;

	//Motion program
	private FastList<MDLnMode> modes;
	private int modeIndex;
	private long modeStartTime;
	private boolean isProgramReady;
	private boolean isNewProgram;
	private boolean isEnabled;
	private boolean isDead;

	private FastMap<Integer, NeighborStatus> neighborStatusTable;
	private long lastNetworkUpdate;
	private long lastDataTransmit;
	private long lastRequest;

	public MDLnController(NewMDLnAgent a) {
		super();
		agent = a;
		cachedPacket = new SyrupPacket("null", 0);
		engineTimer = new Timer();

		myRole = 0;
		myData = new DataVector(agent.getMyId());
		neighborStatusTable = new FastMap<Integer,NeighborStatus>();

		isEnabled = false;
		isProgramReady = false;

		FastList<Integer> currNet = agent.getNeighbors();
		for(Integer neighbor : currNet){
			neighborStatusTable.put(neighbor, new NeighborStatus(false,false));
		}
		lastNetworkUpdate = System.currentTimeMillis();
		lastRequest = System.currentTimeMillis();

		// the GUTS!
		isDead = false;
		engineTimer.scheduleAtFixedRate(new TimerTask(){

			@Override
			public void run() {
				//				System.out.println("MDLnAgent: " + Thread.currentThread());
				getSensorData();
				if(isEnabled){

					//apply MDLn program
					if(isProgramReady){
						executeProgram();
					}
					// send shareable information to all neighbors who are receiving
					long currTime1 = System.currentTimeMillis();
					if((currTime1 - lastDataTransmit) > TRANSMIT_DELAY){
						sendShareableInfo();
						lastDataTransmit = System.currentTimeMillis();
					}

				}

				//look for new message
				processMessage(agent.getMessage());

				long currTime2 = System.currentTimeMillis();
				if((currTime2 - lastNetworkUpdate) > UPDATE_DELAY){
					handleNetworkUpdate();
					if(neighborStatusTable.containsKey(0)){
						updateGui();
					}
					lastNetworkUpdate = currTime2;
					System.out.println("MDLnAgent" + agent.getMyId() + ": " + neighborStatusTable.size() + " neighbor(s)" );
				}

				if(isDead){
					System.out.println("Agent" + agent.getMyId() + " is down.");
					engineTimer.cancel();
					agent.stopRequest();
				}


			}

		}, 0, this.CTRL_PERIOD);
	}

	/**
	 * This method pulls in the current data from the feed.
	 */
	private final void getSensorData(){
		SyrupPacket currPacket = null;
		currPacket = this.read();
		// process packet
		if(currPacket != null){
			// update the cached packet if the new one is not null
			cachedPacket = currPacket;
		}
		for(int i = 0; i < cachedPacket.getSize(); i++){
			Packet p = cachedPacket.getPacket(i);
			//			p.debug();
			if(p.getPacketType().toUpperCase().equals(SensorType.SONAR.toString())){
				FastList<Float> sonarRead = FastList.newInstance();
				for(float f : ((SonarPacket) p).getSonarReadings()){
					sonarRead.add(f);
				}
				if(!sonarRead.isEmpty()){
					myData.getDataMap().remove(SensorType.SONAR);
					myData.getDataMap().put(SensorType.SONAR, sonarRead);
				}
			}
			if(p.getPacketType().toUpperCase().equals(SensorType.LOCAL.toString())){
				FastList<Float> localRead = FastList.newInstance();
				localRead.add(((LocalPosePacket)p).getPositionX());
				localRead.add(((LocalPosePacket)p).getPositionY());
				localRead.add(((LocalPosePacket)p).getTheta());
				myData.getDataMap().remove(SensorType.LOCAL);
				myData.getDataMap().put(SensorType.LOCAL, localRead);				
			}
			if(p.getPacketType().toUpperCase().equals(SensorType.IR.toString())){
				FastList<Float> irRead = FastList.newInstance();
				for(float f : ((IRPacket) p).getIRReadings()){
					irRead.add(f);
				}
				myData.getDataMap().remove(SensorType.IR);
				myData.getDataMap().put(SensorType.IR, irRead);
			}

		}
//		System.out.println(myData);
	}

	private final void executeProgram(){

		//		System.out.println("Executing!");

		//check for end of program
		if(modeIndex < modes.size()){

			MDLnMode currMode = modes.get(modeIndex);
			// record start time of first mode
			if(modeIndex == 0 && isNewProgram){

				modeStartTime = System.currentTimeMillis();
				System.out.println("--- Agent" + agent.getMyId() + " program started ---");
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

			System.out.println("Agent" + agent.getMyId() + " completed program.");
		}

	}

	private final void processMessage(final SyrupPacket msg){
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

					Packet pkt = msg.getPacket(i);
					if(pkt.getPacketType().equals("AgentRole")){
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
					SyrupPacket finished = new SyrupPacket(MDLnMessage.FINISHED, agent.getMyId());
					// make sure to clean up with buddies and agents receiving data!
					if(e.getValue().isReceiving() || e.getValue().isBuddy()){
						agent.sendMessage(e.getKey(), finished);
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
				SyrupPacket kill = new SyrupPacket(MDLnMessage.KILL, agent.getMyId());
				System.out.println("Agent " + agent.getMyId() + " going down...");
				agent.sendMessage(0, kill);
				isDead = true;
			}

			else {
				//				System.err.println("Warning: Message type " + msg.getType() + " not supported.");
			}

		}
	}

	private final void handleBuddyReq(final SyrupPacket msg){
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
			SyrupPacket ackMsg = new SyrupPacket(MDLnMessage.BUDDYACK, agent.getMyId());
			agent.sendMessage(requestId, ackMsg);
			//			robotHandle.getNetworkManager().sendPacket(requestId, ackMsg);
		}
	}

	private void handleBuddyAck(final SyrupPacket msg) {
		System.out.println("Received ACK from " + msg.getSenderID());
		neighborStatusTable.get(msg.getSenderID()).setBuddy(true);
		buddyData.put(msg.getSenderID(), new DataVector(msg.getSenderID()));
		neighborStatusTable.get(msg.getSenderID()).setSharing(true);
	}


	private final void handleBuddyDone(final SyrupPacket msg){
		System.out.println(msg.getSenderID() + " is done!");
		// disable buddy flag, disable receiving data flag
		if(neighborStatusTable.get(msg.getSenderID()).isBuddy()){
			neighborStatusTable.get(msg.getSenderID()).setBuddy(false);
		}
		if(neighborStatusTable.get(msg.getSenderID()).isReceiving()){
			neighborStatusTable.get(msg.getSenderID()).setReceiving(false);
		}
	}

	private final void handleBuddyData(final SyrupPacket msg) {
		System.out.println("Receiving data from " + msg.getSenderID());
		// extract incoming data
		Integer buddyId = msg.getSenderID(); 
		//		System.out.println(buddyData.get(buddyId));
		if(buddyData.get(buddyId) != null){
			if(buddyData.containsKey(buddyId)){
				for(int i = 0; i < msg.getSize(); i++){
					if(msg.getPacket(i).getPacketType().toUpperCase().equals(SensorType.SONAR.toString())){
						SonarPacket sonar = (SonarPacket) msg.getPacket(i);
						FastList<Float> sonarData = new FastList<Float>();

						for(float f : sonar.getSonarReadings()){
							sonarData.add(f);
						}
						buddyData.get(buddyId).updateData(SensorType.SONAR, sonarData);
					}
					else if(msg.getPacket(i).getPacketType().toUpperCase().equals(SensorType.IR.toString())){
						IRPacket ir = (IRPacket) msg.getPacket(i);
						FastList<Float> irData = new FastList<Float>();
						for(float f : ir.getIRReadings()){
							irData.add(f);
						}
						buddyData.get(buddyId).updateData(SensorType.IR, irData);
					}
					else if(msg.getPacket(i).getPacketType().toUpperCase().equals(SensorType.LOCAL.toString())){
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

	private final void handleNetworkUpdate(){
		//update the network
		FastList<Integer> currNet = agent.getNeighbors();

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

	private final void notifyBuddies(final FastList<String> currBuddies, final FastList<String> nextBuddies){
		if(nextBuddies != null){
			for(String buddyName : currBuddies){
				// as long as the buddy is not in next list, send DONE
				Integer buddyId = agent.getMyProps().getIdMap().get(buddyName);
				if(neighborStatusTable.containsKey(buddyId)){
					if(!nextBuddies.contains(buddyName) && neighborStatusTable.get(buddyId).isBuddy()){
						//						System.out.println("Notify " + buddyName + " that mode is done.");
						SyrupPacket done = new SyrupPacket(MDLnMessage.BUDDYDONE, agent.getMyId());
						agent.sendMessage(agent.getMyProps().getIdMap().get(buddyName), done);
//						robotHandle.getNetworkManager().sendPacket(myProps.getIdMap().get(buddyName), done);
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
				Integer buddyId = agent.getMyProps().getIdMap().get(name);
				//				System.out.println("Notify " + name + " that program is done.");
				SyrupPacket done = new SyrupPacket(MDLnMessage.BUDDYDONE, agent.getMyId());
				if(neighborStatusTable.containsKey(buddyId)){
					if(neighborStatusTable.get(buddyId).isBuddy()){
						agent.sendMessage(agent.getMyProps().getIdMap().get(name), done);
//						robotHandle.getNetworkManager().sendPacket(myProps.getIdMap().get(name), done);
						//clear out current buddy data entry
						buddyData.get(buddyId).clearData();
						buddyData.remove(buddyId);
						//						System.out.println(buddyData);
					}
				}
			}
		}
	}

	private final void requestBuddies(final FastList<String> buddies){
		if((System.currentTimeMillis() - lastRequest) > this.REQ_DELAY){
			// for each buddy, check its status in the neighbor table
			for(String buddyName : buddies){
				Integer buddyId = agent.getMyProps().getIdMap().get(buddyName);
				if(neighborStatusTable.containsKey(buddyId)){
					if(!neighborStatusTable.get(buddyId).isBuddy()){
						//						System.out.println("Send request to " + buddyName);
						SyrupPacket request = new SyrupPacket(MDLnMessage.BUDDYREQ, agent.getMyId());
						RolePacket rp = new RolePacket("AgentRole");
						rp.addRole(this.myRole);
						request.addPacket(rp);
						agent.sendMessage(buddyId, request);
					}
				}

			}
			lastRequest = System.currentTimeMillis();
		}
	}

	private final void sendShareableInfo(){
		//iterate through the neighbor table
		for (FastMap.Entry<Integer, NeighborStatus> e = neighborStatusTable.head(), end = neighborStatusTable.tail(); (e = e.getNext()) != end;) {
			//send information to allowable neighbors
			if(e.getValue().isReceiving()){

				System.out.println("Send data to +" + e.getKey() + "!");
				SyrupPacket dataMsg = new SyrupPacket(MDLnMessage.DATA, agent.getMyId());

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
						SonarPacket sp = new SonarPacket();
						sp.setSonarReadings(sonar);
						dataMsg.addPacket(sp);
					}
					else if(curr.getKey().equals(SensorType.IR)){
						FastList<Float> irData = curr.getValue();
						float[] ir = new float[irData.size()];
						for(int i = 0; i < irData.size(); i++){
							ir[i] = irData.get(i);
						}						
						IRPacket irp = new IRPacket();
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
				agent.sendMessage(e.getKey(), dataMsg);
				//						robotHandle.getNetworkManager().sendPacket(e.getKey(), dataMsg);
			}
		}
	}		

	private final void setControl(final ControlParam ctrlParam){
		MotorPacket command = new MotorPacket();
		command.setVelocity(ctrlParam.getTranslationVel());
		command.setRotationalVelocity(ctrlParam.getRotationVel());

		SyrupPacket msg = new SyrupPacket("request", 1);
		msg.addPacket(command);

		//		robotHandle.getDeviceManager().processSyrupPacket(msg);
		agent.pushRequest(msg);
	}

	private final void updateGui() {
		SyrupPacket updateMsg = new SyrupPacket(MDLnMessage.UPDATE, agent.getMyId());
		//		robotHandle.getNetworkManager().sendPacket(0, updateMsg);
		agent.sendMessage(0, updateMsg);
	}


	private final void handleFinished(final SyrupPacket msg) {
		System.out.println(msg.getSenderID() + " is finished. Clean up!");
		if(neighborStatusTable.get(msg.getSenderID()).isReceiving()){
			neighborStatusTable.get(msg.getSenderID()).setReceiving(false);
		}
		if(neighborStatusTable.get(msg.getSenderID()).isBuddy()){
			neighborStatusTable.get(msg.getSenderID()).setBuddy(false);
		}
	}
	private final void stopRobot(){
		MotorPacket command = new MotorPacket();
		command.setVelocity(0);
		command.setRotationalVelocity(0);
		SyrupPacket msg = new SyrupPacket("request", 1);
		msg.addPacket(command);
		System.out.println("STOP ME!");
		agent.pushRequest(msg);
	}

}
