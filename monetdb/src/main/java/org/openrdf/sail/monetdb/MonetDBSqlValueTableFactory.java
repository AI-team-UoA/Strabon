package org.openrdf.sail.monetdb;

import org.openrdf.sail.generaldb.GeneralDBSqlValueTableFactory;


public class MonetDBSqlValueTableFactory extends GeneralDBSqlValueTableFactory {

	public MonetDBSqlValueTableFactory() {
		super(new MonetDBSqlTableFactory());
	}
}
