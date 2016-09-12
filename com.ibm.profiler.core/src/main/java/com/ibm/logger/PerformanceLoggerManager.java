package com.ibm.logger;

import java.lang.management.ManagementFactory;
import java.util.logging.Logger;

import javax.management.InstanceAlreadyExistsException;
import javax.management.JMX;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;

import com.ibm.commerce.cache.LoggingHelper;
import com.ibm.logger.jmx.PerformanceLoggerManagerMXBean;

/**
 * @author Bryan Johnson
 * 
 */
public class PerformanceLoggerManager implements PerformanceLoggerManagerMXBean {

    private static final PerformanceLoggerManagerMXBean manager;

    public static final String JMX_DOMAIN = "com.ibm.profiler";

    private static final String PERFORMANCE_DOMAIN = "PerformanceDomain";

    private static final Logger LOGGER = Logger.getLogger( PerformanceLoggerManager.class.getName() );

    static {
        PerformanceLoggerManagerMXBean setManager = createInitialPerformanceLoggerManager();
        manager = setManager;
    }

    protected static PerformanceLoggerManagerMXBean createInitialPerformanceLoggerManager()
    {
        PerformanceLoggerManagerMXBean setManager = null;

        setManager = fetchRegisteredPerformanceLogger();

        if (setManager == null)
        {
            setManager = registerPerformanceLoggerManager();
        }
        return setManager;
    }

    private static PerformanceLoggerManagerMXBean fetchRegisteredPerformanceLogger()
    {
        PerformanceLoggerManagerMXBean setManager = null;
        try
        {
            ObjectName objectName = getObjectName();
            MBeanServer platformMBeanServer = ManagementFactory.getPlatformMBeanServer();
            if (platformMBeanServer.isRegistered(objectName))
            {
                setManager = JMX.newMBeanProxy(platformMBeanServer, objectName, PerformanceLoggerManagerMXBean.class);
            }
        }
        catch (Exception ex)
        {
            LoggingHelper.logUnexpectedException(LOGGER, PerformanceLogger.class.getName(),
                "PerformanceLoggerManager.static()", ex);
        }
        return setManager;
    }

    /**
     * @return The unique PerformanceLoggerManager
     */
    public static PerformanceLoggerManagerMXBean getManager() {
        return manager;
    }

    /**
     * @return the JMX object name of the PerformanceLoggerManager
     */
    public static ObjectName getObjectName() {
        try {
            return new ObjectName( JMX_DOMAIN + ":" + PERFORMANCE_DOMAIN + "=PerformanceManager" );
        } catch ( MalformedObjectNameException e ) {
            LoggingHelper.logUnexpectedException( LOGGER, PerformanceLogger.class.getName(), "getObjectName()", e );
        }
        return null;
    }

    /**
     * Constructor
     */
    private PerformanceLoggerManager() {
    }

    protected static PerformanceLoggerManagerMXBean registerPerformanceLoggerManager()
    {
        PerformanceLoggerManagerMXBean objectToRegister = new PerformanceLoggerManager();
        String methodName = "registerPerformanceLoggerManager";
        try {
            ManagementFactory.getPlatformMBeanServer().registerMBean( objectToRegister, getObjectName() );
        } catch ( InstanceAlreadyExistsException e ) {
            PerformanceLoggerManagerMXBean existingManager = fetchRegisteredPerformanceLogger();
            if( existingManager == null) {
                LoggingHelper.logUnexpectedException( LOGGER, PerformanceLogger.class.getName(), methodName, e );
            }
            else {
                objectToRegister = existingManager;
            }
        } catch ( MBeanRegistrationException e ) {
            LoggingHelper.logUnexpectedException( LOGGER, PerformanceLogger.class.getName(), methodName, e );
        } catch ( NotCompliantMBeanException e ) {
            LoggingHelper.logUnexpectedException( LOGGER, PerformanceLogger.class.getName(), methodName, e );
        }
        return objectToRegister;
    }

    /**
     * Reset internal counters.
     * 
     */
    @Override
    public void clear() {
        PerformanceLogger.clear();
    }

    /**
     * Disable PerformanceLogger
     * 
     */
    @Override
    public void disable() {
        PerformanceLogger.setEnabled( false );
    }

    /**
     * Dump internal counters for PerformanceLogger to log files
     * 
     */
    @Override
    public void dumpToLogger() {
        PerformanceLogger.dumpPerformanceLogs();
    }

    /**
     * Enable PerformanceLogger.
     * 
     */

    @Override
    public void enable() {
        PerformanceLogger.setEnabled( true );
    }

    @Override
    public boolean isEnabled() {
        return PerformanceLogger.isEnabled();
    }

	@Override
	public String dumpToTableView() {
		return PerformanceLogger.dumpPerformanceLogsTableToString();
	}

	@Override
	public String dumpToCsv() {
		return PerformanceLogger.dumpPerformanceLogsCsvToString();
	}
}
