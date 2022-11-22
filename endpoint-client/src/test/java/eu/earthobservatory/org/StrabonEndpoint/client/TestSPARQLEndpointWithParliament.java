///* This Source Code Form is subject to the terms of the Mozilla Public
// * License, v. 2.0. If a copy of the MPL was not distributed with this
// * file, You can obtain one at http://mozilla.org/MPL/2.0/.
// * 
// * Copyright (C) 2013, Pyravlos Team
// * 
// * http://www.strabon.di.uoa.gr/
// */
//package eu.earthobservatory.org.StrabonEndpoint.client;
//
//import static org.junit.Assert.assertTrue;
//
//import java.io.IOException;
//
//import org.junit.Before;
//import org.junit.Test;
//import org.openrdf.query.resultio.stSPARQLQueryResultFormat;
//
///**
// * @author Kallirroi Dogani <kallirroi@di.uoa.gr>
// *
// */
//
//public class TestSPARQLEndpointWithParliament {
//
//	private SPARQLEndpoint endpoint; 
//	private String query;
//	
//	@Before
//	public void init() {
//		// initialize endpoint
//		endpoint = new SPARQLEndpoint("luna.di.uoa.gr", 8080, "parliament/sparql");
//		
//		// set query
//		query = "PREFIX ex: <http://example.org/> \n" +
//				"SELECT ?k ?g WHERE {\n" +
//				" ex:pol1 ?k ?g\n" +
//				"}" +
//				"\nLIMIT 1";
//				
//	}
//	
//	/**
//	 * Test method for {@link eu.earthobservatory.org.StrabonEndpoint.client.SPARQLEndpoint#query(java.lang.String, org.openrdf.query.resultio.stSPARQLQueryResultFormat)}.
//	 */
//	@Test
//	public void testQuery() {
//			try {
//				EndpointResult response = endpoint.query(query, stSPARQLQueryResultFormat.XML);
//				
//				System.out.println(response.getResponse());
//				
//				if (response.getStatusCode() != 200) {
//					System.err.println("Status code ("+response.getStatusCode()+"):" + response.getStatusText());
//					
//				}
//				
//				assertTrue(response.getStatusCode() == 200);
//				
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//			
//	}
//	
//	/**
//	 * Test method for testing that method {@link eu.earthobservatory.org.StrabonEndpoint.client.SPARQLEndpoint#query(java.lang.String, org.openrdf.query.resultio.stSPARQLQueryResultFormat)}.
//	 * returns an IOException when it should do so.
//	 */
//	@Test(expected= IOException.class)
//	public void testIOException() throws Exception {
//		SPARQLEndpoint ep = new SPARQLEndpoint("blabla.dgr", 80, "bla");
//		ep.query(query, stSPARQLQueryResultFormat.XML);
//	}
//}
