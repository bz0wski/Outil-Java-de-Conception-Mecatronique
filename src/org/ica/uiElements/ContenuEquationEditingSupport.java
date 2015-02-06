package org.ica.uiElements;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.ica.briquePackage.Equation;
import org.ica.exceptions.MainApplicationException;
import org.ica.main.Observer;
import org.ica.main.Subject;
import org.ica.main.ValidationObserver;

public class ContenuEquationEditingSupport extends EditingSupport implements Subject,ValidationObserver {
	private final TableViewer viewer;
	private static CellEditor cellEditor;
	private final Map<Integer, String> exportEquations;
	private static int tableItemCount;
	private static TableViewer tempViewer; 
	private IStatus status;
	
	final private List<Observer> listeners = new ArrayList<Observer>();
	private Map<Integer, IStatus> tableEquationValidationStatus;

	public ContenuEquationEditingSupport(ColumnViewer viewer) {
		super(viewer);


		this.viewer = (TableViewer) viewer;
		setTempViewer(this.viewer);
		
		cellEditor = new TextCellEditor(((TableViewer) viewer).getTable(), SWT.NONE);
		this.exportEquations = new HashMap<>();
		tableEquationValidationStatus = new HashMap<>();
		cellEditor.getControl().addListener(SWT.FocusIn, listener->{
		});
		
		cellEditor.getControl().addListener(SWT.FocusOut, listener->{
			tableItemCount = (Integer)Arrays.asList(((TableViewer)viewer).getTable().getItems()).size();
			int tableSelectionIndex = ((TableViewer)viewer).getTable().getSelectionIndex();
			String string = ((TableViewer)viewer).getSelection().toString();
			
			/*
			//In the first part of this block of code i check the Map to see if the currently selected table index
			//is stored in the map, if it is not, I extract the equation statement to be passed to
			// the tablecellcomboboxeditor and place it in the Map with its Key as the table index;
			//In the second part, if the Map already has the index, I replace the equation with
			// the latest update and I notify all the observers of the change. 
			 //* I use the same mechanism to verify the validation status of the equation statement, 
			 //* but in the second case I'm observing the status from the AddEquation class. */
			
			if (!exportEquations.containsKey(tableSelectionIndex) && string != null) 
				exportEquations.put( tableSelectionIndex, string.substring(1, string.length()-1));
			
			else if (exportEquations.containsKey(tableSelectionIndex)) {
				exportEquations.replace(tableSelectionIndex, (String)string.substring(1, string.length()-1));
			}
			
			
			if (!tableEquationValidationStatus.containsKey(tableSelectionIndex) && status != null) 
				tableEquationValidationStatus.put( tableSelectionIndex, status);
			
			else if (tableEquationValidationStatus.containsKey(tableSelectionIndex)) {
				tableEquationValidationStatus.replace(tableSelectionIndex, status);
			}
			notifyObserver();
		});


	}

	@Override
	protected CellEditor getCellEditor(Object element) {
		return cellEditor;
	}
	@Override
	protected boolean canEdit(Object element) {

		return true;
	}
	@Override
	protected Object getValue(Object element) {
		return ((Equation) element).getContenuEqn();
	}


	@Override
	protected void setValue(Object element, Object value) {
		
			((Equation) element).setContenuEqn(String.valueOf(value));
	
		try {			 			
			((Equation)element).getListeDeParametresEqn_DYNAMIC();
		} catch (MainApplicationException e) {
			e.printStackTrace();
		}
		viewer.update(element, null);
		

	}


	@Override
	public void register(Observer o) {
		listeners.add(o);
	}


	@Override
	public void unregister(Observer o) {
		listeners.remove(o);

	}


	@Override
	public void notifyObserver() {
		for (Observer o : listeners) {
			o.update(tableItemCount,exportEquations);
		}

	}
	@Override
	public void update(IStatus status) {
		this.status = status;
	}
	
	public TableViewer getViewer(){
		return this.viewer;
	}
	

	public static CellEditor getCellEditor() {
		
		return cellEditor;
	}

	public Map<Integer, IStatus> getTableEquationValidationStatus() {
		return tableEquationValidationStatus;
	}

	public void setTableEquationValidationStatus(
			Map<Integer, IStatus> tableEquationValidationStatus) {
		this.tableEquationValidationStatus = tableEquationValidationStatus;
	}

	/**
	 * @return the tempViewer
	 */
	public static TableViewer getTempViewer() {
		return tempViewer;
	}

	/**
	 * @param tempViewer the tempViewer to set
	 */
	public static void setTempViewer(TableViewer tempViewer) {
		ContenuEquationEditingSupport.tempViewer = tempViewer;
	}

}
