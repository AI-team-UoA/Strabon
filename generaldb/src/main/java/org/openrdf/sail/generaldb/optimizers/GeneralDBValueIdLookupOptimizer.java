/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.generaldb.optimizers;

import org.openrdf.query.BindingSet;
import org.openrdf.query.Dataset;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.Var;
import org.openrdf.query.algebra.evaluation.QueryOptimizer;
import org.openrdf.query.algebra.helpers.QueryModelVisitorBase;
import org.openrdf.sail.generaldb.GeneralDBValueFactory;

/**
 * Iterates through the query and converting the values into RDBMS values.
 * 
 * @author James Leigh
 * 
 */
public class GeneralDBValueIdLookupOptimizer implements QueryOptimizer {

	GeneralDBValueFactory vf;

	public GeneralDBValueIdLookupOptimizer(GeneralDBValueFactory vf) {
		super();
		this.vf = vf;
	}

	public void optimize(TupleExpr tupleExpr, Dataset dataset, BindingSet bindings) {
		tupleExpr.visit(new VarVisitor());
	}

	protected class VarVisitor extends QueryModelVisitorBase<RuntimeException> {

		@Override
		public void meet(Var var) {
			if (var.hasValue()) {
				var.setValue(vf.asRdbmsValue(var.getValue()));
			}
		}
	}
}
