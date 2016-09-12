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

import com.datastax.driver.core.PreparedStatement;
import com.google.common.util.concurrent.ListenableFuture;

public class ProfiledListenableFutureForPreparedStatement implements ListenableFuture<PreparedStatement> {

    private final ListenableFuture<PreparedStatement> listenableFuture;

    public ProfiledListenableFutureForPreparedStatement( ListenableFuture<PreparedStatement> prepareAsync ) {
        this.listenableFuture = prepareAsync;
    }

    @Override
    public boolean cancel( boolean mayInterruptIfRunning ) {
        return listenableFuture.cancel( mayInterruptIfRunning );
    }

    @Override
    public boolean isCancelled() {
        return listenableFuture.isCancelled();
    }

    @Override
    public boolean isDone() {
        return listenableFuture.isDone();
    }

    @Override
    public PreparedStatement get() throws InterruptedException, ExecutionException {
        PreparedStatement preparedStatement = listenableFuture.get();
        if ( preparedStatement != null ) {
            preparedStatement = new ProfiledPreparedStatement( preparedStatement );
        }
        return preparedStatement;
    }

    @Override
    public PreparedStatement get( long timeout, TimeUnit unit ) throws InterruptedException, ExecutionException, TimeoutException {
        PreparedStatement preparedStatement = listenableFuture.get( timeout, unit );
        if ( preparedStatement != null ) {
            preparedStatement = new ProfiledPreparedStatement( preparedStatement );
        }
        return preparedStatement;
    }

    @Override
    public void addListener( Runnable arg0, Executor arg1 ) {
        listenableFuture.addListener( arg0, arg1 );
    }

}
