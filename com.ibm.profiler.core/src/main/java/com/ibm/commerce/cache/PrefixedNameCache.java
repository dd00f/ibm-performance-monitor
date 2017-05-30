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
package com.ibm.commerce.cache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Cache used to store names with a prefix.
 */
public class PrefixedNameCache {

	private String prefix;

	/**
	 * Constructor.
	 * 
	 * @param setPrefix
	 *            The prefix to append to every name.
	 */
	public PrefixedNameCache(String setPrefix) {
		prefix = setPrefix;
	}

	private final Map<String, String> taskNameMap = new ConcurrentHashMap<String, String>();

	/**
	 * Get a prefixed name.
	 * 
	 * @param name
	 *            The original name.
	 * @return name with a prefix.
	 */
	public String getPrefixedName(String name) {
		String returnValue = taskNameMap.get(name);
		if (returnValue == null) {
			returnValue = prefix + name;
			taskNameMap.put(name, returnValue);
		}
		return returnValue;
	}

	/**
	 * Clear the cache.
	 */
	public void clear() {
		taskNameMap.clear();
	}

}
