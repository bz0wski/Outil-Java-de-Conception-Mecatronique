package org.ica.uiElements;


import java.io.File;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.ica.briquePackage.Brique;
import org.ica.briquePackage.CompositionBriques;
import org.ica.briquePackage.Equation;
import org.ica.briquePackage.Parametre;
import org.ica.main.MainCanvasObserver;
import org.ica.utilityClasses.AddGlobalEquationToolItem.AddGlobalEquation;
import org.ica.utilityClasses.FactoryTools;
import org.ica.utilityClasses.ImageHolder;
import org.ica.utilityClasses.MatchIdenticalParameters;
import org.ica.utilityClasses.ObjectFactory;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;



public class MainWindowTabs implements MainCanvasObserver{

	private final TabFolder mainTabFolder;
	private final MainApplicationCanvas mainCanvas;
	/**Create the images.*/
	private final ImageHolder imageHolder;
	/**This is the value that's updated by the {@code MainApplicationCanvas}, it indicates whether or not to create a new project.*/
	private int update;
	/**@param isNewProject This variable indicates whether to create a new project or not. */
	private boolean isNewProject = false;
	private static int inner = -1445;
	/**This will hold a reference to the object currently being worked on, only one instance must exist at any point*/
	private CompositionBriques compositionBriques = null;
	private AfficherEtModifierBrique briqueTree;

	/**Keeps the save location of the current project*/
	private String saveLocation = null;
	/**Stores the name of the current project, after it has been saved atleast once on disk*/
	private String projectName = null;
	final private TabItem mainApplicationCanvasItem;
	/**Holds a reference to the only MainApplicationWindowContents instance.*/
	private final MainApplicationWindowContents contents = ObjectFactory.getMainApplicationWindowContents
			(MainApplicationWindowContents.class.getName());
/**
 * Creates the controls for the tabbed display on the {@code MainApplicationCanvas}. 
 * @param parent : parent {@code Composite} on which to draw the controls.
 */
	public MainWindowTabs(Composite parent){
		mainTabFolder = new TabFolder(parent, SWT.NONE);

		mainTabFolder.setLayout(new FormLayout());

		FormData formData = new FormData();
		formData.right = new FormAttachment(100, 0);
		formData.bottom = new FormAttachment(100, 0);
		formData.top = new FormAttachment(0, 0);
		formData.left = new FormAttachment(0, 0);
		mainTabFolder.setLayoutData(formData);


		imageHolder = ImageHolder.getImageHolder();
		TabFolderMouseListener mouseListener = new TabFolderMouseListener(parent.getShell());
		mainTabFolder.addListener(SWT.MouseDown, mouseListener);
		mainTabFolder.addListener(SWT.MouseMove, mouseListener);
		TabFolderKeyListener keyListener = new TabFolderKeyListener();
		mainTabFolder.addKeyListener(keyListener);

		mainApplicationCanvasItem = new TabItem(mainTabFolder, SWT.NONE,0);
		FactoryTools moretools = new FactoryTools(MainApplicationCanvas.class.getName(), mainTabFolder);
		mainCanvas = ObjectFactory.getMainApplicationCanvas(moretools);
		mainCanvas.registerObserver(this);
		mainApplicationCanvasItem.setControl(mainCanvas.getComposite());
		mainApplicationCanvasItem.setText("Nouveau Projet");
		mainApplicationCanvasItem.setImage(imageHolder.getBrique16());
		mainApplicationCanvasItem.setToolTipText("");

		mainApplicationCanvasItem.getControl().addListener(SWT.Paint, new ItemZeroPaintListener());

		final Action enregistrer = new Action() {
			@Override
			public void run() {					
				super.run();				
				if( saveProject())
				 updateUserAfterSave();
			}
		};

		final Action ajouterEquation = new Action() {
			@Override
			public void run() {					
				super.run();				
				AddGlobalEquation.addEquation(parent.getShell());
			}
		};
		mainApplicationCanvasItem.getControl().addMenuDetectListener(listener->{

			MenuManager menuManager = new MenuManager();
			menuManager.setRemoveAllWhenShown(true);
			menuManager.addMenuListener(manager->{
				menuManager.add(enregistrer);
				enregistrer.setText("Enregistrer \tCtrl+S");
				enregistrer.setAccelerator(SWT.CTRL|'s');
				
				menuManager.add(ajouterEquation);
				ajouterEquation.setText("Ajouter Equation \tCtrl+E");
				ajouterEquation.setAccelerator(SWT.CTRL|'e');


			});
			mainApplicationCanvasItem.getControl().setMenu(menuManager.createContextMenu(mainApplicationCanvasItem.getControl()));
		});

		//Adding a dispose listener to warn the user to save an unsaved project on exit
		whenDisposing(mainTabFolder);
	}

	/**
	 * This sub routine closes the tabItem associated with a deleted object on the main canvas.
	 * @param object
	 * 	Holds a reference to the object to be deleted
	 * 
	 * */
	public void closeTabItem(Object object){
		if (object == null) return;
		if (mainTabFolder.isDisposed()) return;
		if(mainTabFolder.getItemCount() == 1) return;

		if(mainTabFolder.getItemCount() == 2){

			if (object.equals(mainTabFolder.getItem(1).getData())) {
				if(mainTabFolder.getItem(1) != null)
					mainTabFolder.getItem(1).dispose();
			}
		}
	}

