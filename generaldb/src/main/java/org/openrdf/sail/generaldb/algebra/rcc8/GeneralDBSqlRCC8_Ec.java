/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.generaldb.algebra.rcc8;

 
import org.openrdf.sail.generaldb.algebra.GeneralDBSqlGeoSpatial;
import org.openrdf.sail.generaldb.algebra.base.BinaryGeneralDBOperator; 
import org.openrdf.sail.generaldb.algebra.base.GeneralDBQueryModelVisitorBase;
import org.openrdf.sail.generaldb.algebra.base.GeneralDBSqlExpr;

public class GeneralDBSqlRCC8_Ec extends GeneralDBSqlGeoSpatial{

	public GeneralDBSqlRCC8_Ec(GeneralDBSqlExpr left, GeneralDBSqlExpr right) {
		super(left, right);
	}
 
}