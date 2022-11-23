/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * Copyright (C) 2012, 2013, Pyravlos Team
 *
 * http://www.strabon.di.uoa.gr/
 */
package eu.earthobservatory.org.StrabonEndpoint.client;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;

import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.TupleQueryResultHandlerException;
import org.openrdf.query.resultio.QueryResultIO;
import org.openrdf.query.resultio.QueryResultParseException;
import org.openrdf.query.resultio.TupleQueryResultFormat;
import org.openrdf.query.resultio.UnsupportedQueryResultFormatException;
import org.openrdf.query.resultio.stSPARQLQueryResultFormat;
import org.openrdf.query.resultio.sparqlkml.stSPARQLResultsKMLWriter;

/**
 * SpatialEndpoint is a SPARQLEndpoint which can store and 
 * query for spatial data. It also supports KML format for 
 * this kind of data.
 * 
 * @author Charalampos Nikolaou <charnik@di.uoa.gr>
 * @author Kallirroi Dogani <kallirroi@di.uoa.gr>
 */
public class SpatialEndpoint extends SPARQLEndpoint {
	
	public SpatialEndpoint(String host, int port) {
		super(host, port);
	}
	
	public SpatialEndpoint(String host, int port, String endpointName) {
		super(host, port, endpointName);
	}
	
	public EndpointResult queryForKML(String sparqlQuery) throws IOException, QueryResultParseException, TupleQueryResultHandlerException, UnsupportedQueryResultFormatException, QueryEvaluationException{
		
		EndpointResult xmlResult = query(sparqlQuery, stSPARQLQueryResultFormat.XML);
		
		if (xmlResult.getStatusCode() != 200) {
			throw new RuntimeException("Failed : HTTP error code : " + xmlResult.getStatusCode() + " " + xmlResult.getStatusText());
		}
		
		String xml = xmlResult.getResponse();
		
		InputStream inputStream = new ByteArrayInputStream(xml.getBytes("UTF-8"));  
		TupleQueryResult results = QueryResultIO.parse(inputStream, TupleQueryResultFormat.SPARQL);
		
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		stSPARQLResultsKMLWriter kmlWriter = new stSPARQLResultsKMLWriter(outputStream);
			
		kmlWriter.startQueryResult(results.getBindingNames());
					
		while(results.hasNext()){
		
				kmlWriter.handleSolution(results.next());
		}	
					
		kmlWriter.endQueryResult();
			
		EndpointResult kmlResult = new EndpointResult(xmlResult.getStatusCode(), xmlResult.getStatusText(), outputStream.toString());
		return kmlResult;
	}

}
