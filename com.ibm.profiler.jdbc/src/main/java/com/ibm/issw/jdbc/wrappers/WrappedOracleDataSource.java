package com.ibm.issw.jdbc.wrappers;

/*
 *-----------------------------------------------------------------
 * IBM Confidential
 *
 * OCO Source Materials
 *
 * WebSphere Commerce
 *
 * (C) Copyright IBM Corp. 2013, 2014
 *
 * The source code for this program is not published or otherwise
 * divested of its trade secrets, irrespective of what has
 * been deposited with the U.S. Copyright Office.
 *-----------------------------------------------------------------
 */

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import javax.sql.PooledConnection;

import oracle.jdbc.pool.OracleConnectionPoolDataSource;

import com.ibm.issw.jdbc.profiler.JdbcProfilerDaemon;
/**
 * 
 * WrappedOracleDataSource
 */
public class WrappedOracleDataSource extends
		OracleConnectionPoolDataSource {
	/**
	 * serialized UID 
	 */
	private static final long serialVersionUID = 8868362999561836439L;
	
	/**
	 * IBM Copyright notice field.
	 */
	public static final String COPYRIGHT = com.ibm.commerce.copyright.IBMCopyright.SHORT_COPYRIGHT;
	
	/**
	 * 
	 * @throws SQLException if anything goes wrong.
	 */
	public WrappedOracleDataSource() throws SQLException {
		JdbcProfilerDaemon.initializeDaemon();
	}

	/*
	 * (non-Javadoc)
	 * @see oracle.jdbc.pool.OracleConnectionPoolDataSource#getPooledConnection()
	 */
	@Override
    public PooledConnection getPooledConnection() throws SQLException {
		PooledConnection pooledConn = super.getPooledConnection();
		return wrapOraclePooledConnection(pooledConn);
	}
	/*
	 * (non-Javadoc)
	 * @see oracle.jdbc.pool.OracleConnectionPoolDataSource#getPooledConnection(java.lang.String, java.lang.String)
	 */
	@Override
    public PooledConnection getPooledConnection(String user, String pass)
			throws SQLException {
		PooledConnection pooledConn = super.getPooledConnection(user, pass);
		return wrapOraclePooledConnection(pooledConn);
	}

	private PooledConnection wrapOraclePooledConnection(
			PooledConnection pooledConn) {
		if ((pooledConn instanceof WrappedPooledConnection)) {
			return pooledConn;
		}
		return new WrappedOraclePooledConnection(pooledConn);
	}
	/*
	 * (non-Javadoc)
	 * @see oracle.jdbc.pool.OracleDataSource#getConnection()
	 */
	@Override
    public Connection getConnection() throws SQLException {
		Connection conn = super.getConnection();
		return WrappedOracleDriver.wrapOracleConnection(conn);
	}

	/*
	 * (non-Javadoc)
	 * @see oracle.jdbc.pool.OracleDataSource#getConnection(java.util.Properties)
	 */
	@Override
    public Connection getConnection(Properties props) throws SQLException {
		Connection conn = super.getConnection(props);
		return WrappedOracleDriver.wrapOracleConnection(conn);
	}
	/*
	 * (non-Javadoc)
	 * @see oracle.jdbc.pool.OracleDataSource#getConnection(java.lang.String, java.lang.String, java.util.Properties)
	 */
	@Override
    public Connection getConnection(String user, String pass, Properties props)
			throws SQLException {
		Connection conn = super.getConnection(user, pass, props);
		return WrappedOracleDriver.wrapOracleConnection(conn);
	}
	/*
	 * (non-Javadoc)
	 * @see oracle.jdbc.pool.OracleDataSource#getConnection(java.lang.String, java.lang.String)
	 */
	@Override
    public Connection getConnection(String user, String pass)
			throws SQLException {
		Connection conn = super.getConnection(user, pass);
		return WrappedOracleDriver.wrapOracleConnection(conn);
	}
}
