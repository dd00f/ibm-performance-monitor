package com.ibm.service.detailed;

import java.util.logging.Logger;

import com.ibm.commerce.cache.LogMetricGatherer;
import com.ibm.commerce.cache.OperationMetric;
import com.ibm.commerce.cache.PrefixedNameCache;

/**
 * Logger used to gather metrics about task execution.
 */
public class TaskLogger {
	public static final Logger LOGGER = Logger.getLogger(TaskLogger.class
			.getName());

	public static final LogMetricGatherer GATHERER = new LogMetricGatherer(
			LOGGER);

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
			GATHERER.gatherMetric(metric);
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
			GATHERER.gatherMetricEntryLog(metric);
		}
		return metric;
	}

	private static boolean isMeasurementEnabled() {
		return GATHERER.isLoggable();
	}

}
