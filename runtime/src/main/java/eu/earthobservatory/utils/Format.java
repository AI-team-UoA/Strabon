/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * 
 * Copyright (C) 2010, 2011, 2012, Pyravlos Team
 * 
 * http://www.strabon.di.uoa.gr/
 */
package eu.earthobservatory.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * This enumeration type represents the available formats
 * for the results of the evaluation of a SPARQL query.
 * 
 * @author Charalampos Nikolaou <charnik@di.uoa.gr>
 */
public enum Format {

	/**
	 * Default format
	 */
	DEFAULT(""),
	
	/**
	 * XML format
	 */
	XML("XML"),
	
	/**
	 * KML format
	 */
	KML("KML"),
	
	/**
	 * KMZ format (compressed KML)
	 */
	KMZ("KMZ"),
	
	/**
	 * GeoJSON format
	 */
	GEOJSON("GeoJSON"),
	
	/**
	 * Format for experiments
	 */
	EXP("EXP"),

	/**
	 * Tuple query object
	 */
	TUQU("TUPLEQUERY"),
	
	/**
	 * HTML format
	 */
	HTML("HTML"),
	
	/**
	 * TSV (tab-separated values) format
	 */
	TSV("TSV"),
	
	/**
	 * Sesame's JSON format 
	 */
	SESAME_JSON("SPARQL/JSON"), 
	
	/**
	 * Sesame's XML format
	 */
	SESAME_XML("SPARQL/XML"),
	
	/**
	 * Sesame's Binary format
	 */
	SESAME_BINARY("BINARY"),
	
	/**
	 * Sesame's CSV format
	 */
	SESAME_CSV("SPARQL/CSV"),
	
	/**
	 * Sesame's TSV format
	 */
	SESAME_TSV("SPARQL/TSV"),
	
	/**
	 * Invalid format.
	 */
	INVALID("INVALID"),
	
	PIECHART("PIECHART"),
	
	COLUMNCHART("COLUMNCHART"),
	
	AREACHART("AREACHART");
	
	/**
	 * The string representation of this format
	 */
	private String name;
	
	/**
	 * Map a string constant to a Format
	 */
	private static final Map<String, Format> stringToEnum = new HashMap<String, Format>();
	
	
	static { // initialize map from constant name to enum constant
		for (Format format : values()) {
			// add both upper- and lower-case versions of the format 
			stringToEnum.put(format.toString(), format);
			stringToEnum.put(format.toString().toLowerCase(), format);
	
		}
	}
	
	/**
	 * Format constructor.
	 * 
	 * @param name
	 */
	Format(String name) {
		this.name = name;
	}
	
	@Override
	public String toString() {
		return name;
	}
	
	/**
	 * Returns a Format enum given a format string.
	 * 
	 * @param lang
	 * @return
	 */
	public static Format fromString(String format) {
		return (stringToEnum.get(format) == null) ? INVALID:stringToEnum.get(format);
	}
}
