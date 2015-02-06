package org.ica.utilityClasses;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.ica.exceptions.EquationVide;
import org.ica.exceptions.ErreurDeSyntaxe;
import org.ica.exceptions.MainApplicationException;

/* Fonctions de validation de syntaxe des équations [V]
//--------------------------------------------------------------------------
// 1) Pour qu'une equation soit valide, il faudra qu'elle ne contienne aucun
// des caractères: ? ! : ; & # " ' @ ° { } | % µ £ $ ¤ § \ ² ~
// 2)L'équation devra être constituée de: =, >, >=, < ou <=.
// 3)Le nombre de paranthèses/crochets ouverts et fermés devra correspondre
// à chacun des cotés du signe d'égalité/inégalité (remplacement 
// automatique: '[' par '(').
// 4) Deux signes opératoires + - * / ^ = > < ne pourront pas se suivre et
 *  devront être entourés par un membre (eq: ne peuvent pas se trouver comme
 *   caractère final ou initial) à l'exception de >= et <=.
 *   5) Une fermeture de parenthèse devra être immédiatement suivie d'un signe
 *   opératoire à l'exception du dernier charactère.
 *   6) Une ouverture de parenthèse devra être précédée obligatoirement d'un
% signe opératoire, d'une fonction ou d'une autre parenthèse ouverte.
% 7) Le caractère , ne pourra être utilisé qu'à l'intérieur d'une fonction,
% sans présence d'aucun signe d'égalité = < >.

 */
public class VerificationSyntaxeEquation {

	private String exprEquation;

	private static final String CARACTERES_INVALIDES = "[?|!|\\|:|;|&|\"|\'|@|¤|$|#|{|}|[|]|€|£|µ|%|²|~|é|è|ç|à|û|ê|\\\\]";
	private static final String CARACTERES_OBLIGATOIRES = "[=|>|>=|<|<=]";
	private static final String OPERATEURS = "[+|-|/|^|%|*|=|<|>]";
	private static final String OPERATEURS_AVEC_PARENTHESES_FERMETURE = "[+|\\-|/|^|%|*|=|<|>|)\\s*]";

	private static final String OPERATEURS_AVEC_PARENTHESES_OUVERTURE = "([+|\\-|/|^|%|*|=|<|>|(]\\s*[(])";
	private static final String SIGNES_OPERATEURS = "[+|\\-|/|^|%|*]";



	private static final String FONCTIONS = "((?![+-/^%*=<>()])?\\s*(abs|acos|asin|atan|cbrt|max|min|ceil|cos|cosh|exp|expm1|floor|log|sin|sinh|sqrt|tan|tanh){1}\\s*\\({1})";

	public VerificationSyntaxeEquation(String contenuEqn) throws MainApplicationException {
		exprEquation = contenuEqn;
		
		//0)Remplacer avec Parentheses tous les " [","{","}","]".
		remplacerAvecParentheses();

		//1)Cette fonction vérifie qu'un chiffre n'est suivi que d'un signe opératoire pas d'une parenthese ouvrant.
		EquationSyntax();

		// 2)L'équation devra obligatoirement être constituée d'un seul de: =, >, >=, < ou <=.
		caracteresObligatoires();

		//3)Nombre de parenthèses à gauche et à droite de l'équation doivent être le même.
		verrificationNombreParentheses();

		//4)Deux signes opératoires « + - * / ^ = > < » ne pourront pas se suivre
		//et devront être entourés par un membre ( ne peuvent pas se trouver comme 
		//caractère <code>final</code> ou initial) à l'exception de « >= et <= » .
		deuxOpérateursNeSeSuiventPas();

		// 5) Une fermeture de parenthèse devra être immédiatement suivie d'un signe
		// opératoire à l'exception du dernier caractère.
		reglesParenthesesDeFermeture();

		// 6) Une ouverture de parenthèse devra être précédée obligatoirement d'un
		// signe opératoire, d'une fonction ou d'une autre parenthèse ouverte.
		reglesParenthesesOuverture();

		//7)Dernier Contrôle pour vérifier que l'équation ne contient aucun de ces caractères
		this.caracteresInvalides();

		//8)Verifie l'emplacement des signes d'égalité
		signeEgalité();
		
		//9)Verifie l'emplacement des signes opératoires
		signeOperatoire();
		
		//10)Virgules dans l'équation. 
		privilegeMinMax();

		System.out.println("INITIAL TEST PASSED\n\n");
	}

