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
 * Represents a variable's label value in an SQL expression.
 * 
 * @author James Leigh
 * 
 */
public class GeneralDBLabelColumn extends GeneralDBValueColumnBase {

	private boolean spatial = false;
	
	public GeneralDBLabelColumn(Var var) {
		 
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

	public GeneralDBLabelColumn(GeneralDBColumnVar var) {
		super(var);
		
		if(var.isSpatial())
		{
			setSpatial(true);
		}
	}
	
	public GeneralDBLabelColumn(GeneralDBColumnVar var, boolean isSpatial) {
		super(var);
		
		setSpatial(isSpatial);
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
