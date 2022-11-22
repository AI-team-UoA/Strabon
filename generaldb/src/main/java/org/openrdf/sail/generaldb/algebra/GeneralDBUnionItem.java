/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.generaldb.algebra;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openrdf.query.algebra.QueryModelNode;
import org.openrdf.query.algebra.QueryModelVisitor;
import org.openrdf.sail.generaldb.algebra.base.GeneralDBFromItem;
import org.openrdf.sail.generaldb.algebra.base.GeneralDBQueryModelVisitorBase;
import org.openrdf.sail.rdbms.schema.ValueTypes;

/**
 * An SQL UNION expression between two {@link GeneralDBFromItem}s.
 * 
 * @author James Leigh
 * 
 */
public class GeneralDBUnionItem extends GeneralDBFromItem {

	private List<GeneralDBFromItem> union = new ArrayList<GeneralDBFromItem>();

	public GeneralDBUnionItem(String alias) {
		super(alias);
	}

	@Override
	public GeneralDBFromItem getFromItem(String alias) {
		for (GeneralDBFromItem from : union) {
			GeneralDBFromItem item = from.getFromItem(alias);
			if (item != null)
				return item;
		}
		return super.getFromItem(alias);
	}

	public List<String> getSelectVarNames() {
		List<GeneralDBColumnVar> vars = new ArrayList<GeneralDBColumnVar>();
		for (GeneralDBFromItem from : union) {
			from.appendVars(vars);
		}
		List<String> selectVars = new ArrayList<String>();
		for (GeneralDBColumnVar var : vars) {
			if (var.isHidden())
				continue;
			if (!selectVars.contains(var.getName())) {
				selectVars.add(var.getName());
			}
		}
		return selectVars;
	}

	public List<GeneralDBColumnVar> getSelectColumns() {
		List<GeneralDBColumnVar> vars = new ArrayList<GeneralDBColumnVar>();
		for (GeneralDBFromItem from : union) {
			from.appendVars(vars);
		}
		List<GeneralDBColumnVar> columns = new ArrayList<GeneralDBColumnVar>();
		Map<String, GeneralDBColumnVar> selectVars = new HashMap<String, GeneralDBColumnVar>();
		for (GeneralDBColumnVar var : vars) {
			if (var.isHidden())
				continue;
			if (selectVars.containsKey(var.getName())) {
				GeneralDBColumnVar existing = selectVars.get(var.getName());
				existing.setValue(null);
				ValueTypes types = existing.getTypes();
				types = types.clone().merge(var.getTypes());
				existing.setTypes(types);
			}
			else {
				String name = var.getAlias() + var.getColumn();
				GeneralDBColumnVar as = var.as(getAlias(), name);
				columns.add(as);
				selectVars.put(var.getName(), as);
			}
		}
		return columns;
	}

	@Override
	public List<GeneralDBColumnVar> appendVars(List<GeneralDBColumnVar> columns) {
		columns.addAll(getSelectColumns());
		return super.appendVars(columns);
	}

	@Override
	public GeneralDBColumnVar getVar(String name) {
		for (GeneralDBColumnVar var : appendVars(new ArrayList<GeneralDBColumnVar>())) {
			if (var.getName().equals(name)) {
				return var;
			}
		}
		return null;
	}

	@Override
	public GeneralDBColumnVar getVarForChildren(String name) {
		for (GeneralDBFromItem join : union) {
			GeneralDBColumnVar var = join.getVar(name);
			if (var != null)
				return var;
		}
		return super.getVarForChildren(name);
	}

	public void addUnion(GeneralDBFromItem from) {
		union.add(from);
		from.setParentNode(this);
	}

	public List<GeneralDBFromItem> getUnion() {
		return union;
	}

	@Override
	public GeneralDBUnionItem clone() {
		GeneralDBUnionItem clone = (GeneralDBUnionItem)super.clone();
		clone.union = new ArrayList<GeneralDBFromItem>();
		for (GeneralDBFromItem from : union) {
			clone.addUnion(from.clone());
		}
		return clone;
	}

	@Override
	public <X extends Exception> void visit(GeneralDBQueryModelVisitorBase<X> visitor)
		throws X
	{
		visitor.meet(this);
	}

	@Override
	public void replaceChildNode(QueryModelNode current, QueryModelNode replacement) {
		for (int i = 0, n = union.size(); i < n; i++) {
			if (current == union.get(i)) {
				union.set(i, (GeneralDBFromItem)replacement);
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
		for (GeneralDBFromItem join : new ArrayList<GeneralDBFromItem>(union)) {
			join.visit(visitor);
		}
	}

}
