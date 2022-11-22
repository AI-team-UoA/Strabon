/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.generaldb.algebra;

  
import org.openrdf.sail.generaldb.algebra.base.GeneralDBQueryModelVisitorBase;
import org.openrdf.sail.generaldb.algebra.base.GeneralDBSqlExpr;

/** Addition for datetime metric functions
 * 
 * @author George Garbis <ggarbis@di.uoa.gr>
 * 
 */

public class GeneralDBSqlExtDiffDateTime extends GeneralDBSqlDateTimeMetricBinary {

	public GeneralDBSqlExtDiffDateTime(GeneralDBSqlExpr left, GeneralDBSqlExpr right) {
		super(left, right);
	}

	@Override
	public <X extends Exception> void visit(GeneralDBQueryModelVisitorBase<X> visitor)
		throws X
	{
		visitor.meet(this);
	}

}