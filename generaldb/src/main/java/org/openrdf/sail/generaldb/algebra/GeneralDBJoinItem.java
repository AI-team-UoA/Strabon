/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.generaldb.algebra;

import java.util.ArrayList;
import java.util.List;

import org.openrdf.sail.generaldb.algebra.base.GeneralDBFromItem;
import org.openrdf.sail.generaldb.algebra.base.GeneralDBQueryModelVisitorBase;

/**
 * An SQL join.
 * 
 * @author James Leigh
 * 
 */
public class GeneralDBJoinItem extends GeneralDBFromItem {

	private String tableName;

	private Number predId;

	private List<GeneralDBColumnVar> vars = new ArrayList<GeneralDBColumnVar>();

	public GeneralDBJoinItem(String alias, String tableName, Number predId) {
		super(alias);
		this.tableName = tableName;
		this.predId = predId;
	}

	public GeneralDBJoinItem(String alias, String tableName) {
		super(alias);
		this.tableName = tableName;
		this.predId = 0;
	}

	public String getTableName() {
		return tableName;
	}

	public Number getPredId() {
		return predId;
	}

	public void addVar(GeneralDBColumnVar var) {
		this.vars.add(var);
	}

	@Override
	public GeneralDBColumnVar getVarForChildren(String name) {
		for (GeneralDBColumnVar var : vars) {
			if (var.getName().equals(name))
				return var;
		}
		return super.getVarForChildren(name);
	}

	@Override
	public List<GeneralDBColumnVar> appendVars(List<GeneralDBColumnVar> vars) {
		vars.addAll(this.vars);
		return super.appendVars(vars);
	}

	@Override
	public String getSignature() {
		StringBuilder sb = new StringBuilder();
		if (isLeft()) {
			sb.append("LEFT ");
		}
		sb.append(super.getSignature());
		sb.append(" ").append(tableName);
		sb.append(" ").append(getAlias());
		return sb.toString();
	}

	@Override
	public GeneralDBJoinItem clone() {
		GeneralDBJoinItem clone = (GeneralDBJoinItem)super.clone();
		clone.vars = new ArrayList<GeneralDBColumnVar>(vars);
		return clone;
	}

	@Override
	public <X extends Exception> void visit(GeneralDBQueryModelVisitorBase<X> visitor)
		throws X
	{
		visitor.meet(this);
	}

}
