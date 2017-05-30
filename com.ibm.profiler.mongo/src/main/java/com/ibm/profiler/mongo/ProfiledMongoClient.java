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
     * Gets the options that this client uses to connect to server.
     *
     * <p>Note: {@link MongoClientOptions} is immutable.</p>
     *
     * @return the options
     */
    public MongoClientOptions getMongoClientOptions() {
        return client.getMongoClientOptions();
    }

    /**
     * Gets the list of credentials that this client authenticates all connections with
     *
     * @return the list of credentials
     * @since 2.11.0
     */
    public List<MongoCredential> getCredentialsList() {
        return client.getCredentialsList();
    }

    /**
     * Get a list of the database names
     *
     * @mongodb.driver.manual reference/command/listDatabases List Databases
     * @return an iterable containing all the names of all the databases
     * @since 3.0
     */
    public MongoIterable<String> listDatabaseNames() {
       return client.listDatabaseNames();
    }

    /**
     * Gets the list of databases
     *
     * @return the list of databases
     * @since 3.0
     */
    public ListDatabasesIterable<Document> listDatabases() {
        return listDatabases(Document.class);
    }

    /**
     * Gets the list of databases
     *
     * @param clazz the class to cast the database documents to
     * @param <T>   the type of the class to use instead of {@code Document}.
     * @return the list of databases
     * @since 3.0
     */
    public <T> ListDatabasesIterable<T> listDatabases(final Class<T> clazz) {
        return client.listDatabases(clazz);
    }

    /**
     * Get a database by name.
     * 
     * @param databaseName the name of the database to retrieve
     * @return a {@code MongoDatabase} representing the specified database
     * @throws IllegalArgumentException if databaseName is invalid
     * @see MongoNamespace#checkDatabaseNameValidity(String)
     */
    public MongoDatabase getDatabase(final String databaseName) {
        
        MongoDatabase database = client.getDatabase(databaseName);
        ProfiledMongoDatabase profiledDatabase = new ProfiledMongoDatabase(database);
        return profiledDatabase;
        
    }
}
