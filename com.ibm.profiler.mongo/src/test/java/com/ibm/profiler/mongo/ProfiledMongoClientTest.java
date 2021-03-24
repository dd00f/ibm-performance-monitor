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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.bson.Document;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.ibm.commerce.cache.LogMetricGathererManager;
import com.ibm.commerce.cache.log4j.Log4JMetricGathererFactory;
import com.ibm.service.detailed.MongoLogger;
import com.mongodb.CursorType;
import com.mongodb.Function;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.ReadPreference;
import com.mongodb.client.ClientSession;
import com.mongodb.client.DistinctIterable;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MapReduceIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoIterable;
import com.mongodb.client.model.Accumulators;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.BulkWriteOptions;
import com.mongodb.client.model.Collation;
import com.mongodb.client.model.CountOptions;
import com.mongodb.client.model.DeleteOptions;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.FindOneAndDeleteOptions;
import com.mongodb.client.model.FindOneAndReplaceOptions;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.IndexModel;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.InsertManyOptions;
import com.mongodb.client.model.InsertOneModel;
import com.mongodb.client.model.InsertOneOptions;
import com.mongodb.client.model.ReplaceOptions;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.WriteModel;

/**
 * ProfiledMongoClientTest
 * 
 * @author Steve McDuff
 */
public class ProfiledMongoClientTest
{
    public static ProfiledMongoClient client;
    public static MongoClient m1;
    public static MongoDatabase db;
    public static MongoCollection<Document> coll;
    public static TestLogMetricGatherer gatherer;
	private static ClientSession startSession;

    static
    {
        System.setProperty(LogMetricGathererManager.GATHERER_FACTORY_CLASS_NAME_PROPERTY,
            TestMetricGathererFactory.class.getName());

        Log4JMetricGathererFactory alternative = new Log4JMetricGathererFactory();
        alternative.getClass();
        // System.setProperty(LogMetricGathererManager.GATHERER_FACTORY_CLASS_NAME_PROPERTY,
        // Log4JMetricGathererFactory.class.getName());
    }

    public ProfiledMongoClientTest()
    {
        super();
    }

    @AfterClass
    public static void tearDown() throws InterruptedException
    {

        coll.drop();

        db.drop();
    }

    @BeforeClass
    public static void setup()
    {
        String mongoUrl = "mongodb://localhost:27017";

        try
        {
            Properties prop = new Properties();
            prop.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("mongo.properties"));
            mongoUrl = prop.getProperty("mongourl");
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }

        // create an instance of client and establish the connection
        MongoClientURI connectionString = new MongoClientURI(mongoUrl);
        m1 = new MongoClient(connectionString);
        client = new ProfiledMongoClient(m1);

        db = client.getDatabase("testMongo");

        coll = db.getCollection("car");

        coll.insertOne(Document.parse(
            "{\"car_id\":\"c1\",\"name\":\"Audi\",\"color\":\"Black\",\"cno\":\"H110\",\"mfdcountry\":\"Germany\",\"speed\":72,\"price\":11.25}"));
        coll.insertOne(Document.parse(
            "{\"car_id\":\"c2\",\"name\":\"Polo\",\"color\":\"White\",\"cno\":\"H111\",\"mfdcountry\":\"Japan\",\"speed\":65,\"price\":8.5}"));
        coll.insertOne(Document.parse(
            "{\"car_id\":\"c3\",\"name\":\"Alto\",\"color\":\"Silver\",\"cno\":\"H112\",\"mfdcountry\":\"India\",\"speed\":53,\"price\":4.5}"));
        coll.insertOne(
            Document.parse(
                "{\"car_id\":\"c4\",\"name\":\"Santro\",\"color\":\"Grey\",\"cno\":\"H113\",\"mfdcountry\":\"Sweden\",\"speed\":89,\"price\":3.5}"),
            new InsertOneOptions());

        gatherer = (TestLogMetricGatherer) MongoLogger.GATHERER;

//        assertEquals("Mongo : car : insertOne", gatherer.getLastMetric().getOperationName());
//        assertEquals("car", gatherer.getLastMetric().getProperty(MongoProperties.MONGO_COLLECTION));
//        assertEquals("testMongo", gatherer.getLastMetric().getProperty(MongoProperties.MONGO_DATABASE));

