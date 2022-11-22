/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.generaldb.algebra.base;

import org.openrdf.query.algebra.QueryModelNode;
import org.openrdf.query.algebra.QueryModelVisitor;
import org.openrdf.query.algebra.helpers.QueryModelTreePrinter;
import org.openrdf.sail.generaldb.optimizers.GeneralDBSqlConstantOptimizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An SQL operator with one argument.
 * 
 * @author Charalampos Nikolaou <charnik@di.uoa.gr>
 * @author James Leigh
 */
public abstract class UnaryGeneralDBOperator extends GeneralDBQueryModelNodeBase implements GeneralDBSqlExpr {

	private static Logger logger = LoggerFactory.getLogger(org.openrdf.sail.generaldb.algebra.base.UnaryGeneralDBOperator.class);

	private GeneralDBSqlExpr arg;

	public UnaryGeneralDBOperator() {
		super();
	}

	public UnaryGeneralDBOperator(GeneralDBSqlExpr arg) {
		super();
		setArg(arg);
	}

	public GeneralDBSqlExpr getArg() {
		return arg;
	}

	public void setArg(GeneralDBSqlExpr arg) {
		this.arg = arg;
		arg.setParentNode(this);
	}

	@Override
	public <X extends Exception> void visitChildren(QueryModelVisitor<X> visitor)
		throws X
	{
		arg.visit(visitor);
	}

	@Override
	public void replaceChildNode(QueryModelNode current, QueryModelNode replacement) {
		if (arg == current) {
			setArg((GeneralDBSqlExpr)replacement);
		}
		else {
			if (logger.isWarnEnabled()) {
				logger.warn("[Strabon.GeneralDB] The next call will blow things up. I'll try to suppress it, but notify me in case anything goes wrong.");
			}
			//super.replaceChildNode(current, replacement);
		}
	}

	@Override
	public UnaryGeneralDBOperator clone() {
		UnaryGeneralDBOperator clone = (UnaryGeneralDBOperator)super.clone();
		clone.setArg(arg.clone());
		return clone;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((arg == null) ? 0 : arg.hashCode());
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
		final UnaryGeneralDBOperator other = (UnaryGeneralDBOperator)obj;
		if (arg == null) {
			if (other.arg != null)
				return false;
		}
		else if (!arg.equals(other.arg))
			return false;
		return true;
	}

	@Override
	public String toString() {
		QueryModelTreePrinter treePrinter = new QueryModelTreePrinter();
		UnaryGeneralDBOperator clone = this.clone();
		UnaryGeneralDBOperator parent = new UnaryGeneralDBOperator(clone) {

			@Override
			public <X extends Exception> void visit(GeneralDBQueryModelVisitorBase<X> visitor)
				throws X
			{
				visitor.meetOther(this);
			}
		};
		new GeneralDBSqlConstantOptimizer().optimize(clone);
		parent.getArg().visit(treePrinter);
		return treePrinter.getTreeString();
	}

}
