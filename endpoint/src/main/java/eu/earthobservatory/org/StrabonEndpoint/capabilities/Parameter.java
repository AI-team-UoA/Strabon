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

import java.util.HashSet;
import java.util.Set;

/**
 * @author Charalampos Nikolaou <charnik@di.uoa.gr>
 *
 */
public class Parameter {

	private String name;
	private String value;
	
	private Set<String> acceptedValues;
	
	public Parameter(String name, String value) {
		this.name = name;
		this.value = value;
		
		this.acceptedValues = new HashSet<String>();
	}
	
	public String getName() {
		return name;
	}
	
	public String getValue() {
		return value;
	}
	
	public void addAcceptedValue(String value) {
		acceptedValues.add(value);
	}
	
	public Set<String> getAcceptedValues() {
		return acceptedValues;
	}
}
