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

import java.io.Closeable;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * Collection of utility methods used by the cache measurement library.
 */
public class CacheUtilities {
    private static final String ESCAPED_AMPERSAND = "&amp;";

    private static final String ESCAPED_TAB = "&tab;";

    private static final String ESCAPED_CARRIAGE_RETURN = "&ret;";

    private static final String ESCAPED_NEW_LINE = "&nln;";

    private static final String ESCAPED_SEMI_COLON = "&sem;";

    private static final String ESCAPED_COMMA = "&com;";

    private static final String ESCAPED_COLON = "&col;";

    private static final String ESCAPED_DOT = "&dot;";
    
	public static final String HOUR_SUFFIX = "h";

	public static final String DAY_SUFFIX = "d";
	
	public static final String MINUTE_SUFFIX = "m";
	
	public static final String SECOND_SUFFIX = "s";
	
	public static final String MILLISECOND_SUFFIX = "ms";
	
	public static final String MICRO_SYMBOL = "\u00b5";
	
	public static final String MICROSECOND_SUFFIX = MICRO_SYMBOL
			+ "s";

	public static final String MICROSECOND_SIMPLE_SUFFIX = "us";
	
	public static final String NANOSECOND_SUFFIX = "ns";

	public static final long MICROSECOND_IN_NANOSECONDS = 1000l;

	public static final long MILLISECOND_IN_NANOSECONDS = 1000l*MICROSECOND_IN_NANOSECONDS;

	public static final long SECOND_IN_NANOSECONDS = 1000l*MILLISECOND_IN_NANOSECONDS;

	public static final long MINUTE_IN_NANOSECONDS = 60l*SECOND_IN_NANOSECONDS;

	public static final long HOUR_IN_NANOSECONDS = 60l*MINUTE_IN_NANOSECONDS;

	public static final long DAY_IN_NANOSECONDS = 24l*HOUR_IN_NANOSECONDS;    
    

    /**
     * The operation ID header used to chain operations together.
     */
    public static final String OPERATION_ID_HEADER = "OperationID";

    /** line separator */
    public static final String LINE_SEPARATOR = System.getProperty( "line.separator" );

    /**
     * IBM Copyright notice field.
     */
    public static final String COPYRIGHT = com.ibm.commerce.copyright.IBMCopyright.SHORT_COPYRIGHT;

    /**
     * class name
     */
    private static final String CLASS_NAME = CacheUtilities.class.getName();

    /**
     * logger
     */
    private static final Logger LOGGER = LoggingHelper.getLogger( CLASS_NAME );

    /**
     * Join a thread quietly for a maximum amount of time.
     * 
     * @param threadToJoin
     *            the thread to join with
     * @param threadJoinTimeout
     *            the maximum amount of time to wait in milliseconds.
     * @return true if the join operation was successfully completed in time.
     */
    public static boolean joinQuietly( Thread threadToJoin, long threadJoinTimeout ) {
        if ( threadToJoin != null ) {
            try {
                threadToJoin.join( threadJoinTimeout );
                return true;
            } catch ( InterruptedException e ) {

                boolean traceEnabled = LoggingHelper.isTraceEnabled( LOGGER );
                if ( traceEnabled ) {
                    String logMessage = "joinQuietly operation on thread : " + threadToJoin.getName() + " was interrupted.";
                    LOGGER.log( Level.FINE, logMessage, e );
                }
            }
        }
        return false;
    }

    /**
     * close a database connection quietly.
     * 
     * @param connection
     *            the connection to close
     * 
     * @return true if the close operation was successful.
     */
    public static boolean closeQuietly( Connection connection ) {
        if ( connection != null ) {
            try {
                connection.close();
                return true;
            } catch ( Exception e ) {
                boolean traceEnabled = LoggingHelper.isTraceEnabled( LOGGER );
                if ( traceEnabled ) {
                    String message = "Exception caught while running closeQuietly(Connection connection).";
                    LOGGER.log( Level.FINE, message, e );
                }
            }
        }
        return false;
    }

    /**
     * close a SQL statement quietly
     * 
     * @param statement
     *            the statement to close
     * 
     * @return true if the close operation was successful.
     */
    public static boolean closeQuietly( Statement statement ) {
        if ( statement != null ) {
            try {
                statement.close();
                return true;
            } catch ( Exception e ) {
                boolean traceEnabled = LoggingHelper.isTraceEnabled( LOGGER );
                if ( traceEnabled ) {
                    String message = "Exception caught while running closeQuietly(Statement statement).";
                    LOGGER.log( Level.FINE, message, e );
                }
            }
        }
        return false;
    }

