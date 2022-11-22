/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.generaldb;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import info.aduna.concurrent.locks.Lock;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.sail.SailException;
import org.openrdf.sail.helpers.DefaultSailChangedEvent;
import org.openrdf.sail.generaldb.evaluation.GeneralDBQueryBuilderFactory;
import org.openrdf.sail.generaldb.evaluation.GeneralDBSqlBracketBuilder;
import org.openrdf.sail.generaldb.evaluation.GeneralDBSqlJoinBuilder;
import org.openrdf.sail.generaldb.evaluation.GeneralDBSqlQueryBuilder;
import org.openrdf.sail.generaldb.iteration.EmptyGeneralDBResourceIteration;
import org.openrdf.sail.generaldb.iteration.EmptyGeneralDBStatementIteration;
import org.openrdf.sail.generaldb.iteration.GeneralDBResourceIteration;
import org.openrdf.sail.generaldb.iteration.GeneralDBStatementIteration;
import org.openrdf.sail.rdbms.exceptions.RdbmsException;
import org.openrdf.sail.generaldb.managers.TransTableManager;
import org.openrdf.sail.generaldb.managers.TripleManager;
import org.openrdf.sail.rdbms.model.RdbmsResource;
import org.openrdf.sail.rdbms.model.RdbmsStatement;
import org.openrdf.sail.rdbms.model.RdbmsURI;
import org.openrdf.sail.rdbms.model.RdbmsValue;
import org.openrdf.sail.generaldb.schema.BNodeTable;
import org.openrdf.sail.generaldb.schema.IdSequence;
import org.openrdf.sail.generaldb.schema.LiteralTable;
import org.openrdf.sail.generaldb.schema.URITable;
import org.openrdf.sail.generaldb.schema.ValueTable;

/**
 * Facade to {@link GeneralDBTransTableManager}, {@link URITable}, {@link BNodeTable} and
 * {@link LiteralTable} for adding, removing, and retrieving statements from the
 * database.
 * 
 * @author Manos Karpathiotatis <mk@di.uoa.gr>
 * @author James Leigh
 */
public abstract class GeneralDBTripleRepository {

	public static int STMT_BUFFER = 32;

	protected Connection conn;

	protected GeneralDBValueFactory vf;

	protected TransTableManager statements;

	protected GeneralDBQueryBuilderFactory factory;

	protected BNodeTable bnodes;

	protected URITable uris;

	protected LiteralTable literals;

	protected Lock readLock;

	protected DefaultSailChangedEvent sailChangedEvent;

	protected TripleManager manager;

	protected LinkedList<RdbmsStatement> queue = new LinkedList<RdbmsStatement>();

	protected IdSequence ids;

	public Connection getConnection() {
		return conn;
	}

	public void setConnection(Connection conn) {
		this.conn = conn;
	}

	public void setIdSequence(IdSequence ids) {
		this.ids = ids;
	}

	public GeneralDBValueFactory getValueFactory() {
		return vf;
	}

	public void setValueFactory(GeneralDBValueFactory vf) {
		this.vf = vf;
	}

	public DefaultSailChangedEvent getSailChangedEvent() {
		return sailChangedEvent;
	}

	public void setSailChangedEvent(DefaultSailChangedEvent sailChangedEvent) {
		this.sailChangedEvent = sailChangedEvent;
	}

	public void setQueryBuilderFactory(GeneralDBQueryBuilderFactory factory) {
		this.factory = factory;
	}

	public void setBNodeTable(BNodeTable bnodes) {
		this.bnodes = bnodes;
	}

	public void setURITable(URITable uris) {
		this.uris = uris;
	}

	public void setLiteralTable(LiteralTable literals) {
		this.literals = literals;
	}

	public void setTransaction(TransTableManager temporary) {
		this.statements = temporary;
	}

	public void setTripleManager(TripleManager tripleManager) {
		this.manager = tripleManager;
	}

	public void flush()
		throws RdbmsException
	{
		try {
			synchronized (queue) {
				while (!queue.isEmpty()) {
					insert(queue.removeFirst());
				}
			}
			vf.flush();
			manager.flush();
		}
		catch (SQLException e) {
			throw new RdbmsException(e);
		}
		catch (InterruptedException e) {
			throw new RdbmsException(e);
		}
	}

