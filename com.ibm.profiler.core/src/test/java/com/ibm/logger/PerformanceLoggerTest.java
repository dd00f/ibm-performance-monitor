package com.ibm.logger;

/*
 *-----------------------------------------------------------------
 * IBM Confidential
 *
 * OCO Source Materials
 *
 * WebSphere Commerce
 *
 * (C) Copyright IBM Corp. 2014, 2015
 *
 * The source code for this program is not published or otherwise
 * divested of its trade secrets, irrespective of what has
 * been deposited with the U.S. Copyright Office.
 *-----------------------------------------------------------------
 */

import java.lang.management.ManagementFactory;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import junit.framework.Assert;

import org.junit.Test;

import com.ibm.commerce.cache.CacheUtilities;
import com.ibm.logger.jmx.PerformanceLoggerManagerMXBean;
import com.ibm.logger.jmx.TimeIntervalLogEntryMXBean;
import com.ibm.logger.stats.LogType;
import com.ibm.logger.stats.TimeIntervalLogEntry;
import com.ibm.logger.stats.TotalLogEntry;

public class PerformanceLoggerTest {

    // public static final Logger LOGGER = Logger
    // .getLogger(AsynchronousFileHandlerTest.class.getName());
    public static final Logger LOGGER = Logger.getLogger( "com.ibm.service.entry.test.MyEntryLogger" );

    AtomicLong timeSpent = new AtomicLong();

    @Test
    public void basicPerformanceTest() {
        long start = System.currentTimeMillis();
        PerformanceLogger.setEnabled( true );
        PerformanceLogger.clear();
        PerformanceLogger.startLogging( "OuterLoop" );
        for ( int i = 0; i < 5000; i++ ) {
            PerformanceLogger.startLogging( "InnerLoop" );
            PerformanceLogger.increase( "InnerTimer", 1 );
            PerformanceLogger.stopLogging( "InnerLoop" );
        }

        PerformanceLogger.stopLogging( "OuterLoop" );
        // PerformanceLogger.dumpPerformanceLogs();

        Assert.assertNotNull( PerformanceLogger.getPerformanceLog( "InnerTimer" ) );
        Assert.assertNotNull( PerformanceLogger.getPerformanceLog( "InnerLoop" ) );
        Assert.assertNotNull( PerformanceLogger.getPerformanceLog( "OuterLoop" ) );

        Assert.assertEquals( 5000, PerformanceLogger.getPerformanceLog( "InnerTimer" ).getCallCount() );
        Assert.assertEquals( 5000, PerformanceLogger.getPerformanceLog( "InnerLoop" ).getCallCount() );
        Assert.assertEquals( 1, PerformanceLogger.getPerformanceLog( "OuterLoop" ).getCallCount() );

        System.out.println( "basicPerformanceTest total Time: " + ( System.currentTimeMillis() - start ) );
    }

//    @Test
//    public void test50SessonMetric() {
//        runThreaded( "50 Session : Type Metric", 50, 100000, LogType.METRIC );
//    }
//
//    @Test
//    public void test250SessonMetric() {
//        runThreaded( "250 Session : Type Metric", 250, 100000, LogType.METRIC );
//    }

    @Test
    public void test500SessonMetric() {
        runThreaded( "500 Session : Type Metric", 500, 20000, LogType.METRIC );
    }

//    @Test
//    public void test50SessonTimer() {
//        runThreaded( "50 Session : Type Timer", 50, 100000, LogType.TIMER );
//    }
//
//    @Test
//    public void test250SessonTimer() {
//        runThreaded( "250 Session : Type Timer", 250, 100000, LogType.TIMER );
//    }

    @Test
    public void test500SessonTimer() {
        runThreaded( "500 Session : Type Timer", 500, 20000, LogType.TIMER );
    }

//    @Test
//    public void test50SessonStatistic() {
//        runThreaded( "50 Session : Type Statistic", 50, 100000, LogType.STATISTIC );
//    }
//
//    @Test
//    public void test250SessonStatistic() {
//        runThreaded( "250 Session : Type Statistic", 250, 100000, LogType.STATISTIC );
//    }

    @Test
    public void test500SessonStatistic() {
        runThreaded( "500 Session : Type Statistic", 500, 20000, LogType.STATISTIC );
    }

