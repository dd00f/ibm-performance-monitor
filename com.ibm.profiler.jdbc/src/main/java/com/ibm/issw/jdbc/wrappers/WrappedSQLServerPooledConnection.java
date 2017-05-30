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
	 * @param conn the connection
	 * @return the wrapped connection
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
