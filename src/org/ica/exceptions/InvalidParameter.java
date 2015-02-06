package org.ica.exceptions;

public class InvalidParameter extends MainApplicationException {

	/**
	 * 
	 */
	private String errorMessage;
	
	private static final long serialVersionUID = 1L;

	public InvalidParameter() {
	}

	public InvalidParameter(String err) {
		errorMessage = err;
		System.err.println();
	}
	@Override
	public String toString() {
		return errorMessage;
	}
}
