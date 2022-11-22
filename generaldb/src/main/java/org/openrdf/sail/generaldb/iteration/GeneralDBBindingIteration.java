/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.generaldb.iteration;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;

import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.algebra.evaluation.QueryBindingSet;
import org.openrdf.query.algebra.evaluation.function.spatial.WKTHelper;
import org.openrdf.sail.generaldb.GeneralDBSpatialFuncInfo;
import org.openrdf.sail.generaldb.GeneralDBValueFactory;
import org.openrdf.sail.generaldb.algebra.GeneralDBColumnVar;
import org.openrdf.sail.generaldb.schema.IdSequence;
import org.openrdf.sail.generaldb.schema.ValueTable;
import org.openrdf.sail.rdbms.exceptions.RdbmsQueryEvaluationException;
import org.openrdf.sail.rdbms.iteration.base.RdbmIterationBase;
import org.openrdf.sail.rdbms.model.RdbmsResource;
import org.openrdf.sail.rdbms.model.RdbmsValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.earthobservatory.constants.GeoConstants;

/**
 * Converts a {@link ResultSet} into a {@link BindingSet} in an iteration.
 * 
 * @author Charalampos Nikolaou <charnik@di.uoa.gr.
 * @author Manos Karpathiotakis <mk@di.uoa.gr>
 */
public abstract class GeneralDBBindingIteration extends RdbmIterationBase<BindingSet, QueryEvaluationException> {

	private static Logger logger = LoggerFactory.getLogger(org.openrdf.sail.generaldb.iteration.GeneralDBBindingIteration.class);
	
	protected BindingSet bindings;

	protected Collection<GeneralDBColumnVar> projections;

	protected GeneralDBValueFactory vf;

	protected IdSequence ids;

	protected HashMap<String, Integer> geoNames = new HashMap<String, Integer>();

	//protected HashMap<String, Integer> sp_ConstructIndexesAndNames = new HashMap<String, Integer>();
	protected HashMap<GeneralDBSpatialFuncInfo, Integer> sp_ConstructIndexesAndNames = new HashMap<GeneralDBSpatialFuncInfo, Integer>();

	//	protected HashMap<String, Integer> sp_MetricIndexesAndNames = new HashMap<String, Integer>();
	//	
	//	protected HashMap<String, Integer> sp_IntPropertiesIndexesAndNames = new HashMap<String, Integer>();
	//	
	//	protected HashMap<String, Integer> sp_BoolPropertiesIndexesAndNames = new HashMap<String, Integer>();
	//	
	//	protected HashMap<String, Integer> sp_StringPropertiesIndexesAndNames = new HashMap<String, Integer>();

	//

	public GeneralDBBindingIteration(PreparedStatement stmt)
	throws SQLException
	{
		super(stmt);
	}

	public HashMap<GeneralDBSpatialFuncInfo, Integer> getConstructIndexesAndNames() {
		return sp_ConstructIndexesAndNames;
	}

	public void setConstructIndexesAndNames(HashMap<GeneralDBSpatialFuncInfo, Integer> indexesAndNames) {
		this.sp_ConstructIndexesAndNames = indexesAndNames;
	}

	public HashMap<String, Integer> getGeoNames() {
		return geoNames;
	}

	public void setGeoNames(HashMap<String, Integer> geoNames) {
		this.geoNames = geoNames;
	}

	public void setBindings(BindingSet bindings) {
		this.bindings = bindings;
	}

	public void setProjections(Collection<GeneralDBColumnVar> proj) {
		this.projections = proj;
	}

	public void setValueFactory(GeneralDBValueFactory vf) {
		this.vf = vf;
	}

	public void setIdSequence(IdSequence ids) {
		this.ids = ids;
	}

