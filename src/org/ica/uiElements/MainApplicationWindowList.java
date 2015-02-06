package org.ica.uiElements;


import java.util.ArrayList;
import java.util.List;
import java.util.Set;



import org.eclipse.core.databinding.beans.BeansObservables;
import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.core.databinding.observable.map.IMapChangeListener;
import org.eclipse.core.databinding.observable.map.IObservableMap;
import org.eclipse.core.databinding.observable.map.MapChangeEvent;
import org.eclipse.core.databinding.observable.masterdetail.IObservableFactory;
import org.eclipse.core.databinding.observable.set.IObservableSet;
import org.eclipse.core.internal.databinding.observable.EmptyObservableList;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.databinding.viewers.ObservableListTreeContentProvider;
import org.eclipse.jface.databinding.viewers.TreeStructureAdvisor;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.ica.briquePackage.Brique;
import org.ica.briquePackage.CompositionBriques;
import org.ica.briquePackage.LevelOnePackage;
import org.ica.briquePackage.LevelTwoPackage;
import org.ica.exceptions.MainApplicationException;
import org.ica.utilityClasses.FactoryTools;
import org.ica.utilityClasses.ImageHolder;
import org.ica.utilityClasses.ObjectFactory;
import org.ica.utilityClasses.PersistData;



/**
 * Creates the main list on the main application window that displays the content of the database.
 * @author Salim Ahmed
 */
public class MainApplicationWindowList implements DragSourceListener{

	private final Tree mainTree;
	private final TreeViewer mainTreeViewer;

