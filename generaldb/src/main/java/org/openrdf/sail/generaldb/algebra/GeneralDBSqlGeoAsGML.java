/**
 * 
 */
package org.openrdf.sail.generaldb.algebra;

import org.openrdf.sail.generaldb.algebra.base.GeneralDBSqlExpr;

/**
 * @see {@link org.openrdf.query.algebra.evaluation.function.spatial.stsparql.property.AsGMLFunc}
 * 
 * @author Charalampos Nikolaou <charnik@di.uoa.gr>
 */
public class GeneralDBSqlGeoAsGML extends GeneralDBSqlSpatialProperty {

	public GeneralDBSqlGeoAsGML(GeneralDBSqlExpr expr) {
		super(expr);
	}
}