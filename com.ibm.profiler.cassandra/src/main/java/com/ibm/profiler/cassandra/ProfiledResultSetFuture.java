package com.ibm.profiler.cassandra;

/*
 *-----------------------------------------------------------------
 * IBM Confidential
 *
 * OCO Source Materials
 *
 * WebSphere Commerce
 *
 * (C) Copyright IBM Corp. 2014
 *
 * The source code for this program is not published or otherwise
 * divested of its trade secrets, irrespective of what has
 * been deposited with the U.S. Copyright Office.
 *-----------------------------------------------------------------
 */

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.ResultSetFuture;
import com.ibm.commerce.cache.OperationMetric;

public class ProfiledResultSetFuture implements ResultSetFuture {

    private final ResultSetFuture resultSetFuture;

    private final OperationMetric metric;

    public ProfiledResultSetFuture( ResultSetFuture executeAsync, OperationMetric metric ) {
        this.resultSetFuture = executeAsync;
        this.metric = metric;
    }

    @Override
    public void addListener( Runnable arg0, Executor arg1 ) {
        resultSetFuture.addListener( arg0, arg1 );
    }

    @Override
    public boolean isCancelled() {
        return resultSetFuture.isCancelled();
    }

    @Override
    public boolean isDone() {
        return resultSetFuture.isDone();
    }

    @Override
    public ResultSet get() throws InterruptedException, ExecutionException {
        ResultSet resultSet = null;
        try {
            resultSet = resultSetFuture.get();
        } finally {
            ProfilingUtilities.logMetric( metric, resultSet );
        }
        return resultSet;
    }

    @Override
    public ResultSet get( long timeout, TimeUnit unit ) throws InterruptedException, ExecutionException, TimeoutException {
        ResultSet resultSet = null;
        try {
            resultSet = resultSetFuture.get( timeout, unit );
        } finally {
            ProfilingUtilities.logMetric( metric, resultSet );
        }
        return resultSet;
    }

    @Override
    public boolean cancel( boolean arg0 ) {
        return resultSetFuture.cancel( arg0 );
    }

    @Override
    public ResultSet getUninterruptibly() {
        ResultSet resultSet = null;
        try {
            resultSet = resultSetFuture.getUninterruptibly();
        } finally {
            ProfilingUtilities.logMetric( metric, resultSet );
        }
        return resultSet;
    }

    @Override
    public ResultSet getUninterruptibly( long arg0, TimeUnit arg1 ) throws TimeoutException {
        ResultSet resultSet = null;
        try {
            resultSet = resultSetFuture.getUninterruptibly( arg0, arg1 );
        } finally {
            ProfilingUtilities.logMetric( metric, resultSet );
        }
        return resultSet;
    }

}
