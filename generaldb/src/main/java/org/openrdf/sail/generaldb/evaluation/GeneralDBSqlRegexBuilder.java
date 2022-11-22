/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.generaldb.evaluation;

/**
 * Facilitates the building of a regular expression in SQL.
 * 
 * @author James Leigh
 * 
 */
public abstract class GeneralDBSqlRegexBuilder {

	protected GeneralDBSqlExprBuilder where;

	protected GeneralDBSqlExprBuilder value;

	protected GeneralDBSqlExprBuilder pattern;

	protected GeneralDBSqlExprBuilder flags;

	public GeneralDBSqlRegexBuilder(GeneralDBSqlExprBuilder where, GeneralDBQueryBuilderFactory factory) {
		super();
		this.where = where;
		value = factory.createSqlExprBuilder();
		pattern = factory.createSqlExprBuilder();
		flags = factory.createSqlExprBuilder();
	}

	public GeneralDBSqlExprBuilder value() {
		return value;
	}

	public GeneralDBSqlExprBuilder pattern() {
		return pattern;
	}

	public GeneralDBSqlExprBuilder flags() {
		return flags;
	}

	public GeneralDBSqlExprBuilder close() {
		appendRegExp(where);
		return where;
	}

//	protected void appendRegExp(GeneralDBSqlExprBuilder where) {
//		where.append("REGEXP(");
//		appendValue(where);
//		where.append(", ");
//		appendPattern(where);
//		where.append(", ");
//		appendFlags(where);
//		where.append(")");
//	}
	protected abstract void appendRegExp(GeneralDBSqlExprBuilder where);

	protected GeneralDBSqlExprBuilder appendValue(GeneralDBSqlExprBuilder where) {
		where.append(value.toSql());
		where.addParameters(value.getParameters());
		return where;
	}

	protected GeneralDBSqlExprBuilder appendPattern(GeneralDBSqlExprBuilder where) {
		where.append(pattern.toSql());
		where.addParameters(pattern.getParameters());
		return where;
	}

	protected GeneralDBSqlExprBuilder appendFlags(GeneralDBSqlExprBuilder where) {
		where.append(flags.toSql());
		where.addParameters(flags.getParameters());
		return where;
	}
}
