/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.generaldb.optimizers;

import org.openrdf.query.BindingSet;
import org.openrdf.query.Dataset;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.evaluation.QueryOptimizer;
import org.openrdf.sail.generaldb.algebra.GeneralDBBNodeColumn;
import org.openrdf.sail.generaldb.algebra.GeneralDBColumnVar;
import org.openrdf.sail.generaldb.algebra.GeneralDBDatatypeColumn;
import org.openrdf.sail.generaldb.algebra.GeneralDBDateTimeColumn;
import org.openrdf.sail.generaldb.algebra.GeneralDBHashColumn;
import org.openrdf.sail.generaldb.algebra.GeneralDBIdColumn;
import org.openrdf.sail.generaldb.algebra.GeneralDBLabelColumn;
import org.openrdf.sail.generaldb.algebra.GeneralDBLanguageColumn;
import org.openrdf.sail.generaldb.algebra.GeneralDBLongLabelColumn;
import org.openrdf.sail.generaldb.algebra.GeneralDBLongURIColumn;
import org.openrdf.sail.generaldb.algebra.GeneralDBNumericColumn;
import org.openrdf.sail.generaldb.algebra.GeneralDBRefIdColumn;
import org.openrdf.sail.generaldb.algebra.GeneralDBSelectQuery;
import org.openrdf.sail.generaldb.algebra.GeneralDBSqlIsNull;
import org.openrdf.sail.generaldb.algebra.GeneralDBSqlNull;
import org.openrdf.sail.generaldb.algebra.GeneralDBURIColumn;
import org.openrdf.sail.generaldb.algebra.base.GeneralDBFromItem;
import org.openrdf.sail.generaldb.algebra.base.GeneralDBQueryModelVisitorBase;
import org.openrdf.sail.generaldb.algebra.base.GeneralDBValueColumnBase;

/**
 * Localises variables to use an available column in the current variable scope.
 * 
 * @author James Leigh
 * 
 */
public class GeneralDBVarColumnLookupOptimizer extends GeneralDBQueryModelVisitorBase<RuntimeException> implements
		QueryOptimizer
{

	private GeneralDBFromItem parent;

	private GeneralDBFromItem gparent;

	public GeneralDBVarColumnLookupOptimizer() {
		super();
	}

	public void optimize(TupleExpr tupleExpr, Dataset dataset, BindingSet bindings) {
		parent = null;
		tupleExpr.visit(this);
	}

	@Override
	public void meetFromItem(GeneralDBFromItem node)
		throws RuntimeException
	{
		GeneralDBFromItem top = gparent;
		gparent = parent;
		parent = node;
		super.meetFromItem(node);
		parent = gparent;
		gparent = top;
	}

	@Override
	public void meet(GeneralDBSelectQuery node)
		throws RuntimeException
	{
		gparent = node.getFrom();
		parent = node.getFrom();
		super.meet(node);
		parent = null;
		gparent = null;
	}

	@Override
	public void meet(GeneralDBBNodeColumn node)
		throws RuntimeException
	{
		GeneralDBColumnVar var = replaceVar(node);
		if (var == null)
			return;
		if (!var.getTypes().isBNodes()) {
			node.replaceWith(new GeneralDBSqlNull());
		}
	}

	@Override
	public void meet(GeneralDBDatatypeColumn node)
		throws RuntimeException
	{
		GeneralDBColumnVar var = replaceVar(node);
		if (var == null)
			return;
		if (!var.getTypes().isTyped()) {
			node.replaceWith(new GeneralDBSqlNull());
		}
	}

	@Override
	public void meet(GeneralDBDateTimeColumn node)
		throws RuntimeException
	{
		GeneralDBColumnVar var = replaceVar(node);
		if (var == null)
			return;
		if (!var.getTypes().isCalendar()) {
			node.replaceWith(new GeneralDBSqlNull());
		}
	}

	@Override
	public void meet(GeneralDBLabelColumn node)
		throws RuntimeException
	{
		GeneralDBColumnVar var = replaceVar(node);
		if (var == null)
			return;
		if (!var.getTypes().isLiterals()) {
			node.replaceWith(new GeneralDBSqlNull());
		}
	}

	@Override
	public void meet(GeneralDBLongLabelColumn node)
		throws RuntimeException
	{
		GeneralDBColumnVar var = replaceVar(node);
		if (var == null)
			return;
		if (!var.getTypes().isLong() || !var.getTypes().isLiterals()) {
			node.replaceWith(new GeneralDBSqlNull());
		}
	}

	@Override
	public void meet(GeneralDBLanguageColumn node)
		throws RuntimeException
	{
		GeneralDBColumnVar var = replaceVar(node);
		if (var == null)
			return;
		if (!var.getTypes().isLanguages()) {
			node.replaceWith(new GeneralDBSqlNull());
		}
	}

	@Override
	public void meet(GeneralDBNumericColumn node)
		throws RuntimeException
	{
		GeneralDBColumnVar var = replaceVar(node);
		if (var == null)
			return;
		if (!var.getTypes().isNumeric()) {
			node.replaceWith(new GeneralDBSqlNull());
		}
	}

	@Override
	public void meet(GeneralDBLongURIColumn node)
		throws RuntimeException
	{
		GeneralDBColumnVar var = replaceVar(node);
		if (var == null)
			return;
		if (!var.getTypes().isLong() || !var.getTypes().isURIs()) {
			node.replaceWith(new GeneralDBSqlNull());
		}
	}

	@Override
	public void meet(GeneralDBURIColumn node)
		throws RuntimeException
	{
		GeneralDBColumnVar var = replaceVar(node);
		if (var == null)
			return;
		if (!var.getTypes().isURIs()) {
			node.replaceWith(new GeneralDBSqlNull());
		}
	}

	@Override
	public void meet(GeneralDBRefIdColumn node)
		throws RuntimeException
	{
		replaceVar(node);
	}

	@Override
	public void meet(GeneralDBHashColumn node)
		throws RuntimeException
	{
		replaceVar(node);
	}

	private GeneralDBColumnVar replaceVar(GeneralDBValueColumnBase node) {
		GeneralDBColumnVar var = null;
		if (var == null) {
			var = parent.getVar(node.getVarName());
		}
		if (var == null && gparent != parent) {
			var = gparent.getVarForChildren(node.getVarName());
		}
		if (var == null) {
			node.replaceWith(new GeneralDBSqlNull());
		}
		else if (var.isImplied() && node.getParentNode() instanceof GeneralDBSqlIsNull) {
			node.replaceWith(new GeneralDBIdColumn(var.getAlias(), "subj"));
		}
		else {
			node.setRdbmsVar(var);
		}
		return var;
	}
}
