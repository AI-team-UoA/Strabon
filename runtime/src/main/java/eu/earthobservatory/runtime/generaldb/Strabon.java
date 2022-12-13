/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * 
 * Copyright (C) 2010, 2011, 2012, 2013 Pyravlos Team
 * 
 * http://www.strabon.di.uoa.gr/
 */
package eu.earthobservatory.runtime.generaldb;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import javax.servlet.ServletOutputStream;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;

import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.BindingSet;
import org.openrdf.query.GraphQuery;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.BooleanQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.TupleQueryResultHandlerException;
import org.openrdf.query.Update;
import org.openrdf.query.UpdateExecutionException;
import org.openrdf.query.resultio.TupleQueryResultWriter;
import org.openrdf.query.resultio.BooleanQueryResultWriter;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.repository.sail.SailRepositoryConnection;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.sail.helpers.SailBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.earthobservatory.utils.Format;
import eu.earthobservatory.utils.RDFHandlerFactory;
import eu.earthobservatory.utils.stSPARQLQueryResultToFormatAdapter;
import org.openrdf.query.resultio.sparqlxml.SPARQLBooleanXMLWriter;
import org.openrdf.query.resultio.text.BooleanTextWriter;

public abstract class Strabon {

	private static Logger logger = LoggerFactory.getLogger(eu.earthobservatory.runtime.generaldb.Strabon.class);
	/**
	 * Connection details (shared with subclasses)
	 */
	protected String databaseName;
	protected String user;
	protected String password;
	protected int port;
	protected String serverName;
	
	protected SailBase db_store;
	private SailRepository repo;
	private SailRepositoryConnection con = null;

	public Strabon(String databaseName, String user, String password, int port, String serverName, boolean checkForLockTable) throws Exception {
		this.databaseName = databaseName;
		this.user = user;
		this.password = password;
		this.port = port;
		this.serverName = serverName;
		
		if (checkForLockTable == true) { // force check of locked table and delete if exists
			checkAndDeleteLock(databaseName, user, password, port, serverName);
			
		} else if (isLocked()) { // check for lock and exit if exists
			throw new Exception("Cannot connect to database. Database is already locked by another process.");
			
		}

		long start = System.currentTimeMillis();
		initiate(databaseName, user, password, port, serverName);
		long end = System.currentTimeMillis();
		
		logger.info("[Strabon] Initialization took {} ms.", (end - start));
	}


	/**
	 * Deregisters the JDBC driver. This is advisable when running <tt>Strabon</tt>
	 * through the <tt>strabon-endpoint</tt>, i.e., in a container, such as Apache Tomcat.
	 * Earlier versions of Tomcat would not deregister the JDBC drivers leading
	 * to memory leaks.
	 * 
	 * Deregistering the JDBC driver when running Strabon through <tt>Java</tt> is not required.
	 * Instead, it might lead to unexpected errors when creating many <tt>Strabon</tt> instances,
	 * one after the other, and deregistering the driver. Subsequent instantiations of
	 * <tt>Strabon</tt> in the same <tt>Java</tt> run would fail to load the driver again.
	 */
	public abstract void deregisterDriver();
	
	/**
	 * Called in Strabon constructor to initialize Strabon (establish connection to the
	 * underlying database, etc.).
	 * 
	 * @param databaseName
	 * @param user
	 * @param password
	 * @param port
	 * @param serverName
	 */
	protected abstract void initiate(String databaseName, String user, String password, int port, String serverName);

	protected void init() {

		//Setting up store

		//Used for the conversions taking place involving JTS + WGS84 (4326)
		//Hints.putSystemDefault(Hints.FORCE_LONGITUDE_FIRST_AXIS_ORDER, Boolean.FALSE);
		System.setProperty("org.geotools.referencing.forceXY", "true");
		
		//our repository
		repo = new GeneralDBSailRepository(db_store);

		try {
			repo.initialize();
			
		} catch (RepositoryException e) {
			logger.error("[Strabon.init] initialize", e);
		}

		try {
			con = repo.getConnection();
			
		} catch (RepositoryException e) {
			logger.error("[Strabon.init] getConnection", e);
		}
	}

