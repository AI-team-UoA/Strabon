/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * 
 * Copyright (C) 2010, 2011, 2012, 2013 Pyravlos Team
 * 
 * http://www.strabon.di.uoa.gr/
 */
package org.openrdf.sail.generaldb.evaluation;

import info.aduna.iteration.CloseableIteration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openrdf.model.Literal;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.BooleanLiteralImpl;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.query.BindingSet;
import org.openrdf.query.Dataset;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.algebra.Avg;
import org.openrdf.query.algebra.Distinct;
import org.openrdf.query.algebra.Filter;
import org.openrdf.query.algebra.FunctionCall;
import org.openrdf.query.algebra.Group;
import org.openrdf.query.algebra.GroupElem;
import org.openrdf.query.algebra.Order;
import org.openrdf.query.algebra.QueryModelNode;
import org.openrdf.query.algebra.Reduced;
import org.openrdf.query.algebra.Slice;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.ValueExpr;
import org.openrdf.query.algebra.Var;
import org.openrdf.query.algebra.evaluation.QueryBindingSet;
import org.openrdf.query.algebra.evaluation.ValueExprEvaluationException;
import org.openrdf.query.algebra.evaluation.function.Function;
import org.openrdf.query.algebra.evaluation.function.FunctionRegistry;
import org.openrdf.query.algebra.evaluation.function.spatial.AbstractWKT;
import org.openrdf.query.algebra.evaluation.function.spatial.GeometryDatatype;
import org.openrdf.query.algebra.evaluation.function.spatial.SpatialConstructFunc;
import org.openrdf.query.algebra.evaluation.function.spatial.SpatialMetricFunc;
import org.openrdf.query.algebra.evaluation.function.spatial.SpatialPropertyFunc;
import org.openrdf.query.algebra.evaluation.function.spatial.SpatialRelationshipFunc;
import org.openrdf.query.algebra.evaluation.function.spatial.StrabonPolyhedron;
import org.openrdf.query.algebra.evaluation.function.spatial.WKTHelper;
import org.openrdf.query.algebra.evaluation.function.spatial.geosparql.nontopological.GeoSparqlConvexHullFunc;
import org.openrdf.query.algebra.evaluation.function.spatial.geosparql.property.GeoSparqlGetSRIDFunc;
import org.openrdf.query.algebra.evaluation.function.spatial.postgis.construct.Centroid;
import org.openrdf.query.algebra.evaluation.function.spatial.stsparql.construct.BoundaryFunc;
import org.openrdf.query.algebra.evaluation.function.spatial.stsparql.construct.BufferFunc;
import org.openrdf.query.algebra.evaluation.function.spatial.stsparql.metric.AreaFunc;
import org.openrdf.query.algebra.evaluation.function.spatial.stsparql.metric.DistanceFunc;
import org.openrdf.query.algebra.evaluation.function.spatial.stsparql.property.AsGMLFunc;
import org.openrdf.query.algebra.evaluation.function.spatial.stsparql.property.AsTextFunc;
import org.openrdf.query.algebra.evaluation.function.spatial.stsparql.property.DimensionFunc;
import org.openrdf.query.algebra.evaluation.function.spatial.stsparql.property.GeometryTypeFunc;
import org.openrdf.query.algebra.evaluation.function.spatial.stsparql.property.IsEmptyFunc;
import org.openrdf.query.algebra.evaluation.function.spatial.stsparql.property.IsSimpleFunc;
import org.openrdf.query.algebra.evaluation.function.spatial.stsparql.property.SridFunc;
import org.openrdf.query.algebra.evaluation.function.spatial.stsparql.relation.AboveFunc;
import org.openrdf.query.algebra.evaluation.function.spatial.stsparql.relation.BelowFunc;
import org.openrdf.query.algebra.evaluation.function.spatial.stsparql.relation.ContainsFunc;
import org.openrdf.query.algebra.evaluation.function.spatial.stsparql.relation.CrossesFunc;
import org.openrdf.query.algebra.evaluation.function.spatial.stsparql.relation.DisjointFunc;
import org.openrdf.query.algebra.evaluation.function.spatial.stsparql.relation.EqualsFunc;
import org.openrdf.query.algebra.evaluation.function.spatial.stsparql.relation.IntersectsFunc;
import org.openrdf.query.algebra.evaluation.function.spatial.stsparql.relation.LeftFunc;
import org.openrdf.query.algebra.evaluation.function.spatial.stsparql.relation.OverlapsFunc;
import org.openrdf.query.algebra.evaluation.function.spatial.stsparql.relation.RightFunc;
import org.openrdf.query.algebra.evaluation.function.spatial.stsparql.relation.TouchesFunc;
import org.openrdf.query.algebra.evaluation.function.spatial.stsparql.relation.WithinFunc;
import org.openrdf.query.algebra.evaluation.function.spatial.stsparql.relation.mbb.MbbContainsFunc;
import org.openrdf.query.algebra.evaluation.function.spatial.stsparql.relation.mbb.MbbEqualsFunc;
import org.openrdf.query.algebra.evaluation.function.spatial.stsparql.relation.mbb.MbbIntersectsFunc;
import org.openrdf.query.algebra.evaluation.function.spatial.stsparql.relation.mbb.MbbWithinFunc;
import org.openrdf.query.algebra.evaluation.impl.EvaluationStrategyImpl;
import org.openrdf.query.algebra.evaluation.iterator.FilterIterator;
import org.openrdf.query.algebra.evaluation.iterator.OrderIterator;
import org.openrdf.query.algebra.evaluation.iterator.StSPARQLFilterIterator;
import org.openrdf.query.algebra.evaluation.iterator.StSPARQLGroupIterator;
import org.openrdf.query.algebra.evaluation.util.JTSWrapper;
import org.openrdf.query.algebra.evaluation.util.StSPARQLOrderComparator;
import org.openrdf.sail.generaldb.GeneralDBSpatialFuncInfo;
import org.openrdf.sail.generaldb.GeneralDBTripleRepository;
import org.openrdf.sail.generaldb.GeneralDBValueFactory;
import org.openrdf.sail.generaldb.algebra.GeneralDBColumnVar;
import org.openrdf.sail.generaldb.algebra.GeneralDBLabelColumn;
import org.openrdf.sail.generaldb.algebra.GeneralDBLongLabelColumn;
import org.openrdf.sail.generaldb.algebra.GeneralDBNumericColumn;
import org.openrdf.sail.generaldb.algebra.GeneralDBSelectProjection;
import org.openrdf.sail.generaldb.algebra.GeneralDBSelectQuery;
import org.openrdf.sail.generaldb.algebra.GeneralDBSelectQuery.OrderElem;
import org.openrdf.sail.generaldb.algebra.GeneralDBSqlAbstractGeoSrid;
import org.openrdf.sail.generaldb.algebra.GeneralDBSqlCase;
import org.openrdf.sail.generaldb.algebra.GeneralDBSqlDateTimeMetricBinary;
import org.openrdf.sail.generaldb.algebra.GeneralDBSqlGeoAsGML;
import org.openrdf.sail.generaldb.algebra.GeneralDBSqlGeoAsText;
import org.openrdf.sail.generaldb.algebra.GeneralDBSqlGeoDimension;
import org.openrdf.sail.generaldb.algebra.GeneralDBSqlGeoGeometryType;
import org.openrdf.sail.generaldb.algebra.GeneralDBSqlGeoIsEmpty;
import org.openrdf.sail.generaldb.algebra.GeneralDBSqlGeoIsSimple;
import org.openrdf.sail.generaldb.algebra.GeneralDBSqlGeoSPARQLSrid;
import org.openrdf.sail.generaldb.algebra.GeneralDBSqlGeoSpatial;
import org.openrdf.sail.generaldb.algebra.GeneralDBSqlGeoSrid;
import org.openrdf.sail.generaldb.algebra.GeneralDBSqlIsNull;
import org.openrdf.sail.generaldb.algebra.GeneralDBSqlMathExpr;
import org.openrdf.sail.generaldb.algebra.GeneralDBSqlNot;
import org.openrdf.sail.generaldb.algebra.GeneralDBSqlSpatialConstructBinary;
import org.openrdf.sail.generaldb.algebra.GeneralDBSqlSpatialConstructTriple;
import org.openrdf.sail.generaldb.algebra.GeneralDBSqlSpatialConstructUnary;
import org.openrdf.sail.generaldb.algebra.GeneralDBSqlSpatialMetricBinary;
import org.openrdf.sail.generaldb.algebra.GeneralDBSqlSpatialMetricTriple;
import org.openrdf.sail.generaldb.algebra.GeneralDBSqlSpatialMetricUnary;
import org.openrdf.sail.generaldb.algebra.GeneralDBSqlSpatialProperty;
import org.openrdf.sail.generaldb.algebra.GeneralDBURIColumn;
import org.openrdf.sail.generaldb.algebra.base.GeneralDBSqlExpr;
import org.openrdf.sail.generaldb.exceptions.UnsupportedExtensionFunctionException;
import org.openrdf.sail.generaldb.model.GeneralDBPolyhedron;
import org.openrdf.sail.generaldb.schema.IdSequence;
import org.openrdf.sail.generaldb.evaluation.util.StSPARQLValueComparator;
import org.openrdf.sail.rdbms.exceptions.RdbmsException;
import org.openrdf.sail.rdbms.exceptions.RdbmsQueryEvaluationException;
import org.openrdf.sail.rdbms.exceptions.UnsupportedRdbmsOperatorException;
import org.openrdf.sail.rdbms.model.RdbmsLiteral;
import org.openrdf.sail.rdbms.model.RdbmsURI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;

