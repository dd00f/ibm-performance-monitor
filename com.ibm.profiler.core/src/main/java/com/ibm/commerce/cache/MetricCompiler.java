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
package com.ibm.commerce.cache;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sql.DataSource;

/**
 * MetricCompiler offers utility methods to create, clear or drop the Cache
 * Metric measurement framework. It also offers methods to calculate potential
 * cache effectiveness and recommend cache allocation size.
 * 
 */
public class MetricCompiler {


    /**
     * Class name
     */
    private static final String CLASS_NAME = MetricCompiler.class.getName();

    /**
     * Logger
     */
    private static final Logger LOGGER = LoggingHelper.getLogger( CLASS_NAME );

    /**
     * SQL to aggregate cache hits together
     */
    private static final String AGGREGATE_CACHE_HITS_SQL = "INSERT INTO UNIQUECACHEHITS " + "(OPERATIONNAME, KEYVALUE,CACHEHITCOUNT,CALLCOUNT,AVERAGEDURATION,AVERAGEDURATIONMISS, AVERAGEDURATIONHIT, AVERAGERESULTSIZE,REALCACHEHITCOUNT,REALCACHEACTCOUNT)"
        + " SELECT " + "OPERATIONNAME, " + "KEYVALUE," + "count(*)-1 as CACHEHITCOUNT," + "count(*) as CALLCOUNT," + "avg(DURATION) as AVERAGEDURATION,"
        + "sum(CASE WHEN FROMCACHE = '0' THEN DURATION ELSE 0 END) / (0.5* ((count(*) - sum(cast( FROMCACHE as BIGINT ))+1+ABS(-1+(count(*) - sum(cast( FROMCACHE as BIGINT ))))))) as AVERAGEDURATIONMISS,"
        + "sum(CASE WHEN FROMCACHE = '1' THEN DURATION ELSE 0 END) / (0.5* ( (1+sum(cast( FROMCACHE as BIGINT ))) + ABS (-1+sum(cast( FROMCACHE as BIGINT ))) )) as AVERAGEDURATIONHIT," + "avg(RESULTSIZE) as AVERAGERESULTSIZE, "
        + "sum(cast( FROMCACHE as BIGINT )) as REALCACHEHITCOUNT, " + "sum(cast( CACHEENABLED as BIGINT )) as REALCACHEACTCOUNT " + "FROM METRIC " + "GROUP BY OPERATIONNAME,KEYVALUE";

    /**
     * clear the METRIC table SQL
     */
    private static final String CLEAR_METRIC_TABLE_SQL = "delete from METRIC";

    /**
     * drop the METRIC table SQL
     */
    private static final String DROP_METRIC_TABLE_SQL = "drop table METRIC";

    /** operation name maximum length */
    public static final int OPERATION_NAME_MAXIMUM_LENGTH = 255;
    /*
    private static final String CACHEHITSTEPS_COLUMNS = "SECONDSAVEDPERBYTE   BIGINT NOT NULL," + "CACHEALLOCATION   BIGINT NOT NULL," + "AVERAGEDURATION   BIGINT NOT NULL," + "AVERAGEDURATIONHIT   BIGINT NOT NULL,"
        + "AVERAGEDURATIONMISS   BIGINT NOT NULL," + "AVERAGERESULTSIZE BIGINT NOT NULL," + "SUMAVERAGERESULTSIZE BIGINT NOT NULL," + "KEYVALUECOUNT     BIGINT NOT NULL," + "SUMCACHEHITCOUNT  BIGINT NOT NULL," + "SUMREALCACHEHITCOUNT  BIGINT NOT NULL,"
        + "SUMREALCACHEACTCOUNT  BIGINT NOT NULL," + "SUMCALLCOUNT      BIGINT NOT NULL," + "OPERATIONNAME     VARCHAR(" + OPERATION_NAME_MAXIMUM_LENGTH + ")," + "STEP               VARCHAR(64)";

    */
    private static final String CACHEHITSTEPS_COLUMNS = "SECONDSAVEDPERBYTE   DOUBLE NOT NULL," + "CACHEALLOCATION   DOUBLE NOT NULL," + "AVERAGEDURATION   DOUBLE NOT NULL," + "AVERAGEDURATIONHIT   DOUBLE NOT NULL,"
        + "AVERAGEDURATIONMISS   DOUBLE NOT NULL," + "AVERAGERESULTSIZE DOUBLE NOT NULL," + "SUMAVERAGERESULTSIZE DOUBLE NOT NULL," + "KEYVALUECOUNT     DOUBLE NOT NULL," + "SUMCACHEHITCOUNT  DOUBLE NOT NULL," + "SUMREALCACHEHITCOUNT  DOUBLE NOT NULL,"
        + "SUMREALCACHEACTCOUNT  DOUBLE NOT NULL," + "SUMCALLCOUNT      DOUBLE NOT NULL," + "OPERATIONNAME     VARCHAR(" + OPERATION_NAME_MAXIMUM_LENGTH + ") NOT NULL," + "STEP               VARCHAR(64) NOT NULL";// + ", PADDING               VARCHAR(4096)";

    
    /** unique key maximum length */
    public static final int UNIQUE_KEY_MAXIMUM_LENGTH = 1024;

