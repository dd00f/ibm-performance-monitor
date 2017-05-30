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
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ibm.commerce.cache.LoggingHelper;
import com.ibm.commerce.cache.MetricFileLoader;
import com.ibm.commerce.cache.OperationMetric;
import com.ibm.logger.PerformanceLogger;
import com.ibm.service.detailed.JdbcLogger;

/**
 * 
 * JdbcProfiler
 */
public final class JdbcProfiler {



    public static final String JDBC_ALL_OPERATIONS = "JDBC_All_Operations";


	private static final String COMMIT = "commit";

	private static volatile boolean profilingEnabled = true;

	private static volatile boolean stackCaptureEnabled = false;

	private static volatile boolean eventListeningEnabled = false;

	private static volatile boolean eventMeasurementEnabled = true;
	
	private static final String CLASSNAME = JdbcProfiler.class
			.getName();
	
	private static final Logger LOG = Logger.getLogger(CLASSNAME);

	private static final JdbcProfiler PROFILER = new JdbcProfiler();
	/** operation type constant */
	public static final String OP_PREPARE = "prepareStatement";
	/** operation type constant */
	public static final String OP_EXECUTE_QUERY = "executeQuery";
	/** operation type constant */
	public static final String OP_EXECUTE_UPDATE = "executeUpdate";
	
    /** operation type constant */
	public static final String OP_EXECUTE_BATCH = "executeBatch";

	/** operation type constant */
	public static final String OP_ALLOCATE_CON = "allocateConnection";
	/** operation type constant */
	public static final String OP_COMMIT = COMMIT;
	/** operation type constant */
	public static final String OP_ROLLBACK = "rollback";
	/** operation type constant */
	public static final String OP_UOW = "unitOfWork";
	/** operation type constant */
	public static final String PREPARED = "prepared";
	/** operation type constant */
	public static final String CALLABLE = "callable";
	/** operation type constant */
	public static final String STATEMENT = "statement";
    /** operation type constant */
    public static final String TRANSACTION_ID = "transactionId";

	/**
	 * Each thread contains a list of all the JDBC events that occurred after
	 * the last commit/rollback Those events are sent to the listeners if
	 * eventListeningEnabled is set to true. This isn't used if
	 * eventListeningEnabled is set to false.
	 */
    private static final JdbcEventsThreadLocal EVENTS = new JdbcEventsThreadLocal();

	private static final JdbcEventThreadLocal JDBC_EVENT = new JdbcEventThreadLocal();

	/**
	 * Thread local map where the key is the combination of
	 * "operation"+"objectref" and the value is the operation start time in
	 * nanoseconds as long.
	 */
	private static final OperationMapThreadLocal OPERATION_MAP = new OperationMapThreadLocal();

	private static final SequenceThreadLocal SEQUENCE = new SequenceThreadLocal();
	
	protected static List<JdbcEvent> getPendingEvents() {
		return EVENTS.get();
	}
	
	protected static Map<String, JdbcEvent> getPendingJdbcEvents() {
		return JDBC_EVENT.get();
	}

	protected static Map<String, Long> getPendingOperations() {
		return OPERATION_MAP.get();
	}
	
	/**
	 * 
	 * getInstance
	 * 
	 * @return the isntance
	 */
	public static JdbcProfiler getInstance() {
		return PROFILER;
	}

	/**
	 * 
	 * start
	 * 
	 * @param operation the operation
	 * @param objRef the reference string
	 */
	public void start(String operation, String objRef) {

		if (profilingEnabled) {
			if (operation == null) {
				throw new IllegalArgumentException("Operation cannot be null");
			}
			Map<String, Long> map = OPERATION_MAP.get();
			Long startTime = new Long(System.nanoTime());
			if (LOG.isLoggable(Level.FINE)) {
				LOG.fine("Adding to operation map: " + operation + objRef + ","
						+ startTime);
			}
			map.put(operation + objRef, startTime);
		}
	}