	public String getExprEquation() {
		return exprEquation;
	}

	public void setExprEquation(String exprEquation) {
		this.exprEquation = exprEquation;
	}

	/**Signe d'égalité ne pourra être ni le premier ni le dernier caractère*/
	private void signeEgalité() throws ErreurDeSyntaxe{
		
		Matcher matcher = Pattern.compile("^\\s*"+CARACTERES_OBLIGATOIRES).matcher(getExprEquation());
		if (matcher.find()) {
			throw new ErreurDeSyntaxe("opérateur d'égalité mal placé.");
		}
		matcher.usePattern(Pattern.compile(CARACTERES_OBLIGATOIRES+"\\s*$"));
		matcher.reset(getExprEquation());
		if (matcher.find()) {
			throw new ErreurDeSyntaxe("Erreur d'expression.Opérateur mal placé.");
		}
	}

	/**Signe Operatoire ne pourra être ni le premier ni le dernier caractère*/
		private void signeOperatoire() throws ErreurDeSyntaxe{
			
			Matcher matcher = Pattern.compile("^\\s*"+SIGNES_OPERATEURS).matcher(getExprEquation());
			if (matcher.find()) {
				throw new ErreurDeSyntaxe("signe opératoire mal placé.");
			}
			matcher.usePattern(Pattern.compile(SIGNES_OPERATEURS+"\\s*$"));
			matcher.reset(getExprEquation());
			if (matcher.find()) {
				throw new ErreurDeSyntaxe("signe opératoire mal placé.");
			}
		}

		
	/**Remplacer tous les [,{,},] avec (,)*/
	private void remplacerAvecParentheses() throws EquationVide{
		if(this.exprEquation.isEmpty() || this.exprEquation == null){
			throw new EquationVide();
		}
		this.exprEquation = exprEquation.replaceAll("\\[", "\\(").replaceAll("\\]", "\\)").replaceAll("\\{", "\\(").replaceAll("\\}", "\\)");
	}

	/**Cette fonction vérifie qu'un chiffre n'est suivi que d'un signe opératoire et non pas d'une parenthese ouvrant.*/
	private void EquationSyntax() throws ErreurDeSyntaxe{

		Matcher matcher = Pattern.compile("(\\d+)\\s*(.){1}").matcher(getExprEquation());
		while (matcher.find()) {		
			if(matcher.group(2).matches("\\w+")) continue;
			if (!OPERATEURS_AVEC_PARENTHESES_FERMETURE.contains(matcher.group(2))) {
				throw new ErreurDeSyntaxe("vérifier équation.");
			}
		}
		
	}


	/**L'équation devra obligatoirement être constituée d'un seul de: =, >, >=, < ou <=.*/
	private void caracteresObligatoires() throws ErreurDeSyntaxe{
		char [] eqnCh = exprEquation.toCharArray();
		int obligCount = 0;
		Pattern patt = Pattern.compile(CARACTERES_OBLIGATOIRES);
		for(char c : eqnCh){
			Matcher match = patt.matcher(String.valueOf(c));
			if(match.matches())
				obligCount++;
		}
		if(obligCount == 0)
			throw new ErreurDeSyntaxe("Il manque un opérateur d'égalité ou inégalité","");
		else if (obligCount > 1)
			throw new ErreurDeSyntaxe("Trop d'opérateurs d'égalité ou Erreur de signe d'opérateurs d'égalité","");
	}

	/**
	 Le nombre de paranthèses/crochets ouverts et fermés devra correspondre
	 à chacun des cotés du signe d'égalité/inégalité (remplacement 
	 automatique: '[' par '(').
	 */

