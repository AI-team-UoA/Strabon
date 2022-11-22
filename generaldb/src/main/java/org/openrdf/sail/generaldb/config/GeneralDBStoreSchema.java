/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.generaldb.config;

import org.openrdf.model.URI;
import org.openrdf.model.IRI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.SimpleValueFactory;

/**
 * Vocabulary for the RDBMS configuration.
 * 
 * @author James Leigh
 * 
 */
public class GeneralDBStoreSchema {

	public static final String NAMESPACE = "http://www.openrdf.org/config/sail/rdbms#";

	public final static IRI JDBC_DRIVER;

	public final static IRI URL;

	public final static IRI USER;

	public final static IRI PASSWORD;

	public final static IRI MAX_TRIPLE_TABLES;

	static {
		ValueFactory factory = SimpleValueFactory.getInstance();
		JDBC_DRIVER = factory.createIRI(NAMESPACE, "jdbcDriver");
		URL = factory.createIRI(NAMESPACE, "url");
		USER = factory.createIRI(NAMESPACE, "user");
		PASSWORD = factory.createIRI(NAMESPACE, "password");
		MAX_TRIPLE_TABLES = factory.createIRI(NAMESPACE, "maxTripleTables");
	}
}
