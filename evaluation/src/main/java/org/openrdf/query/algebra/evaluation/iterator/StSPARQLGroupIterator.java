/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * 
 * Copyright (C) 2010, 2011, 2012, Pyravlos Team
 * 
 * http://www.strabon.di.uoa.gr/
 */
package org.openrdf.query.algebra.evaluation.iterator;

import info.aduna.iteration.CloseableIteration;
import info.aduna.iteration.CloseableIteratorIteration;
import info.aduna.lang.ObjectUtil;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.openrdf.model.Literal;
import org.openrdf.model.Value;
import org.openrdf.model.datatypes.XMLDatatypeUtil;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.algebra.AggregateOperator;
import org.openrdf.query.algebra.AggregateOperatorBase;
import org.openrdf.query.algebra.Avg;
import org.openrdf.query.algebra.Compare;
import org.openrdf.query.algebra.Count;
import org.openrdf.query.algebra.FunctionCall;
import org.openrdf.query.algebra.Group;
import org.openrdf.query.algebra.GroupConcat;
import org.openrdf.query.algebra.GroupElem;
import org.openrdf.query.algebra.MathExpr.MathOp;
import org.openrdf.query.algebra.Max;
import org.openrdf.query.algebra.Min;
import org.openrdf.query.algebra.Sample;
import org.openrdf.query.algebra.Sum;
import org.openrdf.query.algebra.ValueExpr;
import org.openrdf.query.algebra.Var;
import org.openrdf.query.algebra.evaluation.EvaluationStrategy;
import org.openrdf.query.algebra.evaluation.QueryBindingSet;
import org.openrdf.query.algebra.evaluation.ValueExprEvaluationException;
import org.openrdf.query.algebra.evaluation.function.Function;
import org.openrdf.query.algebra.evaluation.function.FunctionRegistry;
import org.openrdf.query.algebra.evaluation.function.spatial.GeometryDatatype;
import org.openrdf.query.algebra.evaluation.function.spatial.StrabonPolyhedron;
import org.openrdf.query.algebra.evaluation.function.spatial.WKTHelper;
import org.openrdf.query.algebra.evaluation.function.spatial.stsparql.aggregate.ExtentFunc;
import org.openrdf.query.algebra.evaluation.function.spatial.stsparql.construct.BoundaryFunc;
import org.openrdf.query.algebra.evaluation.function.spatial.stsparql.construct.BufferFunc;
import org.openrdf.query.algebra.evaluation.function.spatial.stsparql.construct.ConvexHullFunc;
import org.openrdf.query.algebra.evaluation.function.spatial.stsparql.construct.DifferenceFunc;
import org.openrdf.query.algebra.evaluation.function.spatial.stsparql.construct.EnvelopeFunc;
import org.openrdf.query.algebra.evaluation.function.spatial.stsparql.construct.IntersectionFunc;
import org.openrdf.query.algebra.evaluation.function.spatial.stsparql.construct.SymDifferenceFunc;
import org.openrdf.query.algebra.evaluation.function.spatial.stsparql.construct.TransformFunc;
import org.openrdf.query.algebra.evaluation.function.spatial.stsparql.construct.UnionFunc;
import org.openrdf.query.algebra.evaluation.function.spatial.stsparql.metric.AreaFunc;
import org.openrdf.query.algebra.evaluation.function.spatial.stsparql.metric.DistanceFunc;
import org.openrdf.query.algebra.evaluation.util.MathUtil;
import org.openrdf.query.algebra.evaluation.util.ValueComparator;
import org.openrdf.query.impl.EmptyBindingSet;

import com.vividsolutions.jts.geom.Geometry;

import eu.earthobservatory.constants.GeoConstants;

/**
 * 
 * @author Manos Karpathiotakis <mk@di.uoa.gr>
 */
public class StSPARQLGroupIterator extends CloseableIteratorIteration<BindingSet, QueryEvaluationException> {

	/*-----------*
	 * Constants *
	 *-----------*/

	private final ValueFactoryImpl vf = ValueFactoryImpl.getInstance();

	private final EvaluationStrategy strategy;

	private final BindingSet parentBindings;

