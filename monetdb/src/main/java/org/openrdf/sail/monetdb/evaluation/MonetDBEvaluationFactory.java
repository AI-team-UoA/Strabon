package org.openrdf.sail.monetdb.evaluation;

import org.openrdf.query.Dataset;
import org.openrdf.sail.generaldb.evaluation.GeneralDBEvaluation;
import org.openrdf.sail.generaldb.evaluation.GeneralDBEvaluationFactory;

public class MonetDBEvaluationFactory extends GeneralDBEvaluationFactory{

	@Override
	public GeneralDBEvaluation createRdbmsEvaluation(Dataset dataset) {
		return new MonetDBEvaluation(factory, triples, dataset, ids);
	}
}
