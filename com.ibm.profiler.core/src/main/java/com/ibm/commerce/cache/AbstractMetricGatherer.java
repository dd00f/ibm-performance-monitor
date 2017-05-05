package com.ibm.commerce.cache;

/*
 *-----------------------------------------------------------------
 * IBM Confidential
 *
 * OCO Source Materials
 *
 * WebSphere Commerce
 *
 * (C) Copyright IBM Corp. 2012, 2014
 *
 * The source code for this program is not published or otherwise
 * divested of its trade secrets, irrespective of what has
 * been deposited with the U.S. Copyright Office.
 *-----------------------------------------------------------------
 */

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;



/**
 * Abstract class containing reusable methods to gather potential cache metrics.
 * This class will provide the methods to keep the cache metrics and in memory
 * and write them in a separate thread to avoid any kind of latency caused by
 * the capture of those metrics.
 * <p>
 * The separate thread will sleep for a maximum amount of time specified in
 * {@link #getWriterMaximumSleepTime()} before writing new metrics. The metrics
 * gathering event will also wake up the thread if the number of pending record
 * reaches the value specified in {@link #getPendingFlushSize()}.
 * <p>
 * Any instance of this class must be started with the {@link #start()} method
 * and stopped with the {@link #stop()} method.
 * <p>
 * This class will also ensure that metrics are ignored if the write throughput
 * isn't sufficient to keep up with the system. This will ensure that the amount
 * of memory allocated to gather metrics will be limited by the maximum pending
 * size variable.
 * <p>
 * If any exception is caught during the process of writing metrics, only one
 * exception will be reported in the logs at the warning level. Other logs will
 * be available at the trace level.
 */
public abstract class AbstractMetricGatherer implements DirectMetricGather {
	/**
	 * IBM Copyright notice field.
	 */
	public static final String COPYRIGHT = com.ibm.commerce.copyright.IBMCopyright.SHORT_COPYRIGHT;

	/**
	 * Initial maximum number of pending operation metrics to keep in memory
	 * before writing.
	 */
	private static final int INITIAL_MAXIMUM_PENDING_SIZE = 1000;

	/**
	 * Initial number of pending operation metrics before triggering a write
	 * operation.
	 */
	private static final int INITIAL_PENDING_FLUSH_SIZE = 500;

	/**
	 * Initial maximum amount of time to wait for the writer thread to stop in
	 * milliseconds.
	 */
	private static final long INITIAL_THREAD_JOIN_TIMEOUT = 1000;

	/**
	 * Initial maximum amount of time for the writing thread to sleep before
	 * verifying if it needs to write a new batch of items.
	 */
	private static final long INITIAL_WRITER_MAXIMUM_SLEEP_TIME = 100;

	/**
	 * class name
	 */
	private static final String CLASS_NAME = AbstractMetricGatherer.class
			.getName();

	/**
	 * logger
	 */
	private static final Logger LOGGER = LoggingHelper.getLogger(CLASS_NAME);

	/**
	 * List of pending metrics. Its size will never go above the value
	 * configured in {@link #maximumPendingSize}. Access to this list must be
	 * synchronized since it's being used across multiple threads.
	 */
	private List<OperationMetric> pendingMetrics = new ArrayList<OperationMetric>();

	/**
	 * Maximum size that the {@link #pendingMetrics} list will be allowed to
	 * reach. Ensures that the amount of memory consumed by metrics gathering
	 * will never go above this defined limit.
	 */
	private int maximumPendingSize = INITIAL_MAXIMUM_PENDING_SIZE;

	/**
	 * Minimum number of records in the pending {@link #pendingMetrics} list to
	 * reach before waiting the writing thread to write the pending data.
	 */
	private int pendingFlushSize = INITIAL_PENDING_FLUSH_SIZE;

	/**
	 * Maximum amount of time the writing thread is allowed to sleep before
	 * writing any pending Operation Metrics.
	 */
	private long writerMaximumSleepTime = INITIAL_WRITER_MAXIMUM_SLEEP_TIME;

