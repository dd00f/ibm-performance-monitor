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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sql.DataSource;



/**
 * Performance metric gathering utility that utilizes a database to write the
 * captured metrics.
 */
public class DatabaseMetricGatherer extends AbstractMetricGatherer {
	/**
	 * IBM Copyright notice field.
	 */
	public static final String COPYRIGHT = com.ibm.commerce.copyright.IBMCopyright.SHORT_COPYRIGHT;

	/**
	 * Index used to write data in the SQL prepared statement.
	 */
	private static final int PARENT_IDENTIFIER_INDEX = 10;
	private static final int IDENTIFIER_INDEX = 9;
	private static final int UNIQUE_KEY_INDEX = 8;
	private static final int OPERATION_NAME_INDEX = 7;
	private static final int IS_CACHE_ENABLED_INDEX = 6;
	private static final int IS_CACHE_HIT_INDEX = 5;
	private static final int RESULT_SIZE_INDEX = 4;
	private static final int DURATION_INDEX = 3;
	private static final int STOP_TIME_INDEX = 2;
	private static final int START_TIME_INDEX = 1;

	/**
	 * insert data SQL statement.
	 */
	private static final String INSERT_METRIC_SQL = "insert into METRIC ("
			+ "STARTIME,STOPTIME,DURATION,RESULTSIZE,FROMCACHE,CACHEENABLED,OPERATIONNAME,KEYVALUE,IDENTIFIER,PARENTIDENTIFIER"
			+ ") values (?,?,?,?,?,?,?,?,?,?)";

	/**
	 * Class name
	 */
	private static final String CLASS_NAME = DatabaseMetricGatherer.class
			.getName();

	/**
	 * Logger.
	 */
	private static final Logger LOGGER = LoggingHelper.getLogger(CLASS_NAME);

	/**
	 * initial maximum database batch size.
	 */
	public static final int INITIAL_MAXIMUM_DATABASE_BATCH_SIZE = 1000;

	/**
	 * Maximum number of database insertions to be contained in a single
	 * database writing batch.
	 */
	private int maximumDatabaseBatchSize = INITIAL_MAXIMUM_DATABASE_BATCH_SIZE;

	/**
	 * Data source used to write all the data.
	 */
	private DataSource dataSource;

	/**
	 * should truncate sql select statements
	 */
	private boolean truncatingSqlSelect = true;

	/**
	 * Constructor.
	 */
	public DatabaseMetricGatherer() {
	}

	/**
	 * @return the maximum database batch size
	 */
	public int getMaximumDatabaseBatchSize() {
		return maximumDatabaseBatchSize;
	}

	/**
	 * @param maximumDatabaseBatchSize
	 *            the maximum database batch size
	 */
	public void setMaximumDatabaseBatchSize(int maximumDatabaseBatchSize) {
		this.maximumDatabaseBatchSize = maximumDatabaseBatchSize;
	}

	/**
	 * @param dataSource
	 *            the data source used by this gatherer
	 */
	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	/**
	 * @return the data source used by this gatherer
	 */
	public DataSource getDataSource() {
		return dataSource;
	}

	/**
	 * Override the {@link #isEnabled()} method to return false if the database
	 * connection isn't configured.
	 * 
	 * @return true if this gatherer is enabled.
	 */
	@Override
	public boolean isEnabled() {
		return dataSource != null;
	}

	/**
	 * @see AbstractMetricGatherer#writeMetrics()
	 */
	@Override
    public boolean writeMetrics() {

		final String METHODNAME = "writeMetrics()";
		boolean isTraceLogEnabled = LoggingHelper.isTraceEnabled(LOGGER);
		boolean entryExitLogEnabled = LoggingHelper
				.isEntryExitTraceEnabled(LOGGER);

		boolean retVal = false;

		if (entryExitLogEnabled) {
			LOGGER.entering(CLASS_NAME, METHODNAME);
		}

		List<OperationMetric> pendingMetricsToWrite = getPendingMetricsToWrite();

		Connection connection = null;
		PreparedStatement preparedStatement = null;

		if (dataSource == null) {
			return true;
		}

		try {

			try {
				if (isTraceLogEnabled) {
					String msg = "Writing " + pendingMetricsToWrite.size()
							+ " Operation Metrics.";
					LOGGER.log(Level.FINE, msg);
				}

				// $ANALYSIS-IGNORE
				connection = dataSource.getConnection();

				// $ANALYSIS-IGNORE
				preparedStatement = connection
						.prepareStatement(INSERT_METRIC_SQL);

				int i = 0;

				for (OperationMetric operationMetric : pendingMetricsToWrite) {

					retVal = true;

					addOperationMetricWriteToBatch(preparedStatement,
							operationMetric);

					i++;
					if (i % maximumDatabaseBatchSize == 0) {

						if (isTraceLogEnabled) {
							String traceMessage = "maximumDatabaseBatchSize of "
									+ maximumDatabaseBatchSize
									+ "reached. Trigger execute batch.";
							LOGGER.log(Level.FINE, traceMessage);
						}

						executeBatch(preparedStatement,
								maximumDatabaseBatchSize);
					}
				}

				executeBatch(preparedStatement, i % maximumDatabaseBatchSize);

				setRunningWithExceptions(false);
			} finally {
				CacheUtilities.closeQuietly(preparedStatement);
				CacheUtilities.closeQuietly(connection);
			}

		} catch (Exception ex) {
			logWriteMetricException(ex);
		}
		if (entryExitLogEnabled) {
			LOGGER.exiting(CLASS_NAME, METHODNAME);
		}

		return retVal;
	}

