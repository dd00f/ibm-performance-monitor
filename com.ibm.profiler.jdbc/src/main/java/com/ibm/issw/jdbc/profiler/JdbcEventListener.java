package com.ibm.issw.jdbc.profiler;

/*
 *-----------------------------------------------------------------
 * IBM Confidential
 *
 * OCO Source Materials
 *
 * WebSphere Commerce
 *
 * (C) Copyright IBM Corp. 2013, 2014
 *
 * The source code for this program is not published or otherwise
 * divested of its trade secrets, irrespective of what has
 * been deposited with the U.S. Copyright Office.
 *-----------------------------------------------------------------
 */

/**
 * 
 * JdbcEventListener
 */
public abstract interface JdbcEventListener {
	/**
	 * IBM Copyright notice field.
	 */
	public static final String COPYRIGHT = com.ibm.commerce.copyright.IBMCopyright.SHORT_COPYRIGHT;

	/**
	 * 
	 * notifyJdbcEvent
	 * @param paramArrayOfJdbcEvent the list of jdbc events
	 */
	public abstract void notifyJdbcEvent(JdbcEvent[] paramArrayOfJdbcEvent);
}
