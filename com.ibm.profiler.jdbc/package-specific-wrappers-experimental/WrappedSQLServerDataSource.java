/* 
 * Copyright 2014 IBM. All rights reserved.
 */
package com.microsoft.sqlserver.jdbc;

/**
 * WrappedSQLServerDataSource : Used to bypass the WebSphere Liberty
 * DataStoreHelper functions which rely on package names to find the right
 * helper.
 * 
 * @author Steve McDuff
 */
public class WrappedSQLServerDataSource extends com.ibm.issw.jdbc.wrappers.WrappedSQLServerDataSource
{
    public WrappedSQLServerDataSource()
    {
        super();
    }
}
