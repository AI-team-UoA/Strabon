/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * Copyright (C) 2012, Pyravlos Team
 *
 * http://www.strabon.di.uoa.gr/
 */
package org.openrdf.query.algebra.evaluation.function.spatial.stsparql.aggregate;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.algebra.evaluation.ValueExprEvaluationException;
import org.openrdf.query.algebra.evaluation.function.Function;

import eu.earthobservatory.constants.GeoConstants;

/**
 * @author Garmpis Georgios <ggarbis@di.uoa.gr>
 *
 */
public class DateTimeDiff implements Function {

protected static String name = "diffDatetime";
	
	@Override
	public String getURI() {
//		return GeoConstants.stRDF+name;
		return "http://strdf.di.uoa.gr/ontology#diffDateTime";
	}

	@Override
	public Value evaluate(ValueFactory valueFactory, Value... args)
            throws ValueExprEvaluationException {
        if (args.length != 2) {
            throw new ValueExprEvaluationException("strdf:" + name
                    + " requires exactly 2 arguments, got " + args.length);
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-DD'T'hh:mm:ss"); //the format of xsd:Datetime
		
		long diff = 0;
    	try {
    	    String date1 = args[0].toString();
    	    date1 = date1.replace("^^<http://www.w3.org/2001/XMLSchema#dateTime>", "");
    	    date1 = date1.replace("\"", "");
    	    Calendar cal1 = new GregorianCalendar();
    		cal1.setTime(sdf.parse(date1));
    		
    		String date2 = args[1].toString();
    		date2 = date2.replace("^^<http://www.w3.org/2001/XMLSchema#dateTime>", "");
    	    date2 = date2.replace("\"", "");
    	    Calendar cal2 = new GregorianCalendar();
    		cal2.setTime(sdf.parse(date2));
    		
    		diff = cal2.getTimeInMillis() - cal1.getTimeInMillis();
    		
    	} catch (java.text.ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
        return valueFactory.createLiteral(diff);
    }

	
}
