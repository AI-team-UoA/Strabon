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

/**
 * A stSPARQLQueryResultParserFactory returns {@link stSPARQLQueryResultParser}s for
 * a specific tuple query result format.
 * 
 * @see {@link TupleQueryResultParserFactory}
 * 
 * @author Charalampos Nikolaou <charnik@di.uoa.gr>
 *
 */
public interface stSPARQLQueryResultParserFactory {
	/**
	 * Returns the tuple query result format for this factory.
	 */
	public stSPARQLQueryResultFormat getTupleQueryResultFormat();

	/**
	 * Returns a TupleQueryResultParser instance.
	 */
	public stSPARQLQueryResultParser getParser();

}
