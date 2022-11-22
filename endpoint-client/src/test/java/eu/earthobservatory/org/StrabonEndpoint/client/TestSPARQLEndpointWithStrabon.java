/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * 
 * Copyright (C) 2012, Pyravlos Team
 * 
 * http://www.strabon.di.uoa.gr/
 */
package eu.earthobservatory.org.StrabonEndpoint.client;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Vector;

import org.junit.Before;
import org.junit.Test;
import org.openrdf.query.resultio.TupleQueryResultFormat;
import org.openrdf.query.resultio.stSPARQLQueryResultFormat;

/**
 * @author Charalampos Nikolaou <charnik@di.uoa.gr>
 *
 */
public class TestSPARQLEndpointWithStrabon {

	private SPARQLEndpoint endpoint; 
	private String query;
	private Vector<stSPARQLQueryResultFormat> formats = new Vector<stSPARQLQueryResultFormat>();
	
	@Before
	public void init() {
		// initialize endpoint
		endpoint = new SPARQLEndpoint("geo.linkedopendata.gr", 80, "gag-endpoint/Query");
		
		// set query
		query = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
				"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> " +
				"PREFIX gag: <http://geo.linkedopendata.gr/gag/ontology/> " +

				"SELECT ?g " +
				"WHERE {" +
  				"	<http://geo.linkedopendata.gr/gag/id/9186> rdf:έχει_γεωμετρία ?g . " +
				" } "  ;
		
		// initialized formats
		for (TupleQueryResultFormat format : stSPARQLQueryResultFormat.values()) {
				if (format instanceof stSPARQLQueryResultFormat) {
					formats.add((stSPARQLQueryResultFormat) format);
				}
		}
				
	}
	
	/**
	 * Test method for {@link eu.earthobservatory.org.StrabonEndpoint.client.SPARQLEndpoint#query(java.lang.String, org.openrdf.query.resultio.stSPARQLQueryResultFormat)}.
	 */
	@Test
	public void testQuery() {
		//for (stSPARQLQueryResultFormat format : formats) {
			try {
				EndpointResult response = endpoint.query(query, stSPARQLQueryResultFormat.XML);
				
				if (response.getStatusCode() != 200) {
					System.err.println("Status code ("+response.getStatusCode()+"):" + response.getStatusText());
				}
				
				assertTrue(response.getStatusCode() == 200);

			} catch (IOException e) {
				e.printStackTrace();
			}
			
		//}
	}
	
	/**
	 * Test method for testing that method {@link eu.earthobservatory.org.StrabonEndpoint.client.SPARQLEndpoint#query(java.lang.String, org.openrdf.query.resultio.stSPARQLQueryResultFormat)}.
	 * returns an IOException when it should do so.
	 */
	@Test(expected= IOException.class)
	public void testIOException() throws IOException {
		SPARQLEndpoint ep = new SPARQLEndpoint("blabla.dgr", 80, "bla");
		ep.query(query, formats.get(0));
	}
}