	/**
	 * 
	 * stop
	 * 
     * @param operation the operation
     * @param objRef the reference string
	 */
	public void stop(String operation, String objRef) {
		if (profilingEnabled) {
			if (operation == null) {
				throw new IllegalArgumentException("Operation cannot be null");
			}
			long endTime = System.nanoTime();
			Map<String, Long> map = OPERATION_MAP.get();
			if (LOG.isLoggable(Level.FINE)) {
				LOG.fine("Fetching operation for " + operation + objRef);
			}
			Long startTime = map.remove(operation + objRef);
			if (LOG.isLoggable(Level.FINE)) {
				LOG.fine("Operation " + operation + " startTime " + startTime
						+ " endTime " + endTime);
			}

			long operationTime = 0L;
			operationTime = endTime - startTime.longValue();
			
			if (LOG.isLoggable(Level.FINE)) {
				LOG.fine("Operation " + operation + " time ("
						+ NumberFormat.getInstance().format(operationTime)
						+ " ms)");
			}

			JdbcEvent event = null;
			if (!COMMIT.equals(operation) && !OP_ROLLBACK.equals(operation)) {
				event = getJdbcEvent(objRef);
			}

			if (OP_PREPARE.equals(operation)) {
				event.setPrepareTime(new Long(operationTime));
			} else if ((OP_EXECUTE_QUERY.equals(operation))
					|| (OP_EXECUTE_UPDATE.equals(operation))
					|| (OP_EXECUTE_BATCH.equals(operation))) {
				event.setExecuteTime(new Long(operationTime));
				event.setStartTime(startTime);
				event.setStopTime(endTime);
				event.setThreadName(Thread.currentThread().getName());

				Integer s = SEQUENCE.get();
				s = new Integer(s.intValue() + 1);
				SEQUENCE.set(s);
				event.setSequence(s);

			} else if (COMMIT.equals(operation)) {
				SEQUENCE.set(Integer.valueOf(0));
				if (LOG.isLoggable(Level.FINE)) {
					LOG.fine("Transaction completion time " + operationTime);
				}
				JdbcEventManager.notifyListeners(EVENTS.getEvents());
				clearPendingEvents();
			} else if (OP_ROLLBACK.equals(operation)) {
                SEQUENCE.set(Integer.valueOf(0));
				clearPendingEvents();
			}
		}
	}

	public void clearPendingEvents() {
		EVENTS.get().clear();
		OPERATION_MAP.get().clear();
		
		// force end the pending events, even if we are still reading their
		// result set.
		// this is done in case anyone forgets to close their result set &
		// prepared statement.
		Map<String, JdbcEvent> map = JDBC_EVENT.get();
		Set<String> keySet = map.keySet();
		if (keySet.size() > 0) {
			ArrayList<String> keyList = new ArrayList<String>();
			keyList.addAll(keySet);
			for (String eventRef : keyList) {
				addRowsRead(1, eventRef, true);
			}
		}
	}


	/**
	 * 
	 * getJdbcEvent
	 * 
     * @param objRef the reference string
	 * @return the event.
	 */
	public JdbcEvent getJdbcEvent(String objRef) {
		if (objRef == null) {
			throw new AssertionError("reference key can not be null");
		}
		Map<String, JdbcEvent> map = JDBC_EVENT.get();
		if (!map.containsKey(objRef)) {
			map.put(objRef, new JdbcEvent());
		}
		return map.get(objRef);
	}

	/**
	 * 
	 * addSqlStatement
	 * 
	 * @param sql the sql
	 * @param objRef the execution reference
	 * @param transactionId the transactionID
	 */
	public void addSqlStatement(String sql, String objRef, String transactionId) {
		if (profilingEnabled) {
			JdbcEvent event = getJdbcEvent(objRef);
			event.setSqlStatement(sql.trim());
			event.setTransactionId(transactionId);
		}
	}

	/**
	 * 
	 * addRowsUpdated
	 * 
	 * @param rows rows updated
	 * @param objRef the execution reference
	 * @param success was the execution successful
	 */
	public void addRowsUpdated(int rows, String objRef, boolean success) {
		if (profilingEnabled) {
			JdbcEvent event = getJdbcEvent(objRef);
			event.setRowsUpdated(Integer.valueOf(rows));
			logEvent(event, success);
			removeAllObjectReferences(objRef);
		}
	}

	private void removeAllObjectReferences(String objRef) {
		removeJdbcEvent(objRef);
	}

	private void removeJdbcEvent(String objRef) {
		Map<String, JdbcEvent> map = JDBC_EVENT.get();
		map.remove(objRef);
	}

	/**
	 * 
	 * addTableNames
	 * 
	 * @param names table names
	 * @param objRef the execution reference
	 */
	public void addTableNames(String[] names, String objRef) {
		if (profilingEnabled) {
			JdbcEvent event = getJdbcEvent(objRef);

			StringBuilder tables = new StringBuilder();
			for (int i = 0; i < names.length; i++) {
				tables.append(names[i]).append(' ');
			}
			event.setTables(tables.toString().trim());
		}
	}

	/**
	 * 
	 * addRowsRead
	 * 
	 * @param rows row count
	 * @param objRef the execution reference
	 * @param success was the insertion successful
	 */
	public void addRowsRead(int rows, String objRef, boolean success) {
		if (profilingEnabled) {
			JdbcEvent event = JDBC_EVENT.get().get(objRef);
			if( event != null ) {
				event.setRowsRead(Integer.valueOf(rows));
				if (event.getSequence() == -1) {
					// result set was already closed. Ignore the 2nd close call.
					// new Exception("What is this?").printStackTrace();
				}
				else {
					logEvent(event, success);
				}
				removeAllObjectReferences(objRef);
				
			}
		}
	}

