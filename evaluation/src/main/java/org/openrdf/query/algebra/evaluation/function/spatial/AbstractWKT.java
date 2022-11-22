/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * 
 * Copyright (C) 2013, Pyravlos Team
 * 
 * http://www.strabon.di.uoa.gr/
 */
package org.openrdf.query.algebra.evaluation.function.spatial;

import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.earthobservatory.constants.GeoConstants;


/**
 * This class generalizes WKT literal values that can be given according
 * to the specification of stRDF/stSPARQL or GeoSPARQL. Notice that no
 * actual parsing is carried out, so the representation at this point
 * might not be valid.
 * 
 * @author Charalampos Nikolaou <charnik@di.uoa.gr>
 * @author Panayiotis Smeros <psmeros@di.uoa.gr>
 */
public class AbstractWKT {

	private static Logger logger = LoggerFactory.getLogger(org.openrdf.query.algebra.evaluation.function.spatial.AbstractWKT.class);
	
	/**
	 * WKT representation for an empty geometry
	 * 
	 * When used with POINT instead of MULTIPOLYGON, JTS throws an
	 * Illegal argument exception, since empty geometries for points
	 * are not represented in WKB (see 
	 * http://tsusiatsoftware.net/jts/javadoc/com/vividsolutions/jts/io/WKBWriter.html).
	 * 
	 * EMPTY_GEOM is present here to address Req. 13 of GeoSPARQL for empty geometries.
	 * However, we act in the same way for strdf:WKT.
	 */
	protected final String EMPTY_GEOM = "MULTIPOLYGON EMPTY"; 
	
	/** 
	 * The datatype of this WKT literal
	 * 
	 * Should be either {@link GeoConstants.WKT} or {@link GeoConstants.WKTLITERAL} 
	 */
	private String datatype;
	
	/**
	 * true when this WKT is given according to the specification of stRDF/stSPARQL
	 * false when it is given according to GeoSPARQL 
	 */
	private boolean isstRDFWKT;
	
	/**
	 * The actual/standard WKT value as read by JTSWrapper 
	 */
	private String wkt;
	
	/**
	 * The SRID for the represented geometry
	 */
	private int srid;
	
	/**
	 * NOTICE: no check of the validity of the supplied WKT is done
	 * 
	 * @param literalValue
	 * @param datatype
	 */
	public AbstractWKT(String literalValue, String datatype) {
		this.datatype = datatype;
		
		if (GeoConstants.WKT.equals(datatype)) { // stRDF:WKT
			isstRDFWKT = true;
			parsestRDFWKT(literalValue);
			
		} else if (GeoConstants.WKTLITERAL.equals(datatype)) { // wktLiteral
			isstRDFWKT = false;
			parseWKTLITERAL(literalValue);
			
		} // naturally, whoever creates AbstractWKT instances, 
		// should have either of the two datatypes, thus we don't check for errors
	}
	
	/**
	 * This constructor tries to guess the datatype of the WKT literal.
	 * It should be used only when one does not have such information
	 * available.
	 * 
	 * NOTICE 1: it is not guaranteed that guessing will fill-in the
	 * datatype, but it is guaranteed that parsing will be done
	 * correctly in any case (well, except for illegal cases).
	 * 
	 * NOTICE 2: no check of the validity of the supplied WKT is done
	 * 
	 * @param literalValue
	 */
	public AbstractWKT(String literalValue) {
		datatype = null;
		
		if (literalValue.lastIndexOf(WKTHelper.STRDF_SRID_DELIM) > 0) { // strdf:WKT
			datatype = GeoConstants.WKT;
		
			isstRDFWKT = true;
			parsestRDFWKT(literalValue);
			
		} else { // wktLiteral or plain WKT
			if (literalValue.charAt(0) == '<') { // starts with a URI, assume wktLiteral
				datatype = GeoConstants.WKTLITERAL;
				
				isstRDFWKT = false;
				parseWKTLITERAL(literalValue);
				
			} else { // cannot guess, only WKT representation was given
				parsestRDFWKT(literalValue);	
			}
		}
	}
	
	/**
	 * Parses a WKT literal according to the specification of stRDF/stSPARQL.
	 * The literal value may (not) specify the URI of a spatial reference system.
	 * 
	 * @param literalValue
	 */
	private void parsestRDFWKT(String literalValue) {
		if (literalValue.trim().length() == 0) {
			literalValue = EMPTY_GEOM;
		}
		
		// we already have this case in {@link WKTHelper}
		wkt = WKTHelper.getWithoutSRID(literalValue);
		srid = WKTHelper.getSRID(literalValue);
	}
	
	private void parseWKTLITERAL(String literalValue) {
		wkt = literalValue.trim();
		srid = GeoConstants.default_GeoSPARQL_SRID;
		
		if (wkt.length() == 0) { // empty geometry
			wkt = EMPTY_GEOM;
		}
		
		if (wkt.charAt(0) == '<') {// if a CRS URI is specified
			int uriIndx = wkt.indexOf('>');
			
			// FIXME: handle invalid URIs
			URI crs = URI.create(wkt.substring(1, uriIndx));
			
			/*
			if (GeoConstants.CRS84_URI.equals(crs.toString())) {
				srid = GeoConstants.EPSG4326_SRID;
			}
			else {
				srid = getEPSG_SRID(crs.toString());
			}*/
			
			// FIXME: this code assumes an EPSG URI or CRS84
			srid = WKTHelper.getSRID(crs.toString());
							
			// trim spaces after URI and get the WKT value
			wkt = wkt.substring(uriIndx + 1).trim();
		}
	}
	
	public String getWKT() {
		return wkt;
	}
	
	public int getSRID() {
		return srid;
	}
	
	public String getDatatype() {
		return datatype;
	}
	
	public boolean isstRDFWKT() {
		return isstRDFWKT;
	}
	
	/**
	 * Returns the SRID corresponding to the URI of the given CRS. We assume
	 * EPSG URIs only.
	 * 
	 * In case of an error, the default is returned, i.e., {@link GeoConstants#defaultSRID}.
	 * 
	 * @param wkt
	 * @return
	 
	protected Integer getEPSG_SRID(String wkt) {
		int srid = GeoConstants.defaultSRID;
		
		try {
			srid = Integer.parseInt(wkt.substring(wkt.lastIndexOf('/') + 1).replace(">", ""));

		} catch (NumberFormatException e) {
			logger.warn("[Strabon.AbstractWKT] Was expecting an integer. The URL of the EPSG SRID was {}. Continuing with the default SRID, {}", wkt, srid);
			
		}
		
		return srid;
	}
	*/
}
