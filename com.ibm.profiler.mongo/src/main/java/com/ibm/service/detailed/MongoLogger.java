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
package com.ibm.service.detailed;

import com.ibm.commerce.cache.ILogMetricGatherer;
import com.ibm.commerce.cache.LogMetricGathererManager;
import com.ibm.commerce.cache.Markers;

/**
 * 
 * MongoLogger
 */
public class MongoLogger {

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
