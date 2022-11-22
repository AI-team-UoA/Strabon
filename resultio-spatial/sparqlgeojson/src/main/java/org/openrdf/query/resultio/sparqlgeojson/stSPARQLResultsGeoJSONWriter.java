/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * 
 * Copyright (C) 2010, 2011, 2012, Pyravlos Team
 * 
 * http://www.strabon.di.uoa.gr/
 */
package org.openrdf.query.resultio.sparqlgeojson;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Collection;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geojson.feature.FeatureJSON;
import org.geotools.geojson.geom.GeometryJSON;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.rio.RioSetting;
import org.openrdf.rio.WriterConfig;
import org.openrdf.query.resultio.QueryResultFormat;
import org.openrdf.query.Binding;
import org.openrdf.query.BindingSet;
import org.openrdf.query.TupleQueryResultHandlerException;
import org.openrdf.query.algebra.evaluation.function.spatial.AbstractWKT;
import org.openrdf.query.algebra.evaluation.function.spatial.StrabonPolyhedron;
import org.openrdf.query.algebra.evaluation.util.JTSWrapper;
import org.openrdf.query.resultio.TupleQueryResultFormat;
import org.openrdf.query.resultio.TupleQueryResultWriter;
import org.openrdf.query.resultio.stSPARQLQueryResultFormat;
import org.openrdf.sail.generaldb.model.GeneralDBPolyhedron;
import org.openrdf.sail.generaldb.model.XMLGSDatatypeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.locationtech.jts.geom.Geometry;

import eu.earthobservatory.constants.GeoConstants;

/**
 * A TupleQueryResultWriter that writes query results in the <a
 * href="http://www.geojson.org/geojson-spec.html/">GeoJSON Format</a>.
 *
 * @author Manos Karpathiotakis <mk@di.uoa.gr>
 * @author Charalampos Nikolaou <charnik@di.uoa.gr>
 */
public class stSPARQLResultsGeoJSONWriter implements TupleQueryResultWriter {

	private static final Logger logger = LoggerFactory.getLogger(org.openrdf.query.resultio.sparqlgeojson.stSPARQLResultsGeoJSONWriter.class);

	/**
	 * The underlying output stream to write
	 */
	private OutputStream out;

	/**
	 * Set a Feature Collection
	 */
	private DefaultFeatureCollection sfCollection;

	/**
	 * The wrapper of JTS library
	 */
	private JTSWrapper jts;

	/**
	 * Keep track of the number of results
	 */
	private int nresults;

	/**
	 * The class to use for serializing to GeoJSON
	 */
	private FeatureJSON fjson;

	/**
	 * Keep track of the number of features
	 */
	private int nfeatures;

	public stSPARQLResultsGeoJSONWriter(OutputStream out) {
		this.out = out;

		// set the feature collection
		sfCollection = new DefaultFeatureCollection("geomOutput",null);

		// get the instance of JTSWrapper
		jts = JTSWrapper.getInstance();
		
		// initialize results/features
		nresults = 0;
		nfeatures = 0;
	}

	@Override
	public void startQueryResult(List<String> bindingNames) throws TupleQueryResultHandlerException {
		fjson = new FeatureJSON(new GeometryJSON(jts.getPrecision()));
		fjson.setEncodeFeatureCRS(true);
	}

	@Override
	public void endQueryResult() throws TupleQueryResultHandlerException {
		try {
			fjson.writeFeatureCollection(sfCollection, out);
			out.write("\n".getBytes(Charset.defaultCharset()));
			
			// write a warning when there are no features in the answer
			if (nfeatures < nresults) {
				logger.warn("[Strabon.GeoJSONWriter] No spatial binding found in the result, hence the result is empty eventhough query evaluation produced {} results. GeoJSON requires that at least one binding maps to a geometry.", nresults);
				
			}
		} catch (IOException e) {
			throw new TupleQueryResultHandlerException(e);
		}
	}

