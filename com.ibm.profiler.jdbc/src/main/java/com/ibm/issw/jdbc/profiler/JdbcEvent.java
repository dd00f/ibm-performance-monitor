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

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 
 * JdbcEvent
 */
public final class JdbcEvent implements Serializable {
	/**
	 * IBM Copyright notice field.
	 */
	public static final String COPYRIGHT = com.ibm.commerce.copyright.IBMCopyright.SHORT_COPYRIGHT;
	private static final long serialVersionUID = 7516690936155498183L;
	private String sqlStatement = "";

	private long startTime = -1;

	private long stopTime = -1;

	private long executeTime = -1;

	private long prepareTime = -1;

	private String threadName = "unknown";

	private int rowsUpdated = 0;

	private int rowsRead = 0;

	private int sequence = -1;

	private long readSize = 0;

	private long transactionTime = -1;

	private String tables = "unknown";

	private String statementType = "unknown";
	
	private String transactionId = null;
	
	private StackTraceElement[] stack;
	private Map<Integer, Serializable> parameters = new HashMap<Integer, Serializable>();

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
    public String toString() {
		StringBuilder builder = new StringBuilder(super.toString());
		builder.append(':');
		builder.append("sequence=");
		builder.append(this.getSequence());
		builder.append(',');
		builder.append("threadName=");
		builder.append(this.getThreadName());
		builder.append(',');
		builder.append("executeTime=");
		builder.append(this.getExecuteTime());
		builder.append(',');
		builder.append("rowsRead=");
		builder.append(this.getRowsRead());
		builder.append(',');
		builder.append("rowsUpdated=");
		builder.append(this.getRowsUpdated());
		builder.append(',');
		builder.append("tables=");
		builder.append(this.getTables());
		builder.append(',');
		builder.append("txTime=");
		builder.append(this.getTransactionTime());
		builder.append(',');
		builder.append("sql=");
		builder.append(this.getSqlStatement());

		return builder.toString();
	}

	/**
	 * 
	 * getSqlStatement
	 * @return getSqlStatement
	 */
	public String getSqlStatement() {
		return sqlStatement;
	}

	/**
	 * 
	 * setSqlStatement
	 * @param sqlStatement
	 */
	public void setSqlStatement(String sqlStatement) {
		this.sqlStatement = sqlStatement;
	}

	/**
	 * 
	 * getStartTime
	 * @return getStartTime
	 */
	public long getStartTime() {
		return startTime;
	}

	/**
	 * 
	 * setStartTime
	 * @param startTime
	 */
	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	/**
	 * 
	 * getStopTime
	 * @return getStopTime
	 */
	public long getStopTime() {
		return stopTime;
	}

	/**
	 * 
	 * setStopTime
	 * @param stopTime
	 */
	public void setStopTime(long stopTime) {
		this.stopTime = stopTime;
	}

	/**
	 * 
	 * getExecuteTime
	 * @return getExecuteTime
	 */
	public long getExecuteTime() {
		return executeTime;
	}

	/**
	 * 
	 * setExecuteTime
	 * @param executeTime
	 */
	public void setExecuteTime(long executeTime) {
		this.executeTime = executeTime;
	}

	/**
	 * 
	 * getPrepareTime
	 * @return getPrepareTime
	 */
	public long getPrepareTime() {
		return prepareTime;
	}

	/**
	 * 
	 * setPrepareTime
	 * @param prepareTime
	 */
	public void setPrepareTime(long prepareTime) {
		this.prepareTime = prepareTime;
	}

	/**
	 * 
	 * getThreadName
	 * @return getThreadName
	 */
	public String getThreadName() {
		return threadName;
	}

	/**
	 * 
	 * setThreadName
	 * @param threadName
	 */
	public void setThreadName(String threadName) {
		this.threadName = threadName;
	}

	/**
	 * 
	 * getRowsUpdated
	 * @return getRowsUpdated
	 */
	public int getRowsUpdated() {
		return rowsUpdated;
	}

	/**
	 * 
	 * setRowsUpdated
	 * @param rowsUpdated
	 */
	public void setRowsUpdated(int rowsUpdated) {
		this.rowsUpdated = rowsUpdated;
	}

	/**
	 * 
	 * getRowsRead
	 * @return getRowsRead
	 */
	public int getRowsRead() {
		return rowsRead;
	}

	/**
	 * 
	 * setRowsRead
	 * @param rowsRead
	 */
	public void setRowsRead(int rowsRead) {
		this.rowsRead = rowsRead;
	}

	/**
	 * 
	 * getSequence
	 * @return getSequence
	 */
	public int getSequence() {
		return sequence;
	}

	/**
	 * 
	 * setSequence
	 * @param sequence
	 */
	public void setSequence(int sequence) {
		this.sequence = sequence;
	}

	/**
	 * 
	 * getReadSize
	 * @return getReadSize
	 */
	public long getReadSize() {
		return readSize;
	}

	/**
	 * 
	 * setReadSize
	 * @param readSize
	 */
	public void setReadSize(long readSize) {
		this.readSize = readSize;
	}

	/**
	 * 
	 * getTransactionTime
	 * @return getTransactionTime
	 */
	public long getTransactionTime() {
		return transactionTime;
	}

	/**
	 * 
	 * setTransactionTime
	 * @param transactionTime
	 */
	public void setTransactionTime(long transactionTime) {
		this.transactionTime = transactionTime;
	}

	/**
	 * 
	 * getTables
	 * @return getTables
	 */
	public String getTables() {
		return tables;
	}

	/**
	 * 
	 * setTables
	 * @param tables
	 */
	public void setTables(String tables) {
		this.tables = tables;
	}

	/**
	 * 
	 * getStatementType
	 * @return getStatementType
	 */
	public String getStatementType() {
		return statementType;
	}

	/**
	 * 
	 * setStatementType
	 * @param statementType
	 */
	public void setStatementType(String statementType) {
		this.statementType = statementType;
	}

	/**
	 * 
	 * getStack
	 * @return getStack
	 */
	public StackTraceElement[] getStack() {
		return stack;
	}

	/**
	 * 
	 * setStack
	 * @param stack
	 */
	public void setStack(StackTraceElement[] stack) {
		this.stack = stack;
	}

	/**
	 * 
	 * getParameters
	 * @return parameters
	 */
	public Map<Integer, Serializable> getParameters() {
		return parameters;
	}

	/**
	 * 
	 * setParameters
	 * @param parameters
	 */
	public void setParameters(Map<Integer, Serializable> parameters) {
		this.parameters = parameters;
	}

    public String getTransactionId()
    {
        return transactionId;
    }

    public void setTransactionId(String transactionId)
    {
        this.transactionId = transactionId;
    }
}
