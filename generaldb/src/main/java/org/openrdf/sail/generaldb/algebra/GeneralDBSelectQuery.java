/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.generaldb.algebra;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openrdf.query.algebra.QueryModelNode;
import org.openrdf.query.algebra.QueryModelVisitor;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.sail.generaldb.algebra.base.GeneralDBFromItem;
import org.openrdf.sail.generaldb.algebra.base.GeneralDBQueryModelNodeBase;
import org.openrdf.sail.generaldb.algebra.base.GeneralDBQueryModelVisitorBase;
import org.openrdf.sail.generaldb.algebra.base.GeneralDBSqlConstant;
import org.openrdf.sail.generaldb.algebra.base.GeneralDBSqlExpr;

/**
 * An SQL query.
 * 
 * @author James Leigh
 * 
 */
public class GeneralDBSelectQuery extends GeneralDBQueryModelNodeBase implements TupleExpr {

	public static class OrderElem {

		public final GeneralDBSqlExpr sqlExpr;

		public final boolean isAscending;

		protected OrderElem(GeneralDBSqlExpr sqlExpr, boolean isAscending) {
			this.sqlExpr = sqlExpr;
			this.isAscending = isAscending;
		}
	}

	private Map<String, GeneralDBSelectProjection> projections = new HashMap<String, GeneralDBSelectProjection>();

	private Map<String, String> bindingVars;
	
	/**
	 * XXX addition for spatial constructs in select
	 */
	private Map<String, GeneralDBSqlExpr> spatialConstructs = new HashMap<String, GeneralDBSqlExpr>();
	
	//Extra addition for true spatial selections and joins in FROM clause - 07/09/2011
	//private List<GeneralDBSqlExpr> spatialFilters = new ArrayList<GeneralDBSqlExpr>();
	/**
	 * 
	 */

	private boolean distinct;

	private GeneralDBFromItem from;

	private List<OrderElem> order = new ArrayList<OrderElem>();

	private Long offset;

	private Long limit;
	
	/**
	 * 
 	 */

	public Map<String, GeneralDBSqlExpr> getSpatialConstructs() {
		return spatialConstructs;
	}

	public void setSpatialConstructs(Map<String, GeneralDBSqlExpr> spatialConstructs) {
		this.spatialConstructs = spatialConstructs;
	}
	
	//09/09/2011 XXX
//	public List<GeneralDBSqlExpr> getSpatialFilters() {
//		return spatialFilters;
//	}
//
//	public void addSpatialFilter(GeneralDBSqlExpr spatialFilter) {
//		this.spatialFilters.add(spatialFilter);
//	}

	/**
	 * 
 	 */

	public boolean isDistinct() {
		return distinct;
	}

	public void setDistinct(boolean b) {
		distinct = b;
	}

	public boolean isComplex() {
		if (offset != null || limit != null)
			return true;
		return isDistinct() || !order.isEmpty();
	}

	public GeneralDBFromItem getFrom() {
		return from;
	}

	public void setFrom(GeneralDBFromItem from) {
		this.from = from;
		from.setParentNode(this);
	}

	public List<OrderElem> getOrderElems() {
		return order;
	}

	public void addOrder(GeneralDBSqlExpr order, boolean isAscending) {
		if (order instanceof GeneralDBSqlNull)
			return;
		if (order instanceof GeneralDBSqlConstant<?>)
			return;
		this.order.add(new OrderElem(order, isAscending));
		order.setParentNode(this);
	}

	public Long getOffset() {
		return offset;
	}

	public void setOffset(Long offset) {
		this.offset = offset;
	}

	public Long getLimit() {
		return limit;
	}

	public void setLimit(Long limit) {
		this.limit = limit;
	}

	public Collection<String> getBindingNames(GeneralDBColumnVar var) {
		if (bindingVars == null)
			return Collections.singleton(var.getName());
		List<String> list = new ArrayList<String>(bindingVars.size());
		for (String name : bindingVars.keySet()) {
			if (var.getName().equals(bindingVars.get(name))) {
				list.add(name);
			}
		}
		return list;
	}

	public Set<String> getBindingNames() {
		if (bindingVars == null) {
			Set<String> names = new HashSet<String>();
			for (GeneralDBColumnVar var : getVars()) {
				names.add(var.getName());
			}
			return names;
		}
		return new HashSet<String>(bindingVars.keySet());
	}
	
	public Set<String> getAssuredBindingNames() {
		// FIXME: implement this properly
		return Collections.emptySet();
	}

	public void setBindingVars(Map<String, String> bindingVars) {
		this.bindingVars = bindingVars;
	}

	public Collection<GeneralDBSelectProjection> getSqlSelectVar() {
		return projections.values();
	}

	public void setSqlSelectVar(Collection<GeneralDBSelectProjection> projections) {
		this.projections.clear();
		for (GeneralDBSelectProjection p : projections) {
			addSqlSelectVar(p);
		}
	}

