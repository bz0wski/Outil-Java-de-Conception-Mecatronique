package org.ica.uiElements;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.ica.briquePackage.Brique;
import org.ica.briquePackage.CompositionBriques;
import org.ica.briquePackage.Equation;
import org.ica.briquePackage.LevelOnePackage;
import org.ica.briquePackage.LevelTwoPackage;
import org.ica.utilityClasses.AddGlobalEquationToolItem;
import org.ica.utilityClasses.CompileToolItem;
import org.ica.utilityClasses.ExcelToolItem;
import org.ica.utilityClasses.FactoryTools;
import org.ica.utilityClasses.ImageHolder;
import org.ica.utilityClasses.NewProjectMenuItem;
import org.ica.utilityClasses.ObjectFactory;
import org.ica.utilityClasses.OpenMenuItem;
import org.ica.utilityClasses.SaveAsMenuItem;
import org.ica.utilityClasses.SaveMenuItem;


/**
 * @author Salim Ahmed
 *
 */

public class MainApplicationWindowContents {
	
	private static Button addbutton;
	private static Button removebutton;
	private SashForm form;
	private MainApplicationWindowList mainList;
	private MainWindowTabs mainWindowTabs ;
	private ImageHolder imageHolder;
	private Button validateBriqueModification;
	private Button addEquation;
	private Button removeEquation;
	private CLabel applicationStatusUpdate;
	private ProgressBar progressBar;
	private ToolItem compileToolItem;
	
	public MainApplicationWindowContents(Shell shell){
		createContents(shell);
		createMenuItems(shell);
	}
	
