/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * 
 * Copyright (C) 2010, 2011, 2012, Pyravlos Team
 * 
 * http://www.strabon.di.uoa.gr/
 */
package org.openrdf.query.resultio.sparqlxml;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

import info.aduna.xml.XMLWriter;

/**
 * @author Charalampos Nikolaou <charnik@di.uoa.gr>
 *
 */
public class stSPARQLXMLWriter extends XMLWriter {

	/**
	 * @param writer
	 */
	public stSPARQLXMLWriter(Writer writer) {
		super(writer);
	}

	/**
	 * @param outputStream
	 */
	public stSPARQLXMLWriter(OutputStream outputStream) {
		super(outputStream);
	}

	/**
	 * @param outputStream
	 * @param charEncoding
	 * @throws UnsupportedEncodingException
	 */
	public stSPARQLXMLWriter(OutputStream outputStream, String charEncoding) throws UnsupportedEncodingException {
		super(outputStream, charEncoding);
	}
	
	/**
	 * Like XMLWriter.text(String text) but without escaping the string.
	 */
	public void unescapedText(String text) throws IOException {
		_write(text);
	}

}
