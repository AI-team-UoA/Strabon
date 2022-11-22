/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.generaldb.algebra.base;

import java.sql.Types;

import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.query.algebra.Compare.CompareOp;
import org.openrdf.query.algebra.MathExpr;
import org.openrdf.sail.generaldb.algebra.GeneralDBDoubleValue;
import org.openrdf.sail.generaldb.algebra.GeneralDBFalseValue;
import org.openrdf.sail.generaldb.algebra.GeneralDBSqlAbove;
import org.openrdf.sail.generaldb.algebra.GeneralDBSqlAbs;
import org.openrdf.sail.generaldb.algebra.GeneralDBSqlAnd;
import org.openrdf.sail.generaldb.algebra.GeneralDBSqlBelow;
import org.openrdf.sail.generaldb.algebra.GeneralDBSqlCase;
import org.openrdf.sail.generaldb.algebra.GeneralDBSqlCast;
import org.openrdf.sail.generaldb.algebra.GeneralDBSqlCompare;
import org.openrdf.sail.generaldb.algebra.GeneralDBSqlConcat;
import org.openrdf.sail.generaldb.algebra.GeneralDBSqlContains;
import org.openrdf.sail.generaldb.algebra.GeneralDBSqlGeoSPARQLSrid;
import org.openrdf.sail.generaldb.algebra.GeneralDBSqlMbbContains;
import org.openrdf.sail.generaldb.algebra.GeneralDBSqlCrosses;
import org.openrdf.sail.generaldb.algebra.GeneralDBSqlDiffDateTime;
import org.openrdf.sail.generaldb.algebra.GeneralDBSqlDisjoint;
import org.openrdf.sail.generaldb.algebra.GeneralDBSqlEq;
import org.openrdf.sail.generaldb.algebra.GeneralDBSqlEqualsSpatial;
import org.openrdf.sail.generaldb.algebra.GeneralDBSqlExtDiffDateTime;
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
import org.openrdf.sail.generaldb.algebra.GeneralDBSqlGeoSrid;
import org.openrdf.sail.generaldb.algebra.GeneralDBSqlGeoSymDifference;
import org.openrdf.sail.generaldb.algebra.GeneralDBSqlGeoTransform;
import org.openrdf.sail.generaldb.algebra.GeneralDBSqlGeoUnion;
import org.openrdf.sail.generaldb.algebra.GeneralDBSqlIntersects;
import org.openrdf.sail.generaldb.algebra.GeneralDBSqlIsNull;
import org.openrdf.sail.generaldb.algebra.GeneralDBSqlLeft;
import org.openrdf.sail.generaldb.algebra.GeneralDBSqlLike;
import org.openrdf.sail.generaldb.algebra.GeneralDBSqlLowerCase;
import org.openrdf.sail.generaldb.algebra.GeneralDBSqlMathExpr;
import org.openrdf.sail.generaldb.algebra.GeneralDBSqlMbbEquals;
import org.openrdf.sail.generaldb.algebra.GeneralDBSqlMbbWithin;
import org.openrdf.sail.generaldb.algebra.GeneralDBSqlMbbIntersects;
import org.openrdf.sail.generaldb.algebra.GeneralDBSqlNot;
import org.openrdf.sail.generaldb.algebra.GeneralDBSqlNull;
import org.openrdf.sail.generaldb.algebra.GeneralDBSqlOr;
import org.openrdf.sail.generaldb.algebra.GeneralDBSqlOverlaps;
import org.openrdf.sail.generaldb.algebra.GeneralDBSqlRegex;
import org.openrdf.sail.generaldb.algebra.GeneralDBSqlRelate;
import org.openrdf.sail.generaldb.algebra.GeneralDBSqlRight;
import org.openrdf.sail.generaldb.algebra.GeneralDBSqlST_Centroid;
import org.openrdf.sail.generaldb.algebra.GeneralDBSqlST_MakeLine;
import org.openrdf.sail.generaldb.algebra.GeneralDBSqlTouches;
import org.openrdf.sail.generaldb.algebra.GeneralDBSqlWithin;
import org.openrdf.sail.generaldb.algebra.GeneralDBStringValue;
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
import org.openrdf.sail.rdbms.exceptions.UnsupportedRdbmsOperatorException;