import eu.earthobservatory.constants.GeoConstants;
import eu.earthobservatory.constants.OGCConstants;

/**
 * Extends the default strategy by accepting {@link GeneralDBSelectQuery} and evaluating
 * them on a database.
 * 
 * @author Dimitrianos Savva <dimis@di.uoa.gr>
 * @author Charalampos Nikolaou <charnik@di.uoa.gr>
 * @author Manos Karpathiotakis <mk@di.uoa.gr>
 */
public abstract class GeneralDBEvaluation extends EvaluationStrategyImpl {

	private static final Logger logger = LoggerFactory.getLogger(org.openrdf.sail.generaldb.evaluation.GeneralDBEvaluation.class);

	protected GeneralDBQueryBuilderFactory factory;

	protected GeneralDBValueFactory vf;

	protected GeneralDBTripleRepository triples;

	protected IdSequence ids;

	protected HashMap<String, Integer> geoNames = new HashMap<String, Integer>();

	protected List<GeneralDBSqlExpr> thematicExpressions = new ArrayList<GeneralDBSqlExpr>(5);
	
	/**
	 * Enumeration of the possible types of the results of spatial functions.
	 * A <tt>NULL</tt> result type is to be interpreted as error.   
	 */ 
	public enum ResultType { INTEGER, STRING, BOOLEAN, WKT, WKTLITERAL, DOUBLE, URI, NULL};

	//used to retrieve the appropriate column in the Binding Iteration
	protected HashMap<GeneralDBSpatialFuncInfo, Integer> constructIndexesAndNames = new HashMap<GeneralDBSpatialFuncInfo, Integer>();
	//private HashMap<String, Integer> constructIndexesAndNames = new HashMap<String, Integer>();
	//	private HashMap<String, Integer> metricIndexesAndNames = new HashMap<String, Integer>();
	//	private HashMap<String, Integer> intPropertiesIndexesAndNames = new HashMap<String, Integer>();
	//	private HashMap<String, Integer> boolPropertiesIndexesAndNames = new HashMap<String, Integer>();
	//	private HashMap<String, Integer> stringPropertiesIndexesAndNames = new HashMap<String, Integer>();

	public GeneralDBEvaluation(GeneralDBQueryBuilderFactory factory, GeneralDBTripleRepository triples, Dataset dataset, IdSequence ids)
	{
		super(new GeneralDBTripleSource(triples), dataset);
		this.factory = factory;
		this.triples = triples;
		this.vf = triples.getValueFactory();
		this.ids = ids;
	}

	@Override
	public CloseableIteration<BindingSet, QueryEvaluationException> evaluate(TupleExpr expr, BindingSet bindings) throws QueryEvaluationException
	{
		if (expr instanceof GeneralDBSelectQuery)
			return evaluate((GeneralDBSelectQuery)expr, bindings);
		else if (expr instanceof Group) {
			return evaluate((Group)expr, bindings);
		}
		else if (expr instanceof Order) {
			return evaluate((Order)expr, bindings);
		}
		return super.evaluate(expr, bindings);
	}
	
	@Override
	public Value evaluate(ValueExpr expr, BindingSet bindings) throws ValueExprEvaluationException, QueryEvaluationException
	{
		if (expr instanceof Var) {
			return evaluate((Var)expr, bindings);
		}
		else if (expr instanceof FunctionCall) {
			return evaluate((FunctionCall)expr, bindings);
		}
		return super.evaluate(expr, bindings);
	}

