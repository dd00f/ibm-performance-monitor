package com.ibm.commerce.cache;

/*
 *-----------------------------------------------------------------
 * IBM Confidential
 *
 * OCO Source Materials
 *
 * WebSphere Commerce
 *
 * (C) Copyright IBM Corp. 2012, 2014
 *
 * The source code for this program is not published or otherwise
 * divested of its trade secrets, irrespective of what has
 * been deposited with the U.S. Copyright Office.
 *-----------------------------------------------------------------
 */

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Operation metric gathering structure.
 * <p>
 * The {@link #startOperation(String, boolean, String...)} and
 * {@link #stopOperation(int, boolean)} methods are offered as a shortcut to
 * fill common values entered during metric gathering.
 */
public class OperationMetric implements IOperationMetric
{

    private static final String DOUBLE_QUOTE = "\"";

    private static final String EQUAL_OPEN_QUOTE = "=\"";

    private static final String SPACE = " ";

    public static final String FIELD_OPERATION = "operation";

    public static final String FIELD_SUCCESSFUL = "successful";

    public static final String FIELD_CACHE_ENABLED = "cacheEnabled";

    public static final String FIELD_CACHE_HIT = "cacheHit";

    public static final String FIELD_RESULT_SIZE = "resultSize";

    public static final String FIELD_DURATION = "duration";

    public static final String FIELD_STOP_TIME = "stopTime";

    public static final String FIELD_START_TIME = "startTime";

    public static final String FIELD_PARENT_ID = "parentId";

    public static final String FIELD_ID = "id";

    public static final String FIELD_PARAMETERS = "parameters";

    public static final String FIELD_DURATION_MS = "durationMs";

    private static final Map<String, String> SANITIZED_KEY_VALUES;

    static
    {
        Map<String, String> sanitizedValues = new HashMap<String, String>();

        putSanitizedFieldValue(sanitizedValues, FIELD_OPERATION);
        putSanitizedFieldValue(sanitizedValues, FIELD_SUCCESSFUL);
        putSanitizedFieldValue(sanitizedValues, FIELD_CACHE_ENABLED);
        putSanitizedFieldValue(sanitizedValues, FIELD_CACHE_HIT);
        putSanitizedFieldValue(sanitizedValues, FIELD_RESULT_SIZE);
        putSanitizedFieldValue(sanitizedValues, FIELD_DURATION);
        putSanitizedFieldValue(sanitizedValues, FIELD_STOP_TIME);
        putSanitizedFieldValue(sanitizedValues, FIELD_START_TIME);
        putSanitizedFieldValue(sanitizedValues, FIELD_PARENT_ID);
        putSanitizedFieldValue(sanitizedValues, FIELD_ID);
        putSanitizedFieldValue(sanitizedValues, FIELD_PARAMETERS);
        putSanitizedFieldValue(sanitizedValues, FIELD_DURATION_MS);

        SANITIZED_KEY_VALUES = Collections.unmodifiableMap(sanitizedValues);
    }

    /**
     * IBM Copyright notice field.
     */
    public static final String COPYRIGHT = com.ibm.commerce.copyright.IBMCopyright.SHORT_COPYRIGHT;

    /**
     * Separator used to separate key and values in a list.
     */
    private static final String KEY_VALUE_SEPARATOR = ":";

    /**
     * Serialization separator of values.
     */
    private static final String SERIALIZATION_SEPARATOR = SPACE;

    /**
     * Class name.
     */
    private static final String CLASS_NAME = OperationMetric.class.getName();

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggingHelper.getLogger(CLASS_NAME);

    /**
     * Start time in nanoseconds
     */
    private long startTime = 0;

    /**
     * Stop time in nanoseconds
     */
    private long stopTime = 0;

    /**
     * operation duration in nanoseconds
     */
    private long duration = 0;

    /**
     * size of the result in cache
     */
    private int resultSize = 0;

    /**
     * flag indicating that the result was fetched from the cache.
     */
    private boolean isResultFetchedFromCache = false;

    /**
     * is the operation cache enabled
     */
    private boolean isOperationCacheEnabled = false;

    /**
     * is the operation successful
     */
    private boolean isSuccessful = true;

    /**
     * list of all the key and value pairs used to identify unique operations.
     * The list assumes that even indexes (0,2,4...) refer to key names and that
     * odd indexes (1,3,5...) refer to key values. If no parameters are
     * specified, the list remains null.
     */
    private List<String> keyValuePairList = null;

    /**
     * name of the operation.
     */
    private String operationName;

    /**
     * identification assignment value
     */
    // fill the first 32 bits with a random value to get a good enough
    // unique ID.
    private static final AtomicLong IDENTIFIER_ASSIGNER = new AtomicLong(Math.abs(System.nanoTime() << 32));

    /**
     * metric identifier
     */
    private long identifier = IDENTIFIER_ASSIGNER.incrementAndGet();

    /**
     * parent operation identifier
     */
    private long parentIdentifier = 0;

    private Map<String, String> properties = null;

    /**
     * Clean the properties. This can be invoked before print properties for the
     * exit logging.
     */
    @Override
    public void cleanProperties()
    {
        properties = null;
    }

    private static void putSanitizedFieldValue(Map<String, String> sanitizedValues, String fieldCacheEnabled)
    {
        sanitizedValues.put(fieldCacheEnabled, "_" + fieldCacheEnabled);
    }

    /**
     * set property using a set of set and value of each pair stored in
     * properties
     * 
     * @param key
     *            key part of property pair
     * @param value
     *            value part of property pair
     */
    @Override
    public void setProperty(String key, String value)
    {
        if (properties == null)
        {
            properties = new HashMap<String, String>();
        }
        key = sanitizeGenericPropertyKey(key);
        properties.put(key, value);
    }

    /**
     * Ensure that a property key doesn't overlap with a registered property.
     * Keys that require sanitization will have an underscore appended to their
     * value.
     * 
     * @param key
     *            the key to sanitize.
     * @return The sanitized key.
     */
    public static String sanitizeGenericPropertyKey(String key)
    {
        String sanitizedKey = SANITIZED_KEY_VALUES.get(key);
        if (sanitizedKey == null)
        {
            sanitizedKey = key;
        }
        return sanitizedKey;
    }

    /**
     * retrieve properties given any key value
     * 
     * @param key
     *            key value to specify which value to return
     * @return a mapping value for the key of the property is returned
     */
    @Override
    public String getProperty(String key)
    {
        if (properties == null)
        {
            return null;
        }
        return properties.get(key);
    }
    
    /**
     * Get the map of properties (modifiable)
     * @return The map of properties. May be null.
     */
    public Map<String, String> getProperties()
    {
        return properties;
    }

    /**
     * prints to property values to a writer
     * 
     * @param builder
     *            name of writer
     * @throws IOException
     *             if anything goes wrong in the writer
     */
    public void printProperty(Writer builder) throws IOException
    {
        printPropertiesToWriter(properties, builder);
    }

    /**
     * writes properties to a writer for logging
     * 
     * @param properties
     *            properties of the metric
     * @param builder
     *            builder to write into
     * @throws IOException
     *             if anything goes wrong in the writer
     */
    public static void printPropertiesToWriter(Map<String, String> properties, Writer builder) throws IOException
    {
        if (properties != null && properties.size() > 0)
        {
            Set<String> keyset = new TreeSet<String>(properties.keySet());
            for (String key : keyset)
            {
                String value = properties.get(key);
                builder.append(SPACE);
                CacheUtilities.printXmlEscapedStringToWriter(builder, key);
                builder.append(EQUAL_OPEN_QUOTE);
                CacheUtilities.printXmlEscapedStringToWriter(builder, value);
                builder.append(DOUBLE_QUOTE);
            }
        }
    }

    /**
     * Thread local variable used to keep track of the current parent
     * identifier. If the value is zero, it means that no parent operation is
     * currently identified.
     */
    private static final ThreadLocal<Long> PARENT_TRACKER = new ThreadLocal<Long>()
    {

        /*
         * (non-Javadoc)
         * 
         * @see java.lang.ThreadLocal#initialValue()
         */
        @Override
        protected Long initialValue()
        {
            return Long.valueOf(0);
        }
    };

    /**
     * @return the operation start time.
     */
    @Override
    public long getStartTime()
    {
        return startTime;
    }

    /**
     * @param startTime
     *            the operation start time.
     */
    @Override
    public void setStartTime(long startTime)
    {
        this.startTime = startTime;
    }

    /**
     * @return the operation stop time.
     */
    @Override
    public long getStopTime()
    {
        return stopTime;
    }

    /**
     * @param stopTime
     *            the operation stop time.
     */
    @Override
    public void setStopTime(long stopTime)
    {
        this.stopTime = stopTime;
    }

    /**
     * @return the operation duration
     */
    @Override
    public long getDuration()
    {
        return duration;
    }

    /**
     * @param duration
     *            the operation duration
     */
    @Override
    public void setDuration(long duration)
    {
        this.duration = duration;
    }

    /**
     * @return the result size
     */
    @Override
    public int getResultSize()
    {
        return resultSize;
    }

    /**
     * @param resultSize
     *            the result size
     */
    @Override
    public void setResultSize(int resultSize)
    {
        this.resultSize = resultSize;
    }

    /**
     * @return the key value pair list
     */
    @Override
    public List<String> getKeyValuePairList()
    {
        return keyValuePairList;
    }

    /**
     * @param keyValuePairList
     *            the key value pair list
     */
    @Override
    public void setKeyValuePairList(List<String> keyValuePairList)
    {
        this.keyValuePairList = keyValuePairList;
    }

    /**
     * @return the operation name
     */
    @Override
    public String getOperationName()
    {
        return operationName;
    }

    /**
     * @param operationName
     *            the operation name
     */
    @Override
    public void setOperationName(String operationName)
    {
        this.operationName = operationName;
    }

    /**
     * Is the operation successful.
     * 
     * @return true if the operation was successful.
     */
    @Override
    public boolean isSuccessful()
    {
        return isSuccessful;
    }

    /**
     * Set the successful flag.
     * 
     * @param isSuccessful
     *            the new successful flag.
     */
    @Override
    public void setSuccessful(boolean isSuccessful)
    {
        this.isSuccessful = isSuccessful;
    }

    /**
     * Start an operation, automatically capture the operation start time.
     * 
     * @param operationName
     *            name of the operation
     * @param isOperationCacheEnabled
     *            is the operation cache enabled
     * @param keyValuePairs
     *            the list of key value pairs
     */
    public void startOperation(String operationName, boolean isOperationCacheEnabled, String... keyValuePairs)
    {

        this.operationName = operationName;
        startTime = System.nanoTime();
        this.isOperationCacheEnabled = isOperationCacheEnabled;

        if (keyValuePairs != null)
        {
            keyValuePairList = Arrays.asList(keyValuePairs);
        }
        else
        {
            keyValuePairList = null;
        }

        parentIdentifier = getThreadParentOperationIdentifier();
        setThreadParentOperationIdentifier(identifier);
    }

    /**
     * check to see if an operation matches another one. To match, the operation
     * name and all the key value pairs must match.
     * 
     * @param metrics
     *            the other metric to match
     * @return true if the other metric matches.
     */
    @Override
    public boolean matches(IOperationMetric metrics)
    {
        if (metrics == null)
        {
            return false;
        }

        if (!CacheUtilities.equals(operationName, metrics.getOperationName()))
        {
            return false;
        }

        if (!CacheUtilities.equals(keyValuePairList, metrics.getKeyValuePairList()))
        {
            return false;
        }

        return true;
    }

    /**
     * Stop the operation and automatically capture the stop time and the
     * duration.
     * 
     * @param resultSize
     *            the size of the result.
     * @param isResultFetchedFromCache
     *            flag indicating if the result was fetched from a cache.
     * @param isSuccessful
     *            was the operation successful.
     */
    public void stopOperation(int resultSize, boolean isResultFetchedFromCache, boolean isSuccessful)
    {
        stopOperation(resultSize, isResultFetchedFromCache);
        this.isSuccessful = isSuccessful;
    }

    /**
     * Stop the operation and automatically capture the stop time and the
     * duration.
     * 
     * @param resultSize
     *            the size of the result.
     * @param isResultFetchedFromCache
     *            flag indicating if the result was fetched from a cache.
     */
    public void stopOperation(int resultSize, boolean isResultFetchedFromCache)
    {
        if (startTime == 0)
        {
            String message = "startOperation must be called before the stopOperation.";
            LOGGER.log(Level.WARNING, message);
        }

        stopTime = System.nanoTime();
        // while nanotime might roll over, this would take ~292 years, which is
        // more time
        // than we expect for a system to stay operational.
        duration = stopTime - startTime;
        this.isResultFetchedFromCache = isResultFetchedFromCache;
        this.resultSize = resultSize;

        setThreadParentOperationIdentifier(parentIdentifier);
    }

    /**
     * @return is the result fetched from cache
     */
    @Override
    public boolean isResultFetchedFromCache()
    {
        return isResultFetchedFromCache;
    }

    /**
     * @param isResultFetchedFromCache
     *            is the result fetched from cache
     */
    @Override
    public void setResultFetchedFromCache(boolean isResultFetchedFromCache)
    {
        this.isResultFetchedFromCache = isResultFetchedFromCache;
    }

    /**
     * @return is the operation cache enabled
     */
    @Override
    public boolean isOperationCacheEnabled()
    {
        return isOperationCacheEnabled;
    }

    /**
     * @param isOperationCacheEnabled
     *            is the operation cache enabled
     */
    @Override
    public void setOperationCacheEnabled(boolean isOperationCacheEnabled)
    {
        this.isOperationCacheEnabled = isOperationCacheEnabled;
    }

    /**
     * Fetch a unique key used to aggregate all the key value pairs in a key
     * that is easy to store and compare.
     * 
     * @return the unique key.
     */
    public String getUniqueKey()
    {
        StringBuilder builder = new StringBuilder();

        if (keyValuePairList != null)
        {
            for (String currentString : keyValuePairList)
            {

                builder.append(CacheUtilities.escapeString(currentString));
                builder.append(KEY_VALUE_SEPARATOR);
            }
        }

        return builder.toString();
    }

    /**
     * Serialize this object to a string.
     * 
     * @return the serialized version of this object.
     */
    public String toSerializedString()
    {
        StringBuilder builder = new StringBuilder();

        toSerializedString(builder);

        return builder.toString();
    }

    /**
     * Serialize this object to a string builder.
     * 
     * @param builder
     *            the string builder in which to serialize this object.
     */
    public void toSerializedString(StringBuilder builder)
    {
        builder.append(identifier);
        builder.append(SERIALIZATION_SEPARATOR);
        builder.append(parentIdentifier);
        builder.append(SERIALIZATION_SEPARATOR);
        builder.append(CacheUtilities.escapeString(operationName));
        builder.append(SERIALIZATION_SEPARATOR);
        builder.append(getUniqueKey());
        builder.append(SERIALIZATION_SEPARATOR);
        builder.append(startTime);
        builder.append(SERIALIZATION_SEPARATOR);
        builder.append(stopTime);
        builder.append(SERIALIZATION_SEPARATOR);
        builder.append(duration);
        builder.append(SERIALIZATION_SEPARATOR);
        builder.append(getDurationInMilliseconds());
        builder.append(SERIALIZATION_SEPARATOR);
        builder.append(resultSize);
        builder.append(SERIALIZATION_SEPARATOR);
        builder.append(isResultFetchedFromCache);
        builder.append(SERIALIZATION_SEPARATOR);
        builder.append(isOperationCacheEnabled);
        builder.append(SERIALIZATION_SEPARATOR);
        builder.append(isSuccessful);
    }

    private long getDurationInMilliseconds()
    {
        return duration / 1000000;
    }

    /**
     * Write the metric to a writer.
     * 
     * @param builder
     *            The writer to fill with metric data.
     * @throws IOException
     *             if anything goes wrong.
     */
    public void toSerializedString(Writer builder) throws IOException
    {
        builder.append(Long.toString(identifier));
        builder.append(SERIALIZATION_SEPARATOR);
        builder.append(Long.toString(parentIdentifier));
        builder.append(SERIALIZATION_SEPARATOR);

        CacheUtilities.escapeStringToWriter(builder, operationName);

        builder.append(SERIALIZATION_SEPARATOR);

        if (keyValuePairList != null)
        {
            for (String currentString : keyValuePairList)
            {

                CacheUtilities.escapeStringToWriter(builder, currentString);
                builder.append(KEY_VALUE_SEPARATOR);
            }
        }

        builder.append(SERIALIZATION_SEPARATOR);
        builder.append(Long.toString(startTime));
        builder.append(SERIALIZATION_SEPARATOR);
        builder.append(Long.toString(stopTime));
        builder.append(SERIALIZATION_SEPARATOR);
        builder.append(Long.toString(duration));
        builder.append(SERIALIZATION_SEPARATOR);
        builder.append(Long.toString(getDurationInMilliseconds()));
        builder.append(SERIALIZATION_SEPARATOR);
        builder.append(Integer.toString(resultSize));
        builder.append(SERIALIZATION_SEPARATOR);
        builder.append(Boolean.toString(isResultFetchedFromCache));
        builder.append(SERIALIZATION_SEPARATOR);
        builder.append(Boolean.toString(isOperationCacheEnabled));
        builder.append(SERIALIZATION_SEPARATOR);
        builder.append(Boolean.toString(isSuccessful));
    }

    /**
     * Write the metric to a writer in XML.
     * 
     * @param builder
     *            The writer to fill with metric data.
     * @throws IOException
     *             if anything goes wrong.
     */
    public void toSerializedXmlString(Writer builder) throws IOException
    {
        builder.append(SPACE);
        builder.append(FIELD_OPERATION);
        builder.append(EQUAL_OPEN_QUOTE);
        CacheUtilities.printXmlEscapedStringToWriter(builder, operationName);
        builder.append(DOUBLE_QUOTE);

        builder.append(SPACE);
        builder.append(FIELD_PARAMETERS);
        builder.append(EQUAL_OPEN_QUOTE);
        logKeyValuePairList(builder);
        builder.append(DOUBLE_QUOTE);

        builder.append(SPACE);
        builder.append(FIELD_ID);
        builder.append(EQUAL_OPEN_QUOTE);
        builder.append(Long.toString(identifier));
        builder.append(DOUBLE_QUOTE);

        builder.append(SPACE);
        builder.append(FIELD_PARENT_ID);
        builder.append(EQUAL_OPEN_QUOTE);
        builder.append(Long.toString(parentIdentifier));
        builder.append(DOUBLE_QUOTE);

        builder.append(SPACE);
        builder.append(FIELD_START_TIME);
        builder.append(EQUAL_OPEN_QUOTE);
        builder.append(Long.toString(startTime));
        builder.append(DOUBLE_QUOTE);

        builder.append(SPACE);
        builder.append(FIELD_STOP_TIME);
        builder.append(EQUAL_OPEN_QUOTE);
        builder.append(Long.toString(stopTime));
        builder.append(DOUBLE_QUOTE);

        builder.append(SPACE);
        builder.append(FIELD_DURATION);
        builder.append(EQUAL_OPEN_QUOTE);
        builder.append(Long.toString(duration));
        builder.append(DOUBLE_QUOTE);

        builder.append(SPACE);
        builder.append(FIELD_DURATION_MS);
        builder.append(EQUAL_OPEN_QUOTE);
        builder.append(Long.toString(getDurationInMilliseconds()));
        builder.append(DOUBLE_QUOTE);

        builder.append(SPACE);
        builder.append(FIELD_RESULT_SIZE);
        builder.append(EQUAL_OPEN_QUOTE);
        builder.append(Integer.toString(resultSize));
        builder.append(DOUBLE_QUOTE);

        builder.append(SPACE);
        builder.append(FIELD_CACHE_HIT);
        builder.append(EQUAL_OPEN_QUOTE);
        builder.append(Boolean.toString(isResultFetchedFromCache));
        builder.append(DOUBLE_QUOTE);

        builder.append(SPACE);
        builder.append(FIELD_CACHE_ENABLED);
        builder.append(EQUAL_OPEN_QUOTE);
        builder.append(Boolean.toString(isOperationCacheEnabled));
        builder.append(DOUBLE_QUOTE);

        builder.append(SPACE);
        builder.append(FIELD_SUCCESSFUL);
        builder.append(EQUAL_OPEN_QUOTE);
        builder.append(Boolean.toString(isSuccessful));
        builder.append(DOUBLE_QUOTE);
    }

    /**
     * Write the metric to a writer in XML for entry log
     * 
     * @param builder
     *            The writer to fill with metric data.
     * @throws IOException
     *             if anything goes wrong.
     */
    public void toSerializedXmlStringEntryLog(Writer builder) throws IOException
    {

        builder.append(SPACE);
        builder.append(FIELD_OPERATION);
        builder.append(EQUAL_OPEN_QUOTE);
        CacheUtilities.printXmlEscapedStringToWriter(builder, operationName);
        builder.append(DOUBLE_QUOTE);
        builder.append(SPACE);
        builder.append(FIELD_PARAMETERS);
        builder.append(EQUAL_OPEN_QUOTE);
        logKeyValuePairList(builder);
        builder.append(DOUBLE_QUOTE);
        builder.append(SPACE);
        builder.append(FIELD_ID);
        builder.append(EQUAL_OPEN_QUOTE);
        builder.append(Long.toString(identifier));
        builder.append(DOUBLE_QUOTE);
        builder.append(SPACE);
        builder.append(FIELD_PARENT_ID);
        builder.append(EQUAL_OPEN_QUOTE);
        builder.append(Long.toString(parentIdentifier));
        builder.append(DOUBLE_QUOTE);
        builder.append(SPACE);
        builder.append(FIELD_START_TIME);
        builder.append(EQUAL_OPEN_QUOTE);
        builder.append(Long.toString(startTime));
        builder.append(DOUBLE_QUOTE);
    }

    /**
     * log keyValuePairList if it is not null
     * 
     * @param builder
     *            builder to write to
     * @throws IOException
     *             If anything goes wrong with the writer.
     */
    public void logKeyValuePairList(Writer builder) throws IOException
    {
        if (keyValuePairList != null)
        {
            Boolean delimiterEQ = true;
            for (int i = 0; i < keyValuePairList.size(); i++)
            {
                String currentString = keyValuePairList.get(i);
                if (currentString == null)
                {
                    currentString = "null";
                }
                CacheUtilities.printXmlEscapedStringToWriter(builder, currentString);

                if (i != keyValuePairList.size() - 1)
                {
                    if (delimiterEQ)
                    {
                        builder.append("=");
                        delimiterEQ = false;
                    }
                    else
                    {
                        builder.append(SERIALIZATION_SEPARATOR);
                        delimiterEQ = true;
                    }
                }
            }
        }
    }

    /**
     * Write log details to writer and return the message string to log
     * 
     * @param metric
     *            the metric used to log
     * @param isEntry
     *            boolean value, if true then log entry, if false then log exit
     * @return the message to be logged
     */
    public static String writeEntryExitLog(OperationMetric metric, boolean isEntry)
    {
        StringWriter writer = new StringWriter();
        try
        {
            if (isEntry)
            {
                writer.append(LogMetricGatherer.PERFORMANCE_METRIC_ENTRYLOG_PREFIX);
                metric.toSerializedXmlStringEntryLog(writer);
                metric.printProperty(writer);
                writer.append(LogMetricGatherer.PERFORMANCE_METRIC_LOG_SUFFIX);
            }
            else
            { // isExit
                writer.append(LogMetricGatherer.PERFORMANCE_METRIC_LOG_PREFIX);
                metric.toSerializedXmlString(writer);
                // don't print properties on exit, they are redundant and
                // sometimes voluminous.
                // metric.printProperty(writer);
                writer.append(LogMetricGatherer.PERFORMANCE_METRIC_LOG_SUFFIX);
            }
        }
        catch (IOException e)
        {
            LOGGER.log(Level.WARNING, "Failed to serialize string", e);
        }
        String logMessage = writer.toString();
        CacheUtilities.closeQuietly(writer);

        return logMessage;
    }

    /**
     * Fill this object by reading data from a serialized string.
     * 
     * @param serializedForm
     *            The object serialized form.
     * @return True if the read operation was successful. False otherwise.
     */
    public boolean fromSerializedString(String serializedForm)
    {
        if (serializedForm == null)
        {
            return false;
        }

        try
        {
            String[] split = serializedForm.split(SERIALIZATION_SEPARATOR);
            if (split.length < 10)
            {
                // not enough tokens
                return false;
            }

            int i = 0;
            identifier = Long.parseLong(split[i++]);
            parentIdentifier = Long.parseLong(split[i++]);
            operationName = CacheUtilities.unescapeString(split[i++]);
            buildKeyValuePairListFromSerializedString(split[i++]);
            startTime = Long.parseLong(split[i++]);
            stopTime = Long.parseLong(split[i++]);
            duration = Long.parseLong(split[i++]);
            i++; // duration in MS.
            resultSize = Integer.parseInt(split[i++]);
            isResultFetchedFromCache = Boolean.parseBoolean(split[i++]);
            isOperationCacheEnabled = Boolean.parseBoolean(split[i++]);

            // successful flag didn't exist in previous version.
            isSuccessful = true;
            if (split.length > i)
            {
                isSuccessful = Boolean.parseBoolean(split[i++]);
            }
        }
        catch (Exception ex)
        {
            boolean isTraceLogEnabled = LoggingHelper.isTraceEnabled(LOGGER);
            if (isTraceLogEnabled)
            {
                String errorMessage = "Failed to parse Operation Metric due to exeptions.";
                LOGGER.log(Level.FINE, errorMessage, ex);
            }
            return false;
        }

        return true;
    }

    /**
     * Rebuild the list of key value pairs from it's serialized version.
     * 
     * @param serializedKeyValuePairList
     *            the serialized key value pair list.
     */
    private void buildKeyValuePairListFromSerializedString(String serializedKeyValuePairList)
    {
        String[] split = serializedKeyValuePairList.split(KEY_VALUE_SEPARATOR);
        keyValuePairList = Arrays.asList(split);

        int size = keyValuePairList.size();

        for (int i = 0; i < size; i++)
        {
            String currentKvp = keyValuePairList.get(i);
            String unescapedKvp = CacheUtilities.unescapeString(currentKvp);
            keyValuePairList.set(i, unescapedKvp);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object o)
    {
        if (o == null)
        {
            return false;
        }

        if (o.getClass() != getClass())
        {
            return false;
        }

        OperationMetric otherMetric = (OperationMetric) o;

        return CacheUtilities.equals(operationName, otherMetric.operationName) &&
            CacheUtilities.equals(keyValuePairList, otherMetric.keyValuePairList) &&
            identifier == otherMetric.identifier && parentIdentifier == otherMetric.parentIdentifier &&
            startTime == otherMetric.startTime && stopTime == otherMetric.stopTime &&
            duration == otherMetric.duration && resultSize == otherMetric.resultSize &&
            isResultFetchedFromCache == otherMetric.isResultFetchedFromCache &&
            isSuccessful == otherMetric.isSuccessful && isOperationCacheEnabled == otherMetric.isOperationCacheEnabled;
    }

    /**
     * @see Object#hashCode()
     * @return the hash code of this object.
     */
    @Override
    public int hashCode()
    {
        HashCodeBuilder builder = new HashCodeBuilder();

        builder.append(identifier);
        builder.append(parentIdentifier);
        builder.append(operationName);
        builder.append(keyValuePairList);
        builder.append(startTime);
        builder.append(stopTime);
        builder.append(duration);
        builder.append(resultSize);
        builder.append(isResultFetchedFromCache);
        builder.append(isOperationCacheEnabled);
        builder.append(isSuccessful);

        return builder.toHashCode();
    }

    /**
     * @return the object identifier
     */
    @Override
    public long getIdentifier()
    {
        return identifier;
    }

    /**
     * @param identifier
     *            the object identifier
     */
    @Override
    public void setIdentifier(long identifier)
    {
        this.identifier = identifier;
    }

    /**
     * @return the parent identifier
     */
    @Override
    public long getParentIdentifier()
    {
        return parentIdentifier;
    }

    /**
     * @param parentIdentifier
     *            the parent identifier
     */
    @Override
    public void setParentIdentifier(long parentIdentifier)
    {
        this.parentIdentifier = parentIdentifier;
    }

    /**
     * Fetch the the current parent operation identifier. If no operations are
     * registered, the value zero will be returned.
     * 
     * @return the
     */
    public static Long getThreadParentOperationIdentifier()
    {
        Long parentId = PARENT_TRACKER.get();
        if (parentId == null)
        {
            parentId = Long.valueOf(0);
        }
        return parentId;
    }

    /**
     * Associate the current thread with the specified parent operation
     * identifier. This operation is offered to allow manual control of the call
     * stack for operations that initiate work in child threads. For single
     * threaded operations, a single call to
     * {@link #startOperation(String, boolean, String...)} is sufficient to
     * manage the parent identifiers.
     * 
     * @param parentOperationIdentifier
     *            the new operation parent identifier. Use zero to reset the
     *            value to defaults.
     */
    public static void setThreadParentOperationIdentifier(Long parentOperationIdentifier)
    {
        PARENT_TRACKER.set(parentOperationIdentifier);
    }

    /**
     * set the initial identifier value
     * 
     * @param initialValue
     *            the initial value to use
     */
    public static void setInitialIdentifierValue(long initialValue)
    {
        IDENTIFIER_ASSIGNER.set(initialValue);
    }

    /**
     * Read a metric from a XML document.
     * 
     * @param parse
     *            The xml document.
     * @return True if parsing succeeded. Throws exceptions otherwise.
     */
    public boolean fromXmlDocument(Document parse)
    {

        Element documentElement = parse.getDocumentElement();
        operationName = documentElement.getAttributes().getNamedItem(FIELD_OPERATION).getNodeValue();
        String parameters = documentElement.getAttributes().getNamedItem(FIELD_PARAMETERS).getNodeValue();
        keyValuePairList = Arrays.asList(parameters.split("[ =]"));
        identifier = Long.valueOf(documentElement.getAttributes().getNamedItem(FIELD_ID).getNodeValue());
        parentIdentifier = Long.valueOf(documentElement.getAttributes().getNamedItem(FIELD_PARENT_ID).getNodeValue());
        startTime = Long.valueOf(documentElement.getAttributes().getNamedItem(FIELD_START_TIME).getNodeValue());
        stopTime = Long.valueOf(documentElement.getAttributes().getNamedItem(FIELD_STOP_TIME).getNodeValue());
        duration = Long.valueOf(documentElement.getAttributes().getNamedItem(FIELD_DURATION).getNodeValue());
        resultSize = Integer.valueOf(documentElement.getAttributes().getNamedItem(FIELD_RESULT_SIZE).getNodeValue());
        isResultFetchedFromCache = Boolean
            .valueOf(documentElement.getAttributes().getNamedItem(FIELD_CACHE_HIT).getNodeValue());
        isOperationCacheEnabled = Boolean
            .valueOf(documentElement.getAttributes().getNamedItem(FIELD_CACHE_ENABLED).getNodeValue());
        Node namedItem = documentElement.getAttributes().getNamedItem(FIELD_SUCCESSFUL);

        isSuccessful = true;
        if (namedItem != null)
        {
            isSuccessful = Boolean.valueOf(namedItem.getNodeValue());
        }
        return true;
    }

}