    public void runThreaded( final String testName, final int numThreads, final int numPerThread, final LogType type ) {
        long start = System.currentTimeMillis();
        PerformanceLogger.setEnabled( true );
        PerformanceLogger.clear();

        Assert.assertNotNull( PerformanceLogger.getPerformanceLogs() );
        Assert.assertEquals( PerformanceLogger.getPerformanceLogs().size(), 0 );

        List<Thread> threads = new ArrayList<Thread>();
        for ( int i = 0; i < numThreads; i++ ) {
            final int threadNum = i;
            Runnable run = new Runnable() {
                @Override
                public void run() {
                    Thread.currentThread().setName( "loop." + threadNum );
                    loop( testName, numPerThread, type );
                    // System.out.println( "Done." + Thread.currentThread().getName() );
                }
            };
            Thread myThread = new Thread( run, "Logger Thread " + i );
            myThread.start();
            threads.add( myThread );
        }

        for ( Thread thread : threads ) {
            CacheUtilities.joinQuietly( thread, 1000000 );
        }

        // PerformanceLogger.dumpPerformanceLogs();

        Assert.assertNotNull( PerformanceLogger.getPerformanceLog( "thread." + testName ) );
        Assert.assertNotNull( PerformanceLogger.getPerformanceLog( testName ) );

        Assert.assertEquals( numThreads, PerformanceLogger.getPerformanceLog( "thread." + testName ).getCallCount() );
        Assert.assertEquals( numThreads * numPerThread, PerformanceLogger.getPerformanceLog( testName ).getCallCount() );

        System.out.println( testName + " total Time: " + ( System.currentTimeMillis() - start ) + " ms" );

    }

    public void loop( String id, int numPerThread, LogType type ) {

        try {
            PerformanceLogger.startLogging( "thread." + id );

            for ( int i = 1; i <= numPerThread; i++ ) {
                if ( type == LogType.METRIC ) {
                    PerformanceLogger.increase( id, 1 );
                } else if ( type == LogType.STATISTIC ) {
                    PerformanceLogger.addStatistic( id, 1 );
                } else if ( type == LogType.TIMER ) {
                    PerformanceLogger.startLogging( id );
                    PerformanceLogger.stopLogging( id );
                }
            }

            PerformanceLogger.stopLogging( "thread." + id );

        } catch ( Throwable t ) {
            t.printStackTrace();
        }
    }
    
	@Test
	public void testCleanup() throws Exception {
		
		PerformanceLogger.setIntervals(new long[]{CacheUtilities.DAY_IN_NANOSECONDS});
        PerformanceLogger.clear();
        PerformanceLogger.setEnabled(true);
		
		MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();

//		TimeIntervalLogEntryMXBean activeEntry = PerformanceLogger.getOrCreateEntry(
//				"testActive", "metric");
		// make the current counter active
		
		PerformanceLogger.addStatistic("testActive", 123);
		PerformanceLogger.addStatistic("testInactive", 123);
		// activeEntry.addValue(123);

		ObjectName activeBeanName = PerformanceLogger.channelMXBeanName(
				"testActive", PerformanceLogger.TOTAL_INTERVAL_NAME,
				TotalLogEntry.class);
		
		ObjectName activeBeanNameInterval = PerformanceLogger.channelMXBeanName(
				"testActive", "1d",
				TimeIntervalLogEntry.class);

		TimeIntervalLogEntryMXBean inactive = PerformanceLogger.getOrCreateEntry("testInactive", "metric");
		WeakReference<TimeIntervalLogEntryMXBean> weakRef = new WeakReference<TimeIntervalLogEntryMXBean>(inactive);
		inactive = null;

		ObjectName inactiveBeanName = PerformanceLogger.channelMXBeanName(
				"testInactive", PerformanceLogger.TOTAL_INTERVAL_NAME,
				TotalLogEntry.class);
		
		ObjectName inactiveBeanNameInterval = PerformanceLogger.channelMXBeanName(
				"testInactive", "1d",
				TimeIntervalLogEntry.class);

		long currentTimeMillis = System.currentTimeMillis();
		PerformanceLogger.lastCheck.set(currentTimeMillis);

		mbs.getMBeanInfo(activeBeanName);
		mbs.getMBeanInfo(inactiveBeanName);
		mbs.getMBeanInfo(activeBeanNameInterval);
		mbs.getMBeanInfo(inactiveBeanNameInterval);
		
		TimeIntervalLogEntryMXBean dummyEntry = PerformanceLogger.getOrCreateEntry("dummy",
				"metric");
		dummyEntry.addValue(123);
		mbs.getMBeanInfo(activeBeanName);
		mbs.getMBeanInfo(inactiveBeanName);
		mbs.getMBeanInfo(activeBeanNameInterval);
		mbs.getMBeanInfo(inactiveBeanNameInterval);
		
		// test at 59 minutes, expect no cleanup
		PerformanceLogger.lastCheck
				.set(currentTimeMillis - 59 * 60 * 1000);
		PerformanceLogger.getOrCreateEntry("dummy", "metric");
		mbs.getMBeanInfo(activeBeanName);
		mbs.getMBeanInfo(inactiveBeanName);
		mbs.getMBeanInfo(activeBeanNameInterval);
		mbs.getMBeanInfo(inactiveBeanNameInterval);		
		Assert.assertNotNull(weakRef.get());

		// test at 61 minutes, expect cleanup
		PerformanceLogger.loggerCountBeforeCleanup = 0;
		PerformanceLogger.lastCheck
				.set(currentTimeMillis - 61 * 60 * 1000);
		// first check marks the count to match
		PerformanceLogger.getOrCreateEntry("dummy", "metric");
		PerformanceLogger.addStatistic("testActive", 123);		
		// second check deletes the entry for good
		PerformanceLogger.lastCheck
		.set(currentTimeMillis - 61 * 60 * 1000);		
		PerformanceLogger.getOrCreateEntry("dummy", "metric");
		mbs.getMBeanInfo(activeBeanName);
		mbs.getMBeanInfo(activeBeanNameInterval);
		
		try {
			mbs.getMBeanInfo(inactiveBeanName);
			Assert.fail();
		} catch (InstanceNotFoundException ex) {

		}
		try {
			mbs.getMBeanInfo(inactiveBeanNameInterval);		
			Assert.fail();
		} catch (InstanceNotFoundException ex) {

		}
		// confirm the object is gone from memory
		System.gc();
		Assert.assertNull(weakRef.get());

		Assert.assertTrue(PerformanceLogger.lastCheck.get() >= currentTimeMillis );

	}
	
