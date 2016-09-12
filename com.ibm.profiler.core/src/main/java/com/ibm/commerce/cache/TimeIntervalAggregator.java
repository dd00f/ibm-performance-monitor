/*
 ******************************************************************************
 * 
 * IBM Confidential
 * 
 * OCO Source Materials
 * 
 * 5725D06, 5725Q72
 * 
 * (C) Copyright IBM Corp. 2013, 2015
 * 
 * The source code for this program is not published or otherwise
 * divested of its trade secrets, irrespective of what has
 * been deposited with the U.S. Copyright Office.
 *
 *******************************************************************************
 */
package com.ibm.commerce.cache;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReferenceArray;

/**
 * This class aggregates statistics about operation executions on the system.
 * 
 * Divides time into discrete and constant sized intervals beginning at
 * construction. The size and number of intervals can be configured at
 * construction.
 * 
 * The {@link #getIntervalsInRange(long, boolean)} method will traverse through the
 * intervals within range of the argument and returns the values that fit.
 * 
 * This class is thread safe, high concurrency
 * 
 * @author mcduffs
 * 
 */
public abstract class TimeIntervalAggregator<IntervalType extends TimeInterval> {

	private final AtomicReferenceArray<IntervalType> intervalArray;

	private final long intervalWidthInNanos;

	private final int intervalCount;

	/**
	 * Constructor
	 * 
	 * @param intervalWidthInNanos
	 *            The width of a time interval in nano seconds
	 * @param intervalCount
	 *            maximum number of intervals that is tracked
	 */
	public TimeIntervalAggregator(final long intervalWidthInNanos,
			final int intervalCount) {
		this.intervalWidthInNanos = intervalWidthInNanos;
		this.intervalCount = intervalCount;
		intervalArray = new AtomicReferenceArray<IntervalType>(
				intervalCount);
		for (int i = 0; i < intervalCount; i++) {
			IntervalType newInterval = createNewInterval();
			intervalArray.set(i, newInterval);
		}
	}

	/**
	 * Get the current interval based on a call to
	 * System.getNanoTime();
	 * 
	 * @return The current time interval.
	 */
	public IntervalType getCurrentInterval() {
		long nanoTime = System.nanoTime();
		return getInterval(nanoTime);
	}

	/**
	 * Get the interval based on the specified time.
	 * 
	 * @param nanoTime
	 *            The time in nanoseconds.
	 * @return The requested time interval.
	 */
	public IntervalType getInterval(long nanoTime) {
		long intervalStartIndex = nanoTime / intervalWidthInNanos;
		int index = (int) (intervalStartIndex % intervalCount);
		IntervalType operationIntervalStatistics = intervalArray
				.get(index);
		long intervalIndex = operationIntervalStatistics.getIndex();
		if (intervalStartIndex != intervalIndex) {
			// need to reset
			IntervalType newStats = createNewInterval();
			newStats.setIndex(intervalStartIndex);
			boolean setSuccessful = intervalArray.compareAndSet(index,
					operationIntervalStatistics, newStats);
			if (setSuccessful) {
				operationIntervalStatistics = newStats;
			} else {
				// another thread won, get its update.
				operationIntervalStatistics = intervalArray.get(index);
			}
		}
		return operationIntervalStatistics;
	}

	protected abstract IntervalType createNewInterval();

	/**
	 * Get intervals for the specified time period. This method works by looking
	 * at all the stored time intervals and returning the ones that are within
	 * the specified time window.
	 * 
	 * @param sinceNanosAgo
	 *            The amount of time to cover in the intervals. This number will
	 *            be rounded to the closest time interval boundary.
	 * @param skipCurrentInterval
	 *            An option to skip the current time interval in intervals since
	 *            that interval is still accumulating data. Setting this to true
	 *            increases the counter accuracy.
	 * @return The the list of intervals that match the desired time range.
	 */
	public List<IntervalType> getIntervalsInRange(final long sinceNanosAgo,
			boolean skipCurrentInterval) {
		long nanoTime = System.nanoTime();

		return getIntervalsInRange(sinceNanosAgo, skipCurrentInterval, nanoTime);
	}

	/**
	 * Get intervals for the specified time period. This method works by looking
	 * at all the stored time intervals and returning the ones that are within
	 * the specified time window.
	 * 
	 * @param sinceNanosAgo
	 *            The amount of time to cover in the intervals. This number will
	 *            be rounded to the closest time interval boundary.
	 * @param skipCurrentInterval
	 *            An option to skip the current time interval in intervals since
	 *            that interval is still accumulating data. Setting this to true
	 *            increases the counter accuracy.
	 * @param nanoEndTime
	 *            the end time at which to take the statistics snapshot.
	 * @return The the list of intervals that match the desired time range.
	 */
	public List<IntervalType> getIntervalsInRange(final long sinceNanosAgo,
			boolean skipCurrentInterval, long nanoEndTime) {
		long maximumIndex = nanoEndTime / intervalWidthInNanos;
		long minimumIndex = maximumIndex
				- (sinceNanosAgo / intervalWidthInNanos);

		if (skipCurrentInterval) {
			maximumIndex -= 1;
			minimumIndex -= 1;
		}
		
		int maxIntervalCount = intervalArray.length();
		List<IntervalType> returnValue = new ArrayList<IntervalType>(maxIntervalCount);

		if (!skipCurrentInterval ) {
			for (int i = 0; i < intervalCount; ++i) {
				IntervalType operationStatistics = intervalArray.get(i);
				long currentIndex = operationStatistics.getIndex();
				if( CacheUtilities.isIndexInRange(currentIndex, minimumIndex, maximumIndex)) {
					returnValue.add(operationStatistics);
				}
			}
		}

		return returnValue;
	}

	/**
	 * 
	 * @return The interval width in nanoseconds.
	 */
	public long getIntervalWidthInNanos() {
		return intervalWidthInNanos;
	}
	
	/**
	 * 
	 * @return The number of intervals.
	 */
	public int getIntervalCount() {
		return intervalCount;
	}
	
	/**
	 * 
	 * @return The internal interval array.
	 */
	protected AtomicReferenceArray<IntervalType> getIntervalArray() {
		return intervalArray;
	}
}
