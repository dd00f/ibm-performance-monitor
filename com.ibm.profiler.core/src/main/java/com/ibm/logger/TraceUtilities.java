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

import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.ConstructorSignature;
import org.aspectj.lang.reflect.MethodSignature;

import com.ibm.commerce.cache.LogMetricGatherer;
import com.ibm.commerce.cache.LoggingHelper;
import com.ibm.commerce.cache.OperationMetric;
import com.ibm.logger.stats.LogType;
import com.ibm.logger.trace.Print;
import com.ibm.logger.trace.PrintMode;

public class TraceUtilities {

    private static final String CLASSNAME = TraceUtilities.class.getCanonicalName();

    /**
     * The logger.
     */
    public static final Logger LOGGER = Logger.getLogger( CLASSNAME );

    /**
     * suggested default method trace aspect
     */
    public static final String DEFAULT_METHOD_TRACE_ASPECT = "!within(*..test..*) && !within(*..*Test*) && !within(*..*Aspect*) && (execution(* *.*(..)))";

    /**
     * suggested default constructor trace aspect
     */
    public static final String DEFAULT_CONSTRUCTOR_TRACE_ASPECT = "!within(*..test..*) && !within(*..*Test*) && !within(*..*Aspect*) && (execution( *.new(..)))";

    /**
     * Get the size of an object. If the operation logger isn't enabled at the
     * FINER level or below, use an approximation to avoid the serialization
     * cost.
     * 
     * @param object
     *            The object to measure.
     * @param approximateSizePerObject
     *            The size to use when the operation logger isn't enabled at
     *            FINER or below.
     * @param logger the logger to use.
     * @param sizeMeasurementLevel The level at which to do fine grain size measurement.
     * @return The measured object size.
     */
    public static int getObjectSize( Object object, int approximateSizePerObject, Logger logger, Level sizeMeasurementLevel ) {
        int returnValue = 0;
        if ( object != null ) {
            if ( logger.isLoggable( sizeMeasurementLevel ) ) {
                // measure the real size of each object using toString operator
                // only if size measurement is enabled.
                String safeToString = getParameterPrintString( object, PrintMode.FULL );
                if ( safeToString != null ) {
                    returnValue = safeToString.length();
                }
            } else {
                returnValue = approximateSizePerObject;
            }
        }
        return returnValue;
    }

    /**
     * Concurrent hash map to look up loggers by name.
     * This is about 3 times faster than doing a Logger.getLogger call.
     */
    private static final ConcurrentHashMap<String, Logger> LOGGER_MAP = new ConcurrentHashMap<String, Logger>();

    /**
     * Get a logger by name.
     * is is about 3 times faster than doing a Logger.getLogger call.
     * 
     * @param loggerName The name of the logger.
     * @return The matching logger.
     */
    public static Logger getLoggerByName( String loggerName ) {
        Logger logger = LOGGER_MAP.get( loggerName );
        if ( logger == null ) {
            logger = Logger.getLogger( loggerName );
            LOGGER_MAP.put( loggerName, logger );
        }
        return logger;
    }

    /**
     * Generate traces and gather statistics on a method.
     * 
     * @param point The proceeding join point.
     * @param gatherSizeLevel Logging level at which the returned value of the function should be measured.
     * @param secure Is the method secure ? If so, both the arguments and the return value will be masked.
     * @param trace Is trace enabled ? If it is, an exit trace will be printed if the logger bound to the class name is
     *            enabled at the FINE level. An entry log will be generated if the logger is enabled at the FINER level.
     * @return The join point returned value.
     * @throws Throwable If the join point throws an exception.
     */
    public static Object traceAndMeasureJoinPoint( ProceedingJoinPoint point, Level gatherSizeLevel, boolean secure, boolean trace ) throws Throwable {
        MethodSignature methodSignature = MethodSignature.class.cast( point.getSignature() );
        Method method = methodSignature.getMethod();
        String opName = method.toString();

        Logger logger = null;
        if ( trace ) {
            logger = getLoggerByName( method.getDeclaringClass().getName() );
        }

        OperationMetric metric = initializeMetricIfEnabled( point, secure, logger, method, opName );

        return traceAndMeasureJoinPointWithLogger( point, gatherSizeLevel, secure, metric, opName, logger, true, method );
    }

