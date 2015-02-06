package org.ica.utilityClasses;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.ica.briquePackage.Brique;
import org.ica.briquePackage.CompositionBriques;
import org.ica.briquePackage.Equation;
import org.ica.briquePackage.Parametre;
import org.ica.exceptions.MainApplicationException;

/**This class contains only one public function <code>execute(String oldName, CompositionBriques compositionBriques)</code>
 * which renames all the contents of a compositionBriques objects when it's name is changed. 
 * @author Salim Ahmed*/
public class RenameContents {


	/**Renames the contents of a {@link CompositionBriques} object passed as a parameter.
	 * @param oldName: The oldName of the object before the name change.
	 * @param compositionBriques: The {@link CompositionBriques} whose contents are to be renamed.
	 * @throws MainApplicationException  {@code MainApplicationException} */

	public static void execute(String oldName, CompositionBriques compositionBriques) throws MainApplicationException {
		String compositionName = firstLetterToLowerCase(compositionBriques.getNomComposition());
		List<Brique> brique = compositionBriques.getBrique();
		for (Brique ptiB : brique) {
			String briqueName = ptiB.getNomBrique();
			
			StringBuilder builder = new StringBuilder(briqueName);
			if(briqueName.regionMatches(true, 0, oldName, 0, oldName.length())){
				builder.replace(0, oldName.length(), compositionName);
				ptiB.setNomBrique(builder.toString());

				renameParameters(ptiB, oldName, compositionName);
			}											
		}
	}
	
	/**Renames the contents of a {@link Brique} object passed as a parameter.
	 * @param oldName: The oldName of the object before the name change.
	 * @param brique: The {@link Brique} whose contents are to be renamed.
	 * @throws MainApplicationException  {@code MainApplicationException} */

	public static void execute(String oldName, Brique brique) throws MainApplicationException {
		String briqueName = firstLetterToLowerCase(brique.getNomBrique());
		renameParameters(brique, oldName, briqueName);														
		
	}
	
	
	/**Converts the first alphabet of the string passed as parameter to a lower alphabet. 
	 * @param toLowerCase : The string whose first letter to convert a to lower case alphabet.
	 * @return A string which begins with a lower case letter.*/
	public static String firstLetterToLowerCase(String toLowerCase) {
		if (toLowerCase.isEmpty()) {
			return toLowerCase;
		}
		StringBuilder builder = new StringBuilder(toLowerCase);
		if (toLowerCase.length() > 1) {
			String subString = builder.substring(0, 1);
			builder.replace(0, 1, subString.toLowerCase());
		}
		
		return builder.toString();

	}

	private final static String PARAMETER_PATTERN =  "([a-z]+\\w*)";
	private final static String AROUND_PARAMETER_PATTERN = "(?!"+PARAMETER_PATTERN+")*([\\W?+|\\d+])(?!"+PARAMETER_PATTERN+")*";

