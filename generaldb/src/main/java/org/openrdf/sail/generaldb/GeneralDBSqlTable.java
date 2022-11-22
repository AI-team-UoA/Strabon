/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.generaldb;

import java.sql.SQLException;

import org.openrdf.sail.rdbms.schema.RdbmsTable;

/**
 * Converts table names to lower-case and include the analyse optimisation.
 * 
 * @author George Garbis <ggarbis@di.uoa.gr>
 * @author Manos Karpathiotakis <mk@di.uoa.gr>
 */
public abstract class GeneralDBSqlTable extends RdbmsTable {
	
	public GeneralDBSqlTable(String name) {
		super(name.toLowerCase());
	}

	@Override
	protected String buildLongIndex(String... columns) {
		// TODO How can we index text columns?
		return null;
	}

	@Override
	protected abstract String buildOptimize()
		throws SQLException;

	@Override
	protected abstract String buildClear();

	public abstract String buildGeometryCollumn();
	
	public abstract String buildIndexOnGeometryCollumn();

	public abstract String buildInsertGeometryValue();
	
	public abstract String buildInsertValue(String type);
	
	@Override
	protected abstract String buildCreateTemporaryTable(CharSequence columns) ;
	
	public abstract String buildDummyFromAndWhere(String fromDummy);
	
	public abstract String buildDynamicParameterInteger();
	
	public abstract String buildWhere();
}
