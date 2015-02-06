package org.ica.uiElements;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import org.eclipse.core.databinding.Binding;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.beans.BeanProperties;
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
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.internal.databinding.observable.EmptyObservableList;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.databinding.fieldassist.ControlDecorationSupport;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.databinding.viewers.ObservableListTreeContentProvider;
import org.eclipse.jface.databinding.viewers.TreeStructureAdvisor;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewerEditor;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationEvent;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationStrategy;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.FocusCellOwnerDrawHighlighter;
import org.eclipse.jface.viewers.ICellEditorListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.TreeViewerEditor;
import org.eclipse.jface.viewers.TreeViewerFocusCellManager;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.ica.briquePackage.Brique;
import org.ica.briquePackage.CompositionBriques;
import org.ica.briquePackage.Equation;
import org.ica.briquePackage.Parametre;
import org.ica.enumeration.SousTypeParametre;
import org.ica.enumeration.TypeParametre;
import org.ica.exceptions.InvalidParameter;
import org.ica.exceptions.MainApplicationException;
import org.ica.utilityClasses.FactoryTools;
import org.ica.utilityClasses.ImageHolder;
import org.ica.utilityClasses.ObjectFactory;
import org.ica.utilityClasses.RenameContents;
import org.ica.utilityClasses.VerificationSyntaxeEquation;
import org.ica.utilityClasses.VerificationValeurParametre;

/** 
 * 
 * Creates the components which enable the visualisation and the modification of {@code Brique} objects
 * @author Salim Ahmed
 */
public class AfficherEtModifierBriqueContents {

	private static final String[] parameterColumnNames = {"Brique","Equation","Nom Paramètre","Type Paramètre", "Sous Type","Valeur","Unité","Propriété","Description"} ;

	private static final String[] PARAMETERTYPES = {"INPUT","UNDETERMINED","OUTPUT"};

	private static final String[] INPUT_PARAMETER_SUBTYPE = {"CONSTANT","RANGE","SET"};

	private static final String[] UNDETERMINED_PARAMETER_SUBTYPE = {"INPUT_UNDETERMINED","OUTPUT_UNDETERMINED","FREE"};

	private static final String[] OUTPUT_PARAMETER_SUBTYPE = {"OUTPUT"};

	private static final String[] EMPTY_STRING_ARRAY = {""};

	private final TextCellEditor textEditor;
	private final TextCellEditor valeurParametreEditor;
	private final ComboBoxCellEditor typeParametreEditor;
	private WritableList writableListEquations;
	private final List<?> backingList = new ArrayList<>();
	private final Object objectToVerify;
	private final TreeViewer briqueTreeViewer;
	//stores the validated state of the contents.
	private boolean isValid = true;

	private final  MainWindowTabs mwt = ObjectFactory.getMainWindowTabs(new FactoryTools(MainWindowTabs.class.getName()));
	//Serves as both the Parametre object to observe and the temporary Parametre object to verify the entries. 
	final private Parametre t_parametre = new Parametre();

	/**@param aff
	 * 	AfficherEtModifierBrique object on which the contents will be placed
	 * @param object
	 * 	Represents either a Brique or CompositionBriques object whose content will be displayed
	 * <br>
	 * Creates the contents of AfficherEtModifierBrique window which will hold data to be displayed and modified by
	 * the user.
	 * */
	public AfficherEtModifierBriqueContents(AfficherEtModifierBrique aff,Object object) {

		briqueTreeViewer = aff.getBriqueTreeViewer();
		
		ObservableListTreeContentProvider contentProvider = new ObservableListTreeContentProvider(new MyTreeListFactory(), new MyTreeStructureAdvisor());
		briqueTreeViewer.setContentProvider(contentProvider);
	
		textEditor = new TextCellEditor(aff.getBriqueTreeViewer().getTree(),SWT.NONE);
		textEditor. addListener(new MyCustomCellEditorListener());

		valeurParametreEditor = new TextCellEditor(aff.getBriqueTreeViewer().getTree(),SWT.NONE);
		valeurParametreEditor. addListener(new MyCustomCellEditorListener());

		typeParametreEditor = new ComboBoxCellEditor(aff.getBriqueTreeViewer().getTree(), PARAMETERTYPES,SWT.NONE|SWT.READ_ONLY|SWT.SINGLE);
		typeParametreEditor.addListener(new MyCustomCellEditorListener());

	
		
		if (object instanceof CompositionBriques) {			
			writableListEquations = new WritableList(backingList,Brique.class);	
			
		}
		else if (object.getClass().getName().equals(Brique.class.getName())) {			
			writableListEquations = new WritableList(backingList,Equation.class);
		}
		briqueTreeViewer.setInput(writableListEquations);
		this.objectToVerify = object;

		
	
		
		//Customising editing support
		TreeViewerFocusCellManager focusCellManager =  new TreeViewerFocusCellManager(briqueTreeViewer,new FocusCellOwnerDrawHighlighter(briqueTreeViewer)); 

		ColumnViewerEditorActivationStrategy actSupport = new ColumnViewerEditorActivationStrategy(briqueTreeViewer){
			@Override
			protected boolean isEditorActivationEvent(
					ColumnViewerEditorActivationEvent event) {
				return event.eventType == ColumnViewerEditorActivationEvent.TRAVERSAL
						|| event.eventType == ColumnViewerEditorActivationEvent.MOUSE_DOUBLE_CLICK_SELECTION
						|| (event.eventType == ColumnViewerEditorActivationEvent.KEY_PRESSED && event.keyCode == SWT.CR)
						||  event.eventType == ColumnViewerEditorActivationEvent.PROGRAMMATIC;
			}
		};

		TreeViewerEditor.create(briqueTreeViewer, focusCellManager, actSupport,
				ColumnViewerEditor.TABBING_HORIZONTAL
				|ColumnViewerEditor.TABBING_MOVE_TO_ROW_NEIGHBOR
				|ColumnViewerEditor.TABBING_VERTICAL
				|ColumnViewerEditor.KEEP_EDITOR_ON_DOUBLE_CLICK);




		////////////////////////////////////////////////////Column NomBrique////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		if (object instanceof CompositionBriques) {

			final TreeViewerColumn nomBrique = new TreeViewerColumn(aff.getBriqueTreeViewer(), SWT.NONE);
			nomBrique.getColumn().setWidth(100);
			nomBrique.getColumn().setMoveable(true);
			nomBrique.getColumn().setResizable(true);
			nomBrique.getColumn().setText(parameterColumnNames[0]);
			nomBrique.getColumn().pack();
			
			IObservableMap mapNomBrique = BeansObservables.observeMap(contentProvider.getKnownElements(), "nomBrique");
			nomBrique.setLabelProvider(new MyCustomLabelProvider(mapNomBrique) {

				@Override
				public void update(ViewerCell cell) {
					super.update(cell);
					if (cell.getElement() instanceof Brique) {
						Brique brique = (Brique)cell.getElement(); 
						StyledString styledString = new StyledString((String)brique.getNomBrique()
								!= null ? (String) brique.getNomBrique() : "");
						cell.setText(styledString.getString());
						cell.setStyleRanges(styledString.getStyleRanges());
					}
				}
			});
		}


////////////////////////////////////////////////////////Column NomEquation//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

		
			final TreeViewerColumn nomEquation = new TreeViewerColumn(aff.getBriqueTreeViewer(), SWT.NONE);
			nomEquation.getColumn().setWidth(100);
			nomEquation.getColumn().setMoveable(true);
			nomEquation.getColumn().setResizable(true);
			nomEquation.getColumn().setText(parameterColumnNames[1]);
			nomEquation.getColumn().pack();
			
