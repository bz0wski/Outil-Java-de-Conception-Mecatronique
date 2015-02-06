/**
 * 
 */
package org.ica.utilityClasses;

import java.util.List;

import org.ica.briquePackage.LevelOnePackage;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

/**
 * @author Salim AHMED
 * This Class is intended to be the org.ica.main data holder. 
 * Holds the data for the Briques
 *
 */

@Root(name = "LevelOnePackageStore")
public final class MainDataHouse {
	@ElementList(entry = "LeveOnePackage", inline = true, required=true, empty = true)
	private List<LevelOnePackage> levelOnePackages;
	
	
	public MainDataHouse(@ElementList(entry = "LeveOnePackage", inline = true, required=true, empty = true)
							List<LevelOnePackage> list) {
		this.levelOnePackages = list;
	}

	
	public List<LevelOnePackage> getLevelOnePackages(){
		return levelOnePackages;
	}
}
