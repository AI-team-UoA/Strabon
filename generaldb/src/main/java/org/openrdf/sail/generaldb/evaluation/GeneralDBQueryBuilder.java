/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.generaldb.evaluation;

import java.util.ArrayList;
import java.util.List;

import org.openrdf.model.Value;
import org.openrdf.sail.generaldb.GeneralDBValueFactory;
import org.openrdf.sail.generaldb.algebra.GeneralDBBNodeColumn;
import org.openrdf.sail.generaldb.algebra.GeneralDBColumnVar;
import org.openrdf.sail.generaldb.algebra.GeneralDBDatatypeColumn;
import org.openrdf.sail.generaldb.algebra.GeneralDBDateTimeColumn;
import org.openrdf.sail.generaldb.algebra.GeneralDBDoubleValue;
import org.openrdf.sail.generaldb.algebra.GeneralDBFalseValue;
import org.openrdf.sail.generaldb.algebra.GeneralDBHashColumn;
import org.openrdf.sail.generaldb.algebra.GeneralDBIdColumn;
import org.openrdf.sail.generaldb.algebra.GeneralDBJoinItem;
import org.openrdf.sail.generaldb.algebra.GeneralDBLabelColumn;
import org.openrdf.sail.generaldb.algebra.GeneralDBLanguageColumn;
import org.openrdf.sail.generaldb.algebra.GeneralDBLongLabelColumn;
import org.openrdf.sail.generaldb.algebra.GeneralDBLongURIColumn;
import org.openrdf.sail.generaldb.algebra.GeneralDBNumberValue;
import org.openrdf.sail.generaldb.algebra.GeneralDBNumericColumn;
import org.openrdf.sail.generaldb.algebra.GeneralDBRefIdColumn;
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
import org.openrdf.sail.generaldb.algebra.GeneralDBSqlShift;
import org.openrdf.sail.generaldb.algebra.GeneralDBSqlTouches;
import org.openrdf.sail.generaldb.algebra.GeneralDBSqlWithin;
import org.openrdf.sail.generaldb.algebra.GeneralDBStringValue;
import org.openrdf.sail.generaldb.algebra.GeneralDBTrueValue;
import org.openrdf.sail.generaldb.algebra.GeneralDBURIColumn;
import org.openrdf.sail.generaldb.algebra.base.BinaryGeneralDBOperator;
import org.openrdf.sail.generaldb.algebra.base.GeneralDBFromItem;
import org.openrdf.sail.generaldb.algebra.base.GeneralDBSqlConstant;
import org.openrdf.sail.generaldb.algebra.base.GeneralDBSqlExpr;
import org.openrdf.sail.generaldb.algebra.base.GeneralDBValueColumnBase;
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
import org.openrdf.sail.rdbms.exceptions.RdbmsException;
import org.openrdf.sail.rdbms.exceptions.UnsupportedRdbmsOperatorException;

/**
 * Constructs an SQL query from {@link GeneralDBSqlExpr}s and {@link GeneralDBFromItem}s.
 * 
 * @author Manos Karpathiotakis <mk@di.uoa.gr>
 * 
 */
public abstract class GeneralDBQueryBuilder {

	protected GeneralDBSqlQueryBuilder query;

	protected GeneralDBValueFactory vf;

	protected boolean usingHashTable;

	public GeneralDBQueryBuilder() {}

	public GeneralDBQueryBuilder(GeneralDBSqlQueryBuilder builder) {
		super();
		this.query = builder;
	}

	public void setValueFactory(GeneralDBValueFactory vf) {
		this.vf = vf;
	}

	public void setUsingHashTable(boolean usingHashTable) {
		this.usingHashTable = usingHashTable;
	}

	public void distinct() {
		query.distinct();
	}

	public GeneralDBQueryBuilder filter(GeneralDBColumnVar var, Value val)
	throws RdbmsException
	{
		String alias = var.getAlias();
		String column = var.getColumn();
		query.filter().and().columnEquals(alias, column, vf.getInternalId(val));
		return this;
	}

	public void from(GeneralDBFromItem from) throws RdbmsException, UnsupportedRdbmsOperatorException {
		from(query, from);
	}

	public List<?> getParameters() {
		return query.findParameters(new ArrayList<Object>());
	}

	public void limit(Long limit) {
		query.limit(limit);
	}

	public void offset(Long offset) {
		query.offset(offset);
	}

	public void orderBy(GeneralDBSqlExpr expr, boolean isAscending)
	throws UnsupportedRdbmsOperatorException
	{
		GeneralDBSqlExprBuilder orderBy = query.orderBy();
		dispatch(expr, orderBy);
		if (!isAscending) {
			orderBy.append(" DESC");
		}
	}

	public GeneralDBQueryBuilder select(GeneralDBSqlExpr expr)
	throws UnsupportedRdbmsOperatorException
	{
		dispatch(expr, query.select());

		return this;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT ");
		if (query.distinct) {
			sb.append("DISTINCT ");
		}
		if (query.select.isEmpty()) {
			sb.append("*");
		}
		else {
			sb.append(query.select.toSql());
		}
		if (query.from != null) {
			sb.append("\nFROM ").append(query.from.getFromClause());
			if (!query.from.on().isEmpty()) {
				sb.append("\nWHERE ");
				sb.append(query.from.on().toSql());
			}
		}
		sb.append(query.group);
		if (query.union != null && !query.union.isEmpty()) {
			sb.append("\nUNION ALL ");
			sb.append(query.union.toString());
		}
		if (!query.order.isEmpty()) {
			sb.append("\nORDER BY ").append(query.order.toSql());
		}
		if (query.limit != null) {
			// For some reason, PostgreSQL does not accept full Java long values as 
			// values for the limit clause (despite it being within BigInt value range).
			// Workaround is to use PgSql-specific "LIMIT ALL" syntax.
			if (Long.MAX_VALUE == query.limit) {
				sb.append("\nLIMIT ALL");
			}
			else {
				sb.append("\nLIMIT ").append(query.limit);
			}
		}
		if (query.offset != null) {
			sb.append("\nOFFSET ").append(query.offset);
		}
		return sb.toString();
	}	

