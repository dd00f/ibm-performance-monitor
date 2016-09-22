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

import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Struct;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ibm.commerce.cache.OperationMetric;
import com.ibm.issw.jdbc.profiler.JdbcProfiler;
import com.ibm.service.detailed.JdbcLogger;

/**
 * 
 * WrappedConnection
 */
public class WrappedConnection implements Connection {
	/**
	 * IBM Copyright notice field.
	 */
	public static final String COPYRIGHT = com.ibm.commerce.copyright.IBMCopyright.SHORT_COPYRIGHT;
	
	private static final Logger LOG = Logger.getLogger(WrappedConnection.class
			.getName());
	private final Connection realConnection;
	protected static final RefCountThreadLocal REF_COUNT = new RefCountThreadLocal();
	
	private String transaction = getNextRefCount();
	
	/**
	 * ctor
	 * @param connection
	 */
	public WrappedConnection(Connection connection) {
		this.realConnection = connection;
	}
	
	/**
	 * Get the real connection
	 * @return
	 */
	public Connection getRealConnection() {
		return realConnection;
	}

	/**
	 * 
	 * isWrappingEnabled
	 * @return true if enabled
	 */
	public boolean isWrappingEnabled() {
		return JdbcProfiler.isProfilingEnabled();
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.sql.Connection#createStatement()
	 */
	@Override
    public final Statement createStatement() throws SQLException {
		Statement s = this.realConnection.createStatement();
		if (LOG.isLoggable(Level.FINE)) {
			LOG.fine("Real statement " + s.toString());
		}
		if (isWrappingEnabled()) {
			String ref = getNextRefCount();
			JdbcProfiler.getInstance().setStatementType(JdbcProfiler.STATEMENT, ref);
			return wrapStatement(s, ref);
		}
		return s;
	}

    protected WrappedStatement wrapStatement(Statement s, String ref)
    {
        String transaction = getTransactionIdentifier();
        return new WrappedStatement(s, ref, transaction, this);
    }

    protected String getTransactionIdentifier()
    {
        return transaction;
    }

    protected void endTransaction()
    {
        transaction = getNextRefCount();
    }

    /*
	 * (non-Javadoc)
	 * @see java.sql.Connection#prepareStatement(java.lang.String)
	 */
	@Override
    public final PreparedStatement prepareStatement(String sql)
			throws SQLException {

		boolean wrappingEnabled = isWrappingEnabled();
		String ref = null;
		if (wrappingEnabled) {
			ref = getNextRefCount();
			JdbcProfiler.getInstance().setStatementType(JdbcProfiler.PREPARED, ref);
			JdbcProfiler.getInstance().start(JdbcProfiler.OP_PREPARE, ref);
		}
		PreparedStatement prepareStatement = this.realConnection
				.prepareStatement(sql);
		if (wrappingEnabled) {

			PreparedStatement pstmt = null;
			pstmt = wrapPreparedStatement(sql, ref, prepareStatement);
			JdbcProfiler.getInstance().stop(JdbcProfiler.OP_PREPARE, ref);
			return pstmt;

		}
		return prepareStatement;
	}

    public static String getNextRefCount()
    {
        return REF_COUNT.nextValue().toString();
    }

    protected WrappedPreparedStatement wrapPreparedStatement(String sql, String ref, PreparedStatement prepareStatement)
    {
        String transactionIdentifier = getTransactionIdentifier();
        return new WrappedPreparedStatement(prepareStatement, sql, ref, transactionIdentifier,this);
    }
    
	/*
	 * (non-Javadoc)
	 * @see java.sql.Connection#prepareCall(java.lang.String)
	 */
	@Override
    public final CallableStatement prepareCall(String sql) throws SQLException {
		boolean wrappingEnabled = isWrappingEnabled();
		String ref = null;
		if (wrappingEnabled) {
			ref = getNextRefCount();
			JdbcProfiler.getInstance().setStatementType(JdbcProfiler.CALLABLE, ref);
			JdbcProfiler.getInstance().start(JdbcProfiler.OP_PREPARE, ref);
		}
		CallableStatement prepareCall = this.realConnection.prepareCall(sql);
		if (wrappingEnabled) {

			CallableStatement stmt = wrapCallableStatement(sql, ref, prepareCall);
			JdbcProfiler.getInstance().stop(JdbcProfiler.OP_PREPARE, ref);
			return stmt;
		}
		return prepareCall;
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.Connection#nativeSQL(java.lang.String)
	 */
	@Override
    public final String nativeSQL(String sql) throws SQLException {
		return this.realConnection.nativeSQL(sql);
	}

	/*
	 * (non-Javadoc)
	 * @see java.sql.Connection#setAutoCommit(boolean)
	 */
	@Override
    public final void setAutoCommit(boolean autoCommit) throws SQLException {
		this.realConnection.setAutoCommit(autoCommit);
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.Connection#getAutoCommit()
	 */
	@Override
    public final boolean getAutoCommit() throws SQLException {
		return this.realConnection.getAutoCommit();
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.Connection#commit()
	 */
	@Override
    public final void commit() throws SQLException {
        OperationMetric metric = null;
        String ref = null;
        boolean wrappingEnabled = isWrappingEnabled();
        if (wrappingEnabled) {
            ref = getNextRefCount();
            
            metric = new OperationMetric();
            metric.startOperation("JDBC_Commit", false, ref);
            metric.setProperty(JdbcProfiler.TRANSACTION_ID, transaction);
            JdbcLogger.LOG_GATHERER.gatherMetricEntryLog(metric);
            
            JdbcProfiler.getInstance().start(JdbcProfiler.OP_ROLLBACK, ref);
        }

        try
        {
            this.realConnection.commit();
        }
        finally
        {
            endTransaction();
            if (metric != null )
            {
                metric.stopOperation(1, false);
                JdbcLogger.LOG_GATHERER.gatherMetric(metric, true);
                JdbcProfiler.getInstance().stop(JdbcProfiler.OP_ROLLBACK, ref);
            }
        }   
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.Connection#rollback()
	 */
	@Override
    public final void rollback() throws SQLException {
        OperationMetric metric = null;
        String ref = null;
        boolean wrappingEnabled = isWrappingEnabled();
        if (wrappingEnabled) {
            ref = getNextRefCount();
            
            metric = new OperationMetric();
            metric.startOperation("JDBC_Rollback", false, ref);
            metric.setProperty(JdbcProfiler.TRANSACTION_ID, transaction);
            JdbcLogger.LOG_GATHERER.gatherMetricEntryLog(metric);
            
            JdbcProfiler.getInstance().start(JdbcProfiler.OP_ROLLBACK, ref);
        }

        try
        {
            this.realConnection.rollback();
        }
        finally
        {
            endTransaction();
            if (metric != null )
            {
                metric.stopOperation(1, false);
                JdbcLogger.LOG_GATHERER.gatherMetric(metric, true);
                JdbcProfiler.getInstance().stop(JdbcProfiler.OP_ROLLBACK, ref);
            }
        }		
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.Connection#close()
	 */
	@Override
    public final void close() throws SQLException {
        OperationMetric metric = null;
        boolean wrappingEnabled = isWrappingEnabled();
        if (wrappingEnabled) {
            String ref = getNextRefCount();
            
            metric = new OperationMetric();
            metric.startOperation("JDBC_Close", false, ref);
            metric.setProperty("transaction", transaction);
            JdbcLogger.LOG_GATHERER.gatherMetricEntryLog(metric);
        }

        try
        {
            this.realConnection.close();
            JdbcProfiler.getInstance().clearPendingEvents();
        }
        finally
        {
            endTransaction();
            if (metric != null )
            {
                metric.stopOperation(1, false);
                JdbcLogger.LOG_GATHERER.gatherMetric(metric, true);
            }
        }
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.Connection#isClosed()
	 */
	@Override
    public final boolean isClosed() throws SQLException {
		return this.realConnection.isClosed();
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.Connection#getMetaData()
	 */
	@Override
    public final DatabaseMetaData getMetaData() throws SQLException {
		return this.realConnection.getMetaData();
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.Connection#setReadOnly(boolean)
	 */
	@Override
    public final void setReadOnly(boolean readOnly) throws SQLException {
		this.realConnection.setAutoCommit(readOnly);
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.Connection#isReadOnly()
	 */
	@Override
    public final boolean isReadOnly() throws SQLException {
		return this.realConnection.isReadOnly();
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.Connection#setCatalog(java.lang.String)
	 */
	@Override
    public final void setCatalog(String catalog) throws SQLException {
		this.realConnection.setCatalog(catalog);
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.Connection#getCatalog()
	 */
	@Override
    public final String getCatalog() throws SQLException {
		return this.realConnection.getCatalog();
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.Connection#setTransactionIsolation(int)
	 */
	@Override
    public final void setTransactionIsolation(int level) throws SQLException {
		try {
			this.realConnection.setTransactionIsolation(level);
		} catch (SQLException e) {
			LOG.severe("Failed to set isolation level to " + level);
			throw e;
		}
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.Connection#getTransactionIsolation()
	 */
	@Override
    public final int getTransactionIsolation() throws SQLException {
		return this.realConnection.getTransactionIsolation();
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.Connection#getWarnings()
	 */
	@Override
    public final SQLWarning getWarnings() throws SQLException {
		return this.realConnection.getWarnings();
	}

	/*
	 * (non-Javadoc)
	 * @see java.sql.Connection#clearWarnings()
	 */
	@Override
    public final void clearWarnings() throws SQLException {
		this.realConnection.clearWarnings();
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.Connection#createStatement(int, int)
	 */
	@Override
    public final Statement createStatement(int resultSetType,
			int resultSetConcurrency) throws SQLException {
		Statement createStatement = this.realConnection.createStatement(
				resultSetType, resultSetConcurrency);
		if (isWrappingEnabled()) {
			String ref = getNextRefCount();
			JdbcProfiler.getInstance().setStatementType(JdbcProfiler.STATEMENT, ref);
			return wrapStatement(createStatement, ref);
		}
		return createStatement;
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.Connection#prepareStatement(java.lang.String, int, int)
	 */
	@Override
    public final PreparedStatement prepareStatement(String sql,
			int resultSetType, int resultSetConcurrency) throws SQLException {

		String ref = null;
		boolean wrappingEnabled = isWrappingEnabled();
		if (wrappingEnabled) {
			ref = getNextRefCount();
			JdbcProfiler.getInstance().setStatementType(JdbcProfiler.PREPARED, ref);
			JdbcProfiler.getInstance().start(JdbcProfiler.OP_PREPARE, ref);
		}
		PreparedStatement prepareStatement = this.realConnection
				.prepareStatement(sql, resultSetType, resultSetConcurrency);

		if (wrappingEnabled) {
			PreparedStatement pstmt = null;

			pstmt = wrapPreparedStatement(sql, ref, prepareStatement);

			JdbcProfiler.getInstance().stop(JdbcProfiler.OP_PREPARE, ref);
			return pstmt;
		}
		return prepareStatement;
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.Connection#prepareCall(java.lang.String, int, int)
	 */
	@Override
    public final CallableStatement prepareCall(String sql, int resultSetType,
			int resultSetConcurrency) throws SQLException {
		boolean wrappingEnabled = isWrappingEnabled();
		String ref = null;
		if (wrappingEnabled) {
			ref = getNextRefCount();
			JdbcProfiler.getInstance().setStatementType(JdbcProfiler.CALLABLE, ref);
			JdbcProfiler.getInstance().start(JdbcProfiler.OP_PREPARE, ref);
		}
		CallableStatement prepareCall = this.realConnection.prepareCall(sql,
				resultSetType, resultSetConcurrency);

		if (wrappingEnabled) {

			CallableStatement stmt = wrapCallableStatement(sql, ref, prepareCall);
			JdbcProfiler.getInstance().stop(JdbcProfiler.OP_PREPARE, ref);
			return stmt;
		}
		return prepareCall;
	}

    protected WrappedCallableStatement wrapCallableStatement(String sql, String ref, CallableStatement prepareCall)
    {
        String transactionIdentifier = getTransactionIdentifier();
        return new WrappedCallableStatement(prepareCall,
        		sql, ref, transactionIdentifier, this);
    }
    
	/*
	 * (non-Javadoc)
	 * @see java.sql.Connection#getTypeMap()
	 */
	@Override
    public final Map<String,Class<?>> getTypeMap() throws SQLException {
		return this.realConnection.getTypeMap();
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.Connection#setHoldability(int)
	 */
	@Override
    public final void setHoldability(int holdability) throws SQLException {
		this.realConnection.setHoldability(holdability);
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.Connection#getHoldability()
	 */
	@Override
    public final int getHoldability() throws SQLException {
		return this.realConnection.getHoldability();
	}

	/*
	 * (non-Javadoc)
	 * @see java.sql.Connection#setSavepoint()
	 */
	@Override
    public final Savepoint setSavepoint() throws SQLException {
		return this.realConnection.setSavepoint();
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.Connection#setSavepoint(java.lang.String)
	 */
	@Override
    public final Savepoint setSavepoint(String name) throws SQLException {
		return this.realConnection.setSavepoint(name);
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.Connection#rollback(java.sql.Savepoint)
	 */
	@Override
    public final void rollback(Savepoint savepoint) throws SQLException {
		this.realConnection.rollback(savepoint);
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.Connection#releaseSavepoint(java.sql.Savepoint)
	 */
	@Override
    public final void releaseSavepoint(Savepoint savepoint) throws SQLException {
		this.realConnection.releaseSavepoint(savepoint);
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.Connection#createStatement(int, int, int)
	 */
	@Override
    public final Statement createStatement(int resultSetType,
			int resultSetConcurrency, int resultSetHoldability)
			throws SQLException {
		Statement createStatement = this.realConnection.createStatement(
				resultSetType, resultSetConcurrency, resultSetHoldability);
		if (isWrappingEnabled()) {
			String ref = getNextRefCount();
			JdbcProfiler.getInstance().setStatementType(JdbcProfiler.STATEMENT, ref);
			return wrapStatement(createStatement, ref);
		}
		return createStatement;
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.Connection#prepareStatement(java.lang.String, int, int, int)
	 */
	@Override
    public final PreparedStatement prepareStatement(String sql,
			int resultSetType, int resultSetConcurrency,
			int resultSetHoldability) throws SQLException {
		boolean wrappingEnabled = isWrappingEnabled();
		String ref = null;
		if (wrappingEnabled) {
			ref = getNextRefCount();
			JdbcProfiler.getInstance().setStatementType(JdbcProfiler.PREPARED, ref);
			JdbcProfiler.getInstance().start(JdbcProfiler.OP_PREPARE, ref);
		}
		PreparedStatement prepareStatement = this.realConnection
				.prepareStatement(sql, resultSetType, resultSetConcurrency,
						resultSetHoldability);

		if (wrappingEnabled) {
			PreparedStatement pstmt = null;
			pstmt = wrapPreparedStatement(sql, ref, prepareStatement);
			JdbcProfiler.getInstance().stop(JdbcProfiler.OP_PREPARE, ref);
			return pstmt;
		}
		return prepareStatement;
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.Connection#prepareCall(java.lang.String, int, int, int)
	 */
	@Override
    public final CallableStatement prepareCall(String sql, int resultSetType,
			int resultSetConcurrency, int resultSetHoldability)
			throws SQLException {
		boolean wrappingEnabled = isWrappingEnabled();
		String ref = null;
		if (wrappingEnabled) {
			ref = getNextRefCount();
			JdbcProfiler.getInstance().setStatementType(JdbcProfiler.CALLABLE, ref);
			JdbcProfiler.getInstance().start(JdbcProfiler.OP_PREPARE, ref);
		}

		CallableStatement prepareCall = this.realConnection.prepareCall(sql,
				resultSetType, resultSetConcurrency, resultSetHoldability);

		if (wrappingEnabled) {

			CallableStatement stmt = wrapCallableStatement(sql, ref, prepareCall);
			JdbcProfiler.getInstance().stop(JdbcProfiler.OP_PREPARE, ref);
			return stmt;
		}
		return prepareCall;
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.Connection#prepareStatement(java.lang.String, int)
	 */
	@Override
    public final PreparedStatement prepareStatement(String sql,
			int autoGeneratedKeys) throws SQLException {
		boolean wrappingEnabled = isWrappingEnabled();
		String ref = null;
		if (wrappingEnabled) {
			ref = getNextRefCount();
			JdbcProfiler.getInstance().setStatementType(JdbcProfiler.PREPARED, ref);
			JdbcProfiler.getInstance().start(JdbcProfiler.OP_PREPARE, ref);

		}
		PreparedStatement prepareStatement = this.realConnection
				.prepareStatement(sql, autoGeneratedKeys);

		if (wrappingEnabled) {

			PreparedStatement pstmt = null;
			pstmt = wrapPreparedStatement(sql, ref, prepareStatement);
			JdbcProfiler.getInstance().stop(JdbcProfiler.OP_PREPARE, ref);
			return pstmt;
		}
		return prepareStatement;
	}

	/*
	 * (non-Javadoc)
	 * @see java.sql.Connection#prepareStatement(java.lang.String, int[])
	 */
	@Override
    public final PreparedStatement prepareStatement(String sql,
			int[] columnIndexes) throws SQLException {
		boolean wrappingEnabled = isWrappingEnabled();
		String ref = null;
		if (wrappingEnabled) {
			ref = getNextRefCount();
			JdbcProfiler.getInstance().setStatementType(JdbcProfiler.PREPARED, ref);
			JdbcProfiler.getInstance().start(JdbcProfiler.OP_PREPARE, ref);
		}
		PreparedStatement prepareStatement = this.realConnection
				.prepareStatement(sql, columnIndexes);

		if (wrappingEnabled) {
			PreparedStatement pstmt = null;
			pstmt = wrapPreparedStatement(sql, ref, prepareStatement);
			JdbcProfiler.getInstance().stop(JdbcProfiler.OP_PREPARE, ref);
			return pstmt;
		}
		return prepareStatement;
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.Connection#prepareStatement(java.lang.String, java.lang.String[])
	 */
	@Override
    public final PreparedStatement prepareStatement(String sql,
			String[] columnNames) throws SQLException {

		boolean wrappingEnabled = isWrappingEnabled();
		String ref = null;
		if (wrappingEnabled) {
			ref = getNextRefCount();
			JdbcProfiler.getInstance().setStatementType(JdbcProfiler.PREPARED, ref);
			JdbcProfiler.getInstance().start(JdbcProfiler.OP_PREPARE, ref);
		}
		PreparedStatement prepareStatement = this.realConnection
				.prepareStatement(sql, columnNames);

		if (wrappingEnabled) {
			PreparedStatement pstmt = null;
			pstmt = wrapPreparedStatement(sql, ref, prepareStatement);
			JdbcProfiler.getInstance().stop(JdbcProfiler.OP_PREPARE, ref);
			return pstmt;
		}
		return prepareStatement;
	}

	private static final class RefCountThreadLocal extends ThreadLocal<Long> {
		/*
		 * (non-Javadoc)
		 * @see java.lang.ThreadLocal#initialValue()
		 */
		@Override
        protected Long initialValue() {
			return new Long(0L);
		}

		/**
		 * @return the next value 
		 */
		public Long nextValue() {
			Long val = get();
			long l = val.intValue();
			l += 1L;

			if (9223372036854775807L == l) {
				l = 0L;
			}
			set(new Long(l));
			return val;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see java.sql.Connection#createArrayOf(java.lang.String, java.lang.Object[])
	 */
	@Override
	public Array createArrayOf(String typeName, Object[] elements)
			throws SQLException {
		return this.realConnection.createArrayOf(typeName, elements);
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.Connection#createBlob()
	 */
	@Override
	public Blob createBlob() throws SQLException {
		return this.realConnection.createBlob();
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.Connection#createClob()
	 */
	@Override
	public Clob createClob() throws SQLException {
		return this.realConnection.createClob();
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.Connection#createNClob()
	 */
	@Override
	public NClob createNClob() throws SQLException {
		return this.realConnection.createNClob();
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.Connection#createSQLXML()
	 */
	@Override
	public SQLXML createSQLXML() throws SQLException {
		return this.realConnection.createSQLXML();
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.Connection#createStruct(java.lang.String, java.lang.Object[])
	 */
	@Override
	public Struct createStruct(String typeName, Object[] attributes)
			throws SQLException {
		return this.realConnection.createStruct(typeName, attributes);
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.Connection#getClientInfo()
	 */
	@Override
	public Properties getClientInfo() throws SQLException {
		return this.realConnection.getClientInfo();
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.Connection#getClientInfo(java.lang.String)
	 */
	@Override
	public String getClientInfo(String name) throws SQLException {
		return this.realConnection.getClientInfo(name);
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.Connection#isValid(int)
	 */
	@Override
	public boolean isValid(int timeout) throws SQLException {
		return this.realConnection.isValid(timeout);
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.Connection#setClientInfo(java.util.Properties)
	 */
	@Override
	public void setClientInfo(Properties properties)
			throws SQLClientInfoException {
		this.realConnection.setClientInfo(properties);

	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.Connection#setClientInfo(java.lang.String, java.lang.String)
	 */
	@Override
	public void setClientInfo(String name, String value)
			throws SQLClientInfoException {
		this.realConnection.setClientInfo(name, value);
	}

	/*
	 * (non-Javadoc)
	 * @see java.sql.Connection#setTypeMap(java.util.Map)
	 */
	@Override
	public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
		this.realConnection.setTypeMap(map);
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.Wrapper#isWrapperFor(java.lang.Class)
	 */
	@Override
	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		return this.realConnection.isWrapperFor(iface);
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.Wrapper#unwrap(java.lang.Class)
	 */
	@Override
	public <T> T unwrap(Class<T> iface) throws SQLException {
		return this.realConnection.unwrap(iface);
	}

	/*
	 * (non-Javadoc)
	 * @see java.sql.Connection#abort(java.util.concurrent.Executor)
	 */
	@Override
	public void abort(Executor arg0) throws SQLException {
		this.realConnection.abort(arg0);
	}

	/*
	 * (non-Javadoc)
	 * @see java.sql.Connection#getNetworkTimeout()
	 */
	@Override
	public int getNetworkTimeout() throws SQLException {
		return this.realConnection.getNetworkTimeout();
	}

	/*
	 * (non-Javadoc)
	 * @see java.sql.Connection#getSchema()
	 */
	@Override
	public String getSchema() throws SQLException {
		return this.realConnection.getSchema();
	}

	/*
	 * (non-Javadoc)
	 * @see java.sql.Connection#setNetworkTimeout(java.util.concurrent.Executor, int)
	 */
	@Override
	public void setNetworkTimeout(Executor arg0, int arg1) throws SQLException {
		this.realConnection.setNetworkTimeout(arg0, arg1);
	}

	/*
	 * (non-Javadoc)
	 * @see java.sql.Connection#setSchema(java.lang.String)
	 */
	@Override
	public void setSchema(String arg0) throws SQLException {
		this.realConnection.setSchema(arg0);
	}
	
    public static void endDriverConnectMetric(boolean success, OperationMetric metric)
    {
        if( metric != null ) {
            metric.stopOperation(1, false, success);
            JdbcLogger.LOG_GATHERER.gatherMetric(metric);
        }
    }

    public static OperationMetric initializeDriverConnectMetric(OperationMetric metric)
    {
        if( JdbcProfiler.isProfilingEnabled()) {
            metric = new OperationMetric();
            metric.startOperation("JDBC_driver_connect", false);
            JdbcLogger.LOG_GATHERER.gatherMetricEntryLog(metric);
        }
        return metric;
    }
}

