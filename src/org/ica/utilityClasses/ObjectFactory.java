package org.ica.utilityClasses;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.ica.briquePackage.Brique;
import org.ica.briquePackage.CompositionBriques;
import org.ica.uiElements.AddEquation;
import org.ica.uiElements.AddModel;
import org.ica.uiElements.AddParameter;
import org.ica.uiElements.AddParameterContent;
import org.ica.uiElements.AfficherEtModifierBriqueContents;
import org.ica.uiElements.AfficherEtModifierEquation;
import org.ica.uiElements.MainApplicationCanvas;
import org.ica.uiElements.MainApplicationWindowContents;
import org.ica.uiElements.MainApplicationWindowList;
import org.ica.uiElements.MainWindowTabs;

/**
 * This class is responsible for instantiating and holding references to most of the objects manipulated
 * by the program.
 * @author Salim AHMED
 *
 */
public final class ObjectFactory {
	/**
	 * @param
	 * A {@link Map}   which contains all the objects in the program which will be reused. this prevents creating mutiple instances of the same object*/
	private static Map<FactoryTools, Object> objectFactory = new HashMap<>();
	
	/**@param
	 * A {@link Map }  which contains all the objects in the program which will be reused. this prevents creating mutiple instances of the same object*/
	private static Map<String, Object> backupObjectFactory = new HashMap<>();
	
	/**
	 * @param
	 * A {@link Map}  which contains the validation status of all the items in the 
	 * Model creation process.*/
	private static Map<String, Boolean> register = new HashMap<>();
	
	/**@param
	 * A {@link List} which contains all the currently open CompositionBriques objects.
	 *  This to prevent opening the same window multiple times*/
	private static Set<CompositionBriques> openCompositionBriques = new HashSet<>();

