package org.ica.uiElements;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.eclipse.core.databinding.Binding;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.beans.BeansObservables;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.core.databinding.observable.map.IMapChangeListener;
import org.eclipse.core.databinding.observable.map.IObservableMap;
import org.eclipse.core.databinding.observable.map.MapChangeEvent;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.jface.databinding.fieldassist.ControlDecorationSupport;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.databinding.viewers.ObservableListContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewerEditor;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationEvent;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationStrategy;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.FocusCellOwnerDrawHighlighter;
import org.eclipse.jface.viewers.ICellEditorListener;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TableViewerEditor;
import org.eclipse.jface.viewers.TableViewerFocusCellManager;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.ica.briquePackage.Equation;
import org.ica.briquePackage.Parametre;
import org.ica.enumeration.SousTypeParametre;
import org.ica.enumeration.TypeParametre;
import org.ica.exceptions.InvalidParameter;
import org.ica.exceptions.MainApplicationException;
import org.ica.uiElements.AfficherEtModifierBriqueContents.VerifyBeforeValidation;
import org.ica.utilityClasses.FactoryTools;
import org.ica.utilityClasses.ObjectFactory;
import org.ica.utilityClasses.VerificationValeurParametre;

/** 
 * 
 * Creates the components which enable the visualisation and the modification of {@code Equation} objects 
 * on the {@link MainApplicationCanvas}
 * @author Salim Ahmed
 */

public class AfficherEtModifierEquation {
	private static final String[] parameterColumns = {"Nom Paramètre","Type Paramètre", "Sous Type","Valeur","Unité","Propriété","Description"} ;

	private static final String[] PARAMETERTYPES = {"INPUT","UNDETERMINED","OUTPUT"};

	private static final String[] INPUT_PARAMETER_SUBTYPE = {"CONSTANT","RANGE","SET"};

	private static final String[] UNDETERMINED_PARAMETER_SUBTYPE = {"INPUT_UNDETERMINED","OUTPUT_UNDETERMINED","FREE"};

	private static final String[] OUTPUT_PARAMETER_SUBTYPE = {"OUTPUT"};

	private static final String[] EMPTY_STRING_ARRAY = {""};

	private final ComboBoxCellEditor typeParametreEditor;
	private final TextCellEditor valeurParametreEditor;
	private final TextCellEditor textEditor;

	private TableViewer parameterTableViewer;
	private Table parameterTable;
	List<Equation> listEquation = new ArrayList<>();
	final private WritableList writableList;
	private final Equation equation;
	/**stores the validated state of the equation contents.*/
	private boolean isValid = true;
	/**Temporary {@code Parametre } object to help monotor 
	 * the currently edited Equation parameter.*/
	private Parametre t_parametre = new Parametre();

