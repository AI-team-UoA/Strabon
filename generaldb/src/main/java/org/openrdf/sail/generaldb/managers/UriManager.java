/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.generaldb.managers;

import java.sql.SQLException;

import org.openrdf.generaldb.managers.base.ValueManagerBase;
import org.openrdf.sail.rdbms.model.RdbmsURI;
import org.openrdf.sail.generaldb.schema.URITable;

/**
 * Manages URIs. Including creating, inserting, and looking up their IDs.
 * 
 * @author James Leigh
 * 
 */
public class UriManager extends ValueManagerBase<RdbmsURI> {

	public static UriManager instance;

	private URITable table;

	public UriManager() {
		instance = this;
	}

	public void setUriTable(URITable shorter) {
		this.table = shorter;
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
	protected int getBatchSize() {
		return table.getBatchSize();
	}

	@Override
	protected String key(RdbmsURI value) {
		return value.stringValue();
	}

	@Override
	protected void insert(Number id, RdbmsURI resource)
		throws SQLException, InterruptedException
	{
		String uri = resource.stringValue();
		if (getIdSequence().isLong(id)) {
			table.insertLong(id, uri);
		}
		else {
			table.insertShort(id, uri);
		}
	}

	@Override
	protected void optimize()
		throws SQLException
	{
		super.optimize();
		table.optimize();
	}

}
