/*
 * #%L
 * IBM 10x
 * 
 * IBM Confidential
 * OCO Source Materials
 * %%
 * Copyright (C) 2013 - 2015 IBM Corp.
 * %%
 * The source code for this program is not published or otherwise divested of
 * its trade secrets, irrespective of what has been deposited with the U.S.
 * Copyright Office.  IBM and the IBM logo are trademarks of IBM Corporation
 * in the United States other countries, or both.  Java and all Java-based
 * trademarks and logos are trademarks or registered trademarks of Oracle
 * and/or its affiliates. Other company, product or service names may be
 * trademarks or service marks of others.
 * #L%
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
