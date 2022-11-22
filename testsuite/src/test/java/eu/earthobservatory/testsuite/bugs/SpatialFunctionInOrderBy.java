/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * 
 * Copyright (C) 2010, 2011, 2012, 2013, 2014 Pyravlos Team
 * 
 * http://www.strabon.di.uoa.gr/
 */
package eu.earthobservatory.testsuite.bugs;

import eu.earthobservatory.testsuite.utils.TemplateTest;


/**
 * Test for Bug #62 (http://bug.strabon.di.uoa.gr/ticket/62).
 * 
 * @author Dimitrianos Savva <dimis@di.uoa.gr>
 */
public class SpatialFunctionInOrderBy extends TemplateTest{
	public SpatialFunctionInOrderBy() {
		super();
		this.orderResults=true;
	}
}
