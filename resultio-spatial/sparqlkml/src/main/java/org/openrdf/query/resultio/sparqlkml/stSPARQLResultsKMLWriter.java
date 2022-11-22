/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. Copyright (C) 2010, 2011, 2012,
 * 2013, 2014 Pyravlos Team http://www.strabon.di.uoa.gr/
 */

package org.openrdf.query.resultio.sparqlkml;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Hashtable;
import java.util.List;

import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;

import org.geotools.kml.KML;
import org.geotools.kml.KMLConfiguration;
import org.geotools.xsd.Encoder;
import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.Value;
import org.openrdf.query.Binding;
import org.openrdf.query.BindingSet;
import org.openrdf.query.TupleQueryResultHandlerException;
import org.openrdf.query.algebra.evaluation.function.spatial.AbstractWKT;
import org.openrdf.query.algebra.evaluation.function.spatial.StrabonPolyhedron;
import org.openrdf.query.algebra.evaluation.util.JTSWrapper;
import org.openrdf.query.resultio.TupleQueryResultFormat;
import org.openrdf.query.resultio.TupleQueryResultWriter;
import org.openrdf.query.resultio.stSPARQLQueryResultFormat;
import org.openrdf.query.resultio.sparqlxml.stSPARQLXMLWriter;
import org.openrdf.sail.generaldb.model.GeneralDBPolyhedron;
import org.openrdf.sail.generaldb.model.XMLGSDatatypeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.openrdf.rio.RioSetting;
import org.openrdf.rio.WriterConfig;
import org.openrdf.query.resultio.QueryResultFormat;


import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.MultiPoint;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.io.ParseException;

import eu.earthobservatory.constants.GeoConstants;

/**
 * @author Manos Karpathiotakis <mk@di.uoa.gr>
 * @author Charalampos Nikolaou <charnik@di.uoa.gr>
 * @author Panayiotis Smeros <psmeros@di.uoa.gr>
 * @author Konstantina Bereta <konstantina.bereta@di.uoa.gr>
 */
public class stSPARQLResultsKMLWriter implements TupleQueryResultWriter {
	private static final Logger logger = LoggerFactory.getLogger(org.openrdf.query.resultio.sparqlkml.stSPARQLResultsKMLWriter.class);

	// KML tags/attributes
	protected static final String ROOT_TAG 			= "kml";
	protected static final String NAMESPACE 		= "http://www.opengis.net/kml/2.2";
	protected static final String RESULT_SET_TAG 	= "Folder";
	protected static final String PLACEMARK_TAG 	= "Placemark";
	protected static final String TIMESTAMP_TAG 	= "TimeStamp";
	protected static final String TIMESPAN_TAG 		= "TimeSpan";
	protected static final String BEGIN_TAG 		= "begin";
	protected static final String END_TAG 			= "end";
	protected static final String WHEN_TAG 			= "when";
	protected static final String NAME_TAG 			= "name";
	protected static final String DESC_TAG 			= "description";
	protected static final String EXT_DATA_TAG 		= "ExtendedData";
	protected static final String DATA_TAG 			= "Data";
	protected static final String VALUE_TAG			= "value";
	protected static final String NAME_ATTR			= NAME_TAG;

	protected static final String TABLE_ROW_BEGIN 	= "<TR>";
	protected static final String TABLE_ROW_END 	= "</TR>";
	protected static final String TABLE_DATA_BEGIN 	= "<TD>";
	protected static final String TABLE_DATA_END 	= "</TD>";
	protected static final String NEWLINE 			= "\n";
	protected static final String TABLE_DESC_BEGIN 	= "<![CDATA[<TABLE border=\"1\">"+ NEWLINE;
	protected static final String TABLE_DESC_END 	= "</TABLE>]]>" + NEWLINE;

	/**
	 * The underlying XML formatter.
	 */
	protected stSPARQLXMLWriter xmlWriter;

	/**
	 * The ordered list of binding names of the result.
	 */
	protected List<String> bindingNames;
	
	/**
	 * The number of results seen.
	 */
	protected int nresults;

	/**
	 * True if results have at least one geometry.
	 */
	protected Boolean hasGeometry;

	/**
	 * True if results have at least one temporal element.
	 * This element can be either an instant (xsd:dateTime value) or a period (strdf:period)
	 */
	protected Boolean hasTimestamp;
    
	protected Boolean hasTimespan;
	/**
	 * The JTS wrapper
	 */
	protected JTSWrapper jts;

	/**
	 * Stream for manipulating geometries
	 */
	protected ByteArrayOutputStream baos;

	/**
	 * Description string holding the projected variables of the SPARQL query
	 */
	protected StringBuilder descHeader;

	/**
	 * Description string holding the values for the projected variables of the
	 * SPARQL query
	 */
	protected StringBuilder descData;

	/**
	 * Indentation used in tags that are constructed manually
	 */
	protected int depth;

