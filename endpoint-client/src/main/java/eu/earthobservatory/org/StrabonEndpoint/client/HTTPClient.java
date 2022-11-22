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

import org.apache.http.client.HttpClient;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;

/**
 * @author Charalampos Nikolaou <charnik@di.uoa.gr>
 *
 */
public abstract class HTTPClient {

	/**
	 * The host on which the endpoint is located.
	 */
	protected String host;
	
	/**
	 * The port of the host.
	 */
	protected int port;
	
	/**
	 * The name of the endpoint.
	 * 
	 * This is useful for {@link SPARQLEndpoint} instances that are usually
	 * deployed in a tomcat container as web applications.
	 */
	protected String endpointName;
	
	/**
	 * The username to be used in case the endpoint requires authentication.
	 */
	protected String user;
	
	/**
	 * The password to be used in case the endpoint requires authentication.
	 */
	protected String password;
	
	/**
	 * The connection manager that manages sharing of connections to endpoints
	 * among several threads.
	 */
	private ClientConnectionManager connectionManager;
	
	/**
	 * The HttpClient to be used for connecting to an endpoint.
	 */
	protected HttpClient hc;
	
	public HTTPClient(String host, int port) {
		this(host, port, "/");
	}
	
	public HTTPClient(String host, int port, String endpointName) {
		this.host = host;
		this.port = port;
		
		this.endpointName = (endpointName == null ? "":endpointName);
		
		// create a connection manager for allowing the users of this class use threads
		connectionManager = new PoolingClientConnectionManager();
		
		// create an HttpClient instance that establishes connections based on the connection manager
		hc = new DefaultHttpClient(connectionManager);
	}
		
	public String getHost() {
		return host;
	}
	
	public int getPort() {
		return port;
	}
	
	public void setUser(String user) {
		this.user = user;
	}
	
	public void setPassword(String pass) {
		this.password = pass;
	}
	
	public String getUser() {
		return user;
	}
	
	public String getPassword() {
		return password;
	}
	
	/**
	 * Returns a URL (actually a {@link String}) for establishing connections
	 * to an endpoint based on the information given to the constructor. 
	 * 
	 * @return
	 */
	public String getConnectionURL() {
		return "http://" + host + ((port == 80) ? "":":" + port) + "/" + endpointName; 
	}
}