	private final Group group;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public StSPARQLGroupIterator(EvaluationStrategy strategy, Group group, BindingSet parentBindings)
		throws QueryEvaluationException
	{
		this.strategy = strategy;
		this.group = group;
		this.parentBindings = parentBindings;
		super.setIterator(createIterator());
	}

	/*---------*
	 * Methods *
	 *---------*/

	private Iterator<BindingSet> createIterator()
		throws QueryEvaluationException
	{
		Collection<Entry> entries = buildEntries();
		Collection<BindingSet> bindingSets = new LinkedList<BindingSet>();

		for (Entry entry : entries) {
			QueryBindingSet sol = new QueryBindingSet(parentBindings);

			for (String name : group.getGroupBindingNames()) {
				BindingSet prototype = entry.getPrototype();
				if (prototype != null) {
					Value value = prototype.getValue(name);
					if (value != null) {
						// Potentially overwrites bindings from super
						sol.setBinding(name, value);
					}
				}
			}
			//XXX added bindings to enable the evaluation of order by!! 
			//Must probably find a way to remove them afterwards!!
			for(String name : entry.getPrototype().getBindingNames())
			{
				if(name.startsWith("-mbb-"))
				{
					sol.setBinding(name,entry.getPrototype().getValue(name));
				}
			}

			entry.bindSolution(sol);

			bindingSets.add(sol);
		}

		return bindingSets.iterator();
	}

	private Collection<Entry> buildEntries()
		throws QueryEvaluationException
	{
		CloseableIteration<BindingSet, QueryEvaluationException> iter;
		iter = strategy.evaluate(group.getArg(), parentBindings);

		try {
			Map<Key, Entry> entries = new LinkedHashMap<Key, Entry>();

			if (!iter.hasNext()) {
				// no solutions, still need to process aggregates to produce a
				// zero-result.
				entries.put(new Key(new EmptyBindingSet()), new Entry(new EmptyBindingSet()));
			}

			while (iter.hasNext()) {
				BindingSet sol;
				try {
					sol = iter.next();
				}
				catch (NoSuchElementException e) {
					break; // closed
				}
				Key key = new Key(sol);
				Entry entry = entries.get(key);

				if (entry == null) {
					entry = new Entry(sol);
					entries.put(key, entry);
				}

				entry.addSolution(sol);
			}

			return entries.values();
		}
		finally {
			iter.close();
		}

	}

	/**
	 * A unique key for a set of existing bindings.
	 * 
	 * @author David Huynh
	 */
	protected class Key {

		private BindingSet bindingSet;

		private int hash;

		public Key(BindingSet bindingSet) {
			this.bindingSet = bindingSet;

			for (String name : group.getGroupBindingNames()) {
				Value value = bindingSet.getValue(name);
				if (value != null) {
					this.hash ^= value.hashCode();
				}
			}
		}

		@Override
		public int hashCode() {
			return hash;
		}

		@Override
		public boolean equals(Object other) {
			if (other instanceof Key && other.hashCode() == hash) {
				BindingSet otherSolution = ((Key)other).bindingSet;

				for (String name : group.getGroupBindingNames()) {
					Value v1 = bindingSet.getValue(name);
					Value v2 = otherSolution.getValue(name);

					if (!ObjectUtil.nullEquals(v1, v2)) {
						return false;
					}
				}

				return true;
			}

			return false;
		}
	}

	private class Entry {

		private BindingSet prototype;

		private Map<String, Aggregate> aggregates;

		private Map<String, FunctionCall> spatialAggregates;

		private Map<FunctionCall, Geometry> spatialAggregatesResult;
		
		/**
		 * Map holding the datatypes of the geometries. We do not use
		 * a single map (like StrabonPolyhedron) for safety reasons of
		 * the implementation of hashCode(), equals(), etc. We need this
		 * to retain the datatype of the expression (or the constants) and
		 * pass it to the final result. Hence, the output will be consistent
		 * with the datatype of the input. 
		 */
		private Map<FunctionCall, GeometryDatatype> spatialAggregatesResultDatatype;

