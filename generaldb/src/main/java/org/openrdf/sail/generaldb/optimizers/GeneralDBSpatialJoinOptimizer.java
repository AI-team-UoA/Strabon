/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.generaldb.optimizers;

import java.util.ArrayList;
import java.util.List;

import org.openrdf.query.BindingSet;
import org.openrdf.query.Dataset;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.evaluation.QueryOptimizer;

import org.openrdf.sail.generaldb.algebra.GeneralDBJoinItem;
import org.openrdf.sail.generaldb.algebra.base.GeneralDBFromItem;
import org.openrdf.sail.generaldb.algebra.base.GeneralDBQueryModelVisitorBase;
import org.openrdf.sail.generaldb.algebra.base.GeneralDBSqlExpr;


/**
 * 
 * @author manolee
 * Only responsibility of this optimizer: Append spatial filters to the last occurrence of geo_values
 */
//XXX not used currently. may be reused - altered in a bit. 09/09/2011
public class GeneralDBSpatialJoinOptimizer extends GeneralDBQueryModelVisitorBase<RuntimeException> implements
QueryOptimizer
{
	private int spatialCounter = 0;
	private int threshold = 0;
	private List<GeneralDBSqlExpr> spatialJoins = new ArrayList<GeneralDBSqlExpr>();

	public void optimize(TupleExpr tupleExpr, Dataset dataset, BindingSet bindings, 
			int spatialJoinsNo, List<GeneralDBSqlExpr> toBeAppended) {
		threshold = spatialJoinsNo;
		spatialJoins = toBeAppended;
		optimize(tupleExpr, dataset, bindings);
	}

	public void optimize(TupleExpr tupleExpr, Dataset dataset, BindingSet bindings) {
		tupleExpr.visit(this);
	}

	@Override
	public void meetFromItem(GeneralDBFromItem node)
	throws RuntimeException
	{
		if(node instanceof GeneralDBJoinItem)
		{
			if(((GeneralDBJoinItem) node).getTableName().equals("geo_values"))
			{
				spatialCounter++;
				if(spatialCounter >= threshold)
				{
					for(GeneralDBSqlExpr expr : spatialJoins)
					{
						node.addFilter(expr);

					}
				}
			}
		}
		node.visitChildren(this);
	}



}
