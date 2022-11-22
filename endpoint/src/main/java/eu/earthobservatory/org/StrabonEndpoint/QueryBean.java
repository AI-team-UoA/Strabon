/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * Copyright (C) 2010, 2011, 2012, 2013, 2014 Pyravlos Team
 *
 * http://www.strabon.di.uoa.gr/
 */
package eu.earthobservatory.org.StrabonEndpoint;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.resultio.TupleQueryResultFormat;
import org.openrdf.query.resultio.stSPARQLQueryResultFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import eu.earthobservatory.utils.Format;

/**
 *
 * @author Kostis Kyzirakos <kkyzir@di.uoa.gr>
 * @author Manos Karpathiotakis <mk@di.uoa.gr>
 * @author Charalampos Nikolaou <charnik@di.uoa.gr>
 * @author Stella Giannakopoulou <sgian@di.uoa.gr>
 * @author Konstantina Bereta <konstantina.bereta@di.uoa.gr>
 */
public class QueryBean extends HttpServlet {

    private static final long serialVersionUID = -378175118289907707L;

    private static Logger logger = LoggerFactory.getLogger(eu.earthobservatory.org.StrabonEndpoint.QueryBean.class);

    /**
     * Attributes carrying values to be rendered by the query.jsp file
     */
    private static final String ERROR = "error";
    private static final String RESPONSE = "response";

    /**
     * Error returned by QueryBean
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

    /**
     * The name of the temporary directory to store KML/KMZ files for
     * presentation in Google Maps
     */
    private String tempDirectory;

    /**
     * The absolute path of the temporary directory
     */
    private String basePath;