	/** This sub routine closes any open tabItem other than the primary one 
	 * */
	public void closeExistingTabItem(){
		if (mainTabFolder.isDisposed()) return;
		if(mainTabFolder.getItemCount() == 1) return;
		if(mainTabFolder.getItemCount() == 2){
			if(mainTabFolder.getItem(1) != null)
				mainTabFolder.getItem(1).dispose();
		}
	}
	/**
	 * This method is called whenever the second tab is to be created.
	 * @param object
	 * 	Holds a reference to the object to be displayed in the new tabItem.
	 */
	public void createNewTabItem(Object object){
		if (object == null) {
			return;
		}
		if(mainTabFolder.isDisposed())return;
		if (object instanceof CompositionBriques) {
			CompositionBriques compo = (CompositionBriques)object;

			if(mainTabFolder.getItemCount() == 2){
				if(mainTabFolder.getItem(1) != null)
					mainTabFolder.getItem(1).dispose();
			}
			if (mainTabFolder.getItemCount() == 1) {
				TabItem tempTabItem = new TabItem(mainTabFolder, SWT.NONE,1);
				tempTabItem.setData(compo);
				Composite composite = new Composite(mainTabFolder, SWT.NONE);

				composite.setLayout(new FormLayout());				 
				composite.setLayoutData(composite.getParent().getLayoutData());

				briqueTree = new AfficherEtModifierBrique(composite, compo);				
				tempTabItem.setControl(composite);


				createTreeResizeListener((Composite)tempTabItem.getControl(), briqueTree.getBriqueTreeViewer().getTree());

				tempTabItem.setText(compo.getNomComposition());
				tempTabItem.setImage(imageHolder.getHoverClose());
				tempTabItem.setToolTipText("");


				tempTabItem.addListener(SWT.Dispose, new ItemDisposal());
				//Switch tabs
				if (mainTabFolder.getItemCount() > 1) {
					try {
						MainApplicationWindowContents contents = ObjectFactory.getMainApplicationWindowContents
								(MainApplicationWindowContents.class.getName());
						mainTabFolder.setSelection(1);
						
							//show all the buttons
					if (contents !=  null) {
						contents.setAddEquationVisible();
						contents.setValidateModificationVisible();
						contents.setRemoveEquationVisible();
					}
							
						
						
					} catch (Exception e) {
						if (e instanceof NullPointerException) {
							e.printStackTrace();
						}
						else {
							e.printStackTrace();
						}
					}

				}
			}
		}
		else if (object instanceof Equation) {
			
			Equation equation = (Equation)object;
			if(mainTabFolder.getItemCount() == 2){
				mainTabFolder.getItem(1).dispose();
			}
			if (mainTabFolder.getItemCount() == 1) {
				TabItem tempTabItem = new TabItem(mainTabFolder, SWT.NONE,1);
				tempTabItem.setData(equation);
				Composite composite = new Composite(mainTabFolder, SWT.NONE);

				composite.setLayout(new FormLayout());				 
				composite.setLayoutData(composite.getParent().getLayoutData());
				AfficherEtModifierEquation modifierEquation = new AfficherEtModifierEquation(composite,equation);
				
				//Storing the AfficherEtModifierEquation instance for use later 
				ObjectFactory.putAfficherEtModifierEquation(modifierEquation);
				Table table = modifierEquation.getParameterTable();
				tempTabItem.setControl(composite);
				createTableResizeListener((Composite)tempTabItem.getControl(),table);




				tempTabItem.setText(equation.getContenuEqn());
				tempTabItem.setImage(imageHolder.getHoverClose());
				tempTabItem.setToolTipText("");
				
				tempTabItem.addListener(SWT.Dispose, new ItemDisposal());
				//Switch tabs
				if (mainTabFolder.getItemCount() > 1) {
					try {
						MainApplicationWindowContents contents = ObjectFactory.getMainApplicationWindowContents
								(MainApplicationWindowContents.class.getName());						
						mainTabFolder.setSelection(1);
						
							//show only the validate button
							if (contents != null) 
								contents.setValidateModificationVisible();														
						
					} catch (Exception e) {
						if (e instanceof NullPointerException) {
							e.printStackTrace();
						}
						else {
							e.printStackTrace();
						}
					}

				}
			}
		}
		else {
			Brique brique = (Brique)object;

			if(mainTabFolder.getItemCount() == 2){
				mainTabFolder.getItem(1).dispose();
			}
			if (mainTabFolder.getItemCount() == 1) {
				TabItem tempTabItem = new TabItem(mainTabFolder, SWT.NONE,1);
				tempTabItem.setData(brique);
				Composite composite = new Composite(mainTabFolder, SWT.NONE);

				composite.setLayout(new FormLayout());				 
				composite.setLayoutData(composite.getParent().getLayoutData());
				briqueTree = new AfficherEtModifierBrique(composite, brique);
				tempTabItem.setControl(composite);
				createTreeResizeListener((Composite)tempTabItem.getControl(), briqueTree.getBriqueTreeViewer().getTree());




				tempTabItem.setText(brique.getNomBrique());
				tempTabItem.setImage(imageHolder.getHoverClose());
				tempTabItem.setToolTipText("");
				tempTabItem.addListener(SWT.Dispose, new ItemDisposal());

				//Switch tabs
				if (mainTabFolder.getItemCount() > 1) {
					try {
						MainApplicationWindowContents contents = ObjectFactory.getMainApplicationWindowContents
								(MainApplicationWindowContents.class.getName());						
						mainTabFolder.setSelection(1);
						if (tempTabItem.getData() instanceof Equation) {
							//show only the validate button
							if (contents != null) {
								contents.setValidateModificationVisible();
							}
						}
						else {
							if (contents != null) {
								contents.setAddEquationVisible();
								contents.setValidateModificationVisible();
								contents.setRemoveEquationVisible();
							}
						}
					} catch (Exception e) {
						if (e instanceof NullPointerException) {
							e.printStackTrace();
						}
						else {
							e.printStackTrace();
						}
					}

				}
			}
		}
	}


