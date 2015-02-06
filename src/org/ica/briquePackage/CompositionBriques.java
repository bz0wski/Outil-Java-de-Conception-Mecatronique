/**
 * 
 */
package org.ica.briquePackage;

import java.io.IOException;
import java.io.NotActiveException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Order;
import org.simpleframework.xml.Root;


/**
 * Class responsible for constructing a super model, i.e a model which contains other models<br>
 *  <br><br>  {@link #hashCode()} and {@link #equals(Object)} overridden
 * @author Salim AHMED
 */
@Root
@Order(elements={"Nom-Composition","Brique-Composition","Equation-Composition", "Parametre-Composition"})
public class CompositionBriques extends Brique implements Serializable {
	private static long compId;
	
	@Attribute(required = true, name= "compositionId", empty ="0")
	/** uniqueCompId : Id of the CompositionBriques( super model) object*/
	private long uniqueCompId;

	@Element(name = "Nom-Composition", required=true)
	/**Name of the composition*/
	private String nomComposition;

	@ElementList(entry = "Brique-Composition", inline=true,required = false)
	/**brique : Holds a list of all the {@link Brique} objects contained in the CompositionBriques.*/
	private List <Brique> brique;

	//Note to self: Don't write the Equation and Parameter List to disk, this creates double copies of all the 
	//Equations and Parameters, the reason I think is because the persister on creating the object graph for  
	//every Brique object recognises that the elements in the list references certain elements already written???.

	
	@ElementList(entry = "Equation-Composition", inline = true, required =  false)
	/** equation : A {@link List} of all the equations contained in the <code>CompositionBriques</code> object*/
	private List <Equation> equation;

	
	@ElementList(entry = "Parametre-Composition", inline = true, required =  false)
	/** parametre : A {@link List} of all the parameters contained in the <code>CompositionBriques</code> object*/
	private List <Parametre> parametre;	


	@ElementList(entry = "Global-Equation-Composition", inline = true, required =  false)
	/** globalEquations : A list of all the global Equations contained in the {@link CompositionBriques} object
	 * <br>Global equations are no different from regular equations, the reason I need them stored separately is to be able to restore
	 * them to the project canvas with minimal overhead.  <br> <br>NB: This list isn't used in any operation whatsoever, it's content are duplicated 
	 *  in the main equation List*/
	private List <Equation> globalEquations;	

	private static final long serialVersionUID = 1L;

	/**Default constructor of CompositionBriques
	 * */
	public CompositionBriques() {

		this.uniqueCompId = compId+=1;
		this.nomComposition = "";
		this.equation = new ArrayList<Equation>();
		this.parametre = new ArrayList<Parametre>();
		this.brique = new ArrayList<>();
		this.globalEquations = new ArrayList<>();

	}

	/**@param nom
	 * contains the name of a composition
	 * @param brique
	 * contains a list of Brique to initialise the composition
	 * */
	public CompositionBriques(String nom,List<Brique> brique) {
		uniqueCompId = compId+=1;
		this.brique = brique;
		this.nomComposition = nom;
		this.equation = new ArrayList<Equation>();
		this.parametre = new ArrayList<>();
		this.globalEquations = new ArrayList<>();

	}
	/**
	 * 
	 * @return the list of all the briques contained in this object
	 */
	public List<Brique> getBrique() {
		return brique;
	}

	/**@param brique, list of Brique to add to the Composition
	 *
	 * */
	public void setBrique(List<Brique> brique) {
		List<Brique>oldValue = this.brique;
		this.brique = brique;
		firePropertyChange("brique", oldValue, brique);
		List<Equation> tempListEqn = new ArrayList<>();
		List<Parametre> tempListP = new ArrayList<>();

		for (Brique tempBrique : brique) {
			tempListEqn.addAll(tempBrique.getListEquations());
			for (Equation equation : tempBrique.getListEquations()) {
				tempListP.addAll(equation.getListeDeParametresEqn());
			}


		}
		this.setEquation(tempListEqn);
		this.setParametre(tempListP);
		firePropertyChange("brique", oldValue, brique);
	}

