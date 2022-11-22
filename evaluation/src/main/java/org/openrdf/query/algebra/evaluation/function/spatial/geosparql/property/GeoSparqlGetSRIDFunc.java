/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * 
 * Copyright (C) 2013, Pyravlos Team
 * 
 * http://www.strabon.di.uoa.gr/
 */
package org.openrdf.query.algebra.evaluation.function.spatial.geosparql.property;

import org.openrdf.query.algebra.evaluation.function.spatial.SpatialPropertyFunc;

import eu.earthobservatory.constants.GeoConstants;

/**
 * Implementation of the <code>geof:getSRID(geom: ogc:geomLiteral): xsd:anyURI</code> 
 * function of GeoSPARQL.
 * 
 * @author Charalampos Nikolaou <charnik@di.uoa.gr>
 *
 */
public class GeoSparqlGetSRIDFunc extends SpatialPropertyFunc {

	@Override
	public String getURI() {
		return GeoConstants.geoSparqlGetSRID;
	}
}