	public synchronized void begin()
		throws SQLException
	{
		conn.setAutoCommit(false);
	}

	abstract public void close()
		throws SQLException;

	public synchronized void commit()
		throws SQLException, RdbmsException, InterruptedException 
	{
	
	}

	public void rollback()
		throws SQLException, SailException
	{
		synchronized (queue) {
			queue.clear();
		}
		manager.clear();
		if (!conn.getAutoCommit()) {
			conn.rollback();
			conn.setAutoCommit(true);
		}
		releaseLock();
	}

	@Override
	protected void finalize()
		throws Throwable
	{
		releaseLock();
		super.finalize();
	}

	public void add(RdbmsStatement st)
		throws SailException, SQLException, InterruptedException
	{
		acquireLock();
		synchronized (queue) {
			queue.add(st);
			if (queue.size() > getMaxQueueSize()) {
				insert(queue.removeFirst());
			}
		}
	}

	public GeneralDBStatementIteration find(Resource subj, URI pred, Value obj, Resource... ctxs)
		throws RdbmsException
	{
		try {
			RdbmsResource s = vf.asRdbmsResource(subj);
			RdbmsURI p = vf.asRdbmsURI(pred);
			RdbmsValue o = vf.asRdbmsValue(obj);
			RdbmsResource[] c = vf.asRdbmsResource(ctxs);
			flush();
			GeneralDBSqlQueryBuilder query = buildSelectQuery(s, p, o, c);
			if (query == null)
				return new EmptyGeneralDBStatementIteration();
			List<?> parameters = query.findParameters(new ArrayList<Object>());
			PreparedStatement stmt = conn.prepareStatement(query.toString());
			try {
				for (int i = 0, n = parameters.size(); i < n; i++) {
					stmt.setObject(i + 1, parameters.get(i));
				}
				return new GeneralDBStatementIteration(vf, stmt, ids);
			}
			catch (SQLException e) {
				stmt.close();
				throw e;
			}
		}
		catch (SQLException e) {
			throw new RdbmsException(e);
		}

	}

	public GeneralDBResourceIteration findContexts()
		throws SQLException, RdbmsException
	{
		flush();
		String qry = buildContextQuery();
		if (qry == null)
			return new EmptyGeneralDBResourceIteration();
		PreparedStatement stmt = conn.prepareStatement(qry);
		try {
			return new GeneralDBResourceIteration(vf, stmt);
		}
		catch (SQLException e) {
			stmt.close();
			throw e;
		}
	}

	public boolean isClosed()
		throws SQLException
	{
		return conn.isClosed();
	}

	public int remove(Resource subj, URI pred, Value obj, Resource... ctxs)
		throws RdbmsException
	{
		RdbmsResource s = vf.asRdbmsResource(subj);
		RdbmsURI p = vf.asRdbmsURI(pred);
		RdbmsValue o = vf.asRdbmsValue(obj);
		RdbmsResource[] c = vf.asRdbmsResource(ctxs);
		flush();
		try {
			Collection<Number> predicates;
			if (p == null) {
				predicates = statements.getPredicateIds();
			}
			else {
				predicates = Collections.singleton(vf.getInternalId(p));
			}
			int total = 0;
			for (Number id : predicates) {
				String tableName = statements.findTableName(id);
				if (!statements.isPredColumnPresent(id)) {
					p = null;
				}
				String query = buildDeleteQuery(tableName, s, p, o, c);
				PreparedStatement stmt = conn.prepareStatement(query);
				try {
					setSelectQuery(stmt, s, p, o, c);
					int count = stmt.executeUpdate();
//					System.err.println("statement: "+stmt.toString());
					statements.removed(id, count);
					total += count;
				}
				finally {
					stmt.close();
				}
			}
			if (total > 0) {
				sailChangedEvent.setStatementsRemoved(true);
			}
			return total;
		}
		catch (SQLException e) {
			throw new RdbmsException(e);
		}
	}

