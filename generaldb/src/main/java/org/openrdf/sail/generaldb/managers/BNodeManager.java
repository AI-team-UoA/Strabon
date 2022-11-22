/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.generaldb.managers;

import java.sql.SQLException;

import org.openrdf.generaldb.managers.base.ValueManagerBase;
import org.openrdf.sail.rdbms.model.RdbmsBNode;
import org.openrdf.sail.generaldb.schema.BNodeTable;

/**
 * Manages BNodes. Including creating, inserting, and looking up their IDs.
 * 
 * @author James Leigh
 * 
 */
public class BNodeManager extends ValueManagerBase<RdbmsBNode> {

	public static BNodeManager instance;

	private BNodeTable table;

	public BNodeManager() {
		instance = this;
	}

	public void setTable(BNodeTable table) {
		this.table = table;
	}

	@Override
	public void close()
		throws SQLException
	{
		super.close();
		if (table != null) {
			table.close();
		}
	}

	@Override
	protected boolean expunge(String condition)
		throws SQLException
	{
		return table.expunge(condition);
	}

	@Override
	protected void optimize()
		throws SQLException
	{
		super.optimize();
		table.optimize();
	}

	@Override
	protected int getBatchSize() {
		return table.getBatchSize();
	}

	@Override
	protected String key(RdbmsBNode value) {
		return value.stringValue();
	}

	@Override
	protected void insert(Number id, RdbmsBNode resource)
		throws SQLException, InterruptedException
	{
		table.insert(id, resource.stringValue());
	}

}
