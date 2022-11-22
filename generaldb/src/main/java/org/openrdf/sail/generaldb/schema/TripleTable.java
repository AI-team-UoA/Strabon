/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.generaldb.schema;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.openrdf.sail.rdbms.schema.RdbmsTable;
import org.openrdf.sail.rdbms.schema.ValueTypes;
import org.openrdf.sail.rdbms.schema.ValueType;

/**
 * Manages the life-cycle of the rows in a single predicate table.
 * 
 * @author James Leigh
 * 
 */
public class TripleTable {

	public static int tables_created;

	public static int total_st;

	public static final boolean UNIQUE_INDEX_TRIPLES = true;

	private static final String[] PKEY = { "obj", "subj", "ctx", "expl" };

	private static final String[] SUBJ_INDEX = { "subj" };

	private static final String[] CTX_INDEX = { "ctx" };

	private static final String[] PRED_PKEY = { "obj", "subj", "pred", "ctx", "expl" };

	private static final String[] PRED_INDEX = { "pred" };

	private static final String[] EXPL_INDEX = { "expl" };

	private RdbmsTable table;

	private ValueTypes objTypes = new ValueTypes();

	private ValueTypes subjTypes = new ValueTypes();

	private boolean initialize;

	private boolean predColumnPresent;

	private boolean indexed;

	private IdSequence ids;

	public TripleTable(RdbmsTable table) {
		this.table = table;
	}

	public void setIdSequence(IdSequence ids) {
		this.ids = ids;
	}

	public boolean isPredColumnPresent() {
		return predColumnPresent;
	}

	public void setPredColumnPresent(boolean present) {
		predColumnPresent = present;
	}

	public void setIndexed(boolean indexingTriples) {
		indexed = true;
	}

	public synchronized void initTable()
		throws SQLException
	{
		if (initialize)
			return;
		table.createTransactionalTable(buildTableColumns());
		tables_created++;
		total_st++;
		if (UNIQUE_INDEX_TRIPLES) {
			if (isPredColumnPresent()) {
				table.primaryIndex(PRED_PKEY);
				total_st++;
			}
			else {
				table.primaryIndex(PKEY);
				total_st++;
			}
		}
		if (indexed) {
			createIndex();
		}
		initialize = true;
	}

	public void reload()
		throws SQLException
	{
		table.count();
		if (table.size() > 0) {
			ValueType[] values = ValueType.values();
			String[] OBJ_CONTAINS = new String[values.length];
			String[] SUBJ_CONTAINS = new String[values.length];
			StringBuilder sb = new StringBuilder();
			for (int i = 0, n = values.length; i < n; i++) {
				sb.delete(0, sb.length());
				ValueType code = values[i];
				sb.append("MAX(CASE WHEN obj BETWEEN ").append(ids.minId(code));
				sb.append(" AND ").append(ids.maxId(code));
				sb.append(" THEN 1 ELSE 0 END)");
				OBJ_CONTAINS[i] = sb.toString();
				sb.delete(0, sb.length());
				sb.append("MAX(CASE WHEN subj BETWEEN ").append(ids.minId(code));
				sb.append(" AND ").append(ids.maxId(code));
				sb.append(" THEN 1 ELSE 0 END)");
				SUBJ_CONTAINS[i] = sb.toString();
			}
			int[] aggregate = table.aggregate(OBJ_CONTAINS);
			for (int i = 0; i < aggregate.length; i++) {
				if (aggregate[i] == 1) {
					objTypes.add(values[i]);
				}
			}
			aggregate = table.aggregate(SUBJ_CONTAINS);
			for (int i = 0; i < aggregate.length; i++) {
				if (aggregate[i] == 1) {
					subjTypes.add(values[i]);
				}
			}

		}
		initialize = true;
	}

	public void close()
		throws SQLException
	{
		table.close();
	}

	public boolean isIndexed()
		throws SQLException
	{
		return table.getIndexes().size() > 1;
	}

