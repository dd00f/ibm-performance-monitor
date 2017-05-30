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
package com.ibm.logger.stats;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.ibm.commerce.cache.OperationMetric;

public class TimeIntervalLogEntryTest {

	public TimeIntervalLogEntry logEntry;

	@Before
	public void setup() {
		// create a 1 minute interval entry.
		logEntry = new TimeIntervalLogEntry("test", 60000000000l);
	}

	@Test
	public void testAddValueOperationMetric() {

		long nanoTime = System.nanoTime();
		long metricTime = nanoTime - (30l * 1000000000l);

		OperationMetric metric = new OperationMetric();
		metric.startOperation("name", false, "123");
		metric.stopOperation(200, false, false);
		metric.setDuration(100);
		metric.setStopTime(metricTime);
		logEntry.addValue(metric);

		metric.setDuration(110);
		metric.setResultSize(220);
		logEntry.addValue(metric);

		metric.setDuration(90);
		metric.setResultSize(180);
		metric.setSuccessful(true);
		logEntry.addValue(metric);

		assertEquals(100.0, logEntry.getAverageDuration(), 0.001);
		assertEquals(90, logEntry.getMinimumDuration());
		assertEquals(110, logEntry.getMaximumDuration());

		assertEquals(200.0, logEntry.getAverageResponseSize(), 0.001);
		assertEquals(180, logEntry.getMinimumResponseSize());
		assertEquals(220, logEntry.getMaximumResponseSize());

		assertEquals(0, logEntry.getCacheEnabledCount());
		assertEquals(0, logEntry.getCacheEnabledPercentage(), 0.00001);
		assertEquals(0, logEntry.getCacheHitCount());
		assertEquals(0, logEntry.getCacheHitPercentage(), 0.00001);
		assertEquals(3, logEntry.getCallCount());
		assertEquals(2, logEntry.getErrorCallCount());
		assertEquals(0.6666666666666, logEntry.getErrorCallPercentage(),
				0.00001);
		assertEquals(60000000000l, logEntry.getIntervalDuration());

		assertEquals(1, logEntry.getSuccessCallCount());
		assertEquals(0.33333333333, logEntry.getSuccessCallPercentage(),
				0.00001);
	}

	@Test
	public void testAddValueOperationMetricCacheStatistics() {

		long nanoTime = System.nanoTime();
		long metricTime = nanoTime - (30l * 1000000000l);

		OperationMetric metric = new OperationMetric();
		metric.startOperation("name", true, "123");
		metric.stopOperation(200, false, true);
		metric.setDuration(100);
		metric.setStopTime(metricTime);
		logEntry.addValue(metric);

		logEntry.addValue(metric);

		metric.setResultFetchedFromCache(true);
		logEntry.addValue(metric);

		assertEquals(3, logEntry.getCacheEnabledCount());
		assertEquals(1.0, logEntry.getCacheEnabledPercentage(), 0.00001);
		assertEquals(1, logEntry.getCacheHitCount());
		assertEquals(0.333333333, logEntry.getCacheHitPercentage(), 0.00001);
		assertEquals(3, logEntry.getCallCount());
	}

	@Test
	public void testAddValue() {

		logEntry.addValue(100, 200, false, false, true);
		logEntry.addValue(110, 220, false, false, true);
		logEntry.addValue(90, 180, false, false, false);
		logEntry.setSkipLastInterval(false);

		assertEquals(100.0, logEntry.getAverageDuration(), 0.001);
		assertEquals(90, logEntry.getMinimumDuration());
		assertEquals(110, logEntry.getMaximumDuration());

		assertEquals(200.0, logEntry.getAverageResponseSize(), 0.001);
		assertEquals(180, logEntry.getMinimumResponseSize());
		assertEquals(220, logEntry.getMaximumResponseSize());

		assertEquals(0, logEntry.getCacheEnabledCount());
		assertEquals(0, logEntry.getCacheEnabledPercentage(), 0.00001);
		assertEquals(0, logEntry.getCacheHitCount());
		assertEquals(0, logEntry.getCacheHitPercentage(), 0.00001);
		assertEquals(3, logEntry.getCallCount());
		assertEquals(2, logEntry.getErrorCallCount());
		assertEquals(0.6666666666666, logEntry.getErrorCallPercentage(),
				0.00001);
		assertEquals(60000000000l, logEntry.getIntervalDuration());

		assertEquals(1, logEntry.getSuccessCallCount());
		assertEquals(0.33333333333, logEntry.getSuccessCallPercentage(),
				0.00001);
	}

	@Test
	public void testAddValueCacheStatistics() {

		logEntry.addValue(100, 200, true, false, false);
		logEntry.addValue(110, 220, true, false, false);
		logEntry.addValue(90, 180, true, true, false);
		logEntry.setSkipLastInterval(false);

		assertEquals(3, logEntry.getCacheEnabledCount());
		assertEquals(1.0, logEntry.getCacheEnabledPercentage(), 0.00001);
		assertEquals(1, logEntry.getCacheHitCount());
		assertEquals(0.333333333, logEntry.getCacheHitPercentage(), 0.00001);
		assertEquals(3, logEntry.getCallCount());
	}

