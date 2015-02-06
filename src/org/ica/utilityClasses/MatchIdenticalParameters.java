package org.ica.utilityClasses;

import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;

import org.ica.briquePackage.Equation;
import org.ica.briquePackage.Parametre;

public class MatchIdenticalParameters {

	public MatchIdenticalParameters() {}

	/**This sub-routine matches and replaces identical parameters with the first instance of the
	 * parameter encountered in a given equations.
	 * @param equation The equation to be fed for matching.
	 * */
	private static void matchIdenticalEquationParameters(Equation equation) {
		//Operator to replace all parameters with matching names inside a given Equation object
		UnaryOperator<Parametre> parametreOperator = parametre->{
			for (Parametre replacement : equation.getListeDeParametresEqn()) {
				if (parametre.getNomP().equals(replacement.getNomP())) {
					return replacement;
				}
			}
			return parametre;
			
		};
		//System.out.println("Before replacement "+equation.getListeDeParametresEqn());
		equation.getListeDeParametresEqn().replaceAll(parametreOperator);
	
	}
	
	
	/**This sub-routine matches and replaces identical parameters with the first instance of the
	 * parameter encountered in every equation in a given list of equations.
	 * @param equations The list of equations to be fed to the sub-routine for matching.
	 * */
	private static void matchIdenticalListOfEquationParameters(List<Equation> equations) {
		for (Equation equation : equations) {
			matchIdenticalEquationParameters(equation);			
		//	System.out.println("After replacement "+equation.getListeDeParametresEqn());	
		}				
		
	}
	/**This sub-routine matches and replaces identical parameters with the first instance of the
	 * parameter encountered .
	 * @param equations The list of all the brique equations for which all matching parameters will be replaced.
	 *  */
	public static void matchIdenticalBriqueParameters(List<Equation> equations) {
		//#1 - first of all, replace all the matching parameters in all the equations
		matchIdenticalListOfEquationParameters(equations);
		
		//#2 - Then replace all of the matching parameters in all of the equations.
		
		//Operator to replace all parameters with matching names inside a given Brique object
		List<Parametre> listOfAllParametres = new ArrayList<>();
		for (Equation equation : equations) {
			listOfAllParametres.addAll(equation.getListeDeParametresEqn());
		}
	////	System.out.println("Before replacement "+listOfAllParametres);
		
		UnaryOperator<Parametre> parameterReplacementOperator = parametre->{
			for (Parametre replacement : listOfAllParametres) {
				if (parametre.getNomP().equals(replacement.getNomP())) {
					return replacement;
				}
			}
			return parametre;
					
		};
		for (Equation equation : equations) {
			equation.getListeDeParametresEqn().replaceAll(parameterReplacementOperator);
		}
		
		///System.out.println("After replacement "+listOfAllParametres);
		/*Operator to replace all parameters with matching names inside all the Equations in a given Brique object
		UnaryOperator<Parametre> operator = (parameter)->{
			for (Parametre replacement : brique.getListParametres()) {
				if (parameter.getNomP().equals(replacement.getNomP())) {
				//	System.out.println("EQUALS: Parametre - "+parameter.getNomP()+"  Replacement - "+replacement.getNomP());
					return replacement;
				}	
			}
			return parameter;
					
		};
		
		for (Iterator<Map.Entry<Equation, List<Parametre>>> iterator = complex.entrySet().iterator(); iterator.hasNext();) {
			Map.Entry<Equation, List<Parametre>> entry =  iterator.next();
			Equation eqn = (Equation)entry.getKey();
			
			
			eqn.getListeDeParametresEqn().replaceAll(operator);
		}
		
		*/
		
	}
}



/*		
List<Parametre> listParametres = eqn.getListeDeParametresEqn();
for (int i = 0; i < listParametres.size(); i++) {
	Parametre t_parametre = listParametres.get(i);
	for (Parametre replacement : brique.getListParametres()) {
		if (t_parametre.getNomP().equals(replacement.getNomP())) {
			eqn.getListeDeParametresEqn().remove(t_parametre);
			//eqn.getListeDeParametresEqn().re
		}
	}
}

*/				
/*
for (Iterator<Parametre> p_iterator = eqn.getListeDeParametresEqn().iterator(); p_iterator.hasNext();) {
	Parametre parametre = (Parametre) p_iterator.next();
	for (Parametre replacement : brique.getListParametres()) {
		if (parametre.getNomP().equals(replacement.getNomP())) {
			System.out.println("EQUALS: Parametre - "+parametre.getNomP()+"  Replacement - "+replacement.getNomP());
			//parametre = replacement;
			//eqn.getListeDeParametresEqn().remove(parametre);
			//p_iterator.remove();
			
			//eqn.getListeDeParametresEqn().add(replacement);
		}
	}
	
}*/
