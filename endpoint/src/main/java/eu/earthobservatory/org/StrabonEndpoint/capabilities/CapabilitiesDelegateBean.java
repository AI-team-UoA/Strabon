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


/**
 * 
 * @author Charalampos Nikolaou <charnik@di.uoa.gr>
 */
public class CapabilitiesDelegateBean implements org.springframework.beans.factory.DisposableBean {

	private static Capabilities caps;
	
	public CapabilitiesDelegateBean(boolean autoDiscoverCapabilities) {
		if (autoDiscoverCapabilities) {
			caps = new AutoDiscoveryCapabilities();
			
		} else {
			caps = new EndpointCapabilities();
		}
	}
	
	@Override
	public void destroy() throws Exception {
		// nothing to destroy
	}
	
	public Capabilities getEndpointCapabilities() {
		return caps;
	}
}
