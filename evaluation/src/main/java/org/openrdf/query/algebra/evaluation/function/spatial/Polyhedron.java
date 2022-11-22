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

import java.io.Serializable;


/**
 * @author Kostis Kyzirakos <kkyzir@di.uoa.gr>
 *
 */
public class Polyhedron implements Serializable {
	
	private static final long serialVersionUID = 76255052985367749L;
	
	public static final int OPTIMAL_CONVEX_PARTITION = 0; //Optimal number of pieces, O(n^4) time and O(n^3) space
	public static final int APPROXIMATE_CONVEX_PARTITION = 1; //Approximate optimal number of pieces, uses approximation algorithm of Hertel and Mehlhorn (triangulation), O(n) time and space.
	public static final int GREEN_CONVEX_PARTITION = 2; //Approximate optimal number of pieces, uses sweep-line approximation algorithm of Greene, O(n log(n)) time and O(n) space.
	public static final int Y_MONOTONE_PARTITION = 3; //Same complexity as Hertel and Mehlhorn, but can sometimes produce better results (i.e., convex partitions with fewer pieces).

	public static final String stRDFSemiLinearPointset="http://strdf.di.uoa.gr/ontology#SemiLinearPointSet";
	public static final String ogcGeometry="http://strdf.di.uoa.gr/ontology#WKT";

}
