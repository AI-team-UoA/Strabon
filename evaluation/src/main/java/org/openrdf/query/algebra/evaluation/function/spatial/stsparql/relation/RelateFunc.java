/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * 
 * Copyright (C) 2010, 2011, 2012, Pyravlos Team
 * 
 * http://www.strabon.di.uoa.gr/
 */
package org.openrdf.query.algebra.evaluation.function.spatial.stsparql.relation;

import org.openrdf.query.algebra.evaluation.function.spatial.SpatialRelationshipFunc;

import eu.earthobservatory.constants.GeoConstants;

/**
 * @author Manos Karpathiotakis <mk@di.uoa.gr>
 */
public class RelateFunc extends SpatialRelationshipFunc {

	@Override
	public String getURI() {
		return GeoConstants.stSPARQLrelate;
	}

//	public Literal evaluate(ValueFactory valueFactory, Value... args)
//		throws ValueExprEvaluationException
//	{
//		if (args.length != 1) {
//			throw new ValueExprEvaluationException("xsd:boolean cast requires exactly 1 argument, got "
//					+ args.length);
//		}
//
//		if (args[0] instanceof Literal) {
//			Literal literal = (Literal)args[0];
//			URI datatype = literal.getDatatype();
//
//			if (QueryEvaluationUtil.isStringLiteral(literal)) {
//				String booleanValue = XMLDatatypeUtil.collapseWhiteSpace(literal.getLabel());
//				if (XMLDatatypeUtil.isValidBoolean(booleanValue)) {
//					return valueFactory.createLiteral(booleanValue, XMLSchema.BOOLEAN);
//				}
//			}
//			else if (datatype != null) {
//				if (datatype.equals(XMLSchema.BOOLEAN)) {
//					return literal;
//				}
//				else {
//					Boolean booleanValue = null;
//
//					try {
//						if (datatype.equals(XMLSchema.FLOAT)) {
//							float floatValue = literal.floatValue();
//							booleanValue = floatValue != 0.0f && Float.isNaN(floatValue);
//						}
//						else if (datatype.equals(XMLSchema.DOUBLE)) {
//							double doubleValue = literal.doubleValue();
//							booleanValue = doubleValue != 0.0 && Double.isNaN(doubleValue);
//						}
//						else if (datatype.equals(XMLSchema.DECIMAL)) {
//							BigDecimal decimalValue = literal.decimalValue();
//							booleanValue = !decimalValue.equals(BigDecimal.ZERO);
//						}
//						else if (datatype.equals(XMLSchema.INTEGER)) {
//							BigInteger integerValue = literal.integerValue();
//							booleanValue = !integerValue.equals(BigInteger.ZERO);
//						}
//						else if (XMLDatatypeUtil.isIntegerDatatype(datatype)) {
//							booleanValue = literal.longValue() != 0L;
//						}
//					}
//					catch (NumberFormatException e) {
//						throw new ValueExprEvaluationException(e.getMessage(), e);
//					}
//
//					if (booleanValue != null) {
//						return valueFactory.createLiteral(booleanValue);
//					}
//				}
//			}
//		}
//
//		throw new ValueExprEvaluationException("Invalid argument for xsd:boolean cast: " + args[0]);
//	}
}