	@Test
	public void testAddValueLong() {

		logEntry.addValue(100);
		logEntry.addValue(110);
		logEntry.addValue(90);
		logEntry.setSkipLastInterval(false);

		assertEquals(100.0, logEntry.getAverageDuration(), 0.001);
		assertEquals(90, logEntry.getMinimumDuration());
		assertEquals(110, logEntry.getMaximumDuration());

		assertEquals(0.0, logEntry.getAverageResponseSize(), 0.001);
		assertEquals(0, logEntry.getMinimumResponseSize());
		assertEquals(0, logEntry.getMaximumResponseSize());

		assertEquals(0, logEntry.getCacheEnabledCount());
		assertEquals(0, logEntry.getCacheEnabledPercentage(), 0.00001);
		assertEquals(0, logEntry.getCacheHitCount());
		assertEquals(0, logEntry.getCacheHitPercentage(), 0.00001);
		assertEquals(3, logEntry.getCallCount());
		assertEquals(0, logEntry.getErrorCallCount());
		assertEquals(0.0, logEntry.getErrorCallPercentage(), 0.00001);
		assertEquals(60000000000l, logEntry.getIntervalDuration());

		assertEquals(3, logEntry.getSuccessCallCount());
		assertEquals(1.0, logEntry.getSuccessCallPercentage(), 0.00001);
	}

	@Test
	public void testClear() {
		// not expecting any change
		logEntry.addValue(100);
		logEntry.addValue(110);
		logEntry.addValue(90);
		logEntry.setSkipLastInterval(false);
		assertEquals(3, logEntry.getCallCount());
		logEntry.clear();
		assertEquals(3, logEntry.getCallCount());
	}

	@Test
	public void testAddValueLongBoolean() {
		logEntry.addValue(100, true);
		logEntry.addValue(110, true);
		logEntry.addValue(90, false);
		logEntry.setSkipLastInterval(false);

		assertEquals(100.0, logEntry.getAverageDuration(), 0.001);
		assertEquals(90, logEntry.getMinimumDuration());
		assertEquals(110, logEntry.getMaximumDuration());

		assertEquals(0.0, logEntry.getAverageResponseSize(), 0.001);
		assertEquals(0, logEntry.getMinimumResponseSize());
		assertEquals(0, logEntry.getMaximumResponseSize());

		assertEquals(0, logEntry.getCacheEnabledCount());
		assertEquals(0, logEntry.getCacheEnabledPercentage(), 0.00001);
		assertEquals(0, logEntry.getCacheHitCount());
		assertEquals(0, logEntry.getCacheHitPercentage(), 0.00001);
		assertEquals(3, logEntry.getCallCount());
		assertEquals(2, logEntry.getErrorCallCount());
		assertEquals(0.6666666666666, logEntry.getErrorCallPercentage(),
				0.00001);
		assertEquals(60000000000l, logEntry.getIntervalDuration());

		assertEquals(1, logEntry.getSuccessCallCount());
		assertEquals(0.33333333333, logEntry.getSuccessCallPercentage(),
				0.00001);
	}
	
	@Test
	public void testTimeWindowRollover() {
		
		long nanoTime = System.nanoTime();
		
		OperationMetric metric = new OperationMetric();
		metric.startOperation("name", false, "123");
		metric.stopOperation(200, false, false);
		
		long secondIncrement = 12;
		// use increments of 5 seconds, ensure older logs are discarded.
		long maxIndex = (2l*60l/secondIncrement);
		for(long i=maxIndex;i>0;--i) {
			long newStopTime = nanoTime - i* secondIncrement * 1000000000l;
			metric.setStopTime(newStopTime);
			logEntry.addValue(metric);
		}
		
		long callCount = logEntry.getCallCount();
		assertEquals( 60/secondIncrement, callCount);
	}
	
	@Test
	public void testConstructor() {
		logEntry = new TimeIntervalLogEntry("test", 60000000000l);
		assertEquals( 12, logEntry.getInternalStatistics().getIntervalCount());
		assertEquals( 6000000000l, logEntry.getInternalStatistics().getIntervalWidthInNanos());
		
		logEntry = new TimeIntervalLogEntry("test", 1000000000l);
		assertEquals( 3, logEntry.getInternalStatistics().getIntervalCount());
		assertEquals( 1000000000l, logEntry.getInternalStatistics().getIntervalWidthInNanos());

		
		logEntry = new TimeIntervalLogEntry("test", 100000000l);
		assertEquals( 3, logEntry.getInternalStatistics().getIntervalCount());
		assertEquals( 100000000l, logEntry.getInternalStatistics().getIntervalWidthInNanos());
	}

}
