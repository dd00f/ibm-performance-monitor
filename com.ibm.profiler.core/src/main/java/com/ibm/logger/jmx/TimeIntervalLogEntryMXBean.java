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
package com.ibm.logger.jmx;


/**
 * This bean captures time interval performance metrics.
 */
public interface TimeIntervalLogEntryMXBean {

	/**
	 * clear the statistics
	 */
	public void clear();

	/**
	 * 
	 * @return The average duration of each call in the interval in nanoseconds.
	 */
	public double getAverageDuration();

	/**
	 * 
	 * @return The minimum call duration during the interval in nanoseconds.
	 */
	public long getMinimumDuration();

	/**
	 * 
	 * @return The maximum call duration during the interval in nanoseconds.
	 */
	public long getMaximumDuration();

	/**
	 * 
	 * @return The average call result size during the interval in bytes.
	 */
	public double getAverageResponseSize();

	/**
	 * 
	 * @return The minimum call result size during the interval in bytes.
	 */
	public long getMinimumResponseSize();

	/**
	 * 
	 * @return The maximum call result size during the interval in bytes.
	 */
	public long getMaximumResponseSize();

	/**
	 * 
	 * @return The number of calls during the interval.
	 */
	public long getCallCount();

	/**
	 * 
	 * @return The number of calls during the interval which resulted in errors.
	 */
	public long getErrorCallCount();

	/**
	 * 
	 * @return The percentage of calls during the interval which resulted in
	 *         errors. This value will be between 0.0 and 1.0.
	 */
	public float getErrorCallPercentage();

	/**
	 * 
	 * @return The number of calls during the interval which resulted in
	 *         success.
	 */
	public long getSuccessCallCount();

	/**
	 * 
	 * @return The percentage of calls during the interval which resulted in
	 *         success. This value will be between 0.0 and 1.0.
	 */
	public float getSuccessCallPercentage();

	/**
	 * 
	 * @return The number of calls during the interval where caching was
	 *         enabled.
	 */
	public long getCacheEnabledCount();

	/**
	 * 
	 * @return The percentage of calls during the interval where caching was
	 *         enabled. This value will be between 0.0 and 1.0.
	 */
	public float getCacheEnabledPercentage();

	/**
	 * 
	 * @return The number of calls during the interval where the result was
	 *         fetched from cache.
	 */
	public long getCacheHitCount();

	/**
	 * 
	 * @return The percentage of calls during the interval where the result was
	 *         fetched from cache. This value will be between 0.0 and 1.0.
	 */
	public float getCacheHitPercentage();

	/**
	 * 
	 * @return The operation identifier.
	 */
	public String getId();

	/**
	 * 
	 * @return The operation name. (same as the identifier)
	 */
	public String getName();

	/**
	 * 
	 * @return The interval duration in nanoseconds. Returns zero if the
	 *         interval duration is infinite.
	 */
	public long getIntervalDuration();
	
	/**
	 * 
	 * @return The interval name in the form of human readable time unit.
	 */
	public String getIntervalName();
	
	/**
	 * 
	 * @return The total amount of time in nanoseconds that was spent performing all the executions.
	 */
	public float getTotalDuration();

	/**
	 * 
	 * @return The total amount of data in bytes that was returned performing all the executions.
	 */
	public float getTotalResponseSize();

    /**
     * Add an execution statistic
     * @param duration the duration.
     * @param failed flag indicating if the execution failed or not.
     */
    public void addValue(long duration, boolean failed);

    /**
     * Add an execution statistic
     * @param duration the duration.
     */
    public void addValue(long duration);

    /**
     * Add an execution statistic.
     * 
     * @param duration
     *            Duration in nanoseconds.
     * @param resultSize
     *            result size.
     * @param operationCacheEnabled
     *            was cache enabled.
     * @param resultFetchedFromCache
     *            was result fetched from cache.
     * @param failed
     *            was call failed.
     */
    public void addValue(long duration, int resultSize, boolean operationCacheEnabled, boolean resultFetchedFromCache,
        boolean failed);
    
    
	/**
	 * Fetch metrics for a specified interval by name.
	 * 
	 * @param intervalName
	 *            The interval name.
	 * @return The desired metrics. Null if none could be found.
	 */
	public TimeIntervalLogEntryMXBean getMetricByIntervalName(
			String intervalName);
}
