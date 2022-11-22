package org.openrdf.sail.postgis;

import org.openrdf.sail.generaldb.GeneralDBSqlValueTableFactory;


public class PostGISSqlValueTableFactory extends GeneralDBSqlValueTableFactory {

	public PostGISSqlValueTableFactory() {
		super(new PostGISSqlTableFactory());
	}
}