    /**
     * create the METRIC table SQL
     */
    private static final String CREATE_METRIC_TABLE_SQL = "CREATE TABLE METRIC (" + "IDENTIFIER        BIGINT NOT NULL," + "PARENTIDENTIFIER  BIGINT NOT NULL," + "STARTIME          BIGINT NOT NULL," + "STOPTIME          BIGINT NOT NULL,"
        + "DURATION          BIGINT NOT NULL," + "RESULTSIZE        BIGINT NOT NULL," + "FROMCACHE         CHAR(1) NOT NULL," + "CACHEENABLED      CHAR(1) NOT NULL," + "OPERATIONNAME     VARCHAR(" + OPERATION_NAME_MAXIMUM_LENGTH + "),"
        + "KEYVALUE          VARCHAR(" + UNIQUE_KEY_MAXIMUM_LENGTH + ")" + ")";

    /**
     * Clear the UNIQUECACHEHITS table SQL.
     */
    private static final String CLEAR_UNIQUECACHEHITS_TABLE_SQL = "delete from UNIQUECACHEHITS";

    /**
     * Drop the UNIQUECACHEHITS table SQL.
     */
    private static final String DROP_UNIQUECACHEHITS_TABLE_SQL = "drop table UNIQUECACHEHITS";

    /**
     * Create the UNIQUECACHEHITS table SQL.
     */
    private static final String CREATE_UNIQUECACHEHITS_TABLE_SQL = "CREATE TABLE UNIQUECACHEHITS (" + "AVERAGEDURATION   BIGINT NOT NULL," + "AVERAGEDURATIONHIT BIGINT NOT NULL," + "AVERAGEDURATIONMISS BIGINT NOT NULL,"
        + "AVERAGERESULTSIZE BIGINT NOT NULL," + "CACHEHITCOUNT     BIGINT NOT NULL," + "CALLCOUNT         BIGINT NOT NULL," + "REALCACHEHITCOUNT BIGINT NOT NULL," + "REALCACHEACTCOUNT BIGINT NOT NULL," + "OPERATIONNAME     VARCHAR("
        + OPERATION_NAME_MAXIMUM_LENGTH + ") NOT NULL," + "KEYVALUE          VARCHAR(" + UNIQUE_KEY_MAXIMUM_LENGTH + ") NOT NULL, "
        // + "PADDING VARCHAR(4096)," 
        + "CONSTRAINT PK_UNIQUECACHEHITS PRIMARY KEY (OPERATIONNAME,KEYVALUE)" + ")";

    /**
     * Drop the IDX_UNIQUECACHEHITS_CACHEHITCOUNT index SQL.
     */
    private static final String DROP_IDX_UNIQUECACHEHITS_CACHEHITCOUNT_SQL = "DROP INDEX IDX_UNIQUECACHEHITS_CACHEHITCOUNT";

    /**
     * Create the IDX_UNIQUECACHEHITS_CACHEHITCOUNT index SQL.
     */
    private static final String CREATE_IDX_UNIQUECACHEHITS_CACHEHITCOUNT_SQL = "CREATE INDEX IDX_UNIQUECACHEHITS_CACHEHITCOUNT ON UNIQUECACHEHITS ( " + "CACHEHITCOUNT ASC    )";

    /**
     * Drop the IDX_METRIC_IDENTIFIER index SQL.
     */
    public static final String DROP_IDX_METRIC_IDENTIFIER = "DROP INDEX IDX_METRIC_IDENTIFIER";

    /**
     * Create the IDX_METRIC_IDENTIFIER index SQL.
     */
    public static final String CREATE_IDX_METRIC_IDENTIFIER = "CREATE INDEX IDX_METRIC_IDENTIFIER ON METRIC ( " + "IDENTIFIER ASC, OPERATIONNAME ASC )";

    /**
     * Drop the IDX_METRIC_PARENTIDENTIFIER index SQL.
     */
    public static final String DROP_IDX_METRIC_PARENTIDENTIFIER = "DROP INDEX IDX_METRIC_PARENTIDENTIFIER";

    /**
     * Create the IDX_METRIC_IDENTIFIER index SQL.
     */
    public static final String CREATE_IDX_METRIC_PARENTIDENTIFIER = "CREATE INDEX IDX_METRIC_PARENTIDENTIFIER ON METRIC ( " + "PARENTIDENTIFIER ASC, IDENTIFIER ASC )";

    /**
     * Drop the IDX_METRIC_OPERATIONNAME index SQL.
     */
    public static final String DROP_IDX_METRIC_OPERATIONNAME = "DROP INDEX IDX_METRIC_OPERATIONNAME";

    /**
     * Create the IDX_METRIC_IDENTIFIER index SQL.
     */
    public static final String CREATE_IDX_METRIC_OPERATIONNAME = "CREATE INDEX IDX_METRIC_OPERATIONNAME ON METRIC ( " + "OPERATIONNAME ASC)";

    /**
     * Clear the CACHEHITSTEPS table SQL.
     */
    private static final String CLEAR_CACHE_HIT_STEPS_TABLE_SQL = "delete from CACHEHITSTEPS";

    /**
     * Clear the CACHEALLOCATION table SQL.
     */
    private static final String CLEAR_CACHE_ALLOCATION_TABLE_SQL = "delete from CACHEALLOCATION";

    /**
     * Drop the CACHEHITSTEPS table SQL.
     */
    private static final String DROP_CACHE_HIT_STEPS_TABLE_SQL = "drop table CACHEHITSTEPS";

    /**
     * Drop the CACHEALLOCATION table SQL.
     */
    private static final String DROP_CACHE_ALLOCATION_TABLE_SQL = "drop table CACHEALLOCATION";

    /**
     * Create the CACHEHITSTEPS table SQL.
     */
    private static final String CREATE_CACHE_HIT_STEPS_TABLE_SQL = "CREATE TABLE CACHEHITSTEPS (" + CACHEHITSTEPS_COLUMNS + ",CONSTRAINT PK_CACHEHITSTEPS PRIMARY KEY (OPERATIONNAME,STEP)" + ")";

