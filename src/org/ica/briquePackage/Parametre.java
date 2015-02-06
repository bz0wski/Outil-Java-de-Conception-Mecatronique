package org.ica.briquePackage;
import java.io.IOException;
import java.io.NotActiveException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.ica.enumeration.SousTypeParametre;
import org.ica.enumeration.TypeParametre;
import org.ica.exceptions.MainApplicationException;
import org.ica.exceptions.SubtypeInconsistency;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Namespace;
import org.simpleframework.xml.Order;
import org.simpleframework.xml.Root;

/**Class Responsible for creating a {@code Parametre} object
 * <br> <br> {@linkplain #hashCode()} and {@link #equals(Object)} overridden.
  
 *

 * @author Salim Ahmed*/
@Root
@Namespace(reference = "org.ica.briquePackage.Parametre")
@Order(elements={"Nom-Paramètre","Type","Sous-Type","Valeur-Paramètre","Unité","Propriété","Description"})

public class Parametre extends MyAbstractModelObject implements Serializable {

	private static final long serialVersionUID = 145L;
		//Définition des caractéristiques d'un paramètre
			private static long paramId;
			
			@Attribute(name = "parametreId", required = true)
			/**uniqueParamId : Id of the {@code Parametre} object*/
			private long uniqueParamId;
			
			@Element(name = "Nom-Paramètre", required=true)
			/**@param nomP : Name of the {@code Parametre} object*/
			private String nomP;
			
			@Element(name = "Type", required=true)
			/** typeP : The {@link TypeParametre} of this {@code Parametre} object.*/
			private TypeParametre typeP;
			
			@Element(name = "Sous-Type", required=true)
			/**sousTypeP : The {@link SousTypeParametre} of this {@code Parametre} object*/
			private SousTypeParametre sousTypeP;
			
			@Element(name = "Valeur-Paramètre", required = false)
			/** valeurP : Indicates the value of the {@code Parametre} object, only if it's type is input.*/
			private String valeurP;
			
			@Element(name = "Unité", required=false)
			/** @param uniteP : The unit of the {@code Parametre}  .*/
			private String uniteP;
			
			@Element(name = "Description")
			/** descP : Presents additional information on the {@code Parametre}.*/
			private String descP;
			
			@Element(name = "Propriété")
			/**proprieteP : Supplementary information on the {@code Parametre} , may serve to classify the parameter.*/
			private String proprieteP;
			
			
			
	/**
	 * Default Constructor, intialises all the attributes to the default values of a {@code Parametre} object. Takes no attributes.
	 */
	public Parametre(){
		Parametre.paramId +=1;
		this.uniqueParamId = Parametre.paramId;
		this.nomP = "null";
		this.typeP = TypeParametre.UNDETERMINED;
		this.valeurP = "";
		this.descP = "Aucune description";
		this.sousTypeP = SousTypeParametre.FREE;
		this.proprieteP = "Propriété";
		this.uniteP = "null";
	}
	/**
	 * This Constructor takes one parameter, the name of the {@code Parametre} object, the remainder of the attributes 
	 * are initialised to the default values of a {@code Parametre} object.
	 * @param nom : A {@link String}, the name of the {@code Parametre} object.
	 */
	public Parametre(String nom){
		Parametre.paramId +=1;
		this.uniqueParamId = Parametre.paramId;
		this.nomP = nom;
		this.typeP = TypeParametre.UNDETERMINED;
		this.valeurP = "";
		this.descP = "Aucune description";
		this.sousTypeP = SousTypeParametre.FREE;
		this.proprieteP = "Propriété";
		this.uniteP = "";
	}
	
