/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.generaldb.managers.base;

import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicInteger;

import info.aduna.collections.LRUMap;

import org.openrdf.sail.generaldb.managers.HashManager;
import org.openrdf.sail.rdbms.model.RdbmsValue;
import org.openrdf.sail.generaldb.schema.IdSequence;

public abstract class ValueManagerBase<V extends RdbmsValue> extends ManagerBase {

	private LRUMap<Object, V> cache;

	private HashManager hashes;

	private AtomicInteger version = new AtomicInteger();

	private IdSequence ids;

	public void setHashManager(HashManager hashes) {
		this.hashes = hashes;
	}

	public IdSequence getIdSequence() {
		return ids;
	}

	public void setIdSequence(IdSequence ids) {
		this.ids = ids;
	}

	public void init() {
		cache = new LRUMap<Object, V>(getBatchSize());
	}

	@Override
	public void flush()
	throws SQLException, InterruptedException
	{
		if (hashes != null) {
			hashes.flush();
		}
		super.flush();
	}

	public V findInCache(Object key) {
		synchronized (cache) {
			if (cache.containsKey(key))
				return cache.get(key);
		}
		return null;
	}

	public void cache(V value)
	throws InterruptedException
	{
		if (value.isExpired(getIdVersion())) {
			synchronized (cache) {
				cache.put(key(value), value);
			}
			if (hashes != null) {
				hashes.lookupId(value);
			}
		}
	}

	public Number getInternalId(V value)
	throws SQLException, InterruptedException
	{
		if (value.isExpired(getIdVersion())) {
			if (hashes == null) {
				Number id = ids.idOf(value);
				value.setInternalId(id);
				value.setVersion(getIdVersion());
				insert(id, value);
			}
			else if (value.isExpired(getIdVersion())) {
				hashes.assignId(value, getIdVersion());
			}
		}
		return value.getInternalId();
	}

	

	public int getIdVersion() {
		return version.intValue() + (hashes == null ? 0 : hashes.getIdVersion());
	}

	public void removedStatements(String condition)
	throws SQLException
	{
		if (expunge(condition)) {
			version.addAndGet(1);
		}
	}

	protected abstract int getBatchSize();

	protected abstract void insert(Number id, V value)
	throws SQLException, InterruptedException;

	protected abstract Object key(V value);

	protected abstract boolean expunge(String condition)
	throws SQLException;

	@Override
	protected void optimize()
	throws SQLException
	{
		if (hashes != null) {
			hashes.optimize();
		}
	}

}