	/**
	 * Had to use it for the cases met in group by (Union as an aggregate)
	 */
	@Override
	public Value evaluate(Var var, BindingSet bindings) throws ValueExprEvaluationException, QueryEvaluationException
	{
		boolean groupBy = false;
		//		//Case met when ORDER BY involved (?)
		//		if(var.getName().startsWith("-mbbVar-"))
		//		{
		//			var.setName(var.getName().replace("-mbbVar-",""));
		//		}
		
		
		//Case met when evaluating a construct function inside an aggregate 
		if(var.getName().endsWith("?spatial"))
		{
			var.setName(var.getName().replace("?spatial",""));
		}

		if(var.getName().endsWith("?forGroupBy"))
		{
			var.setName(var.getName().replace("?forGroupBy",""));
			groupBy = true;
		}
		Value value = var.getValue();

		if (value == null) {
			value = bindings.getValue(var.getName());
		}

		if (value == null) {
			throw new ValueExprEvaluationException();
		}

		if(!groupBy)
		{
			return value;
		}
		else
		{
			GeneralDBPolyhedron poly = (GeneralDBPolyhedron) value;
			return poly.getPolyhedron();
		}
	}

	/**
	 * Had to use it for the cases met in group by (Union as an aggregate)
	 * 
	 * There is a lot of work here to be done for refactoring the code.
	 * For example, while it returns a Value, that value might be a 
	 * StrabonPolyhedron and thus we have lost the datatype of the input
	 * geometries (constant or GeneralDBPolyhedron -- database values).
	 * Therefore, later on, when the writer iterates over the results, it
	 * has to select the default WKT datatype for StrabonPolyhedron values.  
	 */
	@Override
	public Value evaluate(FunctionCall fc, BindingSet bindings) throws ValueExprEvaluationException, QueryEvaluationException
	{
		if(fc.getParentNode() instanceof Avg)
		{
			if(fc.getParentNode().getParentNode() instanceof GroupElem)
			{
				GroupElem original = (GroupElem) fc.getParentNode().getParentNode();
				Value val = bindings.getValue(original.getName());
				if(val!=null)
				{
					return val;
				}
			}
		}

		// get the function corresponding to the function call
		Function function = FunctionRegistry.getInstance().get(fc.getURI());
		
		if (function == null) {
			throw new UnsupportedExtensionFunctionException("Extension function " + fc.getURI()+ " is not supported.");
		}
		
		try {
			if (fc.getArgs().size() == 0) {
				throw new NoSuchMethodException("too few arguments");
			}
		
			// get the first argument of the function call
			ValueExpr left = fc.getArgs().get(0);
		
			// evaluated first argument of function
			Value leftResult = null;
			
			// evaluated second argument of function (if any)
			Value rightResult = null;
		
			// evaluated third argument of function (if any)
			Value thirdResult = null;
					
			// evaluate first argument
			leftResult = evaluate(left, bindings);
			
			// function call with 2 or more arguments, evaluate the second one now
			// see distance function as example
			if ( fc.getArgs().size() >= 2 )
			{
				ValueExpr right = fc.getArgs().get(1);
				rightResult = evaluate(right, bindings);
				
				if (fc.getArgs().size() == 3) {
					ValueExpr third = fc.getArgs().get(2);
					thirdResult = evaluate(third, bindings);
				}
			}
		
			// having evaluated the arguments, evaluate the function now
			
			//
			// SPATIAL METRIC FUNCTIONS
			//
			if (function instanceof SpatialMetricFunc)
			{
				double funcResult = 0;
				Geometry leftGeom = getValueAsStrabonPolyhedron(leftResult).getGeometry();
				
				if(function instanceof AreaFunc)
				{
					// check required number of arguments
					checkArgs(leftResult, rightResult, thirdResult, 1);
					
					funcResult = leftGeom.getArea();
				} 
				else if(function instanceof DistanceFunc)
				{
					// check required number of arguments
					checkArgs(leftResult, rightResult, thirdResult, 3);
					
					// get the second geometry
					Geometry rightGeom = getValueAsStrabonPolyhedron(rightResult).getGeometry();
					
					// FIXME we have a third one as well, but
					// TODO for the time being we do not support it if it is in meters
					if (OGCConstants.OGCmetre.equals(thirdResult.stringValue())) {
						logger.info("[GeneraDBEvaluation] Computation of {} will be done in degrees.", function.getURI());
					}
					
					int targetSRID = leftGeom.getSRID();
					int sourceSRID = rightGeom.getSRID();
					Geometry rightConverted = JTSWrapper.getInstance().transform(rightGeom, sourceSRID, targetSRID);
					funcResult = leftGeom.distance(rightConverted);
				}
				
				return ValueFactoryImpl.getInstance().createLiteral(funcResult);
				
			}
			
			//
			// SPATIAL CONSTRUCT FUNCTIONS
			//
			else if (function instanceof SpatialConstructFunc) {
				return spatialConstructPicker(function, leftResult, rightResult, thirdResult);
			}
			
			//
			// SPATIAL RELATIONSHIP FUNCTIONS
			//
			else if(function instanceof SpatialRelationshipFunc)	{
				// Any boolean function present in HAVING - Must evaluate here!
				
				// check required number of arguments
				checkArgs(leftResult, rightResult, thirdResult, 2);
				
				boolean funcResult = false;
				
				// get the geometries and the SRIDs of the left/right arguments
				Geometry leftGeom = getValueAsStrabonPolyhedron(leftResult).getGeometry();
				Geometry rightGeom = getValueAsStrabonPolyhedron(rightResult).getGeometry();
				int targetSRID = leftGeom.getSRID();
				int sourceSRID = rightGeom.getSRID();
				
				// transform the second geometry to the SRID of the first one
				Geometry rightConverted = JTSWrapper.getInstance().transform(rightGeom, sourceSRID, targetSRID);
				
				// check the spatial relationship
				if(function instanceof AboveFunc)
				{
					funcResult = leftGeom.getEnvelopeInternal().getMinY() > rightConverted.getEnvelopeInternal().getMaxY();
				}
				else if(function instanceof IntersectsFunc)
				{
					funcResult = leftGeom.intersects(rightConverted);
				}
				else if(function instanceof BelowFunc)
				{
					funcResult = leftGeom.getEnvelopeInternal().getMaxY() < rightConverted.getEnvelopeInternal().getMinY();
				}
				else if(function instanceof ContainsFunc)
				{
					funcResult = leftGeom.contains(rightConverted);
				}
				else if(function instanceof CrossesFunc)
				{
					funcResult = leftGeom.crosses(rightConverted);
				}
				else if(function instanceof DisjointFunc)
				{
					funcResult = leftGeom.disjoint(rightConverted);
				}
				else if(function instanceof EqualsFunc)
				{
					funcResult = leftGeom.equals(rightConverted);
				}
				else if(function instanceof WithinFunc)
				{
					funcResult = leftGeom.within(rightConverted);
				}
				else if(function instanceof LeftFunc)
				{
					funcResult = leftGeom.getEnvelopeInternal().getMaxX() < rightConverted.getEnvelopeInternal().getMinX();
				}
				else if(function instanceof OverlapsFunc)
				{
					funcResult = leftGeom.overlaps(rightConverted);
				}
				else if(function instanceof RightFunc)
				{
					funcResult = leftGeom.getEnvelopeInternal().getMinX() > rightConverted.getEnvelopeInternal().getMaxX();
				}
				else if(function instanceof TouchesFunc)
				{
					funcResult = leftGeom.touches(rightConverted);
				}
				else if(function instanceof MbbIntersectsFunc)
				{
					funcResult = leftGeom.getEnvelope().intersects(rightConverted.getEnvelope());
				}
				else if(function instanceof MbbWithinFunc)
				{
					funcResult = leftGeom.getEnvelope().within(rightConverted.getEnvelope());
				}
				else if(function instanceof MbbContainsFunc)
				{
					funcResult = leftGeom.getEnvelope().contains(rightConverted.getEnvelope());
				}
				else if(function instanceof MbbEqualsFunc)
				{
					funcResult = leftGeom.getEnvelope().equals(rightConverted.getEnvelope());
				}

				return funcResult ? BooleanLiteralImpl.TRUE : BooleanLiteralImpl.FALSE;
			}
			
			//
			// SPATIAL PROPERTY FUNCTIONS
			//
			else if (function instanceof SpatialPropertyFunc) {
				ValueFactory localvf = ValueFactoryImpl.getInstance();
				
				if (function instanceof GeoSparqlGetSRIDFunc) {
					return localvf.createURI(getSRIDFromValue(leftResult));
					
				} else if (function instanceof SridFunc) {
					return localvf.createLiteral(getSRIDFromValue(leftResult));
					
				} else if (function instanceof IsSimpleFunc) {
					return localvf.createLiteral(getGeometryFromValue(leftResult).isSimple());
					
				} else if (function instanceof IsEmptyFunc) {
					return localvf.createLiteral(getGeometryFromValue(leftResult).isEmpty());
					
				} else if (function instanceof GeometryTypeFunc) {
					return localvf.createLiteral(getGeometryFromValue(leftResult).getGeometryType());
					
				} else if (function instanceof DimensionFunc) {
					return localvf.createLiteral(getGeometryFromValue(leftResult).getDimension());
					
				} else if (function instanceof AsTextFunc) { 
					// already handled
					return leftResult;
					
				} else if (function instanceof AsGMLFunc) {
					return localvf.createLiteral(JTSWrapper.getInstance().GMLWrite(getGeometryFromValue(leftResult)));
					
				} else {
					logger.error("[Strabon.evaluate(FunctionCall)] Function {} has not been implemented yet. ", function.getURI());
					return null;
				}
				
			} else {
				//Default Sesame Behavior
				List<ValueExpr> args = fc.getArgs();

				Value[] argValues = new Value[args.size()];

				for (int i = 0; i < args.size(); i++) {
					argValues[i] = evaluate(args.get(i), bindings);
				}
				
				return function.evaluate(tripleSource.getValueFactory(), argValues);
			}
			
		} catch (Exception e) {
			logger.error("[Strabon.evaluate(FunctionCall)] Error during evaluation of extension function {}: {}", function.getURI(), e.getMessage());
			return null;
		}
	}