	/**
	 * Maximum amount of time to wait for the writing thread to finish it's work
	 * when stopping the metric gatherer.
	 */
	private long threadJoinTimeout = INITIAL_THREAD_JOIN_TIMEOUT;

	/**
	 * Metric writing thread runnable object.
	 */
	private MetricWriterRunnable metricWriter = new MetricWriterRunnable();

	/**
	 * The metric gathering thread running {@link #metricWriter}.
	 */
	private Thread metricWriterThread = new Thread(metricWriter);

	/**
	 * Flag indicating of the metric gatherer is running or not. It is consumed
	 * by the {@link #metricWriter} runnable to detect when it should exit.
	 */
	private boolean running = false;

	/**
	 * Flag indicating that exceptions were caught in the metric writing thread.
	 * While this flag is set to true, all exceptions will be logged with the
	 * {@link Level#FINE} level instead of {@link Level#WARNING} level.
	 */
	private boolean isRunningWithExceptions = false;

	/**
	 * object used to make threads wait while the buffer of performance logs to
	 * write is too full.
	 */
	private Object bufferFullWait = new Object();

	/**
	 * Metric writing runnable definition
	 */
	private class MetricWriterRunnable implements Runnable {

		/**
		 * Metric writing thread run entry method.
		 */
		@Override
		public void run() {
			runMetricWriter();
		}

	}

	/**
	 * run the metric writing thread
	 */
	private void runMetricWriter() {

		final String METHODNAME = "runMetricWriter()";
		boolean entryExitLogEnabled = LoggingHelper
				.isEntryExitTraceEnabled(LOGGER);

		if (entryExitLogEnabled) {
			LOGGER.entering(CLASS_NAME, METHODNAME);
		}

		while (true) {
			CacheUtilities.waitSilently(metricWriter, writerMaximumSleepTime);

			boolean keepRunning = true;
			while (keepRunning) {
				keepRunning = writeMetrics();
			}

			if (!isRunning()) {

				writeMetrics();

				executeBeforeBackgroundThreadStop();

				break;
			}
		}

		if (entryExitLogEnabled) {
			LOGGER.exiting(CLASS_NAME, METHODNAME);
		}
	}

	/**
	 * Optional method that sub classes may override in order to inject code
	 * before stopping their metric writing thread. Usually used to close off
	 * resources such as network sockets, database connections or files.
	 */
	protected void executeBeforeBackgroundThreadStop() {

	}

	/**
	 * this method is synchronized since it
	 * 
	 * @return true if the application is running
	 */
	public synchronized boolean isRunning() {
		return running;
	}

	/**
	 * @param running
	 *            set the new running state
	 */
	protected synchronized void setRunning(boolean running) {
		this.running = running;
	}

	/**
	 * constructor
	 */
	public AbstractMetricGatherer() {
	}

	/**
	 * Start the metric gathering writing thread.
	 */
	@Override
    public void start() {

		final String METHODNAME = "start()";
		boolean entryExitLogEnabled = LoggingHelper
				.isEntryExitTraceEnabled(LOGGER);

		if (entryExitLogEnabled) {
			LOGGER.entering(CLASS_NAME, METHODNAME);
		}

		if (isRunning()) {
			return;
		}
		metricWriterThread.start();
		setRunning(true);

		if (entryExitLogEnabled) {
			LOGGER.exiting(CLASS_NAME, METHODNAME);
		}
	}

	/**
	 * Stop the metric gathering writing thread.
	 */
	@Override
    public void stop() {
		final String METHODNAME = "stop()";
		boolean entryExitLogEnabled = LoggingHelper
				.isEntryExitTraceEnabled(LOGGER);

		if (entryExitLogEnabled) {
			LOGGER.entering(CLASS_NAME, METHODNAME);
		}

		if (!isRunning()) {
			return;
		}
		setRunning(false);
		CacheUtilities.joinQuietly(metricWriterThread, threadJoinTimeout);

		if (entryExitLogEnabled) {
			LOGGER.exiting(CLASS_NAME, METHODNAME);
		}
	}

