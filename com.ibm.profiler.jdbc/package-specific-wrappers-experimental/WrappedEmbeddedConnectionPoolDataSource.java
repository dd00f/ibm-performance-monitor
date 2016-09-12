/* 
 * Copyright 2014 IBM. All rights reserved.
 */
package org.apache.derby.jdbc;

import com.ibm.issw.jdbc.wrappers.WrappedDerbyDataSource;

/**
 * WrappedEmbeddedConnectionPoolDataSource : Used to bypass the WebSphere
 * Liberty DataStoreHelper functions which rely on package names to find the
 * right helper.
 * 
 * @author Administrator
 */
public class WrappedEmbeddedConnectionPoolDataSource extends WrappedDerbyDataSource
{
    public WrappedEmbeddedConnectionPoolDataSource()
    {
        super();
    }
}
