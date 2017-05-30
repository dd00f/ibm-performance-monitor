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
import java.util.concurrent.TimeUnit;

import org.bson.BsonDocument;
import org.bson.conversions.Bson;

import com.ibm.commerce.cache.OperationMetric;
import com.ibm.service.detailed.MongoLogger;
import com.mongodb.Block;
import com.mongodb.Function;
import com.mongodb.MongoClient;
import com.mongodb.client.MapReduceIterable;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoIterable;
import com.mongodb.client.model.Collation;
import com.mongodb.client.model.MapReduceAction;

/**
 * ProfiledMapReduceIterable
 * 
 * @author Steve McDuff
 */
public class ProfiledMapReduceIterable<TDocument, TResult> implements MapReduceIterable<TResult>, ProfiledCursorCreator, ProfiledMongoIterable<TResult>
{

    private MapReduceIterable<TResult> mapReduceIterable;

    private ProfiledMongoCollection<TDocument> collection;

    private Bson filter;

    private int limit = -1;

    private int batchSize = -1;

    private Bson sort;
    
    private Bson scope;
    
    private Collation collation;

    private String mapFunction;

    private String reduceFunction;

    public ProfiledMapReduceIterable(String mapFunction, String reduceFunction, MapReduceIterable<TResult> mapReduce, ProfiledMongoCollection<TDocument> collection)
    {
        super();
        this.mapFunction = mapFunction;
        this.reduceFunction = reduceFunction;
        this.mapReduceIterable = mapReduce;
        this.collection = collection;
    }

    public ProfiledMongoCollection<TDocument> getCollection()
    {
        return collection;
    }

    @Override
    public MongoCursor<TResult> iterator()
    {
        ProfiledMongoCursor<TResult, TResult> profiledMongoCursor = new ProfiledMongoCursor<TResult, TResult>(this);
        profiledMongoCursor.start(mapReduceIterable.iterator());
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
        MongoIterable<U> map = mapReduceIterable.map(mapper);
        ProfiledMapMongotIterable<TResult, TResult, U> profiledMap = 
            new ProfiledMapMongotIterable<TResult, TResult, U>(map, mapper, this );
        return profiledMap;
    }

    @Override
    public void forEach(Block<? super TResult> block)
    {
        OperationMetric metric = startMetric("forEach " + block.getClass().getName(), null);
        mapReduceIterable.forEach(block);
        stopMetric(metric, 0);
    }

    @Override
    public <A extends Collection<? super TResult>> A into(A target)
    {
        return ProfiledMapMongotIterable.profileInto(target, this, this);
    }

    @Override
    public MapReduceIterable<TResult> filter(Bson filter)
    {
        this.filter = filter;
        mapReduceIterable.filter(filter);
        return this;
    }

    @Override
    public MapReduceIterable<TResult> limit(int limit)
    {
        this.limit = limit;
        mapReduceIterable.limit(limit);
        return this;
    }

    @Override
    public MapReduceIterable<TResult> maxTime(long maxTime, TimeUnit timeUnit)
    {
        mapReduceIterable.maxTime(maxTime, timeUnit);
        return this;
    }

    @Override
    public MapReduceIterable<TResult> sort(Bson sort)
    {
        this.sort = sort;
        mapReduceIterable.sort(sort);
        return this;
    }

    @Override
    public MapReduceIterable<TResult> batchSize(int batchSize)
    {
        this.batchSize = batchSize;
        mapReduceIterable.batchSize(batchSize);
        return this;
    }

    @Override
    public MapReduceIterable<TResult> collation(Collation collation)
    {
        this.collation = collation;
        mapReduceIterable.collation(collation);
        return this;
    }

    public MapReduceIterable<TResult> getMapReduceIterable()
    {
        return mapReduceIterable;
    }

    public Bson getFilter()
    {
        return filter;
    }

    public int getLimit()
    {
        return limit;
    }

    public int getBatchSize()
    {
        return batchSize;
    }

    public Bson getSort()
    {
        return sort;
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
        builder.append(" : mapReduce");
        
        if( function != null ) {
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

        Bson sort = getSort();
        if (sort != null)
        {
            builder.append(" : Sort ");
            builder.append(sort.toString());
        }

        if (limit != -1)
        {
            builder.append(" : Limit ");
            builder.append(limit);
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
    public OperationMetric startMetric(String function, List<String> keyValuePairs) {
        
        if( ! MongoLogger.GATHERER.isEnabled() ) {
            return null;
        }
        
        OperationMetric metric = new OperationMetric();
        String operationName = getOperationName(function);
        
        if( keyValuePairs == null ) {
            keyValuePairs = new ArrayList<String>();
        }
        
        keyValuePairs.add("mapFunction");
        keyValuePairs.add(mapFunction);
        keyValuePairs.add("reduceFunction");
        keyValuePairs.add(reduceFunction);
        
        Bson scope2 = getScope();
        if( scope2 != null ) {
            keyValuePairs.add("scope");
            keyValuePairs.add(scope2.toString());
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
    public void stopMetric(OperationMetric metric, int resultSize) {
        if( metric == null ) {
            return;
        }
        metric.stopOperation(resultSize, false);
        MongoLogger.GATHERER.gatherMetric(metric);
    }

    @Override
    public void toCollection()
    {
        OperationMetric metric = startMetric("toCollection", null);
        mapReduceIterable.toCollection();
        stopMetric(metric, 0);
    }

    @Override
    public MapReduceIterable<TResult> collectionName(String collectionName)
    {
        mapReduceIterable.collectionName(collectionName);
        return this;
    }

    @Override
    public MapReduceIterable<TResult> finalizeFunction(String finalizeFunction)
    {
        mapReduceIterable.finalizeFunction(finalizeFunction);
        return this;
    }

    @Override
    public MapReduceIterable<TResult> scope(Bson scope)
    {
        this.scope = scope;
        mapReduceIterable.scope(scope);
        return this;
    }
    
    public Bson getScope()
    {
        return scope;
    }

    @Override
    public MapReduceIterable<TResult> jsMode(boolean jsMode)
    {
        mapReduceIterable.jsMode(jsMode);
        return this;
    }

    @Override
    public MapReduceIterable<TResult> verbose(boolean verbose)
    {
        mapReduceIterable.verbose(verbose);
        return this;
    }

    @Override
    public MapReduceIterable<TResult> action(MapReduceAction action)
    {
        mapReduceIterable.action(action);
        return this;
    }

    @Override
    public MapReduceIterable<TResult> databaseName(String databaseName)
    {
        mapReduceIterable.databaseName(databaseName);
        return this;
    }

    @Override
    public MapReduceIterable<TResult> sharded(boolean sharded)
    {
        mapReduceIterable.sharded(sharded);
        return this;
    }

    @Override
    public MapReduceIterable<TResult> nonAtomic(boolean nonAtomic)
    {
        mapReduceIterable.nonAtomic(nonAtomic);
        return this;
    }

    @Override
    public MapReduceIterable<TResult> bypassDocumentValidation(Boolean bypassDocumentValidation)
    {
        mapReduceIterable.bypassDocumentValidation(bypassDocumentValidation);
        return this;
    }

    @Override
    public MongoIterable<TResult> getMongoIterable()
    {
        return mapReduceIterable;
    }
}
