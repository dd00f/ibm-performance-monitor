package com.ibm.commerce.cache;

/*
 *-----------------------------------------------------------------
 * IBM Confidential
 *
 * OCO Source Materials
 *
 * WebSphere Commerce
 *
 * (C) Copyright IBM Corp. 2014
 *
 * The source code for this program is not published or otherwise
 * divested of its trade secrets, irrespective of what has
 * been deposited with the U.S. Copyright Office.
 *-----------------------------------------------------------------
 */

/**
 * This class keeps track of the statistics of a single operation.
 */
public class OperationStatistics implements TimeInterval {

    /** copyright */
    public static final String COPYRIGHT = com.ibm.commerce.copyright.IBMCopyright.SHORT_COPYRIGHT;

    /** line separator */
    private static final String LINE_SEPARATOR = System.getProperty( "line.separator" );

    /** the operation name */
    private String name;

    /** the string to use when printing logs to avoid delimiter problems */
    private String printName;

    /** number of calls to the operation */
    private long callCount;
    
    /** number of calls to the operation that were successful */
    private long successCallCount;

    /** number of calls where cache was enabled */
    private long cacheEnabledCallCount;

    /** number of calls that resulted in a cache hit */
    private long cacheHitCount;

    // $ANALYSIS-IGNORE
    /** the sum of all execution time, used to calculate the average */
    private float sumExecutionTime;
    
    /** fragment of the sum of execution time */
    private long sumExecutionTimeFragment;

    /** the smallest execution time */
    private long minExecutionTime;

    /** the biggest execution time */
    private long maxExecutionTime;

    // $ANALYSIS-IGNORE
    /** the sum of all the result size, used to calculate the average */
    private float sumResultSize;
    
    /** fragment of the sum of execution time */
    private long sumResultSizeFragment;

    /** the minimum result size */
    private long minResultSize;

    /** the maximum result size */
    private long maxResultSize;

    /**
     * did we log at least once that we had no more execution. This flag prevents logging an operation once it stopped
     * runnign.
     */
    private boolean clearCompleted = true;
    
    /**
     * index of this statistics blocks during intervals.
     */
    private long index;
    
    /**
     * Used to identify the index range requested.
     */
    private long minimumIndex;

    /**
     * Constructor
     */
    public OperationStatistics() {
        reset();
    }

	/**
	 * 
	 * @return the minimum index of the statistics.
	 */
	public long getMinimumIndex() {
		return minimumIndex;
	}

	/**
	 * 
	 * @param minimumIndex
	 *            the minimum index of the statistics.
	 */
	public void setMinimumIndex(long minimumIndex) {
		this.minimumIndex = minimumIndex;
	}
    
    /**
     * 
     * @return The statistics interval index.
     */
    @Override
    public long getIndex() {
		return index;
	}
    
    /**
     * 
     * @param index The statistics interval index.
     */
    @Override
    public void setIndex(long index) {
		this.index = index;
	}

    /**
     * getName
     * 
     * @return the operation name
     */
    public String getName() {
        return name;
    }

    /**
     * setName
     * 
     * @param name the operation name
     */
    public void setName( String name ) {
        this.name = name;
        printName = name.replaceAll( "[,:]", " " );
    }
    
    /**
     * Aggregate statistics only if the other statistics are in the right index interval.
     * @param statistics The statistics to aggregate.
     * @param minimumIndex The minimum index value to match.
     * @param maximumIndex The maximum index value to match.
     */
	public synchronized void aggregateStatisticsIfInInterval(
			OperationStatistics statistics, long minimumIndex, long maximumIndex) {
		// ensure the target object doesn't change.
		synchronized (statistics) {
			long targetIndex = statistics.index;
			boolean inRange = CacheUtilities.isIndexInRange(targetIndex, minimumIndex,
					maximumIndex);
			
			if( ! inRange ) {
				return;
			}

			callCount += statistics.callCount;
			successCallCount += statistics.successCallCount;
			maxExecutionTime = Math.max(maxExecutionTime,
					statistics.maxExecutionTime);
			minExecutionTime = Math.min(minExecutionTime,
					statistics.minExecutionTime);
			addExecutionTime(statistics.getSumExecutionTime());

			maxResultSize = Math.max(maxResultSize, statistics.maxResultSize);
			minResultSize = Math.min(minResultSize, statistics.minResultSize);
			addResultSize( statistics.getSumResultSize() );
			cacheEnabledCallCount += statistics.cacheEnabledCallCount;
			cacheHitCount += statistics.cacheHitCount;
		}
	}


    