    /**
     * Create the CACHEHITSTEPS table SQL.
     */
    private static final String CREATE_CACHE_ALLOCATION_TABLE_SQL = "CREATE TABLE CACHEALLOCATION (" + CACHEHITSTEPS_COLUMNS + ",CONSTRAINT PK_CACHEALLOCATION PRIMARY KEY (OPERATIONNAME,STEP)" + ")";

    /**
     * SQL update used to aggregate the cache hits together based on how
     * frequently they are reused. This SQL also calculates the average number
     * of seconds saved for every potential byte of cache allocation.
     */
    private static final String AGGREGATE_CACHE_HIT_STEPS_SQL_FRAGMENT = "(AVERAGEDURATION,AVERAGEDURATIONHIT,AVERAGEDURATIONMISS,SUMAVERAGERESULTSIZE,AVERAGERESULTSIZE,KEYVALUECOUNT,SUMCACHEHITCOUNT,SUMCALLCOUNT,OPERATIONNAME,STEP,SECONDSAVEDPERBYTE,CACHEALLOCATION,SUMREALCACHEHITCOUNT,SUMREALCACHEACTCOUNT)"
        + " SELECT "
        + "sum(cast(AVERAGEDURATION*CALLCOUNT as double))/(0.5*((1+sum(CALLCOUNT))+ABS(-1+sum(CALLCOUNT)))) as STEPAVERAGEDURATION,"
        + "sum(cast(AVERAGEDURATIONHIT*REALCACHEHITCOUNT as double))/(0.5*((1+sum(REALCACHEHITCOUNT))+ABS(-1+sum(REALCACHEHITCOUNT)))) as STEPAVERAGEDURATIONHIT,"
        + "sum(cast(AVERAGEDURATIONMISS*(CALLCOUNT-REALCACHEHITCOUNT) as double))/(0.5*((1+sum(CALLCOUNT-REALCACHEHITCOUNT))+ABS(-1+sum(CALLCOUNT-REALCACHEHITCOUNT)))) as STEPAVERAGEDURATIONMISS,"
        + "sum(cast(AVERAGERESULTSIZE as double)) as SUMAVERAGERESULTSIZE,"
        + "sum(cast(AVERAGERESULTSIZE*CALLCOUNT as double))/(0.5*((1+sum(CALLCOUNT))+ABS(-1+sum(CALLCOUNT)))) as AVERAGERESULTSIZE,"
        + "count(*) as KEYVALUECOUNT,"
        + "sum(CACHEHITCOUNT) as STEPSUMCACHEHITCOUNT,"
        + "sum(CALLCOUNT) as STEPSUMCALLCOUNT,"
        + "OPERATIONNAME, "
        + "?, " // the step name
        + "((sum(CACHEHITCOUNT) * (sum(cast(AVERAGEDURATIONMISS*(CALLCOUNT-REALCACHEHITCOUNT) as double))/(0.5*((1+sum(CALLCOUNT-REALCACHEHITCOUNT))+ABS(-1+sum(CALLCOUNT-REALCACHEHITCOUNT)))))+0.0)) / (((0.5*((1+sum(cast(AVERAGERESULTSIZE as double)))+ABS(-1+sum(cast(AVERAGERESULTSIZE as double)))))) * 1000000000.0) as SECONDSAVEDPERBYTE, "
        + "0, " // the cache allocation, starting at zero
        + "sum(REALCACHEHITCOUNT) as STEPSUMREALCACHEHITCOUNT," + "sum(REALCACHEACTCOUNT) as STEPSUMREALCACHEACTCOUNT " + "FROM UNIQUECACHEHITS WHERE " + "CACHEHITCOUNT >= ? AND CACHEHITCOUNT <= ? " + "GROUP BY OPERATIONNAME";

    /*
     * 
     *     private static final String AGGREGATE_CACHE_HIT_STEPS_SQL_FRAGMENT = "(AVERAGEDURATION,AVERAGEDURATIONHIT,AVERAGEDURATIONMISS,SUMAVERAGERESULTSIZE,AVERAGERESULTSIZE,KEYVALUECOUNT,SUMCACHEHITCOUNT,SUMCALLCOUNT,OPERATIONNAME,STEP,SECONDSAVEDPERBYTE,CACHEALLOCATION,SUMREALCACHEHITCOUNT,SUMREALCACHEACTCOUNT)"
        + " SELECT "
        + "sum(cast(AVERAGEDURATION*CALLCOUNT as float))/(0.5*((1+sum(CALLCOUNT))+ABS(-1+sum(CALLCOUNT)))) as STEPAVERAGEDURATION,"
        + "sum(AVERAGEDURATIONHIT*REALCACHEHITCOUNT)/(0.5*((1+sum(REALCACHEHITCOUNT))+ABS(-1+sum(REALCACHEHITCOUNT)))) as STEPAVERAGEDURATIONHIT,"
        + "sum(AVERAGEDURATIONMISS*(CALLCOUNT-REALCACHEHITCOUNT))/(0.5*((1+sum(CALLCOUNT-REALCACHEHITCOUNT))+ABS(-1+sum(CALLCOUNT-REALCACHEHITCOUNT)))) as STEPAVERAGEDURATIONMISS,"
        + "sum(AVERAGERESULTSIZE) as SUMAVERAGERESULTSIZE,"
        + "sum(AVERAGERESULTSIZE*CALLCOUNT)/(0.5*((1+sum(CALLCOUNT))+ABS(-1+sum(CALLCOUNT)))) as AVERAGERESULTSIZE,"
        + "count(*) as KEYVALUECOUNT,"
        + "sum(CACHEHITCOUNT) as STEPSUMCACHEHITCOUNT,"
        + "sum(CALLCOUNT) as STEPSUMCALLCOUNT,"
        + "OPERATIONNAME, "
        + "?, " // the step name
        + "((sum(CACHEHITCOUNT) * (sum(AVERAGEDURATIONMISS*(CALLCOUNT-REALCACHEHITCOUNT))/(0.5*((1+sum(CALLCOUNT-REALCACHEHITCOUNT))+ABS(-1+sum(CALLCOUNT-REALCACHEHITCOUNT)))))+0.0)) / (((0.5*((1+sum(AVERAGERESULTSIZE))+ABS(-1+sum(AVERAGERESULTSIZE))))) * 1000000000.0) as SECONDSAVEDPERBYTE, "
        + "0, " // the cache allocation, starting at zero
        + "sum(REALCACHEHITCOUNT) as STEPSUMREALCACHEHITCOUNT," + "sum(REALCACHEACTCOUNT) as STEPSUMREALCACHEACTCOUNT " + "FROM UNIQUECACHEHITS WHERE " + "CACHEHITCOUNT >= ? AND CACHEHITCOUNT <= ? " + "GROUP BY OPERATIONNAME";

     * 
     */
    
