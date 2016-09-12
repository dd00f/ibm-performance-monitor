package com.ibm.profiler.client.jmx;

//IBM Confidential OCO Source Material
//5725-F81 (C) COPYRIGHT International Business Machines Corp. 2011,2014
//The source code for this program is not published or otherwise divested
//of its trade secrets, irrespective of what has been deposited with the
//U.S. Copyright Office.

import javax.management.MBeanInfo;
import javax.management.MBeanServerConnection;
import javax.management.ObjectInstance;

/**
 * @author Bryan Johnson
 * 
 */
public abstract class AbstractJMXStatistic implements JMXStatistic {
    private JMXStatisticKey key = null;

    public abstract void _parse( MBeanServerConnection conn, MBeanInfo mbean, ObjectInstance one );

    public int compareTo( Object arg0 ) {
        if ( arg0 == null )
            return -1;
        try {
            if ( ( (AbstractJMXStatistic) arg0 ).key != null )
                if ( ( (AbstractJMXStatistic) arg0 ).key.compareTo( key ) != 0 ) {
                    return ( (AbstractJMXStatistic) arg0 ).key.compareTo( key );
                }

            return ( (AbstractJMXStatistic) arg0 )._compareTo( this );

        } catch ( Exception e ) {
            return 0;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ibm.profiler.client.jmx.JMXStatistic#getKey()
     */
    public JMXStatisticKey getKey() {
        return key;
    }

    protected abstract int _compareTo( AbstractJMXStatistic abstractMEGJMXStatistics );

    @Override
    public boolean equals( Object obj ) {
        if ( this == obj )
            return true;
        if ( obj == null )
            return false;
        if ( getClass() != obj.getClass() )
            return false;
        AbstractJMXStatistic other = (AbstractJMXStatistic) obj;
        if ( key == null ) {
            if ( other.key != null )
                return false;
        } else if ( !key.equals( other.key ) )
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ( ( key == null ) ? 0 : key.hashCode() );
        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ibm.profiler.client.jmx.JMXStatistic#parse(javax.management.MBeanServerConnection,
     * javax.management.MBeanInfo, javax.management.ObjectInstance)
     */
    public final JMXStatistic parse( MBeanServerConnection conn, MBeanInfo mbean, ObjectInstance one ) {

        if ( one == null ) {
            return this;
        }
        key = new JMXStatisticKey( one.getObjectName().getDomain(), one.getClassName() );
        _parse( conn, mbean, one );
        return this;
    }

    @Override
    public String toString() {
        return "AbstractJMXStatistics [key=" + key + "]";
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ibm.profiler.client.jmx.JMXStatistic#isEmpty()
     */
    public boolean isEmpty() {
        // TODO Auto-generated method stub
        return false;
    }

}
