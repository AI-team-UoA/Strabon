/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * 
 * Copyright (C) 2010, 2011, 2012, 2013 Pyravlos Team
 * 
 * http://www.strabon.di.uoa.gr/
 */
package org.openrdf.sail.postgis.evaluation;

import java.sql.Types;

import org.openrdf.sail.generaldb.evaluation.GeneralDBQueryBuilderFactory;
import org.openrdf.sail.generaldb.evaluation.GeneralDBSqlCastBuilder;
import org.openrdf.sail.generaldb.evaluation.GeneralDBSqlExprBuilder;

public class PostGISSqlCastBuilder extends PostGISSqlExprBuilder implements GeneralDBSqlCastBuilder {
	
	protected GeneralDBSqlExprBuilder where;

	protected int jdbcType;

	public PostGISSqlCastBuilder(GeneralDBSqlExprBuilder where, GeneralDBQueryBuilderFactory factory, int jdbcType) {
		super(factory);
		this.where = where;
		this.jdbcType = jdbcType;
		append(" CAST(");
	}

	public GeneralDBSqlExprBuilder close() {
		append(" AS ");
		append(getSqlType(jdbcType));
		append(")");
		where.append(toSql());
		where.addParameters(getParameters());
		return where;
	}

	protected CharSequence getSqlType(int type) {
		switch (type) {
			case Types.VARCHAR:
				return "VARCHAR";
			default:
				throw new AssertionError(type);
		}
	}
}
