/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * 
 * Copyright (C) 2010, 2011, 2012, 2013, 2014 Pyravlos Team
 * 
 * http://www.strabon.di.uoa.gr/
 */
package org.openrdf.sail.postgis.evaluation;

import java.util.ArrayList;
import java.util.List;

import org.openrdf.query.algebra.evaluation.function.spatial.AbstractWKT;
import org.openrdf.query.algebra.evaluation.function.spatial.WKTHelper;
import org.openrdf.sail.generaldb.algebra.GeneralDBColumnVar;
import org.openrdf.sail.generaldb.algebra.GeneralDBDateTimeColumn;
import org.openrdf.sail.generaldb.algebra.GeneralDBDoubleValue;
import org.openrdf.sail.generaldb.algebra.GeneralDBLabelColumn;
import org.openrdf.sail.generaldb.algebra.GeneralDBNumberValue;
import org.openrdf.sail.generaldb.algebra.GeneralDBNumericColumn;
import org.openrdf.sail.generaldb.algebra.GeneralDBSqlAbove;
import org.openrdf.sail.generaldb.algebra.GeneralDBSqlAbstractGeoSrid;
import org.openrdf.sail.generaldb.algebra.GeneralDBSqlAnd;
import org.openrdf.sail.generaldb.algebra.GeneralDBSqlBelow;
import org.openrdf.sail.generaldb.algebra.GeneralDBSqlCase;
import org.openrdf.sail.generaldb.algebra.GeneralDBSqlContains;
import org.openrdf.sail.generaldb.algebra.GeneralDBSqlCrosses;
import org.openrdf.sail.generaldb.algebra.GeneralDBSqlDiffDateTime;
import org.openrdf.sail.generaldb.algebra.GeneralDBSqlDisjoint;
import org.openrdf.sail.generaldb.algebra.GeneralDBSqlEqualsSpatial;
import org.openrdf.sail.generaldb.algebra.GeneralDBSqlGeoArea;
import org.openrdf.sail.generaldb.algebra.GeneralDBSqlGeoAsGML;
import org.openrdf.sail.generaldb.algebra.GeneralDBSqlGeoAsText;
import org.openrdf.sail.generaldb.algebra.GeneralDBSqlGeoBoundary;
import org.openrdf.sail.generaldb.algebra.GeneralDBSqlGeoBuffer;
import org.openrdf.sail.generaldb.algebra.GeneralDBSqlGeoConvexHull;
import org.openrdf.sail.generaldb.algebra.GeneralDBSqlGeoDifference;
import org.openrdf.sail.generaldb.algebra.GeneralDBSqlGeoDimension;
import org.openrdf.sail.generaldb.algebra.GeneralDBSqlGeoDistance;
import org.openrdf.sail.generaldb.algebra.GeneralDBSqlGeoEnvelope;
import org.openrdf.sail.generaldb.algebra.GeneralDBSqlGeoGeometryType;
import org.openrdf.sail.generaldb.algebra.GeneralDBSqlGeoIntersection;
import org.openrdf.sail.generaldb.algebra.GeneralDBSqlGeoIsEmpty;
import org.openrdf.sail.generaldb.algebra.GeneralDBSqlGeoIsSimple;
import org.openrdf.sail.generaldb.algebra.GeneralDBSqlGeoSPARQLSrid;
import org.openrdf.sail.generaldb.algebra.GeneralDBSqlGeoSpatial;
import org.openrdf.sail.generaldb.algebra.GeneralDBSqlGeoSrid;
import org.openrdf.sail.generaldb.algebra.GeneralDBSqlGeoSymDifference;
import org.openrdf.sail.generaldb.algebra.GeneralDBSqlGeoTransform;
import org.openrdf.sail.generaldb.algebra.GeneralDBSqlGeoUnion;
import org.openrdf.sail.generaldb.algebra.GeneralDBSqlIntersects;
import org.openrdf.sail.generaldb.algebra.GeneralDBSqlIsNull;
import org.openrdf.sail.generaldb.algebra.GeneralDBSqlLeft;
import org.openrdf.sail.generaldb.algebra.GeneralDBSqlMathExpr;
import org.openrdf.sail.generaldb.algebra.GeneralDBSqlMbbContains;
import org.openrdf.sail.generaldb.algebra.GeneralDBSqlMbbEquals;
import org.openrdf.sail.generaldb.algebra.GeneralDBSqlMbbIntersects;
import org.openrdf.sail.generaldb.algebra.GeneralDBSqlMbbWithin;
import org.openrdf.sail.generaldb.algebra.GeneralDBSqlNot;
import org.openrdf.sail.generaldb.algebra.GeneralDBSqlNull;
import org.openrdf.sail.generaldb.algebra.GeneralDBSqlOverlaps;
import org.openrdf.sail.generaldb.algebra.GeneralDBSqlRelate;
import org.openrdf.sail.generaldb.algebra.GeneralDBSqlRight;
import org.openrdf.sail.generaldb.algebra.GeneralDBSqlST_Centroid;
import org.openrdf.sail.generaldb.algebra.GeneralDBSqlST_MakeLine;
import org.openrdf.sail.generaldb.algebra.GeneralDBSqlSpatialConstructBinary;
import org.openrdf.sail.generaldb.algebra.GeneralDBSqlSpatialConstructTriple;
import org.openrdf.sail.generaldb.algebra.GeneralDBSqlSpatialConstructUnary;
import org.openrdf.sail.generaldb.algebra.GeneralDBSqlSpatialMetricBinary;
import org.openrdf.sail.generaldb.algebra.GeneralDBSqlSpatialMetricTriple;
import org.openrdf.sail.generaldb.algebra.GeneralDBSqlSpatialMetricUnary;
import org.openrdf.sail.generaldb.algebra.GeneralDBSqlSpatialProperty;
import org.openrdf.sail.generaldb.algebra.GeneralDBSqlTouches;
import org.openrdf.sail.generaldb.algebra.GeneralDBSqlWithin;
import org.openrdf.sail.generaldb.algebra.GeneralDBStringValue;
import org.openrdf.sail.generaldb.algebra.GeneralDBURIColumn;
import org.openrdf.sail.generaldb.algebra.GeneralDBUnionItem;
import org.openrdf.sail.generaldb.algebra.base.BinaryGeneralDBOperator;
import org.openrdf.sail.generaldb.algebra.base.GeneralDBFromItem;
import org.openrdf.sail.generaldb.algebra.base.GeneralDBSqlExpr;
import org.openrdf.sail.generaldb.algebra.base.TripleGeneralDBOperator;
import org.openrdf.sail.generaldb.algebra.base.UnaryGeneralDBOperator;
import org.openrdf.sail.generaldb.algebra.egenhofer.GeneralDBSqlEgenhofer_Contains;
import org.openrdf.sail.generaldb.algebra.egenhofer.GeneralDBSqlEgenhofer_CoveredBy;
import org.openrdf.sail.generaldb.algebra.egenhofer.GeneralDBSqlEgenhofer_Covers;
import org.openrdf.sail.generaldb.algebra.egenhofer.GeneralDBSqlEgenhofer_Disjoint;
import org.openrdf.sail.generaldb.algebra.egenhofer.GeneralDBSqlEgenhofer_Equals;
import org.openrdf.sail.generaldb.algebra.egenhofer.GeneralDBSqlEgenhofer_Inside;
import org.openrdf.sail.generaldb.algebra.egenhofer.GeneralDBSqlEgenhofer_Meet;
import org.openrdf.sail.generaldb.algebra.egenhofer.GeneralDBSqlEgenhofer_Overlap;
import org.openrdf.sail.generaldb.algebra.rcc8.GeneralDBSqlRCC8_Dc;
import org.openrdf.sail.generaldb.algebra.rcc8.GeneralDBSqlRCC8_Ec;
import org.openrdf.sail.generaldb.algebra.rcc8.GeneralDBSqlRCC8_Eq;
import org.openrdf.sail.generaldb.algebra.rcc8.GeneralDBSqlRCC8_Ntpp;
import org.openrdf.sail.generaldb.algebra.rcc8.GeneralDBSqlRCC8_Ntppi;
import org.openrdf.sail.generaldb.algebra.rcc8.GeneralDBSqlRCC8_Po;
import org.openrdf.sail.generaldb.algebra.rcc8.GeneralDBSqlRCC8_Tpp;
import org.openrdf.sail.generaldb.algebra.rcc8.GeneralDBSqlRCC8_Tppi;
import org.openrdf.sail.generaldb.algebra.sf.GeneralDBSqlSF_Contains;
import org.openrdf.sail.generaldb.algebra.sf.GeneralDBSqlSF_Crosses;
import org.openrdf.sail.generaldb.algebra.sf.GeneralDBSqlSF_Disjoint;
import org.openrdf.sail.generaldb.algebra.sf.GeneralDBSqlSF_Equals;
import org.openrdf.sail.generaldb.algebra.sf.GeneralDBSqlSF_Intersects;
import org.openrdf.sail.generaldb.algebra.sf.GeneralDBSqlSF_Overlaps;
import org.openrdf.sail.generaldb.algebra.sf.GeneralDBSqlSF_Touches;
import org.openrdf.sail.generaldb.algebra.sf.GeneralDBSqlSF_Within;
import org.openrdf.sail.generaldb.evaluation.GeneralDBQueryBuilder;
import org.openrdf.sail.generaldb.evaluation.GeneralDBSqlBracketBuilder;
import org.openrdf.sail.generaldb.evaluation.GeneralDBSqlExprBuilder;
import org.openrdf.sail.generaldb.evaluation.GeneralDBSqlJoinBuilder;
import org.openrdf.sail.generaldb.evaluation.GeneralDBSqlQueryBuilder;
import org.openrdf.sail.rdbms.exceptions.RdbmsException;
import org.openrdf.sail.rdbms.exceptions.UnsupportedRdbmsOperatorException;

import eu.earthobservatory.constants.GeoConstants;
import eu.earthobservatory.constants.OGCConstants;

/**
 * Constructs an SQL query from {@link GeneralDBSqlExpr}s and {@link GeneralDBFromItem}s.
 * 
 * @author Manos Karpathiotakis <mk@di.uoa.gr>
 * @author Dimitrianos Savva <dimis@di.uoa.gr>
 */
public class PostGISQueryBuilder extends GeneralDBQueryBuilder {

	public static final String STRDFGEO_FIELD	= "strdfgeo";
	public static final String SRID_FIELD		= "srid";
	public static final String ST_TRANSFORM 	= "ST_Transform";
	public static final String ST_ASBINARY		= "ST_AsBinary";
	public static final String GEOGRAPHY		= "Geography";
	public static final String GEOMETRY			= "Geometry";
	
	/**
	 * If (spatial) label column met is null, I must not try to retrieve its srid. 
	 * Opting to ask for 'null' instead
	 */
	boolean nullLabel = false;

	public enum SpatialOperandsPostGIS { intersects, equals, contains, inside, left, right, above, below; }
	public enum SpatialFunctionsPostGIS 
	{ 	//stSPARQL++
		//Spatial Relationships
		ST_Equals,
		ST_Disjoint,
		ST_Intersects,
		ST_Touches, 
		ST_Crosses,
		ST_Within,
		ST_Contains,
		ST_Overlaps,
		ST_Relate,
		
		//Spatial Constructs - Binary
		ST_Union, 
		ST_Intersection, 
		ST_Difference,
		ST_Buffer,
		ST_Transform,
		ST_SymDifference,
		
		// Spatial Constructs - Binary (PostGIS namespace)
		ST_MakeLine,

		//Spatial Constructs - Unary
		ST_Envelope,
		ST_ConvexHull,
		ST_Boundary,

		// Spatial Constructs - Unary (PostGIS namespace)
		ST_Centroid,
		
		//Spatial Metrics - Binary
		ST_Distance,

		//Spatial Metrics - Unary
		ST_Area,

		//Spatial Properties - All Unary
		ST_Dimension,
		ST_GeometryType,
		ST_AsGML,
		ST_AsText,
		ST_SRID,
		ST_IsEmpty,
		ST_IsSimple,

		//GeoSPARQL
		//Simple Features
		SF_Equals,
		SF_Disjoint,
		SF_Intersects,
		SF_Touches,
		SF_Within,
		SF_Contains,
		SF_Overlaps,
		SF_Crosses,

		//RCC8
		RCC8_Eq,
		RCC8_Dc,
		RCC8_Ec,
		RCC8_Po,
		RCC8_Tppi,
		RCC8_Tpp,
		RCC8_Ntppi,
		RCC8_Ntpp,

		//Egenhofer
		EH_Equals,
		EH_Disjoint,
		EH_Meet,
		EH_Overlap,
		EH_Covers,
		EH_CoveredBy,
		EH_Inside,
		EH_Contains
	}

	/** Addition for datetime metric functions
	 * 
	 * @author George Garbis <ggarbis@di.uoa.gr>
	 * 
	 */
	public enum DateTimeFunctionPostGIS { Difference; }
	/***/
	
	public PostGISQueryBuilder() {
		super();
	}

	public PostGISQueryBuilder(GeneralDBSqlQueryBuilder builder) {
		super(builder);
		this.query = builder;
	}

	@Override
	protected void append(GeneralDBSqlNull expr, GeneralDBSqlExprBuilder filter) {
		filter.appendNull();
	}

	@Override
	protected void append(GeneralDBSqlIsNull expr, GeneralDBSqlExprBuilder filter) throws UnsupportedRdbmsOperatorException
	{
		dispatch(expr.getArg(), filter);
		filter.isNull();
	}

	@Override
	protected void append(GeneralDBSqlNot expr, GeneralDBSqlExprBuilder filter) throws UnsupportedRdbmsOperatorException
	{
		if (expr.getArg() instanceof GeneralDBSqlIsNull) {
			GeneralDBSqlIsNull arg = (GeneralDBSqlIsNull)expr.getArg();
			dispatch(arg.getArg(), filter);
			filter.isNotNull();
		}
		else {
			GeneralDBSqlBracketBuilder open = filter.not();
			dispatch(expr.getArg(), (GeneralDBSqlExprBuilder) open);
			open.close();
		}
	}

	@Override
	protected void append(GeneralDBDateTimeColumn var, GeneralDBSqlExprBuilder filter) {
		String alias = getDateTimeAlias(var.getRdbmsVar());
		filter.column(alias, "value");
	}
	
	@Override
	protected void append(GeneralDBLabelColumn var, GeneralDBSqlExprBuilder filter) {
		if (var.getRdbmsVar() == null || var.getRdbmsVar().isResource()) {
			filter.appendNull();
			nullLabel = true;
		}
		else {
			if(var.isSpatial())
			{
				filter.appendFunction(ST_ASBINARY);
				filter.openBracket();
				//XXX SRID
				filter.appendFunction(ST_TRANSFORM);
				filter.openBracket();
				//
				String alias = getLabelAlias(var.getRdbmsVar());

				filter.column(alias, STRDFGEO_FIELD);
				//XXX SRID
				filter.appendComma();
				filter.column(alias, SRID_FIELD);
				filter.closeBracket();
				//
				filter.closeBracket();

				//Adding srid field explicitly for my StrabonPolyhedron constructor later on!
				filter.appendComma();
				filter.column(alias, SRID_FIELD);
			}
			else
			{
				//XXX original/default case
				String alias = getLabelAlias(var.getRdbmsVar());
				filter.column(alias, "value");
			}
		}
	}