	/**
	 * Check whether the database is locked by another instance of Strabon or Endpoint.
	 * 
	 * @return
	 */
	protected abstract boolean isLocked() throws SQLException, ClassNotFoundException;

	protected abstract void checkAndDeleteLock(String databaseName, String user, String password, int port, String serverName)
			throws SQLException, ClassNotFoundException;

	public SailRepositoryConnection getSailRepoConnection() {
		return con;
	}

	public void setCon1(SailRepositoryConnection con1) {
		this.con = con1;
	}

	/**
	 * Close connection to Strabon.
	 */
	public void close() {
		logger.info("[Strabon.close] Closing connection...");

		try {
			con.commit();
			
		} catch (RepositoryException e) {
			logger.error("[Strabon.close]", e);
			
		} finally {
			try {
				con.close();
				repo.shutDown();
				
				// delete the lock as well
				checkAndDeleteLock(databaseName, user, password, port, serverName);
				
			} catch (RepositoryException e) {
				logger.error("[Strabon.close]", e);
				
			}catch (SQLException e) {
				logger.error("[Strabon.close] Error in deleting lock", e);
				
			} catch (ClassNotFoundException e) {
				logger.error("[Strabon.close] Error in deleting lock", e);
			}
			
			logger.info("[Strabon.close] Connection closed.");
		}
	}

	public Object query(String queryString, OutputStream out)
	throws  MalformedQueryException, QueryEvaluationException, IOException, TupleQueryResultHandlerException {
		return query(queryString, Format.DEFAULT, this.getSailRepoConnection(), out);	
	}

	public Object query(String queryString, Format resultsFormat, OutputStream out)
	throws  MalformedQueryException , QueryEvaluationException, IOException, TupleQueryResultHandlerException {
		return query(queryString, resultsFormat, this.getSailRepoConnection(), out);
	}

	public ArrayList<String> query(String queryString, SailRepositoryConnection con)
	throws  MalformedQueryException, QueryEvaluationException, IOException, TupleQueryResultHandlerException {
		TupleQuery tupleQuery = null;
		ArrayList<String> ret = new ArrayList<String>();
		
		try {

			tupleQuery = con.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
			TupleQueryResult result = tupleQuery.evaluate();

			while (result.hasNext()) {
				BindingSet bindingSet = result.next();
				ret.add(bindingSet.toString());
			}
			
		} catch (RepositoryException e) {
			logger.error("[Strabon.query] Error in preparing tuple query.", e);
			
		}
		
		return ret;
	}

