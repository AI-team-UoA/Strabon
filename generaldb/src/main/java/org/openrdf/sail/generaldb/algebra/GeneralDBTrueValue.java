/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.generaldb.algebra;

import org.openrdf.sail.generaldb.algebra.base.GeneralDBBooleanValue;
import org.openrdf.sail.generaldb.algebra.base.GeneralDBQueryModelVisitorBase;

/**
 * The boolean SQL expression of true.
 * 
 * @author James Leigh
 * 
 */
public class GeneralDBTrueValue extends GeneralDBBooleanValue {

	public GeneralDBTrueValue() {
		super(true);
	}

	@Override
	public <X extends Exception> void visit(GeneralDBQueryModelVisitorBase<X> visitor)
		throws X
	{
		visitor.meet(this);
	}
}