    /**
     * wait on a specified object silently
     * 
     * @param objectToWait
     *            the object to wait on
     * @param waitTime
     *            the maximum amount of time to wait in milliseconds.
     * 
     * @return true if the wait operation was successful without interruptions.
     */
    public static boolean waitSilently( Object objectToWait, long waitTime ) {
        if ( objectToWait != null ) {
            try {
                synchronized ( objectToWait ) {
                    objectToWait.wait( waitTime );
                }
                return true;
            } catch ( InterruptedException e ) {
                // ignore the exception, we expect to be interrupted

                boolean traceEnabled = LoggingHelper.isTraceEnabled( LOGGER );
                if ( traceEnabled ) {
                    String errorMessage = "waitSilently received interruption.";
                    LOGGER.log( Level.FINE, errorMessage, e );
                }
            }
        }
        return false;
    }

    /**
     * Sleep silently. Ignoring exceptions.
     * 
     * @param sleepTime
     *            the amount of time to sleep in milliseconds.
     * 
     * @return true if the sleep operation was successful without interruptions.
     */
    public static boolean sleepSilently( long sleepTime ) {
        try {
            Thread.sleep( sleepTime );
            return true;
        } catch ( InterruptedException e ) {
            boolean traceEnabled = LoggingHelper.isTraceEnabled( LOGGER );
            if ( traceEnabled ) {
                String errorMessage = "sleepSilently interrupted.";
                LOGGER.log( Level.FINE, errorMessage, e );
            }
        }
        return false;
    }

    /**
     * close a closeable object quietly.
     * 
     * @param closeableObject
     *            the closeable object to close.
     * 
     * @return true if the close operation was successful.
     */
    public static boolean closeQuietly( Closeable closeableObject ) {
        if ( closeableObject != null ) {
            try {
                closeableObject.close();
                return true;
            } catch ( Exception e ) {
                boolean traceEnabled = LoggingHelper.isTraceEnabled( LOGGER );
                if ( traceEnabled ) {
                    LOGGER.log( Level.FINE, "closeQuietly caught exception.", e );
                }
            }
        }
        return false;
    }

    /**
     * Execute a SQL update and automatically close the connection and
     * statement.
     * 
     * @see Statement#executeUpdate(String)
     * @param dataSource
     *            The data source to use.
     * @param sql
     *            The SQL update to execute.
     * @return the SQL return code.
     * @throws SQLException
     *             Any exception generated while executing the SQL update.
     */
    public static int executeUpdate( DataSource dataSource, String sql ) throws SQLException {

        final String METHODNAME = "executeUpdate(DataSource dataSource, String sql)";

        boolean entryExitLogEnabled = LoggingHelper.isEntryExitTraceEnabled( LOGGER );

        if ( entryExitLogEnabled ) {
            Object[] params = { dataSource, sql };
            LOGGER.entering( CLASS_NAME, METHODNAME, params );
        }

        Statement createStatement = null;
        Connection connection = null;
        int retVal = 0;
        try {
            // $ANALYSIS-IGNORE
            connection = dataSource.getConnection();
            // $ANALYSIS-IGNORE
            createStatement = connection.createStatement();
            retVal = createStatement.executeUpdate( sql );
        } finally {
            CacheUtilities.closeQuietly( createStatement );
            CacheUtilities.closeQuietly( connection );

            if ( entryExitLogEnabled ) {
                LOGGER.exiting( CLASS_NAME, METHODNAME );
            }
        }
        return retVal;
    }

    /**
     * Execute a SQL request, ignoring result sets, closing the connection and
     * statement.
     * 
     * @see Statement#execute(String)
     * @param dataSource
     *            The data source to use.
     * @param sql
     *            The SQL request to execute.
     * @return The boolean return code from the execute request.
     * @throws SQLException
     *             Any SQL exception generated while executing the SQL.
     */
    public static boolean execute( DataSource dataSource, String sql ) throws SQLException {

        final String METHODNAME = "execute(DataSource dataSource, String sql)";

        boolean entryExitLogEnabled = LoggingHelper.isEntryExitTraceEnabled( LOGGER );

        if ( entryExitLogEnabled ) {
            Object[] params = { dataSource, sql };
            LOGGER.entering( CLASS_NAME, METHODNAME, params );
        }

        Connection connection = null;
        Statement createStatement = null;
        boolean retVal = false;
        try {
            // $ANALYSIS-IGNORE
            connection = dataSource.getConnection();
            // $ANALYSIS-IGNORE
            createStatement = connection.createStatement();
            retVal = createStatement.execute( sql );
        } finally {
            CacheUtilities.closeQuietly( createStatement );
            CacheUtilities.closeQuietly( connection );

            if ( entryExitLogEnabled ) {
                LOGGER.exiting( CLASS_NAME, METHODNAME );
            }
        }
        return retVal;
    }

