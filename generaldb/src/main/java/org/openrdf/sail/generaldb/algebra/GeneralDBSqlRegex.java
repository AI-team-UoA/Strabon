/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.generaldb.algebra;

import org.openrdf.query.algebra.QueryModelVisitor;
import org.openrdf.sail.generaldb.algebra.base.BinaryGeneralDBOperator;
import org.openrdf.sail.generaldb.algebra.base.GeneralDBQueryModelVisitorBase;
import org.openrdf.sail.generaldb.algebra.base.GeneralDBSqlExpr;

/**
 * The regular SQL expression - notation varies between databases.
 * 
 * @author James Leigh
 * 
 */
public class GeneralDBSqlRegex extends BinaryGeneralDBOperator {

	/*-----------*
	 * Variables *
	 *-----------*/

	private GeneralDBSqlExpr flagsArg;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public GeneralDBSqlRegex() {
	}

	public GeneralDBSqlRegex(GeneralDBSqlExpr expr, GeneralDBSqlExpr pattern) {
		super(expr, pattern);
	}

	public GeneralDBSqlRegex(GeneralDBSqlExpr expr, GeneralDBSqlExpr pattern, GeneralDBSqlExpr flags) {
		super(expr, pattern);
		setFlagsArg(flags);
	}

	/*---------*
	 * Methods *
	 *---------*/

	public GeneralDBSqlExpr getArg() {
		return super.getLeftArg();
	}

	public void setArg(GeneralDBSqlExpr leftArg) {
		super.setLeftArg(leftArg);
	}

	public GeneralDBSqlExpr getPatternArg() {
		return super.getRightArg();
	}

	public void setPatternArg(GeneralDBSqlExpr rightArg) {
		super.setRightArg(rightArg);
	}

	public void setFlagsArg(GeneralDBSqlExpr flags) {
		this.flagsArg = flags;
	}

	public GeneralDBSqlExpr getFlagsArg() {
		return flagsArg;
	}

	@Override
	public <X extends Exception> void visitChildren(QueryModelVisitor<X> visitor)
		throws X
	{
		super.visitChildren(visitor);
		if (flagsArg != null) {
			flagsArg.visit(visitor);
		}
	}

	@Override
	public GeneralDBSqlRegex clone() {
		GeneralDBSqlRegex clone = (GeneralDBSqlRegex)super.clone();
		if (flagsArg != null) {
			clone.setFlagsArg(flagsArg.clone());
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
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((flagsArg == null) ? 0 : flagsArg.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		final GeneralDBSqlRegex other = (GeneralDBSqlRegex)obj;
		if (flagsArg == null) {
			if (other.flagsArg != null)
				return false;
		}
		else if (!flagsArg.equals(other.flagsArg))
			return false;
		return true;
	}
}