    /**
     * maximum reverse caller depth to analyze
     */
    public static final int MAX_CALLER_DEPTH = 20;

    /**
     * Execute the database update to aggregate all the cache hits together in
     * the UNIQUECACHEHITS table based on the content of the METRIC table.
     * 
     * @param dataSource
     *            The data source to use while running the query.
     * @throws SQLException
     *             Any unexpected SQL error that might occur.
     */
    public static void aggregateCacheHits( DataSource dataSource ) throws SQLException {

        final String METHODNAME = "aggregateCacheHits(DataSource dataSource)";

        boolean entryExitLogEnabled = LoggingHelper.isEntryExitTraceEnabled( LOGGER );

        if ( entryExitLogEnabled ) {
            Object[] params = { dataSource };
            LOGGER.entering( CLASS_NAME, METHODNAME, params );
        }
        long startTime = System.currentTimeMillis();

        CacheUtilities.executeUpdate( dataSource, AGGREGATE_CACHE_HITS_SQL );

        long duration = System.currentTimeMillis() - startTime;
        LOGGER.log( Level.INFO, "Took " + duration + " ms to run aggregateCacheHits." );

        if ( entryExitLogEnabled ) {
            LOGGER.exiting( CLASS_NAME, METHODNAME );
        }

    }

    /**
     * Regroup the cache hits stored in UNIQUECACHEHITS into the CACHEHITSTEPS
     * by splitting the frequency at which each cache value would be hit in
     * various steps. For example, specifying a stepSeparationPoint of
     * {100,1000} would create 3 step-groups of cache hits:
     * <ul>
     * <li>Values that were reused between 0 and 99 times.</li>
     * <li>Values that were reused between 100 and 999 times.</li>
     * <li>Values that were reused between 1000 and Long.MAX_VALUE times.</li>
     * </ul>
     * The duration of a step is the average of all the values regrouped.
     * <p>
     * The size of a step is the sum of all the values regrouped.
     * <p>
     * The number of cache it count for each step is the sum of all the cache hit of the values regrouped.
     * <p>
     * The Second Saved Per Byte is a calculation of : STEPSUMCACHEHITCOUNT * STEPAVERAGEDURATION / SUMAVERAGERESULTSIZE
     * / 1000000000
     * 
     * @param dataSource
     *            The data source to use to run the update.
     * @param stepSeparationPoints
     *            the number of cache hits between steps without specifying the
     *            starting zero or the end value of Long.MAX_VALUE. Must be
     *            positive and never repeat a value.
     * @throws SQLException
     *             Any unexpected SQL error that might occur.
     */
    public static void regroupCacheHitsInSteps( DataSource dataSource, long[] stepSeparationPoints ) throws SQLException {

        final String METHODNAME = " regroupCacheHitsInSteps(DataSource dataSource,long[] stepSeparationPoints)";

        boolean entryExitLogEnabled = LoggingHelper.isEntryExitTraceEnabled( LOGGER );

        if ( entryExitLogEnabled ) {
            Object[] params = { dataSource, Arrays.toString( stepSeparationPoints ) };
            LOGGER.entering( CLASS_NAME, METHODNAME, params );
        }

        List<Step> stepList = getStepListFromBreakPoints( stepSeparationPoints );

        for ( Step step : stepList ) {
            executeCacheStepAggregation( dataSource, step.getStart(), step.getStop(), step.toString(), "INSERT INTO CACHEHITSTEPS " );
        }

        Step singleStep = new Step();
        executeCacheStepAggregation( dataSource, singleStep.getStart(), singleStep.getStop(), singleStep.toString(), "INSERT INTO CACHEALLOCATION " );

        if ( entryExitLogEnabled ) {
            LOGGER.exiting( CLASS_NAME, METHODNAME );
        }
    }

