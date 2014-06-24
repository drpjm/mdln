package edu.gatech.grits.gui;

public class ObserverPacket {

	private MessageType dataType;
	private Object data; 
	
	public ObserverPacket(MessageType dataType, Object data) {
		super();
		this.dataType = dataType;
		this.data = data;
	}

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}

	public MessageType getDataType() {
		return dataType;
	}

	public void setDataType(MessageType dataType) {
		this.dataType = dataType;
	}
	
	public enum MessageType {
		
		PORT_OPEN(0),
		PORT_CLOSE(1),
		SEND_DATA(2),
		NEW_DATA(3),
		PRINT(4),
		START(5),
		STOPPED(6),
		CANCELLED(7),
		NEW_PROGRAM(8),
		KILLED(9),
		START_ALL(10);
		
		private int type;
		private MessageType(int t){
			type = t;
		}
		public int getType() {
			return type;
		}
		
	}
	
}
