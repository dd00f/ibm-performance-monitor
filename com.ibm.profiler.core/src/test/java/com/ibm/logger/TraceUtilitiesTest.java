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
package com.ibm.logger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import javax.management.JMX;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.internal.matchers.Contains;
import org.mockito.internal.matchers.Not;

import com.ibm.commerce.cache.CacheUtilities;
import com.ibm.logger.jmx.PerformanceLoggerManagerMXBean;
import com.ibm.logger.jmx.TimeIntervalLogEntryMXBean;
import com.ibm.logger.stats.TimeIntervalLogEntry;
import com.ibm.logger.stats.TotalLogEntry;
import com.ibm.logger.trace.PrintMode;

public class TraceUtilitiesTest {

    public final class FakeLogHandler extends Handler {

        public List<LogRecord> logRecordList = new ArrayList<LogRecord>();

        public FakeLogHandler() {
            setLevel( Level.FINER );
        }

        @Override
        public void publish( LogRecord record ) {
            logRecordList.add( record );
        }

        @Override
        public void flush() {

        }

        @Override
        public void close() throws SecurityException {

        }
    }

    public Logger logger = Logger.getLogger( "MyTestLogger" );

    public FakeLogHandler logHandler = new FakeLogHandler();

    public FakeLogHandler aspectClassLogHandler = new FakeLogHandler();

    public FakeLogHandler serviceLoggerlogHandler = new FakeLogHandler();

    public FakeClassWithAspect classWithAspect = new FakeClassWithAspect();

    private Object nullToStringObject = new Object() {
        @Override
        public String toString() {
            return null;
        }
    };

    private Object badToStringObject = new Object() {
        @Override
        public String toString() {
            throw new NullPointerException();
        }
    };

    @Before
    public void setUp() {
        logHandler = new FakeLogHandler();
        aspectClassLogHandler = new FakeLogHandler();
        serviceLoggerlogHandler = new FakeLogHandler();

        logHandler.setLevel( Level.FINE );
        logger.addHandler( logHandler );
        logger.setLevel( Level.FINE );

        FakeClassWithAspect.LOGGER.addHandler( aspectClassLogHandler );
        FakeClassWithAspect.LOGGER.setLevel( Level.FINE );

        FakeClassWithAspect.SERVICE_LOGGER.addHandler( serviceLoggerlogHandler );
        FakeClassWithAspect.SERVICE_LOGGER.setLevel( Level.FINE );

        ApplyTraceAspect.reset();
    }

    @After
    public void tearDown() {
        logger.removeHandler( logHandler );

        FakeClassWithAspect.LOGGER.removeHandler( aspectClassLogHandler );

        FakeClassWithAspect.SERVICE_LOGGER.removeHandler( serviceLoggerlogHandler );
    }

    @Test
    public void testConstructor() {
        TraceUtilities x = new TraceUtilities();
        Assert.assertNotNull( x );
    }

    @Test
    public void testGetObjectSizeNull() {
        assertEquals( 0, TraceUtilities.getObjectSize( null, 123, logger, Level.FINE ) );
    }

    @Test
    public void testGetObjectSizeNotLoggable() {
        assertEquals( 123, TraceUtilities.getObjectSize( "abcd", 123, logger, Level.FINER ) );
    }

    @Test
    public void testGetObjectSize() {
        assertEquals( 4, TraceUtilities.getObjectSize( "abcd", 123, logger, Level.FINE ) );
    }

    @Test
    public void testGetObjectSizeBadToString() {
        assertEquals( 0, TraceUtilities.getObjectSize( badToStringObject, 123, logger, Level.FINE ) );
    }

    @Test
    public void testGetObjectNullToString() {
        assertEquals( 0, TraceUtilities.getObjectSize( nullToStringObject, 123, logger, Level.FINE ) );
    }

    @Test
    public void testGetParameterPrintStringFull() {
        assertEquals( "abc", TraceUtilities.getParameterPrintString( "abc", PrintMode.FULL ) );
    }

