/* 
 * Copyright 2014 IBM. All rights reserved.
 */
package com.ibm.issw.jdbc.wrappers;

import java.sql.SQLException;

import microsoft.sql.DateTimeOffset;

import com.ibm.issw.jdbc.profiler.JdbcEvent;
import com.microsoft.sqlserver.jdbc.ISQLServerResultSet;

/**
 * WrappedSQLServerResultSet
 * 
 * @author Administrator
 */
public class WrappedSQLServerResultSet extends WrappedResultSet implements ISQLServerResultSet
{

    private final ISQLServerResultSet rslt;

    public WrappedSQLServerResultSet(ISQLServerResultSet resultSet, String reference, JdbcEvent event)
    {
        super(resultSet, reference, event);
        this.rslt = resultSet;
    }

    @Override
    public DateTimeOffset getDateTimeOffset(int arg0) throws SQLException
    {
        return rslt.getDateTimeOffset(arg0);
    }

    @Override
    public DateTimeOffset getDateTimeOffset(String arg0) throws SQLException
    {
        return rslt.getDateTimeOffset(arg0);
    }

    @Override
    public void updateDateTimeOffset(int arg0, DateTimeOffset arg1) throws SQLException
    {
        rslt.updateDateTimeOffset(arg0, arg1);
    }

    @Override
    public void updateDateTimeOffset(String arg0, DateTimeOffset arg1) throws SQLException
    {
        rslt.updateDateTimeOffset(arg0, arg1);
    }
}
