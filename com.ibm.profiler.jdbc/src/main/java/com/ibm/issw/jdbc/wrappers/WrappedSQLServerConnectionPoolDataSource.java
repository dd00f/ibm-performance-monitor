package com.ibm.issw.jdbc.wrappers;

import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sql.PooledConnection;

import com.ibm.issw.jdbc.profiler.JdbcProfilerDaemon;
import com.microsoft.sqlserver.jdbc.SQLServerConnectionPoolDataSource;

public class WrappedSQLServerConnectionPoolDataSource extends SQLServerConnectionPoolDataSource {
	/**
	 * IBM Copyright notice field.
	 */
	public static final String COPYRIGHT = com.ibm.commerce.copyright.IBMCopyright.SHORT_COPYRIGHT;
	
	private static final long serialVersionUID = -3224567299738986531L;
	private static final Logger LOG = Logger
			.getLogger(WrappedSQLServerConnectionPoolDataSource.class.getName());

	/**
	 * ctor
	 */
	public WrappedSQLServerConnectionPoolDataSource() {
		JdbcProfilerDaemon.initializeDaemon();
	}

	/*
	 * (non-Javadoc)
	 * @see com.ibm.db2.jcc.DB2ConnectionPoolDataSource#getPooledConnection()
	 */
	@Override
    public PooledConnection getPooledConnection() throws SQLException {
		PooledConnection pooledConn = super.getPooledConnection();
		if (LOG.isLoggable(Level.FINE)) {
			LOG.fine("Real pooled connection " + pooledConn.toString());
		}

		if ((pooledConn instanceof WrappedPooledConnection)) {
			return pooledConn;
		}
		return new WrappedPooledConnection(pooledConn);
	}
	/*
	 * (non-Javadoc)
	 * @see com.ibm.db2.jcc.DB2ConnectionPoolDataSource#getPooledConnection(java.lang.String, java.lang.String)
	 */
	@Override
    public PooledConnection getPooledConnection(String user, String password)
			throws SQLException {
		PooledConnection pooledConn = super.getPooledConnection(user, password);
		if (LOG.isLoggable(Level.FINE)) {
			LOG.fine("Real pooled connection " + pooledConn.toString());
		}

		if ((pooledConn instanceof WrappedPooledConnection)) {
			return pooledConn;
		}
		return new WrappedPooledConnection(pooledConn);
	}
}
