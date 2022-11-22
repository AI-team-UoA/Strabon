/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.postgis;

import java.sql.SQLException;

import org.openrdf.sail.generaldb.GeneralDBSqlTable;

/**
 * Converts table names to lower-case and include the analyse optimisation.
 * 
 * @author Charalampos Nikolaou <charnik@di.uoa.gr>
 * @author Manos Karpathiotakis <mk@di.uoa.gr>
 * 
 */
public class PostGISSqlTable extends GeneralDBSqlTable {

	/**
	 * This value should not be changed whatever the semantics of Strabon
	 * is for EPSG:4326. This number is hardcoded in PostGIS's SQL statements
	 * that define several of their ST_ methods. 
	 */
	public static final int DEFAULT_SRID = 4326;
	
	public PostGISSqlTable(String name) {
		super(name.toLowerCase());
	}

	@Override
	protected String buildOptimize()
		throws SQLException
	{
		return "VACUUM ANALYZE " + getName();
	}

	@Override
	protected String buildClear() {
		return "TRUNCATE " + getName();
	}
	
	@Override
	public String buildGeometryCollumn() {
		return "SELECT AddGeometryColumn('', 'geo_values', 'strdfgeo', " + DEFAULT_SRID + ", 'GEOMETRY', 2)";
	}
	
	@Override
	public String buildIndexOnGeometryCollumn() {
		return "CREATE INDEX geoindex ON geo_values USING GIST (strdfgeo)";
	}
	
	/**
	 * SQL arguments
	 * 	arg1: hash
	 * 	arg2: geometry (binary)
	 * 	arg3: SRID of the given geometry (used to transform it to PostGIS' 4326 long/lat CRS)
	 * 	arg4: SRID of the given geometry to save to the database
	 */
	@Override
	public String buildInsertGeometryValue() {
		return " (id, strdfgeo, srid) VALUES (?, ST_Transform(ST_GeomFromWKB(?, ?),"+DEFAULT_SRID+"), ?)";
	}
	
	@Override
	public String buildInsertValue(String type) {
		return " (id, value) VALUES (?, ?) ";
	}
	
	@Override
	protected String buildCreateTemporaryTable(CharSequence columns) {
		StringBuilder sb = new StringBuilder();
		sb.append("CREATE TEMPORARY TABLE ").append(getName());
		sb.append(" (\n").append(columns).append(")");
		return sb.toString();
	}
	
	@Override
	public String buildDummyFromAndWhere(String fromDummy) {
		StringBuilder sb = new StringBuilder(256);
		sb.append(fromDummy); 
		sb.append("\nWHERE 1=0");
		return sb.toString();
	}
	
	@Override
	public String buildDynamicParameterInteger() {
			return "?";
	}
	
	@Override
	public String buildWhere() {
		return " WHERE (1=1) ";
	}
}