		public Entry(BindingSet prototype)
			throws ValueExprEvaluationException, QueryEvaluationException
		{
			this.prototype = prototype;
			this.aggregates = new LinkedHashMap<String, Aggregate>();
			this.spatialAggregates = new LinkedHashMap<String, FunctionCall>();
			this.spatialAggregatesResult = new LinkedHashMap<FunctionCall, Geometry>();
			this.spatialAggregatesResultDatatype = new LinkedHashMap<FunctionCall, GeometryDatatype>();

			for (GroupElem ge : group.getGroupElements()) {
				if(ge.getName().endsWith("-aggregateInside-"))
				{
					//System.out.println("Placeholder");
					String name = ge.getName();
					if(!ge.getName().startsWith("havingCondition"))
					{
						name = name.replace("-aggregateInside-","");
					}
					if(((Avg) ge.getOperator()).getArg() instanceof FunctionCall)
					{
						spatialAggregates.put(name, (FunctionCall) ((Avg)ge.getOperator()).getArg());
					}
					if(((Avg) ge.getOperator()).getArg() instanceof Compare)
					{
						//						Compare tmp = (Compare) ((Avg) ge.getOperator()).getArg();
						//						if(tmp.getLeftArg() instanceof FunctionCall)
						//						{
						//							
						//						}
						//						if(tmp.getRightArg() instanceof FunctionCall)
						//						{
						//							
						//						}
					}

				}
				else
				{
					Aggregate create = create(ge.getOperator());
					if (create != null) {
						aggregates.put(ge.getName(), create);
					}
				}
			}
		}

		public BindingSet getPrototype() {
			return prototype;
		}

		public void addSolution(BindingSet bindingSet)
			throws QueryEvaluationException
		{
			for (Aggregate aggregate : aggregates.values()) {
				aggregate.processAggregate(bindingSet);
			}
			for (FunctionCall spatialAggregate : spatialAggregates.values()) {
				processSpatialAggregate(spatialAggregate, bindingSet);
				//spatialAggregates.processAggregate(bindingSet);
			}
		}

		public void bindSolution(QueryBindingSet sol)
			throws QueryEvaluationException
		{
			for (String name : aggregates.keySet()) {
				try {
					Value value = aggregates.get(name).getValue();
					if (value != null) {
						// Potentially overwrites bindings from super
						sol.setBinding(name, value);
					}
				}
				catch (ValueExprEvaluationException ex) {
					// There was a type error when calculating the value of the
					// aggregate.
					// We silently ignore the error, resulting in no result value
					// being bound.
				}
			}

			//			for(String name : spatialAggregates.keySet())
			//			{
			//				//Must compute the spatial construct at this point
			//				ValueExpr expr = spatialAggregates.get(name);
			//				
			//				
			//				//the names are no longer the same
			//				Geometry geom = spatialAggregatesResult.get(spatialAggregates.get(name));
			//				StrabonPolyhedron poly = null;
			//				try {
			//					poly = new StrabonPolyhedron(geom);
			//				} catch (Exception e) {
			//					e.printStackTrace();
			//				}
			//				sol.setBinding(name,poly);
			//
			//			}

			for(String name : spatialAggregates.keySet())
			{
				//Must compute the spatial construct at this point
				ValueExpr expr = spatialAggregates.get(name);

				Value val = null;
				try {
					val = evaluateConstruct(expr, this.prototype);
					} catch (Exception e) {
					e.printStackTrace();
				}
				if (val != null) {
					if(val instanceof StrabonPolyhedron)
					{ // TODO FIXME why we assume here strdf:WKT? Can we generalize?
						String label = WKTHelper.createWKT(val.toString(), 
														   ((StrabonPolyhedron)val).getGeometry().getSRID(), 
														   GeoConstants.WKT);
						Literal wkt = new LiteralImpl(label,new URIImpl(GeoConstants.WKT));
						sol.setBinding(name,wkt);
					}
					else
					{
						sol.setBinding(name, val);
					}
				}
			}

		}

		/**
		 * XXX addition
		 */

