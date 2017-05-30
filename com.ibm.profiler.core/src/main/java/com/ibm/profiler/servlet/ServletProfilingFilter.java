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
package com.ibm.profiler.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponseWrapper;

import com.ibm.commerce.cache.CacheUtilities;
import com.ibm.commerce.cache.ILogMetricGatherer;
import com.ibm.commerce.cache.LoggingHelper;
import com.ibm.commerce.cache.MetricGatherer;
import com.ibm.commerce.cache.OperationMetric;
import com.ibm.logger.PerformanceLogger;
import com.ibm.service.detailed.ServletLogger;
import com.ibm.service.entry.ServletEntryLogger;

/**
 * Filter used to log the entry and exit of every JSP fragment using the service
 * logger.
 */
public class ServletProfilingFilter implements Filter {

	/**
	 * Class name
	 */
	private static final String CLASSNAME = ServletProfilingFilter.class
			.getName();

	/**
	 * The INCLUDE action servlet path attribute name
	 */
	private static final String JAVAX_SERVLET_INCLUDE_SERVLET_PATH = "javax.servlet.include.servlet_path";

	/**
	 * The ERROR action servlet path attribute name
	 */
	private static final String JAVAX_SERVLET_ERROR_REQUEST_URI = "javax.servlet.error.request_uri";

	/**
	 * The FORWARD action servlet path attribute name
	 */
	private static final String JAVAX_SERVLET_FORWARD_PATH_INFO = "javax.servlet.forward.path_info";


	/**
	 * The class logger.
	 */
	private static final Logger LOGGER = LoggingHelper.getLogger(CLASSNAME);

	/**
	 * Fetch the log gatherer used to measure the request.
	 * @param isInitialRequest Is this the first request to hit the servlet engine or an internal include/forward request.
	 * @return The log gatherer used for this servlet profiler.
	 */
	public ILogMetricGatherer getLogGatherer(boolean isInitialRequest) {
	    if( isInitialRequest ) {
	        return ServletEntryLogger.LOG_GATHERER;
	    }
		return ServletLogger.LOG_GATHERER;
	}

	/**
	 * Main init method, called to initialise the cache filter.
	 * 
	 * @see javax.servlet.Filter#init(FilterConfig)
	 * @param arg0
	 *            FilterConfig
	 * @throws ServletException
	 *             not used.
	 */
	@Override
    public void init(FilterConfig arg0) throws ServletException {

	}

	/**
	 * Mask variable values to avoid exposing sensitive information. Extend this
	 * method to mask parameter values.
	 * 
	 * @param parameterName
	 *            The name of the parameter.
	 * @param parameterValue
	 *            The parameter value.
	 * @return The parameter value, potentially masked.
	 */
	public String getMaskedValue(String parameterName, String parameterValue) {
		return parameterValue;
		// return MaskingFactory.getInstance().mask(parameterName,
		// parameterValue);
	}