	public long size(RdbmsResource... ctxs)
		throws SQLException, SailException
	{
		flush();
		String qry = buildCountQuery(ctxs);
		if (qry == null)
			return 0;
		PreparedStatement stmt = conn.prepareStatement(qry);
		try {
			setCountQuery(stmt, ctxs);
			ResultSet rs = stmt.executeQuery();
			try {
				if (rs.next())
					return rs.getLong(1);
				throw new RdbmsException("Could not determine size");
			}
			finally {
				rs.close();
			}
		}
		finally {
			stmt.close();
		}
	}

	protected int getMaxQueueSize() {
		return STMT_BUFFER;
	}

	protected synchronized void acquireLock()
		throws InterruptedException
	{
		if (readLock == null) {
			readLock = vf.getIdReadLock();
		}
	}

	protected synchronized void releaseLock() {
		if (readLock != null) {
			readLock.release();
			readLock = null;
		}
	}

	protected String buildContextQuery()
		throws SQLException
	{
		if (statements.isEmpty())
			return null;
		String tableName = statements.getCombinedTableName();
		GeneralDBSqlQueryBuilder query = factory.createSqlQueryBuilder();
		query.select().column("t", "ctx");
		query.select().append("CASE WHEN MIN(u.value) IS NOT NULL THEN MIN(u.value) ELSE MIN(b.value) END");
		GeneralDBSqlJoinBuilder join = query.from(tableName, "t");
		join.leftjoin(bnodes.getName(), "b").on("id", "t.ctx");
		join.leftjoin(uris.getShortTableName(), "u").on("id", "t.ctx");
		GeneralDBSqlBracketBuilder open = query.filter().and().open();
		open.column("u", "value").isNotNull();
		open.or();
		open.column("b", "value").isNotNull();
		open.close();
		query.groupBy("t.ctx");
		return query.toString();
	}

	protected abstract String buildCountQuery(RdbmsResource... ctxs)
		throws SQLException;

	protected abstract String buildDeleteQuery(String tableName, RdbmsResource subj, RdbmsURI pred, RdbmsValue obj,
			RdbmsResource... ctxs)
		throws RdbmsException, SQLException;

	protected GeneralDBSqlQueryBuilder buildSelectQuery(RdbmsResource subj, RdbmsURI pred, RdbmsValue obj,
			RdbmsResource... ctxs)
		throws RdbmsException, SQLException
	{
		String tableName = statements.getTableName(vf.getInternalId(pred));
		GeneralDBSqlQueryBuilder query = factory.createSqlQueryBuilder();
		query.select().column("t", "ctx");
		query.select().append(
				"CASE WHEN cu.value IS NOT NULL THEN cu.value WHEN clu.value IS NOT NULL THEN clu.value ELSE cb.value END");
		query.select().column("t", "subj");
		query.select().append(
				"CASE WHEN su.value IS NOT NULL THEN su.value WHEN slu.value IS NOT NULL THEN slu.value ELSE sb.value END");
		query.select().column("pu", "id");
		query.select().column("pu", "value");
		query.select().column("t", "obj");
		query.select().append(
				"CASE WHEN ou.value IS NOT NULL THEN ou.value" + " WHEN olu.value IS NOT NULL THEN olu.value"
						+ " WHEN ob.value IS NOT NULL THEN ob.value"
						+ " WHEN ol.value IS NOT NULL THEN ol.value ELSE oll.value END");
		query.select().column("od", "value");
		query.select().column("og", "value");
		GeneralDBSqlJoinBuilder join;
		if (pred != null) {
			join = query.from(uris.getShortTableName(), "pu");
			// TODO what about long predicate URIs?
			join = join.join(tableName, "t");
		}
		else {
			join = query.from(tableName, "t");
		}
		if (pred == null) {
			join.join(uris.getShortTableName(), "pu").on("id", "t.pred");
		}
		join.leftjoin(uris.getShortTableName(), "cu").on("id", "t.ctx");
		join.leftjoin(uris.getLongTableName(), "clu").on("id", "t.ctx");
		join.leftjoin(bnodes.getName(), "cb").on("id", "t.ctx");
		join.leftjoin(uris.getShortTableName(), "su").on("id", "t.subj");
		join.leftjoin(uris.getLongTableName(), "slu").on("id", "t.subj");
		join.leftjoin(bnodes.getName(), "sb").on("id", "t.subj");
		join.leftjoin(uris.getShortTableName(), "ou").on("id", "t.obj");
		join.leftjoin(uris.getLongTableName(), "olu").on("id", "t.obj");
		join.leftjoin(bnodes.getName(), "ob").on("id", "t.obj");
		join.leftjoin(literals.getLabelTable().getName(), "ol").on("id", "t.obj");
		join.leftjoin(literals.getLongLabelTable().getName(), "oll").on("id", "t.obj");
		join.leftjoin(literals.getLanguageTable().getName(), "og").on("id", "t.obj");
		join.leftjoin(literals.getDatatypeTable().getName(), "od").on("id", "t.obj");
		if (ctxs != null && ctxs.length > 0) {
			Number[] ids = new Number[ctxs.length];
			for (int i = 0; i < ids.length; i++) {
				ids[i] = vf.getInternalId(ctxs[i]);
			}
			query.filter().and().columnIn("t", "ctx", ids);
		}
		if (subj != null) {
			Number id = vf.getInternalId(subj);
			query.filter().and().columnEquals("t", "subj", id);
		}
		if (pred != null) {
			Number id = vf.getInternalId(pred);
			query.filter().and().columnEquals("pu", "id", id);
			if (statements.isPredColumnPresent(id)) {
				query.filter().and().columnEquals("t", "pred", id);
			}
		}
		if (obj != null) {
			Number id = vf.getInternalId(obj);
			query.filter().and().columnEquals("t", "obj", id);
		}
		return query;
	}

