/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * 
 * Copyright (C) 2012, Pyravlos Team
 * 
 * http://www.strabon.di.uoa.gr/
 */
package eu.earthobservatory.org.StrabonEndpoint.capabilities;

import java.util.Collection;
import java.util.Hashtable;

/**
 * @author Charalampos Nikolaou <charnik@di.uoa.gr>
 *
 */
public class Parameters {

	private Hashtable<String, Parameter> params;
	
	public Parameters() {
		params = new Hashtable<String, Parameter>();
	}
	
	/**
	 * Adds a new parameter. If the parameter already exists,
	 * the operation does nothing.
	 * 
	 * @param param
	 */
	public void addParameter(Parameter param) {
		if (params.get(param.getName()) == null) {
			params.put(param.getName(), param);
		}
	}
	
	public Parameter getParameter(String name) {
		return params.get(name);
	}
	
	public Collection<Parameter> getParameters() {
		return params.values();
	}
}
