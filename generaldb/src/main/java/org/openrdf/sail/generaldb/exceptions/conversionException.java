package org.openrdf.sail.generaldb.exceptions;


/**
 * @author kkyzir
 * 
 */
public class conversionException extends Exception {

	String error;

	/**
	 * Custom exception, thrown when the conversion from H to V format fails.
	 */
	public conversionException() {
		super();
		error = "not specified";
	}

	/**
	 * @param arg0
	 *            some kind of message that is saved in an instance variable.
	 */
	public conversionException(String arg0) {
		super(arg0);
		error = arg0;
	}

	/**
	 * Public method, callable by exception catcher. It returns the error message.
	 * @return the error message.
	 */
	public String getError() {
		return error;
	}

}