	@Override
	protected BindingSet convert(ResultSet rs)
	throws SQLException
	{
		/// debug
		/*for(int i=1; i<12;i++) {
			Object o = rs.getObject(i);
			if (o instanceof byte[] ) {
				byte[] label = rs.getBytes(i);
				int srid = rs.getInt(i + 1);
				GeneralDBPolyhedron g = vf.getRdbmsPolyhedron(114, StrabonPolyhedron.ogcGeometry, label, srid);
				System.out.println(i+": "+g.getPolyhedronStringRep());
			} else if (o instanceof Blob ) {
				Blob labelBlob = rs.getBlob(i); 
				byte[] label = labelBlob.getBytes((long)1, (int)labelBlob.length());
				int srid = rs.getInt(i + 1);
				GeneralDBPolyhedron g = vf.getRdbmsPolyhedron(114, StrabonPolyhedron.ogcGeometry, label, srid);
				System.out.println(i+": "+g.getPolyhedronStringRep());
			}  
			else 
				System.out.println(i+": "+rs.getObject(i));
		}*/
		///

		QueryBindingSet result = new QueryBindingSet(bindings);
		for (GeneralDBColumnVar var : projections) {
			String name = var.getName();
			if (var != null && !result.hasBinding(name)) {
				Value value = var.getValue();
				if (value == null) {
					if(!var.isSpatial())
					{
						//default action
						value = createValue(rs, var.getIndex() + 1);
					}
					else//geoVar encountered
					{
						value = createGeoValue(rs, var.getIndex() + 1);
					}
				}
				if (value != null) {
					result.addBinding(var.getName(), value);
				}
			}
		}

		for(GeneralDBSpatialFuncInfo construct : sp_ConstructIndexesAndNames.keySet())
		{
			Value value = null;
			switch(construct.getType())
			{
				case BOOLEAN: 
					value = createBooleanGeoValueForSelectConstructs(rs, sp_ConstructIndexesAndNames.get(construct));
					break;
				case DOUBLE: 
					value = createDoubleGeoValueForSelectConstructs(rs, sp_ConstructIndexesAndNames.get(construct));
					break;
				case INTEGER: 
					value = createIntegerGeoValueForSelectConstructs(rs, sp_ConstructIndexesAndNames.get(construct));
					break;
				case STRING:
					value = createStringGeoValueForSelectConstructs(rs, sp_ConstructIndexesAndNames.get(construct));
					break;
				case WKT: 
					value = createWellKnownTextGeoValueForSelectConstructs(rs, sp_ConstructIndexesAndNames.get(construct));
					break;
				case WKTLITERAL: 
					value = createWellKnownTextLiteralGeoValueForSelectConstructs(rs, sp_ConstructIndexesAndNames.get(construct));
					break;
				case URI:
					value = createURIGeoValueForSelectConstructs(rs, sp_ConstructIndexesAndNames.get(construct), construct.isSRIDFunc());				
					break;
				default:
					logger.error("[GeneralDBBindingIteration] Unknown result type for function.");
					break;
			}
			
			if (value != null) {
				result.addBinding(construct.getFieldName(), value);
			}
		}

		return result;
	}

	@Override
	protected QueryEvaluationException convertSQLException(SQLException e) {
		return new RdbmsQueryEvaluationException(e);
	}

	protected RdbmsResource createResource(ResultSet rs, int index)
	throws SQLException
	{
		Number id = ids.idOf(rs.getLong(index));
		if (id.longValue() == ValueTable.NIL_ID)
			return null;
		return vf.getRdbmsResource(id, rs.getString(index + 1));
	}

	protected RdbmsValue createValue(ResultSet rs, int index)
	throws SQLException
	{
		Number id = ids.idOf(rs.getLong(index));
		if (ids.isLiteral(id)) {
			String label = rs.getString(index + 1);
			String language = rs.getString(index + 2);
			String datatype = rs.getString(index + 3);
			return vf.getRdbmsLiteral(id, label, language, datatype);
		}
		return createResource(rs, index);
	}