	public Object query(String queryString, Format resultsFormat, SailRepositoryConnection con, OutputStream out)
	throws MalformedQueryException, QueryEvaluationException, IOException, TupleQueryResultHandlerException {
		boolean status = true;
		
		logger.info("[Strabon.query] Executing query: {}", queryString);
		
		// check for null stream
		if (out == null) {
			logger.error("[Strabon.query] Cannot write to null stream.");
			
			return false;
		}
		
		TupleQuery tupleQuery = null;
		BooleanQuery askQuery = null;
		Boolean isAskQuery = false;
		try {
			tupleQuery = con.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
		} catch (Exception e1) {
				try {
					askQuery = con.prepareBooleanQuery(QueryLanguage.SPARQL, queryString);
					isAskQuery = true;
				} catch (Exception e2) {
					logger.error("[Strabon.query] Error in preparing tuple or ask query.", e2);
					status = false;
					throw new RuntimeException(e2.getMessage());
				}
		}
		
		if (logger.isDebugEnabled()) {
			logger.debug("Serializing results ({})", resultsFormat.name());
		}
		if (!isAskQuery) {
			TupleQueryResult result = null;
			switch (resultsFormat) {
				case EXP:
					long results = 0;
					
					long t1 = System.nanoTime();
					result = tupleQuery.evaluate();
					long t2 = System.nanoTime();
					
					while (result.hasNext()) {
						results++;
						result.next();
					}
					
					long t3 = System.nanoTime();

					logger.info((t2-t1)+" + "+(t3-t2)+" = "+(t3-t1)+" | "+results);
					return new long[]{t2-t1, t3-t2, t3-t1, results};
	//				break;
				
				case TUQU:
					
					return tupleQuery;
	//				break;	
				case PIECHART:
					return tupleQuery.evaluate();
					
				case AREACHART:
					return tupleQuery.evaluate();

				case COLUMNCHART:
					return tupleQuery.evaluate();
					
				default:
					// get the writer for the specified format
					TupleQueryResultWriter resultWriter = stSPARQLQueryResultToFormatAdapter.createstSPARQLQueryResultWriter(resultsFormat, out);
					
					// check for null format
					if (resultWriter == null) {
						logger.error("[Strabon.query] Invalid format.");
						return false;
					}

					tupleQuery.evaluate(resultWriter);
			}
		} else {
				Boolean result = false;
				switch (resultsFormat) {
					case EXP:
						long results = 0;
						long t1 = System.nanoTime();
						result = askQuery.evaluate();
						long t2 = System.nanoTime();
						long t3 = System.nanoTime();

						logger.info((t2-t1)+" + "+(t3-t2)+" = "+(t3-t1)+" | "+"1");
						return new long[]{t2-t1, t3-t2, t3-t1, results};
					case TUQU:
						return askQuery;
					case PIECHART:
						return askQuery.evaluate();

					case AREACHART:
						return askQuery.evaluate();

					case COLUMNCHART:
						return askQuery.evaluate();

					default:
						result = askQuery.evaluate();
						BooleanQueryResultWriter writer = null;
						String output = null;
						switch(resultsFormat) {
							case SESAME_JSON:
								output = "{\n \"head\": {},\n \"boolean\":" + String.valueOf(result) + "\n}";
								out.write(output.getBytes());
								break;
							case XML:
							case SESAME_XML:
								writer = new SPARQLBooleanXMLWriter(out);
								writer.write(result);
								break;
							case TSV:
							case DEFAULT:
							case SESAME_CSV:
							case SESAME_TSV:
								output = "\"bool\"\n" + (result ? "1" : "0") + "\n";
								out.write(output.getBytes());
								break;
							case HTML:
								writer = new BooleanTextWriter(out);
								writer.write(result);
								break;
							case SESAME_BINARY:
								output = (result ? "1" : "0");
								out.write(output.getBytes());
								break;
							default:
								logger.error("[Strabon.query] Invalid format.");
								return false;
						}
				}
		}

		return status;
	}

	public void update(String updateString, SailRepositoryConnection con) throws MalformedQueryException 
	{
		Update update = null;

		try {
			update = con.prepareUpdate(QueryLanguage.SPARQL, updateString);
			
		} catch (RepositoryException e) {
			logger.error("[Strabon.update]", e);
		}

		logger.info("[Strabon.update] executing update query: " + updateString);

		try {
			update.execute();
		} catch (UpdateExecutionException e) {
			logger.error("[Strabon.update]", e);
		}
	}

	public void storeInRepo(String src, String format, Boolean inference) throws RDFParseException, RepositoryException, IOException, RDFHandlerException, InvalidDatasetFormatFault
	{
		storeInRepo(src, null, null, format, inference);
	}

