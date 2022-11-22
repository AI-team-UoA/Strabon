/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * 
 * Copyright (C) 2010, 2011, 2012, 2013 Pyravlos Team
 * 
 * http://www.strabon.di.uoa.gr/
 */
package eu.earthobservatory.runtime.generaldb;

import java.util.Hashtable;

import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.ContextStatementImpl;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.util.RDFInserter;
import org.openrdf.rio.RDFHandlerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.earthobservatory.vocabulary.GeoSPARQL;
import eu.earthobservatory.vocabulary.SimpleFeatures;

/**
 * This is the implementation of the RDFS Entailment Extension for
 * GeoSPARQL. All requirements of this extension are implemented
 * except for Requirement 25 identified by the URI 
 * <a>http://www.opengis.net/spec/geosparql/1.0/req/rdfs-entailment-extension/bgp-rdfs-ent</a>.
 * 
 * With respect to GML class hierarchy, the GML Simple Features Profile 2.0 is only supported. 
 * 
 * @author Charalampos Nikolaou <charnik@di.uoa.gr>
 * @author Konstantina Bereta <konstantina.bereta@di.uoa.gr>
 * @author Panayiotis Smeros <psmeros@di.uoa.gr>
 */
public class GeosparqlRDFHandlerBase extends RDFInserter {
	
	private static final Logger logger = LoggerFactory.getLogger(eu.earthobservatory.runtime.generaldb.GeosparqlRDFHandlerBase.class);
	
	public static boolean ENABLE_INFERENCE;
	
	/**
	 * Keeps a String to URI mapping for the URIs of Simple Features and GML
	 */
	private Hashtable<String, URI> uriMap;
	
	/** 
	 * The number of triples that we inferred
	 */
	private int numInfTriples = 0;
	
	public GeosparqlRDFHandlerBase(RepositoryConnection con) {
		super(con);
		
		this.uriMap = new Hashtable<String, URI>();
		
		if (logger.isDebugEnabled()) {
			logger.debug("[Strabon.GeoSPARQLEntailment] RDFS Entailment Extension of GeoSPARQL started.");
		}
	}
	
	@Override
	public void startRDF() throws RDFHandlerException {
		if (ENABLE_INFERENCE) {
			insertGeoSPARQLClassHierarchy();
			insertSimpleFeaturesClassHierarchy();
		}
	}
	
	@Override
	public void endRDF() throws RDFHandlerException {
		if (ENABLE_INFERENCE) {
			logger.info("[Strabon.GeoSPARQLEntailment] Inferred {} triples.", numInfTriples);
		}
	}
	
	/**
	 * Inserts an inferred statement using the underlying {@link RDFInserter#handleStatement}
	 * method. 
	 * 
	 * @param subj
	 * @param pred
	 * @param obj
	 * @param ctxt
	 */
	protected void handleInferredStatement(Resource subj, URI pred, Value obj, Resource ctxt) throws RDFHandlerException {
		Statement stmt;
		
		if (ctxt == null) {
			stmt = new StatementImpl(subj, pred, obj);
			
		} else {
			stmt = new ContextStatementImpl(subj, pred, obj, ctxt);
			
		}
		
		super.handleStatement(stmt);
		numInfTriples++;
	}
	