			IObservableMap mapNomE = BeansObservables.observeMap(contentProvider.getKnownElements(), "contenuEqn");
			nomEquation.setLabelProvider(new MyCustomLabelProvider(mapNomE) {

				@Override
				public void update(ViewerCell cell) {
					//super.update(cell);
					if (cell.getElement() instanceof Equation) {
						Equation equation = (Equation)cell.getElement(); 
						StyledString styledString = new StyledString(equation.getContenuEqn() 
								!= null ? (String) equation.getContenuEqn() : "");

						cell.setText(styledString.getString());
						cell.setStyleRanges(styledString.getStyleRanges());
					}
				}
			});

////////////////////////////////////Column nomParametre /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////		
		final TreeViewerColumn nomParametre = new TreeViewerColumn(aff.getBriqueTreeViewer(), SWT.NONE);
		nomParametre.getColumn().setMoveable(true);
		nomParametre.getColumn().setResizable(true);

		nomParametre.getColumn().setWidth(100);
		nomParametre.getColumn().setText(parameterColumnNames[2]);
		nomParametre.getColumn().pack();

		IObservableMap mapNom = BeansObservables.observeMap(contentProvider.getKnownElements(), "nomP");

		nomParametre.setLabelProvider(new MyCustomLabelProvider(mapNom) {

			@Override
			public void update(ViewerCell cell) {
				super.update(cell);
				if (cell.getElement() instanceof Parametre) {
					Parametre parametre = (Parametre)cell.getElement(); 
					StyledString styledString = new StyledString(parametre.getNomP() 
							!= null ? (String) parametre.getNomP() : "");

					cell.setText(styledString.getString());
					cell.setStyleRanges(styledString.getStyleRanges());
				}
			}
		});


//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////// TYPE PARAMETRE ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////		



		final TreeViewerColumn typeParametre = new TreeViewerColumn(aff.getBriqueTreeViewer(), SWT.NONE);

		typeParametre.getColumn().setMoveable(true);
		typeParametre.getColumn().setResizable(true);

		typeParametre.getColumn().setWidth(100);
		typeParametre.getColumn().setText(parameterColumnNames[3]);
		typeParametre.getColumn().pack();
		IObservableMap mapType = BeansObservables.observeMap(contentProvider.getKnownElements(),"typeP");
		typeParametre.setLabelProvider(new MyCustomLabelProvider(mapType) {

			@Override
			public void update(ViewerCell cell) {
				super.update(cell);
				if (cell.getElement() instanceof Parametre) {
					Parametre parametre = (Parametre)cell.getElement(); 
					StyledString styledString = new StyledString(parametre.getTypeP().toString() 
							!= null ? (String) parametre.getTypeP().toString() : "");

					cell.setText(styledString.getString());
					cell.setStyleRanges(styledString.getStyleRanges());
				}
			}
		});

		typeParametre.setEditingSupport(new EditingSupport(aff.getBriqueTreeViewer()) {

			@Override
			protected void setValue(Object element, Object value) {
				if (element instanceof Parametre) {
					int type = Integer.valueOf(String.valueOf(value));
					switch (type) {
					case 0:
						((Parametre) element).setTypeP(TypeParametre.INPUT);

						break;
					case 1:
						((Parametre) element).setTypeP(TypeParametre.UNDETERMINED);
						break;
					case 2:
						((Parametre) element).setTypeP(TypeParametre.OUTPUT);
						break;

					default:
						break;
					}
				}
				getViewer().update(element, null);
				refreshParameterValues(writableListEquations);
			}

			@Override
			protected Object getValue(Object element) {
				return 0;
			}

			@Override
			protected CellEditor getCellEditor(Object element) {
				return typeParametreEditor;
			}

			@Override
			protected boolean canEdit(Object element) {
				if (element instanceof Parametre) {
					return true;
				}
				return false;
			}
		});	


		//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

		/////////////////////////////////////////////// Column sousTypeParametre ///////////////////////////////////////////////////////////////////////////////////////////////	

		final TreeViewerColumn sousTypeParametre = new TreeViewerColumn(aff.getBriqueTreeViewer(), SWT.NONE);

		sousTypeParametre.getColumn().setMoveable(true);
		sousTypeParametre.getColumn().setResizable(true);
		sousTypeParametre.getColumn().setWidth(100);
		sousTypeParametre.getColumn().setText(parameterColumnNames[4]);
		sousTypeParametre.getColumn().pack();