    @Test
    public void testGetParameterPrintStringFullNull() {
        assertEquals( null, TraceUtilities.getParameterPrintString( null, PrintMode.FULL ) );
    }

    @Test
    public void testGetParameterPrintStringHash() {
        assertEquals( Integer.toString( "abc".hashCode() ), TraceUtilities.getParameterPrintString( "abc", PrintMode.HASH ) );
    }

    @Test
    public void testGetParameterPrintStringHashNull() {
        assertEquals( "0", TraceUtilities.getParameterPrintString( null, PrintMode.HASH ) );
    }

    @Test
    public void testGetParameterPrintStringMask() {
        assertEquals( "********", TraceUtilities.getParameterPrintString( "abc", PrintMode.MASK ) );
    }

    @Test
    public void testGetParameterPrintStringNoDisplay() {
        assertEquals( "", TraceUtilities.getParameterPrintString( "abc", PrintMode.NO_DISPLAY ) );
    }

    @Test
    public void testGetParameterPrintStringBadToString() {
        assertEquals( null, TraceUtilities.getParameterPrintString( badToStringObject, PrintMode.FULL ) );
    }

    @Test
    public void testGetParameterPrintStringNullToString() {
        assertEquals( null, TraceUtilities.getParameterPrintString( nullToStringObject, PrintMode.FULL ) );
    }

    @Test
    public void testGetParameterPrintStringNativeArray() {
        assertEquals( "[1, 2, 3]", TraceUtilities.getParameterPrintString( new int[] { 1, 2, 3 }, PrintMode.FULL ) );
    }

    @Test
    public void testGetParameterPrintStringObjectArray() {
        assertEquals( "[1, 2, 3, abc]", TraceUtilities.getParameterPrintString( new Object[] { 1, 2, 3, "abc" }, PrintMode.FULL ) );
    }

    @Test
    public void testGetParameterPrintStringEmbeddedArray() {
        assertEquals( "[1, 2, 3, abc, [1, 2, 3, abc]]", TraceUtilities.getParameterPrintString( new Object[] { 1, 2, 3, "abc", new Object[] { 1, 2, 3, "abc" } }, PrintMode.FULL ) );
    }

    @Test
    public void testGetParameterPrintStringEmbeddedNativeArray() {
        assertEquals( "[1, 2, 3, abc, [1, 2, 3]]", TraceUtilities.getParameterPrintString( new Object[] { 1, 2, 3, "abc", new int[] { 1, 2, 3 } }, PrintMode.FULL ) );
    }

    @Test
    public void testGetLoggerByName() {
        Logger loggerByName1 = TraceUtilities.getLoggerByName( "uniqueName" );
        Logger loggerByName2 = TraceUtilities.getLoggerByName( "uniqueName" );

        assertSame( loggerByName1, loggerByName2 );
    }

    @Test
    public void testTraceAndMeasureJoinPointLoggerDisabled() {

        FakeClassWithAspect.LOGGER.setLevel( Level.INFO );
        classWithAspect.methodWithTrace();

        // expect no trace logs
        List<LogRecord> logRecordList = aspectClassLogHandler.logRecordList;
        assertEquals( 0, logRecordList.size() );
    }

    @Test
    public void testTraceAndMeasureJoinPointExitLog() {

        classWithAspect.methodWithTrace();

        // expect an exit log
        List<LogRecord> logRecordList = aspectClassLogHandler.logRecordList;
        assertEquals( 1, logRecordList.size() );
        LogRecord logRecord = logRecordList.get( 0 );
        String message = logRecord.getMessage();
        System.out.println( message );
        Assert.assertThat( message, new Contains( "<exit" ) );
        Assert.assertThat( message, new Contains( "operation=\"public java.lang.String com.ibm.logger.FakeClassWithAspect.methodWithTrace()\"" ) );
        Assert.assertThat( message, new Contains( "parameters=\"\"" ) );
        Assert.assertThat( message, new Contains( "cacheHit=\"false\"" ) );
        Assert.assertThat( message, new Contains( "cacheEnabled=\"false\"" ) );
        Assert.assertThat( message, new Contains( "resultSize=\"1000\"" ) );
        Assert.assertThat( message, new Contains( "return=\"Hello world1\"" ) );

        // measure and trace disabled
        // trace entry
        // trace exit
        // measurement impact

        // method throwing an exception
        // print mode on method return
        // print mode on method argument

        // option not to print return

        // option not to trace

    }

