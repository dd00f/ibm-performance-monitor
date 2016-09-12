package com.ibm.commerce.cache;

/*
 *-----------------------------------------------------------------
 * IBM Confidential
 *
 * OCO Source Materials
 *
 * WebSphere Commerce
 *
 * (C) Copyright IBM Corp. 2006, 2014
 *
 * The source code for this program is not published or otherwise
 * divested of its trade secrets, irrespective of what has
 * been deposited with the U.S. Copyright Office.
 *-----------------------------------------------------------------
 */

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * A utility class for creating a Java logger that is used for tracing and
 * logging messages, constants, and methods used for standard tracing features
 * that have not been defined by the logger APIs.
 */
public class LoggingHelper {

    /**
     * IBM Copyright notice field.
     */
    public static final String COPYRIGHT = com.ibm.commerce.copyright.IBMCopyright.SHORT_COPYRIGHT;

    /**
     * The log level used for entry and exit trace. This constant is used for
     * testing whether this level of trace is enabled.
     */
    public static final Level ENTRY_EXIT_LOG_LEVEL = Level.FINER;

    /**
     * The default trace level that standard code trace points should use. This
     * trace level is for code trace points when tracing important steps and
     * procedures in the code.
     */
    public static final Level DEFAULT_TRACE_LOG_LEVEL = java.util.logging.Level.FINE;

    private static final Map<String, String> CACHED_RESOURCE_BUNDLES = Collections.synchronizedMap( new java.util.Hashtable<String, String>() );

    private static final String NOT_FOUND = "NOT FOUND";

    private static final String FILE_SEPARATOR = System.getProperty( "file.separator" );

    private static Handler iFileHandler = null;

    /**
     * Creates an instance of the logger utility class.
     */
    private LoggingHelper() {
        super();
    }

    /**
     * Returns the logger associated with the specified class. This logger is
     * the package name the class belongs to.
     * 
     * @param loggedClass
     *            The class for which the logger is being returned for.
     * @return The logger for the specified class that can be used to log and
     *         trace messages.
     */
    static public Logger getLogger( Class<?> loggedClass ) {
        return getLogger( loggedClass.getName() );
    }

    /**
     * Returns the logger associated with the specified class name. This logger
     * is the package name the class name belongs to.
     * 
     * @param loggedClassName
     *            The class name for which the logger is being returned for.
     * @return The logger for the specified class name that can be used to log
     *         and trace messages.
     */
    static public Logger getLogger( String loggedClassName ) {
        String packageName = getPackageName( loggedClassName );
        String resourceBundle = null;
        List<String> packageNames = new ArrayList<String>();
        while ( resourceBundle == null && packageName != null ) {
            resourceBundle = CACHED_RESOURCE_BUNDLES.get( packageName );
            if ( resourceBundle == null ) {
                packageNames.add( packageName );
                String componentName = getLastPackageName( packageName );
                componentName = capitalize( componentName );
                StringBuilder bundleNameBuilder = new StringBuilder();
                bundleNameBuilder.append( packageName );
                bundleNameBuilder.append( ".logging.properties.Wc" );
                bundleNameBuilder.append( componentName );
                bundleNameBuilder.append( "Messages" );
                resourceBundle = bundleNameBuilder.toString();
                try {
                    ResourceBundle.getBundle( resourceBundle );
                } catch ( MissingResourceException missingBundle ) {
                    resourceBundle = null;
                    packageName = getPackageName( packageName );
                }
            }
        }
        if ( resourceBundle == null ) {
            resourceBundle = NOT_FOUND;
        }
        registerAllBundleAssociations( resourceBundle, packageNames );
        if ( NOT_FOUND.equals( resourceBundle ) ) {
            resourceBundle = null;
        }
        Logger logger = Logger.getLogger( loggedClassName, resourceBundle );
        return logger;
    }

    /**
     * Returns the administrative logger associated with the specified class.
     * This logger is the package name the class belongs to.
     * 
     * @param loggedClass
     *            The class for which the logger is being returned for.
     * @return The logger for the specified class that can be used to log and
     *         trace messages.
     */
    static public Logger getAdminLogger( Class<?> loggedClass ) {
        return getAdminLogger( loggedClass.getName() );
    }

    /**
     * Returns the administrative logger associated with the specified class
     * name. This logger is the package name the class name belongs to.
     * 
     * @param loggedClassName
     *            The class name for which the logger is being returned for.
     * @return The logger for the specified class name that can be used to log
     *         and trace messages.
     */
    static public Logger getAdminLogger( String loggedClassName ) {
        Logger logger = getLogger( loggedClassName );
        if ( iFileHandler == null ) {
            try {
                initializeAdminHandler();
            } catch ( IOException e ) {
                logger.log(Level.SEVERE, e.getMessage(), e );
            }
        }
        // Associate a custom handler to logger
        logger.addHandler( iFileHandler );
        return logger;
    }

