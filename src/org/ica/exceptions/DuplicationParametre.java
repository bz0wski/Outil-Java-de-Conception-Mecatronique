package org.ica.exceptions;

public class DuplicationParametre extends MainApplicationException {
		String errorMessage;  
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public DuplicationParametre() {
		errorMessage ="Parametre dupliqu�.";
	}

	public DuplicationParametre(String arg0) {
		errorMessage = arg0;
		System.out.println("Le parametre "+arg0 +" est dupliqu�.");
	}
	@Override
	public String toString() {
	// TODO Auto-generated method stub
	return errorMessage;
	}
}