    @Test
    public void testTraceAndMeasureJoinPointEntryLog() {

        FakeClassWithAspect.LOGGER.setLevel( Level.FINER );

        classWithAspect.methodWithTrace();

        // expect an exit log
        List<LogRecord> logRecordList = aspectClassLogHandler.logRecordList;
        assertEquals( 2, logRecordList.size() );
        LogRecord logRecord = logRecordList.get( 0 );
        String message = logRecord.getMessage();
        System.out.println( message );
        Assert.assertThat( message, new Contains( "<entry" ) );
        Assert.assertThat( message, new Contains( "operation=\"public java.lang.String com.ibm.logger.FakeClassWithAspect.methodWithTrace()\"" ) );
        Assert.assertThat( message, new Contains( "parameters=\"\"" ) );

        logRecord = logRecordList.get( 1 );
        message = logRecord.getMessage();
        System.out.println( message );
        Assert.assertThat( message, new Contains( "<exit" ) );
        Assert.assertThat( message, new Contains( "operation=\"public java.lang.String com.ibm.logger.FakeClassWithAspect.methodWithTrace()\"" ) );
        Assert.assertThat( message, new Contains( "parameters=\"\"" ) );
        Assert.assertThat( message, new Contains( "cacheHit=\"false\"" ) );
        Assert.assertThat( message, new Contains( "cacheEnabled=\"false\"" ) );
        Assert.assertThat( message, new Contains( "resultSize=\"12\"" ) );
        Assert.assertThat( message, new Contains( "return=\"Hello world1\"" ) );
    }

    @Test
    public void testTraceAndMeasureJoinPointTracesDisabled() {
        ApplyTraceAspect.trace = false;
        FakeClassWithAspect.LOGGER.setLevel( Level.FINER );

        classWithAspect.methodWithTrace();

        // expect an exit log
        List<LogRecord> logRecordList = aspectClassLogHandler.logRecordList;
        assertEquals( 0, logRecordList.size() );
    }

    @Test
    public void testTraceAndMeasureJoinPointEntryLogParameterAndPrintMode() {

        FakeClassWithAspect.LOGGER.setLevel( Level.FINER );

        classWithAspect.methodWithTrace( "arg1", "arg2", 123, new int[] { 1, 2, 3 }, "filter" );

        // expect an exit log
        List<LogRecord> logRecordList = aspectClassLogHandler.logRecordList;
        assertEquals( 2, logRecordList.size() );
        LogRecord logRecord = logRecordList.get( 0 );
        String message = logRecord.getMessage();
        System.out.println( message );
        Assert.assertThat( message, new Contains( "<entry" ) );
        Assert.assertThat( message, new Contains( "operation=\"public java.lang.String com.ibm.logger.FakeClassWithAspect.methodWithTrace(java.lang.String,java.lang.String,int,int[],java.lang.String)\"" ) );
        String parameterTraceString = "parameters=\"arg1, " + Integer.toString( "arg2".hashCode() ) + ", ********, , filter\"";
        Assert.assertThat( message, new Contains( parameterTraceString ) );

        logRecord = logRecordList.get( 1 );
        message = logRecord.getMessage();
        System.out.println( message );
        Assert.assertThat( message, new Contains( "<exit" ) );
        Assert.assertThat( message, new Contains( "operation=\"public java.lang.String com.ibm.logger.FakeClassWithAspect.methodWithTrace(java.lang.String,java.lang.String,int,int[],java.lang.String)\"" ) );
        Assert.assertThat( message, new Contains( parameterTraceString ) );
        Assert.assertThat( message, new Contains( "cacheHit=\"false\"" ) );
        Assert.assertThat( message, new Contains( "cacheEnabled=\"false\"" ) );
        Assert.assertThat( message, new Contains( "resultSize=\"12\"" ) );
        Assert.assertThat( message, new Contains( "return=\"********\"" ) );
    }

