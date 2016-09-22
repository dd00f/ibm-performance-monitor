package com.ibm.issw.jdbc.wrappers;

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

import com.ibm.issw.jdbc.profiler.JdbcEvent;
import com.ibm.issw.jdbc.profiler.JdbcProfiler;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 
 * WrappedStatement
 */
public class WrappedStatement implements Statement {
	/**
	 * IBM Copyright notice field.
	 */
	public static final String COPYRIGHT = com.ibm.commerce.copyright.IBMCopyright.SHORT_COPYRIGHT;

	private static final Logger LOG = Logger.getLogger(WrappedStatement.class
			.getName());

	private static final Pattern SELECT_PATTERN = Pattern.compile(
			"(\\bSELECT\\b.*\\bFROM\\b\\s)(.*)(\\bWHERE\\b)", 2);

	private static final Pattern SELECT_PATTERN_2 = Pattern.compile(
			"(^\\bSELECT\\b.*\\bFROM\\b\\s)(.*$)", 2);

	private static final Pattern INSERT_PATTERN = Pattern.compile(
			"(^\\bINSERT\\sINTO\\b\\s)(\\w+)(.*\\(.*$)", 2);

	private static final Pattern UPDATE_PATTERN = Pattern.compile(
			"(^\\bUPDATE\\s\\b)(\\b.+)(\\bSET\\b.*$)", 2);

	private static final Pattern DELETE_PATTERN = Pattern.compile(
			"(^\\bDELETE\\b.*\\bFROM\\b\\s)(\\w+)(.*$)", 2);
	private Statement stmt;
	
	/** reference */
	protected String ref;
	
	protected String transaction;
	
	protected final Connection connection;
	
	protected List<ResultSet> pendingResultSets = new ArrayList<ResultSet>();

