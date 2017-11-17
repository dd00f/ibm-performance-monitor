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
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.bson.BsonDocument;
import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.conversions.Bson;

import com.ibm.commerce.cache.CacheUtilities;
import com.ibm.commerce.cache.CommonMetricProperties;
import com.ibm.commerce.cache.OperationMetric;
import com.ibm.service.detailed.MongoLogger;
import com.mongodb.MongoNamespace;
import com.mongodb.ReadConcern;
import com.mongodb.ReadPreference;
import com.mongodb.WriteConcern;
import com.mongodb.bulk.BulkWriteResult;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.DistinctIterable;
import com.mongodb.client.FindIterable;
import com.mongodb.client.ListIndexesIterable;
import com.mongodb.client.MapReduceIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.BulkWriteOptions;
import com.mongodb.client.model.CountOptions;
import com.mongodb.client.model.DeleteOptions;
import com.mongodb.client.model.FindOneAndDeleteOptions;
import com.mongodb.client.model.FindOneAndReplaceOptions;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.IndexModel;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.InsertManyOptions;
import com.mongodb.client.model.InsertOneOptions;
import com.mongodb.client.model.RenameCollectionOptions;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.WriteModel;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;

/**
 * ProfiledMongoCollection
 * 
 * @author Steve McDuff
 */
public class ProfiledMongoCollection<TDocument> implements MongoCollection<TDocument>
{
    private final MongoCollection<TDocument> collection;

    public ProfiledMongoCollection(MongoCollection<TDocument> collection)
    {
        this.collection = collection;
    }

    protected OperationMetric startMetric(String function, List<String> keyValuePairs)
    {

        if (!MongoLogger.GATHERER.isEnabled())
        {
            return null;
        }

        OperationMetric metric = new OperationMetric();

        metric.startOperation(function, false);
        metric.setKeyValuePairList(keyValuePairs);
        addCollectionInformation(metric);

        MongoLogger.GATHERER.gatherMetricEntryLog(metric);

        return metric;
    }

    public void addCollectionInformation(OperationMetric metric)
    {
        metric.setProperty(MongoProperties.MONGO_COLLECTION, getNamespace().getCollectionName());
        metric.setProperty(MongoProperties.MONGO_DATABASE, getNamespace().getDatabaseName());
    }

    protected void stopMetric(OperationMetric metric, int resultSize)
    {
        if (metric == null)
        {
            return;
        }
        metric.stopOperation(resultSize, false);
        MongoLogger.GATHERER.gatherMetric(metric);
        MongoUtilities.incrementMongoStats(metric);
    }

    @Override
    public AggregateIterable<TDocument> aggregate(List<? extends Bson> arg0)
    {
        OperationMetric metric = null;
        if (MongoLogger.GATHERER.isEnabled())
        {
            List<BsonDocument> aggregation = MongoUtilities.filterParameters(arg0);
            List<String> keyValuePairs = MongoUtilities.getKeyValuePairs(arg0);
            String operationName = "Mongo : " + getNamespace().getCollectionName() + " : aggregate " +
                aggregation.toString();
            metric = startMetric(operationName, keyValuePairs);
            metric.setKeyValuePairList(keyValuePairs);
            addReadConcernAndPreference(metric);
        }

        AggregateIterable<TDocument> aggregate = collection.aggregate(arg0);

        stopMetric(metric, 0);
        return aggregate;
    }

    @Override
    public <TResult> AggregateIterable<TResult> aggregate(List<? extends Bson> arg0, Class<TResult> arg1)
    {
        OperationMetric metric = null;
        if (MongoLogger.GATHERER.isEnabled())
        {
            List<BsonDocument> aggregation = MongoUtilities.filterParameters(arg0);
            List<String> keyValuePairs = MongoUtilities.getKeyValuePairs(arg0);
            String operationName = "Mongo : " + getNamespace().getCollectionName() + " : aggregate " +
                aggregation.toString();
            metric = startMetric(operationName, keyValuePairs);
            addReadConcernAndPreference(metric);
        }

        AggregateIterable<TResult> aggregate = collection.aggregate(arg0, arg1);

        stopMetric(metric, 0);
        return aggregate;
    }