	/**
	 * This constructor takes all the attributes of a {@code Parametre} object as parameters and initialises all of them.
	 * @param nom : A {@code String}, the name of the {@code Parametre} object.
	 * @param type : A {@link TypeParametre}, the type of the {@code Parametre} object. 
	 * @param valeur : A {@code String}, the numerical value(s) of the {@code Parametre} object.
	 * @param desc :A {@code String}, a description of the {@code Parametre} object.
	 * @param sstype : A {@link SousTypeParametre}, the subtype of the {@code Parametre} object. 
	 * @param prop  : A {@code String}, gives a summary of teh properties  of the {@code Parametre} object. 
	 * @param unite : A {@code String}, the unit of the {@code Parametre} object. 
	 * @throws MainApplicationException  {@code MainApplicationException}
	 */
	public Parametre( String nom, TypeParametre type,String valeur,String desc,SousTypeParametre sstype, String prop, String unite) throws MainApplicationException{
		Parametre.paramId +=1;
		this.uniqueParamId = Parametre.paramId;
		this.nomP = nom;
		this.typeP = type;
		this.valeurP = valeur;
		this.descP = desc;
		this.setSousTypeP(sstype);
		this.proprieteP = prop;
		this.uniteP = unite;
		
	}


	public static long getParamId() {
		return paramId;
	}


	public static void setParamId(long paramId) {
		
		Parametre.paramId = paramId;
		
	}


	public long getUniqueParamId() {
		return uniqueParamId;
	}


	public void setUniqueParamId(long uniqueParamId) {
		long oldValue = this.uniqueParamId;
		this.uniqueParamId = uniqueParamId;
		firePropertyChange("uniqueParamId", oldValue, uniqueParamId);
	}

/**
 * 
 * @return The name of the {@code Parametre} object.
 */
	public String getNomP() {
		return nomP;
	}

/**
 * 
Sets the name of the {@code Parametre} object.
 * @param nomP : The name to set.
 */
	public void setNomP(String nomP) {
		String oldValue = this.nomP;
		this.nomP = nomP;
		firePropertyChange("nomP", oldValue, nomP);
	}

/**
 * 
 * @return The type of the {@code Parametre} object.
 */
	public TypeParametre getTypeP() {
		return typeP;
	}

/**
 * 
 * @param typeP : The type to set
 */
	public void setTypeP(TypeParametre typeP) {
		TypeParametre oldValue = this.typeP;
		this.typeP = typeP;
		firePropertyChange("typeP", oldValue, typeP);
	}

/**
 * 
 * @return The numerical value(s) of the {@code Parametre} object.
 */
	public String getValeurP() {
		return valeurP;
	}

/**
 * 
 * @param valeurP : Sets the numerical value(s) of the {@code Parametre} object.
 */
	public void setValeurP(String valeurP) {
		String oldValue = this.valeurP;
		this.valeurP = valeurP;
		firePropertyChange("valeurP", oldValue, valeurP);
	}

/**
 * 
 * @return the description of the {@code Parametre} object.
 */
	public String getDescP() {
		return descP;
	}

/**
 * 
 * @param descP : the description of the {@code Parametre} object.
 */
	public void setDescP(String descP) {
		String oldValue = this.descP;
		this.descP = descP;
		firePropertyChange("descP", oldValue, descP);
	}

/**
 * 
 * @return The subtype of the {@code Parametre} object.
 */
	public SousTypeParametre getSousTypeP() {
		return sousTypeP;
	}
	
/**
 * 
 * @return The property of the {@code Parametre} object.
 */