    @Test
    public void testTraceAndMeasureJoinPointEntryLogParameterSecured() {

        ApplyTraceAspect.secure = true;
        FakeClassWithAspect.LOGGER.setLevel( Level.FINER );

        classWithAspect.methodWithTrace();

        // expect an exit log
        List<LogRecord> logRecordList = aspectClassLogHandler.logRecordList;
        assertEquals( 2, logRecordList.size() );
        LogRecord logRecord = logRecordList.get( 0 );
        String message = logRecord.getMessage();
        System.out.println( message );
        Assert.assertThat( message, new Contains( "<entry" ) );
        Assert.assertThat( message, new Contains( "operation=\"public java.lang.String com.ibm.logger.FakeClassWithAspect.methodWithTrace()\"" ) );
        String parameterTraceString = "parameters=\"********\"";
        Assert.assertThat( message, new Contains( parameterTraceString ) );

        logRecord = logRecordList.get( 1 );
        message = logRecord.getMessage();
        System.out.println( message );
        Assert.assertThat( message, new Contains( "<exit" ) );
        Assert.assertThat( message, new Contains( "operation=\"public java.lang.String com.ibm.logger.FakeClassWithAspect.methodWithTrace()\"" ) );
        Assert.assertThat( message, new Contains( parameterTraceString ) );
        Assert.assertThat( message, new Contains( "cacheHit=\"false\"" ) );
        Assert.assertThat( message, new Contains( "cacheEnabled=\"false\"" ) );
        Assert.assertThat( message, new Contains( "resultSize=\"12\"" ) );
        Assert.assertThat( message, new Contains( "return=\"********\"" ) );
    }

    @Test
    public void testTraceAndMeasureJoinPointMethodThrowingError() {
        FakeClassWithAspect.LOGGER.setLevel( Level.FINER );

        try {
            classWithAspect.methodWithTrace( "throwing" );
            fail( "expected NullPointerException" );
        } catch ( NullPointerException ex ) {
            assertEquals( "my npe", ex.getMessage() );
        }

        // expect an exit log
        List<LogRecord> logRecordList = aspectClassLogHandler.logRecordList;
        assertEquals( 2, logRecordList.size() );
        LogRecord logRecord = logRecordList.get( 0 );
        String message = logRecord.getMessage();
        System.out.println( message );
        Assert.assertThat( message, new Contains( "<entry" ) );
        Assert.assertThat( message, new Contains( "operation=\"public java.lang.String com.ibm.logger.FakeClassWithAspect.methodWithTrace(java.lang.String)\"" ) );
        Assert.assertThat( message, new Contains( "parameters=\"throwing\"" ) );

        logRecord = logRecordList.get( 1 );
        message = logRecord.getMessage();
        System.out.println( message );
        Assert.assertThat( message, new Contains( "<exit" ) );
        Assert.assertThat( message, new Contains( "operation=\"public java.lang.String com.ibm.logger.FakeClassWithAspect.methodWithTrace(java.lang.String)\"" ) );
        Assert.assertThat( message, new Contains( "parameters=\"throwing\"" ) );
        Assert.assertThat( message, new Contains( "cacheHit=\"false\"" ) );
        Assert.assertThat( message, new Contains( "cacheEnabled=\"false\"" ) );
        Assert.assertThat( message, new Contains( "resultSize=\"38\"" ) );
        Assert.assertThat( message, new Contains( "return=\"java.lang.NullPointerException: my npe\"" ) );
    }

