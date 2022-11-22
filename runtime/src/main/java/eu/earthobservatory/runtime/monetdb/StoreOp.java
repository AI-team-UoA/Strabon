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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StoreOp {

	private static Logger logger = LoggerFactory.getLogger(eu.earthobservatory.runtime.monetdb.StoreOp.class);
	
	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) {

		if (args.length < 6) {
			help();
			System.exit(1);
		}

		String host = args[0];
		Integer port = new Integer(args[1]);
		String db = args[2];
		String user = args[3];
		String passwd = args[4];		
		String src = args[5];
		String format = "NTRIPLES";
		String graph = null;
		Boolean inference = false;
		
		for (int i = 6; i < args.length; i += 2) {
			if (args[i].equals("-f")) {
				if (i + 1 >= args.length) {
					System.err.println("Option \"-f\" requires an argument.");
					help();
					System.exit(1);
					
				} else {
					format = args[i+1];
				}
			} else if (args[i].equals("-g")) {
				if (i + 1 >= args.length) {
					System.err.println("Option \"-g\" requires an argument.");
					help();
					System.exit(1);
					
				} else {
					graph = args[i+1];
				}			
			} else if (args[i].equals("-i")) {
				if (i + 1 >= args.length) {
					System.err.println("Option \"-i\" requires an argument.");
					help();
					System.exit(1);
					
				} else {
					inference = Boolean.valueOf(args[i+1]);
				}
				
			} else {
					System.err.println("Unknown argument \"" + args[i] + "\".");
					help();
					System.exit(1);
			}
		}

		Strabon strabon = null;
		try {
			strabon = new Strabon(db, user, passwd, port, host, false);
			if (graph == null) {
				strabon.storeInRepo(src, format, inference);
				
			} else {
				strabon.storeInRepo(src, null, graph, format, inference);
			}
		
		} catch (Exception e) {
			logger.error("[Strabon.StoreOp] Error during store.", e);
			
		} finally {
			if (strabon != null) {
				strabon.close();
			}
		}
	}
	
	private static void help() {
		System.err.println("Usage: eu.earthobservatory.runtime.monetdb.StoreOp <HOST> <PORT> <DATABASE> <USERNAME> <PASSWORD> <FILE> [-f <FORMAT>] [-g <NAMED_GRAPH>] [-i <INFERENCE>]");
		System.err.println("       where <HOST>       		 is the postgis database host to connect to");
		System.err.println("             <PORT>       		 is the port to connect to on the database host");		
		System.err.println("             <DATABASE>   		 is the spatially enabled postgis database that Strabon will use as a backend");
		System.err.println("             <USERNAME>   		 is the username to use when connecting to the database");
		System.err.println("             <PASSWORD>   		 is the password to use when connecting to the database");
		System.err.println("             <FILE>       		 is the file to be stored");
		System.err.println("             [-f <FORMAT>] 		 is the format of the file (default: NTRIPLES)");
		System.err.println("             [-g <NAMED_GRAPH>]  is the URI of the named graph to store the input file (default: default graph)");
		System.err.println("             [-i <INFERENCE>] 	 is true when inference is enabled (default: false)");
	}

}
