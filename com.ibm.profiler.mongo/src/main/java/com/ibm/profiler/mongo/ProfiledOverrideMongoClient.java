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

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoClientURI;
import com.mongodb.MongoCredential;
import com.mongodb.MongoException;
import com.mongodb.MongoNamespace;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoDatabase;
import com.mongodb.MongoDriverInformation;

/**
 * ProfiledOverrideMongoClient
 * 
 * @author Steve McDuff
 */
public class ProfiledOverrideMongoClient extends MongoClient
{


    /**
     * Creates an instance based on a (single) mongodb node (localhost, default port).
     */
    public ProfiledOverrideMongoClient() {
        this(new ServerAddress());
    }

    /**
     * Creates a Mongo instance based on a (single) mongodb node.
     *
     * @param host server to connect to in format host[:port]
     */
    public ProfiledOverrideMongoClient(final String host) {
        this(new ServerAddress(host));
    }

    /**
     * Creates a Mongo instance based on a (single) mongodb node (default port).
     *
     * @param host    server to connect to in format host[:port]
     * @param options default query options
     */
    public ProfiledOverrideMongoClient(final String host, final MongoClientOptions options) {
        this(new ServerAddress(host), options);
    }

    /**
     * Creates a Mongo instance based on a (single) mongodb node.
     *
     * @param host the database's host address
     * @param port the port on which the database is running
     */
    public ProfiledOverrideMongoClient(final String host, final int port) {
        this(new ServerAddress(host, port));
    }

    /**
     * Creates a Mongo instance based on a (single) mongodb node
     *
     * @param addr the database address
     * @see com.mongodb.ServerAddress
     */
    public ProfiledOverrideMongoClient(final ServerAddress addr) {
        this(addr, new MongoClientOptions.Builder().build());
    }

    /**
     * Creates a Mongo instance based on a (single) mongodb node and a list of credentials
     *
     * @param addr            the database address
     * @param credentials     the credentials used to authenticate all connections
     * @see com.mongodb.ServerAddress
     * @since 2.11.0
     */
    public ProfiledOverrideMongoClient(final ServerAddress addr, final MongoCredential credentials) {
        this(addr, credentials, new MongoClientOptions.Builder().build());
    }

    /**
     * Creates a Mongo instance based on a (single) mongo node using a given ServerAddress and default options.
     *
     * @param addr    the database address
     * @param options default options
     * @see com.mongodb.ServerAddress
     */
    public ProfiledOverrideMongoClient(final ServerAddress addr, final MongoClientOptions options) {
        super(addr, options);
    }

    /**
     * Creates a Mongo instance based on a (single) mongo node using a given ServerAddress and default options.
     *
     * @param addr            the database address
     * @param credentials 	  the credentials used to authenticate all connections
     * @param options         default options
     * @see com.mongodb.ServerAddress
     * @since 2.11.0
     */
    public ProfiledOverrideMongoClient(final ServerAddress addr, final MongoCredential credentials, final MongoClientOptions options) {
    	super(addr, credentials, options);
    }

    /**
     * <p>Creates an instance based on a list of replica set members or mongos servers. For a replica set it will discover all members.
     * For a list with a single seed, the driver will still discover all members of the replica set.  For a direct
     * connection to a replica set member, with no discovery, use the {@link #ProfiledOverrideMongoClient(ServerAddress)} constructor instead.</p>
     *
     * <p>When there is more than one server to choose from based on the type of request (read or write) and the read preference (if it's a
     * read request), the driver will randomly select a server to send a request. This applies to both replica sets and sharded clusters.
     * The servers to randomly select from are further limited by the local threshold.  See
     * {@link MongoClientOptions#getLocalThreshold()}</p>
     *
     * @param seeds Put as many servers as you can in the list and the system will figure out the rest.  This can either be a list of mongod
     *              servers in the same replica set or a list of mongos servers in the same sharded cluster.
     * @see MongoClientOptions#getLocalThreshold()
     */
    public ProfiledOverrideMongoClient(final List<ServerAddress> seeds) {
        this(seeds, new MongoClientOptions.Builder().build());
    }

    /**
     * <p>Creates an instance based on a list of replica set members or mongos servers. For a replica set it will discover all members.
     * For a list with a single seed, the driver will still discover all members of the replica set.  For a direct
     * connection to a replica set member, with no discovery, use the {@link #ProfiledOverrideMongoClient(ServerAddress, MongoCredential)}
     * constructor instead.</p>
     *
     * <p>When there is more than one server to choose from based on the type of request (read or write) and the read preference (if it's a
     * read request), the driver will randomly select a server to send a request. This applies to both replica sets and sharded clusters.
     * The servers to randomly select from are further limited by the local threshold.  See
     * {@link MongoClientOptions#getLocalThreshold()}</p>
     *
     * @param seeds Put as many servers as you can in the list and the system will figure out the rest.  This can either be a list of mongod
     *              servers in the same replica set or a list of mongos servers in the same sharded cluster.
     * @param credentials the credentials used to authenticate all connections
     * @see MongoClientOptions#getLocalThreshold()
     * @since 2.11.0
     */
    public ProfiledOverrideMongoClient(final List<ServerAddress> seeds, final MongoCredential credentials) {
        this(seeds, credentials, new MongoClientOptions.Builder().build());
    }

