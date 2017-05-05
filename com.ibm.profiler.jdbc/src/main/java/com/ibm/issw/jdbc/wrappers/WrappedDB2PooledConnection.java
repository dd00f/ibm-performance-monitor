package com.ibm.issw.jdbc.wrappers;

/*
 *-----------------------------------------------------------------
 * IBM Confidential
 *
 * OCO Source Materials
 *
 * WebSphere Commerce
 *
 * (C) Copyright IBM Corp. 2014
 *
 * The source code for this program is not published or otherwise
 * divested of its trade secrets, irrespective of what has
 * been deposited with the U.S. Copyright Office.
 *-----------------------------------------------------------------
 */

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sql.PooledConnection;

import com.ibm.db2.jcc.DB2Connection;

/**
 * 
 * WrappedDB2PooledConnection
 */
public class WrappedDB2PooledConnection extends WrappedPooledConnection {
	/**
	 * IBM Copyright notice field.
	 */
	public static final String COPYRIGHT = com.ibm.commerce.copyright.IBMCopyright.SHORT_COPYRIGHT;
	
	private static final Logger LOG = Logger
			.getLogger(WrappedDB2PooledConnection.class.getName());

	/**
	 * ctor
	 * @param pooledConn the connection to wrap
	 */
	public WrappedDB2PooledConnection(PooledConnection pooledConn) {
		super(pooledConn);
	}
	/*
	 * (non-Javadoc)
	 * @see com.ibm.issw.jdbc.wrappers.WrappedPooledConnection#getConnection()
	 */
	@Override
    public Connection getConnection() throws SQLException {
		PooledConnection pooledConnection = this.getPooledConnection();
        Connection conn = getConnectionFromPooledConnection(pooledConnection);

		if (LOG.isLoggable(Level.FINE)) {
			LOG.fine("Real connection " + conn.toString());
		}

		if ((conn instanceof WrappedConnection)) {
			return conn;
		}
		
		if ((conn instanceof DB2Connection)) {
			return new WrappedDB2Connection((DB2Connection) conn);
		}

		return new WrappedConnection(conn);
	}
	

}
