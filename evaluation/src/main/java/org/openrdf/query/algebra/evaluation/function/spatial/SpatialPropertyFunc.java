/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * 
 * Copyright (C) 2010, 2011, 2012, Pyravlos Team
 * 
 * http://www.strabon.di.uoa.gr/
 */
package org.openrdf.query.algebra.evaluation.function.spatial;

import org.apache.log4j.Logger;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.algebra.evaluation.ValueExprEvaluationException;
import org.openrdf.query.algebra.evaluation.function.Function;

/**
 * This class represents a spatial function returning/computing a property
 * on a geometry, such as its dimension (0 for points, 1 for line segments
 * and so on), its type (Polygon, Point, etc.), SRID, etc.
 * 
 * @see package {@link org.openrdf.query.algebra.evaluation.function.spatial.stsparql.property}
 * @see package {@link org.openrdf.query.algebra.evaluation.function.spatial.geosparql.property}
 * 
 * @author Manos Karpathiotakis <mk@di.uoa.gr>
 * @author Charalampos Nikolaou <charnik@di.uoa.gr>
 *
 */
public abstract class SpatialPropertyFunc implements Function {

	private static Logger logger = Logger.getLogger(org.openrdf.query.algebra.evaluation.function.spatial.SpatialPropertyFunc.class);
	
	//No need for any implementation, I will have replaced this class's presence before reaching this place
	public Value evaluate(ValueFactory valueFactory, Value... args)
	throws ValueExprEvaluationException {
		logger.error(this.getURI() + ": I should have been taken care of before in GeneralDBEvaluation.evaluate(FunctionCall, BindingSet).");
		return null;
	}

	public abstract String getURI();
}
