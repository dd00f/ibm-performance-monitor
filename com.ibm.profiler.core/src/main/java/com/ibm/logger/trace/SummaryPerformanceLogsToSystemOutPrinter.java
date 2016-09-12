/* 
 * Copyright 2014 IBM. All rights reserved.
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
