/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.generaldb.algebra.base;

/**
 * A constant SQL value, like a varchar or number.
 * 
 * @author James Leigh
 * 
 */
public abstract class GeneralDBSqlConstant<T> extends GeneralDBQueryModelNodeBase implements GeneralDBSqlExpr {

	private T value;

	public GeneralDBSqlConstant() {
		super();
	}

	public GeneralDBSqlConstant(T value) {
		super();
		this.value = value;
	}

	public T getValue() {
		return value;
	}

	public void setValue(T value) {
		this.value = value;
	}

	@Override
	public String getSignature() {
		return super.getSignature() + " " + value;
	}

	@Override
	public GeneralDBSqlConstant<T> clone() {
		GeneralDBSqlConstant<T> clone = (GeneralDBSqlConstant<T>)super.clone();
		clone.setValue(value);
		return clone;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((value == null) ? 0 : value.hashCode());
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
		final GeneralDBSqlConstant other = (GeneralDBSqlConstant)obj;
		if (value == null) {
			if (other.value != null)
				return false;
		}
		else if (!value.equals(other.value))
			return false;
		return true;
	}
}
