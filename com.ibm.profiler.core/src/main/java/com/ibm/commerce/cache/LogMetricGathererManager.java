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
package com.ibm.commerce.cache;

import org.apache.commons.lang3.StringUtils;

/**
 * LogMetricGathererManager
 * 
 * @author Steve McDuff
 */
public class LogMetricGathererManager
{
    public static final String GATHERER_FACTORY_CLASS_NAME_PROPERTY = "com.ibm.profiler.gathererFactoryClassName";

    private static final LogMetricGathererFactory FACTORY;

    static
    {
        String gathererClass = System.getProperty(GATHERER_FACTORY_CLASS_NAME_PROPERTY);
        LogMetricGathererFactory factory = null;

        if (!StringUtils.isBlank(gathererClass))
        {
            try
            {
                factory = (LogMetricGathererFactory) Class.forName(gathererClass).newInstance();
            }
            catch (Exception ex)
            {
                ex.printStackTrace();
            }
        }

        if (factory == null)
        {
            factory = new JulLogMetricGathererFactory();
        }
        FACTORY = factory;
    }

    private LogMetricGathererManager()
    {
        super();
    }

    /**
     * Get a log metric gatherer
     * 
     * @param loggerName
     *            the logger name
     * @return The created metric gatherer
     */
    public static ILogMetricGatherer getLogMetricGatherer(String loggerName)
    {
        return FACTORY.createLogMetricGatherer(loggerName);
    }

    public static ILogMetricGatherer getLogMetricGatherer(Class<?> class1)
    {
        return getLogMetricGatherer(class1.getName());
    }
}
