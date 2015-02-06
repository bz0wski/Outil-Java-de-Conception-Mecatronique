package org.ica.exceptions;

public class ErreurDeSyntaxe extends MainApplicationException {

	private String errorMessage;
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ErreurDeSyntaxe() {
	}

	public ErreurDeSyntaxe(String arg0) {
		
		errorMessage = " Erreur de Syntaxe, "+ arg0;
	}

	public ErreurDeSyntaxe(String str1,String str2) {		
		errorMessage = str1 + str2;
	}

	@Override
	public String toString() {
		return errorMessage;
	}
}
