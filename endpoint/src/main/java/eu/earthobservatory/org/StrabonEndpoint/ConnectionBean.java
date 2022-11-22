/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * 
 * Copyright (C) 2010, 2011, 2012, Pyravlos Team
 * 
 * http://www.strabon.di.uoa.gr/
 */
package eu.earthobservatory.org.StrabonEndpoint;


import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.util.Properties;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * @author Charalampos Nikolaou <charnik@di.uoa.gr>
 *
 */
public class ConnectionBean extends HttpServlet {

	private static final long serialVersionUID = 2237815345608023368L;

	private static Logger logger = LoggerFactory.getLogger(eu.earthobservatory.org.StrabonEndpoint.ConnectionBean.class);
	
	/**
	 * The filename of the connection.properties file
	 */
	private static final String CONNECTION_PROPERTIES_FILE = "/WEB-INF/connection.properties";
	
	/**
	 * Lock for updating connection.properties file 
	 */
	private static Object lock;
	
	/**
	 * Wrapper over Strabon
	 */
	private StrabonBeanWrapper strabonWrapper;
	
	/**
	 * The context of the servlet
	 */
	private ServletContext context;
	
	//Check for localHost. Works with ipV4 and ipV6
	public static boolean isLocalClient(HttpServletRequest request) { 
        HttpServletRequest testRequest = request; 
        try { 
            	InetAddress remote = InetAddress.getByName(testRequest.getRemoteAddr()); 
            	if (remote.isLoopbackAddress()) { 
            		return true;
            	} 
            	InetAddress localHost = InetAddress.getLocalHost(); 
            	String localAddress = localHost.getHostAddress(); 
            	String remoteAddress = remote.getHostAddress(); 
            	return (remoteAddress != null && remoteAddress.equalsIgnoreCase(localAddress)); 
        } catch (Exception e) { } 
        return false; 
    } 
	
	@Override
	public void init(ServletConfig servletConfig) throws ServletException {
		super.init(servletConfig);

		// get the context of the servlet
		context = getServletContext();
		
		// get the context of the application
		WebApplicationContext applicationContext = WebApplicationContextUtils.getWebApplicationContext(context);

		// the strabon wrapper
		strabonWrapper = (StrabonBeanWrapper) applicationContext.getBean("strabonBean");

		// initialize lock
		lock = new Object();
	}
	
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}
	
	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		RequestDispatcher dispatcher;
		
		//Authorization
		boolean authorized;
		
		if(!isLocalClient(request)) {
			Authenticate authenticate = new Authenticate();
			String authorization = request.getHeader("Authorization");
	   		
			authorized = authenticate.authenticateUser(authorization, context);
		}
		else
			authorized = true;
				
	   	if (!authorized) {	   		 
	   		// not allowed, so report he's unauthorized
	   		response.setHeader("WWW-Authenticate", "BASIC realm=\"Please login\"");
	   		response.sendError(HttpServletResponse.SC_UNAUTHORIZED);	   		 
	   	}
	   	else {	 		
	   	// set new connection details
	 		strabonWrapper.setConnectionDetails(request.getParameter("dbname"), 
	 											request.getParameter("username"), 
	 											request.getParameter("password"), 
	 											request.getParameter("port"),
	 											request.getParameter("hostname"), 
	 											request.getParameter("dbengine"),
                                                                                                request.getParameter("googlemapskey"));
	 		
	 		// pass the query, format, and the handle that had been issued to the dispatcher
	 		request.setAttribute("query",  	request.getAttribute("query"));
	 		request.setAttribute("format",  request.getAttribute("format"));
	 		request.setAttribute("handle", 	request.getAttribute("handle"));

	 		if (logger.isInfoEnabled()) {
	 			logger.info("[StrabonEndpoint.ConnectionBean] Establishing connection with database using new connection details.");
	 		}
	 		
	 		// establish connection
	 		if (strabonWrapper.init()) { // successfully connected, go to query.jsp
	 			if (logger.isInfoEnabled()) {
	 				logger.info("[StrabonEndpoint.ConnectionBean] Connection with database established.");
	 				logger.info("[StrabonEndpoint.ConnectionBean] Saving new connection details in {}.", CONNECTION_PROPERTIES_FILE);
	 			}
	 			
	 			// save the new connection details
	 			saveNewConnectionDetails(request.getParameter("dbname"),
	 									 request.getParameter("username"), 
	 									 request.getParameter("password"), 
	 									 request.getParameter("port"), 
	 									 request.getParameter("hostname"), 
	 									 request.getParameter("dbengine"),
                                                                                 request.getParameter("googlemapskey"));
	 			
	 			if (logger.isInfoEnabled()) {
	 				logger.info("[StrabonEndpoint.ConnectionBean] New connection details succesfully saved.");
	 			}
	 			
	 			// go to query.jsp
	 			dispatcher = request.getRequestDispatcher("/query.jsp");
	 			
	 		} else { // try again
	 			if (logger.isInfoEnabled()) {
	 				logger.info("[StrabonEndpoint.ConnectionBean] Cannot establish connection with database.");
	 			}
	 			
	 			// pass the current details of the connection
	 			request.setAttribute("dbname", 	 request.getParameter("dbname"));
	 			request.setAttribute("username", request.getParameter("username"));
	 			request.setAttribute("password", request.getParameter("password"));
	 			request.setAttribute("port", 	 request.getParameter("port"));
	 			request.setAttribute("hostname", request.getParameter("hostname"));
	 			request.setAttribute("dbengine", request.getParameter("dbengine"));
                                request.setAttribute("googlemapskey", request.getParameter("googlemapskey"));
	 			
	 			dispatcher = request.getRequestDispatcher("/connection.jsp");
	 			
	 		}
	 		
	 		dispatcher.forward(request, response);
	   	 }		
			
	}

	private void saveNewConnectionDetails(String dbname, String username, String password, 
				 		String port, String hostname, String dbengine, String googlemapskey) throws IOException {
		Properties properties = new Properties();
		
		synchronized (lock) {
			// get connection.properties as input stream
			InputStream input = new FileInputStream(context.getRealPath(CONNECTION_PROPERTIES_FILE));
			
			// load the properties
			properties.load(input);
			
			// update them
			properties.setProperty("dbname", 	dbname);
			properties.setProperty("username", 	username);
			properties.setProperty("password", 	password);
			properties.setProperty("port", 		port);
			properties.setProperty("hostname", 	hostname);
			properties.setProperty("dbengine", 	dbengine);
                        properties.setProperty("googlemapskey", googlemapskey);
			
			// store them
			properties.store(new FileOutputStream(context.getRealPath(CONNECTION_PROPERTIES_FILE)), null);
			
			// close the stream
			input.close();
		}
	}
}
