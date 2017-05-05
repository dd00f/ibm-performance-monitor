package com.ibm.profiler.client.jmx;

//IBM Confidential OCO Source Material
//5725-F81 (C) COPYRIGHT International Business Machines Corp. 2011,2014
//The source code for this program is not published or otherwise divested
//of its trade secrets, irrespective of what has been deposited with the
//U.S. Copyright Office.

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanServerConnection;
import javax.management.ObjectInstance;
import javax.management.openmbean.CompositeDataSupport;

/**
 * @author Bryan Johnson
 * 
 */
public class TotalLogEntry extends AbstractJMXStatistic {

    @Override
    public String toString() {
        return "TotalLogEntry [key=" + getKey() + ", id=" + id + ", avg=" + avg + ", min=" + min + ", max=" + max + ", total=" + total + ", calls=" + calls + ", type=" + type + "]";
    }

    private final String format = "%14.14s %14.14s %14.14s %14.14s %14.14s %10.10s %s";

    private final String DASH = "--------------------------------------------------------------------------------------------";

    private boolean DEBUG = false;

    private static final long NANOSECS_TO_MILLISECS = 1000000;

    private String id;

    private String avg;

    private String min;

    private String max;

    private String total;

    private String calls;

    private String type;

    @Override
    protected int _compareTo( AbstractJMXStatistic arg0 ) {
        if ( arg0 == null )
            return -1;
        try {
            if ( id == null )
                return -1;
            if ( ( (TotalLogEntry) arg0 ).id != null )
                if ( ( (TotalLogEntry) arg0 ).id.compareTo( id ) != 0 ) {
                    return ( (TotalLogEntry) arg0 ).id.compareTo( id );
                }
            return 0;
        } catch ( Exception e ) {
            return 0;
        }
    }


    @Override
    public void _parse( MBeanServerConnection conn, MBeanInfo mbean, ObjectInstance one ) {

        try {
            id = (String) conn.getAttribute( one.getObjectName(), "Id" );
            avg = conn.getAttribute( one.getObjectName(), "AverageDuration" ).toString();
            try {
                double avgDouble = Double.parseDouble( avg ) / NANOSECS_TO_MILLISECS;
                avg = String.format( "%.2f", avgDouble );
            } catch ( Exception e ) {
                e.printStackTrace();
            }
            try {
                min = conn.getAttribute( one.getObjectName(), "MinimumDuration" ).toString();
                long tmpLong = Long.parseLong( min ) / NANOSECS_TO_MILLISECS;
                min = String.format( "%d", tmpLong );
            } catch ( Exception e ) {
                e.printStackTrace();
            }
            try {
                max = conn.getAttribute( one.getObjectName(), "MaximumDuration" ).toString();
                long tmpLong = Long.parseLong( max ) / NANOSECS_TO_MILLISECS;
                max = String.format( "%d", tmpLong );
            } catch ( Exception e ) {
                e.printStackTrace();
            }
            try {
                total = conn.getAttribute( one.getObjectName(), "TotalDuration" ).toString();
                float tmpFloat = Float.parseFloat( total ) / NANOSECS_TO_MILLISECS;
                total = String.format( "%.0f", tmpFloat );
            } catch ( Exception e ) {
                e.printStackTrace();
            }
            calls = conn.getAttribute( one.getObjectName(), "CallCount" ).toString();
            type = conn.getAttribute( one.getObjectName(), "IntervalName" ).toString();
        } catch ( Throwable t ) {
            t.printStackTrace();
        }

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
                        System.out.println( "\t" + obj.toString() );
                    }
                }
            } catch ( Throwable e ) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void doDetail() {
        int NANO = 1000000;

        if ( type.equals( "TIMER" ) || type.equals( "STATISTIC" ) )
            System.out.println( String.format( format, calls, parseLongAndDivide( avg, NANO ), parseLongAndDivide( total, NANO ),
            // avg,total,
                parseLongAndDivide( min, NANO ), parseLongAndDivide( max, NANO ), type, id ) );
        else
            System.out.println( String.format( format, calls, avg, total, min, max, type, id ) );
    }

    @Override
    public void doHeader() {
        System.out.println( "IBM B2B PerformanceStatistics\n" );
        System.out.println( String.format( format, "Num Calls", "Average", "Total", "Minimum", "Maximum", "Type", "Statistic ID" ) );
        System.out.println( String.format( format, DASH, DASH, DASH, DASH, DASH, DASH, DASH.substring( 0, 30 ) ) );

    }

    /**
     * @return the execution time average
     */
    public String getAvg() {
        return avg;
    }

    /**
     * @return the call count.
     */
    public String getCalls() {
        return calls;
    }

    /**
     * @return the statistics ID.
     */
    public String getId() {
        return id;
    }

    /**
     * @return maximum execution time.
     */
    public String getMax() {
        return max;
    }

    /**
     * @return minimum execution time
     */
    public String getMin() {
        return min;
    }

    /**
     * @return total execution time.
     */
    public String getTotal() {
        return total;
    }

    /**
     * @return the type of metrics.
     */
    public String getType() {
        return type;
    }

    @Override
    public boolean isEmpty() {
        if ( id == null ) {
            return true;
        }

        return false;
    }

    private String parseLongAndDivide( String initVal, int divider ) {
        String retValue = initVal;
        try {
            double val = Double.parseDouble( initVal );
            double dblval = val / divider;
            retValue = String.format( "%.1f", dblval );
        } catch ( Throwable t ) {
            t.printStackTrace();
        }
        return retValue;
    }

}