	@Test
	public void testgetIntervalsFromProperty() {
		long[] defaultValue = new long[]{-1};
		long[] intervalsFromProperty = PerformanceLogger.getIntervalsFromProperty("", defaultValue);
		Assert.assertEquals( 0, intervalsFromProperty.length );

		intervalsFromProperty = PerformanceLogger.getIntervalsFromProperty(null, defaultValue);
		Assert.assertEquals( 1, intervalsFromProperty.length );
		Assert.assertEquals( -1, intervalsFromProperty[0] );
		
		intervalsFromProperty = PerformanceLogger.getIntervalsFromProperty("123ns", defaultValue);
		Assert.assertEquals( 1, intervalsFromProperty.length );
		Assert.assertEquals( 123, intervalsFromProperty[0] );

		intervalsFromProperty = PerformanceLogger.getIntervalsFromProperty("1d", defaultValue);
		Assert.assertEquals( 1, intervalsFromProperty.length );
		Assert.assertEquals( CacheUtilities.DAY_IN_NANOSECONDS, intervalsFromProperty[0] );

		intervalsFromProperty = PerformanceLogger.getIntervalsFromProperty("1s", defaultValue);
		Assert.assertEquals( 1, intervalsFromProperty.length );
		Assert.assertEquals( CacheUtilities.SECOND_IN_NANOSECONDS, intervalsFromProperty[0] );

		intervalsFromProperty = PerformanceLogger.getIntervalsFromProperty("1", defaultValue);
		Assert.assertEquals( 1, intervalsFromProperty.length );
		Assert.assertEquals( CacheUtilities.SECOND_IN_NANOSECONDS, intervalsFromProperty[0] );
		
		intervalsFromProperty = PerformanceLogger.getIntervalsFromProperty("123ns," + Long.MAX_VALUE +"ns", defaultValue);
		Assert.assertEquals( 2, intervalsFromProperty.length );
		Assert.assertEquals( 123, intervalsFromProperty[0] );
		Assert.assertEquals( Long.MAX_VALUE, intervalsFromProperty[1] );
		
		intervalsFromProperty = PerformanceLogger.getIntervalsFromProperty("junk,123ns,345ns", defaultValue);
		Assert.assertEquals( 2, intervalsFromProperty.length );
		Assert.assertEquals( 123, intervalsFromProperty[0] );
		Assert.assertEquals( 345, intervalsFromProperty[1] );
		
		intervalsFromProperty = PerformanceLogger.getIntervalsFromProperty("123ns,345ns,junk", defaultValue);
		Assert.assertEquals( 2, intervalsFromProperty.length );
		Assert.assertEquals( 123, intervalsFromProperty[0] );
		Assert.assertEquals( 345, intervalsFromProperty[1] );
		
		intervalsFromProperty = PerformanceLogger.getIntervalsFromProperty("-1", defaultValue);
		Assert.assertEquals( 0, intervalsFromProperty.length );
		
		intervalsFromProperty = PerformanceLogger.getIntervalsFromProperty("0", defaultValue);
		Assert.assertEquals( 0, intervalsFromProperty.length );
		
		intervalsFromProperty = PerformanceLogger.getIntervalsFromProperty("12312ns,-1,98798987ns", defaultValue);
		Assert.assertEquals( 2, intervalsFromProperty.length );
		Assert.assertEquals( 12312, intervalsFromProperty[0] );
		Assert.assertEquals( 98798987, intervalsFromProperty[1] );
	}

