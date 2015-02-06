package org.ica.utilityClasses;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.ica.briquePackage.Parametre;
import org.ica.enumeration.TypeParametre;
import org.ica.exceptions.InvalidParameter;


public class VerificationValeurParametre {

	private static final String UNESEULEVALEUR = "\\s*[0-9]+\\.{0,1}[0-9]*\\s*";
	private static final String INTERVALLECONTINU = "\\s*([\\]\\[]"+UNESEULEVALEUR+"\\s*;"+UNESEULEVALEUR+"[\\]\\[])\\s*";
	private static final String ENSEMBLEDEVALEURS = "\\s*\\{("+UNESEULEVALEUR+";)+"+UNESEULEVALEUR+"\\}";
	
	
	public VerificationValeurParametre() {
	}
	/**
	 * @param parametre
	 * 	The parameter object who's value is going to be monitored
	 * <br> Verifies that the value of the parameter is in conformity with the type of the parameter.
	 * @throws InvalidParameter {@code InvalidParameter}
	 */
	public static void parameterValues(Parametre parametre) throws InvalidParameter {
		if (parametre.getTypeP().equals(TypeParametre.INPUT)) {
			String val = parametre.getValeurP();
			switch (parametre.getSousTypeP()) {
			case CONSTANT:
				
				Pattern pattern_CONSTANT = Pattern.compile(UNESEULEVALEUR);
				Matcher matcher_CONSTANT = pattern_CONSTANT.matcher(val);
				if (matcher_CONSTANT.matches()) {
					Float.parseFloat(matcher_CONSTANT.group());
				}
				else
					throw new InvalidParameter("Erreur, vérifier la saisie.");
				break;
				
			case RANGE:
			
				Pattern pattern_RANGE = Pattern.compile(INTERVALLECONTINU);
				Matcher matcher_RANGE = pattern_RANGE.matcher(val);
				if (matcher_RANGE.matches()) {
					List<Float> values = new ArrayList<Float>(); 
				
					matcher_RANGE.reset();
					matcher_RANGE.usePattern(Pattern.compile(UNESEULEVALEUR));
					while (matcher_RANGE.find()) {
						values.add(Float.parseFloat(matcher_RANGE.group()));
					}
			
					
				}
				else
					throw new InvalidParameter("Erreur, vérifier la saisie.");
				break;
				
			case SET:
		
				Pattern pattern_SET = Pattern.compile(ENSEMBLEDEVALEURS);
				Matcher matcher_SET = pattern_SET.matcher(val);
				if (matcher_SET.matches()) {
					List<Float> values = new ArrayList<Float>();
			
					matcher_SET.reset();
					matcher_SET.usePattern(Pattern.compile(UNESEULEVALEUR));
					while (matcher_SET.find()) {
						values.add(Float.parseFloat(matcher_SET.group()));
					}
	
					
				}
				else
					throw new InvalidParameter("Erreur, vérifier la saisie.");
				break;
			default:
				break;
			}
		}
	}
	

	
	
	/**@param parametre
	 * 	The parameter object who's value is going to be monitored
	 * @param valeurEnCoursDeSaisie
	 * 	As string object which contains which is updated as the value of the parameter is inserted.
	 * 
	 * <br>I need this second function to for the GUI, to constantly monitor the text that is entered.
	The validator works better this way.
	 @throws InvalidParameter {@code InvalidParameter}*/
	public static void parameterValues(Parametre parametre, String valeurEnCoursDeSaisie) throws InvalidParameter {
		if (parametre.getTypeP().equals(TypeParametre.INPUT) && valeurEnCoursDeSaisie.length()>0) {
			
			switch (parametre.getSousTypeP()) {
			case CONSTANT:
				
				Pattern pattern_CONSTANT = Pattern.compile(UNESEULEVALEUR);
				Matcher matcher_CONSTANT = pattern_CONSTANT.matcher(valeurEnCoursDeSaisie);
				if (matcher_CONSTANT.matches()) {
					Float.parseFloat(matcher_CONSTANT.group());
				}
				else
					throw new InvalidParameter("Erreur, vérifier la saisie.");
				break;
				
			case RANGE:
			
				Pattern pattern_RANGE = Pattern.compile(INTERVALLECONTINU);
				Matcher matcher_RANGE = pattern_RANGE.matcher(valeurEnCoursDeSaisie);
				if (matcher_RANGE.matches()) {
					List<Float> values = new ArrayList<Float>(); 
				
					matcher_RANGE.reset();
					matcher_RANGE.usePattern(Pattern.compile(UNESEULEVALEUR));
					while (matcher_RANGE.find()) {
						values.add(Float.parseFloat(matcher_RANGE.group()));
					}
			
					
				}
				else
					throw new InvalidParameter("Erreur, vérifier la saisie.");
				break;
				
			case SET:
		
				Pattern pattern_SET = Pattern.compile(ENSEMBLEDEVALEURS);
				Matcher matcher_SET = pattern_SET.matcher(valeurEnCoursDeSaisie);
				if (matcher_SET.matches()) {
					List<Float> values = new ArrayList<Float>();
			
					matcher_SET.reset();
					matcher_SET.usePattern(Pattern.compile(UNESEULEVALEUR));
					while (matcher_SET.find()) {
						values.add(Float.parseFloat(matcher_SET.group()));
					}
	
					
				}
				else
					throw new InvalidParameter("Erreur, vérifier la saisie.");
				break;
			default:
				break;
			}
		}
	}

}