	/**
	 * This method attaches a resize listener to the tree created in the second tab and 
	 * resizes it to fill the entire parent composite.
	 * @param control
	 * 	The control on which to attach a resize Listener
	 * @param tree
	 * 	The tree object the adjusts it's size to fit the control
	 * <br>
	 * This methods hooks a resize listener to the composite and adjusts the tree to fit the composite 
	 *  
	 * */

	private void createTreeResizeListener(Composite control, Tree tree){
		control.addListener(SWT.Resize, e->{
		
			Rectangle compositeArea = control.getClientArea();

			Point oldSize = tree.getSize();

			if(oldSize.x > compositeArea.width){				
				// Composite is getting smaller so make the columns 
				// smaller first and then resize the table to
				// match the client area width				
				for(TreeColumn col:tree.getColumns())

					if (col.getWidth() < compositeArea.width/tree.getColumnCount())
						col.setWidth(col.getWidth());
					else
						col.setWidth(compositeArea.width/tree.getColumnCount()); 

				tree.setSize(compositeArea.width, compositeArea.height);
			}
			else{
				// Composite is getting bigger so make the tree 
				// bigger first and then make the columns wider
				// to match the client area width
				//	tree.setSize(compositeArea.width-vBar.getSize().x, compositeArea.height);
				tree.setSize(compositeArea.width, compositeArea.height);
				for(TreeColumn col:tree.getColumns()){
					if (col.getWidth() > compositeArea.width/tree.getColumnCount())
						col.setWidth(col.getWidth());
					else
						col.setWidth(compositeArea.width/tree.getColumnCount()); 
				}
			}
		});

	}
	/**
	 * This method attaches a resize listener to the table created in the second tab and 
	 * resizes it to fill the entire parent composite.
	 * @param control
	 * 	The control on which to attach a resize Listener
	 * @param table
	 * 	The table object the adjusts it's size to fit the control
	 * <br>
	 * This methods hooks a resize listener to the composite and adjusts the table to fit the composite 
	 *  
	 * */
	
	private void createTableResizeListener(Composite control, Table table){
		control.addListener(SWT.Resize, e->{
			
			Rectangle compositeArea = control.getClientArea();

			Point oldSize = table.getSize();

			if(oldSize.x > compositeArea.width){				
				// Composite is getting smaller so make the columns 
				// smaller first and then resize the table to
				// match the client area width				
				for(TableColumn col:table.getColumns())

					if (col.getWidth() < compositeArea.width/table.getColumnCount())
						col.setWidth(col.getWidth());
					else
						col.setWidth(compositeArea.width/table.getColumnCount()); 

				table.setSize(compositeArea.width, compositeArea.height);
			}
			else{
				// Composite is getting bigger so make the tree 
				// bigger first and then make the columns wider
				// to match the client area width
				//	tree.setSize(compositeArea.width-vBar.getSize().x, compositeArea.height);
				for(TableColumn col:table.getColumns()){
					if (col.getWidth() > compositeArea.width/table.getColumnCount())
						col.setWidth(col.getWidth());
					else
						col.setWidth(compositeArea.width/table.getColumnCount()); 
				}
			}
		});
	}

	/**This subroutine is called every time there's a change on the main composite
	 * If the canvas is empty, a new project is created, else the information added is 
	 * appended onto the current project data*/
	private void updateCanvas() {
		if (mainTabFolder.isDisposed()) {
			throw new NullPointerException("MainTabFolder isDisposed");	
		}
		//if isNewProject, create a new CompositionBriques Object and keep it alive for the 
		//duration of the current project
		if (isNewProject) {
			//create new project			
			if (compositionBriques == null) {
				compositionBriques = new CompositionBriques();
			}
			else {
				System.out.println("isNewProject and composition is not null");
			}
		//	System.out.println("Created new project\n");
		}

		if(update != inner){
			//then the contents of the main composite have changed
			modified();
			inner = update;
		}
	}
	/**
	 * Apppends a * prefix to the project to indicate there's been a modification and the project 
	 * needs to be saved again.
	 */
	public void modified(){
		//indicate to the user there has been a modification and the project needs to be saved
		if (mainTabFolder.getItemCount() > 0) {
			TabItem item = (mainTabFolder).getItem(0);
			if(item == null) return;
			if (compositionBriques == null) {
				throw new NullPointerException("CompositionBriques object is null.");
			}
			String title = item.getText();
			if (!title.isEmpty()) {
				if (!title.startsWith("*")) {
					title = "*"+title;
					item.setText(title);
				}

			}
		}
	}