		IObservableMap mapSousType = BeansObservables.observeMap(contentProvider.getKnownElements(),"sousTypeP");
		sousTypeParametre.setLabelProvider(new MyCustomLabelProvider(mapSousType) {

			@Override
			public void update(ViewerCell cell) {
				super.update(cell);
				if (cell.getElement() instanceof Parametre) {
					Parametre parametre = (Parametre)cell.getElement(); 
					StyledString styledString = new StyledString(parametre.getSousTypeP().toString() 
							!= null ? (String) parametre.getSousTypeP().toString() : "");

					cell.setText(styledString.getString());
					cell.setStyleRanges(styledString.getStyleRanges());
				}
			}
		});

		sousTypeParametre.setEditingSupport(new EditingSupport(aff.getBriqueTreeViewer()) {

			@Override
			protected void setValue(Object element, Object value) {
				if (element instanceof Parametre) {
					int type = Integer.valueOf(String.valueOf(value));
					try{
						switch (((Parametre)element).getTypeP()) {
						case INPUT:

							switch (type) {
							case 0:
								((Parametre) element).setSousTypeP(SousTypeParametre.CONSTANT);

								break;
							case 1:
								((Parametre) element).setSousTypeP(SousTypeParametre.RANGE);
								break;
							case 2:
								((Parametre) element).setSousTypeP(SousTypeParametre.SET);
								break;
							default:
								break;
							}

							break;

						case UNDETERMINED:

							switch (type) {
							case 0:
								((Parametre) element).setSousTypeP(SousTypeParametre.INPUT_UNDETERMINED);

								break;
							case 1:
								((Parametre) element).setSousTypeP(SousTypeParametre.OUTPUT_UNDETERMINED);
								break;
							case 2:
								((Parametre) element).setSousTypeP(SousTypeParametre.FREE);
								break;
							default:
								break;
							}

							break;

						case OUTPUT:
							if (type == 0) {
								((Parametre) element).setSousTypeP(SousTypeParametre.OUTPUT);
							}
							break;
						default:
							break;
						}
					}
					catch(MainApplicationException e){
						if (e instanceof InvalidParameter) {
							e.printStackTrace();
						}
						e.printStackTrace();
					}

				}
				getViewer().update(element, null);
				refreshParameterValues(writableListEquations);
			}

			@Override
			protected Object getValue(Object element) {
				return 0;
			}

			@Override
			protected CellEditor getCellEditor(Object element) {
				if (element instanceof Parametre) {
					switch (((Parametre)element).getTypeP()) {
					case INPUT:
						ComboBoxCellEditor editor =  new ComboBoxCellEditor(aff.getBriqueTreeViewer().getTree(),INPUT_PARAMETER_SUBTYPE,SWT.NONE|SWT.READ_ONLY|SWT.SINGLE);
						editor.addListener(new MyCustomCellEditorListener());
						return editor;

					case UNDETERMINED:
						ComboBoxCellEditor editor2 =  new ComboBoxCellEditor(aff.getBriqueTreeViewer().getTree(),UNDETERMINED_PARAMETER_SUBTYPE,SWT.NONE|SWT.READ_ONLY|SWT.SINGLE);
						editor2.addListener(new MyCustomCellEditorListener());
						return editor2;
					case OUTPUT:
						ComboBoxCellEditor editor3 =  new ComboBoxCellEditor(aff.getBriqueTreeViewer().getTree(),OUTPUT_PARAMETER_SUBTYPE,SWT.NONE|SWT.READ_ONLY|SWT.SINGLE);
						editor3.addListener(new MyCustomCellEditorListener());
						return editor3;

					default:
						break;
					}
				}
				return  new ComboBoxCellEditor(aff.getBriqueTreeViewer().getTree(),EMPTY_STRING_ARRAY,SWT.NONE|SWT.READ_ONLY|SWT.SINGLE);
			}

			@Override
			protected boolean canEdit(Object element) {
				if (element instanceof Parametre) {
					return true;
				}
				return false;
			}
		});

		////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

		////////////////////////////////Column valeurParametre ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////	

		final TreeViewerColumn valeurParametre = new TreeViewerColumn(aff.getBriqueTreeViewer(), SWT.NONE);

		valeurParametre.getColumn().setMoveable(true);
		valeurParametre.getColumn().setResizable(true);
		valeurParametre.getColumn().setMoveable(true);
		valeurParametre.getColumn().setWidth(100);
		valeurParametre.getColumn().setText(parameterColumnNames[5]);
		valeurParametre.getColumn().pack();

		IObservableMap mapValeur = BeansObservables.observeMap(contentProvider.getKnownElements(),"valeurP");
		valeurParametre.setLabelProvider(new MyCustomLabelProvider(mapValeur) {

			@Override
			public void update(ViewerCell cell) {
				super.update(cell);
				if (cell.getElement() instanceof Parametre) {
					Parametre parametre = (Parametre)cell.getElement(); 
					StyledString styledString = new StyledString(parametre.getValeurP()
							!= null ? (String) parametre.getValeurP() : "");

					cell.setText(styledString.getString());
					cell.setStyleRanges(styledString.getStyleRanges());
				}
			}
		});

		valeurParametre.setEditingSupport(new EditingSupport(aff.getBriqueTreeViewer()) {
			@Override
			protected void setValue(Object element, Object value) {
				if (element instanceof Parametre) {	
					//Passing the currently edited Parametre object to the validator
					updateParametre(((Parametre)element),aff.getBriqueTreeViewer().getTree().getParent());
					((Parametre)element).setValeurP((String)value);	
				}

				getViewer().update(element, null);
				refreshParameterValues(writableListEquations);
			}

			@Override
			protected Object getValue(Object element) {
				if (element instanceof Parametre) {
					//Passing the currently edited Parametre object to the validator
					updateParametre(((Parametre)element),aff.getBriqueTreeViewer().getTree().getParent());
					return (String)((Parametre)element).getValeurP();

				}
				return "";
			}

			@Override
			protected CellEditor getCellEditor(Object element) {
				return valeurParametreEditor;
			}

			@Override
			protected boolean canEdit(Object element) {
				if (element instanceof Parametre) {
					if (((Parametre)element).getTypeP().equals(TypeParametre.INPUT)) {
						return true;
					}
				}
				return false;
			}
		});


