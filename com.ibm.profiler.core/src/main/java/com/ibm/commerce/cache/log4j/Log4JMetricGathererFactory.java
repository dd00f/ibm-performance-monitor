/*
 */
package com.ibm.commerce.cache.log4j;

import org.apache.logging.log4j.LogManager;

import com.ibm.commerce.cache.ILogMetricGatherer;
import com.ibm.commerce.cache.LogMetricGathererFactory;

/**
 * Log4JMetricGathererFactory
 * 
 * @author Steve McDuff
 */
public class Log4JMetricGathererFactory implements LogMetricGathererFactory
{
    public Log4JMetricGathererFactory()
    {
        super();
    }

    @Override
    public ILogMetricGatherer createLogMetricGatherer(String loggerName)
    {
        return new Log4JMetricGatherer(LogManager.getLogger(loggerName));
    }
}