    /**
     * <p>Construct an instance based on a list of replica set members or mongos servers. For a replica set it will discover all members.
     * For a list with a single seed, the driver will still discover all members of the replica set.  For a direct
     * connection to a replica set member, with no discovery, use the {@link #ProfiledOverrideMongoClient(ServerAddress, MongoClientOptions)} constructor
     * instead.</p>
     *
     * <p>When there is more than one server to choose from based on the type of request (read or write) and the read preference (if it's a
     * read request), the driver will randomly select a server to send a request. This applies to both replica sets and sharded clusters.
     * The servers to randomly select from are further limited by the local threshold.  See
     * {@link MongoClientOptions#getLocalThreshold()}</p>
     *
     * @param seeds Put as many servers as you can in the list and the system will figure out the rest.  This can either be a list of mongod
     *              servers in the same replica set or a list of mongos servers in the same sharded cluster.
     * @param options the options
     * @see MongoClientOptions#getLocalThreshold()
     */
    public ProfiledOverrideMongoClient(final List<ServerAddress> seeds, final MongoClientOptions options) {
        super(seeds, options);
    }

    /**
     * <p>Creates an instance based on a list of replica set members or mongos servers. For a replica set it will discover all members.
     * For a list with a single seed, the driver will still discover all members of the replica set.  For a direct
     * connection to a replica set member, with no discovery, use the {@link #ProfiledOverrideMongoClient(ServerAddress, MongoCredential, MongoClientOptions)}
     * constructor instead.</p>
     *
     * <p>When there is more than one server to choose from based on the type of request (read or write) and the read preference (if it's a
     * read request), the driver will randomly select a server to send a request. This applies to both replica sets and sharded clusters.
     * The servers to randomly select from are further limited by the local threshold.  See
     * {@link MongoClientOptions#getLocalThreshold()}</p>
     *
     * @param seeds Put as many servers as you can in the list and the system will figure out the rest.  This can either be a list of mongod
     *              servers in the same replica set or a list of mongos servers in the same sharded cluster.
     * @param credentials 	  the credentials used to authenticate all connections
     * @param options         the options
     * @see MongoClientOptions#getLocalThreshold()
     * @since 2.11.0
     */
    public ProfiledOverrideMongoClient(final List<ServerAddress> seeds, final MongoCredential credentials, final MongoClientOptions options) {
        super(seeds, credentials, options);
    }

    /**
     * Creates a Mongo described by a URI. If only one address is used it will only connect to that node, otherwise it will discover all
     * nodes.
     *
     * @param uri the URI
     * @throws MongoException if theres a failure
     */
    public ProfiledOverrideMongoClient(final MongoClientURI uri) {
        super(uri);
    }

    /**
     * Creates a Mongo described by a URI.
     *
     * <p>Note: Intended for driver and library authors to associate extra driver metadata with the connections.</p>
     *
     * @param uri the URI
     * @param mongoDriverInformation any driver information to associate with the MongoClient
     * @throws MongoException if theres a failure
     * @since 3.4
     */
    public ProfiledOverrideMongoClient(final MongoClientURI uri, final MongoDriverInformation mongoDriverInformation) {
        super(uri, mongoDriverInformation);
    }

    /**
     * Creates a MongoClient to a single node using a given ServerAddress.
     *
     * <p>Note: Intended for driver and library authors to associate extra driver metadata with the connections.</p>
     *
     * @param addr            the database address
     * @param credentials the list of credentials used to authenticate all connections
     * @param options         default options
     * @param mongoDriverInformation any driver information to associate with the MongoClient
     * @see com.mongodb.ServerAddress
     * @since 3.4
     */
    public ProfiledOverrideMongoClient(final ServerAddress addr, final MongoCredential credentials, final MongoClientOptions options,
                       final MongoDriverInformation mongoDriverInformation) {
        super(addr, credentials, options, mongoDriverInformation);
    }

    /**
     * Creates a MongoClient
     *
     * <p>Note: Intended for driver and library authors to associate extra driver metadata with the connections.</p>
     *
     * @param seeds Put as many servers as you can in the list and the system will figure out the rest.  This can either be a list of mongod
     *              servers in the same replica set or a list of mongos servers in the same sharded cluster.
     * @param credentials the credentials used to authenticate all connections
     * @param options         the options
     * @param mongoDriverInformation any driver information to associate with the MongoClient
     * @since 3.4
     */
    public ProfiledOverrideMongoClient(final List<ServerAddress> seeds, final MongoCredential credentials, final MongoClientOptions options,
                       final MongoDriverInformation mongoDriverInformation) {
        super(seeds, credentials, options, mongoDriverInformation);
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
    @Override
    public MongoDatabase getDatabase(final String databaseName)
    {

        MongoDatabase database = super.getDatabase(databaseName);
        ProfiledMongoDatabase profiledDatabase = new ProfiledMongoDatabase(database);
        return profiledDatabase;

    }
    

}
