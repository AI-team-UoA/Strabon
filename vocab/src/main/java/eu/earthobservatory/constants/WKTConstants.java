/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * 
 * Copyright (C) 2014 Pyravlos Team
 * 
 * http://www.strabon.di.uoa.gr/
 */

package eu.earthobservatory.constants;

/**
 * This is class contains constants that represent type of geometries
 * that can be found in a WKT literal, not necessarily compliant
 * with GeoSPARQL or strdf. This is to "catch"the cases where 
 * geometries are represented using WKT literals
 * 
 * @author Konstantina Bereta <konstantina.bereta@di.uoa.gr>
 */

public class WKTConstants {
	
	public static final String WKTPOINT = "POINT";
	public static final String WKTLINESTRING = "LINESTRING";
	public static final String WKTLINEARRING = "LINEARRING";
	public static final String WKTPOLYGON = "POLYGON";
	public static final String WKTMULTIPOINT = "MULTIPOINT";
	public static final String WKTMULTILINESTRING = "MULTILINESTRING";
	public static final String WKTMULTIPOLYGON = "MULTIPOLYGON";
	public static final String WKTGEOMETRYCOLLECTION = "GEOMETRYCOLLECTION";

}
