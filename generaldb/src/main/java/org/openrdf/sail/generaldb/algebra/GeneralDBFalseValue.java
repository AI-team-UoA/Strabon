/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.generaldb.algebra;

import org.openrdf.sail.generaldb.algebra.base.GeneralDBBooleanValue;
import org.openrdf.sail.generaldb.algebra.base.GeneralDBQueryModelVisitorBase;

/**
 * Represents the value false in an SQL expression.
 * 
 * @author James Leigh
 * 
 */
public class GeneralDBFalseValue extends GeneralDBBooleanValue {

	public GeneralDBFalseValue() {
		super(false);
	}

	@Override
	public <X extends Exception> void visit(GeneralDBQueryModelVisitorBase<X> visitor)
		throws X
	{
		visitor.meet(this);
	}
}