    /**
     * Create a list of specific step object based on the separation points
     * specified.
     * 
     * @param stepSeparationPoints
     *            the number of cache hits between steps without specifying the
     *            starting zero or the end value of Long.MAX_VALUE. Negative
     *            values are ignored. Duplicate values are ignored.
     * @return The list of step objects specifying the start and stop values.
     */
    protected static List<Step> getStepListFromBreakPoints( long[] stepSeparationPoints ) {

        final String METHODNAME = " getStepListFromBreakPoints(long[] stepSeparationPoints)";

        boolean isTraceLogEnabled = LoggingHelper.isTraceEnabled( LOGGER );
        boolean entryExitLogEnabled = LoggingHelper.isEntryExitTraceEnabled( LOGGER );

        if ( entryExitLogEnabled ) {
            Object[] params = { Arrays.toString( stepSeparationPoints ) };
            LOGGER.entering( CLASS_NAME, METHODNAME, params );
        }

        List<Step> stepList = new ArrayList<Step>();
        if ( stepSeparationPoints == null || stepSeparationPoints.length == 0 ) {
            // single all inclusive step to add
            Step singleStep = new Step();
            stepList.add( singleStep );

            if ( isTraceLogEnabled ) {
                String msg = "No separation points specified, using default values. Start : " + singleStep.getStart() + ", Stop : " + singleStep.getStop();
                LOGGER.log( Level.FINE, msg );
            }

        } else {
            long startValue = 0;
            long stopValue = Long.MAX_VALUE;
            Arrays.sort( stepSeparationPoints );
            for ( int i = 0; i < stepSeparationPoints.length; i++ ) {
                stopValue = stepSeparationPoints[i];
                if ( stopValue <= 0 ) {
                    String messageKey = "Step value ignored. A step value must be bigger than zero. Current value : " + stopValue;
                    LOGGER.log( Level.WARNING, messageKey );
                    continue;
                }

                if ( stopValue == startValue ) {
                    String messageKey = "Step value ignored. A step value can't be repeated. Current value : " + stopValue;
                    LOGGER.log( Level.WARNING, messageKey );
                    continue;
                }

                long stop = stopValue - 1;

                Step stepToAdd = new Step();
                stepToAdd.setStart( startValue );
                stepToAdd.setStop( stop );

                stepList.add( stepToAdd );

                if ( isTraceLogEnabled ) {
                    LOGGER.log( Level.FINE, "Adding step from : " + startValue + " to : " + stop );
                }

                // prepare the next step start value
                startValue = stopValue;
            }
            Step lastStepToAdd = new Step();
            lastStepToAdd.setStart( startValue );
            lastStepToAdd.setStop( Long.MAX_VALUE );

            if ( isTraceLogEnabled ) {
                String msg = "Adding the last step from : " + lastStepToAdd.getStart() + " to : " + lastStepToAdd.getStop();
                LOGGER.log( Level.FINE, msg );
            }

            stepList.add( lastStepToAdd );
        }

        if ( entryExitLogEnabled ) {
            LOGGER.exiting( CLASS_NAME, METHODNAME );
        }

        return stepList;
    }

    /**
     * Regroup all the cache hits based on the number of times they resulted on
     * a cache hit. Each step will start with the start number and end with the
     * stop number. This step will sum the total space required to cache all the
     * regrouped cache hit and do an average of the time it took to fetch the
     * data.
     * 
     * @param dataSource
     *            the database connection that contains the data to regroup as
     *            steps.
     * @param start
     *            the minimum number of hits a value must have.
     * @param stop
     *            the maximum number of hits a value must have.
     * @param stepName
     *            the name of the step to create. This name should be unique
     *            compared to other steps.
     * @param insertIntoFragment
     *            the "insert into X" part of the SQL statement
     * @throws SQLException
     *             If any unexpected error occurs on the database.
     */
    private static void executeCacheStepAggregation( DataSource dataSource, long start, long stop, String stepName, String insertIntoFragment ) throws SQLException {

        final String METHODNAME = "executeCacheStepAggregation(DataSource dataSource,long start, long stop, String stepName";

        boolean entryExitLogEnabled = LoggingHelper.isEntryExitTraceEnabled( LOGGER );

        long startTime = System.currentTimeMillis();

        if ( entryExitLogEnabled ) {
            Object[] params = { start, stop, stepName };
            LOGGER.entering( CLASS_NAME, METHODNAME, params );
        }

        PreparedStatement createStatement = null;
        Connection connection = null;
        try {
            // $ANALYSIS-IGNORE
            connection = dataSource.getConnection();
            // $ANALYSIS-IGNORE
            String sql = insertIntoFragment + AGGREGATE_CACHE_HIT_STEPS_SQL_FRAGMENT;
            // $ANALYSIS-IGNORE
            createStatement = connection.prepareStatement( sql );
            createStatement.setString( 1, stepName );
            createStatement.setLong( 2, start );
            createStatement.setLong( 3, stop );

            createStatement.execute();
        } finally {

            long duration = System.currentTimeMillis() - startTime;
            LOGGER.log( Level.INFO, "Took " + duration + " ms to run executeCacheStepAggregation on " + stepName );

            CacheUtilities.closeQuietly( createStatement );
            CacheUtilities.closeQuietly( connection );

            if ( entryExitLogEnabled ) {
                LOGGER.exiting( CLASS_NAME, METHODNAME );
            }
        }
    }

