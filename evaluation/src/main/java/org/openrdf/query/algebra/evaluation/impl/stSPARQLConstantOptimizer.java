/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * 
 * Copyright (C) 2010, 2011, 2012, Pyravlos Team
 * 
 * http://www.strabon.di.uoa.gr/
 */
package org.openrdf.query.algebra.evaluation.impl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.openrdf.model.Value;
import org.openrdf.model.impl.BooleanLiteralImpl;
import org.openrdf.query.BindingSet;
import org.openrdf.query.Dataset;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.algebra.And;
import org.openrdf.query.algebra.BinaryValueOperator;
import org.openrdf.query.algebra.Bound;
import org.openrdf.query.algebra.FunctionCall;
import org.openrdf.query.algebra.Or;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.UnaryValueOperator;
import org.openrdf.query.algebra.ValueConstant;
import org.openrdf.query.algebra.ValueExpr;
import org.openrdf.query.algebra.Var;
import org.openrdf.query.algebra.evaluation.EvaluationStrategy;
import org.openrdf.query.algebra.evaluation.QueryOptimizer;
import org.openrdf.query.algebra.evaluation.ValueExprEvaluationException;
import org.openrdf.query.algebra.evaluation.function.Function;
import org.openrdf.query.algebra.evaluation.function.FunctionRegistry;
import org.openrdf.query.algebra.evaluation.function.spatial.DateTimeMetricFunc;
import org.openrdf.query.algebra.evaluation.function.spatial.SpatialConstructFunc;
import org.openrdf.query.algebra.evaluation.function.spatial.SpatialMetricFunc;
import org.openrdf.query.algebra.evaluation.function.spatial.SpatialPropertyFunc;
import org.openrdf.query.algebra.evaluation.function.spatial.SpatialRelationshipFunc;
import org.openrdf.query.algebra.helpers.QueryModelVisitorBase;
import org.openrdf.query.impl.EmptyBindingSet;

/**
 * A query optimizer that optimizes constant value expressions for stSPARQL.
 * 
 * @author Manos Karpathiotakis <mk@di.uoa.gr>
 */
public class stSPARQLConstantOptimizer implements QueryOptimizer {

	protected final Logger logger = LoggerFactory.getLogger(this.getClass());

	protected final EvaluationStrategy strategy;

	public stSPARQLConstantOptimizer(EvaluationStrategy strategy) {
		this.strategy = strategy;
	}

	/**
	 * Applies generally applicable optimizations to the supplied query: variable
	 * assignments are inlined.
	 */
	public void optimize(TupleExpr tupleExpr, Dataset dataset, BindingSet bindings) {
		tupleExpr.visit(new ConstantVisitor());
	}

	protected class ConstantVisitor extends QueryModelVisitorBase<RuntimeException> {

		@Override
		public void meet(Or or)
		{
			or.visitChildren(this);

			try {
				if (isConstant(or.getLeftArg()) && isConstant(or.getRightArg())) {
					boolean value = strategy.isTrue(or, EmptyBindingSet.getInstance());
					or.replaceWith(new ValueConstant(BooleanLiteralImpl.valueOf(value)));
				}
				else if (isConstant(or.getLeftArg())) {
					boolean leftIsTrue = strategy.isTrue(or.getLeftArg(), EmptyBindingSet.getInstance());
					if (leftIsTrue) {
						or.replaceWith(new ValueConstant(BooleanLiteralImpl.TRUE));
					}
					else {
						or.replaceWith(or.getRightArg());
					}
				}
				else if (isConstant(or.getRightArg())) {
					boolean rightIsTrue = strategy.isTrue(or.getRightArg(), EmptyBindingSet.getInstance());
					if (rightIsTrue) {
						or.replaceWith(new ValueConstant(BooleanLiteralImpl.TRUE));
					}
					else {
						or.replaceWith(or.getLeftArg());
					}
				}
			}
			catch (ValueExprEvaluationException e) {
				// TODO: incompatible values types(?), remove the affected part of the query tree
				logger.error("Failed to evaluate BinaryValueOperator with two constant arguments", e);
			}
			catch (QueryEvaluationException e) {
				logger.error("Query evaluation exception caught", e);
			}
		}

