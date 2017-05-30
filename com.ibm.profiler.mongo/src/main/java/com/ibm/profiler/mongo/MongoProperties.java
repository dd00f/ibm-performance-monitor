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

/**
 * MongoProperties
 * 
 * @author Steve McDuff
 */
public class MongoProperties
{
    /**
     * Time spent creating a cursor on MongoDB.
     */
    public static final String MONGO_CURSOR_CREATION_DURATION_NS = "mongoCursorCreationDurationNs";
    
    /**
     * Mongo collection name.
     */
    public static final String MONGO_COLLECTION = "mongoCollection";

    /**
     * Mongo database name.
     */
    public static final String MONGO_DATABASE = "mongoDatabase";

    /**
     * Read preference of a mongoDB query.
     */
    public static final String MONGO_READ_PREFERENCE = "mongoReadPreference";

    /**
     * Read concern of a mongoDB query.
     */
    public static final String MONGO_READ_CONCERN = "mongoReadConcern";

    /**
     * Write concern of a mongoDB query.
     */
    public static final String MONGO_WRITE_CONCERN = "mongoWriteConcern";

    /**
     * Number of objects matched in an mongodb update.
     */
    public static final String MONGO_UPDATE_MATCHED_COUNT = "mongoUpdateMatchedCount";

    /**
     * Number of objects modified by a mongodb update.
     */
    public static final String MONGO_UPDATE_MODIFIED_COUNT = "mongoUpdateModifiedCount";

    public MongoProperties()
    {
        super();
    }
}
