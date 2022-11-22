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

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.earthobservatory.constants.GeoConstants;
import eu.earthobservatory.constants.OGCConstants;


/**
 * This class implements the {@link Capabilities} interface and
 * shall be used only for versions of Strabon Endpoint newer than
 * version 3.2.4.
 * 
 * @author Charalampos Nikolaou <charnik@di.uoa.gr>
 */
public class EndpointCapabilities implements Capabilities {

	private static Logger logger = LoggerFactory.getLogger(eu.earthobservatory.org.StrabonEndpoint.capabilities.EndpointCapabilities.class);
	
	private static final String VERSION_PROPERTIES_FILE = "/version.properties";
	private static final Properties PROPERTIES = new Properties();

	private static String VERSION;
	
	// load the properties file to get the version
	static {
		InputStream vin = Capabilities.class.getResourceAsStream(VERSION_PROPERTIES_FILE);
		if (vin != null) {
			try {
				PROPERTIES.load(vin);
				vin.close();
				
			} catch (IOException e) {
				logger.error("[StrabonEndpoint.EndpointCapabilities] Error during reading of {} file.", VERSION_PROPERTIES_FILE, e);
			}
		} else {
			logger.warn("[StrabonEndpoint.EndpointCapabilities] Could not read version file.");
		}
		
		VERSION = PROPERTIES.getProperty("version");
	}
	
	@Override
	public String getVersion() {
		return VERSION;
	}
	
	@Override
	public boolean supportsLimit() {
		return true;
	}

	@Override
	public boolean supportsAuthentication() {
		return true;
	}

	@Override
	public boolean supportsConnectionModification() {
		return true;
	}
	
	@Override
	public boolean supportsQuerying() {
		return true;
	}

	@Override
	public boolean supportsUpdating() {
		return true;
	}

	@Override
	public boolean supportsStoring() {
		return true;
	}

	@Override
	public boolean supportsDescribing() {
		return true;
	}

	@Override
	public boolean supportsBrowsing() {
		return true;
	}

	@Override
	public RequestCapabilities getQueryCapabilities() {
		return QueryBeanCapabilities.getInstance();
	}

	@Override
	public RequestCapabilities getUpdateCapabilities() {
		return UpdateBeanCapabilities.getInstance();
	}

	@Override
	public RequestCapabilities getStoreCapabilities() {
		return StoreBeanCapabilities.getInstance();
	}

	@Override
	public RequestCapabilities getBrowseCapabilities() {
		return BrowseBeanCapabilities.getInstance();
	}

	@Override
	public RequestCapabilities getConnectionCapabilities() {
		return ConnectionBeanCapabilities.getInstance();
	}

	/* (non-Javadoc)
	 * @see eu.earthobservatory.org.StrabonEndpoint.capabilities.Capabilities#getstSPARQLSpatialExtensionFunctions()
	 */
	@Override
	public List<String> getstSPARQLSpatialExtensionFunctions() {
		return GeoConstants.STSPARQLSpatialExtFunc;
	}

	/* (non-Javadoc)
	 * @see eu.earthobservatory.org.StrabonEndpoint.capabilities.Capabilities#getGeoSPARQLSpatialExtensionFunctions()
	 */
	@Override
	public List<String> getGeoSPARQLSpatialExtensionFunctions() {
		return GeoConstants.GEOSPARQLExtFunc;
	}

	@Override
	public List<String> getUnitsOfMeasure() {
		return OGCConstants.supportedUnitsOfMeasure;
	}
}
