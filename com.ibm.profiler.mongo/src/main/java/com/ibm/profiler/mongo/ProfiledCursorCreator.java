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
package com.ibm.profiler.mongo;

import java.util.List;

import com.ibm.commerce.cache.OperationMetric;

/**
 * ProfiledCursorCreator
 * 
 * @author Steve McDuff
 */
public interface ProfiledCursorCreator
{
    
    public void stopMetric(OperationMetric metric, int resultSize);
    
    public OperationMetric startMetric( String operationName, List<String> keyValuePairs);

}
