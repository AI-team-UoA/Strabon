/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.generaldb.evaluation;

import java.util.ArrayList;
import java.util.List;

import org.openrdf.query.algebra.MathExpr;
import org.openrdf.sail.generaldb.algebra.GeneralDBSqlCompare;

/**
 * Assemblies an SQL expression.
 * 
 * @author James Leigh
 * 
 */
public abstract class GeneralDBSqlExprBuilder {

	protected class Mark {

		public int length;

		public int size;

		public Mark(int length, int size) {
			this.length = length;
			this.size = size;
		}
	}

	protected static final String NULL = " NULL ";

	protected GeneralDBQueryBuilderFactory factory;

	protected List<Object> parameters = new ArrayList<Object>();

	protected StringBuilder where = new StringBuilder();

	public GeneralDBSqlExprBuilder(GeneralDBQueryBuilderFactory factory) {
		super();
		this.factory = factory;
	}

	public GeneralDBSqlBracketBuilder abs() {
		where.append(" ABS");
		return open();
	}

	public GeneralDBSqlExprBuilder and() {
		if (!isEmpty()) {
			where.append("\n AND ");
		}
		return this;
	}

	public GeneralDBSqlExprBuilder append(CharSequence sql) {
		where.append(sql);
		return this;
	}

	public abstract void appendBoolean(boolean booleanValue);

	public GeneralDBSqlExprBuilder appendNull() {
		where.append(getSqlNull());
		return this;
	}

	public abstract GeneralDBSqlExprBuilder appendNumeric(Number doubleValue);

	public void appendOperator(GeneralDBSqlCompare.Operator op) {
		switch (op) {
		case GE:
			where.append(" >= ");
			break;
		case GT:
			where.append(" > ");
			break;
		case LE:
			where.append(" <= ");
			break;
		case LT:
			where.append(" < ");
			break;
		}
	}

	public void as(String column) {
		where.append(" AS ").append(column);
	}

	public abstract GeneralDBSqlExprBuilder number(Number time) ;

	public GeneralDBSqlCaseBuilder caseBegin() {
		return new GeneralDBSqlCaseBuilder(this);
	}

	public GeneralDBSqlCastBuilder cast(int jdbcType) {
		return factory.createSqlCastBuilder(this, jdbcType);
	}

	public GeneralDBSqlExprBuilder column(String alias, String column) {
		where.append(alias).append(".").append(column);
		return this;
	}

	public GeneralDBSqlExprBuilder columnEquals(String alias, String column, Number id) {
		return column(alias, column).eq().number(id);
	}

	public GeneralDBSqlExprBuilder columnEquals(String alias, String column, String label) {
		return column(alias, column).eq().varchar(label);
	}

	public GeneralDBSqlExprBuilder columnIn(String alias, String column, Number[] ids) {
		if (ids.length == 1) {
			return columnEquals(alias, column, ids[0]);
		}
		GeneralDBSqlBracketBuilder open = open();
		for (int i = 0; i < ids.length; i++) {
			if (i > 0) {
				open.or();
			}
			open.column(alias, column);
			open.eq();
			open.number(ids[i]);
		}
		open.close();
		return this;
	}

	public GeneralDBSqlExprBuilder columnsEqual(String al1, String col1, String al2, String col2) {
		return column(al1, col1).eq().column(al2, col2);
	}

	public void concat() {
		append(" || ");
	}

	public GeneralDBSqlExprBuilder eq() {
		where.append(" = ");
		return this;
	}

	public List<Object> getParameters() {
		return parameters;
	}

	public boolean isEmpty() {
		return where.length() == 0;
	}

	public GeneralDBSqlExprBuilder isNotNull() {
		where.append(" IS NOT NULL ");
		return this;
	}

	public GeneralDBSqlExprBuilder isNull() {
		where.append(" IS NULL ");
		return this;
	}

	public void like() {
		where.append(" LIKE ");
	}

	public GeneralDBSqlBracketBuilder lowerCase() {
		where.append(" lower");
		return open();
	}

	public void math(MathExpr.MathOp op) {
		append(" ").append(op.getSymbol()).append(" ");
	}

	public GeneralDBSqlBracketBuilder mod(int value) {
		where.append(" MOD");
		GeneralDBSqlBracketBuilder open = open();
		open.setClosing("," + value + open.getClosing());
		return open;
	}

