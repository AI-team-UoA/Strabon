/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * 
 * Copyright (C) 2010, 2011, 2012, Pyravlos Team
 * 
 * http://www.strabon.di.uoa.gr/
 */
package eu.earthobservatory.org.StrabonEndpoint;

/**
 * This class holds the methods constructing the various
 * responses to a client issuing a query, update, or store
 * request.
 * 
 * @author charnik
 *
 */
public class ResponseMessages {

	/**
	 * Used as the template answer for UPDATE queries.
	 * @return
	 */
	public static String getXMLHeader() {
		return "<?xml version='1.0' encoding='UTF-8'?>\n" +
			   "<response>\n" +
			   "\t";
	}
	
	/**
	 * Used as the template answer for UPDATE queries.
	 * Actually, it encloses msg around an <exception>
	 * XML element tag.
	 * @param msg
	 * @return
	 */
	public static String getXMLException(String msg) {
		return "<exception>\n"+msg+"\n\t</exception>";
	}
	
	/**
	 * Used as the template answer for UPDATE queries.
	 * @return
	 */
	public static String getXMLFooter() {
		return "\n</response>\n";
	}
	
	
	
}