    @Override
    public BulkWriteResult bulkWrite(List<? extends WriteModel<? extends TDocument>> arg0)
    {
        int writeSize = 0;
        OperationMetric metric = null;
        if (MongoLogger.GATHERER.isEnabled())
        {
            List<String> keyValuePairs = createWriteKeyValuePairs();
            String operationName = "Mongo : " + getNamespace().getCollectionName() + " : bulkWrite";
            metric = startMetric(operationName, keyValuePairs);
            metric.setProperty(CommonMetricProperties.REQUEST_OBJECT_COUNT, Integer.toString(arg0.size()));
            addWriteConcern(metric);

            if (MongoLogger.isResultSetSizeMeasured())
            {
                writeSize = arg0.toString().length();
            }
        }

        BulkWriteResult bulkWrite = collection.bulkWrite(arg0);

        stopMetric(metric, writeSize);

        return bulkWrite;
    }

    @Override
    public BulkWriteResult bulkWrite(List<? extends WriteModel<? extends TDocument>> arg0, BulkWriteOptions arg1)
    {
        int writeSize = 0;
        OperationMetric metric = null;
        if (MongoLogger.GATHERER.isEnabled())
        {
            List<String> keyValuePairs = createWriteKeyValuePairs();
            String operationName = "Mongo : " + getNamespace().getCollectionName() + " : bulkWrite";
            metric = startMetric(operationName, keyValuePairs);
            metric.setProperty(CommonMetricProperties.REQUEST_OBJECT_COUNT, Integer.toString(arg0.size()));
            addWriteConcern(metric);

            if (MongoLogger.isResultSetSizeMeasured())
            {
                writeSize = arg0.toString().length();
            }
        }

        BulkWriteResult bulkWrite = collection.bulkWrite(arg0, arg1);

        stopMetric(metric, writeSize);

        return bulkWrite;
    }

    @Override
    public long count()
    {
        int writeSize = 0;
        OperationMetric metric = null;
        if (MongoLogger.GATHERER.isEnabled())
        {
            List<String> keyValuePairs = null;
            String operationName = "Mongo : " + getNamespace().getCollectionName() + " : count";
            metric = startMetric(operationName, keyValuePairs);
            addReadConcernAndPreference(metric);
        }

        long count = collection.count();

        stopMetric(metric, writeSize);

        return count;
    }

    @Override
    public long count(Bson filter)
    {
        int writeSize = 0;
        OperationMetric metric = null;
        if (MongoLogger.GATHERER.isEnabled())
        {
            List<String> keyValuePairs = MongoUtilities.getKeyValuePairs(filter);
            String operationName = "Mongo : " + getNamespace().getCollectionName() + " : count " +
                MongoUtilities.filterParameters(filter).toString();
            metric = startMetric(operationName, keyValuePairs);
            addReadConcernAndPreference(metric);
        }

        long count = collection.count(filter);

        stopMetric(metric, writeSize);

        return count;
    }

    @Override
    public long count(Bson filter, CountOptions arg1)
    {
        int writeSize = 0;
        OperationMetric metric = null;
        if (MongoLogger.GATHERER.isEnabled())
        {
            List<String> keyValuePairs = MongoUtilities.getKeyValuePairs(filter);
            String operationName = "Mongo : " + getNamespace().getCollectionName() + " : count " +
                MongoUtilities.filterParameters(filter).toString();
            metric = startMetric(operationName, keyValuePairs);
            addReadConcernAndPreference(metric);
        }

        long count = collection.count(filter, arg1);

        stopMetric(metric, writeSize);

        return count;
    }

    @Override
    public String createIndex(Bson arg0)
    {
        int writeSize = 0;
        OperationMetric metric = null;
        if (MongoLogger.GATHERER.isEnabled())
        {
            List<String> keyValuePairs = Arrays.asList(arg0.toString());
            String operationName = "Mongo : " + getNamespace().getCollectionName() + " : createIndex";
            metric = startMetric(operationName, keyValuePairs);
        }

        String retVal = collection.createIndex(arg0);

        stopMetric(metric, writeSize);

        return retVal;
    }

    @Override
    public String createIndex(Bson arg0, IndexOptions arg1)
    {
        int writeSize = 0;
        OperationMetric metric = null;
        if (MongoLogger.GATHERER.isEnabled())
        {
            List<String> keyValuePairs = Arrays.asList(arg0.toString());
            String operationName = "Mongo : " + getNamespace().getCollectionName() + " : createIndex";
            metric = startMetric(operationName, keyValuePairs);
        }

        String retVal = collection.createIndex(arg0, arg1);

        stopMetric(metric, writeSize);

        return retVal;
    }

