package org.openrdf.sail.generaldb.model;

import javax.xml.datatype.XMLGregorianCalendar;

import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.query.algebra.evaluation.function.spatial.StrabonPolyhedron;

import eu.earthobservatory.constants.GeoConstants;
import eu.earthobservatory.constants.WKTConstants;


public class XMLGSDatatypeUtil {
	
	/**
	 * Returns true when the given value is an instance of class @{link GeneralDBPolyhedron} 
	 * or @{link Literal} with datatype @{link StrabonPolyhedron#ogcGeometry} (WKT) or 
	 * @{link StrabonPolyhedron#gml} (GML).  
	 * 
	 * @param value
	 * @return
	 * @author Charalampos Nikolaou <charniK@di.uoa.gr>
	 */
	public static boolean isGeometryValue(Value value) {
		if (value instanceof Literal) {
			Literal literal = (Literal) value;

			if (isWKTLiteral(literal) || isGMLLiteral(literal)) {
				return true;
			}
			
		} else if (value instanceof GeneralDBPolyhedron || value instanceof StrabonPolyhedron) {
			return true;
		}
		
		return false;
	}
	
	/**
	 * Returns true when the given literal has as datatype the WKT URI as it is
	 * in @{link StrabonPolyhedron.WKT}.
	 * 
	 * @param literal
	 * @return
	 */
	public static boolean isWKTLiteral(Literal literal) {
		if (literal.getDatatype() == null){
			return containsWKTGeometry(literal);
		} else {
			return isWKTDatatype(literal.getDatatype());
		}		
	}
	
	/**
	 * Returns true when the given literal has as datatype the GML URI as it is
	 * in @{link StrabonPolyhedron.GML}.
	 * 
	 * @param literal
	 * @return
	 */
	public static boolean isGMLLiteral(Literal literal) {
		return isGMLDatatype(literal.getDatatype());
	}
	
	/**
	 * Checks whether the supplied datatype is actually a WKT literal.
	 * 
	 * @param datatype
	 * @return
	 */
	public static boolean isWKTDatatype(URI datatype) {
		if(datatype == null) {
			return false;
		}
		
		return GeoConstants.WKT.equals(datatype.stringValue()) || 
				GeoConstants.WKTLITERAL.equals(datatype.stringValue());
	}
	
	//TODO: seems a bit quick and dirty
	
	public static boolean containsWKTGeometry(Literal literal){
		return
		literal.stringValue().contains(WKTConstants.WKTPOINT)||
		literal.stringValue().contains(WKTConstants.WKTLINESTRING)||
		literal.stringValue().contains(WKTConstants.WKTLINEARRING)||
		literal.stringValue().contains(WKTConstants.WKTPOLYGON)||
		literal.stringValue().contains(WKTConstants.WKTMULTIPOINT)||
		literal.stringValue().contains(WKTConstants.WKTMULTILINESTRING)||
		literal.stringValue().contains(WKTConstants.WKTGEOMETRYCOLLECTION)||
		literal.stringValue().contains(WKTConstants.WKTMULTIPOLYGON);
	}
	
	/**
	 * Checks whether the supplied datatype is actually a GML literal.
	 * 
	 * @param datatype
	 * @return
	 */
	public static boolean isGMLDatatype(URI datatype)
	{
		if(datatype == null) {
			return false;
		}
	
		return GeoConstants.GML.equals(datatype.stringValue());
	}
	
	/**
	 * Checks whether the supplied datatype is actually a SemiLinearPointSet literal.
	 * 
	 * @param datatype
	 * @return
	 */
	public static boolean isSemiLinearPointSetDatatype(URI datatype) {
		if(datatype == null) {
			return false;
		}
		
		return datatype.toString().equals("http://stsparql.di.uoa.gr/SemiLinearPointSet");
		//return datatype.toString().equals(StrabonPolyhedron.stRDFSemiLinearPointset);
	}
	
	/**
	 * Checks whether the supplied datatype is a primitive XML Schema datatype.
	 */
	public static boolean isPrimitiveDatatype(URI datatype) {
		return
		XMLSchema.DURATION.equals(datatype)||
		XMLSchema.DATETIME.equals(datatype) ||
		XMLSchema.TIME.equals(datatype) ||
		XMLSchema.DATE.equals(datatype) ||
		XMLSchema.GYEARMONTH.equals(datatype) ||
		XMLSchema.GYEAR.equals(datatype) ||
		XMLSchema.GMONTHDAY.equals(datatype) ||
		XMLSchema.GDAY.equals(datatype) ||
		XMLSchema.GMONTH.equals(datatype) ||
		XMLSchema.STRING.equals(datatype) ||
		XMLSchema.BOOLEAN.equals(datatype) ||
		XMLSchema.BASE64BINARY.equals(datatype) ||
		XMLSchema.HEXBINARY.equals(datatype) ||
		XMLSchema.FLOAT.equals(datatype) ||
		XMLSchema.DECIMAL.equals(datatype) ||
		XMLSchema.DOUBLE.equals(datatype) ||
		XMLSchema.ANYURI.equals(datatype) ||
		XMLSchema.QNAME.equals(datatype) ||
		XMLSchema.NOTATION.equals(datatype);
	}