		//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

		////////////////////////////////////////////////////////Column uniteParametre//////////////////////////////////////////////////////////////////////////////////////////////////////////////

		final TreeViewerColumn uniteParametre = new TreeViewerColumn(aff.getBriqueTreeViewer(), SWT.NONE);

		uniteParametre.getColumn().setMoveable(true);
		uniteParametre.getColumn().setResizable(true);
		uniteParametre.getColumn().setText(parameterColumnNames[6]);
		uniteParametre.getColumn().pack();

		IObservableMap mapUnite = BeansObservables.observeMap(contentProvider.getKnownElements(),"uniteP");
		uniteParametre.setLabelProvider(new MyCustomLabelProvider(mapUnite) {

			@Override
			public void update(ViewerCell cell) {
				super.update(cell);
				if (cell.getElement() instanceof Parametre) {
					Parametre parametre = (Parametre)cell.getElement(); 
					StyledString styledString = new StyledString(parametre.getUniteP()
							!= null ? (String) parametre.getUniteP() : "");

					cell.setText(styledString.getString());
					cell.setStyleRanges(styledString.getStyleRanges());
				}
			}
		});

		EditingSupport uniteParametreEditingSupport = new EditingSupport(aff.getBriqueTreeViewer()) {

			@Override
			protected void setValue(Object element, Object value) {
				if (element instanceof Parametre) {

					((Parametre)element).setUniteP((String)value);
				}
				getViewer().update(element, null);
				refreshParameterValues(writableListEquations);
			}

			@Override
			protected Object getValue(Object element) {
				if (element instanceof Parametre) {
					return ((Parametre)element).getUniteP();
				}
				return "";
			}

			@Override
			protected CellEditor getCellEditor(Object element) {
				return textEditor;
			}

			@Override
			protected boolean canEdit(Object element) {
				if (element instanceof Parametre) {
					return true;
				}
				return false;
			}
		};
		uniteParametre.setEditingSupport( uniteParametreEditingSupport);



////////////////////////////////////////////Column proprieteParametre ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		final TreeViewerColumn proprieteParametre = new TreeViewerColumn(briqueTreeViewer, SWT.NONE);

		proprieteParametre.getColumn().setMoveable(true);
		proprieteParametre.getColumn().setResizable(true);
		proprieteParametre.getColumn().setMoveable(true);
		proprieteParametre.getColumn().setText(parameterColumnNames[7]);
		proprieteParametre.getColumn().pack();

		IObservableMap mapPropriete = BeansObservables.observeMap(contentProvider.getKnownElements(),"proprieteP");
		proprieteParametre.setLabelProvider(new MyCustomLabelProvider(mapPropriete) {

			@Override
			public void update(ViewerCell cell) {
				super.update(cell);
				if (cell.getElement() instanceof Parametre) {
					Parametre parametre = (Parametre)cell.getElement(); 
					StyledString styledString = new StyledString(parametre.getProprieteP()
							!= null ? (String) parametre.getProprieteP() : "");

					cell.setText(styledString.getString());
					cell.setStyleRanges(styledString.getStyleRanges());
				}
			}
		});

		proprieteParametre.setEditingSupport(new EditingSupport(briqueTreeViewer) {

			@Override
			protected void setValue(Object element, Object value) {
				if (element instanceof Parametre) {

					((Parametre)element).setProprieteP((String)value);
				}
				getViewer().update(element, null);
				refreshParameterValues(writableListEquations);
			}

			@Override
			protected Object getValue(Object element) {
				if (element instanceof Parametre) {
					return ((Parametre)element).getProprieteP();
				}
				return "";
			}

			@Override
			protected CellEditor getCellEditor(Object element) {
				return textEditor;
			}

			@Override
			protected boolean canEdit(Object element) {
				if (element instanceof Parametre) {
					return true;
				}
				return false;
			}
		});

		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////



		//////////////////////////////////////////////Column descParametre ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////	
		final TreeViewerColumn descParametre = new TreeViewerColumn(briqueTreeViewer, SWT.NONE);

		descParametre.getColumn().setMoveable(true);
		descParametre.getColumn().setResizable(true);
		descParametre.getColumn().setMoveable(true);
		descParametre.getColumn().setText(parameterColumnNames[8]);
		descParametre.getColumn().pack();
		IObservableMap mapDesc = BeansObservables.observeMap(contentProvider.getKnownElements(),"descP");
		descParametre.setLabelProvider(new MyCustomLabelProvider(mapDesc) {

			@Override
			public void update(ViewerCell cell) {
				super.update(cell);
				if (cell.getElement() instanceof Parametre) {
					Parametre parametre = (Parametre)cell.getElement(); 
					StyledString styledString = new StyledString(parametre.getDescP()
							!= null ? (String) parametre.getDescP() : "");

					cell.setText(styledString.getString());
					cell.setStyleRanges(styledString.getStyleRanges());
				}
			}
		});
		descParametre.setEditingSupport(new EditingSupport(briqueTreeViewer) {

			@Override
			protected void setValue(Object element, Object value) {
				if (element instanceof Parametre) {

					((Parametre)element).setDescP((String)value);
				}
				getViewer().update(element, null);
				refreshParameterValues(writableListEquations);
			}

			@Override
			protected Object getValue(Object element) {
				if (element instanceof Parametre) {

					return ((Parametre)element).getDescP();
				}
				return "";
			}

			@Override
			protected CellEditor getCellEditor(Object element) {
				return textEditor;
			}

			@Override
			protected boolean canEdit(Object element) {
				if (element instanceof Parametre) {
					return true;
				}
				return false;
			}
		});


//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//Finally add the content into the writable List to complete the constructor!!
		if (object instanceof CompositionBriques) {
			writableListEquations.addAll(((CompositionBriques)object).getBrique());
		}
		else {
			writableListEquations.addAll(((Brique)object).getListEquations());
		}
		
		

