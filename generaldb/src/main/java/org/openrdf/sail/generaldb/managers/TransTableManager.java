/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.generaldb.managers;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

import org.openrdf.sail.generaldb.schema.Batch;
import org.openrdf.sail.generaldb.schema.IdSequence;
import org.openrdf.sail.generaldb.schema.TransactionTable;
import org.openrdf.sail.generaldb.schema.TripleTable;
import org.openrdf.sail.generaldb.schema.ValueTable;
import org.openrdf.sail.helpers.DefaultSailChangedEvent;
import org.openrdf.sail.rdbms.schema.RdbmsTable;
import org.openrdf.sail.rdbms.schema.TableFactory;
import org.openrdf.sail.rdbms.schema.ValueTypes;

/**
 * Manages and delegates to a collection of {@link TransactionTable}s.
 * 
 * @author James Leigh
 * 
 */
public class TransTableManager {

	public static int BATCH_SIZE = 8 * 1024;

	public static final boolean TEMPORARY_TABLE_USED = TripleTable.UNIQUE_INDEX_TRIPLES;

	private TableFactory factory;

	private TripleTableManager triples;

	private RdbmsTable temporaryTable;

	private Map<Number, TransactionTable> tables = new HashMap<Number, TransactionTable>();

	private int removedCount;

	private String fromDummy;

	private Connection conn;

	private BlockingQueue<Batch> batchQueue;

	private DefaultSailChangedEvent sailChangedEvent;

	private IdSequence ids;

	public void setConnection(Connection conn) {
		this.conn = conn;
	}

	public void setTemporaryTableFactory(TableFactory factory) {
		this.factory = factory;
	}

	public void setStatementsTable(TripleTableManager predicateTableManager) {
		this.triples = predicateTableManager;
	}

	public void setFromDummyTable(String fromDummy) {
		this.fromDummy = fromDummy;
	}

	public void setBatchQueue(BlockingQueue<Batch> queue) {
		this.batchQueue = queue;
	}

	public void setSailChangedEvent(DefaultSailChangedEvent sailChangedEvent) {
		this.sailChangedEvent = sailChangedEvent;
	}

	public void setIdSequence(IdSequence ids) {
		this.ids = ids;
	}

	public int getBatchSize() {
		return BATCH_SIZE;
	}

	public void initialize()
	throws SQLException
	{
	}

	//FIXME 2 last arguments used to accommodate need for temporal
	public void insert(Number ctx, Number subj, Number pred, Number obj)//,Timestamp intervalStart,Timestamp intervalEnd)
	throws SQLException, InterruptedException
	{
		getTable(pred).insert(ctx, subj, pred, obj);//,intervalStart,intervalEnd);
	}

	public void close()
	throws SQLException
	{
		try {
			if (temporaryTable != null) {
				temporaryTable.drop();
				temporaryTable.close();
			}
		}
		catch (SQLException e) {
			// ignore
		}
		for (TransactionTable table : tables.values()) {
			table.close();
		}
	}

	public String findTableName(Number pred)
	throws SQLException
	{
		return triples.findTableName(pred);
	}

	public String getCombinedTableName()
	throws SQLException
	{
		String union = " UNION ALL ";
		StringBuilder sb = new StringBuilder(1024);
		sb.append("(");
		for (Number pred : triples.getPredicateIds()) {
			TripleTable predicate;
			try {
				predicate = triples.getPredicateTable(pred);
			}
			catch (SQLException e) {
				throw new AssertionError(e);
			}
			TransactionTable table = findTable(pred);
			if ((table == null || table.isEmpty()) && predicate.isEmpty())
				continue;
			sb.append("SELECT ctx, subj, ");
			if (predicate.isPredColumnPresent()) {
				sb.append(" pred,");
			}
			else {
				sb.append(pred).append(" AS pred,");
			}
			sb.append(" obj");
			sb.append("\nFROM ");
			sb.append(predicate.getNameWhenReady());
			sb.append(union);
			predicate.blockUntilReady();
		}
		if (sb.length() < union.length())
			return getEmptyTableName();
		sb.delete(sb.length() - union.length(), sb.length());
		sb.append(")");
		return sb.toString();
	}

	public String getTableName(Number pred)
	throws SQLException
	{
		if (pred.equals(ValueTable.NIL_ID))
			return getCombinedTableName();
		String tableName = triples.getTableName(pred);
		if (tableName == null)
			return getEmptyTableName();
		return tableName;
	}

	public void committed(boolean locked)
	throws SQLException
	{
		synchronized (tables) {
			for (TransactionTable table : tables.values()) {
				table.committed();
			}
			tables.clear();
		}
		if (removedCount > 0) {
			triples.removed(removedCount, locked);
		}
	}