	@Override
	protected void append(GeneralDBSqlAnd expr, GeneralDBSqlExprBuilder filter)
			throws UnsupportedRdbmsOperatorException
			{
		dispatch(expr.getLeftArg(), filter);
		filter.and();
		dispatch(expr.getRightArg(), filter);
			}

	protected GeneralDBSqlJoinBuilder subJoinAndFilter(GeneralDBSqlJoinBuilder query, GeneralDBFromItem from)
			throws RdbmsException, UnsupportedRdbmsOperatorException
			{
		if (from instanceof GeneralDBUnionItem) {
			GeneralDBUnionItem union = (GeneralDBUnionItem)from;
			List<String> names = union.getSelectVarNames();
			List<GeneralDBColumnVar> vars = union.appendVars(new ArrayList<GeneralDBColumnVar>());
			GeneralDBSqlQueryBuilder subquery = query.subquery();
			for (GeneralDBFromItem item : union.getUnion()) {
				for (int i = 0, n = names.size(); i < n; i++) {
					GeneralDBColumnVar var = item.getVar(names.get(i));
					GeneralDBSqlExprBuilder select = subquery.select();
					if (var == null) {
						select.appendNull();
					}
					else if (var.isImplied()) {
						select.appendNumeric(vf.getInternalId(var.getValue()));
					}
					else {
						select.column(var.getAlias(), var.getColumn());
					}
					select.as(vars.get(i).getColumn());
				}
				from(subquery, item);
				subquery = subquery.union();
			}
		}
		for (GeneralDBFromItem join : from.getJoins()) {
			join(query, join);
		}
		for (GeneralDBSqlExpr expr : from.getFilters()) {
			dispatch(expr, query.on().and());
		}
		return query;
			}

	//FIXME my addition from here on

	//Issue with this function: crashes when MathExpr is present in Select but does not
	//involve spatial variables! must escape this somehow
	@Override
	public GeneralDBQueryBuilder construct(GeneralDBSqlExpr expr) throws UnsupportedRdbmsOperatorException
	{
		if(!(expr instanceof GeneralDBSqlSpatialMetricBinary) 
				&&!(expr instanceof GeneralDBSqlSpatialMetricTriple)
				&&!(expr instanceof GeneralDBSqlSpatialMetricUnary)
				&&!(expr instanceof GeneralDBSqlMathExpr)
				&&!(expr instanceof GeneralDBSqlSpatialProperty)
				&& !(expr instanceof GeneralDBSqlGeoSpatial))
		{
			query.select().appendFunction(ST_ASBINARY);
		}
		else
		{
			query.select();
		}
		
		if(expr instanceof BinaryGeneralDBOperator)
		{
			dispatchBinarySqlOperator((BinaryGeneralDBOperator) expr, query.select);
		}
		else if(expr instanceof UnaryGeneralDBOperator)
		{
			dispatchUnarySqlOperator((UnaryGeneralDBOperator) expr, query.select);
		}
		else if(expr instanceof GeneralDBSqlSpatialMetricTriple)
		{
			dispatchTripleSqlOperator((GeneralDBSqlSpatialMetricTriple) expr, query.select);
		}
		else if(expr instanceof GeneralDBSqlSpatialConstructTriple)
		{
			dispatchTripleSqlOperator((GeneralDBSqlSpatialConstructTriple) expr, query.select);
		}
		//SRID support must be explicitly added!

		return this;
	}

	//Spatial Relationship Functions	
	@Override
	protected void append(GeneralDBSqlEqualsSpatial expr, GeneralDBSqlExprBuilder filter)
			throws UnsupportedRdbmsOperatorException {

		appendGeneralDBSpatialFunctionBinary(expr, filter, SpatialFunctionsPostGIS.ST_Equals);
	}
	
	@Override
	protected void append(GeneralDBSqlDisjoint expr, GeneralDBSqlExprBuilder filter)
			throws UnsupportedRdbmsOperatorException {

		appendGeneralDBSpatialFunctionBinary(expr, filter, SpatialFunctionsPostGIS.ST_Disjoint);
	}
	
	@Override
	protected void append(GeneralDBSqlIntersects expr, 	GeneralDBSqlExprBuilder filter)
			throws UnsupportedRdbmsOperatorException {
		appendGeneralDBSpatialFunctionBinary(expr, filter, SpatialFunctionsPostGIS.ST_Intersects);
	}
	
	@Override
	protected void append(GeneralDBSqlTouches expr, GeneralDBSqlExprBuilder filter)
			throws UnsupportedRdbmsOperatorException {

		appendGeneralDBSpatialFunctionBinary(expr, filter, SpatialFunctionsPostGIS.ST_Touches);
	}
	
	@Override
	protected void append(GeneralDBSqlCrosses expr, GeneralDBSqlExprBuilder filter)
			throws UnsupportedRdbmsOperatorException {

		appendGeneralDBSpatialFunctionBinary(expr, filter, SpatialFunctionsPostGIS.ST_Crosses);
	}	
	
	@Override
	protected void append(GeneralDBSqlWithin expr, GeneralDBSqlExprBuilder filter)
			throws UnsupportedRdbmsOperatorException {

		appendGeneralDBSpatialFunctionBinary(expr, filter, SpatialFunctionsPostGIS.ST_Within);

	}
	
	@Override
	protected void append(GeneralDBSqlContains expr, GeneralDBSqlExprBuilder filter)
			throws UnsupportedRdbmsOperatorException {

		appendGeneralDBSpatialFunctionBinary(expr, filter, SpatialFunctionsPostGIS.ST_Contains);
	}
	
	@Override
	protected void append(GeneralDBSqlOverlaps expr, GeneralDBSqlExprBuilder filter)
			throws UnsupportedRdbmsOperatorException {

		appendGeneralDBSpatialFunctionBinary(expr, filter, SpatialFunctionsPostGIS.ST_Overlaps);
	}
	
	@Override
	protected void append(GeneralDBSqlRelate expr, GeneralDBSqlExprBuilder filter)
			throws UnsupportedRdbmsOperatorException {
		appendGeneralDBSpatialFunctionTriple(expr, filter, SpatialFunctionsPostGIS.ST_Relate);
	}

//	@Override
//	protected void append(GeneralDBSqlCovers expr, GeneralDBSqlExprBuilder filter)
//			throws UnsupportedRdbmsOperatorException {
//
//		appendGeneralDBSpatialFunctionBinary(expr, filter, SpatialFunctionsPostGIS.ST_Covers);
//	}
//
//	@Override
//	protected void append(GeneralDBSqlCoveredBy expr, GeneralDBSqlExprBuilder filter)
//			throws UnsupportedRdbmsOperatorException {
//
//		appendGeneralDBSpatialFunctionBinary(expr, filter, SpatialFunctionsPostGIS.ST_CoveredBy);
//	}

	

	

	
	@Override	
	protected void append(GeneralDBSqlLeft expr, GeneralDBSqlExprBuilder filter)
			throws UnsupportedRdbmsOperatorException
			{
		appendStSPARQLSpatialOperand(expr, filter, SpatialOperandsPostGIS.left);
			}

	@Override
	protected void append(GeneralDBSqlRight expr, GeneralDBSqlExprBuilder filter) throws UnsupportedRdbmsOperatorException
	{
		appendStSPARQLSpatialOperand(expr, filter, SpatialOperandsPostGIS.right);
	}

	@Override
	protected void append(GeneralDBSqlAbove expr, GeneralDBSqlExprBuilder filter) throws UnsupportedRdbmsOperatorException
	{
		appendStSPARQLSpatialOperand(expr, filter, SpatialOperandsPostGIS.above);
	}

	@Override
	protected void append(GeneralDBSqlBelow expr, GeneralDBSqlExprBuilder filter) throws UnsupportedRdbmsOperatorException
	{
		appendStSPARQLSpatialOperand(expr, filter, SpatialOperandsPostGIS.below);
	}

	@Override
	protected void append(GeneralDBSqlMbbIntersects expr, GeneralDBSqlExprBuilder filter)
			throws UnsupportedRdbmsOperatorException {
		appendStSPARQLSpatialOperand(expr, filter, SpatialOperandsPostGIS.intersects);
	}

	@Override
	protected void append(GeneralDBSqlMbbWithin expr, GeneralDBSqlExprBuilder filter)
			throws UnsupportedRdbmsOperatorException {
		appendStSPARQLSpatialOperand(expr, filter, SpatialOperandsPostGIS.inside);
	}

	
	@Override
	protected void append(GeneralDBSqlMbbContains expr, GeneralDBSqlExprBuilder filter) throws UnsupportedRdbmsOperatorException {
		appendStSPARQLSpatialOperand(expr, filter, SpatialOperandsPostGIS.contains);
	}

	
	@Override
	protected void append(GeneralDBSqlMbbEquals expr, GeneralDBSqlExprBuilder filter)
			throws UnsupportedRdbmsOperatorException {
		appendStSPARQLSpatialOperand(expr, filter, SpatialOperandsPostGIS.equals);
	}

	//GeoSPARQL - Spatial Relationship Functions 
	//Simple Features
	@Override
	protected void append(GeneralDBSqlSF_Contains expr, GeneralDBSqlExprBuilder filter)
			throws UnsupportedRdbmsOperatorException
			{
		appendGeneralDBSpatialFunctionBinary(expr, filter,SpatialFunctionsPostGIS.ST_Contains);
			}

	@Override
	protected void append(GeneralDBSqlSF_Crosses expr, GeneralDBSqlExprBuilder filter)
			throws UnsupportedRdbmsOperatorException
			{
		//follow the same approach as stSPARQL, because the implementation used in
		//appendgeoSPARQLSpatialRelation (which is based on ST_Relate) is not correct for this case
		appendGeneralDBSpatialFunctionBinary(expr, filter, SpatialFunctionsPostGIS.ST_Crosses);
			}

	@Override
	protected void append(GeneralDBSqlSF_Disjoint expr, GeneralDBSqlExprBuilder filter)
			throws UnsupportedRdbmsOperatorException
			{
		appendGeneralDBSpatialFunctionBinary(expr, filter,SpatialFunctionsPostGIS.ST_Disjoint);
			}

	@Override
	protected void append(GeneralDBSqlSF_Equals expr, GeneralDBSqlExprBuilder filter)
			throws UnsupportedRdbmsOperatorException
			{
		appendGeneralDBSpatialFunctionBinary(expr, filter,SpatialFunctionsPostGIS.ST_Equals);
			}

	@Override
	protected void append(GeneralDBSqlSF_Intersects expr, GeneralDBSqlExprBuilder filter)
			throws UnsupportedRdbmsOperatorException
			{
		appendGeneralDBSpatialFunctionBinary(expr, filter,SpatialFunctionsPostGIS.ST_Intersects);
			}

	@Override
	protected void append(GeneralDBSqlSF_Overlaps expr, GeneralDBSqlExprBuilder filter)
			throws UnsupportedRdbmsOperatorException
			{
		//follow the same approach as stSPARQL, because the implementation used in
		//appendgeoSPARQLSpatialRelation (which is based on ST_Relate) is not correct for this case
		appendGeneralDBSpatialFunctionBinary(expr, filter, SpatialFunctionsPostGIS.ST_Overlaps);
			}

	@Override
	protected void append(GeneralDBSqlSF_Touches expr, GeneralDBSqlExprBuilder filter)
			throws UnsupportedRdbmsOperatorException
			{
		appendGeneralDBSpatialFunctionBinary(expr, filter,SpatialFunctionsPostGIS.ST_Touches);
			}

	@Override
	protected void append(GeneralDBSqlSF_Within expr, GeneralDBSqlExprBuilder filter)
			throws UnsupportedRdbmsOperatorException
			{
		appendGeneralDBSpatialFunctionBinary(expr, filter,SpatialFunctionsPostGIS.ST_Within);
			}

	//Egenhofer
	@Override
	protected void append(GeneralDBSqlEgenhofer_CoveredBy expr, GeneralDBSqlExprBuilder filter)
			throws UnsupportedRdbmsOperatorException
			{
		appendgeoSPARQLSpatialRelation(expr, filter,SpatialFunctionsPostGIS.EH_CoveredBy);
			}

	@Override
	protected void append(GeneralDBSqlEgenhofer_Covers expr, GeneralDBSqlExprBuilder filter)
			throws UnsupportedRdbmsOperatorException
			{
		appendgeoSPARQLSpatialRelation(expr, filter,SpatialFunctionsPostGIS.EH_Covers);
			}

	@Override
	protected void append(GeneralDBSqlEgenhofer_Contains expr, GeneralDBSqlExprBuilder filter)
			throws UnsupportedRdbmsOperatorException
			{
		appendgeoSPARQLSpatialRelation(expr, filter,SpatialFunctionsPostGIS.EH_Contains);
			}

	@Override
	protected void append(GeneralDBSqlEgenhofer_Disjoint expr, GeneralDBSqlExprBuilder filter)
			throws UnsupportedRdbmsOperatorException
			{
		appendgeoSPARQLSpatialRelation(expr, filter,SpatialFunctionsPostGIS.EH_Disjoint);
			}

	@Override
	protected void append(GeneralDBSqlEgenhofer_Equals expr, GeneralDBSqlExprBuilder filter)
			throws UnsupportedRdbmsOperatorException
			{
		appendgeoSPARQLSpatialRelation(expr, filter,SpatialFunctionsPostGIS.EH_Equals);
			}

	@Override
	protected void append(GeneralDBSqlEgenhofer_Inside expr, GeneralDBSqlExprBuilder filter)
			throws UnsupportedRdbmsOperatorException
			{
		appendgeoSPARQLSpatialRelation(expr, filter,SpatialFunctionsPostGIS.EH_Inside);
			}

	@Override
	protected void append(GeneralDBSqlEgenhofer_Meet expr, GeneralDBSqlExprBuilder filter)
			throws UnsupportedRdbmsOperatorException
			{
		appendgeoSPARQLSpatialRelation(expr, filter,SpatialFunctionsPostGIS.EH_Meet);
			}

	@Override
	protected void append(GeneralDBSqlEgenhofer_Overlap expr, GeneralDBSqlExprBuilder filter)
			throws UnsupportedRdbmsOperatorException
			{
		//follow the same approach as stSPARQL, because the implementation used in
		//appendgeoSPARQLSpatialRelation (which is based on ST_Relate) is not correct for this case
		appendGeneralDBSpatialFunctionBinary(expr, filter, SpatialFunctionsPostGIS.ST_Overlaps);
			}

	//RCC8
	@Override
	protected void append(GeneralDBSqlRCC8_Dc expr, GeneralDBSqlExprBuilder filter)
			throws UnsupportedRdbmsOperatorException
			{
		appendgeoSPARQLSpatialRelation(expr, filter,SpatialFunctionsPostGIS.RCC8_Dc);
			}

	@Override
	protected void append(GeneralDBSqlRCC8_Eq expr, GeneralDBSqlExprBuilder filter)
			throws UnsupportedRdbmsOperatorException
			{
		appendgeoSPARQLSpatialRelation(expr, filter,SpatialFunctionsPostGIS.RCC8_Eq);
			}

