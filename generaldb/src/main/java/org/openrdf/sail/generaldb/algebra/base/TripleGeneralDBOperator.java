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
 * @author manolee
 * A new type of operator , used to accommodate the need for Triple operators
 * such as relate(geo1,geo2,2) 
 * 
 */
public abstract class TripleGeneralDBOperator extends GeneralDBQueryModelNodeBase implements GeneralDBSqlExpr {

	private GeneralDBSqlExpr leftArg;

	private GeneralDBSqlExpr rightArg;
	
	private GeneralDBSqlExpr thirdArg;

	public TripleGeneralDBOperator() {
		super();
	}

	public TripleGeneralDBOperator(GeneralDBSqlExpr leftArg, GeneralDBSqlExpr rightArg, GeneralDBSqlExpr thirdArg) {
		super();
		setLeftArg(leftArg);
		setRightArg(rightArg);
		setThirdArg(thirdArg);
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
	
	public GeneralDBSqlExpr getThirdArg() {
		return thirdArg;
	}

	public void setThirdArg(GeneralDBSqlExpr thirdArg) {
		this.thirdArg = thirdArg;
		thirdArg.setParentNode(this);
	}

	@Override
	public <X extends Exception> void visitChildren(QueryModelVisitor<X> visitor)
		throws X
	{
		leftArg.visit(visitor);
		rightArg.visit(visitor);
		thirdArg.visit(visitor);
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
	public TripleGeneralDBOperator clone() {
		TripleGeneralDBOperator clone = (TripleGeneralDBOperator)super.clone();
		clone.setLeftArg(leftArg.clone());
		clone.setRightArg(rightArg.clone());
		clone.setThirdArg(thirdArg.clone());
		return clone;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((leftArg == null) ? 0 : leftArg.hashCode());
		result = prime * result + ((rightArg == null) ? 0 : rightArg.hashCode());
		result = prime * result + ((thirdArg == null) ? 0 : thirdArg.hashCode());
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
		final TripleGeneralDBOperator other = (TripleGeneralDBOperator)obj;
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
		if (thirdArg == null) {
			if (other.thirdArg != null)
				return false;
		}
		else if (!thirdArg.equals(other.thirdArg))
			return false;
		return true;
	}

	@Override
	public String toString() {
		QueryModelTreePrinter treePrinter = new QueryModelTreePrinter();
		TripleGeneralDBOperator clone = this.clone();
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