    /**
     * Close quietly a Result Set, ignoring null values and logging any
     * exception that might occur.
     * 
     * @param rs
     *            The result set to close.
     * @return True if the result set was properly closed. False if an exception
     *         was produced or the result set was null.
     */
    public static boolean closeQuietly( ResultSet rs ) {
        if ( rs != null ) {
            try {
                rs.close();
                return true;
            } catch ( Exception e ) {
                boolean traceEnabled = LoggingHelper.isTraceEnabled( LOGGER );
                if ( traceEnabled ) {
                    LOGGER.log( Level.FINE, "closeQuietly(ResultSet rs) caught exception.", e );
                }
            }
        }
        return false;
    }

    /**
     * escape the following characters out of any string : ".:," as well as
     * "\n\r\t" by leveraging the characters "&amp;;"
     * 
     * @param currentString
     *            the string to escape
     * @return the escaped form
     */
    public static String escapeString( String currentString ) {

        if ( currentString == null ) {
            return null;
        }
        StringWriter out = null;
        String returnValue = null;
        // $ANALYSIS-IGNORE impossible for this method to throw.
        out = new StringWriter( currentString.length() * 2 );
        escapeStringToWriterSilent( out, currentString );
        out.flush();
        returnValue = out.toString();
        closeQuietly( out );
        return returnValue;
    }

    /**
     * Unescape the following characters out of any string : ".:," as well as
     * "\n\r\t" by leveraging the characters "&amp;;"
     * 
     * @param currentString
     *            the escaped string
     * @return the unescaped version of the string
     */
    public static String unescapeString( String currentString ) {

        if ( currentString == null ) {
            return null;
        }
        StringWriter out = null;
        String returnValue = null;
        // $ANALYSIS-IGNORE impossible for this method to throw exceptions
        out = new StringWriter( currentString.length() * 2 );
        unescapeStringToWriterSilent( out, currentString );
        out.flush();
        returnValue = out.toString();
        closeQuietly( out );
        return returnValue;
    }

    /**
     * unescape a string to a writer, hiding any exception that shouldn't occur.
     * 
     * @param writer
     *            the writer to use
     * @param currentString
     *            the string to unescape
     */
    protected static void unescapeStringToWriterSilent( Writer writer, String currentString ) {
        try {
            unescapeStringToWriter( writer, currentString );
        } catch ( IOException e ) {
            boolean traceEnabled = LoggingHelper.isTraceEnabled( LOGGER );
            if ( traceEnabled ) {
                String logMessage = "unescapeStringToWriterSilent caught an unexpected IOException";
                LOGGER.log( Level.FINE, logMessage, e );
            }
        }
    }

    /**
     * escape a string to a writer by hiding any exception that shouldn't occur.
     * 
     * @param writer
     *            the writer to use
     * @param currentString
     *            the string to escape
     */
    protected static void escapeStringToWriterSilent( Writer writer, String currentString ) {
        try {
            escapeStringToWriter( writer, currentString );
        } catch ( IOException e ) {
            boolean traceEnabled = LoggingHelper.isTraceEnabled( LOGGER );
            if ( traceEnabled ) {
                String logMessage = "escapeStringToWriterSilent caught an unexpected IOException";
                LOGGER.log( Level.FINE, logMessage, e );
            }
        }
    }

    /**
     * escape the following characters out of any string : ".:," as well as
     * "\n\r\t" by leveraging the characters "&amp;;"
     * 
     * @param writer
     *            the writer to use
     * @param currentString
     *            the string to escape
     * @throws IOException
     *             that might be thrown by write operations.
     */
    public static void escapeStringToWriter( Writer writer, String currentString ) throws IOException {

        if ( currentString == null ) {
            return;
        }
        int size;
        size = currentString.length();
        for ( int i = 0; i < size; ++i ) {
            char ch = currentString.charAt( i );

            switch ( ch ) {
                case '.':
                    writer.write( ESCAPED_DOT );
                    break;
                case ':':
                    writer.write( ESCAPED_COLON );
                    break;
                case ',':
                    writer.write( ESCAPED_COMMA );
                    break;
                case ';':
                    writer.write( ESCAPED_SEMI_COLON );
                    break;
                case '\n':
                    writer.write( ESCAPED_NEW_LINE );
                    break;
                case '\r':
                    writer.write( ESCAPED_CARRIAGE_RETURN );
                    break;
                case '\t':
                    writer.write( ESCAPED_TAB );
                    break;
                case '&':
                    writer.write( ESCAPED_AMPERSAND );
                    break;
                default:
                    writer.write( ch );
                    break;
            }
        }
    }

    /**
     * Map of all the escaped string forms as keys leading to their unescaped
     * character version.
     */
    private static final Map<String, Character> ESCAPED_STRING_TO_CHARACTERS_MAP = new HashMap<String, Character>();

