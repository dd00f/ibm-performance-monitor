package com.ibm.issw.jdbc.wrappers;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Properties;
import java.util.logging.Logger;

import com.ibm.commerce.cache.OperationMetric;
import com.ibm.issw.jdbc.profiler.JdbcProfilerDaemon;
import com.microsoft.sqlserver.jdbc.ISQLServerConnection;
import com.microsoft.sqlserver.jdbc.SQLServerDriver;

public class WrappedSQLServerDriver implements Driver {
	
	static {
		JdbcProfilerDaemon.initializeDaemon();
	}
	
	private SQLServerDriver driver;
	
	public WrappedSQLServerDriver() {
		driver = new SQLServerDriver();
	}

	@SuppressWarnings("resource")
	@Override
	public Connection connect(String url, Properties info) throws SQLException {
		Connection connect = superGetConnection(url, info);
		if( connect == null ) {
		    return null;
		}
		if (connect instanceof WrappedConnection) {
			return connect;
		}
		if (connect instanceof ISQLServerConnection) {
			new WrappedSQLServerConnection((ISQLServerConnection) connect);
		}
		return new WrappedConnection(connect);
	}

	@Override
	public boolean acceptsURL(String url) throws SQLException {
		return driver.acceptsURL(url);
	}

	@Override
	public DriverPropertyInfo[] getPropertyInfo(String url, Properties info)
			throws SQLException {
		return driver.getPropertyInfo(url, info);
	}

	@Override
	public int getMajorVersion() {
		return driver.getMajorVersion();
	}

	@Override
	public int getMinorVersion() {
		return driver.getMinorVersion();
	}

	@Override
	public boolean jdbcCompliant() {
		return driver.jdbcCompliant();
	}

	@Override
	public Logger getParentLogger() throws SQLFeatureNotSupportedException {
		throw new SQLFeatureNotSupportedException();
	}
	
	private Connection superGetConnection(String arg0, Properties arg1) throws SQLException
    {
        boolean success = false;
        Connection returnedValue = null;
        OperationMetric metric = null;
        metric = WrappedConnection.initializeDriverConnectMetric(metric);
        try
        {
            returnedValue = driver.connect(arg0, arg1);
            success = true;
        }
        finally
        {
            WrappedConnection.endDriverConnectMetric(success, metric);
        }
        return returnedValue;
    }

}
