package com.ibm.commerce.cache;

/*
 *-----------------------------------------------------------------
 * IBM Confidential
 *
 * OCO Source Materials
 *
 * WebSphere Commerce
 *
 * (C) Copyright IBM Corp. 2012, 2015
 *
 * The source code for this program is not published or otherwise
 * divested of its trade secrets, irrespective of what has
 * been deposited with the U.S. Copyright Office.
 *-----------------------------------------------------------------
 */

import java.awt.Desktop;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Scanner;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import javax.sql.DataSource;

import org.apache.commons.lang3.StringEscapeUtils;

// import com.ibm.db2.jcc.DB2Driver;

/**
 * Class used to analyze metric files and generate reports.
 */
public class AnalyzeMetricFile {

    /** copyright */
    public static final String COPYRIGHT = com.ibm.commerce.copyright.IBMCopyright.SHORT_COPYRIGHT;

    private static final Logger LOGGER = Logger.getLogger( AnalyzeMetricFile.class.getName() );

    private static final String SELECT_ALL_OPERATIONS_SQL = "SELECT OPERATIONNAME from CACHEALLOCATION order by (AVERAGEDURATION*SUMCALLCOUNT) desc";

    private static final String STACK_STEP_FIELD_LIST = "IDENTIFIER,"// 0
        + "PARENTIDENTIFIER," // 1
        + "OPERATIONNAME," // 2
        + "STARTIME," // 3
        + "STOPTIME," // 4
        + "DURATION,"// 5
        + "RESULTSIZE," // 6
        + "FROMCACHE," // 7
        + "CACHEENABLED," // 8
        + "KEYVALUE";// 9

    private static final String SELECT_CHILD_STACK_STEPS_SQL = "SELECT " + STACK_STEP_FIELD_LIST + " from METRIC where PARENTIDENTIFIER=? order by STARTIME";

    // $ANALYSIS-IGNORE
    /**
     * SQL fragment used to limit the results to 1 entry.
     */
    public static final String SQL_LIMIT_TO_ONE_RESULT;

    static {
        String limitClause = "";
        boolean runningWithLimit = true;
        if ( runningWithLimit ) {
            limitClause = " FETCH FIRST 1 ROWS ONLY";
        }
        SQL_LIMIT_TO_ONE_RESULT = limitClause;
    }

    private static final String SELECT_FASTEST_SQL = "SELECT " + STACK_STEP_FIELD_LIST + " from METRIC where OPERATIONNAME=?" + " and DURATION = (select min(DURATION) from METRIC where OPERATIONNAME=? )" + " order by DURATION asc"
        + SQL_LIMIT_TO_ONE_RESULT;

    private static final String SELECT_AVERAGE_SQL = "SELECT " + STACK_STEP_FIELD_LIST + " from METRIC where OPERATIONNAME=?" + " and DURATION >= (select avg(DURATION) from METRIC where OPERATIONNAME=? ) order by DURATION asc" + SQL_LIMIT_TO_ONE_RESULT;

    private static final String SELECT_SLOWEST_SQL = "SELECT " + STACK_STEP_FIELD_LIST + " from METRIC where OPERATIONNAME=?" + " and DURATION = (select max(DURATION) from METRIC where OPERATIONNAME=? ) order by DURATION desc" + SQL_LIMIT_TO_ONE_RESULT;

    private boolean loadDataFiles;

    private boolean computeMetrics;

    private boolean reportResult;

    private boolean reportStack;

    private boolean recreateDatabaseTables;

    private List<String> fileToLoadList = new ArrayList<String>();

    private List<String> logFileToLoadList = new ArrayList<String>();

    private Map<String, PreparedStatement> statementCache = new HashMap<String, PreparedStatement>();

    private static final boolean CACHE_PREPARED_STATEMENTS = false;

    private DirectDatabaseMetricGatherer gatherer;

    private SingleJdbcDataSource dataSource;

    private SingleJdbcConnection jdbcConnection;

    private String reportOutputDir;

    private String stackOutputDir;

    private String executionOutputDir;

    private boolean clearDatabase;

    private String logFileCharsetName;

    private boolean reportCaller;

    private String callerOutputDir;

    private boolean truncateSqlStatementOperationNames;

    private boolean reportExecution;

    private boolean reportTrend;

    /**
     * Maximum number of entries to generate in the operation and the execution
     * reports.
     */
    private long entryCountLimit;

    private static String propertyFileName = "analysis.properties";

    /**
     * prefix used to read system properties specified by the user instead of
     * the properties file
     */
    public static final String PROPERTY_PREFIX = "com.ibm.service.report.";

    /**
     * 
     * @return option to create database tables on start.
     */
    public boolean recreateTableOnStart() {
        return recreateDatabaseTables;
    }

    /**
     * Start the analysis.
     * 
     * @param args
     *            Program arguments. Only one argument is optional : the
     *            argument to specify the property file to load. Defaults to
     *            "analysis.properties" if not specified.
     */
    public static void main( String[] args ) {
        try {
            // initializeLogger();
            if ( args != null && args.length > 0 ) {
                propertyFileName = args[0];
            }

            AnalyzeMetricFile analyzer = new AnalyzeMetricFile();
            analyzer.runAnalysis();
        } catch ( Exception e ) {
            LOGGER.log( Level.SEVERE, "Exception caught, aborting program : " + e.getMessage(), e );
            System.exit( -1 );
        }
    }

    /**
     * utility method
     * 
     * @throws IOException
     *             unexpected.
     */
    public static void initializeLogger() throws IOException {
        System.setProperty( "java.util.logging.config.file", "logging.properties" );
        LogManager.getLogManager().readConfiguration();
    }

    /**
     * Constructor.
     * 
     * @throws Exception
     *             unexpected.
     */
    public AnalyzeMetricFile() throws Exception {
        loadProperties();
    }

    /**
     * Load the analysis properties.
     * 
     * @throws Exception
     *             unexpected.
     */
    private void loadProperties() throws Exception {
        Properties prop = new Properties();
        File propFile = new File(propertyFileName);
        if( propFile.exists() ) {
            LOGGER.log( Level.INFO, "Loading property file : " + propFile.getAbsolutePath() );
            InputStream propIS = null;
            try {
            	propIS = new FileInputStream( propFile );
            	prop.load(propIS);
                LOGGER.log( Level.INFO, "Properties read : " + prop );
                stripKeyPrefix( PROPERTY_PREFIX, prop );
            } catch (IOException e) {
            	LOGGER.log( Level.SEVERE, "Failed to load property file : " + propFile.getAbsolutePath() + e.getMessage(), e);
            } finally {
            	CacheUtilities.closeQuietly(propIS);
            }
        }
        else {
            LOGGER.log( Level.WARNING, "Specified property file couldn't be found : " + propertyFileName );
        }
        
        mergeSystemProperties( PROPERTY_PREFIX, prop, true );
        LOGGER.log( Level.INFO, "Properties after merge with system properties : " + prop );

        allocatedCacheSize = readLongProperty( prop, "allocatedCacheSize", 1000000000l );
        entryCountLimit = readLongProperty( prop, "entryCountLimit", 1000l );

        trendTimeIntervalInNano = readLongProperty( prop, "trendTimeIntervalInNano", 1000l * 1000l * 1000l );

        String limitMessage = "Entry count limit in reports \"entryCountLimit\" set to : " + entryCountLimit;
        LOGGER.log( Level.INFO, limitMessage );

        loadDataFiles = readBooleanProperty( prop, "loadDataFiles", true );
        computeMetrics = readBooleanProperty( prop, "computeMetrics", true );
        reportResult = readBooleanProperty( prop, "reportResult", true );
        reportStack = readBooleanProperty( prop, "reportStack", true );
        reportCaller = readBooleanProperty( prop, "reportCaller", true );
        reportExecution = readBooleanProperty( prop, "reportExecution", true );
        reportTrend = readBooleanProperty( prop, "reportTrend", true );
        recreateDatabaseTables = readBooleanProperty( prop, "recreateDatabaseTables", true );
        reportResult = readBooleanProperty( prop, "reportResult", true );

        parseWasJdbcTrace = readBooleanProperty( prop, "parseWasJdbcTrace", false );
        logTimestampFormat = prop.getProperty( "logTimestampFormat", MetricFileLoader.DEFAULT_TIMESTAMP_FORMAT );
        logJdbcTraceRegularExpression = prop.getProperty( "logJdbcTraceRegularExpression", MetricFileLoader.DEFAULT_JDBC_TRACE_REGEX );
        logEntryTraceRegularExpression = prop.getProperty( "logEntryTraceRegularExpression", MetricFileLoader.DEFAULT_TRACE_ENTRY_REGEX );
        logExitTraceRegularExpression = prop.getProperty( "logExitTraceRegularExpression", MetricFileLoader.DEFAULT_TRACE_EXIT_REGEX );
        jdbcDriver = prop.getProperty( "jdbcDriver", "org.apache.derby.jdbc.EmbeddedDriver" );
        jdbcUrl = prop.getProperty( "jdbcUrl", "jdbc:derby:./cache-metric-database;create=true" );

        reportOutputDir = prop.getProperty( "reportOutputDir", "./reports" );

        java.util.Date date = new java.util.Date();
        Timestamp timestamp = new Timestamp( date.getTime() );
        String timeStampString = timestamp.toString().replaceAll( ":", "." );
        reportOutputDir = reportOutputDir + "/" + timeStampString;

        executionOutputDir = reportOutputDir + "/execution";
        stackOutputDir = reportOutputDir + "/stack";
        callerOutputDir = reportOutputDir + "/caller";

        logFileCharsetName = prop.getProperty( "logFileCharsetName", "UTF-8" );

        fileToLoadList = readStringListProperty( prop, "fileToLoadList", "\\|" );

        logFileToLoadList = readStringListProperty( prop, "logFileToLoadList", "\\|" );
        clearDatabase = readBooleanProperty( prop, "clearDatabase", false );
        truncateSqlStatementOperationNames = readBooleanProperty( prop, "truncateSqlStatementOperationNames", true );

        String cacheHitStepsString = prop.getProperty( "cacheHitSteps" );
        if ( !CacheUtilities.isBlank( cacheHitStepsString ) ) {
            try {
                String[] split = cacheHitStepsString.split( "," );
                long[] steps = new long[split.length];

                for ( int i = 0; i < split.length; ++i ) {
                    String longString = split[i];
                    long parseLong = Long.parseLong( longString );
                    steps[i] = parseLong;
                }
                cacheHitSteps = steps;
            } catch ( Exception e ) {
                String message = "Failed to parse the value of the cacheHitSteps property : \"" + cacheHitStepsString + "\". The error message is : " + e.getMessage();
                throw new Exception( message, e );
            }
        }
    }