	/**
	 * This sub routine serves to add equations to the main equation list that're
	 * not part of any Brique object
	 * @param equations : the Equation list to append to the current Equation list
	 */
	public void augmentEquationList(List<Equation> equations) {
		List<Parametre> parametres = new ArrayList<Parametre>();
		for (Equation equation : equations) {
			parametres.addAll(equation.getListeDeParametresEqn());
		}
		getEquation().addAll(equations);
		getParametre().addAll(parametres);
		setGlobalEquations(equations);
	}

	/**
	 * @return the globalEquations
	 */
	public List<Equation> getGlobalEquations() {
		return globalEquations;
	}

	/**
	 * @param globalEquations the globalEquations to set
	 */
	public void setGlobalEquations(List<Equation> globalEquations) {
		List<Equation>oldValue = this.globalEquations;
		this.globalEquations = globalEquations;
		firePropertyChange("globalEquations", oldValue, globalEquations);
	}

	public List<Equation> getEquation() {
		return equation;

	}


	public void setEquation(List<Equation> equation) {
		List<Equation>oldValue = this.equation;
		this.equation = equation;
		firePropertyChange("equation", oldValue, equation);
	}


	public List<Parametre> getParametre() {
		return parametre;
	}


	public void setParametre(List<Parametre> parametre) {
		List<Parametre>oldValue = this.parametre;
		this.parametre = parametre;
		firePropertyChange("parametre", oldValue, parametre);
	}


	public  long getUniqueCompId() {
		return uniqueCompId;
	}


	public  void setUniqueCompId(long uniqueCompId) {
		long oldValue = this.uniqueCompId;
		this.uniqueCompId = uniqueCompId;
		firePropertyChange("uniqueCompId", oldValue, uniqueCompId);
	}

	public String getNomComposition() {
		return nomComposition;
	}

	public void setNomComposition(String nomCompositionString) {
		String oldValue = this.nomComposition;
		this.nomComposition = nomCompositionString;
		firePropertyChange("nomComposition", oldValue, nomCompositionString);
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

		compId = Math.max(compId, getUniqueCompId());
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
		return "CompositionBriques [uniqueCompId=" + uniqueCompId
				+ ", nomComposition=" + nomComposition + ", brique=" + brique
				+ ", equation=" + equation + ", parametre=" + parametre + "]";
	}

	/**I generated custom hashcode()  and equals methods to ensure correct behaviour of the functions
	 * This is due partly to the fact that objects are contantly being serialised and
	 * de-serilised, creating clones with unidentical hashcodes, this causes the default 
	 * equals() method to return false for identical objects.*
	 *
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((brique == null) ? 0 : brique.hashCode());
		result = prime * result
				+ ((equation == null) ? 0 : equation.hashCode());
		result = prime * result
				+ ((nomComposition == null) ? 0 : nomComposition.hashCode());
		result = prime * result
				+ ((parametre == null) ? 0 : parametre.hashCode());
		result = prime * result + (int) (uniqueCompId ^ (uniqueCompId >>> 32));
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
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (!(obj instanceof CompositionBriques)) {
			return false;
		}
		CompositionBriques other = (CompositionBriques) obj;
		if (brique == null) {
			if (other.brique != null) {
				return false;
			}
		} else if (!brique.equals(other.brique)) {
			return false;
		}
		if (equation == null) {
			if (other.equation != null) {
				return false;
			}
		} else if (!equation.equals(other.equation)) {
			return false;
		}
		if (nomComposition == null) {
			if (other.nomComposition != null) {
				return false;
			}
		} else if (!nomComposition.equals(other.nomComposition)) {
			return false;
		}
		if (parametre == null) {
			if (other.parametre != null) {
				return false;
			}
		} else if (!parametre.equals(other.parametre)) {
			return false;
		}
		if (uniqueCompId != other.uniqueCompId) {
			return false;
		}
		return true;
	}




}
