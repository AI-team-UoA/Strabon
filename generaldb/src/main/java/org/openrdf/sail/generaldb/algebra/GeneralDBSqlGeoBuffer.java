/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.generaldb.algebra;


import org.openrdf.sail.generaldb.algebra.base.GeneralDBQueryModelVisitorBase;
import org.openrdf.sail.generaldb.algebra.base.GeneralDBSqlExpr;

public class GeneralDBSqlGeoBuffer extends GeneralDBSqlSpatialConstructTriple {

	public GeneralDBSqlGeoBuffer(GeneralDBSqlExpr left, GeneralDBSqlExpr right, GeneralDBSqlExpr third, String resultType)
	{
		super(left, right, third, resultType);
	}

	@Override
	public <X extends Exception> void visit(GeneralDBQueryModelVisitorBase<X> visitor) throws X
	{
		visitor.meet(this);
	}
}
