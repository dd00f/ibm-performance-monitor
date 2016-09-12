package com.ibm.issw.jdbc.wrappers;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.PooledConnection;

public class WrappedOraclePooledConnection extends WrappedPooledConnection {

	public WrappedOraclePooledConnection(PooledConnection connection) {
		super(connection);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.ibm.issw.jdbc.wrappers.WrappedPooledConnection#getConnection()
	 */
	@Override
    public Connection getConnection() throws SQLException {
		PooledConnection pooledConnection = this.getPooledConnection();
        Connection conn = getConnectionFromPooledConnection(pooledConnection);

        return WrappedOracleDriver.wrapOracleConnection(conn);
    }
}
