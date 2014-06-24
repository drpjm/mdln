package edu.gatech.grits.mdln.lang.util;

import edu.gatech.grits.util.DataVector;
import javolution.util.FastList;
import javolution.util.FastMap;

/**
 * Interface used for calling implemented interrupt functions when running a mode string.
 * @author pmartin
 *
 */
public interface InterruptAdapter {

//	boolean isInterrupted(FastMap<SensorId, Object> stateVector);
//	boolean isInterrupted(DataVector localData, FastList<DataVector> neighborData);
	boolean isInterrupted(DataVector localData, FastMap<Integer,DataVector> neighborData);
}
