/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * 
 * Copyright (C) 2013, Pyravlos Team
 * 
 * http://www.strabon.di.uoa.gr/
 */
package org.openrdf.sail.generaldb.exceptions;

import org.openrdf.query.QueryEvaluationException;

/**
 * This exception is raised when a SPARQL query contains an
 * extension function that is not supported by Strabon. 
 * 
 * @author Charalampos Nikolaou <charnik@di.uoa.gr>
 */
public class UnsupportedExtensionFunctionException extends QueryEvaluationException {
	private static final long serialVersionUID = -5926890463141193859L;

	public UnsupportedExtensionFunctionException() {
		super();
	}
	
	public UnsupportedExtensionFunctionException(String msg) {
		super(msg);
	}
}
