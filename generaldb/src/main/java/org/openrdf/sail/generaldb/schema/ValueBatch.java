package org.openrdf.sail.generaldb.schema;


import org.openrdf.sail.rdbms.schema.RdbmsTable;

import java.sql.SQLException;


public class ValueBatch extends Batch {

	public static int total_rows;

	public static int total_st;

	private RdbmsTable table;

	public void setTable(RdbmsTable table) {
		assert table != null;
		this.table = table;
	}

	public synchronized int flush()
		throws SQLException
	{
		synchronized (table) {
			int count = super.flush();
			total_rows += count;
			total_st += 2;
			table.modified(count, 0);
			return count;
		}
	}



}

