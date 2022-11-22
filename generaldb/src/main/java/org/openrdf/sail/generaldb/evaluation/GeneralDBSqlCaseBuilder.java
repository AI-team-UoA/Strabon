/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.generaldb.evaluation;

/**
 * Facilitates the creation of a CASE expression in SQL.
 * 
 * @author James Leigh
 * 
 */
public class GeneralDBSqlCaseBuilder {

	private GeneralDBSqlExprBuilder where;

	public GeneralDBSqlCaseBuilder(GeneralDBSqlExprBuilder where) {
		super();
		this.where = where;
		where.append("CASE ");
	}

	public GeneralDBSqlExprBuilder when() {
		where.append(" WHEN ");
		return where;
	}

	public GeneralDBSqlExprBuilder then() {
		where.append(" THEN ");
		return where;
	}

	public GeneralDBSqlExprBuilder end() {
		where.append(" END");
		return where;
	}
}