    @Test
    public void testTraceAndMeasureConstructorJoinPointTracesDisabled() {
        ApplyTraceAspect.trace = false;
        FakeClassWithAspect.LOGGER.setLevel( Level.FINER );

        classWithAspect = new FakeClassWithAspect( "hello" );

        // expect no logs
        List<LogRecord> logRecordList = aspectClassLogHandler.logRecordList;
        assertEquals( 0, logRecordList.size() );
    }

    @Test
    public void testTraceAndMeasureConstructorJoinPointEntryAndExitLogSecured() {
        ApplyTraceAspect.secure = true;
        FakeClassWithAspect.LOGGER.setLevel( Level.FINER );

        classWithAspect = new FakeClassWithAspect( "hello" );

        // expect an exit log
        List<LogRecord> logRecordList = aspectClassLogHandler.logRecordList;
        assertEquals( 2, logRecordList.size() );
        LogRecord logRecord = logRecordList.get( 0 );
        String message = logRecord.getMessage();
        System.out.println( message );
        Assert.assertThat( message, new Contains( "<entry" ) );
        Assert.assertThat( message, new Contains( "operation=\"public com.ibm.logger.FakeClassWithAspect(java.lang.String)\"" ) );
        Assert.assertThat( message, new Contains( "parameters=\"********\"" ) );

        logRecord = logRecordList.get( 1 );
        message = logRecord.getMessage();
        System.out.println( message );
        Assert.assertThat( message, new Contains( "<exit" ) );
        Assert.assertThat( message, new Contains( "operation=\"public com.ibm.logger.FakeClassWithAspect(java.lang.String)\"" ) );
        Assert.assertThat( message, new Contains( "parameters=\"********\"" ) );
        Assert.assertThat( message, new Contains( "cacheHit=\"false\"" ) );
        Assert.assertThat( message, new Contains( "cacheEnabled=\"false\"" ) );
        Assert.assertThat( message, new Contains( "resultSize=\"0\"" ) );
    }

    @Test
    public void testTraceAndMeasureConstructorJoinPointEntryAndExitLog() {

        FakeClassWithAspect.LOGGER.setLevel( Level.FINER );

        classWithAspect = new FakeClassWithAspect( "hello" );

        // expect an exit log
        List<LogRecord> logRecordList = aspectClassLogHandler.logRecordList;
        assertEquals( 2, logRecordList.size() );
        LogRecord logRecord = logRecordList.get( 0 );
        String message = logRecord.getMessage();
        System.out.println( message );
        Assert.assertThat( message, new Contains( "<entry" ) );
        Assert.assertThat( message, new Contains( "operation=\"public com.ibm.logger.FakeClassWithAspect(java.lang.String)\"" ) );
        Assert.assertThat( message, new Contains( "parameters=\"hello\"" ) );

        logRecord = logRecordList.get( 1 );
        message = logRecord.getMessage();
        System.out.println( message );
        Assert.assertThat( message, new Contains( "<exit" ) );
        Assert.assertThat( message, new Contains( "operation=\"public com.ibm.logger.FakeClassWithAspect(java.lang.String)\"" ) );
        Assert.assertThat( message, new Contains( "parameters=\"hello\"" ) );
        Assert.assertThat( message, new Contains( "cacheHit=\"false\"" ) );
        Assert.assertThat( message, new Contains( "cacheEnabled=\"false\"" ) );
        Assert.assertThat( message, new Contains( "resultSize=\"0\"" ) );
    }

    @Test
    public void testTraceAndMeasureJoinPointInMemoryMetric() throws Exception {
        setJmxStatisticsEnabled( true );

        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();

        classWithAspect.methodWithTrace();

        String id = "public java.lang.String com.ibm.logger.FakeClassWithAspect.methodWithTrace()";

        ObjectName channelMXBeanName = PerformanceLogger.channelMXBeanName( id, PerformanceLogger.TOTAL_INTERVAL_NAME, TotalLogEntry.class );
        TimeIntervalLogEntryMXBean logEntry = JMX.newMBeanProxy( mbs, channelMXBeanName, TimeIntervalLogEntryMXBean.class );
        long numCalls = logEntry.getCallCount();

        classWithAspect.methodWithTrace();

        long newNumCalls = logEntry.getCallCount();
        assertEquals( numCalls + 1, newNumCalls );
    }

