/*
 */
package com.ibm.commerce.cache;

/**
 * LogMetricGathererFactory
 * 
 * @author Steve McDuff
 */
public interface LogMetricGathererFactory
{
    
    public ILogMetricGatherer createLogMetricGatherer( String loggerName );

}
