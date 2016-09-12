package com.ibm.commerce.cache;

public interface TimeInterval {
	
	/**
	 * 
	 * @return the time interval index.
	 */
	public long getIndex();
	
	/**
	 * 
	 * @param indexValue The new index value
	 */
	public void setIndex(long indexValue);

}
