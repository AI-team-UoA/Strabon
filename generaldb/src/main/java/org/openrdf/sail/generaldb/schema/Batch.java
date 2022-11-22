
package org.openrdf.sail.generaldb.schema;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.openrdf.sail.rdbms.schema.RdbmsTable;



public class Batch {

	public static Batch CLOSED_SIGNAL = new Batch();

	private RdbmsTable temporary;

	private PreparedStatement insertBatch;

	private PreparedStatement insertSelect;

	private int maxBatchSize;

	private int batchCount;

	public int getMaxBatchSize() {
		return maxBatchSize;
	}

	public void setMaxBatchSize(int batchSize) {
		this.maxBatchSize = batchSize;
	}

	public void setTemporary(RdbmsTable temporary) {
		assert temporary != null;
		this.temporary = temporary;
	}

	public void setBatchStatement(PreparedStatement insert) {
		assert insert != null;
		this.insertBatch = insert;
	}

	public void setInsertStatement(PreparedStatement insert) {
		assert insert != null;
		this.insertSelect = insert;
	}

	public int size() {
		return batchCount;
	}

	public boolean isFull() {
		return batchCount >= getMaxBatchSize();
	}

	public boolean isReady() {
		return true;
	}

	public void setObject(int parameterIndex, Object x)
		throws SQLException
	{
			insertBatch.setObject(parameterIndex, x);
	}

	public void setString(int parameterIndex, String x)
		throws SQLException
	{
		insertBatch.setString(parameterIndex, x);
	}
	
	public void setBytes(int parameterIndex, byte[] x)
		throws SQLException
	{
//		byte[] part1 = "ST_GeomFromWKB(".getBytes();
//		byte[] part3 = ")".getBytes();
//		byte[] all = ArrayUtils.addAll(ArrayUtils.addAll(part1,x),part3);
		insertBatch.setBytes(parameterIndex, x);
	}
	
	public void addBatch()
		throws SQLException
	{
		insertBatch.addBatch();
		batchCount++;
	}

	/**
	 * 
	 * @return <code>-1</code> if already flushed
	 * @throws SQLException
	 */
	public int flush()
		throws SQLException
	{
		if (insertBatch == null)
			return -1;
		try {
			int count;
			if (temporary == null) {
				int[] results = insertBatch.executeBatch();
				count = results.length;
			}
			else {
				synchronized (temporary) {
					insertBatch.executeBatch();
					count = insertSelect.executeUpdate();
					temporary.clear();
				}
			}
			return count;
		}
		finally {
			insertBatch.close();
			insertBatch = null;
		}
	}

}