		/**
		 * Code added in order to evaluate spatial constructs present in select that contain a spatial aggregate (e.g. Union(?geo1))
		 * @param expr a spatial construct or a spatial var
		 * @param prototype the bindings needed to retrieve values for the Vars
		 * @return The evaluated stSPARQL construct
		 * @throws Exception 
		 */
		private Value evaluateConstruct(ValueExpr expr, BindingSet prototype) throws Exception
		{
			if(prototype instanceof EmptyBindingSet)
			{
				return null;
			}
			if(expr instanceof FunctionCall)
			{
				StrabonPolyhedron leftArg = null;
				StrabonPolyhedron rightArg = null;

				Function function = FunctionRegistry.getInstance().get(((FunctionCall) expr).getURI());

				if(function instanceof UnionFunc)
				{
					if(((FunctionCall) expr).getArgs().size()==1)
					{
						//Aggregate!!! => Value ready in spatialAggregatesResults
						return new StrabonPolyhedron(spatialAggregatesResult.get(expr),
													 spatialAggregatesResultDatatype.get(expr));
					}
					else
					{
						leftArg = (StrabonPolyhedron) evaluateConstruct(((FunctionCall) expr).getArgs().get(0),prototype);
						rightArg = (StrabonPolyhedron) evaluateConstruct(((FunctionCall) expr).getArgs().get(1),prototype);
						return StrabonPolyhedron.union(leftArg, rightArg);
					}
				}
				else if(function instanceof ExtentFunc)
				{
					//Aggregate!!! => Value ready in spatialAggregatesResults
					return new StrabonPolyhedron(spatialAggregatesResult.get(expr),
							 					 spatialAggregatesResultDatatype.get(expr));
				}
				else if(function instanceof BufferFunc)
				{
					//FIXME Still haven't run example when 2nd argument is a Var
					leftArg = (StrabonPolyhedron) evaluateConstruct(((FunctionCall) expr).getArgs().get(0),prototype);
					Value radius = strategy.evaluate(((FunctionCall) expr).getArgs().get(1),prototype);
					LiteralImpl lit = (LiteralImpl) radius;
					return StrabonPolyhedron.buffer(leftArg,lit.doubleValue());
				}
				else if(function instanceof TransformFunc)
				{
					leftArg = (StrabonPolyhedron) evaluateConstruct(((FunctionCall) expr).getArgs().get(0),prototype);
					Value sridCoarse = strategy.evaluate(((FunctionCall) expr).getArgs().get(1),prototype);
					URIImpl srid = (URIImpl) sridCoarse;
					return StrabonPolyhedron.transform(leftArg,srid);
				}
				else if(function instanceof EnvelopeFunc)
				{
					leftArg = (StrabonPolyhedron) evaluateConstruct(((FunctionCall) expr).getArgs().get(0),prototype);
					return StrabonPolyhedron.envelope(leftArg);
				}
				else if(function instanceof ConvexHullFunc)
				{
					leftArg = (StrabonPolyhedron) evaluateConstruct(((FunctionCall) expr).getArgs().get(0),prototype);
					return StrabonPolyhedron.convexHull(leftArg);
				}
				else if(function instanceof BoundaryFunc)
				{
					leftArg = (StrabonPolyhedron) evaluateConstruct(((FunctionCall) expr).getArgs().get(0),prototype);
					return StrabonPolyhedron.boundary(leftArg);
				}
				else if(function instanceof IntersectionFunc)
				{
					if(((FunctionCall) expr).getArgs().size()==1)
					{
						//Aggregate!!! => Value ready in spatialAggregatesResults
						return new StrabonPolyhedron(spatialAggregatesResult.get(expr),
								 					 spatialAggregatesResultDatatype.get(expr));
					}
					else
					{
						leftArg = (StrabonPolyhedron) evaluateConstruct(((FunctionCall) expr).getArgs().get(0),prototype);
						rightArg = (StrabonPolyhedron) evaluateConstruct(((FunctionCall) expr).getArgs().get(1),prototype);
						return StrabonPolyhedron.intersection(leftArg, rightArg);
					}
				}
				else if(function instanceof DifferenceFunc)
				{
					leftArg = (StrabonPolyhedron) evaluateConstruct(((FunctionCall) expr).getArgs().get(0),prototype);
					rightArg = (StrabonPolyhedron) evaluateConstruct(((FunctionCall) expr).getArgs().get(1),prototype);
					return StrabonPolyhedron.difference(leftArg, rightArg);
				}
				else if(function instanceof SymDifferenceFunc)
				{
					leftArg = (StrabonPolyhedron) evaluateConstruct(((FunctionCall) expr).getArgs().get(0),prototype);
					rightArg = (StrabonPolyhedron) evaluateConstruct(((FunctionCall) expr).getArgs().get(1),prototype);
					return StrabonPolyhedron.symDifference(leftArg, rightArg);
				}
				//FOR HAVING!!
				else if(function instanceof AreaFunc)
				{
					leftArg = (StrabonPolyhedron) evaluateConstruct(((FunctionCall) expr).getArgs().get(0),prototype);
					return vf.createLiteral(""+StrabonPolyhedron.area(leftArg), XMLSchema.DOUBLE);
				}
				else if(function instanceof DistanceFunc)
				{
					leftArg = (StrabonPolyhedron) evaluateConstruct(((FunctionCall) expr).getArgs().get(0),prototype);
					rightArg = (StrabonPolyhedron) evaluateConstruct(((FunctionCall) expr).getArgs().get(1),prototype);
					return vf.createLiteral(""+StrabonPolyhedron.distance(leftArg, rightArg), XMLSchema.DOUBLE);
				}
				else
				{
					throw new Exception("Function "+function.getURI().toString()+" not currently supported");
				}

			}
			else if(expr instanceof Var)
			{
				//				Value tmp =  prototype.getValue(((Var) expr).getName().replace("?spatial",""));
				//				return tmp.;
				Var tmp = (Var) expr;
				if(tmp.getName().contains("?spatial"))
				{
					tmp.setName(tmp.getName().replace("?spatial","?forGroupBy"));
				}
				else
				{
					tmp.setName(tmp.getName()+"?forGroupBy");
				}
				return strategy.evaluate(tmp,prototype);
			}
			else
			{
				throw new Exception("Functionality required for this aggregate not currently included");
			}

		}

