// IBM Confidential OCO Source Material
// 5725-F81 (C) COPYRIGHT International Business Machines Corp. 2011,2014
// The source code for this program is not published or otherwise divested
// of its trade secrets, irrespective of what has been deposited with the
// U.S. Copyright Office.
package com.ibm.logger;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javax.management.MBeanRegistrationException;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import com.ibm.commerce.cache.LoggingHelper;
import com.ibm.logger.jmx.JMXBeanRegistrar;
import com.ibm.logger.stats.LogEntry;
import com.ibm.logger.stats.LogEntry.LogType;

/**
 * @author Bryan Johnson
 * 
 */
public class PerformanceLogger implements Serializable {

    private static class LogEntryThreadLocal extends ThreadLocal<Map<String, LogEntry>> implements Serializable {
        /**
         * 
         */
        private static final long serialVersionUID = -3786835408569755981L;

        @Override
        protected synchronized Map<String, LogEntry> initialValue() {
            return new HashMap<String, LogEntry>();
        }
    }

    private final static String globalLock = "lock";

    private static boolean isEnabled;

    private static final Logger LOGGER = Logger.getLogger( PerformanceLogger.class.getName() );

    static {
        try {
            isEnabled = Boolean.parseBoolean( System.getProperty( "com.ibm.logger.PerformanceLogger.enabled", "false" ) );
            PerformanceLoggerManager.getManager();
        } catch ( Exception e ) {
            LoggingHelper.logUnexpectedException( LOGGER, PerformanceLogger.class.getName(), "staticInitialization", e );
        }
    }

    private static final long serialVersionUID = -9173634709021207651L;

    private static final LogEntryThreadLocal _timers = new LogEntryThreadLocal();

    private static final JMXBeanRegistrar<LogEntry> statsRegister = new JMXBeanRegistrar<LogEntry>();

    private static final Map<String, LogEntry> _logEntries = new ConcurrentHashMap<String, LogEntry>( 128 );

    public static final String JMX_DOMAIN = "IBM B2B PerformanceLogger";

    private static final Pattern ILLEGAL_CHAR_PATTERN = Pattern.compile( "[,=:\"*?]" );

    private static final String METRIC_TYPE = "MetricType";

    public static final String PERFORMANCE_DOMAIN = "PerformanceDomain";

    public static final String PERFORMANCE_DOMAIN_VALUE = "PerformanceMetrics";

    private static final int LOCAL_TIMER_THRESHOLD = 100;

    /**
     * Add a value to an internal counter.
     * 
     * @param id
     * @param processed
     * @return
     */
    public static boolean addStatistic( String id, float processed ) {
        if ( !isEnabled || id.equals( "" ) ) {
            return false;
        }

        increase( id, (long) processed );

        return true;
    }

    /**
     * 
     * 
     * @param id
     * @param clazz
     * @return
     */
    protected static ObjectName channelMXBeanName( String id, Class<? extends LogEntry> clazz ) {
        Hashtable<String, String> props = new Hashtable<String, String>();
        props.put( PERFORMANCE_DOMAIN, quoteAsNecessary( PERFORMANCE_DOMAIN_VALUE ) );
        props.put( "ID", quoteAsNecessary( id ) );
        props.put( METRIC_TYPE, clazz.getSimpleName() );

        try {
            return new ObjectName( JMX_DOMAIN, props );
        } catch ( MalformedObjectNameException e ) {
            throw new IllegalStateException( e );
        }
    }

    /**
     * 
     * Clear the internal counters
     * 
     */
    public static void clear() {
        try {
            PerformanceLogger.statsRegister.destroy();
            PerformanceLogger._logEntries.clear();
        } catch ( MBeanRegistrationException e ) {
            LoggingHelper.logUnexpectedException( LOGGER, PerformanceLogger.class.getName(), "clear", e );
        }
    }

    /**
     * 
     * dump internal counter tables to the standard logger
     * 
     */
    public static void dumpPerformanceLogs() {
        try {
            boolean headers = false;
            for ( LogEntry pil : getPerformanceLogs().values() ) {
                if ( !headers ) {
                    headers = true;
                    String dash = "=================================================";
                    System.out.println( String.format( "%-45.45s %8.8s %11.11s %11.11s %11.11s", "Name", "NumCalls", "Average  ", "Minimum  ", "Maximum  " ) );
                    System.out.println( String.format( "%-45.45s %8.8s %11.11s %11.11s %11.11s", dash, dash, dash, dash, dash ) );
                }
                System.out.println( String.format( "%-45.45s %8d %11.2f %11.2f %11.2f", pil.getName(), pil.getNumCalls(), pil.getAverage(), pil.getMinimum(), pil.getMaximum() ) );
            }

        } catch ( Exception e ) {
            LoggingHelper.logUnexpectedException( LOGGER, PerformanceLogger.class.getName(), "dumpPerformanceLogs", e );
        }

    }

