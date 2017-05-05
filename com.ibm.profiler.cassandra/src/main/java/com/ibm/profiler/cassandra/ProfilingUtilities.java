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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.datastax.driver.core.BatchStatement;
import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.RegularStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Statement;
import com.ibm.commerce.cache.LoggingHelper;
import com.ibm.commerce.cache.MetricFileLoader;
import com.ibm.commerce.cache.OperationMetric;
import com.ibm.logger.PerformanceLogger;
import com.ibm.service.detailed.cassandra.CassandraLogger;

/**
 * Utility methods to perform cassandra profiling.
 */
public class ProfilingUtilities {

    private static final String CLASSNAME = ProfilingUtilities.class.getCanonicalName();

    /**
     * The logger.
     */
    public static final Logger LOGGER = Logger.getLogger( CLASSNAME );

    /**
     * Integer string cache for common values.
     */
    private static final String[] NUMBER_AS_STRING = new String[20];

    static {
        for ( int i = 0; i < NUMBER_AS_STRING.length; ++i ) {
            NUMBER_AS_STRING[i] = Integer.toString( i );
        }
    }

    /**
     * Get an integer as string. Adds the benefit of leveraging a cache for the
     * common values between 0 and 19.
     * 
     * @param i
     *            The integer value.
     * @return The string value.
     */
    public static String getIntegerString( int i ) {
        String returnValue = null;
        if ( i < NUMBER_AS_STRING.length ) {
            returnValue = NUMBER_AS_STRING[i];
        } else {
            returnValue = Integer.toString( i );
        }
        return returnValue;
    }

    private static final String[] EMPTY_STRING_ARRAY = new String[0];

    /**
     * Convert an argument map to an array of key-value strings.
     * 
     * @param argumentMap
     *            The argument map.
     * @return The ordered key-value string array.
     */
    public static String[] convertArgumentMapToArray( Map<String, Object> argumentMap ) {
        final String METHODNAME = "convertArgumentMapToArray(Map<String, Object> argumentMap)";
        final boolean entryExitTraceEnabled = LoggingHelper.isEntryExitTraceEnabled( LOGGER );
        // final boolean traceEnabled = LoggingHelper.isTraceEnabled(LOGGER);
        if ( entryExitTraceEnabled ) {
            Object[] parameters = new Object[] { argumentMap };
            LOGGER.entering( CLASSNAME, METHODNAME, parameters );
        }

        String[] returnValue = EMPTY_STRING_ARRAY;
        if ( argumentMap != null ) {
            returnValue = new String[argumentMap.size() * 2];
            ArrayList<String> keyList = new ArrayList<String>( argumentMap.keySet() );
            Collections.sort( keyList );
            int i = 0;
            for ( String key : keyList ) {
                Object value = argumentMap.get( key );
                returnValue[i++] = key;
                returnValue[i++] = convertValueToStringSafe( value );
            }
        }

        if ( entryExitTraceEnabled ) {
            LOGGER.exiting( CLASSNAME, METHODNAME );
        }
        return returnValue;
    }

    /**
     * Convert an object to a string in a safe way that hides exceptions.
     * 
     * @param value
     *            The object to convert.
     * @return The string value. Never null. If the object is null, "null" is
     *         returned.
     */
    public static String convertValueToStringSafe( Object value ) {
        final String METHODNAME = "convertValueToString(Object value)";
        final boolean entryExitTraceEnabled = LoggingHelper.isEntryExitTraceEnabled( LOGGER );
        // final boolean traceEnabled = LoggingHelper.isTraceEnabled(LOGGER);
        if ( entryExitTraceEnabled ) {
            Object[] parameters = new Object[] { value };
            LOGGER.entering( CLASSNAME, METHODNAME, parameters );
        }

        String returnValue = "null";
        if ( value != null ) {
            try {
                returnValue = value.toString();
            } catch ( Exception ex ) {
                LoggingHelper.logUnexpectedException( LOGGER, CLASSNAME, METHODNAME, ex );
                returnValue = "unknown";
            }
        }

        if ( entryExitTraceEnabled ) {
            LOGGER.exiting( CLASSNAME, METHODNAME );
        }
        return returnValue;
    }

