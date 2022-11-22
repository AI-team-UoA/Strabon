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

import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.algebra.evaluation.ValueExprEvaluationException;
import org.openrdf.query.algebra.evaluation.function.Function;

/**
 * This class represents a function doing a metric computation on xsd:dateTime values
 * (e.g., calculating the difference between two xsd:dateTime values).
 * 
 * @see package {@link org.openrdf.query.algebra.evaluation.function.spatial.stsparql.metric}
 * 
 * @author George Garbis <ggarbis@di.uoa.gr>
 */
public abstract class DateTimeMetricFunc implements Function {

	//No need for any implementation, I will have replaced this class's presence before reaching this place
	public Value evaluate(ValueFactory valueFactory, Value... args) throws ValueExprEvaluationException {

		return null;
	}

	// charnik: made method (and hence the class) abstract
	public abstract String getURI();
}
