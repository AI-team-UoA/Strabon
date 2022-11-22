/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.generaldb.algebra;

import org.openrdf.query.algebra.Var;
import org.openrdf.sail.generaldb.algebra.base.GeneralDBQueryModelVisitorBase;
import org.openrdf.sail.generaldb.algebra.base.GeneralDBValueColumnBase;

/**
 * Represents a variable's long label value in an SQL expression.
 * 
 * @author James Leigh
 * 
 */
public class GeneralDBLongLabelColumn extends GeneralDBValueColumnBase {

private boolean spatial = false;
	
	public GeneralDBLongLabelColumn(Var var) {
		 
		super(var);
		
		if(var.getName().endsWith("?spatial"))
		{
			setSpatial(true);
			int whereToCut = var.getName().lastIndexOf("?");
			String originalName = var.getName().substring(0, whereToCut);
			var.setName(originalName);
			super.setVarName(originalName);
		}
	}

	public GeneralDBLongLabelColumn(GeneralDBColumnVar var) {
		super(var);
		
		if(var.isSpatial())
		{
			setSpatial(true);
		}
	}

	public boolean isSpatial() {
		return spatial;
	}

	public void setSpatial(boolean spatial) {
		this.spatial = spatial;
	}

	@Override
	public <X extends Exception> void visit(GeneralDBQueryModelVisitorBase<X> visitor)
		throws X
	{
		visitor.meet(this);
	}

}
