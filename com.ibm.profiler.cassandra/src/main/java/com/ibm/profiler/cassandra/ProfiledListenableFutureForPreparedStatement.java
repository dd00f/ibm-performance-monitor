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
package com.ibm.profiler.cassandra;


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