    /**
     * Generate traces and gather statistics on a method.
     * 
     * @param point The proceeding join point.
     * @param gatherSizeLevel Logging level at which the returned value of the function should be measured.
     * @param secure Is the method secure ? If so, both the arguments and the return value will be masked.
     * @param trace Is trace enabled ? If it is, an exit trace will be printed if the logger bound to the class name is
     *            enabled at the FINE level. An entry log will be generated if the logger is enabled at the FINER level.
     * @return The join point returned value.
     * @throws Throwable If the join point throws an exception.
     */
    public static Object traceAndMeasureConstructorJoinPoint( ProceedingJoinPoint point, Level gatherSizeLevel, boolean secure, boolean trace ) throws Throwable {
        ConstructorSignature methodSignature = ConstructorSignature.class.cast( point.getSignature() );
        Constructor<?> method = methodSignature.getConstructor();
        String opName = method.toString();

        Logger logger = null;
        if ( trace ) {
            logger = getLoggerByName( method.getDeclaringClass().getName() );
        }

        OperationMetric metric = initializeConstructorMetricIfEnabled( point, secure, logger, method, opName );

        return traceAndMeasureJoinPointWithLogger( point, gatherSizeLevel, secure, metric, opName, logger, true, method );
    }

    /**
     * Generate traces and gather statistics on a method.
     * 
     * @param point The proceeding join point.
     * @param gatherSizeLevel Logging level at which the returned value of the function should be measured.
     * @param secure Is the method secure ? If so, both the arguments and the return value will be masked.
     * @param logger The logger to use.
     * @param operationNamePrefix the name of the layer being measured, appended as a prefix to the operation name.
     * @param printReturnValue option to print the returned value.
     * @return The join point returned value.
     * @throws Throwable If the join point throws an exception.
     */
    public static Object traceAndMeasureJoinPointWithLogger( ProceedingJoinPoint point, Level gatherSizeLevel, boolean secure, Logger logger, String operationNamePrefix, boolean printReturnValue ) throws Throwable {
        MethodSignature methodSignature = MethodSignature.class.cast( point.getSignature() );
        Method method = methodSignature.getMethod();
        String opName = operationNamePrefix + method.toString();

        OperationMetric metric = initializeMetricIfEnabled( point, secure, logger, method, opName );

        return traceAndMeasureJoinPointWithLogger( point, gatherSizeLevel, secure, metric, opName, logger, printReturnValue, method );
    }

    private static OperationMetric initializeMetricIfEnabled( ProceedingJoinPoint point, boolean secure, Logger logger, Method method, String opName ) {
        OperationMetric metric = null;
        if ( logger != null && logger.isLoggable( Level.FINE ) ) {
            metric = initializeMetric( point, secure, opName, method );
            if ( logger.isLoggable( Level.FINER ) ) {
                LogMetricGatherer.logEntryLogMetricToLogger( metric, logger, Level.FINER );
            }
        }
        return metric;
    }

    /**
     * Generate traces and gather statistics on a constructor.
     * 
     * @param point The proceeding join point.
     * @param gatherSizeLevel Logging level at which the returned value of the function should be measured.
     * @param secure Is the method secure ? If so, both the arguments and the return value will be masked.
     * @param logger The logger to use.
     * @param operationNamePrefix the name of the layer being measured, appended as a prefix to the operation name.
     * @param printReturnValue option to print the returned value.f
     * @return The join point returned value.
     * @throws Throwable If the join point throws an exception.
     */
    public static Object traceAndMeasureConstructorJoinPointWithLogger( ProceedingJoinPoint point, Level gatherSizeLevel, boolean secure, Logger logger, String operationNamePrefix, boolean printReturnValue ) throws Throwable {
        ConstructorSignature constructorSignature = ConstructorSignature.class.cast( point.getSignature() );
        Constructor<?> constructor = constructorSignature.getConstructor();
        String opName = operationNamePrefix + constructor.toString();

        OperationMetric metric = initializeConstructorMetricIfEnabled( point, secure, logger, constructor, opName );

        return traceAndMeasureJoinPointWithLogger( point, gatherSizeLevel, secure, metric, opName, logger, printReturnValue, constructor );
    }

