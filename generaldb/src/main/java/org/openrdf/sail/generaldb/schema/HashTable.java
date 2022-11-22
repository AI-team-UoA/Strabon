/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.generaldb.schema;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;
import java.util.Map;

/**
 * 
 * @author James Leigh
 */
public class HashTable {

	private static final int CHUNK_SIZE = 15;

	private ValueTable table;

	private PreparedStatement select;

	private int removedStatementsSinceExpunge;

	public HashTable(ValueTable table) {
		super();
		this.table = table;
	}

	public String getName() {
		return table.getName();
	}

	public int getBatchSize() {
		return table.getBatchSize();
	}

	public int getSelectChunkSize() {
		return CHUNK_SIZE;
	}

	public void init()
		throws SQLException
	{
	}

	public void close()
		throws SQLException
	{
		if (select != null) {
			select.close();
		}
		table.close();
	}

	public List<Long> maxIds(int shift, int mod)
		throws SQLException
	{
		return table.maxIds(shift, mod);
	}

	public void insert(Number id, long hash)
		throws SQLException, InterruptedException
	{
		synchronized (table) {
			HashBatch batch = (HashBatch)table.getValueBatch();
			if (table.isExpired(batch)) {
				batch = newHashBatch();
				table.initBatch(batch);
			}
			batch.addBatch(id, hash);
			table.queue(batch);
		}
	}

	public boolean expungeRemovedStatements(int count, String condition)
		throws SQLException
	{
		removedStatementsSinceExpunge += count;
		if (condition != null && timeToExpunge()) {
			boolean removed = table.expunge(condition);
			removedStatementsSinceExpunge = 0;
			return removed;
		}
		return false;
	}

	protected boolean timeToExpunge() {
		return removedStatementsSinceExpunge > table.size() / 4;
	}

	public void optimize()
		throws SQLException
	{
		table.optimize();
	}

	public String toString() {
		return table.toString();
	}

	public Map<Long, Number> load(Map<Long, Number> hashes)
		throws SQLException
	{
		assert !hashes.isEmpty();
		assert hashes.size() <= getSelectChunkSize();
		if (select == null) {
			StringBuilder sb = new StringBuilder();
			sb.append("SELECT id, value\nFROM ").append(getName());
			sb.append("\nWHERE value IN (");
			for (int i = 0, n = getSelectChunkSize(); i < n; i++) {
				sb.append("?,");
			}
			sb.setCharAt(sb.length() - 1, ')');
			select = prepareSelect(sb.toString());
		}
		int p = 0;
		for (Long hash : hashes.keySet()) {
			select.setLong(++p, hash);
		}
		while (p < getSelectChunkSize()) {
			select.setNull(++p, Types.BIGINT);
		}
		ResultSet rs = select.executeQuery();
		try {
			while (rs.next()) {
				long id = rs.getLong(1);
				long hash = rs.getLong(2);
				hashes.put(hash, id);
			}
		}
		finally {
			rs.close();
		}
		return hashes;
	}

	protected HashBatch newHashBatch() {
		return new HashBatch();
	}

	protected PreparedStatement prepareSelect(String sql)
		throws SQLException
	{
		return table.getRdbmsTable().prepareStatement(sql);
	}

}