	@Override
	protected void append(GeneralDBSqlRCC8_Ec expr, GeneralDBSqlExprBuilder filter)
			throws UnsupportedRdbmsOperatorException
			{
		appendgeoSPARQLSpatialRelation(expr, filter,SpatialFunctionsPostGIS.RCC8_Ec);
			}

	@Override
	protected void append(GeneralDBSqlRCC8_Po expr, GeneralDBSqlExprBuilder filter)
			throws UnsupportedRdbmsOperatorException
			{
		appendgeoSPARQLSpatialRelation(expr, filter,SpatialFunctionsPostGIS.RCC8_Po);
			}

	@Override
	protected void append(GeneralDBSqlRCC8_Tppi expr, GeneralDBSqlExprBuilder filter)
			throws UnsupportedRdbmsOperatorException
			{
		appendgeoSPARQLSpatialRelation(expr, filter,SpatialFunctionsPostGIS.RCC8_Tppi);
			}

	@Override
	protected void append(GeneralDBSqlRCC8_Tpp expr, GeneralDBSqlExprBuilder filter)
			throws UnsupportedRdbmsOperatorException
			{
		appendgeoSPARQLSpatialRelation(expr, filter,SpatialFunctionsPostGIS.RCC8_Tpp);
			}

	@Override
	protected void append(GeneralDBSqlRCC8_Ntpp expr, GeneralDBSqlExprBuilder filter)
			throws UnsupportedRdbmsOperatorException
			{
		appendgeoSPARQLSpatialRelation(expr, filter,SpatialFunctionsPostGIS.RCC8_Ntpp);
			}

	@Override
	protected void append(GeneralDBSqlRCC8_Ntppi expr, GeneralDBSqlExprBuilder filter)
			throws UnsupportedRdbmsOperatorException
			{
		appendgeoSPARQLSpatialRelation(expr, filter,SpatialFunctionsPostGIS.RCC8_Ntppi);
			}

	//Spatial Construct Functions
	@Override
	protected void append(GeneralDBSqlGeoUnion expr, GeneralDBSqlExprBuilder filter)
			throws UnsupportedRdbmsOperatorException
			{
		appendGeneralDBSpatialFunctionBinary(expr, filter, SpatialFunctionsPostGIS.ST_Union);
			}

	@Override
	protected void append(GeneralDBSqlGeoBuffer expr, GeneralDBSqlExprBuilder filter) throws UnsupportedRdbmsOperatorException
	{
		appendBuffer(expr, filter, SpatialFunctionsPostGIS.ST_Buffer);
	}

	//XXX Different Behavior
	@Override
	protected void append(GeneralDBSqlGeoTransform expr, GeneralDBSqlExprBuilder filter)
			throws UnsupportedRdbmsOperatorException
			{
		appendTransformFunc(expr, filter);
			}

	@Override
	protected void append(GeneralDBSqlGeoEnvelope expr, GeneralDBSqlExprBuilder filter)
			throws UnsupportedRdbmsOperatorException
			{
		appendGeneralDBSpatialFunctionUnary(expr, filter, SpatialFunctionsPostGIS.ST_Envelope);
			}

	@Override
	protected void append(GeneralDBSqlGeoConvexHull expr, GeneralDBSqlExprBuilder filter)
			throws UnsupportedRdbmsOperatorException
			{
		appendGeneralDBSpatialFunctionUnary(expr, filter, SpatialFunctionsPostGIS.ST_ConvexHull);
			}

	@Override
	protected void append(GeneralDBSqlGeoBoundary expr, GeneralDBSqlExprBuilder filter)
			throws UnsupportedRdbmsOperatorException
			{
		appendGeneralDBSpatialFunctionUnary(expr, filter, SpatialFunctionsPostGIS.ST_Boundary);
			}

	@Override
	protected void append(GeneralDBSqlGeoIntersection expr, GeneralDBSqlExprBuilder filter) throws UnsupportedRdbmsOperatorException
	{
		appendGeneralDBSpatialFunctionBinary(expr, filter, SpatialFunctionsPostGIS.ST_Intersection);
	}

	@Override
	protected void append(GeneralDBSqlGeoDifference expr, GeneralDBSqlExprBuilder filter) throws UnsupportedRdbmsOperatorException
	{
		appendGeneralDBSpatialFunctionBinary(expr, filter, SpatialFunctionsPostGIS.ST_Difference);
	}

	@Override
	protected void append(GeneralDBSqlGeoSymDifference expr, GeneralDBSqlExprBuilder filter) throws UnsupportedRdbmsOperatorException
	{
		appendGeneralDBSpatialFunctionBinary(expr, filter, SpatialFunctionsPostGIS.ST_SymDifference);
	}
	
	@Override
	protected void append(GeneralDBSqlST_MakeLine expr, GeneralDBSqlExprBuilder filter) throws UnsupportedRdbmsOperatorException {
		appendGeneralDBSpatialFunctionBinary(expr, filter, SpatialFunctionsPostGIS.ST_MakeLine);
	}
	
	@Override
	protected void append(GeneralDBSqlST_Centroid expr, GeneralDBSqlExprBuilder filter) throws UnsupportedRdbmsOperatorException {
		appendGeneralDBSpatialFunctionUnary(expr, filter, SpatialFunctionsPostGIS.ST_Centroid);
	}

	/** Addition for datetime metric functions
	 * 
	 * @author George Garbis <ggarbis@di.uoa.gr>
	 * 
	 */
	@Override
	protected void append(GeneralDBSqlDiffDateTime expr, GeneralDBSqlExprBuilder filter)
		throws UnsupportedRdbmsOperatorException
	{
		appendGeneralDBDateTimeFunctionBinary(expr, filter, DateTimeFunctionPostGIS.Difference);
	}
	/***/
	
	//Spatial Metric Functions
	@Override
	protected void append(GeneralDBSqlGeoDistance expr, GeneralDBSqlExprBuilder filter) throws UnsupportedRdbmsOperatorException
	{
		appendDistance(expr, filter, SpatialFunctionsPostGIS.ST_Distance);
	}

	@Override
	protected void append(GeneralDBSqlGeoArea expr, GeneralDBSqlExprBuilder filter)
			throws UnsupportedRdbmsOperatorException
			{
		appendGeneralDBSpatialFunctionUnary(expr, filter, SpatialFunctionsPostGIS.ST_Area);
			}

	//Spatial Property Functions
	@Override
	protected void append(GeneralDBSqlGeoDimension expr, GeneralDBSqlExprBuilder filter)
			throws UnsupportedRdbmsOperatorException
			{
		appendGeneralDBSpatialFunctionUnary(expr, filter, SpatialFunctionsPostGIS.ST_Dimension);
			}

	@Override
	protected void append(GeneralDBSqlGeoGeometryType expr, GeneralDBSqlExprBuilder filter)
			throws UnsupportedRdbmsOperatorException
			{
		appendGeneralDBSpatialFunctionUnary(expr, filter, SpatialFunctionsPostGIS.ST_GeometryType);
			}

	@Override
	protected void append(GeneralDBSqlGeoAsText expr, GeneralDBSqlExprBuilder filter)
			throws UnsupportedRdbmsOperatorException
			{
		appendGeneralDBSpatialFunctionUnary(expr, filter, SpatialFunctionsPostGIS.ST_AsText);
			}

	@Override
	protected void append(GeneralDBSqlGeoAsGML expr, GeneralDBSqlExprBuilder filter)
	throws UnsupportedRdbmsOperatorException
	{
		appendGeneralDBSpatialFunctionUnary(expr, filter, SpatialFunctionsPostGIS.ST_AsGML);
	}

/*	
   @Override
	protected void append(GeneralDBSqlGeoSrid expr, GeneralDBSqlExprBuilder filter)
	throws UnsupportedRdbmsOperatorException
	{
		appendGeneralDBSpatialFunctionUnary(expr, filter, SpatialFunctionsPostGIS.ST_SRID);
	}
*/
	/**
	 * This will call the method below: 
	 * {@link org.openrdf.sail.postgis.evaluation.PostGISQueryBuilder.append#(org.openrdf.sail.generaldb.algebra.GeneralDBSqlGeoSrid, org.openrdf.sail.generaldb.evaluation.GeneralDBSqlExprBuilder)}
	 */
	@Override
	protected void append(GeneralDBSqlGeoSrid expr, GeneralDBSqlExprBuilder filter) throws UnsupportedRdbmsOperatorException {
		appendSrid(expr, filter);
	}
	
	/**
	 * This will call the method below: 
	 * {@link org.openrdf.sail.postgis.evaluation.PostGISQueryBuilder.append#(org.openrdf.sail.generaldb.algebra.GeneralDBSqlGeoSrid, org.openrdf.sail.generaldb.evaluation.GeneralDBSqlExprBuilder)}
	 */
	@Override
	protected void append(GeneralDBSqlGeoSPARQLSrid expr, GeneralDBSqlExprBuilder filter) throws UnsupportedRdbmsOperatorException {
		appendSrid(expr, filter);
	}
	
	/**
	 * Special case because I need to retrieve a single different column from geo_values when this function occurs
	 * in the select clause and not call the st_srid() function, which will always give me 4326.
	 */
	protected void appendSrid(GeneralDBSqlAbstractGeoSrid expr, GeneralDBSqlExprBuilder filter) throws UnsupportedRdbmsOperatorException {
		filter.openBracket();

		boolean check1 = expr.getArg().getClass().getCanonicalName().equals("org.openrdf.sail.generaldb.algebra.GeneralDBSqlNull");
		boolean check2 = false;
		if(expr.getArg() instanceof GeneralDBLabelColumn)
		{
			if(((GeneralDBLabelColumn) expr.getArg()).getRdbmsVar() == null || ((GeneralDBLabelColumn) expr.getArg()).getRdbmsVar().isResource())
			{
				check2 = true;
			}
		}
		if(check1)
		{
			this.append((GeneralDBSqlNull)expr.getArg(), filter);

		}
		else if (check2)
		{
			appendMBB((GeneralDBLabelColumn)(expr.getArg()),filter);
		}
		else
		{
			// Incorporating SRID
			GeneralDBSqlExpr tmp = expr;
			if(tmp.getParentNode() == null)
			{
				String sridExpr;
				while(true)
				{
					GeneralDBSqlExpr child = null;

					if(tmp instanceof BinaryGeneralDBOperator)
					{
						child = ((BinaryGeneralDBOperator) tmp).getLeftArg();
					}
					else if(tmp instanceof UnaryGeneralDBOperator)
					{
						child = ((UnaryGeneralDBOperator) tmp).getArg();
					}
					else if(tmp instanceof GeneralDBLabelColumn)
					{
						//Reached the innermost left var -> need to capture its SRID
						String colRef = null;
						String alias = null;
						if (((GeneralDBLabelColumn) tmp).getRdbmsVar()==null || ((GeneralDBLabelColumn) tmp).getRdbmsVar().isResource()) {
							//Predicates used in triple patterns non-existent in db
							colRef = "NULL";
						}
						else
						{
							//Reached the innermost left var -> need to capture its SRID
							alias = getLabelAlias(((GeneralDBLabelColumn) tmp).getRdbmsVar());
							colRef = alias + ".srid";
						}
						sridExpr = colRef;
						
						filter.append(sridExpr);
						filter.closeBracket();
						
						if (alias != null) { // append an alias for the column of the SRID, 
							// replacing the part of the name corresponding to the geo_values table
							filter.as((alias + "_srid").replace("l_", ""));
						}
						
						return;
					}
					else if(tmp instanceof GeneralDBStringValue)
					{
						// We need the srid, since this is a constant in the query, so we
						// should not return just the default SRID, but instead we should
						// determine it.
						// Computing it based on the following code using ST_SRID, ST_GeomFromText,
						// and appendWKT is not the best way, but it does the job good.
						break;
					}
					
					tmp = child;
				}
			}

			// we have to compute it
			filter.appendFunction("ST_SRID");
			filter.openBracket();
			
			if(expr.getArg() instanceof GeneralDBStringValue)
			{
				appendWKT(expr.getArg(),filter);
			}
			else if(expr.getArg() instanceof GeneralDBSqlSpatialConstructBinary)
			{
				appendConstructFunction(expr.getArg(), filter);
			}
			else if(expr.getArg() instanceof GeneralDBSqlSpatialConstructUnary)
			{
				appendConstructFunction(expr.getArg(), filter);
			}
			else if(expr.getArg() instanceof GeneralDBSqlSpatialConstructTriple)
			{
				appendConstructFunction(expr.getArg(), filter);
			}
			else if(expr.getArg() instanceof GeneralDBSqlCase)
			{
				GeneralDBLabelColumn onlyLabel = (GeneralDBLabelColumn)((GeneralDBSqlCase)expr.getArg()).getEntries().get(0).getResult();
				appendMBB(onlyLabel, filter); 
			}
			else
			{
				appendMBB((GeneralDBLabelColumn)(expr.getArg()),filter);
			}

			filter.closeBracket();
		}
		
		filter.closeBracket();
	}

	@Override
	protected void append(GeneralDBSqlGeoIsSimple expr, GeneralDBSqlExprBuilder filter)
			throws UnsupportedRdbmsOperatorException
			{
		appendGeneralDBSpatialFunctionUnary(expr, filter, SpatialFunctionsPostGIS.ST_IsSimple);
			}

	@Override
	protected void append(GeneralDBSqlGeoIsEmpty expr, GeneralDBSqlExprBuilder filter)
			throws UnsupportedRdbmsOperatorException
			{
		appendGeneralDBSpatialFunctionUnary(expr, filter, SpatialFunctionsPostGIS.ST_IsEmpty);
			}


	/**
	 * 'helper' functions
	 * @throws UnsupportedRdbmsOperatorException 
	 */
	@Override
	protected String appendWKT(GeneralDBSqlExpr expr, GeneralDBSqlExprBuilder filter) throws UnsupportedRdbmsOperatorException
	{
		GeneralDBStringValue arg = (GeneralDBStringValue) expr;
		String raw = arg.getValue();
		
		// parse raw WKT
		AbstractWKT wkt = new AbstractWKT(raw);
		filter.append(" ST_GeomFromText('" + wkt.getWKT() + "'," + String.valueOf(wkt.getSRID()) + ")");
		
		return raw;
	}

	protected String appendConstantWKT(GeneralDBSqlExpr expr, GeneralDBSqlExprBuilder filter) throws UnsupportedRdbmsOperatorException
	{
		GeneralDBStringValue arg = (GeneralDBStringValue) expr;
		String raw = arg.getValue();
		
		// parse raw WKT
		AbstractWKT wkt = new AbstractWKT(raw);
		// transform constant geometry to the default SRID
		filter.append("ST_Transform(");
		filter.append(" ST_GeomFromText('" + wkt.getWKT() + "'," + String.valueOf(wkt.getSRID()) + ")");
		filter.append(", "+GeoConstants.defaultSRID +")");
		
		return raw;
	}
	
