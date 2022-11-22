/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * 
 * Copyright (C) 2010, 2011, 2012, Pyravlos Team
 * 
 * http://www.strabon.di.uoa.gr/
 */
package org.openrdf.query.algebra.evaluation.function.spatial.stsparql.property;

import org.openrdf.query.algebra.evaluation.function.spatial.SpatialPropertyFunc;

import eu.earthobservatory.constants.GeoConstants;
 
/**
 * A spatial function for testing whether a geometry is empty.
 * 
 * @author Manos Karpathiotakis <mk@di.uoa.gr>
 */
public class IsEmptyFunc extends SpatialPropertyFunc {

	@Override
	public String getURI() {
		return GeoConstants.stSPARQLisEmpty;
	}
}
