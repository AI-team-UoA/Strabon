/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.generaldb.schema;

import java.sql.SQLException;

import org.openrdf.sail.helpers.DefaultSailChangedEvent;

/**
 * 
 * @author James Leigh
 */
public class TripleBatch extends Batch {

	public static int total_rows;

	public static int total_st;

	public static int total_wait;

	private TripleTable table;

	private DefaultSailChangedEvent sailChangedEvent;

	public void setTable(TripleTable table) {
		assert table != null;
		this.table = table;
	}

	public void setSailChangedEvent(DefaultSailChangedEvent sailChangedEvent) {
		this.sailChangedEvent = sailChangedEvent;
	}

	public boolean isReady() {
		return table.isReady();
	}

	public synchronized int flush()
		throws SQLException
	{
		table.blockUntilReady();
		long start = System.currentTimeMillis();
		int count = super.flush();
		long end = System.currentTimeMillis();
		total_rows += count;
		total_st += 2;
		total_wait += end - start;
		if (count > 0) {
			sailChangedEvent.setStatementsAdded(true);
		}
		return count;
	}

}
