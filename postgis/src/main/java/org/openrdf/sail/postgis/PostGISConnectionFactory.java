/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * 
 * Copyright (C) 2010, 2011, 2012, 2013 Pyravlos Team
 * 
 * http://www.strabon.di.uoa.gr/
 */
package org.openrdf.sail.postgis;

import static java.sql.Connection.TRANSACTION_READ_COMMITTED;
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
import org.openrdf.sail.generaldb.optimizers.GeneralDBQueryOptimizer;
import org.openrdf.sail.generaldb.optimizers.GeneralDBSelectQueryOptimizerFactory;
import org.openrdf.sail.helpers.DefaultSailChangedEvent;
import org.openrdf.sail.postgis.evaluation.PostGISEvaluationFactory;
import org.openrdf.sail.postgis.evaluation.PostGISQueryBuilderFactory;
import org.openrdf.sail.rdbms.util.DatabaseLockManager;
import org.openrdf.sail.rdbms.exceptions.RdbmsException;
import org.openrdf.sail.generaldb.managers.TransTableManager;
import org.openrdf.sail.generaldb.managers.TripleManager;
import org.openrdf.sail.rdbms.schema.TableFactory;
import org.openrdf.sail.generaldb.schema.ValueTableFactory;

/**
 * Responsible to initialise and wire all components together that will be
 * needed to satisfy any sail connection request.
 * 
 * @author Manos Karpathiotakis <mk@di.uoa.gr>
 */
public class PostGISConnectionFactory extends GeneralDBConnectionFactory {

	@Override
	protected TableFactory createTableFactory() {
		return new PostGISSqlTableFactory();
	}
	
	@Override
	protected ValueTableFactory createValueTableFactory() {
		return new PostGISSqlValueTableFactory();
	}
	
	@Override
	protected Lock createDatabaseLock()
		throws SailException
	{
		DatabaseLockManager manager;
		manager = new DatabaseLockManager(ds, user, password);
		if (manager.isDebugEnabled())
			return manager.tryLock();
		return manager.lockOrFail();
	}

	@Override
	protected GeneralDBQueryBuilderFactory createQueryBuilderFactory() {
		return new PostGISQueryBuilderFactory();
	}
	
	@Override
	public SailConnection createConnection()
	throws SailException
	{
		try {
			Connection db = getConnection();
			db.setAutoCommit(true);
			if (db.getTransactionIsolation() != TRANSACTION_READ_COMMITTED) {
				db.setTransactionIsolation(TRANSACTION_READ_COMMITTED);
			}
			TripleManager tripleManager = new TripleManager();
			GeneralDBTripleRepository s = new PostGISTripleRepository();
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
			GeneralDBEvaluationFactory efactory = new PostGISEvaluationFactory();
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
		return " ";
	}
}
