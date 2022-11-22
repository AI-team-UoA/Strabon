/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * 
 * Copyright (C) 2010, 2011, 2012, 2013 Pyravlos Team
 * 
 * http://www.strabon.di.uoa.gr/
 */
package org.openrdf.sail.monetdb;

import static java.sql.Connection.TRANSACTION_SERIALIZABLE;
import info.aduna.concurrent.locks.Lock;

import java.sql.Connection;
import java.sql.SQLException;

import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailException;
import org.openrdf.sail.generaldb.GeneralDBConnection;
import org.openrdf.sail.generaldb.GeneralDBConnectionFactory;
import org.openrdf.sail.generaldb.GeneralDBTripleRepository;
import org.openrdf.sail.generaldb.evaluation.GeneralDBEvaluationFactory;
import org.openrdf.sail.generaldb.evaluation.GeneralDBQueryBuilderFactory;
import org.openrdf.sail.generaldb.managers.TransTableManager;
import org.openrdf.sail.generaldb.managers.TripleManager;
import org.openrdf.sail.generaldb.optimizers.GeneralDBQueryOptimizer;
import org.openrdf.sail.generaldb.optimizers.GeneralDBSelectQueryOptimizerFactory;
import org.openrdf.sail.generaldb.schema.ValueTableFactory;
import org.openrdf.sail.helpers.DefaultSailChangedEvent;
import org.openrdf.sail.monetdb.evaluation.MonetDBEvaluationFactory;
import org.openrdf.sail.monetdb.evaluation.MonetDBQueryBuilderFactory;
import org.openrdf.sail.monetdb.util.MonetDBLockManager;
import org.openrdf.sail.rdbms.exceptions.RdbmsException;
import org.openrdf.sail.rdbms.schema.TableFactory;

/** 
 * @author George Garbis <ggarbis@di.uoa.gr>
 * @author Charalampos Nikolaou <charnik@di.uoa.gr>
 */
public class MonetDBConnectionFactory extends GeneralDBConnectionFactory {

	@Override
	protected TableFactory createTableFactory() {
		return new MonetDBSqlTableFactory();
	}
	
	@Override
	protected ValueTableFactory createValueTableFactory() {
		return new MonetDBSqlValueTableFactory();
	}
	
	@Override
	protected Lock createDatabaseLock()
		throws SailException
	{
		MonetDBLockManager manager;
		manager = new MonetDBLockManager(ds, user, password);
		if (manager.isDebugEnabled())
			return manager.tryLock();
		return manager.lockOrFail();
	}

	@Override
	protected GeneralDBQueryBuilderFactory createQueryBuilderFactory() {
		return new MonetDBQueryBuilderFactory();
	}
	
	@Override
	public SailConnection createConnection()
	throws SailException
	{
		try {
			
			/**
			 * //Connection db = getConnection();
			 * 
			 * It's crucial not to create another connection here (as was 
			 * the case previously) and share the <nsAndTableIndexes> connection. Here, 
			 * the connection is going to be used for creating predicate tables. On
			 * the other hand, <nsAndTableIndexes> connection is used for creating
			 * the temporary <transaction_statements> table and executing the inserts
			 * into it, and afterwards executing the inserts into the respective
			 * predicate table. Since <nsAndTableIndexes> connection has autocommit off
			 * and the connection here has autocommit on, then the tables created by the
			 * connection here are not seen by <nsAndTableIndexes> (recall that connection
			 * <nsAndTableIndexes> has already been started, so it precedes the newly connection
			 * created here). Thus, we were getting an exception like "INSERT INTO <predicate>: 
			 * no such table". 
			 * 
			 * The workaround is not to create a new connection for the creation of predicate tables
			 * and just share the <nsAndTableIndexes> connection.
			 */
			Connection db = nsAndTableIndexes;
			db.setAutoCommit(true);

			/**
			 * In contrast to Postgres, MonetDB (actually the jdbc implementation of MonetDB) allows 
			 * only serializable transactions. In every other case, an exception (or warning) is thrown.
			 * To prevent this, we explicitly set the isolation level to TRANSACTION_SERIALIZABLE.   
			 */
			db.setTransactionIsolation(TRANSACTION_SERIALIZABLE);
			
			TripleManager tripleManager = new TripleManager();
			GeneralDBTripleRepository s = new MonetDBTripleRepository();
			s.setTripleManager(tripleManager);
			s.setValueFactory(vf);
			s.setConnection(db);
			s.setBNodeTable(bnodeTable);
			s.setURITable(uriTable);
			s.setLiteralTable(literalTable);
			s.setIdSequence(ids);
			DefaultSailChangedEvent sailChangedEvent = new DefaultSailChangedEvent(sail);
			s.setSailChangedEvent(sailChangedEvent);
			TableFactory tables = createTableFactory();
			TransTableManager trans = createTransTableManager();
			trans.setIdSequence(ids);
			tripleManager.setTransTableManager(trans);
			trans.setBatchQueue(tripleManager.getQueue());
			trans.setSailChangedEvent(sailChangedEvent);
			trans.setConnection(db);
			trans.setTemporaryTableFactory(tables);
			trans.setStatementsTable(tripleTableManager);
			trans.setFromDummyTable(getFromDummyTable());
			trans.initialize();
			s.setTransaction(trans);
			GeneralDBQueryBuilderFactory bfactory = createQueryBuilderFactory();
			bfactory.setValueFactory(vf);
			bfactory.setUsingHashTable(hashManager != null);
			s.setQueryBuilderFactory(bfactory);
			
			GeneralDBConnection conn = new GeneralDBConnection(sail, s);
			conn.setNamespaces(namespaces);
			GeneralDBEvaluationFactory efactory = new MonetDBEvaluationFactory();
			efactory.setQueryBuilderFactory(bfactory);
			efactory.setRdbmsTripleRepository(s);
			efactory.setIdSequence(ids);
			conn.setRdbmsEvaluationFactory(efactory);
			GeneralDBQueryOptimizer optimizer = createOptimizer();
			GeneralDBSelectQueryOptimizerFactory selectOptimizerFactory = createSelectQueryOptimizerFactory();
			selectOptimizerFactory.setTransTableManager(trans);
			selectOptimizerFactory.setValueFactory(vf);
			selectOptimizerFactory.setIdSequence(ids);
			optimizer.setSelectQueryOptimizerFactory(selectOptimizerFactory);
			optimizer.setValueFactory(vf);
			optimizer.setBnodeTable(bnodeTable);
			optimizer.setUriTable(uriTable);
			optimizer.setLiteralTable(literalTable);
			optimizer.setHashTable(hashTable);
			conn.setRdbmsQueryOptimizer(optimizer);
			conn.setLockManager(lock);
			
			return conn;
		}
		catch (SQLException e) {
			throw new RdbmsException(e);
		}
	}

	
	/**
	 * FROM DUAL
	 * 
	 * @return from clause or empty string
	 */
	@Override
	protected String getFromDummyTable() {
		return " FROM sys.uri_values ";
	}
}
