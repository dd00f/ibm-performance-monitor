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

import com.ibm.issw.jdbc.profiler.JdbcProfilerDaemon;
import com.microsoft.sqlserver.jdbc.SQLServerDataSource;
import com.microsoft.sqlserver.jdbc.SQLServerException;

/**
 * Instrumented datasource for MS SQL Server
 */
public class WrappedSQLServerDataSource extends SQLServerDataSource {

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
