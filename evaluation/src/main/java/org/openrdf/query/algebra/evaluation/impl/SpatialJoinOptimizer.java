/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * 
 * Copyright (C) 2010, 2011, 2012, Pyravlos Team
 * 
 * http://www.strabon.di.uoa.gr/
 */
package org.openrdf.query.algebra.evaluation.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openrdf.query.BindingSet;
import org.openrdf.query.Dataset;
import org.openrdf.query.algebra.Compare;
import org.openrdf.query.algebra.Extension;
import org.openrdf.query.algebra.Filter;
import org.openrdf.query.algebra.FunctionCall;
import org.openrdf.query.algebra.Join;
import org.openrdf.query.algebra.LeftJoin;
import org.openrdf.query.algebra.MathExpr;
import org.openrdf.query.algebra.Or;
import org.openrdf.query.algebra.QueryModelNode;
import org.openrdf.query.algebra.StatementPattern;
import org.openrdf.query.algebra.StatementPattern.Scope;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.ValueExpr;
import org.openrdf.query.algebra.Var;
import org.openrdf.query.algebra.evaluation.function.Function;
import org.openrdf.query.algebra.evaluation.function.FunctionRegistry;
import org.openrdf.query.algebra.evaluation.function.spatial.SpatialConstructFunc;
import org.openrdf.query.algebra.evaluation.function.spatial.SpatialMetricFunc;
import org.openrdf.query.algebra.evaluation.function.spatial.SpatialPropertyFunc;
import org.openrdf.query.algebra.evaluation.function.spatial.SpatialRelationshipFunc;
import org.openrdf.query.algebra.helpers.QueryModelVisitorBase;
import org.openrdf.query.algebra.helpers.StatementPatternCollector;

/**
 * A query optimizer that re-orders nested Joins.
 * 
 * @author Manos Karpathiotakis <mk@di.uoa.gr>
 */