	/**creating a new project*/
	public void createNewProject(){
		clear();
	}


	/**Organise the project data to be stored to disk. 
	 * @return true if no errors were encountered, else false.*/
	private boolean organiseProjectforPersistence(){
		if (mainApplicationCanvasItem == null) {
			throw new NullPointerException("Une erreur fatale est survenue, Veuillez relancer l'application");
		}
		if (projectName == null) {
			/*placeholder name for the project as the 
			 * project name will be null at the first call*/			
			return false;
		}
		if (getCompositionBriques() == null) {			
			return false;
		}
		if(getCompositionBriques().getNomComposition().isEmpty())
			getCompositionBriques().setNomComposition(projectName);
		
		pack();
		
		return true;	
	}
	/**Method responsible for refreshing/organising the contents of the composition 
	 * being worked on currently.
	 *  
	 * */
	public void pack(){
		Set<Brique> briqueSet = new HashSet<>();	
		Set<Equation> equationSet = new HashSet<>();	
		
		Control [] controls = ((Composite)mainApplicationCanvasItem.getControl()).getChildren();

		Display.getCurrent().syncExec(()->{	
			//Get all the components on the canvas and add to the project; 
			for (int i = 0; i < controls.length; i++) {
				if (controls[i] == null)return;

				TempCanvas tp = (TempCanvas)controls[i];
				Object object = tp.getDataObject(); 
				if ( object instanceof CompositionBriques) {
					CompositionBriques compo = (CompositionBriques)tp.getDataObject();
					briqueSet.addAll(compo.getBrique());					
				}
				else if(object instanceof Equation){
					Equation equation = (Equation)object;
					equationSet.add(equation);
				}
				else {
					Brique brique = (Brique)object;					
					briqueSet.add(brique);

				}
			}
			getCompositionBriques().setBrique(new ArrayList<>(briqueSet));
			getCompositionBriques().augmentEquationList(new ArrayList<>(equationSet));
			replaceMatchingParameters();
		});
		
	}
	
	/**Replaces all matching parameters in the whole project*/
	private void replaceMatchingParameters() {
			CompositionBriques composition = getCompositionBriques();		
			List<Equation> eqn = composition.getEquation();	
			MatchIdenticalParameters.matchIdenticalBriqueParameters(eqn);
	}
	
	
	/**
	 * Organise the project data to be processed by the Matlab functions(compile)
	 * same principle as organiseProjectforPersistence, with just a few minor 
	 * optimizations.
	 * @return The {@code CompositionBriques} if all the conditions are right. <b><i>See source code</i></b>
	 */
	public CompositionBriques organiseProjectforProcessing(){
		//before compiling the project, it has to be saved, this implies 
		//organising it for persistence
		if(saveProject()){
			return getCompositionBriques();
		}
		return null;
	}
	/**Persist data to disk.
	 * 
	 * @param file : The {@code File} object to write to.
	 */
	private void persistdata(File file){

		Display.getDefault().asyncExec(()->{

			Serializer serializer = new Persister();						
			try	 {																				
				serializer.write(getCompositionBriques(), file);

			} catch (Exception e) {
				if(contents !=  null)
					contents.updateUser("Erreur lors de l'enregistrement", imageHolder.getError16());
				if(e instanceof NullPointerException){

				}

				if(e instanceof ConcurrentModificationException){
					if(contents !=  null)
						contents.updateUser(e.toString(), imageHolder.getError16());
				}
				e.printStackTrace();
			}
		});




	}
	/**This function does what it's name suggests, clears the main composite*/
	private void clear(){
		if (mainTabFolder.getItemCount() > 0) {
			TabItem item = (mainTabFolder).getItem(0);
			if(item == null) return;
			Control [] controls = ((Composite)item.getControl()).getChildren();
			if (controls.length > 0) {

				for (int i = 0; i < controls.length; i++) {

					if (controls[i] != null) {
						controls[i].dispose();
						controls[i] = null;
					}
				}
			}	
		}
		//after clearing out the composite, initialise all values for the new project.
		isNewProject = false;

		inner = -1445;

		setCompositionBriques(null);

		saveLocation = null;

		projectName = null;

		//Set the journal tracker to zero. It will signal the creation of a new project.
		mainCanvas.resetJournal();
		if (mainApplicationCanvasItem != null) {				
			mainApplicationCanvasItem.setText("Nouveau Projet");	
			mainApplicationCanvasItem.setImage(imageHolder.getBrique16());
		}
	}
	/**
	 * Saves the current project to disk.
	 *  @return A boolean value which indicates whether the save was successful, true or not, false.*/
	public boolean saveProject() {
		if (mainCanvas.getComposite().isDisposed()) {
			return false;
		}
		Composite composite = mainCanvas.getComposite();
		if (composite.getChildren().length == 0) {
			return false;
		}
		
		if (saveLocation == null) {
			FileDialog fileDialog = new FileDialog(composite.getShell(), SWT.SAVE);
			fileDialog.setText("Enregistrer Projet Sous");
			fileDialog.setOverwrite(true);
			fileDialog.setFilterExtensions(new String []{"*.xml"});

			saveLocation = fileDialog.open();
			if (saveLocation == null) {
				return false;
			}
			File file = new File(saveLocation);

			if (mainApplicationCanvasItem == null) {
				throw new NullPointerException("Une erreur fatale est survenue, Veuillez relancer l'application");
			}	
			//this the new name of the project
			String name = fileDialog.getFileName().replaceAll("\\.xml", "");
			if (projectName == null) {
				projectName = name;
				mainApplicationCanvasItem.setText(projectName);		
			}
			else if (!projectName.equalsIgnoreCase(name)) {
				projectName = name;
				mainApplicationCanvasItem.setText(projectName);
			}

			if (organiseProjectforPersistence()) {
				persistdata(file);
			}else {
				System.err.println("There was a problem, couldn't save the project");
				return false;
			}

			return true;
		}
		else {
			File file = new File(saveLocation);
			if (organiseProjectforPersistence()) {
				persistdata(file);
			}else {
				System.err.println("There was a problem");
				return false;
			}			
			if (projectName != null && mainApplicationCanvasItem != null) {				
				mainApplicationCanvasItem.setText(projectName);			
			}

			return true;
		}
		//if saveProject returns true, remove the asterix to show that the project has been saved.
	}
	/**A new Equation can be added to the current project if and only if there're already some components
	 * @return A boolean which indicates whether or not an equation will be added. */
	public boolean canAddNewEquation() {
		if (mainCanvas.getComposite().isDisposed()) {
			return false;
		}
		Composite composite = mainCanvas.getComposite();
		if (composite.getChildren().length == 0) {
			return false;
		}
		return true;
	}
	
