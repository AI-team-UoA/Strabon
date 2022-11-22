/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * 
 * Copyright (C) 2010, 2011, 2012, Pyravlos Team
 * 
 * http://www.strabon.di.uoa.gr/
 */
package org.openrdf.query.resultio.sparqlhtml;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.List;

import org.openrdf.model.BNode;
import org.openrdf.query.Binding;
import org.openrdf.query.BindingSet;
import org.openrdf.query.TupleQueryResultHandlerException;
import org.openrdf.query.resultio.TupleQueryResultFormat;
import org.openrdf.query.resultio.TupleQueryResultWriter;
import org.openrdf.query.resultio.stSPARQLQueryResultFormat;
import org.openrdf.query.resultio.sparqlxml.stSPARQLXMLWriter;
import org.openrdf.model.Value;
import org.openrdf.model.URI;
import org.openrdf.rio.RioSetting;
import org.openrdf.rio.WriterConfig;
import org.openrdf.query.resultio.QueryResultFormat;

/**
 * @author Charalampos Nikolaou <charnik@di.uoa.gr>
 * 
 */
public class stSPARQLResultsHTMLWriter implements TupleQueryResultWriter {

	public static final String TABLE				= "TABLE";
	public static final String TABLE_ROW_TAG		= "TR";
	public static final String TABLE_HEADER_TAG 	= "TH";
	public static final String TABLE_DATA_TAG		= "TD";
	public static final String LINK				    = "A";
	public static final String LINK_REF				= "HREF";
	public static final String STYLE				= "class";
	public static final String ID					= "id";
	public static final String LINK_ID				= "uri";
	public static final String TABLE_HEADER_CLASS	= "query_results_header";
	public static final String TABLE_DATA_CLASS		= "query_results_data";
	public static final String TABLE_CLASS			= "query_results_table";
	public static final String MORE_LINK			= "comment more";
	
	/**
	 * The underlying XML formatter.
	 */
	private stSPARQLXMLWriter xmlWriter;
	
	/**
	 * The ordered list of binding names of the result.
	 */
	private List<String> bindingNames;
	
	public stSPARQLResultsHTMLWriter(OutputStream out) {
		this(new stSPARQLXMLWriter(out));
	}
	
	public stSPARQLResultsHTMLWriter(stSPARQLXMLWriter writer) {
		xmlWriter = writer;
		xmlWriter.setPrettyPrint(true);
	}
	
	@Override
	public void startQueryResult(List<String> bindingNames)
			throws TupleQueryResultHandlerException {
		
		try {
			// keep the order of binding names
			this.bindingNames = bindingNames;			
			// set style for table
			xmlWriter.setAttribute(STYLE, TABLE_CLASS);					
			// write start of table
			xmlWriter.startTag(TABLE);			
			// write Table header containing the bindings
			xmlWriter.startTag(TABLE_ROW_TAG);
			for (String bindingName: bindingNames) {
				// set style for header
				xmlWriter.setAttribute(STYLE, TABLE_HEADER_CLASS);
				xmlWriter.textElement(TABLE_HEADER_TAG, bindingName);
			}
			
			xmlWriter.endTag(TABLE_ROW_TAG);
		} catch (IOException e) {
			throw new TupleQueryResultHandlerException(e);
		}
		
	}

	@Override
	public void endQueryResult() throws TupleQueryResultHandlerException {
		try {
			
			// write end of table
			xmlWriter.endTag(TABLE);
						
			// needed to flush data
			xmlWriter.endDocument();
			
		} catch (IOException e) {
			throw new TupleQueryResultHandlerException(e);
		}
	}

	@Override
	public void handleSolution(BindingSet bindingSet) throws TupleQueryResultHandlerException {
		try {
			StringBuilder value = new StringBuilder();
			Value boundValue = null;
			String href;
			
			// if set to FALSE, urls link to web. if set to TRUE, urls are described //
			boolean linkURL = true; 
			///////////////////////////////////////////////////////////////////////////
			
			
			xmlWriter.startTag(TABLE_ROW_TAG);
			for (String bindingName : bindingNames) {
				Binding binding = bindingSet.getBinding(bindingName);
				if(binding != null)
				{	
					boundValue = binding.getValue();
					value.append(boundValue.stringValue());
														
					if(boundValue instanceof BNode) {
						value.insert(0, "_:");
					}															
					
					// If the value is a uri, make it link
					if(boundValue instanceof URI)
					{
						xmlWriter.setAttribute(STYLE, TABLE_DATA_CLASS);
						xmlWriter.startTag(TABLE_DATA_TAG);
						
						// select all the triples that contain the boundValue  
						if (linkURL){
							String query= "select * " +
								"where " +
								"{ " +
								  "?subject ?predicate ?object . "+
								  "FILTER((?subject = <"+ boundValue.toString()+ ">) || "+
								         "(?predicate = <"+ boundValue.toString()+ ">)  || "+
								         "(?object = <"+ boundValue.toString()+ ">)) " +  
								"}";
							
							// FIXME maybe using URLEncoder.encode() for encoding the query and the "boundValue" 
							// is not the proper way to encode the final URL (see related bugs #65 and #49), but
							// I am not 100% sure
							href = "Browse?view=HTML&query="+URLEncoder.encode(query, "UTF-8")+"&format=HTML&resource="+URLEncoder.encode(boundValue.toString(), "UTF-8");
							
						}
						else{							
							href = boundValue.toString();
						}
						xmlWriter.setAttribute(LINK_REF, href);
						xmlWriter.startTag(LINK);							
						xmlWriter.text(boundValue.toString());					
						xmlWriter.endTag(LINK);							
					}
					else
					{	
						xmlWriter.setAttribute(STYLE, TABLE_DATA_CLASS+" "+MORE_LINK);												
						xmlWriter.startTag(TABLE_DATA_TAG);
						xmlWriter.text(boundValue.toString());
					}																					
					xmlWriter.endTag(TABLE_DATA_TAG);							
				}
				else
				{
					xmlWriter.setAttribute(STYLE, TABLE_DATA_CLASS);
					xmlWriter.startTag(TABLE_DATA_TAG);
					xmlWriter.endTag(TABLE_DATA_TAG);						
				}	
				value.setLength(0);
			}
			xmlWriter.endTag(TABLE_ROW_TAG);
			
		} catch (IOException e) {
			throw new TupleQueryResultHandlerException(e);
		}
	}

	@Override
	public TupleQueryResultFormat getTupleQueryResultFormat() {
		return stSPARQLQueryResultFormat.HTML;
	}


	@Override
	public void handleLinks(List<String> linkUrls){}

	@Override
	public void handleBoolean(boolean value){}

	@Override
	public Collection<RioSetting<?>> getSupportedSettings(){return null;}

	@Override
	public WriterConfig	getWriterConfig(){ return null;}

	@Override
	public void	setWriterConfig(WriterConfig config) {}

	@Override
	public void startHeader(){}

	@Override
	public void startDocument() {}

	@Override
	public void	endHeader(){}

	@Override
	public void	handleStylesheet(String stylesheetUrl) {}

	@Override
	public void	handleNamespace(String prefix, String uri) {}

	@Override
	public QueryResultFormat getQueryResultFormat() {return null;}
}