    /**
     * Convert an argument array to a numeric argument map. Each argument will
     * get a key based on it's order.
     * 
     * @param mapToPopulate
     *            The map to populate.
     * @param arg0
     *            The list of arguments.
     */
    public static void convertArgumentArrayToNumericArgumentMap( Map<String, Object> mapToPopulate, Object... arg0 ) {

        final String METHODNAME = "convertArgumentArrayToNumericArgumentMap(Map<String, Object> mapToPopulate, Object... arg0)";
        final boolean entryExitTraceEnabled = LoggingHelper.isEntryExitTraceEnabled( LOGGER );
        // final boolean traceEnabled = LoggingHelper.isTraceEnabled(LOGGER);
        if ( entryExitTraceEnabled ) {
            Object[] parameters = new Object[] { mapToPopulate, arg0 };
            LOGGER.entering( CLASSNAME, METHODNAME, parameters );
        }

        if ( arg0 != null && mapToPopulate != null ) {
            int i = 0;
            for ( Object object : arg0 ) {
                String integerString = ProfilingUtilities.getIntegerString( i );
                mapToPopulate.put( integerString, object );
                i++;
            }
        }

        if ( entryExitTraceEnabled ) {
            LOGGER.exiting( CLASSNAME, METHODNAME );
        }
    }

    /**
     * Convert an argument array to an ordered key-value string array using the
     * argument order as the key.
     * 
     * @param arg0
     *            The array of arguments.
     * @return The key-value string array.
     */
    public static String[] convertArgumentArrayToNumericArgumentArray( Object... arg0 ) {

        final String METHODNAME = "String[] convertArgumentArrayToNumericArgumentArray(Object... arg0)";
        final boolean entryExitTraceEnabled = LoggingHelper.isEntryExitTraceEnabled( LOGGER );
        // final boolean traceEnabled = LoggingHelper.isTraceEnabled(LOGGER);
        if ( entryExitTraceEnabled ) {
            LOGGER.entering( CLASSNAME, METHODNAME, arg0 );
        }

        String[] returnValue = EMPTY_STRING_ARRAY;
        if ( arg0 != null ) {
            returnValue = new String[arg0.length * 2];
            int i = 0;
            int counter = 0;
            for ( Object object : arg0 ) {
                returnValue[i++] = ProfilingUtilities.getIntegerString( counter++ );
                returnValue[i++] = convertValueToStringSafe( object );
            }
        }
        if ( entryExitTraceEnabled ) {
            LOGGER.exiting( CLASSNAME, METHODNAME );
        }

        return returnValue;
    }

    /**
     * Initialize a metric based on a statement and arguments.
     * 
     * @param statement
     *            The statement to measure.
     * @param arguments
     *            The arguments sent.
     * @return The initialized metric. May be null if measurement is disabled.
     */
    public static OperationMetric initializeMetric( String statement, String... arguments ) {
        final String METHODNAME = "initializeMetric(String statement, String... parameters)";
        final boolean entryExitTraceEnabled = LOGGER.isLoggable( Level.FINE );
        // final boolean traceEnabled = LoggingHelper.isTraceEnabled(LOGGER);
        if ( entryExitTraceEnabled ) {
            Object[] parameters = new Object[] { statement, arguments };
            LOGGER.entering( CLASSNAME, METHODNAME, parameters );
        }

        OperationMetric metric = null;
        if ( CassandraLogger.LOG_GATHERER.isEnabled() ) {
            try {
                metric = new OperationMetric();
                String[] orderedKeyValues = arguments;
                String operationName = "Cassandra : " + statement;
                metric.startOperation( operationName, false, orderedKeyValues );
                MetricFileLoader.adjustCassandraMetric(metric);
                
                CassandraLogger.LOG_GATHERER.gatherMetricEntryLog(metric);
                
            } catch ( Throwable ex ) {
                LoggingHelper.logUnexpectedException( LOGGER, CLASSNAME, METHODNAME, ex );
            }
        }

        if ( entryExitTraceEnabled ) {
            LOGGER.exiting( CLASSNAME, METHODNAME, metric );
        }
        return metric;
    }

