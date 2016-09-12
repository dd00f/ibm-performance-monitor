package com.ibm.commerce.copyright;

/*
 *-----------------------------------------------------------------
 * IBM Confidential
 *
 * OCO Source Materials
 *
 * WebSphere Commerce
 *
 * (C) Copyright IBM Corp. 1996, 2014
 *
 * The source code for this program is not published or otherwise
 * divested of its trade secrets, irrespective of what has
 * been deposited with the U.S. Copyright Office.
 *-----------------------------------------------------------------
 */
////////////////////////////////////////////////////////////////////////////////
//
// Change History
//
// YYMMDD    F/D#   WHO       Description
//------------------------------------------------------------------------------
//
////////////////////////////////////////////////////////////////////////////////

//$ANALYSIS-IGNORE
/**
 * Contains the IBM copyright information for WebSphere Commerce.
 */
public class IBMCopyright {

	/**
	 * <p>The full version of the WebSphere Commerce copyright statement.</p>
	 * <pre>
	 * Licensed Materials - Property of IBM
	 * WebSphere Commerce
	 * (c) Copyright International Business Machines Corporation 1996,2008.
	 * All rights reserved
	 * US Government Users Restricted Rights - Use, duplication or
	 * disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
	 * </pre>
	 */
	public static final String LONG_COPYRIGHT =
		"Licensed Materials - Property of IBM" +
	    "WebSphere Commerce" +
	    "(c) Copyright International Business Machines Corporation 1996,2008." +
	    "All rights reserved" +
	    "US Government Users Restricted Rights - Use, duplication or" +
		"disclosure restricted by GSA ADP Schedule Contract with IBM Corp.";

	/**
	 * <p>The short version of the WebSphere Commerce copyright statement.</p>
	 * <code>
	 * (c) Copyright International Business Machines Corporation 1996,2008
	 * </code>
	 */
	public static final String SHORT_COPYRIGHT =
	    "(c) Copyright International Business Machines Corporation 1996,2008";
	/**
	 * The IBM copyright notice field.
	 */
	public static final String COPYRIGHT = SHORT_COPYRIGHT;
/**
 * Creates the IBM Copyright object.
 */
private IBMCopyright() {
	super();
}

/**
 * Returns the long version of the WebSphere Commerce copyright statement.
 * @return The full version of the copyright statement.
 */
public static String getLongCopyright() {
	return LONG_COPYRIGHT;
}

/**
 * Returns the short version of the WebSphere Commerce copyright statement.
 * @return The short version of the WebSphere Commerce copyright statement.
 */
public static String getShortCopyright() {
	return SHORT_COPYRIGHT;
}
}
