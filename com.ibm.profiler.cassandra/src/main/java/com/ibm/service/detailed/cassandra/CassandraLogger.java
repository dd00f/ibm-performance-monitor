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
package com.ibm.service.detailed.cassandra;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.ibm.commerce.cache.ILogMetricGatherer;
import com.ibm.commerce.cache.LogMetricGatherer;
import com.ibm.commerce.cache.LogMetricGathererManager;

/**
 * Cassandra request performance logging.
 */
public class CassandraLogger {

	private static final String CLASSNAME = CassandraLogger.class
			.getCanonicalName();

	/**
	 * The logger.
	 */
	private static final Logger LOGGER = Logger.getLogger(CLASSNAME);

	/**
	 * The metric gatherer.
	 * @deprecated use LOG_GATHERER instead.
	 */
	public static final LogMetricGatherer METRIC_GATHERER = new LogMetricGatherer(
			LOGGER);
	
    public static final ILogMetricGatherer LOG_GATHERER = LogMetricGathererManager.getLogMetricGatherer(CassandraLogger.class);
    
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
