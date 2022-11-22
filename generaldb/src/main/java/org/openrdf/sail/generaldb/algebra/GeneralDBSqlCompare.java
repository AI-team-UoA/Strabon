/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.generaldb.algebra;

import org.openrdf.query.algebra.Compare.CompareOp;
import org.openrdf.sail.generaldb.algebra.base.BinaryGeneralDBOperator;
import org.openrdf.sail.generaldb.algebra.base.GeneralDBQueryModelVisitorBase;
import org.openrdf.sail.generaldb.algebra.base.GeneralDBSqlExpr;

/**
 * The SQL compare expressions (>, <, >=, <=).
 * 
 * @author James Leigh
 * 
 */
public class GeneralDBSqlCompare extends BinaryGeneralDBOperator {

	public enum Operator {
		GT,
		LT,
		GE,
		LE
	}

	private Operator op;

	public GeneralDBSqlCompare(GeneralDBSqlExpr leftArg, CompareOp op, GeneralDBSqlExpr rightArg) {
		super(leftArg, rightArg);
		switch (op) {
			case GT:
				this.op = Operator.GT;
				break;
			case LT:
				this.op = Operator.LT;
				break;
			case GE:
				this.op = Operator.GE;
				break;
			case LE:
				this.op = Operator.LE;
				break;
			default:
				throw new AssertionError(op);
		}
	}

	public Operator getOperator() {
		return op;
	}

	@Override
	public String getSignature() {
		return super.getSignature() + " (" + op + ")";
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
		result = prime * result + ((op == null) ? 0 : op.hashCode());
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
		final GeneralDBSqlCompare other = (GeneralDBSqlCompare)obj;
		if (op == null) {
			if (other.op != null)
				return false;
		}
		else if (!op.equals(other.op))
			return false;
		return true;
	}
}
