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
import com.mongodb.CursorType;
import com.mongodb.Function;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoIterable;
import com.mongodb.client.model.Collation;

/**
 * ProfiledFindIterable
 * 
 * @author Steve McDuff
 */
public class ProfiledFindIterable<TDocument, TResult> implements FindIterable<TResult>, ProfiledCursorCreator, ProfiledMongoIterable<TResult>
{

    private FindIterable<TResult> findIterable;

    private ProfiledMongoCollection<TDocument> collection;

    private Bson filter;

    private int limit = -1;

    private Bson modifiers;

    private int batchSize = -1;

    private Bson projection;

    private Bson sort;

    private int skip = -1;

    private Collation collation;

    private CursorType cursorType;

    public ProfiledFindIterable(FindIterable<TResult> findIterable, ProfiledMongoCollection<TDocument> collection)
    {
        super();
        this.findIterable = findIterable;
        this.collection = collection;
    }

    public ProfiledMongoCollection<TDocument> getCollection()
    {
        return collection;
    }

    @Override
    public MongoCursor<TResult> iterator()
    {
        ProfiledMongoCursor<TDocument, TResult> profiledMongoCursor = new ProfiledMongoCursor<TDocument, TResult>(this);
        profiledMongoCursor.start(findIterable.iterator());
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
        MongoIterable<U> map = findIterable.map(mapper);
        ProfiledMapMongotIterable<TDocument, TResult, U> profiledMap = 
            new ProfiledMapMongotIterable<TDocument, TResult, U>(map, mapper, this );
        return profiledMap;
    }

    @Override
    public void forEach(Block<? super TResult> block)
    {
        OperationMetric metric = startMetric("forEach " + block.getClass().getName(), null);
        findIterable.forEach(block);
        stopMetric(metric, 0);
    }

    @Override
    public <A extends Collection<? super TResult>> A into(A target)
    {
        return ProfiledMapMongotIterable.profileInto(target, this, this);
    }

    @Override
    public FindIterable<TResult> filter(Bson filter)
    {
        this.filter = filter;
        findIterable.filter(filter);
        return this;
    }

    @Override
    public FindIterable<TResult> limit(int limit)
    {
        this.limit = limit;
        findIterable.limit(limit);
        return this;
    }

    @Override
    public FindIterable<TResult> skip(int skip)
    {
        this.skip = skip;
        findIterable.skip(skip);
        return this;
    }

    @Override
    public FindIterable<TResult> maxTime(long maxTime, TimeUnit timeUnit)
    {
        findIterable.maxTime(maxTime, timeUnit);
        return this;
    }

    @Override
    public FindIterable<TResult> maxAwaitTime(long maxAwaitTime, TimeUnit timeUnit)
    {
        findIterable.maxAwaitTime(maxAwaitTime, timeUnit);
        return this;
    }

    @Override
    public FindIterable<TResult> modifiers(Bson modifiers)
    {
        this.modifiers = modifiers;
        findIterable.modifiers(modifiers);
        return this;
    }

    @Override
    public FindIterable<TResult> projection(Bson projection)
    {
        this.projection = projection;
        findIterable.projection(projection);
        return this;
    }

    @Override
    public FindIterable<TResult> sort(Bson sort)
    {
        this.sort = sort;
        findIterable.sort(sort);
        return this;
    }

    @Override
    public FindIterable<TResult> noCursorTimeout(boolean noCursorTimeout)
    {
        findIterable.noCursorTimeout(noCursorTimeout);
        return this;
    }

    @Override
    public FindIterable<TResult> oplogReplay(boolean oplogReplay)
    {
        findIterable.oplogReplay(oplogReplay);
        return this;
    }

    @Override
    public FindIterable<TResult> partial(boolean partial)
    {
        findIterable.partial(partial);
        return this;
    }

    @Override
    public FindIterable<TResult> cursorType(CursorType cursorType)
    {
        this.cursorType = cursorType;
        findIterable.cursorType(cursorType);
        return this;
    }

    @Override
    public FindIterable<TResult> batchSize(int batchSize)
    {
        this.batchSize = batchSize;
        findIterable.batchSize(batchSize);
        return this;
    }

    @Override
    public FindIterable<TResult> collation(Collation collation)
    {
        this.collation = collation;
        findIterable.collation(collation);
        return this;
    }

    public FindIterable<TResult> getFindIterable()
    {
        return findIterable;
    }

    public Bson getFilter()
    {
        return filter;
    }

    public int getLimit()
    {
        return limit;
    }

    public Bson getModifiers()
    {
        return modifiers;
    }

    public int getBatchSize()
    {
        return batchSize;
    }

    public Bson getProjection()
    {
        return projection;
    }

    public Bson getSort()
    {
        return sort;
    }

    public int getSkip()
    {
        return skip;
    }

    public Collation getCollation()
    {
        return collation;
    }

    public CursorType getCursorType()
    {
        return cursorType;
    }

    public String getOperationName(String function)
    {
        StringBuilder builder = new StringBuilder();

        String collectionName = getCollection().getNamespace().getCollectionName();

        builder.append("Mongo : ");
        builder.append(collectionName);
        builder.append(" : find");
        
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

        Bson modifiers = getModifiers();
        if (modifiers != null)
        {
            builder.append(" : Modifiers ");
            builder.append(modifiers.toString());
        }

        Bson projection = getProjection();
        if (projection != null)
        {
            builder.append(" : Projection ");
            builder.append(projection.toString());
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

        CursorType cursorType2 = getCursorType();
        if (cursorType2 != null)
        {
            builder.append(" : Cursor Type ");
            builder.append(cursorType2.toString());
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
        
        MongoUtilities.addKeyValuePairs(getFilter(), keyValuePairs);
        
        if (skip != -1)
        {
            keyValuePairs.add("skip");
            keyValuePairs.add(Integer.toString(skip));
        }
        
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
        MongoUtilities.incrementMongoStats(metric);
    }

    @Override
    public MongoIterable<TResult> getMongoIterable()
    {
        return findIterable;
    }
}