	public void createIndex()
		throws SQLException
	{
//		if (isPredColumnPresent()) {
//			table.index(PRED_INDEX);
//			total_st++;
//		}
//		table.index(SUBJ_INDEX);
//		total_st++;
//		table.index(CTX_INDEX);
//		total_st++;
//		table.index(EXPL_INDEX);
//		total_st++;
		
		///////
		if (isPredColumnPresent()) {
			String[] PRED_OBJ_SUBJ_INDEX = {"pred", "obj", "subj"};
			table.index(PRED_OBJ_SUBJ_INDEX);
			total_st++;
			String[] PRED_SUBJ_OBJ_INDEX = {"pred", "subj", "obj"};
			table.index(PRED_SUBJ_OBJ_INDEX);
			total_st++;
			String[] OBJ_PRED_SUBJ_INDEX = { "obj", "pred", "subj"};
			table.index(OBJ_PRED_SUBJ_INDEX);
			total_st++;
			String[] OBJ_SUBJ_PRED_INDEX = { "obj", "subj", "pred"};
			table.index(OBJ_SUBJ_PRED_INDEX);
			total_st++;
			String[] SUBJ_OBJ_PRED_INDEX = {"subj", "obj", "pred"};
			table.index(SUBJ_OBJ_PRED_INDEX);
			total_st++;
			String[] SUBJ_PRED_OBJ_INDEX = {"subj", "pred", "obj"};
			table.index(SUBJ_PRED_OBJ_INDEX);
			total_st++;
		} else {
//			String[] OBJ_SUBJ_INDEX = {"obj", "subj"};
//			table.index(OBJ_SUBJ_INDEX);
//			total_st++;
			String[] SUBJ_OBJ_INDEX = {"subj", "obj"};
			table.index(SUBJ_OBJ_INDEX);
			total_st++;
		}
		//////
		
	}

	public void dropIndex()
		throws SQLException
	{
		for (Map.Entry<String, List<String>> e : table.getIndexes().entrySet()) {
			if (!e.getValue().contains("OBJ") && !e.getValue().contains("obj")) {
				table.dropIndex(e.getKey());
			}
		}
	}

	public boolean isReady() {
		return initialize;
	}

	public void blockUntilReady()
		throws SQLException
	{
		if (initialize)
			return;
		initTable();
	}

	public String getName()
		throws SQLException
	{
		return table.getName();
	}

	public String getNameWhenReady()
		throws SQLException
	{
		blockUntilReady();
		return table.getName();
	}

	public ValueTypes getObjTypes() {
		return objTypes;
	}

	public void setObjTypes(ValueTypes valueTypes) {
		this.objTypes.merge(valueTypes);
	}

	public ValueTypes getSubjTypes() {
		return subjTypes;
	}

	public void setSubjTypes(ValueTypes valueTypes) {
		this.subjTypes.merge(valueTypes);
	}

	public void modified(int addedCount, int removedCount)
		throws SQLException
	{
		blockUntilReady();
		table.modified(addedCount, removedCount);
		table.optimize();
		if (isEmpty()) {
			objTypes.reset();
			subjTypes.reset();
		}
	}

	public boolean isEmpty()
		throws SQLException
	{
		blockUntilReady();
		return table.size() == 0;
	}

	@Override
	public String toString() {
		return table.getName();
	}

	public void drop()
		throws SQLException
	{
		blockUntilReady();
		table.drop();
	}

	protected CharSequence buildTableColumns() {
		StringBuilder sb = new StringBuilder();
		sb.append("  ctx ").append(ids.getSqlType()).append(" NOT NULL,\n");
		sb.append("  subj ").append(ids.getSqlType()).append(" NOT NULL,\n");
		if (isPredColumnPresent()) {
			sb.append("  pred ").append(ids.getSqlType()).append(" NOT NULL,\n");
		}
		sb.append("  obj ").append(ids.getSqlType()).append(" NOT NULL,\n");
//		sb.append("  expl ").append("BOOL").append(" NOT NULL,\n");
		sb.append("  expl ").append("BOOL").append(" NOT NULL\n");
		//FIXME
//		sb.append("  interval_start ").append("TIMESTAMP DEFAULT NULL").append(",\n");
//		sb.append("  interval_end ").append("TIMESTAMP DEFAULT NULL").append("\n");
		
		return sb;
	}
}
