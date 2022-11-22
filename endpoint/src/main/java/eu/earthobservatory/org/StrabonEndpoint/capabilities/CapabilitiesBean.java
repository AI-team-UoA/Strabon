/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * 
 * Copyright (C) 2012, Pyravlos Team
 * 
 * http://www.strabon.di.uoa.gr/
 */
package eu.earthobservatory.org.StrabonEndpoint.capabilities;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import eu.earthobservatory.org.StrabonEndpoint.Common;

/**
 * 
 * @author Charalampos Nikolaou <charnik@di.uoa.gr>
 */
public class CapabilitiesBean extends HttpServlet {
	
	private static final long serialVersionUID = -8941754144139158506L;
	
	/**
	 * The context of the servlet
	 */
	private ServletContext context;

	/**
	 * The name of this web application
	 */
	private String appName;
	
	/**
	 * The capabilities of the endpoint
	 */
	private Capabilities caps;
	
	public void init(ServletConfig servletConfig) throws ServletException {
		super.init(servletConfig);
		
		// get the context of the servlet
		context = getServletContext();
		
		// get the context of the application
		WebApplicationContext applicationContext = WebApplicationContextUtils.getWebApplicationContext(context);

		// the the strabon wrapper
		CapabilitiesDelegateBean capsBean = (CapabilitiesDelegateBean) applicationContext.getBean("capsBean");

		// get the name of this web application
		appName = context.getContextPath().replace("/", "");
				
		// get capabilities
		caps = capsBean.getEndpointCapabilities();
	}

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");
		
		if (Common.VIEW_TYPE.equals(request.getParameter(Common.VIEW))) {
			// HTML visual interface
			processVIEWRequest(request, response);

		} else {// invoked as a service
			processRequest(request, response);
	    }
	}
	
	/**
     * Processes the request made from the HTML visual interface of Strabon Endpoint.
     * 
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException
     */
	private void processVIEWRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		//RequestDispatcher dispatcher;
	}
	
	/**
     * Processes the request made by a client of the endpoint that uses it as a service. 
     * 
     * @param request
     * @param response
     * @throws IOException 
     */
	private void processRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
		ServletOutputStream out = response.getOutputStream();
		
		out.println("Version						: " + caps.getVersion());
		out.println("Supports limit					: " + getYesNo(caps.supportsLimit()));
		out.println("Supports authentication				: " + getYesNo(caps.supportsAuthentication()));
		out.println("Supports modification of database connection	: " + getYesNo(caps.supportsConnectionModification()));
		out.println("Supports SPARQL					: " + getYesNo(caps.supportsQuerying()));
		out.println("Supports SPARQL Update				: " + getYesNo(caps.supportsUpdating()));
		out.println("Supports storing				: " + getYesNo(caps.supportsStoring()));
		out.println("Supports DESCRIBE				: " + getYesNo(caps.supportsDescribing()));
		out.println("Supports CONSTRUCT				: " + getYesNo(caps.supportsDescribing()));
		out.println("Supports browsing				: "	+ getYesNo(caps.supportsBrowsing()));
		out.println();
		
		if (caps instanceof AutoDiscoveryCapabilities) {
			AutoDiscoveryCapabilities autocaps = (AutoDiscoveryCapabilities) caps;
			autocaps.setEndpointDetails(request.getServerName(), request.getServerPort(), appName);
			//autocaps.setEndpointDetails("localhost", 8080, "NOA");
		}
		
		RequestCapabilities reCap = caps.getQueryCapabilities();
		
		if (reCap.getParametersObject() != null) {
			for (Parameter param : reCap.getParametersObject().getParameters()) {
				out.println("Supports parameter  : " + param.getName());
				
				if (param.getAcceptedValues().size() > 0) {
					out.println("    Accepted values : ");
					for (String acceptedValue : param.getAcceptedValues()) {
						out.println("\t\t      " + acceptedValue);
					}
				}
			}
		}
		
		// print supported spatial extension functions for stSPARQL
		if (caps.getstSPARQLSpatialExtensionFunctions() != null) {
			out.println("stSPARQL Extension Functions");
			out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
			for(String extFunc : caps.getstSPARQLSpatialExtensionFunctions()) {
				out.println(extFunc);
			}
		}
		
		out.println();
		
		// print supported spatial extension functions for GeoSPARQL
		if (caps.getGeoSPARQLSpatialExtensionFunctions() != null) {
			out.println("GeoSPARQL Extension Functions");
			out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
			for(String extFunc : caps.getGeoSPARQLSpatialExtensionFunctions()) {
				out.println(extFunc);
			}
		}
		
		out.println();
		
		// print supported units of measure
		if (caps.getUnitsOfMeasure() != null) {
			out.println("Supported Units of Measure (OGC)");
			out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
			for(String uom : caps.getUnitsOfMeasure()) {
				out.println(uom);
			}
		}
	}
	
	private String getYesNo(boolean val) {
		return val ? "yes":"no";
	}
}