package edu.gatech.grits.app;

import edu.gatech.grits.pancakes.util.Properties;


public class TestApp {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		NewMDLnAgent ma = new NewMDLnAgent(new Properties(args[0]), args[1]);
		System.err.println("Test application started!");
		while(ma.isAlive());
		System.exit(0);
	}

}
