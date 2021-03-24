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

import java.util.Arrays;
import java.util.List;

import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.conversions.Bson;

import com.ibm.commerce.cache.CacheUtilities;
import com.ibm.commerce.cache.OperationMetric;
import com.ibm.service.detailed.MongoLogger;
import com.mongodb.ReadConcern;
import com.mongodb.ReadPreference;
import com.mongodb.WriteConcern;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.ChangeStreamIterable;
import com.mongodb.client.ClientSession;
import com.mongodb.client.ListCollectionsIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoIterable;
import com.mongodb.client.model.CreateCollectionOptions;
import com.mongodb.client.model.CreateViewOptions;

/**
 * ProfiledMongoDatabase
 * 
 * @author Steve McDuff
 */
public class ProfiledMongoDatabase implements MongoDatabase
{
    private final MongoDatabase database;

    public ProfiledMongoDatabase(MongoDatabase database)
    {
        this.database = database;
    }

    @Override
    public String getName()
    {
        return database.getName();
    }

    @Override
    public CodecRegistry getCodecRegistry()
    {
        return database.getCodecRegistry();
    }

    @Override
    public ReadPreference getReadPreference()
    {
        return database.getReadPreference();
    }

    @Override
    public WriteConcern getWriteConcern()
    {
        return database.getWriteConcern();
    }

    @Override
    public ReadConcern getReadConcern()
    {
        return database.getReadConcern();
    }

    @Override
    public MongoDatabase withCodecRegistry(CodecRegistry codecRegistry)
    {
        return new ProfiledMongoDatabase(database.withCodecRegistry(codecRegistry));
    }

    @Override
    public MongoDatabase withReadPreference(ReadPreference readPreference)
    {
        return new ProfiledMongoDatabase(database.withReadPreference(readPreference));
    }

    @Override
    public MongoDatabase withWriteConcern(WriteConcern writeConcern)
    {
        return new ProfiledMongoDatabase(database.withWriteConcern(writeConcern));
    }

    @Override
    public MongoDatabase withReadConcern(ReadConcern readConcern)
    {
        return new ProfiledMongoDatabase(database.withReadConcern(readConcern));
    }

    @Override
    public MongoCollection<Document> getCollection(String collectionName)
    {
        MongoCollection<Document> collection = database.getCollection(collectionName);
        return new ProfiledMongoCollection<Document>(collection);
    }

    @Override
    public <TDocument> MongoCollection<TDocument> getCollection(String collectionName, Class<TDocument> documentClass)
    {
        MongoCollection<TDocument> collection = database.getCollection(collectionName, documentClass);
        return new ProfiledMongoCollection<TDocument>(collection);
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
        addDatabaseInformation(metric);

        MongoLogger.GATHERER.gatherMetricEntryLog(metric);

        return metric;
    }