	/**
	 * Chaining method doFilter is called from RuntimeServletFilter and calls
	 * the RequestServlet
	 * 
	 * @see javax.servlet.Filter#doFilter(ServletRequest, ServletResponse,
	 *      FilterChain)
	 * @param servletRequest
	 *            javax.servlet.ServletRequest
	 * @param servletResponse
	 *            javax.servket.ServletResponse
	 * @param filterChain
	 *            javax.servlet.FilterChain
	 * @throws ServletException
	 *             generated deeper in the filter chain.
	 * @throws IOException
	 *             generated deeper in the filter chain.
	 */
	@Override
    public void doFilter(ServletRequest servletRequest,
			ServletResponse servletResponse, FilterChain filterChain)
			throws IOException, ServletException {
		final String METHODNAME = "doFilter(ServletRequest servletRequest,ServletResponse servletResponse, FilterChain filterChain)";

		boolean entryExitTraceEnabled = LoggingHelper
				.isEntryExitTraceEnabled(LOGGER);
		// boolean traceEnabled = LoggingHelper.isTraceEnabled(LOGGER);
		if (entryExitTraceEnabled) {
			Object[] parameters = new Object[] { servletRequest,
					servletResponse, filterChain };
			LOGGER.entering(CLASSNAME, METHODNAME, parameters);
		}

		boolean parentIdUpdated = false;
		Long threadParentOperationIdentifier = OperationMetric
				.getThreadParentOperationIdentifier();
		if (threadParentOperationIdentifier.longValue() == 0
				&& servletRequest instanceof HttpServletRequest) {
		    parentIdUpdated = true;
			CacheUtilities
					.updateOperationParentIdentifierFromHeader((HttpServletRequest) servletRequest);
		}

		OperationMetric metric = null;
		int startSize = 0;
		boolean isInitialRequest = isInitialRequest(servletRequest);
		boolean measurementEnabled = isMeasurementEnabledForRequest(servletRequest, isInitialRequest);
		boolean sizeMeasurementEnabled = false;

		if (measurementEnabled) {
			sizeMeasurementEnabled = isResponseSizeMeasurementEnabled(isInitialRequest);
			metric = initializeMetric(servletRequest, isInitialRequest);
			if (sizeMeasurementEnabled) {
				startSize = getCurrentResponseSize(servletResponse);
			}
		}

		boolean successful = false;
		try {
			filterChain.doFilter(servletRequest, servletResponse);
			successful = true;
		} finally {
			if (measurementEnabled) {
				logMetric(servletRequest, servletResponse, metric, startSize,
						sizeMeasurementEnabled, getLogGatherer(isInitialRequest), successful);
			}

			if (parentIdUpdated) {
				OperationMetric
						.setThreadParentOperationIdentifier(threadParentOperationIdentifier);
			}

			if (entryExitTraceEnabled) {
				LOGGER.exiting(CLASSNAME, METHODNAME);
			}
		}

	}

	/**
	 * Log an operation metric.
	 * 
	 * @param servletRequest
	 *            The servlet request.
	 * @param servletResponse
	 *            The servlet response.
	 * @param metric
	 *            The metric to log.
	 * @param startSize
	 *            The request start size.
	 * @param sizeMeasurementEnabled
	 *            option indicating if size measurement is enabled.
	 * @param logGatherer
	 *            The metric gatherer.
	 * @param successful
	 *            Flag indicating if the operation was successful or not.
	 */
	public static void logMetric(ServletRequest servletRequest,
			ServletResponse servletResponse, OperationMetric metric,
			int startSize, boolean sizeMeasurementEnabled,
			MetricGatherer logGatherer, boolean successful) {
		final String METHODNAME = "logMetric(ServletRequest servletRequest,"
				+ "ServletResponse servletResponse, OperationMetric metric,int startSize, "
				+ "boolean sizeMeasurementEnabled, MetricGatherer gatherer, boolean successful)";
		final boolean entryExitTraceEnabled = LoggingHelper
				.isEntryExitTraceEnabled(LOGGER);
		final boolean traceEnabled = LoggingHelper.isTraceEnabled(LOGGER);
		if (entryExitTraceEnabled) {
			Object[] parameters = new Object[] { servletRequest,
					servletResponse, metric, startSize, sizeMeasurementEnabled,
					logGatherer };
			LOGGER.entering(CLASSNAME, METHODNAME, parameters);
		}

		try {
			int fragmentSize = 1000; // dummy value to begin with
			if (sizeMeasurementEnabled) {
				int stopSize = getCurrentResponseSize(servletResponse);
				fragmentSize = stopSize - startSize;
				if (fragmentSize < 1) {
					// ensure no fragment size is below one byte in size
					if (traceEnabled) {
						String message = "Response size is below 1 at "
								+ fragmentSize + " with start size "
								+ startSize + " for operation : "
								+ metric.getOperationName();
						LOGGER.logp(LoggingHelper.DEFAULT_TRACE_LOG_LEVEL,
								CLASSNAME, METHODNAME, message);
					}

				}
			}
			boolean wasCacheHit = false;

			metric.stopOperation(fragmentSize, wasCacheHit, successful);

			logGatherer.gatherMetric(metric);
			
			if( isInitialRequest(servletRequest)) {
				metric.setOperationName("Servlet : Request : All");
				PerformanceLogger.increase(metric);
			}
			
			
		} catch (Exception ex) {
			LoggingHelper.logUnexpectedException(LOGGER, CLASSNAME, METHODNAME,
					ex);
		}

		if (entryExitTraceEnabled) {
			LOGGER.exiting(CLASSNAME, METHODNAME);
		}
	}

