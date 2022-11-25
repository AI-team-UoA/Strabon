/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * 
 * Copyright (C) 2010, 2011, 2012, Pyravlos Team
 * 
 * http://www.strabon.di.uoa.gr/
 */
package eu.earthobservatory.utils;

import java.io.OutputStream;

import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.binary.BinaryRDFWriter;
import org.openrdf.rio.n3.N3Writer;
import org.openrdf.rio.ntriples.NTriplesWriter;
import org.openrdf.rio.rdfxml.util.RDFXMLPrettyWriter;
import org.openrdf.rio.trig.TriGWriter;
import org.openrdf.rio.trix.TriXWriter;
import org.openrdf.rio.turtle.TurtleWriter;

/**
 * Factory class for creating instances of RDFHandler class
 * based on the given format, which should be one of the formats
 * mentioned in {@link org.openrdf.rio.RDFFormat} class, and 
 * an OutputStream to which the handler should write to.
 * 
 * @author Charalampos Nikolaou <charnik@di.uoa.gr>
 */
public class RDFHandlerFactory {

	public static RDFHandler createRDFHandler(String format, OutputStream out) {
		RDFHandler handler = null;
		//RDFFomrat rdfFormat = RDFFormat.valueOf(format);
		RDFFormat rdfFormat;
		if(format.equals("text/n3") || format.equals("N3") || format.equals(RDFFormat.N3.getName()))
			rdfFormat = RDFFormat.N3;
		else if(format.equals("application/rdf+xml") || format.equals("RDFXML") || format.equals(RDFFormat.RDFXML.getName()))
			rdfFormat = RDFFormat.RDFXML;
		else if(format.equals("text/turtle") || format.equals("TURTLE") || format.equals(RDFFormat.TURTLE.getName()))
			rdfFormat = RDFFormat.TURTLE;
		else if(format.equals("application/trig") || format.equals("TRIG") || format.equals(RDFFormat.TRIG.getName()))
			rdfFormat = RDFFormat.TRIG;
		else if(format.equals("application/trix") || format.equals("TRIX") || format.equals(RDFFormat.TRIX.getName()))
			rdfFormat = RDFFormat.TRIX;
		else if(format.equals("application/x-binary-rdf") || format.equals("BINARY") || format.equals(RDFFormat.BINARY.getName()))
			rdfFormat = RDFFormat.BINARY;
		else if(format.equals("application/n-triples") || format.equals("NTRIPLES") || format.equals(RDFFormat.NTRIPLES.getName()))
			rdfFormat = RDFFormat.NTRIPLES;
		else
			rdfFormat = null;
		
		if (rdfFormat == RDFFormat.NTRIPLES || rdfFormat == null) {
			handler = new NTriplesWriter(out);
			
		} else if (rdfFormat == RDFFormat.N3) {
			handler = new N3Writer(out);
			
		} else if (rdfFormat == RDFFormat.RDFXML) {
			handler = new RDFXMLPrettyWriter(out);
			
		} else if (rdfFormat == RDFFormat.TURTLE) {
			handler = new TurtleWriter(out);
			
		} else if (rdfFormat == RDFFormat.TRIG) {
			handler = new TriGWriter(out);
			
		} else if (rdfFormat == RDFFormat.TRIX) {
			handler = new TriXWriter(out);
			
		} else if (rdfFormat == RDFFormat.BINARY) {
			handler = new BinaryRDFWriter(out);
			
		}
		
		return handler;
	}
}
