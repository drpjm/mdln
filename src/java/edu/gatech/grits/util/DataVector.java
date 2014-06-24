package edu.gatech.grits.util;

import javolution.util.*;

/**
 * Class that contains data that an agent can use in its algorithms.
 * @author pmartin
 *
 */
public class DataVector {

	private Integer id;
	private FastMap<SensorType,FastList<Float>> dataMap;
	

	/**
	 * @param id
	 */
	public DataVector(Integer id) {
		super();
		this.id = id;
		dataMap = new FastMap<SensorType,FastList<Float>>();
	}

	/**
	 * @param dataMap
	 * @param id
	 */
	public DataVector(FastMap<SensorType, FastList<Float>> dataMap, Integer id) {
		super();
		this.dataMap = dataMap;
		this.id = id;
	}

	public FastMap<SensorType, FastList<Float>> getDataMap() {
		return dataMap;
	}

	public Integer getId() {
		return id;
	}

	public void updateData(SensorType st, FastList<Float> data){
		dataMap.put(st, data);
	}
	
	public FastList<Float> getDataFrom(SensorType st){
		return dataMap.get(st);
	}
	public boolean hasDataFrom(SensorType st){
		if(dataMap.containsKey(st)){
			return true;
		}
		else
			return false;
	}
	
	public void clearData(){
		dataMap.clear();
	}
	
	@Override
	public String toString() {
		String str = "id: " + id + ", " ;
		str += dataMap.toString();
		return str;
	}
	
}
