/*
 * Copyright 2017 Steve McDuff
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ibm.service.detailed;


import java.util.logging.Level;
import java.util.logging.Logger;

import com.ibm.commerce.cache.ILogMetricGatherer;
import com.ibm.commerce.cache.LogMetricGatherer;
import com.ibm.commerce.cache.LogMetricGathererManager;
import com.ibm.commerce.cache.Markers;

/**
 * 
 * JdbcMetricLogger
 */
public class JdbcLogger {

	/**
	 * level to measure operations
	 */
	public static final Level MEASURE_CACHE_POTENTIAL_LEVEL = Level.FINE;

	/**
	 * level to measure result size
	 * @deprecated use GATHER.isEnabled(Marker.RESULT_SIZE);
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
		return GATHERER.isEnabled(Markers.RESULT_SIZE_MARKER);
	}

}
