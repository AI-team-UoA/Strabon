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

import org.openrdf.query.TupleQueryResultHandler;

/**
 * The interface of objects that writer query results in a specific query result
 * format.
 * 
 * @author Charalampos Nikolaou <charnik@di.uoa.gr>
 */
public interface stSPARQLQueryResultWriter extends TupleQueryResultHandler {

	/**
	 * Gets the query result format that this writer uses.
	 */
	public stSPARQLQueryResultFormat getTupleQueryResultFormat();
}
