package com.ibm.logger.stats;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.ibm.commerce.cache.OperationMetric;

public class TotalLogEntryTest {
	
	public long[] intervals;
	
	public TotalLogEntry entry;

	public List<TimeIntervalLogEntry> intervalStatistics;

	@Before
	public void setup() {
		intervals = new long[]{60000000000l};
		initializeStatistics();
	}

	public void initializeStatistics() {
		entry = new TotalLogEntry("identifier", intervals);
		intervalStatistics = entry.getIntervalStatistics();
	}
	
	@Test
	public void testAddValueNoIntervals() {
		intervals = null;
		initializeStatistics();
		intervals = new long[]{};
		initializeStatistics();
		OperationMetric metric = new OperationMetric();
		metric.startOperation("name", false, "123");
		metric.stopOperation(200, false, false);
		entry.addValue(metric);
		assertOneErrorCall();
		entry.addValue(1000, 200, false, false, true);
		assertEquals( 2, entry.getCallCount());
		assertEquals( 0, intervalStatistics.size());
	}
	
	@Test
	public void testAddValueMultipleIntervals() {
		intervals = new long[]{60000000000l,3600l*1000000000l};
		initializeStatistics();
		OperationMetric metric = new OperationMetric();
		metric.startOperation("name", false, "123");
		metric.stopOperation(200, false, false);
		entry.addValue(metric);
		assertOneErrorCall();
		entry.addValue(1000, 200, false, false, true);
		assertEquals( 2, entry.getCallCount());
		assertEquals( 2, intervalStatistics.size());
	}
	
	@Test
	public void testAddValueOperationMetric() {
		OperationMetric metric = new OperationMetric();
		metric.startOperation("name", false, "123");
		metric.stopOperation(200, false, false);
		
		assertEquals( 0, entry.getCallCount());
		
		for (TimeIntervalLogEntry timeInterval : intervalStatistics) {
			assertEquals( 0, timeInterval.getStatistics(false).getCallCount());
		}
		
		entry.addValue(metric);
		assertOneErrorCall();
	}

	private void assertOneErrorCall() {
		assertEquals( 1, entry.getCallCount());
		assertEquals( 1, entry.getErrorCallCount());
		assertEquals( 0, entry.getSuccessCallCount());
		assertEquals( 1, entry.getStatistics().getCallCount());
		
		
		for (TimeIntervalLogEntry timeInterval : intervalStatistics) {
			assertEquals( 1, timeInterval.getStatistics(false).getCallCount());
			assertEquals( 1, timeInterval.getStatistics(false).getErrorCallCount());
			assertEquals( 0, timeInterval.getStatistics(false).getSuccessCallCount());
		}
	}

	@Test
	public void testAddValueLongIntBooleanBooleanBoolean() {
		OperationMetric metric = new OperationMetric();
		metric.startOperation("name", false, "123");
		metric.stopOperation(200, false, false);
		
		assertEquals( 0, entry.getCallCount());
		assertEquals( 0, entry.getErrorCallCount());
		assertEquals( 0, entry.getSuccessCallCount());
		
		for (TimeIntervalLogEntry timeInterval : intervalStatistics) {
			assertEquals( 0, timeInterval.getStatistics(false).getCallCount());
		}
		
		entry.addValue(1000, 200, false, false, true);
		assertOneErrorCall();
		
		entry.clear();
		
		assertEquals( 0, entry.getCallCount());
		assertEquals( 0, entry.getErrorCallCount());
		assertEquals( 0, entry.getSuccessCallCount());
		
		
		for (TimeIntervalLogEntry timeInterval : intervalStatistics) {
			assertEquals( 1, timeInterval.getStatistics(false).getCallCount());
		}
	}

	@Test
	public void testGetIntervalDuration() {
		assertEquals(0, entry.getIntervalDuration());
	}

	@Test
	public void testisUsedSinceLastCheck() {
		assertEquals(false, entry.isUsedSinceLastCheck());
		entry.addValue(1000, 200, false, false, false);
		assertEquals(true, entry.isUsedSinceLastCheck());
		assertEquals(false, entry.isUsedSinceLastCheck());
	}

}
