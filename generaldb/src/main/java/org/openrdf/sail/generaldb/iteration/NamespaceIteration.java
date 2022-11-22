/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.generaldb.iteration;

import java.util.Iterator;

import info.aduna.iteration.CloseableIteration;
import info.aduna.iteration.Iteration;
import info.aduna.iteration.IteratorIteration;

import org.openrdf.model.Namespace;
import org.openrdf.sail.SailException;

/**
 * {@link Namespace} typed {@link Iteration}.
 * 
 * @author James Leigh
 * 
 */
public class NamespaceIteration extends IteratorIteration<Namespace, SailException> implements
		CloseableIteration<Namespace, SailException>
{

	public NamespaceIteration(Iterator<? extends Namespace> iter) {
		super(iter);
	}

	public void close()
		throws SailException
	{
		// do nothing
	}

}