    /**
     * Log an operation metric based on a result set.
     * 
     * @param metric
     *            The metric to log. If null, no entry is logged.
     * @param execute
     *            The result set to measure for size.
     */
    public static void logMetric( OperationMetric metric, ResultSet execute ) {
        final String METHODNAME = "logMetric(OperationMetric metric,ResultSet execute)";
        final boolean entryExitTraceEnabled = LoggingHelper.isEntryExitTraceEnabled( LOGGER );
        // final boolean traceEnabled = LoggingHelper.isTraceEnabled(LOGGER);
        if ( entryExitTraceEnabled ) {
            Object[] parameters = new Object[] { metric, execute };
            LOGGER.entering( CLASSNAME, METHODNAME, parameters );
        }

        if ( metric != null ) {
            try {
                int responseSize = 1000; // default value
                if ( execute != null ) {

                    // approximate the read data size based on the number of
                    // rows fetched and the number of columns read.
                    int availableWithoutFetching = execute.getAvailableWithoutFetching();
                    int columnCount = execute.getColumnDefinitions().size();
                    responseSize = availableWithoutFetching * columnCount * 100;
                }

                boolean wasCacheHit = false;
                metric.stopOperation( responseSize, wasCacheHit );
                

                CassandraLogger.LOG_GATHERER.gatherMetric( metric );
                
                metric.setOperationName("Cassandra_All_Operations");
                PerformanceLogger.increase(metric);
                
            } catch ( Throwable ex ) {
                LoggingHelper.logUnexpectedException( LOGGER, CLASSNAME, METHODNAME, ex );
            }
        }
        if ( entryExitTraceEnabled ) {
            LOGGER.exiting( CLASSNAME, METHODNAME );
        }
    }

    private static String[] EMTPY_STRING_ARRAY = new String[] {};

    /**
     * Get the arguments of a statement in an ordered key-value array.
     * 
     * @param arg0
     *            The statement.
     * @return The key-value array.
     */
    public static String[] getStatementArguments( Statement arg0 ) {
        String[] returnValue = EMTPY_STRING_ARRAY;
        if ( arg0 instanceof ProfiledBoundStatement ) {
            returnValue = ( (ProfiledBoundStatement) arg0 ).getArgumentList();
        } else if ( arg0 instanceof BatchStatement ) {
            List<String> argumentList = new ArrayList<String>();
            Collection<Statement> statements = ( (BatchStatement) arg0 ).getStatements();
            for ( Statement statement : statements ) {
                String[] statementArguments = getStatementArguments( statement );
                Collections.addAll( argumentList, statementArguments );
            }
            returnValue = argumentList.toArray( new String[argumentList.size()] );
        }
        return returnValue;
    }

    /**
     * Get the name of a statement.
     * 
     * @param arg0 The statement.
     * @return The name used for logging.
     */
    public static String getStatementName( Statement arg0 ) {
        String returnValue = "unknown";
        if ( arg0 instanceof RegularStatement ) {
            returnValue = ( (RegularStatement) arg0 ).getQueryString();
        } else if ( arg0 instanceof BoundStatement ) {
            PreparedStatement preparedStatement = ( (BoundStatement) arg0 ).preparedStatement();
            returnValue = preparedStatement.getQueryString();
        } else if ( arg0 instanceof BatchStatement ) {
            StringBuilder value = new StringBuilder( "Batch : " );
            Collection<Statement> statements = ( (BatchStatement) arg0 ).getStatements();
            boolean first = true;
            for ( Statement statement : statements ) {
                if ( first ) {
                    first = false;
                } else {
                    value.append( ", " );
                }
                String statementName = getStatementName( statement );
                value.append( statementName );
            }
            returnValue = value.toString();
        }
        return returnValue;
    }
}
