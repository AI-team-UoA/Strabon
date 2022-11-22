/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.generaldb.schema;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author James Leigh
 */
public class HashBatch extends ValueBatch {

	private List<Long> hashes;

	@Override
	public void setMaxBatchSize(int batchSize) {
		super.setMaxBatchSize(batchSize);
		hashes = new ArrayList<Long>(batchSize);
	}

	public List<Long> getHashes() {
		return hashes;
	}

	public void addBatch(Number id, long hash)
		throws SQLException
	{
		hashes.add(hash);
		setObject(1, id);
		setObject(2, hash);
		addBatch();
	}

}
