/* 
 * Copyright 2014 IBM. All rights reserved.
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
