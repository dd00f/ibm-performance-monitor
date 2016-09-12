package com.ibm.service.detailed;


import java.util.logging.Logger;

import com.ibm.commerce.cache.LogMetricGatherer;

/**
 * Logger used to gather metrics about task execution.
 */
public class ServletLogger {
	public static final Logger LOGGER = Logger.getLogger(ServletLogger.class
			.getName());

	public static final LogMetricGatherer GATHERER = new LogMetricGatherer(
			LOGGER);

}
