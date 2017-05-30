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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import com.ibm.commerce.cache.CacheUtilities;

public class NoObjectPerformanceLoggerTest {

    public abstract static class RunLog {

        public int threadCount = 1;

        public int methodCount = 1000;

        public int runPerThreadCount = 1;

        public String[] methodNames;

        public String name = "Default";

        public void generateLogs() {

            methodNames = new String[methodCount];
            for ( int i = 0; i < methodCount; ++i ) {
                methodNames[i] = "com.ibm.blah.blah.blah.gdha.MethodSignature" + i;
            }

            long startTime = System.currentTimeMillis();

            List<Thread> threadList = new ArrayList<Thread>();

            for ( int i = 0; i < threadCount; i++ ) {
                Runnable run = new Runnable() {
                    @Override
                    public void run() {
                        sendLogs();
                    }

                };
                Thread myThread = new Thread( run, "Logger Thread " + i );
                myThread.start();
                threadList.add( myThread );
            }

            for ( Thread thread : threadList ) {
                CacheUtilities.joinQuietly( thread, 100000 );
            }

            long stopTime = System.currentTimeMillis();
            long duration = stopTime - startTime;

            System.out.println( name + ", Total time to call logger : " + duration + "  ms" );
        }

        public void sendLogs() {
            for ( int i = 0; i < runPerThreadCount; ++i ) {
                int methodId = i % methodCount;
                String methodName = methodNames[methodId];
                runMethod( methodName );
            }

        }

        public abstract void runMethod( String methodName );
    }

    /*
     * @Test
     * public void testStartLoggingString() {
     * RunLog log = new RunLog() {
     * 
     * @Override
     * public void runMethod( String methodName ) {
     * NoObjectPerformanceLogger.startLogging( methodName );
     * NoObjectPerformanceLogger.stopLogging( methodName );
     * }
     * };
     * 
     * log.name = "MAP+START+STOP";
     * log.generateLogs();
     * 
     * }
     */

    // @Test
    // public void testListLogging() {
    // RunLog log = new RunLog() {
    //
    // @Override
    // public void runMethod( String methodName ) {
    // PerformanceLogger.startLogging( methodName );
    // PerformanceLogger.stopLogging( methodName );
    // }
    // };
    //
    // log.name = "LIST+START+STOP";
    // log.generateLogs();
    //
    // }

    // @Test
    public void testIncreaseWarmp() {
        testIncrease();
    }

    // @Test
    public void testIncrease() {
        RunLog log = new RunLog() {

            @Override
            public void runMethod( String methodName ) {
                long startTime = System.currentTimeMillis();
                long duration = System.currentTimeMillis() - startTime;
                PerformanceLogger.increase( methodName, duration );
            }
        };

        log.name = "testIncrease";
        log.generateLogs();
    }

    // @Test
    public void testIncreaseWithLoggerLookupWarmup() {
        testIncreaseWithLoggerLookupLocalCaching();
    }

    private static ConcurrentHashMap<String, Logger> loggerMap = new ConcurrentHashMap<String, Logger>();

    public Logger myGetLogger( String methodName ) {
        Logger logger = loggerMap.get( methodName );
        if ( logger == null ) {
            logger = Logger.getLogger( methodName );
            loggerMap.put( methodName, logger );
        }
        return logger;
    }

    // @Test
    public void testIncreaseWithLoggerLookup() {
        RunLog log = new RunLog() {

            @Override
            public void runMethod( String methodName ) {
                Logger logger = Logger.getLogger( methodName );
                logger.isLoggable( Level.FINE );
                long startTime = System.currentTimeMillis();
                long duration = System.currentTimeMillis() - startTime;
                PerformanceLogger.increase( methodName, duration );
            }

        };

        log.name = "testIncreaseWithLoggerLookup";
        log.generateLogs();
    }

    // @Test
    public void testIncreaseWithLoggerLookupLocalCaching() {
        RunLog log = new RunLog() {

            @Override
            public void runMethod( String methodName ) {
                Logger logger = myGetLogger( methodName );
                logger.isLoggable( Level.FINE );
                long startTime = System.currentTimeMillis();
                long duration = System.currentTimeMillis() - startTime;
                PerformanceLogger.increase( methodName, duration );
            }

        };

        log.name = "testIncreaseWithLoggerLookupLocalCaching";
        log.generateLogs();
    }

    private FakeClassWithAspect fake = new FakeClassWithAspect();

    public AtomicInteger count = new AtomicInteger();

    public SimpleFormatter formatter = new SimpleFormatter();

    public boolean print = false;

    private Handler handler = null;

    {
        handler = new Handler() {

            @Override
            public void publish( LogRecord record ) {
                count.incrementAndGet();
                if ( print ) {
                    System.out.print( formatter.format( record ) );
                }
            }

            @Override
            public void flush() {

            }

            @Override
            public void close() throws SecurityException {

            }
        };
        // ConsoleHandler consoleHandler = new ConsoleHandler();
        // consoleHandler.setFormatter( new SimpleFormatter() );
        // handler = consoleHandler;
    }

