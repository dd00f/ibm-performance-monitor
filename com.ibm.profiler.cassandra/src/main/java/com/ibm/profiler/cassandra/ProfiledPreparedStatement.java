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

import java.nio.ByteBuffer;
import java.util.Map;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.CodecRegistry;
import com.datastax.driver.core.ColumnDefinitions;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.PreparedId;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.policies.RetryPolicy;

public class ProfiledPreparedStatement implements PreparedStatement {

    private final PreparedStatement preparedStatement;

    public ProfiledPreparedStatement( PreparedStatement prepare ) {
        this.preparedStatement = prepare;
    }

    @Override
    public BoundStatement bind( Object... values ) {
        ProfiledBoundStatement bs = new ProfiledBoundStatement( this );
        return bs.bind( values );
    }

    @Override
    public BoundStatement bind() {
        return new ProfiledBoundStatement( this );
    }

    @Override
    public PreparedStatement disableTracing() {
        return preparedStatement.disableTracing();
    }

    @Override
    public PreparedStatement enableTracing() {
        return preparedStatement.enableTracing();
    }

    @Override
    public ConsistencyLevel getConsistencyLevel() {
        return preparedStatement.getConsistencyLevel();
    }

    @Override
    public PreparedId getPreparedId() {
        return preparedStatement.getPreparedId();
    }

    @Override
    public String getQueryKeyspace() {
        return preparedStatement.getQueryKeyspace();
    }

    @Override
    public String getQueryString() {
        return preparedStatement.getQueryString();
    }

    @Override
    public RetryPolicy getRetryPolicy() {
        return preparedStatement.getRetryPolicy();
    }

    @Override
    public ByteBuffer getRoutingKey() {
        return preparedStatement.getRoutingKey();
    }

    @Override
    public ConsistencyLevel getSerialConsistencyLevel() {
        return preparedStatement.getSerialConsistencyLevel();
    }

    @Override
    public ColumnDefinitions getVariables() {
        return preparedStatement.getVariables();
    }

    @Override
    public boolean isTracing() {
        return preparedStatement.isTracing();
    }

    @Override
    public PreparedStatement setConsistencyLevel( ConsistencyLevel arg0 ) {
        return preparedStatement.setConsistencyLevel( arg0 );
    }

    @Override
    public PreparedStatement setRetryPolicy( RetryPolicy arg0 ) {
        return preparedStatement.setRetryPolicy( arg0 );
    }

    @Override
    public PreparedStatement setRoutingKey( ByteBuffer arg0 ) {
        return preparedStatement.setRoutingKey( arg0 );
    }

    @Override
    public PreparedStatement setRoutingKey( ByteBuffer... arg0 ) {
        return preparedStatement.setRoutingKey( arg0 );
    }

    @Override
    public PreparedStatement setSerialConsistencyLevel( ConsistencyLevel arg0 ) {
        return preparedStatement.setSerialConsistencyLevel( arg0 );
    }

    /**
     * 
     * @return The bound prepared statement.
     */
    public PreparedStatement getPreparedStatement() {
        return preparedStatement;
    }

    @Override
    public CodecRegistry getCodecRegistry() {
        return preparedStatement.getCodecRegistry();
    }

    @Override
    public Map<String, ByteBuffer> getIncomingPayload() {
        return preparedStatement.getIncomingPayload();
    }

    @Override
    public Map<String, ByteBuffer> getOutgoingPayload() {
        return preparedStatement.getOutgoingPayload();
    }

    @Override
    public Boolean isIdempotent() {
        return preparedStatement.isIdempotent();
    }

    @Override
    public PreparedStatement setIdempotent( Boolean arg0 ) {
        return preparedStatement.setIdempotent( arg0 );
    }

    @Override
    public PreparedStatement setOutgoingPayload( Map<String, ByteBuffer> arg0 ) {
        return setOutgoingPayload( arg0 );
    }

}