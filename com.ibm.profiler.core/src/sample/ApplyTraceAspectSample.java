package com.ibm.logger;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

@Aspect
public class ApplyTraceAspectSample {


	@Around("execution(com.ibm.logger.FakeClassWithAspect.new(int))")
	public Object aroundConstructorWithLogger(ProceedingJoinPoint point)
			throws Throwable {
		boolean printReturnValue = true;
		Level sizeLevel = Level.FINER;
		boolean secure = false;
		String prefix = "Operation : ";
		Logger serviceLogger = Logger.getLogger("my.logger.ClassName");
		return TraceUtilities.traceAndMeasureConstructorJoinPointWithLogger(
				point, sizeLevel, secure, serviceLogger, prefix,
				printReturnValue);
	}

	@Around("execution(com.ibm.logger.FakeClassWithAspect.new(String))")
	public Object aroundConstructor(ProceedingJoinPoint point) throws Throwable {
		
		boolean printReturnValue = true;
		Level sizeLevel = Level.FINER;
		boolean secure = false;
		boolean trace = true;
		String prefix = "Operation : ";
		Logger serviceLogger = Logger.getLogger("my.logger.ClassName");

		
		return TraceUtilities.traceAndMeasureConstructorJoinPoint(point,
				sizeLevel, secure, trace);
	}

	@Around("execution(* com.ibm.logger.FakeClassWithAspect.methodWithTrace(..))")
	public Object aroundTrace(ProceedingJoinPoint point) throws Throwable {
		return TraceUtilities.traceAndMeasureJoinPoint(point, sizeLevel,
				secure, trace);
	}

	@Around("execution(* my.sample.ClassName.*(..))")
	public Object aroundService(ProceedingJoinPoint point) throws Throwable {

		boolean printReturnValue = true;
		Level sizeLevel = Level.FINER;
		boolean secure = false;
		String prefix = "Operation : ";
		Logger serviceLogger = Logger.getLogger("my.logger.ClassName");

		return com.ibm.logger.TraceUtilities
				.traceAndMeasureJoinPointWithLogger(point, sizeLevel, secure,
						serviceLogger, prefix, printReturnValue);
	}
}
