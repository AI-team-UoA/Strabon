/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.generaldb.evaluation;

import info.aduna.iteration.CloseableIteration;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.algebra.evaluation.TripleSource;
import org.openrdf.sail.SailException;
import org.openrdf.sail.generaldb.GeneralDBTripleRepository;
import org.openrdf.sail.generaldb.GeneralDBValueFactory;
import org.openrdf.sail.rdbms.model.RdbmsResource;
import org.openrdf.sail.rdbms.model.RdbmsURI;
import org.openrdf.sail.rdbms.model.RdbmsValue;

/**
 * Proxies request to a {@link GeneralDBTripleRepository}.
 * 
 * @author James Leigh
 * 
 */
public class GeneralDBTripleSource implements TripleSource {

	private GeneralDBTripleRepository triples;

	public GeneralDBTripleSource(GeneralDBTripleRepository triples) {
		super();
		this.triples = triples;
	}

	@Override
	public GeneralDBValueFactory getValueFactory() {
		return triples.getValueFactory();
	}

	@Override
	public CloseableIteration getStatements(Resource subj, URI pred, Value obj, Resource... contexts)
		throws QueryEvaluationException
	{
		try {
			GeneralDBValueFactory vf = triples.getValueFactory();
			RdbmsResource s = vf.asRdbmsResource(subj);
			RdbmsURI p = vf.asRdbmsURI(pred);
			RdbmsValue o = vf.asRdbmsValue(obj);
			RdbmsResource[] c = vf.asRdbmsResource(contexts);
			return triples.find(s, p, o, c);
		}
		catch (SailException e) {
			throw new QueryEvaluationException(e);
		}
	}

}
