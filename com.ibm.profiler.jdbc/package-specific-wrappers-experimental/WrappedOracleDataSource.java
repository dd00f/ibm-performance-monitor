/* 
 * Copyright 2014 IBM. All rights reserved.
 */
package oracle.jdbc.pool;

import java.sql.SQLException;

/**
 * WrappedOracleDataSource : Used to bypass the WebSphere Liberty
 * DataStoreHelper functions which rely on package names to find the right
 * helper.
 * 
 * @author Steve MCDuff
 */
public class WrappedOracleDataSource extends com.ibm.issw.jdbc.wrappers.WrappedOracleDataSource
{
    public WrappedOracleDataSource() throws SQLException
    {
        super();
    }
}
