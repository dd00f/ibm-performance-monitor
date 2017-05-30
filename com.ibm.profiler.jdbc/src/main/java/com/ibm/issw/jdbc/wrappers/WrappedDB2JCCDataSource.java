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

import com.ibm.db2.jcc.DB2BaseDataSource;
import com.ibm.db2.jcc.DB2ConnectionPoolDataSource;
import com.ibm.issw.jdbc.profiler.JdbcProfilerDaemon;

/**
 * 
 * WrappedDB2JCCDataSource
 */
public class WrappedDB2JCCDataSource extends DB2ConnectionPoolDataSource {

	private static final long serialVersionUID = -7336054204593565525L;
	private static final Logger LOG = Logger
			.getLogger(WrappedDB2JCCDataSource.class.getName());

	/**
	 * ctor
	 */
	public WrappedDB2JCCDataSource() {
		JdbcProfilerDaemon.initializeDaemon();
		// JdbcEventManager.addJdbcEventListener(JdbcProfilerDaemon.getInstance());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.db2.jcc.DB2ConnectionPoolDataSource#getPooledConnection()
	 */
	@Override
    public PooledConnection getPooledConnection() throws SQLException {
		PooledConnection conn = super.getPooledConnection();
		if (LOG.isLoggable(Level.FINE)) {
			LOG.fine("Real pooled connection " + conn.toString());
		}

		if ((conn instanceof WrappedPooledConnection)) {
			return conn;
		}
		return new WrappedDB2PooledConnection(conn);
	}

	/**
	 * 
	 * getPooledConnection
	 * 
	 * @param ds the data source
	 * @param user the user
	 * @param pass the password
	 * @return connection the connection
	 * @throws SQLException if anything fails.
	 */
	public PooledConnection getPooledConnection(DB2BaseDataSource ds,
			String user, String pass) throws SQLException {
		PooledConnection conn = super.getPooledConnection(user, pass);
		if (LOG.isLoggable(Level.FINE)) {
			LOG.fine("Real pooled connection " + conn.toString());
		}

		if ((conn instanceof WrappedPooledConnection)) {
			return conn;
		}
		return new WrappedDB2PooledConnection(conn);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.ibm.db2.jcc.DB2ConnectionPoolDataSource#getPooledConnection(java.
	 * lang.Object)
	 */
	@Override
    public PooledConnection getPooledConnection(Object object)
			throws SQLException {
		PooledConnection conn = super.getPooledConnection(object);
		if (LOG.isLoggable(Level.FINE)) {
			LOG.fine("Real pooled connection " + conn.toString());
		}

		if ((conn instanceof WrappedPooledConnection)) {
			return conn;
		}
		return new WrappedDB2PooledConnection(conn);
	}
}