    /**
     * remove the prefix from all property keys that match it.
     * 
     * @param propertyPrefix
     *            the prefix to remove
     * @param prop
     *            the properties to clean.
     */
    public static void stripKeyPrefix( String propertyPrefix, Properties prop ) {
        Properties copy = new Properties();
        copy.putAll( prop );
        Set<Entry<Object, Object>> entrySet = copy.entrySet();
        for ( Entry<Object, Object> entry : entrySet ) {
            Object key = entry.getKey();
            if ( key instanceof String ) {
                String keyString = (String) key;
                if ( keyString.startsWith( propertyPrefix ) ) {
                    keyString = keyString.substring( propertyPrefix.length() );
                    if ( !prop.contains( keyString ) ) {
                        prop.put( keyString, entry.getValue() );
                        prop.remove(key);
                    }
                }
            }
        }
    }

    /**
     * merge System Properties to allow the user to override properties from the
     * configuration file on the command line.
     * 
     * @param prefix
     *            The property name prefix to look for.
     * @param prop
     *            the properties to fill.
     * @param stripPrefix
     *            if set to true, the property name put in the prop parameter
     *            will be stripped of the property prefix.
     */
    public static void mergeSystemProperties( String prefix, Properties prop, boolean stripPrefix ) {
        Properties properties = System.getProperties();
        Set<Entry<Object, Object>> entrySet = properties.entrySet();
        for ( Entry<Object, Object> entry : entrySet ) {
            Object key = entry.getKey();
            if ( key instanceof String ) {
                String keyString = (String) key;
                if ( keyString.startsWith( prefix ) ) {
                    if ( stripPrefix ) {
                        keyString = keyString.substring( prefix.length() );
                    }
                    prop.put( keyString, entry.getValue() );
                }
            }
        }
    }

    /**
     * Read a long value from properties.
     * 
     * @param prop
     *            The properties.
     * @param string
     *            The key.
     * @param defaultValue
     *            The default value.
     * @return The long value.
     * @throws Exception
     *             If the value is wrong.
     */
    public static long readLongProperty( Properties prop, String string, long defaultValue ) throws Exception {
        long returnValue = defaultValue;
        String propertyValue = prop.getProperty( string );
        if ( !CacheUtilities.isBlank( propertyValue ) ) {
            try {
                returnValue = Long.parseLong( propertyValue );
            } catch ( Exception ex ) {
                String message = "Failed to parse a numeric property name : " + string + " with current value: \"" + propertyValue + "\". The error message is : " + ex.getMessage();
                throw new Exception( message, ex );
            }
        }
        return returnValue;
    }

    private static List<String> readStringListProperty( Properties prop, String propertyName, String separator ) {
        List<String> retVal = new ArrayList<String>();

        String property = prop.getProperty( propertyName );
        if ( !CacheUtilities.isBlank( property ) ) {
            String[] split = property.split( separator );
            for ( int i = 0; i < split.length; i++ ) {
                String currentFile = split[i];
                if ( !CacheUtilities.isBlank( currentFile ) ) {
                    retVal.add( currentFile );
                }
            }
        }

        return retVal;
    }

    /**
     * Read a boolean property.
     * 
     * @param prop
     *            The properties.
     * @param propertyName
     *            The property name.
     * @param defaultValue
     *            The default value.
     * @return The boolean property value.
     */
    public static boolean readBooleanProperty( Properties prop, String propertyName, boolean defaultValue ) {
        boolean retVal = defaultValue;
        String property = prop.getProperty( propertyName );
        if ( !CacheUtilities.isBlank( property ) ) {
            if ( "true".equalsIgnoreCase( property ) ) {
                retVal = true;
            } else if ( "false".equalsIgnoreCase( property ) ) {
                retVal = false;
            } else {
                String message = "Invalid value for property " + propertyName + ". Expected a boolean value of true or false. Actual value : " + property;

                LOGGER.log( Level.INFO, message );
            }
        }
        return retVal;
    }

    /**
     * Run the analysis.
     * 
     * @throws Exception
     *             If anything goes wrong.
     */
    public void runAnalysis() throws Exception {

        long startTime = System.currentTimeMillis();

        setUp();

        clearIfEnabled( clearDatabase );

        loadIfEnabled( loadDataFiles );

        computeIfEnabled( computeMetrics );

        reportResultIfEnabled( reportResult );

        reportStackIfEnabled( reportStack );

        reportExecutionIfEnabled( reportExecution );

        reportTrendIfEnabled( reportTrend );

        reportCallerIfEnabled( reportCaller );
        
        long duration = System.currentTimeMillis() - startTime;
        LOGGER.log( Level.INFO, "Total execution time : " + duration + "ms." );
    }

    private void reportTrendIfEnabled( boolean reportTrend ) throws Exception {
        if ( !reportTrend ) {
            return;
        }
        LOGGER.log( Level.INFO, "Generating trend reports." );

        long startTime = System.currentTimeMillis();

        String trendOutputDir = reportOutputDir + "/trend";
        File makeFolder = new File( trendOutputDir );
        if ( !makeFolder.exists() ) {
            makeFolder.mkdirs();
        }

        String selectStatement = getSelectAllOperationsSql();
        List<List<Object>> operationNameList = executeQuery( selectStatement );

        String htmlText = loadResourceAsString( "com/ibm/commerce/cache/reports/report-trend.html" );

        for ( List<Object> list : operationNameList ) {
            String operationName = (String) list.get( 0 );

            printOperationTrend( operationName, trendOutputDir, htmlText );
        }

        long duration = System.currentTimeMillis() - startTime;
        LOGGER.log( Level.INFO, "Took " + duration + " ms to generate trend for : " + operationNameList.size() + " operations. Output folder : " + makeFolder.getCanonicalPath() );

    }

    private void printOperationTrend( String operationName, String trendOutputDir, String htmlText ) throws Exception {

        String selectQuery = "SELECT (STOPTIME/" + trendTimeIntervalInNano + ") as TIME, "
            + "count(IDENTIFIER) as CALL_COUNT, MIN(DURATION) as MINIMUM_DURATION, AVG(DURATION) as AVERAGE_DURATION, MAX(DURATION) as MAXIMUM_DURATION from METRIC where OPERATIONNAME=? " + "group by (STOPTIME/" + trendTimeIntervalInNano
            + ") order by TIME asc";

        List<List<Object>> result = executeQuery( selectQuery, operationName );

        if ( result.size() == 0 ) {
            return;
        }

        String[] seriesName = new String[] { "time", "call count", "minimum response time", "average response time", "maximum response time" };
        double[] divisor = new double[] { 1, 1, 1000000, 1000000, 1000000 };

        int[] printIndex = new int[] { 0, 2, 4, 3 };

        String responseTimeTrend = printGraphWithIndex( result, seriesName, divisor, printIndex, false );

        int[] printIndexCount = new int[] { 0, 1 };

        String callCountTrend = printGraphWithIndex( result, seriesName, divisor, printIndexCount, true );

        String timeUnit = getTimeIncrementFromDivision( trendTimeIntervalInNano );

        htmlText = htmlText.replace( "%%RESPONSE_TIME_TREND%%", responseTimeTrend );
        htmlText = htmlText.replace( "%%CALL_COUNT_TREND%%", callCountTrend );
        htmlText = htmlText.replace( "%%OPERATION_NAME%%", operationName );
        htmlText = htmlText.replaceAll( "%%TIME_UNIT%%", timeUnit );

        String reportFileName = convertOperationNameToFileName( operationName );
        String outputFileName = trendOutputDir + "/" + reportFileName + ".html";

        printToFile( htmlText, outputFileName );
    }

    /**
     * Get a textual representation of a time unit from it's nanosecond count
     * 
     * @param nanosecondCount the number of nanosecond in the time unit.
     * @return the desired text.
     */
    public static String getTimeIncrementFromDivision( long nanosecondCount ) {
        String timeUnit = "second increment";
        if ( nanosecondCount >= 24l * 60l * 60l * 1000l * 1000l * 1000l ) {
            timeUnit = nanosecondCount / ( 24l * 60l * 60l * 1000l * 1000l * 1000l ) + " day increment";
        } else if ( nanosecondCount >= 60l * 60l * 1000l * 1000l * 1000l ) {
            timeUnit = nanosecondCount / ( 60l * 60l * 1000l * 1000l * 1000l ) + " hour increment";
        } else if ( nanosecondCount >= 60l * 1000l * 1000l * 1000l ) {
            timeUnit = nanosecondCount / ( 60l * 1000l * 1000l * 1000l ) + " minute increment";
        } else if ( nanosecondCount >= 1000l * 1000l * 1000l ) {
            timeUnit = nanosecondCount / ( 1000l * 1000l * 1000l ) + " second increment";
        } else if ( nanosecondCount >= 1000l * 1000l ) {
            timeUnit = nanosecondCount / ( 1000l * 1000l ) + " millisecond increment";
        } else if ( nanosecondCount >= 1000l ) {
            timeUnit = nanosecondCount / 1000l + " microsecond increment";
        } else {
            timeUnit = nanosecondCount + " nanosecond increment";
        }
        return timeUnit;
    }

