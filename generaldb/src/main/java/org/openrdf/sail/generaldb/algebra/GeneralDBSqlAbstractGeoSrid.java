/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * 
 * Copyright (C) 2012, 2013 Pyravlos Team
 * 
 * http://www.sextant.di.uoa.gr/
 */
package org.openrdf.sail.generaldb.algebra;

import org.openrdf.sail.generaldb.algebra.base.GeneralDBQueryModelVisitorBase;
import org.openrdf.sail.generaldb.algebra.base.GeneralDBSqlExpr;

/**
 * This class is used as a superclass for the classes {@link GeneralDBSqlGeoSPARQLSrid}
 * and {@link GeneralDBSqlGeoSrid}. This is needed because the corresponding functions
 * differ only in the types of the returning results, so we do not want to  duplicate
 * code for computing them. Instead, we will compute them based on this abstract
 * function, and then, when converting the result set, we will differentiate our
 * behavior based on the actual instantiation.   
 * 
 * @author Charalampos Nikolaou <charnik@di.uoa.gr>
 */
public class GeneralDBSqlAbstractGeoSrid extends GeneralDBSqlSpatialProperty {

	public GeneralDBSqlAbstractGeoSrid(GeneralDBSqlExpr expr) {
		super(expr);
	}

	@Override
	public <X extends Exception> void visit(GeneralDBQueryModelVisitorBase<X> visitor)
		throws X
	{
		visitor.meet(this);
	}
}
