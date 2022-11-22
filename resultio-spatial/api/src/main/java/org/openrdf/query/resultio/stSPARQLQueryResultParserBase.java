/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * 
 * Copyright (C) 2012, Pyravlos Team
 * 
 * http://www.strabon.di.uoa.gr/
 */
package org.openrdf.query.resultio;

import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.query.TupleQueryResultHandler;

/**
 * Base class for {@link stSPARQLQueryResultParser}s offering common functionality for
 * query result parsers.
 * 
 * @author Charalampos Nikolaou <charnik@di.uoa.gr>
 *
 */
public abstract class stSPARQLQueryResultParserBase implements stSPARQLQueryResultParser {

	/*-----------*
	 * Variables *
	 *-----------*/

	/**
	 * The ValueFactory to use for creating RDF model objects.
	 */
	protected ValueFactory valueFactory;

	/**
	 * The TupleQueryResultHandler that will handle the parsed query results.
	 */
	protected TupleQueryResultHandler handler;

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new parser base that, by default, will use an instance of
	 * {@link ValueFactoryImpl} to create Value objects.
	 */
	public stSPARQLQueryResultParserBase() {
		this(new ValueFactoryImpl());
	}

	/**
	 * Creates a new parser base that will use the supplied ValueFactory to
	 * create Value objects.
	 */
	public stSPARQLQueryResultParserBase(ValueFactory valueFactory) {
		setValueFactory(valueFactory);
	}

	/*---------*
	 * Methods *
	 *---------*/

	public void setValueFactory(ValueFactory valueFactory) {
		this.valueFactory = valueFactory;
	}

	public void setTupleQueryResultHandler(TupleQueryResultHandler handler) {
		this.handler = handler;
	}

}
