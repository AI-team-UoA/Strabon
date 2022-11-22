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

import info.aduna.lang.service.FileFormatServiceRegistry;

/**
 * A registry that keeps track of the available
 * {@link stSPARQLQueryResultParserFactory}s.
 * 
 * @author Charalampos Nikolaou <charnik@di.uoa.gr>
 *
 */
public class stSPARQLQueryResultParserRegistry extends FileFormatServiceRegistry<stSPARQLQueryResultFormat, stSPARQLQueryResultParserFactory> {

	private static stSPARQLQueryResultParserRegistry defaultRegistry;

	/**
	 * Gets the default stSPARQLQueryResultParserRegistry.
	 * 
	 * @return The default registry.
	 */
	public static synchronized stSPARQLQueryResultParserRegistry getInstance() {
		if (defaultRegistry == null) {
			defaultRegistry = new stSPARQLQueryResultParserRegistry();
		}

		return defaultRegistry;
	}

	public stSPARQLQueryResultParserRegistry() {
		super(stSPARQLQueryResultParserFactory.class);
	}

	@Override
	protected stSPARQLQueryResultFormat getKey(stSPARQLQueryResultParserFactory factory) {
		return factory.getTupleQueryResultFormat();
	}

}
