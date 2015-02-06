package org.ica.exceptions;

public class DuplicationParametre extends MainApplicationException {
		String errorMessage;  
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public DuplicationParametre() {
		errorMessage ="Parametre dupliqué.";
	}

	public DuplicationParametre(String arg0) {
		errorMessage = arg0;
		System.out.println("Le parametre "+arg0 +" est dupliqué.");
	}
	@Override
	public String toString() {
	// TODO Auto-generated method stub
	return errorMessage;
	}
}