	@Override
	public void handleStatement(Statement st) throws RDFHandlerException
	{
		// pass it to RDFInserter first
		super.handleStatement(st);
		
		// now we do our play
		String pred = st.getPredicate().toString();
		String obj = st.getObject().toString();
		
		if (!ENABLE_INFERENCE) {
			return ;
		}
			
		/* Infer
		 * 		subj rdf:type geo:SpatialObject
		 * 		obj  rdf:type geo:SpatialObject
		 * from
		 * 		subj {any topological property from the Topology Vocabulary Extension} obj
		 */
		if( pred.startsWith(GeoSPARQL.GEO+"sf") ||
			pred.startsWith(GeoSPARQL.GEO+"eh") || 
			pred.startsWith(GeoSPARQL.GEO+"rcc8")) {
			
			handleInferredStatement(st.getSubject(), RDF.TYPE, getURI(GeoSPARQL.SpatialObject), st.getContext());
			if (st.getObject() instanceof Resource) { // necessary check, because it could be a Literal
				handleInferredStatement((Resource) st.getObject(), RDF.TYPE, getURI(GeoSPARQL.SpatialObject), st.getContext());
			}
		}
		/* Infer 
		 * 		subj rdf:type geo:SpatialObject
		 * from
		 * 		subj rdf:type geo:Feature
		 * or
		 * 		subj rdf:type geo:Geometry 
		 */
		else if(pred.equals(RDF.TYPE.stringValue()) && (obj.equals(GeoSPARQL.Feature) || obj.equals(GeoSPARQL.Geometry))) {
			handleInferredStatement(st.getSubject(), RDF.TYPE, getURI(GeoSPARQL.SpatialObject), st.getContext());
		} 
		/*
		 * Infer
		 * 		subj rdf:type geo:Feature
		 * 		subj rdf:type geo:SpatialObject
		 * 		obj  rdf:type geo:Feature
		 * 		obj  rdf:type geo:SpatialObject
		 * from
		 * 		subj geo:hasGeometry obj
		 * or
		 * 		sub geo:hasDefaultGeometry obj
		 */
		else if(pred.equals(GeoSPARQL.hasGeometry) || pred.equals(GeoSPARQL.hasDefaultGeometry))
		{
			handleInferredStatement(st.getSubject(), RDF.TYPE, getURI(GeoSPARQL.Feature), st.getContext());
			handleInferredStatement(st.getSubject(), RDF.TYPE, getURI(GeoSPARQL.SpatialObject), st.getContext());

			if (st.getObject() instanceof Resource) { // necessary check, because it could be a Literal
				handleInferredStatement((Resource) st.getObject(), RDF.TYPE, getURI(GeoSPARQL.Feature), st.getContext());
				handleInferredStatement((Resource) st.getObject(), RDF.TYPE, getURI(GeoSPARQL.SpatialObject), st.getContext());
			}
		}
		else if (pred.equals(RDF.TYPE.stringValue())) {
/* THE FOLLOWING CORRESPONDS TO GML AND NEEDS REWRITING TO FIT THAT OF SIMPLE FEATURES */			
//			// GML class hierarchy
//			if (obj.equals(GeoConstants.GML_OGC + "GM_Complex")
//					|| obj.equals(GeoConstants.GML_OGC + "GM_Aggregate")
//					|| obj.equals(GeoConstants.GML_OGC + "GM_Primitive")) {
//				String triple = "<" + subj + "> <" + TYPE + "> <" + GeoConstants.GML_OGC
//						+ "GM_Object" + "> .\n";
//			}
//			if (obj.equals(GeoConstants.GML_OGC + "GM_Composite")) {
//				String triple = "<" + subj + "> <" + TYPE + "> <" + GeoConstants.GML_OGC
//						+ "GM_Complex" + "> .\n" + "<" + subj + "> <" + TYPE
//						+ "> <" + GeoConstants.GML_OGC + "GM_Object" + "> .\n";
//
//			}
//			if (obj.equals(GeoConstants.GML_OGC + "GM_MultiPrimitive")) {
//				String triple = "<" + subj + "> <" + TYPE + "> <" + GeoConstants.GML_OGC
//						+ "GM_Aggregate" + "> .\n" + "<" + subj + "> <"
//						+ TYPE + "> <" + GeoConstants.GML_OGC + "GM_Object" + "> .\n";
//
//			}
//			if (obj.equals(GeoConstants.GML_OGC + "GM_Point")
//					|| obj.equals(GeoConstants.GML_OGC + "GM_OrientablePrimitive")
//					|| obj.equals(GeoConstants.GML_OGC + "GM_Solid")) {
//				String triple = "<" + subj + "> <" + TYPE + "> <" + GeoConstants.GML_OGC
//						+ "GM_Primitive" + "> .\n" + "<" + subj + "> <"
//						+ TYPE + "> <" + GeoConstants.GML_OGC + "GM_Object" + "> .\n";
//
//			}
//			if (obj.equals(GeoConstants.GML_OGC + "GM_OrientableCurve")
//					|| obj.equals(GeoConstants.GML_OGC + "GM_OrientableSurface")) {
//				String triple = "<" + subj + "> <" + TYPE + "> <" + GeoConstants.GML_OGC
//						+ "GM_OrientablePrimitive" + "> .\n" + "<" + subj
//						+ "> <" + TYPE + "> <" + GeoConstants.GML_OGC + "GM_Primitive" + "> .\n"
//						+ "<" + subj + "> <" + TYPE + "> <" + GeoConstants.GML_OGC
//						+ "GM_Object" + "> .\n";
//				triples.append(triple);
//				numTriples++;
//
//			}
//			if (obj.equals(GeoConstants.GML_OGC + "GM_Curve")) {
//				String triple = "<" + subj + "> <" + TYPE + "> <" + GeoConstants.GML_OGC
//						+ "GM_Aggregate" + "> .\n"
//						+ "<" + subj + "> <" + TYPE +"> <" + GeoConstants.GML_OGC + "GM_OrientableCurve" + "> .\n"
//						+ "<" + subj + "> <" + TYPE + "> <" + GeoConstants.GML_OGC + "GM_OrientablePrimitive" + "> .\n"
//						+ "<" + subj + "> <" + TYPE + "> <" + GeoConstants.GML_OGC + "GM_Primitive" + "> .\n"
//						+ "<" + subj + "> <" + TYPE + "> <" + GeoConstants.GML_OGC + "GM_Object" + "> .\n";
//
//			}
//			if (obj.equals(GeoConstants.GML_OGC + "GM_Surface")) {
//				String triple = "<" + subj + "> <" + TYPE + "> <" + GeoConstants.GML_OGC+ "GM_Aggregate" + "> .\n"
//						+ "<" + subj + "> <" + TYPE + "> <" + GeoConstants.GML_OGC + "GM_OrientableSurface" + "> .\n"
//						+ "<" + subj + "> <" + TYPE + "> <" + GeoConstants.GML_OGC + "GM_OrientablePrimitive" + "> .\n"
//						+ "<" + subj + "> <" + TYPE + "> <" + GeoConstants.GML_OGC + "GM_Primitive" + "> .\n"
//						+ "<" + subj + "> <" + TYPE + "> <" + GeoConstants.GML_OGC
//						+ "GM_Object" + "> .\n";
//
//			}
//			if (obj.equals(GeoConstants.GML_OGC + "GM_CompositeCurve")) {
//				String triple = "<" + subj + "> <" + TYPE + "> <" + GeoConstants.GML_OGC
//						+ "GM_Aggregate" + "> .\n" + "<" + subj + "> <"
//						+ TYPE + "> <" + GeoConstants.GML_OGC + "GM_OrientableCurve" + "> .\n"
//						+ "<" + subj + "> <" + TYPE + "> <" + GeoConstants.GML_OGC
//						+ "GM_OrientablePrimitive" + "> .\n" + "<" + subj
//						+ "> <" + TYPE + "> <" + GeoConstants.GML_OGC + "GM_Primitive" + "> .\n"
//						+ "<" + subj + "> <" + TYPE + "> <" + GeoConstants.GML_OGC
//						+ "GM_Complex" + "> .\n" + "<" + subj + "> <" + TYPE
//						+ "> <" + GeoConstants.GML_OGC + "GM_Composite" + "> .\n" + "<"
//						+ subj + "> <" + TYPE + "> <" + GeoConstants.GML_OGC + "GM_Object"
//						+ "> .\n";
//
//			}
//			if (obj.equals(GeoConstants.GML_OGC + "GM_CompositeSurface")) {
//				String triple = "<" + subj + "> <" + TYPE + "> <" + GeoConstants.GML_OGC
//						+ "GM_OrientableSurface" + "> .\n" +
//
//						"<" + subj + "> <" + TYPE + "> <" + GeoConstants.GML_OGC
//						+ "GM_OrientablePrimitive" + "> .\n" + "<" + subj
//						+ "> <" + TYPE + "> <" + GeoConstants.GML_OGC + "GM_Primitive" + "> .\n"
//						+ "<" + subj + "> <" + TYPE + "> <" + GeoConstants.GML_OGC
//						+ "GM_Complex" + "> .\n" + "<" + subj + "> <" + TYPE
//						+ "> <" + GeoConstants.GML_OGC + "GM_Composite" + "> .\n" + "<"
//						+ subj + "> <" + TYPE + "> <" + GeoConstants.GML_OGC + "GM_Object"
//						+ "> .\n";
//
//			}
//			if (obj.equals(GeoConstants.GML_OGC + "GM_CompositeSolid")) {
//				String triple = "<" + subj + "> <" + TYPE + "> <" + GeoConstants.GML_OGC
//						+ "GM_Solid" + "> .\n" + "<" + subj + "> <" + TYPE
//						+ "> <" + GeoConstants.GML_OGC + "GM_Primitive" + "> .\n" + "<"
//						+ subj + "> <" + TYPE + "> <" + GeoConstants.GML_OGC + "GM_Complex"
//						+ "> .\n" + "<" + subj + "> <" + TYPE + "> <" + GeoConstants.GML_OGC
//						+ "GM_Composite" + "> .\n" + "<" + subj + "> <"
//						+ TYPE + "> <" + GeoConstants.GML_OGC + "GM_Object" + "> .\n";
//
//			}
//			if (obj.equals(GeoConstants.GML_OGC + "GM_MultiPoint")
//					|| obj.equals(GeoConstants.GML_OGC + "GM_MultiCurve")
//					|| obj.equals(GeoConstants.GML_OGC + "GM_MultiSurface")
//					|| obj.equals(GeoConstants.GML_OGC + "GM_MultiSolid")) {
//				String triple = "<" + subj + "> <" + TYPE + "> <" + GeoConstants.GML_OGC
//						+ "GM_MultiPrimitive" + "> .\n" + "<" + subj + "> <"
//						+ TYPE + "> <" + GeoConstants.GML_OGC + "GM_Aggregate" + "> .\n" + "<"
//						+ subj + "> <" + TYPE + "> <" + GeoConstants.GML_OGC + "GM_Object"
//						+ "> .\n";
//			}
			/*
			 * Simple Features class hierarchy
			 */
			if (SimpleFeatures.Point.equals(obj)   || 
				SimpleFeatures.Curve.equals(obj)   ||
				SimpleFeatures.Surface.equals(obj) ||
				SimpleFeatures.GeometryCollection.equals(obj)) {// first level
				handleInferredStatement(st.getSubject(), RDF.TYPE, getURI(SimpleFeatures.Geometry), st.getContext());
				
				handleInferredStatement(st.getSubject(), RDF.TYPE, getURI(GeoSPARQL.Geometry), st.getContext());
				handleInferredStatement(st.getSubject(), RDF.TYPE, getURI(GeoSPARQL.SpatialObject), st.getContext());
				
			} else if (SimpleFeatures.LineString.equals(obj)) { // second level
				handleInferredStatement(st.getSubject(), RDF.TYPE, getURI(SimpleFeatures.Curve), st.getContext());
				handleInferredStatement(st.getSubject(), RDF.TYPE, getURI(SimpleFeatures.Geometry), st.getContext());
				
				handleInferredStatement(st.getSubject(), RDF.TYPE, getURI(GeoSPARQL.Geometry), st.getContext());
				handleInferredStatement(st.getSubject(), RDF.TYPE, getURI(GeoSPARQL.SpatialObject), st.getContext());
				
			} else if (SimpleFeatures.Polygon.equals(obj) || 
					   SimpleFeatures.PolyhedralSurface.equals(obj)) { // second level
				handleInferredStatement(st.getSubject(), RDF.TYPE, getURI(SimpleFeatures.Surface), st.getContext());
				handleInferredStatement(st.getSubject(), RDF.TYPE, getURI(SimpleFeatures.Geometry), st.getContext());
				
				handleInferredStatement(st.getSubject(), RDF.TYPE, getURI(GeoSPARQL.Geometry), st.getContext());
				handleInferredStatement(st.getSubject(), RDF.TYPE, getURI(GeoSPARQL.SpatialObject), st.getContext());
				
			} else if (SimpleFeatures.MultiSurface.equals(obj) ||
					SimpleFeatures.MultiCurve.equals(obj) ||
					SimpleFeatures.MultiPoint.equals(obj)) { // second level
				handleInferredStatement(st.getSubject(), RDF.TYPE, getURI(SimpleFeatures.GeometryCollection), st.getContext());
				handleInferredStatement(st.getSubject(), RDF.TYPE, getURI(SimpleFeatures.Geometry), st.getContext());
				
				handleInferredStatement(st.getSubject(), RDF.TYPE, getURI(GeoSPARQL.Geometry), st.getContext());
				handleInferredStatement(st.getSubject(), RDF.TYPE, getURI(GeoSPARQL.SpatialObject), st.getContext());
				
			} else if ( SimpleFeatures.Line.equals(obj) || 
						SimpleFeatures.LinearRing.equals(obj)) { // third level
				handleInferredStatement(st.getSubject(), RDF.TYPE, getURI(SimpleFeatures.LineString), st.getContext());
				handleInferredStatement(st.getSubject(), RDF.TYPE, getURI(SimpleFeatures.Curve), st.getContext());
				handleInferredStatement(st.getSubject(), RDF.TYPE, getURI(SimpleFeatures.Geometry), st.getContext());
				
				handleInferredStatement(st.getSubject(), RDF.TYPE, getURI(GeoSPARQL.Geometry), st.getContext());
				handleInferredStatement(st.getSubject(), RDF.TYPE, getURI(GeoSPARQL.SpatialObject), st.getContext());
				
			} else if (SimpleFeatures.Triangle.equals(obj)) { // third level
				handleInferredStatement(st.getSubject(), RDF.TYPE, getURI(SimpleFeatures.Polygon), st.getContext());
				handleInferredStatement(st.getSubject(), RDF.TYPE, getURI(SimpleFeatures.Surface), st.getContext());
				handleInferredStatement(st.getSubject(), RDF.TYPE, getURI(SimpleFeatures.Geometry), st.getContext());
				
				handleInferredStatement(st.getSubject(), RDF.TYPE, getURI(GeoSPARQL.Geometry), st.getContext());
				handleInferredStatement(st.getSubject(), RDF.TYPE, getURI(GeoSPARQL.SpatialObject), st.getContext());
				
			} else if (SimpleFeatures.TIN.equals(obj)) { // third level
				handleInferredStatement(st.getSubject(), RDF.TYPE, getURI(SimpleFeatures.PolyhedralSurface), st.getContext());
				handleInferredStatement(st.getSubject(), RDF.TYPE, getURI(SimpleFeatures.Surface), st.getContext());
				handleInferredStatement(st.getSubject(), RDF.TYPE, getURI(SimpleFeatures.Geometry), st.getContext());
				
				handleInferredStatement(st.getSubject(), RDF.TYPE, getURI(GeoSPARQL.Geometry), st.getContext());
				handleInferredStatement(st.getSubject(), RDF.TYPE, getURI(GeoSPARQL.SpatialObject), st.getContext());
				
			} else if (SimpleFeatures.MultiPolygon.equals(obj)) { // third level
				handleInferredStatement(st.getSubject(), RDF.TYPE, getURI(SimpleFeatures.MultiSurface), st.getContext());
				handleInferredStatement(st.getSubject(), RDF.TYPE, getURI(SimpleFeatures.GeometryCollection), st.getContext());
				handleInferredStatement(st.getSubject(), RDF.TYPE, getURI(SimpleFeatures.Geometry), st.getContext());
				
				handleInferredStatement(st.getSubject(), RDF.TYPE, getURI(GeoSPARQL.Geometry), st.getContext());
				handleInferredStatement(st.getSubject(), RDF.TYPE, getURI(GeoSPARQL.SpatialObject), st.getContext());
				
			} else if (SimpleFeatures.MultiLineString.equals(obj)) {// third level
				handleInferredStatement(st.getSubject(), RDF.TYPE, getURI(SimpleFeatures.MultiCurve), st.getContext());
				handleInferredStatement(st.getSubject(), RDF.TYPE, getURI(SimpleFeatures.GeometryCollection), st.getContext());
				handleInferredStatement(st.getSubject(), RDF.TYPE, getURI(SimpleFeatures.Geometry), st.getContext());
				
				handleInferredStatement(st.getSubject(), RDF.TYPE, getURI(GeoSPARQL.Geometry), st.getContext());
				handleInferredStatement(st.getSubject(), RDF.TYPE, getURI(GeoSPARQL.SpatialObject), st.getContext());
				
			}
		/* Spatial properties
		 * ~~~~~~~~~~~~~~~~~~~~
		 * Infer
		 * 		subj rdf:type geo:Feature
		 * 		subj rdf:type geo:SpatialObject
		 * from
		 * 		subj {any spatial property defined in Req. 9, 14, and 18} obj
		 */
		} else if ( GeoSPARQL.spatialDimension.equals(pred)    || GeoSPARQL.dimension.equals(pred)  		||
					GeoSPARQL.coordinateDimension.equals(pred) || GeoSPARQL.isEmpty.equals(pred) 			||
					GeoSPARQL.isSimple.equals(pred) 		   || GeoSPARQL.hasSerialization.equals(pred)   ||
					GeoSPARQL.asWKT.equals(pred)	 		   || GeoSPARQL.asGML.equals(pred)) {
			
			handleInferredStatement(st.getSubject(), RDF.TYPE, getURI(GeoSPARQL.Geometry), null);
			handleInferredStatement(st.getSubject(), RDF.TYPE, getURI(GeoSPARQL.SpatialObject), null);
		}
	}

