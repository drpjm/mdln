package edu.gatech.grits.mdln.lang.util;

import javolution.util.FastList;
import javolution.util.FastMap;
import edu.gatech.grits.util.DataVector;

public interface BuddyMapAdapter {

	FastList<Integer> checkBuddies(DataVector localData /*, neighborhood  */);
}
