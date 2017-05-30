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
package com.ibm.commerce.cache;


/**
 * Common methods offered by Metric gathering implementations.
 * <p>
 * Metrics gathering is used to determine the potential benefit of caching the
 * output of certain methods in the code.
 * <p>
 * Some metric gatherer must be started with the {@link #start()} method before
 * they are functional. A call to the {@link #stop()} method should also be made
 * once the application is done with the gatherer.
 */
public interface MetricGatherer {

	/**
	 * Gather an operation metric for writing. Once passed as an argument, it is
	 * assumed that the {@link OperationMetric} object is never modified.
	 * 
	 * @param metric
	 *            the operation metric to gather
	 */
	public void gatherMetric(OperationMetric metric);

	/**
	 * Start the metric gatherer.
	 */
	public void start();

	/**
	 * Stop the metric gatherer.
	 */
	public void stop();
	
	/**
	 * Is the metric gatherer enabled.
	 * @return true if metric gathering is enabled
	 */
	public boolean isEnabled();
	
	/**
	 * Check if certain logging options are enabled.
	 * 
	 * @param marker the option marker.
	 * @return true if the option is enabled.
	 */
	public boolean isEnabled(String marker);
}