	/**
	 * Saves the current project to disk with the option of using a different project name. You know the drill.
	 * @return A boolean value which indicates whether the save was successful, true or not, false.
	 */
	public boolean saveProjectAs() {
		if (mainCanvas.getComposite().isDisposed()) {
			return false;
		}
		Composite composite = mainCanvas.getComposite();
		if (composite.getChildren().length == 0) {
			return false;
		}
		//Option #1
		//if second tab is open, perform verification on the contents
		if (saveLocation == null) {
			FileDialog fileDialog = new FileDialog(composite.getShell(), SWT.SAVE);
			fileDialog.setText("Enregistrer Projet Sous");
			fileDialog.setOverwrite(true);
			fileDialog.setFilterExtensions(new String []{"*.xml"});

			saveLocation = fileDialog.open();
			if (saveLocation == null) {
				return false;
			}
			File file = new File(saveLocation);

			if (mainApplicationCanvasItem == null) {
				throw new NullPointerException("Une erreur fatale est survenue, Veuillez relancer l'application");
			}	
			//this the new name of the project
			String name = fileDialog.getFileName().replaceAll("\\.xml", "");
			if (projectName == null) {
				projectName = name;
				mainApplicationCanvasItem.setText(projectName);		
			}
			else if (!projectName.equalsIgnoreCase(name)) {
				projectName = name;
				mainApplicationCanvasItem.setText(projectName);
			}

			if (organiseProjectforPersistence()) {
				persistdata(file);
			}else {
				System.err.println("There was a problem, couldn't save the project");
				return false;
			}


			return true;
		}
		else {
			FileDialog fileDialog = new FileDialog(composite.getShell(), SWT.SAVE);
			fileDialog.setText("Enregistrer Projet Sous");
			fileDialog.setOverwrite(true);
			fileDialog.setFilterExtensions(new String []{"*.xml"});

			saveLocation = fileDialog.open();
			if (saveLocation == null) {
				return false;
			}
			File file = new File(saveLocation);

			if (mainApplicationCanvasItem == null) {
				throw new NullPointerException("Une erreur fatale est survenue, Veuillez relancer l'application");
			}	
			//this the new name of the project
			String name = fileDialog.getFileName().replaceAll("\\.xml", "");
		
			projectName = name;
			mainApplicationCanvasItem.setText(projectName);	
			
			if (organiseProjectforPersistence()) {
				persistdata(file);
			}else {
				System.err.println("There was a problem");
				return false;
			}			
			if (projectName != null && mainApplicationCanvasItem != null) {				
				mainApplicationCanvasItem.setText(projectName);			
			}

			return true;
		}
		//if saveProject returns true, remove the asterix to show that the project has been saved.
	}
	/**
	  If saveProject returns true, this sub routine removes the asterix to show that the project has been saved.*/
	public void updateUserAfterSave() {

		if (mainApplicationCanvasItem == null) {
			throw new NullPointerException("TabItem is null (MainWindowTabs)");
		}
		String title = mainApplicationCanvasItem.getText();
		if (title.startsWith("*")) {
			mainApplicationCanvasItem.setText(title.replaceFirst("\\*", ""));
		}

	}
	/**
	 * This method warns if there're unsaved changes when closing the application.
	 * @param composite : The {@code Composite} whose disposing should trip the warning.
	 */
	private void whenDisposing(Composite composite) {
		composite.addListener(SWT.Dispose, e-> {
			//	if (saveLocation == null || projectName == null || mainApplicationCanvasItem == null)return;	
			String title = mainApplicationCanvasItem.getText();
			if (title.startsWith("*")) {				
				MessageBox msg = new MessageBox(composite.getParent().getShell(), SWT.APPLICATION_MODAL|SWT.YES|SWT.NO|SWT.ICON_QUESTION);

				msg.setText("Avertissement");
				msg.setMessage("Voulez vous fermer le projet sans l'enregistrer");
				long rsp = msg.open();
				if (rsp == SWT.YES) {
					composite.dispose();
					System.exit(0);
				}			
				else {
					saveProject();
				}
			}
		});

	}
	/**
	 * Creates and displays an error dialog
	 * @param parent : The generator of the error
	 * @param errorMessage : The message to display
	 */
	static void showErrorMessage(Composite parent, String errorMessage){
		Display.getCurrent().asyncExec(()->{
			MessageBox dialog =  new MessageBox(parent.getShell(), SWT.ICON_ERROR | SWT.OK);

			dialog.setText("Attention");
			dialog.setMessage(errorMessage);	
			// open dialog and await user selection
			dialog.open(); 
			//	return;
		});

	}