    private String printGraphWithIndex( List<List<Object>> result, String[] seriesName, double[] divisor, int[] printIndex, boolean padToZero ) {

        double startTime = ( (Number) result.get( 0 ).get( 0 ) ).doubleValue();
        StringBuilder trend = new StringBuilder();
        boolean first = true;
        trend.append( "[" );
        for ( int index = 0; index < printIndex.length; ++index ) {
            if ( first ) {
                first = false;
            } else {
                trend.append( "," );
            }
            trend.append( "'" );
            trend.append( seriesName[printIndex[index]] );
            trend.append( "'" );
        }
        trend.append( "]" );
        trend.append( ",\n" );
        first = true;

        for ( int i = 0; i < result.size(); ++i ) {
            List<Object> list = result.get( i );

            if ( padToZero && i != 0 ) {
                List<Object> prevList = result.get( i - 1 );
                long currentTime = ( (Number) list.get( 0 ) ).longValue();
                long previousTime = ( (Number) prevList.get( 0 ) ).longValue();
                long expectedPreviousTime = currentTime - 1;
                if ( previousTime != expectedPreviousTime && previousTime != expectedPreviousTime - 1 ) {
                    // missing data before data point, insert a zero value
                    insertZeroDataPoint( divisor, printIndex, startTime, trend, list, expectedPreviousTime );
                }
            }

            if ( first ) {
                first = false;
            } else {
                trend.append( ",\n" );
            }

            printTrendDataPoint( divisor, printIndex, startTime, trend, list );

            if ( padToZero && i + 1 < result.size() ) {
                List<Object> nextList = result.get( i + 1 );
                long currentTime = ( (Number) list.get( 0 ) ).longValue();
                long nextTime = ( (Number) nextList.get( 0 ) ).longValue();
                long expectedNextTime = currentTime + 1;
                if ( nextTime != expectedNextTime ) {
                    // missing data after data point, insert a zero value
                    insertZeroDataPoint( divisor, printIndex, startTime, trend, list, expectedNextTime );
                }
            }
        }

        return trend.toString();
    }

    private void insertZeroDataPoint( double[] divisor, int[] printIndex, double startTime, StringBuilder trend, List<Object> list, long expectedTime ) {
        Long zeroValue = Long.valueOf( 0 );
        List<Object> listCopy = new ArrayList<Object>( list );
        for ( int j = 1; j < listCopy.size(); ++j ) {
            listCopy.set( j, zeroValue );
        }
        listCopy.set( 0, Long.valueOf( expectedTime ) );
        trend.append( ",\n" );
        printTrendDataPoint( divisor, printIndex, startTime, trend, listCopy );
    }

    private void printTrendDataPoint( double[] divisor, int[] printIndex, double startTime, StringBuilder trend, List<Object> list ) {
        trend.append( "[" );
        boolean innerFirst = true;
        for ( int index = 0; index < printIndex.length; ++index ) {
            double currentDivisor = divisor[printIndex[index]];
            Number value = (Number) list.get( printIndex[index] );
            double doubleValue = value.doubleValue();
            if ( innerFirst ) {
                innerFirst = false;
                doubleValue -= startTime;
            } else {
                trend.append( "," );
            }
            trend.append( doubleValue / currentDivisor );

        }
        trend.append( "]" );
    }

    protected void printDojoTrend( List<List<Object>> result, String[] seriesName, double[] divisor ) {
        StringBuilder trend = new StringBuilder();
        for ( int index = 1; index <= 4; ++index ) {
            trend.append( ".addSeries(\"" );
            trend.append( seriesName[index] );
            trend.append( "\", [" );
            boolean first = true;
            double currentDivisor = divisor[index];
            for ( List<Object> list : result ) {
                String time = list.get( 0 ).toString();
                Number value = (Number) list.get( index );
                if ( first ) {
                    first = false;
                } else {
                    trend.append( "," );
                }
                trend.append( "{x:" );
                trend.append( time );
                trend.append( ",y:" );
                double doubleValue = value.doubleValue();
                trend.append( doubleValue / currentDivisor );
                trend.append( "}" );

            }
            trend.append( "])\n" );
        }

        System.out.println( trend.toString() );
    }

    private void reportExecutionIfEnabled( boolean reportExecutionEnabled ) throws Exception {

        if ( reportExecutionEnabled ) {
            LOGGER.log( Level.INFO, "Generating execution reports." );

            long startTime = System.currentTimeMillis();
            File makeFolder = new File( executionOutputDir );
            if ( !makeFolder.exists() ) {
                makeFolder.mkdirs();
            }

            String selectStatement = "SELECT IDENTIFIER, OPERATIONNAME from METRIC where PARENTIDENTIFIER = 0 order by DURATION desc fetch first " + entryCountLimit + " rows only";
            List<List<Object>> metricIdList = executeQuery( selectStatement );

            for ( List<Object> list : metricIdList ) {
                printStackByOperationId( list.get( 0 ), list.get( 1 ) );
            }

            long duration = System.currentTimeMillis() - startTime;
            LOGGER.log( Level.INFO, "Took " + duration + " ms to generate report for : " + metricIdList.size() + " executions. Output folder : " + makeFolder.getCanonicalPath() );
        }
    }

    private void printStackByOperationId( Object id, Object name ) throws Exception {
        String selectStatement = "SELECT " + STACK_STEP_FIELD_LIST + " from METRIC where IDENTIFIER=?";
        StackStep stackStepFromSql = getStackStepFromSql( selectStatement, id );

        String operationName = id.toString() + "-" + name.toString();

        printStackXML( operationName, executionOutputDir, stackStepFromSql );
    }

    private void printStackXML( String operationName, String baseFolder, StackStep averageStack ) throws IOException {

        String reportFileName = convertOperationNameToFileName( operationName );

        reportFileName = baseFolder + "/" + reportFileName + ".xml";
        
        Writer report = null;
		try {
            report = createReportWriter(reportFileName);
            
            insertXmlUtf8EncodingHeader(report);
            
			report.append("<operation name=\"");
			report.append(StringEscapeUtils.escapeXml(operationName));
			report.append("\">").append(CacheUtilities.LINE_SEPARATOR)
					.append(CacheUtilities.LINE_SEPARATOR);

			printStackToReportXML(averageStack, report);
			report.append(CacheUtilities.LINE_SEPARATOR);

			report.append("</operation>").append(CacheUtilities.LINE_SEPARATOR);

		} finally {
			CacheUtilities.closeQuietly(report);
		}
    }

    private void reportCallerIfEnabled( boolean setReportCaller ) throws Exception {
        if ( !setReportCaller ) {
            return;
        }

        File rootDir = new File( callerOutputDir );
        if ( !rootDir.exists() ) {
            rootDir.mkdirs();
        }

        LOGGER.log( Level.INFO, "Generating caller reports." );

        long startTime = System.currentTimeMillis();

        File makeFolder = new File( callerOutputDir );
        if ( !makeFolder.exists() ) {
            makeFolder.mkdirs();
        }

        String selectStatement = getSelectAllOperationsSql();
        List<List<Object>> operationNameList = executeQuery( selectStatement );

        // printOperationCaller(
        // "SELECT (1 field) FROM CATALOG WHERE CATALOG.CATALOG_ID=?",
        // callerOutputDir);
        for ( List<Object> list : operationNameList ) {
            String operationName = (String) list.get( 0 );

            printOperationCaller( operationName, callerOutputDir );
        }

        long duration = System.currentTimeMillis() - startTime;
        if ( LOGGER.isLoggable( Level.INFO ) ) {
            String message = "Took " + duration + " ms to generate caller report for : " + operationNameList.size() + " operations. Output folder : " + rootDir.getCanonicalPath();
            LOGGER.log( Level.INFO, message );
        }

    }

    private String getSelectAllOperationsSql() {
        return SELECT_ALL_OPERATIONS_SQL + " fetch first " + entryCountLimit + " rows only";
    }

    private void printOperationCaller( String operationName, String outputDir ) throws Exception {
        long startTime = System.currentTimeMillis();

        CallerStep caller = getCallerHierarchy( operationName );

        printCallerStepsXML( caller, outputDir );

        long duration = System.currentTimeMillis() - startTime;
        LOGGER.log( Level.FINE, "Took " + duration + " ms to generate caller report for operation : " + operationName );
    }

    private void printCallerStepsXML( CallerStep caller, String baseFolder ) throws IOException {

        String operationName = caller.getOperationName();

        String reportFileName = convertOperationNameToFileName( operationName );

        reportFileName = baseFolder + "/" + reportFileName + ".xml";

		Writer report = null;
		try {
            report = createReportWriter(reportFileName);

			insertXmlUtf8EncodingHeader(report);
			printCallerStepXMLToString(caller, report, 0);
		} finally {
			CacheUtilities.closeQuietly(report);
		}
    }

