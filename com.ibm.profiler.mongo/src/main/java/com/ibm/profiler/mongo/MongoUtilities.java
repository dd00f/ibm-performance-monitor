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

    private static BsonString MULTIPLE_QUESTION_MARK_BSON = new BsonString("*?");

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

            String key = entry.getKey();
            BsonValue value = entry.getValue();
            if (isParameterlessOperationName(key))
            {
                // nothing to do.
            }
            else if (isArrayOperationName(key) && value instanceof BsonArray)
            {
                BsonArray bsonArray = new BsonArray();
                bsonArray.add(MULTIPLE_QUESTION_MARK_BSON);
                entry.setValue(bsonArray);
            }
            else
            {
                entry.setValue(filterValue(value));
            }
        }

        return returnValue;
    }

    private static boolean isParameterlessOperationName(String key)
    {
        return "$lookup".equalsIgnoreCase(key) || "$group".equalsIgnoreCase(key);
    }

    private static boolean isArrayOperationName(String key)
    {
        return "$in".equalsIgnoreCase(key) || "$nin".equalsIgnoreCase(key) || "$all".equalsIgnoreCase(key);
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
        BsonDocument bsonDocument = filter.toBsonDocument(BsonDocument.class, MongoClient.getDefaultCodecRegistry());
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
