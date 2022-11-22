/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.generaldb.evaluation;

import org.openrdf.query.Dataset;
import org.openrdf.sail.generaldb.GeneralDBTripleRepository;
import org.openrdf.sail.generaldb.schema.IdSequence;

/**
 * Creates an {@link GeneralDBEvaluation}.
 * 
 * @author James Leigh
 * 
 */
public abstract class GeneralDBEvaluationFactory {

	protected GeneralDBQueryBuilderFactory factory;

	protected GeneralDBTripleRepository triples;

	protected IdSequence ids;

	public void setQueryBuilderFactory(GeneralDBQueryBuilderFactory factory) {
		this.factory = factory;
	}

	public void setRdbmsTripleRepository(GeneralDBTripleRepository triples) {
		this.triples = triples;
	}

	public void setIdSequence(IdSequence ids) {
		this.ids = ids;
	}

	public abstract GeneralDBEvaluation createRdbmsEvaluation(Dataset dataset);
}