	//Used in all the generaldb boolean spatial functions of the form ?GEO1 ~ ?GEO2 
	//	protected void appendStSPARQLSpatialOperand(BinaryGeneralDBOperator expr, GeneralDBSqlExprBuilder filter, SpatialOperandsPostGIS operand) throws UnsupportedRdbmsOperatorException
	//	{
	//		filter.openBracket();
	//
	//		boolean check1a = expr.getLeftArg().getClass().getCanonicalName().equals("org.openrdf.sail.generaldb.algebra.GeneralDBSqlNull");
	//		//boolean check2a = expr.getRightArg().getClass().getCanonicalName().equals("org.openrdf.sail.generaldb.algebra.GeneralDBSqlNull");
	//
	//		if(check1a)
	//		{
	//			this.append((GeneralDBSqlNull)expr.getLeftArg(), filter);
	//
	//		}
	////		else if(check2a)
	////		{
	////			this.append((GeneralDBSqlNull)expr.getRightArg(), filter);
	////		}
	//		else
	//		{
	//			if(expr.getLeftArg() instanceof GeneralDBSqlCase)
	//			{
	//				this.append((GeneralDBSqlCase)expr.getLeftArg(), filter);
	//			}
	//			else if(expr.getLeftArg() instanceof GeneralDBStringValue)
	//			{
	//				appendWKT(expr.getLeftArg(),filter);
	//			}
	//			else if(expr.getLeftArg() instanceof GeneralDBSqlSpatialConstructBinary)
	//			{
	//				appendConstructFunction(expr.getLeftArg(), filter);
	//			}
	//			else if(expr.getLeftArg() instanceof GeneralDBSqlSpatialConstructUnary)
	//			{
	//				appendConstructFunction(expr.getLeftArg(), filter);
	//			}
	//			else
	//			{
	//				appendMBB((GeneralDBLabelColumn)(expr.getLeftArg()),filter);
	//			}
	//
	//			switch(operand)
	//			{
	//			case mbbIntersects: filter.mbbIntersects(); break;
	//			case equals: filter.equals(); break;
	//			case contains: filter.contains(); break;
	//			case inside: filter.inside(); break;
	//			case left: filter.left(); break;
	//			case right: filter.right(); break;
	//			case above: filter.above(); break;
	//			case below: filter.below(); break;
	//			}
	//
	//			boolean check2a = expr.getRightArg().getClass().getCanonicalName().equals("org.openrdf.sail.generaldb.algebra.GeneralDBSqlNull");
	//
	//			if(check2a)
	//			{
	//				this.append((GeneralDBSqlNull)expr.getRightArg(), filter);
	//			}
	//			else
	//			{
	//
	//				if(expr.getRightArg() instanceof GeneralDBSqlCase)
	//				{
	//					this.append((GeneralDBSqlCase)expr.getRightArg(), filter);
	//				}
	//				else if(expr.getRightArg() instanceof GeneralDBStringValue)
	//				{
	//					appendWKT(expr.getRightArg(),filter);
	//				}
	//				else if(expr.getRightArg() instanceof GeneralDBSqlSpatialConstructBinary)
	//				{
	//					appendConstructFunction(expr.getRightArg(), filter);
	//				}
	//				else if(expr.getRightArg() instanceof GeneralDBSqlSpatialConstructUnary)
	//				{
	//					appendConstructFunction(expr.getRightArg(), filter);
	//				}
	//				else
	//				{
	//					appendMBB((GeneralDBLabelColumn)(expr.getRightArg()),filter);
	//				}
	//
	//			}
	//		}
	//		filter.closeBracket();
	//	}


	protected void appendStSPARQLSpatialOperand(BinaryGeneralDBOperator expr, GeneralDBSqlExprBuilder filter, SpatialOperandsPostGIS operand) throws UnsupportedRdbmsOperatorException
	{
		filter.openBracket();

		boolean check1a = expr.getLeftArg().getClass().getCanonicalName().equals("org.openrdf.sail.generaldb.algebra.GeneralDBSqlNull");
		boolean check2a = expr.getRightArg().getClass().getCanonicalName().equals("org.openrdf.sail.generaldb.algebra.GeneralDBSqlNull");

		if(check1a)
		{
			this.append((GeneralDBSqlNull)expr.getLeftArg(), filter);

		}
		else if(check2a)
		{
			this.append((GeneralDBSqlNull)expr.getRightArg(), filter);
		}
		else
		{
			if(expr.getLeftArg() instanceof GeneralDBSqlCase)
			{
				this.append((GeneralDBSqlCase)expr.getLeftArg(), filter);
			}
			else if(expr.getLeftArg() instanceof GeneralDBStringValue)
			{
				appendWKT(expr.getLeftArg(),filter);
			}
			else if(expr.getLeftArg() instanceof GeneralDBSqlSpatialConstructBinary)
			{
				appendConstructFunction(expr.getLeftArg(), filter);
			}
			else if(expr.getLeftArg() instanceof GeneralDBSqlSpatialConstructUnary)
			{
				appendConstructFunction(expr.getLeftArg(), filter);
			}
			else if(expr.getLeftArg() instanceof GeneralDBSqlSpatialConstructTriple)
			{
				appendConstructFunction(expr.getLeftArg(), filter);
			}
			else
			{
				appendMBB((GeneralDBLabelColumn)(expr.getLeftArg()),filter);
			}

			switch(operand)
			{
			case intersects: filter.intersectsMBB(); break;
			case equals: filter.equalsMBB(); break;
			case contains: filter.containsMBB(); break;
			case inside: filter.insideMBB(); break;
			case left: filter.leftMBB(); break;
			case right: filter.rightMBB(); break;
			case above: filter.aboveMBB(); break;
			case below: filter.belowMBB(); break;
			}

			//			boolean check2a = expr.getRightArg().getClass().getCanonicalName().equals("org.openrdf.sail.generaldb.algebra.GeneralDBSqlNull");
			//
			//			if(check2a)
			//			{
			//				this.append((GeneralDBSqlNull)expr.getRightArg(), filter);
			//			}
			//			else
			//			{

			if(expr.getRightArg() instanceof GeneralDBSqlCase)
			{
				this.append((GeneralDBSqlCase)expr.getRightArg(), filter);
			}
			else if(expr.getRightArg() instanceof GeneralDBStringValue)
			{
				appendWKT(expr.getRightArg(),filter);
			}
			else if(expr.getRightArg() instanceof GeneralDBSqlSpatialConstructBinary)
			{
				appendConstructFunction(expr.getRightArg(), filter);
			}
			else if(expr.getRightArg() instanceof GeneralDBSqlSpatialConstructUnary)
			{
				appendConstructFunction(expr.getRightArg(), filter);
			}
			else if(expr.getLeftArg() instanceof GeneralDBSqlSpatialConstructTriple)
			{
				appendConstructFunction(expr.getLeftArg(), filter);
			}
			else
			{
				appendMBB((GeneralDBLabelColumn)(expr.getRightArg()),filter);
			}

			//}
		}
		filter.closeBracket();
	}

	//Used in all the generaldb stsparql boolean spatial functions of the form ST_Function(?GEO1,?GEO2) 
	protected void appendTransformFunc(GeneralDBSqlGeoTransform expr, GeneralDBSqlExprBuilder filter) throws UnsupportedRdbmsOperatorException
	{
		//In the case where no variable is present in the expression! e.g ConvexHull("POLYGON((.....))")
		boolean sridNeeded = true;
		//XXX Incorporating SRID
		String sridExpr = null;

		filter.openBracket();
		filter.appendFunction(ST_TRANSFORM);
		filter.openBracket();

		boolean check1 = expr.getLeftArg().getClass().getCanonicalName().equals("org.openrdf.sail.generaldb.algebra.GeneralDBSqlNull");
		boolean check2 = expr.getRightArg().getClass().getCanonicalName().equals("org.openrdf.sail.generaldb.algebra.GeneralDBSqlNull");
	
		if(check1)
		{
			this.append((GeneralDBSqlNull)expr.getLeftArg(), filter);

		}
		else if(check2)
		{
			this.append((GeneralDBSqlNull)expr.getRightArg(), filter);
		}
		else
		{
			GeneralDBSqlExpr tmp = expr;
			if(tmp instanceof GeneralDBSqlSpatialConstructBinary && tmp.getParentNode() == null)
			{
				while(true)
				{
					GeneralDBSqlExpr child;

					if(tmp instanceof BinaryGeneralDBOperator)
					{
						child = ((BinaryGeneralDBOperator) tmp).getLeftArg();
					}
					else //(tmp instanceof UnaryGeneralDBOperator)
					{
						child = ((UnaryGeneralDBOperator) tmp).getArg();
					}

					tmp = child;
					if(tmp instanceof GeneralDBLabelColumn)
					{
						String alias;
						if (((GeneralDBLabelColumn) tmp).getRdbmsVar()==null ||  ((GeneralDBLabelColumn) tmp).getRdbmsVar().isResource()) {
							//Predicates used in triple patterns non-existent in db
							alias="NULL";
						}
						else
						{
							//Reached the innermost left var -> need to capture its SRID
							alias = getLabelAlias(((GeneralDBLabelColumn) tmp).getRdbmsVar());
							alias=alias+".srid";
						}
						sridExpr = alias;
						break;
					}
					else if (tmp instanceof GeneralDBStringValue) // constant!!
					{
						sridNeeded  = false;
						sridExpr = String.valueOf(WKTHelper.getSRID(((GeneralDBStringValue) tmp).getValue()));
						break;
					}

				}
				if(sridNeeded)
				{
					filter.appendFunction(ST_TRANSFORM);
					filter.openBracket();
				}
			}

			if(expr.getLeftArg() instanceof GeneralDBStringValue)
			{
				appendWKT(expr.getLeftArg(),filter);
			}
			else if(expr.getLeftArg() instanceof GeneralDBSqlSpatialConstructBinary)
			{
				appendConstructFunction(expr.getLeftArg(), filter);
			}
			else if(expr.getLeftArg() instanceof GeneralDBSqlSpatialConstructUnary)
			{
				appendConstructFunction(expr.getLeftArg(), filter);
			}
			else if(expr.getLeftArg() instanceof GeneralDBSqlSpatialConstructTriple)
			{
				appendConstructFunction(expr.getLeftArg(), filter);
			}
			else if(expr.getLeftArg() instanceof GeneralDBSqlCase)
			{
				GeneralDBLabelColumn onlyLabel = (GeneralDBLabelColumn)((GeneralDBSqlCase)expr.getLeftArg()).getEntries().get(0).getResult();
				appendMBB(onlyLabel,filter); 
			}
			else
			{
				appendMBB((GeneralDBLabelColumn)(expr.getLeftArg()),filter);
			}

			//SRID Support
			if(sridNeeded)
			{	
				if(expr instanceof GeneralDBSqlSpatialConstructBinary && expr.getParentNode() == null && sridNeeded)
				{
					filter.appendComma();
					//filter.append(((GeneralDBSqlSpatialConstructBinary)expr).getSrid());
					filter.append(sridExpr);
					filter.closeBracket();
				}
			}

			filter.appendComma();

			if(expr.getRightArg() instanceof GeneralDBSqlCase) //case met in transform!
			{
				GeneralDBURIColumn plainURI = (GeneralDBURIColumn)((GeneralDBSqlCase)expr.getRightArg()).getEntries().get(0).getResult();

				//XXX This case would be met if we recovered the SRID URI from the db!!!
				//Need to set sridExpr to the value of this new URI, otherwise the appended uri
				//to the spatial object will be the wrong one!!!! (Seee following case)
				filter.keepSRID_part1();
				append(plainURI, filter);
				filter.keepSRID_part2();
				append(plainURI, filter);
				filter.keepSRID_part3();
			}
			else if(expr.getRightArg() instanceof GeneralDBStringValue)
			{ // the argument is the URI of a CRS
				String unparsedCRS = ((GeneralDBStringValue)expr.getRightArg()).getValue();
				sridExpr = String.valueOf(WKTHelper.getSRID_forURI(unparsedCRS));
				filter.append(sridExpr);
				filter.closeBracket();
			}


		}
		filter.closeBracket();
		//In this case, SRID is the one that has been provided by the user!! 
		//I am including this extra binding to be used in subsequent (Aggregate) steps
		if(expr instanceof GeneralDBSqlSpatialConstructBinary && expr.getParentNode() == null)
		{
			filter.appendComma();
			filter.append(sridExpr);
		}
	}

	/** Addition for datetime metric functions
	 * 
	 * @author George Garbis <ggarbis@di.uoa.gr>
	 * 
	 */
	protected void appendGeneralDBDateTimeFunctionBinary(BinaryGeneralDBOperator expr, GeneralDBSqlExprBuilder filter, DateTimeFunctionPostGIS func)
			throws UnsupportedRdbmsOperatorException
	{
		filter.openBracket();

		boolean check1 = expr.getLeftArg().getClass().getCanonicalName().equals("org.openrdf.sail.generaldb.algebra.GeneralDBSqlNull");
		boolean check2 = expr.getRightArg().getClass().getCanonicalName().equals("org.openrdf.sail.generaldb.algebra.GeneralDBSqlNull");

		if(check1)
		{
			this.append((GeneralDBSqlNull)expr.getLeftArg(), filter);

		}
		else if(check2)
		{
			this.append((GeneralDBSqlNull)expr.getRightArg(), filter);
		}
		else
		{

			GeneralDBSqlExpr tmp = expr;
			if(tmp instanceof GeneralDBSqlSpatialConstructBinary && tmp.getParentNode() == null)
			{
				while(true)
				{
					GeneralDBSqlExpr child;

					if(tmp instanceof BinaryGeneralDBOperator)
					{
						child = ((BinaryGeneralDBOperator) tmp).getLeftArg();
					}
					else //(tmp instanceof UnaryGeneralDBOperator)
					{
						child = ((UnaryGeneralDBOperator) tmp).getArg();
					}

					tmp = child;
					if(tmp instanceof GeneralDBLabelColumn)
					{
						//Reached the innermost left var -> need to capture its SRID
						String alias;
						if (((GeneralDBLabelColumn) tmp).getRdbmsVar()==null || ((GeneralDBLabelColumn) tmp).getRdbmsVar().isResource()) {
							//Predicates used in triple patterns non-existent in db
							alias="NULL";
						}
						else
						{
							//Reached the innermost left var -> need to capture its SRID
							alias = getLabelAlias(((GeneralDBLabelColumn) tmp).getRdbmsVar());
						}
						break;
					}
				}
			}

			filter.openBracket();

			if(expr.getLeftArg() instanceof GeneralDBStringValue)
			{
				GeneralDBStringValue arg = (GeneralDBStringValue) expr.getLeftArg();
				String raw = arg.getValue();
				filter.append(" "+raw+" ");
			}
			else if(expr.getLeftArg() instanceof GeneralDBNumberValue)
			{
				append(((GeneralDBNumberValue)expr.getLeftArg()), filter);
			}
			else if(expr.getLeftArg() instanceof GeneralDBDateTimeColumn)
			{
				append(((GeneralDBDateTimeColumn)expr.getLeftArg()),filter);
			}
			else
			{
				// Den prepei na ftasei edw
			}
						
			switch(func)
			{
				case Difference: filter.append(" - "); break;			
			}
			
			if(expr.getRightArg() instanceof GeneralDBStringValue)
			{
				GeneralDBStringValue arg = (GeneralDBStringValue) expr.getRightArg();
				String raw = arg.getValue();
				filter.append(" "+raw+" ");
			}
			else if(expr.getRightArg() instanceof GeneralDBNumberValue)
			{
				append(((GeneralDBNumberValue)expr.getRightArg()), filter);
			}
			else if(expr.getRightArg() instanceof GeneralDBDateTimeColumn)
			{
				append(((GeneralDBDateTimeColumn)expr.getRightArg()),filter);	
			}
			else
			{
				// Den prepei na ftasei edw
			}


			filter.closeBracket();
		}
		filter.closeBracket();
	}	
	/***/

