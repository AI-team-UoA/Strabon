/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.generaldb.evaluation;

import java.util.List;

/**
 * Facilitates the building of a SQL query.
 * 
 * @author James Leigh
 * 
 */
public class GeneralDBSqlQueryBuilder {

	private GeneralDBQueryBuilderFactory factory;

	public boolean distinct;

	public GeneralDBSqlExprBuilder select;

	public GeneralDBSqlJoinBuilder from;

	public StringBuilder group = new StringBuilder();

	public GeneralDBSqlExprBuilder order;

	public GeneralDBSqlQueryBuilder union;

	public Long offset;

	public Long limit;

	public GeneralDBSqlQueryBuilder(GeneralDBQueryBuilderFactory factory) {
		super();
		this.factory = factory;
		select = factory.createSqlExprBuilder();
		order = factory.createSqlExprBuilder();
	}

	public List<Object> findParameters(List<Object> parameters) {
		parameters.addAll(select.getParameters());
		if (from != null) {
			from.findParameters(parameters);
		}
		if (union != null) {
			union.findParameters(parameters);
		}
		parameters.addAll(order.getParameters());
		return parameters;
	}

	public void distinct() {
		distinct = true;
	}

	public GeneralDBSqlExprBuilder select() {
		if (!select.isEmpty())
			select.append(",\n ");
		return select;
	}

	public GeneralDBSqlJoinBuilder from(String table, String alias) {
		assert from == null : alias;
		return from = factory.createSqlJoinBuilder(table, alias);
	}

	public GeneralDBSqlJoinBuilder from(String alias) {
		assert from == null : alias;
		return from = factory.createSqlJoinBuilder(null, alias);
	}

	public GeneralDBSqlExprBuilder filter() {
		assert from != null;
		return from.on();
	}

	public GeneralDBSqlQueryBuilder groupBy(String... expressions) {
		for (String expr : expressions) {
			if (group.length() == 0) {
				group.append("\nGROUP BY ");
			}
			else {
				group.append(", ");
			}
			group.append(expr);
		}
		return this;
	}

	public GeneralDBSqlQueryBuilder union() {
		assert union == null : union;
		return union = factory.createSqlQueryBuilder();
	}

	public boolean isEmpty() {
		return select.isEmpty() && from == null;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT ");
		if (distinct) {
			sb.append("DISTINCT ");
		}
		if (select.isEmpty()) {
			sb.append("*");
		}
		else {
			sb.append(select.toSql());
		}
		if (from != null) {
			sb.append("\nFROM ").append(from.getFromClause());
			if (!from.on().isEmpty()) {
				sb.append("\nWHERE ");
				sb.append(from.on().toSql());
			}
		}
		sb.append(group);
		if (union != null && !union.isEmpty()) {
			sb.append("\nUNION ALL ");
			sb.append(union.toString());
		}
		if (!order.isEmpty()) {
			sb.append("\nORDER BY ").append(order.toSql());
		}
		if (limit != null) {
			sb.append("\nLIMIT ").append(limit);
		}
		if (offset != null) {
			sb.append("\nOFFSET ").append(offset);
		}
		return sb.toString();
	}

	public GeneralDBSqlExprBuilder orderBy() {
		if (!order.isEmpty())
			order.append(",\n ");
		return order;
	}

	public void offset(Long offset) {
		this.offset = offset;
		if (limit == null) {
			limit = Long.MAX_VALUE;
		}
	}

	public void limit(Long limit) {
		this.limit = limit;
	}
}