	/**
	 * 
	 * logEvent
	 * 
	 * @param event the event
	 * @param success  was it a success
	 */
	public void logEvent(JdbcEvent event, boolean success) {
		if (eventMeasurementEnabled) {
			measureEvent(event, success);
		}

		if (eventListeningEnabled) {
			EVENTS.addEvent(event);
		}
	}

	private static AtomicLong uniqueIDincrementer = new AtomicLong();

	private void measureEvent(JdbcEvent event, boolean success) {
		OperationMetric metric = new OperationMetric();

		String operationName = event.getSqlStatement();
		List<String> asList = null;
		
		if (isSelectStatement(operationName)) {
			asList = getOrderedParameterKeyValueArray(event.getParameters());
			MetricFileLoader.addJdbcParameterSubstitution(operationName, asList);
		} else {
			// hide all the parameters for create/update/delete operations
			String retVal = "unique " + uniqueIDincrementer.getAndIncrement();
			asList = new ArrayList<String>(1);
			asList.add(retVal);
		}

		// disabled : unique ID assignment. Could be used to cleanup the
		// parameters and only get a single unique ID.
		// if( event.rowsRead == 0)
		// {
		// // updated detected, add a unique parameter
		// String uniqueParameterName = "unique " + UNIQUE_ID.incrementAndGet();
		// asList.add(uniqueParameterName);
		// }

		String metricOperationName = adjustJdbcOperationName(operationName);
		metric.startOperation(metricOperationName, false);
		metric.setKeyValuePairList(asList);
		
		// JdbcLogger.LOG_GATHERER.gatherMetricEntryLog(metric);

		long resultSize = event.getReadSize();
		if (resultSize == 0) {
			int max = Math.max(event.getRowsRead(), event.getRowsUpdated());
			max = Math.max(max, 1);

			// dummy 100 per row size for now
			resultSize = 100 * max;
		}

		long maxValue = Integer.MAX_VALUE;
		// $ANALYSIS-IGNORE
		int intReadSize = (int) resultSize;
		if (resultSize >= maxValue) {
			intReadSize = Integer.MAX_VALUE;
		}

		metric.stopOperation(intReadSize, false);
		long startTime = event.getStartTime();
		if( startTime == -1 ) {
			// skip invalid metrics.
			return;
		}
		
		metric.setStartTime(startTime);
		metric.setStopTime(event.getStopTime());
		metric.setDuration(event.getStopTime() - startTime);
		metric.setSuccessful(success);
		String transactionIdentifier = event.getTransactionId();
		if( transactionIdentifier != null ) {
		    metric.setProperty(JdbcProfiler.TRANSACTION_ID, transactionIdentifier);
		}

		JdbcLogger.GATHERER.gatherMetric(metric);

		// increase metrics of all JDBC operations put together.
		metric.setOperationName(JDBC_ALL_OPERATIONS);
		PerformanceLogger.increase(metric);
	}

	public String adjustJdbcOperationName(String operationName) {
		String parameterizedSql = MetricFileLoader.substituteJdbcParameters(operationName);
		String metricOperationName = "JDBC : " + parameterizedSql;
		return metricOperationName;
	}

    private static final Pattern SELECT_PATTERN = Pattern.compile("[ ]*?select .*", Pattern.CASE_INSENSITIVE);

	/**
	 * 
	 * isSelectStatement
	 * 
	 * @param operationName the operation name
	 * @return true if it's a select
	 */
	public static boolean isSelectStatement(String operationName) {
		Matcher matcher = SELECT_PATTERN.matcher(operationName);
		return matcher.matches();
	}

	// public static void testIsSelect(String op) {
	// System.out.println(isSelectStatement(op) + " for " + op);
	// }
	//
	// public static void main(String[] args) {
	// testIsSelect("    select BLAH");
	// testIsSelect(" select BLAH");
	// testIsSelect("select BLAH");
	// testIsSelect("SELECT BLAH");
	// testIsSelect(" update SELECT BLAH");
	// testIsSelect(" UPDATE SELECT BLAH");
	// }

	private static List<String> getOrderedParameterKeyValueArray(
			Map<Integer, Serializable> parameterMap) {

		SortedMap<Integer, Serializable> keyValueSortedMap = new TreeMap<Integer, Serializable>();

		keyValueSortedMap.putAll(parameterMap);

		int keyValueCount = keyValueSortedMap.size();
		List<String> orderedParametersKeyValues = new ArrayList<String>(
				keyValueCount * 2 + 1);

		Set<Entry<Integer, Serializable>> entrySet = keyValueSortedMap
				.entrySet();
		for (Entry<Integer, Serializable> entry : entrySet) {
			orderedParametersKeyValues.add(entry.getKey().toString());
			Serializable value = entry.getValue();
			String stringValue = null;
			if (value != null) {
				if (value.getClass().isArray()) {
					stringValue = Arrays.deepToString((Object[]) value);
				} else {
					stringValue = value.toString();
				}
			}
			orderedParametersKeyValues.add(stringValue);
		}
		return orderedParametersKeyValues;
	}

