/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.postgis;


import info.aduna.concurrent.locks.Lock;

import java.sql.SQLException;

import org.openrdf.sail.generaldb.GeneralDBTripleRepository;
import org.openrdf.sail.rdbms.exceptions.RdbmsException;
import org.openrdf.sail.rdbms.model.RdbmsResource;
import org.openrdf.sail.rdbms.model.RdbmsURI;
import org.openrdf.sail.rdbms.model.RdbmsValue;
import org.openrdf.sail.generaldb.schema.BNodeTable;
import org.openrdf.sail.generaldb.schema.LiteralTable;
import org.openrdf.sail.generaldb.schema.URITable;


/**
 * Facade to {@link GeneralDBTransTableManager}, {@link URITable}, {@link BNodeTable} and
 * {@link LiteralTable} for adding, removing, and retrieving statements from the
 * database.
 * 
 * @author James Leigh
 */
public class PostGISTripleRepository extends GeneralDBTripleRepository {

	@Override
	protected String buildDeleteQuery(String tableName, RdbmsResource subj, RdbmsURI pred, RdbmsValue obj,
			RdbmsResource... ctxs)
		throws RdbmsException, SQLException
	{
		StringBuilder sb = new StringBuilder();
		sb.append("DELETE FROM ").append(tableName);
		return buildWhere(sb, subj, pred, obj, ctxs);
	}
	
	@Override
	protected String buildWhere(StringBuilder sb, RdbmsResource subj, RdbmsURI pred, RdbmsValue obj,
			RdbmsResource... ctxs)
	{
		sb.append("\nWHERE (1=1)");
		if (ctxs != null && ctxs.length > 0) {
			sb.append(" AND (");
			for (int i = 0; i < ctxs.length; i++) {
				sb.append("ctx = ?");
				if (i < ctxs.length - 1) {
					sb.append(" OR ");
				}
			}
			sb.append(")");
		}
		if (subj != null) {
			sb.append(" AND subj = ? ");
		}
		if (pred != null) {
			sb.append(" AND pred = ? ");
		}
		if (obj != null) {
			sb.append(" AND obj = ?");
		}
		return sb.toString();
	}	
	
	@Override
	public synchronized void commit()
		throws SQLException, RdbmsException, InterruptedException
	{
		synchronized (queue) {
			while (!queue.isEmpty()) {
				insert(queue.removeFirst());
			}
		}
		manager.flush();
		conn.commit();
		conn.setAutoCommit(true);
		releaseLock();
		Lock writeLock = vf.tryIdWriteLock();
		try {
			vf.flush();
			statements.committed(writeLock != null);
		}
		finally {
			if (writeLock != null) {
				writeLock.release();
			}
		}
	}
	
	@Override
	protected String buildCountQuery(RdbmsResource... ctxs)
		throws SQLException
	{
		String tableName = statements.getCombinedTableName();
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT COUNT(*) FROM ");
		sb.append(tableName).append(" t");
		if (ctxs != null && ctxs.length > 0) {
			sb.append("\nWHERE ");
			for (int i = 0; i < ctxs.length; i++) {
				sb.append("t.ctx = ?");
				if (i < ctxs.length - 1) {
					sb.append(" OR ");
				}
			}
		}
		return sb.toString();
	}
	
	@Override
	public synchronized void close()
		throws SQLException
	{
		manager.close();
		if (!conn.getAutoCommit()) {
			conn.rollback();
		}
		conn.setAutoCommit(true);
		conn.close();
		releaseLock();
	}
}
