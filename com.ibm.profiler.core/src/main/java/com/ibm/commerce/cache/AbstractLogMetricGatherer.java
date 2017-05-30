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

import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Gather performance metrics within a java logger output.
 */
public abstract class AbstractLogMetricGatherer implements ILogMetricGatherer {

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