	protected void append(GeneralDBBNodeColumn var, GeneralDBSqlExprBuilder filter) {
		String alias = getBNodeAlias(var.getRdbmsVar());
		filter.column(alias, "value");
	}

	protected void append(GeneralDBDatatypeColumn var, GeneralDBSqlExprBuilder filter) {
		if (var.getRdbmsVar().isResource()) {
			filter.appendNull();
		}
		else {
			String alias = getDatatypeAlias(var.getRdbmsVar());
			filter.column(alias, "value");
		}
	}

	protected void append(GeneralDBDateTimeColumn var, GeneralDBSqlExprBuilder filter) {
		if (var.getRdbmsVar().isResource()) {
			filter.appendNull();
		}
		else {
			String alias = getDateTimeAlias(var.getRdbmsVar());
			filter.column(alias, "value");
		}
	}

	protected void append(GeneralDBDoubleValue expr, GeneralDBSqlExprBuilder filter) {
		filter.appendNumeric(expr.getValue());
	}

	protected void append(GeneralDBFalseValue expr, GeneralDBSqlExprBuilder filter) {
		filter.appendBoolean(false);
	}

	protected void append(GeneralDBHashColumn var, GeneralDBSqlExprBuilder filter) {
		if (usingHashTable) {
			String alias = getHashAlias(var.getRdbmsVar());
			filter.column(alias, "value");
		}
		else {
			filter.column(var.getAlias(), var.getColumn());
		}
	}

	protected void append(GeneralDBIdColumn expr, GeneralDBSqlExprBuilder filter) {
		filter.column(expr.getAlias(), expr.getColumn());
	}

	protected abstract void append(GeneralDBLabelColumn var, GeneralDBSqlExprBuilder filter);

	protected void append(GeneralDBLongLabelColumn var, GeneralDBSqlExprBuilder filter) {
		if (var.getRdbmsVar().isResource()) {
			filter.appendNull();
		}
		else {
			String alias = getLongLabelAlias(var.getRdbmsVar());
			filter.column(alias, "value");
		}
	}

	protected void append(GeneralDBLanguageColumn var, GeneralDBSqlExprBuilder filter) {
		if (var.getRdbmsVar().isResource()) {
			filter.appendNull();
		}
		else {
			String alias = getLanguageAlias(var.getRdbmsVar());
			filter.column(alias, "value");
		}
	}

	protected void append(GeneralDBLongURIColumn uri, GeneralDBSqlExprBuilder filter) {
		GeneralDBColumnVar var = uri.getRdbmsVar();
		String alias = getLongURIAlias(var);
		filter.column(alias, "value");
	}

	protected void append(GeneralDBNumberValue expr, GeneralDBSqlExprBuilder filter) {
		filter.number(expr.getValue());
	}

	protected void append(GeneralDBNumericColumn var, GeneralDBSqlExprBuilder filter) {
		if (var.getRdbmsVar().isResource()) {
			filter.appendNull();
		}
		else {
			String alias = getNumericAlias(var.getRdbmsVar());
			filter.column(alias, "value");
		}
	}

	protected void append(GeneralDBRefIdColumn expr, GeneralDBSqlExprBuilder filter) {
		filter.column(expr.getAlias(), expr.getColumn());
	}

	protected void append(GeneralDBSqlAbs expr, GeneralDBSqlExprBuilder filter)
			throws UnsupportedRdbmsOperatorException
			{
		GeneralDBSqlBracketBuilder abs = filter.abs();
		dispatch(expr.getArg(), (GeneralDBSqlExprBuilder) abs);
		abs.close();
			}

	protected void append(GeneralDBSqlAnd expr, GeneralDBSqlExprBuilder filter)
			throws UnsupportedRdbmsOperatorException
			{
		dispatch(expr.getLeftArg(), filter);
		filter.and();
		dispatch(expr.getRightArg(), filter);
			}	

	protected void append(GeneralDBSqlCase expr, GeneralDBSqlExprBuilder filter)
	throws UnsupportedRdbmsOperatorException
	{
		GeneralDBSqlCaseBuilder caseExpr = filter.caseBegin();
		for (GeneralDBSqlCase.Entry e : expr.getEntries()) {
			caseExpr.when();
			dispatch(e.getCondition(), filter);
			caseExpr.then();
			dispatch(e.getResult(), filter);
		}
		caseExpr.end();
	}

	protected void append(GeneralDBSqlCast expr, GeneralDBSqlExprBuilder filter)
	throws UnsupportedRdbmsOperatorException
	{
		GeneralDBSqlCastBuilder cast = filter.cast(expr.getType());
		dispatch(expr.getArg(), (GeneralDBSqlExprBuilder) cast);
		cast.close();
	}

	protected void append(GeneralDBSqlCompare expr, GeneralDBSqlExprBuilder filter)
	throws UnsupportedRdbmsOperatorException
	{
		dispatch(expr.getLeftArg(), filter);
		filter.appendOperator(expr.getOperator());
		dispatch(expr.getRightArg(), filter);
	}

	protected void append(GeneralDBSqlConcat expr, GeneralDBSqlExprBuilder filter)
	throws UnsupportedRdbmsOperatorException
	{
		GeneralDBSqlBracketBuilder open = filter.open();
		dispatch(expr.getLeftArg(), (GeneralDBSqlExprBuilder) open);
		open.concat();
		dispatch(expr.getRightArg(), (GeneralDBSqlExprBuilder) open);
		open.close();
	}

	protected void append(GeneralDBSqlEq expr, GeneralDBSqlExprBuilder filter)
	throws UnsupportedRdbmsOperatorException
	{
		dispatch(expr.getLeftArg(), filter);
		filter.eq();
		dispatch(expr.getRightArg(), filter);
	}

	protected abstract void append(GeneralDBSqlIsNull expr, GeneralDBSqlExprBuilder filter)
			throws UnsupportedRdbmsOperatorException;

	protected void append(GeneralDBSqlLike expr, GeneralDBSqlExprBuilder filter)
	throws UnsupportedRdbmsOperatorException
	{
		dispatch(expr.getLeftArg(), filter);
		filter.like();
		dispatch(expr.getRightArg(), filter);
	}

