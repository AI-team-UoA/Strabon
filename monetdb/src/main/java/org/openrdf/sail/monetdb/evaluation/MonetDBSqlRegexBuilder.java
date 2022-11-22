/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.monetdb.evaluation;

import org.openrdf.sail.generaldb.evaluation.GeneralDBSqlExprBuilder;
import org.openrdf.sail.generaldb.evaluation.GeneralDBSqlRegexBuilder;

/**
 * Facilitates the building of a regular expression in SQL.
 * 
 * @author James Leigh
 * 
 */
public class MonetDBSqlRegexBuilder extends GeneralDBSqlRegexBuilder {

	public MonetDBSqlRegexBuilder(MonetDBSqlExprBuilder where, MonetDBQueryBuilderFactory factory) {
		super(where, factory);
		// Be carefull with class of factory
	}

	@Override
	protected void appendRegExp(GeneralDBSqlExprBuilder where) {
		where.append(" pcre_match(");
		appendValue(where);
		where.append(", ");
		appendPattern(where);
		where.append(") ");
	}
}