	/**
	 * 
	 * setStatementType
	 * 
	 * @param type the statement type
	 * @param objRef the execution reference
	 */
	public void setStatementType(String type, String objRef) {
		if (profilingEnabled) {
			JdbcEvent event = getJdbcEvent(objRef);
			event.setStatementType(type);
		}
	}

	/**
	 * 
	 * addStack
	 * 
	 * @param objRef the execution reference
	 */
	public void addStack(String objRef) {
		if (profilingEnabled && stackCaptureEnabled) {
			StackTraceElement[] stack = new Exception().getStackTrace();
			JdbcEvent event = getJdbcEvent(objRef);
			event.setStack(new StackTraceElement[stack.length - 2]);
			System.arraycopy(stack, 2, event.getStack(), 0,
					event.getStack().length);
		}
	}

	/**
	 * 
	 * addSetData
	 * 
	 * @param index the index
	 * @param data the data
	 * @param objRef the execution reference
	 */
	public void addSetData(int index, Object data, String objRef) {
		final String methodName = "addSetData(int index, Object data, String objRef)";
		if (profilingEnabled) {
			JdbcEvent event = getJdbcEvent(objRef);
			Serializable serializableData = null;
			if (data == null) {
				serializableData = "null";
			} else if ((data instanceof Serializable)) {
				serializableData = (Serializable) data;
			} else {
				serializableData = "unserializable:?";
				try {
					serializableData = "unserializable:" + data.toString();
				} catch (Throwable ex) {
					LoggingHelper.logUnexpectedException(LOG, CLASSNAME,
							methodName, ex);
				}
			}

			event.getParameters().put(Integer.valueOf(index), serializableData);
		}
	}

	/**
	 * 
	 * setStackCaptureEnabled
	 * 
	 * @param stack_capture_enabled set stack captured enable
	 */
	public static void setStackCaptureEnabled(boolean stack_capture_enabled) {
		stackCaptureEnabled = stack_capture_enabled;
	}

	/**
	 * 
	 * isStackCaptureEnabled
	 * 
	 * @return isStackCaptureEnabled
	 */
	public static boolean isStackCaptureEnabled() {
		return stackCaptureEnabled;
	}

	/**
	 * 
	 * isProfilingEnabled
	 * 
	 * @return isProfilingEnabled
	 */
	public static boolean isProfilingEnabled() {
		return profilingEnabled && JdbcLogger.isLoggable();
	}

	/**
	 * 
	 * setProfilingEnabled
	 * 
	 * @param enabled is profiling enabled
	 */
	public static void setProfilingEnabled(boolean enabled) {
		getInstance().clearPendingEvents();
		profilingEnabled = enabled;
	}

	private static final class JdbcEventThreadLocal extends
			ThreadLocal<Map<String, JdbcEvent>> {
		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.ThreadLocal#initialValue()
		 */
		@Override
        protected Map<String, JdbcEvent> initialValue() {
			return new HashMap<String, JdbcEvent>();
		}

		/**
		 * 
		 * clear
		 */
		@SuppressWarnings("unused")
		public void clear() {
			super.get().clear();
		}
	}

	private static final class OperationMapThreadLocal extends
			ThreadLocal<Map<String, Long>> {
		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.ThreadLocal#initialValue()
		 */
		@Override
        protected Map<String, Long> initialValue() {
			return new HashMap<String, Long>();
		}
	}

	private static final class SequenceThreadLocal extends ThreadLocal<Integer> {
		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.ThreadLocal#initialValue()
		 */
		@Override
        protected Integer initialValue() {
			return Integer.valueOf(0);
		}
	}

	private static final class JdbcEventsThreadLocal extends
			ThreadLocal<List<JdbcEvent>> {
		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.ThreadLocal#initialValue()
		 */
		@Override
        protected List<JdbcEvent> initialValue() {
			return new ArrayList<JdbcEvent>();
		}

		/**
		 * 
		 * addEvent
		 * 
		 * @param event the event
		 */
		public void addEvent(JdbcEvent event) {
			super.get().add(event);
		}

		/**
		 * 
		 * getEvents
		 * 
		 * @return the events
		 */
		public JdbcEvent[] getEvents() {
			List<JdbcEvent> events = super.get();
			return events.toArray(new JdbcEvent[events.size()]);
		}

	}
}