	/**
	 * @see MetricGatherer#gatherMetric(OperationMetric)
	 */
	@Override
	public void gatherMetric(OperationMetric metric) {
		final String METHODNAME = "gatherMetric(OperationMetric metric)";
		boolean isTraceLogEnabled = LoggingHelper.isTraceEnabled(LOGGER);
		boolean entryExitLogEnabled = LoggingHelper
				.isEntryExitTraceEnabled(LOGGER);

		if (entryExitLogEnabled) {
			Object[] params = { metric };
			LOGGER.entering(CLASS_NAME, METHODNAME, params);
		}

		if (!isEnabled()) {
			if (isTraceLogEnabled) {
				LOGGER.log(Level.FINE,
						"Metric gatherer not enabled, metric ignored.");
			}
			return;
		}

		int size = 0;

		boolean running = isRunning();

		synchronized (pendingMetrics) {
			size = pendingMetrics.size();
			if (running || size >= maximumPendingSize) {
				pendingMetrics.add(metric);
			}
		}
		size += 1;

		if (size >= pendingFlushSize) {

			if (isTraceLogEnabled) {
				LOGGER.log(Level.FINE,
						"Metric gatherer pending log list reached the write triggering size of : "
								+ pendingFlushSize
								+ ". Waking the write thread.");
			}
			wakeWriteThread();
		}

		if (size >= maximumPendingSize) {

			if (isTraceLogEnabled) {
				LOGGER.log(Level.FINE,
						"Metric gatherer pending log list reached the maximum size of : "
								+ maximumPendingSize + ". Metric ignored.");
			}

			waitForWrite();
		}

		if (entryExitLogEnabled) {
			LOGGER.exiting(CLASS_NAME, METHODNAME);
		}
	}



	private void waitForWrite() {
		synchronized (bufferFullWait) {
			CacheUtilities.waitSilently(bufferFullWait, 1000);
		}
	}

//	private static int WAIT_COUNT = 0;
//	private static long WAIT_TIME = 0;
//	private void waitForWrite1() {
//		long start = System.nanoTime();
//		WAIT_COUNT++;
//		synchronized (bufferFullWait) {
//			CacheUtilities.waitSilently(bufferFullWait, 1000);
//		}
//		long duration = (System.nanoTime() - start) / 1000;
//		WAIT_TIME += duration;
//		long averageDuration = WAIT_TIME / (WAIT_COUNT);
//	}

	private void notifyAllOfWrite() {
		synchronized (bufferFullWait) {
			bufferFullWait.notifyAll();
		}
	}

	/**
	 * @return the pending flush size
	 */
	@Override
    public int getPendingFlushSize() {
		return pendingFlushSize;
	}

	/**
	 * @param pendingFlushSize
	 *            the new pending flush size
	 */
	public void setPendingFlushSize(int pendingFlushSize) {
		this.pendingFlushSize = pendingFlushSize;
	}

	/**
	 * Test if the metric gatherer is enabled enough to write metrics. If some
	 * configuration options are missing, this method will return false.
	 * 
	 * @return true if metrics can be gathered.
	 */
	@Override
    public boolean isEnabled() {
		return true;
	}

	/**
	 * Attempt to wake the metric gathering thread.
	 */
	private void wakeWriteThread() {
		final String METHODNAME = "wakeWriteThread()";
		boolean entryExitLogEnabled = LoggingHelper
				.isEntryExitTraceEnabled(LOGGER);

		if (entryExitLogEnabled) {
			LOGGER.entering(CLASS_NAME, METHODNAME);
		}

		synchronized (metricWriter) {
			metricWriter.notify();
		}

		if (entryExitLogEnabled) {
			LOGGER.exiting(CLASS_NAME, METHODNAME);
		}
	}

	/**
	 * This method is automatically called by the metric gathering thread in
	 * order to flush the pending operation metrics. It's implementation depends
	 * on the concrete class.
	 * @return true if a new metric was written.
	 */
	@Override
    public abstract boolean writeMetrics();

