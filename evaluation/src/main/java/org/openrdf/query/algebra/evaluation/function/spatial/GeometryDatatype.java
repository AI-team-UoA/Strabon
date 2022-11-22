/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * 
 * Copyright (C) 2012, 2013 Pyravlos Team
 * 
 * http://www.sextant.di.uoa.gr/
 */
package org.openrdf.query.algebra.evaluation.function.spatial;

import java.util.HashMap;
import java.util.Map;

import eu.earthobservatory.constants.GeoConstants;

/**
 * @author Charalampos Nikolaou <charnik@di.uoa.gr>
 */
public enum GeometryDatatype {
	/**
	 * stRDF WKT literal
	 */
	stRDFWKT(GeoConstants.WKT),
	
	/**
	 * GeoSPARQL WKT literal
	 */
	wktLiteral(GeoConstants.WKTLITERAL),
	
	/**
	 * GML literal
	 */
	GML(GeoConstants.GMLLITERAL),
	
	/**
	 * Unknown geometry format
	 */
	UNKNOWN("Unknown GeometryDatatype");
	
	/**
	 * The string representation of this format
	 */
	private String name;
	
	/**
	 * Map a string constant to a Format
	 */
	private static final Map<String, GeometryDatatype> stringToEnum = new HashMap<String, GeometryDatatype>();
	
	
	static { // initialize map from constant name to enum constant
		for (GeometryDatatype format : values()) {
			stringToEnum.put(format.toString(), format);
		}
	}
	
	/**
	 * GeometryDatatype constructor
	 * 
	 * @param name
	 */
	GeometryDatatype(String name) {
		this.name = name;
	}
	
	@Override
	public String toString() {
		return name;
	}
	
	/**
	 * Returns a GeometryDatatype enum given a format string.
	 * 
	 * @param lang
	 * @return
	 */
	public static GeometryDatatype fromString(String format) {
		return (stringToEnum.get(format) == null) ? UNKNOWN:stringToEnum.get(format);
	}
}