    private void printCallerStepXMLToString( CallerStep caller, Writer report, int increment ) throws IOException {
        printIndentationXML( report, increment );

        report.append( "<caller" );
        report.append( " call-count=\"" );
        report.append( Long.toString(caller.getCallCount() ));
        report.append( "\" average-call-count=\"" );
        report.append( String.format( "%.2f", caller.getAverageCallCount() ) );
        report.append( "\" operation-name=\"" );
        // $ANALYSIS-IGNORE
        String operationName = StringEscapeUtils.escapeXml( caller.getOperationName() );
        report.append( operationName );

        report.append( "\" average-duration-ms=\"" );
        report.append( Long.toString(caller.getAverageDuration() / 1000000) );
        report.append( "\" average-size=\"" );
        report.append( Long.toString(caller.getAverageSize()) );
        report.append( "\" unique-caller-count=\"" );
        report.append( Long.toString(caller.getUniqueCallerCount() ) );
        report.append( "\" parent-count=\"" );
        report.append( Float.toString(caller.getParentCount()) );

        int childIncrement = increment + 1;

        List<CallerStep> childStepList = caller.childStepList;

        if ( childStepList.size() > 0 ) {
            report.append( "\">" ).append( CacheUtilities.LINE_SEPARATOR );

            for ( CallerStep childStep : childStepList ) {

                printCallerStepXMLToString( childStep, report, childIncrement );
            }

            printIndentationXML( report, increment );
            report.append( "</caller>" ).append( CacheUtilities.LINE_SEPARATOR );
        } else {
            report.append( "\"/>" ).append( CacheUtilities.LINE_SEPARATOR );
        }
    }

    private CallerStep getCallerHierarchy( String operationName ) throws Exception {

        // fetch the first caller information
        String rootSelect = "select " + callerSelectedFields + " from METRIC where OPERATIONNAME = ?" + " group by OPERATIONNAME";

        List<List<Object>> rootResult = executeQuery( rootSelect, operationName );

        CallerStep rootStep = new CallerStep();

        if ( rootResult.size() != 1 ) {
            LOGGER.severe( "******* Unexpected root result size for caller stack " + rootResult.size() + ", skipping analysis" );

        } else {

            rootStep.fieldValues = rootResult.get( 0 );
            boolean useTempTable = true;

            if ( useTempTable ) {
                fetchChildCallerStepsTempTables( rootStep, 0 );
            } else {
                LinkedList<String> parameterValueList = new LinkedList<String>();
                fetchChildCallerStepsAppendingSql( rootStep, null, parameterValueList );
            }

        }
        return rootStep;
    }

    private void fetchChildCallerStepsTempTables( CallerStep currentStep, int depth ) throws Exception {

        String opName = currentStep.getOperationName();

        if ( depth >= MetricCompiler.MAX_CALLER_DEPTH ) {
            // too deep
            return;
        }

        int previousDepth = depth - 1;

        // clear the table
        executeUpdate( "delete from caller" + depth );

        String idMatchWhereClause = "IDENTIFIER in (SELECT IDENTIFIER from caller" + previousDepth + ") and ";
        if ( depth == 0 ) {
            idMatchWhereClause = "";
        }

        String insertStatement = "INSERT INTO caller" + depth + "(IDENTIFIER) select PARENTIDENTIFIER from METRIC where " + idMatchWhereClause + "OPERATIONNAME = ?";

        executeUpdate( insertStatement, opName );

        // we now have the list of all our callers for a specific operation

        String select = "select " + callerSelectedFields + " from METRIC INNER JOIN caller" + depth + " ON METRIC.IDENTIFIER=caller" + depth + ".IDENTIFIER" + " GROUP BY METRIC.OPERATIONNAME";
        // String select = "select "
        // + callerSelectedFields
        // + " from METRIC where IDENTIFIER in (SELECT IDENTIFIER from caller"
        // + depth + ") GROUP BY OPERATIONNAME";
        int childDepth = depth + 1;

        List<List<Object>> callerList = executeQuery( select );

        for ( List<Object> list : callerList ) {
            CallerStep caller = new CallerStep();
            caller.fieldValues = list;
            currentStep.childStepList.add( caller );

            fetchChildCallerStepsTempTables( caller, childDepth );
        }
    }

    private void fetchChildCallerStepsAppendingSql( CallerStep currentStep, String subSelect, LinkedList<String> parameterValueList ) throws Exception {

        parameterValueList.addFirst( currentStep.getOperationName() );

        String whereClause = "IDENTIFIER in (select PARENTIDENTIFIER from METRIC where ";
        if ( subSelect != null ) {
            // $ANALYSIS-IGNORE
            whereClause += " ( " + subSelect + " ) and ";
        }
        // $ANALYSIS-IGNORE
        whereClause += "(OPERATIONNAME = ?) )";

        String regroupClause = "GROUP BY OPERATIONNAME";
        String select = "select " + callerSelectedFields + " from METRIC where " + whereClause + " " + regroupClause;

        List<List<Object>> callerList = executeQuery( select, parameterValueList.toArray() );
        for ( List<Object> list : callerList ) {
            CallerStep caller = new CallerStep();
            caller.fieldValues = list;
            currentStep.childStepList.add( caller );

            fetchChildCallerStepsAppendingSql( caller, whereClause, parameterValueList );
        }

        parameterValueList.removeFirst();
    }

    private void reportStackIfEnabled( boolean reportStack ) throws Exception {
        if ( reportStack ) {
            printOperationCallStacks( stackOutputDir );
        }
    }

