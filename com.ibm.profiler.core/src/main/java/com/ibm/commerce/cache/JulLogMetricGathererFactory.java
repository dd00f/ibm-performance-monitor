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

import java.util.logging.Logger;

/**
 * JulLogMetricGathererFactory
 * 
 * @author Steve McDuff
 */
public class JulLogMetricGathererFactory implements LogMetricGathererFactory
{
    public JulLogMetricGathererFactory()
    {
        super();
    }

    @Override
    public ILogMetricGatherer createLogMetricGatherer(String loggerName)
    {
        return new LogMetricGatherer(Logger.getLogger(loggerName));
    }
}
