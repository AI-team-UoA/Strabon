/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * 
 * Copyright (C) 2013, Pyravlos Team
 * 
 * http://www.strabon.di.uoa.gr/
 */
package org.openrdf.query.algebra.evaluation.function.spatial.postgis.construct;

import org.openrdf.query.algebra.evaluation.function.spatial.SpatialConstructFunc;

import eu.earthobservatory.vocabulary.PostGIS;

/**
 * This SPARQL extension function corresponds to the PostGIS 
 * <code>ST_Centroid(geometry geom)</code> function as described
 * in <a>http://postgis.org/docs/ST_Centroid.html</a>.
 *
 * @author Charalampos Nikolaou <charnik@di.uoa.gr>
 *
 */
public class Centroid extends SpatialConstructFunc {

	@Override
	public String getURI() {
		return PostGIS.ST_CENTROID;
	}
}