    /**
     * Log statistics about an operation.
     * 
     * @param metric The metric to log.
     */
    public void logStatistic( IOperationMetric metric ) {
        if ( metric == null ) {
            return;
        }

        long duration = metric.getDuration();
        int resultSize = metric.getResultSize();
        boolean operationCacheEnabled = metric.isOperationCacheEnabled();
        boolean resultFetchedFromCache = metric.isResultFetchedFromCache();
        boolean successful = metric.isSuccessful();

        logStatistic( duration, resultSize, operationCacheEnabled, resultFetchedFromCache, successful );
    }

    /**
     * Log an execution statistic.
     * 
     * @param duration The duration.
     * @param resultSize The result size.
     * @param operationCacheEnabled Was operation cache enabled.
     * @param resultFetchedFromCache Was result fetched from cache.
     */
    public synchronized void logStatistic( long duration, int resultSize, boolean operationCacheEnabled, boolean resultFetchedFromCache, boolean successful) {
        ++callCount;
        if( successful) {
        	++successCallCount;
        }
        maxExecutionTime = Math.max( maxExecutionTime, duration );
        minExecutionTime = Math.min( minExecutionTime, duration );
        addExecutionTime(duration);

        maxResultSize = Math.max( maxResultSize, resultSize );
        minResultSize = Math.min( minResultSize, resultSize );
        addResultSize(resultSize);
        if ( operationCacheEnabled ) {
            ++cacheEnabledCallCount;
        }
        if ( resultFetchedFromCache ) {
            ++cacheHitCount;
        }
    }
    
    private void addResultSize(float resultSize) {
    	long currentSumResultSizeFragment = sumResultSizeFragment;
		sumResultSizeFragment += resultSize;
		// rollover detection.
		if( sumResultSizeFragment < 0)
		{
			sumResultSize += currentSumResultSizeFragment;
			sumResultSizeFragment = (long) resultSize;
		}
	}

	private void addExecutionTime(float duration) {
    	long currentSumExecutionTimeFragment = sumExecutionTimeFragment;
		sumExecutionTimeFragment += duration;
		// rollover detection.
		if( sumExecutionTimeFragment < 0)
		{
			sumExecutionTime += currentSumExecutionTimeFragment;
			sumExecutionTimeFragment = (long) duration;
		}
	}

	/**
     * Log an execution statistic.
     * 
     * @param duration The duration.
     * @param resultSize The result size.
     * @param operationCacheEnabled Was operation cache enabled.
     * @param resultFetchedFromCache Was result fetched from cache.
     */
    public synchronized void logStatistic( long duration, int resultSize, boolean operationCacheEnabled, boolean resultFetchedFromCache ) {
    	logStatistic(duration, resultSize, operationCacheEnabled, resultFetchedFromCache, true);
    }

    /**
     * Reset the statistics
     */
    public synchronized void reset() {
        callCount = 0;
        cacheEnabledCallCount = 0;
        cacheHitCount = 0;
        sumExecutionTime = 0;
        sumResultSize = 0;
        maxExecutionTime = 0;
        minExecutionTime = Long.MAX_VALUE;
        maxResultSize = 0;
        minResultSize = Long.MAX_VALUE;
        successCallCount = 0;
        sumExecutionTimeFragment = 0;
        sumResultSizeFragment = 0;
    }

    /**
     * Append a statistic message and reset the counters.
     * 
     * @param builder The builder in which to append statistics.
     */
    public synchronized void appendStatisticsAndReset( StringBuilder builder ) {
        appendStatistics( builder );
        reset();
    }

