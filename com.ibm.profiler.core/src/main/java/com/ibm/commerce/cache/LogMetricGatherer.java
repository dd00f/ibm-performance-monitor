package com.ibm.commerce.cache;

/*
 *-----------------------------------------------------------------
 * IBM Confidential
 *
 * OCO Source Materials
 *
 * WebSphere Commerce
 *
 * (C) Copyright IBM Corp. 2013, 2015
 *
 * The source code for this program is not published or otherwise
 * divested of its trade secrets, irrespective of what has
 * been deposited with the U.S. Copyright Office.
 *-----------------------------------------------------------------
 */

import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ibm.logger.PerformanceLogger;

/**
 * Gather performance metrics within a java logger output.
 */
public class LogMetricGatherer implements MetricGatherer {

	/** copyright */
	public static final String COPYRIGHT = com.ibm.commerce.copyright.IBMCopyright.SHORT_COPYRIGHT;

	/**
	 * class name
	 */
	private static final String CLASS_NAME = LogMetricGatherer.class.getName();

	/**
	 * logger
	 */
	private static final Logger LOGGER = LoggingHelper.getLogger(CLASS_NAME);

	/**
	 * default log level
	 */
	private static final Level DEFAULT_LOG_LEVEL = Level.FINE;

	/**
	 * default entry log level
	 */
	private static final Level DEFAULT_ENTRY_LOG_LEVEL = Level.FINER;

	/**
	 * prefix added to all performance logs
	 */
	public static final String COMMON_PREFIX = "PerfLog ";

	/**
	 * performance metric log prefix
	 */
	public static final String PERFORMANCE_METRIC_ENTRYLOG_PREFIX = COMMON_PREFIX
			+ "<entry";

	/**
	 * performance metric log property prefix
	 */
	public static final String PERFORMANCE_METRIC_INFO_PREFIX = COMMON_PREFIX
			+ "<info";

	/**
	 * exit log XML Tag prefix
	 */
	public static final String EXIT_XML_TAG_PREFIX = "<exit";

	/**
	 * performance metric log prefix
	 */
	public static final String PERFORMANCE_METRIC_LOG_PREFIX = COMMON_PREFIX
			+ EXIT_XML_TAG_PREFIX;

	/**
	 * performance metric log suffix
	 */
	public static final String PERFORMANCE_METRIC_LOG_SUFFIX = " />";

	/**
	 * logger to use
	 */
	private Logger logger;

	/**
	 * log level to use
	 */
	private Level level = DEFAULT_LOG_LEVEL;

	/**
	 * log level to use
	 */
	private Level entryLevel = DEFAULT_ENTRY_LOG_LEVEL;

	/**
	 * Constructor
	 * 
	 * @param setLogger
	 *            the logger to use
	 */
	public LogMetricGatherer(Logger setLogger) {
		logger = setLogger;
	}

	/**
	 * Empty constructor
	 */
	public LogMetricGatherer() {

	}

	/**
	 * get the log level.
	 * 
	 * @return the log level.
	 */
	public Level getLevel() {
		return level;
	}

	/**
	 * set the log level.
	 * 
	 * @param level
	 *            the log level.
	 */
	public void setLevel(Level level) {
		this.level = level;
	}

	/**
	 * Get the entry metric log level
	 * 
	 * @return The entry metric log level
	 */
	public Level getEntryLevel() {
		return entryLevel;
	}

	/**
	 * Set the new entry log level.
	 * 
	 * @param entryLevel
	 *            The new entry log level.
	 */
	public void setEntryLevel(Level entryLevel) {
		this.entryLevel = entryLevel;
	}

	/**
	 * get the logger.
	 * 
	 * @return the logger.
	 */
	public Logger getLogger() {
		return logger;
	}

	/**
	 * set the logger.
	 * 
	 * @param logger
	 *            the logger.
	 */
	public void setLogger(Logger logger) {
		this.logger = logger;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.ibm.commerce.cache.MetricGatherer#gatherMetric(com.ibm.commerce.cache
	 * .OperationMetric)
	 */
	@Override
	public void gatherMetric(OperationMetric metric) {
		logMetricToLogger(metric, logger, level);
	}
	
    public void gatherMetric(OperationMetric metric, boolean printProperties)
    {
        logMetricToLogger(metric, logger, level, null, printProperties);
    }

	/**
	 * gather entry log
	 * 
	 * @param metric
	 *            metric to log
	 */
	public void gatherMetricEntryLog(OperationMetric metric) {
		if (logger.isLoggable(entryLevel)) {
			logEntryLogMetricToLogger(metric, logger, entryLevel);
		}
	}

