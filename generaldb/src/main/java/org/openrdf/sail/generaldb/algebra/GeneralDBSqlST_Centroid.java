/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * Copyright (C) 2013, Pyravlos Team
 *
 * http://www.strabon.di.uoa.gr/
 */
package org.openrdf.sail.generaldb.algebra;

import org.openrdf.sail.generaldb.algebra.base.GeneralDBSqlExpr;

/**
 * @author Charalampos Nikolaou <charnik@di.uoa.gr>
 *
 */
public class GeneralDBSqlST_Centroid extends GeneralDBSqlSpatialConstructUnary {

	public GeneralDBSqlST_Centroid(GeneralDBSqlExpr expr, String resultType) {
		super(expr, resultType);
	}

}
