/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.postgis.iteration;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.openrdf.query.BindingSet;
import org.openrdf.sail.generaldb.iteration.GeneralDBBindingIteration;
import org.openrdf.sail.rdbms.model.RdbmsValue;

import eu.earthobservatory.constants.GeoConstants;

/**
 * Converts a {@link ResultSet} into a {@link BindingSet} in an iteration.
 * 
 * @author Manos Karpathiotakis <mk@di.uoa.gr>
 * 
 */
public class PostGISBindingIteration extends GeneralDBBindingIteration {

	public PostGISBindingIteration(PreparedStatement stmt)
	throws SQLException
	{
		super(stmt);
	}

	@Override
	protected RdbmsValue createGeoValue(ResultSet rs, int index)
	throws SQLException
	{
		Number id = ids.idOf(rs.getLong(index));
		if (ids.isLiteral(id))
		{
			byte[] label = rs.getBytes(index + 1);
			int srid = rs.getInt(index + 2);
			String datatype = rs.getString(index + 3);
			return vf.getRdbmsPolyhedron(id, datatype, label, srid);
		}

		return createResource(rs, index);
	}
	
	@Override
	protected RdbmsValue createWellKnownTextGeoValueForSelectConstructs(ResultSet rs, int index) throws SQLException
	{		
		//Case of spatial constructs
		byte[] label = rs.getBytes(index + 1);
		int srid = rs.getInt(index + 2);
		
		return vf.getRdbmsPolyhedron(GeoConstants.WKT, label, srid);
	}
	
	@Override
	protected RdbmsValue createWellKnownTextLiteralGeoValueForSelectConstructs(ResultSet rs, int index) throws SQLException
	{		
		//Case of spatial constructs
		byte[] label = rs.getBytes(index + 1);
		int srid = rs.getInt(index + 2);
		return vf.getRdbmsPolyhedron(GeoConstants.WKTLITERAL, label, srid);
	}
}