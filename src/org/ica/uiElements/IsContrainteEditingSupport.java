package org.ica.uiElements;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.ica.briquePackage.Equation;
public class IsContrainteEditingSupport extends EditingSupport {

	final TableViewer viewer;
	static CellEditor editor;
	//final String[] isConstraint = {"Contrainte", "Non Contrainte"}; 
	
	public IsContrainteEditingSupport(ColumnViewer viewer) {
		super(viewer);
		this.viewer = (TableViewer)viewer;
		editor = new CheckboxCellEditor(((TableViewer) viewer).getTable(),SWT.CHECK );
		editor.getLayoutData().horizontalAlignment = SWT.CENTER;
		editor.getLayoutData().grabHorizontal = false;
		editor.setStyle(SWT.CENTER);

	
		
		
	}

	@Override
	protected CellEditor getCellEditor(Object element) {
		return editor;
	}

	@Override
	protected boolean canEdit(Object element) {
		return true;
	}

	@Override
	protected Object getValue(Object element) {
		return Boolean.valueOf(((Equation)element).isConstraint());
	
	}

	@Override
	protected void setValue(Object element, Object value) {
	//	System.out.println("String value "+String.valueOf(value));
		((Equation)element).setIsConstraint(Boolean.valueOf(String.valueOf(value)));
		viewer.update(element, null);
	}

	public static CellEditor getEditor() {
		return editor;
	}
}
