package org.ica.uiElements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.databinding.fieldassist.ControlDecorationSupport;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.databinding.viewers.ObservableListTreeContentProvider;
import org.eclipse.jface.databinding.viewers.TreeStructureAdvisor;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.ica.briquePackage.Equation;
import org.ica.briquePackage.Parametre;
import org.ica.enumeration.SousTypeParametre;
import org.ica.enumeration.TypeParametre;
import org.ica.exceptions.InvalidParameter;
import org.ica.exceptions.MainApplicationException;
import org.ica.utilityClasses.VerificationValeurParametre;

/**
 * This class is responsible for creating the UI components for
 *  modifying {@code Equation } contents.
 * 
 * @author Salim Ahmed
 *
 */

public class AddParameterContent {

	private static final String[] parameterColumnNames = {"Equation","Nom Paramètre","Type Paramètre", "Sous Type","Valeur","Unité","Propriété","Description"} ;

	private static final String[] PARAMETERTYPES = {"INPUT","UNDETERMINED","OUTPUT"};

	private static final String[] INPUT_PARAMETER_SUBTYPE = {"CONSTANT","RANGE","SET"};

	private static final String[] UNDETERMINED_PARAMETER_SUBTYPE = {"INPUT_UNDETERMINED","OUTPUT_UNDETERMINED","FREE"};

	private static final String[] OUTPUT_PARAMETER_SUBTYPE = {"OUTPUT"};

	private static final String[] EMPTY_STRING_ARRAY = {""};

	private final TextCellEditor textEditor;
	private final TextCellEditor valeurParametreEditor;
	private final ComboBoxCellEditor typeParametreEditor;

	private final WritableList writableListEquations; 
	private final List<Equation> eqnList = new ArrayList<>();