	private List<LevelOnePackage> mainData;
	private WritableList mainList;
	private ImageHolder imageHolder; 
	private static final Shell shellForShellViewingContents = new Shell(Display.getDefault());
	
/**
 * Main constructor for initialising the controls for displaying the database content.
 * @param parent : The {@code Composite} on which to draw the components.
 * @throws MainApplicationException {@code MainApplicationException}
 */
	public MainApplicationWindowList(Composite parent) throws MainApplicationException {

		mainTreeViewer = new TreeViewer(parent, SWT.FULL_SELECTION|SWT.BORDER|SWT.SINGLE);
		mainTree = mainTreeViewer.getTree();

		mainTree.setLinesVisible(false);
		mainData = new ArrayList<>();

		//Defining and attaching the layout Information for the Main tree 
		FormData fd_list = new FormData();
		fd_list.left = new FormAttachment(0,0);
		fd_list.bottom = new FormAttachment(100, 0);
		fd_list.right = new FormAttachment(100, 0);
		fd_list.top = new FormAttachment(0);
		mainTree.setLayoutData(fd_list);

		imageHolder = ImageHolder.getImageHolder();

		////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////




		//Setting the content provider for the table, to contain all of it's data
		ObservableListTreeContentProvider contentProvider = new ObservableListTreeContentProvider(new MyTreeListFactory(),new MyTreeStructureAdvisor());
		mainTreeViewer.setContentProvider(contentProvider);	

		//Setting a sorter on the tree to sort the contents alphabetically
		//mainTreeViewer.setSorter(new MySorter());


		IObservableSet set = contentProvider.getKnownElements();

		IObservableMap[] mainMap = BeansObservables.observeMaps(set, new String[]{"nomBrique","listParametres","listEquations"
				,"briqueId","uniqueBriqueId","infoBrique","nomComposition","parametre","equation"
				,"brique","uniqueCompId","packageName","briqueList","nomBrique","subPackageName","listBrique"});

		//Setting a custom LabelProvider for the tree
		mainTreeViewer.setLabelProvider(new MyCustomLabelProvider(mainMap));


		//Loading library from disk/
		Display.getDefault().asyncExec(new Runnable() {				
			@Override
			public void run() {
				//Reading data saved to disk from previous launch of the program
				//Separated it from the main UI Thread because it may take a while 
				//to load and I don't want the app to hang, waiting for the data.
				mainData = PersistData.loadFromDataBase();
				mainList = new WritableList(mainData,LevelOnePackage.class);

				//Setting Input, Must be called after setting content provider
				mainTreeViewer.setInput(mainList);

			}
		});


		int operations = DND.DROP_COPY|DND.DROP_DEFAULT;
		Transfer[] transferTypes = new Transfer[]{TextTransfer.getInstance(), 
				CustomTransferType_BRIQUE.getInstance(), CustomTransferType_COMPOSITION.getInstance()};
		mainTreeViewer.addDragSupport(operations, transferTypes, this);

		//Adding a dispose listener to be informed when the tree is disposed.
		mainTree.addDisposeListener(listener->{			
			//save the application state to a file on Disk.
			PersistData.saveMainList(mainData);	
			shellForShellViewingContents.dispose();

		});


		final Action addPackage = new Action(){			
			@Override	
			public void run() {
				ITreeSelection selection = (ITreeSelection)mainTreeViewer.getSelection();

				if (!selection.isEmpty()) {
					Object object = selection.getFirstElement();

					if (object instanceof LevelOnePackage) {
						LevelOnePackage newPack = new LevelOnePackage("", new ArrayList<>());
						createElement(parent, newPack);
						if (!newPack.getPackageName().isEmpty()) {
							mainList.add(newPack);
						}
					}
					if (object instanceof LevelTwoPackage) {
						LevelOnePackage newPack = new LevelOnePackage("", new ArrayList<>());
						createElement(parent, newPack);
						if (!newPack.getPackageName().isEmpty()) {
							mainList.add(newPack);
						}
					}
					if (object instanceof CompositionBriques) {
						LevelOnePackage newPack = new LevelOnePackage("", new ArrayList<>());
						createElement(parent, newPack);
						if (!newPack.getPackageName().isEmpty()) {
							mainList.add(newPack);
						}
					}
				}else{

					LevelOnePackage newPack = new LevelOnePackage("", new ArrayList<>());
					createElement(parent, newPack);
					if (!newPack.getPackageName().isEmpty()) {
						mainList.add(newPack);
					}
				}
			}

		};

		final Action addSubPackage = new Action() {
			@Override
			public void run() {
				super.run();

				ITreeSelection treeSelection = (ITreeSelection)mainTreeViewer.getSelection();
				if (!treeSelection.isEmpty()) {
					Object object = treeSelection.getFirstElement(); 				
					if (object instanceof LevelOnePackage) {

						if(mainList.contains(object)){

							LevelOnePackage temPackage = mainData.get(mainData.indexOf(object));
							LevelTwoPackage levelTwoPackage = new LevelTwoPackage("", new ArrayList<>());

							createElement(parent, levelTwoPackage);
							if (!levelTwoPackage.getSubPackageName().isEmpty()) {
								List<Brique> holder = temPackage.getMainBriqueList();
								holder.add(levelTwoPackage);
								temPackage.setMainBriqueList(holder);

								mainTreeViewer.insert(object,levelTwoPackage, -1);
								mainTreeViewer.expandToLevel(object,1);
							}


						}
						else {
							System.err.println("Error");
						}					
					}
					if (object instanceof LevelTwoPackage) {
						if (treeSelection.getPaths()[0].getFirstSegment() instanceof LevelOnePackage) {
							LevelOnePackage levelOnePackage = ((LevelOnePackage)treeSelection.getPaths()[0].getFirstSegment());
							if (mainList.contains(levelOnePackage)) {
								LevelOnePackage temp_levelOnePackage = (LevelOnePackage) mainList.get(mainList.indexOf(levelOnePackage));
								LevelTwoPackage levelTwoPackage = new LevelTwoPackage("", new ArrayList<>());
								createElement(parent, levelTwoPackage);
								if (!levelTwoPackage.getSubPackageName().isEmpty()) {
									List<Brique> holder = temp_levelOnePackage.getMainBriqueList();
									holder.add(levelTwoPackage);
									temp_levelOnePackage.setMainBriqueList(holder);
									mainTreeViewer.insert(levelOnePackage, levelTwoPackage, -1);
								}

							}
						}
					}
					if (object instanceof CompositionBriques) {
						if (treeSelection.getPaths()[0].getFirstSegment() instanceof LevelOnePackage) {
							LevelOnePackage levelOnePackage = ((LevelOnePackage)treeSelection.getPaths()[0].getFirstSegment());
							if (mainList.contains(levelOnePackage)) {
								LevelOnePackage temp_levelOnePackage = (LevelOnePackage) mainList.get(mainList.indexOf(levelOnePackage));
								LevelTwoPackage levelTwoPackage = new LevelTwoPackage("", new ArrayList<>());

								createElement(parent, levelTwoPackage);
								if (!levelTwoPackage.getSubPackageName().isEmpty()) {
									List<Brique> holder = temp_levelOnePackage.getMainBriqueList();
									holder.add(levelTwoPackage);
									temp_levelOnePackage.setMainBriqueList(holder);
									mainTreeViewer.insert(levelOnePackage, levelTwoPackage, -1);
								}

							}
						}
					}
				}
			}
		};


		final Action addComposition = new Action() {
			@Override
			public void run() {
				super.run();
				ITreeSelection treeSelection = (ITreeSelection)mainTreeViewer.getSelection();
				//If the the tree selection is not null, then...
				if (!treeSelection.isEmpty()) {
					Object object = treeSelection.getFirstElement(); 				
					//If the selection is a levelOnePackage
					if (object instanceof LevelOnePackage) {
						// if the list contains the selected levelOnePackage then add the composition
						if(mainList.contains(object)){
							//Add a new Composition to the org.ica.main LevelOnepackage
							LevelOnePackage tempPackage = mainData.get(mainData.indexOf(object));
							CompositionBriques compositionBriques = new CompositionBriques("",new ArrayList<>());
							createElement(parent, compositionBriques);

							if (!compositionBriques.getNomComposition().isEmpty()) {
								List<Brique> holder = tempPackage.getMainBriqueList();
								holder.add(compositionBriques);
								tempPackage.setMainBriqueList(holder);

								mainTreeViewer.insert(object,compositionBriques, -1);
								mainTreeViewer.expandToLevel(object, 1);
							}
						}

						else {
							System.err.println("Error");
						}					
					}
					//Else If the selection is a sub Package, add the new Composition to the inner subPackage
					if (object instanceof LevelTwoPackage) {
						//if levelTwoPackage, go up one level to the parent package.
						if (treeSelection.getPaths()[0].getFirstSegment() instanceof LevelOnePackage) {
							LevelOnePackage levelOnePackage = ((LevelOnePackage)treeSelection.getPaths()[0].getFirstSegment());
							List<Brique> subPackage = levelOnePackage.getMainBriqueList();
							if (subPackage.contains(object)) {
								LevelTwoPackage temp_LevelTwoPackage = (LevelTwoPackage) subPackage.get(subPackage.indexOf(object));
								CompositionBriques compositionBriques = new CompositionBriques("",new ArrayList<>());
								createElement(parent, compositionBriques);

								if (!compositionBriques.getNomComposition().isEmpty()) {
									List<Brique> holder = temp_LevelTwoPackage.getListBrique();
									holder.add(compositionBriques);
									temp_LevelTwoPackage.setListBrique(holder);
									mainTreeViewer.insert(temp_LevelTwoPackage, compositionBriques, -1);
									mainTreeViewer.expandToLevel(temp_LevelTwoPackage, 1);
								}

							}
						}
					}
					//Else If the selection is a composition, If the composition is inside the org.ica.main LevelOnePackage
					//Add the new composition to the top level Package, else if it's in a subPackage, then add the 
					//new Composition to the current subPackage.
					if (object instanceof CompositionBriques) {

						ITreeSelection newSelection = (ITreeSelection)mainTreeViewer.getSelection();


						//First condition, if the composition is in the levelOnePackage
						if (newSelection.getPaths()[0].getParentPath().getLastSegment() instanceof LevelOnePackage) {
							LevelOnePackage levelOnePackage = ((LevelOnePackage)newSelection.getPaths()[0].getParentPath().getLastSegment());
							if (mainList.contains(levelOnePackage)) {
								LevelOnePackage temp_levelOnePackage = (LevelOnePackage) mainList.get(mainList.indexOf(levelOnePackage));
								CompositionBriques compositionBriques = new CompositionBriques("",new ArrayList<>());
								createElement(parent, compositionBriques);


								if (!compositionBriques.getNomComposition().isEmpty()) {
									List<Brique> holder = temp_levelOnePackage.getMainBriqueList();
									holder.add(compositionBriques);
									temp_levelOnePackage.setMainBriqueList(holder);
									mainTreeViewer.insert(levelOnePackage, compositionBriques, -1);
								}



							}
						}	
						//Second condition, if the composition is in the inner SubPackage
						if (newSelection.getPaths()[0].getParentPath().getLastSegment() instanceof LevelTwoPackage) {
							LevelTwoPackage levelTwoPackage = ((LevelTwoPackage)newSelection.getPaths()[0].getParentPath().getLastSegment());

							CompositionBriques compositionBriques = new CompositionBriques("",new ArrayList<>());
							createElement(parent, compositionBriques);
							if (!compositionBriques.getNomComposition().isEmpty()) {
								List<Brique> holder = levelTwoPackage.getListBrique();
								holder.add(compositionBriques);
								levelTwoPackage.setListBrique(holder);
								mainTreeViewer.insert(levelTwoPackage, compositionBriques, -1);
							}


						}						
					}
				}
			}
		};

		//Action to add a Brique to a Composition NB: Briques can only be added to Compositions
		//Both LevelOne and LevelTwo
		final Action addBrique = new Action() {
			@Override
			public void run() {
				super.run();

				ITreeSelection treeSelection = (ITreeSelection)mainTreeViewer.getSelection();

				if (!treeSelection.isEmpty()) {
					Object object = treeSelection.getFirstElement();
					if (object instanceof CompositionBriques) {
						CompositionBriques compositionBriques = ((CompositionBriques)object);
						//Call the InitialModelCreation with this here compositionBriques instance.
						FactoryTools tools = new FactoryTools(compositionBriques.getClass().getName(), compositionBriques);
						//		System.out.println("Before adding new Brique "+compositionBriques.getBrique());
						ObjectFactory.getCompositionBriques(tools);

						InitialModelCreation diagCreation = new InitialModelCreation(
								parent.getParent().getShell(), SWT.APPLICATION_MODAL
								| SWT.DIALOG_TRIM);

						try {
							diagCreation.open();
						} catch (Exception e) {	
							e.printStackTrace();
						}
						Brique brique = ObjectFactory.getBrique(Brique.class.getName());
						if (brique != null) {
							mainTreeViewer.insert(object, brique, -1);
							ObjectFactory.removeBrique(brique);
						}

					}
				}
			}
		};

		//Action to enable user to save the current project to the database
		final Action saveActiveProject = new Action() {
			@Override
			public void run() {
				super.run();
				MainWindowTabs mwt = ObjectFactory.getMainWindowTabs(new FactoryTools(MainWindowTabs.class.getName()));
				if (mwt == null) return;
								
				ITreeSelection treeSelection = (ITreeSelection)mainTreeViewer.getSelection();
				if (!treeSelection.isEmpty()) {
					Object object = treeSelection.getFirstElement();
					if (object instanceof LevelOnePackage) {
						if( mwt.saveProject()){
							mwt.updateUserAfterSave();
							CompositionBriques compositionBriques =  mwt.getCompositionBriques();
							
							LevelOnePackage tempPackage = mainData.get(mainData.indexOf(object));
						
							if (!compositionBriques.getNomComposition().isEmpty()) {
								List<Brique> holder = tempPackage.getMainBriqueList();
								holder.add(compositionBriques);
								tempPackage.setMainBriqueList(holder);

								mainTreeViewer.insert(object,compositionBriques, -1);
								mainTreeViewer.expandToLevel(object, 1);
							}
						}
					}
					if (object instanceof LevelTwoPackage) {
						if( mwt.saveProject()){
							mwt.updateUserAfterSave();
							CompositionBriques compositionBriques =  mwt.getCompositionBriques();
							//if levelTwoPackage, go up one level to the parent element.
							Object anotherObject = treeSelection.getPaths()[0].getFirstSegment();
							if (anotherObject instanceof LevelOnePackage) {
								LevelOnePackage levelOnePackage = ((LevelOnePackage)anotherObject);
								List<Brique> subPackageList = levelOnePackage.getMainBriqueList();
								if (subPackageList.contains(object)) {
									LevelTwoPackage temp_LevelTwoPackage = (LevelTwoPackage) subPackageList.get(subPackageList.indexOf(object));


									if (!compositionBriques.getNomComposition().isEmpty()) {
										List<Brique> holder = temp_LevelTwoPackage.getListBrique();
										holder.add(compositionBriques);
										temp_LevelTwoPackage.setListBrique(holder);
										mainTreeViewer.insert(temp_LevelTwoPackage, compositionBriques, -1);
										mainTreeViewer.expandToLevel(temp_LevelTwoPackage, 1);
									}

								}
							}
							
						}
					}
				}
			}
		};
		
		MenuManager menuManager = new MenuManager();
		menuManager.setRemoveAllWhenShown(true);

		menuManager.addMenuListener(manager->{
			IStructuredSelection selection = (IStructuredSelection)mainTreeViewer.getSelection();
			if (!selection.isEmpty()) {


				Object object = selection.getFirstElement();

				if (object instanceof LevelOnePackage) {
					addPackage.setText("Ajouter Nouveau Package");
					addSubPackage.setText("Ajouter Sous Package");
					addComposition.setText("Ajouter Nouveau Modèle");
					saveActiveProject.setText("Ajouter Projet");
					saveActiveProject.setToolTipText("Ajouter le projet en cours dans le package");

					menuManager.add(addPackage);
					menuManager.add(addSubPackage);
					menuManager.add(addComposition);
					menuManager.add(new Separator());
					menuManager.add(saveActiveProject);
				}
				if (object instanceof LevelTwoPackage) {

					addPackage.setText("Ajouter Nouveau Package");
					menuManager.add(addPackage);

					addSubPackage.setText("Ajouter Sous Package");
					menuManager.add(addSubPackage);

					addComposition.setText("Nouveau Modèle");
					menuManager.add(addComposition);
					
					
					saveActiveProject.setText("Ajouter Projet");
					saveActiveProject.setToolTipText("Ajouter le projet en cours dans le package");
					menuManager.add(new Separator());
					menuManager.add(saveActiveProject);
					
					
				}
				if (object instanceof CompositionBriques) {
					addPackage.setText("Ajouter Nouveau Package");
					menuManager.add(addPackage);

					addSubPackage.setText("Ajouter Sous Package");
					menuManager.add(addSubPackage);

					addComposition.setText("Ajouter Nouveau Modèle");
					menuManager.add(addComposition);

					addBrique.setText("Ajouter Nouvelle Brique");
					menuManager.add(addBrique);

				}

			}
			else {
				addPackage.setText("Ajouter Nouveau Package");
				menuManager.add(addPackage);
			}
		});


		mainTreeViewer.getControl().setMenu(menuManager.createContextMenu(mainTreeViewer.getControl()));


		mainTreeViewer.getControl().addListener(SWT.MouseDoubleClick, new CustomMouseDoubleclick(shellForShellViewingContents));




	}
	/**
	 * 
	 * @return  A shell on which non dialog windows will be attached. This enables easy cleanup
	 * when disposing windows.
	 */
	