	public GeneralDBSelectProjection getSelectProjection(String name) {
		return projections.get(name);
	}

	@Override
	public void replaceChildNode(QueryModelNode current, QueryModelNode replacement) {
		for (String name : projections.keySet()) {
			if (projections.get(name) == current) {
				projections.put(name, (GeneralDBSelectProjection)replacement);
				replacement.setParentNode(this);
				return;
			}
		}
		if (from == current) {
			from = (GeneralDBFromItem)replacement;
			replacement.setParentNode(this);
			return;
		}
		for (int i = 0, n = order.size(); i < n; i++) {
			if (order.get(i).sqlExpr == current) {
				if (replacement instanceof GeneralDBSqlNull || order instanceof GeneralDBSqlConstant<?>) {
					order.remove(i);
					return;
				}
				boolean asc = order.get(i).isAscending;
				order.set(i, new OrderElem((GeneralDBSqlExpr)replacement, asc));
				replacement.setParentNode(this);
				return;
			}
		}
		super.replaceChildNode(current, replacement);
	}

	@Override
	public <X extends Exception> void visitChildren(QueryModelVisitor<X> visitor)
		throws X
	{
		super.visitChildren(visitor);
		from.visit(visitor);
		ArrayList<GeneralDBSelectProjection> list = new ArrayList<GeneralDBSelectProjection>(projections.values());
		for (GeneralDBSelectProjection expr : list) {
			expr.visit(visitor);
		}
		for (OrderElem by : new ArrayList<OrderElem>(order)) {
			by.sqlExpr.visit(visitor);
		}
	}

	@Override
	public GeneralDBSelectQuery clone() {
		GeneralDBSelectQuery clone = (GeneralDBSelectQuery)super.clone();
		clone.distinct = distinct;
		clone.projections = new HashMap<String, GeneralDBSelectProjection>();
		for (GeneralDBSelectProjection expr : projections.values()) {
			clone.addSqlSelectVar(expr.clone());
		}
		clone.from = from.clone();
		clone.order = new ArrayList<OrderElem>(order);
		clone.spatialConstructs = new HashMap<String, GeneralDBSqlExpr>(spatialConstructs);
		return clone;
	}

	@Override
	public <X extends Exception> void visit(GeneralDBQueryModelVisitorBase<X> visitor)
		throws X
	{
		visitor.meet(this);
	}

	public boolean hasSqlSelectVar(GeneralDBSelectProjection node) {
		return projections.containsKey(node.getVar().getName());
	}

	public boolean hasSqlSelectVarName(String name) {
		return projections.containsKey(name);
	}

	public void addSqlSelectVar(GeneralDBSelectProjection node) {
		projections.put(node.getVar().getName(), node);
		node.setParentNode(this);
	}

	public Collection<GeneralDBColumnVar> getProjections() {
		List<GeneralDBColumnVar> vars = new ArrayList<GeneralDBColumnVar>();
		for (GeneralDBSelectProjection proj : projections.values()) {
			GeneralDBColumnVar var = proj.getVar();
			if (bindingVars == null) {
				vars.add(var);
			} else {
				for (String name : bindingVars.keySet()) {
					if (var.getName().equals(bindingVars.get(name))) {
						vars.add(var.as(name));
					}
				}
			}
		}
		return vars;
	}

	public Collection<GeneralDBColumnVar> getVars() {
		List<GeneralDBColumnVar> vars = new ArrayList<GeneralDBColumnVar>();
		from.appendVars(vars);
		return vars;
	}

	public GeneralDBColumnVar getVar(String varName) {
		return from.getVar(varName);
	}

	public void addFilter(GeneralDBSqlExpr sql) {
		from.addFilter(sql);
	}

	public void addJoin(GeneralDBSelectQuery right) {
		from.addJoin(right.getFrom());
	}

	public void addLeftJoin(GeneralDBSelectQuery right) {
		GeneralDBFromItem join = right.getFrom();
		join.setLeft(true);
		from.addJoin(join);
	}

	public GeneralDBFromItem getFromItem(String alias) {
		return from.getFromItemNotInUnion(alias);
	}

	public List<GeneralDBSqlExpr> getFilters() {
		return from.getFilters();
	}

	public void removeFilter(GeneralDBSqlExpr sqlExpr) {
		from.removeFilter(sqlExpr);
	}

	public Map<String, GeneralDBColumnVar> getVarMap() {
		Collection<GeneralDBColumnVar> vars = getVars();
		Map<String, GeneralDBColumnVar> map = new HashMap<String, GeneralDBColumnVar>(vars.size());
		for (GeneralDBColumnVar var : vars) {
			if (!map.containsKey(var.getName())) {
				map.put(var.getName(), var);
			}
		}
		return map;
	}

}
