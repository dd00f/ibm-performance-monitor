package com.ibm.service.detailed;

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

import java.util.logging.Level;
import java.util.logging.Logger;

import com.ibm.commerce.cache.ILogMetricGatherer;
import com.ibm.commerce.cache.LogMetricGatherer;
import com.ibm.commerce.cache.LogMetricGathererManager;

/**
 * 
 * JdbcMetricLogger
 */
public class JdbcLogger {
	/**
	 * IBM Copyright notice field.
	 */
	public static final String COPYRIGHT = com.ibm.commerce.copyright.IBMCopyright.SHORT_COPYRIGHT;
	
	/**
	 * level to measure operations
	 */
	public static final Level MEASURE_CACHE_POTENTIAL_LEVEL = Level.FINE;

	/**
	 * level to measure result size
	 */
	public static final Level MEASURE_RESULT_SIZE_LEVEL = Level.FINER;

	/**
	 * LOGGER
	 */
	public static final Logger LOGGER = Logger.getLogger(JdbcLogger.class
			.getName());
	
	/**
	 * log metric gatherer
	 * @deprecated use GATHERER instead.
	 */
	public static final LogMetricGatherer LOG_GATHERER = new LogMetricGatherer(LOGGER);
	
    public static final ILogMetricGatherer GATHERER = LogMetricGathererManager.getLogMetricGatherer(JdbcLogger.class);
    	
	
	static {
		LOG_GATHERER.setLevel(MEASURE_CACHE_POTENTIAL_LEVEL);
	}

	/**
	 * 
	 * isLoggable
	 * @return is loggable
	 */
	public static boolean isLoggable() {
		return GATHERER.isEnabled();
	}
	
	/**
	 * 
	 * isResultSetSizeMeasured
	 * @return isResultSetSizeMeasured
	 */
	public static boolean isResultSetSizeMeasured() {
		return LOGGER.isLoggable(MEASURE_RESULT_SIZE_LEVEL);
	}

}