    @Override
    public List<String> createIndexes(List<IndexModel> arg0)
    {
        int writeSize = 0;
        OperationMetric metric = null;
        if (MongoLogger.GATHERER.isEnabled())
        {
            List<String> keyValuePairs = MongoUtilities.getIndexModelKeyValuePairs(arg0);
            String operationName = "Mongo : " + getNamespace().getCollectionName() + " : createIndexes";
            metric = startMetric(operationName, keyValuePairs);
        }

        List<String> retVal = collection.createIndexes(arg0);

        stopMetric(metric, writeSize);

        return retVal;
    }

    @Override
    public DeleteResult deleteMany(Bson filter)
    {
        OperationMetric metric = null;
        if (MongoLogger.GATHERER.isEnabled())
        {
            List<String> keyValuePairs = MongoUtilities.getKeyValuePairs(filter);
            String operationName = "Mongo : " + getNamespace().getCollectionName() + " : deleteMany " +
                MongoUtilities.filterParameters(filter).toString();
            metric = startMetric(operationName, keyValuePairs);
            addWriteConcern(metric);
        }

        DeleteResult retVal = collection.deleteMany(filter);

        stopMetric(metric, (int) retVal.getDeletedCount());

        return retVal;
    }

    @Override
    public DeleteResult deleteMany(Bson filter, DeleteOptions arg1)
    {
        OperationMetric metric = null;
        if (MongoLogger.GATHERER.isEnabled())
        {
            List<String> keyValuePairs = MongoUtilities.getKeyValuePairs(filter);
            String operationName = "Mongo : " + getNamespace().getCollectionName() + " : deleteMany " +
                MongoUtilities.filterParameters(filter).toString();
            metric = startMetric(operationName, keyValuePairs);
            addWriteConcern(metric);
        }

        DeleteResult retVal = collection.deleteMany(filter, arg1);

        stopMetric(metric, (int) retVal.getDeletedCount());

        return retVal;
    }

    @Override
    public DeleteResult deleteOne(Bson filter)
    {
        OperationMetric metric = null;
        if (MongoLogger.GATHERER.isEnabled())
        {
            List<String> keyValuePairs = MongoUtilities.getKeyValuePairs(filter);
            String operationName = "Mongo : " + getNamespace().getCollectionName() + " : deleteOne " +
                MongoUtilities.filterParameters(filter).toString();
            metric = startMetric(operationName, keyValuePairs);
            addWriteConcern(metric);
        }

        DeleteResult retVal = collection.deleteOne(filter);

        stopMetric(metric, (int) retVal.getDeletedCount());

        return retVal;
    }

    @Override
    public DeleteResult deleteOne(Bson filter, DeleteOptions arg1)
    {
        OperationMetric metric = null;
        if (MongoLogger.GATHERER.isEnabled())
        {
            List<String> keyValuePairs = MongoUtilities.getKeyValuePairs(filter);
            String operationName = "Mongo : " + getNamespace().getCollectionName() + " : deleteOne " +
                MongoUtilities.filterParameters(filter).toString();
            metric = startMetric(operationName, keyValuePairs);
            addWriteConcern(metric);
        }

        DeleteResult retVal = collection.deleteOne(filter, arg1);

        stopMetric(metric, (int) retVal.getDeletedCount());

        return retVal;
    }

    @Override
    public <TResult> DistinctIterable<TResult> distinct(String fieldName, Class<TResult> arg1)
    {
        DistinctIterable<TResult> find = collection.distinct(fieldName, arg1);
        ProfiledDistinctIterable<TDocument, TResult> profiledFindIterable = new ProfiledDistinctIterable<TDocument, TResult>(
            find, fieldName, this);
        return profiledFindIterable;
    }

    @Override
    public <TResult> DistinctIterable<TResult> distinct(String arg0, Bson filter, Class<TResult> arg2)
    {
        DistinctIterable<TResult> find = collection.distinct(arg0, filter, arg2);
        ProfiledDistinctIterable<TDocument, TResult> profiledFindIterable = new ProfiledDistinctIterable<TDocument, TResult>(
            find, arg0, this);
        profiledFindIterable.filter(filter);
        return profiledFindIterable;
    }

    @Override
    public void drop()
    {
        OperationMetric metric = null;
        if (MongoLogger.GATHERER.isEnabled())
        {
            List<String> keyValuePairs = null;
            String operationName = "Mongo : " + getNamespace().getCollectionName() + " : drop";
            metric = startMetric(operationName, keyValuePairs);
        }

        collection.drop();

        stopMetric(metric, 0);

    }

