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
package com.ibm.logger.stats;

import com.ibm.commerce.cache.IOperationMetric;
import com.ibm.commerce.cache.OperationStatistics;
import com.ibm.logger.jmx.TimeIntervalLogEntryMXBean;

public abstract class AbstractLogEntry implements TimeIntervalLogEntryMXBean {

	private final String id;

	private final LogType type;

	/**
	 * Constructor that builds a log entry.
	 * 
	 * @param id
	 *            the initial id for the object to build
	 */
	public AbstractLogEntry(String id) {
		this.id = id;
		type = LogType.STATISTIC;
	}

	public LogType getType() {
		return type;
	}

    public abstract void addValue(IOperationMetric metric);

	@Override
    public void addValue(long value) {
		addValue(value, 0, false, false, false);
	}


	@Override
    public void addValue(long value, boolean failed) {
		addValue(value, 0, false, false, failed);
	}

	@Override
    public abstract void addValue(long processed, int responseSize,
			boolean cacheEnabled, boolean cacheHit, boolean failed);

	@Override
	public String getId() {
		return id;
	}


	@Override
	public String getName() {
		return id;
	}

	@Override
	public String toString() {
		return "LogEntry [name=" + getName() + ",  numCalls=" + getCallCount()
				+ ", max=" + getMaximumDuration() + ", min="
				+ getMinimumDuration() + ", avg=" + getAverageDuration()
				+ ", failedCalls=" + getErrorCallCount() + "]";
	}

	
	@Override
    public double getAverageDuration() {
		OperationStatistics statistics = getStatistics();
		return statistics.getAverageDuration();
	}

	public abstract OperationStatistics getStatistics();

	
	@Override
    public long getMinimumDuration() {
		return getStatistics().getMinExecutionTime();
	}

	
	@Override
    public long getMaximumDuration() {
		return getStatistics().getMaxExecutionTime();
	}

	
	@Override
    public double getAverageResponseSize() {
		OperationStatistics statistics = getStatistics();
		return statistics.getAverageResponseSize();
	}

	
	@Override
    public long getMinimumResponseSize() {
		OperationStatistics statistics = getStatistics();
		return statistics.getMinResultSize();
	}

	
	@Override
    public long getMaximumResponseSize() {
		OperationStatistics statistics = getStatistics();
		return statistics.getMaxResultSize();
	}

	
	@Override
    public long getCallCount() {
		OperationStatistics statistics = getStatistics();
		return statistics.getCallCount();
	}

	
	@Override
    public long getErrorCallCount() {
		OperationStatistics statistics = getStatistics();
		return statistics.getErrorCallCount();
	}

	
	@Override
    public float getErrorCallPercentage() {
		OperationStatistics statistics = getStatistics();
		return statistics.getErrorCallPercentage();
	}

	
	@Override
    public long getSuccessCallCount() {
		OperationStatistics statistics = getStatistics();
		return statistics.getSuccessCallCount();
	}

	
	@Override
    public float getSuccessCallPercentage() {
		OperationStatistics statistics = getStatistics();
		float callCount = statistics.getCallCount();
		float successCallCount = statistics.getSuccessCallCount();
		return successCallCount / callCount;
	}

	
	@Override
    public long getCacheEnabledCount() {
		OperationStatistics statistics = getStatistics();
		return statistics.getCacheEnabledCallCount();
	}

	
	@Override
    public float getCacheEnabledPercentage() {
		OperationStatistics statistics = getStatistics();
		return statistics.getCacheEnabledPercentage();
	}

	
	@Override
    public long getCacheHitCount() {
		OperationStatistics statistics = getStatistics();
		return statistics.getCacheHitCount();
	}

	@Override
    public float getCacheHitPercentage() {
		OperationStatistics statistics = getStatistics();
		return statistics.getCacheHitPercentage();
	}

	@Override
    public float getTotalDuration() {
		OperationStatistics statistics = getStatistics();
		return statistics.getSumExecutionTime();
	}
	
	@Override
    public float getTotalResponseSize() {
		OperationStatistics statistics = getStatistics();
		return statistics.getSumResultSize();
	}
	
	@Override
	public TimeIntervalLogEntryMXBean getMetricByIntervalName(
			String intervalName) {
		return null;
	}
}
