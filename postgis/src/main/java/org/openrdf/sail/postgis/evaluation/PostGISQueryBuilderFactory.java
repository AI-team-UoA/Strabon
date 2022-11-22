/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.postgis.evaluation;

import org.openrdf.sail.generaldb.evaluation.GeneralDBQueryBuilder;
import org.openrdf.sail.generaldb.evaluation.GeneralDBQueryBuilderFactory;
import org.openrdf.sail.generaldb.evaluation.GeneralDBSqlBracketBuilder;
import org.openrdf.sail.generaldb.evaluation.GeneralDBSqlCastBuilder;
import org.openrdf.sail.generaldb.evaluation.GeneralDBSqlExprBuilder;
import org.openrdf.sail.generaldb.evaluation.GeneralDBSqlRegexBuilder;

/**
 * Creates the SQL query building components.
 * 
 * @author James Leigh
 * 
 */
public class PostGISQueryBuilderFactory extends GeneralDBQueryBuilderFactory {

	@Override
	public GeneralDBQueryBuilder createQueryBuilder() {
		GeneralDBQueryBuilder query = new PostGISQueryBuilder(createSqlQueryBuilder());
		query.setValueFactory(vf);
		query.setUsingHashTable(usingHashTable);
		return query;
	}

	@Override
	public GeneralDBSqlExprBuilder createSqlExprBuilder() {
		return new PostGISSqlExprBuilder(this);
	}

	@Override
	public GeneralDBSqlRegexBuilder createSqlRegexBuilder(GeneralDBSqlExprBuilder where) {
		return new PostGISSqlRegexBuilder(where, this);
	}
	
	@Override
	public GeneralDBSqlBracketBuilder createSqlBracketBuilder(GeneralDBSqlExprBuilder where) {
		return new PostGISSqlBracketBuilder(where, this);
	}

	@Override
	public GeneralDBSqlCastBuilder createSqlCastBuilder(GeneralDBSqlExprBuilder where, int type) {
		return new PostGISSqlCastBuilder(where, this, type);
	}
	
}