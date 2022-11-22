/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.generaldb.schema;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.BlockingQueue;

import org.openrdf.sail.generaldb.GeneralDBSqlTable;
import org.openrdf.sail.helpers.DefaultSailChangedEvent;
import org.openrdf.sail.rdbms.schema.RdbmsTable;

/**
 * Manages a temporary table used when uploading new statements with the same
 * predicate into the database.
 * 
 * @author James Leigh
 * 
 */
public class TransactionTable {

	private int batchSize;

	private TripleTable triples;

	private int addedCount;

	private int removedCount;

	private RdbmsTable temporary;

	private Connection conn;

	private TripleBatch batch;

	private BlockingQueue<Batch> queue;

	private DefaultSailChangedEvent sailChangedEvent;

	private IdSequence ids;

	private PreparedStatement insertSelect;

	public void setIdSequence(IdSequence ids) {
		this.ids = ids;
	}

	public void setQueue(BlockingQueue<Batch> queue) {
		this.queue = queue;
	}

	public void setTemporaryTable(RdbmsTable table) {
		this.temporary = table;
	}

	public void setConnection(Connection conn) {
		this.conn = conn;
	}

	public TripleTable getTripleTable() {
		return triples;
	}

	public void setTripleTable(TripleTable statements) {
		this.triples = statements;
	}

	public void setSailChangedEvent(DefaultSailChangedEvent sailChangedEvent) {
		this.sailChangedEvent = sailChangedEvent;
	}

	public int getBatchSize() {
		return batchSize;
	}

	public void setBatchSize(int size) {
		this.batchSize = size;
	}

	public void close()
		throws SQLException
	{
		if (insertSelect != null) {
			insertSelect.close();
		}
		temporary.close();
	}

	public synchronized void insert(Number ctx, Number subj, Number pred, Number obj)//,Timestamp intervalStart,Timestamp intervalEnd)
		throws SQLException, InterruptedException
	{
		if (batch == null || batch.isFull() || !queue.remove(batch)) {
			batch = newTripleBatch();
			batch.setTable(triples);
			batch.setSailChangedEvent(sailChangedEvent);
			batch.setTemporary(temporary);
			batch.setMaxBatchSize(getBatchSize());
			batch.setBatchStatement(prepareInsert());
			if (insertSelect == null) {
				insertSelect = prepareInsertSelect(buildInsertSelect());
			}
			batch.setInsertStatement(insertSelect);
		}
		batch.setObject(1, ctx);
		batch.setObject(2, subj);
		if (temporary == null && !triples.isPredColumnPresent()) {
			batch.setObject(3, obj);
			batch.setObject(4, true); // TODO explicit
			//FIXME
			//batch.setObject(5, intervalStart);
			//batch.setObject(6, intervalEnd);
		}
		else {
			batch.setObject(3, pred);
			batch.setObject(4, obj);
			batch.setObject(5, true); // TODO explicit
			//FIXME
			//batch.setObject(6, intervalStart);
			//batch.setObject(7, intervalEnd);
		}
		batch.addBatch();
		queue.put(batch);
		addedCount++;
		triples.getSubjTypes().add(ids.valueOf(subj));
		triples.getObjTypes().add(ids.valueOf(obj));
	}

	public void committed()
		throws SQLException
	{
		triples.modified(addedCount, removedCount);
		addedCount = 0;
		removedCount = 0;
	}

	public void removed(int count)
		throws SQLException
	{
		removedCount += count;
	}

	public boolean isEmpty()
		throws SQLException
	{
		return triples.isEmpty() && addedCount == 0;
	}

	@Override
	public String toString() {
		return triples.toString();
	}

	protected TripleBatch newTripleBatch() {
		return new TripleBatch();
	}

