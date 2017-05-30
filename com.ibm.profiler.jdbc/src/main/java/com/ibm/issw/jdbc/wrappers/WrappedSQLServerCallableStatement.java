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
/* 
 * Copyright 2014 IBM. All rights reserved.
 */
package com.ibm.issw.jdbc.wrappers;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import microsoft.sql.DateTimeOffset;

import com.ibm.issw.jdbc.profiler.JdbcEvent;
import com.microsoft.sqlserver.jdbc.ISQLServerCallableStatement;
import com.microsoft.sqlserver.jdbc.ISQLServerResultSet;
import com.microsoft.sqlserver.jdbc.SQLServerException;

/**
 * WrappedSQLServerCallableStatement
 * 
 * @author Administrator
 */
public class WrappedSQLServerCallableStatement extends WrappedCallableStatement implements ISQLServerCallableStatement
{

    private ISQLServerCallableStatement stmt;

    /**
     * 
     * @param statement the statement
     * @param sql the sql to run
     * @param ref the reference number
     * @param transaction the transaction
     * @param connection the connection
     */
    public WrappedSQLServerCallableStatement(ISQLServerCallableStatement statement, String sql, String ref, String transaction, Connection connection)
    {
        super(statement, sql, ref, transaction, connection);
        this.stmt = statement;
    }

    @Override
    public void setDateTimeOffset(int arg0, DateTimeOffset arg1) throws SQLException
    {
        stmt.setDateTimeOffset(arg0, arg1);
    }

    @Override
    public String getResponseBuffering() throws SQLServerException
    {
        return stmt.getResponseBuffering();
    }

    @Override
    public void setResponseBuffering(String arg0) throws SQLServerException
    {
        stmt.setResponseBuffering(arg0);
    }

    @Override
    public DateTimeOffset getDateTimeOffset(int arg0) throws SQLException
    {
        return stmt.getDateTimeOffset(arg0);
    }

    @Override
    public DateTimeOffset getDateTimeOffset(String arg0) throws SQLException
    {
        return stmt.getDateTimeOffset(arg0);
    }

    @Override
    public void setDateTimeOffset(String arg0, DateTimeOffset arg1) throws SQLException
    {
        stmt.setDateTimeOffset(arg0, arg1);
    }
    
    @Override
    protected ResultSet wrapResultSet(JdbcEvent jdbcEvent,
    		ResultSet executeQuery, String ref) {
        if (executeQuery instanceof ISQLServerResultSet)
        {
            return WrappedSQLServerStatement.wrapSqlServerResultSet((ISQLServerResultSet) executeQuery, ref, jdbcEvent, this);
        }

        return super.wrapResultSet(jdbcEvent, executeQuery, ref);
    }
}
