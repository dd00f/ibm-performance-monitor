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

/**
 * CommonMetricProperties
 * 
 * @author Steve McDuff
 */
public class CommonMetricProperties
{
    /**
     * Number of objects returned in the response.
     */
    public static final String RESPONSE_OBJECT_COUNT = "responseObjectCount";

    /**
     * Amount of time spent reading from a remote data source.
     */
    public static final String READ_TIME_NS = "readTimeNs";

    /**
     * Maximum amount of time spent reading a single entry from a remote data source.
     */
    public static final String MAXIMUM_READ_DURATION_NS = "maximumReadDurationNs";

    /**
     * Amount of time spent before the operation successfully read the first piece of data from a remote data source.
     */
    public static final String FIRST_READ_DURATION_NS = "firstReadDurationNs";

    /**
     * Maximum size of a single object in the response.
     */
    public static final String MAXIMUM_RESPONSE_OBJECT_SIZE_BYTES = "maximumResponseObjectSizeBytes";

    /**
     * Number of objects written in a query.
     */
    public static final String REQUEST_OBJECT_COUNT = "requestObjectCount";

    /**
     * size of the request in bytes.
     */
    public static final String REQUEST_SIZE_BYTES = "requestSizeBytes";

    private CommonMetricProperties()
    {
        super();
    }

}