	private void verrificationNombreParentheses() throws ErreurDeSyntaxe{

		char [] eqnCh = exprEquation.toCharArray();
		int g_parenthesesGauche = 0,g_parenthesesDroite = 0,d_parenthesesDroite = 0,d_parenthesesGauche = 0 ;
		int takeItFromHere,i = 0;

		Pattern patt = Pattern.compile(CARACTERES_OBLIGATOIRES);
		//Parcourir toute l'équation de gauche à droite.
		Matcher match;
		do{
			//Tant que tu trouve pas un opérateur d'égalité/inégalité...faire
			match = patt.matcher(String.valueOf(eqnCh[i]));
			if(match.matches()){
				//Si tu rencontre un signe d'égalité/inégalité, sors de la boucle, 
				//continue la recherche à partir de l'autre coté de l'équation
				takeItFromHere = i+1;
				break;
			}
			else if(eqnCh[i]=='(')
				g_parenthesesGauche++;
			else if(eqnCh[i] == ')')
				g_parenthesesDroite++;
			i++;
			takeItFromHere = i;
		}
		while(!match.matches() && i<eqnCh.length);


		for(int j = takeItFromHere;j<eqnCh.length;j++){
			if(eqnCh[j]=='(')
				d_parenthesesGauche++;
			else if(eqnCh[j] == ')')
				d_parenthesesDroite++;
		}

		if(d_parenthesesGauche != d_parenthesesDroite)
			throw new ErreurDeSyntaxe("Erreur de nombre de parentheses à droite de l'équation. ","");
		else if( g_parenthesesGauche != g_parenthesesDroite)
			throw new ErreurDeSyntaxe("Erreur de nombre de parentheses à gauche de l'équation. ","");
		//		System.out.println("3 - Equal number of parentheses.");

	}


	/**Deux signes opératoires + - * / ^ = > < ne pourront pas se suivre et
	   devront être entourés par un membre (eq: ne peuvent pas se trouver comme
	  caractère final ou initial) à l'exception de >= et <=.
	 */
	private void deuxOpérateursNeSeSuiventPas() throws ErreurDeSyntaxe{
		Pattern patt_1 = Pattern.compile(OPERATEURS);

		char[] eqnCh = exprEquation.toCharArray();

		for(int i=0; i<eqnCh.length-1;i++){

			Matcher match_1 = patt_1.matcher(String.valueOf(eqnCh[i]));
			if(match_1.matches()){

				Matcher match_2 = patt_1.matcher(String.valueOf(eqnCh[i+1]));
				if((eqnCh[i] =='>' ||eqnCh[i] == '<') && eqnCh[i+1] == '=')	
					continue;
				else if (match_1.matches() && match_2.matches())
					throw new ErreurDeSyntaxe("Deux opérateurs ne peuvent pas se suivre.",""); 

			}
		}	
	}

	 /**Une fermeture de parenthèse devra être immédiatement suivie d'un signe
	 opératoire à l'exception du dernier charactère.
	 */
	private void reglesParenthesesDeFermeture() throws ErreurDeSyntaxe{
		Pattern pattern = Pattern.compile(OPERATEURS_AVEC_PARENTHESES_FERMETURE);
		char[] eqnCh = exprEquation.toCharArray();
		for(int i=0; i<eqnCh.length-1;i++){
			if((eqnCh[i] =='(' && eqnCh[i+1] ==')' )|| (eqnCh[i] == ')' && eqnCh[i+1] == '('))
				throw new ErreurDeSyntaxe("Deux parentheses ne peuvent pas se suivre de cette manniere. ","() ou )("); 
			Matcher match = pattern.matcher(String.valueOf(eqnCh[i+1]));

			if((eqnCh[i] == ')' && !match.matches()) || (eqnCh[i] == ')' && !match.matches() && i != eqnCh.length))
				throw new ErreurDeSyntaxe("Une parenthese de fermeture doit être suivie d'un opérateur si elle n'est pas le "
						+ "dernier caractère de l'expression.","length: "+eqnCh.length+" _ I value :"+i);	

		}
	}