	public static Shell getshellForShellViewingContents(){
		return shellForShellViewingContents;
	}
	/**
	 * 
	 * @return The {@code mainTreeViewer}
	 */
	public TreeViewer getTreeViewer(){
		return this.mainTreeViewer;	
	}



	private class MyTreeListFactory implements IObservableFactory {

		@Override
		public IObservable createObservable(Object target) {

			if (target instanceof IObservableList) {
				try{
					return (IObservableList)target;
				}
				catch(Exception af){
					System.err.println("Assertion failed");
					af.printStackTrace();
				}
			}
			else if (target instanceof LevelOnePackage) {
				
				return BeansObservables.observeList(Realm.getDefault(), target, "mainBriqueList",LevelOnePackage.class);
			}
			else if (target instanceof LevelTwoPackage) {
				
				return BeansObservables.observeList(Realm.getDefault(), target, "listBrique",LevelTwoPackage.class);
			}
			else if (target instanceof CompositionBriques) {
				return BeansObservables.observeList(Realm.getDefault(), target, "brique",CompositionBriques.class);
			}

			return new EmptyObservableList(Realm.getDefault(), null); 

		}}




	private class MyTreeStructureAdvisor extends TreeStructureAdvisor{

		@Override
		public Object getParent(Object element) {
			ITreeSelection treeSelection = (ITreeSelection)mainTreeViewer.getSelection();
			if (!treeSelection.isEmpty()) {

			}
			return super.getParent(element);
		}