		private void processSpatialAggregate(FunctionCall fc, BindingSet bindingSet)
		{
			computeAggregateFunctions(fc, bindingSet);
		}

		//Currently: Either Union OR Extent OR Intersection
		private void computeAggregateFunctions(ValueExpr expr, BindingSet bindingSet)
		{
			if(expr instanceof FunctionCall)
			{
				Function function = FunctionRegistry.getInstance().get(((FunctionCall) expr).getURI());
				boolean condition = ((!(function instanceof UnionFunc) || !(((FunctionCall) expr).getArgs().size()==1))
						&& (!(function instanceof IntersectionFunc) || !(((FunctionCall) expr).getArgs().size()==1))
						&&!(function instanceof ExtentFunc));
				if(condition)
				{
					//Recursively check arguments
					for(int i = 0 ; i< ((FunctionCall) expr).getArgs().size(); i++)
					{
						computeAggregateFunctions(((FunctionCall) expr).getArgs().get(i), bindingSet);
					}
				}
				else
				{
					//Need to compute spatial aggregate
					//Will add result to spatialAggregatesResult and utilize it when the spatialAggregates are iterated

					ValueExpr onlyArg = ((FunctionCall) expr).getArgs().get(0);
					Value val = null;
					StrabonPolyhedron poly = null;
					if(onlyArg instanceof Var)
					{
						try {
							String previousName = ((Var) onlyArg).getName();
							Var copy = (Var) onlyArg.clone();
							if(previousName.contains("?spatial"))
							{
								copy.setName(previousName.replace("?spatial","?forGroupBy"));
							}
							else
							{
								copy.setName(previousName+"?forGroupBy");
							}
							val = strategy.evaluate(copy,bindingSet);
						} catch (ValueExprEvaluationException e) {
							e.printStackTrace();
						} catch (QueryEvaluationException e) {
							e.printStackTrace();
						}
					}
					else //FunctionCall again
					{
						try {
							val = strategy.evaluate(onlyArg, bindingSet);

						} catch (ValueExprEvaluationException e) {
							e.printStackTrace();
						} catch (QueryEvaluationException e) {
							e.printStackTrace();
						}
					}
					poly = (StrabonPolyhedron) val;
					Geometry aggr = this.spatialAggregatesResult.get(expr);
					
					if(aggr==null)
					{

						if(function instanceof UnionFunc)
						{
							this.spatialAggregatesResult.put((FunctionCall) expr, poly.getGeometry());
							this.spatialAggregatesResultDatatype.put((FunctionCall) expr, poly.getGeometryDatatype());
						}
						else if(function instanceof IntersectionFunc)
						{
							this.spatialAggregatesResult.put((FunctionCall) expr, poly.getGeometry());
							this.spatialAggregatesResultDatatype.put((FunctionCall) expr, poly.getGeometryDatatype());
						}
						else if(function instanceof ExtentFunc)
						{
							Geometry env = poly.getGeometry().getEnvelope();
							env.setSRID(poly.getGeometry().getSRID());
							this.spatialAggregatesResult.put((FunctionCall) expr, env);
							this.spatialAggregatesResultDatatype.put((FunctionCall) expr, poly.getGeometryDatatype());
						}
					}
					else
					{
						// get the geometry datatype of the already computed aggregate
						GeometryDatatype aggrType = spatialAggregatesResultDatatype.get(expr);
						
						this.spatialAggregatesResult.remove(expr);
						this.spatialAggregatesResultDatatype.remove(expr);
						if(function instanceof UnionFunc)
						{
							//XXX possible issue with expressions like 
							// ?x hasGeom sth^^4326
							// ?x hasGeom sthElse^^2100
							Geometry united = aggr.union(poly.getGeometry());
							united.setSRID(poly.getGeometry().getSRID());
							this.spatialAggregatesResult.put((FunctionCall) expr, united);
							this.spatialAggregatesResultDatatype.put((FunctionCall) expr, aggrType);
						}
						else if(function instanceof IntersectionFunc)
						{
							//XXX possible issue with expressions like
							// ?x hasGeom sth^^4326
							// ?x hasGeom sthElse^^2100
							Geometry intersection = aggr.intersection(poly.getGeometry());
							intersection.setSRID(poly.getGeometry().getSRID());
							this.spatialAggregatesResult.put((FunctionCall) expr, intersection);
							this.spatialAggregatesResultDatatype.put((FunctionCall) expr, aggrType);
						}
						else if(function instanceof ExtentFunc)
						{
							//XXX possible issue with expressions like 
							// ?x hasGeom sth^^4326
							// ?x hasGeom sthElse^^2100
							Geometry env = aggr.union(poly.getGeometry().getEnvelope()).getEnvelope();
							env.setSRID(poly.getGeometry().getSRID());
							this.spatialAggregatesResult.put((FunctionCall) expr, env);
							this.spatialAggregatesResultDatatype.put((FunctionCall) expr, aggrType);
						}
					}
				}
			}
			else //Var
			{
				return;
			}
		}



