/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.generaldb.algebra;

import org.openrdf.sail.generaldb.algebra.base.GeneralDBQueryModelVisitorBase;
import org.openrdf.sail.generaldb.algebra.base.GeneralDBSqlConstant;

/**
 * An SQL VARCHAR expression.
 * 
 * @author James Leigh
 * 
 */
public class GeneralDBStringValue extends GeneralDBSqlConstant<String> {

	public GeneralDBStringValue(String value) {
		super(value);
		assert value != null;
	}

	@Override
	public <X extends Exception> void visit(GeneralDBQueryModelVisitorBase<X> visitor)
		throws X
	{
		visitor.meet(this);
	}
}
