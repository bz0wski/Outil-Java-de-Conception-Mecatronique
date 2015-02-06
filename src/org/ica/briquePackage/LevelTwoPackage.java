package org.ica.briquePackage;

import java.io.IOException;
import java.io.NotActiveException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.ElementListUnion;
import org.simpleframework.xml.Namespace;
import org.simpleframework.xml.Order;
import org.simpleframework.xml.Root;

/**
 * Class responsible for holding a mix of a top and mid level objects, i.e  super models {@link CompositionBriques} and base models {@link Brique}.
 * This class creates some abstraction with the {@link LevelOnePackage} class in a hierarchical sense, in that the {@link LevelOnePackage} serves a 
 * container for all things below it.
 * 
 *  <br>  {@link #hashCode()} and {@link #equals(Object)} overridden

 * @author Salim AHMED
 */

@Root
@Namespace(reference = "org.ica.briquePackage.LevelTwoPackage")
@Order(elements={"Nom-Sous-Package","L2-Brique-LevelTwo","L2-Composition-LevelTwo"})
public class LevelTwoPackage extends Brique{

	/**A list of Essentially {@link CompositionBriques} i.e, may contain [0,n] Briques, [0,m] Equation or [0,k] Parametre<br>
	 * Holds a list of all the {@link CompositionBriques} and {@link Brique} objects contained in the <code> LevelTwoPackage</code>.*/
	 @ElementListUnion({
		 @ElementList(entry = "L2-Composition-LevelTwo", required = false, inline = true, type=CompositionBriques.class),
	        @ElementList(entry="L2-Brique-LevelTwo", required = false,inline=true, type=Brique.class)
	    })
	private List<Brique> listBrique;
	@Element(name = "Nom-Sous-Package", required = true)
	/**
	 *  subPackageName : Name of the <code> LevelTwoPackage</code> object
	 */
	private String subPackageName;

	private static final long serialVersionUID = 268L;

	public LevelTwoPackage(String name,List<Brique> list) {
		this.subPackageName = name;
		this.listBrique = list;
	}
	
	public List<Brique> getListBrique() {
		return listBrique;
	}

	public void setListBrique(List<Brique> listBrique) {
		List<Brique> oldValue = this.listBrique;
		this.listBrique = listBrique;
		firePropertyChange("listBrique", oldValue, listBrique);
		
	}

	public String getSubPackageName() {
		return subPackageName;
	}

	
	public void setSubPackageName(String packageName) {
		String oldValue = this.subPackageName;
		this.subPackageName = packageName;
		firePropertyChange("subPackageName", oldValue, packageName);
	}

	private void readObject(ObjectInputStream aInputStream)
			throws ClassNotFoundException, IOException,NotActiveException  {
		 // always perform the default de-serialization first
		aInputStream.defaultReadObject();
	}
	
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
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ ((listBrique == null) ? 0 : listBrique.hashCode());
		result = prime * result
				+ ((subPackageName == null) ? 0 : subPackageName.hashCode());
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
		if (!(obj instanceof LevelTwoPackage)) {
			return false;
		}
		LevelTwoPackage other = (LevelTwoPackage) obj;
		if (listBrique == null) {
			if (other.listBrique != null) {
				return false;
			}
		} else if (!listBrique.equals(other.listBrique)) {
			return false;
		}
		if (subPackageName == null) {
			if (other.subPackageName != null) {
				return false;
			}
		} else if (!subPackageName.equals(other.subPackageName)) {
			return false;
		}
		return true;
	}
	
	
}
