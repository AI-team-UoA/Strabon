/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.generaldb.optimizers;

import org.openrdf.sail.generaldb.GeneralDBValueFactory;
import org.openrdf.sail.generaldb.algebra.factories.GeneralDBBNodeExprFactory;
import org.openrdf.sail.generaldb.algebra.factories.GeneralDBBooleanExprFactory;
import org.openrdf.sail.generaldb.algebra.factories.GeneralDBDatatypeExprFactory;
import org.openrdf.sail.generaldb.algebra.factories.GeneralDBHashExprFactory;
import org.openrdf.sail.generaldb.algebra.factories.GeneralDBLabelExprFactory;
import org.openrdf.sail.generaldb.algebra.factories.GeneralDBLanguageExprFactory;
import org.openrdf.sail.generaldb.algebra.factories.GeneralDBNumericExprFactory;
import org.openrdf.sail.generaldb.algebra.factories.GeneralDBSqlExprFactory;
import org.openrdf.sail.generaldb.algebra.factories.GeneralDBTimeExprFactory;
import org.openrdf.sail.generaldb.algebra.factories.GeneralDBURIExprFactory;
import org.openrdf.sail.generaldb.algebra.factories.GeneralDBZonedExprFactory;
import org.openrdf.sail.generaldb.managers.TransTableManager;
import org.openrdf.sail.generaldb.schema.IdSequence;

/**
 * Initialises the {@link GeneralDBSelectQueryOptimizer} with the SQL expression
 * factories.
 * 
 * @author James Leigh
 * 
 */
public class GeneralDBSelectQueryOptimizerFactory {

	private GeneralDBValueFactory vf;

	private TransTableManager tables;

	private IdSequence ids;

	public void setValueFactory(GeneralDBValueFactory vf) {
		this.vf = vf;
	}

	public void setTransTableManager(TransTableManager tables) {
		this.tables = tables;
	}

	public void setIdSequence(IdSequence ids) {
		this.ids = ids;
	}

	public GeneralDBSelectQueryOptimizer createRdbmsFilterOptimizer() {
		GeneralDBLabelExprFactory label = new GeneralDBLabelExprFactory();
		GeneralDBBooleanExprFactory bool = createBooleanExprFactory();
		GeneralDBURIExprFactory uri = new GeneralDBURIExprFactory();
		GeneralDBSqlExprFactory sql = new GeneralDBSqlExprFactory();
		GeneralDBDatatypeExprFactory datatype = new GeneralDBDatatypeExprFactory();
		GeneralDBLanguageExprFactory language = new GeneralDBLanguageExprFactory();
		sql.setBNodeExprFactory(new GeneralDBBNodeExprFactory());
		sql.setBooleanExprFactory(bool);
		sql.setDatatypeExprFactory(datatype);
		sql.setLabelExprFactory(label);
		sql.setLanguageExprFactory(language);
		sql.setURIExprFactory(uri);
		
		sql.setNumericExprFactory(new GeneralDBNumericExprFactory());
		sql.setTimeExprFactory(new GeneralDBTimeExprFactory());
		sql.setZonedExprFactory(new GeneralDBZonedExprFactory(ids));
		sql.setHashExprFactory(new GeneralDBHashExprFactory(vf));
		
		label.setSqlExprFactory(sql);
		uri.setSqlExprFactory(sql);
		bool.setSqlExprFactory(sql);
		GeneralDBSelectQueryOptimizer optimizer = new GeneralDBSelectQueryOptimizer();
		optimizer.setSqlExprFactory(sql);
		optimizer.setValueFactory(vf);
		optimizer.setTransTableManager(tables);
		optimizer.setIdSequence(ids);
		return optimizer;
	}

	protected GeneralDBBooleanExprFactory createBooleanExprFactory() {
		return new GeneralDBBooleanExprFactory();
	}
}