	public String getProprieteP() {
		return proprieteP;
	}
/**
 * 
 * @return The unit of the {@code Parametre} object.
 */
	public String getUniteP() {
		return uniteP;
	}
/**
 * 
 * @param uniteP : The unit of the {@code Parametre} object to set.
 */
	public void setUniteP(String uniteP) {
		String oldValue = this.uniteP;
		this.uniteP = uniteP;
		firePropertyChange("uniteP", oldValue, uniteP);
	}
/**
 * 
 * @param proprieteP : The property of the the {@code Parametre} object to set.
 */
	public void setProprieteP(String proprieteP) {
		String oldValue = this.proprieteP;
		this.proprieteP = proprieteP;
		firePropertyChange("proprieteP", oldValue, proprieteP);
	}
/**
 * Sets the subtype of the {@code Parametre} object.
 * @param sstype : The {@code SousTypeParametre} to set.
 * @throws MainApplicationException {@code MainApplicationException}
 */
	public void setSousTypeP(SousTypeParametre sstype) throws MainApplicationException {
			if (!this.typeP.getSubtype().contains(sstype)) {
				throw new SubtypeInconsistency("Erreur de sous Type");
			}
			
		SousTypeParametre oldValue = this.sousTypeP;
		this.sousTypeP = sstype;
		firePropertyChange("sousTypeP", oldValue, sstype);
	}	

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Parametre";//Parametre [uniqueParamId=" + uniqueParamId + ", nomP=" + nomP
			//+ ", typeP=" + typeP + ", valeurP=" + valeurP + ", descP="
			//	+ descP + ", proprieteP=" + proprieteP + ", uniteP=" + uniteP
			//	+ ", sousTypeP=" + sousTypeP + "]";
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
		
		paramId = Math.max(paramId, getUniqueParamId());
	}
	
	

	/**
	 * * This is the default implementation of writeObject. Customise if
	* necessary.
	 * @param aObjectOutputStream : The {@linkplain ObjectOutputStream} to write to.
	 * @throws IOException {@code IOException}
	 */
	private void writeObject(ObjectOutputStream aObjectOutputStream) throws IOException{
		 // perform the default serialisation for all non-transient, non-static
	    // fields
		aObjectOutputStream.defaultWriteObject();
	}
	
	/**Override equals() and hashCode() to ensure correct behaviour of the functions
	 * This is due partly to the fact that objects are contantly being serialised and
	 * de-serilised, creating clones with unidentical hashcodes, this causes the default 
	 * equals() method to return false for identical objects.*
	 * 
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((descP == null) ? 0 : descP.hashCode());
		result = prime * result + ((nomP == null) ? 0 : nomP.hashCode());
		result = prime * result
				+ ((proprieteP == null) ? 0 : proprieteP.hashCode());
		result = prime * result
				+ ((sousTypeP == null) ? 0 : sousTypeP.hashCode());
		result = prime * result + ((typeP == null) ? 0 : typeP.hashCode());
		result = prime * result
				+ (int) (uniqueParamId ^ (uniqueParamId >>> 32));
		result = prime * result + ((uniteP == null) ? 0 : uniteP.hashCode());
		result = prime * result + ((valeurP == null) ? 0 : valeurP.hashCode());
		return result;
	}
	/**Override equals() and hashCode() to ensure correct behaviour of the functions
	 * This is due partly to the fact that objects are contantly being serialised and
	 * de-serilised, creating clones with unidentical hashcodes, this causes the default 
	 * equals() method to return false for identical objects.*
	 *  (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof Parametre)) {
			return false;
		}
		Parametre other = (Parametre) obj;
		if (descP == null) {
			if (other.descP != null) {
				return false;
			}
		} else if (!descP.equals(other.descP)) {
			return false;
		}
		if (nomP == null) {
			if (other.nomP != null) {
				return false;
			}
		} else if (!nomP.equals(other.nomP)) {
			return false;
		}
		if (proprieteP == null) {
			if (other.proprieteP != null) {
				return false;
			}
		} else if (!proprieteP.equals(other.proprieteP)) {
			return false;
		}
		if (sousTypeP != other.sousTypeP) {
			return false;
		}
		if (typeP != other.typeP) {
			return false;
		}
		if (uniqueParamId != other.uniqueParamId) {
			return false;
		}
		if (uniteP == null) {
			if (other.uniteP != null) {
				return false;
			}
		} else if (!uniteP.equals(other.uniteP)) {
			return false;
		}
		if (valeurP == null) {
			if (other.valeurP != null) {
				return false;
			}
		} else if (!valeurP.equals(other.valeurP)) {
			return false;
		}
		return true;
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		return (Parametre)super.clone();
	}

	
}

