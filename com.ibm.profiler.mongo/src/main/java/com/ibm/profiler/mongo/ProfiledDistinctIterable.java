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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.bson.BsonDocument;
import org.bson.conversions.Bson;

import com.ibm.commerce.cache.OperationMetric;
import com.ibm.service.detailed.MongoLogger;
import com.mongodb.Block;
import com.mongodb.Function;
import com.mongodb.MongoClient;
import com.mongodb.client.DistinctIterable;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoIterable;
import com.mongodb.client.model.Collation;

/**
 * ProfiledFindIterable
 * 
 * @author Steve McDuff
 */
public class ProfiledDistinctIterable<TDocument, TResult> implements DistinctIterable<TResult>, ProfiledCursorCreator, ProfiledMongoIterable<TResult>
{

    private DistinctIterable<TResult> distinctIterable;

    private ProfiledMongoCollection<TDocument> collection;

    private Bson filter;

    private int batchSize = -1;

    private Collation collation;

    private String fieldName;

    public ProfiledDistinctIterable(DistinctIterable<TResult> findIterable, String fieldName,
        ProfiledMongoCollection<TDocument> collection)
    {
        super();
        this.distinctIterable = findIterable;
        this.fieldName = fieldName;
        this.collection = collection;
    }

    public ProfiledMongoCollection<TDocument> getCollection()
    {
        return collection;
    }

    public String getFieldName()
    {
        return fieldName;
    }

    @Override
    public MongoCursor<TResult> iterator()
    {
        ProfiledMongoCursor<TDocument, TResult> profiledMongoCursor = new ProfiledMongoCursor<TDocument, TResult>(this);
        profiledMongoCursor.start(distinctIterable.iterator());
        return profiledMongoCursor;
    }

    @Override
    public TResult first()
    {
        TResult returnValue = null;
        MongoCursor<TResult> iterator = iterator();
        if (iterator.hasNext())
        {
            returnValue = iterator.next();
        }
        iterator.close();
        return returnValue;
    }

    @Override
    public <U> MongoIterable<U> map(Function<TResult, U> mapper)
    {
        MongoIterable<U> map = distinctIterable.map(mapper);
        ProfiledMapMongotIterable<TDocument, TResult, U> profiledMap = new ProfiledMapMongotIterable<TDocument, TResult, U>(
            map, mapper, this);
        return profiledMap;
    }

    @Override
    public void forEach(Block<? super TResult> block)
    {
        OperationMetric metric = startMetric("forEach " + block.getClass().getName(), null);
        distinctIterable.forEach(block);
        stopMetric(metric, 0);
    }

    @Override
    public <A extends Collection<? super TResult>> A into(A target)
    {
        return ProfiledMapMongotIterable.profileInto(target, this, this);
    }

    public DistinctIterable<TResult> getDistinceIterable()
    {
        return distinctIterable;
    }

    public Bson getFilter()
    {
        return filter;
    }

    public int getBatchSize()
    {
        return batchSize;
    }

    public Collation getCollation()
    {
        return collation;
    }

    public String getOperationName(String function)
    {
        StringBuilder builder = new StringBuilder();

        String collectionName = getCollection().getNamespace().getCollectionName();

        builder.append("Mongo : ");
        builder.append(collectionName);
        builder.append(" : distinct ");
        builder.append(fieldName);

        if (function != null)
        {
            builder.append(" : ");
            builder.append(function);
        }

        Bson filter = getFilter();
        if (filter != null)
        {
            filter = MongoUtilities.filterParameters(filter.toBsonDocument(BsonDocument.class,
                MongoClient.getDefaultCodecRegistry()));

            builder.append(" : Filter ");
            builder.append(filter.toString());
        }

        Collation collation = getCollation();
        if (collation != null)
        {
            builder.append(" : Collation ");
            builder.append(collation.asDocument().toString());
        }

        return builder.toString();
    }

    @Override
    public OperationMetric startMetric(String function, List<String> keyValuePairs)
    {

        if (!MongoLogger.GATHERER.isEnabled())
        {
            return null;
        }

        OperationMetric metric = new OperationMetric();
        String operationName = getOperationName(function);
        if (keyValuePairs == null)
        {
            keyValuePairs = new ArrayList<String>();
        }
        MongoUtilities.addKeyValuePairs(getFilter(), keyValuePairs);

        metric.startOperation(operationName, false);
        metric.setKeyValuePairList(keyValuePairs);
        getCollection().addCollectionInformation(metric);
        getCollection().addReadConcernAndPreference(metric);

        MongoLogger.GATHERER.gatherMetricEntryLog(metric);

        return metric;
    }

    @Override
    public void stopMetric(OperationMetric metric, int resultSize)
    {
        if (metric == null)
        {
            return;
        }
        metric.stopOperation(resultSize, false);
        MongoLogger.GATHERER.gatherMetric(metric);
    }

    @Override
    public DistinctIterable<TResult> filter(Bson filter)
    {
        this.filter = filter;
        distinctIterable.filter(filter);
        return this;
    }

    @Override
    public DistinctIterable<TResult> maxTime(long maxTime, TimeUnit timeUnit)
    {
        distinctIterable.maxTime(maxTime, timeUnit);
        return this;
    }

    @Override
    public DistinctIterable<TResult> batchSize(int batchSize)
    {
        this.batchSize = batchSize;
        distinctIterable.batchSize(batchSize);
        return this;
    }

    @Override
    public DistinctIterable<TResult> collation(Collation collation)
    {
        this.collation = collation;
        distinctIterable.collation(collation);
        return this;
    }
    
    @Override
    public MongoIterable<TResult> getMongoIterable()
    {
        return distinctIterable;
    }

}
