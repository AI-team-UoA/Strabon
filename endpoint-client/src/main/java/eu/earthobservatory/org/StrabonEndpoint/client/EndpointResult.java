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

/**
 * @author Charalampos Nikolaou <charnik@di.uoa.gr>
 *
 */
public class EndpointResult {

	private int statusCode;
	private String statusText;
	private String response;
	
	public EndpointResult(int statusCode, String statusLine, String response) {
		this.statusCode = statusCode;
		this.statusText = statusLine;
		this.response = response;
	}
	
	public int getStatusCode() {
		return statusCode;
	}

	public String getStatusText() {
		return statusText;
	}

	public String getResponse() {
		return response;
	}
}
