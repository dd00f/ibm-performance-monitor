package com.ibm.commerce.cache;

/*
 *-----------------------------------------------------------------
 * IBM Confidential
 *
 * OCO Source Materials
 *
 * WebSphere Commerce
 *
 * (C) Copyright IBM Corp. 2012
 *
 * The source code for this program is not published or otherwise
 * divested of its trade secrets, irrespective of what has
 * been deposited with the U.S. Copyright Office.
 *-----------------------------------------------------------------
 */

/**
 * Common methods offered by Metric gathering implementations.
 * <p>
 * Metrics gathering is used to determine the potential benefit of caching the
 * output of certain methods in the code.
 * <p>
 * Some metric gatherer must be started with the {@link #start()} method before
 * they are functional. A call to the {@link #stop()} method should also be made
 * once the application is done with the gatherer.
 */
public interface MetricGatherer {
	/**
	 * IBM Copyright notice field.
	 */
	public static final String COPYRIGHT = com.ibm.commerce.copyright.IBMCopyright.SHORT_COPYRIGHT;

	/**
	 * Gather an operation metric for writing. Once passed as an argument, it is
	 * assumed that the {@link OperationMetric} object is never modified.
	 * 
	 * @param metric
	 *            the operation metric to gather
	 */
	public void gatherMetric(OperationMetric metric);

	/**
	 * Start the metric gatherer.
	 */
	public void start();

	/**
	 * Stop the metric gatherer.
	 */
	public void stop();
	
	/**
	 * Is the metric gatherer enabled.
	 * @return true if metric gathering is enabled
	 */
	public boolean isEnabled();
	
	/**
	 * Check if certain logging options are enabled.
	 * 
	 * @param marker the option marker.
	 * @return true if the option is enabled.
	 */
	public boolean isEnabled(String marker);
}