/**
 * Support method to create SQL expressions.
 *
 * @author Manos Karpathiotakis <mk@di.uoa.gr>
 * @author James Leigh
 *
 */
public class GeneralDBExprSupport {

	public static GeneralDBSqlExpr abs(GeneralDBSqlExpr arg) {
		return new GeneralDBSqlAbs(arg);
	}

	public static GeneralDBSqlExpr and(GeneralDBSqlExpr left, GeneralDBSqlExpr right) {
		return new GeneralDBSqlAnd(left, right);
	}

	public static GeneralDBSqlExpr cmp(GeneralDBSqlExpr left, CompareOp op, GeneralDBSqlExpr right) {
		return new GeneralDBSqlCompare(left, op, right);
	}

	public static GeneralDBSqlExpr coalesce(GeneralDBSqlExpr... exprs) {
		GeneralDBSqlCase sqlCase = new GeneralDBSqlCase();
		for (GeneralDBSqlExpr expr : exprs) {
			sqlCase.when(isNotNull(expr.clone()), expr);
		}
		return sqlCase;
	}

	public static GeneralDBSqlExpr concat(GeneralDBSqlExpr left, GeneralDBSqlExpr right) {
		return new GeneralDBSqlConcat(left, right);
	}

	public static GeneralDBSqlExpr eq(GeneralDBSqlExpr left, GeneralDBSqlExpr right) {
		return new GeneralDBSqlEq(left, right);
	}

	public static GeneralDBSqlExpr eqComparingNull(GeneralDBSqlExpr left, GeneralDBSqlExpr right) {
		GeneralDBSqlExpr leftIsNull = isNull(left.clone());
		GeneralDBSqlExpr rightIsNull = isNull(right.clone());
		GeneralDBSqlExpr bothNull = and(leftIsNull, rightIsNull);
		GeneralDBSqlExpr bothNotNull = and(not(leftIsNull), not(rightIsNull));
		return or(bothNull, and(bothNotNull, eq(left, right)));
	}

	public static GeneralDBSqlExpr eqIfNotNull(GeneralDBSqlExpr left, GeneralDBSqlExpr right) {
		GeneralDBSqlExpr leftIsNotNull = isNotNull(left.clone());
		GeneralDBSqlExpr rightIsNotNull = isNotNull(right.clone());
		GeneralDBSqlExpr bothNotNull = and(leftIsNotNull, rightIsNotNull);
		return and(bothNotNull, eq(left, right));
	}

	public static GeneralDBSqlExpr eqOrBothNull(GeneralDBSqlExpr left, GeneralDBSqlExpr right) {
		GeneralDBSqlExpr leftIsNull = isNull(left.clone());
		GeneralDBSqlExpr rightIsNull = isNull(right.clone());
		GeneralDBSqlExpr bothNull = and(leftIsNull, rightIsNull);
		return or(bothNull, eq(left, right));
	}

	public static GeneralDBSqlExpr eqOrSimpleType(GeneralDBSqlExpr left, GeneralDBSqlExpr right) {
		GeneralDBSqlExpr bothSimple = and(simple(left), simple(right));
		return or(eq(left.clone(), right.clone()), bothSimple);
	}

	public static GeneralDBSqlExpr ge(GeneralDBSqlExpr left, GeneralDBSqlExpr right) {
		return new GeneralDBSqlCompare(left, CompareOp.GE, right);
	}

	public static GeneralDBSqlExpr gt(GeneralDBSqlExpr left, GeneralDBSqlExpr right) {
		return new GeneralDBSqlCompare(left, CompareOp.GT, right);
	}

	public static GeneralDBSqlExpr in(GeneralDBSqlExpr compare, GeneralDBSqlExpr... values) {
		GeneralDBSqlExpr expr = null;
		for (GeneralDBSqlExpr value : values) {
			if (expr == null) {
				expr = new GeneralDBSqlEq(compare, value);
			}
			else {
				expr = or(expr, new GeneralDBSqlEq(compare.clone(), value));
			}
		}
		if (expr == null)
			return new GeneralDBFalseValue();
		return expr;
	}

	public static GeneralDBSqlExpr isNotNull(GeneralDBSqlExpr arg) {
		return not(isNull(arg));
	}

	public static GeneralDBSqlExpr isNull(GeneralDBSqlExpr arg) {
		return new GeneralDBSqlIsNull(arg);
	}