    @Override
    public void dropIndex(String arg0)
    {
        OperationMetric metric = null;
        if (MongoLogger.GATHERER.isEnabled())
        {
            List<String> keyValuePairs = Arrays.asList(arg0);
            String operationName = "Mongo : " + getNamespace().getCollectionName() + " : dropIndex";
            metric = startMetric(operationName, keyValuePairs);
        }

        collection.dropIndex(arg0);

        stopMetric(metric, 0);
    }

    @Override
    public void dropIndex(Bson arg0)
    {
        OperationMetric metric = null;
        if (MongoLogger.GATHERER.isEnabled())
        {
            List<String> keyValuePairs = Arrays.asList(arg0.toString());
            String operationName = "Mongo : " + getNamespace().getCollectionName() + " : dropIndex";
            metric = startMetric(operationName, keyValuePairs);
        }

        collection.dropIndex(arg0);

        stopMetric(metric, 0);
    }

    @Override
    public void dropIndexes()
    {
        OperationMetric metric = null;
        if (MongoLogger.GATHERER.isEnabled())
        {
            List<String> keyValuePairs = null;
            String operationName = "Mongo : " + getNamespace().getCollectionName() + " : dropIndexes";
            metric = startMetric(operationName, keyValuePairs);
        }

        collection.dropIndexes();

        stopMetric(metric, 0);
    }

    @Override
    public FindIterable<TDocument> find()
    {
        FindIterable<TDocument> find = collection.find();
        return new ProfiledFindIterable<TDocument, TDocument>(find, this);
    }

    @Override
    public <TResult> FindIterable<TResult> find(Class<TResult> arg0)
    {
        FindIterable<TResult> find = collection.find(arg0);
        return new ProfiledFindIterable<TDocument, TResult>(find, this);
    }

    @Override
    public FindIterable<TDocument> find(Bson filter)
    {
        FindIterable<TDocument> find = collection.find(filter);
        ProfiledFindIterable<TDocument, TDocument> profiledFindIterable = new ProfiledFindIterable<TDocument, TDocument>(
            find, this);
        profiledFindIterable.filter(filter);
        return profiledFindIterable;
    }

    @Override
    public <TResult> FindIterable<TResult> find(Bson filter, Class<TResult> arg1)
    {
        FindIterable<TResult> find = collection.find(filter, arg1);
        ProfiledFindIterable<TDocument, TResult> profiledFindIterable = new ProfiledFindIterable<TDocument, TResult>(
            find, this);
        profiledFindIterable.filter(filter);
        return profiledFindIterable;
    }

    @Override
    public TDocument findOneAndDelete(Bson filter)
    {
        OperationMetric metric = null;
        if (MongoLogger.GATHERER.isEnabled())
        {
            List<String> keyValuePairs = MongoUtilities.getKeyValuePairs(filter);
            String operationName = "Mongo : " + getNamespace().getCollectionName() + " : findOneAndDelete " +
                MongoUtilities.filterParameters(filter).toString();
            metric = startMetric(operationName, keyValuePairs);
            addWriteConcern(metric);
            addReadConcernAndPreference(metric);
        }

        TDocument retVal = collection.findOneAndDelete(filter);

        stopMetric(metric, measureDocumentSizeIfResultSizeEnabled(retVal));

        return retVal;
    }

    private int measureDocumentSizeIfResultSizeEnabled(TDocument document)
    {
        int retVal = 0;
        if (MongoLogger.isResultSetSizeMeasured())
        {
            retVal = measureDocumentSize(document);
        }
        return retVal;
    }

    private int measureDocumentSize(Object document)
    {
        int retVal = 0;
        if (document != null)
        {
            retVal = document.toString().length();
        }
        return retVal;
    }

    @Override
    public TDocument findOneAndDelete(Bson filter, FindOneAndDeleteOptions arg1)
    {
        OperationMetric metric = null;
        if (MongoLogger.GATHERER.isEnabled())
        {
            List<String> keyValuePairs = MongoUtilities.getKeyValuePairs(filter);
            String operationName = "Mongo : " + getNamespace().getCollectionName() + " : findOneAndDelete " +
                MongoUtilities.filterParameters(filter).toString();
            metric = startMetric(operationName, keyValuePairs);
            addWriteConcern(metric);
            addReadConcernAndPreference(metric);
        }

        TDocument retVal = collection.findOneAndDelete(filter, arg1);

        stopMetric(metric, measureDocumentSizeIfResultSizeEnabled(retVal));

        return retVal;
    }

