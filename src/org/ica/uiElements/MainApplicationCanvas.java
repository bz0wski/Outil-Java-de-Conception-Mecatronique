package org.ica.uiElements;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.ica.briquePackage.Brique;
import org.ica.briquePackage.CompositionBriques;
import org.ica.briquePackage.Equation;
import org.ica.briquePackage.Parametre;
import org.ica.exceptions.MainApplicationException;
import org.ica.main.MainCanvasObserver;
import org.ica.main.MainCanvasSubject;
import org.ica.utilityClasses.FactoryTools;
import org.ica.utilityClasses.ObjectFactory;
import org.ica.utilityClasses.RenameContents;
/**
 * 
 * This class creates the controls to allow for drag-drop support for creating new {@link CompositionBriques} objects. 
 * It houses the  TabFolder that where all the major action happens.
 * @author Salim AHMED
 *
 */

public class MainApplicationCanvas implements MainCanvasSubject{
	/**Drop operations accepted*/
	private final int operations;
	private Transfer[] transfer;
	private final MainApplicationWindowList mainList;

	private final Composite composite;

	private static long briqueId = 0;
	private static long compositionBriqueId = 0;
	/**volatile because It may be shared among different threads*/
	private volatile static long journal = 0;
	/**this is a list of observers listening for changes(like a drop operation) on the canvas.*/
	private List<MainCanvasObserver> observers;
	
	public MainApplicationCanvas(Composite parent) {
		 composite = new ScrolledComposite(parent, SWT.NONE);
		
		
		composite.setBackground(new Color(composite.getDisplay(), new RGB(255, 255, 255)));
		composite.setLayout(new FormLayout());
		
		composite.setBackgroundMode(SWT.INHERIT_DEFAULT);
	
		
	
		FormData fd_dropzone = new FormData();
		fd_dropzone.right = new FormAttachment(100, 0);
		fd_dropzone.bottom = new FormAttachment(100, 0);
		fd_dropzone.top = new FormAttachment(0, 0);
		fd_dropzone.left = new FormAttachment(0, 0);
	
		composite.setLayoutData(parent.getLayoutData());
		
		
		operations = DND.DROP_DEFAULT|DND.DROP_COPY | DND.DROP_MOVE;
		 

		createDropSupport(composite);
	
		
		mainList = ObjectFactory.
				getMainApplicationWindowList(new FactoryTools(MainApplicationWindowList.class.getName()));
	
		composite.addListener(SWT.Resize, listener->{
	
			composite.layout(true, true);
			
			});
		observers = new ArrayList<MainCanvasObserver>();
		

	}	
	