	/**
	 * test to see if a request is an initial request in a servlet chain.
	 * Returns true if the request is not a include, forward or error request.
	 * 
	 * @param servletRequest
	 *            the request.
	 * @return True if the request is the initial one seen by the server.
	 */
	private static boolean isInitialRequest(ServletRequest servletRequest) {
		return servletRequest.getAttribute(JAVAX_SERVLET_INCLUDE_SERVLET_PATH) == null
				&& servletRequest.getAttribute(JAVAX_SERVLET_FORWARD_PATH_INFO) == null
				&& servletRequest.getAttribute(JAVAX_SERVLET_ERROR_REQUEST_URI) == null;
	}

	/**
	 * Initialize a metric object.
	 * 
	 * @param servletRequest
	 *            The servlet request.
	 * @param is the request the initial one.
	 * @return The operation metric.
	 */
	private OperationMetric initializeMetric(ServletRequest servletRequest, boolean isInitialRequest) {

		final String METHODNAME = "initializeMetric(ServletRequest servletRequest)";
		final boolean entryExitTraceEnabled = LoggingHelper
				.isEntryExitTraceEnabled(LOGGER);
		// final boolean traceEnabled = LoggingHelper.isTraceEnabled(LOGGER);
		if (entryExitTraceEnabled) {
			Object[] parameters = new Object[] { servletRequest };
			LOGGER.entering(CLASSNAME, METHODNAME, parameters);
		}

		OperationMetric metric = null;
		try {
			metric = new OperationMetric();
			String[] orderedKeyValues = getOrderedParameterKeyValueArray(servletRequest,isInitialRequest);
			String currentUrl = getRequestDescription(servletRequest);
			metric.startOperation(currentUrl, false, orderedKeyValues);

			getLogGatherer(isInitialRequest).gatherMetricEntryLog(metric);
		} catch (Exception ex) {
			LoggingHelper.logUnexpectedException(LOGGER, CLASSNAME, METHODNAME,
					ex);
		}

		if (entryExitTraceEnabled) {
			LOGGER.exiting(CLASSNAME, METHODNAME, metric);
		}

		return metric;
	}

