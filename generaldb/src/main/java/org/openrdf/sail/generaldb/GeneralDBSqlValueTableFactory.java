/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.generaldb;

import java.sql.Connection;

import org.openrdf.sail.generaldb.schema.TripleTable;
import org.openrdf.sail.generaldb.schema.ValueTableFactory;

/**
 * Overrides PostgreSQL specific table commands.
 * 
 * @author James Leigh
 * 
 */
public abstract class GeneralDBSqlValueTableFactory extends ValueTableFactory {

	public GeneralDBSqlValueTableFactory(GeneralDBSqlTableFactory sqlTableFactory) {
		super(sqlTableFactory);
	}

	@Override
	public GeneralDBSqlValueTable newValueTable() {
		return new GeneralDBSqlValueTable();
	}

	@Override
	public TripleTable createTripleTable(Connection conn, String tableName) {
		return super.createTripleTable(conn, tableName.toLowerCase());
	}
}
