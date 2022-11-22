/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.generaldb.schema;

import java.sql.SQLException;
import java.sql.Types;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;


import org.openrdf.sail.rdbms.schema.ValueType;
import org.openrdf.model.Value;

/**
 * 
 * @author James Leigh
 */
public class LongIdSequence extends IdSequence {

	private long SPAN = 1152921504606846975l;

	private int SHIFT = Long.toBinaryString(SPAN).length();

	private Number[] minIds;

	private ConcurrentMap<ValueType, AtomicLong> seq = new ConcurrentHashMap<ValueType, AtomicLong>();

	public int getShift() {
		return SHIFT;
	}

	public int getJdbcIdType() {
		return Types.BIGINT;
	}

	public String getSqlType() {
		return "BIGINT";
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
				if (max.longValue() > minId(code).longValue()) {
					if (!seq.containsKey(code)
							|| seq.get(code).longValue() < max.longValue()) {
						seq.put(code, new AtomicLong(max.longValue()));
					}
				}
			}
		}
	}

	public Number idOf(Number number) {
		return number.longValue();
	}

	public Number maxId(ValueType type) {
		return minId(type).longValue() + SPAN;
	}

	public Number minId(ValueType type) {
		return minIds[type.index()];
	}

	public Number nextId(Value value) {
		ValueType code = valueOf(value);
		if (!seq.containsKey(code)) {
			seq.putIfAbsent(code, new AtomicLong(minId(code).longValue()));
		}
		return seq.get(code).incrementAndGet();
	}

	@Override
	protected int shift(Number id) {
		return (int)(id.longValue() >>> SHIFT);
	}
}
