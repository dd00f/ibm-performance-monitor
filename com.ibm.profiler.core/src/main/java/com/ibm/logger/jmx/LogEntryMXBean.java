// IBM Confidential OCO Source Material
// 5725-F81 (C) COPYRIGHT International Business Machines Corp. 2011,2014
// The source code for this program is not published or otherwise divested
// of its trade secrets, irrespective of what has been deposited with the
// U.S. Copyright Office.
package com.ibm.logger.jmx;

public interface LogEntryMXBean {

    public void clear();

    public double getAverage();

    public String getId();

    public float getMaximum();

    public float getMinimum();

    public String getName();

    public long getNumCalls();

    public long getFailedCalls();

    public float getTotal();
    
    public String getType();


}
