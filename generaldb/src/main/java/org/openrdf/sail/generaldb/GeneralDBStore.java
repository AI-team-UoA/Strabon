/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.generaldb;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailException;
import org.openrdf.sail.helpers.SailBase;
import org.openrdf.sail.rdbms.exceptions.RdbmsException;

/**
 * The RDBMS SAIL for relational database storage in Sesame. This class acts
 * both as a base class for database specific stores as well as a generic store
 * that can infer the type of database through the JDBC connection.
 * 
 * @author James Leigh
 */
public abstract class GeneralDBStore extends SailBase {

	protected GeneralDBConnectionFactory factory;

	protected String jdbcDriver;

	protected String url;

	protected String user;

	protected String password;

	protected int maxTripleTables;

	protected boolean triplesIndexed = true;

	protected boolean sequenced = true;

	protected BasicDataSource ds;

	public GeneralDBStore() {
		super();
	}

	/**
	 * Creates a new RDBMS RDF Store using the provided database connection.
	 * 
	 * @param url
	 *        JDNI url of a DataSource
	 */
	public GeneralDBStore(String url) {
		this.url = url;
	}

	/**
	 * Creates a new RDBMS RDF Store using the provided database connection.
	 * 
	 * @param url
	 *        JDNI url of a DataSource
	 * @param user
	 * @param password
	 */
	public GeneralDBStore(String url, String user, String password) {
		this.url = url;
		this.user = user;
		this.password = password;
	}

	/**
	 * Creates a new RDBMS RDF Store using the provided database connection.
	 * 
	 * @param jdbcDriver
	 * @param jdbcUrl
	 */
	public GeneralDBStore(String jdbcDriver, String jdbcUrl) {
		this.jdbcDriver = jdbcDriver;
		this.url = jdbcUrl;
	}

	/**
	 * Creates a new RDBMS RDF Store using the provided database connection.
	 * 
	 * @param jdbcDriver
	 * @param jdbcUrl
	 * @param user
	 * @param password
	 */
	public GeneralDBStore(String jdbcDriver, String jdbcUrl, String user, String password) {
		this.jdbcDriver = jdbcDriver;
		this.url = jdbcUrl;
		this.user = user;
		this.password = password;
	}

	public int getMaxNumberOfTripleTables() {
		return maxTripleTables;
	}

	public void setMaxNumberOfTripleTables(int max) {
		maxTripleTables = max;
	}

	public boolean isIndexed() {
		return triplesIndexed;
	}

	public void setIndexed(boolean indexed)
		throws SailException
	{
		triplesIndexed = indexed;
		if (factory != null) {
			factory.setTriplesIndexed(triplesIndexed);
		}
	}

	public boolean isSequenced() {
		return sequenced;
	}

	public void setSequenced(boolean useSequence) {
		this.sequenced = useSequence;
	}

	@Override
	protected void initializeInternal()
		throws SailException
	{
		if (factory == null) {
			try {
				factory = createFactory(jdbcDriver, url, user, password);
			}
			catch (SailException e) {
				throw e;
			}
			catch (Exception e) {
				throw new RdbmsException(e);
			}
		}
		factory.setMaxNumberOfTripleTables(maxTripleTables);
		factory.setTriplesIndexed(triplesIndexed);
		factory.setSequenced(sequenced);
		try {
			factory.init();
		} catch (Exception ex) {
			// TODO Auto-generated catch block
			throw new RdbmsException(ex);

		}
	}

	public boolean isWritable()
		throws SailException
	{
		return factory.isWritable();
	}

	public GeneralDBValueFactory getValueFactory() {
		return factory.getValueFactory();
	}

	@Override
	protected SailConnection getConnectionInternal()
		throws SailException
	{
		return factory.createConnection();
	}

	@Override
	protected void shutDownInternal()
		throws SailException
	{
		factory.shutDown();
		try {
			if (ds != null) {
				ds.close();
			}
		}
		catch (SQLException e) {
			throw new RdbmsException(e);
		}
	}

	protected void setConnectionFactory(GeneralDBConnectionFactory factory) {
		this.factory = factory;
	}

	protected void setBasicDataSource(BasicDataSource ds) {
		this.ds = ds;
	}

	protected GeneralDBConnectionFactory createFactory(String jdbcDriver, String url, String user, String password)
		throws Exception
	{
		if (jdbcDriver != null) {
			Class.forName(jdbcDriver);
		}
		DataSource ds = lookupDataSource(url, user, password);
		Connection con;
		if (user == null || url.startsWith("jdbc:")) {
			con = ds.getConnection();
		}
		else {
			con = ds.getConnection(user, password);
		}
		try {
			DatabaseMetaData metaData = con.getMetaData();
			GeneralDBConnectionFactory factory = newFactory(metaData);
			factory.setSail(this);
			if (user == null || url.startsWith("jdbc:")) {
				factory.setDataSource(ds);
			}
			else {
				factory.setDataSource(ds, user, password);
			}
			return factory;
		}
		finally {
			con.close();
		}
	}

	protected DataSource lookupDataSource(String url, String user, String password)
		throws NamingException
	{
		if (url.startsWith("jdbc:")) {
			BasicDataSource ds = new BasicDataSource();
			ds.setUrl(url);
			ds.setUsername(user);
			ds.setPassword(password);
			setBasicDataSource(ds);
			return ds;
		}
		return (DataSource)new InitialContext().lookup(url);
	}

	protected abstract GeneralDBConnectionFactory newFactory(DatabaseMetaData metaData)
		throws SQLException;

}