public class SpatialJoinOptimizer 
//implements QueryOptimizer //Removed it consciously 
{


	//private Set<String> existingVars = new TreeSet<String>();
	/**
	 * Applies generally applicable optimizations: path expressions are sorted
	 * from more to less specific.
	 * 
	 * @param tupleExpr
	 */
	public void optimize(TupleExpr tupleExpr, Dataset dataset, BindingSet bindings, List<TupleExpr> spatialJoins) {
		tupleExpr.visit(new JoinVisitor(spatialJoins));
	}

	protected class JoinVisitor extends QueryModelVisitorBase<RuntimeException> {
		
		
		
		public JoinVisitor(List<TupleExpr> spatialJoins) {
			super();
			this.spatialJoins = spatialJoins;
		}

		
		private List<TupleExpr> spatialJoins;
		//buffer with a var as a second argument
		private boolean problematicBuffer = false;

		//indicates whether a metric expression contains a spatial function call	
		private boolean containsSpatial = false;
		private boolean optimizableMetricOrProperty = true;

		private int thematicJoinsSize = 0;

		//List<SpatialFilterInfo> allSFilters = new ArrayList<SpatialFilterInfo>();
		Map<TupleExpr, List<Var>> allSFilters = new HashMap<TupleExpr, List<Var>>();

		@Override
		public void meet(Join node) {

			//XXX NOTE: NOT OPTIMIZING CONTENTS OF OPTIONAL CLAUSE!!!
			//Reason1: Errors occurred in the condition of the LeftJoin containing the optional subgraph

			//Not as successful as I hoped. Some OPTIONAL clauses may pass
			if(parentIsOptional(node))
			{
				return;
			}
			//			if(node.getParentNode() instanceof LeftJoin)
			//			{
			//				return;
			//			}

			// Recursively get the join arguments
			List<TupleExpr> joinArgs = getJoinArgs(node, new ArrayList<TupleExpr>());

			if(joinArgs == null)
			{
				//2nd mechanism to avoid OPTIONAL clauses from passing
				return;
			}


			Map<TupleExpr, List<Var>> varsMap = new /*Linked*/HashMap<TupleExpr, List<Var>>();

			for (TupleExpr tupleExpr : joinArgs) {
				varsMap.put(tupleExpr, getStatementPatternVars(tupleExpr));

			}

			//Now I have all the info I need. Just need to structure it efficiently
			int allNodes = varsMap.size() + allSFilters.size();
			thematicJoinsSize = varsMap.size();
			//careful with positions on the diagonal! Do not utilize them!
			int joinsGraph[][] = new int[allNodes][allNodes];

			//prints for debug
			//			Set<TupleExpr> allExprs = varsMap.keySet();
			//			System.out.println(allExprs.toString());
			//			
			//			Set<TupleExpr> allFilters = allSFilters.keySet();
			//			System.out.println(allFilters.toString());

			//Thematic part first
			int i = 0;

			for(List<Var> listHorizontal : varsMap.values())
			{
				int j = 0;
				int k = 0;

				//Other thematics
				for(List<Var> listVertical : varsMap.values())
				{
					if(i==j)
					{
						j++;
						continue;
					}


					joinsGraph[i][j] = sameVar(listHorizontal, listVertical);

					j++;
				}

				//Spatials
				for(List<Var> listVertical : allSFilters.values())
				{

					joinsGraph[i][k+varsMap.size()] = sameVar(listHorizontal, listVertical);

					k++;
				}

				i++;
			}

			//Now for the spatial horizontal nodes
			i = varsMap.size();
			for(List<Var> listHorizontal : allSFilters.values())
			{
				int j = 0;
				int k = 0;

				//Other thematics
				for(List<Var> listVertical : varsMap.values())
				{

					joinsGraph[i][j] = sameVar(listHorizontal, listVertical);

					j++;
				}

				//Spatials
				for(List<Var> listVertical : allSFilters.values())
				{
					if(i==k+varsMap.size())
					{
						k++;
						continue;
					}

					joinsGraph[i][k+varsMap.size()] =sameVar(listHorizontal, listVertical);

					k++;
				}

				i++;
			}

			//Checking graph to be sure
			/*
			for(int a = 0; a < allNodes; a++)
			{
				for(int b = 0; b < allNodes; b++)
				{
					System.out.print(joinsGraph[a][b]+" ");

				}
				System.out.println("");
			}
			*/

			//Time to construct ordered sequence of joins + filters
			List<TupleExpr> orderedJoinArgs = new ArrayList<TupleExpr>(allNodes);
			//maybe I won't need all the positions -> some spatial joins may not be utilized
			List<Integer> tempList = new ArrayList<Integer>(allNodes);
			List<Integer> pathList = new ArrayList<Integer>();
			List<Integer> finalList = new ArrayList<Integer>();
			Set<Var> varsTillNow = new LinkedHashSet<Var>();
			for(int row = 0 ; row < allNodes ; row++)
			{
				tempList.add(row);
				createOrderedJoins(joinsGraph, row, row, 1, varsTillNow, tempList, pathList, finalList);
				pathList.clear();
				if(finalList.size() == allNodes)
				{
					break;
				}

			}

			/*
			System.out.println("*--REWRITTEN TREE--**");
			System.out.println(finalList.toString());
			*/

			int varsMapSize = varsMap.size();
			for(Integer position : finalList)
			{
				if(position<varsMapSize)//thematic!
				{
					int count = 0;
					Iterator it = varsMap.entrySet().iterator();

					while (it.hasNext())
					{
						{
							Map.Entry entry = (Map.Entry)it.next();
							if(count == position)
							{
								orderedJoinArgs.add((TupleExpr) entry.getKey());

								it.remove();
								varsMapSize--;

								for(int fix = 0 ; fix < finalList.size(); fix++)
								{
									if(finalList.get(fix) > position)
									{
										int reduced = finalList.get(fix) - 1;
										finalList.set(fix, reduced);

									}
								}
								break;
							}
							count++;
						}
					}
				}
				else//spatial!
				{
					int count = 0;
					Iterator it = allSFilters.entrySet().iterator();
					while (it.hasNext())
					{
						{
							Map.Entry entry = (Map.Entry)it.next();
							if(count == position - varsMapSize)
							{
								//If I keep record of this entry, I can use the info later to avoid duplicate filters
								spatialJoins.add((TupleExpr) entry.getKey());
								//
								orderedJoinArgs.add((TupleExpr) entry.getKey());
								it.remove();
								for(int fix = 0 ; fix < finalList.size(); fix++)
								{
									if(finalList.get(fix) > position)
									{
										int reduced = finalList.get(fix) - 1;
										finalList.set(fix, reduced);
									}
								}
								break;
							}
							count++;
						}
					}
				}

			}
			//Must take care of the remainders as well!
			Iterator it = varsMap.entrySet().iterator();
			while (it.hasNext())
			{
				Map.Entry entry = (Map.Entry)it.next();
				orderedJoinArgs.add((TupleExpr) entry.getKey());
				it.remove();
			}

			TupleExpr replacement = orderedJoinArgs.get(0);
			for (int ii = 1; ii < orderedJoinArgs.size(); ii++) {
				replacement = new Join(replacement, orderedJoinArgs.get(ii));
			}

			// Replace old join hierarchy
			node.replaceWith(replacement);

		}


		public boolean parentIsOptional(QueryModelNode node)
		{
			if(node.getParentNode() == null)
			{
				return false;
			}
			else if(node.getParentNode() instanceof LeftJoin)
			{
				return true;
			}
			else
			{
				return parentIsOptional(node.getParentNode());
			}
		}

		/**
		 * General Comment: If more than one function exists in the query and can be used to perform the join, no preference is shown 
		 * on which function will actually be used for this purpose. This could cause an issue if ST_Disjoint is the one finally used, 
		 * as the index won't be used for the evaluation in this case. Perhaps I should include some 'priority'
		 */
		@Override
		public void meet(Filter node) {

			/**
			 * Filter node must not be in OPTIONAL clause!
			 * After all, unneeded check. If an optional exists, the 'Filter' tuple expression
			 * is removed and only the Function Calls remain
			 */
			if(!withinHaving(node))
			{
				//if(!(node.getParentNode() instanceof LeftJoin))
				//{
				//XXX Ignore OR in Filters for now! (perhaps permanently)
				if(!(node.getCondition() instanceof Or))
				{
					if(node.getCondition() instanceof FunctionCall)
					{
						//Only interested in spatial ones
						if(isRelevantSpatialFunc((FunctionCall) node.getCondition()))
						{
							//Have to retrieve all nested variables and other info

							//1.Retrieve varNames
							List<Var> varList = getFunctionCallVars((FunctionCall) node.getCondition());

							//Cannot optimize buffer constructs when their 2nd argument is a 
							//thematic var, because I can't manipulate the order of the 
							//numeric_values table
							if(!problematicBuffer)
							{
								//XXX I cannot process cases involving more than 2 arguments in the spatial join!
								// They transcend the theta join level!
								//							if(varList.size()<3)

								//if the arguments are not 2, I am essentially doing a selection!
								//No reason to push into optimizer then
								if(varList.size()==2)
								{
									//Add all important info about this spatial relationship to the appropriate structure
									//allSFilters.add(new SpatialFilterInfo(varList, (FunctionCall) node.getCondition()));
									Filter toEnter = node.clone();

									/**
									 * VERY CAREFUL HERE! 
									 * Reason I did this: If not, I would carry a copy of the whole expression for
									 * every extra filter of my query!
									 * -DUMMY-!!!
									 */
									StatementPattern t = new StatementPattern();
									t.setSubjectVar(new Var("-dummy-"));
									t.setPredicateVar(new Var("-dummy-"));
									t.setObjectVar(new Var("-dummy-"));
									t.setScope(Scope.DEFAULT_CONTEXTS);
									toEnter.setArg(t);

									if(!allSFilters.containsKey(toEnter))
									{
										allSFilters.put(toEnter,varList);
									}
								}
								problematicBuffer = false;
							}
						}
					}
					//Metrics
					//I have a similar problematic case with the one occurring in Buffer! 
					//Cannot manipulate the join order when I am dealing with numerics!
					//Therefore, I cannot use a numeric field (a numeric var) to alter the join sequence
					else if(node.getCondition() instanceof Compare)
					{
						containsSpatial = false;
						List<Var> allVars = new ArrayList<Var>(getVarsInMetricOrProperty(node.getCondition()));
						if(containsSpatial&&optimizableMetricOrProperty)
						{
							//if the arguments are not 2, I am essentially doing a selection!
							//No reason to push into optimizer then
							if(allVars.size()==2)
								//if(allVars.size()<3)
							{
								//Add all important info about this spatial relationship to the appropriate structure
								//allSFilters.add(new SpatialFilterInfo(varList, (FunctionCall) node.getCondition()));
								Filter toEnter = node.clone();

								StatementPattern t = new StatementPattern();
								t.setSubjectVar(new Var("-dummy-"));
								t.setPredicateVar(new Var("-dummy-"));
								t.setObjectVar(new Var("-dummy-"));
								t.setScope(Scope.DEFAULT_CONTEXTS);
								toEnter.setArg(t);

								if(!allSFilters.containsKey(toEnter))
								{
									allSFilters.put(toEnter,allVars);
								}
							}
							containsSpatial = false;
						}
						optimizableMetricOrProperty = true;
					}

				}
				//}
			}

			//Last thing to do is ensuring the entire tree is traversed
			node.visitChildren(this);

		}


		/**
		 * Helper Functions
		 */

		/**
		 * Used to find out whether the Filter expr we are currently visiting is located inside
		 * a HAVING clause.
		 * 
		 * From what I have seen so far, it seems that if this is the case, its arg (or one of the following FILTER args in 
		 * the case of disjunction/conjunction) will be an Extension expr followed by a GROUP expr
		 */
		private boolean withinHaving(Filter expr)
		{
			if(expr.getArg() instanceof Extension)
			{
				return true;
			}
			else if(expr.getArg() instanceof Filter)
			{
				return withinHaving((Filter) expr.getArg());
			}
			else
			{
				return false;
			}
		}

		private <L extends List<TupleExpr>> L getJoinArgs(TupleExpr tupleExpr, L joinArgs) {
			if (tupleExpr instanceof Join) {
				Join join = (Join)tupleExpr;
				if(getJoinArgs(join.getLeftArg(), joinArgs) == null)
					return null;
				if(getJoinArgs(join.getRightArg(), joinArgs) == null)
					return null;
			}
			else if(tupleExpr instanceof LeftJoin) 
			{
				//Trying to avoid OPTIONAL clauses from passing
				return null;
			}
			else 
			{
				joinArgs.add(tupleExpr);
			}

			return joinArgs;
		}

		private List<Var> getStatementPatternVars(TupleExpr tupleExpr) {
			List<StatementPattern> stPatterns = StatementPatternCollector.process(tupleExpr);
			List<Var> varList = new ArrayList<Var>(stPatterns.size() * 4);
			for (StatementPattern sp : stPatterns) {
				sp.getVars(varList);
			}
			return varList;
		}

		/**
		 * spatialContent: Used to declare that this expression does include some spatial 
		 * content and an attempt should be made to use it in the joins' optimization process
		 */
		private Set<Var> getVarsInMetricOrProperty(ValueExpr expr)
		{
			Set<Var> allVars = new LinkedHashSet<Var>();

			if(expr instanceof Compare)
			{
				allVars.addAll(getVarsInMetricOrProperty(((Compare) expr).getLeftArg()));
				allVars.addAll(getVarsInMetricOrProperty(((Compare) expr).getRightArg()));
			}
			else if(expr instanceof MathExpr)
			{
				allVars.addAll(getVarsInMetricOrProperty(((MathExpr) expr).getLeftArg()));
				allVars.addAll(getVarsInMetricOrProperty(((MathExpr) expr).getRightArg()));
			}
			else if(expr instanceof FunctionCall )
			{
				if(isRelevantSpatialFunc((FunctionCall) expr))
				{
					/**
					 * There is a point in continuing the search recursively ONLY 
					 * if I reach this case. Otherwise, the function call
					 * may not refer to a spatial function
					 */
					this.containsSpatial = true;
					allVars.addAll(getFunctionCallVars((FunctionCall) expr));

				}
			}
			else if(expr instanceof Var)
			{
				if(!(expr.getParentNode() instanceof FunctionCall))
				{
					//Cannot manipulate the join order when I am dealing with numerics!
					//Therefore, I cannot use a numeric field (a numeric var) to alter the join sequence
					this.optimizableMetricOrProperty = false;
				}
				if(!allVars.contains(expr))
				{
					allVars.add((Var) expr);
				}
			}

			return allVars;
		}

		private boolean isRelevantSpatialFunc(FunctionCall functionCall)
		{
			Function function = FunctionRegistry.getInstance().get(functionCall.getURI());
			if(function instanceof SpatialConstructFunc)
			{
				//TODO may have to comment this part again
				//uncommented because I use this function in the case of metrics
				return true;
			}
			else if(function instanceof SpatialRelationshipFunc)
			{
				return true;
			}
			else if(function instanceof SpatialPropertyFunc) //1 argument
			{
				return true;
			}
			else if(function instanceof SpatialMetricFunc) //Arguments # depends on the function selected
			{
				return true;
			}
			return false;
		}

		private List<Var> getFunctionCallVars(FunctionCall functionCall) {
			List<Var> varList = new ArrayList<Var>();
			int argList = 0;
			for(ValueExpr expr : functionCall.getArgs())
			{
				argList++;
				if(expr instanceof Var)
				{
					//					if(!existingVars.contains(((Var) expr).getName()))
					//					{
					//						existingVars.add(((Var) expr).getName());
					//					}

					if(!varList.contains(expr))
					{
						//Was using this code when I tried to incorporate the Buffer case buffer(?Spatial,?thematic)
						if(argList == 2 && functionCall.getURI().equals("http://strdf.di.uoa.gr/ontology#buffer"))
						{
							problematicBuffer = true;
						}
						varList.add((Var) expr);
					}

				}
				else if(expr instanceof FunctionCall)
				{
					varList.addAll(getFunctionCallVars((FunctionCall) expr));
				}
				//TODO Should I add any additional cases? I don't think so
				else
				{
					continue;
				}
			}
			return varList;
		}

		/**
		 * Both lists belong to either a thematic node (St.Pattern) or a spatial node (Filter)
		 * Comparing the vars with each other to discover links of the query graph
		 * @param list1 
		 * @param list2
		 * @return
		 */
		private int sameVar(List<Var> list1, List<Var> list2)
		{
			for(Var var : list1)
			{
				if(list2.contains(var))
				{
					return 1;
				}
			}

			return 0;
		}





		//Input: a single line of the table
		//NOTE: NO LOOPS! CAREFUL!!
		private boolean createOrderedJoins(int table[][], int lineToScan,int columnToSkip, 
				int pathLen, Set<Var> varsTillNow, List<Integer> tempList, List<Integer> pathList, List<Integer> finalList)
		{
			boolean success = false;
			int dims = table.length;

			int j;

			//dims: all arguments
			for(j = 0; j < dims; j++)
			{
				if(j == columnToSkip)
				{
					continue;
				}

				//A connection exists!
				if(table[lineToScan][j]>0)
				{
					//Don't want my graph to have circles!!!
					if(!tempList.contains(j)&&!pathList.contains(j))
					{
						if(j >= thematicJoinsSize) //aka if(allSFilters.size() > 0)
						{
							List<Var> thisFilterVars = null;
							int count = 0;
							for(List<Var> vars : allSFilters.values())
							{
								if(count == j - thematicJoinsSize)
								{
									thisFilterVars = vars;
									break;
								}
								count++;
							}
							if(!varsTillNow.isEmpty())
							{
								if(varsTillNow.containsAll(thisFilterVars))
								{
									continue;
								}
							}

							//varsTillNow.add(thisFilterVars.get(1));
							varsTillNow.addAll(thisFilterVars);

						}

						tempList.add((Integer)j);
						pathLen++;
						if(pathLen == dims)
						{
							//End of recursion
							pathList.addAll(tempList);
							tempList.clear();
							finalList.clear();
							finalList.addAll(pathList);

							return true;
						}

						//Recurse
						success = createOrderedJoins(table, j, lineToScan, pathLen, varsTillNow, tempList, pathList, finalList);

						//Success = true => recursion ended with pathLen = maxLen
						if(success)
						{
							return true;
						}
						else
						{	
							//end of a path originating from this node was found -> find an additional path (if exists)
							continue;
						}
					}
				}
			}
			//To reach this place means the end of a path was found 
			pathList.addAll(tempList);
			tempList.clear();
			if(pathList.size() > finalList.size())
			{
				finalList.clear();
				finalList.addAll(pathList);
				if(finalList.size() == dims)
				{
					return true;
				}
			}
			return false;			
		}

	}


}
