/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * 
 * Copyright (C) 2010, 2011, 2012, Pyravlos Team
 * 
 * http://www.strabon.di.uoa.gr/
 */
package org.openrdf.query.algebra.evaluation.function.spatial;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateFilter;

public class ProjectionsFilter implements CoordinateFilter {

	int[] dims = new int[3];
	
	
	
	public ProjectionsFilter(int[] dims) {
		super();
		this.dims = dims;
	}



	public int[] getDims() {
		return dims;
	}



	public void setDims(int[] dims) {
		this.dims = dims;
	}

	public void filter(Coordinate coord) {
		
		if(dims[0]==0)
		{
			coord.x=0;
		}
		
		if(dims[1]==0)
		{
			coord.y=0;
		}
		
		if(dims[2]==0)
		{
			coord.z=0;
		}

	}

}
