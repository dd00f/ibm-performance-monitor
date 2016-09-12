/* 
 * Copyright 2014 IBM. All rights reserved.
 */
package com.ibm.logger.trace;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.ibm.commerce.cache.LoggingHelper;
import com.ibm.logger.PerformanceLogger;
import com.ibm.service.summary.SummaryLogger;

/**
 * This class is used to periodically print the performance logs to a logger
 */
public class SummaryPerformanceLogsToLoggerPrinter implements Runnable {

	public static Logger LOGGER = Logger
			.getLogger(SummaryPerformanceLogsToLoggerPrinter.class.getName());

	public SummaryPerformanceLogsToLoggerPrinter() {
		super();
	}

	@Override
	public void run() {
		dumpPerformanceLogsTableToLogger(SummaryLogger.LOGGER, Level.INFO);
	}

	/**
	 * Dump the performance logs table to the specified logger.
	 * 
	 * @param logger
	 *            The logger to use.
	 * @param level
	 *            The level to use.
	 */
	public static void dumpPerformanceLogsTableToLogger(Logger logger,
			Level level) {
		try {
			if (logger.isLoggable(level)) {
				String performanceLogsTable = PerformanceLogger
						.dumpPerformanceLogsTableToString();
				logger.log(level, performanceLogsTable);
			}
		} catch (Exception e) {
			LoggingHelper.logUnexpectedException(LOGGER,
					SummaryPerformanceLogsToLoggerPrinter.class.getName(),
					"dumpPerformanceLogsTableToLogger", e);
		}
	}

}