	//Used in all the generaldb stsparql (and geosparql) boolean spatial functions of the form ST_Function(?GEO1,?GEO2) 
	//EXCEPT ST_Transform!!!
	protected void appendGeneralDBSpatialFunctionBinary(BinaryGeneralDBOperator expr, GeneralDBSqlExprBuilder filter, SpatialFunctionsPostGIS func) throws UnsupportedRdbmsOperatorException
	{
		//In the case where no variable is present in the expression! e.g ConvexHull("POLYGON((.....))")
		boolean sridNeeded = true;
		//XXX Incorporating SRID
		String sridExpr = null;

		filter.openBracket();
		
		boolean check1 = expr.getLeftArg().getClass().getCanonicalName().equals("org.openrdf.sail.generaldb.algebra.GeneralDBSqlNull");
		boolean check2 = expr.getRightArg().getClass().getCanonicalName().equals("org.openrdf.sail.generaldb.algebra.GeneralDBSqlNull");

		if(check1)
		{
			this.append((GeneralDBSqlNull)expr.getLeftArg(), filter);

		}
		else if(check2)
		{
			this.append((GeneralDBSqlNull)expr.getRightArg(), filter);
		}
		else
		{

			GeneralDBSqlExpr tmp = expr;
			if(tmp instanceof GeneralDBSqlSpatialConstructBinary && tmp.getParentNode() == null)
			{
				while(true)
				{
					GeneralDBSqlExpr child;

					if(tmp instanceof BinaryGeneralDBOperator)
					{
						child = ((BinaryGeneralDBOperator) tmp).getLeftArg();
					}
					else if(tmp instanceof TripleGeneralDBOperator)
					{
						child = ((TripleGeneralDBOperator) tmp).getLeftArg();
					}

					else //(tmp instanceof UnaryGeneralDBOperator)
					{
						child = ((UnaryGeneralDBOperator) tmp).getArg();
					}

					tmp = child;
					if(tmp instanceof GeneralDBLabelColumn)
					{
						//Reached the innermost left var -> need to capture its SRID
						String alias;
						if (((GeneralDBLabelColumn) tmp).getRdbmsVar()==null || ((GeneralDBLabelColumn) tmp).getRdbmsVar().isResource()) {
							//Predicates used in triple patterns non-existent in db
							alias="NULL";
						}
						else
						{
							//Reached the innermost left var -> need to capture its SRID
							alias = getLabelAlias(((GeneralDBLabelColumn) tmp).getRdbmsVar());
							alias=alias+".srid";
						}
						sridExpr = alias;
						break;
					}
					else if (tmp instanceof GeneralDBStringValue) //Constant!!
					{
						sridNeeded  = false;
						sridExpr = String.valueOf(WKTHelper.getSRID(((GeneralDBStringValue) tmp).getValue()));
						break;
					}

				}
				if(sridNeeded)
				{
					filter.appendFunction(ST_TRANSFORM);
					filter.openBracket();
				}
			}
			/////

			//case where both arguments are constnats
			boolean constantArgs = false;	

			switch(func)
			{
			//XXX Careful: ST_Transform support MISSING!!!
			case ST_Difference: filter.appendFunction("ST_Difference"); break;
			case ST_Intersection: filter.appendFunction("ST_Intersection"); break;
			case ST_Union: filter.appendFunction("ST_Union"); break;
			case ST_SymDifference: filter.appendFunction("ST_SymDifference"); break;
			
			// PostGIS
			case ST_MakeLine: filter.appendFunction("ST_MakeLine"); break;
			
			case ST_Equals: filter.appendFunction("ST_Equals"); break;
			case ST_Disjoint: filter.appendFunction("ST_Disjoint"); break;
			case ST_Intersects: filter.appendFunction("ST_Intersects"); break;
			case ST_Touches: filter.appendFunction("ST_Touches"); break;
			case ST_Crosses: filter.appendFunction("ST_Crosses"); break;
			case ST_Within: filter.appendFunction("ST_Within"); break;
			case ST_Contains: filter.appendFunction("ST_Contains"); break;
			case ST_Overlaps: filter.appendFunction("ST_Overlaps"); break;
			}

			filter.openBracket();
			if(expr.getLeftArg() instanceof GeneralDBStringValue)
			{
				if(expr.getRightArg() instanceof GeneralDBStringValue)
				{	
					//both arguments are constants so we do not need
					//to transform the geometries to WGS84
					constantArgs = true;
					appendWKT(expr.getLeftArg(), filter);
				}
				else
					appendConstantWKT(expr.getLeftArg(), filter);
			}
			else if(expr.getLeftArg() instanceof GeneralDBSqlSpatialConstructBinary)
			{
				appendConstructFunction(expr.getLeftArg(), filter);
			}
			else if(expr.getLeftArg() instanceof GeneralDBSqlSpatialConstructUnary)
			{
				appendConstructFunction(expr.getLeftArg(), filter);
			}
			else if(expr.getLeftArg() instanceof GeneralDBSqlSpatialConstructTriple)
			{
				appendConstructFunction(expr.getLeftArg(), filter);
			}
			else if(expr.getLeftArg() instanceof GeneralDBSqlCase)
			{
				GeneralDBLabelColumn onlyLabel = (GeneralDBLabelColumn)((GeneralDBSqlCase)expr.getLeftArg()).getEntries().get(0).getResult();
				appendMBB(onlyLabel,filter); 
			}
			else
			{
				appendMBB((GeneralDBLabelColumn)(expr.getLeftArg()),filter);
			}
			filter.appendComma();

			if(expr.getRightArg() instanceof GeneralDBStringValue)
			{
				if(constantArgs == true)
					// both arguments are constants, so we do not need
					// to transform the geometries to WGS84
					appendWKT(expr.getRightArg(), filter);
				else
					appendConstantWKT(expr.getRightArg(),filter);
			}
			else if(expr.getRightArg() instanceof GeneralDBSqlSpatialConstructUnary)
			{
				appendConstructFunction(expr.getRightArg(), filter);
			}
			else if(expr.getRightArg() instanceof GeneralDBSqlSpatialConstructBinary)
			{
				appendConstructFunction(expr.getRightArg(), filter);
			}
			else if(expr.getRightArg() instanceof GeneralDBSqlSpatialConstructTriple)
			{
				appendConstructFunction(expr.getRightArg(), filter);
			}
			else if(expr.getRightArg() instanceof GeneralDBSqlCase)
			{
				GeneralDBLabelColumn onlyLabel = (GeneralDBLabelColumn)((GeneralDBSqlCase)expr.getRightArg()).getEntries().get(0).getResult();
				appendMBB(onlyLabel,filter);					 
			}
			else if(expr.getRightArg() instanceof GeneralDBDoubleValue) //case met in buffer!
			{
				append(((GeneralDBDoubleValue)expr.getRightArg()), filter);
			}
			else if(expr.getRightArg() instanceof GeneralDBNumericColumn) //case met in buffer!
			{
				append(((GeneralDBNumericColumn)expr.getRightArg()), filter);
			}
			else if(expr.getRightArg() instanceof GeneralDBURIColumn) //case met in transform!
			{
				filter.keepSRID_part1();
				append(((GeneralDBURIColumn)expr.getRightArg()), filter);
				filter.keepSRID_part2();
				append(((GeneralDBURIColumn)expr.getRightArg()), filter);
				filter.keepSRID_part3();
			}
			//case met in buffer when in select -> buffer(?spatial,?thematic)
			else if(expr.getRightArg() instanceof GeneralDBLabelColumn && !((GeneralDBLabelColumn)expr.getRightArg()).isSpatial())
			{
				append(((GeneralDBLabelColumn)expr.getRightArg()),filter);
				appendCastToDouble(filter);
			}
			else if(expr.getRightArg() instanceof GeneralDBSqlSpatialMetricBinary)
			{
				appendMetricFunction(expr.getRightArg(), filter);
			}
			else if(expr.getRightArg() instanceof GeneralDBSqlSpatialMetricUnary)
			{
				appendMetricFunction(expr.getRightArg(), filter);
			}
			else if(expr.getRightArg() instanceof GeneralDBSqlSpatialMetricTriple)
			{
				appendMetricFunction(expr.getRightArg(), filter);
			}
			else
			{
				appendMBB((GeneralDBLabelColumn)(expr.getRightArg()),filter);
			}

			filter.closeBracket();
			//SRID Support
			if(sridNeeded)
			{	
				if(expr instanceof GeneralDBSqlSpatialConstructBinary && expr.getParentNode() == null)
				{
					filter.appendComma();
					//filter.append(((GeneralDBSqlSpatialConstructBinary)expr).getSrid());
					filter.append(sridExpr);
					filter.closeBracket();
				}
			}
			///
		}
		filter.closeBracket();
		//Used to explicitly include SRID
		if(expr instanceof GeneralDBSqlSpatialConstructBinary && expr.getParentNode() == null)
		{
			filter.appendComma();
			filter.append(sridExpr);
		}
	}

	//Distance function
	protected void appendDistance(TripleGeneralDBOperator expr, GeneralDBSqlExprBuilder filter, SpatialFunctionsPostGIS func) throws UnsupportedRdbmsOperatorException
	{
		String units = null;
		
		filter.openBracket();

		boolean check1 = expr.getLeftArg().getClass().getCanonicalName().equals("org.openrdf.sail.generaldb.algebra.GeneralDBSqlNull");
		boolean check2 = expr.getRightArg().getClass().getCanonicalName().equals("org.openrdf.sail.generaldb.algebra.GeneralDBSqlNull");
		boolean check3 = expr.getThirdArg().getClass().getCanonicalName().equals("org.openrdf.sail.generaldb.algebra.GeneralDBSqlNull");

		if(check1)
		{
			this.append((GeneralDBSqlNull)expr.getLeftArg(), filter);

		}
		else if(check2)
		{
			this.append((GeneralDBSqlNull)expr.getRightArg(), filter);
		}
		else if(check3)
		{
			this.append((GeneralDBSqlNull)expr.getThirdArg(), filter);
		}
		else
		{
			filter.appendFunction("ST_Distance");
			filter.openBracket();
	
			if (expr.getThirdArg() instanceof GeneralDBStringValue)
			{
				units = ((GeneralDBStringValue)expr.getThirdArg()).getValue();
				if(!OGCConstants.supportedUnitsOfMeasure.contains(units))
				{
					throw new UnsupportedRdbmsOperatorException("No such unit of measure exists");
				}	

				if(units.equals(OGCConstants.OGCmetre))
				{
					filter.appendFunction(GEOGRAPHY);
					filter.openBracket();
					filter.appendFunction(ST_TRANSFORM);
					filter.openBracket();
				}
				else if(units.equals(OGCConstants.OGCdegree))
				{
					filter.appendFunction(ST_TRANSFORM);
					filter.openBracket();
				}	
			}	
			
			if(expr.getLeftArg() instanceof GeneralDBStringValue)
			{
				appendWKT(expr.getLeftArg(),filter);
			}
			else if(expr.getLeftArg() instanceof GeneralDBSqlSpatialConstructBinary)
			{
				appendConstructFunction(expr.getLeftArg(), filter);
			}
			else if(expr.getLeftArg() instanceof GeneralDBSqlSpatialConstructUnary)
			{
				appendConstructFunction(expr.getLeftArg(), filter);
			}
			else if(expr.getLeftArg() instanceof GeneralDBSqlSpatialConstructTriple)
			{
				appendConstructFunction(expr.getLeftArg(), filter);
			}			
			else if(expr.getLeftArg() instanceof GeneralDBSqlCase)
			{
				GeneralDBLabelColumn onlyLabel = (GeneralDBLabelColumn)((GeneralDBSqlCase)expr.getLeftArg()).getEntries().get(0).getResult();
				appendMBB(onlyLabel,filter);
			}
			else
			{
				appendMBB((GeneralDBLabelColumn)(expr.getLeftArg()),filter);
			}
						
			if(units.equals(OGCConstants.OGCmetre))
			{				
				filter.appendComma();
				filter.append(String.valueOf(GeoConstants.defaultSRID));
				filter.closeBracket(); //close st_transform
				filter.closeBracket(); //close geography
				
				filter.appendComma();

				filter.appendFunction(GEOGRAPHY);
				filter.openBracket();
				filter.appendFunction(ST_TRANSFORM);
				filter.openBracket();
			}
			else if(units.equals(OGCConstants.OGCdegree))
			{
				filter.appendComma();
				filter.append(String.valueOf(GeoConstants.defaultSRID));
				filter.closeBracket(); //close st_transform
				
				filter.appendComma();
				
				filter.appendFunction(ST_TRANSFORM);
				filter.openBracket();
			}	
			else
			{
				filter.appendComma();
			}	
			
			if(expr.getRightArg() instanceof GeneralDBStringValue)
			{
				appendWKT(expr.getRightArg(),filter);
			}
			else if(expr.getRightArg() instanceof GeneralDBSqlSpatialConstructUnary)
			{
				appendConstructFunction(expr.getRightArg(), filter);
			}
			else if(expr.getRightArg() instanceof GeneralDBSqlSpatialConstructBinary)
			{
				appendConstructFunction(expr.getRightArg(), filter);
			}
			else if(expr.getLeftArg() instanceof GeneralDBSqlSpatialConstructTriple)
			{
				appendConstructFunction(expr.getLeftArg(), filter);
			}
			else if(expr.getRightArg() instanceof GeneralDBSqlCase)
			{
				GeneralDBLabelColumn onlyLabel = (GeneralDBLabelColumn)((GeneralDBSqlCase)expr.getRightArg()).getEntries().get(0).getResult();
				appendMBB(onlyLabel,filter);
			}						
			else if(expr.getRightArg() instanceof GeneralDBURIColumn) //case met in transform!
			{
				filter.keepSRID_part1();
				append(((GeneralDBURIColumn)expr.getRightArg()), filter);
				filter.keepSRID_part2();
				append(((GeneralDBURIColumn)expr.getRightArg()), filter);
				filter.keepSRID_part3();
			}
			//case met in buffer when in select -> buffer(?spatial,?thematic)
			else if(expr.getRightArg() instanceof GeneralDBLabelColumn && !((GeneralDBLabelColumn)expr.getRightArg()).isSpatial())
			{
				append(((GeneralDBLabelColumn)expr.getRightArg()),filter);
				appendCastToDouble(filter);
			}
			else if(expr.getRightArg() instanceof GeneralDBSqlSpatialMetricBinary)
			{
				appendMetricFunction(expr.getRightArg(), filter);
			}
			else if(expr.getRightArg() instanceof GeneralDBSqlSpatialMetricUnary)
			{
				appendMetricFunction(expr.getRightArg(), filter);
			}
			else if(expr.getRightArg() instanceof GeneralDBSqlSpatialMetricTriple)
			{
				appendMetricFunction(expr.getRightArg(), filter);
			}
			else
			{
				appendMBB((GeneralDBLabelColumn)(expr.getRightArg()),filter);
			}

			if(units.equals(OGCConstants.OGCmetre))
			{
				filter.appendComma();
				filter.append(String.valueOf(GeoConstants.defaultSRID));
				filter.closeBracket();
				filter.closeBracket();
			}
			else if(units.equals(OGCConstants.OGCdegree))
			{
				filter.appendComma();
				filter.append(String.valueOf(GeoConstants.defaultSRID));
				filter.closeBracket();
			}	

			filter.closeBracket();
		}
		filter.closeBracket();
	}

