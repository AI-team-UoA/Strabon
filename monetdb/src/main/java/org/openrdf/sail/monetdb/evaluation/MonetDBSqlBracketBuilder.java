package org.openrdf.sail.monetdb.evaluation;

import org.openrdf.sail.generaldb.evaluation.GeneralDBQueryBuilderFactory;
import org.openrdf.sail.generaldb.evaluation.GeneralDBSqlBracketBuilder;
import org.openrdf.sail.generaldb.evaluation.GeneralDBSqlExprBuilder;

public class MonetDBSqlBracketBuilder extends MonetDBSqlExprBuilder implements GeneralDBSqlBracketBuilder {

	private MonetDBSqlExprBuilder where;

	private String closing = ")";

	public MonetDBSqlBracketBuilder(GeneralDBSqlExprBuilder where, GeneralDBQueryBuilderFactory factory) {
		super(factory);
		this.where = (MonetDBSqlExprBuilder)where;
		append("(");
	}

	public String getClosing() {
		return closing;
	}

	public void setClosing(String closing) {
		this.closing = closing;
	}

	public MonetDBSqlExprBuilder close() {
		append(closing);
		where.append(toSql());
		where.addParameters(getParameters());
		return where;
	}
	
}
