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

import java.nio.ByteBuffer;

import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ColumnDefinitions;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.PreparedId;
import com.datastax.driver.core.PreparedStatement;

public class ProfiledPreparedStatementTest {

    private ProfiledPreparedStatement statement;

    private PreparedStatement mock;

    @Before
    public void setUp() {
        mock = Mockito.mock( PreparedStatement.class );
        statement = new ProfiledPreparedStatement( mock );

        ColumnDefinitions columns = Mockito.mock( ColumnDefinitions.class );
        Mockito.when( columns.size() ).thenReturn( 3 );
        Mockito.when( mock.getVariables() ).thenReturn( columns );
        PreparedId mockID = Mockito.mock( PreparedId.class );
        Mockito.when( mock.getPreparedId() ).thenReturn( mockID );
    }

    @Test
    public void testBindObjectArray() {
        BoundStatement bind = statement.bind( new Object[] {} );
        assertThat( bind, CoreMatchers.instanceOf( ProfiledBoundStatement.class ) );
    }

    @Test
    public void testBind() {
        BoundStatement bind = statement.bind();
        assertThat( bind, CoreMatchers.instanceOf( ProfiledBoundStatement.class ) );
    }

    @Test
    public void testDisableTracing() {
        statement.disableTracing();
        Mockito.verify( mock ).disableTracing();
    }

    @Test
    public void testEnableTracing() {
        statement.enableTracing();
        Mockito.verify( mock ).enableTracing();
    }

    @Test
    public void testGetConsistencyLevel() {
        statement.getConsistencyLevel();
        Mockito.verify( mock ).getConsistencyLevel();
    }

    @Test
    public void testGetPreparedId() {
        statement.getPreparedId();
        Mockito.verify( mock ).getPreparedId();
    }

    @Test
    public void testGetQueryKeyspace() {
        statement.getQueryKeyspace();
        Mockito.verify( mock ).getQueryKeyspace();
    }

    @Test
    public void testGetQueryString() {
        statement.getQueryString();
        Mockito.verify( mock ).getQueryString();
    }

    @Test
    public void testGetRetryPolicy() {
        statement.getRetryPolicy();
        Mockito.verify( mock ).getRetryPolicy();
    }

    @Test
    public void testGetRoutingKey() {
        statement.getRoutingKey();
        Mockito.verify( mock ).getRoutingKey();
    }

    @Test
    public void testGetSerialConsistencyLevel() {
        statement.getSerialConsistencyLevel();
        Mockito.verify( mock ).getSerialConsistencyLevel();
    }

    @Test
    public void testGetVariables() {
        statement.getVariables();
        Mockito.verify( mock ).getVariables();
    }

    @Test
    public void testIsTracing() {
        statement.isTracing();
        Mockito.verify( mock ).isTracing();
    }

    @Test
    public void testSetConsistencyLevel() {
        statement.setConsistencyLevel( null );
        Mockito.verify( mock ).setConsistencyLevel( null );
    }

    @Test
    public void testSetRetryPolicy() {
        statement.setRetryPolicy( null );
        Mockito.verify( mock ).setRetryPolicy( null );
    }

    @Test
    public void testSetRoutingKeyByteBuffer() {
        ByteBuffer buf = Mockito.mock( ByteBuffer.class );
        statement.setRoutingKey( buf );
        Mockito.verify( mock ).setRoutingKey( buf );
    }

    @Test
    public void testSetRoutingKeyByteBufferArray() {
        ByteBuffer buf = Mockito.mock( ByteBuffer.class );
        ByteBuffer buf2 = Mockito.mock( ByteBuffer.class );
        statement.setRoutingKey( buf, buf2 );
        Mockito.verify( mock ).setRoutingKey( buf, buf2 );
    }

    @Test
    public void testSetSerialConsistencyLevel() {
        statement.setSerialConsistencyLevel( ConsistencyLevel.EACH_QUORUM );
        Mockito.verify( mock ).setSerialConsistencyLevel( ConsistencyLevel.EACH_QUORUM );
    }

}