	/**
	 * Recover a  project from a file.
	 * @param data : Contains the neccessary data to successfully initialise a new project.
	 * @return A boolean which indicates whether the project was created or not
	 */
	
	public boolean openNewProject(Map<String,String> data){
		clear();
		closeExistingTabItem();

		if (data.isEmpty()) return false;

		String fileName = ((String)data.get("fileName")).replaceAll("\\.xml", "");
		String savePath = (String)data.get("saveLocation");
		if (fileName.isEmpty() || savePath.isEmpty()) return false;

		File newProjectFile = new File(savePath);
		Serializer serializer = new Persister();		
		try {			
			setCompositionBriques(new CompositionBriques());
			serializer.read(getCompositionBriques(), newProjectFile);

			Display.getDefault().asyncExec(()->{
				List<Equation> equations = new ArrayList<>();
				List<Parametre> parametres = new ArrayList<>();

				CompositionBriques composition = getCompositionBriques();
				for (Brique brique : composition.getBrique()) {
					equations.addAll(brique.getListEquations());
				}
				//Add the globalEquations
				equations.addAll(composition.getGlobalEquations());
				
				//MatchIdenticalParameters after restoring file from disk	
				MatchIdenticalParameters.matchIdenticalBriqueParameters(equations);

				for (Equation equation : equations) {
					parametres.addAll(equation.getListeDeParametresEqn());
				}
				getCompositionBriques().setEquation(equations);
				getCompositionBriques().setParametre(parametres);

			});

		} catch (Exception e) {
			System.err.println("Impossible d'ouvrir, Fichier Corrompu");
			if(!mainTabFolder.isDisposed()){
				showErrorMessage(mainTabFolder, "Impossible d'ouvrir, Fichier Corrompu");
			}		
			e.printStackTrace();
			return false;
		}
		saveLocation = savePath;	projectName = fileName;
		if (mainApplicationCanvasItem != null) {
			mainApplicationCanvasItem.setText(projectName);
		}
		//After the project is successfully opened, place the recovered objects on the canvas
		restoreProject();
		return true;

	}


	/**Recovers the contents of a CompositionBriques object and places them 
	 * randomly on the canvas*/
	public void restoreProject(){
		if (getCompositionBriques() == null || mainApplicationCanvasItem == null) 
			throw new NullPointerException("Couldn't restore project");
	
		
		List<Brique> list = getCompositionBriques().getBrique();
		List<Equation> globalEquations = getCompositionBriques().getGlobalEquations();
		
		Composite composite = (Composite)mainApplicationCanvasItem.getControl();

		Rectangle rectangle = composite.getClientArea();
		//construct a smaller rectangle and place the recovered objects into it
		Rectangle petitRectangle = new Rectangle(rectangle.x, rectangle.y, 
				(int) (rectangle.width/1.5), (int) (rectangle.height/1.5));
		Random random = new Random();
		for (Brique brique : list) {
			int x = 0, y = 0, counter = 0;		
			do {
				if(counter>10) break;
				x =	random.nextInt(petitRectangle.width);
				y = random.nextInt(petitRectangle.height);
				counter++;
			} while (!petitRectangle.contains(x, y));			

			Point point = new Point(x, y);

			new TempCanvas(composite, brique, brique.getNomBrique(), point, false);
		}
		if (!globalEquations.isEmpty()) {
			for (Equation equation : globalEquations) {
				int x = 0, y = 0, counter = 0;		
				do {
					if(counter>10) break;
					x =	random.nextInt(petitRectangle.width);
					y = random.nextInt(petitRectangle.height);
					counter++;
				} while (!petitRectangle.contains(x, y));			

				Point point = new Point(x, y);

				new TempCanvas(composite, equation, equation.getContenuEqn(), point, false);
			}
		}
	}
	/**
	 * 
	 * @return Returns the data contained in the second tab if it is open, else return {@code null}
	 */
	public Object secondTabContent() {
		TabFolder folder = getMainTabFolder();
		if (folder.getItemCount()>1) {
			TabItem item = folder.getItem(1);
			if(item != null)
				return item.getData();
		}
		return null;
	}
	/**
	 * This method updates the name displayed in the {@code TabItem.getText()} after it's content is 
	 * modified.
	 */
	public void refreshSecondTab() {
		TabFolder folder = getMainTabFolder();
		if (folder.getItemCount()>1) {
			TabItem item = folder.getItem(1);
			if(item != null){

				Object object = item.getData();
				if (object instanceof Equation) {
					item.setText(((Equation)object).getContenuEqn());
				}
				if (object instanceof CompositionBriques) {
					item.setText(((CompositionBriques)object).getNomComposition());

			}
			if(object.getClass().equals(Brique.class)) {
				item.setText(((Brique)object).getNomBrique());
			}
				
				
			
			}
			
		}
		
	}
	/**Return the List of all {@code CompositionBriques} names or {@code Brique} dependent on the parameter type.
	 * @param type : Determines the list to be returned.
	 * @return A list containing all the names of the objects on the main canvas depending on the type of the parameter. 
	 */
	public List<String> getCanvasNamesList(Object type) {
		List<String> names = new ArrayList<>();
		
		if (type instanceof CompositionBriques) {
			Control [] controls = ((Composite)mainApplicationCanvasItem.getControl()).getChildren();
		
				for (int i = 0; i < controls.length; i++) {
					if (controls[i] == null)continue;
					TempCanvas tp = (TempCanvas)controls[i];
					Object object = tp.getDataObject(); 
					
					if ( object instanceof CompositionBriques) {
						CompositionBriques compo = (CompositionBriques)tp.getDataObject();
						names.add(compo.getNomComposition());	
					}
				}
		
		}
		else {
			Control [] controls = ((Composite)mainApplicationCanvasItem.getControl()).getChildren();			
			
			for (int i = 0; i < controls.length; i++) {
				if (controls[i] == null)continue;
				TempCanvas tp = (TempCanvas)controls[i];
				Object object = tp.getDataObject(); 
				if ( object instanceof Brique) {
					Brique compo = (Brique)tp.getDataObject();
					names.add(compo.getNomBrique());	
				}
			}
		}			
		return names;
	}
	/**
	 * 
	 * @return  The compositionBriques object.
	 */
	public synchronized CompositionBriques getCompositionBriques(){
		return compositionBriques;
	}
	