	//Buffer function (Giannis: dont get deceived by "filter". It also applies for the case the buffer occurs in the select clause too)
	protected void appendBuffer(TripleGeneralDBOperator expr, GeneralDBSqlExprBuilder filter, SpatialFunctionsPostGIS func) throws UnsupportedRdbmsOperatorException
	{
		boolean sridNeeded = true;
		//XXX Incorporating SRID
		String sridExpr = null;
		String units = null;

		filter.openBracket();

		boolean check1 = expr.getLeftArg().getClass().getCanonicalName().equals("org.openrdf.sail.generaldb.algebra.GeneralDBSqlNull");
		boolean check2 = expr.getRightArg().getClass().getCanonicalName().equals("org.openrdf.sail.generaldb.algebra.GeneralDBSqlNull");
		boolean check3 = expr.getRightArg().getClass().getCanonicalName().equals("org.openrdf.sail.generaldb.algebra.GeneralDBSqlNull");

		if(check1)
		{
			this.append((GeneralDBSqlNull)expr.getLeftArg(), filter);

		}
		else if(check2)
		{
			this.append((GeneralDBSqlNull)expr.getRightArg(), filter);
		}
		else if(check3)
		{
			this.append((GeneralDBSqlNull)expr.getRightArg(), filter);
		}
		else
		{
			GeneralDBSqlExpr tmp = expr;
			if(tmp instanceof GeneralDBSqlSpatialConstructTriple && tmp.getParentNode() == null)
			{
				while(true)
				{
					GeneralDBSqlExpr child;

					if(tmp instanceof TripleGeneralDBOperator)
					{
						child = ((TripleGeneralDBOperator) tmp).getLeftArg();
					}
					else if(tmp instanceof BinaryGeneralDBOperator)
					{
						child = ((BinaryGeneralDBOperator) tmp).getLeftArg();
					}
					else //(tmp instanceof UnaryGeneralDBOperator)
					{
						child = ((UnaryGeneralDBOperator) tmp).getArg();
					}

					tmp = child;
					if(tmp instanceof GeneralDBLabelColumn)
					{
						//Reached the innermost left var -> need to capture its SRID
						String alias;
						if (((GeneralDBLabelColumn) tmp).getRdbmsVar()==null || ((GeneralDBLabelColumn) tmp).getRdbmsVar().isResource()) {
							//Predicates used in triple patterns non-existent in db
							alias="NULL";
						}
						else
						{
							//Reached the innermost left var -> need to capture its SRID
							alias = getLabelAlias(((GeneralDBLabelColumn) tmp).getRdbmsVar());
							alias=alias+".srid";
						}
						sridExpr = alias;
						break;
					}
					else if (tmp instanceof GeneralDBStringValue) //Constant!!
					{
						sridNeeded  = true;
						sridExpr = String.valueOf(WKTHelper.getSRID(((GeneralDBStringValue) tmp).getValue()));
						break;
					}
				}
				if(sridNeeded)
				{
					filter.appendFunction(ST_TRANSFORM);
					filter.openBracket();
				}
			}			

			if (expr.getThirdArg() instanceof GeneralDBStringValue)
			{
				units = ((GeneralDBStringValue)expr.getThirdArg()).getValue();

				if(!OGCConstants.supportedUnitsOfMeasure.contains(units))
				{
					throw new UnsupportedRdbmsOperatorException("No such unit of measure exists");
				}													
					
				if(units.equals(OGCConstants.OGCmetre))
				{
					if((expr.getRightArg() instanceof GeneralDBDoubleValue) && (((GeneralDBDoubleValue)expr.getRightArg()).getValue().equals(0.0)))
					{
						filter.appendFunction("ST_Buffer");
						filter.openBracket();
						filter.appendFunction(ST_TRANSFORM);
						filter.openBracket();		
					}
					else
					{	
						filter.appendFunction(GEOMETRY);
						filter.openBracket();
						filter.appendFunction("ST_Buffer");
						filter.openBracket();
						filter.appendFunction(GEOGRAPHY);
						filter.openBracket();
						filter.appendFunction(ST_TRANSFORM);
						filter.openBracket();
					}
				}
				else if(units.equals(OGCConstants.OGCdegree))
				{
					filter.appendFunction("ST_Buffer");
					filter.openBracket();
					filter.appendFunction(ST_TRANSFORM);
					filter.openBracket();
				}
			}

			if(expr.getLeftArg() instanceof GeneralDBStringValue)
			{
				appendWKT(expr.getLeftArg(),filter);
			}
			else if(expr.getLeftArg() instanceof GeneralDBSqlSpatialConstructBinary)
			{
				appendConstructFunction(expr.getLeftArg(), filter);
			}
			else if(expr.getLeftArg() instanceof GeneralDBSqlSpatialConstructUnary)
			{
				appendConstructFunction(expr.getLeftArg(), filter);
			}
			else if(expr.getLeftArg() instanceof GeneralDBSqlSpatialConstructTriple)
			{
				appendConstructFunction(expr.getLeftArg(), filter);
			}
			else if(expr.getLeftArg() instanceof GeneralDBSqlCase)
			{
				GeneralDBLabelColumn onlyLabel = (GeneralDBLabelColumn)((GeneralDBSqlCase)expr.getLeftArg()).getEntries().get(0).getResult();
				appendMBB(onlyLabel,filter);
			}
			else
			{
				appendMBB((GeneralDBLabelColumn)(expr.getLeftArg()),filter);
			}

			if(units.equals(OGCConstants.OGCmetre))
			{
				if((expr.getRightArg() instanceof GeneralDBDoubleValue) && (((GeneralDBDoubleValue)expr.getRightArg()).getValue().equals(0.0)))
				{
					filter.appendComma();
					filter.append(String.valueOf(GeoConstants.defaultSRID));
					filter.closeBracket(); //close st_transform
					filter.appendComma();
				}	
				else
				{	
					filter.appendComma();
					filter.append(String.valueOf(GeoConstants.defaultSRID));
					filter.closeBracket(); //close st_transform
					filter.closeBracket(); //close geography
					filter.appendComma();
				}
			}
			else if(units.equals(OGCConstants.OGCdegree))
			{
				filter.appendComma();
				filter.append(String.valueOf(GeoConstants.defaultSRID));
				filter.closeBracket(); //close st_transform
				filter.appendComma();
			}

			if(expr.getRightArg() instanceof GeneralDBStringValue)
			{
				appendWKT(expr.getRightArg(),filter);
			}
			else if(expr.getRightArg() instanceof GeneralDBSqlSpatialConstructUnary)
			{
				appendConstructFunction(expr.getRightArg(), filter);
			}
			else if(expr.getRightArg() instanceof GeneralDBSqlSpatialConstructBinary)
			{
				appendConstructFunction(expr.getRightArg(), filter);
			}
			else if(expr.getRightArg() instanceof GeneralDBSqlSpatialConstructTriple)
			{
				appendConstructFunction(expr.getRightArg(), filter);
			}
			else if(expr.getRightArg() instanceof GeneralDBSqlCase)
			{
				GeneralDBLabelColumn onlyLabel = (GeneralDBLabelColumn)((GeneralDBSqlCase)expr.getRightArg()).getEntries().get(0).getResult();
				appendMBB(onlyLabel,filter);
			}
			else if(expr.getRightArg() instanceof GeneralDBDoubleValue)
			{
				append(((GeneralDBDoubleValue)expr.getRightArg()), filter);
			}
			else if(expr.getRightArg() instanceof GeneralDBNumericColumn)
			{
				append(((GeneralDBNumericColumn)expr.getRightArg()), filter);
			}
			//case met in buffer when in select -> buffer(?spatial,?thematic)
			else if(expr.getRightArg() instanceof GeneralDBLabelColumn && !((GeneralDBLabelColumn)expr.getRightArg()).isSpatial())
			{
				append(((GeneralDBLabelColumn)expr.getRightArg()),filter);
				appendCastToDouble(filter);
			}
			else if(expr.getRightArg() instanceof GeneralDBSqlSpatialMetricBinary)
			{
				appendMetricFunction(expr.getRightArg(), filter);
			}
			else if(expr.getRightArg() instanceof GeneralDBSqlSpatialMetricUnary)
			{
				appendMetricFunction(expr.getRightArg(), filter);
			}
			else if(expr.getRightArg() instanceof GeneralDBSqlSpatialMetricTriple)
			{
				appendMetricFunction(expr.getRightArg(), filter);
			}
			else
			{
				appendMBB((GeneralDBLabelColumn)(expr.getRightArg()),filter);
			}
			
			if(units.equals(OGCConstants.OGCmetre) && !((expr.getRightArg() instanceof GeneralDBDoubleValue) && (((GeneralDBDoubleValue)expr.getRightArg()).getValue().equals(0.0))))
			filter.closeBracket(); //close Geometry
			filter.closeBracket();
			//SRID Support
			if(sridNeeded)
			{	
				if(expr instanceof GeneralDBSqlSpatialConstructTriple && expr.getParentNode() == null)
				{
					filter.appendComma();
					filter.append(sridExpr);
					filter.closeBracket();
				}
			}
			///
		}
		filter.closeBracket();
		//Used to explicitly include SRID
		if(expr instanceof GeneralDBSqlSpatialConstructTriple && expr.getParentNode() == null)
		{
			filter.appendComma();
			filter.append(sridExpr);
		}
	}

	//Used in all the generaldb boolean spatial functions of the form ST_Function(?GEO1) 
	protected void appendGeneralDBSpatialFunctionUnary(UnaryGeneralDBOperator expr, GeneralDBSqlExprBuilder filter, SpatialFunctionsPostGIS func) throws UnsupportedRdbmsOperatorException
	{
		//In the case where no variable is present in the expression! e.g ConvexHull("POLYGON((.....))")
		boolean sridNeeded = true;
		String sridExpr = null;

		filter.openBracket();

		boolean check1 = expr.getArg().getClass().getCanonicalName().equals("org.openrdf.sail.generaldb.algebra.GeneralDBSqlNull");
		boolean check2 = false;
		if(expr.getArg() instanceof GeneralDBLabelColumn)
		{
			if(((GeneralDBLabelColumn) expr.getArg()).getRdbmsVar() ==null || ((GeneralDBLabelColumn) expr.getArg()).getRdbmsVar().isResource())
			{
				check2 = true;
			}
		}
		if(check1)
		{
			this.append((GeneralDBSqlNull)expr.getArg(), filter);

		}
		else if (check2)
		{
			appendMBB((GeneralDBLabelColumn)(expr.getArg()),filter);
		}
		else
		{

			GeneralDBSqlExpr tmp = expr;


			if(tmp instanceof GeneralDBSqlSpatialConstructUnary && tmp.getParentNode() == null)
			{
				while(true)
				{
					GeneralDBSqlExpr child = null;

					if(tmp instanceof BinaryGeneralDBOperator)
					{
						child = ((BinaryGeneralDBOperator) tmp).getLeftArg();
					}
					else if(tmp instanceof UnaryGeneralDBOperator)
					{
						child = ((UnaryGeneralDBOperator) tmp).getArg();
					}
					else if(tmp instanceof GeneralDBStringValue)
					{
						sridNeeded  = false;
						break;
					}
					else if (tmp instanceof GeneralDBSqlSpatialConstructTriple) {
						//here we consider the case where the current argument is a Spatial Construct Ternary function, to dodge the infinite loop
						child = ((GeneralDBSqlSpatialConstructTriple) tmp).getLeftArg();
					}

					tmp = child;
					if(tmp instanceof GeneralDBLabelColumn)
					{
						//Reached the innermost left var -> need to capture its SRID
						String alias;
						if (((GeneralDBLabelColumn) tmp).getRdbmsVar() == null || ((GeneralDBLabelColumn) tmp).getRdbmsVar().isResource()) {
							//Predicates used in triple patterns non-existent in db
							alias="NULL";
						}
						else
						{
							//Reached the innermost left var -> need to capture its SRID
							alias = getLabelAlias(((GeneralDBLabelColumn) tmp).getRdbmsVar());
							alias=alias+".srid";
						}
						sridExpr = alias;
						break;
					}
					else if (tmp instanceof GeneralDBStringValue) //Constant!!
					{
						sridNeeded  = false;
						sridExpr = String.valueOf(WKTHelper.getSRID(((GeneralDBStringValue) tmp).getValue()));
						break;
					}

				}
				if(sridNeeded)
				{
					filter.appendFunction(ST_TRANSFORM);
					filter.openBracket();
				}
			}
			/////

			switch(func)
			{
			case ST_Envelope: filter.appendFunction("ST_Envelope"); break;
			case ST_ConvexHull: filter.appendFunction("ST_ConvexHull"); break;
			case ST_Boundary: filter.appendFunction("ST_Boundary"); break;
			case ST_Area: filter.appendFunction("ST_Area"); break;
			case ST_Dimension: filter.appendFunction("ST_Dimension"); break;
			case ST_GeometryType: filter.appendFunction("ST_GeometryType"); break;
			case ST_AsText: filter.appendFunction("ST_AsText"); break;
			case ST_AsGML: filter.appendFunction("ST_AsGML"); break;
			case ST_SRID: filter.appendFunction("ST_SRID"); break;
			case ST_IsEmpty: filter.appendFunction("ST_IsEmpty"); break;
			case ST_IsSimple: filter.appendFunction("ST_IsSimple"); break;
			case ST_Centroid: filter.appendFunction("ST_Centroid"); break;
			}
			
			filter.openBracket();
			if(expr.getArg() instanceof GeneralDBStringValue)
			{
				appendWKT(expr.getArg(), filter);
			}
			else if(expr.getArg() instanceof GeneralDBSqlSpatialConstructBinary)
			{
				appendConstructFunction(expr.getArg(), filter);
			}
			else if(expr.getArg() instanceof GeneralDBSqlSpatialConstructUnary)
			{
				appendConstructFunction(expr.getArg(), filter);
			}
			else if(expr.getArg() instanceof GeneralDBSqlSpatialConstructTriple)
			{
				appendConstructFunction(expr.getArg(), filter);
			}
			else if(expr.getArg() instanceof GeneralDBSqlCase)
			{
				GeneralDBLabelColumn onlyLabel = (GeneralDBLabelColumn)((GeneralDBSqlCase)expr.getArg()).getEntries().get(0).getResult();
				appendMBB(onlyLabel, filter); 
			}
			else
			{
				appendMBB((GeneralDBLabelColumn)(expr.getArg()),filter);
			}

			filter.closeBracket();
			//			//SRID Support
			if(sridNeeded)
			{
				if(expr instanceof GeneralDBSqlSpatialConstructUnary && expr.getParentNode() == null)
				{
					filter.appendComma();
					//				filter.append(((GeneralDBSqlSpatialConstructUnary)expr).getSrid());
					filter.append(sridExpr);
					filter.closeBracket();
				}
			}
			///
		}

		filter.closeBracket();
		//Used to explicitly include SRID
		
		if(expr instanceof GeneralDBSqlSpatialConstructUnary && expr.getParentNode() == null)
		{
			filter.appendComma();
			filter.append(sridExpr);
		}
		
	}

