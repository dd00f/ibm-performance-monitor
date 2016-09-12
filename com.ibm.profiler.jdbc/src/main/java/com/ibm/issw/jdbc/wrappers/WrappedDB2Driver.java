package com.ibm.issw.jdbc.wrappers;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import com.ibm.commerce.cache.OperationMetric;
import com.ibm.db2.jcc.DB2Connection;
import com.ibm.db2.jcc.DB2Driver;
import com.ibm.issw.jdbc.profiler.JdbcProfilerDaemon;

public class WrappedDB2Driver extends DB2Driver {

	static {
		JdbcProfilerDaemon.initializeDaemon();
	}
	
	public WrappedDB2Driver() {
		
	}
	
	@SuppressWarnings("resource")
	@Override
	public Connection connect(String arg0, Properties arg1) throws SQLException {
		Connection connect = superGetConnection(arg0, arg1);
		if( connect == null ) {
            return null;
        }
		if (connect instanceof WrappedConnection) {
			return connect;
		}
		if (connect instanceof DB2Connection) {
			new WrappedDB2Connection((DB2Connection) connect);
		}
		return new WrappedConnection(connect);
	}

    private Connection superGetConnection(String arg0, Properties arg1) throws SQLException
    {
        boolean success = false;
        Connection returnedValue = null;
        OperationMetric metric = null;
        metric = WrappedConnection.initializeDriverConnectMetric(metric);
        try
        {
            returnedValue = super.connect(arg0, arg1);
            success = true;
        }
        finally
        {
            WrappedConnection.endDriverConnectMetric(success, metric);
        }
        return returnedValue;
    }


}
