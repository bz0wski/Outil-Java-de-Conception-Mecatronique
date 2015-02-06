package org.ica.uiElements;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.ica.briquePackage.Equation;

public class ProprieteEditingSupport extends EditingSupport {

 	final TableViewer viewer;
	final CellEditor editor;
	
	public ProprieteEditingSupport(ColumnViewer viewer) {
		super(viewer);
		this.viewer = (TableViewer) viewer;
		this.editor = new TextCellEditor(((TableViewer) viewer).getTable(), SWT.NONE);	}

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
		return ((Equation)element).getDescEqn();
	}

	@Override
	protected void setValue(Object element, Object value) {
		((Equation)element).setDescEqn(String.valueOf(value));
		viewer.update(element, null);

	}

}

 