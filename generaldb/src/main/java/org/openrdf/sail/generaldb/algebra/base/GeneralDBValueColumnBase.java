/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.generaldb.algebra.base;

import org.openrdf.query.algebra.Var;
import org.openrdf.sail.generaldb.algebra.GeneralDBColumnVar;

/**
 * A column in an SQL expression.
 * 
 * @author James Leigh
 * 
 */
public abstract class GeneralDBValueColumnBase extends GeneralDBQueryModelNodeBase implements GeneralDBSqlExpr {

	private String name;

	/**
	 * 
	 * XXX
	 */
	public void setVarName(String name) {
		this.name = name;
	}
	/**
	 * 
	 */

	private GeneralDBColumnVar var;

	public GeneralDBValueColumnBase(Var var) {
		this.name = var.getName();
	}

	public GeneralDBValueColumnBase(GeneralDBColumnVar var) {
		this.name = var.getName();
		setRdbmsVar(var);
	}

	public String getVarName() {
		return name;
	}

	public GeneralDBColumnVar getRdbmsVar() {
		return var;
	}

	public void setRdbmsVar(GeneralDBColumnVar var) {
		assert var != null;
		this.var = var;
	}

	public String getAlias() {
		return var.getAlias();
	}

	public String getColumn() {
		return var.getColumn();
	}

	@Override
	public String getSignature() {
		if (var != null)
			return super.getSignature() + " " + var;
		return super.getSignature() + " " + name;
	}

	@Override
	public GeneralDBValueColumnBase clone() {
		return (GeneralDBValueColumnBase)super.clone();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final GeneralDBValueColumnBase other = (GeneralDBValueColumnBase)obj;
		if (name == null) {
			if (other.name != null)
				return false;
		}
		else if (!name.equals(other.name))
			return false;
		return true;
	}

}
