/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.generaldb;

import info.aduna.concurrent.locks.ExclusiveLockManager;
import info.aduna.concurrent.locks.Lock;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailException;
import org.openrdf.sail.generaldb.evaluation.GeneralDBQueryBuilderFactory;
import org.openrdf.sail.generaldb.managers.BNodeManager;
import org.openrdf.sail.generaldb.managers.HashManager;
import org.openrdf.sail.generaldb.managers.LiteralManager;
import org.openrdf.sail.generaldb.managers.PredicateManager;
import org.openrdf.sail.generaldb.managers.TransTableManager;
import org.openrdf.sail.generaldb.managers.TripleTableManager;
import org.openrdf.sail.generaldb.managers.UriManager;
import org.openrdf.sail.generaldb.optimizers.GeneralDBQueryOptimizer;
import org.openrdf.sail.generaldb.optimizers.GeneralDBSelectQueryOptimizerFactory;
import org.openrdf.sail.generaldb.schema.BNodeTable;
import org.openrdf.sail.generaldb.schema.HashTable;
import org.openrdf.sail.generaldb.schema.IdSequence;
import org.openrdf.sail.generaldb.schema.IntegerIdSequence;
import org.openrdf.sail.generaldb.schema.LiteralTable;
import org.openrdf.sail.generaldb.schema.LongIdSequence;
import org.openrdf.sail.generaldb.schema.URITable;
import org.openrdf.sail.generaldb.schema.ValueTableFactory;
import org.openrdf.sail.rdbms.exceptions.RdbmsException;
import org.openrdf.sail.rdbms.managers.NamespaceManager;
import org.openrdf.sail.rdbms.schema.NamespacesTable;
import org.openrdf.sail.rdbms.schema.TableFactory;
import org.openrdf.sail.rdbms.util.Tracer;

/**
 * Responsible to initialise and wire all components together that will be
 * needed to satisfy any sail connection request.
 * 
 * @author James Leigh
 */
public abstract class GeneralDBConnectionFactory {

	protected GeneralDBStore sail;

	protected DataSource ds;

	protected String user;

	protected String password;

	protected Connection resourceInserts;

	protected Connection literalInserts;

	protected Connection hashLookups;

	protected Connection nsAndTableIndexes;

	protected NamespaceManager namespaces;

	protected TripleTableManager tripleTableManager;

	protected HashManager hashManager;

	protected GeneralDBValueFactory vf;

	protected UriManager uriManager;

	protected BNodeManager bnodeManager;

	protected LiteralManager literalManager;

	protected PredicateManager predicateManager;

	protected int maxTripleTables;

	protected boolean triplesIndexed = true;

	protected boolean sequenced;

	protected HashTable hashTable;

	protected URITable uriTable;

	protected BNodeTable bnodeTable;

	protected LiteralTable literalTable;

	protected IdSequence ids;

	protected final ExclusiveLockManager lock = new ExclusiveLockManager();

	protected Lock databaseLock;

	public void setSail(GeneralDBStore sail) {
		this.sail = sail;
	}

	public DataSource getDataSource() {
		return ds;
	}

	public void setDataSource(DataSource ds) {
		if (Tracer.isTraceEnabled()) {
			this.ds = Tracer.traceDataSource(ds);
		} else {
			this.ds = ds;
		}
	}

	public void setDataSource(DataSource ds, String user, String password) {
		setDataSource(ds);
		this.user = user;
		this.password = password;
	}

	public int getMaxNumberOfTripleTables() {
		return maxTripleTables;
	}

	public void setMaxNumberOfTripleTables(int max) {
		maxTripleTables = max;
	}

	public boolean isSequenced() {
		return sequenced || hashManager != null;
	}

	public void setSequenced(boolean useSequence) {
		this.sequenced = useSequence;
	}

	public boolean isTriplesIndexed() {
		return triplesIndexed;
	}

	public void setTriplesIndexed(boolean triplesIndexed)
		throws SailException
	{
		this.triplesIndexed = triplesIndexed;
		if (tripleTableManager != null) {
			try {
				if (triplesIndexed) {
					tripleTableManager.createTripleIndexes();
				} else {
					tripleTableManager.dropTripleIndexes();
				}
			} catch (SQLException e) {
				throw new RdbmsException(e);
			}
		}
	}

	public GeneralDBValueFactory getValueFactory() {
		return vf;
	}