    @Override
    public TDocument findOneAndReplace(Bson filter, TDocument arg1)
    {
        OperationMetric metric = null;
        if (MongoLogger.GATHERER.isEnabled())
        {
            List<String> keyValuePairs = MongoUtilities.getKeyValuePairs(filter);
            String operationName = "Mongo : " + getNamespace().getCollectionName() + " : findOneAndReplace " +
                MongoUtilities.filterParameters(filter).toString();
            addWriteKeyValuePairs(keyValuePairs);
            metric = startMetric(operationName, keyValuePairs);
            if (MongoLogger.isRequestSizeMeasured())
            {
                metric.setProperty(CommonMetricProperties.REQUEST_SIZE_BYTES, Integer.toString(measureDocumentSize(arg1)));
            }
            addWriteConcern(metric);
            addReadConcernAndPreference(metric);
        }

        TDocument retVal = collection.findOneAndReplace(filter, arg1);

        stopMetric(metric, measureDocumentSizeIfResultSizeEnabled(retVal));

        return retVal;
    }

    @Override
    public TDocument findOneAndReplace(Bson filter, TDocument arg1, FindOneAndReplaceOptions arg2)
    {
        OperationMetric metric = null;
        if (MongoLogger.GATHERER.isEnabled())
        {
            List<String> keyValuePairs = MongoUtilities.getKeyValuePairs(filter);
            String operationName = "Mongo : " + getNamespace().getCollectionName() + " : findOneAndReplace " +
                MongoUtilities.filterParameters(filter).toString();
            metric = startMetric(operationName, keyValuePairs);
            addWriteKeyValuePairs(keyValuePairs);
            if (MongoLogger.isRequestSizeMeasured())
            {
                metric.setProperty(CommonMetricProperties.REQUEST_SIZE_BYTES, Integer.toString(measureDocumentSize(arg1)));
            }
            addWriteConcern(metric);
            addReadConcernAndPreference(metric);
        }

        TDocument retVal = collection.findOneAndReplace(filter, arg1, arg2);

        stopMetric(metric, measureDocumentSizeIfResultSizeEnabled(retVal));

        return retVal;
    }

    @Override
    public TDocument findOneAndUpdate(Bson filter, Bson arg1)
    {
        OperationMetric metric = null;
        if (MongoLogger.GATHERER.isEnabled())
        {
            List<String> keyValuePairs = MongoUtilities.getKeyValuePairs(filter);
            keyValuePairs.add("update");
            keyValuePairs.add(CacheUtilities.safeToString(arg1));
            String operationName = "Mongo : " + getNamespace().getCollectionName() + " : findOneAndUpdate " +
                MongoUtilities.filterParameters(filter).toString();
            metric = startMetric(operationName, keyValuePairs);
            if (MongoLogger.isRequestSizeMeasured())
            {
                metric.setProperty(CommonMetricProperties.REQUEST_SIZE_BYTES, Integer.toString(measureDocumentSize(arg1)));
            }
            addWriteConcern(metric);
            addReadConcernAndPreference(metric);
        }

        TDocument retVal = collection.findOneAndUpdate(filter, arg1);

        stopMetric(metric, measureDocumentSizeIfResultSizeEnabled(retVal));

        return retVal;
    }

    @Override
    public TDocument findOneAndUpdate(Bson filter, Bson arg1, FindOneAndUpdateOptions arg2)
    {
        OperationMetric metric = null;
        if (MongoLogger.GATHERER.isEnabled())
        {
            List<String> keyValuePairs = MongoUtilities.getKeyValuePairs(filter);
            keyValuePairs.add("update");
            keyValuePairs.add(CacheUtilities.safeToString(arg1));
            String operationName = "Mongo : " + getNamespace().getCollectionName() + " : findOneAndUpdate " +
                MongoUtilities.filterParameters(filter).toString();
            metric = startMetric(operationName, keyValuePairs);
            if (MongoLogger.isRequestSizeMeasured())
            {
                metric.setProperty(CommonMetricProperties.REQUEST_SIZE_BYTES, Integer.toString(measureDocumentSize(arg1)));
            }
            addWriteConcern(metric);
            addReadConcernAndPreference(metric);
        }

        TDocument retVal = collection.findOneAndUpdate(filter, arg1, arg2);

        stopMetric(metric, measureDocumentSizeIfResultSizeEnabled(retVal));

        return retVal;
    }

