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



import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sql.PooledConnection;

import com.ibm.db2.jcc.DB2ConnectionPoolDataSource;
import com.ibm.issw.jdbc.profiler.JdbcProfilerDaemon;

/**
 * 
 * WrappedDB2DataSource
 */
public final class WrappedDB2DataSource extends DB2ConnectionPoolDataSource {

	private static final long serialVersionUID = -3224567299738986531L;
	private static final Logger LOG = Logger
			.getLogger(WrappedDB2DataSource.class.getName());

	/**
	 * ctor
	 */
	public WrappedDB2DataSource() {
		JdbcProfilerDaemon.initializeDaemon();
		// JdbcEventManager.addJdbcEventListener(JdbcProfilerDaemon.getInstance());
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
		return new WrappedDB2PooledConnection(pooledConn);
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
		return new WrappedDB2PooledConnection(pooledConn);
	}
}
