/*
 */
package com.ibm.commerce.cache;


/**
 * LogMetricGathererManager
 * 
 * @author Steve McDuff
 */
public class LogMetricGathererManager
{
    public static final String GATHERER_FACTORY_CLASS_NAME_PROPERTY = "com.ibm.profiler.gathererFactoryClassName";

    private static final LogMetricGathererFactory FACTORY;

    static
    {
        String gathererClass = System.getProperty(GATHERER_FACTORY_CLASS_NAME_PROPERTY);
        LogMetricGathererFactory factory = null;

        try
        {
            factory = (LogMetricGathererFactory) Class.forName(gathererClass).newInstance();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }

        if (factory == null)
        {
            factory = new JulLogMetricGathererFactory();
        }
        FACTORY = factory;
    }

    private LogMetricGathererManager()
    {
        super();
    }

    /**
     * Get a log metric gatherer
     * 
     * @param loggerName
     *            the logger name
     * @return The created metric gatherer
     */
    public static ILogMetricGatherer getLogMetricGatherer(String loggerName)
    {
        return FACTORY.createLogMetricGatherer(loggerName);
    }

    public static ILogMetricGatherer getLogMetricGatherer(Class<?> class1)
    {
        return getLogMetricGatherer(class1.getName());
    }
}
