/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.generaldb.managers;

import java.sql.SQLException;
import java.util.Locale;
import java.util.TimeZone;

import javax.xml.datatype.XMLGregorianCalendar;

import org.openrdf.generaldb.managers.base.ValueManagerBase;
import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.sail.generaldb.model.XMLGSDatatypeUtil;
import org.openrdf.sail.generaldb.schema.LiteralTable;
import org.openrdf.sail.rdbms.model.RdbmsLiteral;

/**
 * Manages RDBMS Literals. Including creation, id lookup, and inserting them
 * into the database.
 * 
 * @author James Leigh
 * 
 */
public class LiteralManager extends ValueManagerBase<RdbmsLiteral> {

	//private static Logger logger = LoggerFactory.getLogger(org.openrdf.sail.generaldb.managers.LiteralManager.class);
	
	private static TimeZone Z = TimeZone.getTimeZone("GMT");

	public static long getCalendarValue(XMLGregorianCalendar xcal) {
		return xcal.toGregorianCalendar(Z, Locale.US, null).getTimeInMillis();
	}

	public static LiteralManager instance;

	private LiteralTable table;

	public LiteralManager() {
		instance = this;
	}

	public void setTable(LiteralTable table) {
		this.table = table;
	}

	@Override
	public void close()
	throws SQLException
	{
		super.close();
		if (table != null) {
			table.close();
		}
	}

	@Override
	protected boolean expunge(String condition)
	throws SQLException
	{
		return table.expunge(condition);
	}

	@Override
	protected void optimize()
	throws SQLException
	{
		super.optimize();
		table.optimize();
	}

	@Override
	protected Literal key(RdbmsLiteral value) {
		return value;
	}

	@Override
	protected void insert(Number id, RdbmsLiteral literal)	throws  NullPointerException,SQLException, InterruptedException, IllegalArgumentException
	{
		String label = literal.getLabel();
		String language = literal.getLanguage();
		URI datatype = literal.getDatatype();
		
		if (datatype == null && language == null) {
			table.insertSimple(id, label);
		}
		else if (datatype == null) {
			table.insertLanguage(id, label, language);
		}
		else { // literal with datatype
			String dt = datatype.stringValue();
			
			try {
				if (XMLGSDatatypeUtil.isNumericDatatype(datatype)) {
					table.insertNumeric(id, label, dt, literal.doubleValue());
				}
				else if (XMLGSDatatypeUtil.isCalendarDatatype(datatype)) {
					long value = getCalendarValue(literal.calendarValue());
					table.insertDateTime(id, label, dt, value);
				}
				else {
					table.insertDatatype(id, label, dt);
					
					/**
					 * FIXME
					 * NOTE: Cannot support the intervalStart and intervalEnd here!! 
					 * Will need some other place to add them if this approach does work
					 * 
					 */
					if(XMLGSDatatypeUtil.isWKTDatatype(datatype)) //WKT case
					{
						table.insertWKT(id, label, dt, null, null);
					}
					else if(XMLGSDatatypeUtil.isGMLDatatype(datatype)) //GML case
					{
						table.insertGML(id, label, dt, null, null);
					} 
				}
				
			}
			catch (NumberFormatException e) {
				table.insertDatatype(id, label, dt);
			}
		}
	}

	@Override
	protected int getBatchSize() {
		return table.getBatchSize();
	}

	/**
	 * @return the literal table linked with the manager
	 */
	public LiteralTable getLiteralTable()
	{
		return table;
	}
}
