/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.generaldb.config;

import org.openrdf.sail.config.SailConfigException;
import org.openrdf.sail.config.SailFactory;
import org.openrdf.sail.config.SailImplConfig;
import org.openrdf.sail.generaldb.GeneralDBStore;

/**
 * A {@link SailFactory} that creates {@link GeneralDBStore}s based on RDF
 * configuration data.
 * 
 * @author James Leigh
 */
public abstract class GeneralDBStoreFactory implements SailFactory {

	/**
	 * The type of repositories that are created by this factory.
	 * 
	 * @see SailFactory#getSailType()
	 */
	public static final String SAIL_TYPE = "openrdf:RdbmsStore";

	/**
	 * Returns the Sail's type: <tt>openrdf:RdbmsStore</tt>.
	 */
	public String getSailType() {
		return SAIL_TYPE;
	}

	public GeneralDBStoreConfig getConfig() {
		return new GeneralDBStoreConfig();
	}

	public abstract GeneralDBStore getSail(SailImplConfig config)
		throws SailConfigException;
}
