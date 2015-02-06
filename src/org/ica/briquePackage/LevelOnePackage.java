package org.ica.briquePackage;

import java.io.IOException;
import java.io.NotActiveException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.ElementListUnion;
import org.simpleframework.xml.Namespace;
import org.simpleframework.xml.Order;
import org.simpleframework.xml.Root;
/**
 * Class responsible for holding all the top level objects, i.e sub Packages {@link LevelTwoPackage} and super models {@link CompositionBriques}.
 * <br>  {@link #hashCode()} and {@link #equals(Object)} overridden
 
 *
 * @author Salim AHMED
 */

@Root
@Namespace(reference = "org.ica.briquePackage.LevelOnePackage")
@Order(elements={"Nom-Package","L1-LevelTwoPackages","L1-CompositionBriques"})
public final class LevelOnePackage extends MyAbstractModelObject implements Serializable{

	
	
	private static final long serialVersionUID = 419L; 

	@Element(name="Nom-Package")
	/**
	 *  packageName : Name of the <code> LevelOnePackage</code> object
	 */
	private String packageName;
	/** This list contains anything from {@link LevelTwoPackage} to {@link CompositionBriques}<br>
	 *  Holds a list of all the {@link CompositionBriques} and {@link LevelTwoPackage} objects contained in the <code> LevelOnePackage</code>.
	*/
	@ElementListUnion({
		 @ElementList(entry ="L1-CompositionBriques", required = false, inline = true, type=CompositionBriques.class),
	     @ElementList(entry="L1-LevelTwoPackages", required = false,inline=true, type=LevelTwoPackage.class)
	    })
	private List<Brique> mainBriqueList;	
	
/**
 * This is the constructor which initialises the package name and its content.
 * @param packageName : Name of the {@code LevelOnePackage}.
 * @param mainBriqueList : List of all {@link LevelTwoPackage} and {@link CompositionBriques} contained in this package.
 */
	public LevelOnePackage(@Element(name="Nom-Package")String packageName,		
									List<Brique> mainBriqueList) {
		this.packageName = packageName;
		this.mainBriqueList = mainBriqueList;	
		
	}
	
	/**Default Constructor, initialises the package. 
	 *
	 * */
	public LevelOnePackage(){
		this.packageName = "";
		this.mainBriqueList = new ArrayList<>();
	}
	
	/**@return mainBriqueList(CompositionBriques and LevelTwoPackage)*/
	public List<Brique> getMainBriqueList() {
		return mainBriqueList;
	}
	/**
	 * @param briqueList : The list of all Briques(CompositionBriques and LevelTwoPackage) to set
	 */
	public void setMainBriqueList(List<Brique> briqueList) {
		List<Brique> oldValue = this.mainBriqueList;
		this.mainBriqueList = briqueList;
		firePropertyChange("mainBriqueList", oldValue, briqueList);
/*		
		List<CompositionBriques> t_compoBriques = this.getListCompositionBriques();
		List<LevelTwoPackage> t_lvl2 = this.getListLevelTwoPackages();
		
		for (Brique brique : oldValue) {
			if (brique instanceof CompositionBriques && !listCompositionBriques.contains(brique)) {
				t_compoBriques.add((CompositionBriques) brique);
			}
			if (brique instanceof LevelTwoPackage && ! listLevelTwoPackages.contains(brique)) {
				t_lvl2.add((LevelTwoPackage) brique);
			}
		}
		this.setListCompositionBriques(t_compoBriques);
		this.setListLevelTwoPackages(t_lvl2);*/
	}
	/**
	 * 
	 * @return packageName
	 */
	public String getPackageName() {
			return packageName;
	}
	/**
	 * Set the name of the package.
	 * @param packageName to set.
	 */
	public void setPackageName(String packageName) {
		String oldValue = this.packageName;
		this.packageName = packageName;
		firePropertyChange("packageName", oldValue, packageName);
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
				+ ((mainBriqueList == null) ? 0 : mainBriqueList.hashCode());
		result = prime * result
				+ ((packageName == null) ? 0 : packageName.hashCode());
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
		if (!(obj instanceof LevelOnePackage)) {
			return false;
		}
		LevelOnePackage other = (LevelOnePackage) obj;
		if (mainBriqueList == null) {
			if (other.mainBriqueList != null) {
				return false;
			}
		} else if (!mainBriqueList.equals(other.mainBriqueList)) {
			return false;
		}
		if (packageName == null) {
			if (other.packageName != null) {
				return false;
			}
		} else if (!packageName.equals(other.packageName)) {
			return false;
		}
		return true;
	}
	
	
}
