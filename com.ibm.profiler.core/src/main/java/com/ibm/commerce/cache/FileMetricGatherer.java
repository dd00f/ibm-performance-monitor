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

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;




/**
 * Performance metric gathering utility that utilizes a file to write the
 * captured metrics.
 */
public class FileMetricGatherer extends AbstractMetricGatherer {

	/**
	 * IBM Copyright notice field.
	 */
	public static final String COPYRIGHT = com.ibm.commerce.copyright.IBMCopyright.SHORT_COPYRIGHT;

	// $ANALYSIS-IGNORE we want the same delimiter on all platforms
	/**
	 * Line separator.
	 */
	private static final String LINE_SEPARATOR = "\n";

	/**
	 * Class name
	 */
	private static final String CLASS_NAME = FileMetricGatherer.class.getName();

	/**
	 * Logger
	 */
	private static final Logger LOGGER = LoggingHelper.getLogger(CLASS_NAME);

	/**
	 * File name to be used when capturing performance metrics.
	 */
	private String fileName;

	/**
	 * Print writer used to write metric data. Linked to the
	 * {@link #bufferedOutputStream}
	 */
	private OutputStreamWriter writer;

	/**
	 * File output stream used to write metric data
	 */
	private FileOutputStream fileOutputStream;

	/**
	 * Buffered output stream used to write data to the
	 * {@link #fileOutputStream}
	 */
	private BufferedOutputStream bufferedOutputStream;

	/**
	 * @return the file name to use
	 */
	public String getFileName() {
		return fileName;
	}

	/**
	 * @param fileName
	 *            the file name to use
	 */
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	/**
	 * @see AbstractMetricGatherer#writeMetrics()
	 */
	@Override
    public synchronized boolean writeMetrics() {

		final String METHODNAME = "writeMetrics()";
		boolean retVal = false;

		boolean isTraceLogEnabled = LoggingHelper.isTraceEnabled(LOGGER);
		boolean entryExitLogEnabled = LoggingHelper
				.isEntryExitTraceEnabled(LOGGER);

		if (entryExitLogEnabled) {
			LOGGER.entering(CLASS_NAME, METHODNAME);
		}

		List<OperationMetric> pendingMetricsToWrite = getPendingMetricsToWrite();

		try {

			long start = 0;
			int batchSize = pendingMetricsToWrite.size();
			if (isTraceLogEnabled) {
				start = System.currentTimeMillis();
				String msg = "Writing " + batchSize + " Operation Metrics.";
				LOGGER.log(Level.FINE, msg);
			}

			initializeWriter();

			for (OperationMetric operationMetric : pendingMetricsToWrite) {

				operationMetric.toSerializedString(writer);
				writer.append(LINE_SEPARATOR);
				retVal = true;
			}

			writer.flush();

			if (isTraceLogEnabled) {
				long duration = System.currentTimeMillis() - start;
				logBatchWriteTime(batchSize, duration);
			}

			setRunningWithExceptions(false);

		} catch (Exception ex) {
			logWriteMetricException(ex);
		}

		if (entryExitLogEnabled) {
			LOGGER.exiting(CLASS_NAME, METHODNAME);
		}
		
		return retVal;
	}

	/**
	 * Initialize the writer to write on the file. If anything goes wrong, all
	 * the required objects are closed.
	 * 
	 * @throws Exception
	 *             any unexpected error when attempting the file for write
	 *             operations.
	 */
	private void initializeWriter() throws Exception {
		if (writer != null) {
			return;
		}

		final String METHODNAME = "initializeWriter()";
		boolean isTraceLogEnabled = LoggingHelper.isTraceEnabled(LOGGER);

		if (isTraceLogEnabled) {
			LOGGER.entering(CLASS_NAME, METHODNAME);
		}

		try {
			// $ANALYSIS-IGNORE this stream must remain open.
			fileOutputStream = new FileOutputStream(fileName);
			// $ANALYSIS-IGNORE this stream must remain open.
			bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
			// $ANALYSIS-IGNORE this writer must remain open.
			writer = new OutputStreamWriter(bufferedOutputStream, "UTF-8");

			if (LOGGER.isLoggable(Level.INFO)) {
				String msg = "FileMetricGatherer writer successfully created for file : "
						+ fileName;
				LOGGER.log(Level.INFO, msg);
			}
		} catch (Exception ex) {

			// exceptions will be logged by the thread calling the writeMetrics
			// method.
			closeWriterSilently();
			throw ex;
		}

		if (isTraceLogEnabled) {
			LOGGER.exiting(CLASS_NAME, METHODNAME);
		}
	}

	/**
	 * Ensure that we close the metrics gathering file before we stop this
	 * gatherer.
	 */
	@Override
	protected void executeBeforeBackgroundThreadStop() {
		final String METHODNAME = "executeBeforeBackgroundThreadStop()";
		boolean entryExitLogEnabled = LoggingHelper
				.isEntryExitTraceEnabled(LOGGER);
		if (entryExitLogEnabled) {
			LOGGER.entering(CLASS_NAME, METHODNAME);
		}

		super.executeBeforeBackgroundThreadStop();

		closeWriterSilently();

		if (entryExitLogEnabled) {
			LOGGER.exiting(CLASS_NAME, METHODNAME);
		}
	}

	/**
	 * Utility method to close all the writers and output streams silently.
	 */
	protected void closeWriterSilently() {
		final String METHODNAME = "closeWriterSilently()";
		boolean entryExitLogEnabled = LoggingHelper
				.isEntryExitTraceEnabled(LOGGER);
		if (entryExitLogEnabled) {
			LOGGER.entering(CLASS_NAME, METHODNAME);
		}

		CacheUtilities.closeQuietly(writer);
		CacheUtilities.closeQuietly(bufferedOutputStream);
		CacheUtilities.closeQuietly(fileOutputStream);

		writer = null;
		bufferedOutputStream = null;
		fileOutputStream = null;

		if (entryExitLogEnabled) {
			LOGGER.exiting(CLASS_NAME, METHODNAME);
		}
	}

}
