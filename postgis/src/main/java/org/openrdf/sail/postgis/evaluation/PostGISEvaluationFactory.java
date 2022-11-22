/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * 
 * Copyright (C) 2010, 2011, 2012, 2013 Pyravlos Team
 * 
 * http://www.strabon.di.uoa.gr/
 */
package org.openrdf.sail.postgis.evaluation;

import org.openrdf.query.Dataset;
import org.openrdf.sail.generaldb.evaluation.GeneralDBEvaluation;
import org.openrdf.sail.generaldb.evaluation.GeneralDBEvaluationFactory;

public class PostGISEvaluationFactory extends GeneralDBEvaluationFactory{

	@Override
	public GeneralDBEvaluation createRdbmsEvaluation(Dataset dataset) {
		return new PostGISEvaluation(factory, triples, dataset, ids);
	}
}
