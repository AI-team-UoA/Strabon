/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.generaldb.algebra;

import org.openrdf.query.algebra.MathExpr.MathOp;
import org.openrdf.sail.generaldb.algebra.base.BinaryGeneralDBOperator;
import org.openrdf.sail.generaldb.algebra.base.GeneralDBQueryModelVisitorBase;
import org.openrdf.sail.generaldb.algebra.base.GeneralDBSqlExpr;

/**
 * The SQL subtraction (-) expression.
 * 
 * @author James Leigh
 * 
 */
public class GeneralDBSqlMathExpr extends BinaryGeneralDBOperator {

	private MathOp op;

	public GeneralDBSqlMathExpr(GeneralDBSqlExpr leftArg, MathOp op, GeneralDBSqlExpr rightArg) {
		super(leftArg, rightArg);
		this.op = op;
	}

	public MathOp getOperator() {
		return op;
	}

	@Override
	public <X extends Exception> void visit(GeneralDBQueryModelVisitorBase<X> visitor)
		throws X
	{
		visitor.meet(this);
	}
}
