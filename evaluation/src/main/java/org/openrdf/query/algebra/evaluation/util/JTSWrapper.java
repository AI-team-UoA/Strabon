/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * 
 * Copyright (C) 2010, 2011, 2012, Pyravlos Team
 * 
 * http://www.strabon.di.uoa.gr/
 */
package org.openrdf.query.algebra.evaluation.util;

import java.io.StringReader;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.openrdf.query.algebra.evaluation.function.spatial.AbstractWKT;
import org.openrdf.query.algebra.evaluation.function.spatial.WKTHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKBReader;
import com.vividsolutions.jts.io.WKBWriter;
import com.vividsolutions.jts.io.WKTReader;
import com.vividsolutions.jts.io.WKTWriter;
import com.vividsolutions.jts.io.gml2.GMLReader;
import com.vividsolutions.jts.io.gml2.GMLWriter;

import eu.earthobservatory.constants.GeoConstants;

/**
 * This class is a singleton and provides access to the readers/writers
 * of Java Topology Suite. 
 * 
 * @author Charalampos Nikolaou <charnik@di.uoa.gr>
 */
public class JTSWrapper {
	
	private static final Logger logger = LoggerFactory.getLogger(org.openrdf.query.algebra.evaluation.util.JTSWrapper.class);

	/**
	 *  Single instance of JTSReaders
	 */
	private static JTSWrapper instance;
	
	/**
	 * Reader for WKT
	 */
	private WKTReader wktr;
	
	/**
	 * Writer for WKT
	 */
	private WKTWriter wktw;
	
	/**
	 * Reader for WKB
	 */
	private WKBReader wkbr;
	
	/**
	 * Writer for WKB
	 */
	private WKBWriter wkbw;
	
	/**
	 * Writer for GML
	 */
	private GMLWriter gmlw;
	
	/**
	 * Stores the number of decimal places for the
	 * default precision model of JTS. 
	 */
	private int numDecimalPlaces;

	private JTSWrapper() {
		// use a private constructor to force call of getInstance method and forbid subclassing
		wktr = new WKTReader();
		wktw = new WKTWriter();
		wkbr = new WKBReader();
		wkbw = new WKBWriter(); // PostGIS
//		wkbw = new WKBWriter(2, WKBConstants.wkbXDR); // MonetDB
		gmlw = new GMLWriter();

		numDecimalPlaces = (new PrecisionModel()).getMaximumSignificantDigits();
	}
	
	public static synchronized JTSWrapper getInstance() {
		if (instance == null) {
			instance = new JTSWrapper();
		}
		return instance;
	}
	
	protected CoordinateReferenceSystem getEPSG_CRS(int srid) throws NoSuchAuthorityCodeException, FactoryException {
//		if (srid == GeoConstants.WGS84_LONG_LAT_SRID) {
//			return DefaultGeographicCRS.WGS84;
//			
//		} else { // otherwise lookup for EPSG code
			// TODO: is there a way to be more general (than EPSG)?
			return CRS.decode("EPSG:" + srid);
//		}
	}
	
	public synchronized Geometry WKTread(String wkt) throws ParseException {
		Geometry geometry = wktr.read(wkt);
		
		 /* SPECIAL NOTICE: When <tt>wkt</tt> is in GML,
		  *  WKTReader does not throw any ParseException! It silently
		  *  returns NULL. That's why we have the following check.    */
		if (geometry == null) {
			throw new ParseException("Invalid WKT.");
		}
		return geometry;
	}
	
	public synchronized String WKTwrite(Geometry geom) {
		return wktw.write(geom);
	}
	
	public synchronized Geometry WKBread(byte[] bytes) throws ParseException {
		return wkbr.read(bytes);
	}
	
	public synchronized byte[] WKBwrite(Geometry geom) {
		return wkbw.write(geom); // PostGIS
		// MonetDB
//		byte[] temp = wkbw.write(geom);
//		temp[0] = 1;
//		return temp;
		//
		
	}
	
