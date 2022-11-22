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


public class UpdateOp {

	private static Logger logger = LoggerFactory.getLogger(eu.earthobservatory.runtime.monetdb.UpdateOp.class);
	
	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) {

		if (args.length < 6) {
			System.err.println("Usage: eu.ist.semsorgrid4env.strabon.Strabon <HOST> <PORT> <DATABASE> <USERNAME> <PASSWORD> <UPDATE> ");
			System.err.println("       where <HOST>       is the postgis database host to connect to");
			System.err.println("             <PORT>       is the port to connect to on the database host");		
			System.err.println("             <DATABASE>   is the spatially enabled postgis database that Strabon will use as a backend, ");
			System.err.println("             <USERNAME>   is the username to use when connecting to the database ");
			System.err.println("             <PASSWORD>   is the password to use when connecting to the database");
			System.err.println("             <UPDATE>     is the stSPARQL update query to evaluate.");
			System.exit(0);
		}

		String host = args[0];
		Integer port = new Integer(args[1]);
		String db = args[2];
		String user = args[3];
		String passwd = args[4];		
		String queryString = args[5];
		
		Strabon strabon = null;
		try {
			strabon = new Strabon(db, user, passwd, port, host, false);
			strabon.update(queryString, strabon.getSailRepoConnection());
			
		} catch (Exception e) {
			logger.error("[Strabon.UpdateOp] Error during execution of UPDATE query.", e);
			
		} finally {
			if (strabon != null) {
				strabon.close();
			}
		}
	}
}
