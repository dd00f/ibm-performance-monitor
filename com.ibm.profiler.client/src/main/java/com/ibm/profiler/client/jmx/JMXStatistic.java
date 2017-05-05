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
public interface JMXStatistic extends Comparable<Object> {

    /**
     * 
     * @param conn connection
     * @param mbean bean info
     * @param one the object instance
     * @return the JMX statistics
     */
    public JMXStatistic parse( MBeanServerConnection conn, MBeanInfo mbean, ObjectInstance one );

    /**
     * @return are the statistics empty
     */
    public boolean isEmpty();

    /**
     * @return get the statistics key
     */
    public JMXStatisticKey getKey();

    /**
     * write a header
     */
    public void doHeader();

    /**
     * write details.
     */
    public void doDetail();

}
