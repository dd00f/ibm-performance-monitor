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

import static org.junit.Assert.assertArrayEquals;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ColumnDefinitions;
import com.datastax.driver.core.LocalDate;
import com.datastax.driver.core.PreparedStatement;

public class ProfiledBoundStatementTest {
    private ProfiledBoundStatement statement;

    private PreparedStatement mock;

    @Before
    public void setUp() {
        ColumnDefinitions columns = Mockito.mock( ColumnDefinitions.class );
        Mockito.when( columns.size() ).thenReturn( 3 );
        mock = Mockito.mock( PreparedStatement.class );
        Mockito.when( mock.getVariables() ).thenReturn( columns );
        statement = new ProfiledBoundStatement( mock );
    }

    public ByteBuffer[] getValues() {
        try {
            Field field = BoundStatement.class.getField( "values" );
            field.setAccessible( true );
            Object object = field.get( statement );
            return (ByteBuffer[]) object;
        } catch ( Exception ex ) {
            throw new RuntimeException( ex );
        }
    }

    @Test
    @Ignore
    public void testSetBoolIntBoolean() {
        statement.setBool( 2, true );
        assertArrayEquals( new String[] { "2", "true" }, statement.getArgumentList() );
    }

    @Test
    @Ignore
    public void testSetDateIntDate() {
        LocalDate v = LocalDate.fromMillisSinceEpoch(System.currentTimeMillis());
        statement.setDate( 1, v );
        assertArrayEquals( new String[] { "1", v.toString() }, statement.getArgumentList() );
    }

}
