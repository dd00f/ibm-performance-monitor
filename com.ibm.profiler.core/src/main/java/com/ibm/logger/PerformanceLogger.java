// IBM Confidential OCO Source Material
// 5725-F81 (C) COPYRIGHT International Business Machines Corp. 2011,2014
// The source code for this program is not published or otherwise divested
// of its trade secrets, irrespective of what has been deposited with the
// U.S. Copyright Office.
package com.ibm.logger;

import java.io.Serializable;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javax.management.JMX;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.apache.commons.lang3.StringUtils;

import com.ibm.commerce.cache.CacheUtilities;
import com.ibm.commerce.cache.LoggingHelper;
import com.ibm.commerce.cache.OperationMetric;
import com.ibm.logger.jmx.JMXBeanRegistrar;
import com.ibm.logger.jmx.TimeIntervalLogEntryMXBean;
import com.ibm.logger.stats.LogEntry;
import com.ibm.logger.stats.LogType;
import com.ibm.logger.stats.TimeIntervalLogEntry;
import com.ibm.logger.stats.TotalLogEntry;
import com.ibm.logger.trace.SummaryPerformanceLogsToSystemOutPrinter;

/**
 * @author Bryan Johnson, Steve McDuff
 * 
 */
public class PerformanceLogger implements Serializable {

	private static final String PROPERTY_CSV_PRINT_INTERVAL_NAME = "com.ibm.logger.performanceLogger.csvPrintIntervalName";

	private static final String PROPERTY_CSV_PRINT_COUNT_LIMIT = "com.ibm.logger.performanceLogger.csvPrintCountLimit";

	private static final String PROPERTY_PERIODIC_METRIC_PRINT_CLASS_NAME = "com.ibm.logger.performanceLogger.periodicMetricPrintClassName";

	private static final String PROPERTY_PERFORMANCE_LOGGER_CLEANUP_DELAY_IN_MILLISECOND = "com.ibm.logger.performanceLogger.cleanupDelayInMillisecond";

	private static final String PROPERTY_PERIODIC_METRIC_PRINT_INTERVAL_IN_MILLISECOND = "com.ibm.logger.performanceLogger.periodicMetricPrintIntervalInMillisecond";

	private static final String PROPERTY_IS_PERIODIC_METRIC_PRINT_ENABLED = "com.ibm.logger.performanceLogger.isPeriodicMetricPrintEnabled";

	private static final String PROPERTY_PERIODIC_METRIC_PRINT_ENABLED = "com.ibm.logger.performanceLogger.PeriodicMetricPrintEnabled";

	private static final String PROPERTY_PERFORMANCE_LOGGER_ENABLED = "com.ibm.logger.performanceLogger.enabled";

	private static final String DEFAULT_PERIODIC_PRINTER = SummaryPerformanceLogsToSystemOutPrinter.class
			.getName();

	private static final int MILLIS_PER_NANO = 1000000;

	private static final String CLASS_NAME = PerformanceLogger.class.getName();

	public static final String TOTAL_INTERVAL_NAME = "total";

	public static final String LINE_SEPARATOR = System
			.getProperty("line.separator");

	private static class LogEntryThreadLocal extends
			ThreadLocal<Map<String, LogEntry>> implements Serializable {
		/**
		 * serial ID
		 */
		private static final long serialVersionUID = -3786835408569755981L;

		@Override
		protected synchronized Map<String, LogEntry> initialValue() {
			return new HashMap<String, LogEntry>();
		}
	}

	private static final Object globalLock = new Object();

	private static boolean isEnabled = false;

	private static boolean isPeriodicMetricPrintEnabled = false;

	private static int periodicMetricPrintIntervalInMillisecond = 60 * 1000;

	private static int cleanupDelayInMillisecond = 60 * 60 * 1000;

	protected static int loggerCountBeforeCleanup = 5000;
	
	protected static int csvPrintCountLimit = 10000;
	
	private static boolean csvCountLimitReached = false;

	private static String periodicMetricPrintClassName = DEFAULT_PERIODIC_PRINTER;

