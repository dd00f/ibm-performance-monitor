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
