/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * 
 * Copyright (C) 2013, Pyravlos Team
 * 
 * http://www.strabon.di.uoa.gr/
 */
package eu.earthobservatory.vocabulary;

/**
 * @author Charalampos Nikolaou <charnik@di.uoa.gr>
 */
public class PostGIS {

	public static final String NAMESPACE = "http://postgis.net/";
	
	/** Construct functions (binary) **/
	public static final String ST_MAKELINE = NAMESPACE + "ST_MakeLine";
	
	/** Construct functions (unary) **/
	public static final String ST_CENTROID = NAMESPACE + "ST_Centroid";
}
