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
	/**
	 * IBM Copyright notice field.
	 */
	public static final String COPYRIGHT = com.ibm.commerce.copyright.IBMCopyright.SHORT_COPYRIGHT;

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
