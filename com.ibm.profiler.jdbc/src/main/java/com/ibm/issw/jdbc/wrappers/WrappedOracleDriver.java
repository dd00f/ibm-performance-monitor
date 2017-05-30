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
