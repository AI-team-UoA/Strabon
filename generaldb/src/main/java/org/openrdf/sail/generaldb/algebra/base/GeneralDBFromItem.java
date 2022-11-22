/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.generaldb.algebra.base;

import java.util.ArrayList;
import java.util.List;

import org.openrdf.query.algebra.QueryModelNode;
import org.openrdf.query.algebra.QueryModelVisitor;
import org.openrdf.sail.generaldb.algebra.GeneralDBColumnVar;

/**
 * An item in the SQL from clause.
 * 
 * @author James Leigh
 * 
 */
public abstract class GeneralDBFromItem extends GeneralDBQueryModelNodeBase {

	private String alias;

	private boolean left;

	private List<GeneralDBFromItem> joins = new ArrayList<GeneralDBFromItem>();

	private List<GeneralDBSqlExpr> filters = new ArrayList<GeneralDBSqlExpr>();

	public GeneralDBFromItem(String alias) {
		super();
		this.alias = alias;
	}

	public String getAlias() {
		return alias;
	}

	public boolean isLeft() {
		return left;
	}

	public void setLeft(boolean left) {
		this.left = left;
	}

	public List<GeneralDBSqlExpr> getFilters() {
		return filters;
	}

	public void addFilter(GeneralDBSqlExpr filter) {
		this.filters.add(filter);
		filter.setParentNode(this);
	}

	public List<GeneralDBFromItem> getJoins() {
		return joins;
	}

	public GeneralDBColumnVar getVarForChildren(String name) {
		for (GeneralDBFromItem join : joins) {
			GeneralDBColumnVar var = join.getVar(name);
			if (var != null)
				return var;
		}
		return null;
	}

	public GeneralDBColumnVar getVar(String name) {
		return getVarForChildren(name);
	}

	public void addJoin(GeneralDBFromItem join) {
		joins.add(join);
		joinAdded(join);
	}

	public void addJoinBefore(GeneralDBFromItem valueJoin, GeneralDBFromItem join) {
		for (int i = 0, n = joins.size(); i < n; i++) {
			if (joins.get(i) == join) {
				joins.add(i, valueJoin);
				joinAdded(valueJoin);
				return;
			}
		}
		addJoin(valueJoin);
	}

	protected void joinAdded(GeneralDBFromItem valueJoin) {
		valueJoin.setParentNode(this);
	}

	public GeneralDBFromItem getFromItem(String alias) {
		if (this.alias.equals(alias))
			return this;
		for (GeneralDBFromItem join : joins) {
			GeneralDBFromItem result = join.getFromItem(alias);
			if (result != null)
				return result;
		}
		return null;
	}

	public GeneralDBFromItem getFromItemNotInUnion(String alias) {
		if (this.alias.equals(alias))
			return this;
		for (GeneralDBFromItem join : joins) {
			GeneralDBFromItem result = join.getFromItemNotInUnion(alias);
			if (result != null)
				return result;
		}
		return null;
	}

	public void removeFilter(GeneralDBSqlExpr sqlExpr) {
		for (int i = filters.size() - 1; i >= 0; i--) {
			if (filters.get(i) == sqlExpr) {
				filters.remove(i);
				break;
			}
		}
	}

	public List<GeneralDBColumnVar> appendVars(List<GeneralDBColumnVar> vars) {
		for (GeneralDBFromItem join : joins) {
			join.appendVars(vars);
		}
		return vars;
	}

	@Override
	public String getSignature() {
		StringBuilder sb = new StringBuilder();
		if (left) {
			sb.append("LEFT ");
		}
		sb.append(super.getSignature());
		sb.append(" ").append(alias);
		return sb.toString();
	}

	@Override
	public GeneralDBFromItem clone() {
		GeneralDBFromItem clone = (GeneralDBFromItem)super.clone();
		clone.joins = new ArrayList<GeneralDBFromItem>();
		for (GeneralDBFromItem join : joins) {
			clone.addJoin(join.clone());
		}
		clone.filters = new ArrayList<GeneralDBSqlExpr>();
		for (GeneralDBSqlExpr expr : filters) {
			clone.addFilter(expr.clone());
		}
		return clone;
	}

	@Override
	public <X extends Exception> void visitChildren(QueryModelVisitor<X> visitor)
		throws X
	{
		super.visitChildren(visitor);
		for (GeneralDBFromItem join : new ArrayList<GeneralDBFromItem>(joins)) {
			join.visit(visitor);
		}
		for (GeneralDBSqlExpr expr : new ArrayList<GeneralDBSqlExpr>(filters)) {
			expr.visit(visitor);
		}
	}

	@Override
	public void replaceChildNode(QueryModelNode current, QueryModelNode replacement) {
		for (int i = 0, n = joins.size(); i < n; i++) {
			if (current == joins.get(i)) {
				joins.set(i, (GeneralDBFromItem)replacement);
				joinAdded((GeneralDBFromItem)replacement);
				return;
			}
		}
		for (int i = 0, n = filters.size(); i < n; i++) {
			if (current == filters.get(i)) {
				filters.set(i, (GeneralDBSqlExpr)replacement);
				replacement.setParentNode(this);
				return;
			}
		}
		super.replaceChildNode(current, replacement);
	}

}
