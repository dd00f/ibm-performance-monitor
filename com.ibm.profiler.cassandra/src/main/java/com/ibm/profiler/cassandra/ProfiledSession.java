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

import java.util.Map;

import com.datastax.driver.core.CloseFuture;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.RegularStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.ResultSetFuture;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.Statement;
import com.google.common.util.concurrent.ListenableFuture;
import com.ibm.commerce.cache.OperationMetric;

public class ProfiledSession implements Session {

    private Session session;

    public ProfiledSession( Session session ) {
        this.session = session;
    }

    @Override
    public void close() {
        session.close();
    }

    @Override
    public CloseFuture closeAsync() {
        return session.closeAsync();
    }

    @Override
    public ResultSet execute( String arg0 ) {
        OperationMetric metric = ProfilingUtilities.initializeMetric( arg0 );
        ResultSet execute = session.execute( arg0 );
        ProfilingUtilities.logMetric( metric, execute );
        return execute;
    }

    @Override
    public ResultSet execute( Statement arg0 ) {
        String statementName = ProfilingUtilities.getStatementName( arg0 );
        String[] statementArguments = ProfilingUtilities.getStatementArguments( arg0 );
        OperationMetric metric = ProfilingUtilities.initializeMetric( statementName, statementArguments );
        ResultSet execute = session.execute( arg0 );
        ProfilingUtilities.logMetric( metric, execute );
        return execute;
    }

    @Override
    public ResultSet execute( String arg0, Object... arg1 ) {
        String[] convertArgumentArrayToNumericArgumentArray = ProfilingUtilities.convertArgumentArrayToNumericArgumentArray( arg0 );
        OperationMetric metric = ProfilingUtilities.initializeMetric( arg0, convertArgumentArrayToNumericArgumentArray );
        ResultSet execute = session.execute( arg0, arg1 );
        ProfilingUtilities.logMetric( metric, execute );
        return execute;
    }

    @Override
    public ResultSetFuture executeAsync( String arg0 ) {
        OperationMetric metric = ProfilingUtilities.initializeMetric( arg0 );
        ResultSetFuture executeAsync = session.executeAsync( arg0 );
        return new ProfiledResultSetFuture( executeAsync, metric );
    }

    @Override
    public ResultSetFuture executeAsync( Statement arg0 ) {
        String statementName = ProfilingUtilities.getStatementName( arg0 );
        String[] statementArguments = ProfilingUtilities.getStatementArguments( arg0 );
        OperationMetric metric = ProfilingUtilities.initializeMetric( statementName, statementArguments );
        ResultSetFuture executeAsync = session.executeAsync( arg0 );
        return new ProfiledResultSetFuture( executeAsync, metric );
    }

    @Override
    public ResultSetFuture executeAsync( String arg0, Object... arg1 ) {
        String[] convertArgumentArrayToNumericArgumentArray = ProfilingUtilities.convertArgumentArrayToNumericArgumentArray( arg0 );
        OperationMetric metric = ProfilingUtilities.initializeMetric( arg0, convertArgumentArrayToNumericArgumentArray );
        ResultSetFuture executeAsync = session.executeAsync( arg0, arg1 );
        return new ProfiledResultSetFuture( executeAsync, metric );
    }

    @Override
    public Cluster getCluster() {
        return session.getCluster();
    }

    @Override
    public String getLoggedKeyspace() {
        return session.getLoggedKeyspace();
    }

    @Override
    public State getState() {
        return session.getState();
    }

    @Override
    public Session init() {
        return session.init();
    }

    @Override
    public boolean isClosed() {
        return session.isClosed();
    }

    @Override
    public PreparedStatement prepare( String arg0 ) {
        PreparedStatement prepare = session.prepare( arg0 );
        return new ProfiledPreparedStatement( prepare );
    }

    @Override
    public PreparedStatement prepare( RegularStatement arg0 ) {
        PreparedStatement prepare = session.prepare( arg0 );
        return new ProfiledPreparedStatement( prepare );
    }

    @Override
    public ListenableFuture<PreparedStatement> prepareAsync( String arg0 ) {
        ListenableFuture<PreparedStatement> prepareAsync = session.prepareAsync( arg0 );
        return new ProfiledListenableFutureForPreparedStatement( prepareAsync );
    }

    @Override
    public ListenableFuture<PreparedStatement> prepareAsync( RegularStatement arg0 ) {
        ListenableFuture<PreparedStatement> prepareAsync = session.prepareAsync( arg0 );
        return new ProfiledListenableFutureForPreparedStatement( prepareAsync );
    }

    @Override
    public ListenableFuture<Session> initAsync() {
        return session.initAsync();
    }

    @Override
    public ResultSet execute(String arg0, Map<String, Object> arg1)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public ResultSetFuture executeAsync(String arg0, Map<String, Object> arg1)
    {
        String[] convertArgumentArrayToNumericArgumentArray = ProfilingUtilities.convertArgumentMapToArray(arg1);
        OperationMetric metric = ProfilingUtilities.initializeMetric( arg0, convertArgumentArrayToNumericArgumentArray );
        ResultSetFuture executeAsync = session.executeAsync( arg0, arg1 );
        return new ProfiledResultSetFuture( executeAsync, metric );
    }

}
