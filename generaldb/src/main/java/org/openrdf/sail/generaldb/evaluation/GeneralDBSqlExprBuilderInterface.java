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
 * @author Manos Karpathiotakis <mk@di.uoa.gr>
 * 
 */
public interface GeneralDBSqlExprBuilderInterface {
	public void appendBoolean(boolean booleanValue);

	public GeneralDBSqlExprBuilder appendNull();

	public abstract GeneralDBSqlExprBuilder appendNumeric(Number doubleValue);

	public void appendOperator(GeneralDBSqlCompare.Operator op);

	public void as(String column);

	public GeneralDBSqlExprBuilder number(Number time);

	public GeneralDBSqlCaseBuilder caseBegin();

	public GeneralDBSqlCastBuilder cast(int jdbcType);

	public GeneralDBSqlExprBuilder column(String alias, String column);

	public GeneralDBSqlExprBuilder columnEquals(String alias, String column, Number id);

	public GeneralDBSqlExprBuilder columnEquals(String alias, String column, String label);

	public GeneralDBSqlExprBuilder columnIn(String alias, String column, Number[] ids);
	
	public GeneralDBSqlExprBuilder columnsEqual(String al1, String col1, String al2, String col2);

	public void concat();

	public GeneralDBSqlExprBuilder eq();

	public List<Object> getParameters();

	public boolean isEmpty();

	public GeneralDBSqlExprBuilder isNotNull();

	public GeneralDBSqlExprBuilder isNull();

	public void like();

	public GeneralDBSqlBracketBuilder lowerCase();
	
	public void math(MathExpr.MathOp op);

	public GeneralDBSqlBracketBuilder mod(int value);

	public GeneralDBSqlBracketBuilder not();

	public GeneralDBSqlExprBuilder notEqual();

	public GeneralDBSqlBracketBuilder open();

	public GeneralDBSqlExprBuilder or();
	
	public GeneralDBSqlExprBuilder and();

	public void plus(int range);

	public GeneralDBSqlRegexBuilder regex();

	public void rightShift(int rightShift);

	public CharSequence toSql();

	public String toString();

	public GeneralDBSqlExprBuilder varchar(String stringValue);

	public void addParameters(List<Object> params);

	/**
	 * my additions
	 * FIXME
	 */
	public void appendFunction(String functionName);

	public void appendComma();
	
	public void openBracket();
	
	public void closeBracket();

	public void intersectsMBB();
	
	public void equalsMBB();
	
	public void containsMBB();
	
	public void insideMBB();
	
	public void leftMBB();
	

	public void rightMBB();
	
	public void aboveMBB();
	
	public void belowMBB();
	
	public void doubleCast();
	
	//	public void overlap();

	public void appendProjections(String relName, String attrName, String rawDimensions);
	

	/**
	 * Used to enable projections along with spatial constructs. 
	 * This function constructs the first part of the GeneralDB statement needed
	 * @param dims
	 */
	public void initConstructProjection(ArrayList<Integer> dims);

	public void endConstructProjection(ArrayList<Integer> dims);
	
	//	public void appendChar(char c);
	/**
	 * end of my addition
	 */



}
