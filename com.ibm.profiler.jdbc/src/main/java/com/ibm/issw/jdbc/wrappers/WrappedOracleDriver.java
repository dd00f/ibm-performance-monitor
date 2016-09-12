package com.ibm.issw.jdbc.wrappers;

/*
 *-----------------------------------------------------------------
 * IBM Confidential
 *
 * OCO Source Materials
 *
 * WebSphere Commerce
 *
 * (C) Copyright IBM Corp. 2013, 2014
 *
 * The source code for this program is not published or otherwise
 * divested of its trade secrets, irrespective of what has
 * been deposited with the U.S. Copyright Office.
 *-----------------------------------------------------------------
 */

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Logger;

import oracle.jdbc.OracleConnection;
import oracle.jdbc.OracleDriver;

import com.ibm.commerce.cache.OperationMetric;
import com.ibm.issw.jdbc.profiler.JdbcProfilerDaemon;

/**
 * 
 * WrappedOracleDriver
 */
public class WrappedOracleDriver extends OracleDriver {
	private static final Logger LOG = Logger
			.getLogger(WrappedOracleDriver.class.getName());
	/**
	 * IBM Copyright notice field.
	 */
	public static final String COPYRIGHT = com.ibm.commerce.copyright.IBMCopyright.SHORT_COPYRIGHT;

	static {
		JdbcProfilerDaemon.initializeDaemon();
	}
	
	public WrappedOracleDriver() {
		
	}

	/*
	 * (non-Javadoc)
	 * @see oracle.jdbc.driver.OracleDriver#connect(java.lang.String, java.util.Properties)
	 */
	@Override
    public final Connection connect(String url, Properties info)
			throws SQLException {
		LOG.info("Allocating connection");
		Connection con = superGetConnection(url, info);
        return wrapOracleConnection(con);
	}

	public static Connection wrapOracleConnection(Connection con) {
		if( con == null ) {
            return null;
        }
		
		if( con instanceof WrappedOracleConnection) {
			return con;
		}
		
		if( con instanceof OracleConnection ) {
			return new WrappedOracleConnection(con);
		}
		
		return new WrappedConnection(con);
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
