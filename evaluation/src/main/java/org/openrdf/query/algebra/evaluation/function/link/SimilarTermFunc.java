/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * 
 * Copyright (C) 2010, 2011, 2012, Pyravlos Team
 * 
 * http://www.strabon.di.uoa.gr/
 */
package org.openrdf.query.algebra.evaluation.function.link;

import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.algebra.evaluation.ValueExprEvaluationException;
import org.openrdf.query.algebra.evaluation.function.Function;

import eu.earthobservatory.constants.GeoConstants;

/**
 * 
 * @author Manos Karpathiotakis <mk@di.uoa.gr>
 */
public class SimilarTermFunc implements Function {
	
	protected static String name = "similarTerm";
	
	@Override
	public String getURI() {
		return GeoConstants.stRDF+name;
	}

    public Value evaluate(ValueFactory valueFactory, Value... args)
            throws ValueExprEvaluationException {
        if (args.length != 2) {
            throw new ValueExprEvaluationException("strdf:" + name
                    + " requires exactly 2 arguments, got " + args.length);
        }

        return valueFactory.createLiteral(true);
    }

}