    /**
     * fetch the unique URL key-value parameters based on URL parameters. Note
     * that parameters will be reordered alphabetically to ensure consistency
     * between callers.
     * 
     * @param request
     *            the servlet request
     * @param isInitialRequest
     *            Is this the initial servlet request.
     * @return the unique key-value string array.
     */
	public String[] getOrderedParameterKeyValueArray(ServletRequest request, boolean isInitialRequest) {
		final String METHODNAME = "getOrderedParameterKeyValueArray(ServletRequest request)";
		final boolean entryExitTraceEnabled = LoggingHelper
				.isEntryExitTraceEnabled(LOGGER);
		// final boolean traceEnabled = LoggingHelper.isTraceEnabled(LOGGER);
		if (entryExitTraceEnabled) {
			Object[] parameters = new Object[] { request };
			LOGGER.entering(CLASSNAME, METHODNAME, parameters);
		}

		SortedMap<String, String[]> keyValueSortedMap = new TreeMap<String, String[]>();

		Map<String, String[]> parameterMap = request.getParameterMap();

		keyValueSortedMap.putAll(parameterMap);

		int keyValueCount = keyValueSortedMap.size();
		int parameterCount = keyValueCount * 2;
		if( isInitialRequest ) {
		    parameterCount += 2;
		}
        int i = 0;
        String[] orderedParametersKeyValues = new String[parameterCount];
        if( isInitialRequest ) {
            // add the path info for initial requests only.
            orderedParametersKeyValues[i++] = "pathInfo";
            String pathInfo = "";
            if( request instanceof HttpServletRequest ) {
                HttpServletRequest hsr = (HttpServletRequest) request;
                pathInfo = hsr.getPathInfo();
            }
            orderedParametersKeyValues[i++] = pathInfo;
        }

		Set<Entry<String, String[]>> entrySet = keyValueSortedMap.entrySet();
		for (Entry<String, String[]> entry : entrySet) {
			String key = entry.getKey();
			String stringValue = null;

			orderedParametersKeyValues[i++] = key;
			Object value = entry.getValue();
			if (value != null) {
				if (value.getClass().isArray()) {
					stringValue = Arrays.deepToString((Object[]) value);
				} else {
					stringValue = value.toString();
				}
			}

			stringValue = getMaskedValue(key, stringValue);
			orderedParametersKeyValues[i++] = stringValue;
		}

		if (entryExitTraceEnabled) {
			LOGGER.exiting(CLASSNAME, METHODNAME, orderedParametersKeyValues);
		}
		return orderedParametersKeyValues;
	}

	/**
	 * test to see if response size measurement is enabled
	 * @param isInitialRequest is this the initial servlet request.
	 * 
	 * @return true if response size measurement is enabled
	 */
	public boolean isResponseSizeMeasurementEnabled(boolean isInitialRequest) {
		return ServletLogger.LOGGER.isLoggable(Level.FINEST);
	}

	/**
	 * Determine if a request will be measured. This is determined based on the
	 * level of the performance logger and characteristics of the request.
	 * 
	 * @param servletRequest
	 *            The servlet request.
	 * @param isInitialRequest is this the main servlet entry point. False during include/forward/error operations.
	 * @return True if the request is measured.
	 */
	public boolean isMeasurementEnabledForRequest(ServletRequest servletRequest, boolean isInitialRequest) {
		return getLogGatherer(isInitialRequest).isEnabled();
	}

	/**
	 * Get the description of the request, including the type of request and the
	 * URL.
	 * 
	 * @param servletRequest
	 *            the servlet request.
	 * @return the description of the request, including the type of request and
	 *         the URL. This value is null if the request isn't one that we want
	 *         to log.
	 */
	public static String getRequestDescription(ServletRequest servletRequest) {
		final String METHODNAME = "getRequestDescription(ServletRequest servletRequest)";
		boolean entryExitTraceEnabled = LoggingHelper
				.isEntryExitTraceEnabled(LOGGER);
		if (entryExitTraceEnabled) {
			Object[] parameters = new Object[] { servletRequest };
			LOGGER.entering(CLASSNAME, METHODNAME, parameters);
		}

		String method = "GET";
		String uri = "";
		String mode = "Request";
		String servletPath = "";
		if (servletRequest instanceof HttpServletRequest) {
			HttpServletRequest httpRequest = (HttpServletRequest) servletRequest;
			method = httpRequest.getMethod();
			uri = httpRequest.getRequestURI();
			servletPath = httpRequest.getContextPath() + httpRequest.getServletPath();
		}

		// priority 1 : JSP servlet
		Object servletPathValue = servletRequest
				.getAttribute(JAVAX_SERVLET_INCLUDE_SERVLET_PATH);

		if (servletPathValue instanceof String) {
			uri = (String) servletPathValue;
			mode = "Include";
		} else {
			// priority 2 : forward path
			Object forwardPathValue = servletRequest
					.getAttribute(JAVAX_SERVLET_FORWARD_PATH_INFO);

			if (forwardPathValue instanceof String) {
				uri = (String) forwardPathValue;
				mode = "Forward";
			} else {
				// priority 3 : error path
				Object errorPath = servletRequest
						.getAttribute(JAVAX_SERVLET_ERROR_REQUEST_URI);

				if (errorPath instanceof String) {
					uri = (String) errorPath;
					mode = "Error";
				}
				else {
				    // this is a request. Abstract out the path to prevent path variables.
				    uri = servletPath;
				}
			}
		}

		// priority 3 : raw request URL
		// Object requestUriValue = servletRequest
		// .getAttribute(COM_IBM_WEBSPHERE_SERVLET_URI_NON_DECODED);
		// if (requestUriValue instanceof String) {
		// String uri = (String) requestUriValue;
		// if (traceEnabled) {
		// String message = "Consumed Servlet request to : " + uri;
		// LOGGER.logp(LoggingHelper.DEFAULT_TRACE_LOG_LEVEL, CLASSNAME,
		// METHODNAME, message);
		// }
		// returnValue = "Request : " + uri;
		// } else {
		// if (traceEnabled) {
		// String message = "Ignored Unidentified Servlet Request.";
		// LOGGER.logp(LoggingHelper.DEFAULT_TRACE_LOG_LEVEL, CLASSNAME,
		// METHODNAME, message);
		// }
		// }

		String returnValue = "Servlet : " + mode + " " + method + " " + uri;

		if (entryExitTraceEnabled) {
			LOGGER.exiting(CLASSNAME, METHODNAME, returnValue);
		}

		return returnValue;
	}

