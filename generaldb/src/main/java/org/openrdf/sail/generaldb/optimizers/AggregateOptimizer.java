/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * Copyright (C) 2010, 2011, 2012, Pyravlos Team
 *
 * http://www.strabon.di.uoa.gr/
 */
package org.openrdf.sail.generaldb.optimizers;

import java.util.Iterator;

import org.openrdf.query.algebra.Extension;
import org.openrdf.query.algebra.ExtensionElem;
import org.openrdf.query.algebra.FunctionCall;
import org.openrdf.query.algebra.Group;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.ValueExpr;
import org.openrdf.query.algebra.evaluation.function.Function;
import org.openrdf.query.algebra.evaluation.function.FunctionRegistry;
import org.openrdf.query.algebra.evaluation.function.spatial.stsparql.aggregate.ExtentFunc;
import org.openrdf.query.algebra.evaluation.function.spatial.stsparql.construct.IntersectionFunc;
import org.openrdf.query.algebra.evaluation.function.spatial.stsparql.construct.UnionFunc;
import org.openrdf.sail.generaldb.algebra.base.GeneralDBQueryModelVisitorBase;
/**
 *
 * @author Stella Giannakopoulou <sgian@di.uoa.gr>
 */

public class AggregateOptimizer extends GeneralDBQueryModelVisitorBase<RuntimeException>
{
	public void optimize(TupleExpr tupleExpr)
	{
		tupleExpr.visit(this);
	}

	@Override
	public void meet(Extension node) throws RuntimeException
	{
		if(!(node.getArg() instanceof Group))
		{
			Iterator<ExtensionElem> iter = node.getElements().iterator();

			while(iter.hasNext())
			{
				ExtensionElem elem = iter.next();
				ValueExpr expr = elem.getExpr();

				if(aggregateInQuery(expr) == true) //Union (or Extent) is used as an aggregate function on Select Clause!
				{
					Group group = new Group((TupleExpr) node.getArg());
					group.setParentNode(node.getArg().getParentNode());
					node.replaceChildNode(node.getArg(), group);

					break;
				}
			}
		}
	}


	private boolean aggregateInQuery(ValueExpr expr)
	{
		if(expr instanceof FunctionCall)
		{
			Function function = FunctionRegistry.getInstance().get(((FunctionCall) expr).getURI());
			if((!(function instanceof UnionFunc) || !(((FunctionCall) expr).getArgs().size()==1))
					&& (!(function instanceof IntersectionFunc) || !(((FunctionCall) expr).getArgs().size()==1))
					&&!(function instanceof ExtentFunc))
			{
				//Recursively check arguments
				boolean aggregatePresent = false;
				for(int i = 0 ; i< ((FunctionCall) expr).getArgs().size(); i++)
				{
					aggregatePresent = aggregatePresent || aggregateInQuery(((FunctionCall) expr).getArgs().get(i));
				}
				return aggregatePresent;
			}
			else
				return true;
		}
		else //var
		{
			return false;
		}
	}
}