		@Override
		public Boolean hasChildren(Object element) {
			if (element instanceof LevelOnePackage) {
				return ((LevelOnePackage)element).getMainBriqueList().size() > 0;
			}
			if (element instanceof LevelTwoPackage) {
				return ((LevelTwoPackage)element).getListBrique().size() > 0;
			}
			if (element instanceof CompositionBriques) {
				;
				//	return ((CompositionBriques)element).getBrique().size() > 0;
			}
			return false;
		}

	}


	private class MyCustomLabelProvider extends StyledCellLabelProvider {
		private IMapChangeListener mapChangeListener = 
				new IMapChangeListener() {

			@Override
			public void handleMapChange(MapChangeEvent event) {
				Set<?> affectedElements = event.diff.getChangedKeys();
				if (!affectedElements.isEmpty()) {
					LabelProviderChangedEvent newEvent = 
							new LabelProviderChangedEvent(MyCustomLabelProvider.this, affectedElements.toArray());
					fireLabelProviderChanged(newEvent);
				}

			}
		};


		public MyCustomLabelProvider(IObservableMap ... attributeMaps){
			for (int i = 0; i < attributeMaps.length; i++) {
				attributeMaps[i].addMapChangeListener(mapChangeListener);

			}
		}


		@Override
		public void addListener(ILabelProviderListener listener) {

		}

