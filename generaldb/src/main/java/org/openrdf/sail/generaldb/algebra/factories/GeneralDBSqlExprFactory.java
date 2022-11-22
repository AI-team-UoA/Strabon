/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.generaldb.algebra.factories;

import org.openrdf.query.algebra.ValueExpr;
import org.openrdf.sail.generaldb.algebra.base.GeneralDBSqlExpr;
import org.openrdf.sail.rdbms.exceptions.UnsupportedRdbmsOperatorException;

/**
 * Boolean SQL expression factory. This factory can convert a number of core
 * algebra nodes into an SQL expression.
 * 
 * @author James Leigh
 * 
 */
public class GeneralDBSqlExprFactory {

	private GeneralDBBNodeExprFactory bnode;

	private GeneralDBBooleanExprFactory bool;

	private GeneralDBDatatypeExprFactory datatype;

	private GeneralDBLabelExprFactory label;

	private GeneralDBLanguageExprFactory language;

	private GeneralDBNumericExprFactory numeric;

	private GeneralDBTimeExprFactory time;

	private GeneralDBURIExprFactory uri;

	private GeneralDBZonedExprFactory zoned;

	private GeneralDBHashExprFactory hash;

	public void setBNodeExprFactory(GeneralDBBNodeExprFactory bnode) {
		this.bnode = bnode;
	}

	public void setBooleanExprFactory(GeneralDBBooleanExprFactory bool) {
		this.bool = bool;
	}
	
	/**
	 * XXX
	 * @return the factory
	 */
	public GeneralDBBooleanExprFactory getBooleanExprFactory() {
		return bool;
	}
	
	public GeneralDBNumericExprFactory getNumericExprFactory() {
		return numeric;
	}
	/**
	 * 
	 * @param datatype
	 */

	public void setDatatypeExprFactory(GeneralDBDatatypeExprFactory datatype) {
		this.datatype = datatype;
	}

	public void setLabelExprFactory(GeneralDBLabelExprFactory label) {
		this.label = label;
	}

	public void setLanguageExprFactory(GeneralDBLanguageExprFactory language) {
		this.language = language;
	}

	public void setNumericExprFactory(GeneralDBNumericExprFactory numeric) {
		this.numeric = numeric;
		//XXX don't like the way I get access to what I need, but it seems it is the only choice
		//Extra note: This setter must FOLLOW the one for the LabelExprFactory!!! 
		//* and the one of the URIExprFactory as well
		//Otherwise an exception will be thrown 
		numeric.setLabelsPeek(label);
		numeric.setUrisPeek(uri);
	}

	public void setTimeExprFactory(GeneralDBTimeExprFactory time) {
		this.time = time;
	}

	public void setURIExprFactory(GeneralDBURIExprFactory uri) {
		this.uri = uri;
	}

	public void setZonedExprFactory(GeneralDBZonedExprFactory zoned) {
		this.zoned = zoned;
	}

	public void setHashExprFactory(GeneralDBHashExprFactory hash) {
		this.hash = hash;
	}

	public GeneralDBSqlExpr createBNodeExpr(ValueExpr arg)
		throws UnsupportedRdbmsOperatorException
	{
		return bnode.createBNodeExpr(arg);
	}

	public GeneralDBSqlExpr createBooleanExpr(ValueExpr arg)
		throws UnsupportedRdbmsOperatorException
	{
		return bool.createBooleanExpr(arg);
	}

	public GeneralDBSqlExpr createLabelExpr(ValueExpr arg)
		throws UnsupportedRdbmsOperatorException
	{
		return label.createLabelExpr(arg);
	}

	public GeneralDBSqlExpr createLanguageExpr(ValueExpr arg)
		throws UnsupportedRdbmsOperatorException
	{
		return language.createLanguageExpr(arg);
	}

//	public GeneralDBSqlExpr createNumericExpr(ValueExpr arg)
//		throws UnsupportedRdbmsOperatorException
//	{
//		return numeric.createNumericExpr(arg);
//	}
	
	//XXX addition for complicated metric expressions
	public GeneralDBSqlExpr createNumericExpr(ValueExpr arg)
	throws UnsupportedRdbmsOperatorException
	{
		
		return numeric.createNumericExpr(arg);
	}

	public GeneralDBSqlExpr createTimeExpr(ValueExpr arg)
		throws UnsupportedRdbmsOperatorException
	{
		return time.createTimeExpr(arg);
	}

	public GeneralDBSqlExpr createZonedExpr(ValueExpr arg)
		throws UnsupportedRdbmsOperatorException
	{
		return zoned.createZonedExpr(arg);
	}

	public GeneralDBSqlExpr createDatatypeExpr(ValueExpr arg)
		throws UnsupportedRdbmsOperatorException
	{
		return datatype.createDatatypeExpr(arg);
	}

	public GeneralDBSqlExpr createUriExpr(ValueExpr arg)
		throws UnsupportedRdbmsOperatorException
	{
		return uri.createUriExpr(arg);
	}

	public GeneralDBSqlExpr createHashExpr(ValueExpr arg)
		throws UnsupportedRdbmsOperatorException
	{
		return hash.createHashExpr(arg);
	}
}
