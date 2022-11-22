/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.monetdb.evaluation;

import info.aduna.iteration.CloseableIteration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.openrdf.query.BindingSet;
import org.openrdf.query.Dataset;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.algebra.evaluation.QueryBindingSet;
import org.openrdf.sail.generaldb.GeneralDBTripleRepository;
import org.openrdf.sail.generaldb.algebra.GeneralDBColumnVar;
import org.openrdf.sail.generaldb.algebra.GeneralDBSelectQuery;
import org.openrdf.sail.generaldb.evaluation.GeneralDBEvaluation;
import org.openrdf.sail.generaldb.evaluation.GeneralDBQueryBuilderFactory;
import org.openrdf.sail.generaldb.iteration.GeneralDBBindingIteration;
import org.openrdf.sail.generaldb.schema.IdSequence;
import org.openrdf.sail.monetdb.iteration.MonetDBBindingIteration;
import org.openrdf.sail.rdbms.exceptions.RdbmsException;
import org.openrdf.sail.rdbms.exceptions.RdbmsQueryEvaluationException;
import org.openrdf.sail.rdbms.exceptions.UnsupportedRdbmsOperatorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extends the default strategy by accepting {@link GeneralDBSelectQuery} and evaluating
 * them on a database.
 * 
 * @author James Leigh
 * 
 */
public class MonetDBEvaluation extends GeneralDBEvaluation {

	private static Logger logger = LoggerFactory.getLogger(org.openrdf.sail.monetdb.evaluation.MonetDBEvaluation.class);

	public MonetDBEvaluation(GeneralDBQueryBuilderFactory factory, GeneralDBTripleRepository triples, Dataset dataset,
			IdSequence ids)
	{
		super(factory, triples, dataset, ids);
		logger = LoggerFactory.getLogger(MonetDBEvaluation.class);
		this.factory = factory;
	}

	@Override
	protected CloseableIteration<BindingSet, QueryEvaluationException> evaluate(GeneralDBSelectQuery qb, BindingSet b)
	throws UnsupportedRdbmsOperatorException, RdbmsQueryEvaluationException
	{
		List<Object> parameters = new ArrayList<Object>();
		try {
			QueryBindingSet bindings = new QueryBindingSet(b);
			String query = toQueryString(qb, bindings, parameters);
			// FIXME MonetDB doesn't handle outer joins correctly so I replace them with inner
//			query = query.replace("LEFT", "INNER");
			
			try {
				Connection conn = triples.getConnection();
				PreparedStatement stmt = conn.prepareStatement(query);
				int p = 0;
				for (Object o : parameters) {
					if ( o instanceof String ) {
						stmt.setString(++p, (String)o); // TODO this must be extended with all types 
					}
					else if ( o instanceof Double ){
						stmt.setDouble(++p, (Double)o); // TODO this must be extended with all types
					}
					else {
						stmt.setObject(++p, o);
					}
				}
				Collection<GeneralDBColumnVar> proj = qb.getProjections();
				GeneralDBBindingIteration result = new MonetDBBindingIteration(stmt);
				result.setProjections(proj);
				result.setBindings(bindings);
				result.setValueFactory(vf);
				result.setIdSequence(ids);
				//XXX addition
				result.setGeoNames(this.geoNames);
				result.setConstructIndexesAndNames(this.constructIndexesAndNames);
				//

				if (logger.isDebugEnabled()) {
					logger.debug("In MonetDB Evaluation, query is: \n{}", query);
				}
				// In MonetDb this stmt.toString() returns just a reference
				return result;
			}
			catch (SQLException e) {
				throw new RdbmsQueryEvaluationException(e.toString() + "\n" + query, e);
			}
		}
		catch (RdbmsException e) {
			throw new RdbmsQueryEvaluationException(e);
		}
	}
}