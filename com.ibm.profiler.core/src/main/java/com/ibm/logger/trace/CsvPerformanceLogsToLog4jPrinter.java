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

import org.apache.logging.log4j.CloseableThreadContext;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ibm.commerce.cache.LoggingHelper;
import com.ibm.logger.PerformanceLogger;
import com.ibm.service.summary.SummaryLogger;

/**
 * This class is used to periodically print the csv performance logs to a logger
 */
public class CsvPerformanceLogsToLog4jPrinter implements Runnable {

	public static java.util.logging.Logger LOGGER = java.util.logging.Logger
			.getLogger(CsvPerformanceLogsToLog4jPrinter.class.getName());

	public static Logger SUMMARY_LOGGER = LogManager.getLogger(SummaryLogger.class.getName());

	public CsvPerformanceLogsToLog4jPrinter() {
		super();
	}

	@Override
	public void run() {
		dumpPerformanceLogsCsvToLogger(SUMMARY_LOGGER, Level.INFO);
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
			if (logger.isEnabled(level)) {
				String performanceLogsTable = PerformanceLogger.dumpPerformanceLogsCsvToString();

				try (final CloseableThreadContext.Instance ctc = CloseableThreadContext.put("logType", "metric")) {
					logger.log(level, performanceLogsTable);
				}
			}
		} catch (Exception e) {
			LoggingHelper.logUnexpectedException(LOGGER, CsvPerformanceLogsToLog4jPrinter.class.getName(),
					"dumpPerformanceLogsTableToLogger", e);
		}
	}

}
