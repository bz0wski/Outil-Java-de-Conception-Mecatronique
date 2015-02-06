package org.ica.exceptions;

public class SubtypeInconsistency extends MainApplicationException {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String errorMessage;
	
	public SubtypeInconsistency(String err) {
		errorMessage = err;
		System.err.println();
	}

	@Override
	public String toString() {
		return errorMessage;
	}
}
