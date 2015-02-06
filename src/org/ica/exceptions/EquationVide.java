package org.ica.exceptions;

public class EquationVide extends MainApplicationException {

	private String errorMessage;

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public EquationVide() {
		System.out.println("Expression de l'équation ne peut pas etre vide");
		errorMessage = "Expression de l'équation ne peut pas etre vide";
	}

	public EquationVide(String arg0) {
		
		System.out.println("Expression de l'équation ne peut pas etre vide");
		errorMessage = "Expression de l'équation ne peut pas etre vide";
	}

	@Override
	public String toString() {
		return errorMessage;
	}
}
