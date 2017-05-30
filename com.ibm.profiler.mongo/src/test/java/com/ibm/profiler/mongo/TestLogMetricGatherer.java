/*
 * #%L
 * IBM 10x
 * 
 * IBM Confidential
 * OCO Source Materials
 * %%
 * Copyright (C) 2013 - 2015 IBM Corp.
 * %%
 * The source code for this program is not published or otherwise divested of
 * its trade secrets, irrespective of what has been deposited with the U.S.
 * Copyright Office.  IBM and the IBM logo are trademarks of IBM Corporation
 * in the United States other countries, or both.  Java and all Java-based
 * trademarks and logos are trademarks or registered trademarks of Oracle
 * and/or its affiliates. Other company, product or service names may be
 * trademarks or service marks of others.
 * #L%
 */
package com.ibm.profiler.mongo;

import com.ibm.commerce.cache.ILogMetricGatherer;
import com.ibm.commerce.cache.OperationMetric;

/**
 * TestLogMetricGatherer
 * 
 * @author Steve McDuff
 */
public class TestLogMetricGatherer implements ILogMetricGatherer
{
    private OperationMetric lastEntryMetric;
    private OperationMetric lastMetric;
    private String loggerName;

    public TestLogMetricGatherer(String loggerName)
    {
        super();
        this.loggerName = loggerName;
    }
    
    public String getLoggerName()
    {
        return loggerName;
    }
    
    public OperationMetric getLastEntryMetric()
    {
        return lastEntryMetric;
    }
    
    public OperationMetric getLastMetric()
    {
        return lastMetric;
    }

    @Override
    public void gatherMetric(OperationMetric metric)
    {
        lastMetric = metric;
    }

    @Override
    public void start()
    {
    }

    @Override
    public void stop()
    {
    }

    @Override
    public boolean isEnabled()
    {
        return true;
    }

    @Override
    public boolean isEnabled(String marker)
    {
        return true;
    }

    @Override
    public void gatherMetricEntryLog(OperationMetric metric)
    {
        lastEntryMetric = metric;
    }
}