	@Test
	public void testMultipleJVMUsingTheSameManager() {
	    
	    PerformanceLoggerManagerMXBean firstManager = PerformanceLoggerManager.getManager();
	    PerformanceLoggerManagerMXBean createInitialPerformanceLoggerManager = PerformanceLoggerManager.createInitialPerformanceLoggerManager();
	    Assert.assertNotSame(firstManager, createInitialPerformanceLoggerManager);
	 
        firstManager.enable();
        Assert.assertTrue(createInitialPerformanceLoggerManager.isEnabled());
        firstManager.disable();
        Assert.assertFalse(createInitialPerformanceLoggerManager.isEnabled());
        firstManager.enable();
	}

    @Test
    public void testMultipleJVMUsingTheSameManagerRaceCondition()
    {

        PerformanceLoggerManagerMXBean firstManager = PerformanceLoggerManager.getManager();
        PerformanceLoggerManagerMXBean createInitialPerformanceLoggerManager = PerformanceLoggerManager
            .registerPerformanceLoggerManager();
        Assert.assertNotSame(firstManager, createInitialPerformanceLoggerManager);

        firstManager.enable();
        Assert.assertTrue(createInitialPerformanceLoggerManager.isEnabled());
        firstManager.disable();
        Assert.assertFalse(createInitialPerformanceLoggerManager.isEnabled());
        firstManager.enable();
    }
	
    @Test
    public void testMultipleJVMUsingTheSameLogEntry()
    {

        String id = "random";
        TimeIntervalLogEntryMXBean firstGet = PerformanceLogger.getOrCreateLogEntryOnCacheMiss(id);
        TimeIntervalLogEntryMXBean secondGet = PerformanceLogger.getOrCreateLogEntryOnCacheMiss(id);
        Assert.assertNotSame(firstGet, secondGet);

        firstGet.addValue(123);
        Assert.assertEquals(1, firstGet.getCallCount());
        Assert.assertEquals(1, secondGet.getCallCount());
        firstGet.addValue(123);
        Assert.assertEquals(2, firstGet.getCallCount());
        Assert.assertEquals(2, secondGet.getCallCount());
    }
    
    @Test
    public void testMultipleJVMRegisteringTheSameLogger()
    {
        String id = "random1";
        ObjectName channelMXBeanName = PerformanceLogger.channelMXBeanName(id, "total", "TotalLogEntry");
        
        TimeIntervalLogEntryMXBean fetch1 = PerformanceLogger.fetchRegisteredPerformanceLogger(channelMXBeanName);
        Assert.assertNull(fetch1);
        
        // first class loader registers first
        TimeIntervalLogEntryMXBean firstGet = PerformanceLogger.registerNewLogger(id, channelMXBeanName);
        // second class loader should attempt to reload the first on failure.
        TimeIntervalLogEntryMXBean secondGet = PerformanceLogger.registerNewLogger(id, channelMXBeanName);

        Assert.assertNotNull(firstGet);
        Assert.assertNotNull(secondGet);
        Assert.assertNotSame(firstGet, secondGet);
    }
    
    @Test
    public void testPrintToString()
    {
    	PerformanceLogger.setEnabled( true );
		PerformanceLogger.addStatistic("testActive", 123);
		PerformanceLogger.addStatistic("testInactive", 123);
		System.out.println(PerformanceLogger.dumpPerformanceLogsTableToString());
        PerformanceLogger.dumpPerformanceLogs();
    }

    @Test
    public void testPrintToCsv()
    {
    	PerformanceLogger.setEnabled( true );
		PerformanceLogger.addStatistic("testActive", 123);
		PerformanceLogger.addStatistic("testInactive", 123);
        System.out.println(PerformanceLogger.dumpPerformanceLogsCsvToString());
    }
}
