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

/**
 * An abstract binary sql operator with two arguments.
 * 
 * @author James Leigh
 * 
 */
public abstract class BinaryGeneralDBOperator extends GeneralDBQueryModelNodeBase implements GeneralDBSqlExpr {

	private GeneralDBSqlExpr leftArg;

	private GeneralDBSqlExpr rightArg;

	public BinaryGeneralDBOperator() {
		super();
	}

	public BinaryGeneralDBOperator(GeneralDBSqlExpr leftArg, GeneralDBSqlExpr rightArg) {
		super(); // Edw to this(distance) exei ginei GeneralDBSqlGeoDistance
		setLeftArg(leftArg);
		setRightArg(rightArg);
	}

	public GeneralDBSqlExpr getLeftArg() {
		return leftArg;
	}

	public void setLeftArg(GeneralDBSqlExpr leftArg) {
		this.leftArg = leftArg;
		leftArg.setParentNode(this);
	}

	public GeneralDBSqlExpr getRightArg() {
		return rightArg;
	}

	public void setRightArg(GeneralDBSqlExpr rightArg) {
		this.rightArg = rightArg;
		rightArg.setParentNode(this);
	}

	@Override
	public <X extends Exception> void visitChildren(QueryModelVisitor<X> visitor)
		throws X
	{
		leftArg.visit(visitor);
		rightArg.visit(visitor);
	}

	@Override
	public void replaceChildNode(QueryModelNode current, QueryModelNode replacement) {
		if (leftArg == current) {
			setLeftArg((GeneralDBSqlExpr)replacement);
		}
		else if (rightArg == current) {
			setRightArg((GeneralDBSqlExpr)replacement);
		}
		else {
			super.replaceChildNode(current, replacement);
		}
	}

	@Override
	public BinaryGeneralDBOperator clone() {
		BinaryGeneralDBOperator clone = (BinaryGeneralDBOperator)super.clone();
		clone.setLeftArg(leftArg.clone());
		clone.setRightArg(rightArg.clone());
		return clone;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((leftArg == null) ? 0 : leftArg.hashCode());
		result = prime * result + ((rightArg == null) ? 0 : rightArg.hashCode());
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
		final BinaryGeneralDBOperator other = (BinaryGeneralDBOperator)obj;
		if (leftArg == null) {
			if (other.leftArg != null)
				return false;
		}
		else if (!leftArg.equals(other.leftArg))
			return false;
		if (rightArg == null) {
			if (other.rightArg != null)
				return false;
		}
		else if (!rightArg.equals(other.rightArg))
			return false;
		return true;
	}

	@Override
	public String toString() {
		QueryModelTreePrinter treePrinter = new QueryModelTreePrinter();
		BinaryGeneralDBOperator clone = this.clone();
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
