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

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import org.apache.commons.lang.StringEscapeUtils;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.Rio;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * {@link DescribeBean} implements the 
 * <A href=http://www.w3.org/TR/rdf-sparql-protocol/>SPARQL Protocol for RDF</A>
 * for the DESCRIBE query form of SPARQL 1.1. The service can be accessed in two
 * ways: 
 * 	1) via the HTML visual interface ({@link describe.jsp}) or 
 *  2) via an HTTP client (<tt>wget</tt>, <tt>curl</tt>, <tt>telnet</tt>, or any
 *  other such method).
 *  
 * In the second case, a single parameter is required which is the "query"
 * parameter carrying the SPARQL DESCRIBE query to execute. The client also
 * has to specify the Accept header. The value can be one of the following mime
 * types and determines the RDF format of the response: 
 * "text/plain" (N-Triples), "application/rdf+xml" (RDF/XML), "text/rdf+n3" (N3),
 * "text/turtle" (Turtle), "application/x-trig" (TRIG), "application/trix" (TRIX),
 * and "application/x-binary-rdf" (BinaryRDF).
 *  
 * In case of an error, an appropriate message is wrapped in an XML document 
 * (see also {@link ResponseMessages}).
 * 
 *  
 * @author Charalampos Nikolaou <charnik@di.uoa.gr>
 * 
 */
public class DescribeBean extends HttpServlet{

	private static final long serialVersionUID = -7541662133934957148L;
	
	/**
	 * Attributes carrying values to be rendered by the describe.jsp file 
	 */
	private static final String ERROR		= "error";
	private static final String RESPONSE	= "response";
	
	/**
	 * Error returned by DescribeBean
	 */
	private static final String PARAM_ERROR = "RDF format or SPARQL query are not set or are invalid.";
	
	private StrabonBeanWrapper strabonWrapper;

    @Override
	public void init(ServletConfig servletConfig) throws ServletException {
		super.init(servletConfig);

		// get StrabonWrapper
		ServletContext context = getServletContext();
		WebApplicationContext applicationContext = WebApplicationContextUtils.getWebApplicationContext(context);

		strabonWrapper = (StrabonBeanWrapper) applicationContext.getBean("strabonBean");
	}
    
    @Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}

    @Override
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
		// get the dispatcher for forwarding the rendering of the response
		RequestDispatcher dispatcher = request.getRequestDispatcher("describe.jsp");
		
		// do not decode the SPARQL query (see bugs #65 and #49)
		//String query = URLDecoder.decode(request.getParameter("query"), "UTF-8");
		String query = request.getParameter("query");
		
		String format = request.getParameter("format");
		String handle = request.getParameter("handle");

		//RDFFormat rdfFormat = RDFFormat.valueOf(format);
		RDFFormat rdfFormat;
		if(format.equals("n3"))
			rdfFormat = RDFFormat.N3;
		else if(format.equals("rdfxml"))
			rdfFormat = RDFFormat.RDFXML;
		else if(format.equals("turtle"))
			rdfFormat = RDFFormat.TURTLE;
		else if(format.equals("trig"))
			rdfFormat = RDFFormat.TRIG;
		else if(format.equals("trix"))
			rdfFormat = RDFFormat.TRIX;
		else if(format.equals("binary"))
			rdfFormat = RDFFormat.BINARY;
		else if(format.equals("ntriples"))
			rdfFormat = RDFFormat.NTRIPLES;
		else
			rdfFormat = null;

		if (format == null || query == null || rdfFormat == null) {
			request.setAttribute(ERROR, PARAM_ERROR);
			dispatcher.forward(request, response);
			
		} else {
			// set the query, format and handle to be selected in the rendered page
			//request.setAttribute("query", URLDecoder.decode(query, "UTF-8"));
			//request.setAttribute("format", URLDecoder.decode(reqFormat, "UTF-8"));
			//request.setAttribute("handle", URLDecoder.decode(handle, "UTF-8"));
		
			if ("download".equals(handle)) { // download as attachment
				ServletOutputStream out = response.getOutputStream();
				
				response.setContentType(rdfFormat.getDefaultMIMEType());
			    response.setHeader("Content-Disposition", 
			    				"attachment; filename=results." + 
			    						rdfFormat.getDefaultFileExtension() + "; " + 
			    						rdfFormat.getCharset());
			    
			    try {
					strabonWrapper.describe(query, format, out);
					response.setStatus(HttpServletResponse.SC_OK);
					
			    } catch (Exception e) {
			    	response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					out.print(ResponseMessages.getXMLHeader());
					out.print(ResponseMessages.getXMLException(e.getMessage()));
					out.print(ResponseMessages.getXMLFooter());
			    }
			    
			    out.flush();
			    
			}
			else //plain
			{	
				try {
					ByteArrayOutputStream bos = new ByteArrayOutputStream();
	
					strabonWrapper.describe(query, format, bos);
					
					request.setAttribute(RESPONSE, StringEscapeUtils.escapeHtml(bos.toString()));
					
				} catch (Exception e) {
					request.setAttribute(ERROR, e.getMessage());
				}
				dispatcher.forward(request, response);
			}			
		}
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
		
		// get the RDF format (we check only the Accept header)
        //RDFFormat format = RDFFormat.forMIMEType(request.getHeader("accept"));
		RDFFormat format = Rio.getParserFormatForMIMEType(request.getHeader("accept"));

        // get the query
		String query = request.getParameter("query");
    	
    	// check for required parameters
    	if (format == null || query == null) {
    		response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			out.print(ResponseMessages.getXMLHeader());
			out.print(ResponseMessages.getXMLException(PARAM_ERROR));
			out.print(ResponseMessages.getXMLFooter());
    		
    	} else {
    		// do not decode the SPARQL query (see bugs #65 and #49)
    		//query = URLDecoder.decode(request.getParameter("query"), "UTF-8");
    		
	    	response.setContentType(format.getDefaultMIMEType());
		    response.setHeader("Content-Disposition", 
		    		"attachment; filename=describe." + format.getDefaultFileExtension() + "; " + format.getCharset());
		    
			try {
				strabonWrapper.describe(query, format.getName(), out);
				response.setStatus(HttpServletResponse.SC_OK);
				
			} catch (Exception e) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				out.print(ResponseMessages.getXMLHeader());
				out.print(ResponseMessages.getXMLException(e.getMessage()));
				out.print(ResponseMessages.getXMLFooter());
			}
    	}
    	
    	out.flush();
    }
}