	/*Evaluation of Filter*/
	public CloseableIteration<BindingSet, QueryEvaluationException> evaluate(Filter filter, BindingSet bindings)
			throws QueryEvaluationException
	{
			CloseableIteration<BindingSet, QueryEvaluationException> result;
			result = this.evaluate(filter.getArg(), bindings);

			Set <String> spatialConstructs = new HashSet<String>();
			for(GeneralDBSpatialFuncInfo construct : constructIndexesAndNames.keySet()) {
				spatialConstructs.add(construct.getFieldName());
			}
			//add the spatial constructs to the scope of the FILTER (case of a BIND clause
			//that contains a spatial construct)	
			result = new StSPARQLFilterIterator(filter, result, spatialConstructs, this);
			return result;
	}

	/**
	 * @param leftResult
	 * @param rightResult
	 * @param thirdResult
	 * @param size
	 * @throws NoSuchMethodException 
	 */
	private void checkArgs(Value leftResult, Value rightResult, Value thirdResult, int size) throws NoSuchMethodException {
		if (size == 0) {
			throw new NoSuchMethodException("too few arguments.");
			
		} else {
			if (size >= 1 && leftResult == null) {
				throw new NoSuchMethodException("expecting an argument.");
			}
			
			if (size >= 2 && rightResult == null) {
				throw new NoSuchMethodException("expecting a second argument.");
			}
			
			if (size >= 3 && thirdResult == null) {
				throw new NoSuchMethodException("expecting a third argument.");
				
			}

			if (size > 3) {
				throw new NoSuchMethodException("too many arguments.");
			}

			if (size == 1 && (rightResult !=null || thirdResult != null) ) {
				throw new NoSuchMethodException("too many arguments.");
			}
			if (size == 2 && thirdResult != null) {
				throw new NoSuchMethodException("too many arguments.");
			}
		}
	}

