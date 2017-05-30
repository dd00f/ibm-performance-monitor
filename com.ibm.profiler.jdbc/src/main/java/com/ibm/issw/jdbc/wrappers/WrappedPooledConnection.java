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
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sql.ConnectionEventListener;
import javax.sql.PooledConnection;
import javax.sql.StatementEventListener;

import com.ibm.commerce.cache.OperationMetric;
import com.ibm.issw.jdbc.profiler.JdbcProfiler;
import com.ibm.service.detailed.JdbcLogger;

/**
 * 
 * WrappedPooledConnection
 */
public class WrappedPooledConnection implements PooledConnection {

	private static final Logger LOG = Logger
			.getLogger(WrappedPooledConnection.class.getName());
	private PooledConnection pooledConnection;
	/**
	 * 
	 * getPooledConnection
	 * @return pooled connection
	 */
	public PooledConnection getPooledConnection() {
		return pooledConnection;
	}

	/**
	 * ctor
	 * @param pooledConn the PooledConnection
	 */
	public WrappedPooledConnection(PooledConnection pooledConn) {
		this.pooledConnection = pooledConn;
	}
	/*
	 * (non-Javadoc)
	 * @see javax.sql.PooledConnection#getConnection()
	 */
	@Override
    public Connection getConnection() throws SQLException {
		Connection conn = getConnectionFromPooledConnection(this.pooledConnection);

		if (LOG.isLoggable(Level.FINE)) {
			LOG.fine("Real connection " + conn.toString());
		}

		if ((conn instanceof WrappedConnection)) {
			return conn;
		}

		return new WrappedConnection(conn);
	}
	/*
	 * (non-Javadoc)
	 * @see javax.sql.PooledConnection#close()
	 */
	@Override
    public void close() throws SQLException {
		if (LOG.isLoggable(Level.FINE)) {
			LOG.fine("Closing connection");
		}
		this.pooledConnection.close();
	}
	/*
	 * (non-Javadoc)
	 * @see javax.sql.PooledConnection#addConnectionEventListener(javax.sql.ConnectionEventListener)
	 */
	@Override
    public void addConnectionEventListener(ConnectionEventListener listener) {
		this.pooledConnection.addConnectionEventListener(listener);
	}
	/*
	 * (non-Javadoc)
	 * @see javax.sql.PooledConnection#removeConnectionEventListener(javax.sql.ConnectionEventListener)
	 */
	@Override
    public void removeConnectionEventListener(ConnectionEventListener listener) {
		this.pooledConnection.removeConnectionEventListener(listener);
	}

	/*
	 * (non-Javadoc)
	 * @see javax.sql.PooledConnection#addStatementEventListener(javax.sql.StatementEventListener)
	 */
	@Override
	public void addStatementEventListener(StatementEventListener arg0) {
		this.pooledConnection.addStatementEventListener(arg0);

	}
	/*
	 * (non-Javadoc)
	 * @see javax.sql.PooledConnection#removeStatementEventListener(javax.sql.StatementEventListener)
	 */
	@Override
	public void removeStatementEventListener(StatementEventListener arg0) {
		this.pooledConnection.removeStatementEventListener(arg0);

	}
	
    public static Connection getConnectionFromPooledConnection(PooledConnection pooledConnection) throws SQLException
    {
        boolean success = false;
        Connection returnedValue = null;
        OperationMetric metric = null;
        if( JdbcProfiler.isProfilingEnabled()) {
            metric = new OperationMetric();
            metric.startOperation("JDBC_PooledConnection_getConnection", false);
            JdbcLogger.GATHERER.gatherMetricEntryLog(metric);
        }
        try
        {
            returnedValue = pooledConnection.getConnection();
            success = true;
        }
        finally
        {
            if( metric != null ) {
                metric.stopOperation(1, false, success);
                JdbcLogger.GATHERER.gatherMetric(metric);
            }
        }
        return returnedValue;
    }

}