	public static GeneralDBSqlExpr le(GeneralDBSqlExpr left, GeneralDBSqlExpr right) {
		return new GeneralDBSqlCompare(left, CompareOp.LE, right);
	}

	public static GeneralDBSqlExpr like(GeneralDBSqlExpr left, GeneralDBSqlExpr right) {
		return new GeneralDBSqlLike(left, right);
	}

	public static GeneralDBSqlExpr lowercase(GeneralDBSqlExpr arg) {
		return new GeneralDBSqlLowerCase(arg);
	}

	public static GeneralDBSqlExpr lt(GeneralDBSqlExpr left, GeneralDBSqlExpr right) {
		return new GeneralDBSqlCompare(left, CompareOp.LT, right);
	}

	public static GeneralDBSqlExpr neq(GeneralDBSqlExpr left, GeneralDBSqlExpr right) {
		return new GeneralDBSqlNot(new GeneralDBSqlEq(left, right));
	}

	public static GeneralDBSqlExpr neqComparingNull(GeneralDBSqlExpr left, GeneralDBSqlExpr right) {
		GeneralDBSqlExpr leftIsNull = isNull(left.clone());
		GeneralDBSqlExpr rightIsNull = isNull(right.clone());
		GeneralDBSqlExpr onlyLeftIsNull = and(not(leftIsNull), rightIsNull.clone());
		GeneralDBSqlExpr onlyRightIsNull = and(leftIsNull.clone(), not(rightIsNull));
		GeneralDBSqlExpr compareNull = or(onlyRightIsNull, onlyLeftIsNull);
		return or(not(eq(left, right)), compareNull);
	}

	public static GeneralDBSqlExpr not(GeneralDBSqlExpr arg) {
		return new GeneralDBSqlNot(arg);
	}

	public static GeneralDBSqlExpr num(double value) {
		return new GeneralDBDoubleValue(value);
	}

	public static GeneralDBSqlExpr or(GeneralDBSqlExpr left, GeneralDBSqlExpr right) {
		return new GeneralDBSqlOr(left, right);
	}

	public static GeneralDBSqlExpr regex(GeneralDBSqlExpr value, GeneralDBSqlExpr pattern) {
		return new GeneralDBSqlRegex(value, pattern);
	}

	public static GeneralDBSqlExpr regex(GeneralDBSqlExpr value, GeneralDBSqlExpr pattern, GeneralDBSqlExpr flags) {
		return new GeneralDBSqlRegex(value, pattern, flags);
	}

	public static GeneralDBSqlExpr simple(GeneralDBSqlExpr arg) {
		GeneralDBSqlExpr isString = eq(arg.clone(), str(XMLSchema.STRING));
		return or(isNull(arg.clone()), isString);
	}

	public static GeneralDBSqlExpr sqlNull() {
		return new GeneralDBSqlNull();
	}

	public static GeneralDBSqlExpr str(String str) {
		if (str == null)
			return sqlNull();
		return new GeneralDBStringValue(str);
	}

	public static GeneralDBSqlExpr str(URI uri) {
		return new GeneralDBStringValue(uri.stringValue());
	}

	public static GeneralDBSqlExpr sub(GeneralDBSqlExpr left, GeneralDBSqlExpr right) {
		return new GeneralDBSqlMathExpr(left, MathExpr.MathOp.MINUS, right);
	}

	public static GeneralDBSqlExpr text(GeneralDBSqlExpr arg) {
		return new GeneralDBSqlCast(arg, Types.VARCHAR);
	}

	public static UnsupportedRdbmsOperatorException unsupported(Object arg) {
		return new UnsupportedRdbmsOperatorException(arg.toString());
	}

	private GeneralDBExprSupport() {
		// no constructor
	}

	//XXX Spatial Relationship Functions - all 9 of them - stSPARQL++
	public static GeneralDBSqlExpr equalsGeo(GeneralDBSqlExpr left, GeneralDBSqlExpr right) {
		return new GeneralDBSqlEqualsSpatial(left, right);
	}

	public static GeneralDBSqlExpr disjoint(GeneralDBSqlExpr left, GeneralDBSqlExpr right) {
		return new GeneralDBSqlDisjoint(left, right);
	}

