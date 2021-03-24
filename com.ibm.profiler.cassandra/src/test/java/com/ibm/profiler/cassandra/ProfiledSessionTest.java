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

import static org.hamcrest.MatcherAssert.assertThat;

import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.RegularStatement;
import com.datastax.driver.core.ResultSetFuture;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.Statement;
import com.google.common.util.concurrent.ListenableFuture;

public class ProfiledSessionTest {

    private ProfiledSession session;

    private Session mock;

    @Before
    public void setUp() {
        mock = Mockito.mock( Session.class );
        session = new ProfiledSession( mock );
    }

    @Test
    public void testClose() {
        session.close();
        Mockito.verify( mock ).close();
    }

    @Test
    public void testCloseAsync() {
        session.closeAsync();
        Mockito.verify( mock ).closeAsync();
    }

    @Test
    public void testExecuteString() {
        session.execute( "test" );
        Mockito.verify( mock ).execute( "test" );
    }

    @Test
    public void testExecuteStatement() {
        Statement statement = Mockito.mock( Statement.class );
        session.execute( statement );
        Mockito.verify( mock ).execute( statement );
    }

    @Test
    public void testExecuteStringObjectArray() {
        Object[] params = new Object[] { "1", "2" };
        session.execute( "test", params );
        Mockito.verify( mock ).execute( "test", params );
    }

    @Test
    public void testExecuteAsyncString() {
        ResultSetFuture executeAsync = session.executeAsync( "test" );
        assertThat( executeAsync, CoreMatchers.instanceOf(ProfiledResultSetFuture.class ) );
        Mockito.verify( mock ).executeAsync( "test" );
    }

    @Test
    public void testExecuteAsyncStatement() {
        Statement statement = Mockito.mock( Statement.class );
        ResultSetFuture executeAsync = session.executeAsync( statement );
        assertThat( executeAsync, CoreMatchers.instanceOf( ProfiledResultSetFuture.class ) );
        Mockito.verify( mock ).executeAsync( statement );
    }

    @Test
    public void testExecuteAsyncStringObjectArray() {
        Object[] params = new Object[] { "1", "2" };
        ResultSetFuture executeAsync = session.executeAsync( "test", params );
        Mockito.verify( mock ).executeAsync( "test", params );
        assertThat( executeAsync, CoreMatchers.instanceOf( ProfiledResultSetFuture.class ) );
    }

    @Test
    public void testGetCluster() {
        session.getCluster();
        Mockito.verify( mock ).getCluster();
    }

    @Test
    public void testGetLoggedKeyspace() {
        session.getLoggedKeyspace();
        Mockito.verify( mock ).getLoggedKeyspace();
    }

    @Test
    public void testGetState() {
        session.getState();
        Mockito.verify( mock ).getState();
    }

    @Test
    public void testInit() {
        session.init();
        Mockito.verify( mock ).init();
    }

    @Test
    public void testIsClosed() {
        session.isClosed();
        Mockito.verify( mock ).isClosed();
    }

    @Test
    public void testPrepareString() {
        PreparedStatement prepare = session.prepare( "test" );
        Mockito.verify( mock ).prepare( "test" );
        assertThat( prepare, CoreMatchers.instanceOf( ProfiledPreparedStatement.class ) );
    }

    @Test
    public void testPrepareRegularStatement() {
        RegularStatement statement = Mockito.mock( RegularStatement.class );
        PreparedStatement prepare = session.prepare( statement );
        Mockito.verify( mock ).prepare( statement );
        assertThat( prepare, CoreMatchers.instanceOf( ProfiledPreparedStatement.class ) );
    }

    @Test
    public void testPrepareAsyncString() {
        ListenableFuture<PreparedStatement> prepare = session.prepareAsync( "test" );
        Mockito.verify( mock ).prepareAsync( "test" );
        assertThat( prepare, CoreMatchers.instanceOf( ProfiledListenableFutureForPreparedStatement.class ) );
    }

    @Test
    public void testPrepareAsyncRegularStatement() {
        RegularStatement statement = Mockito.mock( RegularStatement.class );
        ListenableFuture<PreparedStatement> prepare = session.prepareAsync( statement );
        Mockito.verify( mock ).prepareAsync( statement );
        assertThat( prepare, CoreMatchers.instanceOf( ProfiledListenableFutureForPreparedStatement.class ) );
    }

}