	/**
	 * FIXME don't check function using getURI(); use instanceof instead 
	 */
	public StrabonPolyhedron spatialConstructPicker(Function function, Value left, Value right, Value third) throws Exception
	{
		StrabonPolyhedron leftArg = getValueAsStrabonPolyhedron(left);
		if(function.getURI().equals(GeoConstants.stSPARQLunion))
		{
			// check required number of arguments
			checkArgs(left, right, third, 2);
			
			StrabonPolyhedron rightArg = getValueAsStrabonPolyhedron(right);
			return StrabonPolyhedron.union(leftArg, rightArg);
		}
		else if (function instanceof BufferFunc) {
			// check required number of arguments
			checkArgs(left, right, third, 3);
						
			// TODO implement computation of the buffer in degrees
			// you'll get the type (degrees/meter) from the thirdResult, which
			// would be a URI.
			if (OGCConstants.OGCdegree.equals(third.stringValue())) {
				logger.info("[GeneraDBEvaluation] Computation of {} will be done in meters.", function.getURI());
			}
			
			if(right instanceof LiteralImpl)
			{
				LiteralImpl meters = (LiteralImpl) right;
				return StrabonPolyhedron.buffer(leftArg, meters.doubleValue());
			}
			else if (right instanceof RdbmsLiteral)
			{
				RdbmsLiteral meters = (RdbmsLiteral) right;
				return StrabonPolyhedron.buffer(leftArg, meters.doubleValue());
			}
		}
		else if(function.getURI().equals(GeoConstants.stSPARQLtransform))
		{
			// check required number of arguments
			checkArgs(left, right, third, 2);
						
			if(right instanceof URIImpl)
			{
				URIImpl srid = (URIImpl) right;
				return StrabonPolyhedron.transform(leftArg,srid);
			}
			else if(right instanceof RdbmsURI)
			{
				RdbmsURI srid = (RdbmsURI) right;
				int parsedSRID = WKTHelper.getSRID_forURI(srid.toString());
				Geometry converted = JTSWrapper.getInstance().transform(leftArg.getGeometry(),leftArg.getGeometry().getSRID(), parsedSRID);
				return new StrabonPolyhedron(converted, leftArg.getGeometryDatatype());
			}

		}
		else if(function.getURI().equals(GeoConstants.stSPARQLenvelope))
		{
			return StrabonPolyhedron.envelope(leftArg);
		}
		else if(function.getURI().equals(GeoConstants.stSPARQLconvexHull) ||
				function instanceof GeoSparqlConvexHullFunc)
		{
			return StrabonPolyhedron.convexHull(leftArg);
		}
		else if(function instanceof BoundaryFunc)
		{
			return StrabonPolyhedron.boundary(leftArg);
			
		}
		else if(function.getURI().equals(GeoConstants.stSPARQLintersection))
		{
			// check required number of arguments
			checkArgs(left, right, third, 2);
			
			StrabonPolyhedron rightArg = getValueAsStrabonPolyhedron(right);
			return StrabonPolyhedron.intersection(leftArg, rightArg);
		}
		else if(function.getURI().equals(GeoConstants.stSPARQLdifference))
		{
			// check required number of arguments
			checkArgs(left, right, third, 2);
			
			StrabonPolyhedron rightArg = getValueAsStrabonPolyhedron(right);
			return StrabonPolyhedron.difference(leftArg, rightArg);		
		}
		else if(function.getURI().equals(GeoConstants.stSPARQLsymDifference))
		{
			// check required number of arguments
			checkArgs(left, right, third, 2);
			
			StrabonPolyhedron rightArg = getValueAsStrabonPolyhedron(right);
			return StrabonPolyhedron.symDifference(leftArg, rightArg);
			
		} else if (function instanceof Centroid) {
			return leftArg.getCentroid();
			
		} //else if (function instanceof MakeLine) { // TODO add
		//	return null;
		//}
		
		logger.error("[GeneralDBEvaluation.spatialConstructPicker] Extension function {} has not been implemented yet in this kind of expressions.", function.getURI());
		return null;
	}

	@Override
	public CloseableIteration<BindingSet, QueryEvaluationException> evaluate(Group node, BindingSet bindings)
			throws QueryEvaluationException
			{
		//		Set<String> tmp1 = node.getAggregateBindingNames();
		//		Set<String> tmp2 = node.getAssuredBindingNames();
		//		Set<String> tmp3 = node.getBindingNames();
		//		Set<String> tmp4 = node.getGroupBindingNames();
		//		for(String tmp : tmp4)
		//		{
		//			//System.out.println(node.g);
		//		}
		return new StSPARQLGroupIterator(this, node, bindings);
			}

	@Override
	public CloseableIteration<BindingSet, QueryEvaluationException> evaluate(Order node, BindingSet bindings)
			throws QueryEvaluationException
			{
		StSPARQLValueComparator vcmp = new StSPARQLValueComparator();
		StSPARQLOrderComparator cmp = new StSPARQLOrderComparator(this, node, vcmp);
		boolean reduced = isReduced(node);
		long limit = getLimit(node);
		return new OrderIterator(evaluate(node.getArg(), bindings), cmp, limit, reduced);
			}

	//Duplicated from EvaluationStrategyImpl
	private boolean isReduced(QueryModelNode node) {
		QueryModelNode parent = node.getParentNode();
		if (parent instanceof Slice) {
			return isReduced(parent);
		}
		return parent instanceof Distinct || parent instanceof Reduced;
	}

	//Duplicated from EvaluationStrategyImpl
	protected long getLimit(QueryModelNode node) {
		long offset = 0;
		if (node instanceof Slice) {
			Slice slice = (Slice)node;
			if (slice.hasOffset() && slice.hasLimit()) {
				return slice.getOffset() + slice.getLimit();
			}
			else if (slice.hasLimit()) {
				return slice.getLimit();
			}
			else if (slice.hasOffset()) {
				offset = slice.getOffset();
			}
		}
		QueryModelNode parent = node.getParentNode();
		if (parent instanceof Distinct || parent instanceof Reduced || parent instanceof Slice) {
			long limit = getLimit(parent);
			if (offset > 0L && limit < Long.MAX_VALUE) {
				return offset + limit;
			}
			else {
				return limit;
			}
		}
		return Long.MAX_VALUE;
	}

	// brought it here to override it somehow..
	//	@Override
	//	public CloseableIteration<BindingSet, QueryEvaluationException> evaluate(Extension extension,
	//			BindingSet bindings)
	//		throws QueryEvaluationException
	//	{
	//		CloseableIteration<BindingSet, QueryEvaluationException> result;
	//		
	//		/**
	//		 * additions
	//		 */
	//		Iterator<ExtensionElem> iter = extension.getElements().iterator();
	//		//for(ExtensionElem elem : extension.getElements())
	//		while(iter.hasNext())
	//		{
	//			ExtensionElem elem = iter.next();
	//			if(elem.getExpr() instanceof FunctionCall)
	//			{
	//				Function function = FunctionRegistry.getInstance().get(((FunctionCall) elem.getExpr()).getURI());
	//				if(function instanceof SpatialPropertyFunc || function instanceof SpatialRelationshipFunc || function instanceof SpatialConstructFunction)
	//				{
	//					System.out.println("stopper");
	//					spatialFunctionsInSelect.put(elem.getName(),elem.getExpr());
	//					iter.remove();
	//				}
	//			}
	//		}
	//		/**
	//		 * 	
	//		 */
	//		try {
	//			result = this.evaluate(extension.getArg(), bindings);
	//		}
	//		catch (ValueExprEvaluationException e) {
	//			// a type error in an extension argument should be silently ignored and
	//			// result in zero bindings.
	//			result = new EmptyIteration<BindingSet, QueryEvaluationException>();
	//		}
	//
	//		result = new ExtensionIterator(extension, result, this);
	//		return result;
	//	}

