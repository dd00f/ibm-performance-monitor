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

import com.ibm.commerce.cache.CommonMetricProperties;
import com.ibm.commerce.cache.OperationMetric;
import com.ibm.service.detailed.MongoLogger;
import com.mongodb.Block;
import com.mongodb.Function;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoIterable;

/**
 * ProfiledFindIterable
 * 
 * @author Steve McDuff
 */
public class ProfiledMapMongotIterable<TDocument, TResult, U> implements MongoIterable<U>, ProfiledCursorCreator, ProfiledMongoIterable<U>
{

    private final MongoIterable<U> mongoIterable;

    private ProfiledMongoCollection<TDocument> collection;

    private final ProfiledCursorCreator creator;

    private int batchSize = -1;

    private Function<TResult, U> mapper;

    public ProfiledMapMongotIterable(MongoIterable<U> map, Function<TResult, U> mapper, ProfiledCursorCreator creator)
    {
        super();
        this.mongoIterable = map;
        this.mapper = mapper;
        this.creator = creator;
    }

    public ProfiledMongoCollection<TDocument> getCollection()
    {
        return collection;
    }

    @Override
    public MongoCursor<U> iterator()
    {
        ProfiledMongoCursor<TDocument, U> profiledMongoCursor = new ProfiledMongoCursor<TDocument, U>(this);
        profiledMongoCursor.start(mongoIterable.iterator());
        return profiledMongoCursor;
    }

    @Override
    public U first()
    {
        U returnValue = null;
        MongoCursor<U> iterator = iterator();
        if (iterator.hasNext())
        {
            returnValue = iterator.next();
        }
        iterator.close();
        return returnValue;
    }

    @Override
    public <V> MongoIterable<V> map(Function<U, V> mapper)
    {
        MongoIterable<V> map = mongoIterable.map(mapper);
        ProfiledMapMongotIterable<TDocument, U, V> profiledMap = new ProfiledMapMongotIterable<TDocument, U, V>(map,
            mapper, this);
        return profiledMap;
    }

    @Override
    public void forEach(Block<? super U> block)
    {
        OperationMetric metric = startMetric("forEach " + block.getClass().getName(), null);
        mongoIterable.forEach(block);
        stopMetric(metric, 0);
    }

    @Override
    public <A extends Collection<? super U>> A into(A target)
    {
        return profileInto(target, this, this);
    }
    
    public static <TResult, A extends Collection<? super TResult>> A profileInto(A target, ProfiledMongoIterable<TResult> iterable, ProfiledCursorCreator creator)
    {
        int sizeBefore = 0;
        int countBefore = target.size();
        boolean resultSetSizeMeasured = MongoLogger.isResultSetSizeMeasured();
        if( resultSetSizeMeasured) {
            sizeBefore = target.toString().length();
        }

        OperationMetric metric = creator.startMetric("into", null);
        A into = iterable.getMongoIterable().into(target);
        int size = 0;
        if( resultSetSizeMeasured) {
            size = target.toString().length() - sizeBefore;
        }
        
        int countAdded = target.size() - countBefore;
        
        if( metric != null ) {
            metric.setProperty(CommonMetricProperties.RESPONSE_OBJECT_COUNT, Integer.toString(countAdded));
        }
        
        creator.stopMetric(metric, size);
        return into;
    }

    @Override
    public OperationMetric startMetric(String function, List<String> keyValuePairs)
    {
        if (!MongoLogger.GATHERER.isEnabled())
        {
            return null;
        }

        String mapFunction = "map";

        if (function != null)
        {
            mapFunction = mapFunction + " : " + function;
        }

        if (keyValuePairs == null)
        {
            keyValuePairs = new ArrayList<String>();
        }

        keyValuePairs.add("function");
        keyValuePairs.add(mapper.getClass().getName());

        if (batchSize != -1)
        {
            keyValuePairs.add("batchSize");
            keyValuePairs.add(Integer.toString(batchSize));
        }

        return creator.startMetric(mapFunction, keyValuePairs);
    }

    @Override
    public void stopMetric(OperationMetric metric, int resultSize)
    {
        creator.stopMetric(metric, resultSize);
    }

    @Override
    public MongoIterable<U> batchSize(int batchSize)
    {
        this.batchSize = batchSize;
        mongoIterable.batchSize(batchSize);
        return this;
    }

    public int getBatchSize()
    {
        return batchSize;
    }

    @Override
    public MongoIterable<U> getMongoIterable()
    {
        return mongoIterable;
    }

}
