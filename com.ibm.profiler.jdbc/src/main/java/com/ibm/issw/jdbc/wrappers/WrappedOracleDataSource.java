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
package com.ibm.issw.jdbc.wrappers;


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
