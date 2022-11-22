/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.generaldb.evaluation;

import org.openrdf.sail.generaldb.GeneralDBValueFactory;

/**
 * Creates the SQL query building components.
 * 
 * @author James Leigh
 * 
 */
public abstract class GeneralDBQueryBuilderFactory {

	protected GeneralDBValueFactory vf;

	protected boolean usingHashTable;

	public void setValueFactory(GeneralDBValueFactory vf) {
		this.vf = vf;
	}

	public void setUsingHashTable(boolean b) {
		this.usingHashTable = b;
	}

	public abstract GeneralDBQueryBuilder createQueryBuilder();

	public GeneralDBSqlQueryBuilder createSqlQueryBuilder() {
		return new GeneralDBSqlQueryBuilder(this);
	}

	public abstract GeneralDBSqlExprBuilder createSqlExprBuilder();

	public abstract GeneralDBSqlRegexBuilder createSqlRegexBuilder(GeneralDBSqlExprBuilder where);

	public abstract GeneralDBSqlBracketBuilder createSqlBracketBuilder(GeneralDBSqlExprBuilder where);

	public abstract GeneralDBSqlCastBuilder createSqlCastBuilder(GeneralDBSqlExprBuilder where, int type);

	public GeneralDBSqlJoinBuilder createSqlJoinBuilder(String table, String alias) {
		return new GeneralDBSqlJoinBuilder(table, alias, this);
	}
}