	/**
	 * Materializes the RDF class hierarchy of Simple Features
	 */
	protected void insertSimpleFeaturesClassHierarchy() throws RDFHandlerException {
		// insert rdf:type rdfs:Class
		handleInferredStatement(getURI(SimpleFeatures.Geometry), RDF.TYPE, RDFS.CLASS, null);
		handleInferredStatement(getURI(SimpleFeatures.Point), RDF.TYPE, RDFS.CLASS, null);
		handleInferredStatement(getURI(SimpleFeatures.Curve), RDF.TYPE, RDFS.CLASS, null);
		handleInferredStatement(getURI(SimpleFeatures.Surface), RDF.TYPE, RDFS.CLASS, null);
		handleInferredStatement(getURI(SimpleFeatures.GeometryCollection), RDF.TYPE, RDFS.CLASS, null);
		handleInferredStatement(getURI(SimpleFeatures.LineString), RDF.TYPE, RDFS.CLASS, null);
		handleInferredStatement(getURI(SimpleFeatures.Polygon), RDF.TYPE, RDFS.CLASS, null);
		handleInferredStatement(getURI(SimpleFeatures.PolyhedralSurface), RDF.TYPE, RDFS.CLASS, null);
		handleInferredStatement(getURI(SimpleFeatures.MultiSurface), RDF.TYPE, RDFS.CLASS, null);
		handleInferredStatement(getURI(SimpleFeatures.MultiCurve), RDF.TYPE, RDFS.CLASS, null);
		handleInferredStatement(getURI(SimpleFeatures.MultiPoint), RDF.TYPE, RDFS.CLASS, null);
		handleInferredStatement(getURI(SimpleFeatures.Line), RDF.TYPE, RDFS.CLASS, null);
		handleInferredStatement(getURI(SimpleFeatures.LinearRing), RDF.TYPE, RDFS.CLASS, null);
		handleInferredStatement(getURI(SimpleFeatures.Triangle), RDF.TYPE, RDFS.CLASS, null);
		handleInferredStatement(getURI(SimpleFeatures.TIN), RDF.TYPE, RDFS.CLASS, null);
		handleInferredStatement(getURI(SimpleFeatures.MultiPolygon), RDF.TYPE, RDFS.CLASS, null);
		handleInferredStatement(getURI(SimpleFeatures.MultiLineString), RDF.TYPE, RDFS.CLASS, null);
		
		// insert rdfs:subClassOf geo:Geometry
		handleInferredStatement(getURI(SimpleFeatures.Geometry), RDFS.SUBCLASSOF, getURI(GeoSPARQL.Geometry), null);
		handleInferredStatement(getURI(SimpleFeatures.Point), RDFS.SUBCLASSOF, getURI(GeoSPARQL.Geometry), null);
		handleInferredStatement(getURI(SimpleFeatures.Curve), RDFS.SUBCLASSOF, getURI(GeoSPARQL.Geometry), null);
		handleInferredStatement(getURI(SimpleFeatures.Surface), RDFS.SUBCLASSOF, getURI(GeoSPARQL.Geometry), null);
		handleInferredStatement(getURI(SimpleFeatures.GeometryCollection), RDFS.SUBCLASSOF, getURI(GeoSPARQL.Geometry), null);
		handleInferredStatement(getURI(SimpleFeatures.LineString), RDFS.SUBCLASSOF, getURI(GeoSPARQL.Geometry), null);
		handleInferredStatement(getURI(SimpleFeatures.Polygon), RDFS.SUBCLASSOF, getURI(GeoSPARQL.Geometry), null);
		handleInferredStatement(getURI(SimpleFeatures.PolyhedralSurface), RDFS.SUBCLASSOF, getURI(GeoSPARQL.Geometry), null);
		handleInferredStatement(getURI(SimpleFeatures.MultiSurface), RDFS.SUBCLASSOF, getURI(GeoSPARQL.Geometry), null);
		handleInferredStatement(getURI(SimpleFeatures.MultiCurve), RDFS.SUBCLASSOF, getURI(GeoSPARQL.Geometry), null);
		handleInferredStatement(getURI(SimpleFeatures.MultiPoint), RDFS.SUBCLASSOF, getURI(GeoSPARQL.Geometry), null);
		handleInferredStatement(getURI(SimpleFeatures.Line), RDFS.SUBCLASSOF, getURI(GeoSPARQL.Geometry), null);
		handleInferredStatement(getURI(SimpleFeatures.LinearRing), RDFS.SUBCLASSOF, getURI(GeoSPARQL.Geometry), null);
		handleInferredStatement(getURI(SimpleFeatures.Triangle), RDFS.SUBCLASSOF, getURI(GeoSPARQL.Geometry), null);
		handleInferredStatement(getURI(SimpleFeatures.TIN), RDFS.SUBCLASSOF, getURI(GeoSPARQL.Geometry), null);
		handleInferredStatement(getURI(SimpleFeatures.MultiPolygon), RDFS.SUBCLASSOF, getURI(GeoSPARQL.Geometry), null);
		handleInferredStatement(getURI(SimpleFeatures.MultiLineString), RDFS.SUBCLASSOF, getURI(GeoSPARQL.Geometry), null);
		
		// insert rdfs:subClassOf geo:SpatialObject
		handleInferredStatement(getURI(SimpleFeatures.Geometry), RDFS.SUBCLASSOF, getURI(GeoSPARQL.SpatialObject), null);
		handleInferredStatement(getURI(SimpleFeatures.Point), RDFS.SUBCLASSOF, getURI(GeoSPARQL.SpatialObject), null);
		handleInferredStatement(getURI(SimpleFeatures.Curve), RDFS.SUBCLASSOF, getURI(GeoSPARQL.SpatialObject), null);
		handleInferredStatement(getURI(SimpleFeatures.Surface), RDFS.SUBCLASSOF, getURI(GeoSPARQL.SpatialObject), null);
		handleInferredStatement(getURI(SimpleFeatures.GeometryCollection), RDFS.SUBCLASSOF, getURI(GeoSPARQL.SpatialObject), null);
		handleInferredStatement(getURI(SimpleFeatures.LineString), RDFS.SUBCLASSOF, getURI(GeoSPARQL.SpatialObject), null);
		handleInferredStatement(getURI(SimpleFeatures.Polygon), RDFS.SUBCLASSOF, getURI(GeoSPARQL.SpatialObject), null);
		handleInferredStatement(getURI(SimpleFeatures.PolyhedralSurface), RDFS.SUBCLASSOF, getURI(GeoSPARQL.SpatialObject), null);
		handleInferredStatement(getURI(SimpleFeatures.MultiSurface), RDFS.SUBCLASSOF, getURI(GeoSPARQL.SpatialObject), null);
		handleInferredStatement(getURI(SimpleFeatures.MultiCurve), RDFS.SUBCLASSOF, getURI(GeoSPARQL.SpatialObject), null);
		handleInferredStatement(getURI(SimpleFeatures.MultiPoint), RDFS.SUBCLASSOF, getURI(GeoSPARQL.SpatialObject), null);
		handleInferredStatement(getURI(SimpleFeatures.Line), RDFS.SUBCLASSOF, getURI(GeoSPARQL.SpatialObject), null);
		handleInferredStatement(getURI(SimpleFeatures.LinearRing), RDFS.SUBCLASSOF, getURI(GeoSPARQL.SpatialObject), null);
		handleInferredStatement(getURI(SimpleFeatures.Triangle), RDFS.SUBCLASSOF, getURI(GeoSPARQL.SpatialObject), null);
		handleInferredStatement(getURI(SimpleFeatures.TIN), RDFS.SUBCLASSOF, getURI(GeoSPARQL.SpatialObject), null);
		handleInferredStatement(getURI(SimpleFeatures.MultiPolygon), RDFS.SUBCLASSOF, getURI(GeoSPARQL.SpatialObject), null);
		handleInferredStatement(getURI(SimpleFeatures.MultiLineString), RDFS.SUBCLASSOF, getURI(GeoSPARQL.SpatialObject), null);
		
		// first level 
		handleInferredStatement(getURI(SimpleFeatures.Point), RDFS.SUBCLASSOF, getURI(SimpleFeatures.Geometry), null);
		handleInferredStatement(getURI(SimpleFeatures.Curve), RDFS.SUBCLASSOF, getURI(SimpleFeatures.Geometry), null);
		handleInferredStatement(getURI(SimpleFeatures.Surface), RDFS.SUBCLASSOF, getURI(SimpleFeatures.Geometry), null);
		handleInferredStatement(getURI(SimpleFeatures.GeometryCollection), RDFS.SUBCLASSOF, getURI(SimpleFeatures.Geometry), null);
		
		// second level
		handleInferredStatement(getURI(SimpleFeatures.LineString), RDFS.SUBCLASSOF, getURI(SimpleFeatures.Curve), null);
		handleInferredStatement(getURI(SimpleFeatures.LineString), RDFS.SUBCLASSOF, getURI(SimpleFeatures.Geometry), null);
		
		handleInferredStatement(getURI(SimpleFeatures.Polygon), RDFS.SUBCLASSOF, getURI(SimpleFeatures.Surface), null);
		handleInferredStatement(getURI(SimpleFeatures.Polygon), RDFS.SUBCLASSOF, getURI(SimpleFeatures.Geometry), null);
		
		handleInferredStatement(getURI(SimpleFeatures.PolyhedralSurface), RDFS.SUBCLASSOF, getURI(SimpleFeatures.Surface), null);
		handleInferredStatement(getURI(SimpleFeatures.PolyhedralSurface), RDFS.SUBCLASSOF, getURI(SimpleFeatures.Geometry), null);
		
		handleInferredStatement(getURI(SimpleFeatures.MultiSurface), RDFS.SUBCLASSOF, getURI(SimpleFeatures.GeometryCollection), null);
		handleInferredStatement(getURI(SimpleFeatures.MultiSurface), RDFS.SUBCLASSOF, getURI(SimpleFeatures.Geometry), null);
		
		handleInferredStatement(getURI(SimpleFeatures.MultiCurve), RDFS.SUBCLASSOF, getURI(SimpleFeatures.GeometryCollection), null);
		handleInferredStatement(getURI(SimpleFeatures.MultiCurve), RDFS.SUBCLASSOF, getURI(SimpleFeatures.Geometry), null);
		
		handleInferredStatement(getURI(SimpleFeatures.MultiPoint), RDFS.SUBCLASSOF, getURI(SimpleFeatures.GeometryCollection), null);
		handleInferredStatement(getURI(SimpleFeatures.MultiPoint), RDFS.SUBCLASSOF, getURI(SimpleFeatures.Geometry), null);
		
		// third level
		handleInferredStatement(getURI(SimpleFeatures.Line), RDFS.SUBCLASSOF, getURI(SimpleFeatures.LineString), null);
		handleInferredStatement(getURI(SimpleFeatures.Line), RDFS.SUBCLASSOF, getURI(SimpleFeatures.Curve), null);
		handleInferredStatement(getURI(SimpleFeatures.Line), RDFS.SUBCLASSOF, getURI(SimpleFeatures.Geometry), null);
		
		handleInferredStatement(getURI(SimpleFeatures.LinearRing), RDFS.SUBCLASSOF, getURI(SimpleFeatures.Polygon), null);
		handleInferredStatement(getURI(SimpleFeatures.LinearRing), RDFS.SUBCLASSOF, getURI(SimpleFeatures.Surface), null);
		handleInferredStatement(getURI(SimpleFeatures.LinearRing), RDFS.SUBCLASSOF, getURI(SimpleFeatures.Geometry), null);
		
		handleInferredStatement(getURI(SimpleFeatures.Triangle), RDFS.SUBCLASSOF, getURI(SimpleFeatures.Polygon), null);
		handleInferredStatement(getURI(SimpleFeatures.Triangle), RDFS.SUBCLASSOF, getURI(SimpleFeatures.Surface), null);
		handleInferredStatement(getURI(SimpleFeatures.Triangle), RDFS.SUBCLASSOF, getURI(SimpleFeatures.Geometry), null);
		
		handleInferredStatement(getURI(SimpleFeatures.TIN), RDFS.SUBCLASSOF, getURI(SimpleFeatures.PolyhedralSurface), null);
		handleInferredStatement(getURI(SimpleFeatures.TIN), RDFS.SUBCLASSOF, getURI(SimpleFeatures.Surface), null);
		handleInferredStatement(getURI(SimpleFeatures.TIN), RDFS.SUBCLASSOF, getURI(SimpleFeatures.Geometry), null);
		
		handleInferredStatement(getURI(SimpleFeatures.MultiPolygon), RDFS.SUBCLASSOF, getURI(SimpleFeatures.MultiSurface), null);
		handleInferredStatement(getURI(SimpleFeatures.MultiPolygon), RDFS.SUBCLASSOF, getURI(SimpleFeatures.GeometryCollection), null);
		handleInferredStatement(getURI(SimpleFeatures.MultiPolygon), RDFS.SUBCLASSOF, getURI(SimpleFeatures.Geometry), null);
		
		handleInferredStatement(getURI(SimpleFeatures.MultiLineString), RDFS.SUBCLASSOF, getURI(SimpleFeatures.MultiSurface), null);
		handleInferredStatement(getURI(SimpleFeatures.MultiLineString), RDFS.SUBCLASSOF, getURI(SimpleFeatures.GeometryCollection), null);
		handleInferredStatement(getURI(SimpleFeatures.MultiLineString), RDFS.SUBCLASSOF, getURI(SimpleFeatures.Geometry), null);
	}
	
