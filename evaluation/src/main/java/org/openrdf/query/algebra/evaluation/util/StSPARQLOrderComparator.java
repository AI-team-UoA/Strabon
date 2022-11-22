/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * 
 * Copyright (C) 2010, 2011, 2012, Pyravlos Team
 * 
 * http://www.strabon.di.uoa.gr/
 */
package org.openrdf.query.algebra.evaluation.util;

import java.util.Comparator;

import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.algebra.BindingSetAssignment;
import org.openrdf.query.algebra.FunctionCall;
import org.openrdf.query.algebra.Order;
import org.openrdf.query.algebra.OrderElem;
import org.openrdf.query.algebra.ValueExpr;
import org.openrdf.query.algebra.Var;
import org.openrdf.query.algebra.evaluation.EvaluationStrategy;
import org.openrdf.query.algebra.evaluation.QueryBindingSet;
import org.openrdf.query.algebra.evaluation.ValueExprEvaluationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.earthobservatory.constants.GeoConstants;

/**
 * @author Manos Karpathiotakis <mk@di.uoa.gr>
 * @author Dimitrianos Savva <dimis@di.uoa.gr>
 */
public class StSPARQLOrderComparator implements Comparator<BindingSet> {

	private final Logger logger = LoggerFactory.getLogger(StSPARQLOrderComparator.class);

	private final EvaluationStrategy strategy;

	private final Order order;

	private final Comparator cmp;

	public StSPARQLOrderComparator(EvaluationStrategy strategy, Order order, Comparator vcmp) {
		this.strategy = strategy;
		this.order = order;
		this.cmp = vcmp;
	}

	public int compare(BindingSet o1, BindingSet o2) {
		try {
			for (OrderElem element : order.getElements()) {
				//Flag used to denote a binding brought to compare two Polyhedra will be used
				boolean mbbFlag = false;

				Value v1;
				Value v2;
				if(element.getExpr() instanceof FunctionCall)
				{
					FunctionCall fc = (FunctionCall) element.getExpr();
					if(fc.getURI().equals(GeoConstants.stSPARQLenvelope) && fc.getArgs().size()==2)
					{
						mbbFlag = true;
						FunctionCall expr = (FunctionCall) element.getExpr();
						//I know it is a var cause I 'planted' it earlier
						Var lastArg = (Var) fc.getArgs().get(1);
						String bindingName = lastArg.getName();
						
						//avoid function encapsulation @see GeneralDBSelectQueryOptimizer meet(Order)
						if(bindingName.startsWith("-mbb-"))
						{
							//get the encapsulated function 
							v1=evaluate(expr.getArgs().get(0), o1);
							v2=evaluate(expr.getArgs().get(0), o2);
						}
						else
						{
							v1 = o1.getValue(bindingName);
							v2 = o2.getValue(bindingName);
						}
						
						
						//XXX unfinished
						
						int compare = cmp.compare(v1, v2);

						if (compare != 0) {
							return element.isAscending() ? compare : -compare;
						}
					}
				}

				if(!mbbFlag)
				{
					v1 = evaluate(element.getExpr(), o1);
					v2 = evaluate(element.getExpr(), o2);


					int compare = cmp.compare(v1, v2);

					if (compare != 0) {
						return element.isAscending() ? compare : -compare;
					}
				}
			}
			return 0;
		}
		catch (QueryEvaluationException e) {
			logger.error(e.getMessage(), e);
			return 0;
		}
		catch (IllegalArgumentException e) {
			logger.error(e.getMessage(), e);
			return 0;
		}
	}

	private Value evaluate(ValueExpr valueExpr, BindingSet o)
	throws QueryEvaluationException
	{
		try {
			return strategy.evaluate(valueExpr, o);
		}
		catch (ValueExprEvaluationException exc) {
			return null;
		}
	}
}
