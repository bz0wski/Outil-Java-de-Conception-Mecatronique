package org.ica.uiElements;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.databinding.Binding;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.databinding.fieldassist.ControlDecorationSupport;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.databinding.viewers.ObservableListContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewerEditor;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationEvent;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationStrategy;
import org.eclipse.jface.viewers.FocusCellOwnerDrawHighlighter;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TableViewerEditor;
import org.eclipse.jface.viewers.TableViewerFocusCellManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.ica.briquePackage.Equation;
import org.ica.exceptions.EquationVide;
import org.ica.exceptions.ErreurDeSyntaxe;
import org.ica.main.ValidationObserver;
import org.ica.main.ValidationSubject;
import org.ica.utilityClasses.ObjectFactory;
import org.ica.utilityClasses.VerificationSyntaxeEquation;


/**
 * This class is responsible for creating the UI components for
 *  adding new {@code Equation } objects to an existing {@code Brique}
 * object.
 * @author Salim Ahmed
 *
 */

public class AddEquation implements ValidationSubject{
	
	private static final String[] EquationColumnNames = {"Contrainte","Contenu Equation","Orientation","Parametre de Sortie","Proprieté Equation","Description"} ;
	
	private final TableViewer equationTableViewer;
	
	private  Table equationTable;
	
	List<Equation> listEquation = new ArrayList<>();
	final private WritableList writableList;
		
	private Equation equation;
	private IStatus status;
	private Map<Integer, IStatus> tableEquationValidationStatus;
	private boolean validatedAtLeastOnce = false;
	List<ValidationObserver> listeners = new ArrayList<>();
	
