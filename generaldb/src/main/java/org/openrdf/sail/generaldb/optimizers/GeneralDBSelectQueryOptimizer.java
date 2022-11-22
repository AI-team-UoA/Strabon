/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.generaldb.optimizers;

import static org.openrdf.sail.generaldb.algebra.GeneralDBColumnVar.createCtx;
import static org.openrdf.sail.generaldb.algebra.GeneralDBColumnVar.createObj;
import static org.openrdf.sail.generaldb.algebra.GeneralDBColumnVar.createPred;
import static org.openrdf.sail.generaldb.algebra.GeneralDBColumnVar.createSpatialColumn;
import static org.openrdf.sail.generaldb.algebra.GeneralDBColumnVar.createSubj;
import static org.openrdf.sail.generaldb.algebra.base.GeneralDBExprSupport.coalesce;
import static org.openrdf.sail.generaldb.algebra.base.GeneralDBExprSupport.eq;
import static org.openrdf.sail.generaldb.algebra.base.GeneralDBExprSupport.isNull;
import static org.openrdf.sail.generaldb.algebra.base.GeneralDBExprSupport.or;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.IRI;
import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.openrdf.query.Dataset;
import org.openrdf.query.algebra.And;
import org.openrdf.query.algebra.Avg;
import org.openrdf.query.algebra.BinaryValueOperator;
import org.openrdf.query.algebra.Compare;
import org.openrdf.query.algebra.Distinct;
import org.openrdf.query.algebra.Extension;
import org.openrdf.query.algebra.ExtensionElem;
import org.openrdf.query.algebra.Filter;
import org.openrdf.query.algebra.FunctionCall;
import org.openrdf.query.algebra.Group;
import org.openrdf.query.algebra.GroupElem;
import org.openrdf.query.algebra.Join;
import org.openrdf.query.algebra.LeftJoin;
import org.openrdf.query.algebra.MathExpr;
import org.openrdf.query.algebra.Order;
import org.openrdf.query.algebra.OrderElem;
import org.openrdf.query.algebra.Projection;
import org.openrdf.query.algebra.ProjectionElem;
import org.openrdf.query.algebra.ProjectionElemList;
import org.openrdf.query.algebra.Slice;
import org.openrdf.query.algebra.StatementPattern;
import org.openrdf.query.algebra.StatementPattern.Scope;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.UnaryValueOperator;
import org.openrdf.query.algebra.Union;
import org.openrdf.query.algebra.ValueExpr;
import org.openrdf.query.algebra.Var;
import org.openrdf.query.algebra.evaluation.function.Function;
import org.openrdf.query.algebra.evaluation.function.FunctionRegistry;
import org.openrdf.query.algebra.evaluation.function.spatial.DateTimeMetricFunc;
import org.openrdf.query.algebra.evaluation.function.spatial.SpatialConstructFunc;
import org.openrdf.query.algebra.evaluation.function.spatial.SpatialMetricFunc;
import org.openrdf.query.algebra.evaluation.function.spatial.SpatialPropertyFunc;
import org.openrdf.query.algebra.evaluation.function.spatial.SpatialRelationshipFunc;
import org.openrdf.query.algebra.evaluation.function.spatial.stsparql.aggregate.ExtentFunc;
import org.openrdf.query.algebra.evaluation.function.spatial.stsparql.construct.BufferFunc;
import org.openrdf.query.algebra.evaluation.function.spatial.stsparql.construct.IntersectionFunc;
import org.openrdf.query.algebra.evaluation.function.spatial.stsparql.construct.TransformFunc;
import org.openrdf.query.algebra.evaluation.function.spatial.stsparql.construct.UnionFunc;
import org.openrdf.sail.generaldb.GeneralDBValueFactory;
import org.openrdf.sail.generaldb.algebra.GeneralDBBNodeColumn;
import org.openrdf.sail.generaldb.algebra.GeneralDBColumnVar;
import org.openrdf.sail.generaldb.algebra.GeneralDBDatatypeColumn;
import org.openrdf.sail.generaldb.algebra.GeneralDBIdColumn;
import org.openrdf.sail.generaldb.algebra.GeneralDBJoinItem;
import org.openrdf.sail.generaldb.algebra.GeneralDBLabelColumn;
import org.openrdf.sail.generaldb.algebra.GeneralDBLanguageColumn;
import org.openrdf.sail.generaldb.algebra.GeneralDBLongLabelColumn;
import org.openrdf.sail.generaldb.algebra.GeneralDBLongURIColumn;
import org.openrdf.sail.generaldb.algebra.GeneralDBNumberValue;
import org.openrdf.sail.generaldb.algebra.GeneralDBRefIdColumn;
import org.openrdf.sail.generaldb.algebra.GeneralDBSelectProjection;
import org.openrdf.sail.generaldb.algebra.GeneralDBSelectQuery;
import org.openrdf.sail.generaldb.algebra.GeneralDBSqlEq;
import org.openrdf.sail.generaldb.algebra.GeneralDBSqlOr;
import org.openrdf.sail.generaldb.algebra.GeneralDBURIColumn;
import org.openrdf.sail.generaldb.algebra.GeneralDBUnionItem;
import org.openrdf.sail.generaldb.algebra.base.GeneralDBQueryModelVisitorBase;
import org.openrdf.sail.generaldb.algebra.base.GeneralDBSqlExpr;
import org.openrdf.sail.generaldb.algebra.factories.GeneralDBSqlExprFactory;
import org.openrdf.sail.generaldb.managers.TransTableManager;
import org.openrdf.sail.generaldb.schema.IdSequence;
import org.openrdf.sail.rdbms.exceptions.RdbmsException;
import org.openrdf.sail.rdbms.exceptions.RdbmsRuntimeException;
import org.openrdf.sail.rdbms.exceptions.UnsupportedRdbmsOperatorException;
import org.openrdf.sail.rdbms.model.RdbmsResource;

import eu.earthobservatory.constants.GeoConstants;

/**
 * Rewrites the core algebra model with a relation optimised model, using SQL.
 * 
 * @author Charalampos Nikolaou <charnik@di.uoa.gr>
 * @author Manos Karpathiotakis <mk@di.uoa.gr.
 */
