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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.PreparedStatement;

/**
 * Class used to intercept the parameters used in cassandra bound statements for
 * logging purpose.
 */
public class ProfiledBoundStatement extends BoundStatement {

    private Map<String, Object> arguments = new HashMap<String, Object>();

    public ProfiledBoundStatement( PreparedStatement statement ) {
        super( statement );
    }

    @Override
    public BoundStatement bind( Object... arg0 ) {
        Map<String, Object> mapToPopulate = arguments;
        ProfilingUtilities.convertArgumentArrayToNumericArgumentMap( mapToPopulate, arg0 );
        return super.bind( arg0 );
    }

    @Override
    public BoundStatement setBool( int i, boolean v ) {
        arguments.put( ProfilingUtilities.getIntegerString( i ), v );
        return super.setBool( i, v );
    }

    @Override
    public BoundStatement setBool( String arg0, boolean arg1 ) {
        arguments.put( arg0, arg1 );
        return super.setBool( arg0, arg1 );
    }

    @Override
    public BoundStatement setBytes( int i, ByteBuffer v ) {
        arguments.put( ProfilingUtilities.getIntegerString( i ), v );
        return super.setBytes( i, v );
    }

    @Override
    public BoundStatement setBytes( String arg0, ByteBuffer arg1 ) {
        arguments.put( arg0, arg1 );
        return super.setBytes( arg0, arg1 );
    }

    @Override
    public BoundStatement setBytesUnsafe( int i, ByteBuffer v ) {
        arguments.put( ProfilingUtilities.getIntegerString( i ), v );
        return super.setBytesUnsafe( i, v );
    }

    @Override
    public BoundStatement setBytesUnsafe( String arg0, ByteBuffer arg1 ) {
        arguments.put( arg0, arg1 );
        return super.setBytesUnsafe( arg0, arg1 );
    }

    @Override
    public BoundStatement setDate( int i, Date v ) {
        arguments.put( ProfilingUtilities.getIntegerString( i ), v );
        return super.setDate( i, v );
    }

    @Override
    public BoundStatement setDate( String arg0, Date arg1 ) {
        arguments.put( arg0, arg1 );
        return super.setDate( arg0, arg1 );
    }

    @Override
    public BoundStatement setDecimal( int i, BigDecimal v ) {
        arguments.put( ProfilingUtilities.getIntegerString( i ), v );
        return super.setDecimal( i, v );
    }

    @Override
    public BoundStatement setDecimal( String arg0, BigDecimal arg1 ) {
        arguments.put( arg0, arg1 );
        return super.setDecimal( arg0, arg1 );
    }

    @Override
    public BoundStatement setDouble( int i, double v ) {
        arguments.put( ProfilingUtilities.getIntegerString( i ), v );
        return super.setDouble( i, v );
    }

    @Override
    public BoundStatement setDouble( String arg0, double arg1 ) {
        arguments.put( arg0, arg1 );
        return super.setDouble( arg0, arg1 );
    }

    @Override
    public BoundStatement setFloat( int i, float v ) {
        arguments.put( ProfilingUtilities.getIntegerString( i ), v );
        return super.setFloat( i, v );
    }

    @Override
    public BoundStatement setFloat( String arg0, float arg1 ) {
        arguments.put( arg0, arg1 );
        return super.setFloat( arg0, arg1 );
    }

    @Override
    public BoundStatement setInet( int i, InetAddress v ) {
        arguments.put( ProfilingUtilities.getIntegerString( i ), v );
        return super.setInet( i, v );
    }

    @Override
    public BoundStatement setInet( String arg0, InetAddress arg1 ) {
        arguments.put( arg0, arg1 );
        return super.setInet( arg0, arg1 );
    }

    @Override
    public BoundStatement setInt( int i, int v ) {
        arguments.put( ProfilingUtilities.getIntegerString( i ), v );
        return super.setInt( i, v );
    }

    @Override
    public BoundStatement setInt( String arg0, int arg1 ) {
        arguments.put( arg0, arg1 );
        return super.setInt( arg0, arg1 );
    }

    @Override
    public <T> BoundStatement setList( int arg0, List<T> arg1 ) {
        arguments.put( ProfilingUtilities.getIntegerString( arg0 ), arg1 );
        return super.setList( arg0, arg1 );
    }

    @Override
    public <T> BoundStatement setList( String arg0, List<T> arg1 ) {
        arguments.put( arg0, arg1 );
        return super.setList( arg0, arg1 );
    }

    @Override
    public BoundStatement setLong( int i, long v ) {
        arguments.put( ProfilingUtilities.getIntegerString( i ), v );
        return super.setLong( i, v );
    }

    @Override
    public BoundStatement setLong( String arg0, long arg1 ) {
        arguments.put( arg0, arg1 );
        return super.setLong( arg0, arg1 );
    }

    @Override
    public <K, V> BoundStatement setMap( int arg0, Map<K, V> arg1 ) {
        arguments.put( ProfilingUtilities.getIntegerString( arg0 ), arg1 );
        return super.setMap( arg0, arg1 );
    }

    @Override
    public <K, V> BoundStatement setMap( String arg0, Map<K, V> arg1 ) {
        arguments.put( arg0, arg1 );
        return super.setMap( arg0, arg1 );
    }

    @Override
    public <T> BoundStatement setSet( int arg0, Set<T> arg1 ) {
        arguments.put( ProfilingUtilities.getIntegerString( arg0 ), arg1 );
        return super.setSet( arg0, arg1 );
    }

    @Override
    public <T> BoundStatement setSet( String arg0, Set<T> arg1 ) {
        arguments.put( arg0, arg1 );
        return super.setSet( arg0, arg1 );
    }

    @Override
    public BoundStatement setString( int i, String v ) {
        arguments.put( ProfilingUtilities.getIntegerString( i ), v );
        return super.setString( i, v );
    }

    @Override
    public BoundStatement setString( String arg0, String arg1 ) {
        arguments.put( arg0, arg1 );
        return super.setString( arg0, arg1 );
    }

    @Override
    public BoundStatement setUUID( int i, UUID v ) {
        arguments.put( ProfilingUtilities.getIntegerString( i ), v );
        return super.setUUID( i, v );
    }

    @Override
    public BoundStatement setUUID( String arg0, UUID arg1 ) {
        arguments.put( arg0, arg1 );
        return super.setUUID( arg0, arg1 );
    }

    @Override
    public BoundStatement setVarint( int i, BigInteger v ) {
        arguments.put( ProfilingUtilities.getIntegerString( i ), v );
        return super.setVarint( i, v );
    }

    @Override
    public BoundStatement setVarint( String arg0, BigInteger arg1 ) {
        arguments.put( arg0, arg1 );
        return super.setVarint( arg0, arg1 );
    }

    public String[] getArgumentList() {
        Map<String, Object> argumentMap = arguments;

        return ProfilingUtilities.convertArgumentMapToArray( argumentMap );
    }

}