	/**This sub routine strips an equation into it's individual components so the can be rebuilt in a custom way
	 * for example renaming some of its parameters.
	 * @param equation : The equation to strip into parts
	 * @return
	 *  A map containing the location of all the individual components of the equation. */
	public static Map<Integer, Object> function(Equation equation){

		Matcher parameterMatcher = Pattern.compile(PARAMETER_PATTERN).matcher("");
		Matcher matcher = Pattern.compile(AROUND_PARAMETER_PATTERN).matcher("");

		//Insert all the characters in the list
		String content = equation.getContenuEqn();	
		Map<Integer, Object> locations = new TreeMap<>();


		matcher.reset(content);
		parameterMatcher.reset(content);

		while (parameterMatcher.find()) {				
			if (!parameterMatcher.group().isEmpty()) {

			}
			Parametre p = equation.getParametreByName(parameterMatcher.group());
			if (p != null) {

				int start = parameterMatcher.start();

				locations.put(start, p);					
			}

		}
		while (matcher.find()) {				
			if (!matcher.group().isEmpty()) {				
				int start = matcher.start();

				locations.put(start,matcher.group(2));
			}
		}

		return locations;
	}
/**
 * Renames the parameters of all equations in a {@link Brique}, changing the contents of 
 * the accompanying equations.
 * @param brique : The {@link Brique} whose parameters are to be renamed
 * @param oldName : The old name (prefix) to replace
 * @param newName : The new name(prefix) to append to the parameter name.
 * @throws MainApplicationException
 */
	private static void renameParameters(Brique brique, String oldName, String newName) throws MainApplicationException{
		List<Equation> equations = brique.getListEquations();
		newName = firstLetterToLowerCase(newName);
		
		for (Equation equation : equations) {
			Map<Integer, Object> map = function(equation);
			StringBuilder builder = new StringBuilder();

			for(Iterator<Map.Entry<Integer, Object>> entry = map.entrySet().iterator(); entry.hasNext();){
				Object object = entry.next().getValue();

				if (object instanceof Parametre){
					Parametre p = (Parametre)object;
					String nomP = p.getNomP();
					StringBuilder renameP = new StringBuilder(nomP);

					if(nomP.regionMatches(true, 0, oldName, 0, oldName.length())){
						renameP.replace(0, oldName.length(), newName);
						p.setNomP(renameP.toString());					
						builder.append(renameP.toString());
					}



				}
				else if (object instanceof String) {
					String string = (String)object;				
					builder.append(string);

				}
			}
			equation.setContenuEqn(builder.toString());			
		}
	}

	/**
	 * Appends the prefix String to all the equation parameters, and resets the equation statement
	 * to reflect the changes
	 * @param prefix : The prefix to add to the parameter names
	 * @param equation : The equation object to be modified
	 * @throws MainApplicationException {@code MainApplicationException} 
	 */
	public static void renameEquationParameters(String prefix, Equation equation) throws MainApplicationException {
		prefix = firstLetterToLowerCase(prefix);
		Map<Integer, Object> locations = function(equation);
		StringBuilder builder = new StringBuilder();
		for(Iterator<Map.Entry<Integer, Object>> entry = locations.entrySet().iterator(); entry.hasNext();){
			Object object = entry.next().getValue();

			if (object instanceof Parametre){
				Parametre p = (Parametre)object;
				String newNomP = prefix+"_"+p.getNomP();
				p.setNomP(newNomP);					
				builder.append(newNomP);

			}
			
			else if (object instanceof String) {
				String string = (String)object;				
				builder.append(string);

			}
		}
		equation.setContenuEqn(builder.toString());	
		System.err.println("Equation Statement "+equation.getContenuEqn());
	}

/**
 * Strips the string prefix received as parameter from all the equation parameters,and applies all changes to 
 * the equation statement
 * @param prefix : the prefix to strip
 * @param equation : the equation object to modify
 * @return :
 *   boolean which indicates whether the stripping process was successful, true, or not, false. 
 * @throws MainApplicationException {@code MainApplicationException} 
 */
	public static boolean stripPrefix(String prefix, Equation equation) throws MainApplicationException {
		prefix = prefix+"_";		
		Map<Integer, Object> locations = function(equation);
		StringBuilder builder = new StringBuilder();
		
		for(Iterator<Map.Entry<Integer, Object>> entry = locations.entrySet().iterator(); entry.hasNext();){
			Object object = entry.next().getValue();

			if (object instanceof Parametre){
				Parametre p = (Parametre)object;
				String nomP = p.getNomP();
				StringBuilder renameP = new StringBuilder(nomP);
				if(nomP.regionMatches(true, 0, prefix, 0, prefix.length())){
					renameP.replace(0, prefix.length(), "");
									
					builder.append(renameP.toString());
				}
				
			}
			
			else if (object instanceof String) {
				String string = (String)object;				
				builder.append(string);

			}
		}
		equation.setContenuEqn(builder.toString());	
		return true;
	}
	

}