	private static String csvPrintIntervalName = null;

	private static Runnable periodicMetricPrinter = new SummaryPerformanceLogsToSystemOutPrinter();

	protected static final AtomicLong lastPrint = new AtomicLong(
			System.currentTimeMillis());

	private static long[] intervals;

	private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);

	static {
		isEnabled = parseBooleanProperty(PROPERTY_PERFORMANCE_LOGGER_ENABLED,
				false);

		isPeriodicMetricPrintEnabled = parseBooleanProperty(
				PROPERTY_IS_PERIODIC_METRIC_PRINT_ENABLED, false)
				|| parseBooleanProperty(PROPERTY_PERIODIC_METRIC_PRINT_ENABLED,
						false);

		periodicMetricPrintIntervalInMillisecond = parseIntegerProperty(
				PROPERTY_PERIODIC_METRIC_PRINT_INTERVAL_IN_MILLISECOND, 60000);

		cleanupDelayInMillisecond = parseIntegerProperty(
				PROPERTY_PERFORMANCE_LOGGER_CLEANUP_DELAY_IN_MILLISECOND,
				3600000);
		
		csvPrintCountLimit =  parseIntegerProperty(
				PROPERTY_CSV_PRINT_COUNT_LIMIT,
				10000);

		try {
			periodicMetricPrintClassName = parseStringProperty(
					PROPERTY_PERIODIC_METRIC_PRINT_CLASS_NAME,
					DEFAULT_PERIODIC_PRINTER);
			periodicMetricPrinter = (Runnable) Class.forName(
					periodicMetricPrintClassName).newInstance();
		} catch (Exception e) {
			LoggingHelper.logUnexpectedException(LOGGER, CLASS_NAME,
					"staticInitialization, failed to create periodic metric printer : " + periodicMetricPrintClassName, e);
		}

		csvPrintIntervalName = parseStringProperty(
				PROPERTY_CSV_PRINT_INTERVAL_NAME, null);

		// default to measuring a minute interval.
		long[] defaultIntervals = new long[] {}; // 60000000000l = 1 minute
		String intervalStrings = System
				.getProperty("com.ibm.logger.performanceLogger.intervals");
		intervals = getIntervalsFromProperty(intervalStrings, defaultIntervals);
		PerformanceLoggerManager.getManager();
	}

	public static String parseStringProperty(String propertyName,
			String defaultValue) {
		String returnValue = defaultValue;
		String property = null;

		property = System.getProperty(propertyName, null);
		if (property != null) {
			LOGGER.log(Level.INFO, propertyName + " set to " + property);
			returnValue = property;
		}

		return returnValue;
	}
	
	public static int parseIntegerProperty(String propertyName,
			int defaultValue) {
		int returnValue = defaultValue;
		String property = null;
		try {
			property = System.getProperty(propertyName, null);
			if (property != null) {
				LOGGER.log(Level.INFO, propertyName + " set to " + property);
				returnValue = Integer.parseInt(property);
			}
		} catch (Exception e) {
			LOGGER.log(Level.WARNING, "Property : " + propertyName
					+ " has an invalid value of : " + property
					+ ", Using default value of : " + defaultValue, e);
		}
		return returnValue;
	}

	public static boolean parseBooleanProperty(String propertyName,
			boolean defaultValue) {
		boolean parseBoolean = defaultValue;
		String property = null;
		try {
			property = System.getProperty(propertyName, null);
			if (property != null) {
				LOGGER.log(Level.INFO, propertyName + " set to " + property);
				parseBoolean = Boolean.parseBoolean(property);
			}
		} catch (Exception e) {
			LOGGER.log(Level.WARNING, "Property : " + propertyName
					+ " has an invalid value of : " + property
					+ ", Using default value of : " + defaultValue, e);
		}
		return parseBoolean;
	}

	/**
	 * break down a string into an array of long
	 * 
	 * @param intervalStrings
	 *            the string of comma separated interval values.
	 * @return The intervals array;
	 */
	protected static long[] getIntervalsFromProperty(String intervalStrings,
			long[] defaultValue) {
		final String methodName = "getIntervalsFromProperty(String intervalStrings, long[] defaultValue)";
		long[] intervalsAssignment = defaultValue;

		if (intervalStrings != null) {
			if (StringUtils.isBlank(intervalStrings)) {
				// user chose to remove all intervals
				intervalsAssignment = new long[] {};
			} else {
				try {
					String[] split = intervalStrings.split(",");
					List<Long> intervalsToUse = new ArrayList<Long>();
					for (int i = 0; i < split.length; i++) {
						String intervalValue = split[i];
						try {
							long intervalDuration = CacheUtilities
									.getNanoDurationFromShortText(intervalValue);
							if (intervalDuration > 0) {
								intervalsToUse.add(intervalDuration);
							} else {
								throw new IllegalArgumentException(
										"Interval duration must be postive.");
							}
						} catch (Exception ex) {
							LOGGER.logp(Level.WARNING, CLASS_NAME, methodName,
									"Invalid interval duration : "
											+ intervalValue, ex);
						}
					}
					intervalsAssignment = new long[intervalsToUse.size()];
					int index = 0;
					for (Long value : intervalsToUse) {
						intervalsAssignment[index++] = value;
					}

				} catch (Exception ex) {
					LoggingHelper.logUnexpectedException(LOGGER, CLASS_NAME,
							methodName, ex);
				}
			}
		}
		return intervalsAssignment;
	}

	private static final long serialVersionUID = -9173634709021207651L;

	private static final LogEntryThreadLocal _timers = new LogEntryThreadLocal();

	private static final JMXBeanRegistrar<TotalLogEntry> statsRegister = new JMXBeanRegistrar<TotalLogEntry>();

	private static final Map<String, TimeIntervalLogEntryMXBean> _logEntries = new ConcurrentHashMap<String, TimeIntervalLogEntryMXBean>(
			128);

	private static final Pattern ILLEGAL_CHAR_PATTERN = Pattern
			.compile("[,=:\"*?]");

	private static final String METRIC_TYPE = "MetricType";

	private static final int LOCAL_TIMER_THRESHOLD = 100;

	protected static void setIntervals(long[] intervalValues) {
		intervals = intervalValues;
	}

	/**
	 * Add a value to an internal counter.
	 * 
	 * @param id
	 *            the statistics ID
	 * @param processed
	 *            the amount of time spent
	 * @return true if a measurement was made.
	 */
	public static boolean addStatistic(String id, float processed) {
		if (!isEnabled || id.equals("")) {
			return false;
		}

		increase(id, (long) processed);

		return true;
	}

	/**
	 * Get the name of a JMX bean with the specified parameters.
	 * 
	 * @param id
	 *            the metric ID
	 * @param interval
	 *            the interval name
	 * @param clazz
	 *            the class of the metric
	 * @return the object name.
	 */
	public static ObjectName channelMXBeanName(String id, String interval,
			Class<? extends TimeIntervalLogEntryMXBean> clazz) {
		String classSimpleName = clazz.getSimpleName();
		return channelMXBeanName(id, interval, classSimpleName);
	}

	/**
	 * Get the name of a JMX bean with the specified parameters.
	 * 
	 * @param id
	 *            the metric ID
	 * @param interval
	 *            the interval name
	 * @param metricType
	 *            the metric type name.
	 * @return the object name.
	 */
	public static ObjectName channelMXBeanName(String id, String interval,
			String metricType) {
		Hashtable<String, String> props = new Hashtable<String, String>();
		props.put("ID", quoteAsNecessary(id));
		props.put("interval", quoteAsNecessary(interval));
		props.put(METRIC_TYPE, metricType);

		try {
			return new ObjectName(PerformanceLoggerManager.JMX_DOMAIN, props);
		} catch (MalformedObjectNameException e) {
			throw new IllegalStateException(e);
		}
	}

	/**
	 * 
	 * Clear the internal counters
	 * 
	 */
	public static void clear() {
		try {
			PerformanceLogger.statsRegister.destroy();
			PerformanceLogger._logEntries.clear();
			TotalLogEntry.clearIntervalStatistics();
		} catch (MBeanRegistrationException e) {
			LoggingHelper
					.logUnexpectedException(LOGGER, CLASS_NAME, "clear", e);
		}
	}

	/**
	 * 
	 * dump performance logs tables to System.out
	 * 
	 */
	public static void dumpPerformanceLogs() {
		SummaryPerformanceLogsToSystemOutPrinter
				.dumpPerformanceLogsTableToSystemOut();
	}

	private static final String OP_STRING_LENGTH = "60";

	private static final String PRINT_METRIC = "%-" + OP_STRING_LENGTH + "."
			+ OP_STRING_LENGTH + "s %8d %11.2f %11d %11d %14.2f";

	private static final String HEADER_DASH = "%-" + OP_STRING_LENGTH + "."
			+ OP_STRING_LENGTH + "s %8.8s %11.11s %11.11s %11.11s %14.14s";

	private static final String HEADER_FORMAT = "%-" + OP_STRING_LENGTH + "."
			+ OP_STRING_LENGTH + "s %8.8s %11.11s %11.11s %11.11s %14.14s";

	/**
	 * Dump the performance logs table to a human readable multi-line string
	 * format.
	 * 
	 * @return The the performance logs table.
	 */
	public static String dumpPerformanceLogsTableToString() {
		StringBuilder build = new StringBuilder();
		build.append(LINE_SEPARATOR);
		build.append("Performance Logs");
		build.append(LINE_SEPARATOR);
		build.append(LINE_SEPARATOR);
		boolean headers = false;
		for (TimeIntervalLogEntryMXBean pil : PerformanceLogger
				.getPerformanceLogs().values()) {
			if (!headers) {
				headers = true;
				String dash = "===================================================================================================================================================";
				build.append(String.format(HEADER_FORMAT, "Name", "NumCalls",
						"AverageMS", "MinimumMS", "MaximumMS", "TotalMS"));
				build.append(LINE_SEPARATOR);
				build.append(String.format(HEADER_DASH, dash, dash, dash, dash,
						dash, dash));
				build.append(LINE_SEPARATOR);
			}
			// build.append( String.format( "%-45.45s %8d %11.2f %11.2f %11.2f",
			// pil.getName(), pil.getCallCount(), pil.getAverageDuration(),
			// pil.getMinimumDuration(), pil.getMaximumDuration() ) );
			build.append(String.format(PRINT_METRIC, pil.getName(),
					pil.getCallCount(), pil.getAverageDuration()
							/ MILLIS_PER_NANO, pil.getMinimumDuration()
							/ MILLIS_PER_NANO, pil.getMaximumDuration()
							/ MILLIS_PER_NANO, pil.getTotalDuration()
							/ MILLIS_PER_NANO));
			build.append(LINE_SEPARATOR);
		}

		String performanceLogsTable = build.toString();
		return performanceLogsTable;
	}

	/**
	 * Dump the performance logs to a CSV format.
	 * 
	 * @return The performance logs CSV string.
	 */
	public static String dumpPerformanceLogsCsvToString() {
		StringBuilder build = new StringBuilder();
		boolean headers = false;
		
		int count = 0;
		Collection<TimeIntervalLogEntryMXBean> valuesToPrint = PerformanceLogger
				.getPerformanceLogs().values();
		
		for (TimeIntervalLogEntryMXBean pil : valuesToPrint) {
			if (!headers) {
				headers = true;
				printCsvHeaders(build);
			}

			++count;
			
			// CSV Limit reached. Skip all printing from now on.
			if( count > csvPrintCountLimit) {
				
				printCsvCountLimitReachedWarning(valuesToPrint.size());
				
				break;
			}
			
			build.append(pil.getName().replaceAll(",", "-"));
			printIntervalMetricsToCsv(build, pil);

			if (csvPrintIntervalName != null) {
				TimeIntervalLogEntryMXBean metricByIntervalName = pil
						.getMetricByIntervalName(csvPrintIntervalName);
				if (metricByIntervalName != null) {
					printIntervalMetricsToCsv(build, metricByIntervalName);
				}
			}
			build.append(LINE_SEPARATOR);
		}
		String performanceLogCsv = build.toString();
		return performanceLogCsv;
	}
	
	public static void printCsvCountLimitReachedWarning(int size) {
		if (csvCountLimitReached) {
			return;
		}

		csvCountLimitReached = true;

		LOGGER.log(Level.WARNING,
				"Reached performance metric print size limit of "
						+ csvPrintCountLimit + ". Attempting to print " + size
						+ " metrics. Excess metrics will be ignored.");
	}

	private static void printCsvHeaders(StringBuilder build) {
		build.append("Name,Number of calls,Average Duration milliseconds,Minimum Duration milliseconds,Maximum Duration milliseconds,Total Duration milliseconds,Average Size,Maximum Size,Total Size,Cache enabled count,Cache hit count, Error count");

		if (csvPrintIntervalName != null) {
			build.append(",Number of calls ");
			build.append(csvPrintIntervalName);
			build.append(",Average Duration milliseconds ");
			build.append(csvPrintIntervalName);
			build.append(",Minimum Duration milliseconds ");
			build.append(csvPrintIntervalName);
			build.append(",Maximum Duration milliseconds ");
			build.append(csvPrintIntervalName);
			build.append(",Total Duration milliseconds ");
			build.append(csvPrintIntervalName);
			build.append(",Average Size ");
			build.append(csvPrintIntervalName);
			build.append(",Maximum Size ");
			build.append(csvPrintIntervalName);
			build.append(",Total Size ");
			build.append(csvPrintIntervalName);
			build.append(",Cache enabled count ");
			build.append(csvPrintIntervalName);
			build.append(",Cache hit count ");
			build.append(csvPrintIntervalName);
			build.append(",Error count ");
			build.append(csvPrintIntervalName);
		}

		build.append(LINE_SEPARATOR);
	}

	private static void printIntervalMetricsToCsv(StringBuilder build,
			TimeIntervalLogEntryMXBean pil) {
		
		build.append(",");
		build.append(pil.getCallCount());
		build.append(",");
		build.append(String.format("%1.3f", pil.getAverageDuration()
				/ MILLIS_PER_NANO));
		build.append(",");
		build.append(pil.getMinimumDuration() / MILLIS_PER_NANO);
		build.append(",");
		build.append(pil.getMaximumDuration() / MILLIS_PER_NANO);
		build.append(",");
		build.append(String.format("%1.3f", pil.getTotalDuration()
				/ MILLIS_PER_NANO));
		build.append(",");
		build.append(String.format("%1.3f", pil.getAverageResponseSize()));
		build.append(",");
		build.append(pil.getMaximumResponseSize());
		build.append(",");
		build.append(String.format("%1.3f",pil.getTotalResponseSize()));
		build.append(",");
		build.append(pil.getCacheEnabledCount());
		build.append(",");
		build.append(pil.getCacheHitCount());
		build.append(",");
		build.append(pil.getErrorCallCount());
	}

	/**
	 * Get the objects that stored metrics for a specific operation by key.
	 * 
	 * @param key
	 *            the operation name key.
	 * @return the metrics. Null if no metrics were gathered on the key.
	 */
	public static TimeIntervalLogEntryMXBean getPerformanceLog(String key) {
		TimeIntervalLogEntryMXBean ret = getPerformanceLogs().get(key);
		return ret;

	}

	/**
	 * @return A map of all the metrics that were gathered by operation name
	 *         key.
	 */
	public static Map<String, TimeIntervalLogEntryMXBean> getPerformanceLogs() {
		return Collections.unmodifiableMap(_logEntries);
	}

	/**
	 * Log an operation execution and the time spent.
	 * 
	 * @param id
	 *            The operation name key.
	 * @param value
	 *            the time spent in the operation.
	 */
	public static void increase(String id, long value) {
		increase(id, value, false, LogType.STATISTIC);
	}

	/**
	 * Log an operation execution and the time spent.
	 * 
	 * @param id
	 *            The operation name key.
	 * @param value
	 *            the time spent in the operation.
	 * @param logType
	 *            the type of log to use.
	 */
	public static void increase(String id, long value, LogType logType) {
		increase(id, value, false, logType);
	}

	/**
	 * Log an operation execution and the time spent.
	 * 
	 * @param id
	 *            The operation name key.
	 * @param value
	 *            the time spent in the operation.
	 * @param failed
	 *            flag indicating if the operation failed.
	 */
	public static void increase(String id, long value, boolean failed) {
		increase(id, value, failed, LogType.STATISTIC);
	}

	/**
	 * Log an operation execution and the time spent.
	 * 
	 * @param id
	 *            The operation name key.
	 * @param value
	 *            the time spent in the operation.
	 * @param failed
	 *            flag indicating if the operation failed.
	 * @param logType
	 *            the type of log to use.
	 */
	public static void increase(String id, long value, boolean failed,
			LogType logType) {

		if (logType == null) {
			logType = LogType.STATISTIC;
		}

		if (isEnabled) {
			TimeIntervalLogEntryMXBean logEntry = PerformanceLogger
					.getOrCreateEntry(id, logType.name());
			logEntry.addValue(value, failed);
		}
	}

	/**
	 * Log an operation metric
	 * 
	 * @param metric
	 *            the metric to log.
	 */
	public static void increase(OperationMetric metric) {

		if (isEnabled) {
			String operationName = metric.getOperationName();
			String logTypeName = LogType.STATISTIC.name();
			TimeIntervalLogEntryMXBean logEntry = PerformanceLogger
					.getOrCreateEntry(operationName, logTypeName);

			long duration = metric.getDuration();
			int resultSize = metric.getResultSize();
			boolean operationCacheEnabled = metric.isOperationCacheEnabled();
			boolean resultFetchedFromCache = metric.isResultFetchedFromCache();
			boolean successful = metric.isSuccessful();

			logEntry.addValue(duration, resultSize, operationCacheEnabled,
					resultFetchedFromCache, !successful);
		}
	}

	/**
	 * @return true if the performance logger is enabled.
	 */
	public static boolean isEnabled() {
		return isEnabled;
	}

	/**
	 * quote a string so it becomes valid in JMX
	 * 
	 * @param value
	 *            the value to quote
	 * @return the quoted string.
	 */
	private static String quoteAsNecessary(String value) {
		return ILLEGAL_CHAR_PATTERN.matcher(value).find() ? ObjectName
				.quote(value) : value;
	}

	/**
	 * @param isEnabled
	 *            set whether or not the performance logger is enabled.
	 */
	public static void setEnabled(boolean isEnabled) {
		PerformanceLogger.isEnabled = isEnabled;
	}

	/**
	 * start logging an operation execution. This method assumes a stop logging
	 * call will be made later on in the same thread with the same ID.
	 * 
	 * @param id
	 *            The operation ID to start.
	 * @return true if a measurement is started.
	 */
	public static boolean startLogging(String id) {

		if (!isEnabled || id == null || id.equals("")) {
			return false;
		}
		boolean success = true;

		LogEntry match = _timers.get().get(id);

		if (match == null) {
			match = new LogEntry(id, LogType.METRIC);
			_timers.get().put(id, match);
		}
		match.startTimer(System.nanoTime());

		if (_timers.get().size() > LOCAL_TIMER_THRESHOLD) {
			flushTimers();
		}

		return success;
	}

	private static void flushTimers() {
		Map<String, LogEntry> localTimer = _timers.get();
		for (String _key : Collections.unmodifiableMap(localTimer).keySet()) {
			LogEntry check = localTimer.get(_key);
			if (!check.isInFlight()) {
				localTimer.remove(_key);
			}
		}

	}

	/**
	 * Stop timer and increment values
	 * 
	 * @param id
	 *            the operation ID to stop.
	 * @return true if a measurement was made.
	 */
	public static boolean stopLogging(String id) {
		if (!isEnabled || id == null || id.equals("")) {
			return false;
		}

		LogEntry match = _timers.get().get(id);
		if (match == null) {
			return false;
		}

		match.stopTimer(System.nanoTime());
		syncLocalStats(match);
		match.clear();
		return true;

	}

	/**
	 * Flush thread local values to global counters
	 * 
	 * @param localEntry
	 *            The log entry to add to the statistics.
	 */
	private static void syncLocalStats(final LogEntry localEntry) {
		TimeIntervalLogEntryMXBean myEntry = getOrCreateEntry(
				localEntry.getId(), localEntry.getType());
		myEntry.addValue((long) localEntry.getAverage());
	}

	protected static final AtomicLong lastCheck = new AtomicLong(
			System.currentTimeMillis());

	/**
	 * Get or create the JMX bean that will track metrics for an operation. This
	 * method is thread safe. Only a single bean for a single operation ID will
	 * ever be created.
	 * 
	 * @param id
	 *            the operation identifier.
	 * @param type
	 *            the type of log to produce.
	 * @return The TimeIntervalLogEntryMXBean used to track metrics.
	 */
	protected static TimeIntervalLogEntryMXBean getOrCreateEntry(
			final String id, final String type) {

		checkToPerformCleanupAndPrint();

		TimeIntervalLogEntryMXBean myEntry = _logEntries.get(id);
		if (myEntry == null) {
			synchronized (globalLock) {
				// check the in memory cache first
				myEntry = _logEntries.get(id);
				if (myEntry != null) {
					return myEntry;
				}
				myEntry = getOrCreateLogEntryOnCacheMiss(id);
				_logEntries.put(id, myEntry);
				return myEntry;
			}
		}
		return myEntry;
	}

	/**
	 * Get or create a log entry for the specific ID.
	 * 
	 * @param id
	 *            the logger ID.
	 * @return the log entry.
	 */
	protected static TimeIntervalLogEntryMXBean getOrCreateLogEntryOnCacheMiss(
			final String id) {
		TimeIntervalLogEntryMXBean myEntry = null;
		ObjectName channelMXBeanName = channelMXBeanName(id,
				TOTAL_INTERVAL_NAME, "TotalLogEntry");
		myEntry = fetchRegisteredPerformanceLogger(channelMXBeanName);

		if (myEntry == null) {
			myEntry = registerNewLogger(id, channelMXBeanName);
		}
		return myEntry;
	}

	/**
	 * Register a new logger with the specified ID using JMX.
	 * 
	 * @param id
	 *            The logger ID
	 * @param channelMXBeanName
	 *            the Logger bean name.
	 * @return The newly registered logger. Can also be the previously
	 *         registered logger if another classloader got there first.
	 */
	protected static TimeIntervalLogEntryMXBean registerNewLogger(
			final String id, ObjectName channelMXBeanName) {
		TimeIntervalLogEntryMXBean myEntry;
		final TotalLogEntry jmxEntry = new TotalLogEntry(id, intervals);
		final String methodName = "TotalLogEntry getOrCreateEntry( final String id, final String type )";
		try {
			JMXBeanRegistrar.JMXBeanProvider<TotalLogEntry> jmxBeanProvider = new JMXBeanRegistrar.JMXBeanProvider<TotalLogEntry>() {
				@Override
				public TotalLogEntry provide() {
					return jmxEntry;
				}
			};
			PerformanceLogger.statsRegister.retrieveOrRegister(
					channelMXBeanName, jmxBeanProvider);
			myEntry = jmxEntry;
		} catch (MBeanRegistrationException e) {
			LoggingHelper.logUnexpectedException(LOGGER, CLASS_NAME,
					methodName, e);

			// fallback to an unregistered bean if anything goes wrong.
			myEntry = jmxEntry;
		} catch (IllegalStateException e) {
			myEntry = fetchRegisteredPerformanceLogger(channelMXBeanName);

			if (myEntry == null) {
				LoggingHelper.logUnexpectedException(LOGGER, CLASS_NAME,
						methodName, e);

				myEntry = jmxEntry;
			}
		}
		return myEntry;
	}

	/**
	 * Fetch the registered performance logger.
	 * 
	 * @param channelMXBeanName
	 *            the bean name
	 * @return the registered bean, null if it doesn't exist.
	 */
	protected static TimeIntervalLogEntryMXBean fetchRegisteredPerformanceLogger(
			ObjectName channelMXBeanName) {
		MBeanServer platformMBeanServer = ManagementFactory
				.getPlatformMBeanServer();

		TimeIntervalLogEntryMXBean myEntry = null;
		try {
			if (platformMBeanServer.isRegistered(channelMXBeanName)) {
				myEntry = JMX.newMBeanProxy(platformMBeanServer,
						channelMXBeanName, TimeIntervalLogEntryMXBean.class);
			}
		} catch (Exception ex) {
			LoggingHelper
					.logUnexpectedException(
							LOGGER,
							CLASS_NAME,
							"TimeIntervalLogEntryMXBean getOrCreateEntry( final String id, final String type )",
							ex);
		}
		return myEntry;
	}

	/**
	 * This method will check once every hour to cleanup unused log entries to
	 * prevent memory leaks.
	 */
	private static void checkToPerformCleanupAndPrint() {
		long lastCheckTime = lastCheck.get();
		long currentTimeMillis = System.currentTimeMillis();
		// check every 1 hour
		long nextCheckTime = lastCheckTime + cleanupDelayInMillisecond;
		if (currentTimeMillis > nextCheckTime) {
			synchronized (globalLock) {

				// double check with the lock in place
				lastCheckTime = lastCheck.get();
				nextCheckTime = lastCheckTime + cleanupDelayInMillisecond;
				if (currentTimeMillis > nextCheckTime) {
					lastCheck.set(currentTimeMillis);
					performCleanup();
				}
			}
		}

		if (isPeriodicMetricPrintEnabled && periodicMetricPrinter != null) {
			long lastPrintTime = lastPrint.get();
			long nextPrint = lastPrintTime
					+ periodicMetricPrintIntervalInMillisecond;
			if (currentTimeMillis > nextPrint) {
				if (lastPrint.compareAndSet(lastPrintTime, currentTimeMillis)) {
					try {
						periodicMetricPrinter.run();
					} catch (Exception ex) {
						LoggingHelper.logUnexpectedException(LOGGER,
								CLASS_NAME, "checkToPerformCleanupAndPrint()",
								ex);
					}
				}
			}
		}

	}

	private static void performCleanup() {
		Map<ObjectName, TotalLogEntry> allRegisteredBeans = statsRegister
				.getAllRegisteredBeans();

		if (allRegisteredBeans.size() < loggerCountBeforeCleanup) {
			// The number of loggers can fit well in memory, no need to cleanup.
			return;
		}

		Set<Entry<ObjectName, TotalLogEntry>> entrySet = allRegisteredBeans
				.entrySet();
		for (Entry<ObjectName, TotalLogEntry> entry : entrySet) {
			TotalLogEntry value = entry.getValue();
			if (!value.isUsedSinceLastCheck()) {
				statsRegister.unregisterBean(entry.getKey());
				_logEntries.remove(value.getId());
				List<TimeIntervalLogEntry> intervalStatistics = value
						.getIntervalStatistics();
				for (TimeIntervalLogEntry timeIntervalLogEntry : intervalStatistics) {

					ObjectName channelMXBeanName = PerformanceLogger
							.channelMXBeanName(timeIntervalLogEntry.getId(),
									timeIntervalLogEntry.getIntervalName(),
									TimeIntervalLogEntry.class);

					TotalLogEntry.getIntervalStatsRegister().unregisterBean(
							channelMXBeanName);
				}
			}
		}

	}

}
