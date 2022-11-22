/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.generaldb.algebra;

import org.openrdf.query.algebra.QueryModelNode;
import org.openrdf.query.algebra.QueryModelVisitor;
import org.openrdf.sail.generaldb.algebra.base.GeneralDBQueryModelNodeBase;
import org.openrdf.sail.generaldb.algebra.base.GeneralDBQueryModelVisitorBase;
import org.openrdf.sail.generaldb.algebra.base.GeneralDBSqlExpr;

/**
 * A collection of SQL expressions that form an RDF value binding.
 * 
 * @author James Leigh
 * 
 */
public class GeneralDBSelectProjection extends GeneralDBQueryModelNodeBase {

	private GeneralDBColumnVar var;

	private GeneralDBRefIdColumn id;

	private GeneralDBSqlExpr stringValue;

	private GeneralDBSqlExpr datatype;

	private GeneralDBSqlExpr language;

	public GeneralDBColumnVar getVar() {
		return var;
	}

	public void setVar(GeneralDBColumnVar var) {
		this.var = var;
	}

	public GeneralDBRefIdColumn getId() {
		return id;
	}

	public void setId(GeneralDBRefIdColumn id) {
		this.id = id;
		id.setParentNode(this);
	}

	public GeneralDBSqlExpr getStringValue() {
		return stringValue;
	}

	public void setStringValue(GeneralDBSqlExpr stringValue) {
		this.stringValue = stringValue;
		stringValue.setParentNode(this);
	}

	public GeneralDBSqlExpr getDatatype() {
		return datatype;
	}

	public void setDatatype(GeneralDBSqlExpr datatype) {
		this.datatype = datatype;
		datatype.setParentNode(this);
	}

	public GeneralDBSqlExpr getLanguage() {
		return language;
	}

	public void setLanguage(GeneralDBSqlExpr language) {
		this.language = language;
		language.setParentNode(this);
	}

	@Override
	public <X extends Exception> void visit(GeneralDBQueryModelVisitorBase<X> visitor)
	throws X
	{
		visitor.meet(this);
	}

	@Override
	public <X extends Exception> void visitChildren(QueryModelVisitor<X> visitor)
	throws X
	{
		id.visit(visitor);
		stringValue.visit(visitor);
		//XXX issue with datatype + language again because of the spatial case -> it is null then
		if(datatype!=null)
		{
			datatype.visit(visitor);
		}
		if(language!=null)
		{
			language.visit(visitor);
		}
	}

	@Override
	public void replaceChildNode(QueryModelNode current, QueryModelNode replacement) {
		if (id == current) {
			setId((GeneralDBRefIdColumn)replacement);
		}
		else if (stringValue == current) {
			setStringValue((GeneralDBSqlExpr)replacement);
		}
		else if (datatype == current) {
			setDatatype((GeneralDBSqlExpr)replacement);
		}
		else if (language == current) {
			setLanguage((GeneralDBSqlExpr)replacement);
		}
		else {
			super.replaceChildNode(current, replacement);
		}
	}

	@Override
	public GeneralDBSelectProjection clone() {
		GeneralDBSelectProjection clone = (GeneralDBSelectProjection)super.clone();
		clone.setId(getId().clone());
		clone.setStringValue(getStringValue().clone());
		//XXX issue with datatype + language again because of the spatial case -> it is null then
		if(getDatatype()!=null)
		{
			clone.setDatatype(getDatatype().clone());
		}
		
		if(getLanguage()!=null)
		{
			clone.setLanguage(getLanguage().clone());
		}
		return clone;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((datatype == null) ? 0 : datatype.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((language == null) ? 0 : language.hashCode());
		result = prime * result + ((stringValue == null) ? 0 : stringValue.hashCode());
		result = prime * result + ((var == null) ? 0 : var.hashCode());
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
		final GeneralDBSelectProjection other = (GeneralDBSelectProjection)obj;
		if (datatype == null) {
			if (other.datatype != null)
				return false;
		}
		else if (!datatype.equals(other.datatype))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		}
		else if (!id.equals(other.id))
			return false;
		if (language == null) {
			if (other.language != null)
				return false;
		}
		else if (!language.equals(other.language))
			return false;
		if (stringValue == null) {
			if (other.stringValue != null)
				return false;
		}
		else if (!stringValue.equals(other.stringValue))
			return false;
		if (var == null) {
			if (other.var != null)
				return false;
		}
		else if (!var.equals(other.var))
			return false;
		return true;
	}

}
