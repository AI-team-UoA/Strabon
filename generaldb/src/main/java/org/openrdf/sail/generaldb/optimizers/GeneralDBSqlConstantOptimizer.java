/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.generaldb.optimizers;

import static org.openrdf.sail.generaldb.algebra.base.GeneralDBExprSupport.and;
import static org.openrdf.sail.generaldb.algebra.base.GeneralDBExprSupport.isNull;
import static org.openrdf.sail.generaldb.algebra.base.GeneralDBExprSupport.not;
import static org.openrdf.sail.generaldb.algebra.base.GeneralDBExprSupport.or;
import static org.openrdf.sail.generaldb.algebra.base.GeneralDBExprSupport.str;

import java.util.List;
import java.util.Locale;

import org.openrdf.query.BindingSet;
import org.openrdf.query.Dataset;
import org.openrdf.query.algebra.QueryModelNode;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.evaluation.QueryOptimizer;
import org.openrdf.sail.generaldb.algebra.GeneralDBFalseValue;
import org.openrdf.sail.generaldb.algebra.GeneralDBSelectQuery;
import org.openrdf.sail.generaldb.algebra.GeneralDBSqlAnd;
import org.openrdf.sail.generaldb.algebra.GeneralDBSqlCase;
import org.openrdf.sail.generaldb.algebra.GeneralDBSqlCompare;
import org.openrdf.sail.generaldb.algebra.GeneralDBSqlConcat;
import org.openrdf.sail.generaldb.algebra.GeneralDBSqlEq;
import org.openrdf.sail.generaldb.algebra.GeneralDBSqlIsNull;
import org.openrdf.sail.generaldb.algebra.GeneralDBSqlLowerCase;
import org.openrdf.sail.generaldb.algebra.GeneralDBSqlNot;
import org.openrdf.sail.generaldb.algebra.GeneralDBSqlNull;
import org.openrdf.sail.generaldb.algebra.GeneralDBSqlOr;
import org.openrdf.sail.generaldb.algebra.GeneralDBStringValue;
import org.openrdf.sail.generaldb.algebra.GeneralDBTrueValue;
import org.openrdf.sail.generaldb.algebra.GeneralDBSqlCase.Entry;
import org.openrdf.sail.generaldb.algebra.base.BinaryGeneralDBOperator;
import org.openrdf.sail.generaldb.algebra.base.GeneralDBFromItem;
import org.openrdf.sail.generaldb.algebra.base.GeneralDBQueryModelVisitorBase;
import org.openrdf.sail.generaldb.algebra.base.GeneralDBSqlConstant;
import org.openrdf.sail.generaldb.algebra.base.GeneralDBSqlExpr;
import org.openrdf.sail.generaldb.algebra.base.UnaryGeneralDBOperator;

/**
 * Optimises SQL constants, include operations with static values and null
 * operations.
 * 
 * @author James Leigh
 * 
 */