	//Used in all the generaldb boolean spatial functions of the form ST_Function(?GEO1,?GEO2) 
	protected void appendGeneralDBSpatialFunctionTriple(TripleGeneralDBOperator expr, GeneralDBSqlExprBuilder filter, SpatialFunctionsPostGIS func) throws UnsupportedRdbmsOperatorException
	{
		filter.openBracket();

		boolean check1a = expr.getLeftArg().getClass().getCanonicalName().equals("org.openrdf.sail.generaldb.algebra.GeneralDBSqlNull");
		boolean check2a = expr.getRightArg().getClass().getCanonicalName().equals("org.openrdf.sail.generaldb.algebra.GeneralDBSqlNull");
		boolean check3 = expr.getRightArg().getClass().getCanonicalName().equals("org.openrdf.sail.generaldb.algebra.GeneralDBSqlNull");

		if(check1a)
		{
			this.append((GeneralDBSqlNull)expr.getLeftArg(), filter);

		}
		else if(check2a)
		{
			this.append((GeneralDBSqlNull)expr.getRightArg(), filter);
		}
		else if(check3)
		{
			this.append((GeneralDBSqlNull)expr.getThirdArg(), filter);
		}
		else
		{
			switch(func)
			{
			case ST_Relate: filter.appendFunction("ST_Relate"); break;
			}
			filter.openBracket();
			if(expr.getLeftArg() instanceof GeneralDBStringValue)
			{
				appendWKT(expr.getLeftArg(),filter);
			}
			else if(expr.getLeftArg() instanceof GeneralDBSqlSpatialConstructBinary)
			{
				appendConstructFunction(expr.getLeftArg(), filter);
			}
			else if(expr.getLeftArg() instanceof GeneralDBSqlSpatialConstructUnary)
			{
				appendConstructFunction(expr.getLeftArg(), filter);
			}
			else if(expr.getLeftArg() instanceof GeneralDBSqlSpatialConstructTriple)
			{
				appendConstructFunction(expr.getLeftArg(), filter);
			}
			else if(expr.getLeftArg() instanceof GeneralDBSqlCase)
			{
				GeneralDBLabelColumn onlyLabel = (GeneralDBLabelColumn)((GeneralDBSqlCase)expr.getLeftArg()).getEntries().get(0).getResult();
				appendMBB(onlyLabel,filter); 
			}
			else
			{
				appendMBB((GeneralDBLabelColumn)(expr.getLeftArg()),filter);
			}
			filter.appendComma();
			//			boolean check2 = expr.getRightArg().getClass().getCanonicalName().equals("org.openrdf.sail.generaldb.algebra.GeneralDBSqlNull");
			//			if(check2)
			//			{
			//				this.append((GeneralDBSqlNull)expr.getRightArg(), filter);
			//			}
			//			else
			//			{
			if(expr.getRightArg() instanceof GeneralDBStringValue)
			{
				appendWKT(expr.getRightArg(),filter);
			}
			else if(expr.getRightArg() instanceof GeneralDBSqlSpatialConstructUnary)
			{
				appendConstructFunction(expr.getRightArg(), filter);
			}
			else if(expr.getRightArg() instanceof GeneralDBSqlSpatialConstructBinary)
			{
				appendConstructFunction(expr.getRightArg(), filter);
			}
			else if(expr.getLeftArg() instanceof GeneralDBSqlSpatialConstructTriple)
			{
				appendConstructFunction(expr.getLeftArg(), filter);
			}
			else if(expr.getRightArg() instanceof GeneralDBSqlCase)
			{
				GeneralDBLabelColumn onlyLabel = (GeneralDBLabelColumn)((GeneralDBSqlCase)expr.getRightArg()).getEntries().get(0).getResult();
				appendMBB(onlyLabel,filter);					 
			}
			else if(expr.getRightArg() instanceof GeneralDBDoubleValue) //case met in buffer!
			{
				append(((GeneralDBDoubleValue)expr.getRightArg()), filter);
			}
			else if(expr.getRightArg() instanceof GeneralDBNumericColumn) //case met in buffer!
			{
				append(((GeneralDBNumericColumn)expr.getRightArg()), filter);
			}
			//case met in buffer when in select -> buffer(?spatial,?thematic)
			else if(expr.getRightArg() instanceof GeneralDBLabelColumn && !((GeneralDBLabelColumn)expr.getRightArg()).isSpatial())
			{
				append(((GeneralDBLabelColumn)expr.getRightArg()),filter);
				appendCastToDouble(filter);
			}
			else if(expr.getRightArg() instanceof GeneralDBSqlSpatialMetricBinary)
			{
				appendMetricFunction(expr.getRightArg(), filter);
			}
			else if(expr.getRightArg() instanceof GeneralDBSqlSpatialMetricUnary)
			{
				appendMetricFunction(expr.getRightArg(), filter);
			}
			else if(expr.getRightArg() instanceof GeneralDBSqlSpatialMetricTriple)
			{
				appendMetricFunction(expr.getRightArg(), filter);
			}
			else
			{
				appendMBB((GeneralDBLabelColumn)(expr.getRightArg()),filter);
			}

			//			}
			//3rd arg
			filter.appendComma();
			//			boolean check3 = expr.getRightArg().getClass().getCanonicalName().equals("org.openrdf.sail.generaldb.algebra.GeneralDBSqlNull");
			//			if(check3)
			//			{
			//				this.append((GeneralDBSqlNull)expr.getThirdArg(), filter);
			//			}
			//			else
			//			{

			if(expr.getThirdArg() instanceof GeneralDBStringValue)
			{
				append(((GeneralDBStringValue)expr.getThirdArg()),filter);	
			}
			else if(expr.getThirdArg() instanceof GeneralDBSqlCase)
			{
				append(((GeneralDBSqlCase)expr.getThirdArg()),filter);				 
			}
			//case met in buffer when in select -> buffer(?spatial,?thematic)
			else if(expr.getThirdArg() instanceof GeneralDBLabelColumn )//&& !((GeneralDBLabelColumn)expr.getThirdArg()).isSpatial())
			{

				append(((GeneralDBLabelColumn)expr.getThirdArg()),filter);
			}


			//			}
			filter.closeBracket();
		}

		filter.closeBracket();
			}


	//GeoSPARQL
	//XXX
	protected void appendRelate(BinaryGeneralDBOperator expr, PostGISSqlExprBuilder filter, char[] intersectionPattern) throws UnsupportedRdbmsOperatorException
	{
		filter.openBracket();

		boolean check1 = expr.getLeftArg().getClass().getCanonicalName().equals("org.openrdf.sail.generaldb.algebra.GeneralDBSqlNull");
		boolean check2 = expr.getRightArg().getClass().getCanonicalName().equals("org.openrdf.sail.generaldb.algebra.GeneralDBSqlNull");

		if(check1)
		{
			this.append((GeneralDBSqlNull)expr.getLeftArg(), filter);

		}
		else if(check2)
		{
			this.append((GeneralDBSqlNull)expr.getRightArg(), filter);
		}
		else
		{	
			filter.appendFunction("ST_Relate");


			filter.openBracket();
			if(expr.getLeftArg() instanceof GeneralDBStringValue)
			{
				appendWKT(expr.getLeftArg(),filter);
			}
			else if(expr.getLeftArg() instanceof GeneralDBSqlSpatialConstructBinary)
			{
				appendConstructFunction(expr.getLeftArg(), filter);
			}
			else if(expr.getLeftArg() instanceof GeneralDBSqlSpatialConstructUnary)
			{
				appendConstructFunction(expr.getLeftArg(), filter);
			}
			else if(expr.getLeftArg() instanceof GeneralDBSqlSpatialConstructTriple)
			{
				appendConstructFunction(expr.getLeftArg(), filter);				
			}	
			else if(expr.getLeftArg() instanceof GeneralDBSqlCase)
			{
				GeneralDBLabelColumn onlyLabel = (GeneralDBLabelColumn)((GeneralDBSqlCase)expr.getLeftArg()).getEntries().get(0).getResult();
				appendMBB(onlyLabel,filter); 
			}
			else
			{
				appendMBB((GeneralDBLabelColumn)(expr.getLeftArg()),filter);
			}
			filter.appendComma();

			if(expr.getRightArg() instanceof GeneralDBStringValue)
			{
				appendWKT(expr.getRightArg(),filter);
			}
			else if(expr.getRightArg() instanceof GeneralDBSqlSpatialConstructUnary)
			{
				appendConstructFunction(expr.getRightArg(), filter);
			}
			else if(expr.getRightArg() instanceof GeneralDBSqlSpatialConstructBinary)
			{
				appendConstructFunction(expr.getRightArg(), filter);
			}
			else if(expr.getRightArg() instanceof GeneralDBSqlSpatialConstructTriple)
			{
				appendConstructFunction(expr.getRightArg(), filter);				
			}
			else if(expr.getRightArg() instanceof GeneralDBSqlCase)
			{
				GeneralDBLabelColumn onlyLabel = (GeneralDBLabelColumn)((GeneralDBSqlCase)expr.getRightArg()).getEntries().get(0).getResult();
				appendMBB(onlyLabel,filter);					 
			}
			else if(expr.getRightArg() instanceof GeneralDBDoubleValue) //case met in buffer!
			{
				append(((GeneralDBDoubleValue)expr.getRightArg()), filter);
			}
			else if(expr.getRightArg() instanceof GeneralDBNumericColumn) //case met in buffer!
			{
				append(((GeneralDBNumericColumn)expr.getRightArg()), filter);
			}
			//case met in buffer when in select -> buffer(?spatial,?thematic)
			else if(expr.getRightArg() instanceof GeneralDBLabelColumn && !((GeneralDBLabelColumn)expr.getRightArg()).isSpatial())
			{
				append(((GeneralDBLabelColumn)expr.getRightArg()),filter);
				appendCastToDouble(filter);
			}
			else if(expr.getRightArg() instanceof GeneralDBSqlSpatialMetricBinary)
			{
				appendMetricFunction(expr.getRightArg(), filter);
			}
			else if(expr.getRightArg() instanceof GeneralDBSqlSpatialMetricUnary)
			{
				appendMetricFunction(expr.getRightArg(), filter);
			}
			else if(expr.getRightArg() instanceof GeneralDBSqlSpatialMetricTriple)
			{
				appendMetricFunction(expr.getRightArg(), filter);
			}
			else
			{
				appendMBB((GeneralDBLabelColumn)(expr.getRightArg()),filter);
			}

			//3rd arg
			filter.appendComma();

			//must turn the table of characters I have to a valid sql value!
			filter.append("'");
			for(int i = 0; i< intersectionPattern.length; i++)
			{
				filter.append(intersectionPattern[i]+"");
			}
			filter.append("'");

			filter.closeBracket();

		}

		filter.closeBracket();
			}