	/**
	 * Print a map to a string.
	 * 
	 * @param mapToPrint
	 *            the map to print.
	 * @return The String version of the map.
	 */
	public static String mapToString(Map<?, ?> mapToPrint) {
		if (mapToPrint == null) {
			return "";
		}

		if (mapToPrint.isEmpty()) {
			return "";
		}

		StringBuilder buffer = new StringBuilder();
		Set<? extends Map.Entry<?, ?>> entrySet = mapToPrint.entrySet();
		Iterator<? extends Map.Entry<?, ?>> it = entrySet.iterator();
		while (it.hasNext()) {
			Map.Entry<?, ?> entry = it.next();
			Object key = entry.getKey();
			buffer.append("[");
			if (key != mapToPrint) {
				buffer.append(key);
			} else {
				buffer.append("(this)");
			}
			buffer.append('=');
			Object value = entry.getValue();
			if (value != mapToPrint) {
				buffer.append(value);
			} else {
				buffer.append("(this)");
			}
			buffer.append("]");
			if (it.hasNext()) {
				buffer.append(" ");
			}
		}
		return buffer.toString();
	}

	/**
	 * @see javax.servlet.Filter#destroy()
	 */
	@Override
    public void destroy() {

	}

	/**
	 * Get the current response size. This method isn't guaranteed to work all
	 * the time. It does it's best effort to fetch the current response size.
	 * 
	 * @param servletResponse
	 *            the current response.
	 * @return The current response size in bytes.
	 */
	public static int getCurrentResponseSize(ServletResponse servletResponse) {
		final String METHODNAME = "getCurrentResponseSize(ServletResponse servletResponse)";
		final boolean entryExitTraceEnabled = LoggingHelper
				.isEntryExitTraceEnabled(LOGGER);
		final boolean traceEnabled = LoggingHelper.isTraceEnabled(LOGGER);
		if (entryExitTraceEnabled) {
			Object[] parameters = new Object[] { servletResponse };
			LOGGER.entering(CLASSNAME, METHODNAME, parameters);
		}

		int returnValue = 0;

		try {
			ServletResponse currentResponse = servletResponse;
			while (currentResponse != null) {
				Writer responseWriter = fetchMeasurableWriterFromResponse(currentResponse);

				if (responseWriter instanceof StringWriter) {
					StringWriter bw = (StringWriter) responseWriter;
					returnValue = bw.getBuffer().length();

					if (traceEnabled) {
						String message = "Found a StringWriter that could be used. Result : "
								+ returnValue;
						LOGGER.logp(LoggingHelper.DEFAULT_TRACE_LOG_LEVEL,
								CLASSNAME, METHODNAME, message);
					}

					break;
				}

				if (currentResponse instanceof HttpServletResponseWrapper) {
					HttpServletResponseWrapper wrapper = (HttpServletResponseWrapper) currentResponse;
					currentResponse = wrapper.getResponse();

					if (traceEnabled) {
						String message = "No measureable writer found on response of type "
								+ wrapper.getClass().getName()
								+ ", attempting to fetch one from the wrapped response of type "
								+ currentResponse.getClass().getName();
						LOGGER.logp(LoggingHelper.DEFAULT_TRACE_LOG_LEVEL,
								CLASSNAME, METHODNAME, message);
					}
				} else {
					break;
				}
			}
		} catch (Exception ex) {
			LoggingHelper.logUnexpectedException(LOGGER, CLASSNAME, METHODNAME,
					ex);
		}

		if (traceEnabled) {
			String message = "Failed to fetch size from BufferedWriter or StringWriter. Best match found of : "
					+ returnValue;
			LOGGER.logp(LoggingHelper.DEFAULT_TRACE_LOG_LEVEL, CLASSNAME,
					METHODNAME, message);
		}

		if (entryExitTraceEnabled) {
			LOGGER.exiting(CLASSNAME, METHODNAME, returnValue);
		}

		return returnValue;
	}

