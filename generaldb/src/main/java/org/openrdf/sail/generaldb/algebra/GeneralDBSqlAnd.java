/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.generaldb.algebra;

import org.openrdf.sail.generaldb.algebra.base.BinaryGeneralDBOperator;
import org.openrdf.sail.generaldb.algebra.base.GeneralDBQueryModelVisitorBase;
import org.openrdf.sail.generaldb.algebra.base.GeneralDBSqlExpr;

/**
 * The SQL AND expression.
 * 
 * @author James Leigh
 * 
 */
public class GeneralDBSqlAnd extends BinaryGeneralDBOperator {

	public GeneralDBSqlAnd(GeneralDBSqlExpr leftArg, GeneralDBSqlExpr rightArg) {
		super(leftArg, rightArg);
	}

	@Override
	public <X extends Exception> void visit(GeneralDBQueryModelVisitorBase<X> visitor)
		throws X
	{
		visitor.meet(this);
	}
}
