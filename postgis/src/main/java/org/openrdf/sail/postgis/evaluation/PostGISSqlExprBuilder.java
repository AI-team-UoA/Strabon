/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.postgis.evaluation;

import org.openrdf.sail.generaldb.evaluation.GeneralDBQueryBuilderFactory;
import org.openrdf.sail.generaldb.evaluation.GeneralDBSqlExprBuilder;

/**
 * Assemblies an SQL expression.
 * 
 * @author James Leigh
 * 
 */
public class PostGISSqlExprBuilder extends GeneralDBSqlExprBuilder {

	public PostGISSqlExprBuilder(GeneralDBQueryBuilderFactory factory) {
		super(factory);
	}

	@Override
	public void appendBoolean(boolean booleanValue) {
		if (booleanValue) {
			where.append(" (1=1) ");
		}
		else {
			where.append(" (0=1) ");
		}
	}

	@Override
	public GeneralDBSqlExprBuilder appendNumeric(Number doubleValue) {
		where.append(" ? ");
		parameters.add(doubleValue);
		return this;
	}

	@Override
	public GeneralDBSqlExprBuilder number(Number time) {
		where.append(" ? ");
		parameters.add(time);
		return this;
	}
	
	@Override
	public GeneralDBSqlExprBuilder varchar(String stringValue) {
		if (stringValue == null) {
			appendNull();
		}
		else {
			where.append(" ? ");
			parameters.add(stringValue);
		}
		return this;
	}
	
//	// TODO should this be overriden ??
//	protected String getSqlNull() {
////		return "false"; // FIXME
//		return NULL;
//	}

}