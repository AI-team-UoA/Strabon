/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * 
 * Copyright (C) 2010, 2011, 2012, Pyravlos Team
 * 
 * http://www.strabon.di.uoa.gr/
 */
//package org.openrdf.query.algebra.evaluation.impl;
//
//import java.util.List;
//
//import org.openrdf.model.Value;
//import org.openrdf.query.BindingSet;
//import org.openrdf.query.Dataset;
// 
//import org.openrdf.query.algebra.FunctionCall; 
//import org.openrdf.query.algebra.QueryModel;
//import org.openrdf.query.algebra.TupleExpr;
//import org.openrdf.query.algebra.ValueExpr;
//import org.openrdf.query.algebra.evaluation.QueryOptimizer;
//import org.openrdf.query.algebra.evaluation.function.spatial.mbbIntersectsFunc;
//import org.openrdf.query.algebra.evaluation.function.spatial.ContainsFunc;
//import org.openrdf.query.algebra.evaluation.function.spatial.CoveredByFunc;
//import org.openrdf.query.algebra.evaluation.function.spatial.CoversFunc;
//import org.openrdf.query.algebra.evaluation.function.spatial.DisjointFunc;
//import org.openrdf.query.algebra.evaluation.function.spatial.EqualsFunc;
//import org.openrdf.query.algebra.evaluation.function.Function;
//import org.openrdf.query.algebra.evaluation.function.FunctionRegistry;
//import org.openrdf.query.algebra.evaluation.function.spatial.InsideFunc;
//import org.openrdf.query.algebra.evaluation.function.spatial.OverlapFunc;
//import org.openrdf.query.algebra.evaluation.impl.CompareOptimizer.CompareVisitor;
//import org.openrdf.query.algebra.evaluation.util.QueryOptimizerList;
//import org.openrdf.query.algebra.helpers.QueryModelVisitorBase; 
//
// 
//
//
//public class FunctionCallsOptimizer implements QueryOptimizer  {
//
//
//
//
//	public void optimize(QueryModel query, BindingSet bindings)
//	{
//		query.visit(new FunctionCallVisitor());
//		
//	}
//
//	protected class FunctionCallVisitor extends QueryModelVisitorBase<RuntimeException>
//	{
//		@Override
//		public void meet(FunctionCall functionCall)
//		{
//			super.meet(functionCall);
//
//			Function function = FunctionRegistry.getInstance().get(functionCall.getURI());
//
//			if (function == null) {
//				try 
//				{
//					throw new Exception("Unknown function '" + functionCall.getURI() + "'");
//				} 
//				catch (Exception e) 
//				{
//					e.printStackTrace();
//				}
//
//			}
//
//			List<ValueExpr> args = functionCall.getArgs();
//			
//			if(function instanceof mbbIntersectsFunc)
//			{
//				ValueExpr left = args.get(0);
//				ValueExpr right = args.get(1);
//		
//				//PgSqlExprSupport.mbbIntersects(left,right);
//			}
////FIXME this must be fixed			
////			SpatialTopoOperator operator;
////			if(function instanceof mbbIntersectsFunc)
////				operator = new mbbIntersects(args.get(0),args.get(1));
////			else if(function instanceof ContainsFunc)
////				operator = new Contains(args.get(0),args.get(1));
////			else if(function instanceof CoveredByFunc)
////				operator = new CoveredBy(args.get(0),args.get(1));
////			else if(function instanceof CoversFunc)
////				operator = new Covers(args.get(0),args.get(1));
////			else if(function instanceof DisjointFunc)
////				operator = new Disjoint(args.get(0),args.get(1));
////			else if(function instanceof EqualsFunc)
////				operator = new Equals(args.get(0),args.get(1));
////			else if(function instanceof InsideFunc)
////				operator = new Inside(args.get(0),args.get(1));
////			else if(function instanceof OverlapFunc)
////				operator = new Overlap(args.get(0),args.get(1));
////			else //TouchFunc
////				operator = new Touch(args.get(0),args.get(1));
//
////			functionCall.replaceWith(operator);
//		}
//	}
//
//
//}
