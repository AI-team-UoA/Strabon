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

import java.io.OutputStream;

/**
 * Returns {@link stSPARQLQueryResultWriter}s for a specific tuple query result
 * format.
 * 
 * @see {@link TupleQueryResultWriterFactory}
 *  
 * @author Charalampos Nikolaou <charnik@di.uoa.gr>
 */
public interface stSPARQLQueryResultWriterFactory {

	/**
	 * Returns the tuple query result format for this factory.
	 */
	public stSPARQLQueryResultFormat getTupleQueryResultFormat();

	/**
	 * Returns a stSPARQLQueryResultWriter instance that will write to the supplied
	 * output stream.
	 * 
	 * @param out
	 *        The OutputStream to write the result to.
	 */
	public stSPARQLQueryResultWriter getWriter(OutputStream out);

}
