/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.generaldb.optimizers;

import java.util.ArrayList;
import java.util.List;

import org.openrdf.query.BindingSet;
import org.openrdf.query.Dataset;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.evaluation.QueryOptimizer;
import org.openrdf.sail.generaldb.algebra.GeneralDBBNodeColumn;
import org.openrdf.sail.generaldb.algebra.GeneralDBColumnVar;
import org.openrdf.sail.generaldb.algebra.GeneralDBDatatypeColumn;
import org.openrdf.sail.generaldb.algebra.GeneralDBDateTimeColumn;
import org.openrdf.sail.generaldb.algebra.GeneralDBHashColumn;
import org.openrdf.sail.generaldb.algebra.GeneralDBIdColumn;
import org.openrdf.sail.generaldb.algebra.GeneralDBJoinItem;
import org.openrdf.sail.generaldb.algebra.GeneralDBLabelColumn;
import org.openrdf.sail.generaldb.algebra.GeneralDBLanguageColumn;
import org.openrdf.sail.generaldb.algebra.GeneralDBLongLabelColumn;
import org.openrdf.sail.generaldb.algebra.GeneralDBLongURIColumn;
import org.openrdf.sail.generaldb.algebra.GeneralDBNumericColumn;
import org.openrdf.sail.generaldb.algebra.GeneralDBRefIdColumn;
import org.openrdf.sail.generaldb.algebra.GeneralDBSelectQuery;
import org.openrdf.sail.generaldb.algebra.GeneralDBSqlEq;
import org.openrdf.sail.generaldb.algebra.GeneralDBURIColumn;
import org.openrdf.sail.generaldb.algebra.GeneralDBUnionItem;
import org.openrdf.sail.generaldb.algebra.base.GeneralDBFromItem;
import org.openrdf.sail.generaldb.algebra.base.GeneralDBQueryModelVisitorBase;
import org.openrdf.sail.generaldb.algebra.base.GeneralDBSqlExpr;
import org.openrdf.sail.generaldb.schema.BNodeTable;
import org.openrdf.sail.generaldb.schema.HashTable;
import org.openrdf.sail.generaldb.schema.LiteralTable;
import org.openrdf.sail.generaldb.schema.URITable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Adds LEFT JOINs to the query for value tables.
 * 
 * @author Charalampos Nikolaou <charnik@di.uoa.gr>
 * @author Manos Karpathiotakis <mk@di.uoa.gr>
 * @author James Leigh
 * 
 */
