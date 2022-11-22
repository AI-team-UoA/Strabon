///**
// * This Source Code Form is subject to the terms of the Mozilla Public
// * License, v. 2.0. If a copy of the MPL was not distributed with this
// * file, You can obtain one at http://mozilla.org/MPL/2.0/.
// * 
// * Copyright (C) 2012, Pyravlos Team
// * 
// * http://www.strabon.di.uoa.gr/
// */
//package eu.earthobservatory.org.StrabonEndpoint.client;
//
//
//import static org.junit.Assert.assertTrue;
//
//import java.io.IOException;
//import org.junit.Before;
//import org.junit.Test;
//import org.openrdf.query.resultio.stSPARQLQueryResultFormat;
//
///**
// * @author Stella Giannakopoulou <sgian@di.uoa.gr>
// *
// */
//public class TestLimit {
//
//	private SPARQLEndpoint endpoint; 
//	private String [] testQueries;
//	private stSPARQLQueryResultFormat format;			
//	
//	@Before
//	public void init() {
//		
//		// initialize endpoint		
//		endpoint = new SPARQLEndpoint("localhost", 8080, "strabon-endpoint/Query");		
//		testQueries = new String[6];
//		
//		 // set queries
//		 testQueries[0] = "SELECT *\n" +
//				"WHERE\n"+							
//				"{\n"+
//				"	?x ?y ?z\n" +
//				"}\n" +
//				"limit 3";	
//		 
//		 testQueries[1] = "SELECT *\n" +
//					"WHERE\n"+							
//					"{\n"+
//					"	?x ?limit 5\n" +
//					"}\n" +
//					"limit 3";
//		 
//		 testQueries[2] = "SELECT *\n" +
//					"WHERE\n"+							
//					"{\n"+
//					"	?x ?limit 5\n" +
//					"}\n" +
//					"limit 3\n" +
//					"offset 4";
//		 
//		 testQueries[3] = "SELECT *\n" +
//					"WHERE\n"+							
//					"{\n"+
//					"	?x ?y ?z\n" +
//					"}\n" +
//					"limit 3000";
//		 
//		 testQueries[4] = "SELECT *\n" +
//					"WHERE\n"+							
//					"{\n"+
//					"	?x ?y ?z\n" +
//					"}\n" +
//					"offset 5" +
//					"limit 3000";
//		 
//		 testQueries[5] = "SELECT *\n" +
//					"WHERE\n"+							
//					"{\n"+
//					"	?x ?z 5\n" +
//					"}\n" +
//					"limit3000\n" +
//					"offset 2";
//		
//		// format does not matter for the test
//		format = stSPARQLQueryResultFormat.HTML;								
//	}
//	
//	/**
//	 * Test method for {@link eu.earthobservatory.org.StrabonEndpoint.client.StrabonEndpoint#query(java.lang.String, org.openrdf.query.resultio.stSPARQLQueryResultFormat)}.
//	 */
//	@Test
//	public void testQuery() {				
//				 
//		for (String query : testQueries) {			
//			try {
//				EndpointResult response = endpoint.query(query, format);
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
//		}
//	}	
//}
