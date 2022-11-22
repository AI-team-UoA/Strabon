/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * 
 * Copyright (C) 2013, Pyravlos Team
 * 
 * http://www.strabon.di.uoa.gr/
 */
package eu.earthobservatory.runtime.generaldb;

import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.repository.sail.SailRepositoryConnection;
import org.openrdf.sail.Sail;
import org.openrdf.sail.SailException;

/**
 * This class extends SailRepository only to be able to create
 * connections that are instances of our class {@link GeneralDBSailRepositoryConnection}
 * instead of Sesame's {@link SailRepositoryConnection}. See class
 * {@link GeneralDBSailRepositoryConnection} for the reason behind
 * this extension.
 * 
 * @author Charalampos Nikolaou <charnik@di.uoa.gr>
 */
public class GeneralDBSailRepository extends SailRepository {

	public GeneralDBSailRepository(Sail sail) {
		super(sail);
	}
	
	@Override
	public SailRepositoryConnection getConnection() throws RepositoryException {
		try {
			return new GeneralDBSailRepositoryConnection(this, getSail().getConnection());
		}
		catch (SailException e) {
			throw new RepositoryException(e);
		}
	}
}