    /**
     * initialize the ESCAPED_STRING_TO_CHARACTERS_MAP
     */
    static {
        ESCAPED_STRING_TO_CHARACTERS_MAP.put( ESCAPED_DOT, '.' );
        ESCAPED_STRING_TO_CHARACTERS_MAP.put( ESCAPED_COLON, ':' );
        ESCAPED_STRING_TO_CHARACTERS_MAP.put( ESCAPED_COMMA, ',' );
        ESCAPED_STRING_TO_CHARACTERS_MAP.put( ESCAPED_SEMI_COLON, ';' );
        ESCAPED_STRING_TO_CHARACTERS_MAP.put( ESCAPED_NEW_LINE, '\n' );
        ESCAPED_STRING_TO_CHARACTERS_MAP.put( ESCAPED_CARRIAGE_RETURN, '\r' );
        ESCAPED_STRING_TO_CHARACTERS_MAP.put( ESCAPED_TAB, '\t' );
        ESCAPED_STRING_TO_CHARACTERS_MAP.put( ESCAPED_AMPERSAND, '&' );
    }

    /**
     * Unescape a string by writing it to a Writer.
     * 
     * @param writer
     *            the writer used to write the unescaped version.
     * @param currentString
     *            the escaped string version
     * @throws IOException
     *             if any write operation fails on the writer.
     */
    private static void unescapeStringToWriter( Writer writer, String currentString ) throws IOException {
        if ( currentString == null ) {
            return;
        }
        int size = currentString.length();
        StringBuilder escapedCharacter = new StringBuilder( 5 );
        boolean parsingEscapedCharacter = false;
        for ( int i = 0; i < size; i++ ) {
            char ch = currentString.charAt( i );
            if ( parsingEscapedCharacter ) {

                escapedCharacter.append( ch );
                if ( escapedCharacter.length() == 5 || ch == ';' ) {
                    String string = escapedCharacter.toString();
                    Character character = ESCAPED_STRING_TO_CHARACTERS_MAP.get( string );
                    if ( character != null ) {
                        writer.write( character.charValue() );
                    }

                    escapedCharacter.setLength( 0 );
                    parsingEscapedCharacter = false;
                }
            } else if ( ch == '&' ) {
                parsingEscapedCharacter = true;
                escapedCharacter.append( ch );
            } else {
                writer.write( ch );
            }
        }
    }

    /**
     * SQL Select pattern
     */
    private static final Pattern SQL_PATTERN = Pattern.compile( "(\\bselect\\b[\\S\\s]*?\\bfrom\\b)", Pattern.CASE_INSENSITIVE );

    /**
     * Truncate a potential SQL string by removing the list of selected fields
     * and displaying a field count.
     * 
     * @param potentialSql
     *            the potential SQL to truncate
     * @return the truncated SQL statement, or the original string if it wasn't
     *         a SQL select statement.
     */
    public static String truncateSqlSelectStatement( String potentialSql ) {
        if ( potentialSql == null ) {
            return null;
        }

        String retVal = potentialSql;

        Matcher matcher = SQL_PATTERN.matcher( potentialSql );
        if ( matcher.find() ) {
            String group = matcher.group( 0 );
            int fieldCount = group.split( "," ).length;
            String updatePattern = "";
            if ( fieldCount == 1 ) {
                updatePattern = "SELECT (" + fieldCount + " field) FROM";
            } else {
                updatePattern = "SELECT (" + fieldCount + " fields) FROM";
            }
            retVal = matcher.replaceFirst( updatePattern );
        }
        return retVal;
    }

    /**
     * Print a result set.
     * 
     * @param resultSet
     *            The result set to print.
     * @param repeatColumnName
     *            option to repeat the column names.
     * @param separator
     *            The value separator to use.
     * @return The printed statement.
     * @throws SQLException
     *             If anything goes wrong.
     */
    public static String printResultSet( ResultSet resultSet, boolean repeatColumnName, String separator ) throws SQLException {
        StringBuilder builder = new StringBuilder();
        ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
        boolean first = true;

        while ( resultSet.next() ) {

            if ( !repeatColumnName && first ) {
                // print the header
                for ( int i = 0; i < resultSetMetaData.getColumnCount(); i++ ) {
                    String columnName = getColumnName( separator, resultSetMetaData, i );
                    builder.append( columnName );
                    builder.append( separator );
                }
                builder.append( LINE_SEPARATOR );
            }

            for ( int i = 0; i < resultSetMetaData.getColumnCount(); i++ ) {
                Object object = resultSet.getObject( i + 1 );

                if ( repeatColumnName ) {
                    String columnName = getColumnName( separator, resultSetMetaData, i );
                    builder.append( columnName );
                    builder.append( "=" );
                }
                if ( object != null && object instanceof byte[] ) {
                    byte byteArray[] = (byte[]) object;
                    for ( int j = 0; j < byteArray.length; j++ ) {
                        if ( byteArray[j] < 16 ) {
                            builder.append( "0" );
                        }
                        builder.append( Integer.toHexString( byteArray[j] ) );
                    }
                } else if ( object instanceof java.sql.Clob ) {
                    java.sql.Clob clob = (java.sql.Clob) object;
                    // $ANALYSIS-IGNORE
                    String clobString = clob.getSubString( 1, (int) clob.length() );
                    if ( clobString == null ) {
                        builder.append( "'NULL'" );
                    } else {
                        builder.append( clobString );
                    }
                } else if ( object instanceof Double || object instanceof Float ) {
                    Number number = (Number) object;
                    builder.append( String.format("%f", number.doubleValue()) );
                } else {
                    if ( object == null ) {
                        builder.append( "'NULL'" );
                    } else {
                        String cellStringValue = object.toString();
                        cellStringValue = cellStringValue.replaceAll( separator, "." );
                        builder.append( cellStringValue );
                    }
                }
                builder.append( separator );

            }
            builder.append( LINE_SEPARATOR );

            first = false;

        }
        return builder.toString();
    }

