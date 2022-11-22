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
 * The vocabulary corresponding to the Simple Features Access, mainly
 * the SF namespace and the URIs for the class hierarchy.
 * 
 * @author Charalampos Nikolaou <charnik@di.uoa.gr>
 */
public class SimpleFeatures {

	public static final String NAMESPACE = "http://www.opengis.net/ont/sf#";
	
	public static final String Geometry 			= NAMESPACE + "Geometry";
	public static final String Point 				= NAMESPACE + "Point";
	public static final String Curve 				= NAMESPACE + "Curve";
	public static final String Surface 				= NAMESPACE + "Surface";
	public static final String GeometryCollection 	= NAMESPACE + "GeometryCollection";
	public static final String LineString 			= NAMESPACE + "LineString";
	public static final String Polygon 				= NAMESPACE + "Polygon";
	public static final String PolyhedralSurface	= NAMESPACE + "PolyhedralSurface";
	public static final String MultiSurface 		= NAMESPACE + "MultiSurface";
	public static final String MultiCurve 			= NAMESPACE + "MultiCurve";
	public static final String MultiPoint 			= NAMESPACE + "MultiPoint";
	public static final String Line 				= NAMESPACE + "Line";
	public static final String LinearRing 			= NAMESPACE + "LinearRing";
	public static final String Triangle 			= NAMESPACE + "Triangle";
	public static final String TIN		 			= NAMESPACE + "Tin";
	public static final String MultiPolygon 		= NAMESPACE + "MultiPolygon";
	public static final String MultiLineString 		= NAMESPACE + "MultiLineString";
}
