package com.ibm.commerce.cache;

/*
 *-----------------------------------------------------------------
 * IBM Confidential
 *
 * OCO Source Materials
 *
 * WebSphere Commerce
 *
 * (C) Copyright IBM Corp. 2013, 2014
 *
 * The source code for this program is not published or otherwise
 * divested of its trade secrets, irrespective of what has
 * been deposited with the U.S. Copyright Office.
 *-----------------------------------------------------------------
 */

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;

/**
 * Extracts performance metrics from a log file.
 */
public class LogMetricFileLoader extends MetricFileLoader {

	/** copyright */
	public static final String COPYRIGHT = com.ibm.commerce.copyright.IBMCopyright.SHORT_COPYRIGHT;

	/**
	 * class name
	 */
	private static final String CLASS_NAME = LogMetricFileLoader.class
			.getName();

	/**
	 * logger
	 */
	private static final Logger LOGGER = LoggingHelper.getLogger(CLASS_NAME);
	
	private DocumentBuilderFactory dbFactory;
	private DocumentBuilder dBuilder;

	/**
	 * Constructor
	 * 
	 * @throws ParserConfigurationException
	 *             unexpected.
	 */
	public LogMetricFileLoader() throws ParserConfigurationException {
		dbFactory = DocumentBuilderFactory.newInstance();
		dBuilder = dbFactory.newDocumentBuilder();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.commerce.cache.MetricFileLoader#parseLine(java.lang.String)
	 */
	@Override
	public OperationMetric parseLine(String readLine) {
		String serializedLogText = isolateSerializedLogText(readLine);
		if (serializedLogText == null) {
			return null;
		}
		OperationMetric metric = null;
		InputStream sis = null;
		try {
			//$ANALYSIS-IGNORE
			sis = new ByteArrayInputStream(serializedLogText.getBytes(Charset.forName("UTF-8")));
			Document parse = dBuilder.parse(sis);
			metric = new OperationMetric();
			boolean success = metric.fromXmlDocument(parse);
			if (success) {
			    
			    adjustCassandraMetric(metric);
                adjustJdbcMetric(metric);
			    
				return metric;
			}
		} catch (Exception e) {
			LoggingHelper.logUnexpectedException(LOGGER, CLASS_NAME, "parseLine(String readLine)", e);
		} finally {
			CacheUtilities.closeQuietly(sis);
		}

		return null;
	}

	/**
	 * Isolate the performance metric from the log line.
	 * 
	 * @param readLine
	 *            The line to analyze.
	 * @return The performance metric string. Null if none could be found.
	 */
	public String isolateSerializedLogText(String readLine) {
		if (readLine == null) {
			return null;
		}

		String[] split = readLine
				.split(LogMetricGatherer.EXIT_XML_TAG_PREFIX);
		if (split.length < 2) {
			// marker not found
			return null;
		}

		return LogMetricGatherer.EXIT_XML_TAG_PREFIX + split[1];
	}
}
