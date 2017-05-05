package com.ibm.commerce.cache;

/*
 *-----------------------------------------------------------------
 * IBM Confidential
 *
 * OCO Source Materials
 *
 * WebSphere Commerce
 *
 * (C) Copyright IBM Corp. 2013, 2015
 *
 * The source code for this program is not published or otherwise
 * divested of its trade secrets, irrespective of what has
 * been deposited with the U.S. Copyright Office.
 *-----------------------------------------------------------------
 */

import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Gather performance metrics within a java logger output.
 */
public abstract class AbstractLogMetricGatherer implements ILogMetricGatherer {

	/** copyright */
	public static final String COPYRIGHT = com.ibm.commerce.copyright.IBMCopyright.SHORT_COPYRIGHT;

	/**
	 * class name
	 */
	private static final String CLASS_NAME = AbstractLogMetricGatherer.class.getName();

	/**
	 * logger
	 */
	private static final Logger LOGGER = LoggingHelper.getLogger(CLASS_NAME);

	/**
	 * prefix added to all performance logs
	 */
	public static final String COMMON_PREFIX = "PerfLog ";

	/**
	 * performance metric log prefix
	 */
	public static final String PERFORMANCE_METRIC_ENTRYLOG_PREFIX = COMMON_PREFIX
			+ "<entry";

	/**
	 * performance metric log property prefix
	 */
	public static final String PERFORMANCE_METRIC_INFO_PREFIX = COMMON_PREFIX
			+ "<info";

	/**
	 * exit log XML Tag prefix
	 */
	public static final String EXIT_XML_TAG_PREFIX = "<exit";

	/**
	 * performance metric log prefix
	 */
	public static final String PERFORMANCE_METRIC_LOG_PREFIX = COMMON_PREFIX
			+ EXIT_XML_TAG_PREFIX;

	/**
	 * performance metric log suffix
	 */
	public static final String PERFORMANCE_METRIC_LOG_SUFFIX = " />";

	/**
	 * Empty constructor
	 */
	public AbstractLogMetricGatherer() {

	}

    protected String formatInformationLog(String message, Map<String, String> properties )
    {
        final String methodName = "formatInformationLog(String message, Map<String, String> properties)";
        String logMessage = null;
        StringWriter writer = new StringWriter();
        writer.append(PERFORMANCE_METRIC_INFO_PREFIX);
        try {
        	writer.append(" parentId=\"");
        	writer.append(Long.toString(OperationMetric
        			.getThreadParentOperationIdentifier()));
        	writer.append("\"");

        	if (message != null) {
        		writer.append(" msg=\"");
        		CacheUtilities.printXmlEscapedStringToWriter(writer,
        				message);
        		writer.append("\" ");
        	}
        	if (properties != null) {
        		OperationMetric.printPropertiesToWriter(properties, writer);
        	}
        	writer.append(PERFORMANCE_METRIC_LOG_SUFFIX);
        	logMessage = writer.toString();
        } catch (IOException e) {
        	LoggingHelper.logUnexpectedException(LOGGER, CLASS_NAME,
        			methodName, e);
        } finally {
        	CacheUtilities.closeQuietly(writer);
        }
        return logMessage;
    }


    protected static String formatMetricLog(OperationMetric metric, Object returnValue, boolean printProperties)
    {
        String logMessage = null;
        StringWriter writer = new StringWriter();
        try {
        	writer.append(AbstractLogMetricGatherer.PERFORMANCE_METRIC_LOG_PREFIX);
        	metric.toSerializedXmlString(writer);
        	if (returnValue != null) {
        		writer.append(" return=\"");
        		CacheUtilities.printXmlEscapedStringToWriter(writer,
        				returnValue.toString());
        		writer.append("\"");
        	}
        	if( printProperties ) {
        	    metric.printProperty(writer);
        	}

        	writer.append(AbstractLogMetricGatherer.PERFORMANCE_METRIC_LOG_SUFFIX);
            logMessage = writer.toString();
        } catch (IOException e) {
        	LOGGER.log(Level.WARNING, "Failed to serialize string", e);
        }
        CacheUtilities.closeQuietly(writer);
        return logMessage;
    }


	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.commerce.cache.MetricGatherer#start()
	 */
	@Override
	public void start() {
		// nothing to do here.
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.commerce.cache.MetricGatherer#stop()
	 */
	@Override
	public void stop() {
		// nothing to do here.
	}

}
