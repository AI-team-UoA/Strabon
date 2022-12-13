/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * 
 * Copyright (C) 2010, 2011, 2012, Pyravlos Team
 * 
 * http://www.strabon.di.uoa.gr/
 */
package org.openrdf.query.resultio.sparqlkml;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.openrdf.query.BindingSet;
import org.openrdf.query.TupleQueryResultHandlerException;
import org.openrdf.query.resultio.TupleQueryResultFormat;
import org.openrdf.query.resultio.TupleQueryResultWriter;
import org.openrdf.query.resultio.stSPARQLQueryResultFormat;
import org.openrdf.query.resultio.sparqlkml.stSPARQLResultsKMLWriter;

/**
 * @author Charalampos Nikolaou <charnik@di.uoa.gr>
 *
 */
public class stSPARQLResultsKMZWriter implements TupleQueryResultWriter {

	/**
	 * The name of the KML file that shall be zipped
	 * (by convention corresponds to "doc")
	 */
	private static final String ZIP_ENTRY_FILENAME = "doc.kml";

	/**
	 * After all a KMZ file is a zipped KML one
	 */
	private stSPARQLResultsKMLWriter kmlWriter;
	
	/**
	 *  The zipped output stream to wrap the original one
	 */
	private ZipOutputStream kmzout;
	
	/**
	 * The zip entry
	 */
	private ZipEntry entry;
	
	public stSPARQLResultsKMZWriter(OutputStream out) {
		// create a zip stream on the given output stream
		kmzout = new ZipOutputStream(out);
		
		// initialize the KMLWriter with that stream instead passing the original
		kmlWriter = new stSPARQLResultsKMLWriter(kmzout);
	}

	@Override
	public void startQueryResult(List<String> bindingNames) throws TupleQueryResultHandlerException {
		try {
			// create a zip entry
			entry = new ZipEntry(ZIP_ENTRY_FILENAME);
			
			// add the zip entry in it
			kmzout.putNextEntry(entry);
			
			// now pass execution to KMLWriter
			kmlWriter.startQueryResult(bindingNames);
			
		} catch (IOException e) {
			throw new TupleQueryResultHandlerException(e);
		}
	}

	@Override
	public void endQueryResult() throws TupleQueryResultHandlerException {

		try {
			// pass execution to KMLWriter
			kmlWriter.endQueryResult();
			
			// close the zip entry
			kmzout.closeEntry();
			
			// close the zip stream
			kmzout.close();
			
		} catch (IOException e) {
			throw new TupleQueryResultHandlerException(e);
		}
	}

	@Override
	public void handleSolution(BindingSet bindingSet) throws TupleQueryResultHandlerException {
		// pass execution to KMLWriter
		kmlWriter.handleSolution(bindingSet);
	}

	@Override
	public TupleQueryResultFormat getTupleQueryResultFormat() {
		return stSPARQLQueryResultFormat.KMZ;
	}

}
