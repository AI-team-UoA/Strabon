/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.earthobservatory.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * This enumeration type represents the available DB Engines for the Strabon
 * Runtime system
 *
 * @author Ioannidis Theofilos <tioannid@yahoo.com>
 * @date   24/10/2017
 */
public enum StrabonDBEngine {

    /**
     * PostGIS
     */
    PostGIS("postgis", 5432),
    /**
     * MonetDB
     */
    MonetDB("monetdb", 27017);

    /**
     * Map a string constant to a StrabonDBEngine
     */
    private static final Map<String, StrabonDBEngine> validMap = new HashMap<String, StrabonDBEngine>();

    /* initialize map from constant name to enum constant
    ** but exclude the INVALID constant
    */
    static { 
        for (StrabonDBEngine dbEngine : values()) {
            validMap.put(dbEngine.getName(), dbEngine);
        }
    }

    // -------- Data Members ----------
    private final String name;
    private final int defaultPort;

    // -------- Constructor ----------
    /**
     * StrabonDBEngine constructor.
     *
     * @param name
     * @param defaultPort
     */
    StrabonDBEngine(String name, int defaultPort) {
        this.name = name;
        this.defaultPort = defaultPort;
    }
    
    // -------- Accessors ----------
    public String getName() {
        return name;
    }

    public int getDefaultPort() {    
        return defaultPort;
    }

    // -------- Methods ----------
    @Override
    public String toString() {
        return name;
    }

    /**
     * Returns a StrabonDBEngine enum given a format string.
     *
     * @param name
     * @return
     */
    public static StrabonDBEngine getStrabonDBEngine(String name) {
        return validMap.get(name.toLowerCase());
    }

}
