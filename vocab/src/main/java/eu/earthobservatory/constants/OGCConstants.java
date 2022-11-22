/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * 
 * Copyright (C) 2012, Pyravlos Team
 * 
 * http://www.strabon.di.uoa.gr/
 */
package eu.earthobservatory.constants;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * This class contains several OGC constants that are mainly URIs.
 * These can be found at the OGC Definition Service located at
 * <a>http://www.opengis.net/def/</a>.
 * 
 * @author Charalampos Nikolaou <charnik@di.uoa.gr>
 */
public class OGCConstants {

	/**
	 * Namespace for OGC Units of Measure 1.0
	 */
	public static final String UOM	= "http://www.opengis.net/def/uom/OGC/1.0/";
	
	public static final String OGCdegree		= UOM + "degree";
	public static final String OGCgridSpacing 	= UOM + "GridSpacing";
	public static final String OGCmetre			= UOM + "metre";
	public static final String OGCradian		= UOM + "radian";
	public static final String OGCunity			= UOM + "unity";
	
	public static final List<String> supportedUnitsOfMeasure = new ArrayList<String>();
	
	static {
		Class<OGCConstants> geoConstants = OGCConstants.class;	
		
		try {
			Field[] field = geoConstants.getDeclaredFields();
		
			for (int i = 0; i < field.length; i++) {
				if (field[i].getName().startsWith("OGC")) {
					supportedUnitsOfMeasure.add((String) field[i].get(null));
				}
			}
					
		} catch (SecurityException e) {
			// suppress exception; it should not reach here
		} catch (IllegalArgumentException e) {
			// suppress exception; it should not reach here 
		} catch (IllegalAccessException e) {
			// suppress exception; it should not reach here
		}
	}
}