	/**
	 * Constructor which displays the contents of an Equation object.
	 * @param composite : Composite on which the equation content will be drawn
	 * @param equation : the Equation object whose content will is to be displayed.
	 */
	public AfficherEtModifierEquation(Composite composite, Equation equation){
		List<Parametre> parametres = equation.getListeDeParametresEqn();
		this.equation = equation;

		writableList = new WritableList(parametres, Parametre.class);
		parameterTableViewer =  new TableViewer(composite, SWT.SINGLE|SWT.FULL_SELECTION);
		parameterTable = parameterTableViewer.getTable();

		ObservableListContentProvider contentProvider =  new ObservableListContentProvider();	
		//Setting the content provider for the table, to contain all of it's data
		parameterTableViewer.setContentProvider(contentProvider);

		createColumns(composite, parameterTableViewer, contentProvider);

		//Setting Input, Must be called after setting content provider
		parameterTableViewer.setInput(writableList);



		FormData fd_eqns_Table = new FormData();
		fd_eqns_Table.right = new FormAttachment(100,0);
		fd_eqns_Table.bottom = new FormAttachment(100,0);
		fd_eqns_Table.top = new FormAttachment(0,0);
		fd_eqns_Table.left = new FormAttachment(0, 0);


		parameterTable.setLayoutData(fd_eqns_Table);
		parameterTable.setHeaderVisible(true);
		parameterTable.setLinesVisible(true);

		MyCustomCellEditorListener modifyListener = new MyCustomCellEditorListener();

		typeParametreEditor = new ComboBoxCellEditor(parameterTable, PARAMETERTYPES,SWT.NONE|SWT.READ_ONLY|SWT.SINGLE);
		typeParametreEditor.addListener(modifyListener);

		valeurParametreEditor = new TextCellEditor(parameterTable,SWT.NONE|SWT.SINGLE);
		valeurParametreEditor.addListener(modifyListener);

		textEditor = new TextCellEditor(parameterTable,SWT.NONE);
		textEditor.addListener(modifyListener);


		//This class is responsible for providing concept calls of cells for Table. This concept is needed to provide features like editor activation with the keyboard
		TableViewerFocusCellManager focusCellManager = new TableViewerFocusCellManager(parameterTableViewer, new FocusCellOwnerDrawHighlighter(parameterTableViewer));



		//This class is responsible for determining if a cell selection event is triggers an editor activation. 
		//I add custom controls to the table to activate editing the table cells: events like Traverse and MOUSE_DOUBLE_CLICK_SELECTION.
		ColumnViewerEditorActivationStrategy actSupport = new ColumnViewerEditorActivationStrategy(parameterTableViewer){
			@Override
			protected boolean isEditorActivationEvent(
					ColumnViewerEditorActivationEvent event) {
				return event.eventType == ColumnViewerEditorActivationEvent.TRAVERSAL
						|| event.eventType == ColumnViewerEditorActivationEvent.MOUSE_DOUBLE_CLICK_SELECTION
						|| (event.eventType == ColumnViewerEditorActivationEvent.KEY_PRESSED && event.keyCode == SWT.CR)
						||  event.eventType == ColumnViewerEditorActivationEvent.PROGRAMMATIC;
			}
		};


		//This is to define custom controls for the table, like using the arrow keys and the tab key to navigate
		TableViewerEditor.create(parameterTableViewer, focusCellManager, actSupport,
				ColumnViewerEditor.TABBING_HORIZONTAL
				|ColumnViewerEditor.TABBING_MOVE_TO_ROW_NEIGHBOR
				|ColumnViewerEditor.TABBING_VERTICAL
				|ColumnViewerEditor.KEEP_EDITOR_ON_DOUBLE_CLICK);
		
		bindBeforeSet(t_parametre, valeurParametreEditor.getControl());	

	}

	/**
	 *  * Refreshes the labels of the parameters
	 * @param parametres : The {@code List} of parameters.
	 */
	

