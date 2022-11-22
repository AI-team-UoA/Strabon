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
 * The SQL binary shift right (>>) expression.
 * 
 * @author James Leigh
 * 
 */
public class GeneralDBSqlShift extends UnaryGeneralDBOperator {

	private int shift;

	private int range;

	public GeneralDBSqlShift(GeneralDBSqlExpr arg, int shift, int range) {
		super(arg);
		this.shift = shift;
		this.range = range;
	}

	public int getRightShift() {
		return shift;
	}

	public int getRange() {
		return range;
	}

	@Override
	public <X extends Exception> void visit(GeneralDBQueryModelVisitorBase<X> visitor)
		throws X
	{
		visitor.meet(this);
	}

}