    /**
     * Print a result set to html.
     * 
     * @param title
     *            The report title.
     * @param resultSet
     *            The result set to print.
     * @param repeatColumnName
     *            option to repeat the column names.
     * @return The printed statement.
     * @throws SQLException
     *             If anything goes wrong.
     */
    public static String printResultSetToHtml( String title, ResultSet resultSet, boolean repeatColumnName ) throws SQLException {
        StringBuilder builder = new StringBuilder( "<html>" );

        builder.append( "<head>" );
        builder.append( "<style>" );
        builder.append( "td {border-width: 1px;  border-color: black; border-style: solid; font-family:\"Arial Black\", Gadget, sans-serif; }" );
        builder.append( "table {border-spacing:0px;}" );
        builder.append( "</style>" );
        builder.append( "</head>" );

        builder.append( "<body>" );
        builder.append( "<h1>" );
        builder.append( title );
        builder.append( "</h1><table>" );
        ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
        boolean first = true;

        while ( resultSet.next() ) {

            if ( !repeatColumnName && first ) {
                // print the header
                builder.append( "<tr>" );
                for ( int i = 0; i < resultSetMetaData.getColumnCount(); i++ ) {
                    builder.append( "<td><strong>" );
                    String columnName = resultSetMetaData.getColumnName( i + 1 );
                    columnName = columnName.replaceAll( "_", " " );
                    builder.append( columnName );
                    builder.append( "</strong></td>" );
                }
                builder.append( "</tr>" );
                builder.append( LINE_SEPARATOR );
            }

            builder.append( "<tr>" );
            for ( int i = 0; i < resultSetMetaData.getColumnCount(); i++ ) {
                Object object = resultSet.getObject( i + 1 );

                builder.append( "<td>" );

                if ( repeatColumnName ) {
                    String columnName = resultSetMetaData.getColumnName( i + 1 );
                    builder.append( columnName );
                    builder.append( "=" );
                }
                if ( object != null && object instanceof byte[] ) {
                    byte byteArray[] = (byte[]) object;
                    for ( int j = 0; j < byteArray.length; j++ ) {
                        if ( byteArray[j] < 16 ) {
                            builder.append( "0" );
                        }
                        builder.append( Integer.toHexString( byteArray[j] ) );
                    }
                } else if ( object instanceof java.sql.Clob ) {
                    java.sql.Clob clob = (java.sql.Clob) object;
                    // $ANALYSIS-IGNORE
                    String clobString = clob.getSubString( 1, (int) clob.length() );
                    if ( clobString == null ) {
                        builder.append( "'NULL'" );
                    } else {
                        builder.append( clobString );
                    }
                } else {
                    if ( object == null ) {
                        builder.append( "'NULL'" );
                    } else {
                        String columnName = object.toString();
                        columnName = StringEscapeUtils.escapeHtml4( columnName );
                        builder.append( columnName );
                    }
                }

                builder.append( "</td>" );
            }
            builder.append( "</tr>" );

            builder.append( LINE_SEPARATOR );

            first = false;

        }

        builder.append( LINE_SEPARATOR );
        builder.append( "</table></body></html>" );
        builder.append( LINE_SEPARATOR );

        return builder.toString();
    }

    /**
     * Extract objects from a result set.
     * 
     * @param resultSet
     *            The result set.
     * @return The extracted objects.
     * @throws SQLException
     *             If anything goes wrong.
     */
    public static List<List<Object>> extractResultSet( ResultSet resultSet ) throws SQLException {
        List<List<Object>> retVal = new ArrayList<List<Object>>();
        ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
        while ( resultSet.next() ) {
            List<Object> rowValues = new ArrayList<Object>();
            for ( int i = 0; i < resultSetMetaData.getColumnCount(); i++ ) {
                Object object = resultSet.getObject( i + 1 );
                rowValues.add( object );
            }
            retVal.add( rowValues );
        }

        return retVal;
    }