	/**
	 * @return the composite
	 */
	public Composite getComposite() {
		return composite;
	}

/**
 * This method is responsible for creating DND support, allowing for objects to be dragged from the 
 * {@link MainApplicationWindowList} onto it.
 * @param parent : The {@code Composite} on which to attach the DND support.
 */
	private void createDropSupport(Composite parent){

		final DropTarget  dropTarget= new DropTarget(parent, operations);
		
		final LocalSelectionTransfer localtransfer = LocalSelectionTransfer.getTransfer();
		
		final CustomTransferType_BRIQUE customType = CustomTransferType_BRIQUE.getInstance();

		final TextTransfer textTransfer = TextTransfer.getInstance();
		final CustomTransferType_COMPOSITION customTypeTwo = CustomTransferType_COMPOSITION.getInstance();

		 transfer = new Transfer[] { textTransfer,customType, customTypeTwo, localtransfer};
		 
		 dropTarget.setTransfer(transfer);
		
		 dropTarget.addDropListener(new DropTargetListener() {
			
			@Override
			public void dragEnter(DropTargetEvent event) {
				//System.out.println("Drag Enter "+event.detail );
				//if no modifier keys are pressed when drag enters, specify 
				//event detail to be DROP_COPY else do nothing
				if (event.detail == DND.DROP_DEFAULT) {
					if ((event.operations & DND.DROP_COPY) != 0) {
						event.detail = DND.DROP_COPY;
					}
						else {
							event.detail = DND.DROP_NONE;
						}					
				}
					
				
				if (!mainList.getTreeViewer().getTree().isDisposed()) {
					ITreeSelection treeSelection = (ITreeSelection)mainList.getTreeViewer().getSelection();	
					if (!treeSelection.isEmpty()) {
						
					
						if (treeSelection.getFirstElement() instanceof CompositionBriques) {
							for (int i = 0; i < event.dataTypes.length;i++ ) {
								
								if (customTypeTwo.isSupportedType(event.dataTypes[i])) {
								
									event.currentDataType = event.dataTypes[i];
									//the data to be transferred should only be copied
									if (event.detail != DND.DROP_COPY) {
										event.detail = DND.DROP_NONE;
									
									}
								}
							}
						}
						else if (treeSelection.getFirstElement() instanceof Brique) {
							for (int i = 0; i < event.dataTypes.length;i++ ) {	
						
								if (customType.isSupportedType(event.dataTypes[i])) {
									
									event.currentDataType = event.dataTypes[i];
									//the data to be transferred should only be copied
									if (event.detail != DND.DROP_COPY) {
										event.detail = DND.DROP_NONE;
									}
								}
							}
						}
					}
				}
				////////////////////////////////////////////////////////
				for (int i = 0; i < event.dataTypes.length;i++ ) {
					if (localtransfer.isSupportedType(event.dataTypes[i])) {
						
						event.currentDataType = event.dataTypes[i];
						if (event.detail != DND.DROP_COPY) {
							event.detail = DND.DROP_MOVE;
						}
					}
				}					
			
	}		
			
			@Override
			public void dragOver(DropTargetEvent event) {		
				event.feedback = DND.FEEDBACK_SCROLL;
		/*		if (customType.isSupportedType(event.currentDataType)) {
					//NB:- On unsuported platforms this call will return null; Windows is not unsupported
					Object object = customType.nativeToJava(event.currentDataType);
					if (object != null) {								
						//	System.out.println(((Brique)object).getNomBrique());						
					}
					else {
						System.err.println("Null");
					}
					
				}
				if (customTypeTwo.isSupportedType(event.currentDataType)) {
				Object object = customType.nativeToJava(event.currentDataType);
					if (object != null) {								
							System.out.println(((CompositionBriques)object).getNomBrique());						
					}
					if (object instanceof CompositionBriques) {
				System.out.println(((CompositionBriques)object).getNomComposition());
			}
					
				}*/
			
			}
			@Override
			public void dragOperationChanged(DropTargetEvent event) {
			//	System.out.println("Drag Op changed");
				if (event.detail == DND.DROP_DEFAULT) {
					if ((event.operations & DND.DROP_COPY) != 0) {
						event.detail = DND.DROP_COPY;
					}
					else if ((event.operations & DND.DROP_MOVE) != 0) {
						event.detail = DND.DROP_MOVE;
					}
					else {
						event.detail = DND.DROP_NONE;
					}
				}
		}
		
			@Override
			public void dropAccept(DropTargetEvent event) {
			//	System.out.println("Drop Accept "+event.detail+" ");			
			}

			@Override
			public void drop(DropTargetEvent event) {
				
				//check whether the new object to be dropped from the mainList already exists on the canvas
				//if it does change its name before dropping it
				if (customType.isSupportedType(event.currentDataType)) {
						Object object = event.data;
						try {
							checkIfAlreadyOnCanvas(object);
						} catch (MainApplicationException e) {
							System.err.println("Error with Equation statement");
							e.printStackTrace();
						}
					
						
						String bNom = ((Brique)object).getNomBrique();
						Point trans = new Point(event.x, event.y);
						createVisualRepresentation(parent, (Brique)object, bNom, trans);
						
						//notify any observers of the new addition, and if the cache tracking the number
						// of components on the canvas to know whether to create a new project or not is
						if (journal == 0) {
							notifyObserver(true);
						}
						else
							notifyObserver(false);
						
						journal +=1;
						
				}
				if (customTypeTwo.isSupportedType(event.currentDataType)) {
					Object object = event.data;
					
					if (object instanceof CompositionBriques) {
						//if composition, change name					
						try {
							 checkIfAlreadyOnCanvas(object);
						} catch (MainApplicationException e) {
							System.err.println("Error with Equation statement");
							e.printStackTrace();
						}
						//In the case of a compositionBriques object, check if it contains any Global Equations
						//if it does, place them on the canvas too.
						CompositionBriques compositionBriques = ((CompositionBriques)object);
						String string = compositionBriques.getNomComposition();
						
						Point trans = new Point(event.x, event.y);
						createVisualRepresentation(parent, (CompositionBriques)object, string , trans);
						 
						List<Equation> globalEquations = compositionBriques.getGlobalEquations();
						if (!globalEquations.isEmpty()) {						
								for (Equation equation : globalEquations) {
									
									Point point = new Point(event.x, event.y);
									new TempCanvas(composite, equation, equation.getContenuEqn(), point, false);
								}
							
						}
						//notify any observers of the new addition, journal keeps track of the 
						//components dropped on the canvas, if it is zero, a new project is created.
						if (journal == 0) {
							notifyObserver(true);
						}
						else
							notifyObserver(false);
						
						journal +=1;
					}
				}
				if (localtransfer.isSupportedType(event.currentDataType)) {
									
					Object tmp = ((StructuredSelection) event.data).getFirstElement();
					Object object = ((TempCanvas) tmp).getDataObject();
					Point trans = new Point(event.x, event.y);
					
					if (object instanceof CompositionBriques) {
												
						CompositionBriques compositionBriques = (CompositionBriques)object;						
						createVisualRepresentation(parent,compositionBriques , compositionBriques.getNomComposition() , trans);
						//notify any observers of the new addition, journal keeps track of the 
						//components dropped on the canvas, if it is zero, a new project is created.
						if (journal == 0) {
							notifyObserver(true);
						}
						else
							notifyObserver(false);
						
						journal +=1;
					}
					else if (object instanceof Equation) {
						Equation equation = (Equation)object;
						createVisualRepresentation(parent, equation, equation.getContenuEqn(), trans);
						//notify any observers of the new addition, journal keeps track of the 
						//components dropped on the canvas, if it is zero, a new project is created.
						if (journal == 0) {
							notifyObserver(true);
						}
						else
							notifyObserver(false);
						
						journal +=1;
					}
					else {
					
						Brique brique = (Brique)object;
						createVisualRepresentation(parent,brique ,brique.getNomBrique() , trans);
						//notify any observers of the new addition, journal keeps track of the 
						//components dropped on the canvas, if it is zero, a new project is created.
						if (journal == 0) {
							notifyObserver(true);
						}
						else
							notifyObserver(false);
						
						journal +=1;
					}
				}
			}

			@Override
			public void dragLeave(DropTargetEvent event) {
				
			}
				
				
		});
	   
	}
/**
 * As the name of the method suggests, this method checks if the object passed as parameter 
 * exists on the drop target.
 * @param object : The object whose presence is to be verified on the {@code MainApplicationCanvas}.
 * @return A boolean value to indicate the presence or not.
 * @throws MainApplicationException {@code MainApplicationException}
 */
	private boolean checkIfAlreadyOnCanvas(Object object) throws MainApplicationException{
		if (composite.isDisposed()) {
			return false;
		}
		Control[] children = composite.getChildren();
				
		if (object instanceof CompositionBriques) {
			CompositionBriques compositionBriques = (CompositionBriques)object;		
			String nomCompo = compositionBriques.getNomComposition();
		
			for (int i = 0; i < children.length; i++) {
				Control control = children[i];
				TempCanvas tp = (TempCanvas)control;
				if (tp.getDataObject() instanceof CompositionBriques) {
					
					CompositionBriques tpCompositionBriques = (CompositionBriques)tp.getDataObject();
					String tpNom = tpCompositionBriques.getNomComposition();
					
					if (tpNom.equals(nomCompo)) {
						StringBuilder sB = new StringBuilder(nomCompo+"_"+compositionBriqueId++) ;
						compositionBriques.setNomComposition(sB.toString());
						//rename all the briques in the composition
						//rename all the parameters in the composition
						renameBriques(compositionBriques);
						return true;
					}
					
				}
			}
			//rename all the briques in the composition
			//rename all the parameters in the composition
			renameBriques(compositionBriques);
		}
		else {
			Brique briques = (Brique)object;
			String nomB = briques.getNomBrique();
			for (int i = 0; i < children.length; i++) {
				Control control = children[i];
				TempCanvas tp = (TempCanvas)control;
				if (tp.getDataObject().getClass().getName().equals(Brique.class.getName())) {
					Brique tp_briques = (Brique)tp.getDataObject();
					String tp_Nom = tp_briques.getNomBrique();
					renameParameters(briques);
					if (tp_Nom.equals(nomB)) {
						StringBuilder sb = new StringBuilder(nomB+"_"+briqueId++);
						briques.setNomBrique(sb.toString());
						renameParameters(briques);
						return true;
					}
				
				}
			}
		}
		return false;
	}
	/***Renames all the parameters in a brique object received as a parameter.
 * @param compositionBriques : for every Brique in the name, it renames it to nomCompositionBriques_nomBrique,
 *  a call is made to <code>renameParameters(Brique brique)</code> on every brique object to rename it's parameters
 *  to nomCompositionBriques_nomBrique_nomParametre.
	 * .
	 * @throws MainApplicationException {@code MainApplicationException} */
	private void renameBriques(CompositionBriques compositionBriques) throws MainApplicationException {
		//rename all the briques in the composition
		for (Brique brique : compositionBriques.getBrique()) {
			String nom = brique.getNomBrique();
			String nomC = RenameContents.firstLetterToLowerCase(compositionBriques.getNomComposition());
			brique.setNomBrique(nomC+"_"+nom);
			renameParameters(brique);
		}
	}
/**Renames all the parameters in a brique object received as a parameter.
 * @param brique : for every parameter in the name, it renames it to nomBrique_nomParametre
 * @throws MainApplicationException {@code MainApplicationException}
 * */
	private void renameParameters(Brique brique) throws MainApplicationException{
		List<Equation> equations = brique.getListEquations();
		String briqueName = brique.getNomBrique();

		for (Equation equation : equations) {


			Map<Integer, Object> map = RenameContents.function(equation);
			StringBuilder builder = new StringBuilder();
			for(Iterator<Map.Entry<Integer, Object>> entry = map.entrySet().iterator(); entry.hasNext();){
				Object object = entry.next().getValue();

				if (object instanceof Parametre){
					Parametre p = (Parametre)object;
					String newParameterName = briqueName+"_"+p.getNomP();
					p.setNomP(newParameterName);
					builder.append(newParameterName);


				}
				else if (object instanceof String) {
					String string = (String)object;				
					builder.append(string);

				}
			}
			equation.setContenuEqn(builder.toString());

		}
	}



	
	/**
	 * This methods creates a visual representation of the object to be dropped.
	 * This will enable the user to see and manipulate the object physically.
	 * @param parent : The parent object on which to draw the visual representation.
	 * @param object : The object whose visual representation is to be created.
	 * @param toolTip : The tooltip to display when the user hovers over visual representation. 
	 * @param point : The point at which to draw the visual representation.
	 */
	private void createVisualRepresentation(Composite parent, Object object, String toolTip, Point point){
		new TempCanvas(parent,object, toolTip, point, true );		
	}

	@Override
	public void notifyObserver(boolean empty) {
		//send the new number of children to any interested party who will be listening 
		//for a change in the composite
		int num = !composite.isDisposed()? composite.getChildren().length : -1;
		for (MainCanvasObserver observer : observers) {
			observer.update(num, empty);
		}
		
	}

	@Override
	public void registerObserver(MainCanvasObserver o) {
		observers.add(o);
		
	}

	@Override
	public void unregisterObserver(MainCanvasObserver o) {
		if (o == null) {
			return;
		}
		if (observers.contains(o)) {
			observers.remove(o);
		}	
		
	}

	/**
	 * Reset the journal to it's initial state, 
	 * signaling the creation of a new project;
	 */
	
	public void resetJournal() {
		MainApplicationCanvas.journal = 0;
	}
	
	
	
}