	protected abstract String buildWhere(StringBuilder sb, RdbmsResource subj, RdbmsURI pred, RdbmsValue obj,
			RdbmsResource... ctxs);

	protected void insert(RdbmsStatement st)
		throws RdbmsException, SQLException, InterruptedException
	{
		Number ctx = vf.getInternalId(st.getContext());
		Number subj = vf.getInternalId(st.getSubject());
		Number pred = vf.getPredicateId(st.getPredicate());
		Number obj = vf.getInternalId(st.getObject());
		manager.insert(ctx, subj, pred, obj);
	}

	protected void setCountQuery(PreparedStatement stmt, RdbmsResource... ctxs)
		throws SQLException, RdbmsException
	{
		if (ctxs != null && ctxs.length > 0) {
			for (int i = 0; i < ctxs.length; i++) {
				stmt.setObject(i + 1, vf.getInternalId(ctxs[i]));
			}
		}
	}

	protected void setSelectQuery(PreparedStatement stmt, RdbmsResource subj, RdbmsURI pred, RdbmsValue obj,
			RdbmsResource... ctxs)
		throws SQLException, RdbmsException
	{
		int p = 0;
		if (ctxs != null && ctxs.length > 0) {
			for (int i = 0; i < ctxs.length; i++) {
				if (ctxs[i] == null) {
					stmt.setLong(++p, ValueTable.NIL_ID);
				}
				else {
					stmt.setObject(++p, vf.getInternalId(ctxs[i]));
				}
			}
		}
		if (subj != null) {
			stmt.setObject(++p, vf.getInternalId(subj));
		}
		if (pred != null) {
			stmt.setObject(++p, vf.getInternalId(pred));
		}
		if (obj != null) {
			stmt.setObject(++p, vf.getInternalId(obj));
		}
	}
	
	/**
	 * @author Manos Karpathiotatis <mk@di.uoa.gr>
	 * 
	 * @throws RdbmsException
	 */
	public void clearGeoValues() throws RdbmsException
	{
		try
		{
			String query = buildDeleteQuery("geo_values", null, null, null, (RdbmsResource[]) null);
			PreparedStatement stmt = conn.prepareStatement(query);
			try {
				setSelectQuery(stmt, null, null, null,(RdbmsResource[]) null);
				stmt.executeUpdate();
			}
			finally {
				stmt.close();
			}

		}
		catch (SQLException e) {
			throw new RdbmsException(e);
		}
	}

}
