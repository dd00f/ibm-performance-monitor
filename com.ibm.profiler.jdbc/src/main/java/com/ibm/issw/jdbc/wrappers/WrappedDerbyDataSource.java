package com.ibm.issw.jdbc.wrappers;

/*
 *-----------------------------------------------------------------
 * IBM Confidential
 *
 * OCO Source Materials
 *
 * WebSphere Commerce
 *
 * (C) Copyright IBM Corp. 2013, 2015
 *
 * The source code for this program is not published or otherwise
 * divested of its trade secrets, irrespective of what has
 * been deposited with the U.S. Copyright Office.
 *-----------------------------------------------------------------
 */

import java.io.PrintWriter;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Hashtable;
import java.util.logging.Logger;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.spi.ObjectFactory;
import javax.sql.ConnectionPoolDataSource;
import javax.sql.DataSource;
import javax.sql.PooledConnection;

import org.apache.derby.jdbc.EmbeddedConnectionPoolDataSource;

import com.ibm.issw.jdbc.profiler.JdbcProfilerDaemon;

/**
 * 
 * WrappedDerbyDataSource
 */
public class WrappedDerbyDataSource implements ConnectionPoolDataSource,
		Serializable, DataSource, ObjectFactory {
	/**
	 * IBM Copyright notice field.
	 */
	public static final String COPYRIGHT = com.ibm.commerce.copyright.IBMCopyright.SHORT_COPYRIGHT;
	
	private static final long serialVersionUID = 8135772887072004066L;
//	private static final Logger LOG = Logger
//			.getLogger(WrappedDerbyDataSource.class.getName());
	private final EmbeddedConnectionPoolDataSource ds;

	/**
	 * ctor
	 */
	public WrappedDerbyDataSource() {
		this.ds = new EmbeddedConnectionPoolDataSource();
		JdbcProfilerDaemon.initializeDaemon();
	}
	/*
	 * (non-Javadoc)
	 * @see javax.sql.ConnectionPoolDataSource#getPooledConnection()
	 */
	@Override
    public PooledConnection getPooledConnection() throws SQLException {
		PooledConnection conn = this.ds.getPooledConnection();
		if ((conn instanceof WrappedPooledConnection)) {
			return conn;
		}
		return new WrappedPooledConnection(conn);
	}
	/*
	 * (non-Javadoc)
	 * @see javax.sql.ConnectionPoolDataSource#getPooledConnection(java.lang.String, java.lang.String)
	 */
	@Override
    public PooledConnection getPooledConnection(String username, String password)
			throws SQLException {
		PooledConnection conn = this.ds.getPooledConnection(username, password);
		if ((conn instanceof WrappedPooledConnection)) {
			return conn;
		}
		return new WrappedPooledConnection(conn);
	}
	/*
	 * (non-Javadoc)
	 * @see javax.sql.CommonDataSource#getLogWriter()
	 */
	@Override
    public PrintWriter getLogWriter() throws SQLException {
		return this.ds.getLogWriter();
	}
	/*
	 * (non-Javadoc)
	 * @see javax.sql.CommonDataSource#setLogWriter(java.io.PrintWriter)
	 */
	@Override
    public void setLogWriter(PrintWriter out) throws SQLException {
		this.ds.setLogWriter(out);
	}
	/*
	 * (non-Javadoc)
	 * @see javax.sql.CommonDataSource#setLoginTimeout(int)
	 */
	@Override
    public void setLoginTimeout(int seconds) throws SQLException {
		this.ds.setLoginTimeout(seconds);
	}
	/*
	 * (non-Javadoc)
	 * @see javax.sql.CommonDataSource#getLoginTimeout()
	 */
	@Override
    public int getLoginTimeout() throws SQLException {
		return this.ds.getLoginTimeout();
	}
	/*
	 * (non-Javadoc)
	 * @see javax.sql.DataSource#getConnection()
	 */
	@Override
    public Connection getConnection() throws SQLException {
		Connection conn = this.ds.getConnection();
		if ((conn instanceof WrappedConnection)) {
			return conn;
		}
		return new WrappedConnection(conn);
	}
/*
 * (non-Javadoc)
 * @see javax.sql.DataSource#getConnection(java.lang.String, java.lang.String)
 */
	@Override
    public Connection getConnection(String username, String password)
			throws SQLException {
		Connection conn = this.ds.getConnection(username, password);
		if ((conn instanceof WrappedConnection)) {
			return conn;
		}
		return new WrappedConnection(conn);
	}
	/**
	 * 
	 * setUser
	 * @param user user name
	 */
	public void setUser(String user) {
		this.ds.setUser(user);
	}
	/**
	 * 
	 * setPassword
	 * @param password password
	 */
	public void setPassword(String password) {
		this.ds.setPassword(password);
	}
	/**
	 * 
	 * setDatabaseName
	 * @param name db name
	 */
	public void setDatabaseName(String name) {
		this.ds.setDatabaseName(name);
	}
	/**
	 * 
	 * setCreateDatabase
	 * @param create create db
	 */
	public void setCreateDatabase(String create) {
		this.ds.setCreateDatabase(create);
	}
	/**
	 * 
	 * setConnectionAttributes
	 * @param attributes attributes
	 */
	public void setConnectionAttributes(String attributes) {
		this.ds.setConnectionAttributes(attributes);
	}
	/**
	 * 
	 * setShutdownDatabase
	 * @param shutdown shutdown
	 */
	public void setShutdownDatabase(String shutdown) {
		this.ds.setShutdownDatabase(shutdown);
	}
	/**
	 * 
	 * setDataSourceName
	 * @param name name
	 */
	public void setDataSourceName(String name) {
		this.ds.setDataSourceName(name);
	}
	/**
	 * 
	 * setAttriubtesAsPassword
	 * @param b setAttriubtesAsPassword
	 */
	public void setAttriubtesAsPassword(boolean b) {
		this.ds.setAttributesAsPassword(b);
	}

	/**
	 * 
	 * setDescription
	 * @param description description
	 */
	public void setDescription(String description) {
		this.ds.setDescription(description);
	}

	/**
	 * 
	 * getDatabaseName
	 * @return db name
	 */
	public String getDatabaseName() {
		return this.ds.getDatabaseName();
	}

	/**
	 * 
	 * getDescription
	 * @return desc 
	 */
	public String getDescription() {
		return this.ds.getDescription();
	}

	/**
	 * 
	 * getPassword
	 * @return pw
	 */
	public String getPassword() {
		return this.ds.getPassword();
	}

	/**
	 * 
	 * getReference
	 * @return ref
	 * @throws NamingException if anything goes wrong
	 */
	public Reference getReference() throws NamingException {
		return this.ds.getReference();
	}

	/**
	 * 
	 * getUser
	 * @return user
	 */
	public String getUser() {
		return this.ds.getUser();
	}

	/**
	 * 
	 * getConnectionAttributes
	 * @return a
	 */
	public String getConnectionAttributes() {
		return this.ds.getConnectionAttributes();
	}

	/** 
	 * 
	 * getAttributesAsPassword
	 * @return a
	 */
	public boolean getAttributesAsPassword() {
		return this.ds.getAttributesAsPassword();
	}

	/**
	 * 
	 * getCreateDatabase
	 * @return a
	 */
	public String getCreateDatabase() {
		return this.ds.getCreateDatabase();
	}

	/**
	 * 
	 * getShutdownDatabase
	 * @return a
	 */
	public String getShutdownDatabase() {
		return this.ds.getShutdownDatabase();
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.Wrapper#isWrapperFor(java.lang.Class)
	 */
	@Override
	public boolean isWrapperFor(Class<?> arg0) throws SQLException {
		return this.ds.isWrapperFor(arg0);
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.Wrapper#unwrap(java.lang.Class)
	 */
	@Override
	public <T> T unwrap(Class<T> arg0) throws SQLException {
		return this.ds.unwrap(arg0);
	}
	/*
	 * (non-Javadoc)
	 * @see javax.naming.spi.ObjectFactory#getObjectInstance(java.lang.Object, javax.naming.Name, javax.naming.Context, java.util.Hashtable)
	 */
	@Override
	public Object getObjectInstance(Object arg0, Name arg1, Context arg2,
			Hashtable<?, ?> arg3) throws Exception {
		return this.ds.getObjectInstance(arg0, arg1, arg2, arg3);
	}
	/*
	 * (non-Javadoc)
	 * @see javax.sql.CommonDataSource#getParentLogger()
	 */
	@Override
	public Logger getParentLogger() throws SQLFeatureNotSupportedException {
		throw new SQLFeatureNotSupportedException();
	}
}