	/**
	 * Fetch a list of metrics to write. Once the metrics are returned by this
	 * method, they are removed from the pending list.
	 * 
	 * @return a list of metrics ready to be written.
	 */
	protected List<OperationMetric> getPendingMetricsToWrite() {
		List<OperationMetric> pendingMetricsToWrite = null;

		synchronized (pendingMetrics) {
			pendingMetricsToWrite = new ArrayList<OperationMetric>(
					pendingMetrics);
			pendingMetrics.clear();
		}

		notifyAllOfWrite();

		return pendingMetricsToWrite;
	}

	/**
	 * @return true if the writer is running with exceptions
	 */
	protected boolean isRunningWithExceptions() {
		return isRunningWithExceptions;
	}

	/**
	 * @param isRunningWithExceptions
	 *            set to true if the writer is running with exceptions
	 */
	public void setRunningWithExceptions(boolean isRunningWithExceptions) {
		this.isRunningWithExceptions = isRunningWithExceptions;
	}

	/**
	 * Utility method to fetch the log level of potentially recurring exceptions
	 * during the metric writing process.
	 * 
	 * @return the log level to use while logging exceptions during the write
	 *         process.
	 */
	protected Level getWriteMetricErrorLogLevel() {
		boolean runningWithExceptions = isRunningWithExceptions();
		Level logLevel = Level.WARNING;
		if (runningWithExceptions) {
			logLevel = Level.FINE;
		}
		return logLevel;
	}

	/**
	 * Utility method to log exceptions during the write process. This method
	 * will use the appropriate log level specified in
	 * {@link #getWriteMetricErrorLogLevel()} and ensure that future exceptions
	 * use the correct log level.
	 * 
	 * @param ex
	 *            the exception to log.
	 */
	protected void logWriteMetricException(Exception ex) {
		// check to avoid logging exceptions multiple times.
		Level logLevel = getWriteMetricErrorLogLevel();
		if (LOGGER.isLoggable(logLevel)) {
			String errorMessage = "Failed to write operation performance metrics. Pending metrics will be ignored.";
			LOGGER.log(logLevel, errorMessage, ex);
			setRunningWithExceptions(true);
		}
	}

	/**
	 * @return the maximum pending metric list size
	 */
	public int getMaximumPendingSize() {
		return maximumPendingSize;
	}

	/**
	 * @param maximumPendingSize
	 *            the maximum pending metric list size
	 */
	public void setMaximumPendingSize(int maximumPendingSize) {
		this.maximumPendingSize = maximumPendingSize;
	}

	/**
	 * @return the maximum time for the writer thread to sleep
	 */
	public long getWriterMaximumSleepTime() {
		return writerMaximumSleepTime;
	}

	/**
	 * @param writerMaximumSleepTime
	 *            the maximum time for the writer thread to sleep
	 */
	public void setWriterMaximumSleepTime(long writerMaximumSleepTime) {
		this.writerMaximumSleepTime = writerMaximumSleepTime;
	}

	/**
	 * @return the maximum time to wait for the writer thread to complete its
	 *         work
	 */
	public long getThreadJoinTimeout() {
		return threadJoinTimeout;
	}

	/**
	 * @param threadJoinTimeout
	 *            the maximum time to wait for the writer thread to complete its
	 *            work
	 */
	public void setThreadJoinTimeout(long threadJoinTimeout) {
		this.threadJoinTimeout = threadJoinTimeout;
	}

	/**
	 * log the batch size write time
	 * 
	 * @param batchSize
	 *            the batch size
	 * @param duration
	 *            the duration taken to write the batch
	 */
	protected void logBatchWriteTime(int batchSize, long duration) {
		// $ANALYSIS-IGNORE we want float precision for this metric
		float writePerSecond = ((float) batchSize) / ((float) duration) * 1000f;
		// $ANALYSIS-IGNORE we want to round up the value.
		int roundedWritePerSecond = (int) writePerSecond;
		String message = "Wrote " + batchSize + " records, execution took "
				+ duration + " ms, throughput = " + roundedWritePerSecond
				+ " records per seconds.";

		LOGGER.log(Level.FINE, message);
	}

	//$ANALYSIS-IGNORE
	/**
	 * fetch the internal pending metrics list
	 * 
	 * @return the pending metrics list
	 */
	protected List<OperationMetric> internalGetPendingMetrics() {
	    return pendingMetrics;
	}

}
