/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.generaldb.evaluation;

/**
 * Prints round brackets in an SQL query.
 * 
 * @author James Leigh
 * 
 */
public interface GeneralDBSqlBracketBuilder extends GeneralDBSqlExprBuilderInterface {

	public String getClosing();

	public void setClosing(String closing);

	public GeneralDBSqlExprBuilder close();

	public abstract void appendBoolean(boolean booleanValue);

	public abstract GeneralDBSqlExprBuilder appendNumeric(Number doubleValue);
	
	public GeneralDBSqlExprBuilder columnEquals(String alias, String column, Number id);


}
