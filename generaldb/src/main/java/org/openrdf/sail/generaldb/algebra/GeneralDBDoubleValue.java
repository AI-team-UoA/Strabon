/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.generaldb.algebra;

import org.openrdf.sail.generaldb.algebra.base.GeneralDBQueryModelVisitorBase;
import org.openrdf.sail.generaldb.algebra.base.GeneralDBSqlConstant;

/**
 * A static number in an SQL expression.
 * 
 * @author James Leigh
 * 
 */
public class GeneralDBDoubleValue extends GeneralDBSqlConstant<Double> {

	public GeneralDBDoubleValue(Double value) {
		super(value);
	}

	@Override
	public <X extends Exception> void visit(GeneralDBQueryModelVisitorBase<X> visitor)
		throws X
	{
		visitor.meet(this);
	}
}
