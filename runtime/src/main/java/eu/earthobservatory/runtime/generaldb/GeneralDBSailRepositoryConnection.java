/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * 
 * Copyright (C) 2013, Pyravlos Team
 * 
 * http://www.strabon.di.uoa.gr/
 */
package eu.earthobservatory.runtime.generaldb;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import org.openrdf.OpenRDFUtil;
import org.openrdf.model.Resource;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.repository.sail.SailRepositoryConnection;
import org.openrdf.repository.util.RDFInserter;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.Rio;
import org.openrdf.rio.UnsupportedRDFormatException;
import org.openrdf.rio.helpers.ParseErrorLogger;
import org.openrdf.sail.SailConnection;

/**
 * This class extends {@link SailRepositoryConnection} only to allow
 * for overriding insertion of triples by invoking our implementation
 * of {@link RDFInserter} so that GeoSPARQL Entailment Extension is 
 * incorporated there in a seamless way.  
 * 
 * @author Charalampos Nikolaou <charnik@di.uoa.gr>
 */
public class GeneralDBSailRepositoryConnection extends SailRepositoryConnection {

	protected GeneralDBSailRepositoryConnection(SailRepository repository, SailConnection sailConnection) {
		super(repository, sailConnection);
	}
	
	/**
	 * Adds the data that can be read from the supplied InputStream or Reader to
	 * this repository. 
	 * 
	 * This method is exactly like the one defined in class {@link RepositoryConnectionBase},
	 * except for the fact that uses ???? as the RDF handler to triple insertion instead of
	 * the Sesame's default {@link RDFInserter}. 
	 * 
	 * @param inputStreamOrReader
	 *        An {@link InputStream} or {@link Reader} containing RDF data that
	 *        must be added to the repository.
	 * @param baseURI
	 *        The base URI for the data.
	 * @param dataFormat
	 *        The file format of the data.
	 * @param contexts
	 *        The context(s) to which the data should be added.
	 * @throws IOException
	 * @throws UnsupportedRDFormatException
	 * @throws RDFParseException
	 * @throws RepositoryException
	 */
	protected void addInputStreamOrReader(Object inputStreamOrReader, String baseURI, RDFFormat dataFormat, Resource... contexts) 
																		throws IOException, RDFParseException, RepositoryException {
		OpenRDFUtil.verifyContextNotNull(contexts);

		RDFParser rdfParser = Rio.createParser(dataFormat, getRepository().getValueFactory());
		rdfParser.setParserConfig(getParserConfig());
		rdfParser.setParseErrorListener(new ParseErrorLogger());

		//RDFInserter rdfInserter = new RDFInserter(this);
		RDFInserter rdfInserter = new GeosparqlRDFHandlerBase(this);
		rdfInserter.enforceContext(contexts);
		rdfParser.setRDFHandler(rdfInserter);

		boolean autoCommit = isAutoCommit();
		setAutoCommit(false);

		try {
			if (inputStreamOrReader instanceof InputStream) {
				rdfParser.parse((InputStream)inputStreamOrReader, baseURI);
			}
			else if (inputStreamOrReader instanceof Reader) {
				rdfParser.parse((Reader)inputStreamOrReader, baseURI);
			}
			else {
				throw new IllegalArgumentException(
						"inputStreamOrReader must be an InputStream or a Reader, is a: "
								+ inputStreamOrReader.getClass());
			}
		}
		catch (RDFHandlerException e) {
			if (autoCommit) {
				rollback();
			}
			// RDFInserter only throws wrapped RepositoryExceptions
			throw (RepositoryException)e.getCause();
		}
		catch (RuntimeException e) {
			if (autoCommit) {
				rollback();
			}
			throw e;
		}
		finally {
			setAutoCommit(autoCommit);
		}
	}


}