    private void setJmxStatisticsEnabled( boolean enabled ) {
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        ObjectName objectName = PerformanceLoggerManager.getObjectName();
        PerformanceLoggerManagerMXBean loggerManager = JMX.newMBeanProxy( mbs, objectName, PerformanceLoggerManagerMXBean.class );
        if ( enabled ) {
            loggerManager.enable();
        } else {
            loggerManager.disable();
        }
        Assert.assertEquals( enabled, loggerManager.isEnabled() );
    }

    @Test
    public void testTraceAndMeasureConstructorJoinPointInMemoryMetric() {
        setJmxStatisticsEnabled( true );

        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();

        classWithAspect = new FakeClassWithAspect( "hello" );
        String id = "public com.ibm.logger.FakeClassWithAspect(java.lang.String)";

        ObjectName channelMXBeanName = PerformanceLogger.channelMXBeanName( id, PerformanceLogger.TOTAL_INTERVAL_NAME, TotalLogEntry.class );
        TimeIntervalLogEntryMXBean logEntry = JMX.newMBeanProxy( mbs, channelMXBeanName, TimeIntervalLogEntryMXBean.class );
        long numCalls = logEntry.getCallCount();

        classWithAspect = new FakeClassWithAspect( "hello" );

        long newNumCalls = logEntry.getCallCount();
        assertEquals( numCalls + 1, newNumCalls );
    }

    @Test
    public void testTraceAndMeasureJoinPointInMemoryMetricDisabled() {
        setJmxStatisticsEnabled( true );

        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();

        classWithAspect.methodWithTrace();

        String id = "public java.lang.String com.ibm.logger.FakeClassWithAspect.methodWithTrace()";

        ObjectName channelMXBeanName = PerformanceLogger.channelMXBeanName( id, PerformanceLogger.TOTAL_INTERVAL_NAME, TotalLogEntry.class );
        TimeIntervalLogEntryMXBean logEntry = JMX.newMBeanProxy( mbs, channelMXBeanName, TimeIntervalLogEntryMXBean.class );
        long numCalls = logEntry.getCallCount();

        setJmxStatisticsEnabled( false );
        classWithAspect.methodWithTrace();

        long newNumCalls = logEntry.getCallCount();
        assertEquals( numCalls, newNumCalls );

    }

