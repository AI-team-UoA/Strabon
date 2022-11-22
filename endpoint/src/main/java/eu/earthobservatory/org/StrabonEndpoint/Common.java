/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * 
 * Copyright (C) 2010, 2011, 2012, 2013 Pyravlos Team
 * 
 * http://www.strabon.di.uoa.gr/
 */
package eu.earthobservatory.org.StrabonEndpoint;

import java.util.ArrayList;
import java.util.List;
import java.util.Collection;

import org.openrdf.query.resultio.TupleQueryResultFormat;
import org.openrdf.query.resultio.stSPARQLQueryResultFormat;
import org.openrdf.rio.RDFFormat;
import info.aduna.lang.service.ServiceRegistry;

/**
 * Keeps common variables shared by beans and .jsp pages.
 *
 * @author Charalampos Nikolaou <charnik@di.uoa.gr>
 * @author Panayiotis Smeros <psmeros@di.uoa.gr>
 */
public class Common {
	  
	/**
	 * Parameter used in JSP files to denote the usage
	 * of the HTML interface
	 */
	public static final String VIEW 			= "view";
	public static final String VIEW_TYPE 		= "HTML";

	/**
	 * Parameters used in the store.jsp file
	 */
	public static final String PARAM_DATA 		= "data";
	public static final String PARAM_FORMAT 	= "format";
	public static final String PARAM_DATA_URL	= "url";
	public static final String PARAM_INFERENCE	= "inference";
	public static final String PARAM_GRAPH	    = "graph";


	
	/**
	 * Submit buttons in store.jsp
	 */
	public static final String SUBMIT_INPUT		= "dsubmit";
	public static final String SUBMIT_URL		= "fromurl";
	
 	/**
	 * Parameters used in connection.jsp and in StrabonBeanWrapper.java/beans.xml
	 */
	public static final String DBBACKEND_POSTGIS = "postgis";
	public static final String DBBACKEND_MONETDB = "monetdb";


	/**
	 * Keeps the registered and available RDF formats.
	 */
	public static List<String> registeredFormats = new ArrayList<String>();

	static {
		if(RDFFormat.BINARY != null)
			registeredFormats.add(RDFFormat.BINARY.getName());
		if(RDFFormat.JSONLD != null)
			registeredFormats.add(RDFFormat.JSONLD.getName());
		if(RDFFormat.N3 != null)
			registeredFormats.add(RDFFormat.N3.getName());
		if(RDFFormat.NQUADS != null)
			registeredFormats.add(RDFFormat.NQUADS.getName());
		if(RDFFormat.RDFA != null)
			registeredFormats.add(RDFFormat.RDFA.getName());
		if(RDFFormat.RDFJSON != null)
			registeredFormats.add(RDFFormat.RDFJSON.getName());
		if(RDFFormat.TRIG != null)
			registeredFormats.add(RDFFormat.TRIG.getName());
		if(RDFFormat.TRIX != null)
			registeredFormats.add(RDFFormat.TRIX.getName());
		if(RDFFormat.TURTLE != null)
			registeredFormats.add(RDFFormat.TURTLE.getName());
	}
	
	/**
	 * Keeps the registered and available stSPARQL Query Results Formats.
	 */
	public static final List<TupleQueryResultFormat> registeredQueryResultsFormats = new ArrayList<TupleQueryResultFormat>();
	
	/**
	 * Keeps the name of the registered and available stSPARQL Query Results Formats.
	 * (to be used in the drop-down menu in query.jsp)
	 */
	public static final List<String> registeredQueryResultsFormatNames = new ArrayList<String>();
	
	// initialize registered and available stSPARQL query results formats
	static {
		for (TupleQueryResultFormat format : stSPARQLQueryResultFormat.values()) {
			//if (format instanceof stSPARQLQueryResultFormat) {
				registeredQueryResultsFormats.add(format);
				registeredQueryResultsFormatNames.add(format.getName());
			//}
		}
	}
	
	/**
	 * Method for getting the name of the HTML stSPARQLQueryResultFormat
	 * in .jsp pages.
	 * 
	 * @return
	 */
	public static final String getHTMLFormat() {
		return stSPARQLQueryResultFormat.HTML.getName();
	}
}
