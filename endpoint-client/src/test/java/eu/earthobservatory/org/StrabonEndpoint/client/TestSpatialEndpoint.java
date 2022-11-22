/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * 
 * Copyright (C) 2013, Pyravlos Team
 * 
 * http://www.strabon.di.uoa.gr/
 */
package eu.earthobservatory.org.StrabonEndpoint.client;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQueryResultHandlerException;
import org.openrdf.query.resultio.QueryResultParseException;
import org.openrdf.query.resultio.UnsupportedQueryResultFormatException;

/**
 * @author Kallirroi Dogani <kallirroi@di.uoa.gr>
 *
 */
public class TestSpatialEndpoint {

	private SpatialEndpoint endpoint;
	private String query;
	
	@Before
	public void init() {
		// initialize endpoint
		endpoint = new SpatialEndpoint("geo.linkedopendata.gr", 80, "corine-endpoint/Query");
		
		// set query
		query = "PREFIX corine: <http://geo.linkedopendata.gr/corine/ontology#> \n"+
				"SELECT ?geometry \n" +
				 "WHERE {\n " + 
				 "  ?area corine:hasLandUse corine:burntAreas . \n" +
				 "  ?area corine:hasGeometry ?geometry .  } \n" +
				 "LIMIT 2" ;              
	}
	
	/**
	 * Test method for {@link eu.earthobservatory.org.StrabonEndpoint.client.SpatialEndpoint#query(java.lang.String, org.openrdf.query.resultio.stSPARQLQueryResultFormat)}.
	 * @throws TupleQueryResultHandlerException 
	 */
	@Test
	public void testQuery() {
			try {
					EndpointResult response;
				
					response = endpoint.queryForKML(query); 
					
					//System.out.println("KML format:");
					//System.out.println(response.getResponse());
					
					if (response.getStatusCode() != 200) {
						System.err.println("Status code ("+response.getStatusCode()+"):" + response.getStatusText());
						
					}
					
				//	assertTrue(response.getStatusCode() == 200);	
				
				}catch (QueryResultParseException e) {
					e.printStackTrace();
				} catch (UnsupportedQueryResultFormatException e) {
					e.printStackTrace();
				}
				catch (TupleQueryResultHandlerException e) {
					e.printStackTrace();
				}
				catch (IOException e) {
					e.printStackTrace();
				} catch (QueryEvaluationException e) {
					e.printStackTrace();
				}
			
	}
}
