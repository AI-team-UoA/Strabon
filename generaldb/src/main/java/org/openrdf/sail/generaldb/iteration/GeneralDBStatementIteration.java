/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.generaldb.iteration;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.openrdf.sail.SailException;
import org.openrdf.sail.generaldb.GeneralDBValueFactory;
import org.openrdf.sail.rdbms.exceptions.RdbmsException;
import org.openrdf.sail.rdbms.iteration.base.RdbmIterationBase;
import org.openrdf.sail.rdbms.model.RdbmsResource;
import org.openrdf.sail.rdbms.model.RdbmsStatement;
import org.openrdf.sail.rdbms.model.RdbmsURI;
import org.openrdf.sail.rdbms.model.RdbmsValue;
import org.openrdf.sail.generaldb.schema.IdSequence;
import org.openrdf.sail.generaldb.schema.ValueTable;

/**
 * Converts a {@link ResultSet} into a {@link RdbmsStatement} in an iteration.
 * 
 * @author James Leigh
 * 
 */
public class GeneralDBStatementIteration extends RdbmIterationBase<RdbmsStatement, SailException> {

	private GeneralDBValueFactory vf;

	private IdSequence ids;

	public GeneralDBStatementIteration(GeneralDBValueFactory vf, PreparedStatement stmt, IdSequence ids)
		throws SQLException
	{
		super(stmt);
		this.vf = vf;
		this.ids = ids;
	}

	@Override
	protected RdbmsStatement convert(ResultSet rs)
		throws SQLException
	{
		RdbmsResource ctx = createResource(rs, 1);
		RdbmsResource subj = createResource(rs, 3);
		RdbmsURI pred = (RdbmsURI)createResource(rs, 5);
		RdbmsValue obj = createValue(rs, 7);
		return new RdbmsStatement(subj, pred, obj, ctx);
	}

	@Override
	protected RdbmsException convertSQLException(SQLException e) {
		return new RdbmsException(e);
	}

	private RdbmsResource createResource(ResultSet rs, int index)
		throws SQLException
	{
		Number id = ids.idOf(rs.getLong(index));
		if (id.longValue() == ValueTable.NIL_ID)
			return null;
		String stringValue = rs.getString(index + 1);
		return vf.getRdbmsResource(id, stringValue);
	}

	private RdbmsValue createValue(ResultSet rs, int index)
		throws SQLException
	{
		Number id = ids.idOf(rs.getLong(index));
		if (ids.isLiteral(id)) {
			String label = rs.getString(index + 1);
			String datatype = rs.getString(index + 2);
			String language = rs.getString(index + 3);
			return vf.getRdbmsLiteral(id, label, language, datatype);
		}
		return createResource(rs, index);
	}

}