    private static void initializeAdminHandler() throws IOException {
        LogManager logManager = LogManager.getLogManager();
        InputStream inputStream = LoggingHelper.class.getClassLoader().getResourceAsStream( "admin-logging.properties" );
        logManager.readConfiguration( inputStream );
        String directory = logManager.getProperty( "java.util.logging.FileHandler.directory" );
        String pattern = logManager.getProperty( "java.util.logging.FileHandler.pattern" );
        String limit = logManager.getProperty( "java.util.logging.FileHandler.limit" );
        String count = logManager.getProperty( "java.util.logging.FileHandler.count" );
        if ( directory != null ) {
            // Variable substitution
            while ( directory.length() > 0 ) {
                int x = directory.indexOf( "${" );
                int y = directory.indexOf( "}" );
                if ( x >= 0 && y > 0 ) {
                    String variable = directory.substring( x + 2, y );
                    String value = System.getProperty( variable );
                    if ( value != null ) {
                        directory = directory.replace( "${" + variable + "}", value );
                    }
                } else {
                    break;
                }
            }
        }
        if ( limit != null && count != null ) {
            iFileHandler = new FileHandler( directory + FILE_SEPARATOR + pattern, Integer.valueOf( limit ), Integer.valueOf( count ), true );
        } else {
            iFileHandler = new FileHandler( directory + FILE_SEPARATOR + pattern, true );
        }
    }

    private static void registerAllBundleAssociations( String resourceBundle, List<String> packageNames ) {
        for ( String currentPackage : packageNames ) {
            CACHED_RESOURCE_BUNDLES.put( currentPackage, resourceBundle );
        }
    }

    /**
     * Fetch the name of the last package in a fully qualified package name.
     * <p>
     * Example
     * <p>
     * Input : com.ibm.sample
     * <p>
     * Output: sample
     * <p>
     * Example
     * <p>
     * Input : com
     * <p>
     * Output: com
     * 
     * @param packageName
     *            the fully qualified package name.
     * @return the last package name.
     */
    private static String getLastPackageName( String packageName ) {
        String returnValue = packageName;
        int pos = packageName.lastIndexOf( '.' );
        if ( pos != -1 ) {
            returnValue = packageName.substring( pos + 1 );
        }
        return returnValue;
    }

    /**
     * Fetch the name of the package containing the fully declared name.
     * 
     * <p>
     * Example
     * <p>
     * Input : com.ibm.sample.MyClass
     * <p>
     * Output: com.ibm.sample
     * <p>
     * Example
     * <p>
     * Input : com.ibm.sample
     * <p>
     * Output: com.ibm
     * <p>
     * Example
     * <p>
     * Input : com
     * <p>
     * Output: (null)
     * 
     * @param loggedClassName
     *            the class name
     * @return the package name. Null if there is no package name below the
     *         class.
     */
    private static String getPackageName( String loggedClassName ) {
        String returnValue = null;
        int pos = loggedClassName.lastIndexOf( '.' );
        if ( pos != -1 ) {
            returnValue = loggedClassName.substring( 0, pos );
        }
        return returnValue;
    }

    /**
     * Determines whether entry and exit tracing is enabled for the specified
     * logger. Sometimes entry and exit trace points will create objects so it
     * is best to check if tracing is enabled before creating those objects.
     * 
     * @param logger
     *            The logger used for tracing.
     * @return Whether entry and exit trace is enabled for the specified logger.
     */
    public static boolean isEntryExitTraceEnabled( Logger logger ) {
        return logger.isLoggable( ENTRY_EXIT_LOG_LEVEL );
    }

    /**
     * Returns whether the default trace is enabled for the specified logger.
     * 
     * @param logger
     *            The logger used for tracing.
     * @return Whether the default trace level is enabled for the specified
     *         logger.
     */
    public static boolean isTraceEnabled( Logger logger ) {
        return logger.isLoggable( DEFAULT_TRACE_LOG_LEVEL );
    }

    /**
     * Log an unexpected exception in the system. This method will ensure that
     * we log a Level.SEVERE level exception on one line to notify
     * administrators of unexpected behavior. The full stack will only be
     * printed on the trace loggers if trace logs are enabled.
     * 
     * @param traceLogger
     *            The logger.
     * @param className
     *            The name of class in which the exception occurs.
     * @param methodName
     *            The name of the method in which the exception occurs.
     * @param exception
     *            The exception to be logged.
     */
    public static void logUnexpectedException( Logger traceLogger, String className, String methodName, Throwable exception ) {
        Logger logger = getLogger( LoggingHelper.class );

        // log the error level log first
        String exceptionClassName = exception.getClass().getName();
        String localizedMessage = exception.getLocalizedMessage();
        if( localizedMessage == null ) {
        	localizedMessage = "";
        }
        String traceLoggerName = traceLogger.getName();
        Object[] params = new Object[] { exceptionClassName, localizedMessage, traceLoggerName };

        // log the detailed trace only in trace mode
        boolean traceEnabled = isTraceEnabled( traceLogger );

        String msg = "Unexpected exception.";
        if ( !traceEnabled ) {
            msg = msg + " " + exceptionClassName + " : " + localizedMessage + ". For stack traces, turn on the following logger to the " + DEFAULT_TRACE_LOG_LEVEL.getName() + " level : " + traceLoggerName ;
        }

        logger.logp( Level.SEVERE, className, methodName, msg, params );

        if ( traceEnabled ) {
            traceLogger.logp( DEFAULT_TRACE_LOG_LEVEL, className, methodName, msg, exception );
        }
    }

    /**
     * capitalize the first letter of a string
     * 
     * @param str
     *            the string to capitalize
     * @return the capitalized version.
     */
    public static String capitalize( final String str ) {
        if ( str == null ) {
            return str;
        }
        int strLen = str.length();
        if ( strLen == 0 ) {
            return str;
        }
        return new StringBuilder( strLen ).append( Character.toTitleCase( str.charAt( 0 ) ) ).append( str.substring( 1 ) ).toString();
    }
}