	protected abstract CloseableIteration<BindingSet, QueryEvaluationException> evaluate(GeneralDBSelectQuery qb, BindingSet b)
			throws UnsupportedRdbmsOperatorException, RdbmsQueryEvaluationException;

	protected String toQueryString(GeneralDBSelectQuery qb, QueryBindingSet bindings, List<Object> parameters)
			throws RdbmsException, UnsupportedRdbmsOperatorException
			{
		GeneralDBQueryBuilder query = factory.createQueryBuilder();
		if (qb.isDistinct()) {
			query.distinct();
		}
		query.from(qb.getFrom());
		for (GeneralDBColumnVar var : qb.getVars()) {
			for (String name : qb.getBindingNames(var)) {
				if (var.getValue() == null && bindings.hasBinding(name)) {
					query.filter(var, bindings.getValue(name));
				}
				else if (var.getValue() != null && !bindings.hasBinding(name)
						&& qb.getBindingNames().contains(name))
				{
					bindings.addBinding(name, var.getValue());
				}
			}
		}

		//List<GeneralDBSelectProjection> projForOrderBy = new ArrayList<GeneralDBSelectProjection>();

		int index = 0;
		for (GeneralDBSelectProjection proj : qb.getSqlSelectVar()) {
			GeneralDBColumnVar var = proj.getVar();
			if (!var.isHiddenOrConstant()) {
				for (String name : qb.getBindingNames(var)) {
					if (!bindings.hasBinding(name)) {
						var.setIndex(index);
						// if the variable is actually a GeoVar
						if(var.isSpatial())
						{
							this.geoNames.put(var.getName(), var.getIndex() + 2);
							//I am carrying SRID too! Therefore, shifting index one more position
							index++;
						}
						query.select(proj.getId());
						query.select(proj.getStringValue());
						index += 2;
						if (var.getTypes().isLiterals()) {
							// NOTE: changed to remove extra unneeded joins + selections
							//Original:
							//query.select(proj.getLanguage());
							//query.select(proj.getDatatype());
							//index += 2;

							//Altered:
							if(proj.getLanguage()!=null)
							{
								query.select(proj.getLanguage());
								index++;
							}
							if(proj.getDatatype()!=null)
							{
								query.select(proj.getDatatype());
								index++;
							}

						}
					}
				}
			}
		}

		// Attention: Will try to add projections in select for the constructs
		Iterator it = qb.getSpatialConstructs().entrySet().iterator();
		while (it.hasNext()) {
			@SuppressWarnings("rawtypes")
			Map.Entry pairs = (Map.Entry)it.next();
			
			// Trying to fill what's missing
			GeneralDBSqlExpr expr = (GeneralDBSqlExpr) pairs.getValue();
			locateColumnVars(expr, qb.getVars());
			
			//Assuming thematic aggregates and spatial expressions won't be combined
			if(!this.thematicExpressions.contains(expr))
			{
				query.construct(expr);
				boolean increaseIndex = false;

				GeneralDBSpatialFuncInfo info = null;
				
				ResultType type = getResultType(expr);
				if (type == ResultType.NULL) {
					throw new UnsupportedRdbmsOperatorException("No such spatial expression exists!");
					
				} else {
					info = new GeneralDBSpatialFuncInfo((String) pairs.getKey(), type, expr instanceof GeneralDBSqlAbstractGeoSrid);

					// set increaseIndex to <tt>true</tt> for geometries only (see commend below)
					if (type == ResultType.WKT || type == ResultType.WKTLITERAL) {
						increaseIndex = true;
					}
					
				}

				constructIndexesAndNames.put(info, index++);
				if(increaseIndex)
				{
					//Increasing index by one more because of SRID!
					//However, only in the case when the result is some geometry (e.g. not for metrics)
					index++;
				}
			}
		}
		//

		for (OrderElem by : qb.getOrderElems()) {
			query.orderBy(by.sqlExpr, by.isAscending);
			if (qb.isDistinct()) {
				query.select(by.sqlExpr);
			}
		}
		if (qb.getLimit() != null) {
			query.limit(qb.getLimit());
		}
		if (qb.getOffset() != null) {
			query.offset(qb.getOffset());
		}
		parameters.addAll(query.getParameters());

		return query.toString();
	}

