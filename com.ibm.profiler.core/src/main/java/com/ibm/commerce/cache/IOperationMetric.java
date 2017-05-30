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

import java.util.List;

/**
 * Operation metric gathering structure.
 */
public interface IOperationMetric {

	/**
	 * Clean the properties. This can be invoked before print properties for the
	 * exit logging.
	 */
	public void cleanProperties();
	
	/**
	 * set property using a set of set and value of each pair stored in
	 * properties
	 * 
	 * @param key
	 *            key part of property pair
	 * @param value
	 *            value part of property pair
	 */
	public void setProperty(String key, String value);

	/**
	 * retrieve properties given any key value
	 * 
	 * @param key
	 *            key value to specify which value to return
	 * @return a mapping value for the key of the property is returned
	 */
	public String getProperty(String key);

	/**
	 * @return the operation start time.
	 */
	public long getStartTime();

	/**
	 * @param startTime
	 *            the operation start time.
	 */
	public void setStartTime(long startTime);

	/**
	 * @return the operation stop time.
	 */
	public long getStopTime();

	/**
	 * @param stopTime
	 *            the operation stop time.
	 */
	public void setStopTime(long stopTime);

	/**
	 * @return the operation duration
	 */
	public long getDuration();

	/**
	 * @param duration
	 *            the operation duration
	 */
	public void setDuration(long duration);

	/**
	 * @return the result size
	 */
	public int getResultSize();

	/**
	 * @param resultSize
	 *            the result size
	 */
	public void setResultSize(int resultSize);

	/**
	 * @return the key value pair list
	 */
	public List<String> getKeyValuePairList();

	/**
	 * @param keyValuePairList
	 *            the key value pair list
	 */
	public void setKeyValuePairList(List<String> keyValuePairList);

	/**
	 * @return the operation name
	 */
	public String getOperationName();

	/**
	 * @param operationName
	 *            the operation name
	 */
	public void setOperationName(String operationName);

	/**
	 * Is the operation successful.
	 * 
	 * @return true if the operation was successful.
	 */
	public boolean isSuccessful();

	/**
	 * Set the successful flag.
	 * 
	 * @param isSuccessful
	 *            the new successful flag.
	 */
	public void setSuccessful(boolean isSuccessful);


	/**
	 * check to see if an operation matches another one. To match, the operation
	 * name and all the key value pairs must match.
	 * 
	 * @param metrics
	 *            the other metric to match
	 * @return true if the other metric matches.
	 */
	public boolean matches(IOperationMetric metrics);


	/**
	 * @return is the result fetched from cache
	 */
	public boolean isResultFetchedFromCache();

	/**
	 * @param isResultFetchedFromCache
	 *            is the result fetched from cache
	 */
	public void setResultFetchedFromCache(boolean isResultFetchedFromCache);

	/**
	 * @return is the operation cache enabled
	 */
	public boolean isOperationCacheEnabled();

	/**
	 * @param isOperationCacheEnabled
	 *            is the operation cache enabled
	 */
	public void setOperationCacheEnabled(boolean isOperationCacheEnabled);



	/**
	 * @return the object identifier
	 */
	public long getIdentifier();

	/**
	 * @param identifier
	 *            the object identifier
	 */
	public void setIdentifier(long identifier);

	/**
	 * @return the parent identifier
	 */
	public long getParentIdentifier();

	/**
	 * @param parentIdentifier
	 *            the parent identifier
	 */
	public void setParentIdentifier(long parentIdentifier);

}
