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

import static org.junit.Assert.assertTrue;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.datastax.driver.core.ResultSetFuture;
import com.ibm.commerce.cache.OperationMetric;

public class ProfiledResultSetFutureTest {

    private ProfiledResultSetFuture future;

    private ResultSetFuture mock;

    private OperationMetric metric;

    @Before
    public void setUp() {
        metric = new OperationMetric();
        mock = Mockito.mock( ResultSetFuture.class );
        future = new ProfiledResultSetFuture( mock, metric );
    }

    @Test
    public void testAddListener() {
        Runnable runnable = Mockito.mock( Runnable.class );
        Executor executor = Mockito.mock( Executor.class );
        future.addListener( runnable, executor );
        Mockito.verify( mock ).addListener( runnable, executor );
    }

    @Test
    public void testIsCancelled() {
        future.isCancelled();
        Mockito.verify( mock ).isCancelled();
    }

    @Test
    public void testIsDone() {
        future.isDone();
        Mockito.verify( mock ).isDone();
    }

    @Test
    public void testGet() throws InterruptedException, ExecutionException {
        future.get();
        Mockito.verify( mock ).get();
        assertTrue( "metric should be stopped", metric.getStopTime() != 0 );
    }

    @Test
    public void testGetLongTimeUnit() throws InterruptedException, ExecutionException, TimeoutException {
        future.get( 123, TimeUnit.MILLISECONDS );
        Mockito.verify( mock ).get( 123, TimeUnit.MILLISECONDS );
        assertTrue( "metric should be stopped", metric.getStopTime() != 0 );
    }

    @Test
    public void testCancel() {
        future.cancel( true );
        Mockito.verify( mock ).cancel( true );
    }

    @Test
    public void testGetUninterruptibly() {
        future.getUninterruptibly();
        Mockito.verify( mock ).getUninterruptibly();
        assertTrue( "metric should be stopped", metric.getStopTime() != 0 );
    }

    @Test
    public void testGetUninterruptiblyLongTimeUnit() throws TimeoutException {
        future.getUninterruptibly( 123, TimeUnit.MILLISECONDS );
        Mockito.verify( mock ).getUninterruptibly( 123, TimeUnit.MILLISECONDS );
        assertTrue( "metric should be stopped", metric.getStopTime() != 0 );
    }

}
