package edu.gatech.grits.mdln.control;

import javolution.util.FastList;
import javolution.util.FastMap;
import edu.gatech.grits.mdln.lang.util.ControlAdapter;
import edu.gatech.grits.mdln.lang.util.ControlParam;
import edu.gatech.grits.util.DataVector;

public class Forward implements ControlAdapter {

	private final float MAX_TRANS = 0.3f;
	
	public ControlParam applyControl(DataVector localData,
			FastMap<Integer, DataVector> neighborData) {
		return new ControlParam(MAX_TRANS,0);
	}

}
