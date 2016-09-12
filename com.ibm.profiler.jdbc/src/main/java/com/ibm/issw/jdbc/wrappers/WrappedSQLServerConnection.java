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
