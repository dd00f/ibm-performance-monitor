// IBM Confidential OCO Source Material
// 5725-F81 (C) COPYRIGHT International Business Machines Corp. 2011,2014
// The source code for this program is not published or otherwise divested
// of its trade secrets, irrespective of what has been deposited with the
// U.S. Copyright Office.
package com.ibm.logger.jmx;


/**
 * This bean captures time interval performance metrics. This class effectively
 * duplicates TimeIntervalLogEntryMXBean. It is used to maintain backward
 * compatibility with the 1.0.0.0 release & be able to tolerate the use of multiple class loaders.
 */
public interface TotalLogEntry {

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
     * Test if the log entry was used since the last check.
     * @return True if the log entry was used. False otherwise.
     */
//     public boolean isUsedSinceLastCheck();

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
     * @param successful
     *            was call successful.
     */
    public void addValue(long duration, int resultSize, boolean operationCacheEnabled, boolean resultFetchedFromCache,
        boolean successful);
}
