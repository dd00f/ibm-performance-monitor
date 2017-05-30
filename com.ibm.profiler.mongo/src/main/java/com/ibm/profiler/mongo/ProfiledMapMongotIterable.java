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
