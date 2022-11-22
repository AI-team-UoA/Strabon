/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.postgis;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Iterator;

import javax.imageio.spi.ServiceRegistry;

import org.openrdf.sail.generaldb.GeneralDBConnectionFactory;
import org.openrdf.sail.generaldb.GeneralDBProvider;
import org.openrdf.sail.generaldb.GeneralDBStore;

/**
 * The RDBMS SAIL for relational database storage in Sesame. This class acts
 * both as a base class for database specific stores as well as a generic store
 * that can infer the type of database through the JDBC connection.
 * 
 * @author James Leigh
 */
public class PostGISStore extends GeneralDBStore {

	public PostGISStore() {
		super();
	}
	
	/**
	 * Creates a new RDBMS RDF Store using the provided database connection.
	 * 
	 * @param url
	 *        JDNI url of a DataSource
	 */
	public PostGISStore(String url) {
		super(url);
	}

	/**
	 * Creates a new RDBMS RDF Store using the provided database connection.
	 * 
	 * @param url
	 *        JDNI url of a DataSource
	 * @param user
	 * @param password
	 */
	public PostGISStore(String url, String user, String password) {
		super(url, user, password);
	}

	/**
	 * Creates a new RDBMS RDF Store using the provided database connection.
	 * 
	 * @param jdbcDriver
	 * @param jdbcUrl
	 */
	public PostGISStore(String jdbcDriver, String jdbcUrl) {
		super(jdbcDriver, jdbcUrl);
	}

	/**
	 * Creates a new RDBMS RDF Store using the provided database connection.
	 * 
	 * @param jdbcDriver
	 * @param jdbcUrl
	 * @param user
	 * @param password
	 */
	public PostGISStore(String jdbcDriver, String jdbcUrl, String user, String password) {
		super(jdbcDriver, jdbcUrl, user, password);
	}

	@Override
	protected GeneralDBConnectionFactory newFactory(DatabaseMetaData metaData)
		throws SQLException
	{
		String dbn = metaData.getDatabaseProductName();
		String dbv = metaData.getDatabaseProductVersion();
		GeneralDBConnectionFactory factory;
		Iterator<GeneralDBProvider> providers;
		providers = ServiceRegistry.lookupProviders(GeneralDBProvider.class);
		while (providers.hasNext()) {
			GeneralDBProvider provider = providers.next();
			factory = provider.createRdbmsConnectionFactory(dbn, dbv);
			if (factory != null)
				return factory;
		}
		return new PostGISConnectionFactory();
	}
	

}