	//Serves as both the Parametre object to observe and the temp Parametre object to verify the entries. 
	private Parametre t_parametre = new Parametre();
	private Map<Long, IStatus> treeParameterValidationStatus;
	private final TreeViewer parameterTreeViewer;
	private IStatus status;
/**
 * Creates the UI components for the {@code AddParameter}
 * @param addParameter : the {@code AddParameter } object
 * @param parent : parent {@code Composite}
 */
	public AddParameterContent(AddParameter addParameter, Composite parent) {	
		parameterTreeViewer = addParameter.getTreeViewer();
		//Setting the content provider for the table, to contain all of it's data
		
		ObservableListTreeContentProvider contentProvider = new ObservableListTreeContentProvider(new MyTreeListFactory(),new MyTreeStructureAdvisor());

		parameterTreeViewer.setContentProvider(contentProvider);		


		textEditor = new TextCellEditor(addParameter.getTreeViewer().getTree(),SWT.NONE);
		valeurParametreEditor = new TextCellEditor(addParameter.getTreeViewer().getTree(),SWT.NONE);
		typeParametreEditor = new ComboBoxCellEditor(addParameter.getTreeViewer().getTree(), PARAMETERTYPES,SWT.NONE|SWT.READ_ONLY|SWT.SINGLE);

		treeParameterValidationStatus = new HashMap<Long, IStatus>();

		writableListEquations = new WritableList(eqnList,Equation.class);

		//Setting Input, Must be called after setting content provider
		parameterTreeViewer.setInput(writableListEquations);

		//clear out the list when the window is disposed
		addParameter.getTree().addDisposeListener(listener->{
			eqnList.clear();
		});
		//////////////////////////////////////////////////////DEFINING COLUMNS////////////////////////////////////////////////////////////////


		/////////////////////////////////////////// Column nomEquation /////////////////////////////////////////////////////////////////////////////////////////////////////		
		final TreeViewerColumn nomEquation = new TreeViewerColumn(addParameter.getTreeViewer(), SWT.NONE);
		nomEquation.getColumn().setWidth(100);
		nomEquation.getColumn().setMoveable(true);
		nomEquation.getColumn().setResizable(true);
		nomEquation.getColumn().setText(parameterColumnNames[0]);
		nomEquation.getColumn().pack();
		
		IObservableMap map = BeansObservables.observeMap(contentProvider.getKnownElements(), "contenuEqn");


		nomEquation.setLabelProvider(new MyCustomLabelProvider(map) {

			@Override
			public void update(ViewerCell cell) {
				super.update(cell);
				System.err.println("CELL 1 TYPE "+cell.getElement().getClass());
				if (cell.getElement() instanceof Equation) {
					Equation equation = (Equation)cell.getElement(); 
					StyledString styledString = new StyledString(equation.getContenuEqn() 
							!= null ? (String) equation.getContenuEqn() : "");

					cell.setText(styledString.getString());
					cell.setStyleRanges(styledString.getStyleRanges());
				}
			}
		});




		nomEquation.setEditingSupport(new EditingSupport(addParameter.getTreeViewer()) {

			@Override
			protected void setValue(Object element, Object value) {
				if (element instanceof Equation) {
					
						((Equation)element).setContenuEqn((String)value);
					
				}
				addParameter.getTreeViewer().update(element, null);
			}

			@Override
			protected Object getValue(Object element) {
				if (element instanceof Equation) {
					return ((Equation)element).getContenuEqn();
				}
				else
					return "";
			}

			@Override
			protected CellEditor getCellEditor(Object element) {
				return textEditor;
			}

			@Override
			protected boolean canEdit(Object element) {
				return false;
			}
		});

		////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


		////////////////////////////////////Column nomParametre /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////		
		final TreeViewerColumn nomParametre = new TreeViewerColumn(addParameter.getTreeViewer(), SWT.NONE);
		nomParametre.getColumn().setMoveable(true);
		nomParametre.getColumn().setResizable(true);
		nomParametre.getColumn().setMoveable(true);
		nomParametre.getColumn().setWidth(100);
		nomParametre.getColumn().setText(parameterColumnNames[1]);
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


		nomParametre.setEditingSupport(new EditingSupport(addParameter.getTreeViewer()){

			@Override
			protected void setValue(Object element, Object value) {
				if (element instanceof Parametre) {
					((Parametre)element).setNomP((String)value);
				}
				getViewer().update(element, null);
			}

			@Override
			protected Object getValue(Object element) {
				if (element instanceof Parametre) {
					return ((Parametre)element).getNomP();
				}
				return " ";
			}

			@Override
			protected CellEditor getCellEditor(Object element) {
				return textEditor;
			}

			@Override
			protected boolean canEdit(Object element) {

				return false;
			}
		});
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

///////////////////////////// TYPE PARAMETRE ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////		



		final TreeViewerColumn typeParametre = new TreeViewerColumn(addParameter.getTreeViewer(), SWT.NONE);

		typeParametre.getColumn().setMoveable(true);
		typeParametre.getColumn().setResizable(true);
		typeParametre.getColumn().setMoveable(true);
		typeParametre.getColumn().setWidth(100);
		typeParametre.getColumn().setText(parameterColumnNames[2]);
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


		typeParametre.setEditingSupport(new EditingSupport(addParameter.getTreeViewer()) {

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
				refreshParameterValues(eqnList);
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


		//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

		/////////////////////////////////////////////// Column sousTypeParametre ///////////////////////////////////////////////////////////////////////////////////////////////	

		final TreeViewerColumn sousTypeParametre = new TreeViewerColumn(addParameter.getTreeViewer(), SWT.NONE);

		sousTypeParametre.getColumn().setMoveable(true);
		sousTypeParametre.getColumn().setResizable(true);
		sousTypeParametre.getColumn().setMoveable(true);
		sousTypeParametre.getColumn().setWidth(100);
		sousTypeParametre.getColumn().setText(parameterColumnNames[3]);
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

		sousTypeParametre.setEditingSupport(new EditingSupport(addParameter.getTreeViewer()) {

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
				refreshParameterValues(eqnList);
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
						return new ComboBoxCellEditor(addParameter.getTreeViewer().getTree(),INPUT_PARAMETER_SUBTYPE,SWT.NONE|SWT.READ_ONLY|SWT.SINGLE);

					case UNDETERMINED:
						return new ComboBoxCellEditor(addParameter.getTreeViewer().getTree(),UNDETERMINED_PARAMETER_SUBTYPE,SWT.NONE|SWT.READ_ONLY|SWT.SINGLE);

					case OUTPUT:
						return new ComboBoxCellEditor(addParameter.getTreeViewer().getTree(),OUTPUT_PARAMETER_SUBTYPE,SWT.NONE|SWT.READ_ONLY|SWT.SINGLE);

					default:
						break;
					}
				}
				return  new ComboBoxCellEditor(addParameter.getTreeViewer().getTree(),EMPTY_STRING_ARRAY,SWT.NONE|SWT.READ_ONLY|SWT.SINGLE);
			}

			@Override
			protected boolean canEdit(Object element) {
				if (element instanceof Parametre) {
					return true;
				}
				return false;
			}
		});

/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

/////////////////////////////////////////////Column valeurParametre ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////	

