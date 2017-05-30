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
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

import org.bson.BsonArray;
import org.bson.BsonDocument;
import org.bson.BsonString;
import org.bson.BsonValue;
import org.bson.conversions.Bson;

import com.ibm.commerce.cache.CacheUtilities;
import com.mongodb.MongoClient;
import com.mongodb.client.model.IndexModel;

/**
 * MongoUtilities
 * 
 * @author Steve McDuff
 */
public class MongoUtilities
{
    public MongoUtilities()
    {
        super();
    }

    private static BsonString QUESTION_MARK_BSON = new BsonString("?");

    public static List<BsonDocument> filterParameters(List<? extends Bson> documentListToFilter)
    {
        List<BsonDocument> returnValue = new ArrayList<BsonDocument>();

        for (Bson bsonDocument : documentListToFilter)
        {
            BsonDocument bsonDoc = bsonDocument.toBsonDocument(BsonDocument.class,
                MongoClient.getDefaultCodecRegistry());
            returnValue.add(filterParameters(bsonDoc));
        }

        return returnValue;
    }

    public static BsonDocument filterParameters(BsonDocument documentToFilter)
    {
        BsonDocument returnValue = documentToFilter.clone();

        Set<Entry<String, BsonValue>> entrySet = returnValue.entrySet();
        for (Entry<String, BsonValue> entry : entrySet)
        {
            BsonValue value = entry.getValue();
            entry.setValue(filterValue(value));
        }

        return returnValue;
    }

    public static BsonValue filterValue(BsonValue value)
    {
        BsonValue returnedValue = QUESTION_MARK_BSON;
        if (value instanceof BsonDocument)
        {
            returnedValue = filterParameters((BsonDocument) value);
        }
        else if (value instanceof BsonArray)
        {
            BsonArray array = (BsonArray) value;
            array = array.clone();
            returnedValue = array;
            int length = array.size();
            for (int i = 0; i < length; ++i)
            {
                BsonValue bsonValue = array.get(i);
                array.set(i, filterValue(bsonValue));
            }
        }
        return returnedValue;
    }

    public static List<String> getKeyValuePairs(Bson document)
    {
        List<String> values = new ArrayList<String>();
        addKeyValuePairs(document, values);

        return values;
    }

    public static List<String> getKeyValuePairs(List<? extends Bson> arg0)
    {
        List<String> values = new ArrayList<String>();

        for (Bson bson : arg0)
        {
            addKeyValuePairs(bson, values);

        }

        return values;
    }

    public static void addKeyValuePairs(Bson document, List<String> values)
    {
        if (document != null)
        {
            BsonDocument bsonDocument = document.toBsonDocument(BsonDocument.class,
                MongoClient.getDefaultCodecRegistry());

            addKeyValuePairs(values, bsonDocument);
        }
    }

    private static void addKeyValuePairs(List<String> values, BsonDocument bsonDocument)
    {
        Set<Entry<String, BsonValue>> entrySet = bsonDocument.entrySet();
        for (Entry<String, BsonValue> entry : entrySet)
        {
            values.add(CacheUtilities.safeToString(entry.getKey()));
            values.add(CacheUtilities.safeToString(entry.getValue()));
        }
    }

    public static BsonDocument filterParameters(Bson filter)
    {
        BsonDocument bsonDocument = filter.toBsonDocument(BsonDocument.class,
            MongoClient.getDefaultCodecRegistry());
        return filterParameters(bsonDocument);
    }

    public static List<String> getIndexModelKeyValuePairs(List<IndexModel> arg0)
    {
        List<String> retVal = new ArrayList<String>();
        for (IndexModel indexModel : arg0)
        {
            retVal.add(indexModel.getKeys().toString());
        }
        return retVal;
    }
}