public class GeneralDBSqlConstantOptimizer extends GeneralDBQueryModelVisitorBase<RuntimeException> implements
		QueryOptimizer
{

	@Override
	public void meet(GeneralDBSelectQuery node)
		throws RuntimeException
	{
		super.meet(node);
		List<GeneralDBSqlExpr> filters = node.getFilters();
		for (int i = filters.size() - 1; i >= 0; i--) {
			if (filters.get(i) instanceof GeneralDBTrueValue) {
				node.removeFilter(filters.get(i));
			}
		}
	}

	@Override
	public void meet(GeneralDBSqlAnd node)
		throws RuntimeException
	{
		super.meet(node);
		GeneralDBSqlExpr left = node.getLeftArg();
		GeneralDBSqlExpr right = node.getRightArg();
		if (left instanceof GeneralDBFalseValue || right instanceof GeneralDBFalseValue) {
			replace(node, new GeneralDBFalseValue());
		}
		else if (left instanceof GeneralDBTrueValue && right instanceof GeneralDBTrueValue) {
			replace(node, new GeneralDBTrueValue());
		}
		else if (left instanceof GeneralDBTrueValue) {
			replace(node, right.clone());
		}
		else if (right instanceof GeneralDBTrueValue) {
			replace(node, left.clone());
		}
		else if (right instanceof GeneralDBSqlNull || left instanceof GeneralDBSqlNull) {
			replace(node, new GeneralDBSqlNull());
		}
		else if (right instanceof GeneralDBSqlNot && ((GeneralDBSqlNot)right).getArg().equals(left)) {
			replace(node, new GeneralDBFalseValue());
		}
		else if (left instanceof GeneralDBSqlNot && ((GeneralDBSqlNot)left).getArg().equals(right)) {
			replace(node, new GeneralDBFalseValue());
		}
	}

	@Override
	public void meet(GeneralDBSqlCase node)
		throws RuntimeException
	{
		super.meet(node);
		List<Entry> entries = node.getEntries();
		for (GeneralDBSqlCase.Entry e : entries) {
			if (e.getCondition() instanceof GeneralDBSqlNull) {
				node.removeEntry(e);
			}
			else if (e.getCondition() instanceof GeneralDBFalseValue) {
				node.removeEntry(e);
			}
			else if (e.getCondition() instanceof GeneralDBTrueValue) {
				node.truncateEntries(e);
				break;
			}
		}
		entries = node.getEntries();
		if (entries.isEmpty()) {
			replace(node, new GeneralDBSqlNull());
		}
		else if (entries.size() == 1) {
			Entry entry = entries.get(0);
			if (entry.getCondition() instanceof GeneralDBTrueValue) {
				replace(node, entry.getResult().clone());
			}
			else if (entry.getCondition() instanceof GeneralDBFalseValue) {
				replace(node, new GeneralDBSqlNull());
			}
			else if (entry.getCondition() instanceof GeneralDBSqlNot) {
				GeneralDBSqlNot not = (GeneralDBSqlNot)entry.getCondition();
				if (not.getArg() instanceof GeneralDBSqlIsNull) {
					GeneralDBSqlIsNull is = (GeneralDBSqlIsNull)not.getArg();
					if (is.getArg().equals(entry.getResult())) {
						replace(node, entry.getResult().clone());
					}
				}
			}
		}
	}

	@Override
	public void meet(GeneralDBSqlCompare node)
		throws RuntimeException
	{
		super.meet(node);
		GeneralDBSqlExpr left = node.getLeftArg();
		GeneralDBSqlExpr right = node.getRightArg();
		if (left instanceof GeneralDBSqlNull || right instanceof GeneralDBSqlNull) {
			replace(node, new GeneralDBSqlNull());
		}
	}

	@Override
	public void meet(GeneralDBSqlConcat node)
		throws RuntimeException
	{
		super.meet(node);
		GeneralDBSqlExpr left = node.getLeftArg();
		GeneralDBSqlExpr right = node.getRightArg();
		if (left instanceof GeneralDBStringValue && right instanceof GeneralDBStringValue) {
			GeneralDBStringValue l = (GeneralDBStringValue)left;
			GeneralDBStringValue r = (GeneralDBStringValue)right;
			replace(node, new GeneralDBStringValue(l.getValue() + r.getValue()));
		}
	}

	@Override
	public void meet(GeneralDBSqlEq node)
		throws RuntimeException
	{
		super.meet(node);
		GeneralDBSqlExpr left = node.getLeftArg();
		GeneralDBSqlExpr right = node.getRightArg();
		if (left instanceof GeneralDBSqlNull || right instanceof GeneralDBSqlNull) {
			replace(node, new GeneralDBSqlNull());
		}
		else if (left instanceof GeneralDBSqlConstant<?> && right instanceof GeneralDBSqlConstant<?>) {
			GeneralDBSqlConstant<?> l = (GeneralDBSqlConstant<?>)left;
			GeneralDBSqlConstant<?> r = (GeneralDBSqlConstant<?>)right;
			if (l.getValue().equals(r.getValue())) {
				replace(node, new GeneralDBTrueValue());
			}
			else {
				replace(node, new GeneralDBFalseValue());
			}
		}
	}

	@Override
	public void meet(GeneralDBSqlIsNull node)
		throws RuntimeException
	{
		super.meet(node);
		GeneralDBSqlExpr arg = node.getArg();
		if (arg instanceof GeneralDBSqlNull) {
			replace(node, new GeneralDBTrueValue());
		}
		else if (arg instanceof GeneralDBSqlConstant<?>) {
			replace(node, new GeneralDBFalseValue());
		}
		else if (arg instanceof GeneralDBSqlCase) {
			GeneralDBSqlExpr rep = null;
			GeneralDBSqlExpr prev = null;
			GeneralDBSqlCase scase = (GeneralDBSqlCase)arg;
			for (Entry entry : scase.getEntries()) {
				GeneralDBSqlExpr condition = entry.getCondition();
				if (rep == null) {
					rep = and(condition.clone(), isNull(entry.getResult().clone()));
					prev = not(condition.clone());
				}
				else {
					rep = or(rep, and(and(prev.clone(), condition.clone()), isNull(entry.getResult().clone())));
					prev = and(prev, not(condition.clone()));
				}
			}
			replace(node, or(rep, prev.clone()));
		}
	}

	@Override
	public void meet(GeneralDBSqlLowerCase node)
		throws RuntimeException
	{
		super.meet(node);
		if (node.getArg() instanceof GeneralDBSqlNull) {
			replace(node, new GeneralDBSqlNull());
		}
		else if (node.getArg() instanceof GeneralDBSqlConstant) {
			GeneralDBSqlConstant arg = (GeneralDBSqlConstant)node.getArg();
			String lower = arg.getValue().toString().toLowerCase(Locale.US);
			replace(node, str(lower));
		}
	}

	@Override
	public void meet(GeneralDBSqlNot node)
		throws RuntimeException
	{
		super.meet(node);
		GeneralDBSqlExpr arg = node.getArg();
		if (arg instanceof GeneralDBTrueValue) {
			replace(node, new GeneralDBFalseValue());
		}
		else if (arg instanceof GeneralDBFalseValue) {
			replace(node, new GeneralDBTrueValue());
		}
		else if (arg instanceof GeneralDBSqlNull) {
			replace(node, new GeneralDBSqlNull());
		}
		else if (arg instanceof GeneralDBSqlNot) {
			GeneralDBSqlNot not = (GeneralDBSqlNot)arg;
			replace(node, not.getArg().clone());
		}
		else if (arg instanceof GeneralDBSqlOr) {
			GeneralDBSqlOr or = (GeneralDBSqlOr)arg;
			replace(node, and(not(or.getLeftArg().clone()), not(or.getRightArg().clone())));
		}
	}

	@Override
	public void meet(GeneralDBSqlOr node)
		throws RuntimeException
	{
		super.meet(node);
		GeneralDBSqlExpr left = node.getLeftArg();
		GeneralDBSqlExpr right = node.getRightArg();
		if (left instanceof GeneralDBTrueValue || right instanceof GeneralDBTrueValue) {
			replace(node, new GeneralDBTrueValue());
		}
		else if (left instanceof GeneralDBFalseValue && right instanceof GeneralDBFalseValue) {
			replace(node, new GeneralDBFalseValue());
		}
		else if (left instanceof GeneralDBFalseValue) {
			replace(node, right.clone());
		}
		else if (right instanceof GeneralDBFalseValue) {
			replace(node, left.clone());
		}
		else if (right instanceof GeneralDBSqlNull && andAllTheWay(node)) {
			replace(node, left.clone());
		}
		else if (left instanceof GeneralDBSqlNull && andAllTheWay(node)) {
			replace(node, right.clone());
		}
		else if (right instanceof GeneralDBSqlNull && left instanceof GeneralDBSqlNull) {
			replace(node, new GeneralDBSqlNull());
		}
		else if (left instanceof GeneralDBSqlNull && right instanceof GeneralDBSqlOr) {
			GeneralDBSqlOr r = (GeneralDBSqlOr)right;
			GeneralDBSqlExpr rleft = r.getLeftArg();
			GeneralDBSqlExpr rright = r.getRightArg();
			if (rleft instanceof GeneralDBSqlNull || rright instanceof GeneralDBSqlNull) {
				replace(node, right.clone());
			}
		}
		else if (right instanceof GeneralDBSqlNull && left instanceof GeneralDBSqlOr) {
			GeneralDBSqlOr l = (GeneralDBSqlOr)left;
			GeneralDBSqlExpr lleft = l.getLeftArg();
			GeneralDBSqlExpr lright = l.getRightArg();
			if (lleft instanceof GeneralDBSqlNull || lright instanceof GeneralDBSqlNull) {
				replace(node, left.clone());
			}
		}
		else if (right instanceof GeneralDBSqlNull && left instanceof GeneralDBSqlAnd) {
			// value IS NOT NULL AND value = ? OR NULL
			// -> value = ?
			GeneralDBSqlAnd l = (GeneralDBSqlAnd)left;
			GeneralDBSqlExpr lleft = l.getLeftArg();
			GeneralDBSqlExpr lright = l.getRightArg();
			GeneralDBSqlExpr isNotNull = arg(arg(lleft, GeneralDBSqlNot.class), GeneralDBSqlIsNull.class);
			GeneralDBSqlExpr isNotEq = other(lright, isNotNull, GeneralDBSqlEq.class);
			if (isNotEq instanceof GeneralDBSqlConstant) {
				replace(node, lright);
			}
		}
	}

	public void optimize(GeneralDBSqlExpr sqlExpr) {
		sqlExpr.visit(this);
	}

	public void optimize(TupleExpr tupleExpr, Dataset dataset, BindingSet bindings) {
		tupleExpr.visit(this);
	}

	private boolean andAllTheWay(QueryModelNode node) {
		if (node.getParentNode() instanceof GeneralDBSelectQuery)
			return true;
		if (node.getParentNode() instanceof GeneralDBFromItem)
			return true;
		if (node.getParentNode() instanceof GeneralDBSqlAnd)
			return andAllTheWay(node.getParentNode());
		return false;
	}

	private GeneralDBSqlExpr arg(GeneralDBSqlExpr node, Class<? extends UnaryGeneralDBOperator> type) {
		if (type.isInstance(node))
			return type.cast(node).getArg();
		return null;
	}

	private GeneralDBSqlExpr other(GeneralDBSqlExpr node, GeneralDBSqlExpr compare, Class<? extends BinaryGeneralDBOperator> type) {
		if (type.isInstance(node)) {
			BinaryGeneralDBOperator cast = type.cast(node);
			GeneralDBSqlExpr left = cast.getLeftArg();
			GeneralDBSqlExpr right = cast.getRightArg();
			if (left.equals(compare))
				return right;
			if (right.equals(compare))
				return left;
		}
		return null;
	}

	private void replace(GeneralDBSqlExpr before, GeneralDBSqlExpr after) {
		before.replaceWith(after);
		after.visit(this);
	}
}
