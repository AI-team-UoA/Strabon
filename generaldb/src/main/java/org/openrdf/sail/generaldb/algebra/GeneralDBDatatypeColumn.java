/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.generaldb.algebra;

import org.openrdf.query.algebra.Var;
import org.openrdf.sail.generaldb.algebra.base.GeneralDBQueryModelVisitorBase;
import org.openrdf.sail.generaldb.algebra.base.GeneralDBValueColumnBase;

/**
 * Represents a variable's datatype value in an SQL expression.
 * 
 * @author James Leigh
 * 
 */
public class GeneralDBDatatypeColumn extends GeneralDBValueColumnBase {

	public GeneralDBDatatypeColumn(Var var) {
		super(var);
	}

	public GeneralDBDatatypeColumn(GeneralDBColumnVar var) {
		super(var);
	}

	@Override
	public <X extends Exception> void visit(GeneralDBQueryModelVisitorBase<X> visitor)
		throws X
	{
		visitor.meet(this);
	}

}
