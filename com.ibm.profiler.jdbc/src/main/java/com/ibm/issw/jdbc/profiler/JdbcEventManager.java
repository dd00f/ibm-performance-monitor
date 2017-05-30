/*
 * Copyright 2017 Steve McDuff
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ibm.issw.jdbc.profiler;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * JdbcEventManager
 */
public final class JdbcEventManager {

	private static final List<JdbcEventListener> LISTENERS = new ArrayList<JdbcEventListener>();

	/**
	 * 
	 * addJdbcEventListener
	 * 
	 * @param listener the listener
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
	 * @param listener the listener to remove
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
	 * @param jdbcEvents the events to notify.
	 */
	public static void notifyListeners(JdbcEvent[] jdbcEvents) {
		for (int i = 0; i < LISTENERS.size(); i++) {
			LISTENERS.get(i).notifyJdbcEvent(jdbcEvents);
		}
	}
}