	public static GeneralDBSqlExpr intersects(GeneralDBSqlExpr left, GeneralDBSqlExpr right) {
		return new GeneralDBSqlIntersects(left, right);
	}

	public static GeneralDBSqlExpr touches(GeneralDBSqlExpr left, GeneralDBSqlExpr right) {
		return new GeneralDBSqlTouches(left, right);
	}

	public static GeneralDBSqlExpr crosses(GeneralDBSqlExpr left, GeneralDBSqlExpr right) {
		return new GeneralDBSqlCrosses(left, right);
	}

	public static GeneralDBSqlExpr within(GeneralDBSqlExpr left, GeneralDBSqlExpr right) {
		return new GeneralDBSqlWithin(left, right);
	}

	public static GeneralDBSqlExpr contains(GeneralDBSqlExpr left, GeneralDBSqlExpr right) {
		return new GeneralDBSqlContains(left, right);
	}

	public static GeneralDBSqlExpr overlaps(GeneralDBSqlExpr left, GeneralDBSqlExpr right) {
		return new GeneralDBSqlOverlaps(left, right);
	}

	public static GeneralDBSqlExpr relate(GeneralDBSqlExpr left, GeneralDBSqlExpr right, GeneralDBSqlExpr third) {
		return new GeneralDBSqlRelate(left, right,third);
	}

	// mbb functions

	public static GeneralDBSqlExpr mbbIntersects(GeneralDBSqlExpr left, GeneralDBSqlExpr right) {
		return new GeneralDBSqlMbbIntersects(left, right);
	}
	public static GeneralDBSqlExpr mbbWithin(GeneralDBSqlExpr left, GeneralDBSqlExpr right) {
		return new GeneralDBSqlMbbWithin(left, right);
	}

	public static GeneralDBSqlExpr mbbEqualsGeo(GeneralDBSqlExpr left, GeneralDBSqlExpr right) {
		return new GeneralDBSqlMbbEquals(left, right);
	}

	public static GeneralDBSqlExpr mbbContains(GeneralDBSqlExpr left, GeneralDBSqlExpr right) {
		return new GeneralDBSqlMbbContains(left, right);
	}

	// directional

	public static GeneralDBSqlExpr left(GeneralDBSqlExpr left, GeneralDBSqlExpr right) {
		return new GeneralDBSqlLeft(left, right);
	}

	public static GeneralDBSqlExpr right(GeneralDBSqlExpr left, GeneralDBSqlExpr right) {
		return new GeneralDBSqlRight(left, right);
	}

	public static GeneralDBSqlExpr above(GeneralDBSqlExpr left, GeneralDBSqlExpr right) {
		return new GeneralDBSqlAbove(left, right);
	}

	public static GeneralDBSqlExpr below(GeneralDBSqlExpr left, GeneralDBSqlExpr right) {
		return new GeneralDBSqlBelow(left, right);
	}

	//XXX Spatial Construct Functions
	public static GeneralDBSqlExpr geoUnion(GeneralDBSqlExpr left, GeneralDBSqlExpr right, String resultType) {

		return new GeneralDBSqlGeoUnion(left, right, resultType);
	}

	public static GeneralDBSqlExpr geoBuffer(GeneralDBSqlExpr left, GeneralDBSqlExpr right, GeneralDBSqlExpr third, String resultType) {

		return new GeneralDBSqlGeoBuffer(left, right, third, resultType);
	}

	public static GeneralDBSqlExpr geoTransform(GeneralDBSqlExpr left, GeneralDBSqlExpr right, String resultType) {

		return new GeneralDBSqlGeoTransform(left, right, resultType);
	}

	public static GeneralDBSqlExpr geoEnvelope(GeneralDBSqlExpr expr, String resultType) {

		return new GeneralDBSqlGeoEnvelope(expr, resultType);
	}

	public static GeneralDBSqlExpr geoConvexHull(GeneralDBSqlExpr expr, String resultType) {

		return new GeneralDBSqlGeoConvexHull(expr, resultType);
	}

	public static GeneralDBSqlExpr geoBoundary(GeneralDBSqlExpr expr, String resultType) {

		return new GeneralDBSqlGeoBoundary(expr, resultType);
	}

	public static GeneralDBSqlExpr geoIntersection(GeneralDBSqlExpr left, GeneralDBSqlExpr right, String resultType) {

		return new GeneralDBSqlGeoIntersection(left, right, resultType);
	}