    private static String getColumnName( String separator, ResultSetMetaData resultSetMetaData, int i ) throws SQLException {
        String columnName = resultSetMetaData.getColumnName( i + 1 );
        columnName = columnName.replaceAll( separator, " " );
        return columnName;
    }

    /**
     * Capitalize a string.
     * 
     * @param str
     *            The string to capitalize.
     * @return The capitalized string.
     */
    public static String capitalize( final String str ) {
        int strLen;
        if ( str == null || str.length() == 0 ) {
            return str;
        }
        strLen = str.length();
        StringBuilder stringBuilder = new StringBuilder( strLen );
        stringBuilder.append( Character.toTitleCase( str.charAt( 0 ) ) );
        stringBuilder.append( str.substring( 1 ) );
        return stringBuilder.toString();
    }

    /**
     * Test if a character sequence is blank.
     * 
     * @param cs
     *            The sequence to test.
     * @return True if the sequence is null, empty or only containing blank
     *         characters.
     */
    public static boolean isBlank( final CharSequence cs ) {
        int strLen;
        if ( cs == null || cs.length() == 0 ) {
            return true;
        }
        strLen = cs.length();
        for ( int i = 0; i < strLen; i++ ) {
            if ( Character.isWhitespace( cs.charAt( i ) ) == false ) {
                return false;
            }
        }
        return true;
    }

    /**
     * equality test
     * 
     * @param object1
     *            1
     * @param object2
     *            2
     * @return true if equal
     */
    public static boolean equals( Object object1, Object object2 ) {
        if ( object1 == object2 ) {
            return true;
        }
        if ( object1 == null || object2 == null ) {
            return false;
        }
        return object1.equals( object2 );
    }

    /**
     * a utility to parse string into xml readable format
     * 
     * @param builder
     *            the builder to write into
     * @param str
     *            string to parse
     * @throws IOException if anything goes wrong in the writer.
     */
    public static void printXmlEscapedStringToWriter( Writer builder, String str ) throws IOException {
        if ( str != null ) {
            // $ANALYSIS-IGNORE
            str = str.replaceAll( "[\n\r\t]", " " );
            // $ANALYSIS-IGNORE
            builder.append( StringEscapeUtils.escapeXml( str ) );
        }
    }

    /**
     * update the parent operation
     * 
     * @param httpServletRequest
     *            an httpServletRequest
     * @return the previous parent id
     */
    public static long updateOperationParentIdentifierFromHeader( HttpServletRequest httpServletRequest ) {
        final String METHODNAME = "updateOperationParentIdentifierFromHeader(MessageContext messageContext)";
        final boolean entryExitTraceEnabled = LoggingHelper.isEntryExitTraceEnabled( LOGGER );
        if ( entryExitTraceEnabled ) {
            Object[] parameters = new Object[] { httpServletRequest };
            LOGGER.entering( CLASS_NAME, METHODNAME, parameters );
        }

        long previousParent = OperationMetric.getThreadParentOperationIdentifier();
        try {
            String requestHeader = httpServletRequest.getHeader( OPERATION_ID_HEADER );
            if ( requestHeader != null && !requestHeader.isEmpty() ) {
                long parseLong = Long.parseLong( requestHeader );
                OperationMetric.setThreadParentOperationIdentifier( parseLong );
            } else {
                requestHeader = httpServletRequest.getHeader( "WCOperationID" );
                if ( requestHeader != null && !requestHeader.isEmpty() ) {
                    long parseLong = Long.parseLong( requestHeader );
                    OperationMetric.setThreadParentOperationIdentifier( parseLong );
                }
            }
        } catch ( Exception ex ) {
            LoggingHelper.logUnexpectedException( LOGGER, CLASS_NAME, METHODNAME, ex );
        }
        if ( entryExitTraceEnabled ) {
            LOGGER.exiting( CLASS_NAME, METHODNAME, previousParent );
        }
        return previousParent;
    }
    
	/**
	 * Test if an index is in the defined range. If the maximum index is larger
	 * than the minimum, an index overflow is assumed and the range to match is
	 * reversed.
	 * 
	 * @param targetIndex
	 *            the target index.
	 * @param minimumIndex
	 *            The maximum index value (inclusive)
	 * @param maximumIndex
	 *            The minimum index value (inclusive)
	 * @return true if the target index is in range.
	 */
	public static boolean isIndexInRange(long targetIndex, long minimumIndex,
			long maximumIndex) {
		boolean inRange = true;
		if (minimumIndex <= maximumIndex) {
			if (targetIndex > maximumIndex || targetIndex < minimumIndex) {
				inRange = false;
			}
		} else {
			// index overflow detected, reverse the range.
			if (targetIndex > maximumIndex && targetIndex < minimumIndex) {
				inRange = false;
			}
		}
		return inRange;
	}

