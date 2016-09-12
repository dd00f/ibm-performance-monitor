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
 * This class is used to periodically print the csv performance logs to a logger
 */
public class CsvPerformanceLogsToLoggerPrinter implements Runnable {

	public static Logger LOGGER = Logger
			.getLogger(CsvPerformanceLogsToLoggerPrinter.class.getName());

	public CsvPerformanceLogsToLoggerPrinter() {
		super();
	}

	@Override
	public void run() {
		dumpPerformanceLogsCsvToLogger(SummaryLogger.LOGGER, Level.INFO);
	}

	/**
	 * Dump the performance logs csv to the specified logger.
	 * 
	 * @param logger
	 *            The logger to use.
	 * @param level
	 *            The level to use.
	 */
	public static void dumpPerformanceLogsCsvToLogger(Logger logger, Level level) {
		try {
			if (logger.isLoggable(level)) {
				String performanceLogsTable = PerformanceLogger
						.dumpPerformanceLogsCsvToString();
				logger.log(level, performanceLogsTable);
			}
		} catch (Exception e) {
			LoggingHelper.logUnexpectedException(LOGGER,
					CsvPerformanceLogsToLoggerPrinter.class.getName(),
					"dumpPerformanceLogsTableToLogger", e);
		}
	}

}