		final TreeViewerColumn valeurParametre = new TreeViewerColumn(addParameter.getTreeViewer(), SWT.NONE);

		valeurParametre.getColumn().setMoveable(true);
		valeurParametre.getColumn().setResizable(true);
		valeurParametre.getColumn().setMoveable(true);
		valeurParametre.getColumn().setWidth(100);
		valeurParametre.getColumn().setText(parameterColumnNames[4]);
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

		valeurParametre.setEditingSupport(new EditingSupport(addParameter.getTreeViewer()) {
			@Override
			protected void setValue(Object element, Object value) {
				if (element instanceof Parametre) {	
					//Passing the currently edited Parametre object to the validator
					updateParametre(((Parametre)element),parent);
					((Parametre)element).setValeurP((String)value);	

				}

				getViewer().update(element, null);
				refreshParameterValues(eqnList);
			}

			@Override
			protected Object getValue(Object element) {
				if (element instanceof Parametre) {
					//Passing the currently edited Parametre object to the validator
					updateParametre(((Parametre)element),parent);
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


////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

////////////////////////////////////////////////////////Column uniteParametre////////////////////////////////////////////////////////////////////////////////

		final TreeViewerColumn uniteParametre = new TreeViewerColumn(addParameter.getTreeViewer(), SWT.NONE);

		uniteParametre.getColumn().setMoveable(true);
		uniteParametre.getColumn().setResizable(true);
		uniteParametre.getColumn().setMoveable(true);
		uniteParametre.getColumn().setText(parameterColumnNames[5]);
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

		uniteParametre.setEditingSupport(new EditingSupport(parameterTreeViewer) {

			@Override
			protected void setValue(Object element, Object value) {
				if (element instanceof Parametre) {

					((Parametre)element).setUniteP((String)value);
				}
				getViewer().update(element, null);
				refreshParameterValues(eqnList);
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
				return true;
			}
		});
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////




////////////////////////////////////////////Column proprieteParametre ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		final TreeViewerColumn proprieteParametre = new TreeViewerColumn(addParameter.getTreeViewer(), SWT.NONE);

		proprieteParametre.getColumn().setMoveable(true);
		proprieteParametre.getColumn().setResizable(true);
		proprieteParametre.getColumn().setMoveable(true);
		proprieteParametre.getColumn().setText(parameterColumnNames[6]);
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

		proprieteParametre.setEditingSupport(new EditingSupport(parameterTreeViewer) {

			@Override
			protected void setValue(Object element, Object value) {
				if (element instanceof Parametre) {

					((Parametre)element).setProprieteP((String)value);
				}
				getViewer().update(element, null);
				refreshParameterValues(eqnList);
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
		final TreeViewerColumn descParametre = new TreeViewerColumn(addParameter.getTreeViewer(), SWT.NONE);

		descParametre.getColumn().setMoveable(true);
		descParametre.getColumn().setResizable(true);
		descParametre.getColumn().setMoveable(true);
		descParametre.getColumn().setText(parameterColumnNames[7]);
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
		descParametre.setEditingSupport(new EditingSupport(addParameter.getTreeViewer()) {

			@Override
			protected void setValue(Object element, Object value) {
				if (element instanceof Parametre) {

					((Parametre)element).setDescP((String)value);
				}
				getViewer().update(element, null);
				refreshParameterValues(eqnList);
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



		bindBeforeSet(t_parametre, valeurParametreEditor.getControl());	

///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////	
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////	
///////////////////////////////////////////// END OF CONSTRUCTOR //////////////////////////////////////////////////////////////////////////////////////	

	}


	private class MyCustomLabelProvider extends StyledCellLabelProvider {

		@Override
		public void update(ViewerCell cell) {
			super.update(cell);
		}

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


/////////////////////////////////////////////BINDING AND VALIDATING VALUE DATA///////////////////////////////////////////////////////////////////////////////////////////////	

	/**I use this function to constantly get the currently active object by calling it from 
	 *  the getValue subroutine in the editing support class.
	 * 
	 * @param p : The {@link Parametre } object to copy.
	 * @param parent : Parent {@link Composite} on which to attach the error messages.
	 */
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

	/**
	 * This method binds the {@code Control} to the data inserted to a validator. 
	 * @param p : the {@code Parametre} object to  monitor.
	 * @param celleditor : The {@link Control} to attach the validator to.
	 */
	private void bindBeforeSet(Parametre p,Control celleditor){
		//the DatabindingContext object will manage the databindings
		//In this first part I monitor the the equation expression and check its
		//conformity to the correct syntaxe of the value to be given to the parameter based on its attributes.
		DataBindingContext ctx = new DataBindingContext();
		IObservableValue widget = WidgetProperties.text(SWT.Modify).observe(celleditor);
		IObservableValue value = BeanProperties.value(Parametre.class, "valeurP").observe(p);

		UpdateValueStrategy strategy = new UpdateValueStrategy();

		strategy.setBeforeSetValidator(param->{		
			if (((String)param).isEmpty()) {
				setStatus(ValidationStatus.error("Ne peut pas être vide"));
				return ValidationStatus.error("Ne peut pas être vide");
			}
			try {	
				VerificationValeurParametre.parameterValues(p,(String)param);
			} 
			catch (MainApplicationException e) {
				if (e instanceof InvalidParameter) {
					setStatus(ValidationStatus.error(e.toString()));
					return ValidationStatus.error(e.toString());
				}
				else {
					setStatus(ValidationStatus.error(e.toString()));
					return ValidationStatus.error(e.toString());
				}
			}
			setStatus(ValidationStatus.ok());
			return ValidationStatus.ok();

		});

		//Binding the control and the data with the update strategy to use
		Binding bindValue = ctx.bindValue(widget, value,strategy,null);

		//Adding some decoration to indicate to the user the validation status of
		//of the currently typed equation expression
		ControlDecorationSupport.create(bindValue, SWT.TOP|SWT.RIGHT);
	}


	private class MyTreeListFactory implements IObservableFactory {

		@Override
		public IObservable createObservable(Object target) {
			System.err.println("TARGET "+target.getClass()+"\n"+target);
			
			if (target instanceof Equation) {
				return BeansObservables.observeList(Realm.getDefault(), target, "listeDeParametresEqn");

			}
			if (target instanceof IObservableList) {
				return (IObservableList)target;
			}	

			if (target instanceof List<?>) {
				return new WritableList(((List<?>)target), Equation.class);
			}
			if (target instanceof WritableList) {
				return ((WritableList)target);
			}


			return new EmptyObservableList(Realm.getDefault(),null);
		}}




	private class MyTreeStructureAdvisor extends TreeStructureAdvisor {

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

			if (element instanceof Equation) {					
				return ((Equation)element).getListeDeParametresEqn().size() > 0;

			}
			if (element instanceof List<?>) {
				return ((List<?>)element).size()>0;

			}

			return false;
		}

	}

	/**
	 * This method refreshes the display of all the values in the control.
	 * NB: using the {@code refresh()} method without a parameter causes the 
	 * program to crash. It's an unresolved bug in the {@code SWT} code.
	 * @param equations : The {@code List} of objects whose display to update.
	 */
	private void refreshParameterValues(List<Equation> equations){
		Display.getDefault().asyncExec(()->{
			for (Equation equation : equations) {
				parameterTreeViewer.refresh(equation);
			}
		});
		
	}
	
	/**
	 * This method loads the {@code List} received into the tree.
	 * @param eqn : The {@link List } of {@code Equation } objects to load 
	 * into the tree
	 * @throws MainApplicationException {@code MainApplicationException}
	 */
	public void reloadData(List<Equation> eqn) throws MainApplicationException {
		
		Set<Equation> eqnSet = new HashSet<>();
		
		eqnSet.addAll(eqn);
		
		this.eqnList.addAll(new ArrayList<>(eqnSet));
				
		
		parameterTreeViewer.refresh(eqnList);
	}
	/**
	 * 
	 * @return the {@code treeParameterValidationStatus}
	 */

	public Map<Long, IStatus> getTreeParameterValidationStatus() {
		return treeParameterValidationStatus;
	}
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * 
	 * @return the {@link List} of {@code Equation} objects
	 */
	public List<Equation> getEquationList() {	
		return eqnList;
	}
	/**
	 *Clear the {@code eqnList}
	 */
	public void clearEquationList() {	
		 eqnList.clear();
	/*	 if(!writableListEquations.isDisposed())
			for (Object bObject : writableListEquations) {
				 writableListEquations.remove(bObject);
			}
			System.err.println("clearing lists..");*/
	}
	/**
	 * 
	 * @return the status
	 */
	public IStatus getStatus() {
		return status;
	}


	/**
	 * 
	 * @param status : The status to set
	 */
	public void setStatus(IStatus status) {
		this.status = status;
	}

	/*
	 * 		
	private Object[] equationParameterList(Object parentElement) throws Exception {
		return ((Equation) parentElement).getListeDeParametresEqn().toArray();
			}
/*	
	private class MyContentProvider implements ITreeContentProvider{

		@Override
		public void dispose() {
		}

		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {	
		}

		@Override
		public Object[] getElements(Object inputElement) {
			if (inputElement instanceof List<?>) {
				return writableListEquations.toArray();
			}
			if (inputElement instanceof Equation) {
				try {
					return equationParameterList(inputElement);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			return EMPTY_ARRAY;
		}

		@Override
		public Object[] getChildren(Object parentElement) {
			if (parentElement instanceof Equation) {
				try {
					return ((Equation) parentElement).getListeDeParametresEqn().toArray();
				} catch (Exception e) {	
					e.printStackTrace();
				}
				return EMPTY_ARRAY;
			}


			if (parentElement instanceof IObservableList) {
				return ((IObservableList)parentElement).toArray();

			}
			if (parentElement instanceof List<?>) {
				return ((List<?>)parentElement).toArray();
			}
			parameterTreeViewer.update(parentElement, null);
			parameterTreeViewer.refresh();
			return EMPTY_ARRAY;
		}

		@Override
		public Object getParent(Object element) {

			return null;
		}

		@Override
		public boolean hasChildren(Object element) {
			if (element instanceof Map<?, ?>) {
				return ((Map<?, ?>)element).size() > 0;
			}

			if (element instanceof Equation) {
				try {
					return ((Equation)element).getListeDeParametresEqn().size() > 0;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			if (element instanceof List<?>) {
				return ((List<?>)element).size()>0;

			}

			return false;
		}

	}
	 */

}