    /**
     * @param _key
     * @return
     */
    public static LogEntry getPerformanceLog( String _key ) {
        LogEntry ret = getPerformanceLogs().get( _key );
        return ret;

    }

    /**
     * @return
     */
    public static Map<String, LogEntry> getPerformanceLogs() {
        return Collections.unmodifiableMap( _logEntries );
    }

    /**
     * @param id
     * @param value
     */
    public static void increase( String id, long value ) {
        increase( id, value, false, LogType.STATISTIC );
    }

    /**
     * @param id
     * @param value
     * @param logType
     */
    public static void increase( String id, long value, LogType logType ) {
        increase( id, value, false, logType );
    }

    /**
     * @param id
     * @param value
     * @param failed
     */
    public static void increase( String id, long value, boolean failed ) {
        increase( id, value, failed, LogType.STATISTIC );
    }

    /**
     * @param id
     * @param value
     * @param failed
     * @param logType
     */
    public static void increase( String id, long value, boolean failed, LogType logType ) {

        if ( logType == null ) {
            logType = LogType.STATISTIC;
        }

        if ( isEnabled ) {
            LogEntry logEntry = PerformanceLogger.getOrCreateEntry( id, logType.name() );
            logEntry.addValue( value, failed );
        }

    }

    /**
     * @return
     */
    public static boolean isEnabled() {
        return isEnabled;
    }

    /**
     * @param value
     * @return
     */
    private static String quoteAsNecessary( String value ) {
        return ILLEGAL_CHAR_PATTERN.matcher( value ).find() ? ObjectName.quote( value ) : value;
    }

    /**
     * @param isEnabled
     */
    public static void setEnabled( boolean isEnabled ) {
        PerformanceLogger.isEnabled = isEnabled;
    }

    /**
     * @param id
     * @return
     */
    public static boolean startLogging( String id ) {

        if ( !isEnabled || id == null || id.equals( "" ) ) {
            return false;
        }
        boolean success = true;

        LogEntry match = _timers.get().get( id );

        if ( match == null ) {
            match = new LogEntry( id, LogType.TIMER );
            _timers.get().put( id, match );
        }
        match.startTimer( System.nanoTime() );

        if ( _timers.get().size() > LOCAL_TIMER_THRESHOLD ) {
            flushTimers();
        }

        return success;
    }

    private static void flushTimers() {
        Map<String, LogEntry> localTimer = _timers.get();
        for ( String _key : Collections.unmodifiableMap( localTimer ).keySet() ) {
            LogEntry check = localTimer.get( _key );
            if ( !check.isInFlight() ) {
                localTimer.remove( _key );
            }
        }

    }

    /**
     * Stop timer and increment values
     * 
     * @param id
     * @return
     */
    public static boolean stopLogging( String id ) {
        if ( !isEnabled || id.equals( "" ) ) {
            return false;
        }

        LogEntry match = _timers.get().get( id );
        if ( match == null ) {
            return false;
        }

        match.stopTimer( System.nanoTime() );
        syncLocalStats( match );
        match.clear();
        return true;

    }

    /**
     * Flush thread local values to global counters
     * 
     * @param localEntry
     */
    private static void syncLocalStats( final LogEntry localEntry ) {
        LogEntry myEntry = getOrCreateEntry( localEntry.getId(), localEntry.getType() );
        myEntry.increase( localEntry );
    }

    /**
     * Flush thread local values to global counters
     * 
     * @param id
     * @param type
     * @return LogEntry
     */
    private static LogEntry getOrCreateEntry( final String id, final String type ) {
        LogEntry myEntry = _logEntries.get( id );
        if ( myEntry == null ) {
            synchronized ( globalLock ) {
                myEntry = _logEntries.get( id );
                if ( myEntry != null )
                    return myEntry;
                final LogEntry jmxEntry = new LogEntry( id, type );
                try {
                    ObjectName channelMXBeanName = channelMXBeanName( id, LogEntry.class );
                    JMXBeanRegistrar.JMXBeanProvider<LogEntry> jmxBeanProvider = new JMXBeanRegistrar.JMXBeanProvider<LogEntry>() {
                        @Override
                        public LogEntry provide() {
                            return jmxEntry;
                        }
                    };
                    PerformanceLogger.statsRegister.retrieveOrRegister( channelMXBeanName, jmxBeanProvider );
                } catch ( MBeanRegistrationException e ) {
                    LoggingHelper.logUnexpectedException( LOGGER, PerformanceLogger.class.getName(), "syncLocalStats", e );
                }
                _logEntries.put( id, jmxEntry );
                return jmxEntry;
            }
        }
        return myEntry;
    }

}
