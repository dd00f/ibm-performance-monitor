package com.ibm.service.detailed;


import java.util.logging.Logger;

import com.ibm.commerce.cache.ILogMetricGatherer;
import com.ibm.commerce.cache.LogMetricGatherer;
import com.ibm.commerce.cache.LogMetricGathererManager;

/**
 * Logger used to gather metrics about task execution.
 */
public class ServletLogger {
    
	public static final Logger LOGGER = Logger.getLogger(ServletLogger.class
			.getName());

	/**
	 * @deprecated Use the LOG_GATHERER instead.
	 */
	public static final LogMetricGatherer GATHERER = new LogMetricGatherer(
			LOGGER);
	
	public static final ILogMetricGatherer LOG_GATHERER = LogMetricGathererManager.getLogMetricGatherer(ServletLogger.class);

}