    @Override
    public CodecRegistry getCodecRegistry()
    {
        return collection.getCodecRegistry();
    }

    @Override
    public Class<TDocument> getDocumentClass()
    {
        return collection.getDocumentClass();
    }

    @Override
    public MongoNamespace getNamespace()
    {
        return collection.getNamespace();
    }

    @Override
    public ReadConcern getReadConcern()
    {
        return collection.getReadConcern();
    }

    @Override
    public ReadPreference getReadPreference()
    {
        return collection.getReadPreference();
    }

    @Override
    public WriteConcern getWriteConcern()
    {
        return collection.getWriteConcern();
    }

    @Override
    public void insertMany(List<? extends TDocument> arg0)
    {
        int writeSize = 0;
        OperationMetric metric = null;
        if (MongoLogger.GATHERER.isEnabled())
        {
            List<String> keyValuePairs = createWriteKeyValuePairs();
            String operationName = "Mongo : " + getNamespace().getCollectionName() + " : insertMany";
            metric = startMetric(operationName, keyValuePairs);
            metric.setProperty(CommonMetricProperties.REQUEST_OBJECT_COUNT, Integer.toString(arg0.size()));
            addWriteConcern(metric);

            if (MongoLogger.isRequestSizeMeasured())
            {
                writeSize = arg0.toString().length();
                metric.setProperty(CommonMetricProperties.REQUEST_SIZE_BYTES, Integer.toString(writeSize));
            }
        }

        collection.insertMany(arg0);

        stopMetric(metric, 0);
    }

    private static AtomicLong UNIQUE_ID = new AtomicLong();

    private List<String> createWriteKeyValuePairs()
    {
        List<String> retVal = new ArrayList<String>();
        addWriteKeyValuePairs(retVal);
        return retVal;
    }

    private void addWriteKeyValuePairs(List<String> kvpList)
    {
        kvpList.add("unique");
        kvpList.add(Long.toString(UNIQUE_ID.incrementAndGet()));
    }

    @Override
    public void insertMany(List<? extends TDocument> arg0, InsertManyOptions arg1)
    {
        int writeSize = 0;
        OperationMetric metric = null;
        if (MongoLogger.GATHERER.isEnabled())
        {
            List<String> keyValuePairs = createWriteKeyValuePairs();
            String operationName = "Mongo : " + getNamespace().getCollectionName() + " : insertMany";
            metric = startMetric(operationName, keyValuePairs);
            metric.setProperty(CommonMetricProperties.REQUEST_OBJECT_COUNT, Integer.toString(arg0.size()));
            addWriteConcern(metric);

            if (MongoLogger.isRequestSizeMeasured())
            {
                writeSize = arg0.toString().length();
                metric.setProperty(CommonMetricProperties.REQUEST_SIZE_BYTES, Integer.toString(writeSize));
            }
        }

        collection.insertMany(arg0, arg1);

        stopMetric(metric, writeSize);
    }

    @Override
    public void insertOne(TDocument arg0)
    {
        int writeSize = 0;
        OperationMetric metric = null;
        if (MongoLogger.GATHERER.isEnabled())
        {
            List<String> keyValuePairs = createWriteKeyValuePairs();
            String operationName = "Mongo : " + getNamespace().getCollectionName() + " : insertOne";
            metric = startMetric(operationName, keyValuePairs);
            addWriteConcern(metric);

            if (MongoLogger.isResultSetSizeMeasured())
            {
                writeSize = arg0.toString().length();
            }
        }

        collection.insertOne(arg0);

        stopMetric(metric, writeSize);
    }

    @Override
    public void insertOne(TDocument arg0, InsertOneOptions arg1)
    {
        int writeSize = 0;
        OperationMetric metric = null;
        if (MongoLogger.GATHERER.isEnabled())
        {
            List<String> keyValuePairs = createWriteKeyValuePairs();
            String operationName = "Mongo : " + getNamespace().getCollectionName() + " : insertOne";
            metric = startMetric(operationName, keyValuePairs);
            addWriteConcern(metric);

            if (MongoLogger.isResultSetSizeMeasured())
            {
                writeSize = arg0.toString().length();
            }
        }

        collection.insertOne(arg0, arg1);

        stopMetric(metric, writeSize);
    }