    // @Test
    public void testTraceOff() {
        RunLog log = new RunLog() {

            @Override
            public void runMethod( String methodName ) {
                fake.methodWithTrace();
            }

        };

        log.name = "testTraceOff";
        log.generateLogs();
    }

    // @Test
    public void testServiceOff() {
        RunLog log = new RunLog() {

            @Override
            public void runMethod( String methodName ) {
                fake.methodWithServiceLogger();
            }

        };

        log.name = "testServiceOff";
        log.generateLogs();
    }

    // @Test
    public void testTraceOn() {

        Logger logger = Logger.getLogger( FakeClassWithAspect.class.getName() );
        logger.setLevel( Level.FINE );
        logger.addHandler( handler );
        RunLog log = new RunLog() {

            @Override
            public void runMethod( String methodName ) {
                fake.methodWithTrace();
            }

        };

        log.name = "testTraceOn";
        log.generateLogs();

        logger.removeHandler( handler );
        logger.setLevel( Level.INFO );
        System.out.println( "log count=" + count.get() );
        count.set( 0 );
    }

    // @Test
    public void testTraceOnWithEntry() {

        Logger logger = Logger.getLogger( FakeClassWithAspect.class.getName() );
        logger.setLevel( Level.FINER );
        logger.addHandler( handler );
        RunLog log = new RunLog() {

            @Override
            public void runMethod( String methodName ) {
                fake.methodWithTrace();
            }

        };

        log.name = "testTraceOnWithEntry";
        log.generateLogs();

        logger.removeHandler( handler );
        logger.setLevel( Level.INFO );
        System.out.println( "log count=" + count.get() );
        count.set( 0 );
    }

    // @Test
    public void testServiceOn() {

        Logger logger = FakeClassWithAspect.LOGGER;
        logger.setLevel( Level.FINE );
        logger.addHandler( handler );

        RunLog log = new RunLog() {

            @Override
            public void runMethod( String methodName ) {
                fake.methodWithServiceLogger();
            }

        };

        log.name = "testServiceOn";
        log.generateLogs();

        logger.removeHandler( handler );
        logger.setLevel( Level.INFO );
        System.out.println( "log count=" + count.get() );

        count.set( 0 );
    }

    // @Test
    public void testServiceOnWithEntry() {

        Logger logger = FakeClassWithAspect.LOGGER;
        logger.setLevel( Level.FINER );
        logger.addHandler( handler );

        RunLog log = new RunLog() {

            @Override
            public void runMethod( String methodName ) {
                fake.methodWithServiceLogger();
            }

        };

        log.name = "testServiceOnWithEntry";
        log.generateLogs();

        logger.removeHandler( handler );
        logger.setLevel( Level.INFO );
        System.out.println( "log count=" + count.get() );
        count.set( 0 );
    }

    // @Test
    public void testServiceOnWithEntryParams() {

        Logger logger = FakeClassWithAspect.LOGGER;
        logger.setLevel( Level.FINER );
        logger.addHandler( handler );

        RunLog log = new RunLog() {

            @Override
            public void runMethod( String methodName ) {
                fake.methodWithServiceLogger( "arg1", "arg2", 123, new int[] { 1, 2, 3, 4 } );
            }

        };

        log.name = "testServiceOnWithEntryParams";
        log.generateLogs();

        logger.removeHandler( handler );
        logger.setLevel( Level.INFO );
        System.out.println( "log count=" + count.get() );
        count.set( 0 );
    }

    // @Test
    public void testTraceOnWithEntryParams() {

        Logger logger = Logger.getLogger( FakeClassWithAspect.class.getName() );
        logger.setLevel( Level.FINER );
        logger.addHandler( handler );
        RunLog log = new RunLog() {

            @Override
            public void runMethod( String methodName ) {
                fake.methodWithTrace( "arg1", "arg2", 123, new int[] { 1, 2, 3, 4 } );
            }

        };

        log.name = "testTraceOnWithEntryParams";
        log.generateLogs();

        logger.removeHandler( handler );
        logger.setLevel( Level.INFO );
        System.out.println( "log count=" + count.get() );
        count.set( 0 );
    }

    // @Test
    public void testTraceOnWithEntryFilteredParams() {

        Logger logger = Logger.getLogger( FakeClassWithAspect.class.getName() );
        logger.setLevel( Level.FINER );
        logger.addHandler( handler );
        RunLog log = new RunLog() {

            @Override
            public void runMethod( String methodName ) {
                fake.methodWithTrace( "arg1", "arg2", 123, new int[] { 1, 2, 3, 4 }, "filtered" );
            }

        };

        log.name = "testTraceOnWithEntryFilteredParams";
        log.generateLogs();

        logger.removeHandler( handler );
        logger.setLevel( Level.INFO );
        System.out.println( "log count=" + count.get() );
        count.set( 0 );
    }

    // @Test
    public void testTraceOffWithFilteredParams() {
        RunLog log = new RunLog() {

            @Override
            public void runMethod( String methodName ) {
                fake.methodWithTrace( "arg1", "arg2", 123, new int[] { 1, 2, 3, 4 }, "filtered" );
            }

        };

        log.name = "testTraceOff";
        log.generateLogs();
    }
}