	/**@param compositionBriques : the compositionBriques object to set*/
	private synchronized void setCompositionBriques(CompositionBriques compositionBriques){
		this.compositionBriques = compositionBriques;
	}

	/**
	 * 
	 * @return The mainTabFolder
	 */
	public TabFolder getMainTabFolder() {
		return mainTabFolder;
	}
	/**
	 * @return The list of all the parameters in the project.
	 */
	
	public List<Parametre> getParametreList() {
		return getCompositionBriques().getParametre();
	}
	
	/**
	 *Called every time a new object is dropped on the canvas. Refer to {@link  #updateCanvas()} 
	 */
	@Override
	public void update(int compositeChildren, boolean empty) {
		update = compositeChildren;
		isNewProject = empty;
		
		try {
			updateCanvas();
		} catch (NullPointerException e) {
			e.printStackTrace();
		}

	}

	/**
	 * @return the saveLocation
	 */
	public String getSaveLocation() {
		return saveLocation;
	}

	/**
	 * @param saveLocation : the saveLocation to set
	 */
	public void setSaveLocation(String saveLocation) {
		this.saveLocation = saveLocation;
	}
/**
 * This class is responsible for making the buttons invisible when the second tab is closed.
 * It also removes all temporary data stored in {@link ObjectFactory}.
 * @author Salim AHMED
 *
 */
	class ItemDisposal implements Listener{

