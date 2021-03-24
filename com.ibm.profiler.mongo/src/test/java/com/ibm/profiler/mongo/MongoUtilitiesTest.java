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

import static org.junit.Assert.assertEquals;

import org.bson.BsonDocument;
import org.bson.conversions.Bson;
import org.junit.Test;

import com.mongodb.client.model.Accumulators;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;

/**
 * MongoUtilitiesTest
 * 
 * @author Steve McDuff
 */
public class MongoUtilitiesTest
{
    public MongoUtilitiesTest()
    {
        super();
   }

    @Test
    public void testFilterParametersListOfQextendsBson()
    {

        // [
        // {$match: {dateKey:{$in:[ObjectId("593898622313868b72a296ad"), ObjectId("593898622313868b72a296b4"),
        // ObjectId("593898622313868b72a296bf"), ObjectId("593898622313868b72a296c5"),
        // ObjectId("593898622313868b72a296ca"), ObjectId("593898622313868b72a296d3"),
        // ObjectId("593898622313868b72a296d8")]}}},
        // {$lookup: {from: "dimensions", localField: "dateKey", foreignField: "_id", as: "dateCat"}},
        // {$group:{_id:{month:"$dateCat.month", year:"$dateCat.year",
        // day:"$dateCat.dayOfMonth"},documentCount:{$sum:"$docCount"}}}
        // ]

   }

    @Test
    public void testFilterParametersBsonDocumentMatchIn()
    {
        
        Bson match = Aggregates.match(Filters.in("dateKey", "593898622313868b72a296ad", "593898622313868b72a296b4"));
        BsonDocument filterParameters = MongoUtilities.filterParameters(match);
        
        assertEquals("{\"$match\": {\"dateKey\": {\"$in\": [\"*?\"]}}}", filterParameters.toString());

   }
    
    @Test
    public void testFilterParametersBsonDocumentFilterAll()
    {
        
        Bson match = Filters.all("field name", "value 1", "value 2");
        BsonDocument filterParameters = MongoUtilities.filterParameters(match);
        
        assertEquals("{\"field name\": {\"$all\": [\"*?\"]}}", filterParameters.toString());

   }
    
    @Test
    public void testFilterParametersBsonDocumentFilterbitsAllClear()
    {
        
        Bson match = Filters.bitsAllClear("field name", 123);
        BsonDocument filterParameters = MongoUtilities.filterParameters(match);
        
        assertEquals("{\"field name\": {\"$bitsAllClear\": \"?\"}}", filterParameters.toString());

   }

    
    @Test
    public void testFilterParametersBsonDocumentFilterbitsAllSet()
    {
        
        Bson match = Filters.bitsAllSet("field name", 123);
        BsonDocument filterParameters = MongoUtilities.filterParameters(match);
        
        assertEquals("{\"field name\": {\"$bitsAllSet\": \"?\"}}", filterParameters.toString());

   }
    
    @Test
    public void testFilterParametersBsonDocumentFilterbitsAnyClear()
    {
        
        Bson match = Filters.bitsAnyClear("field name", 123);
        BsonDocument filterParameters = MongoUtilities.filterParameters(match);
        
        assertEquals("{\"field name\": {\"$bitsAnyClear\": \"?\"}}", filterParameters.toString());

   }
    
    @Test
    public void testFilterParametersBsonDocumentFilterbitsAnySet()
    {
        
        Bson match = Filters.bitsAnySet("field name", 123);
        BsonDocument filterParameters = MongoUtilities.filterParameters(match);
        
        assertEquals("{\"field name\": {\"$bitsAnySet\": \"?\"}}", filterParameters.toString());

   }
    
    @Test
    public void testFilterParametersBsonDocumentFilterelemMatch()
    {
        
        Bson match = Filters.elemMatch("field name", Filters.eq("field","value"));
        BsonDocument filterParameters = MongoUtilities.filterParameters(match);
        
        assertEquals("{\"field name\": {\"$elemMatch\": {\"field\": \"?\"}}}", filterParameters.toString());

   }
    
    @Test
    public void testFilterParametersBsonDocumentFilterexists()
    {
        
        Bson match = Filters.exists("field name", false);
        BsonDocument filterParameters = MongoUtilities.filterParameters(match);
        
        assertEquals("{\"field name\": {\"$exists\": \"?\"}}", filterParameters.toString());

   }
    
    @Test
    public void testFilterParametersBsonDocumentFiltergt()
    {
        
        Bson match = Filters.gt("fieldname", 123);
        BsonDocument filterParameters = MongoUtilities.filterParameters(match);
        
        assertEquals("{\"fieldname\": {\"$gt\": \"?\"}}", filterParameters.toString());

   }
    
    @Test
    public void testFilterParametersBsonDocumentFiltermod()
    {
        
        Bson match = Filters.mod("fieldname", 123, 56);
        BsonDocument filterParameters = MongoUtilities.filterParameters(match);
        
        assertEquals("{\"fieldname\": {\"$mod\": [\"?\", \"?\"]}}", filterParameters.toString());

   }
    
    
    @Test
    public void testFilterParametersBsonDocumentFilterNE()
    {
        
        Bson match = Filters.ne("fieldname", 123);
        BsonDocument filterParameters = MongoUtilities.filterParameters(match);
        
        assertEquals("{\"fieldname\": {\"$ne\": \"?\"}}", filterParameters.toString());

   }
    
