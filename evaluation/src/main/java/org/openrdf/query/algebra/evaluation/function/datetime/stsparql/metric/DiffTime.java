/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * Copyright (C) 2013, Pyravlos Team
 *
 * http://www.strabon.di.uoa.gr/
 */
package org.openrdf.query.algebra.evaluation.function.datetime.stsparql.metric;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.query.algebra.evaluation.ValueExprEvaluationException;
import org.openrdf.query.algebra.evaluation.function.spatial.DateTimeMetricFunc;

import eu.earthobservatory.constants.GeoConstants;

/** * 
 * 
 * @author Konstantina Bereta <Konstantina.Bereta@di.uoa.gr>
 * 
 */
public class DiffTime extends DateTimeMetricFunc {

	// This functions returns the difference in msecs of two xsd:dateTimes
	
	@Override
	public String getURI() {
		return GeoConstants.diffTime;
	}
	
	@Override
	public Value evaluate(ValueFactory valueFactory, Value... args)
            throws ValueExprEvaluationException {
        if (args.length != 2) {
            throw new ValueExprEvaluationException(this.getURI()
                    + " requires exactly 2 arguments, got " + args.length);
        }
		String datatype = "<http://www.w3.org/2001/XMLSchema#time>";
		String timeFormat = "hh:mm:ss";
        SimpleDateFormat sdf = new SimpleDateFormat(timeFormat); //the format of xsd:Datetime
        String newTime=null;

		long diff = 0;
    	try {

    		String date1 = args[0].toString();
    	    date1 = date1.replace("^^"+datatype, "");
    	    date1 = date1.replace("\"", "");
    	    Calendar cal1 = new GregorianCalendar();
    		cal1.setTime(sdf.parse(date1));
    		
    		String date2 = args[1].toString();
    		date2 = date2.replace("^^"+datatype, "");
   	    date2 = date2.replace("\"", "");
    	    Calendar cal2 = new GregorianCalendar();
    		cal2.setTime(sdf.parse(date2));
    		
    		diff = cal2.getTimeInMillis() - cal1.getTimeInMillis();
    		
    		 newTime = sdf.format(diff).toString();
    		

    	} catch (java.text.ParseException e) {

			e.printStackTrace();
		}
    	
        return valueFactory.createLiteral(newTime, new URIImpl(datatype));
    }

}
