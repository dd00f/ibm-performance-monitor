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