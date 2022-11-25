/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.generaldb.evaluation.util;

import java.util.Comparator;

import info.aduna.lang.ObjectUtil;

import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.datatypes.XMLDatatypeUtil;
import org.openrdf.query.algebra.Compare.CompareOp;
import org.openrdf.query.algebra.evaluation.ValueExprEvaluationException;
import org.openrdf.query.algebra.evaluation.function.spatial.StrabonPolyhedron;
import org.openrdf.query.algebra.evaluation.util.QueryEvaluationUtil;
import org.openrdf.sail.generaldb.model.GeneralDBPolyhedron;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

/**
 * A comparator that compares values according the SPARQL value ordering as
 * specified in <A
 * href="http://www.w3.org/TR/rdf-sparql-query/#modOrderBy">SPARQL Query
 * Language for RDF</a>.
 * 
 * @author james
 * @author Arjohn Kampman
 */
public class StSPARQLValueComparator implements Comparator<Value> {

	public int compare(Value o1, Value o2) {
		// check equality
		if (ObjectUtil.nullEquals(o1, o2)) {
			return 0;
		}

		// 1. (Lowest) no value assigned to the variable
		if (o1 == null) {
			return -1;
		}
		if (o2 == null) {
			return 1;
		}

		// 2. Blank nodes
		boolean b1 = o1 instanceof BNode;
		boolean b2 = o2 instanceof BNode;
		if (b1 && b2) {
			return 0;
		}
		if (b1) {
			return -1;
		}
		if (b2) {
			return 1;
		}

		// 3. URIs
		boolean u1 = o1 instanceof URI;
		boolean u2 = o2 instanceof URI;
		if (u1 && u2) {
			return compareURIs((URI)o1, (URI)o2);
		}
		if (u1) {
			return -1;
		}
		if (u2) {
			return 1;
		}

		boolean p1 = o1 instanceof GeneralDBPolyhedron;
		boolean p2 = o2 instanceof GeneralDBPolyhedron;

		if(p1&&p2) {
			return comparePolyhedra(((GeneralDBPolyhedron)o1).getPolyhedron(), ((GeneralDBPolyhedron)o2).getPolyhedron());
		}
		if (p1) {
			return -1;
		}
		if (p2) {
			return 1;
		}

		boolean str1 = o1 instanceof StrabonPolyhedron;
		boolean str2 = o2 instanceof StrabonPolyhedron;
		
		if(p1&&p2) {
			return comparePolyhedra((StrabonPolyhedron)o1, (StrabonPolyhedron)o2);
		}
		if (str1) {
			return -1;
		}
		if (str2) {
			return 1;
		}
		
		// 4. RDF literals
		return compareLiterals((Literal)o1, (Literal)o2);
	}

	private int compareURIs(URI leftURI, URI rightURI) {
		return leftURI.toString().compareTo(rightURI.toString());
	}

	private int compareLiterals(Literal leftLit, Literal rightLit) {
		// Additional constraint for ORDER BY: "A plain literal is lower
		// than an RDF literal with type xsd:string of the same lexical
		// form."

		if (!QueryEvaluationUtil.isStringLiteral(leftLit) || !QueryEvaluationUtil.isStringLiteral(rightLit)) {
			try {
				boolean isSmaller = QueryEvaluationUtil.compareLiterals(leftLit, rightLit, CompareOp.LT);

				if (isSmaller) {
					return -1;
				}
				else {
					return 1;
				}
			}
			catch (ValueExprEvaluationException e) {
				// literals cannot be compared using the '<' operator, continue
				// below
			}
		}

		int result = 0;

		// Sort by datatype first, plain literals come before datatyped literals
		URI leftDatatype = leftLit.getDatatype();
		URI rightDatatype = rightLit.getDatatype();

		if (leftDatatype != null) {
			if (rightDatatype != null) {
				// Both literals have datatypes
				result = compareDatatypes(leftDatatype, rightDatatype);
			}
			else {
				result = 1;
			}
		}
		else if (rightDatatype != null) {
			result = -1;
		}

		if (result == 0) {
			// datatypes are equal or both literals are untyped; sort by language
			// tags, simple literals come before literals with language tags
			String leftLanguage = leftLit.getLanguage();
			String rightLanguage = rightLit.getLanguage();

			if (leftLanguage != null) {
				if (rightLanguage != null) {
					result = leftLanguage.compareTo(rightLanguage);
				}
				else {
					result = 1;
				}
			}
			else if (rightLanguage != null) {
				result = -1;
			}
		}

		if (result == 0) {
			// Literals are equal as fas as their datatypes and language tags are
			// concerned, compare their labels
			result = leftLit.getLabel().compareTo(rightLit.getLabel());
		}

		return result;
	}

