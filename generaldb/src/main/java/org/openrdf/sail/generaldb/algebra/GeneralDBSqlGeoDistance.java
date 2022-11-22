/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.generaldb.algebra;

  
import org.openrdf.sail.generaldb.algebra.base.GeneralDBQueryModelVisitorBase;
import org.openrdf.sail.generaldb.algebra.base.GeneralDBSqlExpr;

public class GeneralDBSqlGeoDistance extends GeneralDBSqlSpatialMetricTriple
{
	public GeneralDBSqlGeoDistance(GeneralDBSqlExpr left, GeneralDBSqlExpr right, GeneralDBSqlExpr third)
	{
		super(left, right, third);
	}

	@Override
	public <X extends Exception> void visit(GeneralDBQueryModelVisitorBase<X> visitor) throws X
	{
		visitor.meet(this);
	}
}