	protected void append(GeneralDBSqlLowerCase expr, GeneralDBSqlExprBuilder filter)
	throws UnsupportedRdbmsOperatorException
	{
		GeneralDBSqlBracketBuilder lower = filter.lowerCase();
		dispatch(expr.getArg(), (GeneralDBSqlExprBuilder) lower);
		lower.close();
	}

	protected void append(GeneralDBSqlMathExpr expr, GeneralDBSqlExprBuilder filter)
	throws UnsupportedRdbmsOperatorException
	{
		dispatch(expr.getLeftArg(), filter);
		filter.math(expr.getOperator());
		dispatch(expr.getRightArg(), filter);
	}

	protected abstract void append(GeneralDBSqlNot expr, GeneralDBSqlExprBuilder filter)
			throws UnsupportedRdbmsOperatorException;

	protected abstract void append(GeneralDBSqlNull expr, GeneralDBSqlExprBuilder filter);

	protected void append(GeneralDBSqlOr expr, GeneralDBSqlExprBuilder filter)
	throws UnsupportedRdbmsOperatorException
	{
		GeneralDBSqlBracketBuilder open = filter.open();
		dispatch(expr.getLeftArg(), (GeneralDBSqlExprBuilder) open);
		open.or();
		dispatch(expr.getRightArg(), (GeneralDBSqlExprBuilder) open);
		open.close();
	}

	protected void append(GeneralDBSqlRegex expr, GeneralDBSqlExprBuilder filter)
	throws UnsupportedRdbmsOperatorException
	{
		GeneralDBSqlRegexBuilder regex = filter.regex();
		dispatch(expr.getArg(), regex.value());
		dispatch(expr.getPatternArg(), regex.pattern());
		GeneralDBSqlExpr flags = expr.getFlagsArg();
		if (flags != null) {
			dispatch(flags, regex.flags());
		}
		regex.close();
	}

	protected void append(GeneralDBSqlShift expr, GeneralDBSqlExprBuilder filter)
	throws UnsupportedRdbmsOperatorException
	{
		GeneralDBSqlBracketBuilder mod = filter.mod(expr.getRange());
		GeneralDBSqlBracketBuilder open = mod.open();
		dispatch(expr.getArg(), (GeneralDBSqlExprBuilder) open);
		open.rightShift(expr.getRightShift());
		open.close();
		mod.plus(expr.getRange());
		mod.close();
	}

	protected void append(GeneralDBStringValue expr, GeneralDBSqlExprBuilder filter) {
		filter.varchar(expr.getValue());
	}

	protected void append(GeneralDBTrueValue expr, GeneralDBSqlExprBuilder filter) {
		filter.appendBoolean(true);
	}

	protected void append(GeneralDBURIColumn uri, GeneralDBSqlExprBuilder filter) {
		GeneralDBColumnVar var = uri.getRdbmsVar();
		String alias = getURIAlias(var);
		filter.column(alias, "value");
	}

	protected void dispatch(GeneralDBSqlExpr expr, GeneralDBSqlExprBuilder filter)
	throws UnsupportedRdbmsOperatorException
	{
		if (expr instanceof GeneralDBValueColumnBase) {
			dispatchValueColumnBase((GeneralDBValueColumnBase)expr, filter);
		}
		else if (expr instanceof GeneralDBIdColumn) {
			append((GeneralDBIdColumn)expr, filter);
		}
		else if (expr instanceof GeneralDBSqlConstant<?>) {
			dispatchSqlConstant((GeneralDBSqlConstant<?>)expr, filter);
		}
		else if (expr instanceof UnaryGeneralDBOperator) {
			dispatchUnarySqlOperator((UnaryGeneralDBOperator)expr, filter);
		}
		else if (expr instanceof BinaryGeneralDBOperator) {
			dispatchBinarySqlOperator((BinaryGeneralDBOperator)expr, filter);
		}
		//XXX st_relate!
		else if (expr instanceof TripleGeneralDBOperator) {
			dispatchTripleSqlOperator((TripleGeneralDBOperator)expr, filter);
		}
		else {
			dispatchOther(expr, filter);
		}
	}

