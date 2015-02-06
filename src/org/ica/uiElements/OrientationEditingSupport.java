package org.ica.uiElements;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.ica.briquePackage.Equation;

public class OrientationEditingSupport extends EditingSupport {

	final TableViewer viewer;
	final CellEditor editor;
	
	public OrientationEditingSupport(ColumnViewer viewer) {
		super(viewer);
		this.viewer = (TableViewer) viewer;
		//this.editor = new BooleanCellEditor(((TableViewer) viewer).getTable(),SWT.CHECK);
		this.editor = new CheckboxCellEditor(((TableViewer) viewer).getTable(),SWT.CHECK);
		editor.setStyle(SWT.CENTER);
		editor.getLayoutData().horizontalAlignment = SWT.CENTER;
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
		return ((Equation) element).isOriented();
	
	
	}

	@Override
	protected void setValue(Object element, Object value) {
		((Equation) element).setIsOriented((Boolean)value);
		getViewer().update(element, null);
	}

}
