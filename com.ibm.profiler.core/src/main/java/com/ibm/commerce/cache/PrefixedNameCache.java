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