    /**
     * Append a statistic message.
     * 
     * @param builder The builder in which to append statistics.
     */
    public synchronized void appendStatistics( StringBuilder builder ) {
        if ( callCount > 0 ) {
            clearCompleted = false;
        } else if ( !clearCompleted ) {
            // ensure that we print cleared out operations at least once
            clearCompleted = true;
        } else {
            // once an operation stopped running and we cleared it, stop printing it.
            return;
        }

        builder.append( LINE_SEPARATOR );
        builder.append( "Op:" );
        builder.append( printName );
        builder.append( ",count:" );
        builder.append( callCount );
        builder.append( ",cacheEnabledCount:" );
        builder.append( cacheEnabledCallCount );
        builder.append( ",cacheHitCount:" );
        builder.append( cacheHitCount );
        builder.append( ",avgTime:" );
        // $ANALYSIS-IGNORE
        builder.append( callCount == 0 ? 0 : (long) ( getSumExecutionTime() / ( callCount * 1000000 ) ) );
        builder.append( ",minTime:" );
        builder.append( callCount == 0 ? 0 : minExecutionTime / 1000000 );
        builder.append( ",maxTime:" );
        builder.append( maxExecutionTime / 1000000 );
        builder.append( ",avgResultSize:" );
        // $ANALYSIS-IGNORE
        builder.append( callCount == 0 ? 0 : (long) ( getSumResultSize() / callCount ) );
        builder.append( ",minSize:" );
        builder.append( callCount == 0 ? 0 : minResultSize );
        builder.append( ",maxSize:" );
        builder.append( maxResultSize );
        builder.append( ",successCount:" );
        builder.append( successCallCount );        
        builder.append( ",errorCount:" );
        builder.append( callCount - successCallCount );   
    }

	public String getPrintName() {
		return printName;
	}

	public long getCallCount() {
		return callCount;
	}

	public long getSuccessCallCount() {
		return successCallCount;
	}

	public long getCacheEnabledCallCount() {
		return cacheEnabledCallCount; 
	}

	public long getCacheHitCount() {
		return cacheHitCount;
	}

	public synchronized float getSumExecutionTime() {
		return sumExecutionTime + sumExecutionTimeFragment;
	}

	public synchronized long getMinExecutionTime() {
		if( minExecutionTime == Long.MAX_VALUE ) {
			return 0;
		}
		
		return minExecutionTime;
	}

	public long getMaxExecutionTime() {
		return maxExecutionTime;
	}

	public synchronized float getSumResultSize() {
		return sumResultSize + sumResultSizeFragment;
	}

	public synchronized long getMinResultSize() {
		if( minResultSize == Long.MAX_VALUE ) {
			return 0;
		}
		
		return minResultSize;
	}

	public long getMaxResultSize() {
		return maxResultSize;
	}

	public boolean isClearCompleted() {
		return clearCompleted;
	}
    


	public double getAverageDuration() {
		double sumExecutionTime = getSumExecutionTime();
		double callCount = getCallCount();
		if( callCount == 0 ) {
			return 0;
		}
		double averageDuration = sumExecutionTime / callCount;
		return averageDuration;
	}

	public double getAverageResponseSize() {
		double sumResultSize = getSumResultSize();
		double callCount = getCallCount();
		if( callCount == 0 ) {
			return 0;
		}
		return sumResultSize / callCount;
	}

	public long getErrorCallCount() {
		return getCallCount() - getSuccessCallCount();
	}

	public float getErrorCallPercentage() {
		float callCount = getCallCount();
		float errorCount = callCount - getSuccessCallCount();
		if( callCount == 0 ) {
			return 0;
		}
		return errorCount / callCount;
	}

	public float getSuccessCallPercentage() {
		float callCount = getCallCount();
		float successCallCount = getSuccessCallCount();
		if( callCount == 0 ) {
			return 1;
		}
		return successCallCount / callCount;
	}

	public float getCacheEnabledPercentage() {
		float cacheEnabledCallCount = getCacheEnabledCallCount();
		float callCount = getCallCount();
		if( callCount == 0 ) {
			return 0;
		}
		return cacheEnabledCallCount /  callCount;
	}

	public float getCacheHitPercentage() {
		float cacheHitCallCount = getCacheHitCount();
		float callCount = getCallCount();
		if( callCount == 0 ) {
			return 0;
		}
		return cacheHitCallCount /  callCount;
	}


}
