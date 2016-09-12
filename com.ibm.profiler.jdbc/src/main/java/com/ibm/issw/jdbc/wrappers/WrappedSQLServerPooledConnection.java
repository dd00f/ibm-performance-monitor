package com.ibm.issw.jdbc.wrappers;

import java.sql.Connection;
import java.sql.SQLException;

import com.microsoft.sqlserver.jdbc.SQLServerConnection;
import com.microsoft.sqlserver.jdbc.SQLServerPooledConnection;

public class WrappedSQLServerPooledConnection extends WrappedPooledConnection {

	public WrappedSQLServerPooledConnection(SQLServerPooledConnection pooledConn) {
		super(pooledConn);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.ibm.issw.jdbc.wrappers.WrappedPooledConnection#getConnection()
	 */
	@Override
    public Connection getConnection() throws SQLException {
		Connection conn = getConnectionFromPooledConnection(this.getPooledConnection());

		return wrapSqlServerConnection(conn);
	}

	/**
	 * Wrap a SQL server connection
	 * @param conn
	 * @return
	 */
	public static Connection wrapSqlServerConnection(Connection conn) {
		if ((conn instanceof WrappedConnection)) {
			return conn;
		}
		
		if ((conn instanceof SQLServerConnection)) {
			return new WrappedSQLServerConnection((SQLServerConnection) conn);
		}

		return new WrappedConnection(conn);
	}
}