	/**
	 * Execute a batch of SQL statements. Used to gather timing metrics if trace
	 * logs are enabled.
	 * 
	 * @param preparedStatement
	 *            the prepared statement to execute.
	 * @param batchSize
	 *            the size of the batch being written.
	 * @throws SQLException
	 *             any exception that occurs while writing the batch.
	 */
	private void executeBatch(PreparedStatement preparedStatement, int batchSize)
			throws SQLException {
		boolean traceLogEnabled = LoggingHelper.isTraceEnabled(LOGGER);

		long start = 0;
		if (traceLogEnabled) {
			start = System.currentTimeMillis();
		}

		preparedStatement.executeBatch();

		if (traceLogEnabled) {
			long duration = System.currentTimeMillis() - start;

			logBatchWriteTime(batchSize, duration);
		}

	}

	/**
	 * Add an operation metric insert to a SQL execution batch
	 * 
	 * @param preparedStatement
	 *            the batch in which to insert the new data
	 * @param operationMetric
	 *            the operation metric to write
	 * @throws SQLException
	 *             any exception generated while adding the metrics to the
	 *             batch.
	 */
	private void addOperationMetricWriteToBatch(
			PreparedStatement preparedStatement, OperationMetric operationMetric)
			throws SQLException {

		final String METHODNAME = "addOperationMetricWriteToBatch(PreparedStatement preparedStatement, OperationMetric operationMetric)";
		boolean entryExitLogEnabled = LoggingHelper
				.isEntryExitTraceEnabled(LOGGER);

		if (entryExitLogEnabled) {
			Object[] params = { preparedStatement, operationMetric };
			LOGGER.entering(CLASS_NAME, METHODNAME, params);
		}

		preparedStatement.setLong(START_TIME_INDEX, operationMetric
				.getStartTime());
		preparedStatement.setLong(STOP_TIME_INDEX, operationMetric
				.getStopTime());
		preparedStatement
				.setLong(DURATION_INDEX, operationMetric.getDuration());
		preparedStatement.setInt(RESULT_SIZE_INDEX, operationMetric
				.getResultSize());
		boolean resultFetchedFromCache = operationMetric
				.isResultFetchedFromCache();
		String getBooleanString = getBooleanString(resultFetchedFromCache);
		preparedStatement.setString(IS_CACHE_HIT_INDEX, getBooleanString);
		preparedStatement.setString(IS_CACHE_ENABLED_INDEX,
				getBooleanString(operationMetric.isOperationCacheEnabled()));
		String operationName = operationMetric.getOperationName();
		operationName = adjustOperationName(operationName);
		preparedStatement.setString(OPERATION_NAME_INDEX, truncateString(
				operationName, MetricCompiler.OPERATION_NAME_MAXIMUM_LENGTH));
		preparedStatement.setString(UNIQUE_KEY_INDEX, truncateString(
				operationMetric.getUniqueKey(),
				MetricCompiler.UNIQUE_KEY_MAXIMUM_LENGTH));
		preparedStatement.setLong(IDENTIFIER_INDEX, operationMetric
				.getIdentifier());
		preparedStatement.setLong(PARENT_IDENTIFIER_INDEX, operationMetric
				.getParentIdentifier());

		preparedStatement.addBatch();

		if (entryExitLogEnabled) {
			LOGGER.exiting(CLASS_NAME, METHODNAME);
		}
	}

	/**
	 * should SQL select statements be truncated ?
	 * 
	 * @return true if they are.
	 */
	public boolean isTruncatingSqlSelect() {
		return truncatingSqlSelect;
	}

	/**
	 * should SQL select statements be truncated ?
	 * 
	 * @param truncatingSqlSelect
	 *            set to true to truncate, false otherwise.
	 */
	public void setTruncatingSqlSelect(boolean truncatingSqlSelect) {
		this.truncatingSqlSelect = truncatingSqlSelect;
	}

	/**
	 * Adjust the operation name if necessary.
	 * 
	 * @param operationName
	 *            the operation name
	 * @return the adjusted operation.
	 */
	private String adjustOperationName(String operationName) {
		String returnValue = operationName;
		if (truncatingSqlSelect) {
			returnValue = CacheUtilities
					.truncateSqlSelectStatement(operationName);
		}
		return returnValue;
	}

	/**
	 * Truncate a string to a maximum length
	 * 
	 * @param stringToTruncate
	 *            the string to truncate
	 * @param maximumLength
	 *            the maximum length
	 * @return the truncated string.
	 */
	public static String truncateString(String stringToTruncate,
			int maximumLength) {
		String retVal = stringToTruncate;
		if (stringToTruncate != null
				&& stringToTruncate.length() >= maximumLength) {
			retVal = stringToTruncate.substring(0, maximumLength - 1);
		}
		return retVal;
	}

	/**
	 * Fetch the string value of a boolean value
	 * 
	 * @param booleanValue
	 *            the boolean value
	 * @return the string value of "1" if true, "0" if false.
	 */
	private String getBooleanString(boolean booleanValue) {
		if (booleanValue) {
			return "1";
		}
		return "0";
	}

}
