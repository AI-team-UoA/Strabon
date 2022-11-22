package org.openrdf.sail.generaldb.algebra;
 
import org.openrdf.sail.generaldb.algebra.base.GeneralDBQueryModelVisitorBase;
import org.openrdf.sail.generaldb.algebra.base.GeneralDBSqlExpr;
import org.openrdf.sail.generaldb.algebra.base.UnaryGeneralDBOperator;

/**
 * This class represents a unary operator in a SQL expression for returning various 
 * properties of a geometry.
 * 
 * @see {@link org.openrdf.query.algebra.evaluation.function.spatial.SpatialPropertyFunc} 
 * 
 * @author Manos Karpathiotakis <mk@di.uoa.gr>
 */
public class GeneralDBSqlSpatialProperty extends UnaryGeneralDBOperator {

	public GeneralDBSqlSpatialProperty(GeneralDBSqlExpr arg) {
		super(arg);
	}

	@Override
	public <X extends Exception> void visit(GeneralDBQueryModelVisitorBase<X> visitor)
	throws X
	{
		visitor.meet(this);
	}

}