    public void addDatabaseInformation(OperationMetric metric)
    {
        metric.setProperty(MongoProperties.MONGO_DATABASE, getName());
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
    public Document runCommand(Bson command)
    {
        OperationMetric metric = null;

        if (MongoLogger.GATHERER.isEnabled())
        {
            metric = startMetric("Mongo : runCommand", Arrays.asList("command", command.toString()));
        }
        Document runCommand = database.runCommand(command);
        int resultSize = 0;
        if (MongoLogger.isResultSetSizeMeasured())
        {
            resultSize = CacheUtilities.safeToString(runCommand).length();
        }
        stopMetric(metric, resultSize);
        return runCommand;
    }

    @Override
    public Document runCommand(Bson command, ReadPreference readPreference)
    {
        OperationMetric metric = null;

        if (MongoLogger.GATHERER.isEnabled())
        {
            metric = startMetric("Mongo : runCommand", Arrays.asList("command", command.toString()));
        }
        Document runCommand = database.runCommand(command, readPreference);
        int resultSize = 0;
        if (MongoLogger.isResultSetSizeMeasured())
        {
            resultSize = CacheUtilities.safeToString(runCommand).length();
        }
        stopMetric(metric, resultSize);
        return runCommand;
    }

    @Override
    public <TResult> TResult runCommand(Bson command, Class<TResult> resultClass)
    {
        OperationMetric metric = null;

        if (MongoLogger.GATHERER.isEnabled())
        {
            metric = startMetric("Mongo : runCommand", Arrays.asList("command", command.toString()));
        }
        TResult runCommand = database.runCommand(command, resultClass);
        int resultSize = 0;
        if (MongoLogger.isResultSetSizeMeasured())
        {
            resultSize = CacheUtilities.safeToString(runCommand).length();
        }
        stopMetric(metric, resultSize);
        return runCommand;
    }

    @Override
    public <TResult> TResult runCommand(Bson command, ReadPreference readPreference, Class<TResult> resultClass)
    {
        OperationMetric metric = null;

        if (MongoLogger.GATHERER.isEnabled())
        {
            metric = startMetric("Mongo : runCommand", Arrays.asList("command", command.toString()));
        }
        TResult runCommand = database.runCommand(command, readPreference, resultClass);
        int resultSize = 0;
        if (MongoLogger.isResultSetSizeMeasured())
        {
            resultSize = CacheUtilities.safeToString(runCommand).length();
        }
        stopMetric(metric, resultSize);
        return runCommand;
    }
    
	@Override
	public Document runCommand(ClientSession clientSession, Bson command) {
        OperationMetric metric = null;

        if (MongoLogger.GATHERER.isEnabled())
        {
            metric = startMetric("Mongo : runCommand", Arrays.asList("command", command.toString()));
        }
        Document runCommand = database.runCommand(clientSession, command);
        int resultSize = 0;
        if (MongoLogger.isResultSetSizeMeasured())
        {
            resultSize = CacheUtilities.safeToString(runCommand).length();
        }
        stopMetric(metric, resultSize);
        return runCommand;
	}

	@Override
	public Document runCommand(ClientSession clientSession, Bson command, ReadPreference readPreference) {
        OperationMetric metric = null;

        if (MongoLogger.GATHERER.isEnabled())
        {
            metric = startMetric("Mongo : runCommand", Arrays.asList("command", command.toString()));
        }
        Document runCommand = database.runCommand(clientSession, command, readPreference);
        int resultSize = 0;
        if (MongoLogger.isResultSetSizeMeasured())
        {
            resultSize = CacheUtilities.safeToString(runCommand).length();
        }
        stopMetric(metric, resultSize);
        return runCommand;
	}

	@Override
	public <TResult> TResult runCommand(ClientSession clientSession, Bson command, Class<TResult> resultClass) {
        OperationMetric metric = null;

        if (MongoLogger.GATHERER.isEnabled())
        {
            metric = startMetric("Mongo : runCommand", Arrays.asList("command", command.toString()));
        }
        TResult runCommand = database.runCommand(clientSession, command, resultClass);
        int resultSize = 0;
        if (MongoLogger.isResultSetSizeMeasured())
        {
            resultSize = CacheUtilities.safeToString(runCommand).length();
        }
        stopMetric(metric, resultSize);
        return runCommand;
	}

	@Override
	public <TResult> TResult runCommand(ClientSession clientSession, Bson command, ReadPreference readPreference,
			Class<TResult> resultClass) {
        OperationMetric metric = null;

        if (MongoLogger.GATHERER.isEnabled())
        {
            metric = startMetric("Mongo : runCommand", Arrays.asList("command", command.toString()));
        }
        TResult runCommand = database.runCommand(clientSession, command, readPreference, resultClass);
        int resultSize = 0;
        if (MongoLogger.isResultSetSizeMeasured())
        {
            resultSize = CacheUtilities.safeToString(runCommand).length();
        }
        stopMetric(metric, resultSize);
        return runCommand;
	}
    

    @Override
    public void drop()
    {
        database.drop();
    }

	@Override
	public void drop(ClientSession clientSession) {
		database.drop(clientSession);
	}

    
    @Override
    public MongoIterable<String> listCollectionNames()
    {
        return database.listCollectionNames();
    }
    
	@Override
	public MongoIterable<String> listCollectionNames(ClientSession clientSession) {
		 return database.listCollectionNames(clientSession);
	}


    @Override
    public ListCollectionsIterable<Document> listCollections()
    {
        return database.listCollections();
    }
    
	@Override
	public ListCollectionsIterable<Document> listCollections(ClientSession clientSession) {
		return database.listCollections(clientSession);
	}


    @Override
    public <TResult> ListCollectionsIterable<TResult> listCollections(Class<TResult> resultClass)
    {
        return database.listCollections(resultClass);
    }

	@Override
	public <TResult> ListCollectionsIterable<TResult> listCollections(ClientSession clientSession,
			Class<TResult> resultClass) {
		return database.listCollections(clientSession, resultClass);
	}

    
    @Override
    public void createCollection(String collectionName)
    {
        database.createCollection(collectionName);
    }

	@Override
	public void createCollection(ClientSession clientSession, String collectionName) {
		database.createCollection(clientSession, collectionName);
	}
    
    @Override
    public void createCollection(String collectionName, CreateCollectionOptions createCollectionOptions)
    {
        database.createCollection(collectionName, createCollectionOptions);
    }
    
	@Override
	public void createCollection(ClientSession clientSession, String collectionName,
			CreateCollectionOptions createCollectionOptions) {
		database.createCollection(clientSession, collectionName, createCollectionOptions);
	}
    

    @Override
    public void createView(String viewName, String viewOn, List<? extends Bson> pipeline)
    {
        database.createView(viewName, viewOn, pipeline);
    }

    @Override
    public void createView(String viewName, String viewOn, List<? extends Bson> pipeline,
        CreateViewOptions createViewOptions)
    {
        database.createView(viewName, viewOn, pipeline, createViewOptions);
    }


	@Override
	public void createView(ClientSession clientSession, String viewName, String viewOn, List<? extends Bson> pipeline) {
		database.createView(clientSession, viewName, viewOn, pipeline);
	}

	@Override
	public void createView(ClientSession clientSession, String viewName, String viewOn, List<? extends Bson> pipeline,
			CreateViewOptions createViewOptions) {
		database.createView(clientSession, viewName, viewOn, pipeline, createViewOptions);
	}



	@Override
	public ChangeStreamIterable<Document> watch() {
		return database.watch();
	}

	@Override
	public <TResult> ChangeStreamIterable<TResult> watch(Class<TResult> resultClass) {
		return database.watch(resultClass);
	}

	@Override
	public ChangeStreamIterable<Document> watch(List<? extends Bson> pipeline) {
		return database.watch(pipeline);
	}

	@Override
	public <TResult> ChangeStreamIterable<TResult> watch(List<? extends Bson> pipeline, Class<TResult> resultClass) {
		return database.watch(resultClass);
	}

	@Override
	public ChangeStreamIterable<Document> watch(ClientSession clientSession) {
		return database.watch(clientSession);
	}

	@Override
	public <TResult> ChangeStreamIterable<TResult> watch(ClientSession clientSession, Class<TResult> resultClass) {
		return database.watch(clientSession, resultClass);
	}

	@Override
	public ChangeStreamIterable<Document> watch(ClientSession clientSession, List<? extends Bson> pipeline) {
		return database.watch(clientSession, pipeline);
	}

	@Override
	public <TResult> ChangeStreamIterable<TResult> watch(ClientSession clientSession, List<? extends Bson> pipeline,
			Class<TResult> resultClass) {
		return database.watch(clientSession, pipeline, resultClass);
	}

	@Override
	public AggregateIterable<Document> aggregate(List<? extends Bson> pipeline) {
		return database.aggregate(pipeline);
	}

	@Override
	public <TResult> AggregateIterable<TResult> aggregate(List<? extends Bson> pipeline, Class<TResult> resultClass) {
		return database.aggregate(pipeline, resultClass);
	}

	@Override
	public AggregateIterable<Document> aggregate(ClientSession clientSession, List<? extends Bson> pipeline) {
		return database.aggregate(clientSession, pipeline);
	}

	@Override
	public <TResult> AggregateIterable<TResult> aggregate(ClientSession clientSession, List<? extends Bson> pipeline,
			Class<TResult> resultClass) {
		return database.aggregate(clientSession, pipeline, resultClass);
	}
}