	public AddEquation(Composite parent) throws Exception {
		
		equationTableViewer = new TableViewer(parent, SWT.SINGLE|SWT.FULL_SELECTION|SWT.BORDER);
		createColumns(parent, equationTableViewer);
		equationTable = equationTableViewer.getTable();
		
		this.equation = createEquation();
		
		//writablelist to contain the table input, backed by an ArrayList
		writableList = new WritableList(listEquation, Equation.class);
				
		ObservableListContentProvider contentProvider =  new ObservableListContentProvider();
		
		//Setting the content provider for the table, to contain all of it's data
		equationTableViewer.setContentProvider(contentProvider);
		
		//Setting Input, Must be called after setting content provider
		//I just added this 
		equationTableViewer.setInput(writableList);
		
		
		equationTable.addListener(SWT.Traverse, listener->{
			if (listener.detail == SWT.TRAVERSE_RETURN) {								 
				 try {
					 Equation equation = createEquation();
					getInputList().add(equation);
					equationTableViewer.insert(equation, -1);
				} catch (Exception e) {
				
					e.printStackTrace();
				}
				 
			}
		});
		
		
		/**I'm adding a modify listener to all the cell editors which gets fired once the Equation has been
		 * validated atleast once. After it is fired, every time an edit is made to any of the cells of the
		 * table, it replaces the value of equationValidationOk in the validationRegistry Map with false*/
		Listener listener = (event ->{
			if(isValidatedAtLeastOnce()){
				//System.out.println("editor activated after atleast one validation call.");
				 ObjectFactory.validationRegistry().replace("equationValidationOk", false);
			}
		}); 
		
				
		equationTableViewer.getTable().addListener(SWT.Selection, listener);
		
		FormData fd_eqns_Table = new FormData();
		fd_eqns_Table.right = new FormAttachment(100,0);
		
		//fd_eqns_Table.left = new FormAttachment(100, 10);
		fd_eqns_Table.top = new FormAttachment(0,0);
		fd_eqns_Table.left = new FormAttachment(0, 0);
		fd_eqns_Table.bottom = new FormAttachment(100, -60);
		
		equationTable.setLayoutData(fd_eqns_Table);
		equationTable.setHeaderVisible(true);
		equationTable.setLinesVisible(true);
		
		
		
		
		//This class is responsible to provide the concept of cells for Table. This concept is needed to provide features like editor activation with the keyboard
		TableViewerFocusCellManager focusCellManager = new TableViewerFocusCellManager(equationTableViewer, new FocusCellOwnerDrawHighlighter(equationTableViewer));
		
		
		
		//This class is responsible to determine if a cell selection event is triggers an editor activation. 
		//I add custom controls to the table to activate editing the table cells: events like Traverse and MOUSE_DOUBLE_CLICK_SELECTION.
		ColumnViewerEditorActivationStrategy actSupport = new ColumnViewerEditorActivationStrategy(equationTableViewer){
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
		TableViewerEditor.create(equationTableViewer, focusCellManager, actSupport,
				ColumnViewerEditor.TABBING_HORIZONTAL
				|ColumnViewerEditor.TABBING_MOVE_TO_ROW_NEIGHBOR
				|ColumnViewerEditor.TABBING_VERTICAL
				|ColumnViewerEditor.KEEP_EDITOR_ON_DOUBLE_CLICK);
		
		//In this sub routine I bind the values to the respective controls in order to observer changes 
		//and verify the data insertion
		bindValues();
	}

	/**
	 * In this sub routine I bind the values to the respective controls in order to observer changes 
		and verify the data insertion*/
	
	private void bindValues(){
		
		//the DatabindingContext object will manage the databindings
		//In this first part I monitor the the equation expression and check its
		//conformity to the correct syntaxe of an equation.
		 DataBindingContext ctx = new DataBindingContext();
			IObservableValue widgetValue = WidgetProperties.text(SWT.Modify).observe(ContenuEquationEditingSupport.getCellEditor().getControl());
			IObservableValue modelValue = BeanProperties.value(Equation.class, "contenuEqn").observe(equation);
			//Defining the update strategy
			UpdateValueStrategy strategy = new UpdateValueStrategy();
			
			//Defining the Update Strategy the field validator should use, this validation is passed before the value can be set.
			strategy.setBeforeSetValidator(value->{
				String eqnString = String.valueOf(value);
				try {
					new VerificationSyntaxeEquation(eqnString);
				} catch (Exception e) {
					if (e instanceof ErreurDeSyntaxe){
						status = ValidationStatus.error(e.toString());
						notifyObserver();
						return status;
					}
					else if (e instanceof EquationVide) {
						status = ValidationStatus.error(e.toString());
						notifyObserver();
						return status;
					}
				}
				status = ValidationStatus.ok();
				notifyObserver();
				return status;
			});
			//Binding the control and the data with the update strategy to use
			Binding bindValue = ctx.bindValue(widgetValue, modelValue,strategy,null);
			
			//Adding some decoration to indicate to the user the validation status of
			//of the currently typed equation expression
			ControlDecorationSupport.create(bindValue, SWT.TOP|SWT.LEFT);
			
			//In this second part I aim to monitor the the equation expression and check its parameter names'
			//conformity to the correct syntaxe of a parameter name before showing it in the combobox.
			
	}
	
	/**Create a new Equation instance
	 * @return A new Equation object*/
	private Equation createEquation() throws Exception {
		Equation equation = new Equation(false,
									"a+b=c", 
									false, 
									null, 
									"Propriété",
									"Insérer commentaire");
		equation.setIsModifiable(false);
		return equation;
	}
	
	public List<Equation> getInputList() {
		return listEquation;
	}
	
	
/**
 * Creates table columns for the table.
 * @param parent : Parent composite on which to attach new column.
 * @param viewer : {@link TableViewer} on which to attach new column.
 */
	private void createColumns(final Composite parent, final TableViewer viewer){
		
		
		//first column, a checkbox for whether equation is a constraint or not
		TableViewerColumn contrainteColumn = createTableViewerColumn(EquationColumnNames[0], 100, 0);

		contrainteColumn.setLabelProvider(new EmulatedNativeCheckBoxLabelProvider(equationTableViewer) {
			
			@Override
			protected boolean isChecked(Object element) {
				if (((Equation)element).isConstraint()) {
					return true;
				}
				return false;
			}
			@Override
			public String getText(Object element) {
			
				return null;
			}
		});
		contrainteColumn.setEditingSupport(new IsContrainteEditingSupport(viewer));
			
		
		
		//second column, text field for the equation expression
		TableViewerColumn contenuEquationColumn = createTableViewerColumn(EquationColumnNames[1], 100, 0);
		contenuEquationColumn.setLabelProvider(new ColumnLabelProvider(){
			@Override
			public String getText(Object element) {
				Equation eqn = (Equation) element;
				return eqn.getContenuEqn();
			}
		});
		ContenuEquationEditingSupport contenuEES = new ContenuEquationEditingSupport(viewer);
		//Registering <code>ContenuEquationEditingSupport</code> to notify it 
		//about the validation status of the currently edited function.
		register(contenuEES);
		
		contenuEquationColumn.setEditingSupport(contenuEES);
		tableEquationValidationStatus = contenuEES.getTableEquationValidationStatus();
		
		

		//third column for the orientation of the function
		TableViewerColumn orientationColumn = createTableViewerColumn(EquationColumnNames[2], 100, 0);
		 orientationColumn.setLabelProvider(new EmulatedNativeCheckBoxLabelProvider(equationTableViewer){
				@Override
				protected boolean isChecked(Object element) {
					if (((Equation)element).isOriented()) {
						return true;
					}
					return false;
				}
				@Override
				public String getText(Object element) {
						return "";
				}
			});
			orientationColumn.setEditingSupport(new OrientationEditingSupport(viewer));
		
		
		//fourth column combobox to select from the list of parameters the valeur de sortie
		TableViewerColumn valeurDeSortieColumn = createTableViewerColumn(EquationColumnNames[3], 100, 0);
		valeurDeSortieColumn.setLabelProvider(new ColumnLabelProvider(){
			@Override
			public String getText(Object element) {
				Equation eqn = (Equation) element;
				if (eqn.getParametreDeSortie()!= null) {
					return eqn.getParametreDeSortie().getNomP();
				}
				
				return "";
			}

		});
		
		ParametreDeSortieEditingSupport sortieEditingSupport = new ParametreDeSortieEditingSupport(viewer);
		valeurDeSortieColumn.setEditingSupport(sortieEditingSupport);
		contenuEES.register(sortieEditingSupport);
		
		
		
		//fifth propriété de l'équation(ex: loi d'echelle/regression polynomiale)
		TableViewerColumn proprieteColumn = createTableViewerColumn(EquationColumnNames[4], 100, 0);
		proprieteColumn.setLabelProvider(new ColumnLabelProvider(){
			@Override
			public String getText(Object element) {
				Equation eqn = (Equation) element;
				return eqn.getProprieteEqn();
			}
		});
		
		
		proprieteColumn.setEditingSupport(new DescriptionEditingSupport(viewer));
		
		//sixth column, description de l'équation
		TableViewerColumn descriptionColumn = createTableViewerColumn(EquationColumnNames[5], 100, 0);
		descriptionColumn.setLabelProvider(new ColumnLabelProvider(){
			@Override
			public String getText(Object element) {
				Equation eqn = (Equation) element;
				return eqn.getDescEqn();
			}
		});
		descriptionColumn.setEditingSupport(new DescriptionEditingSupport(viewer));
	}
	
	

/**
 * Returns a newly created {@link TableViewerColumn}
 * @param title : Title of the column.
 * @param width : WIdth of the column.
 * @param colNumber : Index of the column.
 * @return Newly created Table column.
 */
	private TableViewerColumn createTableViewerColumn(String title, int width, final int colNumber){
		final TableViewerColumn tableViewerCol =  new TableViewerColumn(equationTableViewer,SWT.NONE);
		final TableColumn column = tableViewerCol.getColumn();
		
		column.setText(title);
		column.setWidth(width);
		column.setResizable(true);
		column.setMoveable(true);
		column.pack();

		return tableViewerCol;
	}
	

	@Override
	public void register(ValidationObserver o) {
		listeners.add(o);
		
	}

	@Override
	public void unregister(ValidationObserver o) {
		listeners.remove(o);
	}

	@Override
	public void notifyObserver() {
		
		for (ValidationObserver vo : listeners) {
			vo.update(status);
		}
		
	}

	
	public void setFocus(){
		equationTableViewer.getControl().setFocus();
	}
	
	public Map<Integer, IStatus> getTableEquationValidationStatus() {
		return tableEquationValidationStatus;
	}
	
	public Table getTable() {
		return equationTable;
	}
	
	public TableViewer getEquationTableViewer() {
		return this.equationTableViewer;
	}



	/**
	 * @return the validatedAtLeastOnce
	 */
	public boolean isValidatedAtLeastOnce() {
		return validatedAtLeastOnce;
	}



	/**
	 * @param validatedAtLeastOnce the validatedAtLeastOnce to set
	 */
	public void setValidatedAtLeastOnce(boolean validatedAtLeastOnce) {
		this.validatedAtLeastOnce = validatedAtLeastOnce;
	}



	
}
