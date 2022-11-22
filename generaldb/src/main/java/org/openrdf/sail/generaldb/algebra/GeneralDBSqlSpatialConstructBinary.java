package org.openrdf.sail.generaldb.algebra;

import org.openrdf.sail.generaldb.algebra.base.BinaryGeneralDBOperator;
import org.openrdf.sail.generaldb.algebra.base.GeneralDBQueryModelVisitorBase;
import org.openrdf.sail.generaldb.algebra.base.GeneralDBSqlExpr;

/**
 *
 * @author Manos Karpathiotakis <mk@di.uoa.gr>
 */
public class GeneralDBSqlSpatialConstructBinary extends BinaryGeneralDBOperator {
	private String resultType;

	public GeneralDBSqlSpatialConstructBinary(GeneralDBSqlExpr left, GeneralDBSqlExpr right, String resultType) {
		super(left, right);
		this.resultType = resultType;
	}

	@Override
	public <X extends Exception> void visit(GeneralDBQueryModelVisitorBase<X> visitor)
	throws X
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