    @Override
    public ListIndexesIterable<Document> listIndexes()
    {
        return collection.listIndexes();
    }

    @Override
    public <TResult> ListIndexesIterable<TResult> listIndexes(Class<TResult> arg0)
    {
        return collection.listIndexes(arg0);
    }

    @Override
    public MapReduceIterable<TDocument> mapReduce(String mapFunction, String reduceFunction)
    {
        MapReduceIterable<TDocument> mapReduce = collection.mapReduce(mapFunction, reduceFunction);

        ProfiledMapReduceIterable<TDocument, TDocument> profiledMapReduce = new ProfiledMapReduceIterable<TDocument, TDocument>(
            mapFunction, reduceFunction, mapReduce, this);

        return profiledMapReduce;
    }

    @Override
    public <TResult> MapReduceIterable<TResult> mapReduce(String mapFunction, String reduceFunction,
        Class<TResult> arg2)
    {
        MapReduceIterable<TResult> mapReduce = collection.mapReduce(mapFunction, reduceFunction, arg2);

        ProfiledMapReduceIterable<TDocument, TResult> profiledMapReduce = new ProfiledMapReduceIterable<TDocument, TResult>(
            mapFunction, reduceFunction, mapReduce, this);

        return profiledMapReduce;
    }

    @Override
    public void renameCollection(MongoNamespace arg0)
    {
        collection.renameCollection(arg0);
    }

    @Override
    public void renameCollection(MongoNamespace arg0, RenameCollectionOptions arg1)
    {
        collection.renameCollection(arg0, arg1);
    }

    @Override
    public UpdateResult replaceOne(Bson filter, TDocument arg1)
    {
        int writeSize = 0;
        OperationMetric metric = null;
        if (MongoLogger.GATHERER.isEnabled())
        {
            List<String> keyValuePairs = MongoUtilities.getKeyValuePairs(filter);
            addWriteKeyValuePairs(keyValuePairs);
            String operationName = "Mongo : " + getNamespace().getCollectionName() + " : replaceOne : " +
                MongoUtilities.filterParameters(filter);
            metric = startMetric(operationName, keyValuePairs);
            addWriteConcern(metric);

            if (MongoLogger.isResultSetSizeMeasured())
            {
                writeSize = arg1.toString().length();
            }
        }

        UpdateResult retVal = collection.replaceOne(filter, arg1);

        insertUpdateResultProperties(metric, retVal);

        stopMetric(metric, writeSize);

        return retVal;

    }

    @Override
    public UpdateResult replaceOne(Bson filter, TDocument arg1, UpdateOptions arg2)
    {
        int writeSize = 0;
        OperationMetric metric = null;
        if (MongoLogger.GATHERER.isEnabled())
        {
            List<String> keyValuePairs = MongoUtilities.getKeyValuePairs(filter);
            addWriteKeyValuePairs(keyValuePairs);
            String operationName = "Mongo : " + getNamespace().getCollectionName() + " : replaceOne : " +
                MongoUtilities.filterParameters(filter);
            metric = startMetric(operationName, keyValuePairs);
            addWriteConcern(metric);

            if (MongoLogger.isResultSetSizeMeasured())
            {
                writeSize = arg1.toString().length();
            }
        }

        UpdateResult retVal = collection.replaceOne(filter, arg1, arg2);

        insertUpdateResultProperties(metric, retVal);

        stopMetric(metric, writeSize);

        return retVal;
    }

    private void insertUpdateResultProperties(OperationMetric metric, UpdateResult retVal)
    {
        if (metric != null)
        {
            metric.setProperty(MongoProperties.MONGO_UPDATE_MATCHED_COUNT, Long.toString(retVal.getMatchedCount()));
            metric.setProperty(MongoProperties.MONGO_UPDATE_MODIFIED_COUNT, Long.toString(retVal.getModifiedCount()));
        }
    }

    @Override
    public UpdateResult updateMany(Bson filter, Bson arg1)
    {
        int writeSize = 0;
        OperationMetric metric = null;
        if (MongoLogger.GATHERER.isEnabled())
        {
            List<String> keyValuePairs = MongoUtilities.getKeyValuePairs(filter);
            keyValuePairs.add("update");
            keyValuePairs.add(arg1.toString());
            String operationName = "Mongo : " + getNamespace().getCollectionName() + " : updateMany : " +
                MongoUtilities.filterParameters(filter);
            metric = startMetric(operationName, keyValuePairs);
            addWriteConcern(metric);
        }

        UpdateResult retVal = collection.updateMany(filter, arg1);

        insertUpdateResultProperties(metric, retVal);

        stopMetric(metric, writeSize);

        return retVal;
    }