	public void refreshTableContent(List<Parametre> parametres) {
		if(writableList.isDisposed())return;

		writableList.clear();
		writableList.addAll(parametres);

	}
	/**
	 * This method creates the {@link Table } columns with their properties as well as attaching a validator
	 * and {@link CellEditor} support.
	 * @param parent : Parent {@code Composite} on which the {@code Table } is drawn.
	 * @param viewer : Parent {@code Table } which will contain the columns.
	 * @param contentProvider : The {@code ContentProvider}({@link ObservableListContentProvider}) of the  {@code Table }.
	 */
	private void createColumns(final Composite parent, final TableViewer viewer, ObservableListContentProvider contentProvider){


		//first column, non modifiable for showing parameter name

		TableViewerColumn nomPColumn = createTableViewerColumn(parameterColumns[0], viewer);

		IObservableMap mapNomP = BeansObservables.observeMap(contentProvider.getKnownElements(), "nomP");

		nomPColumn.setLabelProvider(new MyCustomLabelProvider(mapNomP) {			
			@Override
			public void update(ViewerCell cell) {
				if (cell.getElement() instanceof Parametre) {
					Parametre parametre = (Parametre)cell.getElement(); 
					StyledString styledString = new StyledString(parametre.getNomP() 
							!= null ? (String) parametre.getNomP() : "");

					cell.setText(styledString.getString());
					cell.setStyleRanges(styledString.getStyleRanges());
				}

			}
		});

		//second column, a combo box for the Type of the parameter.
		TableViewerColumn typePColumn = createTableViewerColumn(parameterColumns[1], viewer);

		IObservableMap mapTypeP = BeansObservables.observeMap(contentProvider.getKnownElements(), "typeP");

		typePColumn.setLabelProvider(new MyCustomLabelProvider(mapTypeP){
			@Override
			public void update(ViewerCell cell) {
				if (cell.getElement() instanceof Parametre) {
					Parametre parametre = (Parametre)cell.getElement(); 
					StyledString styledString = new StyledString(parametre.getTypeP() 
							!= null ? (String) parametre.getTypeP().toString() : "");

					cell.setText(styledString.getString());
					cell.setStyleRanges(styledString.getStyleRanges());
				}

			}

		});

		typePColumn.setEditingSupport(new EditingSupport(viewer) {

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



		//third column for the sub type of the parameter
		TableViewerColumn sousTypePColumn = createTableViewerColumn(parameterColumns[2], viewer);
		IObservableMap mapSubTypeP = BeansObservables.observeMap(contentProvider.getKnownElements(), "sousTypeP");
		sousTypePColumn.setLabelProvider(new MyCustomLabelProvider(mapSubTypeP){
			@Override
			public void update(ViewerCell cell) {
				if (cell.getElement() instanceof Parametre) {
					Parametre parametre = (Parametre)cell.getElement(); 
					StyledString styledString = new StyledString(parametre.getSousTypeP()
							!= null ? (String) parametre.getSousTypeP().toString() : "");

					cell.setText(styledString.getString());
					cell.setStyleRanges(styledString.getStyleRanges());
				}

			}
		});
		sousTypePColumn.setEditingSupport(new EditingSupport(viewer) {

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
						return new ComboBoxCellEditor(parameterTable,INPUT_PARAMETER_SUBTYPE,SWT.NONE|SWT.READ_ONLY|SWT.SINGLE);

					case UNDETERMINED:
						return new ComboBoxCellEditor(parameterTable,UNDETERMINED_PARAMETER_SUBTYPE,SWT.NONE|SWT.READ_ONLY|SWT.SINGLE);

					case OUTPUT:
						return new ComboBoxCellEditor(parameterTable,OUTPUT_PARAMETER_SUBTYPE,SWT.NONE|SWT.READ_ONLY|SWT.SINGLE);

					default:
						break;
					}
				}
				return  new ComboBoxCellEditor(parameterTable,EMPTY_STRING_ARRAY,SWT.NONE|SWT.READ_ONLY|SWT.SINGLE);
			}

			@Override
			protected boolean canEdit(Object element) {
				if (element instanceof Parametre) {
					return true;
				}
				return false;
			}
		});


