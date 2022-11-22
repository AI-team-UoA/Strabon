/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * 
 * Copyright (C) 2013, Pyravlos Team
 * 
 * http://www.strabon.di.uoa.gr/
 */
package eu.earthobservatory.vocabulary;


/**
 * RDFi vocabulary.
 * 
 * TODO: update the ontology at <a>http://rdfi.di.uoa.gr/ontology#</a>
 *       *) what will be the supported properties?
 *       *) or we will have only extension functions?
 * 
 * @author Charalampos Nikolaou <charnik@di.uoa.gr>
 */
public class RDFi {

	/**
	 * The namespace for the RDFi framework
	 */
	public static final String NAMESPACE = "http://rdfi.di.uoa.gr/ontology#";
	
	/**
	 * RCC-8 relations for the RDFi framework
	 */
	public static final String DC			= NAMESPACE + "DC";
	public static final String EC			= NAMESPACE + "EC";
	public static final String PO			= NAMESPACE + "PO";
	public static final String NTPP		= NAMESPACE + "NTPP";
	public static final String NTPPi		= NAMESPACE + "NTPPi";
	public static final String TPP		= NAMESPACE + "TPP";
	public static final String TPPi		= NAMESPACE + "TPPi";
	public static final String EQ			= NAMESPACE + "EQ";
	
}
