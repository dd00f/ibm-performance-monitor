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

import java.util.logging.Logger;

import com.ibm.commerce.cache.ILogMetricGatherer;
import com.ibm.commerce.cache.LogMetricGatherer;
import com.ibm.commerce.cache.LogMetricGathererManager;
import com.ibm.commerce.cache.OperationMetric;
import com.ibm.commerce.cache.PrefixedNameCache;

/**
 * Logger used to gather metrics about task execution.
 */
public class TaskLogger {
	public static final Logger LOGGER = Logger.getLogger(TaskLogger.class
			.getName());

	/**
	 * @deprecated Use LOG_GATHERER instead.
	 */
	public static final LogMetricGatherer GATHERER = new LogMetricGatherer(
			LOGGER);

	public static final ILogMetricGatherer LOG_GATHERER = LogMetricGathererManager.getLogMetricGatherer(TaskLogger.class);
	
	private static final PrefixedNameCache TASK_NAME_CACHE = new PrefixedNameCache(
			"Task : ");

	/**
	 * Stop a metric execution and log it to the appropriate destination.
	 * 
	 * @param metric
	 *            The metric to log. May be null if no metric was used.
	 */
	public static void logTaskMetric(OperationMetric metric) {
		logTaskMetric(metric, true);
	}

	/**
	 * Stop a metric execution and log it to the appropriate destination.
	 * 
	 * @param metric
	 *            The metric to log. May be null if no metric was used.
	 * @param successful
	 *            was the task successful
	 */
	public static void logTaskMetric(OperationMetric metric, boolean successful) {
		if (metric != null) {
			// use a dummy value of 1000
			int resultSize = 1000;
			boolean cacheEnabled = false;
			metric.stopOperation(resultSize, cacheEnabled, successful);
			LOG_GATHERER.gatherMetric(metric);
		}
	}

	/**
	 * Initialize a metric for a task.
	 * 
	 * @param taskName
	 *            The name of the task we are about to measure.
	 * @return The metric that will perform the measurement. May be null if
	 *         metric gathering is disabled.
	 */
	public static OperationMetric initializeTaskMetric(String taskName) {
		OperationMetric metric = null;
		if (isMeasurementEnabled()) {
			metric = new OperationMetric();
			String prefixedTaskName = TASK_NAME_CACHE.getPrefixedName(taskName);
			boolean cacheHit = false;
			metric.startOperation(prefixedTaskName, cacheHit);
			LOG_GATHERER.gatherMetricEntryLog(metric);
		}
		return metric;
	}

	private static boolean isMeasurementEnabled() {
		return LOG_GATHERER.isEnabled();
	}

}