	public void storeInRepo(String src, String baseURI, String context, String format, Boolean inference) throws RDFParseException, RepositoryException, IOException, RDFHandlerException, InvalidDatasetFormatFault
	{
		RDFFormat realFormat = null;

		GeosparqlRDFHandlerBase.ENABLE_INFERENCE=inference;
		
		if ((baseURI != null) && (baseURI.equals(""))) {
			baseURI = null;
		}

		URI uriContext;

		if ((context == null) || (context.equals(""))) {
			uriContext  = null;
			
		} else {
			ValueFactory f = repo.getValueFactory();
			uriContext = f.createURI(context);
		}

		if(format.equalsIgnoreCase("N3") || format.equals(RDFFormat.N3.getName())) {
			realFormat =  RDFFormat.N3;
			
		} else if(format.equalsIgnoreCase("NTRIPLES") || format.equals(RDFFormat.NTRIPLES.getName())) {
			realFormat =  RDFFormat.NTRIPLES;
			
		} else if(format.equalsIgnoreCase("RDFXML") || format.equals(RDFFormat.RDFXML.getName())) {
			realFormat =  RDFFormat.RDFXML;
			
		} else if(format.equalsIgnoreCase("TURTLE") || format.equals(RDFFormat.TURTLE.getName())) {
			realFormat =  RDFFormat.TURTLE;
			
		} else {
			throw new InvalidDatasetFormatFault();
		}

		try{
			URL source = new URL(src);
			storeURL(source, baseURI, uriContext, realFormat);

		} catch(MalformedURLException e) {

			URL fromClasspath = getClass().getResource(src);
			if(fromClasspath!=null) {
				storeURL(fromClasspath, baseURI, uriContext, realFormat);
				
			} else {
				File file = new File(src);
				if (file.exists()) {
					storeURL(new URL("file://" + src), baseURI, uriContext, realFormat);

				} else {
					logger.info("File \"{}\" does not exist. Trying reading as String.", src);
					storeString((String) src, baseURI, uriContext, realFormat);
				}
			}
		}
	}

	private void storeURL(URL url, String baseURI, URI context, RDFFormat format) throws RDFParseException, RepositoryException, IOException, RDFHandlerException
	{
		logger.info("[Strabon.storeURL] Storing file.");
		logger.info("[Strabon.storeURL] URL      : {}", url.toString());
		logger.info("[Strabon.storeURL] Context  : {}", ((context == null) ? "default" : context));
		logger.info("[Strabon.storeURL] Base URI : {}", ((baseURI == null) ? "null" : baseURI));
		logger.info("[Strabon.storeURL] Format   : {}", ((format == null) ? "null" : format));

		if (context == null) {
			con.add(url, baseURI, format);
			
		} else {
			con.add(url, baseURI, format, context);
		}
		
		logger.info("[Strabon.storeURL] Storing was successful.");
	}

	private void storeString(String text, String baseURI, URI context, RDFFormat format) throws RDFParseException, RepositoryException, IOException, RDFHandlerException
	{
		if (baseURI == null) {
			baseURI = "";
		}

		logger.info("[Strabon.storeString] Storing triples.");
		logger.info("[Strabon.storeString] Text     : " + text);
		logger.info("[Strabon.storeString] Base URI : " + baseURI);
		logger.info("[Strabon.storeString] Context  : " + ((context == null) ? "null" : context));
		logger.info("[Strabon.storeString] Format   : " + ((format == null) ? "null" : format.toString()));

		StringReader reader = new StringReader(text);

		if (context == null) {
			con.add(reader, baseURI, format);
			
		} else {
			con.add(reader, baseURI, format, context);
			
		}
		reader.close();
		
		logger.info("[Strabon.storeString] Storing was successful.");
	}

	public void describe(String describeString, String format, SailRepositoryConnection con, OutputStream out) throws MalformedQueryException
	{
		GraphQuery  graphQuery = null;

		try {
			graphQuery = con.prepareGraphQuery(QueryLanguage.SPARQL, describeString);
			
		} catch (RepositoryException e) {
			logger.error("[Strabon.describe]", e);
		}

		logger.info("[Strabon.describe] Executing DESCRIBE query:" + describeString);

		try {
			graphQuery.evaluate(RDFHandlerFactory.createRDFHandler(format, out));

		} catch (Exception e) {
			logger.error("[Strabon.describe]", e);
		}

		logger.info("[Strabon.describe] DESCRIBE query executed successfully.");
	}
}
