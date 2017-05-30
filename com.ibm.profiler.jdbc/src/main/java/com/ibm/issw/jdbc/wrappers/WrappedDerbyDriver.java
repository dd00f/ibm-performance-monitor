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
/* 
 * Copyright 2014 IBM. All rights reserved.
 */
package com.ibm.issw.jdbc.wrappers;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import org.apache.derby.jdbc.EmbeddedDriver;

import com.ibm.commerce.cache.OperationMetric;
import com.ibm.issw.jdbc.profiler.JdbcProfilerDaemon;

/**
 * WrappedDerbyDriver
 * 
 * @author Steve McDuff
 */
public class WrappedDerbyDriver extends EmbeddedDriver {

    static {
        JdbcProfilerDaemon.initializeDaemon();
    }
    
    public WrappedDerbyDriver() {
        
    }
    
    @Override
    public Connection connect(String arg0, Properties arg1) throws SQLException {
        Connection connect = superGetConnection(arg0, arg1);
        if( connect == null ) {
            return null;
        }
        if (connect instanceof WrappedConnection) {
            return connect;
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
