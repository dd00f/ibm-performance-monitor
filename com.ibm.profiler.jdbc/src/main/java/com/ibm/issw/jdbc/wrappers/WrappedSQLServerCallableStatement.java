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
     * @param statement
     * @param sql
     * @param ref
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
            return WrappedSQLServerStatement.wrapSqlServerResultSet((ISQLServerResultSet) executeQuery, ref, jdbcEvent);
        }

        return super.wrapResultSet(jdbcEvent, executeQuery, ref);
    }
}