		@Override
		public void update(ViewerCell cell) {

			super.update(cell);
			if (cell.getElement() instanceof LevelOnePackage) {

				LevelOnePackage lvl1 = (LevelOnePackage)cell.getElement(); 

				StyledString styledString = new StyledString(lvl1.getPackageName() 
						!= null ? (String) lvl1.getPackageName() : "New Package_");

				cell.setText(styledString.getString());
				cell.setImage(imageHolder.getLvl1packageIcon16());
				cell.setStyleRanges(styledString.getStyleRanges());
			}
			else if (cell.getElement() instanceof LevelTwoPackage) {

				LevelTwoPackage lvl2 = (LevelTwoPackage)cell.getElement();

				StyledString styledString = new StyledString(lvl2.getSubPackageName()
						!= null ? lvl2.getSubPackageName() : "New Sub Package_");
				cell.setText(styledString.getString());
				cell.setImage(imageHolder.getLvl2packageIcon16());

				cell.setStyleRanges(styledString.getStyleRanges());
			}
			else if (cell.getElement() instanceof CompositionBriques) {
				CompositionBriques cmpB = (CompositionBriques)cell.getElement();
				StyledString styledString = new StyledString(cmpB.getNomComposition()
						!= null ? cmpB.getNomComposition() : "New Composition_");
				cell.setText(styledString.getString());

				cell.setImage(imageHolder.getCompositionIcon16());
				cell.setStyleRanges(styledString.getStyleRanges());
			}
			else {
				Brique brique = (Brique)cell.getElement();
				StyledString styledString = new StyledString(brique.getNomBrique()
						!= null ? brique.getNomBrique() : "New Model_");
				cell.setText(styledString.getString());
				cell.setImage(imageHolder.getBrique16());
				cell.setStyleRanges(styledString.getStyleRanges());
			}
		}