		@Override
		public void handleEvent(Event event) {	

			//Setting the validate button invisible
			MainApplicationWindowContents contents = ObjectFactory.getMainApplicationWindowContents
					(MainApplicationWindowContents.class.getName());

			if (contents == null) throw new NullPointerException("Erreur, veuillez reessayer, ou relancer l'application.");		
		
			contents.setAddEquationInVisible();
			contents.setRemoveEquationInVisible();
			contents.setValidateModificationInVisible();
			
			Object object = secondTabContent();
			if (object != null) { 
				if ( object instanceof Equation){
					//Removing the AfficherEtModifierEquation from the database
					if(!ObjectFactory.removeAfficherEtModifierEquation())
						System.err.println("Error removing AfficherEtModifierEquation");
					
				}
				else {
					//Removing the AfficherEtModifierBriqueContents from the database
					if(!ObjectFactory.removeAfficherEtModifierBriqueContents()){
						System.err.println("Error removing AfficherEtModifierBriqueContents");
						
					}
				}
			
			}
			


		}

	}
/**
 * Attaches a mousemove and mousedown listener to the {@code TabFolder}.
 * mousedown ensures the tab is validated before it's closed
 * mouseover is functionally aesthetic.
 * @author Salim AHMED
 *
 */
	class TabFolderMouseListener implements Listener{
		private final Shell shell;
		public TabFolderMouseListener(Shell parentShell){
			this.shell = parentShell;
		}
		@Override
		public void handleEvent(Event event) {
			MainApplicationWindowContents contents = ObjectFactory.getMainApplicationWindowContents
					(MainApplicationWindowContents.class.getName());
			Point location = new Point(event.x,event.y);
			TabFolder tab = ((TabFolder)event.widget);
			TabItem item = tab.getItem(location);

			if(item	== null){				
				return;
			}
			Rectangle imageBounds = item.getImage().getBounds();

			switch (event.type) {
			case SWT.MouseDown:				

				//If the first tab has focus, make the button invisible
				if (tab.getItemCount()>0 && item.equals(tab.getItem(0))) {
					if (contents != null){
						contents.setAddEquationInVisible();
						contents.setRemoveEquationInVisible();
						contents.setValidateModificationInVisible();
					}
				
					return;
				}
				//If the second tab has focus, make the buttons visible
				if (tab.getItemCount()> 1 && item.equals(tab.getItem(1))) { 
					if (contents == null) return;
					if (item.getData() instanceof Equation) {				
						contents.setValidateModificationVisible();
					}
					else {
						contents.setAddEquationVisible();
						contents.setRemoveEquationVisible();
						contents.setValidateModificationVisible();
					}
					
				} 


				if(location.x >= item.getBounds().x+ imageBounds.x && location.x <= item.getBounds().x +imageBounds.x + imageBounds.width
						&&	location.y >= item.getBounds().y + imageBounds.y && location.y <= item.getBounds().y + imageBounds.y + imageBounds.height){
					tab.setToolTipText("Fermer");
					/**
					 * <br>This method hooks a dispose listener onto the current tabItem, ensuring that it is validated before it is closed*/
					
					
					if(item.getData() instanceof Equation){
						AfficherEtModifierEquation modifierEquation = ObjectFactory.getAfficherEtModifierEquation();
					
						if (modifierEquation == null)return;
						if(!modifierEquation.isValid())	{
							event.doit = false;
							int style = SWT.APPLICATION_MODAL | SWT.OK |SWT.ICON_INFORMATION;
							MessageBox messageBox = new MessageBox(shell, style);
							messageBox.setText("Information");
							messageBox.setMessage("Valider avant de fermer.");
							messageBox.open();						      
						}
						else {
							item.getControl().dispose();
							item.dispose();
						}
					}
					else {
						AfficherEtModifierBriqueContents afficherEtModifierBriqueContents = ObjectFactory.getAfficherEtModifierBriqueContents();
						if(afficherEtModifierBriqueContents == null)return;
						if(!afficherEtModifierBriqueContents.isValid()){
							event.doit = false;
							int style = SWT.APPLICATION_MODAL | SWT.OK |SWT.ICON_INFORMATION;
							MessageBox messageBox = new MessageBox(shell, style);
							messageBox.setText("Information");
							messageBox.setMessage("Valider avant de fermer.");
							messageBox.open();						      
						}
						else {
							item.getControl().dispose();
							item.dispose();
						}
					}
				}

				break;

			case SWT.MouseMove:								
				tab.setToolTipText("");		
				//If on the first tab, No tooltiptext and set image to hoverClose
				//for the rest of the tabs
				if (tab.getItemCount()>0 && item.equals(tab.getItem(0))) {
					for (int i = 1; i <tab.getItemCount(); i++) {
						tab.getItem(i).setImage(imageHolder.getHoverClose());
					}
					return;
				}
				if(location.x >= item.getBounds().x+ imageBounds.x && location.x <= item.getBounds().x +imageBounds.x + imageBounds.width
						&&	location.y >= item.getBounds().y + imageBounds.y && location.y <= item.getBounds().y + imageBounds.y + imageBounds.height){

					item.setImage(imageHolder.getProperClose());
					tab.setToolTipText("Fermer");
					return;
				}
				item.setImage(imageHolder.getHoverClose());
				break;
			default:
				break;
			}



		}

	}
/**
 * This class is responsible for disappearing the buttons when the tabs are switched with the keyboard
 * arrow keys.
 * @author Salim AHMED
 *
 */
	class TabFolderKeyListener implements KeyListener{

		@Override
		public void keyPressed(KeyEvent e) {

		}

		@Override
		public void keyReleased(KeyEvent e) {
			if (e.keyCode == SWT.ARROW_LEFT || e.keyCode == SWT.ARROW_RIGHT) {
				TabFolder tab = ((TabFolder)e.widget);				

				if (tab.getItemCount() > 1 ) {
					if (tab.getSelectionIndex() == 0) {
						MainApplicationWindowContents contents = ObjectFactory.getMainApplicationWindowContents
								(MainApplicationWindowContents.class.getName());
						if (contents != null){
							
							contents.setAddEquationInVisible();
							contents.setRemoveEquationInVisible();
							contents.setValidateModificationInVisible();
						}
							
					}
					else {
						MainApplicationWindowContents contents = ObjectFactory.getMainApplicationWindowContents
								(MainApplicationWindowContents.class.getName());
						TabItem item = tab.getItem(1);
						if(item == null) return;
						if (contents != null) {
							if (item.getData() instanceof Equation) {				
								contents.setValidateModificationVisible();
							}
							else {
								contents.setAddEquationVisible();
								contents.setRemoveEquationVisible();
								contents.setValidateModificationVisible();
							}
						}
							
					}
				}				
			}

		}

	}
	/**Update the tabitem text when there're no more child components.
	 * i.e remove the asterix when all the objects are deleted from the canvas and the project hasn't been saved yet.*/
	class ItemZeroPaintListener implements Listener{

		@Override
		public void handleEvent(Event e) {

			Composite composite = (Composite)e.widget;

			int childcount = composite.getChildren().length;
			if (childcount == 0) {				
				if(mainApplicationCanvasItem == null) return;

				String title = mainApplicationCanvasItem.getText();
				if (title.startsWith("*") && isNewProject) {
					mainApplicationCanvasItem.setText(title.replaceAll("^\\*", ""));
				}
			}
		}		

	}
}