	/**
	 * Materializes the RDF class hierarchy of GeoSPARQL
	 * @throws RDFHandlerException 
	 */
	protected void insertGeoSPARQLClassHierarchy() throws RDFHandlerException {
		handleInferredStatement(getURI(GeoSPARQL.SpatialObject), RDF.TYPE, RDFS.CLASS, null);
		handleInferredStatement(getURI(GeoSPARQL.Feature), RDF.TYPE, RDFS.CLASS, null);
		handleInferredStatement(getURI(GeoSPARQL.Geometry), RDF.TYPE, RDFS.CLASS, null);
		
		handleInferredStatement(getURI(GeoSPARQL.Feature), RDFS.SUBCLASSOF, getURI(GeoSPARQL.SpatialObject), null);
		handleInferredStatement(getURI(GeoSPARQL.Geometry), RDFS.SUBCLASSOF, getURI(GeoSPARQL.SpatialObject), null);
	}

	/**
	 * Inserts the given URI in the hashtable of URIs
	 * and retrieves the instance of class URI.
	 * 
	 * @param uri
	 * @return
	 */
	private URI getURI(String uri) {
		URI ret = null;
		if ((ret = uriMap.get(uri)) == null) {
			ret = new URIImpl(uri);
			uriMap.put(uri, ret);
		}
		
		return ret;
	}
}