	public void removed(Number pred, int count)
	throws SQLException
	{
		getTable(pred).removed(count);
		removedCount += count;
	}

	public Collection<Number> getPredicateIds() {
		return triples.getPredicateIds();
	}

	public boolean isPredColumnPresent(Number id)
	throws SQLException
	{
		if (id.longValue() == ValueTable.NIL_ID)
			return true;
		return triples.getPredicateTable(id).isPredColumnPresent();
	}

	public ValueTypes getObjTypes(Number pred) {
		TripleTable table = triples.getExistingTable(pred);
		if (table == null)
			return ValueTypes.UNKNOWN;
		return table.getObjTypes();
	}

	public ValueTypes getSubjTypes(Number pred) {
		TripleTable table = triples.getExistingTable(pred);
		if (table == null)
			return ValueTypes.RESOURCE;
		return table.getSubjTypes();
	}

	public boolean isEmpty()
	throws SQLException
	{
		for (Number pred : triples.getPredicateIds()) {
			TripleTable predicate;
			try {
				predicate = triples.getPredicateTable(pred);
			}
			catch (SQLException e) {
				throw new AssertionError(e);
			}
			TransactionTable table = findTable(pred);
			if (table != null && !table.isEmpty() || !predicate.isEmpty())
				return false;
		}
		return true;
	}

	protected String getZeroBigInt() {
		return "0";
	}
	
	protected TransactionTable getTable(Number pred)
	throws SQLException
	{
		synchronized (tables) {
			TransactionTable table = tables.get(pred);
			if (table == null) {
				/**
				 * @author charnik
				 * @see method {@link TripleTableManager#flushManagers()} for details
				 */
				triples.flushManagers();
				
				TripleTable predicate = triples.getPredicateTable(pred);
				Number key = pred;
				if (predicate.isPredColumnPresent()) {
					key = ids.idOf(-1);
					table = tables.get(key);
					if (table != null)
						return table;
				}
				table = createTransactionTable(predicate);
				tables.put(key, table);
			}
			return table;
		}
	}

	protected TransactionTable createTransactionTable(TripleTable predicate)
	throws SQLException
	{
		if (temporaryTable == null && TEMPORARY_TABLE_USED) {
			temporaryTable = createTemporaryTable(conn);
			if (!temporaryTable.isCreated()) {
				createTemporaryTable(temporaryTable);
			}
		}
		TransactionTable table = createTransactionTable();
		table.setIdSequence(ids);
		table.setSailChangedEvent(sailChangedEvent);
		table.setQueue(batchQueue);
		table.setTripleTable(predicate);
		table.setTemporaryTable(temporaryTable);
		table.setConnection(conn);
		table.setBatchSize(getBatchSize());
		return table;
	}

	protected RdbmsTable createTemporaryTable(Connection conn) {
		return factory.createTemporaryTable(conn);
	}

	protected TransactionTable createTransactionTable() {
		return new TransactionTable();
	}

	protected void createTemporaryTable(RdbmsTable table)
	throws SQLException
	{
		String type = ids.getSqlType();
		StringBuilder sb = new StringBuilder();
		sb.append("  ctx ").append(type).append(" NOT NULL,\n");
		sb.append("  subj ").append(type).append(" NOT NULL,\n");
		sb.append("  pred ").append(type).append(" NOT NULL,\n");
		sb.append("  obj ").append(type).append(" NOT NULL,\n");
		sb.append("  expl ").append("BOOL").append(" NOT NULL\n");
		//FIXME
		//sb.append("  interval_start ").append("TIMESTAMP").append(",\n");
		//sb.append("  interval_end ").append("TIMESTAMP").append("\n");
		table.createTemporaryTable(sb);
	}

	private String getEmptyTableName() {
		StringBuilder sb = new StringBuilder(256);
		sb.append("(");
		sb.append("SELECT ");
		sb.append(getZeroBigInt()).append(" AS ctx, ");
		sb.append(getZeroBigInt()).append(" AS subj, ");
		sb.append(getZeroBigInt()).append(" AS pred, ");
		sb.append(getZeroBigInt()).append(" AS obj ");
		sb.append(fromDummy);
	    sb.append("\nWHERE 1=0");
		sb.append(")");
		return sb.toString();
	}

	private TransactionTable findTable(Number pred) {
		synchronized (tables) {
			return tables.get(pred);
		}
	}

	/**
	 * my addition
	 * @return the triple table manager 
	 */
	public TripleTableManager getTripleTableManager()
	{
		return triples;
	}
}