		/**
		 * XXX 24/11/11
		 * Keeping a backup before altering code in order to support nested strdf:union expressions 
		 */
		//		private void processSpatialAggregate(FunctionCall fc, BindingSet bindingSet)
		//		{
		//			ValueExpr expr = fc.getArgs().get(0);
		//			Value val = null;
		//			StrabonPolyhedron poly = null;
		//				if(expr instanceof Var)
		//				{
		//					try {
		//						String previousName = ((Var) expr).getName();
		//						Var copy = (Var) expr.clone();
		////						copy.setName(previousName.replace("?spatial","?forGroupBy"));
		//						if(previousName.contains("?spatial"))
		//						{
		//							copy.setName(previousName.replace("?spatial","?forGroupBy"));
		//						}
		//						else
		//						{
		//							copy.setName(previousName+"?forGroupBy");
		//						}
		//						//((Var) expr).setName(previousName.replace("?spatial","?forGroupBy"));
		//						
		//						val = strategy.evaluate(copy,bindingSet);
		//						//poly = (StrabonPolyhedron) val;
		//					} catch (ValueExprEvaluationException e) {
		//						e.printStackTrace();
		//					} catch (QueryEvaluationException e) {
		//						e.printStackTrace();
		//					}
		//					
		//				}
		//				else //FunctionCall again
		//				{
		//					try {
		//						val = strategy.evaluate(expr, bindingSet);
		//						
		//					} catch (ValueExprEvaluationException e) {
		//						e.printStackTrace();
		//					} catch (QueryEvaluationException e) {
		//						e.printStackTrace();
		//					}
		//				}
		//				poly = (StrabonPolyhedron) val;
		//				Geometry aggr = this.spatialAggregatesResult.get(fc);
		//				if(aggr==null)
		//				{
		//					this.spatialAggregatesResult.put(fc, poly.getGeometry());
		//				}
		//				else
		//				{
		//					this.spatialAggregatesResult.remove(fc);
		//					this.spatialAggregatesResult.put(fc, aggr.union(poly.getGeometry()));
		//					//aggr.union(poly.getGeometry());
		//				}
		//				//System.out.println("placeholder");
		//			
		//		}

