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

import java.util.List;

/**
 * Captures metrics to a database directly.
 */
public class DirectDatabaseMetricGatherer extends DatabaseMetricGatherer {

	/** copyright */
	public static final String COPYRIGHT = com.ibm.commerce.copyright.IBMCopyright.SHORT_COPYRIGHT;

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.commerce.cache.AbstractMetricGatherer#start()
	 */
	@Override
	public void start() {
		// nothing to do here
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.ibm.commerce.cache.AbstractMetricGatherer#gatherMetric(com.ibm.commerce
	 * .cache.OperationMetric)
	 */
	@Override
	public void gatherMetric(OperationMetric metric) {
		List<OperationMetric> pendingMetrics = internalGetPendingMetrics();
		pendingMetrics.add(metric);
	}

}