	protected void dispatchBinarySqlOperator(BinaryGeneralDBOperator expr, GeneralDBSqlExprBuilder filter)
	throws UnsupportedRdbmsOperatorException
	{
		if (expr instanceof GeneralDBSqlAnd) {
			append((GeneralDBSqlAnd)expr, filter);
		}
		else if (expr instanceof GeneralDBSqlEq) {
			append((GeneralDBSqlEq)expr, filter);
		}
		else if (expr instanceof GeneralDBSqlOr) {
			append((GeneralDBSqlOr)expr, filter);
		}
		else if (expr instanceof GeneralDBSqlCompare) {
			append((GeneralDBSqlCompare)expr, filter);
		}
		else if (expr instanceof GeneralDBSqlRegex) {
			append((GeneralDBSqlRegex)expr, filter);
		}
		else if (expr instanceof GeneralDBSqlConcat) {
			append((GeneralDBSqlConcat)expr, filter);
		}
		else if (expr instanceof GeneralDBSqlMathExpr) {
			append((GeneralDBSqlMathExpr)expr, filter);
		}
		else if (expr instanceof GeneralDBSqlLike) {
			append((GeneralDBSqlLike)expr, filter);
		}
		/**
		 * my additions
		 * FIXME
		 */
		//Relationships - boolean - stSPARQL
		else if (expr instanceof GeneralDBSqlCrosses) {
			append((GeneralDBSqlCrosses)expr, filter);
		}
		else if (expr instanceof GeneralDBSqlIntersects) {
			append((GeneralDBSqlIntersects)expr, filter);
		}
		else if (expr instanceof GeneralDBSqlContains) {
			append((GeneralDBSqlContains)expr, filter);
		}
		else if (expr instanceof GeneralDBSqlEqualsSpatial) {
			append((GeneralDBSqlEqualsSpatial)expr, filter);
		}
		else if (expr instanceof GeneralDBSqlWithin) {
			append((GeneralDBSqlWithin)expr, filter);
		}
		else if (expr instanceof GeneralDBSqlTouches) {
			append((GeneralDBSqlTouches)expr, filter);
		}
		else if (expr instanceof GeneralDBSqlDisjoint) {
			append((GeneralDBSqlDisjoint)expr, filter);
		}
		else if (expr instanceof GeneralDBSqlOverlaps) {
			append((GeneralDBSqlOverlaps)expr, filter);
		}
		else if (expr instanceof GeneralDBSqlLeft) {
			append((GeneralDBSqlLeft)expr, filter);
		}
		else if (expr instanceof GeneralDBSqlRight) {
			append((GeneralDBSqlRight)expr, filter);
		}
		else if (expr instanceof GeneralDBSqlAbove) {
			append((GeneralDBSqlAbove)expr, filter);
		}
		else if (expr instanceof GeneralDBSqlBelow) {
			append((GeneralDBSqlBelow)expr, filter);
		}
		else if (expr instanceof GeneralDBSqlMbbIntersects) {
			append((GeneralDBSqlMbbIntersects)expr, filter);
		}
		else if (expr instanceof GeneralDBSqlMbbWithin) {
			append((GeneralDBSqlMbbWithin)expr, filter);
		}
		else if (expr instanceof GeneralDBSqlMbbContains) {
			append((GeneralDBSqlMbbContains)expr, filter);
		}
		else if (expr instanceof GeneralDBSqlMbbEquals) {
			append((GeneralDBSqlMbbEquals)expr, filter);
		}
		//GeoSPARQL
		//Simple Features
		else if (expr instanceof GeneralDBSqlSF_Contains) {
			append((GeneralDBSqlSF_Contains)expr, filter);
		}
		else if (expr instanceof GeneralDBSqlSF_Crosses) {
			append((GeneralDBSqlSF_Crosses)expr, filter);
		}
		else if (expr instanceof GeneralDBSqlSF_Disjoint) {
			append((GeneralDBSqlSF_Disjoint)expr, filter);
		}
		else if (expr instanceof GeneralDBSqlSF_Equals) {
			append((GeneralDBSqlSF_Equals)expr, filter);
		}
		else if (expr instanceof GeneralDBSqlSF_Intersects) {
			append((GeneralDBSqlSF_Intersects)expr, filter);
		}
		else if (expr instanceof GeneralDBSqlSF_Overlaps) {
			append((GeneralDBSqlSF_Overlaps)expr, filter);
		}
		else if (expr instanceof GeneralDBSqlSF_Touches) {
			append((GeneralDBSqlSF_Touches)expr, filter);
		}
		else if (expr instanceof GeneralDBSqlSF_Within) {
			append((GeneralDBSqlSF_Within)expr, filter);
		}
		//RCC8
		else if (expr instanceof GeneralDBSqlRCC8_Dc) {
			append((GeneralDBSqlRCC8_Dc)expr, filter);
		}
		else if (expr instanceof GeneralDBSqlRCC8_Ec) {
			append((GeneralDBSqlRCC8_Ec)expr, filter);
		}
		else if (expr instanceof GeneralDBSqlRCC8_Eq) {
			append((GeneralDBSqlRCC8_Eq)expr, filter);
		}
		else if (expr instanceof GeneralDBSqlRCC8_Ntpp) {
			append((GeneralDBSqlRCC8_Ntpp)expr, filter);
		}
		else if (expr instanceof GeneralDBSqlRCC8_Ntppi) {
			append((GeneralDBSqlRCC8_Ntppi)expr, filter);
		}
		else if (expr instanceof GeneralDBSqlRCC8_Po) {
			append((GeneralDBSqlRCC8_Po)expr, filter);
		}
		else if (expr instanceof GeneralDBSqlRCC8_Tpp) {
			append((GeneralDBSqlRCC8_Tpp)expr, filter);
		}
		else if (expr instanceof GeneralDBSqlRCC8_Tppi) {
			append((GeneralDBSqlRCC8_Tppi)expr, filter);
		}
		//Egenhofer
		else if (expr instanceof GeneralDBSqlEgenhofer_Contains) {
			append((GeneralDBSqlEgenhofer_Contains)expr, filter);
		}
		else if (expr instanceof GeneralDBSqlEgenhofer_CoveredBy) {
			append((GeneralDBSqlEgenhofer_CoveredBy)expr, filter);
		}
		else if (expr instanceof GeneralDBSqlEgenhofer_Covers) {
			append((GeneralDBSqlEgenhofer_Covers)expr, filter);
		}
		else if (expr instanceof GeneralDBSqlEgenhofer_Disjoint) {
			append((GeneralDBSqlEgenhofer_Disjoint)expr, filter);
		}
		else if (expr instanceof GeneralDBSqlEgenhofer_Equals) {
			append((GeneralDBSqlEgenhofer_Equals)expr, filter);
		}
		else if (expr instanceof GeneralDBSqlEgenhofer_Inside) {
			append((GeneralDBSqlEgenhofer_Inside)expr, filter);
		}
		else if (expr instanceof GeneralDBSqlEgenhofer_Meet) {
			append((GeneralDBSqlEgenhofer_Meet)expr, filter);
		}
		else if (expr instanceof GeneralDBSqlEgenhofer_Overlap) {
			append((GeneralDBSqlEgenhofer_Overlap)expr, filter);
		}
		//Constructs
		else if (expr instanceof GeneralDBSqlGeoUnion) {
			append((GeneralDBSqlGeoUnion)expr, filter);
		}
		else if (expr instanceof GeneralDBSqlGeoTransform) {
			append((GeneralDBSqlGeoTransform)expr, filter);
		}
		else if (expr instanceof GeneralDBSqlGeoIntersection) {
			append((GeneralDBSqlGeoIntersection)expr, filter);
		}
		else if (expr instanceof GeneralDBSqlGeoDifference) {
			append((GeneralDBSqlGeoDifference)expr, filter);
		}
		else if (expr instanceof GeneralDBSqlGeoSymDifference) {
			append((GeneralDBSqlGeoSymDifference)expr, filter);
		}
		else if (expr instanceof GeneralDBSqlDiffDateTime) {
			append((GeneralDBSqlDiffDateTime)expr, filter);
		}
		/* PostGIS Construct functions */
		else if (expr instanceof GeneralDBSqlST_MakeLine) {
			append((GeneralDBSqlST_MakeLine)expr, filter);
		}
		/* PostGIS Construct functions */
		/**
		 * end of my addition
		 */
		else {
			throw unsupported(expr);
		}
	}

