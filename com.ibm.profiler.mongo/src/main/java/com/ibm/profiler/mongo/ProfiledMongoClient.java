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

import java.util.List;

import org.bson.Document;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.MongoNamespace;
import com.mongodb.client.ListDatabasesIterable;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoIterable;

/**
 * ProfiledMongoClient
 * 
 * @author Steve McDuff
 */
public class ProfiledMongoClient
{

    private final MongoClient client;

    public ProfiledMongoClient(MongoClient client)
    {
        super();
        this.client = client;
    }

    /**
     * Get the original mongo client to access unprofiled APIs.
     * 
     * @return The mongo client.
     */
    public MongoClient getClient()
    {
        return client;
    }

    /**
     * Gets the options that this client uses to connect to server.
     *
     * <p>
     * Note: {@link MongoClientOptions} is immutable.
     * </p>
     *
     * @return the options
     */
    public MongoClientOptions getMongoClientOptions()
    {
        return client.getMongoClientOptions();
    }

    /**
     * Gets the list of credentials that this client authenticates all connections with
     *
     * @return the list of credentials
     */
    public List<MongoCredential> getCredentialsList()
    {
        return client.getCredentialsList();
    }

    /**
     * Get a list of the database names
     *
     * @return an iterable containing all the names of all the databases
     */
    public MongoIterable<String> listDatabaseNames()
    {
        return client.listDatabaseNames();
    }

    /**
     * Gets the list of databases
     *
     * @return the list of databases
     */
    public ListDatabasesIterable<Document> listDatabases()
    {
        return listDatabases(Document.class);
    }

    /**
     * Gets the list of databases
     *
     * @param clazz
     *            the class to cast the database documents to
     * @param <T>
     *            the type of the class to use instead of {@code Document}.
     * @return the list of databases
     */
    public <T> ListDatabasesIterable<T> listDatabases(final Class<T> clazz)
    {
        return client.listDatabases(clazz);
    }

    /**
     * Get a database by name.
     * 
     * @param databaseName
     *            the name of the database to retrieve
     * @return a {@code MongoDatabase} representing the specified database
     * @throws IllegalArgumentException
     *             if databaseName is invalid
     * @see MongoNamespace#checkDatabaseNameValidity(String)
     */
    public MongoDatabase getDatabase(final String databaseName)
    {

        MongoDatabase database = client.getDatabase(databaseName);
        ProfiledMongoDatabase profiledDatabase = new ProfiledMongoDatabase(database);
        return profiledDatabase;

    }

    /**
     * Closes all resources associated with this instance, in particular any open network connections. Once called, this
     * instance and any databases obtained from it can no longer be used.
     */
    public void close()
    {
        client.close();
    }
}
