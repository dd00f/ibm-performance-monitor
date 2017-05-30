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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;

import org.junit.Assert;
import org.junit.Test;

import com.ibm.commerce.cache.AnalyzeMetricFile;
import com.ibm.commerce.cache.BatchedAsynchronousFileHandler;
import com.ibm.commerce.cache.CacheUtilities;

public class AsynchronousLoggerTest {

    // public static final Logger LOGGER = Logger
    // .getLogger(AsynchronousFileHandlerTest.class.getName());
    public static final Logger LOGGER = Logger.getLogger( "com.ibm.service.entry.test.MyEntryLogger" );

    AtomicLong timeSpent = new AtomicLong();

    // @Test
    public void testFileHandler() throws IOException {
        System.out.println( "testFileHandler" );
        FileHandler afh = new FileHandler( "sync-log", 5000000, 10, false );

        testLogger( afh );
    }

    /*
     * @Test
     * public void testAsynchronousFileHandler() throws IOException {
     * System.out.println("testAsynchronousFileHandler");
     * FileHandler afh = new AsynchronousFileHandler("async-log", 5000000, 10, false);
     * 
     * testLogger(afh);
     * }
     * 
     * @Test
     * public void testConcurrentAsynchronousFileHandler() throws IOException {
     * System.out.println("testConcurrentAsynchronousFileHandler");
     * FileHandler afh = new ConcurrentAsynchronousFileHandler("concurrent-async-log", 5000000, 10, false);
     * 
     * testLogger(afh);
     * }
     */

    // @Test
    public void testBatchedAsynchronousFileHandler() throws IOException {
        System.out.println( "testBatchedAsynchronousFileHandler" );
        StreamHandler afh = new BatchedAsynchronousFileHandler( "batched-async-log", 5000000, 10, false );

        testLogger( afh );
    }

    private void testLogger( StreamHandler afh ) {
        Handler[] handlers = LOGGER.getHandlers();
        for ( Handler handler : handlers ) {
            try {
                LOGGER.removeHandler( handler );
            } catch ( Exception e ) {
                e.printStackTrace();
            }
        }
        afh.setFormatter( new SimpleFormatter() );
        afh.setLevel( Level.FINEST );
        LOGGER.addHandler( afh );
        LOGGER.setLevel( Level.FINEST );

        final int threadCount = 20;
        final int logEntryCount = 100000;

        generateLogs( threadCount, logEntryCount );

        afh.close();

    }

    // @Test
    public void testLogger() {

        System.out.println( System.getProperties() );

        final int threadCount = 20;
        final int logEntryCount = 100;

        generateLogs( threadCount, logEntryCount );

    }

    private void generateLogs( final int threadCount, final int logEntryCount ) {
        List<Thread> threadList = new ArrayList<Thread>();

        for ( int i = 0; i < threadCount; i++ ) {
            Runnable run = new Runnable() {
                @Override
                public void run() {
                    sendLogs( logEntryCount );
                }
            };
            Thread myThread = new Thread( run, "Logger Thread " + i );
            myThread.start();
            threadList.add( myThread );
        }

        for ( Thread thread : threadList ) {
            CacheUtilities.joinQuietly( thread, 100000 );
        }

        System.out.println( "Total time to call logger : " + timeSpent.get() / 1000000 / 1000d );
    }

    private void sendLogs( int logEntryCount ) {
        timeSpent.set( 0 );
        long start = System.nanoTime();
        // String name = Thread.currentThread().getName();
        for ( int i = 0; i < logEntryCount; i++ ) {
            LOGGER.exiting( "source class", "method" );
            // "Thread " + name + " This went wrong " + i);
        }
        long end = System.nanoTime();
        long duration = end - start;
        // System.out.println("time to call logger : " + duration );
        timeSpent.addAndGet( duration );

    }

    @Test
    public void testMerge() {
        Properties prop = new Properties();
        String prefix = "this.is.my.test.";
        String suffix = "hello";
        String suffix2 = "another.one";
        System.setProperty( prefix + suffix, "value1" );
        System.setProperty( prefix + suffix2, "value2" );
        prop.setProperty( suffix, "overridden" );
        AnalyzeMetricFile.mergeSystemProperties( prefix, prop, true );

        System.out.println( prop );

        Assert.assertEquals( 2, prop.size() );

        Assert.assertEquals( "value1", prop.get( suffix ) );
        Assert.assertEquals( "value2", prop.get( suffix2 ) );

    }

    @Test
    public void testStrip() {
        Properties prop = new Properties();
        String prefix = "this.is.my.test.";
        String suffix = "hello";
        String suffix2 = "another.one";
        prop.setProperty( prefix + suffix, "value1" );
        prop.setProperty( prefix + suffix2, "value2" );
        AnalyzeMetricFile.stripKeyPrefix( prefix, prop );

        System.out.println( prop );

        Assert.assertEquals( 2, prop.size() );

        Assert.assertEquals( "value1", prop.get( suffix ) );
        Assert.assertEquals( "value2", prop.get( suffix2 ) );

    }

}