	protected void appendgeoSPARQLSpatialRelation(BinaryGeneralDBOperator expr, GeneralDBSqlExprBuilder filter, SpatialFunctionsPostGIS func)
			throws UnsupportedRdbmsOperatorException
			{
		filter.openBracket();
		boolean check1 = expr.getLeftArg().getClass().getCanonicalName().equals("org.openrdf.sail.generaldb.algebra.GeneralDBSqlNull");
		if(check1)
		{
			this.append((GeneralDBSqlNull)expr.getLeftArg(), filter);

		}
		else
		{
			char[][] intersectionPattern = null;
			switch(func)
			{
			case SF_Contains:  
				intersectionPattern = new char[1][9];
				intersectionPattern[0][0] = 'T';
				intersectionPattern[0][1] = '*';
				intersectionPattern[0][2] = '*';
				intersectionPattern[0][3] = '*';
				intersectionPattern[0][4] = '*';
				intersectionPattern[0][5] = '*';
				intersectionPattern[0][6] = 'F';
				intersectionPattern[0][7] = 'F';
				intersectionPattern[0][8] = '*';
				break;
			case SF_Crosses:
				// FIXME BUG
				// TODO a crosses b, they have some but not all interior points in common 
				// (and the dimension of the intersection is less than that of at least one 
				// of them). Mask selection rules are checked only when dim(a)≠dim(b), 
				// except by point/point inputs, otherwise is false.
				// (II=0) for points,   (II ∧ IE) when dim(a)<dim(b),   (II ∧ EI) when dim(a)>dim(b)
				intersectionPattern = new char[3][9];
				intersectionPattern[0][0] = 'T';
				intersectionPattern[0][1] = '*';
				intersectionPattern[0][2] = 'T';
				intersectionPattern[0][3] = '*';
				intersectionPattern[0][4] = '*';
				intersectionPattern[0][5] = '*';
				intersectionPattern[0][6] = '*';
				intersectionPattern[0][7] = '*';
				intersectionPattern[0][8] = '*';
				//
				intersectionPattern[1][0] = 'T';
				intersectionPattern[1][1] = '*';
				intersectionPattern[1][2] = '*';
				intersectionPattern[1][3] = '*';
				intersectionPattern[1][4] = '*';
				intersectionPattern[1][5] = '*';
				intersectionPattern[1][6] = 'T';
				intersectionPattern[1][7] = '*';
				intersectionPattern[1][8] = '*';
				//
				intersectionPattern[2][0] = '0';
				intersectionPattern[2][1] = '*';
				intersectionPattern[2][2] = '*';
				intersectionPattern[2][3] = '*';
				intersectionPattern[2][4] = '*';
				intersectionPattern[2][5] = '*';
				intersectionPattern[2][6] = '*';
				intersectionPattern[2][7] = '*';
				intersectionPattern[2][8] = '*';
				break;
			case SF_Disjoint:
			case EH_Disjoint:
				intersectionPattern = new char[1][9];
				intersectionPattern[0][0] = 'F';
				intersectionPattern[0][1] = 'F';
				intersectionPattern[0][2] = '*';
				intersectionPattern[0][3] = 'F';
				intersectionPattern[0][4] = 'F';
				intersectionPattern[0][5] = '*';
				intersectionPattern[0][6] = '*';
				intersectionPattern[0][7] = '*';
				intersectionPattern[0][8] = '*';
				break;
			case SF_Equals: 
			case EH_Equals:
			case RCC8_Eq:	
				intersectionPattern = new char[1][9];
				intersectionPattern[0][0] = 'T';
				intersectionPattern[0][1] = 'F';
				intersectionPattern[0][2] = 'F';
				intersectionPattern[0][3] = 'F';
				intersectionPattern[0][4] = 'T';
				intersectionPattern[0][5] = 'F';
				intersectionPattern[0][6] = 'F';
				intersectionPattern[0][7] = 'F';
				intersectionPattern[0][8] = 'T';
				break;
			case SF_Overlaps:
			case EH_Overlap:
				// FIXME BUG
				// TODO a overlaps b, they have some but not all points in common, 
				// they have the same dimension, and the intersection of the 
				// interiors of the two geometries has the same dimension as the 
				// geometries themselves. Mask selection rules are checked 
				// only when dim(a)=dim(b), otherwise is false:
				// (II ∧ IE ∧ EI) for points or surfaces,   (II=1 ∧ IE ∧ EI) for lines
				intersectionPattern = new char[2][9];
				intersectionPattern[0][0] = 'T';
				intersectionPattern[0][1] = '*';
				intersectionPattern[0][2] = 'T';
				intersectionPattern[0][3] = '*';
				intersectionPattern[0][4] = '*';
				intersectionPattern[0][5] = '*';
				intersectionPattern[0][6] = 'T';
				intersectionPattern[0][7] = '*';
				intersectionPattern[0][8] = '*';
				intersectionPattern[1][0] = '1';
				intersectionPattern[1][1] = '*';
				intersectionPattern[1][2] = 'T';
				intersectionPattern[1][3] = '*';
				intersectionPattern[1][4] = '*';
				intersectionPattern[1][5] = '*';
				intersectionPattern[1][6] = 'T';
				intersectionPattern[1][7] = '*';
				intersectionPattern[1][8] = '*';
				break;
			case SF_Within: 
				intersectionPattern = new char[1][9];
				intersectionPattern[0][0] = 'T';
				intersectionPattern[0][1] = '*';
				intersectionPattern[0][2] = 'F';
				intersectionPattern[0][3] = '*';
				intersectionPattern[0][4] = '*';
				intersectionPattern[0][5] = 'F';
				intersectionPattern[0][6] = '*';
				intersectionPattern[0][7] = '*';
				intersectionPattern[0][8] = '*';
				break;
			case EH_Covers: 
				intersectionPattern = new char[1][9];
				intersectionPattern[0][0] = 'T';
				intersectionPattern[0][1] = '*';
				intersectionPattern[0][2] = 'T';
				intersectionPattern[0][3] = 'F';
				intersectionPattern[0][4] = 'T';
				intersectionPattern[0][5] = '*';
				intersectionPattern[0][6] = 'F';
				intersectionPattern[0][7] = 'F';
				intersectionPattern[0][8] = '*';
				break;
			case EH_CoveredBy: 
				intersectionPattern = new char[1][9];
				intersectionPattern[0][0] = 'T';
				intersectionPattern[0][1] = 'F';
				intersectionPattern[0][2] = 'F';
				intersectionPattern[0][3] = '*';
				intersectionPattern[0][4] = 'T';
				intersectionPattern[0][5] = 'F';
				intersectionPattern[0][6] = 'T';
				intersectionPattern[0][7] = '*';
				intersectionPattern[0][8] = '*';
				break;
			case EH_Inside: 
				intersectionPattern = new char[1][9];
				intersectionPattern[0][0] = 'T';
				intersectionPattern[0][1] = 'F';
				intersectionPattern[0][2] = 'F';
				intersectionPattern[0][3] = '*';
				intersectionPattern[0][4] = 'F';
				intersectionPattern[0][5] = 'F';
				intersectionPattern[0][6] = 'T';
				intersectionPattern[0][7] = '*';
				intersectionPattern[0][8] = '*';
				break;
			case EH_Contains: 
				intersectionPattern = new char[1][9];
				intersectionPattern[0][0] = 'T';
				intersectionPattern[0][1] = '*';
				intersectionPattern[0][2] = 'T';
				intersectionPattern[0][3] = 'F';
				intersectionPattern[0][4] = 'F';
				intersectionPattern[0][5] = '*';
				intersectionPattern[0][6] = 'F';
				intersectionPattern[0][7] = 'F';
				intersectionPattern[0][8] = '*';
				break;
			case RCC8_Dc:	
				intersectionPattern = new char[1][9];
				intersectionPattern[0][0] = 'F';
				intersectionPattern[0][1] = 'F';
				intersectionPattern[0][2] = 'T';
				intersectionPattern[0][3] = 'F';
				intersectionPattern[0][4] = 'F';
				intersectionPattern[0][5] = 'T';
				intersectionPattern[0][6] = 'T';
				intersectionPattern[0][7] = 'T';
				intersectionPattern[0][8] = 'T';
				break;
			case RCC8_Ec:	
				intersectionPattern = new char[1][9];
				intersectionPattern[0][0] = 'F';
				intersectionPattern[0][1] = 'F';
				intersectionPattern[0][2] = 'T';
				intersectionPattern[0][3] = 'F';
				intersectionPattern[0][4] = 'T';
				intersectionPattern[0][5] = 'T';
				intersectionPattern[0][6] = 'T';
				intersectionPattern[0][7] = 'T';
				intersectionPattern[0][8] = 'T';
				break;
			case RCC8_Po:	
				intersectionPattern = new char[1][9];
				intersectionPattern[0][0] = 'T';
				intersectionPattern[0][1] = 'T';
				intersectionPattern[0][2] = 'T';
				intersectionPattern[0][3] = 'T';
				intersectionPattern[0][4] = 'T';
				intersectionPattern[0][5] = 'T';
				intersectionPattern[0][6] = 'T';
				intersectionPattern[0][7] = 'T';
				intersectionPattern[0][8] = 'T';
				break;
			case RCC8_Tppi:	
				intersectionPattern = new char[1][9];
				intersectionPattern[0][0] = 'T';
				intersectionPattern[0][1] = 'T';
				intersectionPattern[0][2] = 'T';
				intersectionPattern[0][3] = 'F';
				intersectionPattern[0][4] = 'T';
				intersectionPattern[0][5] = 'T';
				intersectionPattern[0][6] = 'F';
				intersectionPattern[0][7] = 'F';
				intersectionPattern[0][8] = 'T';
				break;
			case RCC8_Tpp:	
				intersectionPattern = new char[1][9];
				intersectionPattern[0][0] = 'T';
				intersectionPattern[0][1] = 'F';
				intersectionPattern[0][2] = 'F';
				intersectionPattern[0][3] = 'T';
				intersectionPattern[0][4] = 'T';
				intersectionPattern[0][5] = 'F';
				intersectionPattern[0][6] = 'T';
				intersectionPattern[0][7] = 'T';
				intersectionPattern[0][8] = 'T';
				break;
			case RCC8_Ntpp:	
				intersectionPattern = new char[1][9];
				intersectionPattern[0][0] = 'T';
				intersectionPattern[0][1] = 'F';
				intersectionPattern[0][2] = 'F';
				intersectionPattern[0][3] = 'T';
				intersectionPattern[0][4] = 'F';
				intersectionPattern[0][5] = 'F';
				intersectionPattern[0][6] = 'T';
				intersectionPattern[0][7] = 'T';
				intersectionPattern[0][8] = 'T';
				break;
			case RCC8_Ntppi:	
				intersectionPattern = new char[1][9];
				intersectionPattern[0][0] = 'T';
				intersectionPattern[0][1] = 'T';
				intersectionPattern[0][2] = 'T';
				intersectionPattern[0][3] = 'F';
				intersectionPattern[0][4] = 'F';
				intersectionPattern[0][5] = 'T';
				intersectionPattern[0][6] = 'F';
				intersectionPattern[0][7] = 'F';
				intersectionPattern[0][8] = 'T';
				break;
			case SF_Intersects:   
				intersectionPattern = new char[4][9];
				intersectionPattern[0][0] = 'T';
				intersectionPattern[0][1] = '*';
				intersectionPattern[0][2] = '*';
				intersectionPattern[0][3] = '*';
				intersectionPattern[0][4] = '*';
				intersectionPattern[0][5] = '*';
				intersectionPattern[0][6] = '*';
				intersectionPattern[0][7] = '*';
				intersectionPattern[0][8] = '*';
				//
				intersectionPattern[1][0] = '*';
				intersectionPattern[1][1] = 'T';
				intersectionPattern[1][2] = '*';
				intersectionPattern[1][3] = '*';
				intersectionPattern[1][4] = '*';
				intersectionPattern[1][5] = '*';
				intersectionPattern[1][6] = '*';
				intersectionPattern[1][7] = '*';
				intersectionPattern[1][8] = '*';
				//
				intersectionPattern[2][0] = '*';
				intersectionPattern[2][1] = '*';
				intersectionPattern[2][2] = '*';
				intersectionPattern[2][3] = 'T';
				intersectionPattern[2][4] = '*';
				intersectionPattern[2][5] = '*';
				intersectionPattern[2][6] = '*';
				intersectionPattern[2][7] = '*';
				intersectionPattern[2][8] = '*';
				//
				intersectionPattern[3][0] = '*';
				intersectionPattern[3][1] = '*';
				intersectionPattern[3][2] = '*';
				intersectionPattern[3][3] = '*';
				intersectionPattern[3][4] = 'T';
				intersectionPattern[3][5] = '*';
				intersectionPattern[3][6] = '*';
				intersectionPattern[3][7] = '*';
				intersectionPattern[3][8] = '*';
				break;

			case SF_Touches: 
			case EH_Meet:
				intersectionPattern = new char[3][9];
				intersectionPattern[0][0] = 'F';
				intersectionPattern[0][1] = 'T';
				intersectionPattern[0][2] = '*';
				intersectionPattern[0][3] = '*';
				intersectionPattern[0][4] = '*';
				intersectionPattern[0][5] = '*';
				intersectionPattern[0][6] = '*';
				intersectionPattern[0][7] = '*';
				intersectionPattern[0][8] = '*';
				//
				intersectionPattern[1][0] = 'F';
				intersectionPattern[1][1] = '*';
				intersectionPattern[1][2] = '*';
				intersectionPattern[1][3] = 'T';
				intersectionPattern[1][4] = '*';
				intersectionPattern[1][5] = '*';
				intersectionPattern[1][6] = '*';
				intersectionPattern[1][7] = '*';
				intersectionPattern[1][8] = '*';
				//
				intersectionPattern[2][0] = 'F';
				intersectionPattern[2][1] = '*';
				intersectionPattern[2][2] = '*';
				intersectionPattern[2][3] = '*';
				intersectionPattern[2][4] = 'T';
				intersectionPattern[2][5] = '*';
				intersectionPattern[2][6] = '*';
				intersectionPattern[2][7] = '*';
				intersectionPattern[2][8] = '*';
				//

			}

			for(int i = 0; i < intersectionPattern.length ; i++)
			{
				appendRelate(expr, filter, intersectionPattern[i]);
				if(i < intersectionPattern.length - 1)
				{
					//append OR and continue
					filter.or();
				}
			}

			//Also need bounding box intersection query to enable the usage of the Gist R-tree index
			if(func != SpatialFunctionsPostGIS.SF_Disjoint && func != SpatialFunctionsPostGIS.EH_Disjoint && func != SpatialFunctionsPostGIS.RCC8_Dc)
			{
				filter.and();
				appendGeneralDBSpatialFunctionBinary(expr, filter,SpatialFunctionsPostGIS.ST_Intersects);
			}
		}
		filter.closeBracket();
			}

	@Override
	//GeoSPARQL
	//XXX

	protected void appendRelate(BinaryGeneralDBOperator expr, GeneralDBSqlExprBuilder filter, char[] intersectionPattern)
			throws UnsupportedRdbmsOperatorException
			{
		filter.openBracket();
		//System.out.println(expr.getLeftArg().getClass().getCanonicalName());
		boolean check1 = expr.getLeftArg().getClass().getCanonicalName().equals("org.openrdf.sail.generaldb.algebra.GeneralDBSqlNull");
		if(check1)
		{
			this.append((GeneralDBSqlNull)expr.getLeftArg(), filter);

		}
		else
		{	
			filter.appendFunction("ST_Relate");
			filter.openBracket();
			if(expr.getLeftArg() instanceof GeneralDBStringValue)
			{
				appendWKT(expr.getLeftArg(),filter);
			}
			else if(expr.getLeftArg() instanceof GeneralDBSqlSpatialConstructBinary)
			{
				appendConstructFunction(expr.getLeftArg(), filter);
			}
			else if(expr.getLeftArg() instanceof GeneralDBSqlSpatialConstructUnary)
			{
				appendConstructFunction(expr.getLeftArg(), filter);
			}
			else if(expr.getLeftArg() instanceof GeneralDBSqlSpatialConstructTriple)
			{
				appendConstructFunction(expr.getLeftArg(), filter);
			}
			else if(expr.getLeftArg() instanceof GeneralDBSqlCase)
			{
				GeneralDBLabelColumn onlyLabel = (GeneralDBLabelColumn)((GeneralDBSqlCase)expr.getLeftArg()).getEntries().get(0).getResult();
				appendMBB(onlyLabel,filter); 
			}
			else
			{
				appendMBB((GeneralDBLabelColumn)(expr.getLeftArg()),filter);
			}
			filter.appendComma();
			boolean check2 = expr.getRightArg().getClass().getCanonicalName().equals("org.openrdf.sail.generaldb.algebra.GeneralDBSqlNull");
			if(check2)
			{
				this.append((GeneralDBSqlNull)expr.getRightArg(), filter);
			}
			else
			{
				if(expr.getRightArg() instanceof GeneralDBStringValue)
				{
					appendWKT(expr.getRightArg(),filter);
				}
				else if(expr.getRightArg() instanceof GeneralDBSqlSpatialConstructUnary)
				{
					appendConstructFunction(expr.getRightArg(), filter);
				}
				else if(expr.getRightArg() instanceof GeneralDBSqlSpatialConstructBinary)
				{
					appendConstructFunction(expr.getRightArg(), filter);
				}
				
				else if(expr.getRightArg() instanceof GeneralDBSqlSpatialConstructTriple)
				{
					appendConstructFunction(expr.getRightArg(), filter);
				}
				else if(expr.getRightArg() instanceof GeneralDBSqlCase)
				{
					GeneralDBLabelColumn onlyLabel = (GeneralDBLabelColumn)((GeneralDBSqlCase)expr.getRightArg()).getEntries().get(0).getResult();
					appendMBB(onlyLabel,filter);					 
				}
				else if(expr.getRightArg() instanceof GeneralDBDoubleValue) //case met in buffer!
				{
					append(((GeneralDBDoubleValue)expr.getRightArg()), filter);
				}
				else if(expr.getRightArg() instanceof GeneralDBNumericColumn) //case met in buffer!
				{
					append(((GeneralDBNumericColumn)expr.getRightArg()), filter);
				}
				//case met in buffer when in select -> buffer(?spatial,?thematic)
				else if(expr.getRightArg() instanceof GeneralDBLabelColumn && !((GeneralDBLabelColumn)expr.getRightArg()).isSpatial())
				{
					append(((GeneralDBLabelColumn)expr.getRightArg()),filter);
					appendCastToDouble(filter);
				}
				else if(expr.getRightArg() instanceof GeneralDBSqlSpatialMetricBinary)
				{
					appendMetricFunction(expr.getRightArg(), filter);
				}
				else if(expr.getRightArg() instanceof GeneralDBSqlSpatialMetricUnary)
				{
					appendMetricFunction(expr.getRightArg(), filter);
				}
				else if(expr.getRightArg() instanceof GeneralDBSqlSpatialMetricTriple)
				{
					appendMetricFunction(expr.getRightArg(), filter);
				}
				else
				{
					appendMBB((GeneralDBLabelColumn)(expr.getRightArg()),filter);
				}

			}
			//3rd arg
			filter.appendComma();

			//must turn the table of characters I have to a valid sql value!
			filter.append("'");
			for(int i = 0; i< intersectionPattern.length; i++)
			{
				filter.append(intersectionPattern[i]+"");
			}
			filter.append("'");

			filter.closeBracket();
		}

		filter.closeBracket();
	}

}
