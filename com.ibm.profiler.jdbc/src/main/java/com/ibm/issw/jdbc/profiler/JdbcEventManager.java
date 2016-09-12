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

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * JdbcEventManager
 */
public final class JdbcEventManager {
	/**
	 * IBM Copyright notice field.
	 */
	public static final String COPYRIGHT = com.ibm.commerce.copyright.IBMCopyright.SHORT_COPYRIGHT;

	private static final List<JdbcEventListener> LISTENERS = new ArrayList<JdbcEventListener>();

	/**
	 * 
	 * addJdbcEventListener
	 * 
	 * @param listener
	 */
	public static void addJdbcEventListener(JdbcEventListener listener) {
		if (!LISTENERS.contains(listener)) {
			LISTENERS.add(listener);
		}
	}

	/**
	 * 
	 * removeJdbcEventListener
	 * 
	 * @param listener
	 */
	public static void removeJdbcEventListener(JdbcEventListener listener) {
		if (LISTENERS.contains(listener)) {
			LISTENERS.remove(listener);
		}
	}

	/**
	 * 
	 * notifyListeners
	 * 
	 * @param jdbcEvents
	 */
	public static void notifyListeners(JdbcEvent[] jdbcEvents) {
		for (int i = 0; i < LISTENERS.size(); i++) {
			LISTENERS.get(i).notifyJdbcEvent(jdbcEvents);
		}
	}
}