    private static OperationMetric initializeConstructorMetricIfEnabled( ProceedingJoinPoint point, boolean secure, Logger logger, Constructor<?> constructor, String opName ) {
        OperationMetric metric = null;
        if ( logger != null && logger.isLoggable( Level.FINE ) ) {
            metric = initializeConstructorMetric( point, secure, opName, constructor );
            if ( logger.isLoggable( Level.FINER ) ) {
                LogMetricGatherer.logEntryLogMetricToLogger( metric, logger, Level.FINER );
            }
        }
        return metric;
    }

    private static Object traceAndMeasureJoinPointWithLogger( ProceedingJoinPoint point, Level gatherSizeLevel, boolean secure, OperationMetric metric, String opName, Logger logger, boolean printReturnValue, AccessibleObject method ) throws Throwable {
        Object result = null;
        Object measuredResult = null;
        boolean failed = false;
        long startTime = 0;
        if( metric == null ) {
        	startTime = System.nanoTime();
        }
        
        try {
            result = point.proceed();
            measuredResult = result;
        } catch ( Throwable ex ) {
            failed = true;
            measuredResult = ex;
            throw ex;
        } finally {
            stopMetric( gatherSizeLevel, secure, metric, logger, printReturnValue, method, measuredResult, failed );
            long duration = 0;
            if( metric == null ) {
                duration = System.nanoTime() - startTime;
                PerformanceLogger.increase( opName, duration, failed, LogType.TIMER );
            }
        }
        return result;
    }

    private static void stopMetric( Level gatherSizeLevel, boolean secure, OperationMetric metric, Logger logger, boolean printReturnValue, AccessibleObject method, Object measuredResult, boolean failed ) {
        if ( metric != null ) {
            int objectSize = getObjectSize( measuredResult, 1000, logger, gatherSizeLevel );
            metric.stopOperation( objectSize, false, !failed );
            if ( printReturnValue ) {
                if ( secure ) {
                    measuredResult = "********";
                } else if ( measuredResult != null ) {
                    Print returnValuePrintMode = method.getAnnotation( Print.class );
                    if ( returnValuePrintMode != null ) {
                        PrintMode printMode = returnValuePrintMode.value();
                        measuredResult = getParameterPrintString( measuredResult, printMode );
                    }
                }
                LogMetricGatherer.logMetricToLogger( metric, logger, Level.FINE, measuredResult );
            } else {
                LogMetricGatherer.logMetricToLogger( metric, logger, Level.FINE );
            }
        }
    }

    /**
     * Initialize an operation metric object.
     * 
     * @param point The join point to measure.
     * @return The created operation metric. Null if anything went wrong.
     */
    private static OperationMetric initializeMetric( ProceedingJoinPoint point, boolean secure, String operationName, Method method ) {
        OperationMetric metric = null;
        String argumentString = getArgumentString( point, secure, method );
        String opName = operationName;
        metric = new OperationMetric();
        metric.startOperation( opName, false, argumentString );

        return metric;
    }

    /**
     * Initialize an operation metric object for a constructor.
     * 
     * @param point The join point to measure.
     * @return The created operation metric. Null if anything went wrong.
     */
    private static OperationMetric initializeConstructorMetric( ProceedingJoinPoint point, boolean secure, String operationName, Constructor<?> constructor ) {
        OperationMetric metric = null;

        String argumentString = getConstructorArgumentString( point, secure, constructor );
        String opName = operationName;
        metric = new OperationMetric();
        metric.startOperation( opName, false, argumentString );

        return metric;
    }

