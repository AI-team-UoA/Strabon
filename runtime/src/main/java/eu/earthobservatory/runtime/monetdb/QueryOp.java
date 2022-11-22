/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * 
 * Copyright (C) 2010, 2011, 2012, Pyravlos Team
 * 
 * http://www.strabon.di.uoa.gr/
 */
package eu.earthobservatory.runtime.monetdb;

import eu.earthobservatory.utils.Format;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;

import org.openrdf.query.MalformedQueryException;
import org.openrdf.sail.generaldb.exceptions.UnsupportedExtensionFunctionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QueryOp {

	private static Logger logger = LoggerFactory.getLogger(eu.earthobservatory.runtime.monetdb.QueryOp.class);
	
	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) {

		if (args.length < 7) {
			System.err.println("Usage: eu.ist.semsorgrid4env.strabon.Strabon <HOST> <PORT> <DATABASE> <USERNAME> <PASSWORD> <QUERY> ");
			System.err.println("       where <HOST>       is the postgis database host to connect to");
			System.err.println("             <PORT>       is the port to connect to on the database host");		
			System.err.println("             <DATABASE>   is the spatially enabled postgis database that Strabon will use as a backend, ");
			System.err.println("             <USERNAME>   is the username to use when connecting to the database ");
			System.err.println("             <PASSWORD>   is the password to use when connecting to the database");
			System.err.println("             <QUERY>      is the stSPARQL query to evaluate.");
			System.err.println("             <DELET_LOCK> is true when deletion of \"locked\" table should be enforced (e.g., when Strabon has been ungracefully shutdown).");
			System.err.println("             [<FORMAT>]   is the format of your results (XML)");
			System.err.println("             [<OUTPUT FILE>]   is the path of the file to store the results (default: Sustem output) (should be preceeded by the format argument)");
			System.exit(0);
		}

		String host = args[0];
		Integer port = new Integer(args[1]);
		String db = args[2];
		String user = args[3];
		String passwd = args[4];		
		String queryString = args[5];
		boolean forceDelete = Boolean.valueOf(args[6]);
		String resultsFormat = "";
		OutputStream os = System.out;
		if ( args.length == 8 ) {
			resultsFormat = args[7];
		}else if (args.length == 9){
			resultsFormat = args[7];
			try {
				os = new FileOutputStream(new File(args[8]));
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		Strabon strabon = null;
		
		try {
			strabon = new Strabon(db, user, passwd, port, host, forceDelete);
			strabon.query(queryString, Format.fromString(resultsFormat), strabon.getSailRepoConnection(), os);
			
		} catch (UnsupportedExtensionFunctionException e) {
			logger.error("[Strabon.QueryOp] {}", e.getMessage());
			
		} catch (MalformedQueryException e) {
			logger.error("[Strabon.QueryOp] {}", e.getMessage());
			
		} catch (Exception e) {
			logger.error("[Strabon.QueryOp] Error during execution of SPARQL query.", e);
			
		} finally {
			if (strabon != null) {
				strabon.close();
			}
		}
	}
}
