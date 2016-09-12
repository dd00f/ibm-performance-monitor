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

import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 
 * JdbcEventQueue
 */
public final class JdbcEventQueue {
	/**
	 * IBM Copyright notice field.
	 */
	public static final String COPYRIGHT = com.ibm.commerce.copyright.IBMCopyright.SHORT_COPYRIGHT;
	
	private static final Logger LOG = Logger.getLogger(JdbcEventQueue.class
			.getName());

	private final LinkedList<JdbcEvent[]> list = new LinkedList<JdbcEvent[]>();

	/**
	 * 
	 * enqueue
	 * @param events
	 */
	public void enqueue(JdbcEvent[] events) {
		if (LOG.isLoggable(Level.FINE)) {
			LOG.fine("Queueing " + events);
		}
		synchronized (this.list) {
			this.list.add(events);
		}
	}

	/**
	 * 
	 * dequeue
	 * @return the dequeued event
	 */
	public JdbcEvent[] dequeue() {
		JdbcEvent[] events = null;

		synchronized (this.list) {
			events = this.list.getFirst();
			this.list.remove(events);
			if (LOG.isLoggable(Level.FINE)) {
				LOG.fine("Dequeueing " + events);
			}
		}
		return events;
	}

	/**
	 * 
	 * isEmpty
	 * @return true if emtpy
	 */
	public boolean isEmpty() {
		return this.list.isEmpty();
	}

	/**
	 * 
	 * clear
	 */
	public void clear() {
		this.list.clear();
	}
}