	protected void dispatchTripleSqlOperator(TripleGeneralDBOperator expr, GeneralDBSqlExprBuilder filter) throws UnsupportedRdbmsOperatorException
	{
		if (expr instanceof GeneralDBSqlRelate) {
			append((GeneralDBSqlRelate)expr, filter);
		}
		//Metrics
		else if (expr instanceof GeneralDBSqlGeoDistance) {
			append((GeneralDBSqlGeoDistance)expr, filter);
		}
		//Construct
		else if (expr instanceof GeneralDBSqlGeoBuffer) {
			append((GeneralDBSqlGeoBuffer)expr, filter);
		}
		else
		{
			throw unsupported(expr);
		}
	}

	protected void dispatchOther(GeneralDBSqlExpr expr, GeneralDBSqlExprBuilder filter)
			throws UnsupportedRdbmsOperatorException
			{
		if (expr instanceof GeneralDBSqlCase) {
			append((GeneralDBSqlCase)expr, filter);
		}
		else {
			throw unsupported(expr);
		}
			}

	protected void dispatchSqlConstant(GeneralDBSqlConstant<?> expr, GeneralDBSqlExprBuilder filter)
			throws UnsupportedRdbmsOperatorException
			{
		if (expr instanceof GeneralDBDoubleValue) {
			append((GeneralDBDoubleValue)expr, filter);
		}
		else if (expr instanceof GeneralDBFalseValue) {
			append((GeneralDBFalseValue)expr, filter);
		}
		else if (expr instanceof GeneralDBTrueValue) {
			append((GeneralDBTrueValue)expr, filter);
		}
		else if (expr instanceof GeneralDBNumberValue) {
			append((GeneralDBNumberValue)expr, filter);
		}
		else if (expr instanceof GeneralDBSqlNull) {
			append((GeneralDBSqlNull)expr, filter);
		}
		else if (expr instanceof GeneralDBStringValue) {
			append((GeneralDBStringValue)expr, filter);
		}
		else {
			throw unsupported(expr);
		}
			}

	protected void dispatchUnarySqlOperator(UnaryGeneralDBOperator expr, GeneralDBSqlExprBuilder filter)
	throws UnsupportedRdbmsOperatorException
	{
		if (expr instanceof GeneralDBSqlAbs) {
			append((GeneralDBSqlAbs)expr, filter);
		}
		else if (expr instanceof GeneralDBSqlIsNull) {
			append((GeneralDBSqlIsNull)expr, filter);
		}
		else if (expr instanceof GeneralDBSqlNot) {
			append((GeneralDBSqlNot)expr, filter);
		}
		else if (expr instanceof GeneralDBSqlShift) {
			append((GeneralDBSqlShift)expr, filter);
		}
		else if (expr instanceof GeneralDBSqlLowerCase) {
			append((GeneralDBSqlLowerCase)expr, filter);
		}
		else if (expr instanceof GeneralDBSqlCast) {
			append((GeneralDBSqlCast)expr, filter);
		}
		//Constructs
		else if (expr instanceof GeneralDBSqlGeoEnvelope) {
			append((GeneralDBSqlGeoEnvelope)expr, filter);
		}
		else if (expr instanceof GeneralDBSqlGeoConvexHull) {
			append((GeneralDBSqlGeoConvexHull)expr, filter);
		}
		else if (expr instanceof GeneralDBSqlGeoBoundary) {
			append((GeneralDBSqlGeoBoundary)expr, filter);
		}
		else if (expr instanceof GeneralDBSqlST_Centroid) {
			append((GeneralDBSqlST_Centroid)expr, filter);
		}
		//Metrics
		else if (expr instanceof GeneralDBSqlGeoArea) {
			append((GeneralDBSqlGeoArea)expr, filter);
		}
		//Properties
		else if (expr instanceof GeneralDBSqlGeoDimension) {
			append((GeneralDBSqlGeoDimension)expr, filter);
		}
		else if (expr instanceof GeneralDBSqlGeoGeometryType) {
			append((GeneralDBSqlGeoGeometryType)expr, filter);
		}
		else if (expr instanceof GeneralDBSqlGeoAsText) {
			append((GeneralDBSqlGeoAsText)expr, filter);
		}
		else if (expr instanceof GeneralDBSqlGeoAsGML) {
			append((GeneralDBSqlGeoAsGML)expr, filter);
		}
		else if (expr instanceof GeneralDBSqlGeoSrid) {
			append((GeneralDBSqlGeoSrid)expr, filter);
			
		} else if (expr instanceof GeneralDBSqlGeoSPARQLSrid) {
			append((GeneralDBSqlGeoSPARQLSrid)expr, filter);
		}
		else if (expr instanceof GeneralDBSqlGeoIsEmpty) {
			append((GeneralDBSqlGeoIsEmpty)expr, filter);
		}
		else if (expr instanceof GeneralDBSqlGeoIsSimple) {
			append((GeneralDBSqlGeoIsSimple)expr, filter);
		}
		////
		else {
			throw unsupported(expr);
		}
	}

	protected void dispatchValueColumnBase(GeneralDBValueColumnBase expr, GeneralDBSqlExprBuilder filter)
			throws UnsupportedRdbmsOperatorException
			{
		if (expr instanceof GeneralDBBNodeColumn) {
			append((GeneralDBBNodeColumn)expr, filter);
		}
		else if (expr instanceof GeneralDBDatatypeColumn) {
			append((GeneralDBDatatypeColumn)expr, filter);
		}
		else if (expr instanceof GeneralDBHashColumn) {
			append((GeneralDBHashColumn)expr, filter);
		}
		else if (expr instanceof GeneralDBDateTimeColumn) {
			append((GeneralDBDateTimeColumn)expr, filter);
		}
		else if (expr instanceof GeneralDBLabelColumn) {
			append((GeneralDBLabelColumn)expr, filter);
		}
		else if (expr instanceof GeneralDBLongLabelColumn) {
			append((GeneralDBLongLabelColumn)expr, filter);
		}
		else if (expr instanceof GeneralDBLongURIColumn) {
			append((GeneralDBLongURIColumn)expr, filter);
		}
		else if (expr instanceof GeneralDBLanguageColumn) {
			append((GeneralDBLanguageColumn)expr, filter);
		}
		else if (expr instanceof GeneralDBNumericColumn) {
			append((GeneralDBNumericColumn)expr, filter);
		}
		else if (expr instanceof GeneralDBURIColumn) {
			append((GeneralDBURIColumn)expr, filter);
		}
		else if (expr instanceof GeneralDBRefIdColumn) {
			append((GeneralDBRefIdColumn)expr, filter);
		}
		else {
			throw unsupported(expr);
		}
			}

