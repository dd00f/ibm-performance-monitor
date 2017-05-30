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