	/**
	 * Compares two literal datatypes and indicates if one should be ordered
	 * after the other. This algorithm ensures that compatible ordered datatypes
	 * (numeric and date/time) are grouped together so that
	 * {@link QueryEvaluationUtil#compareLiterals(Literal, Literal, CompareOp)}
	 * is used in consecutive ordering steps.
	 */
	private int compareDatatypes(URI leftDatatype, URI rightDatatype) {
		if (XMLDatatypeUtil.isNumericDatatype(leftDatatype)) {
			if (XMLDatatypeUtil.isNumericDatatype(rightDatatype)) {
				// both are numeric datatypes
				return compareURIs(leftDatatype, rightDatatype);
			}
			else {
				return -1;
			}
		}
		else if (XMLDatatypeUtil.isNumericDatatype(rightDatatype)) {
			return 1;
		}
		else if (XMLDatatypeUtil.isCalendarDatatype(leftDatatype)) {
			if (XMLDatatypeUtil.isCalendarDatatype(rightDatatype)) {
				// both are calendar datatypes
				return compareURIs(leftDatatype, rightDatatype);
			}
			else {
				return -1;
			}
		}
		else if (XMLDatatypeUtil.isCalendarDatatype(rightDatatype)) {
			return 1;
		}
		else {
			// incompatible or unordered datatypes
			return compareURIs(leftDatatype, rightDatatype);
		}
	}

	private int comparePolyhedra(StrabonPolyhedron p1, StrabonPolyhedron p2)
	{
		Geometry p1geom = p1.getGeometry();
		Geometry p2geom = p2.getGeometry();

		Coordinate[] p1coords= p1geom.getCoordinates();
		Coordinate[] p2coords= p2geom.getCoordinates();

		double x1_min = Double.MIN_VALUE;
		double x1_max = Double.MAX_VALUE;
		double y1_min = Double.MIN_VALUE;
		double y1_max = Double.MAX_VALUE;
		
		double x2_min = Double.MIN_VALUE;
		double x2_max = Double.MAX_VALUE;
		double y2_min = Double.MIN_VALUE;
		double y2_max = Double.MAX_VALUE;

		for(Coordinate coord : p1coords)
		{
			if(coord.x < x1_min)
			{
				x1_min = coord.x;
			}
			if(coord.x > x1_max)
			{
				x1_max = coord.x;
			}
			if(coord.y < y1_min)
			{
				y1_min = coord.y;
			}
			if(coord.y > y1_max)
			{
				y1_max = coord.y;
			}
		}
		
		for(Coordinate coord : p2coords)
		{
			if(coord.x < x2_min)
			{
				x2_min = coord.x;
			}
			if(coord.x > x2_max)
			{
				x2_max = coord.x;
			}
			if(coord.y < y2_min)
			{
				y2_min = coord.y;
			}
			if(coord.y > y2_max)
			{
				y2_max = coord.y;
			}
		}

		//Implementing algorithm found in GeneralDB' btree implementation
		if(largerMBB(x1_min, x1_max, y1_min, y1_max, x2_min, x2_max, y2_min, y2_max))
			return 1;
		else if(equalMBB(x1_min, x1_max, y1_min, y1_max, x2_min, x2_max, y2_min, y2_max))
			return 0;
		else //if(largerMBB(x2_min, x2_max, y2_min, y2_max, x1_min, x1_max, y1_min, y1_max))
		{
			return -1;
		}	
		
	}

	private boolean largerMBB(double x1_min, double x1_max, double y1_min, double y1_max,
			double x2_min, double x2_max, double y2_min, double y2_max)
	{
		if(x1_min != x2_min)
		{
			if(x1_min > x2_min)
				return true;
		}

		if(y1_min!=y2_min)
		{
			if(y1_min > y2_min)
				return true;
		}

		if(x1_max!=x2_max)
		{
			if(x1_max > x2_max)
				return true;
		}

		if(y1_max!=y2_max)
		{
			if(y1_max > y2_max)
				return true;
		}

		return false;
	}

	private boolean equalMBB(double x1_min, double x1_max, double y1_min, double y1_max,
			double x2_min, double x2_max, double y2_min, double y2_max)
	{
		if(x1_min != x2_min)
		{
			return false;
		}

		if(y1_min!=y2_min)
		{
			return false;
		}

		if(x1_max!=x2_max)
		{
			return false;
		}

		if(y1_max!=y2_max)
		{
			return false;
		}

		return true;
	}
}