    /**
     * Create cache allocation spaces based on the specified cache size. This
     * algorithm will allocate cache space for each step of every operation by
     * fulfilling those that save the largest amount of time per byte of cache
     * allocation. Once the cache size is exhausted, the remaining operation
     * steps will not get any cache space allocation.
     * 
     * @param dataSource
     *            the database containing all the potential cache hit value
     *            steps.
     * @param cacheSize
     *            the size of the cache to allocate.
     * @throws SQLException
     *             any unexpected error that occurs on the database.
     */
    // @SuppressWarnings( "resource" )
    public static void allocateCacheSpace( DataSource dataSource, long cacheSize ) throws SQLException {

        final String METHODNAME = "allocateCacheSpace(DataSource dataSource, long cacheSize)";

        boolean isTraceLogEnabled = LoggingHelper.isTraceEnabled( LOGGER );
        boolean entryExitLogEnabled = LoggingHelper.isEntryExitTraceEnabled( LOGGER );

        long startTime = System.currentTimeMillis();

        if ( entryExitLogEnabled ) {
            Object[] params = { dataSource, cacheSize };
            LOGGER.entering( CLASS_NAME, METHODNAME, params );
        }

        long remainingCacheSize = cacheSize;
        if ( remainingCacheSize < 0 ) {
            remainingCacheSize = 0;
        }

        Statement createStatement = null;
        Connection connection = null;
        ResultSet rs = null;
        PreparedStatement updateStatement = null;
        PreparedStatement updateAggregationStatement = null;
        try {
            // $ANALYSIS-IGNORE
            connection = dataSource.getConnection();
            // $ANALYSIS-IGNORE
            createStatement = connection.createStatement( ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY );

            String sqlSelect = "SELECT OPERATIONNAME,STEP,SUMAVERAGERESULTSIZE,CACHEALLOCATION from CACHEHITSTEPS order by SECONDSAVEDPERBYTE DESC";
            // $ANALYSIS-IGNORE
            rs = createStatement.executeQuery( sqlSelect );

            // $ANALYSIS-IGNORE
            String updateCacheHitStepCacheAllocationSQL = "UPDATE CACHEHITSTEPS SET CACHEALLOCATION=? WHERE OPERATIONNAME=? AND STEP=?";
            updateStatement = connection.prepareStatement( updateCacheHitStepCacheAllocationSQL );

            int i = 0;
            int maximumBatchSize = DatabaseMetricGatherer.INITIAL_MAXIMUM_DATABASE_BATCH_SIZE;

            while ( rs.next() ) {
                String operationName = rs.getString( "OPERATIONNAME" );
                String step = rs.getString( "STEP" );

                long sizeRequested = rs.getLong( "SUMAVERAGERESULTSIZE" );
                long sizeAllocated = sizeRequested;
                if ( remainingCacheSize - sizeRequested <= 0 ) {
                    sizeAllocated = remainingCacheSize;
                }
                remainingCacheSize -= sizeAllocated;

                updateStatement.setLong( 1, sizeAllocated );
                updateStatement.setString( 2, operationName );
                updateStatement.setString( 3, step );
                updateStatement.addBatch();
                ++i;

                if ( isTraceLogEnabled ) {
                    String msg = "Allocating " + sizeAllocated + " bytes of cache to operation : " + operationName + " on step : " + step + ". Remaining available cache size : " + remainingCacheSize;
                    LOGGER.log( Level.FINE, msg );
                }

                // ensure that we don't queue up too many batch updates.
                if ( i % maximumBatchSize == 0 ) {
                    updateStatement.executeBatch();
                    CacheUtilities.closeQuietly( updateStatement );
                    updateStatement = connection.prepareStatement( updateCacheHitStepCacheAllocationSQL );
                }
            }

            updateStatement.executeBatch();
            CacheUtilities.closeQuietly( updateStatement );

            // aggregate the cache allocation by operation
            String sqlAggregateSelect = "SELECT OPERATIONNAME,sum(CACHEALLOCATION) as CACHEALLOCATION from CACHEHITSTEPS group by OPERATIONNAME";
            List<List<Object>> executeQuery = AnalyzeMetricFile.executeQuery( dataSource, sqlAggregateSelect );

            // $ANALYSIS-IGNORE
            String updateCacheAllocationSQL = "UPDATE CACHEALLOCATION SET CACHEALLOCATION=? WHERE OPERATIONNAME=? ";
            updateStatement = connection.prepareStatement( updateCacheAllocationSQL );

            i = 0;

            for ( List<Object> list : executeQuery ) {
                String operationName = (String) list.get( 0 );
                long allocation = ( (Number) list.get( 1 ) ).longValue();

                updateStatement.setLong( 1, allocation );
                updateStatement.setString( 2, operationName );
                updateStatement.addBatch();
                ++i;

                if ( isTraceLogEnabled ) {
                    String msg = "Allocating " + allocation + " bytes of cache to operation : " + operationName;
                    LOGGER.log( Level.FINE, msg );
                }

                // ensure that we don't queue up too many batch updates.
                if ( i % maximumBatchSize == 0 ) {
                    updateStatement.executeBatch();
                    CacheUtilities.closeQuietly( updateStatement );
                    updateStatement = connection.prepareStatement( updateCacheAllocationSQL );
                }
            }

            updateStatement.executeBatch();

        } finally {

            long duration = System.currentTimeMillis() - startTime;
            LOGGER.log( Level.INFO, "Took " + duration + " ms to run allocateCacheSpace." );

            CacheUtilities.closeQuietly( rs );
            CacheUtilities.closeQuietly( createStatement );
            CacheUtilities.closeQuietly( updateStatement );
            CacheUtilities.closeQuietly( updateAggregationStatement );
            CacheUtilities.closeQuietly( connection );

            if ( entryExitLogEnabled ) {
                LOGGER.exiting( CLASS_NAME, METHODNAME );
            }
        }
    }

    /**
     * Create all the tables required to capture and analyze cache operation
     * metrics.
     * 
     * @param dataSource
     *            the datasource to use
     * @throws SQLException
     *             any unexpected database error.
     */
    public static void createTables( DataSource dataSource ) throws SQLException {

        final String METHODNAME = "createTables(DataSource dataSource)";

        boolean entryExitLogEnabled = LoggingHelper.isEntryExitTraceEnabled( LOGGER );

        if ( entryExitLogEnabled ) {
            Object[] params = { dataSource };
            LOGGER.entering( CLASS_NAME, METHODNAME, params );
        }

        createMetricTable( dataSource );
        createUniqueCacheHitsTable( dataSource );
        createCacheHitStepsTable( dataSource );
        createCacheAllocationTable( dataSource );
        createUniqueCacheHitsCacheHitCountIndex( dataSource );
        createMetricIdentifierIndex( dataSource );
        createMetricOperationNameIndex( dataSource );
        createMetricParentIdentifierIndex( dataSource );
        createCallerTables( dataSource );

        if ( entryExitLogEnabled ) {
            LOGGER.exiting( CLASS_NAME, METHODNAME );
        }
    }