	/**
	 * Attempt to fetch a measurable writer from the response. Returns null if
	 * none can be found.
	 * 
	 * @param servletResponse
	 *            The servlet response.
	 * @return The measurable response writer, if one could be found.
	 */
	@SuppressWarnings("resource")
	private static Writer fetchMeasurableWriterFromResponse(
			ServletResponse servletResponse) {
		final String METHODNAME = "fetchMeasurableWriterFromResponse(ServletResponse servletResponse)";
		final boolean entryExitTraceEnabled = LoggingHelper
				.isEntryExitTraceEnabled(LOGGER);
		final boolean traceEnabled = LoggingHelper.isTraceEnabled(LOGGER);
		if (entryExitTraceEnabled) {
			Object[] parameters = new Object[] { servletResponse };
			LOGGER.entering(CLASSNAME, METHODNAME, parameters);
		}
		Writer writer = null;
		try {
			writer = servletResponse.getWriter();
			while (true) {

				if (writer instanceof PrintWriter) {
					PrintWriter pw = (PrintWriter) writer;
					Field outField = pw.getClass().getDeclaredField("out");
					outField.setAccessible(true);
					writer = (Writer) outField.get(pw);
					if (traceEnabled) {

						String message = "Fetched writer from PrintWriter private member : out.";
						LOGGER.logp(LoggingHelper.DEFAULT_TRACE_LOG_LEVEL,
								CLASSNAME, METHODNAME, message);
					}
				} else if (writer instanceof StringWriter) {
					if (traceEnabled) {

						String message = "Found StringWriter, returning.";
						LOGGER.logp(LoggingHelper.DEFAULT_TRACE_LOG_LEVEL,
								CLASSNAME, METHODNAME, message);
					}
					break;
				} else {
					// unable to dig through.
					if (traceEnabled) {

						if (writer == null) {
							String message = "Failed to fetch writer size on null writer.";
							LOGGER.logp(LoggingHelper.DEFAULT_TRACE_LOG_LEVEL,
									CLASSNAME, METHODNAME, message);
						} else {
							String message = "Failed to fetch writer size on writer class : "
									+ writer.getClass();
							LOGGER.logp(LoggingHelper.DEFAULT_TRACE_LOG_LEVEL,
									CLASSNAME, METHODNAME, message);
						}
					}
					break;
				}
			}
		} catch (Exception ex) {
			LoggingHelper.logUnexpectedException(LOGGER, CLASSNAME, METHODNAME,
					ex);
		}

		if (entryExitTraceEnabled) {
			LOGGER.exiting(CLASSNAME, METHODNAME, writer);
		}
		return writer;
	}

}