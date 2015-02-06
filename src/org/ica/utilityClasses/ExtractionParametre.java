package org.ica.utilityClasses;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.ica.briquePackage.Equation;
import org.ica.briquePackage.Parametre;
import org.ica.exceptions.InvalidParameter;
import org.ica.exceptions.MainApplicationException;

public class ExtractionParametre {

	private static final String FONCTIONS = "((?![+-/^%*=<>()])?\\s*(abs|acos|asin|atan|cbrt|max|min|ceil|cos|cosh|exp|expm1|floor|log|sin|sinh|sqrt|tan|tanh){1}\\s*\\({1})";
	private final static String CHIFFRES = "\\d+";
	private final static String OPERATEURS_AVEC_PARENTHESES= "([+|\\-|/|^|%|*|=|<|>|(|)|,])";
	private final static String PARAMETER_PATTERN = "([a-z]+\\w*)";

	
	private static  List<String> extractEquationParameters(String exprEquation) throws MainApplicationException{

		List<String> afterExtraction = new ArrayList<String>(); 
		String finalRegex = new String(FONCTIONS  +"|"+ OPERATEURS_AVEC_PARENTHESES);				

		Pattern numberPattern = Pattern.compile(CHIFFRES);
		Matcher numberMatch;
		for(String pt : exprEquation.split(finalRegex,0)){
			pt = pt.replaceAll("\\s*", "");
			numberMatch = numberPattern.matcher(pt);

			if(!pt.isEmpty() && !numberMatch.matches()){				
				if (!afterExtraction.add(pt.replaceAll("\\s*", ""))){
					System.out.println("Parameter Not Added: "+pt);	

				}
			}
		}

		return afterExtraction;

	}


	/***Cette fonction permet de déterminer la validité de la syntaxe des nom des parametres
	 * @param nomP : The parameter name to test.*/
	private static void paramPatternTest(String nomP) throws InvalidParameter{
		Pattern paramPattern = Pattern.compile(PARAMETER_PATTERN);

		Matcher matcher = paramPattern.matcher(nomP);
		if (!matcher.matches()){
			System.out.println("InValid Parameter "+nomP);
			throw new InvalidParameter("Nom Parametre Invalid! "+nomP);
		}

	}

	private static void parameterNameConflict(String nomP) throws InvalidParameter{
		for (String str:Equation.getFunctions()){
			if(str.equals(nomP))
				throw new InvalidParameter("Nom parametre invalide.");
		}
	}

	
	/**Sub routine to create an Equation's parameter objects.
	 * @param equation: Given an instance of an Equation object, parse it's content and extract all the parameters
	 * @return Returns a <code>List</code> containing the new equation parameters.
	 * @throws MainApplicationException {@link MainApplicationException}
	 */
	public static List<Parametre> listEquationParameters(Equation equation) throws MainApplicationException{

		List<String> listParam = extractEquationParameters(equation.getContenuEqn());
		List<Parametre> params = new ArrayList<Parametre>();

		for(Iterator<String> i = listParam.iterator(); i.hasNext();){

			String p = i.next();
			//Checking to see if the parameter name corresponds with the defined pattern
			paramPatternTest(p);
			//Checking to see that the parameter name isn't the same as a function name.
			parameterNameConflict(p);		

			params.add(new Parametre(p));
		}
		return params;

	} 
	/**Sub routine to test the validity of an Equation's parameter names.
	 * @param equation: Given a copy of the Equation content, parse it's content, extract the parameters and test their validity.
	 *  @throws MainApplicationException {@link MainApplicationException}
	 * */
	public static void listEquationParameters(String equation) throws MainApplicationException{

		List<String> listParam = extractEquationParameters(equation);
		for(Iterator<String> i = listParam.iterator(); i.hasNext();){
			String p = i.next();
			//Checking to see if the parameter name corresponds with the defined pattern
			paramPatternTest(p);
			//Checking to see that the parameter name isn't the same as a function name.
			parameterNameConflict(p);					
		}
		

	} 
}
