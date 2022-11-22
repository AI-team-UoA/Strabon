package org.openrdf.sail.generaldb.algebra;

import org.openrdf.sail.generaldb.algebra.base.BinaryGeneralDBOperator;
import org.openrdf.sail.generaldb.algebra.base.GeneralDBQueryModelVisitorBase;
import org.openrdf.sail.generaldb.algebra.base.GeneralDBSqlExpr;

public abstract class GeneralDBSqlGeoSpatial extends BinaryGeneralDBOperator
{

	public GeneralDBSqlGeoSpatial(GeneralDBSqlExpr left, GeneralDBSqlExpr right) {
		super(left, right);
	}

	@Override
	public <X extends Exception> void visit(GeneralDBQueryModelVisitorBase<X> visitor)
		throws X
	{
		visitor.meet(this);
	}

}