	/**
	 * log information into logger
	 * 
	 * @param level
	 *            level needed to log
	 * @param message
	 *            message to log
	 * @param properties
	 *            properties to log
	 */
	public void gatherInformationLog(Level level, String message,
			Map<String, String> properties) {
		String methodName = "gatherInformationLog(Level level, String message, Map<String, String> properties)";
		if (logger.isLoggable(level)) {
			StringWriter writer = new StringWriter();
			writer.append(PERFORMANCE_METRIC_INFO_PREFIX);
			try {
				writer.append(" parentId=\"");
				writer.append(Long.toString(OperationMetric
						.getThreadParentOperationIdentifier()));
				writer.append("\"");

				if (message != null) {
					writer.append(" msg=\"");
					CacheUtilities.printXmlEscapedStringToWriter(writer,
							message);
					writer.append("\" ");
				}
				if (properties != null) {
					OperationMetric.printPropertiesToWriter(properties, writer);
				}
				writer.append(PERFORMANCE_METRIC_LOG_SUFFIX);
				this.logger.logp(level, "", "", writer.toString());
			} catch (IOException e) {
				LoggingHelper.logUnexpectedException(LOGGER, CLASS_NAME,
						methodName, e);
			} finally {
				CacheUtilities.closeQuietly(writer);
			}
		}
	}

	/**
	 * Log a metric to a logger
	 * 
	 * @param metric
	 *            the metric to log
	 * @param currentLogger
	 *            the logger to use
	 * @param logLevel
	 *            the log level to use, if null, the default level of FINE will
	 *            be used.
	 */
	public static void logMetricToLogger(OperationMetric metric,
			Logger currentLogger, Level logLevel) {
		logMetricToLogger(metric, currentLogger, logLevel, null);
	}

	   /**
     * Log a metric to a logger
     * 
     * @param metric
     *            the metric to log
     * @param currentLogger
     *            the logger to use
     * @param logLevel
     *            the log level to use, if null, the default level of FINE will
     *            be used.
     * @param returnValue
     *            The return value to print in the logs.
     */
    public static void logMetricToLogger(OperationMetric metric,
            Logger currentLogger, Level logLevel, Object returnValue) {
        logMetricToLogger(metric, currentLogger, logLevel, returnValue, false);
    }
	
	/**
	 * Log a metric to a logger
	 * 
	 * @param metric
	 *            the metric to log
	 * @param currentLogger
	 *            the logger to use
	 * @param logLevel
	 *            the log level to use, if null, the default level of FINE will
	 *            be used.
	 * @param printProperties 
	 *            print properties if set to true.
	 * @param returnValue
	 *            The return value to print in the logs.
	 */
	public static void logMetricToLogger(OperationMetric metric,
			Logger currentLogger, Level logLevel, Object returnValue, boolean printProperties) {
		if (currentLogger == null) {
			return;
		}
		if (metric == null) {
			return;
		}
		if (logLevel == null) {
			logLevel = DEFAULT_LOG_LEVEL;
		}

		if (currentLogger.isLoggable(logLevel)) {
			StringWriter writer = new StringWriter();
			try {
				writer.append(LogMetricGatherer.PERFORMANCE_METRIC_LOG_PREFIX);
				metric.toSerializedXmlString(writer);
				if (returnValue != null) {
					writer.append(" return=\"");
					CacheUtilities.printXmlEscapedStringToWriter(writer,
							returnValue.toString());
					writer.append("\"");
				}
				if( printProperties ) {
				    metric.printProperty(writer);
				}

				writer.append(LogMetricGatherer.PERFORMANCE_METRIC_LOG_SUFFIX);
			} catch (IOException e) {
				LOGGER.log(Level.WARNING, "Failed to serialize string", e);
			}
			String logMessage = writer.toString();
			CacheUtilities.closeQuietly(writer);

			currentLogger.logp(logLevel, "", "", logMessage);
		}

		PerformanceLogger.increase(metric);
	}

	/**
	 * log entry log to logger
	 * 
	 * @param metric
	 *            the metric to log
	 * @param currentLogger
	 *            current logger
	 * @param logLevel
	 *            the level to log
	 */
	public static void logEntryLogMetricToLogger(OperationMetric metric,
			Logger currentLogger, Level logLevel) {
		if (currentLogger == null) {
			return;
		}
		if (metric == null) {
			return;
		}
		if (logLevel == null) {
			logLevel = DEFAULT_LOG_LEVEL;
		}

		// pass true to write for Entrylog, pass false to write for Exitlog
		currentLogger.logp(logLevel, "", "",
				OperationMetric.writeEntryExitLog(metric, true));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.commerce.cache.MetricGatherer#start()
	 */
	@Override
	public void start() {
		// nothing to do here.
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.commerce.cache.MetricGatherer#stop()
	 */
	@Override
	public void stop() {
		// nothing to do here.
	}

	/**
	 * Can the performance metric be logged.
	 * 
	 * @return true if the performance metric can be logged.
	 */
	public boolean isLoggable() {
		if (logger == null) {
			return false;
		}
		return logger.isLoggable(level)
				|| PerformanceLogger.isEnabled();
	}

}
