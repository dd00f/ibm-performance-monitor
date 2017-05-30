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
package com.ibm.logger.jmx;

/**
 * 
 * 
 * @author Bryan Johnson
 * 
 */
public interface PerformanceLoggerManagerMXBean {

	/**
	 * Clear out the statistics stored in memory.
	 */
	public void clear();

	/**
	 * Enable in memory statistics gathering.
	 */
	public void enable();

	/**
	 * Disable in memory statistics gathering.
	 */
	public void disable();

	/**
	 * Determine if the logger is enabled.
	 * 
	 * @return True if enabled. False otherwise.
	 */
	public boolean isEnabled();

	/**
	 * Dump the performance logs to System.out in a table format.
	 */
	public void dumpToLogger();

	/**
	 * Dump the performance logs to a table format.
	 * 
	 * @return The performance logs table format.
	 */
	public String dumpToTableView();

	/**
	 * Dump the performance logs to a CSV format
	 * 
	 * @return The CSV
	 */
	public String dumpToCsv();

}