		createTreeContextMenu(briqueTreeViewer);
		bindBeforeSet(t_parametre, valeurParametreEditor.getControl());



	}

	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	private class MyCustomCellEditorListener implements ICellEditorListener{
		private MainWindowTabs mwt = ObjectFactory.getMainWindowTabs(new FactoryTools(MainWindowTabs.class.getName()));
		@Override
		public void applyEditorValue() {
			setValid(false);
			if(mwt != null) mwt.modified();
		}

		@Override
		public void cancelEditor() {
			setValid(false);
			if(mwt != null) mwt.modified();

		}

		@Override
		public void editorValueChanged(boolean oldValidState,
				boolean newValidState) {
			setValid(false);
			if(mwt != null) mwt.modified();

		}

	}

	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Refreshes the labels of all the objects contained in the list passed as parameter
	 * @param objects : The list of objects whose labels are to be refreshed.
	 */
	private void refreshParameterValues(List<?> objects){
		Display.getDefault().asyncExec(()->{
			if(briqueTreeViewer == null) return;
			for (Object obj : objects) {
				briqueTreeViewer.refresh(obj);
			}
		});

	}


/**
 * Create a context menu for the tree
 * @param viewer : The viewer onto which to attach the context menu.
 */
	private void createTreeContextMenu(TreeViewer viewer){
		if(viewer == null)return;
		if(viewer.getTree().isDisposed())return;
		final Action edit = new Action() {
			@Override
			public void run() {			
				super.run();
				ITreeSelection selection = (ITreeSelection)viewer.getSelection();
				boolean stripped = false;
				if (!selection.isEmpty()) {
					Object object = selection.getFirstElement();
					if (object instanceof Equation) {
						Equation equation = (Equation) object;
						
						
						if(equation.getIsModifiable()){
						String prefix = "";
						Object obj = selection.getPaths()[0].getParentPath().getLastSegment();
						
						if (obj instanceof Brique) {
							Brique brique = (Brique)obj;
							prefix = brique.getNomBrique();
							}
							String oldContent = equation.getContenuEqn();
						if(obj == null ){
							
							//manually search for the brique object
							long tracking = equation.getReferenceBrique();
							List< Brique> briques = mwt.getCompositionBriques().getBrique();
							for (Brique brique : briques) {
								if(brique.getBriqueId() == tracking){
									prefix = brique.getNomBrique();
									break;
								}
							}
							

							
						}
						//modify equation statement
							if(!prefix.isEmpty()){
								try {
									stripped = RenameContents.stripPrefix(prefix, equation);
								} catch (MainApplicationException e) {
									
									e.printStackTrace();
								}
							}
						AddNewEquation newEquation = new AddNewEquation(viewer.getTree().getShell(), SWT.NONE, equation, true);
						Object result = newEquation.open();
						
						if(result == null){
							equation.setContenuEqn(oldContent);
						 return;}	
						if(result.equals(Boolean.TRUE)){
							if (stripped) {
							try {
								
								RenameContents.renameEquationParameters(prefix, equation);
								new VerificationSyntaxeEquation(equation.getContenuEqn());
							} catch (MainApplicationException e) {
								
								equation.setContenuEqn(oldContent);
								
								e.printStackTrace();
							}	
							}				
						}
						
						replaceMatchingParameters();
						refreshParameterValues(getWritableListEquations());
				}
					}

				}

			}
		};
		
		
		
		viewer.getTree().addMenuDetectListener(menu->{
			MenuManager menuManager = new MenuManager();
			menuManager.setRemoveAllWhenShown(true);
			menuManager.addMenuListener(listener->{
				IStructuredSelection selection = (IStructuredSelection)viewer.getSelection();
				if (!selection.isEmpty()) {
					Object object = selection.getFirstElement();
					if (object instanceof Equation) {
						Equation equation = (Equation) object;
						if(equation.getIsModifiable()){												
							edit.setText("Modifier \t Ctrl+Maj+E");
							edit.setAccelerator(SWT.CTRL | SWT.SHIFT | 'E' );
							menuManager.add(edit);
						}
					}
				}

			});
			viewer.getTree().setMenu(menuManager.createContextMenu(viewer.getTree()));
		});

	}

	/**@param parametre
	 * 	The parametre object whose value is verified. 
	 * @param celleditor
	 * 	The celleditor whose content is supervised.
	 *<br> Binds the parameter value and the celleditor and updates the user 
	 *	   on the status of the data entered.
	 */
	private void bindBeforeSet(Parametre parametre,Control celleditor){
		if (parametre == null || celleditor == null) return;
		//the DatabindingContext object will manage the databindings
		//In this first part I monitor the the equation expression and check its
		//conformity to the correct syntaxe of the value to be given to the parameter based on its attributes.
		DataBindingContext ctx = new DataBindingContext();
		IObservableValue widget = WidgetProperties.text(SWT.Modify).observe(celleditor);
		IObservableValue value = BeanProperties.value(Parametre.class, "valeurP").observe(new Parametre());

		UpdateValueStrategy strategy = new UpdateValueStrategy();

		strategy.setBeforeSetValidator(param->{

			if (((String)param).length()<=0) {
				return ValidationStatus.error("Ne peut pas être vide");
			}
			try {	
				VerificationValeurParametre.parameterValues(parametre,(String)param);
			} 
			catch (MainApplicationException e) {
				if (e instanceof InvalidParameter) {

					return ValidationStatus.error(e.toString());
				}
				else {

					return ValidationStatus.error(e.toString());
				}
			}
			return ValidationStatus.ok();

		});

		//Binding the control and the data with the update strategy to use
		Binding bindValue = ctx.bindValue(widget, value,strategy,null);

		//Adding some decoration to indicate to the user the validation status of
		//of the currently typed equation expression
		ControlDecorationSupport.create(bindValue, SWT.TOP|SWT.RIGHT);
	}

	//I use this function to constantly get the currently active object by calling it from 
	// the getValue subroutine in the editing support class.
	private void updateParametre(Parametre p, Composite parent){
		try {
			t_parametre.setNomP(p.getNomP());
			t_parametre.setTypeP(p.getTypeP());
			t_parametre.setSousTypeP(p.getSousTypeP());
			t_parametre.setValeurP(p.getValeurP());

		} catch (MainApplicationException e) {
			e.printStackTrace();
			MessageBox dialog =  new MessageBox(parent.getShell(), SWT.ICON_ERROR | SWT.OK);
			dialog.equals("");

			dialog.setText("Attention");
			dialog.setMessage("Erreur sur le sous type.");

			// open dialog and await user selection
			dialog.open(); 

		}
	}

	/**@return writableListEquations
	 * The contents of the tree.*/
	public WritableList getWritableListEquations() {
		return writableListEquations;
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

		public MyCustomLabelProvider(IObservableMap attributeMaps){					
			attributeMaps.addMapChangeListener(mapChangeListener);										
		}	
		/*
		public MyCustomLabelProvider(IObservableMap ... attributeMaps){
			for (int i = 0; i < attributeMaps.length; i++) {
				attributeMaps[i].addMapChangeListener(mapChangeListener);
			}
		}
		 */		

	}


	private class  MyTreeStructureAdvisor extends TreeStructureAdvisor{
		@Override
		public Object getParent(Object element) {

			return super.getParent(element);
		}

		@Override
		public Boolean hasChildren(Object element) {
			if (element instanceof IObservableList) {
				return ((IObservableList)element).size() > 0;
			}
			if (element instanceof IObservableSet) {
				return ((IObservableSet)element).size() > 0;
			}
			if (element instanceof Brique) {
				
				return ((Brique)element).getListEquations().size() > 0;
				
			}
			if (element instanceof Equation) {					
				return ((Equation)element).getListeDeParametresEqn().size() > 0;				
			}
			if (element instanceof List<?>) {
				return ((List<?>)element).size()>0;

			}

			return false;
		}

	}


	private class MyTreeListFactory implements IObservableFactory{

		@Override
		public IObservable createObservable(Object target) {

			if (target instanceof WritableList) {
				try {
					return (IObservableList)target;
				} catch (ClassCastException e) {
					e.printStackTrace();
				}
			}			
			if (target instanceof Equation) {
				return BeansObservables.observeList(Realm.getDefault(), target, "listeDeParametresEqn");
			}
			if (target instanceof Brique) {
				return BeansObservables.observeList(Realm.getDefault(), target, "listEquations");
			}
			if (target instanceof CompositionBriques) {
				return BeansObservables.observeList(Realm.getDefault(), target, "brique");
			}
			if (target instanceof IObservableList) {
				return (IObservableList)target;
			}	

			return new EmptyObservableList(Realm.getDefault(),null);

		}	

	}
