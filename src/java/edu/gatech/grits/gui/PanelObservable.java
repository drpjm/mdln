package edu.gatech.grits.gui;

import java.util.ArrayList;

public interface PanelObservable {

	public void notifyChange(ObserverPacket message);
	
}