	/**
	 * Transforms the given geometry in the sourceSRID to a geometry in the
	 * targetSRID.
	 * 
	 * TODO: Is there any synchronization problem?
	 * 
	 * @param input
	 * @param sourceSRID
	 * @param targetSRID
	 * @return
	 */
	public Geometry transform(Geometry input, int sourceSRID, int targetSRID) {
		// the geometry to return
		Geometry output = input;
		
		if (sourceSRID != targetSRID) {
			CoordinateReferenceSystem sourceCRS = null;
			CoordinateReferenceSystem targetCRS = null;
			
			MathTransform transform;
			try {
				sourceCRS = getEPSG_CRS(sourceSRID);
				targetCRS = getEPSG_CRS(targetSRID);
				transform = CRS.findMathTransform(sourceCRS, targetCRS, true);

				output = JTS.transform(input, transform);
				output.setSRID(targetSRID);
				
			} catch (FactoryException e) {
				logger.error("[Strabon.JTSWrapper] Got FactoryException during transformation.", e);
				
			} catch (MismatchedDimensionException e) {
				logger.error("[Strabon.JTSWrapper] Got MismatchedDimensionExtension during transformation.", e);
				
			} catch (TransformException e) {
				logger.error("[Strabon.JTSWrapper] Transformation from SRID {} to SRID {} is not possible.", sourceSRID, targetSRID);
				
			}
		}
		
		return output;
	}
	
	/**
	 * Parses and returns a {@link Geometry} object constructed from the given GML representation.
	 * 
	 * We do not use class {@link GMLReader} that does the job transparently (i.e. we wouldn't 
	 * need to load the XML parser of JTS on our own), since in such a case we are enforced to 
	 * mention a specific SRID, otherwise the #getSRID() function returns a value of 0. Doing 
	 * it the hard way, the SRID field is filled with the SRID that is present in the GML 
	 * representation. 
	 * 
	 * @param gml
	 * @return
	 * @throws JAXBException
	 */
	public Geometry GMLread(String gml) throws JAXBException {
		StringReader reader = new StringReader(gml);

        JAXBContext context = JAXBContext.newInstance("org.jvnet.ogc.gml.v_3_1_1.jts");	
        Unmarshaller unmarshaller = context.createUnmarshaller();
        Geometry geometry = (Geometry) unmarshaller.unmarshal(reader);
		
		reader.close();
		
		/**
		 * When unmarshalling GML, GML-JTS tries to parse srsName as EPSG code using the following patterns:
		 * 	EPSG:{0,number,integer}
         *  urn:ogc:def:crs:EPSG::{0,number,#}
         *  urn:ogc:def:crs:EPSG:{1}:{0,number,#}
         *  urn:x-ogc:def:crs:EPSG::{0,number,#}
         *  urn:x-ogc:def:crs:EPSG:{1}:{0,number,#}
         *  http://www.opengis.net/gml/srs/epsg.xml#\{0,number,#}
		 * If srsName matched one of the pattern, it will be parsed as assigned to the SRID property of the JTS geometry.
         * If none of the patterns matched, srsName will be simply saved to the UserData property of the JTS geometry.
		 */
		
		/**
		 * SOLUTION: To deal with the fact that the srsName might not come in one of the supported formats,
		 * we check the userData variable of the geometry after the unmarshal call. If it is not null,
		 * then we have the string that represents the srid and we need to extract it and set it in the geometry inastance.
		 */
		if (geometry.getUserData() != null) {
			geometry.setSRID(WKTHelper.getSRID((String)geometry.getUserData()));
	        return geometry;
		}
		else {
			return geometry;
		}		
	}
	
	public synchronized String GMLWrite(Geometry geom) {
		return gmlw.write(geom);
	}

	/**
	 * Returns the number of decimal places corresponding to the
	 * precision model that is used by default by the JTS library.
	 * 
	 * @return
	 */
	public int getPrecision() {
		return numDecimalPlaces;
	}
}
