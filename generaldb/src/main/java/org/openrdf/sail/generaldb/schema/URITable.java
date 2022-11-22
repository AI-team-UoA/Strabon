/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.generaldb.schema;

import java.sql.SQLException;

/**
 * Manages the rows in the URI table.
 * 
 * @author James Leigh
 * 
 */
public class URITable {

	private ValueTable shorter;

	private ValueTable longer;

	private int version;

	public URITable(ValueTable shorter, ValueTable longer) {
		super();
		this.shorter = shorter;
		this.longer = longer;
	}

	public void close()
		throws SQLException
	{
		shorter.close();
		longer.close();
	}

	public int getBatchSize() {
		return shorter.getBatchSize();
	}

	public int getVersion() {
		return version;
	}

	public String getShortTableName() {
		return shorter.getName();
	}

	public String getLongTableName() {
		return longer.getName();
	}

	public void insertShort(Number id, String value)
		throws SQLException, InterruptedException
	{
		shorter.insert(id, value);
	}

	public void insertLong(Number id, String value)
		throws SQLException, InterruptedException
	{
		longer.insert(id, value);
	}

	public boolean expunge(String condition)
		throws SQLException
	{
		boolean bool = false;
		bool |= shorter.expunge(condition);
		bool |= longer.expunge(condition);
		return bool;
	}

	@Override
	public String toString() {
		return shorter.getName() + " UNION ALL " + longer.getName();
	}

	public void optimize()
		throws SQLException
	{
		shorter.optimize();
		longer.optimize();
	}
}
