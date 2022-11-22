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
public interface GeneralDBSqlCastBuilder extends GeneralDBSqlExprBuilderInterface {

	public GeneralDBSqlExprBuilder close();
	
	public abstract GeneralDBSqlExprBuilder appendNumeric(Number doubleValue);
	
	public abstract void appendBoolean(boolean booleanValue);
}
