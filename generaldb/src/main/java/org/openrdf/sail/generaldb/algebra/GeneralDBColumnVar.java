/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.generaldb.algebra;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.query.algebra.Var;
import org.openrdf.sail.rdbms.model.RdbmsResource;
import org.openrdf.sail.rdbms.model.RdbmsURI;
import org.openrdf.sail.rdbms.schema.ValueTypes;

/**
 * Represents a variable in an SQL expression.
 * 
 * @author James Leigh
 * 
 */
public class GeneralDBColumnVar implements Cloneable {

	private int index;

	private boolean anonymous;

	private boolean hidden;

	private boolean implied;

	private String name;

	private Value value;

	private String alias;

	

	private String column;

	private boolean nullable;

	private ValueTypes types;
	
	//indicates whether the object is of spatial nature
	private boolean spatial = false;
	
	//used to change numeric in case of buffer in select
	public void setAlias(String alias) {
		this.alias = alias;
	}

	private GeneralDBColumnVar() {
	}

	public static GeneralDBColumnVar createSubj(String alias, Var v, Resource resource) {
		GeneralDBColumnVar var = new GeneralDBColumnVar();
		var.alias = alias;
		var.column = "subj";
		var.name = v.getName();
		var.anonymous = v.isAnonymous();
		var.value = resource;
		var.types = ValueTypes.RESOURCE;
		if (resource instanceof RdbmsURI) {
			var.types = ValueTypes.URI;
		}
		return var;
	}

	public static GeneralDBColumnVar createPred(String alias, Var v, URI uri, boolean implied) {
		GeneralDBColumnVar var = createSubj(alias, v, uri);
		var.column = "pred";
		var.implied = uri != null && implied;
		var.types = ValueTypes.URI;
		return var;
	}

	public static GeneralDBColumnVar createObj(String alias, Var v, Value value) {
		GeneralDBColumnVar var = new GeneralDBColumnVar();
		var.alias = alias;
		var.column = "obj";
		var.name = v.getName();
		var.anonymous = v.isAnonymous();
		var.value = value;
		var.types = ValueTypes.UNKNOWN;
		if (value instanceof RdbmsURI) {
			var.types = ValueTypes.URI;
		}
		else if (value instanceof RdbmsResource) {
			var.types = ValueTypes.RESOURCE;
		}
		return var;
	}
	
	/**
	 * 13/09/2011
	 * Extra constructor to use in spatial cases
	 * 
	 */
	public static GeneralDBColumnVar createSpatialColumn(String alias, Var v, Value value) {
		GeneralDBColumnVar var = new GeneralDBColumnVar();
		var.alias = alias;
		var.column = "id";
		var.name = v.getName();
		var.anonymous = v.isAnonymous();
		var.value = value;
		var.types = ValueTypes.UNKNOWN;
		if (value instanceof RdbmsResource) {
			var.types = ValueTypes.RESOURCE;
		}
		
		var.setSpatial(true);
		
		return var;
	}
	
	/**
	 * Extra constructor to use in spatial cases
	 * @param isSpatial
	 * @return
	 */
	public static GeneralDBColumnVar createObj(String alias, Var v, Value value, boolean isSpatial) {
		GeneralDBColumnVar var = new GeneralDBColumnVar();
		var.alias = alias;
		var.column = "obj";
		var.name = v.getName();
		var.anonymous = v.isAnonymous();
		var.value = value;
		var.types = ValueTypes.UNKNOWN;
		if (value instanceof RdbmsURI) {
			var.types = ValueTypes.URI;
		}
		else if (value instanceof RdbmsResource) {
			var.types = ValueTypes.RESOURCE;
		}
		
		var.setSpatial(isSpatial);
		
		return var;
	}

	public static GeneralDBColumnVar createCtx(String alias, Var v, Resource resource) {
		GeneralDBColumnVar var = new GeneralDBColumnVar();
		var.alias = alias;
		var.column = "ctx";
		if (v == null) {
			var.name = "__ctx" + Integer.toHexString(System.identityHashCode(var));
			var.anonymous = true;
			var.hidden = true;
		}
		else {
			var.name = v.getName();
			var.anonymous = v.isAnonymous();
		}
		var.value = resource;
		var.types = ValueTypes.RESOURCE;
		if (resource instanceof RdbmsURI) {
			var.types = ValueTypes.URI;
		}
		return var;
	}

	
	
	public boolean isSpatial() {
		return spatial;
	}

	public void setSpatial(boolean spatial) {
		this.spatial = spatial;
	}

	public ValueTypes getTypes() {
		return types;
	}

	public void setTypes(ValueTypes types) {
		this.types = types;
	}

	public boolean isAnonymous() {
		return anonymous;
	}

	public boolean isHidden() {
		return hidden;
	}

	public boolean isHiddenOrConstant() {
		return hidden || value != null;
	}

	public boolean isImplied() {
		return implied;
	}

	public boolean isResource() {
		return !types.isLiterals();
	}

	public boolean isURI() {
		return !types.isLiterals() && !types.isBNodes();
	}

	public boolean isNullable() {
		return nullable;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public String getName() {
		return name;
	}

	public Value getValue() {
		return value;
	}

	public void setValue(Value value) {
		this.value = value;
		if (value == null) {
			implied = false;
		}
	}

	public String getColumn() {
		return column;
	}

	public boolean isPredicate() {
		return "pred".equals(column);
	}

	public String getAlias() {
		return alias;
	}

	public GeneralDBColumnVar as(String name) {
		try {
			GeneralDBColumnVar clone = (GeneralDBColumnVar)super.clone();
			clone.name = name;
			return clone;
		}
		catch (CloneNotSupportedException e) {
			throw new AssertionError(e);
		}
	}

	public GeneralDBColumnVar as(String alias, String column) {
		try {
			GeneralDBColumnVar clone = (GeneralDBColumnVar)super.clone();
			clone.alias = alias;
			clone.column = column;
			clone.nullable = true;
			//XXX
			clone.spatial = spatial;
			return clone;
		}
		catch (CloneNotSupportedException e) {
			throw new AssertionError(e);
		}
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof GeneralDBColumnVar) {
			return name.equals(((GeneralDBColumnVar)other).name);
		}

		return false;
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(128);

		sb.append(alias).append(".").append(column);

		sb.append(" (name=").append(name);

		if (value != null) {
			sb.append(", value=").append(value.toString());
		}

		sb.append(")");
		if (index > 0) {
			sb.append("#").append(index);
		}

		return sb.toString();
	}

}