	/**
	 * Get a duration in short text form. Returns a simple rounded number with a
	 * suffix that most closely represents the duration. The suffix can be one
	 * of the following:
	 * <ul>
	 * <li>d for days</li>
	 * <li>h for hours</li>
	 * <li>m for minutes</li>
	 * <li>s for seconds</li>
	 * <li>ms for milliseconds</li>
	 * <li>us for microseconds</li>
	 * <li>ns for nanoseconds</li>
	 * </ul>
	 * 
	 * @param durationInNano
	 *            the duration in nanoseconds.
	 * @return The short text form.
	 */
	public static String getDurationShortText(long durationInNano) {
		StringBuilder builder = new StringBuilder();
		getDurationShortText(durationInNano, builder);
		String returnValue = builder.toString();
		return returnValue;
	}
	
	public static void getDurationShortText(long durationInNano, StringBuilder builder) {
		if( durationInNano < 0 ) {
			builder.append("-");
			getDurationShortText(-durationInNano, builder);
			return;
		}
		
		if (durationInNano >= DAY_IN_NANOSECONDS) {
			builder.append(durationInNano / DAY_IN_NANOSECONDS);
			builder.append( DAY_SUFFIX);
			getDurationShortTextSkipZero(durationInNano
							% DAY_IN_NANOSECONDS, builder);
		} else if (durationInNano >= HOUR_IN_NANOSECONDS) {
			builder.append(durationInNano / HOUR_IN_NANOSECONDS);
			builder.append( HOUR_SUFFIX);
			getDurationShortTextSkipZero(durationInNano
							% HOUR_IN_NANOSECONDS, builder);
		} else if (durationInNano >= MINUTE_IN_NANOSECONDS) {
			builder.append(durationInNano / MINUTE_IN_NANOSECONDS);
			builder.append( MINUTE_SUFFIX);
			getDurationShortTextSkipZero(durationInNano
							% MINUTE_IN_NANOSECONDS, builder);
		} else if (durationInNano >= SECOND_IN_NANOSECONDS) {
			builder.append(durationInNano / SECOND_IN_NANOSECONDS);
			builder.append( SECOND_SUFFIX);
			getDurationShortTextSkipZero(durationInNano
							% SECOND_IN_NANOSECONDS, builder);
		} else if (durationInNano >= MILLISECOND_IN_NANOSECONDS) {
			builder.append(durationInNano / MILLISECOND_IN_NANOSECONDS);
			builder.append( MILLISECOND_SUFFIX);
			getDurationShortTextSkipZero(durationInNano
							% MILLISECOND_IN_NANOSECONDS, builder);
		} else if (durationInNano >= MICROSECOND_IN_NANOSECONDS) {
			builder.append(durationInNano / MICROSECOND_IN_NANOSECONDS);
			builder.append( MICROSECOND_SUFFIX);
			getDurationShortTextSkipZero(durationInNano
							% MICROSECOND_IN_NANOSECONDS, builder);
		} else {
			builder.append(durationInNano);
			builder.append(NANOSECOND_SUFFIX);
		}
	}
	
	private static void getDurationShortTextSkipZero(long durationInNano, StringBuilder builder) {
		if( durationInNano > 0) {
			getDurationShortText(durationInNano, builder);
		}
	}

