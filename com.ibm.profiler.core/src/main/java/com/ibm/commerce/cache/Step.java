/**
 * 
 */
package com.ibm.commerce.cache;

/*
 *-----------------------------------------------------------------
 * IBM Confidential
 *
 * OCO Source Materials
 *
 * WebSphere Commerce
 *
 * (C) Copyright IBM Corp. 2012
 *
 * The source code for this program is not published or otherwise
 * divested of its trade secrets, irrespective of what has
 * been deposited with the U.S. Copyright Office.
 *-----------------------------------------------------------------
 */

/**
 * A step is used to represent a numerical range between a start and a stop
 * number.
 */
public class Step {
	/**
	 * IBM Copyright notice field.
	 */
	public static final String COPYRIGHT = com.ibm.commerce.copyright.IBMCopyright.SHORT_COPYRIGHT;

	/**
	 * start number value
	 */
	private long start = 0;

	/**
	 * stop number value
	 */
	private long stop = Long.MAX_VALUE;

	/**
	 * gets the start value.
	 * 
	 * @return the start value.
	 */
	public long getStart() {
		return start;
	}

	/**
	 * get the stop value.
	 * 
	 * @return the stop value.
	 */
	public long getStop() {
		return stop;
	}

	/**
	 * Set the start value.
	 * 
	 * @param start
	 *            the start value.
	 */
	public void setStart(long start) {
		this.start = start;
	}

	/**
	 * Set the stop value.
	 * 
	 * @param stop
	 *            the stop value.
	 */
	public void setStop(long stop) {
		this.stop = stop;
	}

	/**
	 * toString method override.
	 * 
	 * @return the string representation of this step.
	 */
	@Override
    public String toString() {
		return start + "-" + stop;
	}
}