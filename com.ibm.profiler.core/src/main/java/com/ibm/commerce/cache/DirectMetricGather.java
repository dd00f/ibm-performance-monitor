package com.ibm.commerce.cache;

/*
 *-----------------------------------------------------------------
 * IBM Confidential
 *
 * OCO Source Materials
 *
 * WebSphere Commerce
 *
 * (C) Copyright IBM Corp. 2013, 2014
 *
 * The source code for this program is not published or otherwise
 * divested of its trade secrets, irrespective of what has
 * been deposited with the U.S. Copyright Office.
 *-----------------------------------------------------------------
 */

/**
 * Interface used by metric gatherer to write pending metrics.
 */
public interface DirectMetricGather extends MetricGatherer {

	/**
	 * IBM Copyright notice field.
	 */
	public static final String COPYRIGHT = com.ibm.commerce.copyright.IBMCopyright.SHORT_COPYRIGHT;
	
	/**
	 * 
	 * @return the pending flush size
	 */
	public int getPendingFlushSize();

	/**
	 * This method is automatically called by the metric gathering thread in
	 * order to flush the pending operation metrics. It's implementation depends
	 * on the concrete class.
	 * 
	 * @return True if some metrics were written.
	 */
	public boolean writeMetrics();
}
