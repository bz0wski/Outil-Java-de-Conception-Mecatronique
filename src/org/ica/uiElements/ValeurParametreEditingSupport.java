package org.ica.uiElements;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.ica.briquePackage.Equation;
import org.ica.briquePackage.Parametre;





public class ValeurParametreEditingSupport extends EditingSupport {

	private final CellEditor editor;
	private final TreeViewer treeViewer;
	
	public ValeurParametreEditingSupport(ColumnViewer viewer) {
		super(viewer);
		this.treeViewer = (TreeViewer) viewer;
		this.editor = new TextCellEditor(((TreeViewer)treeViewer).getTree()	,SWT.NONE);
		
/*
		TreeViewerEditor.create(treeViewer, new ColumnViewerEditorActivationStrategy(treeViewer){
			protected boolean isEditorActivationEvent(ColumnViewerEditorActivationEvent event) {  
				return event.eventType == ColumnViewerEditorActivationEvent.MOUSE_DOUBLE_CLICK_SELECTION;
			}  
			
		}, TreeViewerEditor.DEFAULT);
		*/
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
		if (element instanceof Parametre) {
			return	Float.valueOf(((Parametre)element).getValeurP());
		}
		return "";
	}

	@Override
	protected void setValue(Object element, Object value) {
		System.out.println("ELEMENT "+element.getClass() +" OBJECT VALUE :"+value.getClass());
		if (element instanceof Parametre && value instanceof Float) {
			
			
				((Equation)element).getListeDeParametresEqn();
			
		}
		treeViewer.update(element, null);
	}

}
