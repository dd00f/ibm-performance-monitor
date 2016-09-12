package com.ibm.profiler.cassandra;

import static org.junit.Assert.assertArrayEquals;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.Date;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ColumnDefinitions;
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
        Date v = new Date();
        statement.setDate( 1, v );
        assertArrayEquals( new String[] { "1", v.toString() }, statement.getArgumentList() );
    }

}