		@Override
		public boolean isLabelProperty(Object element, String property) {
			return false;
		}

		@Override
		public void removeListener(ILabelProviderListener listener) {
		}



	}

	//Unimplemented class
	public class MySorter extends ViewerComparator{
		@Override
		public int compare(Viewer viewer, Object e1, Object e2) {
			return super.compare(viewer, e1, e2);
		}
		@Override
		public boolean isSorterProperty(Object element, String property) {
			return super.isSorterProperty(element, property);
		}
		@Override
		public int category(Object element) {
			return super.category(element);
		}
	}

	/**
	 * @return the WritableList of MainApplicationWindowList
	 */
	public WritableList getMainList() {
		return mainList;
	}

	/**
	 * Method to create the dialog to insert component names.
	 * @param parent : Composite on which to attach the dialog
	 * @param object : Object to be created.
	 */
	private void createElement(Composite parent, Object object ){

		if (object instanceof LevelOnePackage) {
			NewElementCreation elementCreation = new NewElementCreation(parent.getParent().getShell(),
					SWT.APPLICATION_MODAL|SWT.DIALOG_TRIM,
					(LevelOnePackage)object);
			try {
				elementCreation.open();
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
		if (object instanceof LevelTwoPackage) {
			NewElementCreation elementCreation = new NewElementCreation(parent.getParent().getShell(),
					SWT.APPLICATION_MODAL|SWT.DIALOG_TRIM,
					(LevelTwoPackage)object);
			try {
				elementCreation.open();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (object instanceof CompositionBriques) {
			NewElementCreation elementCreation = new NewElementCreation(parent.getParent().getShell(),
					SWT.APPLICATION_MODAL|SWT.DIALOG_TRIM,
					(CompositionBriques)object);
			try {
				elementCreation.open();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}


	}


	@Override
	public void dragStart(DragSourceEvent event) {
		ITreeSelection treeSelection = (ITreeSelection) mainTreeViewer.getSelection();
		Object object = treeSelection.getFirstElement();

		if (object instanceof LevelOnePackage) {
			event.doit = false;

		}
		if (object instanceof LevelTwoPackage) {
			event.doit = false;
		}


		if (object instanceof CompositionBriques) {
			event.image = imageHolder.getCompositionIcon32();
			event.offsetX = -10;
		}
		else {
			event.image = imageHolder.getBrique32();
			event.offsetX = -10;
		}
	}


	@Override
	public void dragSetData(DragSourceEvent event) {
		ITreeSelection treeSelection = (ITreeSelection) mainTreeViewer.getSelection();
		Object object = treeSelection.getFirstElement();
		if (object instanceof LevelOnePackage) {			
			event.doit = false;
		}
		if (object instanceof LevelTwoPackage) {

			event.doit = false;
		}
		if (object instanceof CompositionBriques) {

			CompositionBriques compositionBriques = (CompositionBriques)object;

			if (TextTransfer.getInstance().isSupportedType(event.dataType)) {
				event.data = compositionBriques.getNomComposition();
			}
			if (CustomTransferType_COMPOSITION.getInstance().isSupportedType(event.dataType)) {
				event.data = compositionBriques;
			}
		}
		//if (object instanceof Brique), this section of the code isn't currently used. I kept it in case it's needed in the future.
		else {			
			Brique briques = (Brique)object;
			if (TextTransfer.getInstance().isSupportedType(event.dataType)) {
				event.data = briques.getNomBrique();
			}
			if (CustomTransferType_BRIQUE.getInstance().isSupportedType(event.dataType)) {
				event.data = briques;
			}
		}
	}


	@Override
	public void dragFinished(DragSourceEvent event) {
		//System.out.println("DRAG FINISHED "+event.doit);

	}

	class CustomMouseDoubleclick implements Listener{
		//	final Composite composite;
		final Shell shell;
		public CustomMouseDoubleclick(Shell parent){
			shell = parent;
		}
		@Override
		public void handleEvent(Event event) {
			ITreeSelection selection = (ITreeSelection)mainTreeViewer.getSelection();
			if (!selection.isEmpty()) {
				if(selection.getFirstElement() instanceof CompositionBriques){
					CompositionBriques compositionBriques = (CompositionBriques)selection.getFirstElement();
					//open the composition in a separate window and show the contents										
					try {
						if(!ObjectFactory.doesContain(compositionBriques)){
							ViewModelContentsWindow view = new ViewModelContentsWindow(shell,
									SWT.NONE, compositionBriques);

							view.open();

						}
					} catch (Exception e) {
						e.printStackTrace();			
					}
				}				

			}


		}

	}

}