	/**
	 * Creates a geospatial value from the given result set and index position.
	 * When projecting on a geospatial value, we get also its SRID, and its 
	 * datatype, so that we are able to assign that datatype to the new value. 
	 * 
	 * @param rs the current result set over which we are iterating
	 * @param index the index position to start reading from
	 *        index + 1: geospatial value (in binary)
	 *        index + 2: SRID
	 *        index + 3: datatype
	 * @return
	 * @throws SQLException
	 */
	protected abstract RdbmsValue createGeoValue(ResultSet rs, int index)
	throws SQLException;
	
	protected abstract RdbmsValue createWellKnownTextGeoValueForSelectConstructs(ResultSet rs, int index) throws SQLException;
	
	protected abstract RdbmsValue createWellKnownTextLiteralGeoValueForSelectConstructs(ResultSet rs, int index) throws SQLException;

	protected RdbmsValue createDoubleGeoValueForSelectConstructs(ResultSet rs, int index) throws SQLException
	{
		double potentialMetric;
		//case of metrics
		if(rs.getArray(index+1) != null)
		{
			potentialMetric = rs.getFloat(index + 1);

			return vf.asRdbmsLiteral(vf.createLiteral(potentialMetric));
		}
		return null;

	}

	protected RdbmsValue createIntegerGeoValueForSelectConstructs(ResultSet rs, int index)
	throws SQLException
	{
		//case of integer spatial properties
		int potentialMetric = rs.getInt(index + 1);

		return vf.asRdbmsLiteral(vf.createLiteral(potentialMetric));
	}

	protected RdbmsValue createBooleanGeoValueForSelectConstructs(ResultSet rs, int index)
	throws SQLException
	{
		boolean spProperty = rs.getBoolean(index + 1);

		return vf.asRdbmsLiteral(vf.createLiteral(spProperty));
	}

	protected RdbmsValue createStringGeoValueForSelectConstructs(ResultSet rs, int index)
	throws SQLException
	{
		String spProperty = rs.getString(index + 1);

		return vf.asRdbmsLiteral(vf.createLiteral(spProperty));

	}
	
	protected RdbmsResource createURIGeoValueForSelectConstructs(ResultSet rs, int index, boolean sridTransform)
	throws SQLException
	{
		String uri = null;
		
		if (sridTransform) {
			// we have to differentiate here for geoSPARQL's getSRID function, since we need to transform
			// the result to a URI
			// this is called for GeoSPARQL's getSRID, thus the column would be of type Integer
			int srid = rs.getInt(index + 1);
			
			// NOTICE however, that in case of EPSG:4326 and wktLiterals it would be better to
			// return CRS84. We have already brought that datatype earlier in the expression, so
			// we will try to locate it
			if (srid == GeoConstants.EPSG4326_SRID) {
				// get the alias name for this column
				ResultSetMetaData meta = rs.getMetaData();
				String aliasSRID = meta.getColumnName(index + 1);
	
				// get the index of the column containing the expression for the reference geometry
				Integer indexOfGeometry = geoNames.get(aliasSRID.replace("_srid", ""));
				if (indexOfGeometry != null) { 
					// index + 2 would have the datatype
					String datatype = rs.getString(indexOfGeometry + 2);
					//System.out.println(datatype);
					
					if (GeoConstants.WKTLITERAL.equals(datatype)) {
						uri = GeoConstants.CRS84_URI;
					}
					
				} else { // we didn't manage to locate the datatype column, so this is probably
						 // a constant for which it is not possible to determine its datatype
						 // since this function is geof:getSRID, we assume a geo:wktLiteral datatype, sorry
					uri = GeoConstants.CRS84_URI;
					
				}
			}
			
			if (uri == null) { // default behavior if we fail to locate or is not
							   // a wktLiteral
				uri = WKTHelper.getEPSGURI_forSRID(srid);
			}
			
		} else { // we get this as a string first, and then we shall construct the URI
			uri = rs.getString(index + 1);
		}
		
		return vf.asRdbmsURI(vf.createURI(uri));
	}
}
