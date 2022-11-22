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

import java.util.List;

/**
 * Interface that exposes the kind of requests that clients
 * of Strabon Endpoint can access its services, i.e.,
 * Query, Store, Update, Browse, etc.   
 * 
 * @author Charalampos Nikolaou <charnik@di.uoa.gr>
 */
public interface RequestCapabilities {
	
	/**
	 * Get the accepted parameters of a service request
	 * as a list.
	 * 
	 * @return
	 */
	public Parameters getParametersObject();
	
	/**
	 * Get the accepted values of a service parameter.
	 * 
	 * @param param
	 * @return
	 */
	public List<String> getAcceptedValues(String param);
	
	/**
	 * Determine wether the given parameter is optional
	 * and thus can be omitted.
	 * 
	 * @param param
	 * @return
	 */
	public boolean isOptional(String param);
}