    private void reportResultIfEnabled( boolean reportResult ) throws Exception {
        if ( reportResult ) {

            File rootDir = new File( reportOutputDir );
            if ( !rootDir.exists() ) {
                rootDir.mkdirs();
            }

            LOGGER.log( Level.INFO, "Generating performance reports start." );
            String selectStatement = null;

            // LOGGER.log(Level.FINE,
            // "============ CACHE ALLOCATION PER OPERATION ============");
            // selectStatement =
            // "SELECT OPERATIONNAME,CACHEALLOCATION from CACHEALLOCATION order by OPERATIONNAME";
            // printToFile(executeQueryAndPrint(selectStatement),
            // reportOutputDir
            // + "/report-cache-allocation.csv");
            //
            // LOGGER.log(Level.FINE,
            // "============ SORT BY SECONDSAVEDPERBYTE ============");
            // selectStatement =
            // "SELECT SECONDSAVEDPERBYTE*1000 as MSSAVEDPERBYTE,OPERATIONNAME,SUMAVERAGERESULTSIZE,AVERAGERESULTSIZE,AVERAGEDURATION/1000000 as AVERAGE_MS_DURATION,SUMCACHEHITCOUNT,SUMCALLCOUNT from CACHEALLOCATION order by SECONDSAVEDPERBYTE DESC";
            // printToFile(executeQueryAndPrint(selectStatement),
            // reportOutputDir
            // + "/report-by-second-saved-per-byte.csv");
            //
            // LOGGER.log(Level.FINE,
            // "============ SORT BY AVERAGEDURATION ============");
            // selectStatement =
            // "SELECT AVERAGEDURATION/1000000 as AVERAGE_MS_DURATION,OPERATIONNAME,SUMAVERAGERESULTSIZE,AVERAGERESULTSIZE,SECONDSAVEDPERBYTE*1000 as MSSAVEDPERBYTE,SUMCACHEHITCOUNT,SUMCALLCOUNT from CACHEALLOCATION order by AVERAGEDURATION DESC";
            // printToFile(executeQueryAndPrint(selectStatement),
            // reportOutputDir
            // + "/report-average-duration.csv");
            //
            // LOGGER.log(Level.FINE,
            // "============ SORT BY SUMAVERAGERESULTSIZE ============");
            // selectStatement =
            // "SELECT SUMAVERAGERESULTSIZE,OPERATIONNAME,AVERAGERESULTSIZE,AVERAGEDURATION/1000000 as AVERAGE_MS_DURATION,SECONDSAVEDPERBYTE*1000 as MSSAVEDPERBYTE,SUMCACHEHITCOUNT,SUMCALLCOUNT from CACHEALLOCATION order by SUMAVERAGERESULTSIZE DESC";
            // printToFile(executeQueryAndPrint(selectStatement),
            // reportOutputDir
            // + "/report-max-cache-size.csv");
            //
            // LOGGER.log(Level.FINE,
            // "============ SORT BY AVERAGERESULTSIZE ============");
            // selectStatement =
            // "SELECT AVERAGERESULTSIZE,OPERATIONNAME,SUMAVERAGERESULTSIZE,AVERAGEDURATION/1000000 as AVERAGE_MS_DURATION,SECONDSAVEDPERBYTE*1000 as MSSAVEDPERBYTE,SUMCACHEHITCOUNT,SUMCALLCOUNT from CACHEALLOCATION order by AVERAGERESULTSIZE DESC";
            // printToFile(executeQueryAndPrint(selectStatement),
            // reportOutputDir
            // + "/report-average-cache-entry-size.csv");
            //
            // LOGGER.log(Level.FINE,
            // "============ SORT BY SUMCACHEHITCOUNT ============");
            // selectStatement =
            // "SELECT SUMCACHEHITCOUNT,OPERATIONNAME,SUMCALLCOUNT,SUMAVERAGERESULTSIZE,SECONDSAVEDPERBYTE*1000 as MSSAVEDPERBYTE,AVERAGEDURATION/1000000 as AVERAGE_MS_DURATION from CACHEALLOCATION order by SUMCACHEHITCOUNT DESC";
            // printToFile(executeQueryAndPrint(selectStatement),
            // reportOutputDir
            // + "/report-by-cache-hit-count.csv");

            LOGGER.log( Level.FINE, "============ OPERATIONS ============" );
            selectStatement = "SELECT " + "OPERATIONNAME as operation_name," // column
                + "cast(AVERAGEDURATION/1000000 as DECIMAL(20,3)) as average_call_duration_in_MS," // column
                + "cast(AVERAGERESULTSIZE as DECIMAL(20,0)) as average_result_size_in_bytes," // column
                + "cast(AVERAGEDURATION*SUMCALLCOUNT/1000000 as DECIMAL(20,0)) as cumulative_execution_time_MS," // column
                + "cast(SUMCALLCOUNT as DECIMAL(20,0)) as call_count" // column
                + " from CACHEALLOCATION order by AVERAGEDURATION DESC FETCH FIRST " + entryCountLimit + " ROWS ONLY";

            String operationsCsv = executeQueryAndPrint( selectStatement );
            printToFile( operationsCsv, reportOutputDir + "/report-operations.csv" );

            LOGGER.log( Level.FINE, "============ OPERATION CACHE ============" );
            selectStatement = "SELECT "
                + "OPERATIONNAME as operation_name," // column
                + "cast (SECONDSAVEDPERBYTE*1000  AS decimal(20,3))as MS_saved_per_byte," // column
                + "cast(CACHEALLOCATION as DECIMAL(20,0)) as cache_allocation_in_bytes," // column
                + "cast(AVERAGEDURATION/1000000 as DECIMAL(20,3)) as average_call_duration_in_MS," // column
                + "cast(AVERAGEDURATIONHIT/1000000 as DECIMAL(20,3)) as average_cache_hit_duration_in_MS," // column
                + "cast(AVERAGEDURATIONMISS/1000000 as DECIMAL(20,3)) as average_cache_miss_duration_in_MS," // column
                + "cast(AVERAGERESULTSIZE as DECIMAL(20,0)) as average_result_size_in_bytes," // column
                + "cast(AVERAGEDURATION*SUMCALLCOUNT/1000000 as DECIMAL(20,3)) as cumulative_execution_time_MS," // column
                + "cast(SUMAVERAGERESULTSIZE as DECIMAL(20,0)) as max_cache_allocation_size_in_bytes," // column
                + "cast(SUMCACHEHITCOUNT*AVERAGEDURATIONMISS/1000000 as DECIMAL(20,3)) as max_cache_benefit_MS," // column
                + "cast(KEYVALUECOUNT as DECIMAL(20,0)) as unique_cache_entry_count," // column
                + "cast(SUMCACHEHITCOUNT as DECIMAL(20,0)) as max_theoric_cache_hit_count," // column
                + "cast(SUMREALCACHEHITCOUNT as DECIMAL(20,0)) as real_cache_hit_count," // column
                + "cast(SUMREALCACHEACTCOUNT as DECIMAL(20,0)) as real_cache_enabled_count," // column
                + "cast (100*SUMREALCACHEACTCOUNT/(0.5 * ((SUMCALLCOUNT+1)+ABS(SUMCALLCOUNT-1))) AS decimal(20,2))as cache_enabled_call_percentage," // column
                + "cast (100*SUMCACHEHITCOUNT/(0.5 * ((SUMCALLCOUNT+1)+ABS(SUMCALLCOUNT-1))) AS decimal(20,2))as max_theoric_cache_hit_percentage," // column
                + "cast (100*SUMREALCACHEHITCOUNT/(0.5 * ((SUMREALCACHEACTCOUNT+1)+ABS(SUMREALCACHEACTCOUNT-1))) AS decimal(20,2))as real_cache_hit_percentage," // column
                + "cast (100*(100*SUMREALCACHEHITCOUNT/(0.5 * ((SUMREALCACHEACTCOUNT+1)+ABS(SUMREALCACHEACTCOUNT-1))))/(cast(100* (0.5 * ((SUMCACHEHITCOUNT+1)+ABS(SUMCACHEHITCOUNT-1))) as FLOAT ) /(0.5 * ((SUMCALLCOUNT+1)+ABS(SUMCALLCOUNT-1)))) AS decimal(20,2))as cache_effectiveness_vs_theory_percentage," // column
                + "cast(SUMCALLCOUNT as DECIMAL(20,0)) as call_count" // column
                + " from CACHEALLOCATION order by AVERAGEDURATION DESC FETCH FIRST " + entryCountLimit + " ROWS ONLY";

            operationsCsv = executeQueryAndPrint( selectStatement );
            printToFile( operationsCsv, reportOutputDir + "/report-operation-cache.csv" );

            String operationsHtml = buildHtmlReportFromCsvAndTemplate( operationsCsv, "com/ibm/commerce/cache/reports/report-operations.html" );
            printToFile( operationsHtml, reportOutputDir + "/report-operations.html" );

            LOGGER.log( Level.FINE, "============ EXECUTIONS ============" );
            selectStatement = "SELECT OPERATIONNAME as operation_name, DURATION/1000000 as duration_MS, STARTIME/1000000 as start_time_MS, STOPTIME/1000000 as stop_time_MS, RESULTSIZE as result_size, KEYVALUE as key_value, identifier "
                + "from METRIC where PARENTIDENTIFIER = 0 order by DURATION desc FETCH FIRST " + entryCountLimit + " ROWS ONLY";
            operationsCsv = executeQueryAndPrint( selectStatement );
            printToFile( operationsCsv, reportOutputDir + "/report-execution.csv" );

            String executionHtml = buildHtmlReportFromCsvAndTemplate( operationsCsv, "com/ibm/commerce/cache/reports/report-execution.html" );
            printToFile( executionHtml, reportOutputDir + "/report-execution.html" );

            LOGGER.log( Level.INFO, "Generating performance reports finished. Output folder : " + rootDir.getCanonicalPath() );

            try {
                Desktop d = Desktop.getDesktop();
                d.open( new File( reportOutputDir + "/report-operations.html" ) );
            } catch ( Exception ex ) {
                LOGGER.log( Level.FINE, "Failed to open " + reportOutputDir + "/report-operations.html" + " on the desktop. " + ex.getMessage(), ex );
            }
        }
    }

    private String buildHtmlReportFromCsvAndTemplate( String csvContent, String htmlResourceName ) {
        String htmlText = loadResourceAsString( htmlResourceName );
        csvContent = formatCsvToHtmlString(csvContent);
        htmlText = htmlText.replace( "%%CSV_DATA%%", csvContent );
        return htmlText;
    }

	public static String formatCsvToHtmlString(String csvContent) {
		csvContent = csvContent.replaceAll( "\"", "\\\\\"" );
        csvContent = csvContent.replaceAll( "[\\n\\r]+", "\\\\n\"+\n\"" );
		return csvContent;
	}

    private String loadResourceAsString( String headerResourceName ) {
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        InputStream resourceAsStream = contextClassLoader.getResourceAsStream( headerResourceName );
        Scanner scanner = new Scanner( resourceAsStream, "UTF-8" );
        String headerText = scanner.useDelimiter( "\\A" ).next();
        scanner.close();
        return headerText;
    }

    private void computeIfEnabled( boolean compute ) throws SQLException {
        if ( compute ) {
            LOGGER.log( Level.INFO, "Computing operation metrics started." );

            MetricCompiler.clearUniqueCacheHitsTable( dataSource );
            MetricCompiler.clearCacheHitStepsTable( dataSource );
            MetricCompiler.clearCacheAllocationTable( dataSource );
            MetricCompiler.aggregateCacheHits( dataSource );
            MetricCompiler.regroupCacheHitsInSteps( dataSource, cacheHitSteps );
            MetricCompiler.allocateCacheSpace( dataSource, allocatedCacheSize );

            LOGGER.log( Level.INFO, "Computing operation metrics finished." );
        }
    }

    private void loadIfEnabled( boolean load ) throws Exception {
        if ( load ) {
            LOGGER.log( Level.INFO, "Loading metric data." );
            initializeGatherer();

            MetricFileLoader loader = new MetricFileLoader();
            loader.setGatherer( gatherer );
            loader.setParseJdbcTraceLogs( parseWasJdbcTrace );
            loader.setTimestampFormat( logTimestampFormat );
            loader.setJdbcTraceRegularExpression( logJdbcTraceRegularExpression );
            loader.setEntryTraceRegularExpression( logEntryTraceRegularExpression );
            loader.setExitTraceRegularExpression( logExitTraceRegularExpression );

            for ( String currentFile : fileToLoadList ) {
                LOGGER.log( Level.INFO, "Loading data file : " + currentFile );
                loader.gatherMetricsFromFile( currentFile );
            }

            LogMetricFileLoader logLoader = new LogMetricFileLoader();
            logLoader.setCharsetName( logFileCharsetName );
            logLoader.setGatherer( gatherer );
            logLoader.setParseJdbcTraceLogs( parseWasJdbcTrace );
            logLoader.setTimestampFormat( logTimestampFormat );
            logLoader.setJdbcTraceRegularExpression( logJdbcTraceRegularExpression );
            logLoader.setEntryTraceRegularExpression( logEntryTraceRegularExpression );
            logLoader.setExitTraceRegularExpression( logExitTraceRegularExpression );

            for ( String currentFile : logFileToLoadList ) {
                LOGGER.log( Level.INFO, "Loading data file : " + currentFile );
                logLoader.gatherMetricsFromFile( currentFile );
            }

            int rowCount = getMetricRowCount();
            LOGGER.log( Level.INFO, "imported number of metrics : " + rowCount );
        }
    }

    private void clearIfEnabled( boolean clear ) throws SQLException {
        if ( clear ) {
            MetricCompiler.clearTables( dataSource );
        }
    }

    private void initializeGatherer() {
        gatherer.setPendingFlushSize( 1000 );
        gatherer.setMaximumDatabaseBatchSize( 1000 );
        gatherer.setMaximumPendingSize( 1000000 );
    }

