package com.ibm.profiler.servlet;

/*
 *-----------------------------------------------------------------
 * IBM Confidential
 *
 * OCO Source Materials
 *
 * WebSphere Commerce
 *
 * (C) Copyright IBM Corp. 2003, 2015
 *
 * The source code for this program is not published or otherwise
 * divested of its trade secrets, irrespective of what has
 * been deposited with the U.S. Copyright Office.
 *-----------------------------------------------------------------
 */

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
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
import javax.servlet.http.HttpServletResponseWrapper;

import com.ibm.commerce.cache.LogMetricGatherer;
import com.ibm.commerce.cache.LoggingHelper;
import com.ibm.commerce.cache.MetricGatherer;
import com.ibm.commerce.cache.OperationMetric;

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
	 * The http request action servlet path attribute name
	 */
	private static final String COM_IBM_WEBSPHERE_SERVLET_URI_NON_DECODED = "com.ibm.websphere.servlet.uri_non_decoded";

	/**
	 * The FORWARD action servlet path attribute name
	 */
	private static final String JAVAX_SERVLET_FORWARD_PATH_INFO = "javax.servlet.forward.path_info";

	/**
	 * The level at which we log the servlet entry and exit.
	 */
	private static final Level SERVLET_PROFILING_LOG_LEVEL = Level.FINE;

	/**
	 * IBM copyright notice field.
	 */
	public static final String COPYRIGHT = com.ibm.commerce.copyright.IBMCopyright.SHORT_COPYRIGHT;

	/**
	 * The class logger.
	 */
	private static final Logger LOGGER = LoggingHelper.getLogger(CLASSNAME);

	private Logger serviceLogger = LoggingHelper
			.getLogger("com.ibm.service.entry.servlet");

	/**
	 * 
	 * @return The service logger in use.
	 */
	public Logger getServiceLogger() {
		return serviceLogger;
	}

	/**
	 * 
	 * @param serviceLogger
	 *            The service logger to use.
	 */
	public void setServiceLogger(Logger serviceLogger) {
		this.serviceLogger = serviceLogger;
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

		OperationMetric metric = null;
		int startSize = 0;
		boolean measurementEnabled = isMeasurementEnabledForRequest(servletRequest);
		boolean sizeMeasurementEnabled = isResponseSizeMeasurementEnabled();

		if (measurementEnabled) {
			metric = initializeMetric(servletRequest);
			if (sizeMeasurementEnabled) {
				startSize = getCurrentResponseSize(servletResponse);
			}
		}

		filterChain.doFilter(servletRequest, servletResponse);

		if (measurementEnabled) {
			logMetric(servletRequest, servletResponse, metric, startSize,
					sizeMeasurementEnabled, serviceLogger);
		}

		if (entryExitTraceEnabled) {
			LOGGER.exiting(CLASSNAME, METHODNAME);
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
	 * @param gatherer
	 *            The metric gatherer.
	 */
	public static void logMetric(ServletRequest servletRequest,
			ServletResponse servletResponse, OperationMetric metric,
			int startSize, boolean sizeMeasurementEnabled, Logger logGatherer) {
		final String METHODNAME = "logMetric(ServletRequest servletRequest,"
				+ "ServletResponse servletResponse, OperationMetric metric,int startSize, "
				+ "boolean sizeMeasurementEnabled, MetricGatherer gatherer)";
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
					fragmentSize = fetchResponseSizeFromCachedData(servletResponse);
				}
			}
			boolean wasCacheHit = false;

			wasCacheHit = updateCacheEnabledOnMetric(servletResponse, metric);

			metric.stopOperation(fragmentSize, wasCacheHit);

			LogMetricGatherer.logMetricToLogger(metric, logGatherer,
					SERVLET_PROFILING_LOG_LEVEL);
		} catch (Throwable ex) {
			LoggingHelper.logUnexpectedException(LOGGER, CLASSNAME, METHODNAME,
					ex);
		}

		if (entryExitTraceEnabled) {
			LOGGER.exiting(CLASSNAME, METHODNAME);
		}
	}

	private static boolean updateCacheEnabledOnMetric(
			ServletResponse servletResponse, OperationMetric metric) {

		final String METHODNAME = "updateCacheEnabledOnMetric(ServletResponse servletResponse, OperationMetric metric, final boolean traceEnabled)";
		final boolean entryExitTraceEnabled = LoggingHelper
				.isEntryExitTraceEnabled(LOGGER);
		final boolean traceEnabled = LoggingHelper.isTraceEnabled(LOGGER);
		if (entryExitTraceEnabled) {
			Object[] parameters = new Object[] { servletResponse, metric };
			LOGGER.entering(CLASSNAME, METHODNAME, parameters);
		}

		boolean wasCacheHit = false;
		boolean wasCacheEnabled = false;
		CacheProxyResponse cachedResponse = fetchCacheProxyResponse(servletResponse);

		if (cachedResponse != null) {
			FragmentComposer fragmentComposer = getFragmentComposer(cachedResponse);

			int cacheType = fragmentComposer.getCacheType();
			if (cacheType == FragmentComposer.WAS_CACHED) {
				wasCacheEnabled = true;
				wasCacheHit = true;
				if (traceEnabled) {
					String message = "CacheProxyResponse getCacheType WAS_CACHED";
					LOGGER.logp(LoggingHelper.DEFAULT_TRACE_LOG_LEVEL,
							CLASSNAME, METHODNAME, message);
				}
			} else if (cacheType == FragmentComposer.POPULATED_CACHE) {
				wasCacheEnabled = true;
				if (traceEnabled) {
					String message = "CacheProxyResponse getCacheType POPULATED_CACHE";
					LOGGER.logp(LoggingHelper.DEFAULT_TRACE_LOG_LEVEL,
							CLASSNAME, METHODNAME, message);
				}
			} else {
				if (traceEnabled) {
					String message = "CacheProxyResponse getCacheType NOT_CACHED";
					LOGGER.logp(LoggingHelper.DEFAULT_TRACE_LOG_LEVEL,
							CLASSNAME, METHODNAME, message);
				}
			}
		} else {
			if (traceEnabled) {
				String message = "response class not anticipated, was : "
						+ servletResponse.getClass().getName();
				LOGGER.logp(LoggingHelper.DEFAULT_TRACE_LOG_LEVEL, CLASSNAME,
						METHODNAME, message);
			}
		}

		metric.setOperationCacheEnabled(wasCacheEnabled);
		return wasCacheHit;
	}

	/**
	 * Fetch the fragment composer for a response.
	 * 
	 * @param cachedResponse
	 *            The cached response.
	 * @return The Matching fragment composer.
	 */
	private static FragmentComposer getFragmentComposer(
			CacheProxyResponse cachedResponse) {
		final String METHODNAME = "getFragmentComposer(CacheProxyResponse cachedResponse";
		final boolean entryExitTraceEnabled = LoggingHelper
				.isEntryExitTraceEnabled(LOGGER);
		final boolean traceEnabled = LoggingHelper.isTraceEnabled(LOGGER);
		if (entryExitTraceEnabled) {
			Object[] parameters = new Object[] { cachedResponse };
			LOGGER.entering(CLASSNAME, METHODNAME, parameters);
		}

		CacheProxyResponse c = cachedResponse;
		FragmentComposer fragmentComposer = c.getFragmentComposer();
		try {
			Field outField = fragmentComposer.getClass().getDeclaredField(
					"contentVector");
			outField.setAccessible(true);
			List contentList = (List) outField.get(fragmentComposer);
			Object object = contentList.get(contentList.size() - 1);
			if (object instanceof FragmentComposer) {
				fragmentComposer = (FragmentComposer) object;
				if (traceEnabled) {
					String message = "Successfully unwrapped the Fragment Composer.";
					LOGGER.logp(LoggingHelper.DEFAULT_TRACE_LOG_LEVEL,
							CLASSNAME, METHODNAME, message);
				}
			}
		} catch (Exception ex) {
			LoggingHelper.logUnexpectedException(LOGGER, CLASSNAME, METHODNAME,
					ex);
		}

		if (entryExitTraceEnabled) {
			LOGGER.exiting(CLASSNAME, METHODNAME, fragmentComposer);
		}
		return fragmentComposer;
	}

	/**
	 * Attempt to fetch the response size from the cached data.
	 * 
	 * @param servletResponse
	 *            The servlet response.
	 * @return The response size.
	 */
	private static int fetchResponseSizeFromCachedData(
			ServletResponse servletResponse) {

		final String METHODNAME = "fetchResponseSizeFromCachedData(ServletResponse servletResponse)";
		final boolean entryExitTraceEnabled = LoggingHelper
				.isEntryExitTraceEnabled(LOGGER);
		final boolean traceEnabled = LoggingHelper.isTraceEnabled(LOGGER);
		if (entryExitTraceEnabled) {
			Object[] parameters = new Object[] { servletResponse };
			LOGGER.entering(CLASSNAME, METHODNAME, parameters);
		}

		ServletResponse startResponse = servletResponse;
		CacheProxyResponse fetchCacheProxyResponse = fetchCacheProxyResponse(servletResponse);
		int returnValue = 1; // default
		try {
			Writer writer = null;

			if (fetchCacheProxyResponse != null) {
				startResponse = fetchCacheProxyResponse;
				writer = startResponse.getWriter();
				while (true) {
					if (writer instanceof CacheProxyWriter) {
						CacheProxyWriter cpw = (CacheProxyWriter) writer;
						char[] cachedData = cpw.getCachedData();
						if (cachedData != null && cachedData.length != 0) {
							returnValue = cachedData.length;

							if (traceEnabled) {
								String message = "Fetched cache data size of : "
										+ returnValue;
								LOGGER.logp(
										LoggingHelper.DEFAULT_TRACE_LOG_LEVEL,
										CLASSNAME, METHODNAME, message);
							}
							break;
						}

						writer = cpw.getWriter();
					} else if (writer instanceof PrintWriter) {

						if (traceEnabled) {
							String message = "Extracting writer from PrintWriter : "
									+ writer;
							LOGGER.logp(LoggingHelper.DEFAULT_TRACE_LOG_LEVEL,
									CLASSNAME, METHODNAME, message);
						}

						PrintWriter pw = (PrintWriter) writer;
						Field outField = pw.getClass().getDeclaredField("out");
						outField.setAccessible(true);
						writer = (Writer) outField.get(pw);
					} else {
						// unable to dig through.
						if (traceEnabled) {
							if (writer == null) {
								String message = "Failed to fetch writer cache data size on null writer.";
								LOGGER.logp(
										LoggingHelper.DEFAULT_TRACE_LOG_LEVEL,
										CLASSNAME, METHODNAME, message);
							} else {
								String message = "Failed to fetch writer cache data size on writer class : "
										+ writer.getClass();
								LOGGER.logp(
										LoggingHelper.DEFAULT_TRACE_LOG_LEVEL,
										CLASSNAME, METHODNAME, message);
							}
						}
						break;
					}
				}
			}

		} catch (Throwable ex) {
			LoggingHelper.logUnexpectedException(LOGGER, CLASSNAME, METHODNAME,
					ex);
		}

		if (entryExitTraceEnabled) {
			LOGGER.exiting(CLASSNAME, METHODNAME, returnValue);
		}

		return returnValue;
	}

	/**
	 * Initialize a metric object.
	 * 
	 * @param servletRequest
	 *            The servlet request.
	 * @return The operation metric.
	 */
	public static OperationMetric initializeMetric(ServletRequest servletRequest) {

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
			String[] orderedKeyValues = getOrderedParameterKeyValueArray(servletRequest);
			String currentUrl = "Servlet : "
					+ getRequestDescription(servletRequest);
			metric.startOperation(currentUrl, false, orderedKeyValues);

			InternalServletRequestLogger.METRIC_GATHERER
					.gatherMetricEntryLog(metric);
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
	 * @return the unique key-value string array.
	 */
	public static String[] getOrderedParameterKeyValueArray(
			ServletRequest request) {
		final String METHODNAME = "getOrderedParameterKeyValueArray(ServletRequest request)";
		final boolean entryExitTraceEnabled = LoggingHelper
				.isEntryExitTraceEnabled(LOGGER);
		// final boolean traceEnabled = LoggingHelper.isTraceEnabled(LOGGER);
		if (entryExitTraceEnabled) {
			Object[] parameters = new Object[] { request };
			LOGGER.entering(CLASSNAME, METHODNAME, parameters);
		}

		SortedMap<String, Object> keyValueSortedMap = new TreeMap<String, Object>();

		Map<String, Object> parameterMap = request.getParameterMap();

		keyValueSortedMap.putAll(parameterMap);

		int keyValueCount = keyValueSortedMap.size();
		String[] orderedParametersKeyValues = new String[keyValueCount * 2];

		Set<Entry<String, Object>> entrySet = keyValueSortedMap.entrySet();
		int i = 0;
		for (Entry<String, Object> entry : entrySet) {
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
	 * 
	 * @return true if response size measurement is enabled
	 */
	public static boolean isResponseSizeMeasurementEnabled() {
		return InternalServletRequestLogger.METRIC_GATHERER.getLogger()
				.isLoggable(
						InternalServletRequestLogger.MEASURE_RESULT_SIZE_LEVEL);
	}

	/**
	 * Determine if a request will be measured. This is determined based on the
	 * level of the performance logger and characteristics of the request.
	 * 
	 * @param servletRequest
	 *            The servlet request.
	 * @return True if the request is measured.
	 */
	public static boolean isMeasurementEnabledForRequest(
			ServletRequest servletRequest) {
		return InternalServletRequestLogger.METRIC_GATHERER.isLoggable()
				&& isRequestMeasureable(servletRequest);
	}

	/**
	 * Get the response cache behavior string description.
	 * 
	 * @param servletResponse
	 *            the servlet response
	 * @return A string description of the caching behavior between
	 */
	private String getResponseCacheBehavior(ServletResponse servletResponse) {
		final String METHODNAME = "getResponseCacheBehavior(ServletResponse servletResponse)";
		boolean entryExitTraceEnabled = LoggingHelper
				.isEntryExitTraceEnabled(LOGGER);
		boolean traceEnabled = LoggingHelper.isTraceEnabled(LOGGER);
		if (entryExitTraceEnabled) {
			Object[] parameters = new Object[] { servletResponse };
			LOGGER.entering(CLASSNAME, METHODNAME, parameters);
		}

		String cacheBehavior = "disabled";
		try {

			CacheProxyResponse cachedResponse = fetchCacheProxyResponse(servletResponse);

			if (cachedResponse != null) {
				FragmentComposer fragmentComposer = getFragmentComposer(cachedResponse);
				int cacheType = fragmentComposer.getCacheType();
				if (cacheType == FragmentComposer.WAS_CACHED) {
					cacheBehavior = "hit";
					if (traceEnabled) {
						String message = "CacheProxyResponse getCacheType WAS_CACHED";
						LOGGER.logp(LoggingHelper.DEFAULT_TRACE_LOG_LEVEL,
								CLASSNAME, METHODNAME, message);
					}
				} else if (cacheType == FragmentComposer.POPULATED_CACHE) {
					cacheBehavior = "miss";
					if (traceEnabled) {
						String message = "CacheProxyResponse getCacheType POPULATED_CACHE";
						LOGGER.logp(LoggingHelper.DEFAULT_TRACE_LOG_LEVEL,
								CLASSNAME, METHODNAME, message);
					}
				} else if (cacheType == FragmentComposer.NOT_CACHED) {
					cacheBehavior = "no";
					if (traceEnabled) {
						String message = "CacheProxyResponse getCacheType NOT_CACHED";
						LOGGER.logp(LoggingHelper.DEFAULT_TRACE_LOG_LEVEL,
								CLASSNAME, METHODNAME, message);
					}
				} else {
					cacheBehavior = "unknown" + cacheType;
					if (traceEnabled) {
						String message = "CacheProxyResponse getCacheType unknown response of : "
								+ cacheType;
						LOGGER.logp(LoggingHelper.DEFAULT_TRACE_LOG_LEVEL,
								CLASSNAME, METHODNAME, message);
					}
				}
			} else {
				if (traceEnabled) {
					String message = "No CacheProxyResponse wrapper was found in the response of type : "
							+ servletResponse.getClass().getName();
					LOGGER.logp(LoggingHelper.DEFAULT_TRACE_LOG_LEVEL,
							CLASSNAME, METHODNAME, message);
				}
			}

		} catch (Exception ex) {
			LoggingHelper.logUnexpectedException(LOGGER, CLASSNAME, METHODNAME,
					ex);
		}

		if (entryExitTraceEnabled) {
			LOGGER.exiting(CLASSNAME, METHODNAME, cacheBehavior);
		}
		return cacheBehavior;
	}

	/**
	 * Fetch the response wrapper of type CacheProxyResponse if one can be
	 * found.
	 * 
	 * @param servletResponse
	 *            the current servlet response.
	 * @return the response wrapper of type CacheProxyResponse if one can be
	 *         found.
	 */
	private static CacheProxyResponse fetchCacheProxyResponse(
			ServletResponse servletResponse) {
		CacheProxyResponse cachedResponse = null;
		ServletResponse findResponse = servletResponse;

		while (findResponse instanceof HttpServletResponseWrapper) {
			if (findResponse instanceof CacheProxyResponse) {
				cachedResponse = (CacheProxyResponse) findResponse;
				break;
			}
			HttpServletResponseWrapper wrapper = (HttpServletResponseWrapper) findResponse;
			findResponse = wrapper.getResponse();
		}
		return cachedResponse;
	}

	/**
	 * The list of blocked request extensions from the logger.
	 */
	private static final List<String> BLOCKED_EXTENSIONS = new ArrayList<String>();
	static {
		initializeBlockedExtensions();
	}

	/**
	 * Get the list of request extensions that won't be logged.
	 * 
	 * @return the list of request extensions that won't be logged.
	 */
	public static List<String> getBlockedExtensions() {
		return BLOCKED_EXTENSIONS;
	}

	/**
	 * Initialize the list of request extensions that won't be logged.
	 */
	private static void initializeBlockedExtensions() {
		// List<String> blocked = BLOCKED_EXTENSIONS;
		// blocked.add(".png");
		// blocked.add(".css");
		// blocked.add(".js");
		// blocked.add(".jpg");
		// blocked.add(".gif");
		// blocked.add(".bmp");
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
		boolean traceEnabled = LoggingHelper.isTraceEnabled(LOGGER);
		if (entryExitTraceEnabled) {
			Object[] parameters = new Object[] { servletRequest };
			LOGGER.entering(CLASSNAME, METHODNAME, parameters);
		}

		String returnValue = null;

		// priority 1 : JSP servlet
		Object servletPathValue = servletRequest
				.getAttribute(JAVAX_SERVLET_INCLUDE_SERVLET_PATH);

		if (servletPathValue instanceof String) {
			String servletPath = (String) servletPathValue;
			if (servletPath.endsWith(".jsp") || servletPath.endsWith(".jspf")) {
				returnValue = "Include : " + servletPath;
				if (traceEnabled) {
					String message = "Consumed Servlet include request to : "
							+ servletPath;
					LOGGER.logp(LoggingHelper.DEFAULT_TRACE_LOG_LEVEL,
							CLASSNAME, METHODNAME, message);
				}
			} else {
				if (traceEnabled) {
					String message = "Ignored Servlet include request to : "
							+ servletPath;
					LOGGER.logp(LoggingHelper.DEFAULT_TRACE_LOG_LEVEL,
							CLASSNAME, METHODNAME, message);
				}
			}

			if (entryExitTraceEnabled) {
				LOGGER.exiting(CLASSNAME, METHODNAME, returnValue);
			}
			return returnValue;
		}

		// priority 2 : forward path
		Object forwardPathValue = servletRequest
				.getAttribute(JAVAX_SERVLET_FORWARD_PATH_INFO);

		if (forwardPathValue instanceof String) {
			String forwardPath = (String) forwardPathValue;

			returnValue = "Forward : " + forwardPath;
			if (traceEnabled) {
				String message = "Consumed Servlet forward path request to : "
						+ forwardPath;
				LOGGER.logp(LoggingHelper.DEFAULT_TRACE_LOG_LEVEL, CLASSNAME,
						METHODNAME, message);
			}

			if (entryExitTraceEnabled) {
				LOGGER.exiting(CLASSNAME, METHODNAME, returnValue);
			}
			return returnValue;
		}

		// priority 3 : raw request URL
		Object requestUriValue = servletRequest
				.getAttribute(COM_IBM_WEBSPHERE_SERVLET_URI_NON_DECODED);
		if (requestUriValue instanceof String) {
			String uri = (String) requestUriValue;
			if (traceEnabled) {
				String message = "Consumed Servlet request to : " + uri;
				LOGGER.logp(LoggingHelper.DEFAULT_TRACE_LOG_LEVEL, CLASSNAME,
						METHODNAME, message);
			}
			returnValue = "Request : " + uri;
		} else {
			if (traceEnabled) {
				String message = "Ignored Unidentified Servlet Request.";
				LOGGER.logp(LoggingHelper.DEFAULT_TRACE_LOG_LEVEL, CLASSNAME,
						METHODNAME, message);
			}
		}

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
	 * Is service logging enabled.
	 * 
	 * @return True if service logging is enabled.
	 */
	public static boolean isServiceLoggingEnabled() {
		return SERVICELOGGER.isLoggable(LoggingHelper.ENTRY_EXIT_LOG_LEVEL);
	}

	/**
	 * @see javax.servlet.Filter#destroy()
	 */
	public void destroy() {

	}

	/**
	 * Test to see if a request can be measured.
	 * 
	 * @param servletRequest
	 *            The request.
	 * @return True if the request can be measured.
	 */
	private static boolean isRequestMeasureable(ServletRequest servletRequest) {

		final String METHODNAME = "isRequestMeasureable(ServletRequest servletRequest)";
		boolean entryExitTraceEnabled = LoggingHelper
				.isEntryExitTraceEnabled(LOGGER);
		boolean traceEnabled = LoggingHelper.isTraceEnabled(LOGGER);
		if (entryExitTraceEnabled) {
			Object[] parameters = new Object[] { servletRequest };
			LOGGER.entering(CLASSNAME, METHODNAME, parameters);
		}

		// priority 1 : JSP servlet
		Object servletPathValue = servletRequest
				.getAttribute("javax.servlet.include.servlet_path");

		if (servletPathValue instanceof String) {
			boolean returnValue = false;
			String servletPath = (String) servletPathValue;
			if (servletPath.endsWith(".jsp") || servletPath.endsWith(".jspf")) {
				if (traceEnabled) {
					String message = "Consumed Servlet include request to : "
							+ servletPath;
					LOGGER.logp(LoggingHelper.DEFAULT_TRACE_LOG_LEVEL,
							CLASSNAME, METHODNAME, message);
				}

				returnValue = true;
			} else {
				if (traceEnabled) {
					String message = "Ignored Servlet include request to : "
							+ servletPath;
					LOGGER.logp(LoggingHelper.DEFAULT_TRACE_LOG_LEVEL,
							CLASSNAME, METHODNAME, message);
				}
			}

			if (entryExitTraceEnabled) {
				LOGGER.exiting(CLASSNAME, METHODNAME, returnValue);
			}
			return returnValue;
		}

		// priority 2 : forward path
		Object forwardPathValue = servletRequest
				.getAttribute("javax.servlet.forward.path_info");

		if (forwardPathValue instanceof String) {
			String forwardPath = (String) forwardPathValue;

			if (traceEnabled) {
				String message = "Consumed Servlet forward path request to : "
						+ forwardPath;
				LOGGER.logp(LoggingHelper.DEFAULT_TRACE_LOG_LEVEL, CLASSNAME,
						METHODNAME, message);
			}

			boolean returnValue = true;
			if (entryExitTraceEnabled) {
				LOGGER.exiting(CLASSNAME, METHODNAME, returnValue);
			}
			return returnValue;
		}

		// priority 3 : raw request URL
		Object requestUriValue = servletRequest
				.getAttribute("com.ibm.websphere.servlet.uri_non_decoded");
		if (requestUriValue instanceof String) {
			String uri = (String) requestUriValue;
			if (traceEnabled) {
				String message = "Consumed Servlet request to : " + uri;
				LOGGER.logp(LoggingHelper.DEFAULT_TRACE_LOG_LEVEL, CLASSNAME,
						METHODNAME, message);
			}
			boolean returnValue = true;
			if (entryExitTraceEnabled) {
				LOGGER.exiting(CLASSNAME, METHODNAME, returnValue);
			}
			return returnValue;
		}

		if (traceEnabled) {
			String message = "Ignored Unidentified Servlet Request.";
			LOGGER.logp(LoggingHelper.DEFAULT_TRACE_LOG_LEVEL, CLASSNAME,
					METHODNAME, message);
		}

		boolean returnValue = false;
		if (entryExitTraceEnabled) {
			LOGGER.exiting(CLASSNAME, METHODNAME, returnValue);
		}
		return returnValue;
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
				if (responseWriter instanceof BufferedWriter) {
					BufferedWriter bw = (BufferedWriter) responseWriter;
					returnValue = bw.getTotal();
					if (traceEnabled) {
						String message = "Found a BufferedWriter that could be used. Result : "
								+ returnValue;
						LOGGER.logp(LoggingHelper.DEFAULT_TRACE_LOG_LEVEL,
								CLASSNAME, METHODNAME, message);
					}

					break;
				}

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
		} catch (Throwable ex) {
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
				if (writer instanceof CacheProxyWriter) {
					CacheProxyWriter cpw = (CacheProxyWriter) writer;
					writer = cpw.getWriter();
					if (traceEnabled) {
						String message = "Fetched writer from CacheProxyWriter.";
						LOGGER.logp(LoggingHelper.DEFAULT_TRACE_LOG_LEVEL,
								CLASSNAME, METHODNAME, message);
					}
				} else if (writer instanceof PrintWriter) {
					PrintWriter pw = (PrintWriter) writer;
					Field outField = pw.getClass().getDeclaredField("out");
					outField.setAccessible(true);
					writer = (Writer) outField.get(pw);
					if (traceEnabled) {

						String message = "Fetched writer from PrintWriter private member : out.";
						LOGGER.logp(LoggingHelper.DEFAULT_TRACE_LOG_LEVEL,
								CLASSNAME, METHODNAME, message);
					}
				} else if (writer instanceof BufferedWriter) {
					if (traceEnabled) {

						String message = "Found BufferedWriter, returning.";
						LOGGER.logp(LoggingHelper.DEFAULT_TRACE_LOG_LEVEL,
								CLASSNAME, METHODNAME, message);
					}
					break;
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
		} catch (Throwable ex) {
			LoggingHelper.logUnexpectedException(LOGGER, CLASSNAME, METHODNAME,
					ex);
		}

		if (entryExitTraceEnabled) {
			LOGGER.exiting(CLASSNAME, METHODNAME, writer);
		}
		return writer;
	}

}