	public static GeneralDBSqlExpr geoDifference(GeneralDBSqlExpr left, GeneralDBSqlExpr right, String resultType) {

		return new GeneralDBSqlGeoDifference(left, right, resultType);
	}

	public static GeneralDBSqlExpr geoSymDifference(GeneralDBSqlExpr left, GeneralDBSqlExpr right, String resultType) {

		return new GeneralDBSqlGeoSymDifference(left, right, resultType);
	}

	/** PostGIS Construct functions **/
	// Binary
	public static GeneralDBSqlExpr st_MakeLine(GeneralDBSqlExpr left, GeneralDBSqlExpr right, String resultType) {

		return new GeneralDBSqlST_MakeLine(left, right, resultType);
	}
	// Unary
	public static GeneralDBSqlExpr st_Centroid(GeneralDBSqlExpr expr, String resultType) {

		return new GeneralDBSqlST_Centroid(expr, resultType);
	}
	/** PostGIS Construct functions **/


	/** Addition for datetime metric functions
	 *
	 * @author George Garbis <ggarbis@di.uoa.gr>
	 *
	 */
	public static GeneralDBSqlExpr extDiffDateTime(GeneralDBSqlExpr left, GeneralDBSqlExpr right) {

		return new GeneralDBSqlExtDiffDateTime(left, right);
	}
	/***/

	/** Addition for datetime metric functions
	 *
	 * @author George Garbis <ggarbis@di.uoa.gr>
	 *
	 */
	public static GeneralDBSqlExpr diffDateTime(GeneralDBSqlExpr left, GeneralDBSqlExpr right) {

		return new GeneralDBSqlDiffDateTime(left, right);
	}
	/***/

	//XXX Spatial Metric Functions
	public static GeneralDBSqlExpr geoArea(GeneralDBSqlExpr expr) {
		return new GeneralDBSqlGeoArea(expr);
	}

	public static GeneralDBSqlExpr geoDistance(GeneralDBSqlExpr left, GeneralDBSqlExpr right, GeneralDBSqlExpr third) {
		return new GeneralDBSqlGeoDistance(left, right,third);
	}

	//XXX Spatial Property Functions
	public static GeneralDBSqlExpr dimension(GeneralDBSqlExpr expr) {
		return new GeneralDBSqlGeoDimension(expr);
	}

	public static GeneralDBSqlExpr geometryType(GeneralDBSqlExpr expr) {
		return new GeneralDBSqlGeoGeometryType(expr);
	}

	public static GeneralDBSqlExpr asText(GeneralDBSqlExpr expr) {
		return new GeneralDBSqlGeoAsText(expr);
	}

	public static GeneralDBSqlExpr asGML(GeneralDBSqlExpr expr) {
		return new GeneralDBSqlGeoAsGML(expr);
	}

	public static GeneralDBSqlExpr srid(GeneralDBSqlExpr expr) {
		return new GeneralDBSqlGeoSrid(expr);
	}
	
	public static GeneralDBSqlExpr geofSRID(GeneralDBSqlExpr expr) {
		return new GeneralDBSqlGeoSPARQLSrid(expr);
	}

	public static GeneralDBSqlExpr isEmpty(GeneralDBSqlExpr expr) {
		return new GeneralDBSqlGeoIsEmpty(expr);
	}

	public static GeneralDBSqlExpr isSimple(GeneralDBSqlExpr expr) {
		return new GeneralDBSqlGeoIsSimple(expr);
	}


	// GeoSPARQL - Spatial Relations
	//Simple Features
	public static GeneralDBSqlExpr sfContains(GeneralDBSqlExpr left, GeneralDBSqlExpr right) {
		return new GeneralDBSqlSF_Contains(left, right);
	}

	public static GeneralDBSqlExpr sfCrosses(GeneralDBSqlExpr left, GeneralDBSqlExpr right) {
		return new GeneralDBSqlSF_Crosses(left, right);
	}

	public static GeneralDBSqlExpr sfDisjoint(GeneralDBSqlExpr left, GeneralDBSqlExpr right) {
		return new GeneralDBSqlSF_Disjoint(left, right);
	}

	public static GeneralDBSqlExpr sfEquals(GeneralDBSqlExpr left, GeneralDBSqlExpr right) {
		return new GeneralDBSqlSF_Equals(left, right);
	}

