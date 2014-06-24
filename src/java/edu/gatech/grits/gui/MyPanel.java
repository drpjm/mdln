package edu.gatech.grits.gui;

import javax.swing.JPanel;

import javolution.util.FastList;

/*
 * Abstract class used for convenience when building GUIs.
 */
public abstract class MyPanel extends JPanel implements PanelObservable {

	protected FastList<PanelObservable> observers;
	
	protected abstract void buildContent();
	protected abstract void buildLayout();
	
	public final void addObserver(PanelObservable po){
		if(observers != null && !observers.contains(po)){
			observers.add(po);
		}
	}
}