    private void printOperationCallStacks( String baseFolder ) throws Exception {
        LOGGER.log( Level.INFO, "Generating stack reports." );
        long startTime = System.currentTimeMillis();

        File makeFolder = new File( baseFolder );
        if ( !makeFolder.exists() ) {
            makeFolder.mkdirs();
        }

        String selectStatement = getSelectAllOperationsSql();
        List<List<Object>> operationNameList = executeQuery( selectStatement );

        for ( List<Object> list : operationNameList ) {
            String operationName = (String) list.get( 0 );

            printOperationCallStacks( operationName, baseFolder );
        }

        long duration = System.currentTimeMillis() - startTime;
        LOGGER.log( Level.INFO, "Took " + duration + " ms to generate report for : " + operationNameList.size() + " operations. Output folder : " + makeFolder.getCanonicalPath() );
    }

    private static class StackStep {
        private List<Object> fieldValues = null;

        private List<StackStep> childStepList = new ArrayList<StackStep>();

        /**
         * 
         * @return identifier
         */
        public Number getIdentifier() {
            return (Number) fieldValues.get( 0 );
        }

        /**
         * 
         * @return parent identifier
         */
        public Number getParentIdentifier() {
            return (Number) fieldValues.get( 1 );
        }

        /**
         * 
         * @return operation name
         */
        public String getOperationName() {
            return (String) fieldValues.get( 2 );
        }

        /**
         * 
         * @return start time
         */
        public long getStartTime() {
            return ( (Number) fieldValues.get( 3 ) ).longValue();
        }

        /**
         * 
         * @return end time
         */
        public long getEndTime() {
            return ( (Number) fieldValues.get( 4 ) ).longValue();
        }

        /**
         * 
         * @return duration
         */
        public long getDuration() {
            return ( (Number) fieldValues.get( 5 ) ).longValue();
        }

        /**
         * 
         * @return result size
         */
        public int getResultSize() {
            return ( (Number) fieldValues.get( 6 ) ).intValue();
        }

        /**
         * 
         * @return is result from cache
         */
        public boolean isFromCache() {
            return getBoolean( fieldValues.get( 7 ) );
        }

        private boolean getBoolean( Object object ) {
            if ( "1".equals( object ) ) {
                return true;
            }
            return false;
        }

        /**
         * 
         * @return is cache enabled
         */
        public boolean isCacheEnabled() {
            return getBoolean( fieldValues.get( 8 ) );
        }

        /**
         * 
         * @return the key value
         */
        public String getKeyValue() {
            return (String) fieldValues.get( 9 );
        }
    }

    private static class CallerStep {
        private List<Object> fieldValues = null;

        private List<CallerStep> childStepList = new ArrayList<CallerStep>();

        /**
         * 
         * @return opeartion name
         */
        public String getOperationName() {
            return (String) fieldValues.get( 0 );
        }

        /**
         * 
         * @return call count
         */
        public long getCallCount() {
            return ( (Number) fieldValues.get( 1 ) ).longValue();
        }

        /**
         * 
         * @return average duration
         */
        public long getAverageDuration() {
            return ( (Number) fieldValues.get( 2 ) ).longValue();
        }

        /**
         * 
         * @return average size
         */
        public long getAverageSize() {
            return ( (Number) fieldValues.get( 3 ) ).longValue();
        }

        /**
         * 
         * @return unique caller count
         */
        public long getUniqueCallerCount() {
            return ( (Number) fieldValues.get( 4 ) ).longValue();
        }

        /**
         * 
         * @return average call count
         */
        public float getAverageCallCount() {
            return ( (Number) fieldValues.get( 5 ) ).floatValue();
        }

        /**
         * 
         * @return parent count
         */
        public float getParentCount() {
            return ( (Number) fieldValues.get( 6 ) ).longValue();
        }
    }

    private String convertOperationNameToFileName( String operationName ) {
        String reportFileName = operationName.replaceAll( "[^A-Za-z0-9 ]", "." );

        // truncate the report name
        if ( reportFileName.length() > 150 ) {
            reportFileName = reportFileName.substring( 0, 150 );
        }
        return reportFileName;
    }

    private void printOperationCallStacks( String operationName, String baseFolder ) throws Exception {

        long startTime = System.currentTimeMillis();

      
        String reportFileName = convertOperationNameToFileName( operationName );

        reportFileName = baseFolder + "/" + reportFileName + ".xml";

        Writer report = null;
        StackStep stack = null;
        try {
            report = createReportWriter(reportFileName);
	        insertXmlUtf8EncodingHeader(report);
	        report.append( "<operation name=\"" );
	        report.append( StringEscapeUtils.escapeXml(operationName) );
	        report.append( "\">" ).append( CacheUtilities.LINE_SEPARATOR ).append( CacheUtilities.LINE_SEPARATOR );
	
	        stack = getFastestStack( operationName );
	        Number fastIdentifier = stack.getIdentifier();
	        report.append( " <fastest-stack>" ).append( CacheUtilities.LINE_SEPARATOR );
	        printStackToReportXML( stack, report );
	        report.append( " </fastest-stack>" ).append( CacheUtilities.LINE_SEPARATOR ).append( CacheUtilities.LINE_SEPARATOR );
	
	        stack = getAverageStack( operationName );
	        Number averageIdentifier = stack.getIdentifier();
	        report.append( " <average-stack>" ).append( CacheUtilities.LINE_SEPARATOR );
	        if(averageIdentifier.equals(fastIdentifier)) {
	        	report.append( "Identical to the fastest stack." ).append( CacheUtilities.LINE_SEPARATOR );
	        } else {
	        	printStackToReportXML( stack, report );
	        }
	        report.append( " </average-stack>" ).append( CacheUtilities.LINE_SEPARATOR ).append( CacheUtilities.LINE_SEPARATOR );
	
	        stack = getSlowestStack( operationName );
	        Number slowIdentifier = stack.getIdentifier();
	        report.append( " <slowest-stack>" ).append( CacheUtilities.LINE_SEPARATOR );
	        if(slowIdentifier.equals(averageIdentifier)) {
	        	report.append( "Identical to the average stack." ).append( CacheUtilities.LINE_SEPARATOR );
	        } else {
	        	printStackToReportXML( stack, report );
	        }
	        report.append( " </slowest-stack>" ).append( CacheUtilities.LINE_SEPARATOR ).append( CacheUtilities.LINE_SEPARATOR );
	
	        report.append( "</operation>" ).append( CacheUtilities.LINE_SEPARATOR );
        }
        finally {
        	CacheUtilities.closeQuietly(report);
        }
        

        long duration = System.currentTimeMillis() - startTime;
        LOGGER.log( Level.FINE, "Took " + duration + " ms to generate stack report for : " + operationName );
    }