public class GeneralDBSelectQueryOptimizer extends GeneralDBQueryModelVisitorBase<RuntimeException> 
//implements QueryOptimizer //removed it consciously
{

	/**
	 * XXX additions (manolee)
	 */
	private List<TupleExpr> spatialJoins;

	//assuming no more than 10 uniquely-named variables will be spatial in the average case 
	private List<String> geoNames = new ArrayList<String>(10);

	//private Map<String, GeneralDBSqlExpr> constructsForSelect = new HashMap<String, GeneralDBSqlExpr>();

	//used to find the final select query and attach the constructs information on it
	private GeneralDBSelectQuery reference;

	//used to find out which is the predicate table a spatial table refers to.
	//Will be used in altering the query tree
	//13/09/2011
	private GeneralDBColumnVar previousAlias;
	private List<Var> existingSpatialJoins = new ArrayList<Var>();
	private GeneralDBColumnVar previousSpatialArg = null;

	//used to keep the names used in the group by clause and retrieve the query's relevant subparts
	private Set<String> namesInGroupBy;

	//If expression is a Function Call: the GeneralDBSqlExpr is ready!
	//All other cases: Value Expr stored, must be mapped to GeneralDBSql class first
	private Map<String,Object> exprInGroupBy = new HashMap<String,Object>();

	private Group referenceGroupBy = null;

	//used to keep the names used in a BIND clause
	private Set<String> namesInBind = new HashSet<String>();

	//Counter used to enumerate expressions in having
	private int havingID = 1;

	private static final String ALIAS = "t";

	private GeneralDBSqlExprFactory sql;

	private int aliasCount = 0;

	private BindingSet bindings;

	private Dataset dataset;

	private GeneralDBValueFactory vf;

	private TransTableManager tables;

	private IdSequence ids;

	public void setSqlExprFactory(GeneralDBSqlExprFactory sql) {
		this.sql = sql;
	}

	public void setValueFactory(GeneralDBValueFactory vf) {
		this.vf = vf;
	}

	public void setTransTableManager(TransTableManager statements) {
		this.tables = statements;
	}

	public void setIdSequence(IdSequence ids) {
		this.ids = ids;
	}

	public void optimize(TupleExpr tupleExpr, Dataset dataset, BindingSet bindings, List<TupleExpr> spatialJoins) {
		this.dataset = dataset;
		this.bindings = bindings;
		this.spatialJoins = spatialJoins;
		tupleExpr.visit(this);
	}

	@Override
	public void meet(Distinct node) throws RuntimeException
	{
		super.meet(node);
		if (node.getArg() instanceof GeneralDBSelectQuery) {
			GeneralDBSelectQuery query = (GeneralDBSelectQuery)node.getArg();
			query.setDistinct(true);
			node.replaceWith(query);
		}
	}

	@Override
	public void meet(Union node) throws RuntimeException
	{
		super.meet(node);
		TupleExpr l = node.getLeftArg();
		TupleExpr r = node.getRightArg();
		if (!(l instanceof GeneralDBSelectQuery && r instanceof GeneralDBSelectQuery))
			return;
		GeneralDBSelectQuery left = (GeneralDBSelectQuery)l;
		GeneralDBSelectQuery right = (GeneralDBSelectQuery)r;
		if (left.isComplex() || right.isComplex())
			return;
		GeneralDBUnionItem union = new GeneralDBUnionItem("u" + aliasCount++);
		union.addUnion(left.getFrom().clone());
		union.addUnion(right.getFrom().clone());
		GeneralDBSelectQuery query = new GeneralDBSelectQuery();

		query.setFrom(union);
		mergeSelectClause(query, left);
		mergeSelectClause(query, right);
		addProjectionsFromUnion(query, union);
		node.replaceWith(query);
	}

	/**
	 * This happens when both sides of the union have the same variable name with
	 * an implied value.
	 */
	private void addProjectionsFromUnion(GeneralDBSelectQuery query, GeneralDBUnionItem union) {
		for (GeneralDBColumnVar var : union.getSelectColumns()) {
			if (!query.hasSqlSelectVarName(var.getName())) {
				GeneralDBSelectProjection proj = new GeneralDBSelectProjection();
				proj.setVar(var);
				proj.setId(new GeneralDBRefIdColumn(var));
				proj.setStringValue(coalesce(new GeneralDBURIColumn(var), new GeneralDBBNodeColumn(var), new GeneralDBLabelColumn(var),
						new GeneralDBLongLabelColumn(var), new GeneralDBLongURIColumn(var)));
				proj.setDatatype(new GeneralDBDatatypeColumn(var));
				proj.setLanguage(new GeneralDBLanguageColumn(var));
				query.addSqlSelectVar(proj);
			}
		}
	}

	@Override
	public void meet(Join node) throws RuntimeException
	{
		super.meet(node);
		TupleExpr l = node.getLeftArg();
		TupleExpr r = node.getRightArg();
		if (!(l instanceof GeneralDBSelectQuery && r instanceof GeneralDBSelectQuery))
			return;
		GeneralDBSelectQuery left = (GeneralDBSelectQuery)l;
		GeneralDBSelectQuery right = (GeneralDBSelectQuery)r;
		if (left.isComplex() || right.isComplex())
			return;
		left = left.clone();
		right = right.clone();
		filterOn(left, right);
		mergeSelectClause(left, right);
		left.addJoin(right);
		node.replaceWith(left);
		/**
		 * XXX cheating - used 'reference' to find the final select query and attach the constructs information on it
		 * This change was made before altering the spatial joins operation
		 */
		reference = left;
	}

	@Override
	public void meet(LeftJoin node) throws RuntimeException
	{
		super.meet(node);
		TupleExpr l = node.getLeftArg();
		TupleExpr r = node.getRightArg();
		if (!(l instanceof GeneralDBSelectQuery && r instanceof GeneralDBSelectQuery))
			return;
		GeneralDBSelectQuery left = (GeneralDBSelectQuery)l;
		GeneralDBSelectQuery right = (GeneralDBSelectQuery)r;
		if (left.isComplex() || right.isComplex())
			return;
		left = left.clone();
		right = right.clone();
		filterOn(left, right);
		mergeSelectClause(left, right);
		left.addLeftJoin(right);
		List<GeneralDBSqlExpr> filters = new ArrayList<GeneralDBSqlExpr>();
		if (node.getCondition() != null) {
			for (ValueExpr expr : flatten(node.getCondition())) {
				try {
					filters.add(sql.createBooleanExpr(expr));
				}
				catch (UnsupportedRdbmsOperatorException e) {
					return;
				}
			}
		}
		for (GeneralDBSqlExpr filter : filters) {
			right.addFilter(filter);
		}
		node.replaceWith(left);
		reference = left;
	}

	@Override
	public void meet(StatementPattern sp) {
		super.meet(sp);
		Var subjVar = sp.getSubjectVar();
		Var predVar = sp.getPredicateVar();
		Var objVar = sp.getObjectVar();
		Var ctxVar = sp.getContextVar();

		Value subjValue = getVarValue(subjVar, bindings);
		Value predValue = getVarValue(predVar, bindings);
		Value objValue = getVarValue(objVar, bindings);
		Value ctxValue = getVarValue(ctxVar, bindings);

		if (subjValue instanceof Literal || predValue instanceof Literal || predValue instanceof BNode
				|| ctxValue instanceof Literal)
		{
			// subj and ctx must be a Resource and pred must be a URI
			return;
		}

		/**
		 * Effort here to recover possible elements located in group by
		 * so that I order the results according to them
		 */
		if(this.namesInGroupBy != null)
		{
			if(this.namesInGroupBy.contains(subjVar.getName()))
			{
				this.exprInGroupBy.put(subjVar.getName(),subjVar);
			}
			if(this.namesInGroupBy.contains(predVar.getName()))
			{
				this.exprInGroupBy.put(predVar.getName(),predVar);
			}
			if(this.namesInGroupBy.contains(objVar.getName()))
			{
				this.exprInGroupBy.put(objVar.getName(),objVar);
			}
		}
		//		for(String possibleVarName : this.namesInGroupBy)
		//		{
		//			if(possibleVarName.equals(subjVar.getName()))
		//			{
		//				this.exprInGroupBy.put(possibleVarName, subjVar);
		//				continue;
		//			}
		//			
		//			if(possibleVarName.equals(predVar.getName()))
		//			{
		//				this.exprInGroupBy.put(possibleVarName, predVar);
		//				continue;
		//			}
		//			
		//			if(possibleVarName.equals(objVar.getName()))
		//			{
		//				this.exprInGroupBy.put(possibleVarName, objVar);
		//				continue;
		//			}
		//			
		//		}
		/**
		 * 
		 */

		Resource[] contexts = getContexts(sp, ctxValue);
		if (contexts == null)
			return;

		String alias = getTableAlias(predValue) + aliasCount++;
		Number predId = getInternalId(predValue);
		String tableName;
		boolean present;
		try {
			tableName = tables.getTableName(predId);
			present = tables.isPredColumnPresent(predId);
		}
		catch (SQLException e) {
			throw new RdbmsRuntimeException(e);
		}
		GeneralDBJoinItem from = new GeneralDBJoinItem(alias, tableName, predId);

		GeneralDBColumnVar s = createSubj(alias, subjVar, (Resource)subjValue);
		GeneralDBColumnVar p = createPred(alias, predVar, (URI)predValue, !present);
		/**
		 * XXX enabled spatial objects
		 */
		boolean spatialObj = false;

		if(geoNames.contains(objVar.getName()))
		{
			spatialObj = true;
		}
		GeneralDBColumnVar o = createObj(alias, objVar, objValue, spatialObj);

		GeneralDBColumnVar c = createCtx(alias, ctxVar, (Resource)ctxValue);

		s.setTypes(tables.getSubjTypes(predId));
		o.setTypes(tables.getObjTypes(predId));

		GeneralDBSelectQuery query = new GeneralDBSelectQuery();
		//XXX volatile change -> just trying to find some place to 'hang' the constructs
		//The fact that I have this type of connection in 3 different places is not
		//necessarily good
		reference = query;

		query.setFrom(from);
		Map<String, GeneralDBColumnVar> vars = new HashMap<String, GeneralDBColumnVar>(4);
		for (GeneralDBColumnVar var : new GeneralDBColumnVar[] { s, p, o, c }) {
			from.addVar(var);
			Value value = var.getValue();
			if (vars.containsKey(var.getName())) {
				GeneralDBIdColumn existing = new GeneralDBIdColumn(vars.get(var.getName()));
				from.addFilter(new GeneralDBSqlEq(new GeneralDBIdColumn(var), existing));
			}
			else if (value != null && !var.isImplied()) {
				try {
					GeneralDBNumberValue vc = new GeneralDBNumberValue(vf.getInternalId(value));
					from.addFilter(new GeneralDBSqlEq(new GeneralDBRefIdColumn(var), vc));
				}
				catch (RdbmsException e) {
					throw new RdbmsRuntimeException(e);
				}
			}
			else {
				vars.put(var.getName(), var);

				if(var.getColumn().equals("obj")&&previousSpatialArg!=null)
				{
					from.addFilter(new GeneralDBSqlEq(new GeneralDBIdColumn(var), new GeneralDBIdColumn(previousSpatialArg)));
					//Re-initializing it so that no unwanted joins are created by accident!! my addition
					previousSpatialArg = null;
				}
			}


			if (!var.isHiddenOrConstant() && value == null) {
				GeneralDBSelectProjection proj = new GeneralDBSelectProjection();
				proj.setVar(var);
				proj.setId(new GeneralDBRefIdColumn(var));

				if(geoNames.contains(var.getName()))
				{
					proj.setStringValue(new GeneralDBLabelColumn(var));
					//13/09/2011 my addition in order to create a spatial join in the meet(Filter) call that will follow
					previousAlias = var;
					
					// add the corresponding datatype (see {@link GeneralDBValueJoinOptimizer.GeneralDBLabelColumn})
					proj.setDatatype(new GeneralDBDatatypeColumn(var));
				}
				else
				{
					proj.setStringValue(coalesce(new GeneralDBURIColumn(var), new GeneralDBBNodeColumn(var), new GeneralDBLabelColumn(var),
							new GeneralDBLongLabelColumn(var), new GeneralDBLongURIColumn(var)));
					proj.setDatatype(new GeneralDBDatatypeColumn(var));
					proj.setLanguage(new GeneralDBLanguageColumn(var));
				}
				query.addSqlSelectVar(proj);
			}
		}
		if (contexts.length > 0) {
			RdbmsResource[] ids = vf.asRdbmsResource(contexts);
			GeneralDBRefIdColumn var = new GeneralDBRefIdColumn(c);
			GeneralDBSqlExpr in = null;
			for (RdbmsResource id : ids) {
				GeneralDBNumberValue longValue;
				try {
					longValue = new GeneralDBNumberValue(vf.getInternalId(id));
				}
				catch (RdbmsException e) {
					throw new RdbmsRuntimeException(e);
				}
				GeneralDBSqlEq eq = new GeneralDBSqlEq(var.clone(), longValue);
				if (in == null) {
					in = eq;
				}
				else {
					in = new GeneralDBSqlOr(in, eq);
				}
			}
			from.addFilter(in);
		}
		sp.replaceWith(query);
	}

	@Override
	public void meet(Filter node) throws RuntimeException
	{
		/**
		 * XXX 21/09/2011 addition for spatial joins
		 * Ekmetalleyomai to gegonos oti exw 'fytepsei' sta embolima filters twn joins mia statement pattern me to onoma -dummy-
		 * Etsi katalabainw pote to Filter dn brisketai sto where clause
		 */
		if(node.getArg() instanceof StatementPattern)
		{
			StatementPattern st = (StatementPattern) node.getArg();
			if(st.getSubjectVar().getName().equals("-dummy-"))
			{
				//Spatial join

				//				List<Var> allVars = retrieveVars(node.getCondition());
				List<Var> allVars = new ArrayList<Var>(retrieveVars(node.getCondition()));
				GeneralDBSelectQuery queries[] = new GeneralDBSelectQuery[allVars.size()];

				int count = 0;

				//will probably be TWO contents at most in all cases concerning spatial filters here - have to make sure of that
				//Don't know which one of the spatial variables is bound with the upper join! Therefore, I altered the code to check 
				//for the possibility any of them is the one.
				int mark = 0;
				//FIXME remove the list after consulting Kostis for certainty, and replace with table[2]
				if(allVars.size()>1)
				{
					for(Var var : allVars)
					{
						if(var.getName().equals(previousAlias.getName()))
						{

							//							if(var.getName().endsWith("?-buffer-"))
							//							{
							//								bufferCase = true;
							//								fixVarName(var);
							//								
							//								existingSpatialJoins.add(var);
							//								queries[count] = new GeneralDBSelectQuery();
							//								Value objValue = getVarValue(var,bindings);
							//								
							//								GeneralDBColumnVar colVar = createObj("l_"+var.getName(), var, objValue);


							//							}
							//							else //DEFAULT CASE. The only case differentiating from this one is buffer(?spatialVar,?thematicVar)
							//							{

							existingSpatialJoins.add(var);
							queries[count] = new GeneralDBSelectQuery();
							Value objValue = getVarValue(var,bindings);

							//any changes in these two lines could cause problems
							GeneralDBColumnVar colVar = createSpatialColumn("l_"+var.getName(), var, objValue);
							GeneralDBJoinItem from = new GeneralDBJoinItem("l_"+var.getName(), "geo_values");

							queries[count].setFrom(from);

							//assuming that only one var will reach this case
							from.addFilter(new GeneralDBSqlEq(new GeneralDBIdColumn(colVar), new GeneralDBIdColumn(previousAlias)));

							//Copying functionality from meet(StatementPattern)
							GeneralDBSelectProjection proj = new GeneralDBSelectProjection();
							proj.setVar(colVar);
							proj.setId(new GeneralDBRefIdColumn(var));
							break;
							//							}
						}
						count++;
					}
					mark = (count+1)%2;
				}
				//The second var of the spatial join -> must incorporate the spatial filter here


				Var var = allVars.get(mark);
				existingSpatialJoins.add(var);
				queries[mark] = new GeneralDBSelectQuery();
				Value objValue = getVarValue(var,bindings);

				//any changes in these two lines could cause problems
				GeneralDBColumnVar colVar = createSpatialColumn("l_"+var.getName(), var, objValue);
				GeneralDBJoinItem from = new GeneralDBJoinItem("l_"+var.getName(), "geo_values");
				queries[mark].setFrom(from);

				previousSpatialArg = colVar;


				super.meet(node);

				//GeneralDBSelectProjection proj = new GeneralDBSelectProjection();
				//proj.setVar(colVar);
				//proj.setId(new GeneralDBRefIdColumn(var));

				//Incorporating spatial filter
				ValueExpr condition = null;
				for (ValueExpr expr : flatten(node.getCondition())) 
				{
					try 
					{
						GeneralDBSqlExpr sqlFilter = sql.createBooleanExpr(expr);

						queries[mark].addFilter(sqlFilter);
					}
					catch (UnsupportedRdbmsOperatorException e)
					{
						if (condition == null) 
						{
							condition = expr;
						}
						else 
						{
							condition = new And(condition, expr);
						}
					}
				}



				queries[count].setParentNode(node.getParentNode());

				node.replaceWith(queries[mark]);
				//				for(Var var: allVars)
				//				{	
				//					existingSpatialJoins.add(var);
				//					queries[count] = new GeneralDBSelectQuery();
				//					Value objValue = getVarValue(var,bindings);
				//					
				//					
				//					//GeneralDBColumnVar colVar = createSpatialColumn(/*"l_"+*/var.getName(), var, objValue);
				//					
				//					//any changes in these two lines could cause problems
				//					GeneralDBColumnVar colVar = createSpatialColumn("l_"+var.getName(), var, objValue);
				//					GeneralDBJoinItem from = new GeneralDBJoinItem("l_"+var.getName(), "geo_values");
				//
				//					queries[count].setFrom(from);
				//					
				//					
				//					//If I am not dealing with the last spatial column
				//					if(!(count+1 == allVars.size()))
				//					{
				//
				//						//assuming that only one var will reach this case
				//						from.addFilter(new GeneralDBSqlEq(new GeneralDBIdColumn(colVar), new GeneralDBIdColumn(previousAlias)));
				//
				//						//Copying functionality from meet(StatementPattern)
				//						GeneralDBSelectProjection proj = new GeneralDBSelectProjection();
				//						proj.setVar(colVar);
				//						proj.setId(new GeneralDBRefIdColumn(var));
				//
				//
				//					}
				//					else //The second var of the spatial join -> must incorporate the spatial filter here
				//					{
				//						//queries[count].getFilters().clear();
				//						super.meet(node);
				//						
				//						//GeneralDBSelectProjection proj = new GeneralDBSelectProjection();
				//						//proj.setVar(colVar);
				//						//proj.setId(new GeneralDBRefIdColumn(var));
				//
				//						//Incorporating spatial filter
				//						ValueExpr condition = null;
				//						for (ValueExpr expr : flatten(node.getCondition())) 
				//						{
				//							try 
				//							{
				//								GeneralDBSqlExpr sqlFilter = sql.createBooleanExpr(expr);
				//							
				//								queries[count].addFilter(sqlFilter);
				//							}
				//							catch (UnsupportedRdbmsOperatorException e)
				//							{
				//								if (condition == null) 
				//								{
				//									condition = expr;
				//								}
				//								else 
				//								{
				//									condition = new And(condition, expr);
				//								}
				//							}
				//						}
				//						
				//						previousSpatialArg = colVar;
				//					}
				//					count++;
				//
				//
				//				}
				//int j = 0;
				//Is this a valid approach? Not sure it is functioning as I expected
				//What i wanted to do is replace a node with 2 GeneralDBSelectQueries that derived from the processing of the Filter!
				//				for(j = 0; j < count - 1; j++)
				//				{
				//					queries[j].setParentNode(node.getParentNode());
				//
				//				}
				//node.replaceWith(queries[j]);

			}
			else
			{
				super.meet(node);
				ValueExpr condition = null;
				for (ValueExpr expr : flatten(node.getCondition())) {
					try {
						this.reference.addFilter(sql.createBooleanExpr(expr));
					}
					catch (UnsupportedRdbmsOperatorException e) {
						if (condition == null) {
							condition = expr;
						}
						else {
							condition = new And(condition, expr);
						}
					}
				}
				if (condition == null) {
					node.replaceWith(node.getArg());
				}
				else {
					node.setCondition(condition);
				}
			}
			//			//XXX extension of default functionality for single statement queries - VOLATILE
			//			if(node.getCondition() instanceof FunctionCall)
			//			{
			//				ValueExpr condition = null;
			//				for (ValueExpr expr : flatten(node.getCondition())) {
			//					try {
			//						this.reference.addFilter(sql.createBooleanExpr(expr));
			//
			//					}
			//					catch (UnsupportedRdbmsOperatorException e) {
			//						if (condition == null) {
			//							condition = expr;
			//						}
			//						else {
			//							condition = new And(condition, expr);
			//						}
			//					}
			//				}
			//				if (condition == null) {
			//					node.replaceWith(node.getArg());
			//				}
			//				else {
			//					node.setCondition(condition);
			//				}
			//			}
		}
		/**
		 * End of addition
		 */
		else //DEFAULT BEHAVIOR (Enhanced with check for duplicates)
		{

			boolean dup = false;
			ValueExpr dupFunctionCall = node.getCondition();
			for(TupleExpr sfilter : this.spatialJoins)
			{
				ValueExpr tmpExpr = ((Filter)sfilter).getCondition();
				if(tmpExpr.equals(dupFunctionCall))
				{
					//					QueryModelNode parent = node.getParentNode();
					//					TupleExpr replacement = ((Filter)node).getArg();
					//					parent.replaceChildNode(node, replacement);
					//					//If I do reach this point, the former 'child' argument will be a traditional Join operator
					//					super.meet((Join)replacement);
					//					node.setCondition(null);
					//					return;
					dup = true;
					break;
				}
			}

			super.meet(node);
			if (node.getArg() instanceof GeneralDBSelectQuery) {
				GeneralDBSelectQuery query = (GeneralDBSelectQuery)node.getArg();

				ValueExpr condition = null;
//				dup=false;
				if(!dup)
				{
					for (ValueExpr expr : flatten(node.getCondition())) {
						try {
							//07/09/2011
							//Attempt to move spatial selections and joins to FROM clause
							//XXX removed because I followed a different approach. May come in handy later on. 09/09/11
							//					if(node.getCondition() instanceof FunctionCall)
							//					{
							//						GeneralDBSqlExpr sqlExpression = sql.createBooleanExpr(expr);
							//						query.addSpatialFilter(sqlExpression);
							//						//query.getFrom().addFilter(sql.createBooleanExpr(expr));
							//						query.addFilter(sqlExpression);
							//					}
							//					else //DEFAULT CASE
							//					{
 							query.addFilter(sql.createBooleanExpr(expr));
							//					}

						}
						catch (UnsupportedRdbmsOperatorException e) {
							if (condition == null) {
								condition = expr;
							}
							else {
								condition = new And(condition, expr);
							}
						}
					}
				}
				else
				{
					condition = null;
				}

				if (condition == null) {
					node.replaceWith(node.getArg());
				}
				else {
					node.setCondition(condition);
				}
			}

			/**
			 * Used a single function to iterate the query graph and try to locate potential aggregates present in HAVING
			 * instead of performing multiple loops
			 */
			examineHaving(node);

			//17/1/2012
			//XXX CAREFUL: Think this means that the FILTER is related to a Having Clause!!
			//Probably only useful for single arguments in Having - Refactoring this case
			//else if(node.getArg() instanceof Extension)
			////Use for multiple arguments present in Having
			//			else if(withinHaving(node))
			//			{
			//				System.out.println("Found Having!!!");
			//				//must add as aggregate if Compare contains a Function call that interests me!
			//				if(this.referenceGroupBy != null)
			//				{
			//					Extension tmp = (Extension) node.getArg();
			//					if(node.getCondition() instanceof Compare)
			//					{
			//						Compare cmp = (Compare) node.getCondition();
			//						if(cmp.getLeftArg() instanceof FunctionCall)
			//						{
			//							GroupElem groupElem = new GroupElem("havingCondition"+(havingID++)+"-aggregateInside-", new Avg(cmp.getLeftArg()));
			//							this.referenceGroupBy.addGroupElement(groupElem);
			//						}
			//						if(cmp.getRightArg() instanceof FunctionCall)
			//						{
			//							GroupElem groupElem = new GroupElem("havingCondition"+(havingID++)+"-aggregateInside-", new Avg(cmp.getRightArg()));
			//							this.referenceGroupBy.addGroupElement(groupElem);
			//						}
			//					}
			//					else if(node.getCondition() instanceof FunctionCall)
			//					{
			//						GroupElem groupElem = new GroupElem("havingCondition"+(havingID++)+"-aggregateInside-", new Avg(node.getCondition()));
			//						this.referenceGroupBy.addGroupElement(groupElem);
			//					}
			//
			//				}
			//
			//			}


		}
			}



	@Override
	public void meet(Projection node)
			throws RuntimeException
			{
		super.meet(node);
		// Edw ftanei to Filter GeneralDBSqlDiffDateTime!
		if (node.getArg() instanceof GeneralDBSelectQuery) {
			GeneralDBSelectQuery query = (GeneralDBSelectQuery)node.getArg();

			Map<String, String> bindingVars = new HashMap<String, String>();
			List<GeneralDBSelectProjection> selection = new ArrayList<GeneralDBSelectProjection>();
			ProjectionElemList list = node.getProjectionElemList();
			for (ProjectionElem e : list.getElements()) {
				String source = e.getSourceName();
				String target = e.getTargetName();
				bindingVars.put(target, source);
				GeneralDBSelectProjection s = query.getSelectProjection(source);
				if (s != null) {
					selection.add(s);
				}
			}
			query.setBindingVars(bindingVars);
			query.setSqlSelectVar(selection);
			node.replaceWith(query);
		}
			}

	/**
	 * XXX used to retrieve the geoNames from the TRADITIONAL filter clauses - not the ones in Joins
	 */
	@Override
	public void meet(FunctionCall node)
			throws RuntimeException
	{
		Function function = FunctionRegistry.getInstance().get(node.getURI()).get();

		super.meet(node);

		if(function instanceof SpatialRelationshipFunc || function instanceof SpatialConstructFunc 
				|| function instanceof SpatialMetricFunc || function instanceof SpatialPropertyFunc )
		{
			List<ValueExpr> allArgs = node.getArgs();

			int argNo = 0; 
			//Used so that the second argument of buffer func is not 
			//mistakenly confused with a spatial variable
			for(ValueExpr arg : allArgs)
			{	
				argNo++;
				if(arg instanceof Var)
				{
					if((!(function instanceof BufferFunc)&&!(function instanceof TransformFunc)) || argNo!=2 )
					{
						//The variable's name is not in the list yet
						if(!geoNames.contains(((Var) arg).getName()))
						{
							geoNames.add(((Var) arg).getName());

						}
						/**
						 * XXX TRICK TO SPECIFY THAT THE VAR IS SPATIAL:
						 * I will alter the var's name and set it back to the original
						 * in the LabelColumn's Constructor! 
						 * 
						 * Only reason to avoid this step: FunctionCall contained in Order
						 * and order is to be evaluated by java code and not sql
						 * No measure taken so far
						 */

						String originalName = ((Var)arg).getName();
						((Var)arg).setName(originalName+"?spatial");
					}
					//					}
					//					else
					//					{
					//						//Replace damage caused by marking thematic variables of buffer
					//						if(((Var)arg).getName().endsWith("?-buffer-"))
					//						{
					//							fixVarName((Var) arg);
					//						}
					//					}
				}
			}

			//			if(this.namesInGroupBy != null && (node.getParentNode() instanceof ExtensionElem))
			//			{
			//				ExtensionElem parent = (ExtensionElem) node.getParentNode();
			//				if(this.namesInGroupBy.contains(parent.getName()))
			//				{
			//					this.exprInGroupBy.put(parent.getName(),node);
			//				}
			//				
			//			}
		}
		/**
		 * Addition for datetime metric functions
		 * 
		 * @author George Garbis <ggarbis@di.uoa.gr>
		 * 
		 */
		else if (function instanceof DateTimeMetricFunc)
		{
			List<ValueExpr> allArgs = node.getArgs();

			int argNo = 0; 
			//Used so that the second argument of buffer func is not 
			//mistakenly confused with a spatial variable
			for(ValueExpr arg : allArgs)
			{	
				argNo++;
				if(arg instanceof Var && argNo!=2)
				{
					String originalName = ((Var)arg).getName();
					((Var)arg).setName(originalName);
				}
			}
		}
		/***/
	}

	//
	@Override
	public void meet(Extension node) throws RuntimeException
	{
		super.meet(node);

		Iterator<ExtensionElem> iter = node.getElements().iterator();

		while(iter.hasNext())
		{
			ExtensionElem elem = iter.next();

			//Simple case: A construct
			ValueExpr expr = elem.getExpr();
			GeneralDBSqlExpr sqlExpr = null;
			String name = elem.getName();
			
			if (expr instanceof FunctionCall)
			//if (expr instanceof FunctionCall && !isFuncExprGrounded(expr))
			{ // if the expr is grounded we are going to evaluate it in Java
				//also if the function involves variables from a BIND clause
				//we evaluate it in java
				if(!evaluateInJava(expr) && !varInBind(expr))
				{
					Function function = FunctionRegistry.getInstance().get(((FunctionCall) expr).getURI()).get();
					if(function instanceof SpatialPropertyFunc  || function instanceof SpatialRelationshipFunc ||
					   function instanceof SpatialConstructFunc || function instanceof SpatialMetricFunc)
					{
						try {
							sqlExpr = sql.getBooleanExprFactory().spatialFunction((FunctionCall) expr);
							reference.getSpatialConstructs().put(name, sqlExpr);
						} catch (UnsupportedRdbmsOperatorException e) {
							e.printStackTrace();
						}
						iter.remove();
					}
				}
				else if(!varInBind(expr)) //Union (or Extent) is used as an aggregate function on Select Clause!
				{
					//must add as aggregate
					if(this.referenceGroupBy != null)
					{
						GroupElem groupElem = new GroupElem(name+"-aggregateInside-", new Avg(expr));
						this.referenceGroupBy.addGroupElement(groupElem);
					}
					iter.remove();
				}
				//if the name of the new variable is not present in the projection
				//then it results from a BIND
				if(!(node.getParentNode() instanceof Projection))
					namesInBind.add(name+"?spatial");
			}
			//Issue: MathExpr is not exclusively met in spatial cases!
			//Need to distinguish thematic and spatial!!

			//One way to do it: Check for label column in children of expression. If none exists: No way sth spatial is involved!
			else if(expr instanceof MathExpr)
			{
				try {
					if(!thematicExpression(expr))
					{
						sqlExpr = sql.getNumericExprFactory().createNumericExpr(expr);

						reference.getSpatialConstructs().put(name, sqlExpr);
						iter.remove();
					}
				} catch (UnsupportedRdbmsOperatorException e) {
					e.printStackTrace();
				}
			}
			/**
			 * Effort here to recover possible elements located in group by
			 * so that I order the results according to them
			 */
			if(this.namesInGroupBy != null) //&& !(expr instanceof FunctionCall))
			{
				if(this.namesInGroupBy.contains(elem.getName()))
				{
					if(expr instanceof FunctionCall)
					{
						/**
						 * No need to produce the sql part twice
						 * What is more, if I try to produce it again, the vars will have the ?spatial part corrected
						 */

						this.exprInGroupBy.put(elem.getName(),sqlExpr);
					}
					else
					{
						this.exprInGroupBy.put(elem.getName(),elem.getExpr());
					}
				}
			}
		}
	}

	/**
	 * Checks whether the given value expression contains only grounded
	 * terms (constants). 
	 * 
	 * This should work for the spatial case, but I am not 100% sure whether
	 * it is going to work for whole SPARQL 1.1.
	 * 
	 * @param funcExpr
	 * @return
	 */
	private boolean isFuncExprGrounded(ValueExpr funcExpr) {
		if (funcExpr instanceof FunctionCall) {
			// recursively check its arguments
			boolean groundedChildren = true;
			for (int i = 0; i < ((FunctionCall) funcExpr).getArgs().size(); i++) {
				groundedChildren &= isFuncExprGrounded(((FunctionCall) funcExpr).getArgs().get(i));
			}
			
			return groundedChildren;
			
		} else if (funcExpr instanceof Var) { // variable
			return false;
			
		} else { // all other cases (constant, UnaryExpressions, etc...) -> dodgy!
			return true;
		}
	}
	
	//Checking that no spatial function exists in this metric expression
	private boolean thematicExpression(ValueExpr expr)
	{
		if(expr instanceof UnaryValueOperator)
		{
			return thematicExpression(((UnaryValueOperator) expr).getArg());
		}
		else if(expr instanceof BinaryValueOperator)
		{
			return thematicExpression(((BinaryValueOperator) expr).getLeftArg()) &&
					thematicExpression(((BinaryValueOperator) expr).getRightArg());
		}
		else
		{
			if(expr instanceof FunctionCall)
			{
				Function function = FunctionRegistry.getInstance().get(((FunctionCall) expr).getURI()).get();
				if(function instanceof SpatialMetricFunc)
				{
					return false;
				}
			}
			return true;
		}

	}

	/**
	 * XXX 23/11/11
	 * Commented because I now aim to evaluate any function calls present in SELECT with JTS in the case they
	 * contain an aggregate function
	 */
	//	@Override
	//	public void meet(Extension node) throws RuntimeException
	//	{
	//		super.meet(node);
	//
	//		Iterator<ExtensionElem> iter = node.getElements().iterator();
	//
	//		while(iter.hasNext())
	//		{
	//			ExtensionElem elem = iter.next();
	//
	//			//Simple case: A construct
	//			ValueExpr expr = elem.getExpr();
	//			GeneralDBSqlExpr sqlExpr = null;
	//			if(expr instanceof FunctionCall)
	//			{
	//				Function function = FunctionRegistry.getInstance().get(((FunctionCall) expr).getURI());
	//				if(function instanceof SpatialPropertyFunc || function instanceof SpatialRelationshipFunc 
	//						|| function instanceof SpatialConstructFunc || function instanceof SpatialMetricFunc)
	//				{
	//					try {
	//						String name = elem.getName();
	//
	//						if(!(function instanceof UnionFunc) || !(((FunctionCall) expr).getArgs().size()==1))
	//						{
	//							sqlExpr = sql.getBooleanExprFactory().spatialFunction((FunctionCall) expr);
	//							reference.getSpatialConstructs().put(name, sqlExpr);
	//						}
	//						else //else: Union is used as an aggregate function on Select Clause!
	//						{
	//							//must add as aggregate
	//							if(this.referenceGroupBy != null)
	//							{
	//								GroupElem groupElem = new GroupElem(name+"-aggregateInside-", new Avg(expr));
	//								this.referenceGroupBy.addGroupElement(groupElem);
	//							}
	//						}
	//						iter.remove();
	//					} catch (UnsupportedRdbmsOperatorException e) {
	//						e.printStackTrace();
	//					}
	//
	//
	//				}
	//			}
	//			else if(expr instanceof MathExpr)
	//			{
	//				try {
	//					sqlExpr = sql.getNumericExprFactory().createNumericExpr(expr);
	//					String name = elem.getName();
	//
	//					reference.getSpatialConstructs().put(name, sqlExpr);
	//
	//					iter.remove();
	//				} catch (UnsupportedRdbmsOperatorException e) {
	//					e.printStackTrace();
	//				}
	//			}
	//			/**
	//			 * Effort here to recover possible elements located in group by
	//			 * so that I order the results according to them
	//			 */
	//			if(this.namesInGroupBy != null) //&& !(expr instanceof FunctionCall))
	//			{
	//				if(this.namesInGroupBy.contains(elem.getName()))
	//				{
	//					if(expr instanceof FunctionCall)
	//					{
	//						/**
	//						 * No need to produce the sql part twice
	//						 * What is more, if I try to produce it again, the vars will have the ?spatial part corrected
	//						 */
	//
	//						this.exprInGroupBy.put(elem.getName(),sqlExpr);
	//					}
	//					else
	//					{
	//						this.exprInGroupBy.put(elem.getName(),elem.getExpr());
	//					}
	//				}
	//			}
	//			/**
	//			 * 
	//			 */
	//
	//
	//		}
	//	}


	/**
	 * Function used recursively to specify whether the function call present in the select clause contains an aggregate
	 * of the form strdf:union(?aggrValue) or strdf:intersection(?aggrValue).
	 *  
	 * @param expr 
	 * @return true if no aggregate is present, false otherwise.
	 */
	private boolean evaluateInJava(ValueExpr expr)
	{
		if(expr instanceof FunctionCall)
		{
			Function function = FunctionRegistry.getInstance().get(((FunctionCall) expr).getURI()).get();
			if((!(function instanceof UnionFunc) || !(((FunctionCall) expr).getArgs().size()==1))
					&&(!(function instanceof IntersectionFunc) || !(((FunctionCall) expr).getArgs().size()==1))
					&&!(function instanceof ExtentFunc))
			{
				//Recursively check arguments
				boolean aggregatePresent = false;
				for(int i = 0 ; i< ((FunctionCall) expr).getArgs().size(); i++)
				{
					//ValueExpr tmp = ((FunctionCall) expr).getArgs().get(i);
					//containsAggregateUnion = containsAggregateUnion || evaluateInDB(tmp);
					//					noUnionPresent = noUnionPresent ^ evaluateInJava(((FunctionCall) expr).getArgs().get(i));
					aggregatePresent = aggregatePresent || evaluateInJava(((FunctionCall) expr).getArgs().get(i));
				}
				return aggregatePresent;
			}
			else
			{
				return true;
			}
		}
		else //Var
		{
			return false;
		}

	}

	/**
	 *
	 * @param expr
	 * @return true if the variable occurs inside a BIND clause
	 * false otherwise
	 */
	private boolean varInBind(ValueExpr expr)
	{
		if(expr instanceof FunctionCall)
		{
			for(int i = 0 ; i< ((FunctionCall) expr).getArgs().size(); i++)
			{
				return varInBind(((FunctionCall) expr).getArgs().get(i));
			}
			return false;
		}
		else if(expr instanceof Var) //Var
		{
			if(namesInBind.contains(((Var)expr).getName()))
				return true;
			return false;
		}
		else
			return false;
	}

	@Override
	public void meet(Slice node)
			throws RuntimeException
			{
		super.meet(node);
		if (node.getArg() instanceof GeneralDBSelectQuery) {
			GeneralDBSelectQuery query = (GeneralDBSelectQuery)node.getArg();
			if (node.getOffset() > 0) {
				query.setOffset(node.getOffset());
			}
			if (node.getLimit() >= 0) {
				query.setLimit(node.getLimit());
			}
			node.replaceWith(query);
		}
		else if(node.getArg() instanceof Projection) {
			//push limit to inner node
			try {
				Projection prj = (Projection) node.getArg();
				if(prj.getArg() instanceof Extension) {
					Extension ext = (Extension) prj.getArg();
					if(ext.getArg() instanceof GeneralDBSelectQuery) {
						GeneralDBSelectQuery query = (GeneralDBSelectQuery)ext.getArg();
						if (node.getOffset() > 0) {
							query.setOffset(node.getOffset());
						}
						if (node.getLimit() >= 0) {
							query.setLimit(node.getLimit());
						}
						node.replaceWith(prj);
					}
				}
			} catch(Exception e) {
				System.err.println("Could not push limit to inner node");
			}
		}
	}

	@Override
	public void meet(Order node)
			throws RuntimeException
			{
		int mbbCounter = 0;
		//		super.meet(node);
		if (!(node.getArg() instanceof GeneralDBSelectQuery))
			//In other words, I have encountered having/groupby
		{
			//Must find a way to incorporate additional projections to Select Query
			for (OrderElem e : node.getElements()) {
				ValueExpr expr = e.getExpr();

				if(!(expr instanceof FunctionCall))
				{
					if(expr instanceof Var)
					{
						if(this.geoNames.contains(((Var) expr).getName()))
						{
							Var copy = (Var) expr.clone();

							String originalName = copy.getName();
							//((Var) expr).setName(originalName+"?spatial");

							FunctionCall fc = new FunctionCall(GeoConstants.stSPARQLenvelope,copy);
							//XXX volatile - using an extra arg to 'hang' the name I need
							fc.addArg(new Var("-mbb-"+originalName));
							ExtensionElem extElem = new ExtensionElem(fc,"-mbb-"+originalName);
							if(node.getArg() instanceof Extension)
							{
								Extension ext = new Extension();
								ext.addElement(extElem);
								//								((Projection)(node.getParentNode())).setArg(ext);
								//								ext.setArg(node);
								Extension tmpExt = (Extension) node.getArg();
								node.setArg(ext);
								ext.setArg(tmpExt);

							}
							//							else if(node.getParentNode() instanceof Projection)
							//							{
							//								Extension ext = new Extension();
							//								ext.addElement(extElem);
							//								((Projection)(node.getParentNode())).setArg(ext);
							//								ext.setArg(node);
							//							}
							else if(node.getParentNode() instanceof Extension)
							{
								((Extension)node.getParentNode()).addElement(extElem);
							}
							e.setExpr(fc);
							//must add bindings for the mbb geometries

						}
					}

				}
				else //Function call met
				{
					FunctionCall fc = new FunctionCall(GeoConstants.stSPARQLenvelope,expr);

					fc.addArg(new Var("-mbb-"+(++mbbCounter)));
					ExtensionElem extElem = new ExtensionElem(fc,"-mbb-"+(mbbCounter));
					if(node.getArg() instanceof Extension)
					{
						Extension ext = new Extension();
						ext.addElement(extElem);
						//						((Projection)(node.getParentNode())).setArg(ext);
						//						ext.setArg(node);
						Extension tmpExt = (Extension) node.getArg();
						node.setArg(ext);
						ext.setArg(tmpExt);
					}
					//					else if(node.getParentNode() instanceof Projection)
					//					{
					//						Extension ext = new Extension();
					//						ext.addElement(extElem);
					//						((Projection)(node.getParentNode())).setArg(ext);
					//						ext.setArg(node);
					//					}
					else if(node.getParentNode() instanceof Extension)
					{
						((Extension)node.getParentNode()).addElement(extElem);
					}
					e.setExpr(fc);
				}

			}
			super.meet(node);
			return;
		}
		super.meet(node);
		GeneralDBSelectQuery query = (GeneralDBSelectQuery)node.getArg();
		try {
			for (OrderElem e : node.getElements()) {
				ValueExpr expr = e.getExpr();
				boolean asc = e.isAscending();
				if(!(expr instanceof FunctionCall))
				{
					boolean isSpatial = false;
					if(expr instanceof Var)
					{
						if(this.geoNames.contains(((Var) expr).getName()))
						{
							String originalName = ((Var)expr).getName();
							((Var) expr).setName(originalName+"?spatial");
							query.addOrder(sql.createLabelExpr(expr), asc);
							isSpatial = true;
						}
					}

					if(!isSpatial)
					{
						//Default behavior
						query.addOrder(sql.createBNodeExpr(expr), asc);
						query.addOrder(sql.createUriExpr(expr), asc);
						query.addOrder(sql.createNumericExpr(expr), asc);
						query.addOrder(sql.createDatatypeExpr(expr), asc);
						query.addOrder(sql.createTimeExpr(expr), asc);
						query.addOrder(sql.createLanguageExpr(expr), asc);
						query.addOrder(sql.createLabelExpr(expr), asc);
					}
				}
				else //Function call met
				{
					query.addOrder(sql.createBooleanExpr(expr),asc);
				}

			}
			node.replaceWith(query);
		}
		catch (UnsupportedRdbmsOperatorException e) {
			// unsupported
		}
			}

	/**
	 * FIXME uncomment if you need sorted results based on the group by's contents!!!
	 * Commented for now because I am not messing with Sesame's native approach to the issue
	 */
	@Override
	public void meet(Group node)
	{
		super.meet(node);
		//		Set<String> tmp1 = node.getAggregateBindingNames();
		//		Set<String> tmp2 = node.getAssuredBindingNames();
		//		Set<String> tmp3 = node.getBindingNames();
		//		Set<String> tmp4 = node.getGroupBindingNames();
		//		System.out.println("Placeholder");
		this.referenceGroupBy = node;
	}
	//	@Override
	//	public void meet(Group node)
	//	{
	//
	//		this.namesInGroupBy = node.getGroupBindingNames();
	//		super.meet(node);
	//		try {
	//			for (Object e : this.exprInGroupBy.values()) {
	//
	//				boolean asc = true;
	//				//e is either a ValueExpr or a GeneralDBSqlExpr in the case of FunctionCall
	//				if(!(e instanceof GeneralDBSqlExpr))
	//				{
	//					ValueExpr expr = (ValueExpr) e;
	//					boolean isSpatial = false;
	//					if(expr instanceof Var)
	//					{
	//						if(this.geoNames.contains(((Var) expr).getName()))
	//						{
	//							String originalName = ((Var)expr).getName();
	//							((Var) expr).setName(originalName+"?spatial");
	//
	//							this.reference.addOrder(sql.createLabelExpr(expr), asc);
	//
	//							isSpatial = true;
	//						}
	//					}
	//
	//					if(!isSpatial)
	//					{
	//						//Default behavior
	//						this.reference.addOrder(sql.createBNodeExpr(expr), asc);
	//						this.reference.addOrder(sql.createUriExpr(expr), asc);
	//						this.reference.addOrder(sql.createNumericExpr(expr), asc);
	//						this.reference.addOrder(sql.createDatatypeExpr(expr), asc);
	//						this.reference.addOrder(sql.createTimeExpr(expr), asc);
	//						this.reference.addOrder(sql.createLanguageExpr(expr), asc);
	//						this.reference.addOrder(sql.createLabelExpr(expr), asc);
	//					}
	//				}
	//				else //Function call met
	//				{
	//					/**
	//					 * Must first fix the Vars inside the function call!
	//					 * Because the tree visitors have already processed these function calls once,
	//					 *  
	//					 */
	//
	//					this.reference.addOrder((GeneralDBSqlExpr) e,asc);
	//				}
	//
	//			}
	//		} catch (UnsupportedRdbmsOperatorException e) {
	//			e.printStackTrace();
	//		}
	//	}

	private void filterOn(GeneralDBSelectQuery left, GeneralDBSelectQuery right) {
		Map<String, GeneralDBColumnVar> lvars = left.getVarMap();
		Map<String, GeneralDBColumnVar> rvars = right.getVarMap();
		Set<String> names = new HashSet<String>(rvars.keySet());
		names.retainAll(lvars.keySet());
		for (String name : names) {
			GeneralDBColumnVar l = lvars.get(name);
			GeneralDBColumnVar r = rvars.get(name);
			if (!l.isImplied() && !r.isImplied()) {
				GeneralDBIdColumn rid = new GeneralDBIdColumn(r);
				GeneralDBSqlExpr filter = eq(rid, new GeneralDBIdColumn(l));
				if (r.isNullable()) {
					filter = or(isNull(rid), filter);
				}
				right.addFilter(filter);
			}
		}
	}

	private Number getInternalId(Value predValue) {
		try {
			return vf.getInternalId(predValue);
		}
		catch (RdbmsException e) {
			throw new RdbmsRuntimeException(e);
		}
	}

	private Resource[] getContexts(StatementPattern sp, Value ctxValue) {
		if (dataset == null) {
			if (ctxValue != null)
				return new Resource[] { (Resource)ctxValue };
			return new Resource[0];
		}
		Set<IRI> graphs = getGraphs(sp);
		if (graphs.isEmpty())
			return null; // Search zero contexts
		if (ctxValue == null)
			return graphs.toArray(new Resource[graphs.size()]);

		if (graphs.contains(ctxValue))
			return new Resource[] { (Resource)ctxValue };
		// pattern specifies a context that is not part of the dataset
		return null;
	}

	private Set<IRI> getGraphs(StatementPattern sp) {
		if (sp.getScope() == Scope.DEFAULT_CONTEXTS)
			return dataset.getDefaultGraphs();
		return dataset.getNamedGraphs();
	}

	private String getTableAlias(Value predValue) {
		if (predValue != null) {
			String localName = ((URI)predValue).getLocalName();
			if (localName.length() >= 1) {
				String alias = localName.substring(0, 1);
				if (isLetters(alias)) {
					return alias;
				}
			}
		}
		return ALIAS;
	}

	private Value getVarValue(Var var, BindingSet bindings) {
		if (var == null) {
			return null;
		}
		else if (var.hasValue()) {
			return var.getValue();
		}
		else {
			return bindings.getValue(var.getName());
		}
	}

	private boolean isLetters(String alias) {
		for (int i = 0, n = alias.length(); i < n; i++) {
			if (!Character.isLetter(alias.charAt(i)))
				return false;
		}
		return true;
	}

	private void mergeSelectClause(GeneralDBSelectQuery left, GeneralDBSelectQuery right) {
		for (GeneralDBSelectProjection proj : right.getSqlSelectVar()) {
			if (!left.hasSqlSelectVar(proj)) {
				proj = proj.clone();
				GeneralDBColumnVar var = proj.getVar();
				String name = var.getName();
				GeneralDBColumnVar existing = left.getVar(name);
				if (existing != null) {
					proj.setVar(existing);
				}
				left.addSqlSelectVar(proj);
			}
		}
	}

	private List<ValueExpr> flatten(ValueExpr condition) {
		return flatten(condition, new ArrayList<ValueExpr>());
	}

	private List<ValueExpr> flatten(ValueExpr condition, List<ValueExpr> conditions) {
		if (condition instanceof And) {
			And and = (And)condition;
			flatten(and.getLeftArg(), conditions);
			flatten(and.getRightArg(), conditions);
		}
		else {
			conditions.add(condition);
		}
		return conditions;
	}

	/***
	 * added for spatial join support
	 * TODO perhaps I will need to elaborate and separate the Function Calls in more cases
	 * when I add the rest of the Constructs, Properties etc.
	 */
	private Set<Var> retrieveVars(ValueExpr expr)
	{
		//		List<Var> vars = new ArrayList<Var>();
		Set<Var> vars = new LinkedHashSet<Var>();
		if(expr instanceof FunctionCall)
		{
			int argNo = 0;
			for(ValueExpr arg : ((FunctionCall) expr).getArgs())
			{	
				argNo++;
				if(arg instanceof Var)
				{
					//if(!((FunctionCall)expr).getURI().equals("http://strdf.di.uoa.gr/ontology#buffer") || argNo!=2 )
					//{
					if(!existingSpatialJoins.contains(arg))
					{
						//XXX note: this may cause error if the user intends to 
						//perform an operation such as ?GEO1 ~ ?GEO1. 
						//Should we cater for such mistaken queries?

						//Want to find the UNIQUE names!
						//if(!vars.contains(arg))
						//{
						vars.add((Var) arg);
						//}
					}
					//}
				}
				else if(arg instanceof FunctionCall)
				{
					vars.addAll(retrieveVars(arg));
				}

			}

		}
		else if(expr instanceof Var)
		{
			if(!existingSpatialJoins.contains(expr))
			{
				//Want to find the UNIQUE names!
				//if(!vars.contains(expr))
				//{					 
				vars.add((Var) expr);
				//}
			}
		}
		//Metrics Support
		else if(expr instanceof Compare)
		{
			vars.addAll(retrieveVars(((Compare) expr).getLeftArg()));
			vars.addAll(retrieveVars(((Compare) expr).getRightArg()));
		}
		else if(expr instanceof MathExpr)
		{
			vars.addAll(retrieveVars(((MathExpr) expr).getLeftArg()));
			vars.addAll(retrieveVars(((MathExpr) expr).getRightArg()));
		}
		return vars;
	}

	/**
	 * Used to find out whether the Filter expr we are currently visiting is located inside
	 * a HAVING clause.
	 * 
	 * From what I have seen so far, it seems that if this is the case, its arg (or one of the following FILTER args in 
	 * the case of disjunction/conjunction) will be an Extension expr followed by a GROUP expr
	 */
	private boolean examineHaving(Filter expr)
	{
		boolean insideHavingClause = false;
		if(expr.getArg() instanceof Filter)
		{
			insideHavingClause = examineHaving((Filter) expr.getArg());
		}
		else if(expr.getArg() instanceof Extension)
		{
			insideHavingClause = true;
		}
		else
		{
			//System.out.println("Traditional Filter inside WHERE clause");
			return insideHavingClause;
		}

		if(insideHavingClause)
		{
			if(this.referenceGroupBy != null)
			{
				if(expr.getCondition() instanceof Compare)
				{
					Compare cmp = (Compare) expr.getCondition();
					if(cmp.getLeftArg() instanceof FunctionCall)
					{

						boolean found = false;
						for(GroupElem e : this.referenceGroupBy.getGroupElements())
						{
							if(((Avg)e.getOperator()).getArg().equals(cmp.getLeftArg()))
							{
								found = true;
								break;
							}
						}

						if(!found)
						{
							Avg toBeEntered = new Avg(cmp.getLeftArg());
							GroupElem groupElem = new GroupElem("havingCondition"+(havingID++)+"-aggregateInside-", toBeEntered);
							this.referenceGroupBy.addGroupElement(groupElem);
						}

					}
					if(cmp.getRightArg() instanceof FunctionCall)
					{
						boolean found = false;
						for(GroupElem e : this.referenceGroupBy.getGroupElements())
						{
							if(((Avg)e.getOperator()).getArg().equals(cmp.getRightArg()))
							{
								found = true;
								break;
							}
						}

						if(!found)
						{
							Avg toBeEntered = new Avg(cmp.getRightArg());
							GroupElem groupElem = new GroupElem("havingCondition"+(havingID++)+"-aggregateInside-", toBeEntered);
							this.referenceGroupBy.addGroupElement(groupElem);
						}
					}
				}
				else if(expr.getCondition() instanceof FunctionCall)
				{
					//Must find possible aggregate functions here
					iterateFunctions((FunctionCall) expr.getCondition());
				}
			}
		}
		return insideHavingClause;
	}

	private void iterateFunctions(FunctionCall func)
	{
		for(ValueExpr expr : func.getArgs())
		{
			if(expr instanceof FunctionCall)
			{
				Function function = FunctionRegistry.getInstance().get(((FunctionCall) expr).getURI()).get();
				//Aggregate Function
				if(((function instanceof UnionFunc) && (((FunctionCall) expr).getArgs().size()==1))
						|| ((function instanceof IntersectionFunc) && (((FunctionCall) expr).getArgs().size()==1))
						|| (function instanceof ExtentFunc))
				{
					GroupElem groupElem = new GroupElem("havingCondition"+(havingID++)+"-aggregateInside-", new Avg(expr));
					this.referenceGroupBy.addGroupElement(groupElem);
					continue;
				}
				else
				{
					iterateFunctions((FunctionCall) expr);
					continue;
				}
			}
			else
			{
				continue;
			}
		}
	}
	//	private void fixVarName(Var buff)
	//	{
	//
	//		int whereToCut = buff.getName().lastIndexOf("?");
	//		String originalName = buff.getName().substring(0, whereToCut);
	//		buff.setName(originalName);
	//
	//	}
}
