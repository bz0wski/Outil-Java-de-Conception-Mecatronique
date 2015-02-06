package org.ica.briquePackage;

import java.io.IOException;
import java.io.NotActiveException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Order;
import org.simpleframework.xml.Root;
/**
 * Class responsible for constructing a base model<br> <br> {@linkplain #hashCode()} and {@link #equals(Object)} overridden.
 * 
 * @author Salim AHMED
 */

@Root
@Order(elements={"Nom-Brique","Equation-Brique","Parametre-Brique","Informations-Brique"})
public class Brique extends MyAbstractModelObject implements Serializable {


	private static final long serialVersionUID = 249L;

	private static long uniqueBriqueId;

	
	@Attribute(required = true , name= "briqueId", empty="0")
	/**briqueId : The id of this {@code Brique} object.*/
	private long briqueId;

	@Element(name = "Nom-Brique", required = false)
	private String nomBrique;



	@ElementList(required = false, entry= "Equation-Brique", inline = true)
	/**listEquations : Holds a list of all the equations contained in the brique.*/
	private List<Equation> listEquations;

	@ElementList(required = false, entry= "Parametre-Brique", inline = true)
	/**listParametres : Holds a list of all the equation parameters contained in the brique.*/
	private List<Parametre> listParametres;

	@Element(required = false, name= "Informations-Brique")	
	/**infoBrique : Presents additional information on the brique.*/
	private String infoBrique;


	/**Default Constructor : Initialises all the attributes
	 * */
	public Brique(){

		this.briqueId = uniqueBriqueId+=1;;	
		this.listEquations = new ArrayList<>();
		this.listParametres = new ArrayList<>();
		this.infoBrique = "Aucune Info";
		this.nomBrique = "";
	}
	/**This constructor requires a name field
	 *  @param nom
	 * contains only letters,numbers and underscores. 
	 * The most used constructor.
	 * */
	public Brique(String nom){
		//	System.err.println("2nd contructor");
		this.briqueId = uniqueBriqueId+=1;;	
		this.listEquations = new ArrayList<>();
		this.listParametres = new ArrayList<>();
		this.infoBrique = "Aucune Info";
		this.nomBrique = nom;
	}

	/**
	 *  * This constructor requires a name field, a Map of Equation with it's Parametres and a Description
	 *  of the Brique, it's the best case implementation
	 *
	 * @param nom
	 * contains only letters,numbers and underscores.
	 *  @param mapEqnsParams : List of the equation parameters.
	 * @param info
	 * contains text on the description of the contents of a Brique.
	 *<br> The only purpose of this Constructor is for debugging.
	 **/
	@Deprecated
	public Brique(String nom,Map<Equation, List<Parametre>> mapEqnsParams,String info) {
		//Définition des caractéristiques d'un modèle de base
	
		this.briqueId = uniqueBriqueId+=1;;
		this.nomBrique = nom;
		this.listEquations = new ArrayList<>();
		this.listParametres = new ArrayList<>();

		for (Iterator<Map.Entry<Equation, List<Parametre>>> mapiterator = mapEqnsParams.entrySet().iterator(); mapiterator.hasNext();) {
			Map.Entry<Equation, List<Parametre>> entry = mapiterator.next();
			if (entry.getKey() != null) {
				Equation equation = (Equation)entry.getKey();
				this.listEquations.add(equation);
				//this.parametre.addAll(entry.getValue());
				for (Parametre parametre : equation.getListeDeParametresEqn()) {
					this.listParametres.add(parametre);
				}
			}
		}

		this.infoBrique = info;
	}

	public long getBriqueId() {
		return this.briqueId;
	}

	public static long getUniqueBriqueId() {
		return uniqueBriqueId;
	}


	public static void setUniqueBriqueId(long uniqueModelId) {
		uniqueBriqueId = uniqueModelId;	
	}


	public String getNomBrique() {
		return nomBrique;
	}

	public void setNomBrique(String nomBrique) {
		String oldValue = this.nomBrique;
		this.nomBrique = nomBrique;
		firePropertyChange("nomBrique", oldValue, nomBrique);
	}

	/**
	 * @return the infoBrique
	 */
	public String getInfoBrique() {
		return infoBrique;
	}

	public void setInfoBrique(String infoModele) {
		String oldValue = this.infoBrique;
		this.infoBrique = infoModele;
		firePropertyChange("infoBrique", oldValue, infoModele);

	}




	/**
	 * @return the listEquations
	 */
	public List<Equation> getListEquations() {
		return listEquations;
	}

	/**
	 * @param listEquations the listEquations to set
	 */
	public void setListEquations(List<Equation> listEquations) {
		List<Equation> oldValue = this.listEquations;
		this.listEquations = listEquations;
		
		List<Parametre> tempListP = new ArrayList<>();

		for (Equation equation : listEquations) {
			tempListP.addAll(equation.getListeDeParametresEqn());
		}
		this.setListParametres(tempListP);
		firePropertyChange("listEquations", oldValue, listEquations);

	}

	/**
	 * @return the listParametres
	 */
	public List<Parametre> getListParametres() {
		return listParametres;
	}

	/**
	 * @param listParametres the listParametres to set
	 */
	public void setListParametres(List<Parametre> listParametres) {
		List<Parametre> oldValue = this.listParametres;
		this.listParametres = listParametres;
		firePropertyChange("listParametres", oldValue, listParametres);
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
		uniqueBriqueId = Math.max(uniqueBriqueId, getBriqueId());

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
		return this.nomBrique+"__";
	}

	/**I generated custom hashcode()  and equals methods.
	 *  (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + (int) (briqueId ^ (briqueId >>> 32));
		result = prime * result
				+ ((listEquations == null) ? 0 : listEquations.hashCode());
		result = prime * result
				+ ((listParametres == null) ? 0 : listParametres.hashCode());
		result = prime * result
				+ ((nomBrique == null) ? 0 : nomBrique.hashCode());
		return result;
	}
	/**I generated custom hashcode()  and equals methods to ensure correct behaviour of the functions
	 * This is due partly to the fact that objects are contantly being serialised and
	 * de-serilised, creating clones with unidentical hashcodes, this causes the default 
	 * equals() method to return false for identical objects.*
	 * 
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (!(obj instanceof Brique)) {
			return false;
		}
		Brique other = (Brique) obj;
		if (briqueId != other.briqueId) {
			return false;
		}
		if (listEquations == null) {
			if (other.listEquations != null) {
				return false;
			}
		} else if (!listEquations.equals(other.listEquations)) {
			return false;
		}
		if (listParametres == null) {
			if (other.listParametres != null) {
				return false;
			}
		} else if (!listParametres.equals(other.listParametres)) {
			return false;
		}
		if (nomBrique == null) {
			if (other.nomBrique != null) {
				return false;
			}
		} else if (!nomBrique.equals(other.nomBrique)) {
			return false;
		}
		return true;
	}

}