    private Writer createReportWriter(String reportFileName) throws UnsupportedEncodingException, FileNotFoundException
    {
        Writer report;
        report = new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(reportFileName) ), "UTF-8");
        return report;
    }

    private void insertXmlUtf8EncodingHeader(Writer report) throws IOException
    {
        report.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>").append( CacheUtilities.LINE_SEPARATOR );
    }

    /**
     * printSlowAverageFastStack in text format using indentation.
     * 
     * @param operationName
     *            The operation name.
     * @param baseFolder
     *            The base report folder.
     * @param fastestStack
     *            the fastest stack.
     * @param averageStack
     *            the average stack.
     * @param slowestStack
     *            the slowest stack.
     */
    protected void printSlowAverageFastStack( String operationName, String baseFolder, StackStep fastestStack, StackStep averageStack, StackStep slowestStack ) {
        StringBuilder report = new StringBuilder();

        String reportFileName = convertOperationNameToFileName( operationName );

        reportFileName = baseFolder + "/" + reportFileName + ".txt";

        report.append( "Operation : " );
        report.append( operationName );
        report.append( CacheUtilities.LINE_SEPARATOR ).append( CacheUtilities.LINE_SEPARATOR );
        report.append( "Fastest Stack :" ).append( CacheUtilities.LINE_SEPARATOR );
        printStackToReport( fastestStack, report );
        report.append( CacheUtilities.LINE_SEPARATOR ).append( CacheUtilities.LINE_SEPARATOR ).append( "Average Stack :" ).append( CacheUtilities.LINE_SEPARATOR );
        printStackToReport( averageStack, report );
        report.append( CacheUtilities.LINE_SEPARATOR ).append( CacheUtilities.LINE_SEPARATOR ).append( "Slowest Stack :" ).append( CacheUtilities.LINE_SEPARATOR );
        printStackToReport( slowestStack, report );

        printToFile( report.toString(), reportFileName );

    }

    private void printStackToReport( StackStep stack, StringBuilder report ) {
        int increment = 1;
        printStackToReportWithIncrement( stack, report, increment );
    }

    private void printStackToReportXML( StackStep stack, Writer report ) throws IOException {
        int increment = 2;
        printStackToReportWithIncrementXML( stack, report, increment );
    }

    private static final long TIME_PRECISION_DIVISION = 1000000;

    private String jdbcDriver;

    private String jdbcUrl;

    private String callerSelectedFields = "METRIC.OPERATIONNAME, " + "count(METRIC.IDENTIFIER) as CALLCOUNT, " + "avg(METRIC.DURATION) as AVERAGEDURATION, " + "avg(METRIC.RESULTSIZE) as AVERAGESIZE, "
        + "count(DISTINCT(METRIC.KEYVALUE)) as UNIQUECALLER, " + "cast( count(METRIC.IDENTIFIER) as float)/count(DISTINCT(METRIC.IDENTIFIER)) as AVERAGECALLCOUNT, " + "count(DISTINCT(METRIC.IDENTIFIER)) as PARENTCOUNT";

    private long[] cacheHitSteps = new long[] {};

    private long allocatedCacheSize;

    private boolean parseWasJdbcTrace;

    private String logTimestampFormat;

    private String logJdbcTraceRegularExpression;

    private String logEntryTraceRegularExpression;

    private String logExitTraceRegularExpression;

    private long trendTimeIntervalInNano;

    private void printStackToReportWithIncrementXML( StackStep stack, Writer report, int increment ) throws IOException {
        printIndentationXML( report, increment );

        report.append( "<stack duration=\"" );
        report.append( Long.toString(stack.getDuration() / TIME_PRECISION_DIVISION ));
        report.append( "\" name=\"" );
        // $ANALYSIS-IGNORE
        String operationName = StringEscapeUtils.escapeXml( stack.getOperationName() );
        report.append( operationName );

        report.append( "\" identifier=\"" );
        report.append( stack.getIdentifier().toString() );
        report.append( "\" parent-identifier=\"" );
        report.append( stack.getParentIdentifier().toString() );
        report.append( "\" start-time-nano=\"" );
        report.append( Long.toString(stack.getStartTime()) );
        report.append( "\" end-time-nano=\"" );
        report.append( Long.toString(stack.getEndTime()) );
        report.append( "\" duration-nano=\"" );
        report.append( Long.toString(stack.getDuration()) );
        report.append( "\" result-size=\"" );
        report.append( Long.toString(stack.getResultSize()) );
        report.append( "\" cache-enabled=\"" );
        report.append( Boolean.toString(stack.isCacheEnabled() ));
        report.append( "\" from-cache=\"" );
        report.append(  Boolean.toString(stack.isFromCache() ));
        report.append( "\" unique-key=\"" );
        // $ANALYSIS-IGNORE
        report.append( StringEscapeUtils.escapeXml( stack.getKeyValue() ) );

        int childIncrement = increment + 1;

        List<StackStep> childStepList = stack.childStepList;

        if ( childStepList.size() > 0 ) {
            report.append( "\">" ).append( CacheUtilities.LINE_SEPARATOR );

            long currentStartTime = stack.getStartTime();

            for ( StackStep childStep : childStepList ) {

                long startTime = childStep.getStartTime();
                long delta = ( startTime - currentStartTime ) / TIME_PRECISION_DIVISION;
                currentStartTime = childStep.getEndTime();
                printDeltaXML( childIncrement, delta, report );
                printStackToReportWithIncrementXML( childStep, report, childIncrement );
            }

            // print the last delta
            long delta = ( stack.getEndTime() - currentStartTime ) / TIME_PRECISION_DIVISION;
            printDeltaXML( childIncrement, delta, report );

            printIndentationXML( report, increment );
            report.append( "</stack>" ).append( CacheUtilities.LINE_SEPARATOR );
        } else {
            report.append( "\"/>" ).append( CacheUtilities.LINE_SEPARATOR );
        }
    }

    private void printStackToReportWithIncrement( StackStep stack, StringBuilder report, int increment ) {
        printIndentation( report, increment );

        report.append( stack.getDuration() / TIME_PRECISION_DIVISION );
        report.append( " " );
        report.append( stack.getOperationName() );
        report.append( " " );
        report.append( stack.fieldValues );
        report.append( "" ).append( CacheUtilities.LINE_SEPARATOR );

        int childIncrement = increment + 1;

        List<StackStep> childStepList = stack.childStepList;

        if ( childStepList.size() > 0 ) {
            long currentStartTime = stack.getStartTime();

            for ( StackStep childStep : childStepList ) {

                long startTime = childStep.getStartTime();
                long delta = ( startTime - currentStartTime ) / TIME_PRECISION_DIVISION;
                currentStartTime = childStep.getEndTime();
                printDelta( childIncrement, delta, report );
                printStackToReportWithIncrement( childStep, report, childIncrement );
            }

            // print the last delta
            long delta = ( stack.getEndTime() - currentStartTime ) / TIME_PRECISION_DIVISION;
            printDelta( childIncrement, delta, report );
        }
    }

    private void printDelta( int increment, long delta, StringBuilder report ) {
        if ( delta != 0 ) {
            printIndentation( report, increment );
            report.append( delta );
            report.append( " delta" ).append( CacheUtilities.LINE_SEPARATOR );
        }
    }

    private void printDeltaXML( int increment, long delta, Writer report ) throws IOException {
        if ( delta != 0 ) {
            printIndentationXML( report, increment );
            report.append( "<delta duration=\"" );
            report.append( Long.toString(delta) );
            report.append( "\"/>" ).append( CacheUtilities.LINE_SEPARATOR );
        }
    }

    private void printIndentationXML( Writer report, int increment ) throws IOException {
        for ( int i = 0; i < increment; ++i ) {
            report.append( " " );
        }
    }

    private void printIndentation( StringBuilder report, int increment ) {
        for ( int i = 0; i < increment; ++i ) {
            report.append( "  " );
        }
    }

    private StackStep getSlowestStack( String operationName ) throws Exception {
        String selectStatement = SELECT_SLOWEST_SQL;
        return getStackStepFromSql( selectStatement, operationName, operationName );
    }

    private StackStep getAverageStack( String operationName ) throws Exception {
        String selectStatement = SELECT_AVERAGE_SQL;
        return getStackStepFromSql( selectStatement, operationName, operationName );
    }

    private StackStep getStackStepFromSql( String selectStatement, Object... parameters ) throws Exception {
        List<List<Object>> fieldValues = executeQuery( selectStatement, parameters );

        StackStep startStep = new StackStep();
        startStep.fieldValues = fieldValues.get( 0 );

        fetchChildSteps( startStep );

        StackStep fastestStack = startStep;
        return fastestStack;
    }

    private StackStep getFastestStack( String operationName ) throws Exception {
        String selectStatement = SELECT_FASTEST_SQL;
        return getStackStepFromSql( selectStatement, operationName, operationName );
    }

    private void fetchChildSteps( StackStep startStep ) throws Exception {

        String selectStatement = SELECT_CHILD_STACK_STEPS_SQL;
        List<List<Object>> fieldValues = executeQuery( selectStatement, startStep.getIdentifier() );

        for ( List<Object> list : fieldValues ) {
            StackStep childStep = new StackStep();
            childStep.fieldValues = list;
            startStep.childStepList.add( childStep );
            fetchChildSteps( childStep );
        }
    }

    public void printToFile( String resultToPrint, String fileName ) {
        PrintWriter writer = null;
        try {
            // $ANALYSIS-IGNORE
            writer = new PrintWriter( fileName );
            writer.append( resultToPrint );
            writer.flush();
        } catch ( IOException ex ) {
            throw new RuntimeException( "Failed to write result to file : " + fileName, ex );
        } finally {
            CacheUtilities.closeQuietly( writer );
        }
    }

    /**
     * Setup the database connection.
     * 
     * @throws Exception
     *             If anything goes wrong.
     */
    protected void setUp() throws Exception {
        connectDB();
        gatherer = new DirectDatabaseMetricGatherer();
        gatherer.setDataSource( dataSource );
        gatherer.setTruncatingSqlSelect( truncateSqlStatementOperationNames );
    }

    /**
     * Connect the database.
     * 
     * @throws Exception
     *             If anything goes wrong.
     */
    public void connectDB() throws Exception {
        Class.forName( jdbcDriver );

        if ( dataSource != null ) {
            return;
        }

        Connection conn = getConnectionWithRetry( jdbcUrl );

        // wrap a test connection that never closes
        jdbcConnection = new SingleJdbcConnection();
        jdbcConnection.setConnection( conn );

        dataSource = new SingleJdbcDataSource();
        dataSource.setConnection( jdbcConnection );

        if ( recreateTableOnStart() ) {
            LOGGER.log( Level.INFO, "Droping all tables start." );
            try {
                MetricCompiler.dropTables( dataSource );
            } catch ( Exception ex ) {
                LOGGER.log( Level.FINE, "Exception ignored while droping tables : " + ex.getMessage(), ex );
            }
            LOGGER.log( Level.INFO, "Droping all tables finished." );
        }

        if ( !isMetricTablePresent( dataSource ) ) {
            LOGGER.log( Level.INFO, "Creating database tables start." );
            MetricCompiler.createTables( dataSource );
            LOGGER.log( Level.INFO, "Creating database tables finished." );
        }
    }

    private boolean isMetricTablePresent( SingleJdbcDataSource dataSource2 ) {
        boolean returnValue = false;
        try {
            executeQuery( dataSource2, "select count(*) from METRIC" );
            LOGGER.log( Level.INFO, "METRIC table is present." );
            returnValue = true;
        } catch ( Exception e ) {
            LOGGER.log( Level.INFO, "METRIC table not present." );
            LOGGER.log( Level.FINE, "METRIC table not present.", e );
        }
        return returnValue;
    }

    private Connection getConnectionWithRetry( String dburl ) throws SQLException {
        LOGGER.log( Level.INFO, "Starting database connection." );
        Connection conn = null;
        long startTime = System.currentTimeMillis();
        
        //DriverManager.registerDriver(new DB2Driver());

        for ( int i = 0; i < 15; ++i ) {
            try {
                conn = DriverManager.getConnection( dburl );
                // conn = DriverManager.getConnection( "jdbc:db2://m2svt55.torolab.ibm.com:50000/cc61", "db2inst1", "diet4coke" );
                if ( conn != null ) {
                    break;
                }
            } catch ( SQLException ex ) {
                if ( !ex.getMessage().contains( "Failed to start database" ) ) {
                    throw ex;
                }
            }
            CacheUtilities.sleepSilently( 1000 );
        }

        long duration = System.currentTimeMillis() - startTime;
        LOGGER.log( Level.INFO, "Took " + duration + " ms to start database connection." );

        return conn;
    }

    /**
     * Execute a query and print the results.
     * 
     * @param sql
     *            The SQL query.
     * @return The printed result.
     * @throws Exception
     *             If anything goes wrong.
     */
    public String executeQueryAndPrint( String sql ) throws Exception {
        Statement createStatement = null;
        Connection connection = null;
        String printResultSet = null;
        ResultSet result = null;
        try {
            if ( LOGGER.isLoggable( Level.FINER ) ) {
                LOGGER.log( Level.FINER, "Running SQL : " + sql );
            }
            // $ANALYSIS-IGNORE
            connection = dataSource.getConnection();
            // $ANALYSIS-IGNORE
            createStatement = connection.createStatement();
            // $ANALYSIS-IGNORE
            result = createStatement.executeQuery( sql );
            printResultSet = CacheUtilities.printResultSet( result, false, "," );
            if ( LOGGER.isLoggable( Level.FINEST ) ) {
                LOGGER.log( Level.FINEST, "SQL Result : " + printResultSet );
            }
        } finally {
            CacheUtilities.closeQuietly( result );
            CacheUtilities.closeQuietly( createStatement );
            CacheUtilities.closeQuietly( connection );
        }
        return printResultSet;
    }

    /**
     * Execute a query and print the results.
     * 
     * @param title
     *            The report title.
     * @param sql
     *            The SQL query.
     * @return The printed result.
     * @throws Exception
     *             If anything goes wrong.
     */
    public String executeQueryAndPrintHtml( String title, String sql ) throws Exception {
        Statement createStatement = null;
        Connection connection = null;
        String printResultSet = null;
        ResultSet result = null;
        try {
            if ( LOGGER.isLoggable( Level.FINER ) ) {
                LOGGER.log( Level.FINER, "Running SQL : " + sql );
            }
            // $ANALYSIS-IGNORE
            connection = dataSource.getConnection();
            // $ANALYSIS-IGNORE
            createStatement = connection.createStatement();
            // $ANALYSIS-IGNORE
            result = createStatement.executeQuery( sql );
            printResultSet = CacheUtilities.printResultSetToHtml( title, result, false );
            if ( LOGGER.isLoggable( Level.FINEST ) ) {
                LOGGER.log( Level.FINEST, "SQL Result : " + printResultSet );
            }
        } finally {
            CacheUtilities.closeQuietly( result );
            CacheUtilities.closeQuietly( createStatement );
            CacheUtilities.closeQuietly( connection );
        }
        return printResultSet;
    }

    /**
     * Execute a SQL query.
     * 
     * @param sql
     *            The query to run.
     * @return The list of results.
     * @throws Exception
     *             If anything goes wrong.
     */
    public List<List<Object>> executeQuery( String sql ) throws Exception {
        DataSource currentDataSource = dataSource;
        return executeQuery( currentDataSource, sql );
    }

    /**
     * Execute a SQL query.
     * 
     * @param currentDataSource
     *            The datasource to use.
     * @param sql
     *            the query.
     * @return The result objects.
     * @throws SQLException
     *             If anything goes wrong.
     */
    public static List<List<Object>> executeQuery( DataSource currentDataSource, String sql ) throws SQLException {
        Statement createStatement = null;
        List<List<Object>> printResultSet = null;
        ResultSet result = null;
        long startTime = System.currentTimeMillis();
        Connection connection = null;
        try {
            if ( LOGGER.isLoggable( Level.FINER ) ) {
                LOGGER.log( Level.FINER, "Running SQL : " + sql );
            }

            // $ANALYSIS-IGNORE
            connection = currentDataSource.getConnection();
            // $ANALYSIS-IGNORE
            createStatement = connection.createStatement();
            // $ANALYSIS-IGNORE
            result = createStatement.executeQuery( sql );
            printResultSet = CacheUtilities.extractResultSet( result );
            if ( LOGGER.isLoggable( Level.FINEST ) ) {
                LOGGER.log( Level.FINEST, "SQL Result : " + printResultSet );
            }
        } finally {
            CacheUtilities.closeQuietly( result );
            CacheUtilities.closeQuietly( createStatement );
            CacheUtilities.closeQuietly( connection );

            if ( LOGGER.isLoggable( Level.FINER ) ) {
                long duration = System.currentTimeMillis() - startTime;
                LOGGER.log( Level.FINER, "Took " + duration + " ms to run DB Query." );
            }

        }
        return printResultSet;
    }

    /**
     * Execute a parameterized SQL query.
     * 
     * @param sql
     *            The query to run.
     * @param parameters
     *            The parameters.
     * @return The list of results.
     * @throws Exception
     *             If anything goes wrong.
     */
    public List<List<Object>> executeQuery( String sql, Object... parameters ) throws Exception {
        PreparedStatement createStatement = null;
        List<List<Object>> printResultSet = null;
        ResultSet result = null;
        long startTime = System.currentTimeMillis();
        try {
            if ( LOGGER.isLoggable( Level.FINER ) ) {
                LOGGER.log( Level.FINER, "Running SQL : " + sql + " with parameters : " + Arrays.deepToString( parameters ) );
            }

            // $ANALYSIS-IGNORE
            createStatement = getPreparedStatement( sql );

            fillParametersOnStatement( createStatement, parameters );

            // $ANALYSIS-IGNORE
            result = createStatement.executeQuery();
            printResultSet = CacheUtilities.extractResultSet( result );

            if ( LOGGER.isLoggable( Level.FINEST ) ) {
                LOGGER.log( Level.FINEST, "SQL Result : " + printResultSet );
            }
        } finally {
            CacheUtilities.closeQuietly( result );
            if ( !CACHE_PREPARED_STATEMENTS ) {
                CacheUtilities.closeQuietly( createStatement );
            }

            if ( LOGGER.isLoggable( Level.FINER ) ) {
                long duration = System.currentTimeMillis() - startTime;
                LOGGER.log( Level.FINER, "Took " + duration + " ms to run DB Query." );
            }

        }
        return printResultSet;
    }

    private void fillParametersOnStatement( PreparedStatement createStatement, Object... parameters ) throws SQLException {
        if ( parameters != null ) {
            int i = 0;
            for ( Object parameter : parameters ) {
                createStatement.setObject( ++i, parameter );
            }
        }
    }

    private PreparedStatement getPreparedStatement( String sql ) throws SQLException {
        PreparedStatement retVal = null;
        if ( CACHE_PREPARED_STATEMENTS ) {
            retVal = statementCache.get( sql );
            if ( retVal == null ) {
                retVal = dataSource.getConnection().prepareStatement( sql );
                statementCache.put( sql, retVal );
            }
        } else {
            retVal = dataSource.getConnection().prepareStatement( sql );
        }
        return retVal;
    }

    /**
     * Execute a SQL update.
     * 
     * @param sql
     *            The update statement.
     * @return The number of rows changed.
     * @throws Exception
     *             If anything goes wrong.
     */
    public int executeUpdate( String sql ) throws Exception {
        Statement createStatement = null;
        int retVal = 0;
        try {
            if ( LOGGER.isLoggable( Level.FINER ) ) {
                LOGGER.log( Level.FINER, "Running SQL : " + sql );
            }
            // $ANALYSIS-IGNORE
            createStatement = dataSource.getConnection().createStatement();
            retVal = createStatement.executeUpdate( sql );
            if ( LOGGER.isLoggable( Level.FINEST ) ) {
                LOGGER.log( Level.FINEST, "SQL Result : " + retVal + " rows updated" );
            }
        } catch ( Exception ex ) {
            LOGGER.log( Level.SEVERE, "Exception caught : " + ex.getMessage(), ex );
            throw ex;
        } finally {
            CacheUtilities.closeQuietly( createStatement );
        }
        return retVal;
    }

    /**
     * Execute a SQL update with parameters.
     * 
     * @param sql
     *            The update statement.
     * @param parameters
     *            The parameters.
     * @return The number of rows affected.
     * @throws Exception
     *             If anything goes wrong.
     */
    public int executeUpdate( String sql, Object... parameters ) throws Exception {
        PreparedStatement createStatement = null;
        int retVal = 0;
        try {
            if ( LOGGER.isLoggable( Level.FINER ) ) {
                LOGGER.log( Level.FINER, "Running SQL : " + sql );
            }
            // $ANALYSIS-IGNORE
            createStatement = dataSource.getConnection().prepareStatement( sql );
            fillParametersOnStatement( createStatement, parameters );

            retVal = createStatement.executeUpdate();
            if ( LOGGER.isLoggable( Level.FINEST ) ) {
                LOGGER.log( Level.FINEST, "SQL Result : " + retVal + " rows updated" );
            }

        } catch ( Exception ex ) {
            LOGGER.log( Level.SEVERE, "Exception caught : " + ex.getMessage(), ex );
            throw ex;
        } finally {
            CacheUtilities.closeQuietly( createStatement );
        }
        return retVal;
    }

    /**
     * Print the metrics table.
     * 
     * @throws Exception
     *             If anything goes wrong.
     */
    public void printMetricsTable() throws Exception {
        executeQueryAndPrint( "select * from metric" );
    }

    /**
     * Count the metrics table.
     * 
     * @throws Exception
     *             If anything goes wrong.
     */
    public void countMetricsTable() throws Exception {
        executeQueryAndPrint( "select count(*) as COUNT from metric" );
    }

    /**
     * Get the metric row count.
     * 
     * @return The metric row count.
     * @throws Exception
     *             If anything goes wrong.
     */
    public int getMetricRowCount() throws Exception {
        List<List<Object>> executeQuery = executeQuery( "select count(*) from metric" );
        Number object = (Number) executeQuery.get( 0 ).get( 0 );
        return object.intValue();
    }
    
    public SingleJdbcDataSource getDataSource() {
		return dataSource;
	}
    
    public SingleJdbcConnection getJdbcConnection() {
		return jdbcConnection;
	}
    
}
