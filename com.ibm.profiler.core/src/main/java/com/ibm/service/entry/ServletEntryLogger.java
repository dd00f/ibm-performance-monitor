package com.ibm.service.entry;


import java.util.logging.Logger;

import com.ibm.commerce.cache.ILogMetricGatherer;
import com.ibm.commerce.cache.LogMetricGatherer;
import com.ibm.commerce.cache.LogMetricGathererManager;

/**
 * Logger used to gather metrics about task execution.
 */
public class ServletEntryLogger {
	public static final Logger LOGGER = Logger.getLogger(ServletEntryLogger.class
			.getName());

	/**
	 * @deprecated
	 */
	public static final LogMetricGatherer GATHERER = new LogMetricGatherer(
			LOGGER);

    public static final ILogMetricGatherer LOG_GATHERER = LogMetricGathererManager.getLogMetricGatherer(ServletEntryLogger.class);
    
}
