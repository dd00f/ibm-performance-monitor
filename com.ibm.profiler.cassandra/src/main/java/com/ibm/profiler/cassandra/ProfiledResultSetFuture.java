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
