/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.generaldb.algebra;

import java.util.ArrayList;
import java.util.List;

import org.openrdf.query.algebra.QueryModelNode;
import org.openrdf.query.algebra.QueryModelVisitor;
import org.openrdf.sail.generaldb.algebra.base.GeneralDBQueryModelNodeBase;
import org.openrdf.sail.generaldb.algebra.base.GeneralDBQueryModelVisitorBase;
import org.openrdf.sail.generaldb.algebra.base.GeneralDBSqlExpr;

/**
 * The SQL CASE WHEN THEN END expression.
 * 
 * @author James Leigh
 * 
 */
public class GeneralDBSqlCase extends GeneralDBQueryModelNodeBase implements GeneralDBSqlExpr {

	private List<Entry> entries = new ArrayList<Entry>();

	public class Entry {

		private GeneralDBSqlExpr condition;

		private GeneralDBSqlExpr result;

		public Entry(GeneralDBSqlExpr condition, GeneralDBSqlExpr result) {
			super();
			this.condition = condition;
			this.result = result;
		}

		public GeneralDBSqlExpr getCondition() {
			return condition;
		}

		public void setCondition(GeneralDBSqlExpr condition) {
			this.condition = condition;
			condition.setParentNode(GeneralDBSqlCase.this);
		}

		public GeneralDBSqlExpr getResult() {
			return result;
		}

		public void setResult(GeneralDBSqlExpr result) {
			this.result = result;
			result.setParentNode(GeneralDBSqlCase.this);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((condition == null) ? 0 : condition.hashCode());
			result = prime * result + ((this.result == null) ? 0 : this.result.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			final Entry other = (Entry)obj;
			if (condition == null) {
				if (other.condition != null)
					return false;
			}
			else if (!condition.equals(other.condition))
				return false;
			if (result == null) {
				if (other.result != null)
					return false;
			}
			else if (!result.equals(other.result))
				return false;
			return true;
		}
	}

	public void when(GeneralDBSqlExpr condition, GeneralDBSqlExpr expr) {
		entries.add(new Entry(condition, expr));
		condition.setParentNode(this);
		expr.setParentNode(this);
	}

	public List<Entry> getEntries() {
		return new ArrayList<Entry>(entries);
	}

	@Override
	public <X extends Exception> void visitChildren(QueryModelVisitor<X> visitor)
		throws X
	{
		for (Entry e : entries) {
			e.getCondition().visit(visitor);
			e.getResult().visit(visitor);
		}
	}

	@Override
	public void replaceChildNode(QueryModelNode current, QueryModelNode replacement) {
		for (Entry e : entries) {
			if (e.getCondition() == current) {
				e.setCondition((GeneralDBSqlExpr)replacement);
			}
			else if (e.getResult() == current) {
				e.setResult((GeneralDBSqlExpr)replacement);
			}
		}
	}

	@Override
	public GeneralDBSqlCase clone() {
		GeneralDBSqlCase clone = (GeneralDBSqlCase)super.clone();
		clone.entries = new ArrayList<Entry>();
		for (Entry e : entries) {
			clone.when(e.getCondition().clone(), e.getResult().clone());
		}
		return clone;
	}

	@Override
	public <X extends Exception> void visit(GeneralDBQueryModelVisitorBase<X> visitor)
		throws X
	{
		visitor.meet(this);
	}

	public void removeEntry(Entry e) {
		entries.remove(e);
	}

	public void truncateEntries(Entry e) {
		int idx = entries.indexOf(e) + 1;
		if (idx < entries.size()) {
			entries = entries.subList(0, idx);
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((entries == null) ? 0 : entries.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final GeneralDBSqlCase other = (GeneralDBSqlCase)obj;
		if (entries == null) {
			if (other.entries != null)
				return false;
		}
		else if (!entries.equals(other.entries))
			return false;
		return true;
	}

}