    /**
     * Create the metric table
     * 
     * @param dataSource
     *            the datasource to use
     * @throws SQLException
     *             any unexpected database error.
     */
    public static void createMetricTable( DataSource dataSource ) throws SQLException {
        CacheUtilities.execute( dataSource, CREATE_METRIC_TABLE_SQL );
    }

    /**
     * Create the unique cache hit table
     * 
     * @param dataSource
     *            the datasource to use
     * @throws SQLException
     *             any unexpected database error.
     */
    public static void createUniqueCacheHitsTable( DataSource dataSource ) throws SQLException {
        CacheUtilities.execute( dataSource, CREATE_UNIQUECACHEHITS_TABLE_SQL );
    }

    /**
     * Create the unique cache hit count index
     * 
     * @param dataSource
     *            the datasource to use
     * @throws SQLException
     *             any unexpected database error.
     */
    public static void createUniqueCacheHitsCacheHitCountIndex( DataSource dataSource ) throws SQLException {
        CacheUtilities.execute( dataSource, CREATE_IDX_UNIQUECACHEHITS_CACHEHITCOUNT_SQL );
    }

    /**
     * Create the metric identifier index
     * 
     * @param dataSource
     *            the datasource to use
     * @throws SQLException
     *             any unexpected database error.
     */
    public static void createMetricIdentifierIndex( DataSource dataSource ) throws SQLException {
        CacheUtilities.execute( dataSource, CREATE_IDX_METRIC_IDENTIFIER );
    }

    /**
     * Create the metric parent identifier index
     * 
     * @param dataSource
     *            the datasource to use
     * @throws SQLException
     *             any unexpected database error.
     */
    public static void createMetricParentIdentifierIndex( DataSource dataSource ) throws SQLException {
        CacheUtilities.execute( dataSource, CREATE_IDX_METRIC_PARENTIDENTIFIER );
    }

    /**
     * Create the metric operation name index
     * 
     * @param dataSource
     *            the datasource to use
     * @throws SQLException
     *             any unexpected database error.
     */
    public static void createMetricOperationNameIndex( DataSource dataSource ) throws SQLException {
        CacheUtilities.execute( dataSource, CREATE_IDX_METRIC_OPERATIONNAME );
    }

    /**
     * Create the cache hit step table
     * 
     * @param dataSource
     *            the datasource to use
     * @throws SQLException
     *             any unexpected database error.
     */
    public static void createCacheHitStepsTable( DataSource dataSource ) throws SQLException {
        CacheUtilities.execute( dataSource, CREATE_CACHE_HIT_STEPS_TABLE_SQL );
    }

    /**
     * Create the cache allocation table
     * 
     * @param dataSource
     *            the datasource to use
     * @throws SQLException
     *             any unexpected database error.
     */
    public static void createCacheAllocationTable( DataSource dataSource ) throws SQLException {
        CacheUtilities.execute( dataSource, CREATE_CACHE_ALLOCATION_TABLE_SQL );
    }

    /**
     * Drop all the cache measurement tables.
     * 
     * @param dataSource
     *            the datasource to use
     * @throws SQLException
     *             any unexpected database error.
     */
    public static void dropTables( DataSource dataSource ) throws SQLException {

        final String METHODNAME = "dropTables(DataSource dataSource)";

        boolean entryExitLogEnabled = LoggingHelper.isEntryExitTraceEnabled( LOGGER );
        boolean traceEnabled = LoggingHelper.isTraceEnabled( LOGGER );

        if ( entryExitLogEnabled ) {
            Object[] params = { dataSource };
            LOGGER.entering( CLASS_NAME, METHODNAME, params );
        }

        try {
            dropUniqueCacheHitsCacheHitCountIndex( dataSource );
        } catch ( Exception ex ) {
            if ( traceEnabled ) {
                LOGGER.log( LoggingHelper.DEFAULT_TRACE_LOG_LEVEL, "dropUniqueCacheHitsCacheHitCountIndex failed", ex );
            }
        }
        try {
            dropMetricTable( dataSource );
        } catch ( Exception ex ) {
            if ( traceEnabled ) {
                LOGGER.log( LoggingHelper.DEFAULT_TRACE_LOG_LEVEL, "dropMetricTable failed", ex );
            }
        }
        try {
            dropUniqueCacheHitsTable( dataSource );
        } catch ( Exception ex ) {
            if ( traceEnabled ) {
                LOGGER.log( LoggingHelper.DEFAULT_TRACE_LOG_LEVEL, "dropUniqueCacheHitsTable failed", ex );
            }
        }
        try {
            dropCacheHitStepsTable( dataSource );
        } catch ( Exception ex ) {
            if ( traceEnabled ) {
                LOGGER.log( LoggingHelper.DEFAULT_TRACE_LOG_LEVEL, "dropCacheHitStepsTable failed", ex );
            }
        }
        try {
            dropCacheAllocationTable( dataSource );
        } catch ( Exception ex ) {
            if ( traceEnabled ) {
                LOGGER.log( LoggingHelper.DEFAULT_TRACE_LOG_LEVEL, "dropCacheAllocationTable failed", ex );
            }
        }

        if ( entryExitLogEnabled ) {
            LOGGER.exiting( CLASS_NAME, METHODNAME );
        }

    }