    @Test
    public void testFilterParametersBsonDocumentFilterNIN()
    {
        
        Bson match = Filters.nin("fieldname", 123,456);
        BsonDocument filterParameters = MongoUtilities.filterParameters(match);
        
        assertEquals("{\"fieldname\": {\"$nin\": [\"*?\"]}}", filterParameters.toString());

   }
    
    @Test
    public void testFilterParametersBsonDocumentFilterREGEX()
    {
        
        Bson match = Filters.regex("fieldname","regex");
        BsonDocument filterParameters = MongoUtilities.filterParameters(match);
        
        assertEquals("{\"fieldname\": \"?\"}", filterParameters.toString());

   }
    
    @Test
    public void testFilterParametersBsonDocumentFilterSIZE()
    {
        
        Bson match = Filters.size("fieldname",123);
        BsonDocument filterParameters = MongoUtilities.filterParameters(match);
        
        assertEquals("{\"fieldname\": {\"$size\": \"?\"}}", filterParameters.toString());

   }
    
    
    @Test
    public void testFilterParametersBsonDocumentFilterTEXT()
    {
        
        Bson match = Filters.text("fieldname");
        BsonDocument filterParameters = MongoUtilities.filterParameters(match);
        
        assertEquals("{\"$text\": {\"$search\": \"?\"}}", filterParameters.toString());

   }
    
    @Test
    public void testFilterParametersBsonDocumentFilterTYPE()
    {
        
        Bson match = Filters.type("fieldname","string");
        BsonDocument filterParameters = MongoUtilities.filterParameters(match);
        
        assertEquals("{\"fieldname\": {\"$type\": \"?\"}}", filterParameters.toString());

   }
    
    @Test
    public void testFilterParametersBsonDocumentFilterwhere()
    {
        Bson match = Filters.where("this.credits - this.debits < 0");
        BsonDocument filterParameters = MongoUtilities.filterParameters(match);
        
        assertEquals("{\"$where\": \"?\"}", filterParameters.toString());
   }
    
    @Test
    public void testFilterParametersBsonDocumentMatchInAndEq()
    {
        
        Bson in = Filters.in("dateKey", "593898622313868b72a296ad", "593898622313868b72a296b4");
        Bson eq = Filters.eq("eqfield","123");
        Bson and = Filters.and(in, eq);
        
        Bson match = Aggregates.match(and);
        BsonDocument filterParameters = MongoUtilities.filterParameters(match);
        
        assertEquals("{\"$match\": {\"$and\": [{\"dateKey\": {\"$in\": [\"*?\"]}}, {\"eqfield\": \"?\"}]}}", filterParameters.toString());

   }
    
    @Test
    public void testFilterParametersBsonDocumentMatchInOrEq()
    {
        
        Bson in = Filters.in("dateKey", "593898622313868b72a296ad", "593898622313868b72a296b4");
        Bson eq = Filters.eq("eqfield","123");
        Bson or = Filters.or(in, eq);
        
        Bson match = Aggregates.match(or);
        BsonDocument filterParameters = MongoUtilities.filterParameters(match);
        
        assertEquals("{\"$match\": {\"$or\": [{\"dateKey\": {\"$in\": [\"*?\"]}}, {\"eqfield\": \"?\"}]}}", filterParameters.toString());

   }


    @Test
    public void testFilterParametersBsonDocumentGroup()
    {
        
        Bson group = Aggregates.group("_id", Accumulators.sum("totalQuantity", "$quantity"));
        BsonDocument filterParameters = MongoUtilities.filterParameters(group);
        
        assertEquals("{\"$group\": {\"_id\": \"_id\", \"totalQuantity\": {\"$sum\": \"$quantity\"}}}", filterParameters.toString());

   }

    @Test
    public void testFilterParametersBsonDocumentLookup()
    {     
        
        Bson lookup = Aggregates.lookup("fromField", "localField", "foreignField", "as");
        BsonDocument filterParameters = MongoUtilities.filterParameters(lookup);
        
        assertEquals("{\"$lookup\": {\"from\": \"fromField\", \"localField\": \"localField\", \"foreignField\": \"foreignField\", \"as\": \"as\"}}", filterParameters.toString());

   }

    @Test
    public void testFilterParametersBsonDocument()
    {

   }

    @Test
    public void testFilterValue()
    {

   }

    @Test
    public void testGetKeyValuePairsBson()
    {

   }

    @Test
    public void testGetKeyValuePairsListOfQextendsBson()
    {

   }

    @Test
    public void testAddKeyValuePairs()
    {

   }

    @Test
    public void testFilterParametersBson()
    {

   }

    @Test
    public void testGetIndexModelKeyValuePairs()
    {

   }
}