		private Aggregate create(AggregateOperator operator)
			throws ValueExprEvaluationException, QueryEvaluationException
		{
			if (operator instanceof Count) {
				return new CountAggregate((Count)operator);
			}
			else if (operator instanceof Min) {
				return new MinAggregate((Min)operator);
			}
			else if (operator instanceof Max) {
				return new MaxAggregate((Max)operator);
			}
			else if (operator instanceof Sum) {
				return new SumAggregate((Sum)operator);
			}
			else if (operator instanceof Avg) {
				return new AvgAggregate((Avg)operator);
			}
			else if (operator instanceof Sample) {
				return new SampleAggregate((Sample)operator);
			}
			else if (operator instanceof GroupConcat) {
				return new ConcatAggregate((GroupConcat)operator);
			}
			return null;
		}
	}

	private abstract class Aggregate {

		private final Set<Value> distinct;

		private final ValueExpr arg;

		public Aggregate(AggregateOperatorBase operator) {
			this.arg = operator.getArg();
			if (operator.isDistinct()) {
				distinct = new HashSet<Value>();
			}
			else {
				distinct = null;
			}
		}

		public abstract Value getValue()
				throws ValueExprEvaluationException;

		public abstract void processAggregate(BindingSet bindingSet)
				throws QueryEvaluationException;

		protected boolean distinct(Value value) {
			return distinct == null || distinct.add(value);
		}

		protected ValueExpr getArg() {
			return arg;
		}

		protected Value evaluate(BindingSet s)
			throws QueryEvaluationException
		{
			try {
				return strategy.evaluate(getArg(), s);
			}
			catch (ValueExprEvaluationException e) {
				return null; // treat missing or invalid expressions as null
			}
		}
	}

	private class CountAggregate extends Aggregate {

		private long count = 0;

		public CountAggregate(Count operator) {
			super(operator);
		}

		@Override
		public void processAggregate(BindingSet s)
			throws QueryEvaluationException
		{
			if (getArg() != null) {
				Value value = evaluate(s);
				if (value != null && distinct(value)) {
					count++;
				}
			}
			else {
				count++;
			}
		}

		@Override
		public Value getValue() {
			return vf.createLiteral(Long.toString(count), XMLSchema.INTEGER);
		}
	}

	private class MinAggregate extends Aggregate {

		private final ValueComparator comparator = new ValueComparator();

		private Value min = null;

		public MinAggregate(Min operator) {
			super(operator);
		}

		@Override
		public void processAggregate(BindingSet s)
			throws QueryEvaluationException
		{
			Value v = evaluate(s);
			if (distinct(v)) {
				if (min == null) {
					min = v;
				}
				else if (comparator.compare(v, min) < 0) {
					min = v;
				}
			}
		}

		@Override
		public Value getValue() {
			return min;
		}
	}

	private class MaxAggregate extends Aggregate {

		private final ValueComparator comparator = new ValueComparator();

		private Value max = null;

		public MaxAggregate(Max operator) {
			super(operator);
		}

		@Override
		public void processAggregate(BindingSet s)
			throws QueryEvaluationException
		{
			Value v = evaluate(s);
			if (distinct(v)) {
				if (max == null) {
					max = v;
				}
				else if (comparator.compare(v, max) > 0) {
					max = v;
				}
			}
		}

		@Override
		public Value getValue() {
			return max;
		}
	}

	private class SumAggregate extends Aggregate {

		private Literal sum = vf.createLiteral("0", XMLSchema.INTEGER);

		private ValueExprEvaluationException typeError = null;