	public GeneralDBSqlBracketBuilder not() {
		where.append(" NOT");
		return open();
	}

	public GeneralDBSqlExprBuilder notEqual() {
		where.append(" <> ");
		return this;
	}

	public GeneralDBSqlBracketBuilder open() {
		return factory.createSqlBracketBuilder(this);
	}

	public GeneralDBSqlExprBuilder or() {
		append(" OR ");
		return this;
	}

	public void plus(int range) {
		where.append(" + " + range);
	}

	public GeneralDBSqlRegexBuilder regex() {
		return factory.createSqlRegexBuilder(this);
	}

	public void rightShift(int rightShift) {
		where.append(" >> " + rightShift);
	}

	public CharSequence toSql() {
		return where;
	}

	@Override
	public String toString() {
		return where.toString();
	}

	public abstract GeneralDBSqlExprBuilder varchar(String stringValue) ;

	public void addParameters(List<Object> params) {
		parameters.addAll(params);
	}

	protected String getSqlNull() {
		return NULL;
	}

	protected Mark mark() {
		return new Mark(where.length(), parameters.size());
	}

	protected void reset(Mark mark) {
		where.delete(mark.length, where.length());
		for (int i = parameters.size() - 1; i >= mark.size; i--) {
			parameters.remove(i);
		}
	}

	/**
	 * my additions
	 * FIXME
	 */
	public void appendFunction(String functionName)
	{
		where.append(functionName);
	}

	public void appendComma()
	{
		where.append(",");
	}

	public void openBracket()
	{
		where.append("(");
	}

	public void closeBracket()
	{
		where.append(")");
	}

	public void intersectsMBB() {

		//XXX
		//edw prepei na ginei allou eidous douleia!! oxi na mpei to mbbIntersects, 
		//alla na prostethei kati pou tha mou pei o kwstis
		where.append(" && ");
	}
	
	public void equalsMBB() {

		where.append(" = ");
	}

	public void containsMBB() {

		where.append(" ~ ");
	}

	public void insideMBB() {

		//den xerw akoma ti symbolo xreiazetai
		where.append(" @ ");
	}

	public void leftMBB() {
		where.append(" << ");
	}

	public void rightMBB() {
		where.append(" >> ");
	}

	public void aboveMBB() {
		where.append(" |>> ");
	}

	public void belowMBB() {
		where.append(" <<|");
	}

	public void doubleCast() {
		where.append(" :: DOUBLE PRECISION ");
	}
	
	public void keepSRID_part1() {
		where.append("CAST ( SUBSTRING(");
	}
	
	public void keepSRID_part2() {
		where.append(" , position('0' in ");
	}
	
	public void keepSRID_part3() {
		where.append(" ) + 2) AS integer) ) ");
	}

	//	public void overlap() {
	//
	//		//den xerw akoma ti symbolo xreiazetai
	//		where.append(" && ");
	//	}

	public void appendProjections(String relName, String attrName, String rawDimensions)
	{
		where.append("ST_Scale(");
		if(!rawDimensions.contains("4"))
		{
			where.append("ST_Force_3D(");
		}
		where.append(relName).append(".").append(attrName);
		if(!rawDimensions.contains("4"))
		{
			where.append(")");
		}

		for(int i = 1; i<4; i++)
		{
			where.append(",");
			if(rawDimensions.contains(""+i))
			{
				where.append("1");
			}
			else
			{
				where.append("0");
			}
		}

		where.append(")");
	}


	/**
	 * Used to enable projections along with spatial constructs. 
	 * This function constructs the first part of the GeneralDB statement needed
	 * @param dims
	 */
	public void initConstructProjection(ArrayList<Integer> dims)
	{
		where.append("ST_Scale(");
		if(!dims.contains(4))
		{
			where.append("ST_Force_3D(");
		}
	}

	public void endConstructProjection(ArrayList<Integer> dims)
	{

		if(!dims.contains(4))
		{
			where.append(")");
		}

		for(int i = 1; i<4; i++)
		{
			where.append(",");
			if(dims.contains(i))
			{
				where.append("1");
			}
			else
			{
				where.append("0");
			}
		}

		where.append(")");
	}

	//	public void appendChar(char c) {
	//		where.append(c);
	//	}
	/**
	 * end of my addition
	 */



}
