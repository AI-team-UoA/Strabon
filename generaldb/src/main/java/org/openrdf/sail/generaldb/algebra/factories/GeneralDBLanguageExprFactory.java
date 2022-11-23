/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.generaldb.algebra.factories;

import static org.openrdf.sail.generaldb.algebra.base.GeneralDBExprSupport.sqlNull;
import static org.openrdf.sail.generaldb.algebra.base.GeneralDBExprSupport.str;
import static org.openrdf.sail.generaldb.algebra.base.GeneralDBExprSupport.unsupported;

import org.openrdf.model.Literal;
import org.openrdf.model.Value;
import org.openrdf.query.algebra.Datatype;
import org.openrdf.query.algebra.Lang;
import org.openrdf.query.algebra.MathExpr;
import org.openrdf.query.algebra.QueryModelNode;
import org.openrdf.query.algebra.Str;
import org.openrdf.query.algebra.ValueConstant;
import org.openrdf.query.algebra.ValueExpr;
import org.openrdf.query.algebra.Var;
import org.openrdf.query.algebra.helpers.QueryModelVisitorBase;
import org.openrdf.sail.generaldb.algebra.GeneralDBLanguageColumn;
import org.openrdf.sail.generaldb.algebra.GeneralDBSqlNull;
import org.openrdf.sail.generaldb.algebra.base.GeneralDBSqlExpr;
import org.openrdf.sail.rdbms.exceptions.UnsupportedRdbmsOperatorException;

/**
 * Creates an SQL expression of a literal's language.
 * 
 * @author James Leigh
 * 
 */
public class GeneralDBLanguageExprFactory extends QueryModelVisitorBase<UnsupportedRdbmsOperatorException> {

	protected GeneralDBSqlExpr result;

	public GeneralDBSqlExpr createLanguageExpr(ValueExpr expr)
		throws UnsupportedRdbmsOperatorException
	{
		result = null;
		if (expr == null)
			return new GeneralDBSqlNull();
		expr.visit(this);
		if (result == null)
			return new GeneralDBSqlNull();
		return result;
	}

	@Override
	public void meet(Datatype node)
		throws UnsupportedRdbmsOperatorException
	{
		result = sqlNull();
	}

	@Override
	public void meet(Lang node)
		throws UnsupportedRdbmsOperatorException
	{
		result = sqlNull();
	}

	@Override
	public void meet(MathExpr node)
		throws UnsupportedRdbmsOperatorException
	{
		result = sqlNull();
	}

	@Override
	public void meet(Str node)
		throws UnsupportedRdbmsOperatorException
	{
		result = sqlNull();
	}

	@Override
	public void meet(ValueConstant vc) {
		result = valueOf(vc.getValue());
	}

	@Override
	public void meet(Var var) {
		if (var.getValue() == null) {
			result = new GeneralDBLanguageColumn(var);
		}
		else {
			result = valueOf(var.getValue());
		}
	}

	@Override
	protected void meetNode(QueryModelNode arg)
		throws UnsupportedRdbmsOperatorException
	{
		throw unsupported(arg);
	}

	private GeneralDBSqlExpr valueOf(Value value) {
		if (value instanceof Literal) {
			Literal lit = (Literal)value;
			return str(lit.getLanguage());
		}
		return sqlNull();
	}

}