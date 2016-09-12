package com.ibm.service.detailed.cassandra;

/*
 *-----------------------------------------------------------------
 * IBM Confidential
 *
 * OCO Source Materials
 *
 * WebSphere Commerce
 *
 * (C) Copyright IBM Corp. 2014
 *
 * The source code for this program is not published or otherwise
 * divested of its trade secrets, irrespective of what has
 * been deposited with the U.S. Copyright Office.
 *-----------------------------------------------------------------
 */

import java.util.logging.Level;
import java.util.logging.Logger;

import com.ibm.commerce.cache.LogMetricGatherer;

/**
 * Cassandra request performance logging.
 */
public class CassandraLogger {

	/** copyright */
	public static final String COPYRIGHT = com.ibm.commerce.copyright.IBMCopyright.SHORT_COPYRIGHT;
	private static final String CLASSNAME = CassandraLogger.class
			.getCanonicalName();

	/**
	 * The logger.
	 */
	private static final Logger LOGGER = Logger.getLogger(CLASSNAME);

	/**
	 * The metric gatherer.
	 */
	public static final LogMetricGatherer METRIC_GATHERER = new LogMetricGatherer(
			LOGGER);

	/**
	 * Level at which to measure the cache potential.
	 */
	public static final Level MEASURE_CACHE_POTENTIAL_LEVEL = Level.FINE;

	/**
	 * Level at which to measure the result size.
	 */
	public static final Level MEASURE_RESULT_SIZE_LEVEL = Level.FINER;

	static {
		METRIC_GATHERER.setLevel(MEASURE_CACHE_POTENTIAL_LEVEL);
	}
}