    /**
     * The name of this web application
     */
    private String appName;

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }

    public void init(ServletConfig servletConfig) throws ServletException {
        super.init(servletConfig);

        // get the context of the servlet
        context = getServletContext();

        // get the context of the application
        WebApplicationContext applicationContext = WebApplicationContextUtils.getWebApplicationContext(context);

        // the the strabon wrapper
        strabonWrapper = (StrabonBeanWrapper) applicationContext.getBean("strabonBean");

        // get the name of this web application
        appName = context.getContextPath().replace("/", "");

    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        // check connection details
        if (strabonWrapper.getStrabon() == null) {
            RequestDispatcher dispatcher = request.getRequestDispatcher("/connection.jsp");

            // pass the current details of the connection
            request.setAttribute("username", strabonWrapper.getUsername());
            request.setAttribute("password", strabonWrapper.getPassword());
            request.setAttribute("dbname", strabonWrapper.getDatabaseName());
            request.setAttribute("hostname", strabonWrapper.getHostName());
            request.setAttribute("port", strabonWrapper.getPort());
            request.setAttribute("dbengine", strabonWrapper.getDBEngine());
            request.setAttribute("googlemapskey", strabonWrapper.getGooglemapskey());

            // pass the other parameters as well
            request.setAttribute("query", request.getParameter("query"));
            if (request.getParameter("format").equalsIgnoreCase("PIECHART")
                    || request.getParameter("format").equalsIgnoreCase("AREACHART")
                    || request.getParameter("format").equalsIgnoreCase("COLUMNCHART")) {
                request.setAttribute("format", "CHART");
            } else {
                request.setAttribute("format", request.getParameter("format"));
            }
            request.setAttribute("handle", request.getParameter("handle"));

            // forward the request
            dispatcher.forward(request, response);

        } else {
            // add googlemapsAPIscriptsource attribute for Google API script source tag
            request.setAttribute("googlemapsAPIscriptsource", strabonWrapper.googleMapsAPIScriptSourceForJSP());

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
    private void processRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ServletOutputStream out = response.getOutputStream();

        // get desired formats (we check only the Accept header)
        List<TupleQueryResultFormat> formats = parseMultiValuedAcceptHeader(request.getHeader("accept"));

        // get the query and the limit
        String query = request.getParameter("query");
        String maxLimit = request.getParameter("maxLimit");

        // check for required parameters
        if (formats.size() == 0 || query == null) {
            logger.error("[StrabonEndpoint.QueryBean] {}", PARAM_ERROR);
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print(ResponseMessages.getXMLHeader());
            out.print(ResponseMessages.getXMLException(PARAM_ERROR));
            out.print(ResponseMessages.getXMLFooter());

        } else {
            // just use the first specified format
        	TupleQueryResultFormat format = formats.get(0);

            // do not decode the SPARQL query (see bugs #65 and #49)
            //query = URLDecoder.decode(request.getParameter("query"), "UTF-8");
            query = request.getParameter("query");

            response.setContentType(format.getDefaultMIMEType());

            try {
                query = strabonWrapper.addLimit(query, maxLimit);
                strabonWrapper.query(query, format.getName(), out);
                response.setStatus(HttpServletResponse.SC_OK);

            } catch (Exception e) {
                logger.error("[StrabonEndpoint.QueryBean] Error during querying.", e);
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

        // check whether Update submit button was fired
        String reqFuncionality = (request.getParameter("submit") == null) ? "" : request.getParameter("submit");

        if (reqFuncionality.equals("Update")) {
            // get the dispatcher for forwarding the rendering of the response
            dispatcher = request.getRequestDispatcher("/Update");
            dispatcher.forward(request, response);

        } else {
            // do not decode the SPARQL query (see bugs #65 and #49)
            //String query = URLDecoder.decode(request.getParameter("query"), "UTF-8");

            String query = request.getParameter("query");
            String format = request.getParameter("format");
            String handle = request.getParameter("handle");
            String maxLimit = request.getParameter("maxLimit");

            // get stSPARQLQueryResultFormat from given format name
            TupleQueryResultFormat queryResultFormat = stSPARQLQueryResultFormat.valueOf(format);

            if (query == null || format == null || queryResultFormat == null) {
                dispatcher = request.getRequestDispatcher("query.jsp");
                request.setAttribute(ERROR, PARAM_ERROR);
                dispatcher.forward(request, response);

            } else {
                query = strabonWrapper.addLimit(query, maxLimit);
                if ("download".equals(handle)) { // download as attachment
                    ServletOutputStream out = response.getOutputStream();

                    response.setContentType(queryResultFormat.getDefaultMIMEType());
                    response.setHeader("Content-Disposition",
                            "attachment; filename=results."
                            + queryResultFormat.getDefaultFileExtension() + "; "
                            + queryResultFormat.getCharset());

                    try {
                        strabonWrapper.query(query, format, out);
                        response.setStatus(HttpServletResponse.SC_OK);

                    } catch (Exception e) {
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        out.print(ResponseMessages.getXMLHeader());
                        out.print(ResponseMessages.getXMLException(e.getMessage()));
                        out.print(ResponseMessages.getXMLFooter());
                    }

                    out.flush();

                } else if (("map".equals(handle) || "map_local".equals(handle) || "timemap".equals(handle))
                        && (queryResultFormat == stSPARQLQueryResultFormat.KML
                        || queryResultFormat == stSPARQLQueryResultFormat.KMZ)) {
                    // show map (only valid for KML/KMZ)

                    // get dispatcher
                    dispatcher = request.getRequestDispatcher("query.jsp");

                    // re-assign handle
                    request.setAttribute("handle", handle);

                    SecureRandom random = new SecureRandom();
                    String temp = new BigInteger(130, random).toString(32);

                    // the temporary KML/KMZ file to create in the server
                    String tempKMLFile = temp + "." + queryResultFormat.getDefaultFileExtension();;

                    try {
                        Date date = new Date();

                        // get the absolute path of the temporary directory
                        if (!request.getParameter("handle").toString().contains("timemap")) {
                            tempDirectory = appName + "-temp";

                            basePath = context.getRealPath("/") + "/../ROOT/" + tempDirectory + "/";
                            // fix the temporary directory for this web application

                            FileUtils.forceMkdir(new File(basePath));

                            @SuppressWarnings("unchecked")
                            Iterator<File> it = FileUtils.iterateFiles(new File(basePath), null, false);
                            while (it.hasNext()) {
                                File tbd = new File((it.next()).getAbsolutePath());
                                if (FileUtils.isFileOlder(new File(tbd.getAbsolutePath()), date.getTime())) {
                                    FileUtils.forceDelete(new File(tbd.getAbsolutePath()));
                                }
                            }
                        } else { //timemap case
                            tempDirectory = "js/timemap";
                            basePath = context.getRealPath("/") + tempDirectory + "/";
                            // fix the temporary directory for this web application
                        }

                        // fix the temporary directory for this web application
                        // create temporary KML/KMZ file
                        File file = new File(basePath + tempKMLFile);
                        // if file does not exist, then create it
                        if (!file.exists()) {
                            file.createNewFile();
                        }

                        try {
                            // query and write the result in the temporary KML/KMZ file
                            FileOutputStream fos = new FileOutputStream(basePath + tempKMLFile);
                            strabonWrapper.query(query, format, fos);
                            fos.close();

                            if (request.getParameter("handle").toString().contains("timemap")) {
                                request.setAttribute("pathToKML", tempDirectory + "/" + tempKMLFile);
                            } else {
                                request.setAttribute("pathToKML",
                                        request.getScheme() + "://"
                                        + request.getServerName() + ":" + request.getServerPort()
                                        + "/" + tempDirectory + "/" + tempKMLFile);
                            }

                        } catch (MalformedQueryException e) {
                            logger.error("[StrabonEndpoint.QueryBean] Error during querying. {}", e.getMessage());
                            request.setAttribute(ERROR, e.getMessage());

                        } catch (Exception e) {
                            logger.error("[StrabonEndpoint.QueryBean] Error during querying.", e);
                            request.setAttribute(ERROR, e.getMessage());
                        }

                        dispatcher.forward(request, response);

                    } catch (IOException e) {
                        logger.error("[StrabonEndpoint.QueryBean] Error during querying.", e);
                    }

                } else { // "plain" is assumed as the default
                    dispatcher = request.getRequestDispatcher("query.jsp");
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();

                    try {
                        strabonWrapper.query(query, format, bos);
                        if (format.equals(Common.getHTMLFormat())) {
                            request.setAttribute(RESPONSE, bos.toString());
                        } else if (format.equals(Format.PIECHART.toString())
                                || format.equals(Format.AREACHART.toString())
                                || format.equals(Format.COLUMNCHART.toString())) {
                            request.setAttribute("format", "CHART");
                            request.setAttribute(RESPONSE, strabonWrapper.getgChartString());
                        } else {
                            request.setAttribute(RESPONSE, StringEscapeUtils.escapeHtml(bos.toString()));
                        }

                    } catch (MalformedQueryException e) {
                        logger.error("[StrabonEndpoint.QueryBean] Error during querying. {}", e.getMessage());
                        request.setAttribute(ERROR, e.getMessage());

                    } catch (Exception e) {
                        logger.error("[StrabonEndpoint.QueryBean] Error during querying.", e);
                        request.setAttribute(ERROR, e.getMessage());

                    } finally {
                        dispatcher.forward(request, response);
                    }
                }
            }
        }
    }

    /**
     * Given an Accept header, it parses it and extracts the mime types for the
     * accepted formats. The header might contain multiple accepted values and
     * qvalues as well, however, qvalues are ignored. The extracted mime types
     * are then transformed to stSPARQLQueryResultFormat and a list of such
     * objects is returned. If a mimetype is not valid, then it is ignored. If
     * all mimetypes are invalid, then the returned list has zero elements, but
     * it is not null.
     *
     * @param header
     * @return
     */
    private List<TupleQueryResultFormat> parseMultiValuedAcceptHeader(String header) {
		List<TupleQueryResultFormat> formats = new ArrayList<TupleQueryResultFormat>();

        StringTokenizer token = new StringTokenizer(header, ", ");

        while (token.hasMoreTokens()) {
            String value = token.nextToken();

            // value might contain qvalues (e.g., "text/plain; q=0.2")
            // for the time being, we just discard them
            int idx_sep_cut = value.indexOf(';');
            if (idx_sep_cut > 0) {
                value = value.substring(0, idx_sep_cut);
            }

            // get the stSPARQL Query Result format 
			String valueFromMime = getValueFromMime(value); 
			TupleQueryResultFormat format = stSPARQLQueryResultFormat.valueOf(valueFromMime);

            // keep only the valid formats (non-null)
            if (format != null) {
                formats.add(format);
            }
        }

        return formats;
    }
    
    private String getValueFromMime(String value) {
		if (value.equalsIgnoreCase("application/sparql-results+json")) {
			return "SPARQL/JSON";
		}
		else if (value.equalsIgnoreCase("application/json") || value.equalsIgnoreCase("application/geojson")) {
			return "GeoJSON";
		}
		else if (value.equalsIgnoreCase("application/vnd.google-earth.kml+xml") || value.equalsIgnoreCase("application/kml")) {
			return "KML";
		}
		else if (value.equalsIgnoreCase("text/tab-separated-values")) {
			return "TSV";
		}
		else if (value.equalsIgnoreCase("text/html")) {
			return "HTML";
		}
		else {
			return "XML";
		}
		
	}

}
