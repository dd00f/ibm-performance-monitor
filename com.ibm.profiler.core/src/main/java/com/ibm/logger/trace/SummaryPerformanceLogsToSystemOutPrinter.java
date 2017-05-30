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
package com.ibm.logger.trace;

import java.util.logging.Logger;

import com.ibm.commerce.cache.LoggingHelper;
import com.ibm.logger.PerformanceLogger;

/**
 * This class is used to periodicaly print the performance logs to System.out
 */
public class SummaryPerformanceLogsToSystemOutPrinter implements Runnable
{

    public static Logger LOGGER = Logger.getLogger(SummaryPerformanceLogsToSystemOutPrinter.class.getName());

    public SummaryPerformanceLogsToSystemOutPrinter()
    {
        super();
    }

    @Override
    public void run()
    {
        dumpPerformanceLogsTableToSystemOut();
    }

    /**
     * Dump the performance logs table to the PerformanceLogger class Logger.
     */
    public static void dumpPerformanceLogsTableToSystemOut()
    {
        try
        {
            String performanceLogsTable = PerformanceLogger.dumpPerformanceLogsTableToString();
            System.out.println(performanceLogsTable);
        }
        catch (Exception e)
        {
            LoggingHelper.logUnexpectedException(LOGGER, SummaryPerformanceLogsToSystemOutPrinter.class.getName(),
                "dumpPerformanceLogsTableToLogger", e);
        }
    }

}