	/**
	 * Creates an stSPARQLResultsKMLWriter that encodes the SPARQL results in
	 * KML.
	 * 
	 * @param out
	 */
	public stSPARQLResultsKMLWriter(OutputStream out) {
		this(new stSPARQLXMLWriter(out));
	}

	public stSPARQLResultsKMLWriter(stSPARQLXMLWriter writer) {
		xmlWriter = writer;
		xmlWriter.setPrettyPrint(true);
		depth = 4;
		jts = JTSWrapper.getInstance();
		baos = new ByteArrayOutputStream();
		descHeader = new StringBuilder();
		descData = new StringBuilder();
		nresults = 0;
		hasGeometry=false;
	}

	@Override
	public void startQueryResult(List<String> bindingNames) throws TupleQueryResultHandlerException {
		try {
			// keep the order of binding names
			this.bindingNames = bindingNames;
			
			xmlWriter.startDocument();
			xmlWriter.setAttribute("xmlns", NAMESPACE);
			xmlWriter.startTag(ROOT_TAG);
			xmlWriter.startTag(RESULT_SET_TAG);

		} catch (IOException e) {
			throw new TupleQueryResultHandlerException(e);
		}
	}

	@Override
	public void endQueryResult() throws TupleQueryResultHandlerException {
		try {
			
			xmlWriter.endTag(RESULT_SET_TAG);
			xmlWriter.endTag(ROOT_TAG);
			xmlWriter.endDocument();
			baos.close();
			
			if (!hasGeometry) {
				logger.warn("[Strabon.KMLWriter] No spatial binding found in the result. KML requires that at least one binding maps to a geometry.", nresults);
			}
			
		} catch (IOException e) {
			throw new TupleQueryResultHandlerException(e);
		}
	}

	@Override
	public void handleSolution(BindingSet bindingSet) throws TupleQueryResultHandlerException {
		try {
			// true if there are bindings that do not correspond to geometries
			boolean hasDesc = false;
			
			Hashtable<String, String> extData = new Hashtable<String, String>();


			// write placemark tag
			xmlWriter.startTag(PLACEMARK_TAG);
			for (Binding binding : bindingSet) {
				if(!binding.getValue().toString().contains("^^")|| (binding.getValue() instanceof org.openrdf.sail.generaldb.model.GeneralDBPolyhedron)){
					continue;
				}
				Literal literal = (Literal) binding.getValue();
				if(XMLGSDatatypeUtil.isCalendarDatatype(literal.getDatatype())){
					hasTimestamp = true;
					xmlWriter.startTag(TIMESTAMP_TAG);
					xmlWriter.textElement(WHEN_TAG, literal.getLabel());
					xmlWriter.endTag(TIMESTAMP_TAG);
				}
			}
			xmlWriter.textElement(NAME_TAG, "Result" + nresults);
			
			// parse binding set
			for (String bindingName : bindingNames) {				
				Binding binding = bindingSet.getBinding(bindingName);
				
				if(binding != null) {
					Value value = binding.getValue();
					
					// check for geometry value
					if (XMLGSDatatypeUtil.isGeometryValue(value)) {
						hasGeometry=true;
	
						if (logger.isDebugEnabled()) {
							logger.debug("[Strabon] Found geometry: {}", value);
						}
						
						xmlWriter.unescapedText(getKML(value));
						
					} else { // URI, BlankNode, or Literal other than spatial literal
						if (logger.isDebugEnabled()) {
							logger.debug("[Strabon.KMLWriter] Found URI/BlankNode/Literal ({}): {}", value.getClass(), value);
						}
						
						// mark that we found sth corresponding to the description
						hasDesc = true;
						
						// write description
						writeDesc(binding);
						
						// fill also the extended data attribute of the Placemark
						extData.put(binding.getName(), getBindingValue(binding));
					}
				}
			}
			
			
			// we have found and constructed a description for this result.
			// Write it down.
			if (hasDesc) {
				
				// create description table and header
				indent(descHeader, depth);
				descHeader.append(TABLE_DESC_BEGIN);
				indent(descHeader, depth);
				
				// append to the table header the actual content from
				// the bindings
				descHeader.append(descData);
				
				// close the table for the description
				descHeader.append(NEWLINE);
				indent(descHeader, depth);
				descHeader.append(TABLE_DESC_END);
				
				// begin the "description" tag
				xmlWriter.startTag(DESC_TAG);
				
				// write the actual description
				xmlWriter.unescapedText(descHeader.toString());
				
				// end the "description" tag
				xmlWriter.endTag(DESC_TAG);
			}
			
			// add the extended data
			if (extData.size() > 0) {
				xmlWriter.startTag(EXT_DATA_TAG);
				for (String key : extData.keySet()) {
					xmlWriter.setAttribute(NAME_ATTR, key);
					xmlWriter.startTag(DATA_TAG);
						xmlWriter.textElement(VALUE_TAG, extData.get(key));
					xmlWriter.endTag(DATA_TAG);
				}
				xmlWriter.endTag(EXT_DATA_TAG);
			}
		
			// end Placemark
			xmlWriter.endTag(PLACEMARK_TAG);
		
			// clear description string builders
			descHeader.setLength(0);
			descData.setLength(0);
		
			// increase result size
			nresults++;
			
		} catch (IOException e) {
			throw new TupleQueryResultHandlerException(e);
		}
	}