	protected PreparedStatement prepareInsertSelect(String sql)
		throws SQLException
	{
		return conn.prepareStatement(sql);
	}
	// george
	protected String buildInsertSelect()
		throws SQLException
	{		
		//Lowest Level Possible
		String tableName = triples.getName();
		StringBuilder sb = new StringBuilder();
		StringBuilder columns = new StringBuilder();
		sb.append("INSERT INTO ").append(tableName).append(" "); //sb.append("INSERT INTO ").append(tableName).append("\n");
		columns.append(" ctx, subj, "); //sb.append("SELECT DISTINCT ctx, subj, ");
		if (triples.isPredColumnPresent()) {
			columns.append("pred, ");//sb.append("pred, ");
		}
		
		//FIXME change due to need to accommodate temporals
//		sb.append("obj, expl, interval_start,interval_end FROM ");
		columns.append("obj, expl ");//sb.append("obj, expl FROM ");
		
		sb.append("(");
		sb.append(columns.toString());
		sb.append(") ");
		sb.append("SELECT DISTINCT ");
		sb.append(columns.toString());
		sb.append("FROM ");
		sb.append(temporary.getName()).append(" tr\n");
		sb.append("WHERE NOT EXISTS (");
		sb.append("SELECT * FROM ");
		sb.append(tableName).append(" st\n");
		sb.append("WHERE st.ctx = tr.ctx");
		sb.append(" AND st.subj = tr.subj");
		if (triples.isPredColumnPresent()) {
			sb.append(" AND st.pred = tr.pred");
		}
		sb.append(" AND st.obj = tr.obj");
		sb.append(" AND st.expl = tr.expl");
		//FIXME added to include support for TEMPORALS
//		sb.append(" AND ((st.interval_start = tr.interval_start");
//		sb.append(" AND st.interval_end = tr.interval_end)");
//		sb.append(" OR (tr.interval_end IS NULL");
//		sb.append(" AND tr.interval_start IS NULL))");

		/** REMOVED DURING OPTIMIZATION
		 * sb.append(" AND (st.interval_start = tr.interval_start OR tr.interval_start IS NULL)");
		 * sb.append(" AND (st.interval_end = tr.interval_end OR tr.interval_end IS NULL)");
		 */
		

		
		
//		sb.append(" OR (tr.interval_end IS NULL");
//		sb.append(" AND tr.interval_start IS NULL))");
		//Trying to cope with duplicate values
		
		sb.append(")");
//		sb.append(" AND tr.interval_end IS NOT NULL");
//		sb.append(" AND tr.interval_start IS NOT NULL");
		//System.out.println("BUILD INSERT SELECT \n"+sb.toString());
		return sb.toString();
	}

	protected PreparedStatement prepareInsert(String sql)
		throws SQLException
	{
		return conn.prepareStatement(sql);
	}

	protected String buildInsert(String tableName, boolean predColumnPresent)
		throws SQLException
	{
		GeneralDBSqlTable temp = (GeneralDBSqlTable)temporary;
		String dynVarInt = temp.buildDynamicParameterInteger();
		
		StringBuilder sb = new StringBuilder();
		sb.append("INSERT INTO ").append(tableName);
		sb.append(" (ctx, subj, ");
		if (predColumnPresent) {
			sb.append("pred, ");
		}
		//FIXME edit to accommodate temporal
		sb.append("obj, expl)\n");
//		sb.append("obj, expl,interval_start,interval_end)\n");
		 
		sb.append("VALUES ( "+dynVarInt+", "+dynVarInt+", ");
		if (predColumnPresent) {
			sb.append(dynVarInt+", ");
		}
		sb.append("?, ?)");
//		sb.append("?, ?, ?, ?)");
		//System.out.println("BUILD INSERT \n"+sb.toString());
		return sb.toString();
	}

	protected boolean isPredColumnPresent() {
		return triples.isPredColumnPresent();
	}

	private PreparedStatement prepareInsert()
		throws SQLException
	{
		if (temporary == null) {
			boolean present = triples.isPredColumnPresent();
			String sql = buildInsert(triples.getName(), present);
			return prepareInsert(sql);
		}
		String sql = buildInsert(temporary.getName(), true);
		return prepareInsert(sql);
	}

}
