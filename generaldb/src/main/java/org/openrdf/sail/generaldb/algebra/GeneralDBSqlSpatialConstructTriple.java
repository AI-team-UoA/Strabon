/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.generaldb.algebra;

import org.openrdf.sail.generaldb.algebra.base.GeneralDBQueryModelVisitorBase;
import org.openrdf.sail.generaldb.algebra.base.GeneralDBSqlExpr;
import org.openrdf.sail.generaldb.algebra.base.TripleGeneralDBOperator;

public class GeneralDBSqlSpatialConstructTriple extends TripleGeneralDBOperator {
	private String resultType;

	public GeneralDBSqlSpatialConstructTriple(GeneralDBSqlExpr left, GeneralDBSqlExpr right, GeneralDBSqlExpr third, String resultType)
	{
		super(left, right, third);
		this.resultType = resultType;
	}

	@Override
	public <X extends Exception> void visit(GeneralDBQueryModelVisitorBase<X> visitor) throws X
	{
		visitor.meet(this);
	}

	public String getResultType() {
		return resultType;
	}

	public void setResultType(String resultType) {
		this.resultType = resultType;
	}
}