	protected void from(GeneralDBSqlQueryBuilder subquery, GeneralDBFromItem item)
			throws RdbmsException, UnsupportedRdbmsOperatorException
			{
		assert !item.isLeft() : item;
		String alias = item.getAlias();
		if (item instanceof GeneralDBJoinItem) {
			String tableName = ((GeneralDBJoinItem)item).getTableName();
			subJoinAndFilter(subquery.from(tableName, alias), item);
		}
		else {
			subJoinAndFilter(subquery.from(alias), item);
		}
			}

	protected String getBNodeAlias(GeneralDBColumnVar var) {
		return "b" + getDBName(var);
	}

	protected String getDatatypeAlias(GeneralDBColumnVar var) {
		return "d" + getDBName(var);
	}

	protected String getDateTimeAlias(GeneralDBColumnVar var) {
		return "t" + getDBName(var);
	}

	protected String getDBName(GeneralDBColumnVar var) {
		String name = var.getName();
		if (name.indexOf('-') >= 0)
			return name.replace('-', '_');
		return "_" + name; // might be a keyword otherwise
	}

	protected String getHashAlias(GeneralDBColumnVar var) {
		return "h" + getDBName(var);
	}

	protected String getLabelAlias(GeneralDBColumnVar var) {
		return "l" + getDBName(var);
	}

	protected String getLongLabelAlias(GeneralDBColumnVar var) {
		return "ll" + getDBName(var);
	}

	protected String getLongURIAlias(GeneralDBColumnVar var) {
		return "lu" + getDBName(var);
	}

	protected String getLanguageAlias(GeneralDBColumnVar var) {
		return "g" + getDBName(var);
	}

	protected String getNumericAlias(GeneralDBColumnVar var) {
		return "n" + getDBName(var);
	}

	protected String getURIAlias(GeneralDBColumnVar var) {
		return "u" + getDBName(var);
	}

	protected void join(GeneralDBSqlJoinBuilder query, GeneralDBFromItem join)
			throws RdbmsException, UnsupportedRdbmsOperatorException
			{
		String alias = join.getAlias();
		if (join instanceof GeneralDBJoinItem) {
			String tableName = ((GeneralDBJoinItem)join).getTableName();
			if (join.isLeft()) {
				subJoinAndFilter(query.leftjoin(tableName, alias), join);
			}
			else {
				subJoinAndFilter(query.join(tableName, alias), join);
			}
		}
		else {
			if (join.isLeft()) {
				subJoinAndFilter(query.leftjoin(alias), join);
			}
			else {
				subJoinAndFilter(query.join(alias), join);
			}
		}
			}

	protected abstract GeneralDBSqlJoinBuilder subJoinAndFilter(GeneralDBSqlJoinBuilder query, GeneralDBFromItem from)
			throws RdbmsException, UnsupportedRdbmsOperatorException;

	protected UnsupportedRdbmsOperatorException unsupported(Object object)
			throws UnsupportedRdbmsOperatorException
			{
		return new UnsupportedRdbmsOperatorException(object.toString());
			}

	//FIXME my addition from here on
	public abstract GeneralDBQueryBuilder construct(GeneralDBSqlExpr expr)
			throws UnsupportedRdbmsOperatorException;

	//Spatial Relationship Functions
	protected abstract void append(GeneralDBSqlIntersects expr, GeneralDBSqlExprBuilder filter)
			throws UnsupportedRdbmsOperatorException;
	
	protected abstract void append(GeneralDBSqlCrosses expr, GeneralDBSqlExprBuilder filter)
			throws UnsupportedRdbmsOperatorException;

	protected abstract void append(GeneralDBSqlContains expr, GeneralDBSqlExprBuilder filter)
			throws UnsupportedRdbmsOperatorException;

	protected abstract void append(GeneralDBSqlEqualsSpatial expr, GeneralDBSqlExprBuilder filter)
			throws UnsupportedRdbmsOperatorException;

	protected abstract void append(GeneralDBSqlWithin expr, GeneralDBSqlExprBuilder filter)
			throws UnsupportedRdbmsOperatorException;

	protected abstract void append(GeneralDBSqlTouches expr, GeneralDBSqlExprBuilder filter)
			throws UnsupportedRdbmsOperatorException;

	protected abstract void append(GeneralDBSqlOverlaps expr, GeneralDBSqlExprBuilder filter)
			throws UnsupportedRdbmsOperatorException;

	protected abstract void append(GeneralDBSqlDisjoint expr, GeneralDBSqlExprBuilder filter)
			throws UnsupportedRdbmsOperatorException;

	protected abstract void append(GeneralDBSqlRelate expr, GeneralDBSqlExprBuilder filter)
			throws UnsupportedRdbmsOperatorException;

	protected abstract void append(GeneralDBSqlLeft expr, GeneralDBSqlExprBuilder filter)
			throws UnsupportedRdbmsOperatorException;

	protected abstract void append(GeneralDBSqlRight expr, GeneralDBSqlExprBuilder filter)
			throws UnsupportedRdbmsOperatorException;

	protected abstract void append(GeneralDBSqlAbove expr, GeneralDBSqlExprBuilder filter)
			throws UnsupportedRdbmsOperatorException;

	protected abstract void append(GeneralDBSqlBelow expr, GeneralDBSqlExprBuilder filter)
			throws UnsupportedRdbmsOperatorException;
	protected abstract void append(GeneralDBSqlMbbIntersects expr, GeneralDBSqlExprBuilder filter)
			throws UnsupportedRdbmsOperatorException;
	protected abstract void append(GeneralDBSqlMbbWithin expr, GeneralDBSqlExprBuilder filter)
			throws UnsupportedRdbmsOperatorException;
	protected abstract void append(GeneralDBSqlMbbContains expr, GeneralDBSqlExprBuilder filter)
			throws UnsupportedRdbmsOperatorException;
	protected abstract void append(GeneralDBSqlMbbEquals expr, GeneralDBSqlExprBuilder filter)
			throws UnsupportedRdbmsOperatorException;

