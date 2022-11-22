/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * 
 * Copyright (C) 2012, Pyravlos Team
 * 
 * http://www.strabon.di.uoa.gr/
 */
package org.openrdf.query.algebra.evaluation.function.link;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.algebra.evaluation.ValueExprEvaluationException;
import org.openrdf.query.algebra.evaluation.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Defines a function that advances a date-time value (first argument)
 * by a given integer number representing minutes (second argument).
 *  
 * @author Konstantina Bereta <Konstantina.Bereta@di.uoa.gr>
 */
public class AddDateTimeFunc implements Function {
	
	private static Logger logger = LoggerFactory.getLogger(org.openrdf.query.algebra.evaluation.function.link.AddDateTimeFunc.class);

	protected static String name = "addDateTime";
	
	@Override
	public String getURI() {
		return "http://example.org/custom-function/" + name;
		
	}

	public Value evaluate(ValueFactory valueFactory, Value... args) throws ValueExprEvaluationException {
        if (args.length != 2) {
            throw new ValueExprEvaluationException("strdf:" + name
                    + " requires exactly 2 arguments, got " + args.length);
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss"); //the format of xsd:Datetime
		GregorianCalendar cal = new GregorianCalendar(); 

    	try {
    		// get minutes, remove possible appearance of integer datatype, and strip double quotes
    		String minutes = args[1].toString().replace("^^<http://www.w3.org/2001/XMLSchema#integer>", "").replace("\"", "");
    		int minutesToAdd = Integer.parseInt(minutes);
    		
    	    String date = args[0].toString();
    	    
    	    // remove possible appearance of dateTime datatype and strip double quotes
    	    date = date.replace("^^<http://www.w3.org/2001/XMLSchema#dateTime>", "").replace("\"", "");
    	    
    	    // set the time (according to 1st argument)
    		cal.setTime(sdf.parse(date));
    		// add the minutes (according to 2nd argument)
    		cal.add(Calendar.MINUTE, minutesToAdd);
		
    	} catch (java.text.ParseException e) {
    		logger.error("[Strabon.AddDateTimeFunc] Error parsing the arguments of \"addDateTime\" extension function.", e);
		}
    
    	XMLGregorianCalendar gxml=null;
		try {
			gxml = DatatypeFactory.newInstance().newXMLGregorianCalendar(cal);
			
		} catch (DatatypeConfigurationException e) {
			logger.error("[Strabon.AddDateTimeFunc] Error constructing a new Datetime value.", e);
		}
		
		Value value =  valueFactory.createLiteral(gxml);
//		System.out.println("value="+value.toString());
		return value;
    }
}
