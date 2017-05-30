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

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

import com.microsoft.sqlserver.jdbc.ISQLServerCallableStatement;
import com.microsoft.sqlserver.jdbc.ISQLServerConnection;
import com.microsoft.sqlserver.jdbc.ISQLServerPreparedStatement;
import com.microsoft.sqlserver.jdbc.ISQLServerStatement;

/**
 * wrapped SQL Server connection
 */
public class WrappedSQLServerConnection extends WrappedConnection implements ISQLServerConnection
{

    private ISQLServerConnection connection;

    public WrappedSQLServerConnection(ISQLServerConnection connection)
    {
        super(connection);
        this.connection = connection;
    }

    @Override
    public UUID getClientConnectionId() throws SQLException
    {
        return connection.getClientConnectionId();
    }

    @Override
    protected WrappedCallableStatement wrapCallableStatement(String sql, String ref, CallableStatement prepareCall)
    {
        if (prepareCall instanceof ISQLServerCallableStatement)
        {
            return new WrappedSQLServerCallableStatement((ISQLServerCallableStatement) prepareCall, sql, ref, getTransactionIdentifier(), connection);
        }

        return new WrappedCallableStatement(prepareCall, sql, ref, getTransactionIdentifier(), connection);
    }

    @Override
    protected WrappedStatement wrapStatement(Statement s, String ref)
    {
        if (s instanceof ISQLServerStatement)
        {
            return new WrappedSQLServerStatement((ISQLServerStatement) s, ref, getTransactionIdentifier(), this);
        }

        return new WrappedStatement(s, ref, getTransactionIdentifier(), this);
    }

    @Override
    protected WrappedPreparedStatement wrapPreparedStatement(String sql, String ref, PreparedStatement prepareStatement)
    {
        if (prepareStatement instanceof ISQLServerPreparedStatement)
        {
            return new WrappedSQLServerPreparedStatement((ISQLServerPreparedStatement) prepareStatement, sql, ref, getTransactionIdentifier(), this);
        }

        return new WrappedPreparedStatement(prepareStatement, sql, ref, getTransactionIdentifier(), this);
    }

}