    @Override
    public UpdateResult updateMany(Bson filter, Bson arg1, UpdateOptions arg2)
    {
        int writeSize = 0;
        OperationMetric metric = null;
        if (MongoLogger.GATHERER.isEnabled())
        {
            List<String> keyValuePairs = MongoUtilities.getKeyValuePairs(filter);
            keyValuePairs.add("update");
            keyValuePairs.add(arg1.toString());
            String operationName = "Mongo : " + getNamespace().getCollectionName() + " : updateMany : " +
                MongoUtilities.filterParameters(filter);
            metric = startMetric(operationName, keyValuePairs);
            addWriteConcern(metric);
        }

        UpdateResult retVal = collection.updateMany(filter, arg1, arg2);

        insertUpdateResultProperties(metric, retVal);

        stopMetric(metric, writeSize);

        return retVal;
    }

    @Override
    public UpdateResult updateOne(Bson filter, Bson arg1)
    {
        int writeSize = 0;
        OperationMetric metric = null;
        if (MongoLogger.GATHERER.isEnabled())
        {
            List<String> keyValuePairs = MongoUtilities.getKeyValuePairs(filter);
            keyValuePairs.add("update");
            keyValuePairs.add(arg1.toString());
            String operationName = "Mongo : " + getNamespace().getCollectionName() + " : updateOne : " +
                MongoUtilities.filterParameters(filter);
            metric = startMetric(operationName, keyValuePairs);
            addWriteConcern(metric);
        }

        UpdateResult retVal = collection.updateOne(filter, arg1);

        insertUpdateResultProperties(metric, retVal);

        stopMetric(metric, writeSize);

        return retVal;
    }

    @Override
    public UpdateResult updateOne(Bson filter, Bson arg1, UpdateOptions arg2)
    {
        int writeSize = 0;
        OperationMetric metric = null;
        if (MongoLogger.GATHERER.isEnabled())
        {
            List<String> keyValuePairs = MongoUtilities.getKeyValuePairs(filter);
            keyValuePairs.add("update");
            keyValuePairs.add(arg1.toString());
            String operationName = "Mongo : " + getNamespace().getCollectionName() + " : updateOne : " +
                MongoUtilities.filterParameters(filter);
            metric = startMetric(operationName, keyValuePairs);
            addWriteConcern(metric);
        }

        UpdateResult retVal = collection.updateOne(filter, arg1, arg2);

        insertUpdateResultProperties(metric, retVal);

        stopMetric(metric, writeSize);

        return retVal;
    }

    @Override
    public MongoCollection<TDocument> withCodecRegistry(CodecRegistry arg0)
    {
        return new ProfiledMongoCollection<TDocument>(collection.withCodecRegistry(arg0));
    }

    @Override
    public <NewTDocument> MongoCollection<NewTDocument> withDocumentClass(Class<NewTDocument> arg0)
    {
        return new ProfiledMongoCollection<NewTDocument>(collection.withDocumentClass(arg0));
    }

    @Override
    public MongoCollection<TDocument> withReadConcern(ReadConcern arg0)
    {
        return new ProfiledMongoCollection<TDocument>(collection.withReadConcern(arg0));
    }

    @Override
    public MongoCollection<TDocument> withReadPreference(ReadPreference arg0)
    {
        return new ProfiledMongoCollection<TDocument>(collection.withReadPreference(arg0));
    }

    @Override
    public MongoCollection<TDocument> withWriteConcern(WriteConcern arg0)
    {
        return new ProfiledMongoCollection<TDocument>(collection.withWriteConcern(arg0));
    }

    public void addReadConcernAndPreference(OperationMetric metric)
    {

        if (!getReadConcern().isServerDefault())
        {
            metric.setProperty(MongoProperties.MONGO_READ_CONCERN, getReadConcern().asDocument().toJson());
        }
        metric.setProperty(MongoProperties.MONGO_READ_PREFERENCE, getReadPreference().getName());

    }

    public void addWriteConcern(OperationMetric metric)
    {

        if (!getWriteConcern().isServerDefault())
        {
            metric.setProperty(MongoProperties.MONGO_WRITE_CONCERN, getWriteConcern().asDocument().toJson());
        }
    }
}
