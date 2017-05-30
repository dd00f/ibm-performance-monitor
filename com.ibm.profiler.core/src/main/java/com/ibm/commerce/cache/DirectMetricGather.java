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
 * Interface used by metric gatherer to write pending metrics.
 */
public interface DirectMetricGather extends MetricGatherer {


	/**
	 * 
	 * @return the pending flush size
	 */
	public int getPendingFlushSize();

	/**
	 * This method is automatically called by the metric gathering thread in
	 * order to flush the pending operation metrics. It's implementation depends
	 * on the concrete class.
	 * 
	 * @return True if some metrics were written.
	 */
	public boolean writeMetrics();
}
