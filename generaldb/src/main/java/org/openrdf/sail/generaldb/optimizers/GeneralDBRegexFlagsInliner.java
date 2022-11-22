/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.generaldb.optimizers;

import org.openrdf.query.BindingSet;
import org.openrdf.query.Dataset;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.evaluation.QueryOptimizer;
import org.openrdf.sail.generaldb.algebra.GeneralDBSqlConcat;
import org.openrdf.sail.generaldb.algebra.GeneralDBSqlNull;
import org.openrdf.sail.generaldb.algebra.GeneralDBSqlRegex;
import org.openrdf.sail.generaldb.algebra.GeneralDBStringValue;
import org.openrdf.sail.generaldb.algebra.base.GeneralDBQueryModelVisitorBase;
import org.openrdf.sail.generaldb.algebra.base.GeneralDBSqlExpr;

/**
 * Moves the regular expression flags into the pattern string as per the
 * PostgreSQL syntax.
 * 
 * @author James Leigh
 * 
 */
public class GeneralDBRegexFlagsInliner extends GeneralDBQueryModelVisitorBase<RuntimeException> implements
		QueryOptimizer
{

	public void optimize(TupleExpr tupleExpr, Dataset dataset, BindingSet bindings) {
		tupleExpr.visit(this);
	}

	@Override
	public void meet(GeneralDBSqlRegex node)
		throws RuntimeException
	{
		super.meet(node);
		GeneralDBSqlExpr flags = node.getFlagsArg();
		if (!(flags instanceof GeneralDBSqlNull)) {
			GeneralDBSqlExpr pattern = node.getPatternArg();
			GeneralDBSqlExpr prefix = concat(str("(?"), flags, str(")"));
			pattern.replaceWith(concat(prefix, pattern.clone()));
			node.setFlagsArg(null);
		}
	}

	private GeneralDBSqlExpr str(String string) {
		return new GeneralDBStringValue(string);
	}

	private GeneralDBSqlExpr concat(GeneralDBSqlExpr... exprs) {
		GeneralDBSqlExpr concat = null;
		for (GeneralDBSqlExpr expr : exprs) {
			if (concat == null) {
				concat = expr;
			}
			else {
				concat = new GeneralDBSqlConcat(concat, expr);
			}
		}
		return concat;
	}
}
