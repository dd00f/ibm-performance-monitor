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

import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import javax.management.MBeanRegistrationException;
import javax.management.ObjectName;

import com.ibm.commerce.cache.IOperationMetric;
import com.ibm.commerce.cache.LoggingHelper;
import com.ibm.commerce.cache.OperationStatistics;
import com.ibm.logger.PerformanceLogger;
import com.ibm.logger.jmx.JMXBeanRegistrar;
import com.ibm.logger.jmx.TimeIntervalLogEntryMXBean;

public class TotalLogEntry extends AbstractLogEntry {

	private static final Logger LOGGER = Logger.getLogger(TotalLogEntry.class
			.getName());

	private final OperationStatistics totalStatistics = new OperationStatistics();

	private final TimeIntervalLogEntry[] intervalStatistics;
	
	private long markedCallCount = 0;

	/**
	 * Constructor that builds a log entry.
	 * 
	 * @param id
	 *            the initial id for the object to build
	 * @param intervals
	 *            the list of time intervals to measure.
	 */
	public TotalLogEntry(String id, long[] intervals) {
		super(id);
		int intervalCount = 0;
		if( intervals != null ) {
			intervalCount = intervals.length;
		}
		intervalStatistics = new TimeIntervalLogEntry[intervalCount];
		for (int i = 0; i < intervalCount; ++i) {
			TimeIntervalLogEntry currentIntervalStatistics = new TimeIntervalLogEntry(
					id, intervals[i]);
			registerIntervalEntry(currentIntervalStatistics);
			intervalStatistics[i] = currentIntervalStatistics;
		}
	}

	@Override
    public void addValue(IOperationMetric metric) {
		totalStatistics.logStatistic(metric);
		for (int i = 0; i < intervalStatistics.length; i++) {
			intervalStatistics[i].addValue(metric);
		}
	}

	/**
	 * @param processed
	 *            the time it took to process the request
	 * */
	@Override
    public void addValue(long processed, int responseSize,
			boolean cacheEnabled, boolean cacheHit, boolean failed) {
		totalStatistics.logStatistic(processed, responseSize, cacheEnabled,
				cacheHit, !failed);
		for (int i = 0; i < intervalStatistics.length; i++) {
			intervalStatistics[i].addValue(processed, responseSize,
					cacheEnabled, cacheHit, failed);
		}
	}

	/**
	 * Clear out active logger entry values
	 * */
	@Override
	public void clear() {
		totalStatistics.reset();
	}

	@Override
	public OperationStatistics getStatistics() {
		return totalStatistics;
	}

	@Override
    public long getIntervalDuration() {
		return 0;
	}
	
	public static void clearIntervalStatistics() {
	    try
        {
            intervalStatsRegister.destroy();
        }
        catch (MBeanRegistrationException e)
        {
            LoggingHelper.logUnexpectedException(LOGGER,
                PerformanceLogger.class.getName(), "clearIntervalStatistics", e);
        }
	}

	private static final JMXBeanRegistrar<TimeIntervalLogEntry> intervalStatsRegister = new JMXBeanRegistrar<TimeIntervalLogEntry>();
	
	public static JMXBeanRegistrar<TimeIntervalLogEntry> getIntervalStatsRegister() {
		return intervalStatsRegister;
	}

	/**
	 * @param entry
	 *            the entry to register.
	 */
	private static void registerIntervalEntry(final TimeIntervalLogEntry entry) {
		try {
			final TimeIntervalLogEntry entryToRegister = entry;
			ObjectName channelMXBeanName = PerformanceLogger.channelMXBeanName(
					entry.getId(),  entry.getIntervalName(),
					TimeIntervalLogEntry.class);
			JMXBeanRegistrar.JMXBeanProvider<TimeIntervalLogEntry> jmxBeanProvider = new JMXBeanRegistrar.JMXBeanProvider<TimeIntervalLogEntry>() {
				@Override
				public TimeIntervalLogEntry provide() {
					return entryToRegister;
				}
			};
			intervalStatsRegister.retrieveOrRegister(channelMXBeanName,
					jmxBeanProvider);
		} catch (MBeanRegistrationException e) {
			LoggingHelper.logUnexpectedException(LOGGER,
					PerformanceLogger.class.getName(), "registerIntervalEntry", e);
		}
	}

	/**
	 * 
	 * @return A boolean flag indicating if a call to this method was logged since the last time this method was called.
	 */
    public boolean isUsedSinceLastCheck() {
		boolean used = false;
		long callCount = totalStatistics.getCallCount();
		if( callCount > markedCallCount ) {
			used = true;
			markedCallCount = callCount;
		}
		return used;
	}
	
	/**
	 * Get the list of interval statistics.
	 * @return  the list of interval statistics.
	 */
	public List<TimeIntervalLogEntry> getIntervalStatistics() {
		return Arrays.asList(intervalStatistics);
	}

	@Override
	public String getIntervalName() {
		return "total";
	}
	
	@Override
	public TimeIntervalLogEntryMXBean getMetricByIntervalName(
			String intervalName) {
		if( getIntervalName().equals(intervalName)) {
			return this;
		}
		
		for (TimeIntervalLogEntry interval : intervalStatistics) {
			if( interval.getIntervalName().equals(intervalName)){
				return interval;
			}
		}
		return null;
	}
}
