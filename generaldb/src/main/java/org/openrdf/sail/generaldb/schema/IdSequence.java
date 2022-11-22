/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.generaldb.schema;

import static org.openrdf.model.datatypes.XMLDatatypeUtil.isCalendarDatatype;
import static org.openrdf.model.datatypes.XMLDatatypeUtil.isNumericDatatype;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;

import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.IRI;
import org.openrdf.model.Value;
import org.openrdf.model.vocabulary.RDF;

import org.openrdf.sail.rdbms.schema.ValueType;
/**
 * 
 * @author James Leigh
 */
public abstract class IdSequence {

	private static final String UTF_8 = "UTF-8";

	private static ThreadLocal<MessageDigest> md5 = new ThreadLocal<MessageDigest>() {

		@Override
		protected MessageDigest initialValue() {
			try {
				return MessageDigest.getInstance("MD5");
			}
			catch (NoSuchAlgorithmException e) {
				throw new AssertionError(e);
			}
		}

	};

	/** 255 */
	private int LONG = 255;

	private int MOD = 16;

	private HashTable table;

	public int getMod() {
		return MOD;
	}

	public abstract int getShift();

	public abstract int getJdbcIdType();

	public abstract String getSqlType();

	public HashTable getHashTable() {
		return table;
	}

	public void setHashTable(HashTable table) {
		this.table = table;
	}

	public abstract void init()
		throws SQLException;

	public abstract Number maxId(ValueType type);

	public abstract Number minId(ValueType type);

	public int code(Literal value) {
		return shift(minId(valueOf(value)));
	}

	public long hashOf(Value value) {
		final long span = 1152921504606846975l;
		MessageDigest digest = md5.get();
		long type = hashLiteralType(digest, value);
		long hash = type * 31 + hash(digest, value.stringValue());
		return hash & span | valueOf(value).index() * (span + 1);
	}

	public abstract Number nextId(Value value);

	public boolean isLiteral(Number id) {
		return valueOf(id).isLiteral();
	}

	public boolean isLong(Number id) {
		return valueOf(id).isLong();
	}

	public boolean isURI(Number id) {
		return valueOf(id).isURI();
	}

	public Number idOf(Value value) {
		return idOf(hashOf(value));
	}

	public abstract Number idOf(Number number);

	public ValueType valueOf(Number id) {
		int idx = shift(id);
		ValueType[] values = ValueType.values();
		if (idx < 0 || idx >= values.length)
			throw new IllegalArgumentException("Invalid ID " + id);
		return values[idx];
	}

	protected abstract int shift(Number id);

	protected long hash(MessageDigest digest, String str) {
		try {
			digest.update(str.getBytes(UTF_8));
			return new BigInteger(1, digest.digest()).longValue();
		}
		catch (UnsupportedEncodingException e) {
			throw new AssertionError(e);
		}
	}

	protected long hashLiteralType(MessageDigest digest, Value value) {
		if (value instanceof Literal) {
			Literal lit = (Literal)value;
			if (lit.getDatatype() != null)
				return hash(digest, lit.getDatatype().stringValue());
			if (lit.getLanguage() != null)
				return hash(digest, lit.getLanguage().get());
		}
		return 0;
	}

	private boolean isZoned(Literal lit) {
		String stringValue = lit.stringValue();
		int length = stringValue.length();
		if (length < 1)
			return false;
		if (stringValue.charAt(length - 1) == 'Z')
			return true;
		if (length < 6)
			return false;
		if (stringValue.charAt(length - 3) != ':')
			return false;
		char chr = stringValue.charAt(length - 6);
		return chr == '+' || chr == '-';
	}

	private ValueType valueOf(BNode value) {
		return ValueType.BNODE;
	}

	protected ValueType valueOf(Literal lit) {
		String lang = lit.getLanguage().get();
		IRI dt = lit.getDatatype();
		int length = lit.stringValue().length();
		if (lang != null) {
			// language
			if (length > LONG)
				return ValueType.LANG_LONG;
			return ValueType.LANG;
		}
		if (dt == null) {
			// simple
			if (length > LONG)
				return ValueType.SIMPLE_LONG;
			return ValueType.SIMPLE;
		}
		if (isNumericDatatype(dt))
			return ValueType.NUMERIC;
		if (isCalendarDatatype(dt)) {
			// calendar
			if (isZoned(lit))
				return ValueType.DATETIME_ZONED;
			return ValueType.DATETIME;
		}
		if (RDF.XMLLITERAL.equals(dt))
			return ValueType.XML;
		if (length > LONG)
			return ValueType.TYPED_LONG;
		return ValueType.TYPED;
	}

	private ValueType valueOf(URI value) {
		if (value.stringValue().length() > LONG)
			return ValueType.URI_LONG;
		return ValueType.URI;
	}

	protected ValueType valueOf(Value value) {
		if (value instanceof URI)
			return valueOf((URI)value);
		if (value instanceof Literal)
			return valueOf((Literal)value);
		assert value instanceof BNode : value;
		return valueOf((BNode)value);
	}
}
