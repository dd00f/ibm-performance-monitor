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
 * This class is used to periodicaly print the performance logs csv to System.out
 */
public class CsvPerformanceLogsToSystemOutPrinter implements Runnable
{

    public static Logger LOGGER = Logger.getLogger(CsvPerformanceLogsToSystemOutPrinter.class.getName());

    public CsvPerformanceLogsToSystemOutPrinter()
    {
        super();
    }

    @Override
    public void run()
    {
        dumpPerformanceLogsCsvToSystemOut();
    }

    /**
     * Dump the performance logs csv to System.out
     */
    public static void dumpPerformanceLogsCsvToSystemOut()
    {
        try
        {
            String performanceLogsTable = PerformanceLogger.dumpPerformanceLogsCsvToString();
            System.out.println(performanceLogsTable);
        }
        catch (Exception e)
        {
            LoggingHelper.logUnexpectedException(LOGGER, CsvPerformanceLogsToSystemOutPrinter.class.getName(),
                "dumpPerformanceLogsTableToLogger", e);
        }
    }

}
