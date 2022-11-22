/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.generaldb.algebra.sf;

 
import org.openrdf.sail.generaldb.algebra.GeneralDBSqlGeoSpatial;
import org.openrdf.sail.generaldb.algebra.base.BinaryGeneralDBOperator; 
import org.openrdf.sail.generaldb.algebra.base.GeneralDBQueryModelVisitorBase;
import org.openrdf.sail.generaldb.algebra.base.GeneralDBSqlExpr;

public class GeneralDBSqlSF_Equals extends GeneralDBSqlGeoSpatial{

	public GeneralDBSqlSF_Equals(GeneralDBSqlExpr left, GeneralDBSqlExpr right) {
		super(left, right);
	}
 
}