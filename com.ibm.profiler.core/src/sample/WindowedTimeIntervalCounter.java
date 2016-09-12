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

import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * An operation counter that can approximately report how many operations have occurred since a given time in the past.
 * 
 * Divides time into discrete and constant sized intervals beginning at construction. The size and number of intervals
 * can be configured at construction. Each call to {@link #increment()} will increment the current interval, creating it
 * if necessary.
 * 
 * Calls to {@link #increment()} will also clean up old intervals that exceed the maximum time span configured.
 * 
 * The {@link #getCount(long)} method will traverse back through the intervals within range of the argument and
 * accumulate all calls to {@link #increment()} that occurred within that time span.
 * 
 * This class is thread safe, high concurrency, and lock free.
 * 
 * @author mharreld
 * 
 */
public final class WindowedTimeIntervalCounter implements TimeIntervalCounter {
    
    private static final int CLEANUPS_PER_CYCLE = 4;

    private final ConcurrentNavigableMap<Long, AtomicLong> countsByIntervalFloor = new ConcurrentSkipListMap<Long, AtomicLong>();

    private final long intervalWidthInMillis;

    // The value of System.currentTimeInMillis() when this object was constructed. Is the beginning of the first
    // interval.
    private final long anchorTimeInMillis;

    private final long cleanupIntervalInMillis;

    private final AtomicLong lastCleanupTime;

    private final int maxIntervals;

    /**
     * Constructor
     * 
     * @param intervalWidthInMillis The width of a time interval in millis
     * @param maxIntervals maximum number of intervals that is tracked
     */
    public WindowedTimeIntervalCounter( final long intervalWidthInMillis, final int maxIntervals ) {
        this.intervalWidthInMillis = intervalWidthInMillis;
        this.anchorTimeInMillis = System.currentTimeMillis();
        this.maxIntervals = maxIntervals;
        this.cleanupIntervalInMillis = intervalWidthInMillis * maxIntervals / CLEANUPS_PER_CYCLE;
        this.lastCleanupTime = new AtomicLong( this.anchorTimeInMillis );
    }

    /* (non-Javadoc)
     * @see com.ibm.b2b.util.concurrent.TimeIntervalCounter#increment()
     */
    @Override
    public void increment() {
        increment( 1 );
    }

    /* (non-Javadoc)
     * @see com.ibm.b2b.util.concurrent.TimeIntervalCounter#increment(long)
     */
    @Override
    public void increment( final long byCount ) {
        final long invokeTimeInMillis = System.currentTimeMillis();
        // Find the intervalFloor for the current time. The intervalFloor is relative to the
        // epoch and is the lowest epoch time in the active time interval. It acts as the key in countsByIntervalFloor
        // and maps to the AtomicLong that counts increments in that interval.
        final long timeElapsedSinceAnchorInMillis = invokeTimeInMillis - anchorTimeInMillis;
        final long numberOfIntervalsPassedSinceAnchor = timeElapsedSinceAnchorInMillis / intervalWidthInMillis;
        final long cumulativeIntervalWidth = numberOfIntervalsPassedSinceAnchor * intervalWidthInMillis;
        final long intervalFloor = anchorTimeInMillis + cumulativeIntervalWidth;
        final AtomicLong count = countsByIntervalFloor.get( intervalFloor );
        if ( count != null ) {
            // we are good, someone already created it so we safely increment
            count.addAndGet( byCount );
        } else {
            // wasn't there, so we are now in a competition to add it - we set our value to byCount to skip the
            // increment
            final AtomicLong previousValue = countsByIntervalFloor.putIfAbsent( intervalFloor, new AtomicLong(
                byCount ) );
            if ( previousValue != null ) {
                // another thread won, we just use the value they put
                previousValue.addAndGet( byCount );
            }
        }
        checkForCleanupDuties( invokeTimeInMillis );
    }

    /* (non-Javadoc)
     * @see com.ibm.b2b.util.concurrent.TimeIntervalCounter#getCount(long)
     */
    @Override
    public long getCount( final long sinceMillisAgo ) {
        final long invokeTimeInMillis = System.currentTimeMillis();
        final long fromTimeInMillis = invokeTimeInMillis - sinceMillisAgo;
        long accumulatedCount = 0;
        Long intervalFloor = countsByIntervalFloor.ceilingKey( fromTimeInMillis );
        while ( intervalFloor != null && intervalFloor <= invokeTimeInMillis ) {
            AtomicLong count = countsByIntervalFloor.get( intervalFloor );
            if ( count != null ) {
                accumulatedCount += count.get();
            }
            intervalFloor = countsByIntervalFloor.higherKey( intervalFloor );
        }
        return accumulatedCount;
    }

    private void checkForCleanupDuties( final long invokeTimeInMillis ) {
        final long lastCleanupTimeSnapshot = lastCleanupTime.get();
        if ( invokeTimeInMillis > lastCleanupTimeSnapshot + cleanupIntervalInMillis
        // if we got this far into the expression, we are eligible for cleanup so we enter the competition to do it
            && lastCleanupTime.compareAndSet( lastCleanupTimeSnapshot, invokeTimeInMillis ) ) {
            // the cleanup time had not changed so we were update winners and do the work
            cleanup( invokeTimeInMillis );
        }
    }

    private void cleanup( final long invokeTimeInMillis ) {
        final long cutoffTimeInMillis = invokeTimeInMillis - ( intervalWidthInMillis * maxIntervals );
        Long intervalFloor = countsByIntervalFloor.firstKey();
        while ( intervalFloor != null && intervalFloor < cutoffTimeInMillis ) {
            if ( countsByIntervalFloor.remove( intervalFloor ) == null ) {
                // someone else already removed it, so we have unexpected competition - bail
                return;
            }
            intervalFloor = countsByIntervalFloor.firstKey();
        }
    }
}
