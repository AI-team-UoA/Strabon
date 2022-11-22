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
import org.openrdf.query.algebra.QueryRoot;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.evaluation.EvaluationStrategy;
import org.openrdf.query.algebra.evaluation.impl.BindingAssigner;
import org.openrdf.query.algebra.evaluation.impl.CompareOptimizer;
import org.openrdf.query.algebra.evaluation.impl.ConjunctiveConstraintSplitter;
import org.openrdf.query.algebra.evaluation.impl.DisjunctiveConstraintOptimizer;
import org.openrdf.query.algebra.evaluation.impl.SameTermFilterOptimizer;
import org.openrdf.query.algebra.evaluation.impl.SpatialJoinOptimizer;
import org.openrdf.query.algebra.evaluation.impl.stSPARQLConstantOptimizer;
import org.openrdf.sail.generaldb.GeneralDBValueFactory;
import org.openrdf.sail.generaldb.schema.BNodeTable;
import org.openrdf.sail.generaldb.schema.HashTable;
import org.openrdf.sail.generaldb.schema.LiteralTable;
import org.openrdf.sail.generaldb.schema.URITable;

/**
 * Facade to the underlying RDBMS optimizations.
 * 
 * @author James Leigh
 */
public class GeneralDBQueryOptimizer {

	private GeneralDBValueFactory vf;

	private URITable uris;

	private BNodeTable bnodes;

	private LiteralTable literals;

	private GeneralDBSelectQueryOptimizerFactory factory;

	private HashTable hashTable;
	
	//Addition to locate duplicate filters caused by the SpatialJoinOptimizer
	List<TupleExpr> spatialJoins = new ArrayList<TupleExpr>();
	//
	
	public void setSelectQueryOptimizerFactory(GeneralDBSelectQueryOptimizerFactory factory) {
		this.factory = factory;
	}

	public void setValueFactory(GeneralDBValueFactory vf) {
		this.vf = vf;
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

	public void setHashTable(HashTable hashTable) {
		this.hashTable = hashTable;
	}

	public TupleExpr optimize(TupleExpr expr, Dataset dataset, BindingSet bindings, EvaluationStrategy strategy)
	{
		// Clone the tuple expression to allow for more aggressive optimisations
		TupleExpr tupleExpr = expr.clone();

		if (!(tupleExpr instanceof QueryRoot)) {
			// Add a dummy root node to the tuple expressions to allow the
			// optimisers to modify the actual root node
			tupleExpr = new QueryRoot(tupleExpr);
		}

		fixAggregates(tupleExpr);
		coreOptimizations(strategy, tupleExpr, dataset, bindings);
		rdbmsOptimizations(tupleExpr, dataset, bindings);

		new GeneralDBSqlConstantOptimizer().optimize(tupleExpr, dataset, bindings);

		return tupleExpr;
	}

	private void fixAggregates(TupleExpr expr)
	{
		AggregateOptimizer agg = new AggregateOptimizer();
		agg.optimize(expr);
	}

	private void coreOptimizations(EvaluationStrategy strategy, TupleExpr expr, Dataset dataset,
			BindingSet bindings)
	{
		new BindingAssigner().optimize(expr, dataset, bindings);
		//addition
		new stSPARQLConstantOptimizer(strategy).optimize(expr, dataset, bindings);
		//
		new CompareOptimizer().optimize(expr, dataset, bindings);
		new ConjunctiveConstraintSplitter().optimize(expr, dataset, bindings);
		new DisjunctiveConstraintOptimizer().optimize(expr, dataset, bindings);
		new SameTermFilterOptimizer().optimize(expr, dataset, bindings);

		//XXX
		new SpatialJoinOptimizer().optimize(expr, dataset, bindings,spatialJoins);
	}

	protected void rdbmsOptimizations(TupleExpr expr, Dataset dataset, BindingSet bindings) {
		new GeneralDBValueIdLookupOptimizer(vf).optimize(expr, dataset, bindings);
		factory.createRdbmsFilterOptimizer().optimize(expr, dataset, bindings,spatialJoins);
		this.spatialJoins.clear();
		new GeneralDBVarColumnLookupOptimizer().optimize(expr, dataset, bindings);
		GeneralDBValueJoinOptimizer valueJoins = new GeneralDBValueJoinOptimizer();
		valueJoins.setBnodeTable(bnodes);
		valueJoins.setUriTable(uris);
		valueJoins.setLiteralTable(literals);
		valueJoins.setHashTable(hashTable);
		valueJoins.optimize(expr, dataset, bindings);

		new GeneralDBRegexFlagsInliner().optimize(expr, dataset, bindings);
	}

}
