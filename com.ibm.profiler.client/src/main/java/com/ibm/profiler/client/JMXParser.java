package com.ibm.profiler.client;

//IBM Confidential OCO Source Material
//5725-F81 (C) COPYRIGHT International Business Machines Corp. 2011,2014
//The source code for this program is not published or otherwise divested
//of its trade secrets, irrespective of what has been deposited with the
//U.S. Copyright Office.

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanServerConnection;
import javax.management.ObjectInstance;
import javax.management.ReflectionException;
import javax.management.RuntimeMBeanException;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import com.ibm.profiler.client.jmx.JMXStatistic;
import com.ibm.profiler.client.jmx.JMXStatisticKey;
import com.ibm.profiler.client.jmx.TotalLogEntry;

/**
 * @author Bryan Johnson
 * 
 */
public class JMXParser {

    private static boolean DEBUG = false;

    public static String PERFORMANCE_LOGGER = "PerformanceLogger";

    public static String MESSAGE_FABRIC = "MessageFabric";

    public static String ALL_STATS = "All";

    public static String SUPPRESS_OUTPUT = "SuppressOutput";

    /**
     * @param args arguments
     */
    public static void main( String[] args ) {
        String host = "gdhape01.svl.ibm.com";
        String port = "8856";
        String service = null;
        HashMap<String, Boolean> dspProps = new HashMap<String, Boolean>();
        dspProps.put( SUPPRESS_OUTPUT, false );
        dspProps.put( MESSAGE_FABRIC, false );
        dspProps.put( PERFORMANCE_LOGGER, false );
        dspProps.put( ALL_STATS, true );
        if ( args != null ) {
            String last = "";
            for ( String arg : args ) {
                if ( arg.equalsIgnoreCase( "-suppressEmpty" ) ) {
                    dspProps.put( SUPPRESS_OUTPUT, true );
                }
                if ( last.equalsIgnoreCase( "-host" ) ) {
                    host = arg;
                }
                if ( last.equalsIgnoreCase( "-service" ) ) {
                    service = arg;
                }
                if ( last.equalsIgnoreCase( "-port" ) ) {
                    port = arg;
                }
                if ( arg.equalsIgnoreCase( "-debug" ) ) {
                    DEBUG = true;
                }
                if ( arg.equalsIgnoreCase( "-usage" ) ) {
                    usage();
                    System.exit( 0 );
                }
                if ( arg.equalsIgnoreCase( "-performanceLogger" ) ) {
                    dspProps.put( PERFORMANCE_LOGGER, true );
                    dspProps.put( ALL_STATS, false );
                }
                if ( arg.equalsIgnoreCase( "-messageFabric" ) ) {
                    dspProps.put( MESSAGE_FABRIC, true );
                    dspProps.put( ALL_STATS, false );
                }
                last = arg;

            }
        }

        MBeanServerConnection conn = null;
        try {

            if ( service == null )
                service = "service:jmx:rmi:///jndi/rmi://" + host + ":" + port + "/jmxrmi";

            System.out.println( "Connecting to : " + service );
            JMXServiceURL url = new JMXServiceURL( service );
            JMXConnector jmx = JMXConnectorFactory.connect( url );
            conn = jmx.getMBeanServerConnection();
        } catch ( Throwable e ) {
            System.out.println( "Unable to connect to JMX Server: " + e );
            System.exit( 1 );
        }
        try {

            JMXParser parser = new JMXParser();

            List<JMXStatistic> myStats = parser.parse( conn, conn.queryMBeans( null, null ), dspProps );
            parser.dumpStats( myStats, dspProps );
        } catch ( Exception e ) {
            System.out.println( "Error parsing: " + e );
            e.printStackTrace();
        }

    }

    /**
     * print usage
     */
    private static void usage() {
        System.out.println( "JMXParser -host <hostname> -port <port> -service <service Name> -suppressEmpty -debug -messageFabric -performanceLogger" );

    }

