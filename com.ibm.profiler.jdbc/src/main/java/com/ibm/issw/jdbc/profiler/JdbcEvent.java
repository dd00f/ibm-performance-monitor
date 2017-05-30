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


import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 
 * JdbcEvent
 */
public final class JdbcEvent implements Serializable {

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
	 * @param sqlStatement the statement
	 */
	public void setSqlStatement(String sqlStatement) {
		this.sqlStatement = sqlStatement;
	}

	/**
	 * 
	 * getStartTime
	 * @return getStartTime the start time
	 */
	public long getStartTime() {
		return startTime;
	}

	/**
	 * 
	 * setStartTime
	 * @param startTime the start time
	 */
	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	/**
	 * 
	 * getStopTime
	 * @return the stop time
	 */
	public long getStopTime() {
		return stopTime;
	}

	/**
	 * 
	 * setStopTime
	 * @param stopTime the stop time
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
	 * @param executeTime the execution time
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
	 * @param prepareTime the prepare time
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
	 * @param threadName the thread name
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
	 * @param rowsUpdated the number of rows updated
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
	 * @param rowsRead the number of rows read
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
	 * @param sequence the sequence number
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
	 * @param readSize the read size
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
	 * @param transactionTime the transaction time
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
	 * @param tables the tables read
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
	 * @param statementType the statement type
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
	 * @param stack the stack elements
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
	 * @param parameters the parameters 
	 */
	public void setParameters(Map<Integer, Serializable> parameters) {
		this.parameters = parameters;
	}

	/**
	 * getTransactionId
	 * @return the transaction id
	 */
    public String getTransactionId()
    {
        return transactionId;
    }

    /**
     * set the transaction id
     * @param transactionId the transaction id
     */
    public void setTransactionId(String transactionId)
    {
        this.transactionId = transactionId;
    }
}