	/**
	 * Get a nanosecond duration from a short text form. The short text form can
	 * be in the form of a simple number (without a decimal value) and a suffix.
	 * The suffix must match one of the following :
	 * <ul>
	 * <li>d for days</li>
	 * <li>h for hours</li>
	 * <li>m for minutes</li>
	 * <li>s for seconds</li>
	 * <li>ms for milliseconds</li>
	 * <li>us for microseconds</li>
	 * <li>ns for nanoseconds</li>
	 * </ul>
	 * 
	 * If no suffix is found, the default unit is seconds.
	 * 
	 * @param shortText
	 *            The short text form. The default unit is in seconds if no unit
	 *            is specified.
	 * @return the duration in nanoseconds.
	 * @throws NullPointerException
	 *             if the input is null.
	 * @throws IllegalArgumentException
	 *             if the input isn't correctly formed.
	 * @throws NumberFormatException
	 *             if the input numeric part isn't formed correctly.
	 */
	public static long getNanoDurationFromShortText(String shortText) {
		String number = shortText.replaceAll("[A-Za-z" + MICRO_SYMBOL + " ]", "");
		String suffix = shortText.replaceAll("[0-9- ]", "");

		long longValue = Long.parseLong(number);
		long returnValue = longValue;

		// default unit is seconds
		if (StringUtils.isBlank(suffix)) {
			suffix = SECOND_SUFFIX;
		}

		if (suffix.equalsIgnoreCase(DAY_SUFFIX)) {
			returnValue = longValue * DAY_IN_NANOSECONDS;
		} else if (suffix.equalsIgnoreCase(HOUR_SUFFIX)) {
			returnValue = longValue * HOUR_IN_NANOSECONDS;
		} else if (suffix.equalsIgnoreCase(MINUTE_SUFFIX)) {
			returnValue = longValue * MINUTE_IN_NANOSECONDS;
		} else if (suffix.equalsIgnoreCase(SECOND_SUFFIX)) {
			returnValue = longValue * SECOND_IN_NANOSECONDS;
		} else if (suffix.equalsIgnoreCase(MILLISECOND_SUFFIX)) {
			returnValue = longValue * MILLISECOND_IN_NANOSECONDS;
		} else if (suffix.equalsIgnoreCase(MICROSECOND_SUFFIX)
				|| suffix.equalsIgnoreCase(MICROSECOND_SIMPLE_SUFFIX)) {
			returnValue = longValue * MICROSECOND_IN_NANOSECONDS;
		} else if (suffix.equalsIgnoreCase(NANOSECOND_SUFFIX)) {
			returnValue = longValue;
		}
		return returnValue;
	}
	
    /**
     * Convert an object to a string safely by catching exceptions.
     * 
     * @param object
     *            The object to print.
     * @return The String value. Returns "null" if the object is null and
     *         "error" if any exception occurs.
     */
    public static String safeToString(Object object)
    {
        if (object == null)
        {
            return "null";
        }
        try
        {
            return object.toString();
        }
        catch (Exception ex)
        {
            LoggingHelper.logUnexpectedException(LOGGER, CLASS_NAME, "safeToString(Object)", ex);
        }
        return "error";
    }

    /**
     * Converts an object to a string safely by catching exceptions. If the
     * object is an array, all the array elements get printed as well.
     * 
     * @param object
     *            The object to print.
     * @return The Sting version of the object. Returns "null" if the object is
     *         null and "error" if any exception occurs.
     */
    public static String deepSafeToString(Object object)
    {
        if (object == null)
        {
            return "null";
        }
        try
        {
            Class<? extends Object> objectClass = object.getClass();
            if (objectClass.isArray())
            {

                if (objectClass == byte[].class)
                {
                    return Arrays.toString((byte[]) object);
                }
                else if (objectClass == short[].class)
                {
                    return Arrays.toString((short[]) object);
                }
                else if (objectClass == int[].class)
                {
                    return Arrays.toString((int[]) object);
                }
                else if (objectClass == long[].class)
                {
                    return Arrays.toString((long[]) object);
                }
                else if (objectClass == char[].class)
                {
                    return Arrays.toString((char[]) object);
                }
                else if (objectClass == float[].class)
                {
                    return Arrays.toString((float[]) object);
                }
                else if (objectClass == double[].class)
                {
                    return Arrays.toString((double[]) object);
                }
                else if (objectClass == boolean[].class)
                {
                    return Arrays.toString((boolean[]) object);
                }

                Object[] arrayValue = (Object[]) object;

                String[] stringArray = deepSafeToString(arrayValue);
                return Arrays.toString(stringArray);
            }

            return object.toString();
        }
        catch (Exception ex)
        {
            LoggingHelper.logUnexpectedException(LOGGER, CLASS_NAME, "safeToString(Object)", ex);
        }
        return "error";
    }

    /**
     * Convert an array of objects to a flat list of string.
     * 
     * @param objectArray
     *            The object array to convert.
     * @return An array of Strings that match every object.
     */
    public static String[] safeToString(Object[] objectArray)
    {
        if (objectArray == null)
        {
            return new String[0];
        }

        int length = objectArray.length;
        String[] returnValue = new String[length];
        for (int i = 0; i < objectArray.length; i++)
        {
            returnValue[i] = safeToString(objectArray[i]);
        }
        return returnValue;
    }

    /**
     * Convert an array of objects to a flat list of string. If one of the
     * object is an array, all the elements in the array are also printed in the
     * string.
     * 
     * @param objectArray
     *            The object array to convert.
     * @return An array of Strings that match every object.
     */
    public static String[] deepSafeToString(Object[] objectArray)
    {
        if (objectArray == null)
        {
            return new String[0];
        }

        int length = objectArray.length;
        String[] returnValue = new String[length];
        for (int i = 0; i < objectArray.length; i++)
        {
            returnValue[i] = deepSafeToString(objectArray[i]);
        }
        return returnValue;
    }
}