/**
 * This method is called when the user hits the validate button while viewing the contents 
 * of a {@code Brique } object. It evaluates the contents of the {@code Brique } object and informs the user
 * of any errors in the event there're any.
 * @param shell : {@link Shell} on which alert dialogs will be displayed.
 */
	public void verifyBeforeValidate(Shell shell) {
		Object object = getObjectToVerify();
		if (object instanceof CompositionBriques) {
			List<Brique> backingList = ((CompositionBriques)object).getBrique();
			List<Equation> eqnList = new ArrayList<Equation>();
			for (Brique brique : backingList) {
				eqnList.addAll(brique.getListEquations());
			}
			VerifyBeforeValidation.verifyBeforeValidate(eqnList, shell);
			setValid(VerifyBeforeValidation.isValid());
		}
		else if (object.getClass().getName().equals(Brique.class.getName())) {
			List<Equation> backingList = ((Brique)object).getListEquations();
			VerifyBeforeValidation.verifyBeforeValidate(backingList, shell);
			setValid(VerifyBeforeValidation.isValid());
		}
	}

	/*create a validate button, when the user clicks on it execute*/



	/**
	 * @return the objectToVerify, the object currently displayed by this instance.
	 */
	public Object getObjectToVerify() {
		return objectToVerify;
	}

	/**Create a new Equation instance and make it modifiable*/
	private Equation createEquation() throws MainApplicationException {
		Equation equation = new Equation(false,
				"a+b=c", 
				false, 
				new Parametre(), 
				"Insérer Propriété",
				"Insérer commentaire");
		equation.setIsModifiable(true);
		return equation;
	}

	
/**Replaces all matching parameters in the whole project*/
	private void replaceMatchingParameters() {
		Display.getDefault().asyncExec(()->{
			if(mwt == null) return;
			mwt.pack();
		
		//	CompositionBriques composition = mwt.getCompositionBriques();
			
		//	List<Equation> eqn = composition.getEquation();	
		//	MatchIdenticalParameters.matchIdenticalBriqueParameters(eqn);
		});

	}
	
	
