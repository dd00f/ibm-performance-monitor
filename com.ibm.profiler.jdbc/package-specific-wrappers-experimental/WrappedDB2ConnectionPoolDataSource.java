/* 
 * Copyright 2014 IBM. All rights reserved.
 */
package com.ibm.db2.jcc;

import com.ibm.issw.jdbc.wrappers.WrappedDB2JCCDataSource;

/**
 * WrappedDB2ConnectionPoolDataSource : Used to bypass the WebSphere Liberty
 * DataStoreHelper functions which rely on package names to find the right
 * helper.
 * 
 * @author Steve McDuff
 */
public class WrappedDB2ConnectionPoolDataSource extends WrappedDB2JCCDataSource
{
    public WrappedDB2ConnectionPoolDataSource()
    {
        super();
    }
}
