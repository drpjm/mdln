package edu.gatech.grits.gui.model;

import edu.gatech.grits.mdln.lang.util.MDLnProgram;

public abstract class CompilerBackend implements Compilable {

	protected MDLnProgram currProgram;

	public MDLnProgram getCurrProgram() {
		return currProgram;
	}
	
	
}