    /**
     * Parse a list of mbeans into an array of JMXStatistics
     * 
     * @param conn A JMX Connection
     * @param queryMBeans queriedMBean list to be parsed
     * @param dspProps DisplayProperties
     * 
     * @return The array of JMXStatistics
     * 
     * @throws InstanceNotFoundException if the bean isn't found.
     * @throws IntrospectionException  if the bean isn't found.
     * @throws ReflectionException if the bean isn't found.
     * @throws IOException if the bean isn't found.
     */
    public List<JMXStatistic> parse( MBeanServerConnection conn, Set<ObjectInstance> queryMBeans, HashMap<String, Boolean> dspProps ) throws InstanceNotFoundException, IntrospectionException, ReflectionException, IOException {
        List<JMXStatistic> stats = new ArrayList<JMXStatistic>();

        if ( dspProps == null )
            dspProps = new HashMap<String, Boolean>();
        System.out.println( "" );
        if ( DEBUG )
            System.out.print( "Parsing JMX MBeans " );
        for ( ObjectInstance one : queryMBeans ) {
            if ( DEBUG )
                System.out.print( " ." );

            MBeanInfo mbean = conn.getMBeanInfo( one.getObjectName() );
            if ( one.getObjectName().getDomain().equals( "IBM B2B MessageFabric" ) ) {

                continue;
            } else if ( one.getObjectName().getDomain().equals( "IBM MEG" ) ) {
                continue;
            } else if ( one.getObjectName().getDomain().equals( "IBM WebSphere MQ classes for JMS" ) ) {
                continue;
            } else if ( one.getObjectName().getDomain().equals( "com.ibm.profiler" ) ) {
                if ( dspProps.get( ALL_STATS ) || dspProps.get( PERFORMANCE_LOGGER ) ) {
                    if ( one.getClassName().equals( "com.ibm.logger.stats.TotalLogEntry" ) ) {
                        JMXStatistic stat = new TotalLogEntry().parse( conn, mbean, one );
                        stats.add( stat );
                    }
                }
            } else if ( one.getObjectName().getDomain().equals( "MQ Classes for Java" ) ) {
                if ( one.getClassName().equals( "com.ibm.mq.jmqi.monitoring.TraceControlImpl" ) ) {

                } else {
                    System.out.println( "Unknown MQ Class for java: " + one.getClassName() );
                }
            } else if ( one.getObjectName().getDomain().equals( "java.nio" ) ) {
                if ( one.getClassName().equals( "com.ibm.lang.management.BufferPoolMXBeanImpl" ) ) {

                } else {
                    System.out.println( "Unknown java.nio: " + one.getClassName() );
                }
            } else if ( one.getObjectName().getDomain().equals( "JMImplementation" ) ) {
                if ( one.getClassName().equals( "javax.management.MBeanServerDelegate" ) ) {

                } else {
                    System.out.println( "Unknown JMIImplentation: " + one.getClassName() );
                }
            } else if ( one.getObjectName().getDomain().equals( "java.util.logging" ) ) {
                if ( one.getClassName().equals( "com.ibm.lang.management.LoggingMXBeanImpl" ) ) {

                } else {
                    System.out.println( "Unknown Logging: " + one.getClassName() );
                }

            } else if ( one.getObjectName().getDomain().equals( "cluster1-metrics" ) ) {
            } else if ( one.getObjectName().getDomain().equals( "org.apache.derby" ) ) {
                if ( one.getClassName().equals( "org.apache.derby.mbeans.VersionMBean" ) ) {

                } else if ( one.getClassName().equals( "org.apache.derby.mbeans.ManagementMBean" ) ) {

                } else if ( one.getClassName().equals( "org.apache.derby.mbeans.JDBCMBean" ) ) {

                } else {
                    System.out.println( "Unknown Derby Class: " + one.getClassName() );
                }

            } else if ( one.getObjectName().getDomain().equals( "java.lang" ) ) {
                if ( one.getClassName().equals( "com.ibm.lang.management.GarbageCollectorMXBeanImpl" ) ) {

                    try {

                    } catch ( Exception e ) {
                        System.out.println( "Collection Count not found" );
                    }
                    continue;
                }
            } else if ( one.getObjectName().getDomain().equals( "com.oracle.jdbc" ) ) {
                if ( one.getClassName().equals( "oracle.jdbc.driver.OracleDiagnosabilityMBean" ) ) {

                    try {

                    } catch ( Exception e ) {
                        System.out.println( "Collection Count not found" );
                    }
                    continue;
                }
            } else if ( one.getObjectName().getDomain().startsWith( "org.apache.cassandra." ) ) {
                dumpJMXObject( one, conn, mbean );
                continue;
            } else {
                System.out.println( "UnknownJMX " + one.getObjectName().getDomain() + " :: " + one.getClassName() );
                if ( DEBUG ) {
                    try {

                        for ( MBeanAttributeInfo info : mbean.getAttributes() ) {
                            System.out.println( info.getName() + ":" );
                            Object obj = conn.getAttribute( one.getObjectName(), info.getName() );
                            if ( CompositeDataSupport.class.isInstance( obj ) ) {
                                CompositeDataSupport cds = (CompositeDataSupport) obj;
                                for ( String key : cds.getCompositeType().keySet() ) {
                                    System.out.println( "\t" + key + "=" + cds.get( key ) );
                                }
                            } else {
                                if ( obj != null )
                                    System.out.println( "\t" + obj.toString() );
                            }
                        }
                    } catch ( RuntimeMBeanException e ) {
                        // do nothing
                    } catch ( Throwable e ) {
                        e.printStackTrace();
                    }
                }
                continue;

            }

        }

        return stats;
    }

    private void dumpStats( List<JMXStatistic> stats, HashMap<String, Boolean> dspProps ) {
        Collections.sort( stats );
        JMXStatisticKey oldKey = null;
        for ( JMXStatistic stat : stats ) {
            if ( stat.isEmpty() && ( dspProps == null || dspProps.get( SUPPRESS_OUTPUT ) ) ) {
                continue;
            }
            if ( oldKey == null || !stat.getKey().equals( oldKey ) ) {
                oldKey = stat.getKey();
                System.out.println( "" );
                stat.doHeader();
            }

            stat.doDetail();
        }

    }

    private void dumpJMXObject( ObjectInstance one, MBeanServerConnection conn, MBeanInfo mbean ) {
        System.out.print( "ObjectName: " + one.getObjectName() );
        try {

            for ( MBeanAttributeInfo info : mbean.getAttributes() ) {
                System.out.print( " " + info.getName() + ":" );
                try {
                    Object obj = conn.getAttribute( one.getObjectName(), info.getName() );
                    if ( CompositeDataSupport.class.isInstance( obj ) ) {
                        CompositeDataSupport cds = (CompositeDataSupport) obj;
                        for ( String key : cds.getCompositeType().keySet() ) {
                            System.out.print( key + "=" + cds.get( key ) );
                        }
                    } else {
                        if ( obj != null )
                            System.out.print( "obj:" + obj.toString() );
                    }
                } catch ( Exception e ) {
                    System.out.print( "Error" );
                }
            }

        } catch ( RuntimeMBeanException e ) {
            // do nothing
        } catch ( Throwable e ) {
            e.printStackTrace();
        }
        System.out.println( "" );
    }

}
