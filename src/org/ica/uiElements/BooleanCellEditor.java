package org.ica.uiElements;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationEvent;
import org.eclipse.jface.viewers.ColumnViewerEditorDeactivationEvent;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerRow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class BooleanCellEditor extends CellEditor{

	 private Button button; 
	 private ViewerRow row; 
	 private int index; 
	 private String restoredText; 
	 private Image restoredImage; 
	 private Boolean oldValue; 
	 
	 /** 
	 * @param parent : The parent {@code Composite}
	 */ 
	 public BooleanCellEditor(Composite parent) 
	 { 
	 super(parent); 
	 } 
	 
	 /** 
	 * @param parent : The parent {@code Composite}
	 * @param style  : style bits
	 */ 
	 public BooleanCellEditor(Composite parent, int style) 
	 { 
	 super(parent, style); 
	 } 
	 
	 public LayoutData getLayoutData() 
	 { 
	 LayoutData data = super.getLayoutData(); 
	 data.horizontalAlignment = SWT.CENTER; 
	 data.grabHorizontal = false; 
	 return data; 
	 } 
	
	 protected Control createControl(Composite parent) 
	 { 
	 Font font = parent.getFont(); 
	 Color bg = parent.getBackground(); 
	 button = new Button(parent, getStyle() | SWT.CHECK); 
	 button.setFont(font); 
	 button.setBackground(bg); 
	 button.addKeyListener(new KeyAdapter() 
	 { 
	 /* 
	 * (non-Javadoc) 
	 * * @see 
	 org.eclipse.swt.events.KeyAdapter#keyReleased(org.eclipse.swt.events.KeyEvent) 
	 
	 */ 
	 public void keyReleased(KeyEvent e) 
	 { 
	 if (e.character == SWT.ESC) 
	 { 
	 fireCancelEditor(); 
	 } 
	 } 
	 }); 
	 return button; 
	 } 
	 
	 protected Object doGetValue() 
	 { 
	 return new Boolean(button.getSelection()); 
	 } 
	 
	 protected void doSetValue(Object value) 
	 { 
	 boolean selection = Boolean.TRUE.equals(value); 
	 button.setSelection(selection); 
	 } 
	 
	 protected void doSetFocus() 
	 { 
	 if (button != null) 
	 { 
	 button.setFocus(); 
	 } 
	 } 
	 
	 protected void deactivate(ColumnViewerEditorDeactivationEvent event) 
	 { 
	 super.deactivate(event); 
	 if (event.eventType == 
	 ColumnViewerEditorDeactivationEvent.EDITOR_CANCELED 
	 || oldValue == button.getSelection()) { 
	 row.setImage(index, restoredImage); 
	 row.setText(index, restoredText); 

	 
	 } 
	 row = null; 
	 restoredImage = null; 
	 restoredText = null; 
	 } 
	 
	 public void activate(ColumnViewerEditorActivationEvent activationEvent) 
	 { 
	 ViewerCell cell = (ViewerCell) activationEvent.getSource(); 
	 index = cell.getColumnIndex(); 
	 row = (ViewerRow) cell.getViewerRow().clone(); 
	 restoredImage = row.getImage(index); 
	 restoredText = row.getText(index); 
	 row.setImage(index, null); 
	 row.setText(index, ""); //$NON-NLS-1$ 
	 super.activate(activationEvent); 
	 oldValue = button.getSelection(); 
	 } 
	 
	 /* 
	 * (non-Javadoc) 
	 * * @see org.eclipse.jface.viewers.CellEditor#getDoubleClickTimeout() 
	 */ 
	 protected int getDoubleClickTimeout() 
	 { 
	 return 0; 
	 } 
	 
	 
	 
	 }

