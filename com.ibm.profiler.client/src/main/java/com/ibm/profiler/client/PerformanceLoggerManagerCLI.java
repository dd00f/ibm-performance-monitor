package com.ibm.profiler.client;

//IBM Confidential OCO Source Material
//5725-F81 (C) COPYRIGHT International Business Machines Corp. 2011,2014
//The source code for this program is not published or otherwise divested
//of its trade secrets, irrespective of what has been deposited with the
//U.S. Copyright Office.

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.management.ManagementFactory;

import javax.management.JMX;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import com.ibm.commerce.cache.CacheUtilities;
import com.ibm.logger.jmx.PerformanceLoggerManagerMXBean;

/**
 * @author Bryan Johnson
 * 
 */
public class PerformanceLoggerManagerCLI {

	
	private static String dumpToCsvFile = null;
	
	private static String dumpToTableFile = null;
	
    /**
     * @param args arguments
     */
    public static void main( String[] args ) {
        String host = "127.0.0.1";
        String port = "0";
        String service = null;
        if ( args != null ) {
            String last = "";
            for ( String arg : args ) {
                if ( last.equalsIgnoreCase( "-host" ) ) {
                    host = arg;
                }
                if ( last.equalsIgnoreCase( "-service" ) ) {
                    service = arg;
                }
                if ( last.equalsIgnoreCase( "-port" ) ) {
                    port = arg;
                }
                if ( last.equalsIgnoreCase( "-dumpCsv" ) ) {
                	dumpToCsvFile = arg;
                }       
                if ( last.equalsIgnoreCase( "-dumpTable" ) ) {
                	dumpToTableFile = arg;
                }                   
                if ( arg.equalsIgnoreCase( "-usage" ) ) {
                    usage();
                    System.exit( 0 );
                }
                last = arg;

            }
        }

        MBeanServerConnection conn = getMBeanServerConnection( host, port, service );

        PerformanceLoggerManagerMXBean myBean = getPerformanceLoggerManagerMBean( conn );
        
        if( dumpToCsvFile != null ) {
        	String fileContent = myBean.dumpToCsv();
        	writeToFile(fileContent, dumpToCsvFile);
        }
        
        if( dumpToTableFile != null ) {
        	String fileContent = myBean.dumpToTableView();
        	writeToFile(fileContent, dumpToTableFile);
        }
        
        if ( myBean == null ) {
            System.out.println( "Unable to get mbean for PerformanceLoggerManager" );
            System.exit( 1 );
        } else {
            myBean.enable();
            if ( args != null ) {
                for ( String arg : args ) {
                    if ( arg.equalsIgnoreCase( "-enable" ) ) {
                        myBean.enable();
                    }
                    if ( arg.equalsIgnoreCase( "-disable" ) ) {
                        myBean.disable();
                    }
                    if ( arg.equalsIgnoreCase( "-clear" ) ) {
                        myBean.clear();
                    }
                    if ( arg.equalsIgnoreCase( "-dump" ) ) {
                        myBean.dumpToLogger();
                    }
                }
            }
        }
    }

    private static void writeToFile(String fileContent, String fileName) {
    	PrintWriter writer = null;
    	try {
			writer = new PrintWriter(new File(fileName), "UTF-8");
			writer.append(fileContent);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit( 1 );
		}
		finally {
			CacheUtilities.closeQuietly(writer);
		}
	}

	/**
     * MBean helper class
     * 
     * @param conn the mbean connection
     * @return the performance logger
     */
    protected static PerformanceLoggerManagerMXBean getPerformanceLoggerManagerMBean( MBeanServerConnection conn ) {
        String JMX_DOMAIN = "com.ibm.profiler";

        String PERFORMANCE_DOMAIN = "PerformanceDomain";

        try {
            ObjectName objectName = new ObjectName( JMX_DOMAIN + ":" + PERFORMANCE_DOMAIN + "=PerformanceManager" );

            // ObjectName objectName = new ObjectName( "com.ibm.profiler" + ":" + "PerformanceDomain" +
            // "=PerformanceManager" );
            if ( conn.isRegistered( objectName ) ) {
                return JMX.newMBeanProxy( conn, objectName, PerformanceLoggerManagerMXBean.class );
            }
        } catch ( IOException e1 ) {
            e1.printStackTrace();
        } catch ( MalformedObjectNameException e ) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * JMX Connector helper class
     * 
     * @param host host name
     * @param port port
     * @return the mbean server connection
     */
    protected static MBeanServerConnection getMBeanServerConnection( String host, String port ) {
        return getMBeanServerConnection( host, port, null );
    }

    /**
     * JMX Connector helper class
     * 
     * @param host host name
     * @param port port number
     * @param service service name
     * @return the mbean connection
     */
    protected static MBeanServerConnection getMBeanServerConnection( String host, String port, String service ) {
        try {
            if ( ( host == null || port == null ) && service == null ) {
                System.out.println( "Using local JMV connection" );
                return ManagementFactory.getPlatformMBeanServer();

            }

            if ( service == null ) {
                service = "service:jmx:rmi:///jndi/rmi://" + host + ":" + port + "/jmxrmi";
            }

            System.out.println( "Connecting to : " + service );
            JMXServiceURL url = new JMXServiceURL( service );
            JMXConnector jmx = JMXConnectorFactory.connect( url );
            return jmx.getMBeanServerConnection();
        } catch ( Throwable e ) {
            System.out.println( "Unable to connect to JMX Server: " + service + " : " + e.getMessage() );
        }
        System.out.println( "Reverting to local jvm connection" );
        return ManagementFactory.getPlatformMBeanServer();

    }

    private static void usage() {
        System.out.println( "PerformanceLoggerManagerCLI options :" );
        System.out.println( "  -host <host name>\n\t\thost name of the JMX server. Default to local host." );
        System.out.println( "  -port <port>\n\t\tport of the JMX server. Mandatory." );
        System.out.println( "  -service <service name>\n\t\tJMX service name, optional, default to null" );
        System.out.println( "  -enable\n\t\tenable in memory performance metric gathering on the remote host." );
        System.out.println( "  -disable\n\t\tdisable in memory performance metric gathering on the remote host." );
        System.out.println( "  -clear\n\t\tclear the in memory performance metric on the remote host." );
        System.out.println( "  -dump\n\t\tmake the remote host dump its performance metrics on the console." );
        System.out.println( "  -dumpCsv <file name>\n\t\tOutput the remote host performance metrics to a local file in CSV format.\n\t\tRequires version 1.3.0.0 or later." );
        System.out.println( "  -dumpTable <file name>\n\t\tOutput the remote host performance metrics to a local file in table format.\n\t\tRequires version 1.3.0.0 or later." );
        System.out.println( "  -usage\n\t\tdisplay this message." );
       
    }

}
