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
