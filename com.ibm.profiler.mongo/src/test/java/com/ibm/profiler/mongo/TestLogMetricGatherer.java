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
