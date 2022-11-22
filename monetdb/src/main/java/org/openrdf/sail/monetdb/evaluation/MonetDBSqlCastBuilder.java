package org.openrdf.sail.monetdb.evaluation;

import java.sql.Types;

import org.openrdf.sail.generaldb.evaluation.GeneralDBQueryBuilderFactory;
import org.openrdf.sail.generaldb.evaluation.GeneralDBSqlCastBuilder;
import org.openrdf.sail.generaldb.evaluation.GeneralDBSqlExprBuilder;

public class MonetDBSqlCastBuilder extends MonetDBSqlExprBuilder implements GeneralDBSqlCastBuilder {
	
	protected GeneralDBSqlExprBuilder where;

	protected int jdbcType;

	public MonetDBSqlCastBuilder(GeneralDBSqlExprBuilder where, GeneralDBQueryBuilderFactory factory, int jdbcType) {
		super(factory);
		this.where = where;
		this.jdbcType = jdbcType;
		append(" CAST(");
	}

	public GeneralDBSqlExprBuilder close() {
		append(" AS ");
		append(getSqlType(jdbcType));
		append(")");
		where.append(toSql());
		where.addParameters(getParameters());
		return where;
	}

	protected CharSequence getSqlType(int type) {
		switch (type) {
			case Types.VARCHAR:
				return "VARCHAR";
			default:
				throw new AssertionError(type);
		}
	}
}
