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

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class TimeIntervalStatisticsAggregatorTest {

	@Test
	public void testTimeIntervalStatisticsAggregator() throws InterruptedException {
		long intervalWidthInNanos = 10000000;
		int intervalCount = 1000;
		long totalDuration = intervalWidthInNanos * intervalCount;
		
		
		// 10 milli per bucket x 1000 buckets = 10 seconds
		final TimeIntervalStatisticsAggregator stats = new TimeIntervalStatisticsAggregator(intervalWidthInNanos, intervalCount);
		
		int threadCount = 500;
		final int addCountPerThread = 10000;
		long callCount = threadCount * addCountPerThread;
		List<Thread> threadList = new ArrayList<Thread>();
		
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				for (int i = 0; i < addCountPerThread; ++i) {
					stats.getCurrentInterval().logStatistic(200, 100, true,
							false, true);
				}
			}
		};

		for(int i=0;i<threadCount;++i ){
			Thread addThread = new Thread(runnable);
			addThread.start();
			threadList.add(addThread);
		}
		
		for (Thread thread : threadList) {
			thread.join();
		}
		
		OperationStatistics statistics = stats.getStatistics(totalDuration, false);
		
		assertEquals( callCount, statistics.getCallCount());
	}

}
