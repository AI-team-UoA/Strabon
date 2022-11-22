/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.generaldb.algebra.factories;

import static org.openrdf.sail.generaldb.algebra.base.GeneralDBExprSupport.coalesce;
import static org.openrdf.sail.generaldb.algebra.base.GeneralDBExprSupport.isNotNull;
import static org.openrdf.sail.generaldb.algebra.base.GeneralDBExprSupport.sqlNull;
import static org.openrdf.sail.generaldb.algebra.base.GeneralDBExprSupport.str;
import static org.openrdf.sail.generaldb.algebra.base.GeneralDBExprSupport.text;
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
import org.openrdf.sail.generaldb.algebra.GeneralDBLabelColumn;
import org.openrdf.sail.generaldb.algebra.GeneralDBLongLabelColumn;
import org.openrdf.sail.generaldb.algebra.GeneralDBSqlCase;
import org.openrdf.sail.generaldb.algebra.GeneralDBSqlNull;
import org.openrdf.sail.generaldb.algebra.base.GeneralDBSqlExpr;
import org.openrdf.sail.rdbms.exceptions.UnsupportedRdbmsOperatorException;

/**
 * Creates a SQl expression of a literal label.
 * 
 * @author James Leigh
 * 
 */
public class GeneralDBLabelExprFactory extends QueryModelVisitorBase<UnsupportedRdbmsOperatorException> {

	protected GeneralDBSqlExpr result;

	private GeneralDBSqlExprFactory sql;

	public void setSqlExprFactory(GeneralDBSqlExprFactory sql) {
		this.sql = sql;
	}

	public GeneralDBSqlExpr createLabelExpr(ValueExpr expr)
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
		GeneralDBSqlCase sqlCase = new GeneralDBSqlCase();
		sqlCase.when(isNotNull(lang(node.getArg())), lang(node.getArg()));
		sqlCase.when(isNotNull(createLabelExpr(node.getArg())), str(""));
		result = sqlCase;
	}

	@Override
	public void meet(MathExpr node)
		throws UnsupportedRdbmsOperatorException
	{
		result = text(num(node));
	}

	@Override
	public void meet(Str str)
		throws UnsupportedRdbmsOperatorException
	{
		ValueExpr arg = str.getArg();
		result = coalesce(uri(arg), createLabelExpr(arg));
	}

	@Override
	public void meet(ValueConstant vc) {
		result = valueOf(vc.getValue());
	}

	@Override
	public void meet(Var var) {
		if (var.getValue() == null) {
			if(var.getName().endsWith("?spatial"))
			{
				//XXX spatial var!
				result = new GeneralDBLabelColumn(var);
			}
			else
			{
				//DEFAULT BEHAVIOR!
				result = coalesce(new GeneralDBLabelColumn(var), new GeneralDBLongLabelColumn(var));
			}
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

	private GeneralDBSqlExpr lang(ValueExpr arg)
		throws UnsupportedRdbmsOperatorException
	{
		return sql.createLanguageExpr(arg);
	}

	private GeneralDBSqlExpr uri(ValueExpr arg)
		throws UnsupportedRdbmsOperatorException
	{
		return sql.createUriExpr(arg);
	}

	private GeneralDBSqlExpr num(ValueExpr arg)
		throws UnsupportedRdbmsOperatorException
	{
		return sql.createNumericExpr(arg);
	}

	private GeneralDBSqlExpr valueOf(Value value) {
		if (value instanceof Literal) {
			return str(((Literal)value).getLabel());
		}
		return sqlNull();
	}

}