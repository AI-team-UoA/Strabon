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

import java.io.IOException;
import java.io.InputStream;

import org.openrdf.model.ValueFactory;
import org.openrdf.query.TupleQueryResultHandler;
import org.openrdf.query.TupleQueryResultHandlerException;

/**
 * A general interface for stSPARQL tuple query result parsers.
 * 
 * @see {@link TupleQueryResultParser}
 * 
 * @author Charalampos Nikolaou <charnik@di.uoa.gr>
 */
public interface stSPARQLQueryResultParser {

	/**
	 * Gets the query result format that this parser can parse.
	 */
	public stSPARQLQueryResultFormat getTupleQueryResultFormat();

	/**
	 * Sets the ValueFactory that the parser will use to create Value objects for
	 * the parsed query result.
	 * 
	 * @param valueFactory
	 *        The value factory that the parser should use.
	 */
	public void setValueFactory(ValueFactory valueFactory);

	/**
	 * Sets the TupleQueryResultHandler that will handle the parsed query result
	 * data.
	 */
	public void setTupleQueryResultHandler(TupleQueryResultHandler handler);

	/**
	 * Parses the data from the supplied InputStream.
	 * 
	 * @param in
	 *        The InputStream from which to read the data.
	 * @throws IOException
	 *         If an I/O error occurred while data was read from the InputStream.
	 * @throws QueryResultParseException
	 *         If the parser has encountered an unrecoverable parse error.
	 * @throws TupleQueryResultHandlerException
	 *         If the configured query result handler has encountered an
	 *         unrecoverable error.
	 */
	public void parse(InputStream in)
		throws IOException, QueryResultParseException, TupleQueryResultHandlerException;

}