	public void init()
		throws Exception
	{
		databaseLock = createDatabaseLock();
		try {
			nsAndTableIndexes = getConnection();
			resourceInserts = getConnection();
			literalInserts = getConnection();
			nsAndTableIndexes.setAutoCommit(true);
			resourceInserts.setAutoCommit(true);
			literalInserts.setAutoCommit(true);
			bnodeManager = new BNodeManager();
			uriManager = new UriManager();
			literalManager = new LiteralManager();
			ValueTableFactory tables = createValueTableFactory();
			tables.setSequenced(sequenced);
			
			if (sequenced) {
				ids = new IntegerIdSequence();
				tables.setIdSequence(ids);
				hashLookups = getConnection();
				hashLookups.setAutoCommit(true);
				hashManager = new HashManager();
				hashTable = tables.createHashTable(hashLookups, hashManager.getQueue());
				ids.setHashTable(hashTable);
				ids.init();
				hashManager.setHashTable(hashTable);
				hashManager.setBNodeManager(bnodeManager);
				hashManager.setLiteralManager(literalManager);
				hashManager.setUriManager(uriManager);
				hashManager.setIdSequence(ids);
				hashManager.init();
				
			} else {
				ids = new LongIdSequence();
				ids.init();
				tables.setIdSequence(ids);
				
			}
			
			namespaces = new NamespaceManager();
			namespaces.setConnection(resourceInserts);
			NamespacesTable nsTable = tables.createNamespacesTable(nsAndTableIndexes);
			nsTable.initialize();
			namespaces.setNamespacesTable(nsTable);
			namespaces.initialize();
			bnodeManager.setHashManager(hashManager);
			bnodeManager.setIdSequence(ids);
			uriManager.setHashManager(hashManager);
			uriManager.setIdSequence(ids);
			bnodeTable = tables.createBNodeTable(resourceInserts, bnodeManager.getQueue());
			uriTable = tables.createURITable(resourceInserts, uriManager.getQueue());
			literalManager.setHashManager(hashManager);
			literalManager.setIdSequence(ids);
			literalTable = tables.createLiteralTable(literalInserts, literalManager.getQueue());
			literalTable.setIdSequence(ids);
			vf = new GeneralDBValueFactory();
			vf.setDelegate(ValueFactoryImpl.getInstance());
			vf.setIdSequence(ids);
			uriManager.setUriTable(uriTable);
			uriManager.init();
			predicateManager = new PredicateManager();
			predicateManager.setUriManager(uriManager);
			tripleTableManager = new TripleTableManager(tables);
			tripleTableManager.setConnection(nsAndTableIndexes);
			tripleTableManager.setIdSequence(ids);
			tripleTableManager.setBNodeManager(bnodeManager);
			tripleTableManager.setUriManager(uriManager);
			tripleTableManager.setLiteralManager(literalManager);
			tripleTableManager.setHashManager(hashManager);
			tripleTableManager.setPredicateManager(predicateManager);
			tripleTableManager.setMaxNumberOfTripleTables(maxTripleTables);
			tripleTableManager.setIndexingTriples(triplesIndexed);
			tripleTableManager.initialize();
			
			if (triplesIndexed) {
				tripleTableManager.createTripleIndexes();
				
			} else {
				tripleTableManager.dropTripleIndexes();
				
			}
			
			bnodeManager.setTable(bnodeTable);
			bnodeManager.init();
			vf.setBNodeManager(bnodeManager);
			vf.setURIManager(uriManager);
			literalManager.setTable(literalTable);
			literalManager.init();
			vf.setLiteralManager(literalManager);
			vf.setPredicateManager(predicateManager);
			
		} catch (SQLException e) {
			throw new RdbmsException(e);
		}
	}

	public boolean isWritable() throws SailException {
		try {
			return !nsAndTableIndexes.isReadOnly();
		} catch (SQLException e) {
			throw new RdbmsException(e);
		}
	}

	public abstract SailConnection createConnection() throws SailException;

	public void shutDown() throws SailException {
		try {
			if (tripleTableManager != null) {
				tripleTableManager.close();
			}
			if (uriManager != null) {
				uriManager.close();
			}
			if (bnodeManager != null) {
				bnodeManager.close();
			}
			if (literalManager != null) {
				literalManager.close();
			}
			if (hashManager != null) {
				hashManager.close();
			}
			if (resourceInserts != null) {
				resourceInserts.close();
				resourceInserts = null;
			}
			if (literalInserts != null) {
				literalInserts.close();
				literalInserts = null;
			}
			if (hashLookups != null) {
				hashLookups.close();
				hashLookups = null;
			}
			if (nsAndTableIndexes != null) {
				nsAndTableIndexes.close();
				nsAndTableIndexes = null;
			}
		} catch (SQLException e) {
			throw new RdbmsException(e);
		} finally {
			if (databaseLock != null) {
				databaseLock.release();
			}
		}
	}

	protected abstract Lock createDatabaseLock() throws SailException;

	protected abstract GeneralDBQueryBuilderFactory createQueryBuilderFactory();

	protected abstract ValueTableFactory createValueTableFactory();

	protected abstract TableFactory createTableFactory();

	protected TransTableManager createTransTableManager() {
		return new TransTableManager();
	}

	protected GeneralDBQueryOptimizer createOptimizer() {
		return new GeneralDBQueryOptimizer();
	}

	protected GeneralDBSelectQueryOptimizerFactory createSelectQueryOptimizerFactory() {
		return new GeneralDBSelectQueryOptimizerFactory();
	}

	/**
	 * FROM DUAL
	 * 
	 * @return from clause or empty string
	 */
	protected String getFromDummyTable() {
		return "";
	}

	protected Connection getConnection()
		throws SQLException 
	{
		Connection conn;
		if (user == null)
			// return ds.getConnection();
			conn = ds.getConnection();
		else
			// return ds.getConnection(user, password);
			conn = ds.getConnection(user, password);

		return conn;
//		return new net.sf.log4jdbc.ConnectionSpy(conn);
	}

}
