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
import org.openrdf.query.resultio.TupleQueryResultFormat;
import org.openrdf.query.resultio.stSPARQLQueryResultFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/*@author Stella Giannakopoulou <sgian@di.uoa.gr>*/

public class BrowseBean extends HttpServlet {

	private static final long serialVersionUID = -378175118289907707L;

	private static Logger logger = LoggerFactory
			.getLogger(eu.earthobservatory.org.StrabonEndpoint.BrowseBean.class);

	/**
	 * Attributes carrying values to be rendered by the browse.jsp file
	 */
	private static final String ERROR = "error";
	private static final String RESPONSE = "response";

	/**
	 * Error returned by BrowseBean
	 */
	private static final String PARAM_ERROR = "stSPARQL Query Results Format or SPARQL query are not set or are invalid.";

	/**
	 * The context of the servlet
	 */
	private ServletContext context;

	/**
	 * Wrapper over Strabon
	 */
	private StrabonBeanWrapper strabonWrapper;


	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doPost(request, response);
	}

	public void init(ServletConfig servletConfig) throws ServletException {
		super.init(servletConfig);

		// get the context of the servlet
		context = getServletContext();

		// get the context of the application
		WebApplicationContext applicationContext = WebApplicationContextUtils
				.getWebApplicationContext(context);

		// the the strabon wrapper
		strabonWrapper = (StrabonBeanWrapper) applicationContext.getBean("strabonBean");

	}

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");

		// check connection details
		if (strabonWrapper.getStrabon() == null) {
			RequestDispatcher dispatcher = request
					.getRequestDispatcher("/connection.jsp");

			// pass the current details of the connection
			request.setAttribute("username", strabonWrapper.getUsername());
			request.setAttribute("password", strabonWrapper.getPassword());
			request.setAttribute("dbname", strabonWrapper.getDatabaseName());
			request.setAttribute("hostname", strabonWrapper.getHostName());
			request.setAttribute("port", strabonWrapper.getPort());
			request.setAttribute("dbengine", strabonWrapper.getDBEngine());

			// pass the other parameters as well
			request.setAttribute("query", request.getParameter("query"));
			request.setAttribute("format", request.getParameter("format"));
			request.setAttribute("handle", request.getParameter("handle"));

			// forward the request
			dispatcher.forward(request, response);

		} else {

			if (Common.VIEW_TYPE.equals(request.getParameter(Common.VIEW))) {
				// HTML visual interface
				processVIEWRequest(request, response);

			} else {// invoked as a service
				processRequest(request, response);
			}
		}
	}

	/**
	 * Processes the request made by a client of the endpoint that uses it as a
	 * service.
	 * 
	 * @param request
	 * @param response
	 * @throws IOException
	 */
	private void processRequest(HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		ServletOutputStream out = response.getOutputStream();

		// get the stSPARQL Query Result format (we check only the Accept
		// header)
		stSPARQLQueryResultFormat format = stSPARQLQueryResultFormat.forMIMEType(request.getHeader("accept"));

		// get the query
		String query = request.getParameter("query");

		// check for required parameters
		if (format == null || query == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			out.print(ResponseMessages.getXMLHeader());
			out.print(ResponseMessages.getXMLException(PARAM_ERROR));
			out.print(ResponseMessages.getXMLFooter());

		} else {
			// decode the query
			// do not decode the SPARQL query (see bugs #65 and #49)
			//query = URLDecoder.decode(request.getParameter("query"), "UTF-8");
			query = request.getParameter("query");

			response.setContentType(format.getDefaultMIMEType());
			try {
				strabonWrapper.query(query, format.getName(), out);
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

	/**
	 * Processes the request made from the HTML visual interface of Strabon
	 * Endpoint.
	 * 
	 * @param request
	 * @param response
	 * @throws ServletException
	 * @throws IOException
	 */
	private void processVIEWRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		RequestDispatcher dispatcher;

		// do not decode the SPARQL query (see bugs #65 and #49)
		//String query = URLDecoder.decode(request.getParameter("query"), "UTF-8");
		String query = request.getParameter("query");
		String format = request.getParameter("format");
		
		// get stSPARQLQueryResultFormat from given format name
		TupleQueryResultFormat queryResultFormat = stSPARQLQueryResultFormat.valueOf(format);

		if (query == null || format == null || queryResultFormat == null) {
			dispatcher = request.getRequestDispatcher("browse.jsp");
			request.setAttribute(ERROR, PARAM_ERROR);
			dispatcher.forward(request, response);

		} else {		
			dispatcher = request.getRequestDispatcher("browse.jsp");
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			request.setAttribute("resource",
					request.getParameter("resource"));

			try {
				strabonWrapper.query(query, format, bos);
				if (format.equals(Common.getHTMLFormat())) {
					request.setAttribute(RESPONSE, bos.toString());
				} else {
					request.setAttribute(RESPONSE, StringEscapeUtils.escapeHtml(bos.toString()));
				}

			} catch (Exception e) {
				logger.error("[StrabonEndpoint.BrowseBean] Error during querying.", e);
				request.setAttribute(ERROR, e.getMessage());

			} finally {
				dispatcher.forward(request, response);
			}			
		}
	}
}