/**Adds a new Equation to the current object
	 * @throws MainApplicationException {@code MainApplicationException}*/
	@SuppressWarnings("unchecked")
	public void AddEquation() throws MainApplicationException{
		Object object = getObjectToVerify();
		TreeViewer viewer = getBriqueTreeViewer();

		if (object instanceof CompositionBriques){		
			if (viewer != null) {
				ITreeSelection selection = (ITreeSelection)viewer.getSelection();

				if (!selection.isEmpty()) {
					Object anotherObject = selection.getFirstElement();
					if (anotherObject instanceof Parametre) {
						Object obj = selection.getPaths()[0].getParentPath().getParentPath().getLastSegment();
						if (obj instanceof Brique) {
							Brique brique = (Brique)obj;
							Equation equation = createEquation();
							equation.setReferenceBrique(brique.getBriqueId());
							//open dialog and have user modify the equation parameters
							AddNewEquation newEquation = new AddNewEquation(viewer.getTree().getShell(), SWT.NONE, equation);

							//check that the return of the operation before accepting the equation
							Object ret = newEquation.open();
							if (ret == null) return;
							if (ret.equals(Boolean.TRUE)) {
								List<Equation>  listEquations = brique.getListEquations();
								listEquations.add(equation);
								brique.setListEquations(listEquations);
								//After adding new equation, rename all the parameters, preceding them with the container composition and Brique names.	
								String prefix = brique.getNomBrique();
								RenameContents.renameEquationParameters(prefix, equation);
								
								//After adding new equation, replace all matching parameters in the current project if any exist.																	
								replaceMatchingParameters();

								viewer.insert(brique, equation, -1);
								viewer.expandToLevel(equation, 2);
								refreshParameterValues(listEquations);
							}						
						}
					}
					if (anotherObject instanceof Equation) {
						Object obj = selection.getPaths()[0].getParentPath().getLastSegment();
						if (obj instanceof Brique) {
							Brique brique = (Brique)obj;
							Equation equation = createEquation();
							equation.setReferenceBrique(brique.getBriqueId());
							//open dialog and have user modify the equation parameters
							AddNewEquation newEquation = new AddNewEquation(viewer.getTree().getShell(), SWT.NONE, equation);

							//check that the return of the operation before accepting the equation
							Object ret = newEquation.open();
							if (ret == null) return;
							if (ret.equals(Boolean.TRUE)) {
								List<Equation>  listEquations =  brique.getListEquations();
								listEquations.add(equation);
								brique.setListEquations(listEquations);
								
								//After adding new equation, rename all the parameters, preceding them with the container composition and Brique names.	
								String prefix =  brique.getNomBrique();
								RenameContents.renameEquationParameters(prefix, equation);
								
								//After adding new equation, replace all matching parameters in the current project if any exist.																	
								replaceMatchingParameters();
								viewer.insert(brique, equation, -1);
								viewer.expandToLevel(equation, 2);
								refreshParameterValues(listEquations);
								
							}
						}

					}
					if (anotherObject instanceof Brique) {

						Brique brique = (Brique)anotherObject;
						Equation equation = createEquation();
						equation.setReferenceBrique(brique.getBriqueId());
						//open dialog and have user modify the equation parameters
						AddNewEquation newEquation = new AddNewEquation(viewer.getTree().getShell(), SWT.NONE, equation);

						//check that the return of the operation before accepting the equation
						Object ret = newEquation.open();
						if (ret == null) return;
						if (ret.equals(Boolean.TRUE)) {

							List<Equation>  listEquations =  brique.getListEquations();
							listEquations.add(equation);
							brique.setListEquations(listEquations);
							
							//After adding new equation, rename all the parameters, preceding them with the container composition and Brique names.	
							String prefix = brique.getNomBrique();
							RenameContents.renameEquationParameters(prefix, equation);
							
							//After adding new equation, replace all matching parameters in the current project if any exist.																	
							replaceMatchingParameters();
							viewer.insert(brique, equation, -1);
							viewer.expandToLevel(brique, 2);
							refreshParameterValues(listEquations);
						}
					}
				}
			}
		}
		else if (object.getClass().getName().equals(Brique.class.getName())){
			if (viewer != null) {
				Brique brique = ((Brique)object);
				Equation equation = createEquation();
				equation.setReferenceBrique(brique.getBriqueId());
				//open dialog and have user modify the equation parameters
				AddNewEquation newEquation = new AddNewEquation(viewer.getTree().getShell(), SWT.NONE, equation);

				//check that the return of the operation before accepting the equation
				Object ret = newEquation.open();
				if (ret == null) return;
				if (ret.equals(Boolean.TRUE)) {
					WritableList listEquations =  getWritableListEquations();
					listEquations.add(equation);
					
					brique.setListEquations(listEquations);
					
					//After adding new equation, rename all the parameters, preceding them with the container composition and Brique names.	
					String prefix = brique.getNomBrique();
					RenameContents.renameEquationParameters(prefix, equation);
					
					//After adding new equation, replace all matching parameters in the current project if any exist.																	
					replaceMatchingParameters();
					viewer.expandToLevel(equation, 2);
					refreshParameterValues(listEquations);
				}
			}
		}
	}


	/**Remove the currently selected equation 
	 * @throws MainApplicationException {@code MainApplicationException} */
	public void removeEquation() throws MainApplicationException{
		Object object = getObjectToVerify();

		TreeViewer viewer = getBriqueTreeViewer();
		if(object instanceof CompositionBriques){
			if (viewer != null) {
				ITreeSelection selection = (ITreeSelection)viewer.getSelection();			
				if (!selection.isEmpty()) {
					Object anotherObject = selection.getFirstElement();
					if (anotherObject instanceof Parametre) {
						Object obj = selection.getPaths()[0].getParentPath().getLastSegment();
						if (obj instanceof Equation) {
							Equation equation = (Equation)obj;
							Object moreObjects = selection.getPaths()[0].getParentPath().getParentPath().getLastSegment();
							if (moreObjects instanceof Brique) {
								Brique brique = (Brique)moreObjects;
								brique.getListEquations().remove(equation);
								if (viewer != null) {
									viewer.remove(brique, new Equation[]{equation});
								}								
							}
							
						}
					}
					if (anotherObject instanceof Equation) {
						Equation equation = (Equation)anotherObject;
						Object obj = selection.getPaths()[0].getParentPath().getLastSegment();

						if (obj instanceof Brique) {
							Brique brique = (Brique)obj;
							brique.getListEquations().remove(equation);
							if (viewer != null) {
								viewer.remove(brique, new Equation[]{equation});
							}
						}
					}
				}

			}
			Display.getDefault().asyncExec(()->{
				if(mwt != null) mwt.pack();
			});
			
		}
		else if (object.getClass().getName().equals(Brique.class.getName())){
			ITreeSelection selection = (ITreeSelection)viewer.getSelection();			
			if (!selection.isEmpty()) {
				Object anotherObject = selection.getFirstElement();
				if (anotherObject instanceof Parametre) {
					Object obj = selection.getPaths()[0].getParentPath().getLastSegment();
					if (obj instanceof Equation) {
						Brique brique = (Brique)object;
						Equation equation = (Equation)obj;
						brique.getListEquations().remove(equation);
						if (viewer != null) {
							viewer.remove(equation);
						}
					}
				}
				if (anotherObject instanceof Equation) {
					Equation equation = (Equation)anotherObject;
					Brique brique = (Brique)object;
					brique.getListEquations().remove(equation);
					if (viewer != null) {
						viewer.remove(anotherObject);
					}
				}
			}
			Display.getDefault().asyncExec(()->{
				if(mwt != null) mwt.pack();
			});
		}
	}


	/**@return briqueTreeViewer
	 * */
	public TreeViewer getBriqueTreeViewer() {
		return this.briqueTreeViewer;
	}

	/**
	 * @return the isValid
	 */
	public boolean isValid() {
		return isValid;
	}

	/**
	 * @param isValid the isValid to set
	 */
	public void setValid(boolean isValid) {
		this.isValid = isValid;
	}

	/**
	 * This class runs the static Function <code>verifyBeforeValidate</code> to verify the contents of a List of equations. */

	public static class VerifyBeforeValidation{
		//Contains the errorMessage for the problem encountered 
		private static String errorMessage;
		//Contains the error status
		private static boolean isValid;
		static ImageHolder imageHolder = ImageHolder.getImageHolder();

		static void verifyBeforeValidate(List<Equation> eqnList, Shell parent){
			if(!verifyBeforeValidate.apply(eqnList)){
				setValid(false);
				showErrorMessage(parent, errorMessage);
			}
			else {
				MainApplicationWindowContents contents = ObjectFactory.getMainApplicationWindowContents(
						MainApplicationWindowContents.class.getName());
				if (contents == null) {
					throw new NullPointerException("Application failure, restart application!");
				}	
				setValid(true);
				contents.updateUser("Verification reussi", imageHolder.getOk16());
			}	
		}

		public static void verifyBeforeValidate(List<Equation> eqnList){
			if(!verifyBeforeValidate.apply(eqnList)){
				setValid(false);

				MainApplicationWindowContents contents = ObjectFactory.getMainApplicationWindowContents(
						MainApplicationWindowContents.class.getName());
				if (contents == null) {
					throw new NullPointerException("Application failure, restart application!");
				}	

				contents.updateUser("Erreur(s) dans le projet, veuillez les rectifier.", imageHolder.getError16());
			}
			else {
				setValid(true);
				MainApplicationWindowContents contents = ObjectFactory.getMainApplicationWindowContents(
						MainApplicationWindowContents.class.getName());
				if (contents == null) {
					throw new NullPointerException("Application failure, restart application!");
				}	

				contents.updateUser("Verification reussi,la compilation va démarrer", imageHolder.getOk16());
			}	
		}
/**
 * This method displays an error message to the user.
 * @param parent : {@link Composite } on which to display error. 
 * @param errorMessage : Error Message
 */
		static void showErrorMessage(Composite parent, String errorMessage){
			Display.getCurrent().asyncExec(()->{
				MessageBox dialog =  new MessageBox(parent.getShell(), SWT.ICON_ERROR | SWT.OK);

				dialog.setText("Attention");
				dialog.setMessage(errorMessage);	
				// open dialog and await user selection
				dialog.open(); 
				
			});

		}


		/**
		 * @return the isValid
		 */
		public static boolean isValid() {
			return isValid;
		}


		/**
		 * @param isValid the isValid to set
		 */
		public static void setValid(boolean isValid) {
			VerifyBeforeValidation.isValid = isValid;
		}

		/**Verifies all the components of an equation. 
		 * @param list: A list of equations to verify
		 * @return true if all tests were passed, false otherwise*/
		static final Function<List<Equation>, Boolean> verifyBeforeValidate = (eqnList)->{

			boolean isIt = true;
			for (Equation equation : eqnList) {
				//First of all verifying that every oriented equation with 
				//parametre de sortie has the corresponding parameter typed as OUTPUT
				if (equation.isOriented()) {
					if(equation.getParametreDeSortie() != null){							
						Parametre p = equation.getParametreDeSortie();
						for (Parametre parametre : equation.getListeDeParametresEqn()) {
							if (parametre.equals(p)) {
								if (!parametre.getTypeP().equals(TypeParametre.OUTPUT)) {
									errorMessage = "Erreur de type de paramètre de sortie. Equation( "+equation.getContenuEqn()+" )";
									return !isIt;
									//return errorMessage;
								}
							}
						}
					}
					else if (equation.getParametreDeSortie() == null) {
						errorMessage = "Vérifier le paramètre de sortie";
						return !isIt;
					}

				}

				//Secondly scan through all the parameters of the equations 
				//to check the Type/SubType pairing
				for (Parametre parametre : equation.getListeDeParametresEqn()){
					if (!parametre.getTypeP().getSubtype().contains(parametre.getSousTypeP())) {
						errorMessage = "Erreur de sous type du paramètre";
						return !isIt;
						
					}
					//Verify that if a parameter is typed OUTPUT, its the same as the local output of the equation
					if (parametre.getTypeP().equals(TypeParametre.OUTPUT)) {
						if (!equation.isOriented()) {
							errorMessage = "Une équation non orientée ne peut pas avoir un paramètre de type OUTPUT.";
							return !isIt;
						}
						else if (equation.isOriented()) {
							if (!equation.getParametreDeSortie().equals(parametre)) {
								errorMessage = "Vous pouvez pas avoir plusieurs sorties pour une même équation.";
								return !isIt;
							}
						}
					}
					//Verify that if the parameter is an input, it contains a value
					if(parametre.getTypeP().equals(TypeParametre.INPUT) && parametre.getValeurP().isEmpty()){
						errorMessage = "Paramètre de type INPUT doit prendre une valeur.";
						return !isIt;
						
					}
					//If the parameter is not typed Input it can't have a value
				
					if(!parametre.getTypeP().equals(TypeParametre.INPUT) && !parametre.getValeurP().isEmpty()){
						parametre.setValeurP("");
						//errorMessage = "Seulement les paramètres de type INPUT peuvent prendre une valeur.";
						return isIt;
						
					}
					//Verifying that the value of a parameter corresponds with the input type
					if (parametre.getTypeP().equals(TypeParametre.INPUT)) {
						try {
							VerificationValeurParametre.parameterValues(parametre);
						} catch (Exception e) {
							errorMessage = e.toString()+" (Paramètre: "+parametre.getNomP()+" )";							
							return !isIt;


						}
					}
				}
			}
			return isIt;					
		};


	}










}
