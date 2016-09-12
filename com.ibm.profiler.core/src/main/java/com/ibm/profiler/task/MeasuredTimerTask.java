package com.ibm.profiler.task;

import java.util.TimerTask;

import com.ibm.commerce.cache.OperationMetric;
import com.ibm.service.detailed.TaskLogger;

/**
 * A timer task that measures its duration.
 */
public abstract class MeasuredTimerTask extends TimerTask {

	/**
	 * Run the task and measure the duration when metrics are enabled.
	 */
	@Override
	public final void run() {
		String taskName = this.getClass().getName();
		OperationMetric metric = TaskLogger.initializeTaskMetric(taskName);
		boolean successful = false;
		try {
			runMeasured();
			successful = true;
		} finally {
			TaskLogger.logTaskMetric(metric, successful);
		}
	}

	/**
	 * Override this method to perform the task.
	 */
	public abstract void runMeasured();
}
