/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.generaldb.algebra;

import org.openrdf.sail.generaldb.algebra.base.GeneralDBQueryModelVisitorBase;
import org.openrdf.sail.generaldb.algebra.base.GeneralDBSqlExpr;
import org.openrdf.sail.generaldb.algebra.base.UnaryGeneralDBOperator;

/**
 * The SQL IS CAST expression.
 * 
 * @author James Leigh
 * 
 */
public class GeneralDBSqlCast extends UnaryGeneralDBOperator {

	private int type;

	public GeneralDBSqlCast(GeneralDBSqlExpr arg, int type) {
		super(arg);
		this.type = type;
	}

	@Override
	public <X extends Exception> void visit(GeneralDBQueryModelVisitorBase<X> visitor)
		throws X
	{
		visitor.meet(this);
	}

	public int getType() {
		return type;
	}

	@Override
	public String getSignature() {
		return super.getSignature() + " AS " + type;
	}

}