    @Test
    public void testTraceAndMeasureConstructorJoinPointInMemoryMetricDisabled() {
        setJmxStatisticsEnabled( true );

        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();

        classWithAspect = new FakeClassWithAspect( "hello" );
        String id = "public com.ibm.logger.FakeClassWithAspect(java.lang.String)";

        ObjectName channelMXBeanName = PerformanceLogger.channelMXBeanName( id, PerformanceLogger.TOTAL_INTERVAL_NAME, TotalLogEntry.class );
        TimeIntervalLogEntryMXBean logEntry = JMX.newMBeanProxy( mbs, channelMXBeanName, TimeIntervalLogEntryMXBean.class );
        long numCalls = logEntry.getCallCount();

        setJmxStatisticsEnabled( false );
        classWithAspect = new FakeClassWithAspect( "hello" );

        long newNumCalls = logEntry.getCallCount();
        assertEquals( numCalls, newNumCalls );
    }
    
    
    @Test
    public void testInMemoryReset() {
        
        PerformanceLogger.setIntervals(new long[]{CacheUtilities.DAY_IN_NANOSECONDS});
        PerformanceLogger.clear();
        
        
        setJmxStatisticsEnabled( true );

        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();

        classWithAspect = new FakeClassWithAspect( "hello" );
        String id = "public com.ibm.logger.FakeClassWithAspect(java.lang.String)";

        ObjectName channelMXBeanName = PerformanceLogger.channelMXBeanName( id, PerformanceLogger.TOTAL_INTERVAL_NAME, TotalLogEntry.class );
        TimeIntervalLogEntryMXBean logEntry = JMX.newMBeanProxy( mbs, channelMXBeanName, TimeIntervalLogEntryMXBean.class );

        ObjectName intervalChannelMXBeanName = PerformanceLogger.channelMXBeanName( id, "1d", TimeIntervalLogEntry.class );
        // TimeIntervalLogEntry intervalLogEntry = JMX.newMBeanProxy( mbs, intervalChannelMXBeanName, TimeIntervalLogEntry.class );
        TimeIntervalLogEntry intervalLogEntry = TotalLogEntry.getIntervalStatsRegister().getAllRegisteredBeans().get(intervalChannelMXBeanName);
        
        Assert.assertNotNull(logEntry);
        Assert.assertNotNull(intervalLogEntry);
        
        
        assertEquals( 1, logEntry.getCallCount());
        assertEquals( 1, intervalLogEntry.getStatistics(false).getCallCount());
        
        setJmxStatisticsEnabled( false );
        classWithAspect = new FakeClassWithAspect( "hello" );

        assertEquals( 1, logEntry.getCallCount());
        assertEquals( 1, intervalLogEntry.getStatistics(false).getCallCount());
        
        setJmxStatisticsEnabled( true );
        
        PerformanceLogger.clear();
        
        intervalLogEntry = TotalLogEntry.getIntervalStatsRegister().getAllRegisteredBeans().get(intervalChannelMXBeanName);
        // Assert.assertNull(intervalLogEntry);
        
        classWithAspect = new FakeClassWithAspect( "hello" );
        classWithAspect = new FakeClassWithAspect( "hello" );
        
        logEntry = JMX.newMBeanProxy( mbs, channelMXBeanName, TimeIntervalLogEntryMXBean.class );
        intervalLogEntry = TotalLogEntry.getIntervalStatsRegister().getAllRegisteredBeans().get(intervalChannelMXBeanName);
     
        Assert.assertNotNull(logEntry);
        Assert.assertNotNull(intervalLogEntry);
        
        assertEquals( 2, logEntry.getCallCount());
        assertEquals( 2, intervalLogEntry.getStatistics(false).getCallCount());
    }
    

    @Test
    public void testTraceAndMeasureJoinPointWithLogger() {
        ApplyTraceAspect.printReturnValue = true;
        FakeClassWithAspect.SERVICE_LOGGER.setLevel( Level.FINER );

        classWithAspect.methodWithServiceLogger();

        // expect an exit log
        List<LogRecord> logRecordList = serviceLoggerlogHandler.logRecordList;
        assertEquals( 2, logRecordList.size() );
        LogRecord logRecord = logRecordList.get( 0 );
        String message = logRecord.getMessage();
        System.out.println( message );
        Assert.assertThat( message, new Contains( "<entry" ) );
        Assert.assertThat( message, new Contains( "operation=\"Operation : public java.lang.String com.ibm.logger.FakeClassWithAspect.methodWithServiceLogger()\"" ) );
        Assert.assertThat( message, new Contains( "parameters=\"\"" ) );

        logRecord = logRecordList.get( 1 );
        message = logRecord.getMessage();
        System.out.println( message );
        Assert.assertThat( message, new Contains( "<exit" ) );
        Assert.assertThat( message, new Contains( "operation=\"Operation : public java.lang.String com.ibm.logger.FakeClassWithAspect.methodWithServiceLogger()\"" ) );
        Assert.assertThat( message, new Contains( "parameters=\"\"" ) );
        Assert.assertThat( message, new Contains( "cacheHit=\"false\"" ) );
        Assert.assertThat( message, new Contains( "cacheEnabled=\"false\"" ) );
        Assert.assertThat( message, new Contains( "resultSize=\"16\"" ) );
        Assert.assertThat( message, new Contains( "return=\"Hello to you too\"" ) );
    }

