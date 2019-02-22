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

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import com.ibm.logger.TraceUtilities;
import com.ibm.logger.jmx.LogEntryMXBean;

public class LogEntry implements LogEntryMXBean {

    private final AtomicLong startTime = new AtomicLong( 0 );

    private final AtomicLong maxValue = new AtomicLong( 0 );

    private final AtomicLong minValue = new AtomicLong( -1 );

    private final AtomicLong numCalls = new AtomicLong( 0 );

    private final AtomicLong failedCalls = new AtomicLong( 0 );

    private final AtomicLong totalValue = new AtomicLong( 0 );

    private final AtomicBoolean inFlight = new AtomicBoolean( false );

    private final String id;
    
    private final String layer;

    private final LogType type;

    /**
     * Constructor that builds a log entry.
     * 
     * @param id the initial id for the object to build
     * @param type the initial type for the object to build
     */
    public LogEntry( String id, LogType type ) {
        this.id = id;
        this.type = type;
        this.layer = TraceUtilities.getLayer(id);
    }

    /**
     * Constructor that builds a new log entry
     * 
     * @param id the initial id for the object to build
     * @param type the initial type for the object to build
     */
    public LogEntry( String id, String type ) {
        this.id = id;
        this.layer = TraceUtilities.getLayer(id);
        LogType match = null;
        for ( LogType value : LogType.values() ) {
            if ( value.name().equalsIgnoreCase( type ) )
                match = value;
        }
        if ( match == null )
            match = LogType.STATISTIC;
        this.type = match;
    }

    /**
     * increment a log counter and increase total with the new value
     * 
     * @param value Amount to increase the total value
     * @return true.
     * */
    public boolean addValue( long value ) {
        numCalls.getAndAdd( 1 );
        totalValue.getAndAdd( value );
        if ( minValue.get() == -1 || value < minValue.get() && value != -1 ) {
            minValue.set( value );
        }
        if ( maxValue.get() < value ) {
            maxValue.set( value );
        }
        return true;
    }

    /**
     * Increment the counters to add the time it took to process a request.
     * @param processed the time it took to process the request
     * @param failed was the request failed.
     * @return true.
     * */
    public boolean addValue( long processed, boolean failed ) {
        if ( failed ) {
            failedCalls.incrementAndGet();
        }
        return addValue( processed );
    }

    /**
     * Clear out active logger entry values
     * */
    @Override
    public void clear() {
        this.failedCalls.set( 0 );
        this.numCalls.set( 0 );
        this.totalValue.set( 0L );
        this.minValue.set( -1 );
        this.maxValue.set( 0 );
    }

    /**
     * Create a clone of the LogEntry
     * */
    @Override
    public LogEntry clone() {
        LogEntry _li = new LogEntry( this.id, this.type );
        _li.inFlight.set( inFlight.get() );
        _li.numCalls.set( numCalls.get() );
        _li.failedCalls.set( failedCalls.get() );
        _li.totalValue.set( totalValue.get() );
        _li.maxValue.set( maxValue.get() );
        _li.minValue.set( minValue.get() );

        return _li;

    }

    @Override
    public boolean equals( Object obj ) {
        if ( this == obj ) {
            return true;
        }
        if ( obj == null ) {
            return false;
        }
        if ( getClass() != obj.getClass() ) {
            return false;
        }
        LogEntry other = (LogEntry) obj;
        if ( id == null ) {
            if ( other.id != null ) {
                return false;
            }
        } else if ( !id.equals( other.id ) ) {
            return false;
        }

        if ( type == null ) {
            if ( other.type != null ) {
                return false;
            }
        } else if ( !type.equals( other.type ) ) {
            return false;
        }
        return true;
    }

    /**
     * @return the average value of the log entry
     */
    @Override
    public double getAverage() {
        double ret = 0.0;
        long numberOfCalls = this.numCalls.get();
        if( numberOfCalls != 0 ) {
        	ret = ((double)this.totalValue.get()) / ((double)numberOfCalls);
        }
        return ret;
    }

    /**
     * @return the failed value of the log entry
     */
    @Override
    public long getFailedCalls() {
        return failedCalls.get();
    }

    /**
     * @return the id of the log entry
     */

    @Override
    public String getId() {
        return id;
    }

    /**
     * @return the maximum value of the log entry
     */

    @Override
    public float getMaximum() {
        return maxValue.floatValue();
    }

    /**
     * @return the minimum value of the log entry
     */
    @Override
    public float getMinimum() {
        return minValue.floatValue();
    }

    /**
     * @return the Name value of the log entry
     */
    @Override
    public String getName() {
        return id;
    }

    @Override
    public long getNumCalls() {
        return numCalls.get();
    }

    /**
     * @return the total number of calls of the log entry
     */
    @Override
    public float getTotal() {
        return totalValue.floatValue();
    }

    /**
     * @return the type of the log entry
     */

    @Override
    public String getType() {
        return type.name();
    }

    @Override
    public int hashCode() {
        int hashCode = 1;
        hashCode = 31 * hashCode + (id == null ? 0 : id.hashCode());
        hashCode = 31 * hashCode + (type == null ? 0 : type.hashCode());
        return hashCode;
    }

    /**
     * 
     * @param logEntry the value to increase internal counters by.
     */
    public void increase( LogEntry logEntry ) {
        this.numCalls.addAndGet( logEntry.numCalls.get() );
        this.failedCalls.addAndGet( logEntry.failedCalls.get() );
        this.totalValue.addAndGet( logEntry.totalValue.get() );

        if ( this.minValue.get() > logEntry.minValue.get() || this.minValue.get() <= -1 ) {
            this.minValue.set( logEntry.minValue.get() );
        }
        if ( this.maxValue.get() < logEntry.maxValue.get() ) {
            this.maxValue.set( logEntry.maxValue.get() );
        }
        logEntry.clear();
    }

    /**
     * 
     * @param startTime Set the starting time of a entry
     */
    public void startTimer( long startTime ) {
        if ( inFlight.get() ) {
            return;
        }
        inFlight.set( true );
        this.startTime.set( startTime );

    }

    /**
     * Stop a request execution, measure the time it took and increment counters.
     * @param endTime End time use for calculation of total time spend.
     * @return true if the stop operation was successful. False if no corresponding startTimer could be found.
     */
    public boolean stopTimer( long endTime ) {
        if ( !inFlight.get() ) {
            return false;
        }
        inFlight.set( false );

        if ( endTime >= startTime.get() ) {
            return addValue( endTime - startTime.get() );
        } else {
            return addValue( 0L );
        }

    }

    @Override
    public String toString() {
        return "LogEntry [name=" + id + ",  numCalls=" + numCalls + ", total=" + totalValue + ", max=" + maxValue + ", min=" + minValue + ", avg=" + getAverage() + ", failedCalls=" + failedCalls + "]";
    }

    public boolean isInFlight() {
        return inFlight.get();
    }

	@Override
	public String getLayer() {
		return layer;
	}
}