    /**
     * Drop the metric table
     * 
     * @param dataSource
     *            the datasource to use
     * @throws SQLException
     *             any unexpected database error.
     */
    public static void dropMetricTable( DataSource dataSource ) throws SQLException {
        CacheUtilities.execute( dataSource, DROP_METRIC_TABLE_SQL );
    }

    /**
     * Drop the unique cache hit table
     * 
     * @param dataSource
     *            the datasource to use
     * @throws SQLException
     *             any unexpected database error.
     */
    public static void dropUniqueCacheHitsTable( DataSource dataSource ) throws SQLException {
        CacheUtilities.execute( dataSource, DROP_UNIQUECACHEHITS_TABLE_SQL );
    }

    /**
     * drop the unique cache hits cache hit count index.
     * 
     * @param dataSource
     *            the datasource to use.
     * @throws SQLException
     *             any unexpected database error.
     */
    public static void dropUniqueCacheHitsCacheHitCountIndex( DataSource dataSource ) throws SQLException {
        CacheUtilities.execute( dataSource, DROP_IDX_UNIQUECACHEHITS_CACHEHITCOUNT_SQL );
    }

    /**
     * drop the cache hit steps table.
     * 
     * @param dataSource
     *            the datasource to use.
     * @throws SQLException
     *             any unexpected database error.
     */
    public static void dropCacheHitStepsTable( DataSource dataSource ) throws SQLException {
        CacheUtilities.execute( dataSource, DROP_CACHE_HIT_STEPS_TABLE_SQL );
    }

    /**
     * drop the cache allocation table.
     * 
     * @param dataSource
     *            the datasource to use.
     * @throws SQLException
     *             any unexpected database error.
     */
    public static void dropCacheAllocationTable( DataSource dataSource ) throws SQLException {
        CacheUtilities.execute( dataSource, DROP_CACHE_ALLOCATION_TABLE_SQL );
    }

    /**
     * Clear all the data stored in the cache measurement tables.
     * 
     * @param dataSource
     *            the datasource to use.
     * @throws SQLException
     *             any unexpected database error.
     */
    public static void clearTables( DataSource dataSource ) throws SQLException {

        final String METHODNAME = "clearTables(DataSource dataSource)";

        boolean entryExitLogEnabled = LoggingHelper.isEntryExitTraceEnabled( LOGGER );

        if ( entryExitLogEnabled ) {
            Object[] params = { dataSource };
            LOGGER.entering( CLASS_NAME, METHODNAME, params );
        }

        LOGGER.log( Level.INFO, "Clearing all existing data." );
        clearMetricTable( dataSource );
        clearUniqueCacheHitsTable( dataSource );
        clearCacheHitStepsTable( dataSource );
        clearCacheAllocationTable( dataSource );

        if ( entryExitLogEnabled ) {
            LOGGER.exiting( CLASS_NAME, METHODNAME );
        }
    }

    /**
     * Clear the metric table.
     * 
     * @param dataSource
     *            the datasource to use.
     * @throws SQLException
     *             any unexpected database error.
     */
    public static void clearMetricTable( DataSource dataSource ) throws SQLException {
        CacheUtilities.execute( dataSource, CLEAR_METRIC_TABLE_SQL );
    }

    /**
     * Clear the unique cache hits table.
     * 
     * @param dataSource
     *            the datasource to use.
     * @throws SQLException
     *             any unexpected database error.
     */
    public static void clearUniqueCacheHitsTable( DataSource dataSource ) throws SQLException {
        CacheUtilities.execute( dataSource, CLEAR_UNIQUECACHEHITS_TABLE_SQL );
    }

    /**
     * Clear the cache hit steps table.
     * 
     * @param dataSource
     *            the datasource to use.
     * @throws SQLException
     *             any unexpected database error.
     */
    public static void clearCacheHitStepsTable( DataSource dataSource ) throws SQLException {
        CacheUtilities.execute( dataSource, CLEAR_CACHE_HIT_STEPS_TABLE_SQL );
    }

    /**
     * create the tables used to analyze the reverse callers
     * 
     * @param dataSource
     *            The datasource to use.
     * @throws SQLException
     *             any unexpected database error
     */
    public static void createCallerTables( DataSource dataSource ) throws SQLException {
        final String METHODNAME = "createCallerTables(DataSource dataSource)";
        for ( int i = 0; i < MAX_CALLER_DEPTH; ++i ) {
            try {
                CacheUtilities.execute( dataSource, "DROP TABLE CALLER" + i );
            } catch ( SQLException ex ) {
                if ( LOGGER.isLoggable( Level.FINER ) ) {
                    String msg = "SQL Exception expected when dropping a table that might not exist.";
                    LOGGER.logp( Level.FINER, CLASS_NAME, METHODNAME, msg, ex );
                }
            } catch ( Exception ex ) {
                LoggingHelper.logUnexpectedException( LOGGER, CLASS_NAME, "createCallerTables(DataSource dataSource)", ex );
            }
            CacheUtilities.execute( dataSource, "CREATE TABLE CALLER" + i + " (" + "IDENTIFIER BIGINT NOT NULL )" );
        }
    }

    /**
     * Clear the cache allocation table.
     * 
     * @param dataSource
     *            the datasource to use.
     * @throws SQLException
     *             any unexpected database error.
     */
    public static void clearCacheAllocationTable( DataSource dataSource ) throws SQLException {
        CacheUtilities.execute( dataSource, CLEAR_CACHE_ALLOCATION_TABLE_SQL );
    }

}