	/**
	 * Instantiates and returns a singleton {@code MainApplicationWindowContents} instance.
	 * @param tools : The <code>FactoryTools</code> object to initialise the <code>MainApplicationWindowContents</code> object with.
	 * @return The {@code MainApplicationWindowContents} instance.
	 */
	public static MainApplicationWindowContents getMainApplicationWindowContents(FactoryTools tools) {
		MainApplicationWindowContents mainAppWindow = null;
		if (tools.get() instanceof Shell) {
			mainAppWindow = (MainApplicationWindowContents)objectFactory.get(tools);
			if (mainAppWindow == null) {
				try {
					mainAppWindow = new MainApplicationWindowContents((Shell)tools.get());
				} catch (Exception e) {
					e.printStackTrace();
				}
				objectFactory.put(tools, mainAppWindow);
				backupObjectFactory.put(mainAppWindow.getClass().getName(), mainAppWindow);
			}
		}	
		return mainAppWindow;
		
	}
	/**
	 * This method returns the singleton instance of {@code MainApplicationWindowContents} object.
	 * @param className : The class name of the singleton {@code MainApplicationWindowContents} instance.
	 * @return The {@code MainApplicationWindowContents} instance.
	 */
	public static MainApplicationWindowContents getMainApplicationWindowContents(String className) {
		MainApplicationWindowContents mainAppWindow = null;
			if(backupObjectFactory.containsKey(className)){
				try {
					mainAppWindow = (MainApplicationWindowContents)backupObjectFactory.get(className);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			return mainAppWindow;
	}
	/**
	 * Instantiates and returns a singleton {@code AddEquation} instance.
	 * @param tools : The <code>FactoryTools</code> object to initialise the <code>AddEquation</code> object with.
	 * @return The {@code AddEquation} instance.
	 */
	public static AddEquation getEquation(FactoryTools tools) {
		AddEquation equation = null;
		 if (tools.get() instanceof Composite) {
				equation = (AddEquation) objectFactory.get(tools);
				if (equation == null) {
					try {
						equation = new AddEquation(((Composite)tools.get()));
					} catch (Exception e) {
						
						e.printStackTrace();
					}
					objectFactory.put(tools, equation);
					backupObjectFactory.put(equation.getClass().toString(), equation);
				}
			}		
		
		return equation;
	}
	/**
	 * This method returns the singleton instance of {@code AddEquation} object.
	 * @param className : The class name of the singleton {@code AddEquation} instance.
	 * @return The {@code AddEquation} instance.
	 */
	public static AddEquation getEquation(String className) {
		AddEquation equation = null;
			if(backupObjectFactory.containsKey(className)){
				try {
					 equation = (AddEquation)backupObjectFactory.get(className);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			return equation;
	}
	
	/**
	 * Instantiates and returns a singleton {@code AddParameter} instance.
	 * @param tools : The <code>FactoryTools</code> object to initialise the <code>AddParameter</code> object with.
	 * @return The {@code AddParameter} instance.
	 */
	public static AddParameter getParameter(FactoryTools tools) {
		AddParameter parameter = null;
	
		 if (tools.get() instanceof Composite) {
			parameter = (AddParameter) objectFactory.get(tools);
			if (parameter == null) {
				try {
					parameter = new AddParameter(((Composite)tools.get()));
					
				} catch (Exception e) {
					
					e.printStackTrace();
				}
				objectFactory.put(tools, parameter);
				backupObjectFactory.put(parameter.getClass().getName(),parameter);
			}
		}
		else if(tools.getClassName() instanceof String){
			if(backupObjectFactory.containsKey(tools.getClassName())){		
				try {
					parameter = (AddParameter)backupObjectFactory.get(tools.getClassName());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			}
		 return parameter;
	}
	/**
	 * This method returns the singleton instance of {@code AddParameter} object.
	 * @param className : The class name of the singleton {@code AddParameter} instance.
	 * @return The {@code AddParameter} instance.
	 */
	public static AddParameter getParameter(String className){
		AddParameter parameter = null;	
		 if(backupObjectFactory.containsKey(className)){			
				try {
					parameter = (AddParameter)backupObjectFactory.get(className);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}	
		return parameter;
	}
	/**
	 * This getter is slightly different in the sense that I put an already existent 
	 * CompositionBrique instance into the database(if {@code tools.get()} is an instance of CompositionBrique).
	 * @param tools : The FactoryTools object
	 * @return The {@code CompositionBriques}.
	 * */
	public static CompositionBriques getCompositionBriques(FactoryTools tools) {
		CompositionBriques compositionBriques = null;
		if (tools.get() instanceof CompositionBriques) {
			compositionBriques = (CompositionBriques) tools.get();
			objectFactory.put(tools, compositionBriques);
			backupObjectFactory.put(compositionBriques.getClass().getName(), compositionBriques);
		}
		return compositionBriques;
	}
	/**
	 * Only one instance of CompositionBriques is ever stored in each of the maps, this method recovers
	 * the {@code CompositionBriques} whose name is given as parameter.
	 * @param name : The name of the {@code CompositionBriques} to recover.
	 * @return The {@code CompositionBriques}.
	 */
	public static CompositionBriques getCompositionBriques(String name) {
		CompositionBriques compositionBriques = null;
		if (backupObjectFactory.containsKey(name)) {
			compositionBriques = (CompositionBriques)backupObjectFactory.get(name);
		}
		return compositionBriques;
	}
	/**
	 * Removes the compositionBriques object stored in the database
	 * @param compositionBriques : The {@code CompositionBriques} to remove.
	 */
	public static void removeCompositionBriques(CompositionBriques compositionBriques) {
		if (backupObjectFactory.containsValue(compositionBriques)) {
			backupObjectFactory.remove(compositionBriques.getClass().getName());
		}
		if (objectFactory.containsValue(compositionBriques)) {
			objectFactory.remove(compositionBriques.getClass().getName());
		}
		
	}
	/**
	 * Stores a <code>Brique</code> for re use later. 
	 * @param tools : the <code>FactoryTools</code> object to initialise the <code>Brique</code> object with.
	 * @return The {@code Brique}.
	 */
	public static Brique getBrique(FactoryTools tools) {
		Brique brique = null;
		if (tools.getClassName() instanceof String) {
			brique = (Brique)objectFactory.get(tools);
			if (brique == null) {
				try {
					brique = new Brique(tools.getClassName());
				} catch (Exception e) {
					e.printStackTrace();
				}
				objectFactory.put(tools, brique);
				backupObjectFactory.put(brique.getClass().getName(), brique);
			}
		}
		return brique;
	}
	/**
	 *  This method recovers a <code>Brique</code> object. 
	 * @param name : The class name of the <code>Brique</code> to recover.
	 * @return The {@code Brique}.
	 */
	public static Brique getBrique(String name) {
		Brique brique = null;
		
		if (backupObjectFactory.containsKey(name)) {
			brique = (Brique)backupObjectFactory.get(name);
		}
		return brique;
	}
	/**
	 * This function will perform cleanup and remove the brique object from the list after 
	 * it is no longer needed. Only one Brique object should be in the list. 
	 *
	 * @param brique :  The {@code Brique} to remove.
	 */
	public static void removeBrique(Brique brique) {
		if (backupObjectFactory.containsValue(brique)) {
			backupObjectFactory.remove(brique.getClass().getName());
		}
	}
	/**
	 * Creates a singleton instance of <code>MainApplicationWindowList</code>
	 * @param tools : the <code>FactoryTools</code> object to initialise the <code>MainApplicationWindowList</code> object with.
	 * @return the singleton instance of {@code MainApplicationWindowList}.
	 */
	public static MainApplicationWindowList getMainApplicationWindowList(FactoryTools tools) {
		MainApplicationWindowList mainApplicationWindowList = null;
		if (tools.get() instanceof Composite) {
			mainApplicationWindowList = (MainApplicationWindowList) objectFactory.get(tools);
			if (mainApplicationWindowList == null) {
				try {
					mainApplicationWindowList = new MainApplicationWindowList(((Composite)tools.get()));	
				} catch (Exception e) {
					
					e.printStackTrace();
				}
				objectFactory.put(tools, mainApplicationWindowList);
				backupObjectFactory.put(mainApplicationWindowList.getClass().getName(),mainApplicationWindowList);
			}
		}
		else if(tools.getClassName() instanceof String){
			if(backupObjectFactory.containsKey(tools.getClassName())){		
				try {
					mainApplicationWindowList = (MainApplicationWindowList)backupObjectFactory.get(tools.getClassName());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			}
		 return mainApplicationWindowList;		
	}
	
	/**
	 * Creates a singleton instance of <code>MainApplicationCanvas</code>
	 * @param tools : the <code>FactoryTools</code> object to initialise the <code>MainApplicationCanvas</code> object with. 
	 * @return the singleton instance of {@code MainApplicationCanvas}.
	 */
	public static MainApplicationCanvas getMainApplicationCanvas(FactoryTools tools) {
		MainApplicationCanvas mainApplicationCanvas = null;
		if (tools.get() instanceof Composite) {
			mainApplicationCanvas = (MainApplicationCanvas) objectFactory.get(tools);
			if (mainApplicationCanvas == null) {
				try {
					mainApplicationCanvas = new MainApplicationCanvas(((Composite)tools.get()));	
				} catch (Exception e) {
					
					e.printStackTrace();
				}
				objectFactory.put(tools, mainApplicationCanvas);
				backupObjectFactory.put(mainApplicationCanvas.getClass().getName(),mainApplicationCanvas);
			}
		}
		else if(tools.getClassName() instanceof String){
			if(backupObjectFactory.containsKey(tools.getClassName())){		
				try {
					mainApplicationCanvas = (MainApplicationCanvas)backupObjectFactory.get(tools.getClassName());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			}
		 return mainApplicationCanvas;		
	}
	

	/**
	 * Returns the only instance of <code>MainWindowTabs</code> Class, if no instance exists, a new one is created.
	 * @param tools : the <code>FactoryTools</code> object to initialise the <code>MainWindowTabs</code> object with. 
	 * @return the singleton instance of {@code MainWindowTabs}.
	 */
	public static MainWindowTabs getMainWindowTabs(FactoryTools tools) {
		MainWindowTabs mainWindowTabs = null;
		if (tools.get() instanceof Composite) {
			mainWindowTabs = (MainWindowTabs) objectFactory.get(tools);
			if (mainWindowTabs == null) {
				try {
					mainWindowTabs = new MainWindowTabs(((Composite)tools.get()));	
				} catch (Exception e) {
					
					e.printStackTrace();
				}
				
				objectFactory.put(tools, mainWindowTabs);
				backupObjectFactory.put(mainWindowTabs.getClass().getName(),mainWindowTabs);
			}
		}
		else if(tools.getClassName() instanceof String){
			if(backupObjectFactory.containsKey(tools.getClassName())){		
				try {
					mainWindowTabs = (MainWindowTabs)backupObjectFactory.get(tools.getClassName());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		
			}
		
		 return mainWindowTabs;		
	}
	
	/**Store <code>AfficherEtModifierBriqueContents</code> object for use later, replace if it's already in the database
	 * @param contents : the <code>AfficherEtModifierBriqueContents</code> object to store.
	 * */
	public static void putAfficherEtModifierBriqueContents(AfficherEtModifierBriqueContents contents){
		if (backupObjectFactory.containsKey(AfficherEtModifierBriqueContents.class.getName()))
			backupObjectFactory.replace(contents.getClass().getName(), contents);
		else
			backupObjectFactory.put(contents.getClass().getName(), contents);
	}
	
	/**
	 * Recover the stored <code>AfficherEtModifierBriqueContents</code> object for use
	 * @return the {@code AfficherEtModifierBriqueContents} object
	 */
	public static AfficherEtModifierBriqueContents getAfficherEtModifierBriqueContents(){
		AfficherEtModifierBriqueContents contents = null;

		if(backupObjectFactory.containsKey(AfficherEtModifierBriqueContents.class.getName()))
		contents = (AfficherEtModifierBriqueContents) backupObjectFactory.get(AfficherEtModifierBriqueContents.class.getName());
		return contents;
	}
	/**
	 * Remove the stored <code>AfficherEtModifierBriqueContents</code> object from database after use.
	 * @return boolean to indicate whether the {@code AfficherEtModifierBriqueContents} was removed, true or not, false. 
	 */
	public static boolean removeAfficherEtModifierBriqueContents() {
		if (backupObjectFactory.containsKey(AfficherEtModifierBriqueContents.class.getName())) {
			return backupObjectFactory.remove(AfficherEtModifierBriqueContents.class.getName()) != null;		
		}
		return false;
	}
	
	
	
	/**
	 * Store <code>AfficherEtModifierEquation</code> object for use later, replace if it's already in the database
	 * @param contents : the <code>AfficherEtModifierEquation</code> object to store
	 */
	public static void putAfficherEtModifierEquation(AfficherEtModifierEquation contents){
		if (backupObjectFactory.containsKey(AfficherEtModifierEquation.class.getName()))
			backupObjectFactory.replace(contents.getClass().getName(), contents);
		else
			backupObjectFactory.put(contents.getClass().getName(), contents);
	}
	
	/**Recover the stored <code>AfficherEtModifierEquation</code> object for use
	 * @return  the stored <code>AfficherEtModifierEquation</code> instance */
	public static AfficherEtModifierEquation getAfficherEtModifierEquation(){
		AfficherEtModifierEquation contents = null;

		if(backupObjectFactory.containsKey(AfficherEtModifierEquation.class.getName()))
		contents = (AfficherEtModifierEquation) backupObjectFactory.get(AfficherEtModifierEquation.class.getName());
		return contents;
	}
	/**
	 * Remove the stored <code>AfficherEtModifierEquation</code> object from database after use.
	 * @return A boolean value to indicate whether the <code>AfficherEtModifierEquation</code> was removed, true or not false.
	 */
	public static boolean removeAfficherEtModifierEquation() {
		if (backupObjectFactory.containsKey(AfficherEtModifierEquation.class.getName())) {
			return backupObjectFactory.remove(AfficherEtModifierEquation.class.getName()) != null;		
		}
		return false;
	}
	
	
	
	/**Store object for use later, replace if it's already in the database
	 *@param contents : */
	public static void putAddParameterContent(AddParameterContent contents){
		if (backupObjectFactory.containsKey(AddParameterContent.class.getName()))
			backupObjectFactory.replace(contents.getClass().getName(), contents);
		else
			backupObjectFactory.put(contents.getClass().getName(), contents);
	}
	
	/**
	 * Recover stored object for use
	 * @return the <code>AddParameterContent</code> object 
	 */
	public static AddParameterContent getAddParameterContent(){
		AddParameterContent contents = null;

		if(backupObjectFactory.containsKey(AfficherEtModifierBriqueContents.class.getName()))
		contents = (AddParameterContent) backupObjectFactory.get(AddParameterContent.class.getName());
		return contents;
	}
	/**Remove stored object from database after use.
	 @return A boolean value to indicate whether the <code>AddParameterContent</code> object was removed, true or not false.
*/
	public static boolean removeAddParameterContent() {
		if (backupObjectFactory.containsKey(AddParameterContent.class.getName())) {
			return backupObjectFactory.remove(AddParameterContent.class.getName()) != null;		
		}
		return false;
	}

	
	/**Returns the only instance of {@code AddModel} Class, if no instance exists, a new one is created.
	 * 
	 * @param tools :  the <code>FactoryTools</code> object to initialise the <code>AddModel</code> object with. 
	 * @return the newly created <code>AddModel</code> object .
	 */
	public static AddModel getAddModel(FactoryTools tools) {
		AddModel addModel = null;
		if (tools.get() instanceof Shell) {
			addModel = (AddModel) objectFactory.get(tools);
			if (addModel == null) {
				addModel = new AddModel((Shell)tools.get(),SWT.APPLICATION_MODAL
				        | SWT.DIALOG_TRIM);
			}
			objectFactory.put(tools, addModel);
			backupObjectFactory.put(addModel.getClass().getName(), addModel);
		}
		else if (tools.getClassName() instanceof String) {
			if (backupObjectFactory.containsKey(tools.getClassName())) {
				addModel = (AddModel) backupObjectFactory.get(tools.getClassName());
			}
		}
		return addModel;
	}
	
	/**Returns a {@link Map}  which contains the validation status of all the items in the 
	 * Model creation process.
	 * @return register: A HashMap containing validation status. 
	 * */
	public static Map<String, Boolean> validationRegistry(){
		return register;
	}
	/**Cleans out all the values in the register at the end of the model creation process
	 * when the model has been successfully validated.
	 * */
	public static void clearRegistry(){
		 register.clear();
	}

	/**@param composition : the {@link CompositionBriques} object whose existence to test in the list.
	 * @return a boolean which indicates whether the {@link CompositionBriques} object was found.
	 */
	public static boolean doesContain(CompositionBriques composition){
		return openCompositionBriques.contains(composition);
	}
	/**
	 * Adds a {@link CompositionBriques} to the list when a new window is created. 
	 * @param composition : the {@link CompositionBriques} object to remove from the list.
	 * @return a boolean which indicates whether the {@link CompositionBriques} object was successfully added;
	 */
	public static boolean addToOpenCompositionBriques(CompositionBriques composition) {
		return openCompositionBriques.add(composition);
	}

	/**Removes a {@link CompositionBriques} object from the list when it's window is disposed.
	 * @param composition the {@link CompositionBriques} object to remove from the list.
	 */
	public static void removeFromOpenCompositionBriques(
			CompositionBriques composition) {
		if (openCompositionBriques.contains(composition)) {
			openCompositionBriques.remove(composition);
		}
		
	}
}

