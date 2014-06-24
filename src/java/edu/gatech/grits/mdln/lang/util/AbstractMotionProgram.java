package edu.gatech.grits.mdln.lang.util;

import javolution.util.FastList;

public abstract class AbstractMotionProgram {
	
	protected FastList<? extends AbstractMode> modes;

	public FastList<? extends AbstractMode> getModes() {
		return modes;
	}
	
	

}