	//GeoSPARQL - Spatial Relationship Functions
	//Simple Features
	protected abstract void append(GeneralDBSqlSF_Contains expr, GeneralDBSqlExprBuilder filter)
			throws UnsupportedRdbmsOperatorException;

	protected abstract void append(GeneralDBSqlSF_Crosses expr, GeneralDBSqlExprBuilder filter)
			throws UnsupportedRdbmsOperatorException;

	protected abstract void append(GeneralDBSqlSF_Disjoint expr, GeneralDBSqlExprBuilder filter)
			throws UnsupportedRdbmsOperatorException;

	protected abstract void append(GeneralDBSqlSF_Equals expr, GeneralDBSqlExprBuilder filter)
			throws UnsupportedRdbmsOperatorException;

	protected abstract void append(GeneralDBSqlSF_Intersects expr, GeneralDBSqlExprBuilder filter)
			throws UnsupportedRdbmsOperatorException;

	protected abstract void append(GeneralDBSqlSF_Overlaps expr, GeneralDBSqlExprBuilder filter)
			throws UnsupportedRdbmsOperatorException;

	protected abstract void append(GeneralDBSqlSF_Touches expr, GeneralDBSqlExprBuilder filter)
			throws UnsupportedRdbmsOperatorException;

	protected abstract void append(GeneralDBSqlSF_Within expr, GeneralDBSqlExprBuilder filter)
			throws UnsupportedRdbmsOperatorException;

	//Egenhofer
	protected abstract void append(GeneralDBSqlEgenhofer_CoveredBy expr, GeneralDBSqlExprBuilder filter)
			throws UnsupportedRdbmsOperatorException;

	protected abstract void append(GeneralDBSqlEgenhofer_Covers expr, GeneralDBSqlExprBuilder filter)
			throws UnsupportedRdbmsOperatorException;

	protected abstract void append(GeneralDBSqlEgenhofer_Contains expr, GeneralDBSqlExprBuilder filter)
			throws UnsupportedRdbmsOperatorException;

	protected abstract void append(GeneralDBSqlEgenhofer_Disjoint expr, GeneralDBSqlExprBuilder filter)
			throws UnsupportedRdbmsOperatorException;

	protected abstract void append(GeneralDBSqlEgenhofer_Equals expr, GeneralDBSqlExprBuilder filter)
			throws UnsupportedRdbmsOperatorException;

	protected abstract void append(GeneralDBSqlEgenhofer_Inside expr, GeneralDBSqlExprBuilder filter)
			throws UnsupportedRdbmsOperatorException;

	protected abstract void append(GeneralDBSqlEgenhofer_Meet expr, GeneralDBSqlExprBuilder filter)
			throws UnsupportedRdbmsOperatorException;

	protected abstract void append(GeneralDBSqlEgenhofer_Overlap expr, GeneralDBSqlExprBuilder filter)
			throws UnsupportedRdbmsOperatorException;

	//RCC8
	protected abstract void append(GeneralDBSqlRCC8_Dc expr, GeneralDBSqlExprBuilder filter)
			throws UnsupportedRdbmsOperatorException;

	protected abstract void append(GeneralDBSqlRCC8_Eq expr, GeneralDBSqlExprBuilder filter)
			throws UnsupportedRdbmsOperatorException;

	protected abstract void append(GeneralDBSqlRCC8_Ec expr, GeneralDBSqlExprBuilder filter)
			throws UnsupportedRdbmsOperatorException;

	protected abstract void append(GeneralDBSqlRCC8_Po expr, GeneralDBSqlExprBuilder filter)
			throws UnsupportedRdbmsOperatorException;

	protected abstract void append(GeneralDBSqlRCC8_Tppi expr, GeneralDBSqlExprBuilder filter)
			throws UnsupportedRdbmsOperatorException;

	protected abstract void append(GeneralDBSqlRCC8_Tpp expr, GeneralDBSqlExprBuilder filter)
			throws UnsupportedRdbmsOperatorException;

	protected abstract void append(GeneralDBSqlRCC8_Ntpp expr, GeneralDBSqlExprBuilder filter)
			throws UnsupportedRdbmsOperatorException;

	protected abstract void append(GeneralDBSqlRCC8_Ntppi expr, GeneralDBSqlExprBuilder filter)
			throws UnsupportedRdbmsOperatorException;

	//Spatial Construct Functions
	protected abstract void append(GeneralDBSqlGeoUnion expr, GeneralDBSqlExprBuilder filter)
			throws UnsupportedRdbmsOperatorException;

	protected abstract void append(GeneralDBSqlGeoBuffer expr, GeneralDBSqlExprBuilder filter)
			throws UnsupportedRdbmsOperatorException;

	protected abstract void append(GeneralDBSqlGeoTransform expr, GeneralDBSqlExprBuilder filter)
			throws UnsupportedRdbmsOperatorException;

	protected abstract void append(GeneralDBSqlGeoEnvelope expr, GeneralDBSqlExprBuilder filter)
			throws UnsupportedRdbmsOperatorException;

	protected abstract void append(GeneralDBSqlGeoConvexHull expr, GeneralDBSqlExprBuilder filter)
			throws UnsupportedRdbmsOperatorException;

	protected abstract void append(GeneralDBSqlGeoBoundary expr, GeneralDBSqlExprBuilder filter)
			throws UnsupportedRdbmsOperatorException;

	protected abstract void append(GeneralDBSqlGeoIntersection expr, GeneralDBSqlExprBuilder filter)
			throws UnsupportedRdbmsOperatorException;

	protected abstract void append(GeneralDBSqlGeoDifference expr, GeneralDBSqlExprBuilder filter)
			throws UnsupportedRdbmsOperatorException;

	protected abstract void append(GeneralDBSqlGeoSymDifference expr, GeneralDBSqlExprBuilder filter)
			throws UnsupportedRdbmsOperatorException;