	/**Une ouverture de parenthèse devra être précédée obligatoirement d'un
 	signe opératoire, d'une fonction ou d'une autre parenthèse ouverte.	
	 * @throws ErreurDeSyntaxe
	 */
	private void reglesParenthesesOuverture() throws ErreurDeSyntaxe{
		int parApresOperateur = 0;
		int numberOfFunctionMatches = 0;
		int numberOfInitPar_OuvMatches = 0;
		int totalNumberofPar_ouv = 0;

		Pattern pattern_fx = Pattern.compile(FONCTIONS);

		Matcher match_fx = pattern_fx.matcher(this.exprEquation);

		while (match_fx.find()) {
			//		System.out.println("EQUATION NAME: "+this.exprEquation);
			numberOfFunctionMatches++;
		}
		//	System.out.println("Matches of functions: "+numberOfFunctionMatches);

		match_fx.reset();
		match_fx.usePattern(Pattern.compile("^(\\s*\\(.*)"));
		while(match_fx.find()){
			numberOfInitPar_OuvMatches++;
		}
		//	System.out.println("Matches of Initial opening parenthesis: "+numberOfInitPar_OuvMatches);


		match_fx.reset();
		match_fx.usePattern(Pattern.compile(OPERATEURS_AVEC_PARENTHESES_OUVERTURE));
		while(match_fx.find()){
			parApresOperateur++;
		}
		//	System.out.println("Matches of parenthesis preceeded by another opening parenthesis or operator: "+parApresOperateur);


		match_fx.reset();
		match_fx.usePattern(Pattern.compile("\\("));
		while(match_fx.find()){
			totalNumberofPar_ouv++;
		}
		//System.out.println("Total number of opening parenthesis: "+totalNumberofPar_ouv);

		if(totalNumberofPar_ouv != (parApresOperateur+numberOfInitPar_OuvMatches+numberOfFunctionMatches))
			throw new ErreurDeSyntaxe("Une parenthese d'ouverture doit être precédée soit d'un opérateur soit d'une fonction si elle n'est pas le "
					+ "premier caractère de l'expression.","");	
		//		 System.out.println("6 - Pas de violation de règle d'ouverture de parenthese.");


	}


	/** 7) Pour qu'une equation soit valide, il faudra qu'elle ne contienne aucun
	des caractères: ? ! : ; & # " ' @ ° { } | % µ £ $ ¤ § \ ² ~
	 * 
	 * @throws ErreurDeSyntaxe
	 */

	private void caracteresInvalides() throws ErreurDeSyntaxe{

		char [] eqnCh = exprEquation.toCharArray();
		Pattern patt = Pattern.compile(CARACTERES_INVALIDES);
		for(char c : eqnCh){
			Matcher match = patt.matcher(String.valueOf(c));
			if(match.matches())
				throw new ErreurDeSyntaxe("Caractère non valide : ", String.valueOf(c));
		}
		//		System.out.println("7 - Pas de caractères Invalides.");

	}

	/***Le caractère ',' ne pourra être utilisé qu'à l'intérieur d'une fonction,
	sans présence d'aucun signe d'égalité = < >.
	 * 
	 * @throws ErreurDeSyntaxe
	 */
	private void privilegeMinMax()throws ErreurDeSyntaxe{
		int minmaxcount = 0, commacount = 0, minmaxcountSyntaxe = 0;

		String fxn = "(min|max)\\s*([\\(]\\s*[\\w]\\s*[,]\\s*[\\w]\\s*[\\)])";

		Pattern comma = Pattern.compile(",");
		Matcher matcher = comma.matcher(this.exprEquation);

		matcher.usePattern(comma);
		while(matcher.find())
			commacount++;

		if(commacount>0){
			//	System.out.println("Comma in the house."+commacount);
			matcher.reset();
			matcher.usePattern( Pattern.compile("(max|min)"));
			while(matcher.find())
				minmaxcount++;	

			if(minmaxcount>0){
				//	System.out.println("minmaxcount in the house.  "+minmaxcount); 
				matcher.reset();
				matcher.usePattern(Pattern.compile(fxn));
				while(matcher.find())
				{
					//	System.out.println("There're some matches in the house."+matcher.group(1));
					if(matcher.group(1).contentEquals("min") ||  matcher.group(1).contentEquals("max"))
						;
					else
						throw new ErreurDeSyntaxe("Il y'a une virgule sans la bonne fonction correpondante.(Pas de virgule pour cette fontion). ", "");	

					minmaxcountSyntaxe++;
				}
			}
		}
		//	System.out.println("minmaxcount in the house.  "+minmaxcount); 
		//	System.out.println("minmaxcountSyntaxe in the house.  "+minmaxcountSyntaxe);

		if(commacount != 0 && (minmaxcount != minmaxcountSyntaxe))
			throw new ErreurDeSyntaxe("Erreur de syntaxe d'une fonction min/max. ", "");
		else if((minmaxcount != 0 || commacount != 0) && commacount > minmaxcount)
			throw new ErreurDeSyntaxe("Il y'a une virgule sans la bonne fonction correpondante. ", "");
		else if((minmaxcount != 0 || commacount != 0) && commacount < minmaxcount)
			throw new ErreurDeSyntaxe("Erreur de syntaxe de la fonction min/max. ", "");

		//	System.out.println("8 - Comma check passed.");
	}


}