	@Override
	public void handleSolution(BindingSet bindingSet) throws TupleQueryResultHandlerException {
		try {
			nresults++;
			
			// list keeping binding names that are not binded to geometries
			ArrayList<String> properties = new ArrayList<String>();
			
			// list keeping values for binding names
			ArrayList<Value> values = new ArrayList<Value>();
			
			// list keeping the features of the result
			ArrayList<SimpleFeatureTypeBuilder> features = new ArrayList<SimpleFeatureTypeBuilder>();
			
			// list keeping the geometries of features
			ArrayList<Geometry> geometries = new ArrayList<Geometry>();
			
			// parse binding set
			for (Binding binding : bindingSet) {
				Value value = binding.getValue();
				
				if (XMLGSDatatypeUtil.isGeometryValue(value)) {
					// it's a spatial value
					if (logger.isDebugEnabled()) {
						logger.debug("[Strabon.GeoJSON] Found geometry: {}", value);
					}
					
					nfeatures++;
					
					// we need the geometry and the SRID 
					Geometry geom = null;
					int srid = -1;
					
					if (value instanceof GeneralDBPolyhedron) {
						GeneralDBPolyhedron dbpolyhedron = (GeneralDBPolyhedron) value;
						
						geom = dbpolyhedron.getPolyhedron().getGeometry();
						srid = dbpolyhedron.getPolyhedron().getGeometry().getSRID();
						
					} else if (value instanceof StrabonPolyhedron) { // spatial case from new geometry construction (SELECT) 
						StrabonPolyhedron poly = (StrabonPolyhedron) value;
						geom = poly.getGeometry();
						srid = geom.getSRID();
							
					} else { // spatial literal WKT or GML
						// get the textual representation of the geometry (WKT or GML)
						String geoText = value.stringValue();
						Literal literal = (Literal) value;
						
						if (XMLGSDatatypeUtil.isWKTLiteral(literal)) {// WKT
							AbstractWKT awkt = new AbstractWKT(geoText, literal.getDatatype().stringValue());
							
							// get its geometry
							geom = jts.WKTread(awkt.getWKT());
							
							// get its SRID
							srid = awkt.getSRID();
							
						} else { // GML
							// get its geometry
							geom = jts.GMLread(geoText);
							
							// get its SRID
							srid = geom.getSRID();
						}
					}
					
					SimpleFeatureTypeBuilder sftb = new SimpleFeatureTypeBuilder();
					sftb.setName("Feature_" + nresults + "_" + nfeatures);
					sftb.add("geometry", Geometry.class);
					
					// EPSG:4326 Long/Lat) is the default CRS for GeoJSON features
					// we transform explicitly, because searching for "EPSG:<code>" CRSs is
					// not the preferred way for GeoJSON (see here 
					// http://geojson.org/geojson-spec.html#coordinate-reference-system-objects). 
					// Instead the OGC CRS URNs should be preferred.
					geom = jts.transform(geom, srid, GeoConstants.EPSG4326_SRID);
					//sftb.setCRS(CRS.decode("EPSG:" + srid));
					//sftb.setSRS("EPSG:" + srid);
					
					// add the feature in the list of features
					features.add(sftb);
					
					// add the geometry of the feature in the list of geometries
					geometries.add(geom);
					
				} else { // URI, BlankNode, or Literal other than geometry
					if (logger.isDebugEnabled()) {
						logger.debug("[Strabon.GeoJSON] Found resource: {}", value);
					}
					
					properties.add(binding.getName());
					values.add(value);
				}
			}
			
			// construct the feature of the result
			for (int i = 0; i < features.size(); i++) {
				SimpleFeatureTypeBuilder sftb = features.get(i);
				
				// add the properties
				for (int p = 0; p < properties.size(); p++) {
					Value val = values.get(p);
					
					if (val instanceof Literal) {
						Literal lit = (Literal) val;
						URI datatype = lit.getDatatype();
						
						if (XMLGSDatatypeUtil.isNumericDatatype(datatype)) {
							sftb.add(properties.get(p), Number.class);
							
						} else if (XMLGSDatatypeUtil.isCalendarDatatype(datatype)) {
							sftb.add(properties.get(p), Calendar.class);
							
						} else if (XMLGSDatatypeUtil.isBooleanDatatype(datatype)) {
							sftb.add(properties.get(p), Boolean.class);
							
						} else { // fallback to String
							sftb.add(properties.get(p), String.class);	
						}
						
					} else { // fallback to String
						sftb.add(properties.get(p), String.class);
					}
					
				}
				
				SimpleFeatureType featureType = sftb.buildFeatureType();
				SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(featureType);				
				
				// add the geometry to the builder of the feature
				featureBuilder.add(geometries.get(i));
				
				// add the values to the builder of the feature
				for (int v = 0; v < values.size(); v++) {
					featureBuilder.add(values.get(v).stringValue());
				}
				
				SimpleFeature feature = featureBuilder.buildFeature(null);
				sfCollection.add(feature);
			}
			
		} catch (Exception e) {
			throw new TupleQueryResultHandlerException(e);
		}
					
	}

	@Override
	public TupleQueryResultFormat getTupleQueryResultFormat() {
		return stSPARQLQueryResultFormat.GEOJSON;
	}

	@Override
	public void handleLinks(List<String> linkUrls){}

	@Override
	public void handleBoolean(boolean value){}

	@Override
	public Collection<RioSetting<?>> getSupportedSettings(){return null;}

	@Override
	public WriterConfig	getWriterConfig(){ return null;}

	@Override
	public void	setWriterConfig(WriterConfig config) {}

	@Override
	public void startHeader(){}

	@Override
	public void startDocument() {}

	@Override
	public void	endHeader(){}

	@Override
	public void	handleStylesheet(String stylesheetUrl) {}

	@Override
	public void	handleNamespace(String prefix, String uri) {}

	@Override
	public QueryResultFormat getQueryResultFormat() {return null;}
}
