package edu.gatech.grits.mdln.lang.util;

import edu.gatech.grits.util.DataVector;
import javolution.util.*;

/**
 * Interface used to define control functions for use by Modes.
 * @author pmartin
 *
 */
public interface ControlAdapter {
	ControlParam applyControl(DataVector localData, FastMap<Integer,DataVector> buddyData);
}