    private static String getArgumentString( ProceedingJoinPoint point, boolean secure, Method method ) {
        // mask the arguments to avoid showing them
        StringBuilder arguments = new StringBuilder();
        if ( !secure ) {
            Object[] argumentArray = point.getArgs();
            Annotation[][] parameterAnnotations = method.getParameterAnnotations();
            appendArgumentsWithPrintModeAnnotation( arguments, argumentArray, parameterAnnotations );
        } else {
            arguments.append( "********" );
        }
        String argumentString = arguments.toString();
        return argumentString;
    }

    private static void appendArgumentsWithPrintModeAnnotation( StringBuilder arguments, Object[] argumentArray, Annotation[][] parameterAnnotations ) {
        int length = argumentArray.length;
        boolean first = true;
        for ( int i = 0; i < length; ++i ) {
            if ( first ) {
                first = false;
            } else {
                arguments.append( ", " );
            }
            Annotation[] annotation = parameterAnnotations[i];
            Object argumentValue = argumentArray[i];
            PrintMode printMode = getPrintMode( annotation );
            String parameterPrintString = getParameterPrintString( argumentValue, printMode );
            arguments.append( parameterPrintString );
        }
    }

    private static String getConstructorArgumentString( ProceedingJoinPoint point, boolean secure, Constructor<?> constructor ) {
        // mask the arguments to avoid showing them
        StringBuilder arguments = new StringBuilder();
        if ( !secure ) {
            Object[] argumentArray = point.getArgs();
            Annotation[][] parameterAnnotations = constructor.getParameterAnnotations();
            appendArgumentsWithPrintModeAnnotation( arguments, argumentArray, parameterAnnotations );
        } else {
            arguments.append( "********" );
        }
        String argumentString = arguments.toString();
        return argumentString;
    }

    /**
     * Print an object to a string based on the print mode selected. This method will catch any exceptions that occur
     * during the toString operations.
     * 
     * @param argumentValue the value to print.
     * @param printMode The print mode to use.
     * @return The string used to print the argumentValue parameter. May be null.
     */
    public static String getParameterPrintString( Object argumentValue, PrintMode printMode ) {
        final String METHODNAME = "getParameterPrintString( Object argumentValue, PrintMode printMode )";
        String returnValue = null;

        try {
            switch ( printMode ) {
                case HASH:
                    returnValue = getHashCodeString( argumentValue );
                    break;

                case MASK:
                    returnValue = "********";
                    break;

                case NO_DISPLAY:
                    returnValue = "";
                    break;

                default:
                    if ( argumentValue == null ) {
                        returnValue = null;
                    } else if ( argumentValue.getClass().isArray() ) {
                        if ( argumentValue.getClass().getComponentType().isPrimitive() ) {
                            int length = Array.getLength( argumentValue );
                            StringBuilder builder = new StringBuilder( "[" );
                            boolean first = true;
                            for ( int i = 0; i < length; i++ ) {
                                if ( first ) {
                                    first = false;
                                } else {
                                    builder.append( ", " );
                                }
                                builder.append( Array.get( argumentValue, i ) );
                            }
                            builder.append( "]" );
                            returnValue = builder.toString();
                        } else {
                            returnValue = Arrays.deepToString( (Object[]) argumentValue );
                        }
                    } else {
                        returnValue = argumentValue.toString();
                    }
            }
        } catch ( Exception ex ) {
            LoggingHelper.logUnexpectedException( LOGGER, CLASSNAME, METHODNAME, ex );
        }
        return returnValue;
    }

    private static String getHashCodeString( Object argumentValue ) {
        if ( argumentValue != null ) {
            return Integer.toString( argumentValue.hashCode() );
        }
        return "0";
    }

    private static PrintMode getPrintMode( Annotation[] annotations ) {
        for ( Annotation annotation : annotations ) {
            if ( annotation instanceof Print ) {
                return Print.class.cast( annotation ).value();
            }
        }
        return PrintMode.FULL;
    }

}
