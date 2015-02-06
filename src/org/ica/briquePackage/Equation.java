package org.ica.briquePackage;

import java.io.IOException;
import java.io.NotActiveException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.ica.exceptions.MainApplicationException;
import org.ica.utilityClasses.ExtractionParametre;
import org.ica.utilityClasses.VerificationSyntaxeEquation;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Namespace;
import org.simpleframework.xml.Order;
import org.simpleframework.xml.Root;


/**Class Responsible for creating an {@code Equation} object.
 <br>  {@link #hashCode()} and {@link #equals(Object)} overridden
 * @author Salim Ahmed*/
@Root
@Namespace(reference = "org.ica.briquePackage.Equation")
@Order(elements = {"Expression-Equation","Contrainte","Orienté","Parametre-de-Sortie","Parametre","Description","Propriétés"})
public class Equation extends MyAbstractModelObject implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1111L;
	
	private static long id;
	@Attribute(required = true, name = "equationId")
	/**uniqueEqnId : Id of the {@code Equation} object*/
	private long uniqueEqnId;
	
	@Element(name = "Expression-Equation")
	/** contenuEqn : Expression of the {@code Equation} object*/
	private String contenuEqn;
	
	@Element(name = "Description")
	/** descEqn : Presents additional information on the {@code Equation}.*/
	private String descEqn;
	
	@Element(name = "Parametre-de-Sortie", required = false)
	/**parametreDeSortie : The output parameter of the {@code Equation} object if it is oriented*/
	private Parametre parametreDeSortie = null ;
	
	@ElementList(entry = "Parametre", inline=true)
	/**listeDeParametresEqn : Holds a list of all the {@code Equation} parameters.*/
	private List<Parametre> listeDeParametresEqn;
	
	@Element(name = "Contrainte")
	/**isConstraint : Indicates whether the {@code Equation} is a constraint or not.*/
	private boolean isConstraint;
	
	@Element(name = "Orienté")
	/** isOriented : Indicates the orientation of the {@code Equation} object.*/
	private boolean isOriented;
	
	@Element(name = "Propriétés", required = false)
	/** proprieteEqn : Supplementary information on the {@code Equation} , may serve to classify the equation.*/
	private String proprieteEqn; 
	
	@Element(name = "Modifiable", required = true, data = false, type = Boolean.class)
	/**Indicates whether the equation statement is modifiable*/
	private boolean isModifiable = false;
	/**This is to hold a reference to the {@link Brique} that'll 
	 * eventually host this {@link Equation}  object, the value should modified anytime
	 * the Equation is added to a {@link Brique}*/
	@Element(name = "Reference-Brique", required = true)
	private long referenceBrique = -1;


	/**
	 * [I]Default Constructor
	 * @throws MainApplicationException {@code MainApplicationException}
	 */
	public Equation() throws MainApplicationException{
		//Définition des caractéristiques d'une équation
		uniqueEqnId = id+=1;
		this.contenuEqn = "x+y=z";
		this.descEqn = "Aucune Description";
		this.parametreDeSortie = null;
		this.listeDeParametresEqn = new ArrayList<Parametre>();
		this.isOriented = false;
		this.proprieteEqn = "";		
		this.isConstraint = false;
		this.isModifiable = false;
	}
	
	/**
	 * [II]Constructor without Valeur de sortie(NON ORIENTED)
	 * @param contrainte
	 *  contains a boolean, sets the isConstraint field of this object
	 * @param contenu
	 * contains a string which represents the equation expression
	 * @param orientation
	 * contains a boolean, sets the isOriented field of this object
	 * @param propriete
	 * contains text on the properties of the equation, this information 
	 * @param commentaire
	 * contains text on the description of the contents of a Brique.
	 * @throws MainApplicationException {@code MainApplicationException}
	 */
	public Equation(Boolean contrainte, String contenu,Boolean orientation,String propriete,String commentaire)  throws MainApplicationException{
		//Définition des caractéristiques d'une équation
		uniqueEqnId = id+=1;
		this.contenuEqn = contenu;
		this.descEqn = commentaire;
		this.isModifiable = false;
		this.parametreDeSortie = null;
		this.listeDeParametresEqn = new ArrayList<Parametre>();
		this.isOriented = orientation;
		this.proprieteEqn = propriete;
		this.isConstraint = contrainte;
		
		VerificationSyntaxeEquation validate = 	new VerificationSyntaxeEquation(this.contenuEqn);
		this.setContenuEqn(validate.getExprEquation());
		
		
		this.setListeDeParametresEqn(ExtractionParametre.listEquationParameters(this));	
	}

	/**
	* This constructor requires all fields to be set.<br>[III]Constructor with all parameters set
	*
	* @param contrainte
	* contains a boolean, sets the isConstraint field of this object
	* @param contenu 
	* contains a string which represents the equation expression
	* 
	* @param orientation
	* contains a boolean, sets the isOriented field of this object
	* @param parametreSortie
	* contains an output parameter if the Equatios is Oriented
	* 
	* @param propriete
	* contains text on the properties of the equation, this information 
	* can be used in classifying the equation
	* @param commentaire
	* contains text on the description of the contents of a Brique.
	* 	
	* @throws MainApplicationException {@code MainApplicationException}
	*/

	public Equation(Boolean contrainte, String contenu,Boolean orientation,Parametre parametreSortie,String propriete,String commentaire) throws MainApplicationException {
		//Définition des caractéristiques d'une équation
		this.contenuEqn = contenu;
		this.descEqn = commentaire;
		this.parametreDeSortie = parametreSortie;
		this.listeDeParametresEqn = new ArrayList<Parametre>();
		this.isOriented = orientation;
		this.isModifiable = false;
		this.proprieteEqn = propriete;
		this.isConstraint = contrainte; 
		
		VerificationSyntaxeEquation validate = new VerificationSyntaxeEquation(this.contenuEqn);
	
		this.setContenuEqn(validate.getExprEquation());

		
		this.setListeDeParametresEqn(ExtractionParametre.listEquationParameters(this));

	}

	public Parametre getParametreByName(String nomP) {		
			for (Parametre parametre : getListeDeParametresEqn()) {
				if (parametre.getNomP().equals(nomP)) {
					return parametre;
				}
			}
		return null;
	}
	
	
	public boolean isConstraint() {
		return isConstraint;
	}

	public boolean getIsConstraint() {
		return isConstraint;
	}
	
	public void setIsConstraint(boolean isConstraint) {
		boolean oldValue = this.isConstraint;
		this.isConstraint = isConstraint;
		firePropertyChange("isConstraint", oldValue, isConstraint);
	}


	
	public boolean isOriented() {
		return isOriented;
	}


	/**
	 * @return the isModifiable
	 */
	public boolean getIsModifiable() {
		return isModifiable;
	}

	/**
	 * @param isModifiable the Modifiable property to set
	 */
	public void setIsModifiable(boolean isModifiable) {
		boolean oldValue = this.isModifiable;
		this.isModifiable = isModifiable;
		firePropertyChange("isModifiable", oldValue, isModifiable);
	}

	public boolean getIsOriented() {
		return isOriented;
		
	}
	
	
	public void setIsOriented(boolean isOriented) {
		Boolean oldValue = this.isOriented;
		this.isOriented = isOriented;
		firePropertyChange("isConstraint", oldValue, isOriented);
	}
	

	public long getUniqueEqnId() {
		return uniqueEqnId;
	}

	public void setUniqueEqnId(int uniqueEqnId) {
		long oldValue = this.uniqueEqnId;
		this.uniqueEqnId = uniqueEqnId;
		firePropertyChange("uniqueEqnId", oldValue, uniqueEqnId);
	}

	public String getContenuEqn() {
		return contenuEqn;
	}

	public void setContenuEqn(String contenuEqn) {
		
		String oldValue = this.contenuEqn;
		this.contenuEqn = contenuEqn;
		firePropertyChange("contenuEqn", oldValue, contenuEqn);
	}


	public String getDescEqn() {
		return descEqn;
	}

	public String getProprieteEqn() {
		return proprieteEqn;
	}

	public void setProprieteEqn(String proprieteEqn) {
		this.proprieteEqn = proprieteEqn;
	}

	public void setDescEqn(String commentaire) {
		String oldValue = this.descEqn;
		this.descEqn = commentaire;
		firePropertyChange("descEqn", oldValue, commentaire);
	}

	public Parametre getParametreDeSortie() {
		return parametreDeSortie;
	}

	public void setParametreDeSortie(Parametre valeurDeSortie) {
		Parametre oldValue = this.parametreDeSortie;
		this.parametreDeSortie = valeurDeSortie;
		firePropertyChange("parametreDeSortie", oldValue, valeurDeSortie);
	}

	public List<Parametre> getListeDeParametresEqn_DYNAMIC() throws MainApplicationException {
		List<Parametre> listP = ExtractionParametre.listEquationParameters(this);
		setListeDeParametresEqn(listP);
		return listP;
	}

	public List<Parametre> getListeDeParametresEqn()  {
		
		return this.listeDeParametresEqn;
	}

	public void setListeDeParametresEqn(List<Parametre> listeDeParametresEqn) {
		List<Parametre> oldValue = this.listeDeParametresEqn;
		this.listeDeParametresEqn = listeDeParametresEqn;
		firePropertyChange("listeDeParametresEqn", oldValue, listeDeParametresEqn);
	}

	public static char[] getDelimiters() {
		//I'm choosing to use char here because I'm dealing with single character items, less cumbersome data to compute unlike String  
		
		char[] delims = {'(',')','[',']','{','}'};
		return delims;
	}
	
	public static char[] getOperators() {
		//I'm choosing to use char here because I'm dealing with single character items, less cumbersome data to compute unlike String  
		
		char[] ops = {'+','-','/','^','%','*','=',','};
		return ops;
		
	}
	
	public static String[] getFunctions(){
		//I'm choosing to use String here because I'm dealing with function names that contain more than one character contrary to 
		// the operator and delimiter 
		String[] fxns = {"abs","acos","asin","atan","cbrt","ceil","cos","cosh","max","min","exp","expm1","floor","log","sin","sinh","sqrt","tan","tanh"};
		return fxns;	
	}

	/**
	 * * Always treat de-serialization as a full-blown constructor, by validating
	* the final state of the de-serialized object.
	 * @param aInputStream : the {@code aInputStream} to read from
	 * @throws ClassNotFoundException {@code ClassNotFoundException}
	 * @throws IOException {@code ClassNotFoundException}
	 * @throws NotActiveException {@code ClassNotFoundException}
	 */
	private void readObject(ObjectInputStream aInputStream)
			throws ClassNotFoundException, IOException,NotActiveException  {
		 // always perform the default de-serialization first
		aInputStream.defaultReadObject();
		//restoring the Static to its old value 
		
		id = Math.max(id, getUniqueEqnId());
		
	}
	
	/**
	 * * This is the default implementation of writeObject. Customise if
	* necessary.
	 * @param aObjectOutputStream : The {@linkplain ObjectOutputStream} to write to.
	 * @throws IOException {@code IOException}
	 */
	private void writeObject(ObjectOutputStream aObjectOutputStream) throws IOException{
		 // perform the default serialization for all non-transient, non-static
	    // fields
		aObjectOutputStream.defaultWriteObject();
	}
	
	@Override
	public String toString() {
		return this.contenuEqn+"__";
	}
	

	/**I generated custom hashcode()  and equals methods to ensure correct behaviour of the functions
	 * This is due partly to the fact that objects are contantly being serialised and
	 * de-serilised, creating clones with unidentical hashcodes, this causes the default 
	 * equals() method to return false for identical objects.*
	 * 
	 (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ ((contenuEqn == null) ? 0 : contenuEqn.hashCode());
		result = prime * result + ((descEqn == null) ? 0 : descEqn.hashCode());
		result = prime * result + (isConstraint ? 1231 : 1237);
		result = prime * result + (isModifiable ? 1231 : 1237);
		result = prime * result + (isOriented ? 1231 : 1237);
		result = prime
				* result
				+ ((listeDeParametresEqn == null) ? 0 : listeDeParametresEqn
						.hashCode());
		result = prime
				* result
				+ ((parametreDeSortie == null) ? 0 : parametreDeSortie
						.hashCode());
		result = prime * result
				+ ((proprieteEqn == null) ? 0 : proprieteEqn.hashCode());
		result = prime * result
				+ (int) (referenceBrique ^ (referenceBrique >>> 32));
		result = prime * result + (int) (uniqueEqnId ^ (uniqueEqnId >>> 32));
		return result;
	}

	/**I generated custom hashcode()  and equals methods to ensure correct behaviour of the functions
	 * This is due partly to the fact that objects are contantly being serialised and
	 * de-serilised, creating clones with unidentical hashcodes, this causes the default 
	 * equals() method to return false for identical objects.*
	 * 
	 (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		Equation other = (Equation) obj;
		if (contenuEqn == null) {
			if (other.contenuEqn != null)
				return false;
		} else if (!contenuEqn.equals(other.contenuEqn))
			return false;
		if (descEqn == null) {
			if (other.descEqn != null)
				return false;
		} else if (!descEqn.equals(other.descEqn))
			return false;
		if (isConstraint != other.isConstraint)
			return false;
		if (isModifiable != other.isModifiable)
			return false;
		if (isOriented != other.isOriented)
			return false;
		if (listeDeParametresEqn == null) {
			if (other.listeDeParametresEqn != null)
				return false;
		} else if (!listeDeParametresEqn.equals(other.listeDeParametresEqn))
			return false;
		if (parametreDeSortie == null) {
			if (other.parametreDeSortie != null)
				return false;
		} else if (!parametreDeSortie.equals(other.parametreDeSortie))
			return false;
		if (proprieteEqn == null) {
			if (other.proprieteEqn != null)
				return false;
		} else if (!proprieteEqn.equals(other.proprieteEqn))
			return false;
		if (referenceBrique != other.referenceBrique)
			return false;
		if (uniqueEqnId != other.uniqueEqnId)
			return false;
		return true;
	}

	/**
	 * @return the referenceBrique
	 */
	public long getReferenceBrique() {
		return referenceBrique;
	}

	/**
	 * @param referenceBrique the referenceBrique to set
	 */
	public void setReferenceBrique(long referenceBrique) {
		long oldValue = this.referenceBrique;
		this.referenceBrique = referenceBrique;
		firePropertyChange("referenceBrique", oldValue, referenceBrique);
	}

	public static long getId(){
		return id;
	}

	public static void setId(long eId){
		Equation.id = eId;
	}
}