        coll.insertMany(Arrays.asList(
            Document.parse(
                "{\"car_id\":\"c5\",\"name\":\"Zen\",\"color\":\"Blue\",\"cno\":\"H114\",\"mfdcountry\":\"Denmark\",\"speed\":94,\"price\":6.5}"),
            Document.parse(
                "{\"car_id\":\"c6\",\"name\":\"Alto\",\"color\":\"Blue\",\"cno\":\"H115\",\"mfdcountry\":\"India\",\"speed\":53,\"price\":4.5}")));

        coll.insertMany(
            Arrays.asList(
                Document.parse(
                    "{\"car_id\":\"c6\",\"name\":\"Alto\",\"color\":\"White\",\"cno\":\"H115\",\"mfdcountry\":\"India\",\"speed\":53,\"price\":4.5}"),
                Document.parse(
                    "{\"car_id\":\"c6\",\"name\":\"Alto\",\"color\":\"Red\",\"cno\":\"H115\",\"mfdcountry\":\"India\",\"speed\":53,\"price\":4.5}")),
            new InsertManyOptions());

        startSession = client.startSession();
    }

    
    @Test
    public void runCommand()
    {
        Document runCommand = db.runCommand(new Document("buildInfo", 1));
        runCommand = db.runCommand(new Document("buildInfo", 1), Document.class);
        runCommand = db.runCommand(new Document("buildInfo", 1), ReadPreference.primary());
        runCommand = db.runCommand(new Document("buildInfo", 1), ReadPreference.primary(), Document.class);
        
        runCommand = db.runCommand(startSession, new Document("buildInfo", 1));
        runCommand = db.runCommand(startSession,new Document("buildInfo", 1), Document.class);
        runCommand = db.runCommand(startSession,new Document("buildInfo", 1), ReadPreference.primary());
        runCommand = db.runCommand(startSession,new Document("buildInfo", 1), ReadPreference.primary(), Document.class);
        Assert.assertNotNull(runCommand);
    }
    
    @Test
    public void createIndex()
    {
        coll.createIndex(new Document("name", 1));

        coll.dropIndex(new Document("name", 1));

        coll.createIndex(new Document("name", 1), new IndexOptions());

        coll.dropIndex(new Document("name", 1));

        coll.createIndexes(Arrays.asList(new IndexModel(new Document("name", 1))));

        coll.dropIndexes();
        
        coll.createIndex(startSession,new Document("name", 1));

        coll.dropIndex(startSession,new Document("name", 1));

        coll.createIndex(startSession,new Document("name", 1), new IndexOptions());

        coll.dropIndex(startSession,new Document("name", 1));

        coll.createIndexes(startSession,Arrays.asList(new IndexModel(new Document("name", 1))));

        coll.dropIndexes(startSession);
    }

    @Test
    public void bulkWrite()
    {
        List<WriteModel<Document>> list = insertOneWithBulk();

        coll.deleteOne(Filters.eq("name", "DELETEME"));

        coll.bulkWrite(list, new BulkWriteOptions());

        coll.deleteMany(Filters.eq("name", "DELETEME"));
        
        coll.deleteOne(startSession,Filters.eq("name", "DELETEME"));

        coll.bulkWrite(startSession,list, new BulkWriteOptions());

        coll.deleteMany(startSession,Filters.eq("name", "DELETEME"));
    }

    private List<WriteModel<Document>> insertOneWithBulk()
    {
        List<WriteModel<Document>> list = new ArrayList<WriteModel<Document>>();
        list.add(new InsertOneModel<Document>(createDeleteDocument()));
        coll.bulkWrite(list);
        return list;
    }

    private Document createDeleteDocument()
    {
        return Document.parse(
            "{\"car_id\":\"c7\",\"name\":\"DELETEME\",\"color\":\"Red\",\"cno\":\"H116\",\"mfdcountry\":\"India\",\"speed\":53,\"price\":4.5}");
    }

    @Test
    public void findOneAndDelete()
    {
        insertOneWithBulk();

        Assert.assertNotNull(coll.findOneAndDelete(Filters.eq("name", "DELETEME")));

        insertOneWithBulk();

        Assert.assertNotNull(coll.findOneAndDelete(Filters.eq("name", "DELETEME"), new FindOneAndDeleteOptions()));
    }

    @Test
    public void findOneAndReplace()
    {
        insertOneWithBulk();

        Document replace = Document.parse(
            "{\"car_id\":\"c7a\",\"name\":\"DELETEME\",\"color\":\"Redaa\",\"cno\":\"H116aa\",\"mfdcountry\":\"India\",\"speed\":53,\"price\":4.5}");

        Assert.assertNotNull(coll.findOneAndReplace(Filters.eq("name", "DELETEME"), replace));

        insertOneWithBulk();

        Assert.assertNotNull(
            coll.findOneAndReplace(Filters.eq("name", "DELETEME"), replace, new FindOneAndReplaceOptions()));

        coll.deleteMany(Filters.eq("name", "DELETEME"));
    }

    @Test
    public void replaceOne()
    {
        insertOneWithBulk();

        Document replace = Document.parse(
            "{\"car_id\":\"c7a\",\"name\":\"DELETEME\",\"color\":\"Redaa\",\"cno\":\"H116aa\",\"mfdcountry\":\"India\",\"speed\":53,\"price\":4.5}");

        Assert.assertEquals(1, coll.replaceOne(Filters.eq("name", "DELETEME"), replace).getModifiedCount());

        coll.deleteMany(Filters.eq("name", "DELETEME"));

        insertOneWithBulk();

        Assert.assertEquals(1,
            coll.replaceOne(Filters.eq("name", "DELETEME"), replace, new ReplaceOptions()).getModifiedCount());

        coll.deleteMany(Filters.eq("name", "DELETEME"));
    }

    @Test
    public void updateMany()
    {
        insertOneWithBulk();

        Assert.assertEquals(1,
            coll.updateMany(Filters.eq("name", "DELETEME"), new Document("$set", new Document("name", "UDPATED")))
                .getModifiedCount());

        Assert.assertEquals(1, coll.updateMany(Filters.eq("name", "UDPATED"),
            new Document("$set", new Document("name", "AGAIN")), new UpdateOptions()).getModifiedCount());

        coll.deleteMany(Filters.eq("name", "AGAIN"));
    }

    @Test
    public void updateOne()
    {
        insertOneWithBulk();

        Assert.assertEquals(1,
            coll.updateOne(Filters.eq("name", "DELETEME"), new Document("$set", new Document("name", "UDPATED")))
                .getModifiedCount());

        Assert.assertEquals(1, coll.updateOne(Filters.eq("name", "UDPATED"),
            new Document("$set", new Document("name", "AGAIN")), new UpdateOptions()).getModifiedCount());

        coll.deleteMany(Filters.eq("name", "AGAIN"));
    }

    @Test
    public void findOneAndUpdate()
    {
        insertOneWithBulk();

        Assert.assertNotNull(coll.findOneAndUpdate(Filters.eq("name", "DELETEME"),
            new Document("$set", new Document("name", "UDPATED"))));

        Assert.assertNotNull(coll.findOneAndUpdate(Filters.eq("name", "UDPATED"),
            new Document("$set", new Document("name", "AGAIN")), new FindOneAndUpdateOptions()));

        coll.deleteMany(Filters.eq("name", "AGAIN"));
    }

    @Test
    public void deleteMany()
    {
        coll.deleteMany(Filters.eq("name", "JUNK"));
        coll.deleteMany(Filters.eq("name", "JUNK"), new DeleteOptions());
        coll.deleteOne(Filters.eq("name", "JUNK"));
        coll.deleteOne(Filters.eq("name", "JUNK"), new DeleteOptions());
    }

    @Test
    public void testAggregate()
    {
        List<Document> docList = new ArrayList<Document>();
        coll.aggregate(Arrays.asList(Aggregates.match(Filters.eq("name", "Alto")),
            Aggregates.group("color", Accumulators.sum("count", 1)))).into(docList);

        assertEquals(1, docList.size());

        docList.clear();

        Document first = coll
            .aggregate(Arrays.asList(Aggregates.match(Filters.eq("name", "Alto")),
                Aggregates.group("color", Accumulators.sum("count", 1))), Document.class)
            .allowDiskUse(true).batchSize(12).bypassDocumentValidation(true).collation(Collation.builder().build())
            .first();
        Assert.assertNotNull(first);

        first = coll
            .aggregate(Arrays.asList(Aggregates.match(Filters.eq("name", "Alto")),
                Aggregates.group("color", Accumulators.sum("count", 1))), Document.class)
            .allowDiskUse(true).batchSize(12).bypassDocumentValidation(true).collation(Collation.builder().build())
            .map(new Function<Document, Document>()
            {
                @Override
                public Document apply(Document t)
                {
                    t.put("hello", "world");
                    return t;
                }
            }).first();
        Assert.assertNotNull(first);

    }

    @Test
    public void testMap()
    {
        MongoIterable<String> map = createMap();

        List<String> mapResult = new ArrayList<String>();
        map.into(mapResult);

        assertEquals(4, mapResult.size());

        Assert.assertNotNull(createMap().batchSize(213).first());

        createMap().forEach(new Consumer<String>()
        {
            @Override
            public void accept(String t)
            {
                System.out.println(t);
            }
        });

        mapResult.clear();
        createMap().map(new Function<String, String>()
        {
            @Override
            public String apply(String t)
            {
                return t + "hello";
            }
        }).into(mapResult);
        assertEquals(4, mapResult.size());
    }

    public MongoIterable<String> createMap()
    {
        return coll.find(Filters.eq("name", "Alto")).map(new Function<Document, String>()
        {
            @Override
            public String apply(Document t)
            {
                return t.getString("name");
            }
        });
    }

    @SuppressWarnings("deprecation")
	@Test
    public void testFind()
    {
        FindIterable<Document> find = coll.find(Filters.eq("name", "Alto"), Document.class)
            .sort(Sorts.ascending("color"));
        List<Document> docList = toDocumentList(find);
        assertEquals(4, docList.size());

        find = coll.find(Filters.eq("name", "Alto")).sort(Sorts.ascending("color"));
        docList = toDocumentList(find);
        assertEquals(4, docList.size());

        find = coll.find(Document.class).filter(Filters.eq("name", "Alto")).sort(Sorts.ascending("color"));
        docList = toDocumentList(find);
        assertEquals(4, docList.size());

        find = coll.find().filter(Filters.eq("name", "Alto")).sort(Sorts.ascending("color"));
        docList = toDocumentList(find);
        assertEquals(4, docList.size());

        find = coll.find().filter(Filters.eq("name", "Alto")).sort(Sorts.ascending("color")).batchSize(123)
            .collation(Collation.builder().build()).cursorType(CursorType.NonTailable).limit(2)
            .maxAwaitTime(12, TimeUnit.DAYS).maxTime(12, TimeUnit.DAYS).noCursorTimeout(true).oplogReplay(false)
            .partial(false).skip(1);
        docList = toDocumentList(find);
        assertEquals(2, docList.size());

        Document firstFind = coll.find().filter(Filters.eq("name", "Alto")).sort(Sorts.ascending("color")).first();
        Assert.assertNotNull(firstFind);

        coll.find().filter(Filters.eq("name", "Alto")).forEach(new Consumer<Document>()
        {
            @Override
            public void accept(Document t)
            {
                System.out.println(t.get("name"));
            }
        });

    }

    @Test
    public void testCountDocuments()
    {
        assertEquals(8, coll.countDocuments());
        assertEquals(4, coll.countDocuments(Filters.eq("name", "Alto")));
        assertEquals(4, coll.countDocuments(Filters.eq("name", "Alto"), new CountOptions()));
    }

    @Test
    public void testMapReduce()
    {
        // map function to categorize overspeed cars
        String carMap = "function (){" + "var criteria;" + "if ( this.speed > 70 ) {" + "criteria = 'overspeed';" +
            "emit(criteria,this.speed);" + "}" + "};";

        // reduce function to add all the speed and calculate the average speed
        String carReduce = "function(key, speed) {" + "var total =0;" + "for (var i = 0; i < speed.length; i++) {" +
            "total = total+speed[i];" + "}" + "return total/speed.length;" + "};";

        // create the mapreduce command by calling map and reduce functions
        MapReduceIterable<Document> mapReduce = coll.mapReduce(carMap, carReduce);
        for (Document document : mapReduce)
        {
            assertEquals("Document{{_id=overspeed, value=85.0}}", document.toString());
        }

        mapReduce = coll.mapReduce(carMap, carReduce, Document.class);
        for (Document document : mapReduce)
        {
            assertEquals("Document{{_id=overspeed, value=85.0}}", document.toString());
        }
    }

    @Test
    public void testDistinct()
    {
        DistinctIterable<String> distinct = coll.distinct("color", String.class);
        List<String> strList = toDocumentList(distinct);
        assertEquals(6, strList.size());
        assertTrue(strList.contains("Red"));

        distinct = coll.distinct("color", Filters.eq("name", "Alto"), String.class);
        strList = toDocumentList(distinct);
        assertEquals(4, strList.size());
        assertTrue(strList.contains("Red"));
    }

    public static <U> List<U> toDocumentList(MongoIterable<U> iterable)
    {
        List<U> retVal = new ArrayList<U>();

        iterable.into(retVal);
        // for (U document : iterable)
        // {
        // retVal.add(document);
        // }

        return retVal;
    }

}
