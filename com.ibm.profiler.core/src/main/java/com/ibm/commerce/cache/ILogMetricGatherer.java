/*
 */
package com.ibm.commerce.cache;

/**
 * ILogMetricGatherer
 * 
 * @author Steve McDuff
 */
public interface ILogMetricGatherer extends MetricGatherer
{

    void gatherMetricEntryLog(OperationMetric metric);

}