		//fourth column combobox to select from the list of parameters the valeur de sortie
		TableViewerColumn valeurPColumn = createTableViewerColumn(parameterColumns[3], viewer);
		IObservableMap mapValeurP =  BeansObservables.observeMap(contentProvider.getKnownElements(), "valeurP");
		valeurPColumn.setLabelProvider(new MyCustomLabelProvider(mapValeurP){
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

		valeurPColumn.setEditingSupport(new EditingSupport(viewer) {

			@Override
			protected void setValue(Object element, Object value) {
				if (element instanceof Parametre) {									
					((Parametre)element).setValeurP((String)value);	
					
					//Passing the currently edited Parametre object to the validator
					updateParametre(((Parametre)element),parent);
				}

				getViewer().update(element, null);
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

	

		//fifth column unite du paramètre
		TableViewerColumn uniteColumn = createTableViewerColumn(parameterColumns[4], viewer);
		IObservableMap mapUniteP =  BeansObservables.observeMap(contentProvider.getKnownElements(), "uniteP");
		uniteColumn.setLabelProvider(new MyCustomLabelProvider(mapUniteP){
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


		uniteColumn.setEditingSupport(new EditingSupport(viewer) {

			@Override
			protected void setValue(Object element, Object value) {
				if (element instanceof Parametre) {

					((Parametre)element).setUniteP((String)value);
				}
				getViewer().update(element, null);				
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

		//last column, propriété du paramètre
		TableViewerColumn proprietePColumn = createTableViewerColumn(parameterColumns[5], viewer);
		IObservableMap mapProprieteP =  BeansObservables.observeMap(contentProvider.getKnownElements(), "proprieteP");
		proprietePColumn.setLabelProvider(new MyCustomLabelProvider(mapProprieteP){
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
		proprietePColumn.setEditingSupport(new EditingSupport(viewer) {
			@Override
			protected void setValue(Object element, Object value) {
				if (element instanceof Parametre) {

					((Parametre)element).setProprieteP((String)value);
				}
				getViewer().update(element, null);
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

		//sixth column, description du paramètre
		TableViewerColumn  descriptionColumn = createTableViewerColumn(parameterColumns[6], viewer);
		IObservableMap mapDescP =  BeansObservables.observeMap(contentProvider.getKnownElements(), "descP");

		descriptionColumn.setLabelProvider(new MyCustomLabelProvider(mapDescP){
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
		descriptionColumn.setEditingSupport(new DescriptionEditingSupport(viewer));



	}


	/**
	 * Creates the {@link TableViewerColumn} for the {@code Table} columns. 
	 * @param title : The {@code String} title of the columns
	 * @param viewer : The {@linkplain Viewer} on which to draw the {@code TableViewerColumn} 
	 * @return the newly created {@code TableViewerColumn} object.
	 */

	private TableViewerColumn createTableViewerColumn(String title, TableViewer viewer){
		final TableViewerColumn tableViewerCol =  new TableViewerColumn(viewer,SWT.NONE);
		final TableColumn column = tableViewerCol.getColumn();

		column.setText(title);
		column.setResizable(true);
		column.setMoveable(true);
		column.pack();

		return tableViewerCol;
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
			//	setStatus(ValidationStatus.error("Ne peut pas être vide"));
				return ValidationStatus.error("Ne peut pas être vide");
			}
			try {	
				VerificationValeurParametre.parameterValues(p,(String)param);
			} 
			catch (MainApplicationException e) {
				if (e instanceof InvalidParameter) {
				//	setStatus(ValidationStatus.error(e.toString()));
					return ValidationStatus.error(e.toString());
				}
				else {
				//	setStatus(ValidationStatus.error(e.toString()));
					return ValidationStatus.error(e.toString());
				}
			}
			//setStatus(ValidationStatus.ok());
			return ValidationStatus.ok();

		});

		//Binding the control and the data with the update strategy to use
		Binding bindValue = ctx.bindValue(widget, value,strategy,null);

		//Adding some decoration to indicate to the user the validation status of
		//of the currently typed equation expression
		ControlDecorationSupport.create(bindValue, SWT.TOP|SWT.RIGHT);
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

	}

	/**
	 * This method is called when the validate button is clicked. It verifies the validity 
	 * of the information entered.
	 * @param shell : Attaches dialogs to this {@link Shell} parameter.
	 */
	public void verifyBeforeValidate(Shell shell) {
		Equation equation = getEquation();
		//I reuse the same class methods because it's more convenient, will have to create a 
		//list out of the equation object though.
		VerifyBeforeValidation.verifyBeforeValidate(Arrays.asList(equation), shell);
		setValid(VerifyBeforeValidation.isValid());
	}

	/**
	 * @return the parameterTable
	 */
	public Table getParameterTable() {
		return parameterTable;
	}



	/**
	 * @param parameterTable the parameterTable to set
	 */
	public void setParameterTable(Table parameterTable) {
		this.parameterTable = parameterTable;
	}



	/**
	 * @return the equation
	 */
	public Equation getEquation() {
		return equation;
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
}