	/**
	 * Checks whether the supplied datatype is a derived XML Schema datatype.
	 */
	public static boolean isDerivedDatatype(URI datatype) {
		return
		XMLSchema.NORMALIZEDSTRING.equals(datatype) ||
		XMLSchema.TOKEN.equals(datatype) ||
		XMLSchema.LANGUAGE.equals(datatype) ||
		XMLSchema.NMTOKEN.equals(datatype) ||
		XMLSchema.NMTOKENS.equals(datatype) ||
		XMLSchema.NAME.equals(datatype) ||
		XMLSchema.NCNAME.equals(datatype) ||
		XMLSchema.ID.equals(datatype) ||
		XMLSchema.IDREF.equals(datatype) ||
		XMLSchema.IDREFS.equals(datatype) ||
		XMLSchema.ENTITY.equals(datatype) ||
		XMLSchema.ENTITIES.equals(datatype) ||
		XMLSchema.INTEGER.equals(datatype) ||
		XMLSchema.LONG.equals(datatype) ||
		XMLSchema.INT.equals(datatype) ||
		XMLSchema.SHORT.equals(datatype) ||
		XMLSchema.BYTE.equals(datatype) ||
		XMLSchema.NON_POSITIVE_INTEGER.equals(datatype) ||
		XMLSchema.NEGATIVE_INTEGER.equals(datatype) ||
		XMLSchema.NON_NEGATIVE_INTEGER.equals(datatype) ||
		XMLSchema.POSITIVE_INTEGER.equals(datatype) ||
		XMLSchema.UNSIGNED_LONG.equals(datatype) ||
		XMLSchema.UNSIGNED_INT.equals(datatype) ||
		XMLSchema.UNSIGNED_SHORT.equals(datatype) ||
		XMLSchema.UNSIGNED_BYTE.equals(datatype);
	}

	/**
	 * Checks whether the supplied datatype is a built-in XML Schema datatype.
	 */
	public static boolean isBuiltInDatatype(URI datatype) {
		return isPrimitiveDatatype(datatype) || isDerivedDatatype(datatype);
	}

	/**
	 * Checks whether the supplied datatype is a numeric datatype, i.e. if it is
	 * equal to xsd:float, xsd:double, xsd:decimal or one of the datatypes
	 * derived from xsd:decimal.
	 */
	public static boolean isNumericDatatype(URI datatype) {
		return isDecimalDatatype(datatype) || isFloatingPointDatatype(datatype);
	}

	/**
	 * Checks whether the supplied datatype is equal to xsd:decimal or one of the
	 * built-in datatypes that is derived from xsd:decimal.
	 */
	public static boolean isDecimalDatatype(URI datatype) {
		return
		XMLSchema.DECIMAL.equals(datatype) ||
		isIntegerDatatype(datatype);
	}

	/**
	 * Checks whether the supplied datatype is equal to xsd:integer or one of the
	 * built-in datatypes that is derived from xsd:integer.
	 */
	public static boolean isIntegerDatatype(URI datatype) {
		return
		XMLSchema.INTEGER.equals(datatype) ||
		XMLSchema.LONG.equals(datatype) ||
		XMLSchema.INT.equals(datatype) ||
		XMLSchema.SHORT.equals(datatype) ||
		XMLSchema.BYTE.equals(datatype) ||
		XMLSchema.NON_POSITIVE_INTEGER.equals(datatype) ||
		XMLSchema.NEGATIVE_INTEGER.equals(datatype) ||
		XMLSchema.NON_NEGATIVE_INTEGER.equals(datatype) ||
		XMLSchema.POSITIVE_INTEGER.equals(datatype) ||
		XMLSchema.UNSIGNED_LONG.equals(datatype) ||
		XMLSchema.UNSIGNED_INT.equals(datatype) ||
		XMLSchema.UNSIGNED_SHORT.equals(datatype) ||
		XMLSchema.UNSIGNED_BYTE.equals(datatype);
	}

	/**
	 * Checks whether the supplied datatype is equal to xsd:float or xsd:double.
	 */
	public static boolean isFloatingPointDatatype(URI datatype) {
		return 
		XMLSchema.FLOAT.equals(datatype) || 
		XMLSchema.DOUBLE.equals(datatype);
	}

	/**
	 * Checks whether the supplied datatype is equal to xsd:dateTime, xsd:date,
	 * xsd:time, xsd:gYearMonth, xsd:gMonthDay, xsd:gYear, xsd:gMonth or
	 * xsd:gDay. These are the primitive datatypes that represent dates and/or
	 * times.
	 * 
	 * @see XMLGregorianCalendar
	 */
	public static boolean isCalendarDatatype(URI datatype) {
		return
		XMLSchema.DATETIME.equals(datatype) ||
		XMLSchema.DATE.equals(datatype) ||
		XMLSchema.TIME.equals(datatype) ||
		XMLSchema.GYEARMONTH.equals(datatype) ||
		XMLSchema.GMONTHDAY.equals(datatype) ||
		XMLSchema.GYEAR.equals(datatype) ||
		XMLSchema.GMONTH.equals(datatype) ||
		XMLSchema.GDAY.equals(datatype);

	}

	/**
	 * Checks whether the supplied datatype is equal to xsd:boolean.
	 * 
	 * @param datatype
	 * @return
	 */
	public static boolean isBooleanDatatype(URI datatype) {
		return XMLSchema.BOOLEAN.equals(datatype);
	}
}
