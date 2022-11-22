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
 * {@link stSPARQLQueryResultWriterFactory}s.
 * 
 * @see {@link TupleQueryResultWriterRegistry}
 * 
 * @author Charalampos Nikolaou <charnik@di.uoa.gr>
 */
public class stSPARQLQueryResultWriterRegistry extends FileFormatServiceRegistry<stSPARQLQueryResultFormat, stSPARQLQueryResultWriterFactory> {

	private static stSPARQLQueryResultWriterRegistry defaultRegistry;

	/**
	 * Gets the default stSPARQLQueryResultWriterRegistry.
	 * 
	 * @return The default registry.
	 */
	public static synchronized stSPARQLQueryResultWriterRegistry getInstance() {
		if (defaultRegistry == null) {
			defaultRegistry = new stSPARQLQueryResultWriterRegistry();
		}

		return defaultRegistry;
	}

	public stSPARQLQueryResultWriterRegistry() {
		super(stSPARQLQueryResultWriterFactory.class);
	}

	@Override
	protected stSPARQLQueryResultFormat getKey(stSPARQLQueryResultWriterFactory factory) {
		return factory.getTupleQueryResultFormat();
	}
}
