package com.ibm.issw.jdbc.wrappers;

import java.sql.Connection;

import com.ibm.issw.jdbc.profiler.JdbcProfilerDaemon;
import com.microsoft.sqlserver.jdbc.SQLServerDataSource;
import com.microsoft.sqlserver.jdbc.SQLServerException;

/**
 * Instrumented datasource for MS SQL Server
 */
public class WrappedSQLServerDataSource extends SQLServerDataSource {
	/**
	 * IBM Copyright notice field.
	 */
	public static final String COPYRIGHT = com.ibm.commerce.copyright.IBMCopyright.SHORT_COPYRIGHT;
	
	private static final long serialVersionUID = -3224567299738986531L;

	/**
	 * ctor
	 */
	public WrappedSQLServerDataSource() {
		JdbcProfilerDaemon.initializeDaemon();
	}
	
	@Override
	public Connection getConnection() throws SQLServerException {
		Connection connection = super.getConnection();
		return WrappedSQLServerPooledConnection.wrapSqlServerConnection(connection);
	}
	
	@Override
	public Connection getConnection(String arg0, String arg1)
			throws SQLServerException {
		Connection connection = super.getConnection(arg0, arg1);
		return WrappedSQLServerPooledConnection.wrapSqlServerConnection(connection);
	}
	
	
}