public class GeneralDBValueJoinOptimizer extends GeneralDBQueryModelVisitorBase<RuntimeException> implements
QueryOptimizer
{

	private URITable uris;

	private BNodeTable bnodes;

	private LiteralTable literals;

	private HashTable hashes;

	private GeneralDBFromItem join;

	private GeneralDBFromItem parent;

	private List<GeneralDBFromItem> stack = new ArrayList<GeneralDBFromItem>();

	private GeneralDBSelectQuery query;

	/**
	 * 
	 * XXX Used to add the spatial filters only once on the query plan
	 * The spatial filters had to be added at the last occurrence of geo_values.
	 * We used data from this tree traversal and supplied it to a new optimizer
	 * that got the job done --> SpatialJoinOptimizer
	 */
	//private boolean spatiallyEnabled = false;
	//Will be used in the FOLLOWING - NEW - OPTIMIZER!
	private int geo_values_occurences = 0;


	private List<GeneralDBSqlExpr> exprToAppend = new ArrayList<GeneralDBSqlExpr>();


	public List<GeneralDBSqlExpr> getExprToAppend() {
		return exprToAppend;
	}


	public int getGeo_values_occurences() {
		return geo_values_occurences;
	}

	public void setUriTable(URITable uris) {
		this.uris = uris;
	}

	public void setBnodeTable(BNodeTable bnodes) {
		this.bnodes = bnodes;
	}

	public void setLiteralTable(LiteralTable literals) {
		this.literals = literals;
	}

	public void setHashTable(HashTable hashes) {
		this.hashes = hashes;
	}

	public void optimize(TupleExpr tupleExpr, Dataset dataset, BindingSet bindings) {
		join = null;
		tupleExpr.visit(this);
	}

	@Override
	public void meetFromItem(GeneralDBFromItem node) throws RuntimeException
	{
		GeneralDBFromItem top = parent;
		parent = join;
		join = node;
		super.meetFromItem(node);
		join = parent;
		parent = top;
	}

	@Override
	public void meet(GeneralDBUnionItem node) throws RuntimeException
	{
		stack.add(node);
		super.meet(node);
		stack.remove(stack.size() - 1);
	}

	@Override
	public void meet(GeneralDBSelectQuery node) throws RuntimeException
	{
		query = node;
		parent = node.getFrom();
		join = node.getFrom();
		super.meet(node);
		join = null;
		parent = null;
		query = null;
	}

	@Override
	public void meet(GeneralDBHashColumn node) throws RuntimeException
	{
		if (hashes == null || hashes.getName() == null) {
			super.meet(node);
		}
		else {
			GeneralDBColumnVar var = node.getRdbmsVar();
			String alias = "h" + getDBName(var);
			String tableName = hashes.getName();
			join(var, alias, tableName, false);
		}
	}

	@Override
	public void meet(GeneralDBBNodeColumn node) throws RuntimeException
	{
		GeneralDBColumnVar var = node.getRdbmsVar();
		String alias = "b" + getDBName(var);
		String tableName = bnodes.getName();
		join(var, alias, tableName);
	}

	@Override
	public void meet(GeneralDBDatatypeColumn node) throws RuntimeException
	{
		GeneralDBColumnVar var = node.getRdbmsVar();
		//XXX If spatial, I don't want this action to take place
		if(!var.isSpatial())
		{
			String alias = "d" + getDBName(var);
			String tableName = literals.getDatatypeTable().getName();
			join(var, alias, tableName);
		}
	}

	@Override
	public void meet(GeneralDBDateTimeColumn node) throws RuntimeException
	{
		GeneralDBColumnVar var = node.getRdbmsVar();
		String alias = "t" + getDBName(var);
		String tableName = literals.getDateTimeTable().getName();

		/**
		 * XXX in the default case this should be true!!
		 * Have changed to false to interleave these types of join with
		 * others involving predicate tables (i.e. hasAcquisitionTime)
		 * 
		 * -> By default, left joins are added to query after the inner ones
		 * 
		 * -> Reverting this in GeneralDBSqlJoinBuilder. The join actually 
		 * executed will be LEFT after all
		 */
		join(var, alias, tableName, false);
	}


	//Careful! Changes at the alias' name can cause great problems in the query plan!
	@Override
	public void meet(GeneralDBLabelColumn node) throws RuntimeException
	{
		GeneralDBColumnVar var = node.getRdbmsVar();
		//
		String alias = "l" + getDBName(var);
		String tableName;
		//XXX If spatial, I want to join with geo_values
		if(!var.isSpatial())
		{
			//String alias = "l" + getDBName(var);
			tableName = literals.getLabelTable().getName();
			join(var, alias, tableName);
		}
		else
		{
			//I don't need a left join in this case! Substituting with inner join!
			join(var, alias, "geo_values", false);
	
			// check whether we are going to project to a geometry value
			// and if so, add a join with datatype_values, so that we retrieve
			// the datatype of the geometry as well (see bug #71)
			if (query.getProjections().contains(var)) {
				//System.out.println("We will project on a geometry: " + var);
				
				String dtAlias = "d" + getDBName(var);
				if (!isJoined(dtAlias)) { // if this is the first time we do this
					// carry also the datatype of the geometry
					GeneralDBFromItem valueJoin = valueJoin(dtAlias,
															literals.getDatatypeTable().getName(), 
															var, 
															false);
					
					// we should add the join to the parent, because geo_values might
					// be joining through another table (e.g., asWKT)
					parent.addJoin(valueJoin);
					
				}
			}
		}

	}

	@Override
	public void meet(GeneralDBLongLabelColumn node)
			throws RuntimeException
			{
		GeneralDBColumnVar var = node.getRdbmsVar();
		String alias = "ll" + getDBName(var);
		String tableName = literals.getLongLabelTable().getName();
		join(var, alias, tableName);
			}

	@Override
	public void meet(GeneralDBLanguageColumn node)
			throws RuntimeException
			{
		GeneralDBColumnVar var = node.getRdbmsVar();
		String alias = "g" + getDBName(var);
		String tableName = literals.getLanguageTable().getName();
		join(var, alias, tableName);
			}

	@Override
	public void meet(GeneralDBNumericColumn node)
			throws RuntimeException
			{
		GeneralDBColumnVar var = node.getRdbmsVar();
		String alias = "n" + getDBName(var);
		String tableName = literals.getNumericTable().getName();
		join(var, alias, tableName);
			}

	@Override
	public void meet(GeneralDBLongURIColumn node)
			throws RuntimeException
			{
		GeneralDBColumnVar var = node.getRdbmsVar();
		String alias = "lu" + getDBName(var);
		String tableName = uris.getLongTableName();
		join(var, alias, tableName);
			}

	@Override
	public void meet(GeneralDBURIColumn node) throws RuntimeException
	{
		GeneralDBColumnVar var = node.getRdbmsVar();
		String alias = "u" + getDBName(var);
		String tableName = uris.getShortTableName();
		join(var, alias, tableName);
	}

	private CharSequence getDBName(GeneralDBColumnVar var) {
		String name = var.getName();
		if (name.indexOf('-') >= 0)
			return name.replace('-', '_');
		return "_" + name; // might be a keyword otherwise
	}

	private void join(GeneralDBColumnVar var, String alias, String tableName) {
		join(var, alias, tableName, true);
	}

	private void join(GeneralDBColumnVar var, String alias, String tableName, boolean left) {
		if (!isJoined(alias)) {
			GeneralDBFromItem valueJoin = valueJoin(alias, tableName, var, left);

			if(tableName.equals("datetime_values"))//The object I just created involves a datetime value
			{
				int counter = 0;
				//Must now find a connection with previous tables
				for(GeneralDBFromItem tmp : join.getJoins())
				{
					GeneralDBFromItem result = tmp.getFromItem(var.getAlias());
					if (result == null)
						counter++;
					else
					{
						break;
					}

				}

				if(counter==join.getJoins().size())
				{
					//If failure to locate an appropriate entry upwards -> revert to default behavior
					Logger logger = LoggerFactory.getLogger(GeneralDBValueJoinOptimizer.class);
					logger.error("[GeneralDBValueJoinOptimizer.info] Failed to push datetime join upwards");
				}
				else
				{
					join.getJoins().add(counter+1,valueJoin);
					return;
				}
			}

			//DEFAULT BEHAVIOR
			if (join == parent || join.getFromItem(var.getAlias()) != null) {
				join.addJoin(valueJoin);
			}
			else {
				parent.addJoinBefore(valueJoin, join);
			}
		}
	}

	private boolean isJoined(String alias) {
		if (stack.isEmpty())
			return query.getFromItem(alias) != null;
		return stack.get(stack.size() - 1).getFromItem(alias) != null;
	}

	private GeneralDBFromItem valueJoin(String alias, String tableName, GeneralDBColumnVar using, boolean left) {
		GeneralDBJoinItem j = new GeneralDBJoinItem(alias, tableName);
		j.setLeft(left);
		j.addFilter(new GeneralDBSqlEq(new GeneralDBIdColumn(alias), new GeneralDBRefIdColumn(using)));
		//XXX 07/09/2011 Spatial Joins and Selections moved to FROM clause 

		//FIXME must find I way so that this code runs on the LAST
		//occurrence of geo_values!!! => Fill structures used by next optimizer -> SpatialJoinOptimizer

		//Commented code because another approach was followed on 09/09/2011. 
		//Switched effort to creating a new optimizer dealing with TupleExpressions
		//		if(tableName.equals("geo_values"))
		//		{
		//			geo_values_occurences++;
		//			if(!spatiallyEnabled)
		//			{
		//				exprToAppend.addAll(query.getSpatialFilters());
		//				query.getFilters().removeAll(query.getSpatialFilters());
		//				spatiallyEnabled = true;
		//			}
		//		}

		//
		return j;
	}

	//	private GeneralDBFromItem valueJoin(String alias, String tableName, GeneralDBColumnVar using, boolean left) {
	//		GeneralDBJoinItem j = new GeneralDBJoinItem(alias, tableName);
	//		j.setLeft(left);
	//		j.addFilter(new GeneralDBSqlEq(new GeneralDBIdColumn(alias), new GeneralDBRefIdColumn(using)));
	//		//XXX 07/09/2011 Spatial Joins and Selections moved to FROM clause 
	//		if(!spatiallyEnabled)
	//		{
	//			//FIXME must find I way so that this code runs on the LAST
	//			//occurrence of geo_values!!!
	//			if(tableName.equals("geo_values"))
	//			{
	//				for(GeneralDBSqlExpr expr : query.getSpatialFilters())
	//				{
	//					j.addFilter(expr);
	//					
	//				}
	//				query.getFilters().removeAll(query.getSpatialFilters());
	//			}
	//			spatiallyEnabled = true;
	//		}
	//		//
	//		return j;
	//	}
}