	/* PostGIS Construct Functions */
	protected abstract void append(GeneralDBSqlST_MakeLine expr, GeneralDBSqlExprBuilder filter)
			throws UnsupportedRdbmsOperatorException;
	protected abstract void append(GeneralDBSqlST_Centroid expr, GeneralDBSqlExprBuilder filter)
			throws UnsupportedRdbmsOperatorException;
	/* PostGIS Construct Functions */
	
	/** Addition for datetime metric functions
	 * 
	 * @author George Garbis <ggarbis@di.uoa.gr>
	 * 
	 */
	protected abstract void append(GeneralDBSqlDiffDateTime expr, GeneralDBSqlExprBuilder filter)
			throws UnsupportedRdbmsOperatorException;
	/***/
	
	//Spatial Metric Functions
	protected abstract void append(GeneralDBSqlGeoDistance expr, GeneralDBSqlExprBuilder filter)
			throws UnsupportedRdbmsOperatorException;

	protected abstract void append(GeneralDBSqlGeoArea expr, GeneralDBSqlExprBuilder filter)
			throws UnsupportedRdbmsOperatorException;

	//Spatial Property Functions
	protected abstract void append(GeneralDBSqlGeoDimension expr, GeneralDBSqlExprBuilder filter)
			throws UnsupportedRdbmsOperatorException;

	protected abstract void append(GeneralDBSqlGeoGeometryType expr, GeneralDBSqlExprBuilder filter)
			throws UnsupportedRdbmsOperatorException;

	protected abstract void append(GeneralDBSqlGeoAsText expr, GeneralDBSqlExprBuilder filter)
			throws UnsupportedRdbmsOperatorException;

	protected abstract void append(GeneralDBSqlGeoAsGML expr, GeneralDBSqlExprBuilder filter)
			throws UnsupportedRdbmsOperatorException;
	
	protected abstract void append(GeneralDBSqlGeoSrid expr, GeneralDBSqlExprBuilder filter)
			throws UnsupportedRdbmsOperatorException;
	
	protected abstract void append(GeneralDBSqlGeoSPARQLSrid expr, GeneralDBSqlExprBuilder filter)
			throws UnsupportedRdbmsOperatorException;

	protected abstract void append(GeneralDBSqlGeoIsSimple expr, GeneralDBSqlExprBuilder filter)
			throws UnsupportedRdbmsOperatorException;

	protected abstract void append(GeneralDBSqlGeoIsEmpty expr, GeneralDBSqlExprBuilder filter)
			throws UnsupportedRdbmsOperatorException;


	/**
	 * 'helper' functions
	 */

	protected void appendMBB(GeneralDBLabelColumn var, GeneralDBSqlExprBuilder filter)
	{
		//I seriously doubt it will ever visit this case

		if (var.getRdbmsVar()==null || var.getRdbmsVar().isResource()) {
			filter.appendNull();

		}
		else {
			String alias = getLabelAlias(var.getRdbmsVar());

			filter.column(alias, "strdfgeo");

		}
	}

	protected abstract String appendWKT(GeneralDBSqlExpr expr, GeneralDBSqlExprBuilder filter) throws UnsupportedRdbmsOperatorException;

	protected void appendConstructFunction(GeneralDBSqlExpr constr, GeneralDBSqlExprBuilder filter) throws UnsupportedRdbmsOperatorException
	{
		if(constr instanceof GeneralDBSqlGeoUnion)
		{
			append((GeneralDBSqlGeoUnion)constr, filter);
		}
		else if(constr instanceof GeneralDBSqlGeoBuffer)
		{
			append((GeneralDBSqlGeoBuffer)constr, filter);
		}
		else if(constr instanceof GeneralDBSqlGeoTransform)
		{
			append((GeneralDBSqlGeoTransform)constr, filter);
		}
		else if(constr instanceof GeneralDBSqlGeoEnvelope)
		{
			append((GeneralDBSqlGeoEnvelope)constr, filter);
		}
		else if(constr instanceof GeneralDBSqlGeoConvexHull)
		{
			append((GeneralDBSqlGeoConvexHull)constr, filter);
		}
		else if(constr instanceof GeneralDBSqlGeoBoundary)
		{
			append((GeneralDBSqlGeoBoundary)constr, filter);
		}
		else if(constr instanceof GeneralDBSqlGeoIntersection)
		{
			append((GeneralDBSqlGeoIntersection)constr, filter);
		}
		else if(constr instanceof GeneralDBSqlGeoDifference)
		{
			append((GeneralDBSqlGeoDifference)constr, filter);
		}
		else if(constr instanceof GeneralDBSqlGeoSymDifference)
		{
			append((GeneralDBSqlGeoSymDifference)constr, filter);
		}
		else if(constr instanceof GeneralDBSqlGeoSymDifference)
		{
			append((GeneralDBSqlGeoSymDifference)constr, filter);
		}
		/* PostGIS functions */
		else if(constr instanceof GeneralDBSqlST_MakeLine)
		{
			append((GeneralDBSqlST_MakeLine)constr, filter);
		}
		else if(constr instanceof GeneralDBSqlST_Centroid)
		{
			append((GeneralDBSqlST_Centroid)constr, filter);
		}
		/* PostGIS functions */
	}

	protected void appendMetricFunction(GeneralDBSqlExpr constr, GeneralDBSqlExprBuilder filter) throws UnsupportedRdbmsOperatorException
	{
		if(constr instanceof GeneralDBSqlGeoDistance)
		{
			append((GeneralDBSqlGeoDistance)constr, filter);
		}
		else if(constr instanceof GeneralDBSqlGeoArea)
		{
			append((GeneralDBSqlGeoArea)constr, filter);
		}
	}

	/** Addition for datetime metric functions
	 * 
	 * @author George Garbis <ggarbis@di.uoa.gr>
	 * 
	 */
	protected void appendCastToEpoch(GeneralDBSqlExprBuilder filter)
	{
//		filter.epochCastBefore();
//		filter.epochCastAfter();
	}	
	/***/
	
	protected void appendCastToDouble(GeneralDBSqlExprBuilder filter)
	{
		filter.doubleCast();
	}
	
	

	//GeoSPARQL
	//XXX

	protected abstract void appendRelate(BinaryGeneralDBOperator expr, GeneralDBSqlExprBuilder filter, char[] intersectionPattern)
			throws UnsupportedRdbmsOperatorException;
}
