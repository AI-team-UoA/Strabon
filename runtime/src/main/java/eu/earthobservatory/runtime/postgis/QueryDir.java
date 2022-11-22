/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * 
 * Copyright (C) 2010, 2011, 2012, Pyravlos Team
 * 
 * http://www.strabon.di.uoa.gr/
 */
package eu.earthobservatory.runtime.postgis;

import eu.earthobservatory.utils.Format;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QueryDir {

	private static Logger logger = LoggerFactory.getLogger(eu.earthobservatory.runtime.postgis.QueryDir.class);
	
	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) {

		if (args.length < 7) {
			System.err.println("Usage: eu.ist.semsorgrid4env.strabon.Strabon <HOST> <PORT> <DATABASE> <USERNAME> <PASSWORD> <PATH> [<FORMAT>]");
			System.err.println("       where <HOST>       is the postgis database host to connect to");
			System.err.println("             <PORT>       is the port to connect to on the database host");		
			System.err.println("             <DATABASE>   is the spatially enabled postgis database that Strabon will use as a backend, ");
			System.err.println("             <USERNAME>   is the username to use when connecting to the database ");
			System.err.println("             <PASSWORD>   is the password to use when connecting to the database");
			System.err.println("             <PATH>       is the path containing all stSPARQL queries to evaluate.");
			System.err.println("             <EXTENSION>  is the extention of the files that contain the stSPARQL queries. (e.g., '.rq')");
			System.err.println("             [<FORMAT>]   is the format of your results (XML)");
			System.exit(0);
		}

		String host = args[0];
		Integer port = new Integer(args[1]);
		String db = args[2];
		String user = args[3];
		String passwd = args[4];
		String path = args[5];
		final String extension = args[6];
		String resultsFormat = "";
		if ( args.length == 8 ) {
			resultsFormat = args[7];
		}

		Strabon strabon = null;
		try {
			strabon = new Strabon(db, user, passwd, port, host, false);
	
			File dir = new File(path);
			FilenameFilter filter = new FilenameFilter() {
				public boolean accept(File dir, String name) {
					return name.endsWith(extension);
				}
			};
	
			String[] children = dir.list(filter);
			if (children != null) {
				for (int i=0; i<children.length; i++) {
					String filename = children[i];
					try {
						logger.info("[Strabon.QueryDir] Evaluating query from '" + path + System.getProperty("file.separator") + filename  +"'.");
						String queryString = readFile(path + System.getProperty("file.separator") + filename);
						logger.info("[Strabon.QueryDir] Evaluating stSPARQL query: \n"+queryString+"\n");
						strabon.query(queryString, Format.fromString(resultsFormat), strabon.getSailRepoConnection(), System.out);
						
					} catch (IOException e) {
						logger.error("[Strabon.QueryDir] IOException while reading " + filename, e);
					}
				}
			}
		} catch (Exception e) {
			logger.error("[Strabon.QueryDir] Error during execution of QueryDir.", e);
			
		} finally {
			if (strabon != null) {
				strabon.close();	
			}
		}
	}

	private static String readFile(String file) throws IOException {
		BufferedReader reader = new BufferedReader( new FileReader(file));
		String line = null;
		StringBuilder stringBuilder = new StringBuilder();
		String ls = System.getProperty("line.separator");
		while( ( line = reader.readLine() ) != null ) {
			stringBuilder.append( line );
			stringBuilder.append( ls );
		}
		reader.close();
		return stringBuilder.toString();
	}
}
