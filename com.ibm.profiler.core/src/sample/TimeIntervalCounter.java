/*
 ******************************************************************************
 * 
 * IBM Confidential
 * 
 * OCO Source Materials
 * 
 * 5725D06, 5725Q72
 * 
 * (C) Copyright IBM Corp. 2013, 2015
 * 
 * The source code for this program is not published or otherwise
 * divested of its trade secrets, irrespective of what has
 * been deposited with the U.S. Copyright Office.
 *
 *******************************************************************************
*/
package com.ibm.commerce.cache;

/**
 * Interface for an operation counter that reports how many operations have occurred since a given time in the past. in
 * the past.
 * 
 * @author rpharris
 * 
 */
public interface TimeIntervalCounter {

    /**
     * Increment the count by 1
     */
    public void increment();

    /**
     * Increment the count by a specified amount
     * 
     * @param byCount the amount to increment by
     */
    public void increment( long byCount );

    /**
     * Get the count since the time specified.
     * 
     * @param sinceMillisAgo how far back to accumulate counts
     * @return the approximate number of increments since sinceMillisAgo milliseconds ago
     */
    public long getCount( long sinceMillisAgo );

}