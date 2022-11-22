/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.generaldb.algebra.base;

/**
 * A boolean value of true or false.
 * 
 * @author James Leigh
 * 
 */
public abstract class GeneralDBBooleanValue extends GeneralDBSqlConstant<Boolean> {

	public GeneralDBBooleanValue(boolean value) {
		super(value);
	}
}