	private void createContents(Shell shell){
		ToolBar toolBar = new ToolBar(shell, SWT.FLAT | SWT.RIGHT);
		FormData fd_toolBar = new FormData();
		fd_toolBar.top = new FormAttachment(0, 0);
		fd_toolBar.right = new FormAttachment(100, 0);
		fd_toolBar.left = new FormAttachment(0, 0);
		toolBar.setLayoutData(fd_toolBar);
		
		imageHolder = ImageHolder.getImageHolder();
		
		compileToolItem = new ToolItem(toolBar, SWT.PUSH);
		compileToolItem.setToolTipText("Compiler le projet");
		compileToolItem.setImage(imageHolder.getCompiler32());
		compileToolItem.addSelectionListener(new CompileToolItem());
	
		ToolItem excelToolItem = new ToolItem(toolBar,SWT.PUSH);
		excelToolItem.setToolTipText("Exporter fichier Excel");
		excelToolItem.setImage(imageHolder.getExcelIcon32());
		excelToolItem.addSelectionListener(new ExcelToolItem());
		
		ToolItem addGlobalEquationToolItem = new ToolItem(toolBar,SWT.PUSH);
		addGlobalEquationToolItem.setToolTipText("Ajouter Equation");
		addGlobalEquationToolItem.setImage(imageHolder.getFunction32());
		addGlobalEquationToolItem.addSelectionListener(new AddGlobalEquationToolItem(shell));
		
		form = new SashForm(shell,SWT.SMOOTH);
		FormData fd_form = new FormData();
		fd_form.top = new FormAttachment(toolBar, 4);
		fd_form.left = new FormAttachment(0);
		fd_form.right = new FormAttachment(100);
		
		form.setLayoutData(fd_form);
		
		Composite firstChild_List = new Composite(form,SWT.BORDER);
	
		firstChild_List.setLayout(new FormLayout());
		
		FactoryTools tools = new FactoryTools(MainApplicationWindowList.class.getName(), firstChild_List);
		
		
			
		mainList = ObjectFactory.getMainApplicationWindowList(tools);
		
		
		//Resize Listener to set the column width to that of the composite Area.
		firstChild_List.addControlListener(new ControlAdapter() {
			@Override
			public void controlResized(ControlEvent e) {
				super.controlResized(e);
				Rectangle compositeArea = firstChild_List.getClientArea();
				Rectangle treeSize = mainList.getTreeViewer().getTree().getClientArea();
				if(compositeArea.width> treeSize.width){
					mainList.getTreeViewer().getTree().setSize(firstChild_List.getSize());
				}
				else
					mainList.getTreeViewer().getTree().setSize(firstChild_List.getSize());				
			}
		});
	
		
		Composite secondChild_DropZone = new Composite(form,SWT.BORDER);
		secondChild_DropZone.setLayout(new FormLayout());
		
		
		FactoryTools moretools = new FactoryTools(MainWindowTabs.class.getName(), secondChild_DropZone);
		mainWindowTabs = ObjectFactory.getMainWindowTabs(moretools);
		

	
		form.setWeights(new int[] {20, 80});
		
		addbutton = new Button(shell, SWT.NONE);
		addbutton.setToolTipText("Ajouter Nouveau Modèle");
		fd_form.bottom = new FormAttachment(addbutton, -6);
		FormData fd_button = new FormData();
		fd_button.top = new FormAttachment(100, -44);
		fd_button.right = new FormAttachment(0, 41);
		fd_button.left = new FormAttachment(0, 5);
		fd_button.bottom = new FormAttachment(100, -12);
		addbutton.setLayoutData(fd_button);
		addbutton.setImage(imageHolder.getAjouter32());
	
		Listener addModelListener = new Listener(){

			@Override
			public void handleEvent(Event event) {
			ITreeSelection selection = (ITreeSelection) mainList.getTreeViewer().getSelection();
			WritableList list = mainList.getMainList();
			TreeViewer viewer = mainList.getTreeViewer();
			
			if (!selection.isEmpty()) {
			
				Object object = selection.getFirstElement();
				if (object instanceof LevelOnePackage) {
					LevelOnePackage levelOnePackage = (LevelOnePackage)object;
					List<Brique> subpackages = levelOnePackage.getMainBriqueList(); 
					LevelTwoPackage newPack = new LevelTwoPackage("", new ArrayList<>());
					createElement(shell, newPack);
					if (!newPack.getSubPackageName().isEmpty()) {
						subpackages.add(newPack);
						levelOnePackage.setMainBriqueList(subpackages);
						if (viewer != null) {
							viewer.insert(object, newPack, -1);
							viewer.expandToLevel(1);
						}
					}
				}
				if (object instanceof LevelTwoPackage) {
					LevelTwoPackage levelTwoPackage = (LevelTwoPackage)object;
					CompositionBriques compositionBriques = new CompositionBriques("", new ArrayList<>());
					createElement(shell, compositionBriques);
					List<Brique > briques = levelTwoPackage.getListBrique();
					if (!compositionBriques.getNomComposition().isEmpty()) {
						briques.add(compositionBriques);
						levelTwoPackage.setListBrique(briques);
						if (viewer != null) {
							viewer.insert(levelTwoPackage, compositionBriques, -1);
							viewer.expandToLevel(1);
						}
						
					}
				}
				if (object instanceof CompositionBriques) {
					CompositionBriques compositionBriques = (CompositionBriques)object;
					//Call the InitialModelCreation with this here compositionBriques instance.
					FactoryTools tools = new FactoryTools(compositionBriques.getClass().getName(), compositionBriques);
					
					ObjectFactory.getCompositionBriques(tools);

					InitialModelCreation diagCreation = new InitialModelCreation(
							shell, SWT.APPLICATION_MODAL
							| SWT.DIALOG_TRIM);

					try {
						diagCreation.open();
					} catch (Exception e) {	
						e.printStackTrace();
					}
					Brique brique = ObjectFactory.getBrique(Brique.class.getName());
					if (brique != null) {
						viewer.insert(object, brique, -1);
						ObjectFactory.removeBrique(brique);
					}
				
				}
			}
			else {
				LevelOnePackage newPack = new LevelOnePackage("", new ArrayList<>());
				createElement(shell, newPack);
				if (!newPack.getPackageName().isEmpty()) {
					list.add(newPack);
				}
			}
			}};
		addbutton.addListener(SWT.Selection, addModelListener);
		
		
		removebutton = new Button(shell, SWT.NONE);
		removebutton.setToolTipText("Supprimer Modèle");
	
		removebutton.setImage(imageHolder.getSupprimmer32());
		FormData fd_button_1 = new FormData();
		fd_button_1.left = new FormAttachment(addbutton, 8);
		fd_button_1.right = new FormAttachment(addbutton, 44, SWT.RIGHT);
		fd_button_1.top = new FormAttachment(100, -44);
		fd_button_1.bottom = new FormAttachment(100, -12);
		removebutton.setLayoutData(fd_button_1);
		
		//Listener removeModelListener
		removebutton.addListener(SWT.MouseUp, new RemoveListener(shell));
		 
	
		Composite lowerRightComposite = new Composite(shell, SWT.NONE); 
		lowerRightComposite.setLayout(new FormLayout());
		FormData fd_lowerRightComposite = new FormData();
		fd_lowerRightComposite.top = new FormAttachment(form,5, SWT.BOTTOM);
		fd_lowerRightComposite.left = new FormAttachment(removebutton, 30);
		fd_lowerRightComposite.right = new FormAttachment(100,0);
		fd_lowerRightComposite.bottom = new FormAttachment(100,-1);
		
		lowerRightComposite.setLayoutData(fd_lowerRightComposite);
		
		validateBriqueModification = new Button(lowerRightComposite, SWT.PUSH);
		validateBriqueModification.setText("Valider");
		validateBriqueModification.setToolTipText("Valider");
		
		validateBriqueModification.setVisible(false);
		validateBriqueModification.addListener(SWT.Selection,e->{
			if(mainWindowTabs != null){
				Object object = mainWindowTabs.secondTabContent();
				if (object != null) {
					if (object instanceof Equation) {
						AfficherEtModifierEquation modifierEquation = ObjectFactory.getAfficherEtModifierEquation();
						if (modifierEquation != null) 
							modifierEquation.verifyBeforeValidate(shell);
					}
					else {
						AfficherEtModifierBriqueContents contents = ObjectFactory.getAfficherEtModifierBriqueContents();
						if(contents != null)
						contents.verifyBeforeValidate(shell);
					}
				}
			}
			
			
		});
		FormData fd_validateBriqueModification = new FormData();
		fd_validateBriqueModification.top = new FormAttachment(0);
		fd_validateBriqueModification.right = new FormAttachment(100);

		validateBriqueModification.setLayoutData(fd_validateBriqueModification);
		
		removeEquation = new Button(lowerRightComposite, SWT.PUSH);
		removeEquation.setText(" Supprimer ");
		removeEquation.setToolTipText("Supprimmer Equation");
		removeEquation.setVisible(false);
		removeEquation.addListener(SWT.Selection, e->{
			AfficherEtModifierBriqueContents contents = ObjectFactory.getAfficherEtModifierBriqueContents();
			if (contents == null) 
				return;
			try {
				contents.removeEquation();
				if (mainWindowTabs != null) {
					mainWindowTabs.modified();
				}
			} catch (Exception e1) {				
				e1.printStackTrace();
			}
		});
		FormData fd_removeEquation = new FormData();
		fd_removeEquation.top = new FormAttachment(0);
		fd_removeEquation.right = new FormAttachment(validateBriqueModification, -10, SWT.LEFT);
		removeEquation.setLayoutData(fd_removeEquation);
		
		
		addEquation = new Button(lowerRightComposite, SWT.PUSH);
		addEquation.setText(" Ajouter ");
		addEquation.setToolTipText("Ajouter Equation");
		addEquation.setVisible(false);
		addEquation.addListener(SWT.Selection, e->{
			AfficherEtModifierBriqueContents contents = ObjectFactory.getAfficherEtModifierBriqueContents();
			if(contents == null) return;
			try {
				contents.AddEquation();
				if (mainWindowTabs != null) {
					mainWindowTabs.modified();
				}
			} catch (Exception e1) {				
				e1.printStackTrace();
			}
		});
		FormData fd_addEquation = new FormData();
		fd_addEquation.top = new FormAttachment(0);
		fd_addEquation.right = new FormAttachment(removeEquation, -10, SWT.LEFT);
		addEquation.setLayoutData(fd_addEquation);
		
		
		progressBar = new ProgressBar(lowerRightComposite, SWT.SMOOTH );
		progressBar.setMinimum(0);
		progressBar.setMaximum(100);
		progressBar.setSelection(0);
		fd_validateBriqueModification.left = new FormAttachment(progressBar, 0, SWT.LEFT);
		progressBar.setToolTipText("Traitement");
		progressBar.setVisible(false);
		FormData fd_progressBar = new FormData();
		fd_progressBar.left = new FormAttachment(100, -85);
		fd_progressBar.bottom = new FormAttachment(100);
		fd_progressBar.right = new FormAttachment(100,-2);

		progressBar.setLayoutData(fd_progressBar);
		
		applicationStatusUpdate = new CLabel(lowerRightComposite, SWT.LEFT_TO_RIGHT);
		applicationStatusUpdate.setVisible(false);
		
		FontData data = new FontData("Helvetica", 12, SWT.NORMAL|SWT.ITALIC);
		Font font = new Font(Display.getCurrent(), data);
		applicationStatusUpdate.setFont(font);
		applicationStatusUpdate.setImage(Display.getCurrent().getSystemImage(SWT.ICON));
		applicationStatusUpdate.setText("placeholder");
		FormData fd_applicationStatusUpdate = new FormData();
		fd_applicationStatusUpdate.top = new FormAttachment(0);
		fd_applicationStatusUpdate.left = new FormAttachment(secondChild_DropZone, 45, SWT.LEFT);
		applicationStatusUpdate.addListener(SWT.Paint, new LabelResizeListener());
		applicationStatusUpdate.setLayoutData(fd_applicationStatusUpdate); 
			
	}
	/**
	 * Method to create the dialog to insert component names.
	 * @param parent : Composite on which to attach the dialog
	 * @param object : Object to be created.
	 */
	private void createElement(Shell parent, Object object ){

		if (object instanceof LevelOnePackage) {
			NewElementCreation elementCreation = new NewElementCreation(parent,
					SWT.APPLICATION_MODAL|SWT.DIALOG_TRIM,
					(LevelOnePackage)object);
			try {
				elementCreation.open();
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
		if (object instanceof LevelTwoPackage) {
			NewElementCreation elementCreation = new NewElementCreation(parent,
					SWT.APPLICATION_MODAL|SWT.DIALOG_TRIM,
					(LevelTwoPackage)object);
			try {
				elementCreation.open();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (object instanceof CompositionBriques) {
			NewElementCreation elementCreation = new NewElementCreation(parent,
					SWT.APPLICATION_MODAL|SWT.DIALOG_TRIM,
					(CompositionBriques)object);
			try {
				elementCreation.open();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}


	}


	/**
	 * This method creates a label that updates the user. 
	 * @param text : The text to update the user with.
	 * @param image :The image to use.
	 * Message to show to user*/
	public void updateUser(String text, Image image){
		Display.getDefault().asyncExec(()->{
			if(applicationStatusUpdate.isDisposed()) return;
			if(!applicationStatusUpdate.isVisible())
				applicationStatusUpdate.setVisible(true);
			
			if(image != null)
				applicationStatusUpdate.setImage(image);
				
				if(!text.isEmpty()){		
		
				applicationStatusUpdate.setText(text);
				applicationStatusUpdate.setToolTipText(text);	}
		
			
			applicationStatusUpdate.redraw();
			applicationStatusUpdate.update();
			
			Display.getDefault().timerExec(5000, ()->{
				clearUpdate();	
			});
		});
		
	}
	/**Clear the last status update posted*/
	public void clearUpdate(){
		Display.getDefault().asyncExec(()->{
			if(applicationStatusUpdate.isDisposed()) return;
			applicationStatusUpdate.setVisible(false);
		});
		
	}
	
	
	/**Creates the menu items on the main window
	 * @param shell : The shell on to which to attach the menu items.
	 */
	private void createMenuItems(Shell shell){
		MenuManager menuManager = new MenuManager();
		menuManager.setRemoveAllWhenShown(true);
		//shell.setMenu(menuManager.createContextMenu(shell));
	
		Menu menuBar = new Menu(shell, SWT.BAR);
		
		
		MenuItem fileMenuItem = new MenuItem(menuBar, SWT.CASCADE);
		fileMenuItem.setText("Fichier");
		Menu fileMenu = new Menu(shell, SWT.DROP_DOWN);
		
		MenuItem newProjectMenuItem = new MenuItem(fileMenu, SWT.PUSH);
		newProjectMenuItem.setText("&Nouveau Projet \tCtrl+N");
		newProjectMenuItem.setAccelerator(SWT.CTRL|'N');
		
		MenuItem openMenuItem = new MenuItem(fileMenu, SWT.PUSH);
		openMenuItem.setText("&Ouvrir \tCtrl+O");
		openMenuItem.setAccelerator(SWT.CTRL|'O');
		
		MenuItem saveMenuItem = new MenuItem(fileMenu, SWT.PUSH);
		saveMenuItem.setText("&Enregistrer \tCtrl+S");
		saveMenuItem.setAccelerator(SWT.CTRL|'S');
		
		MenuItem saveAsMenuItem = new MenuItem(fileMenu, SWT.PUSH);
		saveAsMenuItem.setText("&Enregistrer Sous \tCtrl+Maj+S");
		saveAsMenuItem.setAccelerator(SWT.SHIFT+SWT.CTRL+'S');
		
		new MenuItem(fileMenu, SWT.SEPARATOR);
		
		MenuItem exitMenuItem = new MenuItem(fileMenu, SWT.PUSH);
		exitMenuItem.setText("&Quitter \tCtrl+Maj+Q");
		exitMenuItem.setAccelerator(SWT.CTRL|SWT.SHIFT|'Q');
		fileMenuItem.setMenu(fileMenu);
		
		MenuItem editMenuItem = new MenuItem(menuBar, SWT.CASCADE);
		editMenuItem.setText("&Edition");
		Menu editMenu = new Menu(shell, SWT.DROP_DOWN);
		MenuItem copyMenuItem = new MenuItem(editMenu, SWT.PUSH);
		copyMenuItem.setText("Copier \tCtrl+C");
		copyMenuItem.setAccelerator(SWT.CTRL|'C');
		MenuItem pasteMenuItem = new MenuItem(editMenu, SWT.PUSH);
		pasteMenuItem.setAccelerator(SWT.CTRL|'V');
		pasteMenuItem.setText("Coller \tCtrl+V");
		editMenuItem.setMenu(editMenu);
		//
	
		openMenuItem.addSelectionListener(new OpenMenuItem(shell));
		saveMenuItem.addSelectionListener(new SaveMenuItem());
		newProjectMenuItem.addSelectionListener(new NewProjectMenuItem());
		
		saveAsMenuItem.addSelectionListener(new SaveAsMenuItem());
		exitMenuItem.addListener(SWT.Selection, l->{
			Display.getDefault().dispose();
		});
		
	//	pasteMenuItem.addListener(eventType, listener);
	//	copyMenuItem.addListener(eventType, listener);

		shell.setMenuBar(menuBar);
	}
	/**
	 * Creates a listener for the remove button, triggering this event 
	 * removes the currently selected item in the {@code MainApplicationWindowList}
	 * @author Salim Ahmed
	 *
	 */
	class RemoveListener implements Listener{
		Shell shell;
		public RemoveListener(Shell _shell) {
			this.shell = _shell;
		}
		@Override
		public void handleEvent(Event event) {
			ITreeSelection treeSelection = (ITreeSelection)mainList.getTreeViewer().getSelection();
			if (!treeSelection.isEmpty()) {
			String option = "";
			Object object = treeSelection.getFirstElement();
			
			if(object instanceof LevelOnePackage){
				option = "package";
			}
			if (object instanceof LevelTwoPackage) {
				option = "sous pacakge";
			}
			if (object instanceof CompositionBriques) {
				option = "modèle";
			}
			MessageBox dialog =  new MessageBox(shell, SWT.ICON_QUESTION | SWT.OK| SWT.CANCEL);			
			dialog.setText("Confirmer");
			dialog.setMessage("Voulez vous supprimez ce "+option+"?");

			// open dialog and await user selection
			long returnCode = (long)dialog.open(); 
			
				//ObjectFactory.getMainApplicationWindowList(new FactoryTools(MainApplicationWindowList.class.getName()));
			if (returnCode == SWT.OK) {
				
				if(object instanceof LevelOnePackage){
					if (mainList.getMainList().contains(object)) {
						mainList.getMainList().remove(object);
					}
				}
				if (object instanceof LevelTwoPackage) {
					if(treeSelection.getPaths()[0].getParentPath().getFirstSegment() instanceof LevelOnePackage){
							LevelOnePackage levelOnePackage = ((LevelOnePackage)treeSelection.getPaths()[0].getParentPath().getFirstSegment());
							List<?> tempMainList = mainList.getMainList();
							if (tempMainList.contains(levelOnePackage)) {
								LevelOnePackage temp_levelOnePackage = (LevelOnePackage)tempMainList.get(tempMainList.indexOf(levelOnePackage));
								if(temp_levelOnePackage.getMainBriqueList().contains(object)){
									List<Brique> holder = temp_levelOnePackage.getMainBriqueList();
									holder.remove(object);
									temp_levelOnePackage.setMainBriqueList(holder);
									mainList.getTreeViewer().remove(object);
								}
								
							
							}
					}
				}
				if (object instanceof CompositionBriques) {
					if(treeSelection.getPaths()[0].getParentPath().getLastSegment() instanceof LevelTwoPackage){
						LevelTwoPackage levelTwoPackage = ((LevelTwoPackage)treeSelection.getPaths()[0].getParentPath().getLastSegment());
						List<Brique> tempCompolist = levelTwoPackage.getListBrique();
						if (tempCompolist.contains(object)) {
							tempCompolist.remove(object);
							levelTwoPackage.setListBrique(tempCompolist);
							mainList.getTreeViewer().remove(object);
						}
					
					}
					if(treeSelection.getPaths()[0].getParentPath().getLastSegment() instanceof LevelOnePackage){
						LevelOnePackage levelOnePackage = (LevelOnePackage)treeSelection.getPaths()[0].getParentPath().getLastSegment();
						List<Brique> listB = levelOnePackage.getMainBriqueList();
						if (listB.contains(object)) {
							listB.remove(object);
							levelOnePackage.setMainBriqueList(listB);
							mainList.getTreeViewer().remove(object);
						}
					}
				}
				
			}
			else if (returnCode == SWT.CANCEL) {
				return;
			}
				
														
			}
			
		}
		
	}
	
	/**
	 * Makes the remove Equation button visible.
	 */
	public void setRemoveEquationVisible() {
		
		if(!removeEquation.isDisposed()){
			removeEquation.setEnabled(true);
			removeEquation.setVisible(true);
		}

	}
	/**
	 * Makes the add Equation button visible.
	 */
	public void setAddEquationVisible() {

		if (!addEquation.isDisposed()) {
			addEquation.setEnabled(true);
			addEquation.setVisible(true);
		}
	}
	/**
	 * Makes the validate button visible.
	 */
	public void setValidateModificationVisible() {

		if(!validateBriqueModification.isDisposed()){
			validateBriqueModification.setEnabled(true);
			validateBriqueModification.setVisible(true);
		}
		
	}
	/**
	 * Makes the validate button invisible.
	 */
	public void setValidateModificationInVisible() {
		if (!validateBriqueModification.isDisposed()){
			validateBriqueModification.setVisible(false);
			validateBriqueModification.setEnabled(false);
		}
	}
	/**
	 * Makes the remove Equation button invisible.
	 */
	public void setAddEquationInVisible() {
	
		if (!addEquation.isDisposed()) {
			addEquation.setEnabled(false);
			addEquation.setVisible(false);
		}
	}
	/**
	 * Makes the remove Equation button invisible.
	 */
	public void setRemoveEquationInVisible() {
			
		if(!removeEquation.isDisposed()){
			removeEquation.setEnabled(false);
			removeEquation.setVisible(false);
		}
		
	}
	/**
	 * Makes the progress visible.
	 */
	public void showProgressBar() {
		if(progressBar.isDisposed())return;
			progressBar.setVisible(true);
		
	}
	/**
	 * Makes the progress Invisible.
	 */
	public void hideProgressBar() {
		if(progressBar.isDisposed())return;
			progressBar.setVisible(false); 
			progressBar.setSelection(0);
	}
	/**
	 * Sets the current progress on the progress bar
	 * @param progress : The amount of progress to indicate on the {@link ProgressBar}.
	 */
	public void setProgress(int progress) {
		if (!progressBar.isDisposed()) {
			Display.getDefault().syncExec(()->{
				progressBar.setSelection(progress);
			});
		}
	}
	/**
	 * Sets the {@code compileToolItem} enabled state to true
	 */
	public void enableCompileToolItem(){
		if(!compileToolItem.isDisposed())
			compileToolItem.setEnabled(true);
	}
	/**
	 * Sets the {@code compileToolItem} enabled state to false.
	 * this is to avoid running simultaneous compiling operations
	 */
	public void disableCompileToolItem(){
		if(!compileToolItem.isDisposed())
			compileToolItem.setEnabled(false);
	}
	
	/**
	 * Attaches a resize listener to the {@code applicationStatusUpdate} label
	 * @author Salim Ahmed
	 *
	 */
	class LabelResizeListener implements Listener{
		@Override
		public void handleEvent(Event event) {
		
			CLabel label = (CLabel)event.widget;
			String text = label.getText();
			
			Image image = label.getImage();
			if (image != null) {
				Rectangle rectangle = image.getBounds();			
				Point point = event.gc.textExtent(text, SWT.DRAW_TRANSPARENT);
				//12 points of offset to accomodate spacing between image and text
				point.x = point.x+rectangle.width+12;
				point.y = Math.max(point.y, rectangle.height);
				label.setSize(point);

				
			}
			else {
				
				Point point = event.gc.textExtent(text, SWT.DRAW_TRANSPARENT);
				label.setSize(point);
				event.gc.drawText(text, point.x, point.y, true);
			}
			
		}
		
	}
	
}
