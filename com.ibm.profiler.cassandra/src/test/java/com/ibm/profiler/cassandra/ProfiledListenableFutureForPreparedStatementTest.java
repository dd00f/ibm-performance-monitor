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

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.datastax.driver.core.PreparedStatement;
import com.google.common.util.concurrent.ListenableFuture;

public class ProfiledListenableFutureForPreparedStatementTest {

    private ProfiledListenableFutureForPreparedStatement statement;

    private ListenableFuture<PreparedStatement> mock;

    @SuppressWarnings( "unchecked" )
    @Before
    public void setUp() {
        mock = Mockito.mock( ListenableFuture.class );
        statement = new ProfiledListenableFutureForPreparedStatement( mock );
    }

    @Test
    public void testCancel() {
        statement.cancel( true );
        Mockito.verify( mock ).cancel( true );
    }

    @Test
    public void testIsCancelled() {
        statement.isCancelled();
        Mockito.verify( mock ).isCancelled();
    }

    @Test
    public void testIsDone() {
        statement.isDone();
        Mockito.verify( mock ).isDone();
    }

    @Test
    public void testGetNullStatement() throws InterruptedException, ExecutionException {

        PreparedStatement preparedStatement = statement.get();
        Mockito.verify( mock ).get();
        assertNull( preparedStatement );

    }

    @Test
    public void testGetNonNullStatement() throws InterruptedException, ExecutionException {
        PreparedStatement mockStatement = Mockito.mock( PreparedStatement.class );
        Mockito.when( mock.get() ).thenReturn( mockStatement );
        PreparedStatement preparedStatement = statement.get();
        Mockito.verify( mock ).get();
        assertThat( preparedStatement, CoreMatchers.instanceOf( ProfiledPreparedStatement.class ) );
        ProfiledPreparedStatement profiledStatement = (ProfiledPreparedStatement) preparedStatement;
        assertSame( profiledStatement.getPreparedStatement(), mockStatement );
    }

    @Test
    public void testGetLongTimeUnitNull() throws InterruptedException, ExecutionException, TimeoutException {
        PreparedStatement preparedStatement = statement.get( 1234l, TimeUnit.DAYS );
        Mockito.verify( mock ).get( 1234l, TimeUnit.DAYS );
        assertNull( preparedStatement );

    }

    @Test
    public void testGetLongTimeUnit() throws InterruptedException, ExecutionException, TimeoutException {
        PreparedStatement mockStatement = Mockito.mock( PreparedStatement.class );
        Mockito.when( mock.get( 555l, TimeUnit.DAYS ) ).thenReturn( mockStatement );
        PreparedStatement preparedStatement = statement.get( 555l, TimeUnit.DAYS );
        Mockito.verify( mock ).get( 555l, TimeUnit.DAYS );
        assertThat( preparedStatement, CoreMatchers.instanceOf( ProfiledPreparedStatement.class ) );
        ProfiledPreparedStatement profiledStatement = (ProfiledPreparedStatement) preparedStatement;
        assertSame( profiledStatement.getPreparedStatement(), mockStatement );
    }

    @Test
    public void testAddListener() {
        statement.addListener( null, null );
        Mockito.verify( mock ).addListener( null, null );
    }

}
