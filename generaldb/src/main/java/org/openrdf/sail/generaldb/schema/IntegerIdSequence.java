/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.generaldb.schema;

import org.openrdf.sail.rdbms.schema.ValueType;
import java.sql.SQLException;
import java.sql.Types;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.openrdf.model.Value;

/**
 * 
 * @author James Leigh
 */
public class IntegerIdSequence extends IdSequence {

	private int SPAN = 268435455;

	private int SHIFT = Long.toBinaryString(SPAN).length();

	private Number[] minIds;

	private ConcurrentMap<ValueType, AtomicInteger> seq = new ConcurrentHashMap<ValueType, AtomicInteger>();

	public int getShift() {
		return SHIFT;
	}

	public int getJdbcIdType() {
		return Types.INTEGER;
	}

	public String getSqlType() {
		return "INTEGER";
	}

	public void init()
		throws SQLException
	{
		minIds = new Number[ValueType.values().length];
		for (int i = 0; i < minIds.length; i++) {
			minIds[i] = i * (SPAN + 1);
		}
		if (getHashTable() != null) {
			for (Number max : getHashTable().maxIds(getShift(), getMod())) {
				ValueType code = valueOf(max);
				if (max.intValue() > minId(code).intValue()) {
					if (!seq.containsKey(code)
							|| seq.get(code).intValue() < max.intValue()) {
						seq.put(code, new AtomicInteger(max.intValue()));
					}
				}
			}
		}
	}

	public Number idOf(Number number) {
		return number.intValue();
	}

	public Number maxId(ValueType type) {
		return minId(type).intValue() + SPAN;
	}

	public Number minId(ValueType type) {
		return minIds[type.index()];
	}

	public Number nextId(Value value) {
		ValueType code = valueOf(value);
		if (!seq.containsKey(code)) {
			seq.putIfAbsent(code, new AtomicInteger(minId(code).intValue()));
		}
		int id = seq.get(code).incrementAndGet();
		return id;
	}

	@Override
	protected int shift(Number id) {
		return id.intValue() >>> SHIFT;
	}
}