	/**
	 * Function used to locate all ColumnVars from the select's spatial constructs so that they can later 
	 * be mapped if they have a missing Column Var
	 */
	private void locateColumnVars(GeneralDBSqlExpr expr, Collection<GeneralDBColumnVar> allKnown)
	{
		//ArrayList<GeneralDBColumnVar> allVars = new ArrayList<GeneralDBColumnVar>();
		if(expr instanceof GeneralDBSqlSpatialProperty) //1 arg
		{

			//allVars.addAll(locateColumnVars(((GeneralDBSqlSpatialProperty)expr).getArg(),allKnown));
			locateColumnVars(((GeneralDBSqlSpatialProperty)expr).getArg(),allKnown);
		}
		else if(expr instanceof GeneralDBSqlSpatialConstructBinary)
		{
			//allVars.addAll(locateColumnVars(((GeneralDBSqlSpatialConstruct)expr).getLeftArg(),allKnown));
			//allVars.addAll(locateColumnVars(((GeneralDBSqlSpatialConstruct)expr).getRightArg(),allKnown));
			locateColumnVars(((GeneralDBSqlSpatialConstructBinary)expr).getLeftArg(),allKnown);
			locateColumnVars(((GeneralDBSqlSpatialConstructBinary)expr).getRightArg(),allKnown);
		}
		else if(expr instanceof GeneralDBSqlSpatialConstructUnary)
		{
			locateColumnVars(((GeneralDBSqlSpatialConstructUnary)expr).getArg(),allKnown);
		}
		else if(expr instanceof GeneralDBSqlSpatialConstructTriple)
		{
			locateColumnVars(((GeneralDBSqlSpatialConstructTriple)expr).getLeftArg(),allKnown);
			locateColumnVars(((GeneralDBSqlSpatialConstructTriple)expr).getRightArg(),allKnown);
			locateColumnVars(((GeneralDBSqlSpatialConstructTriple)expr).getThirdArg(),allKnown);
		}
		/** Addition for datetime metric functions
		 * 
		 * @author George Garbis <ggarbis@di.uoa.gr>
		 * 
		 */
		else if(expr instanceof GeneralDBSqlDateTimeMetricBinary)
		{
			locateColumnVars(((GeneralDBSqlDateTimeMetricBinary)expr).getLeftArg(),allKnown);
			locateColumnVars(((GeneralDBSqlDateTimeMetricBinary)expr).getRightArg(),allKnown);
		}
		/***/
		else if(expr instanceof GeneralDBSqlSpatialMetricBinary)
		{
			locateColumnVars(((GeneralDBSqlSpatialMetricBinary)expr).getLeftArg(),allKnown);
			locateColumnVars(((GeneralDBSqlSpatialMetricBinary)expr).getRightArg(),allKnown);
		}

		else if(expr instanceof GeneralDBSqlSpatialMetricTriple)
		{
			locateColumnVars(((GeneralDBSqlSpatialMetricTriple)expr).getLeftArg(),allKnown);
			locateColumnVars(((GeneralDBSqlSpatialMetricTriple)expr).getRightArg(),allKnown);
			locateColumnVars(((GeneralDBSqlSpatialMetricTriple)expr).getThirdArg(),allKnown);
		}

		else if(expr instanceof GeneralDBSqlSpatialMetricUnary)
		{
			locateColumnVars(((GeneralDBSqlSpatialMetricUnary)expr).getArg(),allKnown);
		}
		else if(expr instanceof GeneralDBLongLabelColumn)//Don't think this case is visited any more
		{
			//			GeneralDBColumnVar var = ((GeneralDBLongLabelColumn) expr).getRdbmsVar();
			//			for(GeneralDBColumnVar reference: allKnown)
			//			{
			//				if(var.getName().equals(reference.getName()))
			//				{
			//					var = reference;
			//				}
			//			}
			String name = ((GeneralDBLongLabelColumn) expr).getVarName().replace("?spatial","");

			for(GeneralDBColumnVar reference: allKnown)
			{
				if(name.equals(reference.getName()))
				{
					((GeneralDBLongLabelColumn) expr).setRdbmsVar(reference);

				}
			}

		}
		else if(expr instanceof GeneralDBLabelColumn)//ColumnVar at least
		{
			String name = ((GeneralDBLabelColumn) expr).getVarName().replace("?spatial","");

			for(GeneralDBColumnVar reference: allKnown)
			{
				if(name.equals(reference.getName()))
				{
					((GeneralDBLabelColumn) expr).setRdbmsVar(reference);

				}
			}
			//System.out.println("stopper");
		}
		else if(expr instanceof GeneralDBNumericColumn)//ColumnVar at least
		{
			boolean found = false;
			String name = ((GeneralDBNumericColumn) expr).getVarName();

			//String alias =  ((GeneralDBNumericColumn) expr).getAlias();
			//alias.replaceFirst("n","l");
			for(GeneralDBColumnVar reference: allKnown)
			{
				if(name.equals(reference.getName()))
				{
					//((GeneralDBNumericColumn) expr).setRdbmsVar(reference);

					GeneralDBSqlExpr exprCopy = new GeneralDBLabelColumn(reference,false);
					expr.replaceWith(exprCopy);
					found = true;
				}
			}

			if(!found)
			{
				//Will keep non-spatial math expressions to avoid iterating through them 
				//at QueryBuilder.construct. Otherwise, exception occurs. 
				//This case probably only concerns thematic aggregates
				this.thematicExpressions.add((GeneralDBSqlExpr)expr.getParentNode());
			}
			//System.out.println("stopper");
		}
		else if(expr instanceof GeneralDBURIColumn)//Used for 2nd argument of Transform
		{
			String name = ((GeneralDBURIColumn) expr).getVarName();

			for(GeneralDBColumnVar reference: allKnown)
			{
				if(name.equals(reference.getName()))
				{
					GeneralDBSqlExpr exprCopy = new GeneralDBURIColumn(reference);
					expr.replaceWith(exprCopy);
				}
			}

		}
		else if(expr instanceof GeneralDBSqlMathExpr)//Case when I have calculations in select
		{
			locateColumnVars(((GeneralDBSqlMathExpr)expr).getLeftArg(),allKnown);
			locateColumnVars(((GeneralDBSqlMathExpr)expr).getRightArg(),allKnown);
		}
		else if(expr instanceof GeneralDBSqlGeoSpatial)
		{
			locateColumnVars(((GeneralDBSqlGeoSpatial)expr).getLeftArg(),allKnown); 
			locateColumnVars(((GeneralDBSqlGeoSpatial)expr).getRightArg(),allKnown);
		}
		else
		{
			//must recurse
			if(expr instanceof GeneralDBSqlCase)
			{
				for (GeneralDBSqlCase.Entry e : ((GeneralDBSqlCase) expr).getEntries()) {
					locateColumnVars(e.getCondition(),allKnown);
					locateColumnVars(e.getResult(),allKnown);
					//					allVars.addAll(locateColumnVars(e.getCondition(),allKnown));
					//					allVars.addAll(locateColumnVars(e.getResult(),allKnown));
				}
			}

			if(expr instanceof GeneralDBSqlIsNull)
			{
				//allVars.addAll(locateColumnVars(((GeneralDBSqlIsNull) expr).getArg(),allKnown));
				locateColumnVars(((GeneralDBSqlIsNull) expr).getArg(),allKnown);
			}

			if(expr instanceof GeneralDBSqlNot)
			{
				//allVars.addAll(locateColumnVars(((GeneralDBSqlNot) expr).getArg(),allKnown));
				locateColumnVars(((GeneralDBSqlNot) expr).getArg(),allKnown);
			}

		}
		

		//return allVars;
	}

	
	/**
	 * Very customized method for taking the URI corresponding to the SRID of a
	 * {@link Value} that may be one of {@link StrabonPolyhedron}, 
	 * {@link GeneralDBPolyhedron}, or {@link Literal}.
	 * 
	 * The insight is that we would like to return the URI for CRS84 when the
	 * SRID equals {@link GeoConstants.EPSG4326_SRID} and not the URI for 
	 * EPSG:4326. Since, we have such information available, we choose to
	 * do compute it.
	 * 
	 * @param value
	 * @return
	 * @throws ParseException
	 * 			when the supplied value is a constant ({@link Literal}) and is not
	 * 			a valid WKT literal
	 */
	public String getSRIDFromValue(Value value) throws ParseException {
		boolean iswktLiteral = false;
		int srid = -1;
		
		if (value instanceof GeneralDBPolyhedron) {
			StrabonPolyhedron poly = ((GeneralDBPolyhedron) value).getPolyhedron(); 
			srid = poly.getGeometry().getSRID();
			
			if (poly.getGeometryDatatype() == GeometryDatatype.wktLiteral) {
				iswktLiteral = true;
			}
			
		} else if (value instanceof StrabonPolyhedron) {
			StrabonPolyhedron poly = ((StrabonPolyhedron) value); 
			srid = poly.getGeometry().getSRID();
			
			if (poly.getGeometryDatatype() == GeometryDatatype.wktLiteral) {
				iswktLiteral = true;
			}
			
		} else if (value instanceof Literal) { // this is actually a constant 
			Literal literal = (Literal) value;
			AbstractWKT wkt;
			
			if (literal.getDatatype() != null) {
				wkt = new AbstractWKT(literal.stringValue(), literal.getDatatype().stringValue());
				
			} else {
				wkt = new AbstractWKT(literal.stringValue());
			}
			
			srid = wkt.getSRID();
			
			if (!wkt.isstRDFWKT()) {
				iswktLiteral = true;
			}
			
			// we are constructing a {@link StrabonPolyhedron} now to check
			// its validity
			JTSWrapper.getInstance().WKTread(wkt.getWKT());
		}
		
		if (iswktLiteral && srid == GeoConstants.EPSG4326_SRID) {
			return GeoConstants.CRS84_URI;
			
		} else {
			return WKTHelper.getEPSGURI_forSRID(srid);
		}
	}
	
