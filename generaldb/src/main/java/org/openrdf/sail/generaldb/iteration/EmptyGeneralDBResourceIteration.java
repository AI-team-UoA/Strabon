/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.generaldb.iteration;

import java.sql.SQLException;

import org.openrdf.sail.SailException;

/**
 * Empty iteration that extends {@link GeneralDBResourceIteration}.
 * 
 * @author James Leigh
 * 
 */
public class EmptyGeneralDBResourceIteration extends GeneralDBResourceIteration {

	public EmptyGeneralDBResourceIteration()
		throws SQLException
	{
		super(null, null);
	}

	/*@Override
	public void close()
		throws SailException
	{
	}*/

	@Override
	public boolean hasNext()
		throws SailException
	{
		return false;
	}

}
