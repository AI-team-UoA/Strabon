package org.openrdf.sail.generaldb.algebra;

import java.util.ArrayList;

import org.openrdf.sail.generaldb.algebra.base.BinaryGeneralDBOperator;
import org.openrdf.sail.generaldb.algebra.base.GeneralDBQueryModelVisitorBase;
import org.openrdf.sail.generaldb.algebra.base.GeneralDBSqlExpr;
import org.openrdf.sail.generaldb.algebra.base.UnaryGeneralDBOperator;

/** Addition for datetime metric functions
 * 
 * @author George Garbis <ggarbis@di.uoa.gr>
 * 
 */

public class GeneralDBSqlDateTimeMetricBinary extends BinaryGeneralDBOperator
{


	/*CONSTRUCTOR*/

	public GeneralDBSqlDateTimeMetricBinary(GeneralDBSqlExpr left, GeneralDBSqlExpr right) {
		super(left, right);
	}

	@Override
	public <X extends Exception> void visit(GeneralDBQueryModelVisitorBase<X> visitor)
	throws X
	{
		visitor.meet(this);
	}

}


