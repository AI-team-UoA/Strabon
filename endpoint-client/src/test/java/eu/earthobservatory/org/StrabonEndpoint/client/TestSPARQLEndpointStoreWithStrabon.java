/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * 
 * Copyright (C) 2013, Pyravlos Team
 * 
 * http://www.strabon.di.uoa.gr/
 */
package eu.earthobservatory.org.StrabonEndpoint.client;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.junit.Before;
import org.junit.Test;
import org.openrdf.rio.RDFFormat;

/**
 * @author Kallirroi Dogani <kallirroi@di.uoa.gr>
 *
 */
public class TestSPARQLEndpointStoreWithStrabon {

	private SPARQLEndpoint endpoint; 
	private URL data;
	
	@Before
	public void init() {
		// initialize endpoint
		endpoint = new SPARQLEndpoint("geo.linkedopendata.gr", 80, "teststrabon-endpoint/Store");
		
		// set url data
		try {
			data = new URL("http://geo.linkedopendata.gr/teststrabon-endpoint/map.nt");
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		endpoint.setUser("endpoint");
		endpoint.setPassword("3ndpo1nt");
		
				
	}
	
	/**
	 * Test method for {@link eu.earthobservatory.org.StrabonEndpoint.client.SPARQLEndpoint#store(java.net.URL, org.openrdf.rio.RDFFormat, java.net.URL)}.
	 * @throws IOException 
	 */
	@Test
	public void testStoreFromUrl() throws IOException {
		
			URL namedGraph = new URL("http://geo.linkedopendata.gr/map/example");
			Boolean response = endpoint.store(data, RDFFormat.NTRIPLES , namedGraph);
			assertTrue(response == true);
	}
	
	
	/**
	 * Test method for {@link eu.earthobservatory.org.StrabonEndpoint.client.SPARQLEndpoint#store(java.lang.String, org.openrdf.rio.RDFFormat, java.net.URL)}.
	 * @throws IOException 
	 */
	@Test
	public void testStore() throws IOException {
		
			URL namedGraph = new URL("http://geo.linkedopendata.gr/map/example");
			String data = "<http://geo.linkedopendata.gr/map/id/l22> <http://geo.linkedopendata.gr/map/hasName> \"layer22\" . ";
			Boolean response = endpoint.store(data, RDFFormat.NTRIPLES , namedGraph);
			
			assertTrue(response == true);
		
	}
}
