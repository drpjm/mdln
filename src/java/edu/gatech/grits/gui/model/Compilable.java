package edu.gatech.grits.gui.model;

import java.io.*;

public interface Compilable {

	public boolean compile(File inputFile);
	public boolean isProgramReady();
	public void clearProgram();
	
}
