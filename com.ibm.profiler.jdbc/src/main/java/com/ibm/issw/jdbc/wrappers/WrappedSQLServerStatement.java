/* 
 * Copyright 2014 IBM. All rights reserved.
 */
package com.ibm.issw.jdbc.wrappers;

import java.sql.Connection;
import java.sql.ResultSet;

import com.ibm.issw.jdbc.profiler.JdbcEvent;
import com.ibm.service.detailed.JdbcLogger;
import com.microsoft.sqlserver.jdbc.ISQLServerResultSet;
import com.microsoft.sqlserver.jdbc.ISQLServerStatement;
import com.microsoft.sqlserver.jdbc.SQLServerException;

/**
 * WrappedSQLServerStatement
 * 
 * @author Administrator
 */
public class WrappedSQLServerStatement extends WrappedStatement implements ISQLServerStatement
{
    private ISQLServerStatement stmt;

    public WrappedSQLServerStatement(ISQLServerStatement statement, String reference, String transaction, Connection connection)
    {
        super(statement, reference, transaction, connection);
        this.stmt = statement;
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
    protected ResultSet wrapResultSet(JdbcEvent jdbcEvent, ResultSet executeQuery, String ref)
    {
        if (executeQuery instanceof ISQLServerResultSet)
        {
            return wrapSqlServerResultSet((ISQLServerResultSet) executeQuery, ref, jdbcEvent, this);
        }

        return super.wrapResultSet(jdbcEvent, executeQuery, ref);
    }

    /**
     * 
     * wrapResultSet
     * @param resultSet the result set
     * @param currentRef the reference string
     * @param jdbcEvent the event
     * @param statement the statement
     * @return the wrapped result set
     */
    public static ResultSet wrapSqlServerResultSet(ISQLServerResultSet resultSet,
            String currentRef, JdbcEvent jdbcEvent, WrappedStatement statement) {
        ResultSet rslt;
        if (JdbcLogger.isResultSetSizeMeasured()) {
            rslt = new WrappedCalculatedSQLServerResultSet(resultSet, currentRef,
                    jdbcEvent);
        } else {
            rslt = new WrappedSQLServerResultSet(resultSet, currentRef, jdbcEvent);
        }
        statement.addPendingResultSet(rslt);
        return rslt;
    }
}