    @Test
    public void testTraceAndMeasureJoinPointWithLoggerDontPrint() {
        ApplyTraceAspect.printReturnValue = false;
        FakeClassWithAspect.SERVICE_LOGGER.setLevel( Level.FINER );

        classWithAspect.methodWithServiceLogger();

        // expect an exit log
        List<LogRecord> logRecordList = serviceLoggerlogHandler.logRecordList;
        assertEquals( 2, logRecordList.size() );
        LogRecord logRecord = logRecordList.get( 0 );
        String message = logRecord.getMessage();

        logRecord = logRecordList.get( 1 );
        message = logRecord.getMessage();
        System.out.println( message );
        Assert.assertThat( message, new Contains( "<exit" ) );
        Assert.assertThat( message, new Contains( "operation=\"Operation : public java.lang.String com.ibm.logger.FakeClassWithAspect.methodWithServiceLogger()\"" ) );
        Assert.assertThat( message, new Contains( "parameters=\"\"" ) );
        Assert.assertThat( message, new Contains( "cacheHit=\"false\"" ) );
        Assert.assertThat( message, new Contains( "cacheEnabled=\"false\"" ) );
        Assert.assertThat( message, new Contains( "resultSize=\"16\"" ) );
        Assert.assertThat( message, new Not( new Contains( "return=\"Hello to you too\"" ) ) );
    }

    @Test
    public void testTraceAndMeasureConstructorJoinPointWithLoggerAtInfoLevel() {
        FakeClassWithAspect.SERVICE_LOGGER.setLevel( Level.INFO );
        classWithAspect = new FakeClassWithAspect( 123 );

        // expect no trace logs
        List<LogRecord> logRecordList = aspectClassLogHandler.logRecordList;
        assertEquals( 0, logRecordList.size() );
    }

    @Test
    public void testTraceAndMeasureConstructorJoinPointWithLoggerSetToNull() {
        ApplyTraceAspect.serviceLogger = null;
        classWithAspect = new FakeClassWithAspect( 123 );
    }

    @Test
    public void testTraceAndMeasureJoinPointWithLoggerSetToNull() {
        ApplyTraceAspect.serviceLogger = null;
        classWithAspect.methodWithServiceLogger();
    }

    @Test
    public void testTraceAndMeasureConstructorJoinPointWithLogger() {
        FakeClassWithAspect.SERVICE_LOGGER.setLevel( Level.FINER );

        classWithAspect = new FakeClassWithAspect( 123 );

        // expect an exit log
        List<LogRecord> logRecordList = serviceLoggerlogHandler.logRecordList;
        assertEquals( 2, logRecordList.size() );
        LogRecord logRecord = logRecordList.get( 0 );
        String message = logRecord.getMessage();
        System.out.println( message );
        Assert.assertThat( message, new Contains( "<entry" ) );
        Assert.assertThat( message, new Contains( "operation=\"Operation : public com.ibm.logger.FakeClassWithAspect(int)\"" ) );
        Assert.assertThat( message, new Contains( "parameters=\"123\"" ) );

        logRecord = logRecordList.get( 1 );
        message = logRecord.getMessage();
        System.out.println( message );
        Assert.assertThat( message, new Contains( "<exit" ) );
        Assert.assertThat( message, new Contains( "operation=\"Operation : public com.ibm.logger.FakeClassWithAspect(int)\"" ) );
        Assert.assertThat( message, new Contains( "parameters=\"123\"" ) );
        Assert.assertThat( message, new Contains( "cacheHit=\"false\"" ) );
        Assert.assertThat( message, new Contains( "cacheEnabled=\"false\"" ) );
        Assert.assertThat( message, new Contains( "resultSize=\"0\"" ) );
    }

}