		public SumAggregate(Sum operator) {
			super(operator);
		}

		@Override
		public void processAggregate(BindingSet s)
			throws QueryEvaluationException
		{
			if (typeError != null) {
				// halt further processing if a type error has been raised
				return;
			}

			Value v = evaluate(s);
			if (distinct(v)) {
				if (v instanceof Literal) {
					Literal nextLiteral = (Literal)v;
					// check if the literal is numeric, if not, skip it. This is
					// strictly speaking not spec-compliant, but a whole lot more
					// useful.
					if (nextLiteral.getDatatype() != null
							&& XMLDatatypeUtil.isNumericDatatype(nextLiteral.getDatatype()))
					{
						sum = MathUtil.compute(sum, nextLiteral, MathOp.PLUS);
					}
				}
				else if (v != null) {
					typeError = new ValueExprEvaluationException("not a number: " + v);
				}
			}
		}

		@Override
		public Value getValue()
			throws ValueExprEvaluationException
		{
			if (typeError != null) {
				throw typeError;
			}

			return sum;
		}
	}

	private class AvgAggregate extends Aggregate {

		private long count = 0;

		private Literal sum = vf.createLiteral("0", XMLSchema.INTEGER);

		private ValueExprEvaluationException typeError = null;

		public AvgAggregate(Avg operator) {
			super(operator);
		}

		@Override
		public void processAggregate(BindingSet s)
			throws QueryEvaluationException
		{
			if (typeError != null) {
				// Prevent calculating the aggregate further if a type error has
				// occured.
				return;
			}

			Value v = evaluate(s);
			if (distinct(v)) {
				if (v instanceof Literal) {
					Literal nextLiteral = (Literal)v;
					// check if the literal is numeric, if not, skip it. This is
					// strictly speaking not spec-compliant, but a whole lot more
					// useful.
					if (nextLiteral.getDatatype() != null
							&& XMLDatatypeUtil.isNumericDatatype(nextLiteral.getDatatype()))
					{
						sum = MathUtil.compute(sum, nextLiteral, MathOp.PLUS);
					}
					count++;
				}
				else if (v != null) {
					// we do not actually throw the exception yet, but record it and
					// stop further processing. The exception will be thrown when
					// getValue() is invoked.
					typeError = new ValueExprEvaluationException("not a number: " + v);
				}
			}
		}

		@Override
		public Value getValue()
			throws ValueExprEvaluationException
		{
			if (typeError != null) {
				// a type error occurred while processing the aggregate, throw it
				// now.
				throw typeError;
			}

			if (count == 0) {
				return vf.createLiteral(0.0d);
			}

			Literal sizeLit = vf.createLiteral(count);
			return MathUtil.compute(sum, sizeLit, MathOp.DIVIDE);
		}
	}

	private class SampleAggregate extends Aggregate {

		private Value sample = null;

		public SampleAggregate(Sample operator) {
			super(operator);
		}

		@Override
		public void processAggregate(BindingSet s)
			throws QueryEvaluationException
		{
			if (sample == null) {
				sample = evaluate(s);
			}
		}

		@Override
		public Value getValue() {
			return sample;
		}
	}

	private class ConcatAggregate extends Aggregate {

		private StringBuilder concatenated = new StringBuilder();

		private String separator = " ";

		public ConcatAggregate(GroupConcat groupConcatOp)
				throws ValueExprEvaluationException, QueryEvaluationException
		{
			super(groupConcatOp);
			ValueExpr separatorExpr = groupConcatOp.getSeparator();
			if (separatorExpr != null) {
				Value separatorValue = strategy.evaluate(separatorExpr, parentBindings);
				separator = separatorValue.stringValue();
			}
		}

		@Override
		public void processAggregate(BindingSet s)
			throws QueryEvaluationException
		{
			Value v = evaluate(s);
			if (v != null && distinct(v)) {
				concatenated.append(v.stringValue());
				concatenated.append(separator);
			}
		}

		@Override
		public Value getValue() {
			if (concatenated.length() == 0) {
				return vf.createLiteral("");
			}

			// remove separator at the end.
			int len = concatenated.length() - separator.length();
			return vf.createLiteral(concatenated.substring(0, len));
		}
	}

}