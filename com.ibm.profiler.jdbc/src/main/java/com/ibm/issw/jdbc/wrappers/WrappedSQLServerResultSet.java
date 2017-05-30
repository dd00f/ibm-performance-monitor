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
