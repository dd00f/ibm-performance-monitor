package com.ibm.service.entry;


import java.util.logging.Logger;

import com.ibm.commerce.cache.LogMetricGatherer;

/**
 * Logger used to gather metrics about task execution.
 */
public class ServletEntryLogger {
	public static final Logger LOGGER = Logger.getLogger(ServletEntryLogger.class
			.getName());

	public static final LogMetricGatherer GATHERER = new LogMetricGatherer(
			LOGGER);

}
