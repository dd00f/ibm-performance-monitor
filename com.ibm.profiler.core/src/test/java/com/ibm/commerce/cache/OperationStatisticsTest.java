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

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class OperationStatisticsTest {

	@Test
	public void testLogStatisticOperationMetric() {
		// performance test of statistics aggregation
		final OperationMetric metric = new OperationMetric();
		metric.startOperation("test", true, "a", "b");
		metric.stopOperation(123123, false, true);
		List<Thread> threadList = new ArrayList<Thread>();
		
		final OperationStatistics stats = new OperationStatistics();
		for( int j=0;j<1;++j) {
			Thread myThread = new Thread(new Runnable() {
				
				@Override
				public void run() {
					for( int i = 0; i< 100000000 ; ++i ) {
						stats.logStatistic(metric);
					}
				}
			});
			myThread.start();
			threadList.add(myThread);
		}
		
		for (Thread thread : threadList) {
			try {
				thread.join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	
	@Test
	public void testMinDuration() {
		OperationStatistics stats = new OperationStatistics();
		Assert.assertEquals( 0,stats.getMinExecutionTime() );
		stats.logStatistic(123, 456, false, false);
		Assert.assertEquals( 123,stats.getMinExecutionTime() );
		stats.logStatistic(66, 456, false, false);
		Assert.assertEquals( 66,stats.getMinExecutionTime() );
		stats.logStatistic(454, 456, false, false);
		Assert.assertEquals( 66,stats.getMinExecutionTime() );
		stats.reset();
		Assert.assertEquals( 0,stats.getMinExecutionTime() );
		stats.logStatistic(123, 456, false, false);
		Assert.assertEquals( 123,stats.getMinExecutionTime() );
	}

	@Test
	public void testMinResponseSize() {
		OperationStatistics stats = new OperationStatistics();
		Assert.assertEquals( 0,stats.getMinResultSize() );
		stats.logStatistic(123, 456, false, false);
		Assert.assertEquals( 456,stats.getMinResultSize() );
		stats.logStatistic(66, 34, false, false);
		Assert.assertEquals( 34,stats.getMinResultSize() );
		stats.logStatistic(454, 23423, false, false);
		Assert.assertEquals( 34,stats.getMinResultSize() );		
		stats.reset();
		Assert.assertEquals( 0,stats.getMinResultSize() );
		stats.logStatistic(123, 456, false, false);
		Assert.assertEquals( 456,stats.getMinResultSize() );
	}
}