	public WrappedStatement(Statement statement, String reference, String transaction, Connection connection) {
		this.stmt = statement;
		this.ref = reference;
		this.transaction = transaction;
		if (reference == null) {
			throw new AssertionError("reference can't be null");
		}
		if (statement == null) {
			throw new AssertionError("statement can't be null");
		}
		
		this.connection = connection;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.Statement#executeQuery(java.lang.String)
	 */
	@Override
    public final ResultSet executeQuery(String sql) throws SQLException {
		profileSqlStatement(sql);
		JdbcProfiler.getInstance().start(JdbcProfiler.OP_EXECUTE_QUERY,
				this.ref);
		JdbcEvent jdbcEvent = JdbcProfiler.getInstance().getJdbcEvent(ref);

		ResultSet result = null;
		ResultSet rslt = null;
		boolean success = false;
		try {
			result = this.stmt.executeQuery(sql);
			ResultSet executeQuery = result;
			rslt = wrapResultSet(jdbcEvent, executeQuery, this.ref);
			success = true;
		} finally {
			JdbcProfiler.getInstance().stop(JdbcProfiler.OP_EXECUTE_QUERY,
					this.ref);
			JdbcProfiler.getInstance().addStack(this.ref);
			if (!success) {
				// log the failure on error. Otherwise, let the result set do
				// it.
				JdbcProfiler.getInstance().addRowsRead(0, ref, success);
			}
		}
		return rslt;
	}

    protected ResultSet wrapResultSet(JdbcEvent jdbcEvent, ResultSet executeQuery, String ref)
    {
        ResultSet wrapResultSet = WrappedResultSet.wrapResultSet(
				executeQuery, ref, jdbcEvent, this);
		return wrapResultSet;
    }

	protected void addPendingResultSet(ResultSet wrapResultSet) {
		pendingResultSets.add(wrapResultSet);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.Statement#executeUpdate(java.lang.String)
	 */
	@Override
    public final int executeUpdate(String sql) throws SQLException {
		profileSqlStatement(sql);
		JdbcProfiler.getInstance().start(JdbcProfiler.OP_EXECUTE_UPDATE,
				this.ref);
		int rows = 0;
		boolean success = false;
		try {
			rows = this.stmt.executeUpdate(sql);
			success = true;
		} finally {
			JdbcProfiler.getInstance().stop(JdbcProfiler.OP_EXECUTE_UPDATE,
					this.ref);
			JdbcProfiler.getInstance().addStack(this.ref);
			JdbcProfiler.getInstance().addRowsUpdated(rows, this.ref, success);
		}
		
		return rows;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.Statement#close()
	 */
	@Override
    public final void close() throws SQLException {
		
		for (ResultSet pendingResultSet : pendingResultSets) {
			pendingResultSet.close();
		}
		pendingResultSets.clear();
		
		this.stmt.close();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.Statement#getMaxFieldSize()
	 */
	@Override
    public final int getMaxFieldSize() throws SQLException {
		return this.stmt.getMaxFieldSize();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.Statement#setMaxFieldSize(int)
	 */
	@Override
    public final void setMaxFieldSize(int max) throws SQLException {
		this.stmt.setMaxFieldSize(max);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.Statement#getMaxRows()
	 */
	@Override
    public final int getMaxRows() throws SQLException {
		return this.stmt.getMaxRows();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.Statement#setMaxRows(int)
	 */
	@Override
    public final void setMaxRows(int max) throws SQLException {
		this.stmt.setMaxRows(max);
	}


	/**
	 * 
	 * setEscapeProcessingfinal
	 * @param enable
	 * @throws SQLException
	 */
	public final void setEscapeProcessingfinal(boolean enable)
			throws SQLException {
		this.stmt.setEscapeProcessing(enable);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.Statement#getQueryTimeout()
	 */
	@Override
    public final int getQueryTimeout() throws SQLException {
		return this.stmt.getQueryTimeout();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.Statement#setQueryTimeout(int)
	 */
	@Override
    public final void setQueryTimeout(int seconds) throws SQLException {
		this.stmt.setQueryTimeout(seconds);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.Statement#cancel()
	 */
	@Override
    public final void cancel() throws SQLException {
		this.stmt.cancel();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.Statement#getWarnings()
	 */
	@Override
    public final SQLWarning getWarnings() throws SQLException {
		return this.stmt.getWarnings();
	}

	/*
	 * (non-Javadoc)
	 * @see java.sql.Statement#clearWarnings()
	 */
	@Override
    public final void clearWarnings() throws SQLException {
		this.stmt.clearWarnings();
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.Statement#setCursorName(java.lang.String)
	 */
	@Override
    public final void setCursorName(String name) throws SQLException {
		this.stmt.setCursorName(name);
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.Statement#execute(java.lang.String)
	 */
	@Override
    public final boolean execute(String sql) throws SQLException
    {
        profileSqlStatement(sql);
        JdbcProfiler.getInstance().start(JdbcProfiler.OP_EXECUTE_QUERY, this.ref);

        boolean result = false;
        boolean success = false;
        try
        {
            result = this.stmt.execute(sql);
            success = true;
        }
        finally
        {

            JdbcProfiler.getInstance().stop(JdbcProfiler.OP_EXECUTE_QUERY, this.ref);
            JdbcProfiler.getInstance().addStack(this.ref);
            JdbcProfiler.getInstance().addRowsUpdated(1, this.ref, success);
        }
        return result;
    }

	/*
	 * (non-Javadoc)
	 * @see java.sql.Statement#getResultSet()
	 */
	@Override
    public final ResultSet getResultSet() throws SQLException {
		ResultSet rs = this.stmt.getResultSet();
		if (rs != null) {
			JdbcEvent jdbcEvent = JdbcProfiler.getInstance().getJdbcEvent(ref);
			return wrapResultSet(jdbcEvent, rs,
					  this.ref);
		}

		JdbcProfiler.getInstance().addRowsUpdated(getUpdateCount(), this.ref, true);
		return rs;
	}

	/*
	 * (non-Javadoc)
	 * @see java.sql.Statement#getUpdateCount()
	 */
	@Override
    public final int getUpdateCount() throws SQLException {
		return this.stmt.getUpdateCount();
	}

	/*
	 * (non-Javadoc)
	 * @see java.sql.Statement#getMoreResults()
	 */
	@Override
    public final boolean getMoreResults() throws SQLException {
		return this.stmt.getMoreResults();
	}

	/*
	 * (non-Javadoc)
	 * @see java.sql.Statement#setFetchDirection(int)
	 */
	@Override
    public final void setFetchDirection(int direction) throws SQLException {
		this.stmt.setFetchDirection(direction);
	}

	/*
	 * (non-Javadoc)
	 * @see java.sql.Statement#getFetchDirection()
	 */
	@Override
    public final int getFetchDirection() throws SQLException {
		return this.stmt.getFetchDirection();
	}

	/*
	 * (non-Javadoc)
	 * @see java.sql.Statement#setFetchSize(int)
	 */
	@Override
    public final void setFetchSize(int rows) throws SQLException {
		this.stmt.setFetchSize(rows);
	}

	/*
	 * (non-Javadoc)
	 * @see java.sql.Statement#getFetchSize()
	 */
	@Override
    public final int getFetchSize() throws SQLException {
		return this.stmt.getFetchSize();
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.Statement#getResultSetConcurrency()
	 */
	@Override
    public final int getResultSetConcurrency() throws SQLException {
		return this.stmt.getResultSetConcurrency();
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.Statement#getResultSetType()
	 */
	@Override
    public final int getResultSetType() throws SQLException {
		return this.stmt.getResultSetType();
	}

	private List<String> batchStatement = null;

	/**
	 * 
	 * getBatchList
	 * @return batch list
	 */
	protected List<String> getBatchList() {
		if (batchStatement == null) {
			batchStatement = new ArrayList<String>();
		}
		return batchStatement;
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.Statement#addBatch(java.lang.String)
	 */
	@Override
    public final void addBatch(String sql) throws SQLException {
		getBatchList().add(sql);
		this.stmt.addBatch(sql);
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.Statement#clearBatch()
	 */
	@Override
    public final void clearBatch() throws SQLException {
		getBatchList().clear();
		this.stmt.clearBatch();
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.Statement#executeBatch()
	 */
	@Override
    public final int[] executeBatch() throws SQLException {

		String sql = getBatchSql();

		profileSqlStatement(sql);
		JdbcProfiler.getInstance().start(JdbcProfiler.OP_EXECUTE_BATCH, this.ref);
		int[] rows = null;
		boolean success = false;
		try {
		   rows = this.stmt.executeBatch();
		   success = true;
		}
		finally {
		JdbcProfiler.getInstance().stop(JdbcProfiler.OP_EXECUTE_BATCH, this.ref);
		JdbcProfiler.getInstance().addStack(this.ref);
        int rowCount = sumRows(rows);
        JdbcProfiler.getInstance().addRowsUpdated(rowCount, this.ref, success);
		}


		return rows;
	}

	private int sumRows(int[] rows) {
		int retVal = 0;
		if (rows != null) {
			for (int i : rows) {
				retVal += i;
			}
		}
		return retVal;
	}

	private String getBatchSql() {
		List<String> batchList = getBatchList();
		// set the batch size
		JdbcProfiler.getInstance().addSetData(-1, batchList.size(), ref);
		StringBuilder retVal = new StringBuilder("SQL Batch : ");
		String previous = null;
		for (String string : batchList) {
			if (previous == null) {
				previous = string;
			} else if (previous.equals(string)) {
				continue;
			}
			retVal.append(string);
			retVal.append(" - ");
		}
		return retVal.toString();
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.Statement#getConnection()
	 */
	@Override
    public final Connection getConnection() throws SQLException {
		return connection;
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.Statement#getMoreResults(int)
	 */
	@Override
    public final boolean getMoreResults(int current) throws SQLException {
		return this.stmt.getMoreResults(current);
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.Statement#getGeneratedKeys()
	 */
	@Override
    public final ResultSet getGeneratedKeys() throws SQLException {
		JdbcEvent jdbcEvent = JdbcProfiler.getInstance().getJdbcEvent(ref);

		ResultSet generatedKeys = this.stmt.getGeneratedKeys();
        return wrapResultSet(jdbcEvent, generatedKeys, this.ref);
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.Statement#executeUpdate(java.lang.String, int)
	 */
	@Override
    public final int executeUpdate(String sql, int autoGeneratedKeys)
			throws SQLException {
        profileSqlStatement(sql);
        JdbcProfiler.getInstance().start(JdbcProfiler.OP_EXECUTE_UPDATE, this.ref);

        int rows = 0;
        boolean success = false;
        try
        {
            rows = this.stmt.executeUpdate(sql, autoGeneratedKeys);
            success = true;
        }
        finally
        {
            JdbcProfiler.getInstance().stop(JdbcProfiler.OP_EXECUTE_UPDATE, this.ref);
            JdbcProfiler.getInstance().addStack(this.ref);
            JdbcProfiler.getInstance().addRowsUpdated(rows, this.ref, success);
        }
        return rows;
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.Statement#executeUpdate(java.lang.String, int[])
	 */
	@Override
    public final int executeUpdate(String sql, int[] columnIndexes)
			throws SQLException {
		profileSqlStatement(sql);
		JdbcProfiler.getInstance().start(JdbcProfiler.OP_EXECUTE_UPDATE, this.ref);
        int rows = 0;
        boolean success = false;
        try
        {
            rows = this.stmt.executeUpdate(sql, columnIndexes);
            success = true;
        }
        finally
        {
            JdbcProfiler.getInstance().stop(JdbcProfiler.OP_EXECUTE_UPDATE, this.ref);
            JdbcProfiler.getInstance().addStack(this.ref);
            JdbcProfiler.getInstance().addRowsUpdated(rows, this.ref, success);
        }
		return rows;
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.Statement#executeUpdate(java.lang.String, java.lang.String[])
	 */
	@Override
    public final int executeUpdate(String sql, String[] columnNames)
			throws SQLException {
		profileSqlStatement(sql);
		JdbcProfiler.getInstance().start(JdbcProfiler.OP_EXECUTE_UPDATE, this.ref);
        int rows = 0;
        boolean success = false;
        try
        {
            rows = this.stmt.executeUpdate(sql, columnNames);
            success = true;
        }
        finally
        {

            JdbcProfiler.getInstance().stop(JdbcProfiler.OP_EXECUTE_UPDATE, this.ref);
            JdbcProfiler.getInstance().addStack(this.ref);
            JdbcProfiler.getInstance().addRowsUpdated(rows, this.ref, success);
        }
		return rows;
	}

	/*
	 * (non-Javadoc)
	 * @see java.sql.Statement#execute(java.lang.String, int)
	 */
	@Override
    public final boolean execute(String sql, int autoGeneratedKeys)
			throws SQLException {
		profileSqlStatement(sql);
		JdbcProfiler.getInstance().start(JdbcProfiler.OP_EXECUTE_QUERY, this.ref);
		
        boolean result = false;
        boolean success = false;
        try
        {
            result = this.stmt.execute(sql, autoGeneratedKeys);
            success = true;
        }
        finally
        {
            JdbcProfiler.getInstance().stop(JdbcProfiler.OP_EXECUTE_QUERY, this.ref);
            JdbcProfiler.getInstance().addStack(this.ref);
            JdbcProfiler.getInstance().addRowsUpdated(1, this.ref, success);
        }
		return result;
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.Statement#execute(java.lang.String, int[])
	 */
	@Override
    public final boolean execute(String sql, int[] columnIndexes)
			throws SQLException {
		profileSqlStatement(sql);
        JdbcProfiler.getInstance().start(JdbcProfiler.OP_EXECUTE_QUERY, this.ref);
        boolean result = false;
        boolean success = false;
        try
        {
            result = this.stmt.execute(sql, columnIndexes);
            success = true;
        }
        finally
        {
            JdbcProfiler.getInstance().stop(JdbcProfiler.OP_EXECUTE_QUERY, this.ref);
            JdbcProfiler.getInstance().addStack(this.ref);
            JdbcProfiler.getInstance().addRowsUpdated(1, this.ref, success);
        }

		return result;
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.Statement#execute(java.lang.String, java.lang.String[])
	 */
	@Override
    public final boolean execute(String sql, String[] columnNames)
			throws SQLException {
		profileSqlStatement(sql);
		JdbcProfiler.getInstance().start(JdbcProfiler.OP_EXECUTE_QUERY, this.ref);
        boolean result = false;
        boolean success = false;
        try
        {
            result = this.stmt.execute(sql, columnNames);
            success = true;
        }
        finally
        {
            JdbcProfiler.getInstance().stop(JdbcProfiler.OP_EXECUTE_QUERY, this.ref);
            JdbcProfiler.getInstance().addStack(this.ref);
            JdbcProfiler.getInstance().addRowsUpdated(1, this.ref, success);
        }
		return result;
	}

	/*
	 * (non-Javadoc)
	 * @see java.sql.Statement#getResultSetHoldability()
	 */
	@Override
    public final int getResultSetHoldability() throws SQLException {
		return this.stmt.getResultSetHoldability();
	}

	/**
	 * 
	 * profileSqlStatement
	 * @param sql the sql to profile
	 */
	protected final void profileSqlStatement(String sql) {
		if (sql == null) {
			throw new AssertionError("SQL can not be null");
		}
		if (JdbcProfiler.isProfilingEnabled()) {
			String[] tables = getTableNames(sql);
			if (LOG.isLoggable(Level.FINE)) {
				StringBuilder msg = new StringBuilder();
				for (int i = 0; i < tables.length; i++) {
					msg.append(tables[i]).append(' ');
				}
				LOG.fine("Tables: " + msg);
			}
			JdbcProfiler.getInstance().addTableNames(tables, this.ref);
			JdbcProfiler.getInstance().addSqlStatement(sql, this.ref, transaction);
		}
	}

	private String[] getTableNames(String sql) {
		String[] names = null;
		List<String> tables = new ArrayList<String>();
		Matcher match = SELECT_PATTERN.matcher(sql);
		while (match.find()) {
			if (match.groupCount() > 1) {
				tables.add(match.group(2));
				if (match.group(3) != null) {
					String[] nested = getTableNames(match.group(1));
					if ((nested != null) && (nested.length > 0)) {
						for (int i = 0; i < nested.length;) {
							tables.add(nested[i]);
							return tables.toArray(new String[tables
									.size()]);
						}
					}
				}
			}
		}

		if (tables.size() == 0) {
			match = INSERT_PATTERN.matcher(sql);
			if (match.find()) {
				tables.add(match.group(2));
				return tables.toArray(new String[tables.size()]);
			}
		}

		if (tables.size() == 0) {
			match = UPDATE_PATTERN.matcher(sql);
			if (match.find()) {
				tables.add(match.group(2));
				return tables.toArray(new String[tables.size()]);
			}
		}

		if (tables.size() == 0) {
			match = DELETE_PATTERN.matcher(sql);
			if (match.find()) {
				tables.add(match.group(2));
				return tables.toArray(new String[tables.size()]);
			}
		}

		if (tables.size() == 0) {
			match = SELECT_PATTERN_2.matcher(sql);
			if (match.find()) {
				tables.add(match.group(2));
				return tables.toArray(new String[tables.size()]);
			}
		}
		names = tables.toArray(new String[tables.size()]);
		return names;
	}

	/*
	 * (non-Javadoc)
	 * @see java.sql.Statement#setEscapeProcessing(boolean)
	 */
	@Override
    public final void setEscapeProcessing(boolean enable) throws SQLException {
		this.stmt.setEscapeProcessing(enable);
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.Statement#isClosed()
	 */
	@Override
	public boolean isClosed() throws SQLException {
		return this.stmt.isClosed();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.Statement#isPoolable()
	 */
	@Override
	public boolean isPoolable() throws SQLException {
		return this.stmt.isPoolable();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.Statement#setPoolable(boolean)
	 */
	@Override
	public void setPoolable(boolean poolable) throws SQLException {
		this.stmt.setPoolable(poolable);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.Wrapper#isWrapperFor(java.lang.Class)
	 */
	@Override
	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		return this.stmt.isWrapperFor(iface);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.Wrapper#unwrap(java.lang.Class)
	 */
	@Override
	public <T> T unwrap(Class<T> iface) throws SQLException {
		return this.stmt.unwrap(iface);
	}

	/*
	 * (non-Javadoc)
	 * @see java.sql.Statement#closeOnCompletion()
	 */
	@Override
	public void closeOnCompletion() throws SQLException {
		this.stmt.closeOnCompletion();
	}

	/*
	 * (non-Javadoc)
	 * @see java.sql.Statement#isCloseOnCompletion()
	 */
	@Override
	public boolean isCloseOnCompletion() throws SQLException {
		return this.stmt.isCloseOnCompletion();
	}
}
