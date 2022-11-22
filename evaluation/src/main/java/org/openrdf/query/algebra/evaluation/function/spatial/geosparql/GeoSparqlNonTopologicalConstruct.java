/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * 
 * Copyright (C) 2010, 2011, 2012, Pyravlos Team
 * 
 * http://www.strabon.di.uoa.gr/
 */
package org.openrdf.query.algebra.evaluation.function.spatial.geosparql;

import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.algebra.evaluation.ValueExprEvaluationException;
import org.openrdf.query.algebra.evaluation.function.spatial.SpatialConstructFunc;

/**
 * This class represents a spatial function like the one defined in its superclass 
 * {@link SpatialConstructFunc} class for the case of stSPARQL.
 * 
 * @see package {@link org.openrdf.query.algebra.evaluation.function.spatial.geosparql.nontopological}
 * 
 * @author Manos Karpathiotakis <mk@di.uoa.gr>
 * @author Charalampos Nikolaou <charnik@di.uoa.gr>
 */
public abstract class GeoSparqlNonTopologicalConstruct extends SpatialConstructFunc {

	//No need for any implementation, I will have replaced this class's presence before reaching this place
	public Value evaluate(ValueFactory valueFactory, Value... args)
	throws ValueExprEvaluationException {
		return null;
	}

	// charnik: made the method abstract
	public abstract String getURI();
}
