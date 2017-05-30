package com.ibm.service.detailed;

import com.ibm.commerce.cache.ILogMetricGatherer;
import com.ibm.commerce.cache.LogMetricGathererManager;
import com.ibm.commerce.cache.Markers;

/**
 * 
 * MongoLogger
 */
public class MongoLogger {
	/**
	 * IBM Copyright notice field.
	 */
	public static final String COPYRIGHT = com.ibm.commerce.copyright.IBMCopyright.SHORT_COPYRIGHT;
	
	
    public static final ILogMetricGatherer GATHERER = LogMetricGathererManager.getLogMetricGatherer(MongoLogger.class);
    	
	/**
	 * 
	 * isLoggable
	 * @return is loggable
	 */
	public static boolean isLoggable() {
		return GATHERER.isEnabled();
	}

	   /**
     * 
     * isResultSetSizeMeasured
     * @return isResultSetSizeMeasured
     */
    public static boolean isResultSetSizeMeasured() {
        return GATHERER.isEnabled(Markers.RESULT_SIZE_MARKER);
    }

    public static boolean isRequestSizeMeasured()
    {
        return GATHERER.isEnabled(Markers.REQUEST_SIZE_MARKER);
    }
    
}
