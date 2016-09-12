/* 
 * Copyright 2014 IBM. All rights reserved.
 */
package com.ibm.issw.jdbc.wrappers;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import microsoft.sql.DateTimeOffset;

import com.ibm.issw.jdbc.profiler.JdbcEvent;
import com.microsoft.sqlserver.jdbc.ISQLServerPreparedStatement;
import com.microsoft.sqlserver.jdbc.ISQLServerResultSet;
import com.microsoft.sqlserver.jdbc.SQLServerException;

/**
 * WrappedSQLServerPreparedStatement
 * 
 * @author Administrator
 */
public class WrappedSQLServerPreparedStatement extends WrappedPreparedStatement implements ISQLServerPreparedStatement
{
    private final ISQLServerPreparedStatement pstmt;

    /**
     * ctor
     * 
     * @param preparedStatement
     * @param sql
     * @param ref
     */
    public WrappedSQLServerPreparedStatement(ISQLServerPreparedStatement preparedStatement, String sql, String ref, String transaction, Connection connection)
    {
        super(preparedStatement, sql, ref, transaction, connection);
        this.pstmt = preparedStatement;
    }

    @Override
    public String getResponseBuffering() throws SQLServerException
    {
        return pstmt.getResponseBuffering();
    }

    @Override
    public void setResponseBuffering(String arg0) throws SQLServerException
    {
        pstmt.setResponseBuffering(arg0);
    }

    @Override
    public void setDateTimeOffset(int arg0, DateTimeOffset arg1) throws SQLException
    {
        pstmt.setDateTimeOffset(arg0, arg1);
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