		@Override
		public void meet(And and)
		{
			and.visitChildren(this);

			try {
				if (isConstant(and.getLeftArg()) && isConstant(and.getRightArg())) {
					boolean value = strategy.isTrue(and, EmptyBindingSet.getInstance());
					and.replaceWith(new ValueConstant(BooleanLiteralImpl.valueOf(value)));
				}
				else if (isConstant(and.getLeftArg())) {
					boolean leftIsTrue = strategy.isTrue(and.getLeftArg(), EmptyBindingSet.getInstance());
					if (leftIsTrue) {
						and.replaceWith(and.getRightArg());
					}
					else {
						and.replaceWith(new ValueConstant(BooleanLiteralImpl.FALSE));
					}
				}
				else if (isConstant(and.getRightArg())) {
					boolean rightIsTrue = strategy.isTrue(and.getRightArg(), EmptyBindingSet.getInstance());
					if (rightIsTrue) {
						and.replaceWith(and.getLeftArg());
					}
					else {
						and.replaceWith(new ValueConstant(BooleanLiteralImpl.FALSE));
					}
				}
			}
			catch (ValueExprEvaluationException e) {
				// TODO: incompatible values types(?), remove the affected part of the query tree
				logger.error("Failed to evaluate BinaryValueOperator with two constant arguments", e);
			}
			catch (QueryEvaluationException e) {
				logger.error("Query evaluation exception caught", e);
			}
		}

		@Override
		protected void meetBinaryValueOperator(BinaryValueOperator binaryValueOp)
		{
			super.meetBinaryValueOperator(binaryValueOp);

			if (isConstant(binaryValueOp.getLeftArg()) && isConstant(binaryValueOp.getRightArg())) {
				try {
					Value value = strategy.evaluate(binaryValueOp, EmptyBindingSet.getInstance());
					binaryValueOp.replaceWith(new ValueConstant(value));
				}
				catch (ValueExprEvaluationException e) {
					// TODO: incompatible values types(?), remove the affected part of the query tree
					logger.error("Failed to evaluate BinaryValueOperator with two constant arguments", e);
				}
				catch (QueryEvaluationException e) {
					logger.error("Query evaluation exception caught", e);
				}
			}
		}

		@Override
		protected void meetUnaryValueOperator(UnaryValueOperator unaryValueOp)
		{
			super.meetUnaryValueOperator(unaryValueOp);

			if (isConstant(unaryValueOp.getArg())) {
				try {
					Value value = strategy.evaluate(unaryValueOp, EmptyBindingSet.getInstance());
					unaryValueOp.replaceWith(new ValueConstant(value));
				}
				catch (ValueExprEvaluationException e) {
					// TODO: incompatible values types(?), remove the affected part of the query tree
					logger.error("Failed to evaluate UnaryValueOperator with a constant argument", e);
				}
				catch (QueryEvaluationException e) {
					logger.error("Query evaluation exception caught", e);
				}
			}
		}

		@Override
		public void meet(FunctionCall functionCall)
		{
			Function function = FunctionRegistry.getInstance().get(functionCall.getURI());
			if(!(function instanceof SpatialConstructFunc) && 
					!(function instanceof SpatialMetricFunc) &&
					!(function instanceof SpatialPropertyFunc) &&
					!(function instanceof SpatialRelationshipFunc) 
					/**
					 * Addition for datetime metric functions
					 * 
					 * @author George Garbis <ggarbis@di.uoa.gr>
					 * 
					 */
					&& !(function instanceof DateTimeMetricFunc)
				)
			{
				super.meet(functionCall);

				List<ValueExpr> args = functionCall.getArgs();
				for (ValueExpr arg : args) {
					if (!isConstant(arg)) {
						return;
					}
				}

				// All arguments are constant

				try {
					Value value = strategy.evaluate(functionCall, EmptyBindingSet.getInstance());
					functionCall.replaceWith(new ValueConstant(value));
				}
				catch (ValueExprEvaluationException e) {
					// TODO: incompatible values types(?), remove the affected part of the query tree
					logger.error("Failed to evaluate BinaryValueOperator with two constant arguments", e);
				}
				catch (QueryEvaluationException e) {
					logger.error("Query evaluation exception caught", e);
				}
			}
		}

		@Override
		public void meet(Bound bound)
		{
			super.meet(bound);

			if (bound.getArg().hasValue()) {
				// variable is always bound
				bound.replaceWith(new ValueConstant(BooleanLiteralImpl.TRUE));
			}
		}

		private boolean isConstant(ValueExpr expr) {
			return expr instanceof ValueConstant || expr instanceof Var && ((Var)expr).hasValue();
		}
	}
}