	public static GeneralDBSqlExpr sfIntersects(GeneralDBSqlExpr left, GeneralDBSqlExpr right) {
		return new GeneralDBSqlSF_Intersects(left, right);
	}

	public static GeneralDBSqlExpr sfOverlaps(GeneralDBSqlExpr left, GeneralDBSqlExpr right) {
		return new GeneralDBSqlSF_Overlaps(left, right);
	}

	public static GeneralDBSqlExpr sfTouches(GeneralDBSqlExpr left, GeneralDBSqlExpr right) {
		return new GeneralDBSqlSF_Touches(left, right);
	}

	public static GeneralDBSqlExpr sfWithin(GeneralDBSqlExpr left, GeneralDBSqlExpr right) {
		return new GeneralDBSqlSF_Within(left, right);
	}

	//RCC8
	public static GeneralDBSqlExpr rccEquals(GeneralDBSqlExpr left, GeneralDBSqlExpr right) {
		return new GeneralDBSqlRCC8_Eq(left, right);
	}

	public static GeneralDBSqlExpr rccDisconnected(GeneralDBSqlExpr left, GeneralDBSqlExpr right) {
		return new GeneralDBSqlRCC8_Dc(left, right);
	}

	public static GeneralDBSqlExpr rccExternallyConnected(GeneralDBSqlExpr left, GeneralDBSqlExpr right) {
		return new GeneralDBSqlRCC8_Ec(left, right);
	}

	public static GeneralDBSqlExpr rccPartiallyOverlapping(GeneralDBSqlExpr left, GeneralDBSqlExpr right) {
		return new GeneralDBSqlRCC8_Po(left, right);
	}

	public static GeneralDBSqlExpr rccTangentialProperPartInverse(GeneralDBSqlExpr left, GeneralDBSqlExpr right) {
		return new GeneralDBSqlRCC8_Tppi(left, right);
	}

	public static GeneralDBSqlExpr rccTangentialProperPart(GeneralDBSqlExpr left, GeneralDBSqlExpr right) {
		return new GeneralDBSqlRCC8_Tpp(left, right);
	}

	public static GeneralDBSqlExpr rccNonTangentialProperPart(GeneralDBSqlExpr left, GeneralDBSqlExpr right) {
		return new GeneralDBSqlRCC8_Ntpp(left, right);
	}

	public static GeneralDBSqlExpr rccNonTangentialProperPartInverse(GeneralDBSqlExpr left, GeneralDBSqlExpr right) {
		return new GeneralDBSqlRCC8_Ntppi(left, right);
	}

	//Egenhofer
	public static GeneralDBSqlExpr ehContains(GeneralDBSqlExpr left, GeneralDBSqlExpr right) {
		return new GeneralDBSqlEgenhofer_Contains(left, right);
	}

	public static GeneralDBSqlExpr ehCoveredBy(GeneralDBSqlExpr left, GeneralDBSqlExpr right) {
		return new GeneralDBSqlEgenhofer_CoveredBy(left, right);
	}

	public static GeneralDBSqlExpr ehCovers(GeneralDBSqlExpr left, GeneralDBSqlExpr right) {
		return new GeneralDBSqlEgenhofer_Covers(left, right);
	}

	public static GeneralDBSqlExpr ehDisjoint(GeneralDBSqlExpr left, GeneralDBSqlExpr right) {
		return new GeneralDBSqlEgenhofer_Disjoint(left, right);
	}

	public static GeneralDBSqlExpr ehEquals(GeneralDBSqlExpr left, GeneralDBSqlExpr right) {
		return new GeneralDBSqlEgenhofer_Equals(left, right);
	}

	public static GeneralDBSqlExpr ehInside(GeneralDBSqlExpr left, GeneralDBSqlExpr right) {
		return new GeneralDBSqlEgenhofer_Inside(left, right);
	}

	public static GeneralDBSqlExpr ehMeet(GeneralDBSqlExpr left, GeneralDBSqlExpr right) {
		return new GeneralDBSqlEgenhofer_Meet(left, right);
	}

	public static GeneralDBSqlExpr ehOverlap(GeneralDBSqlExpr left, GeneralDBSqlExpr right) {
		return new GeneralDBSqlEgenhofer_Overlap(left, right);
	}
}