	protected String getKML(Value value) {
		String kml = "";
		
		QName geometryType = null;
		
		// the underlying geometry in value
		Geometry geom = null;
		
		// the underlying SRID of the geometry
		int srid = -1;
		
		// get the KML encoder
		Encoder encoder = null;
		
		try {
			encoder = new Encoder(new KMLConfiguration());
			encoder.setIndenting(true);
			
			if (value instanceof GeneralDBPolyhedron) {
				GeneralDBPolyhedron dbpolyhedron = (GeneralDBPolyhedron) value;
				geom = dbpolyhedron.getPolyhedron().getGeometry();
				srid = dbpolyhedron.getPolyhedron().getGeometry().getSRID();
				
			} else if (value instanceof StrabonPolyhedron) { // spatial case from new geometry construction (SELECT) 
					StrabonPolyhedron poly = (StrabonPolyhedron) value;
					geom = poly.getGeometry();
					srid = geom.getSRID();
					
			} else { // spatial literal
				Literal spatial = (Literal) value;
				String geomRep = spatial.stringValue();
				
				if (XMLGSDatatypeUtil.isWKTLiteral(spatial)) { // WKT

					AbstractWKT awkt = null;
					if (spatial.getDatatype() == null) { // plain WKT literal
						awkt = new AbstractWKT(geomRep);
						
					} else { // typed WKT literal
						awkt = new AbstractWKT(geomRep, spatial.getDatatype().stringValue());
					}

					geom = jts.WKTread(awkt.getWKT());
					srid = awkt.getSRID();

				} else { // GML
					geom = jts.GMLread(geomRep);
					srid = geom.getSRID();
				}
			}
			
			// transform the geometry to {@link GeoConstants#EPSG4326_SRID}
			geom = jts.transform(geom, srid, GeoConstants.EPSG4326_SRID);
			
			if (geom instanceof Point) {
				geometryType = KML.Point;
				
			} else if (geom instanceof Polygon) {
				geometryType = KML.Polygon;
				
			} else if (geom instanceof LineString) {
				geometryType = KML.LineString;
				
			} else if (geom instanceof MultiPoint) {
				geometryType = KML.MultiGeometry;
				
			} else if (geom instanceof MultiLineString) {
				geometryType = KML.MultiGeometry;
				
			} else if (geom instanceof MultiPolygon) {
				geometryType = KML.MultiGeometry;
				
			} else if (geom instanceof GeometryCollection) {
				geometryType = KML.MultiGeometry;
				
			}
			
			if (geometryType == null) {
				logger.warn("[Strabon.KMLWriter] Found unknown geometry type.");
				
			} else {
				encoder.encode(geom, geometryType, baos);
				kml = baos.toString().substring(38).replaceAll(" xmlns:kml=\"http://earth.google.com/kml/2.1\"", "").replaceAll("kml:", "");

				baos.reset();
			}
		} catch (ParseException e) {
			logger.error("[Strabon.KMLWriter] Parse error exception of geometry: {}", e.getMessage());
			
		} catch (IOException e) {
			logger.error("[Strabon.KMLWriter] IOException during KML encoding of geometry: {}",	e.getMessage());
			
		} catch (JAXBException e) {
			logger.error("[Strabon.KMLWriter] Exception during GML parsing: {}", e.getMessage());
		}
		
		return kml;
	}

	/**
	 * Adds to the description table information for a binding.
	 * 
	 * @param binding
	 */
	protected void writeDesc(Binding binding) {
		descData.append(NEWLINE);
		indent(descData, depth + 1);
		descData.append(TABLE_ROW_BEGIN);
		descData.append(TABLE_DATA_BEGIN);
		descData.append(binding.getName());
		descData.append(TABLE_DATA_END);
		descData.append(TABLE_DATA_BEGIN);
		if (binding.getValue() instanceof BNode) {
			descData.append("_:");
		}
		descData.append(binding.getValue().stringValue());
		descData.append(TABLE_DATA_END);
		descData.append(TABLE_ROW_END);
	}

	protected String getBindingValue(Binding binding) {
		String val = binding.getValue().stringValue();
		if (binding.getValue() instanceof BNode) {
			val = "_:" + val;
		}
		
		return val;
	}
	
	@Override
	public TupleQueryResultFormat getTupleQueryResultFormat() {
		return stSPARQLQueryResultFormat.KML;
	}

	/**
	 * Adds indentation to the given string builder according to the specified
	 * depth.
	 * 
	 * @param sb
	 * @param depth
	 */
	protected void indent(StringBuilder sb, int depth) {
		for (int i = 0; i < depth; i++) {
			sb.append(xmlWriter.getIndentString());
		}
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