	public Geometry getGeometryFromValue(Value value) throws ParseException {
		if (value instanceof GeneralDBPolyhedron) {
			return ((GeneralDBPolyhedron) value).getPolyhedron().getGeometry();
			
		} else if (value instanceof StrabonPolyhedron) {
			return ((StrabonPolyhedron) value).getGeometry();
			
		} else if (value instanceof Literal) {
			Literal literal = (Literal) value;
			AbstractWKT wkt = null;
			
			if (literal.getDatatype() == null) {
				wkt = new AbstractWKT(literal.stringValue());
				
			} else {
				wkt = new AbstractWKT(literal.stringValue(), literal.getDatatype().stringValue());
			}
			
			Geometry geom = JTSWrapper.getInstance().WKTread(wkt.getWKT());
			geom.setSRID(wkt.getSRID());
			
			return geom;
			
		} else {
			return null;
		}
	}

	StrabonPolyhedron getValueAsStrabonPolyhedron(Value value) throws ParseException {
		if (value instanceof GeneralDBPolyhedron) {
			return ((GeneralDBPolyhedron) value).getPolyhedron();
			
		} else if (value instanceof StrabonPolyhedron) {
			return (StrabonPolyhedron) value;
			
		} else if (value instanceof Literal) {
			Literal literal = (Literal) value;
			AbstractWKT wkt = null;
			
			if (literal.getDatatype() == null) {
				wkt = new AbstractWKT(literal.stringValue());
				
			} else {
				wkt = new AbstractWKT(literal.stringValue(), literal.getDatatype().stringValue());
			}
			
			return new StrabonPolyhedron(wkt.getWKT(), 
										 wkt.getSRID(), 
										 GeometryDatatype.fromString(literal.getDatatype().stringValue()));
			
		} else { // wrong case
			throw new IllegalArgumentException("The provided argument is not a valid spatial value: " + value.getClass());
		}
	}
	
	/**
	 * Given an expression get the type of the result. 
	 * 
	 * @param expr
	 * @return
	 */
	private ResultType getResultType(GeneralDBSqlExpr expr)
	{
		if(expr instanceof GeneralDBSqlSpatialProperty)
		{
			if(expr instanceof GeneralDBSqlGeoDimension ||
					expr instanceof GeneralDBSqlGeoSrid	)
			{
				return ResultType.INTEGER;
			}
			else if(expr instanceof GeneralDBSqlGeoGeometryType ||
					expr instanceof GeneralDBSqlGeoAsText ||
					expr instanceof GeneralDBSqlGeoAsGML)
			{
				return ResultType.STRING;
			}
			else if(expr instanceof GeneralDBSqlGeoIsSimple ||
					expr instanceof GeneralDBSqlGeoIsEmpty	)
			{
				return ResultType.BOOLEAN;
				
			} else if (expr instanceof GeneralDBSqlGeoSPARQLSrid) {
				return ResultType.URI;
			}

		}
		
		else if(expr instanceof GeneralDBSqlSpatialConstructUnary)
		{	
			GeneralDBSqlSpatialConstructUnary exprUnary = (GeneralDBSqlSpatialConstructUnary) expr;
			if(exprUnary.getResultType() == GeoConstants.WKT)
				return ResultType.WKT;
			else
				return ResultType.WKTLITERAL;
		}
		else if(expr instanceof GeneralDBSqlSpatialConstructBinary)
		{	
			GeneralDBSqlSpatialConstructBinary exprBinary = (GeneralDBSqlSpatialConstructBinary) expr;
			if(exprBinary.getResultType() == GeoConstants.WKT)
				return ResultType.WKT;
			else
				return ResultType.WKTLITERAL;
		}
		else if(expr instanceof GeneralDBSqlSpatialConstructTriple)
		{	
			GeneralDBSqlSpatialConstructTriple exprTriple = (GeneralDBSqlSpatialConstructTriple) expr;
			if(exprTriple.getResultType() == GeoConstants.WKT)
				return ResultType.WKT;
			else
				return ResultType.WKTLITERAL;
		}
					
		else if(expr instanceof GeneralDBSqlSpatialMetricBinary ||
				expr instanceof GeneralDBSqlSpatialMetricUnary ||
				expr instanceof GeneralDBSqlMathExpr ||
				expr instanceof GeneralDBSqlSpatialMetricTriple ||
				/** Addition for datetime metric functions
				 * 
				 * @author George Garbis <ggarbis@di.uoa.gr>
				 *
				 */
				expr instanceof GeneralDBSqlDateTimeMetricBinary
				/***/)
		{
			return ResultType.DOUBLE;
		}
		else if(expr instanceof GeneralDBSqlGeoSpatial)
		{
			return ResultType.BOOLEAN;
		}
		
		return ResultType.NULL;//SHOULD NEVER REACH THIS CASE
	}
}
