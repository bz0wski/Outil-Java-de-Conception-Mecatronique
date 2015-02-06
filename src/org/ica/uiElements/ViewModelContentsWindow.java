/**
 * 
 */
package org.ica.uiElements;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.ica.briquePackage.CompositionBriques;

/**
 * @author Salim AHMED
 *
 */
public class ViewModelContentsWindow{

	private Shell shell;
	final private Shell parentShell;
	private final CompositionBriques compositionBriques;
	
	public ViewModelContentsWindow(Shell parentShell, int style, CompositionBriques composition) {
		this.parentShell = parentShell;
		compositionBriques = composition;	
	}
	
	

	/**
	 * Open the dialog.
	 */
	public void open() {
		createContents();
		shell.open();
		shell.layout();
		Display display = shell.getDisplay();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}		
	}

	
	/**
	 * Create contents of the dialog.
	 */
	private void createContents() {
		//shell = new Shell(getParent(), SWT.DIALOG_TRIM | SWT.MIN | SWT.RESIZE |SWT.MODELESS);
		shell = new Shell(parentShell, SWT.SHELL_TRIM | SWT.MIN | SWT.RESIZE |SWT.MODELESS);
		
		shell.setLayout(new FormLayout());
		
		Composite composite = new Composite(shell, SWT.NONE);
		
		composite.setLayout(new FormLayout());
		 FormData fd_viewTree = new FormData();
		fd_viewTree.right = new FormAttachment(100, 0);
		fd_viewTree.bottom = new FormAttachment(100, 0);
		fd_viewTree.top = new FormAttachment(0, 0);
		fd_viewTree.left = new FormAttachment(0, 0);
		composite.setLayoutData(fd_viewTree);
		
		if (compositionBriques == null) {
			throw new NullPointerException("Invalid Null object passed");
		}
		
		//If nomComposition is not empty use it, else leave the name empty
		String name = compositionBriques.getNomComposition().isEmpty() ? "" : compositionBriques.getNomComposition();
		shell.setText(name);
		ViewModelContents contents = new ViewModelContents(composite, compositionBriques);
		createTreeResizeListener(composite, contents.getCompositionTree());
		
	}
	
	
	private void createTreeResizeListener(Composite shell, Tree tree){
		
		shell.addListener(SWT.Resize, e->{
			Rectangle compositeArea = shell.getClientArea();
			Point oldSize = tree.getSize();	
			
			if(oldSize.x > compositeArea.width){				
				// composite is getting smaller so make the columns 
				// smaller first and then resize the table to
				// match the client area width				
				for(TreeColumn col:tree.getColumns())
					
					if (col.getWidth() < compositeArea.width/tree.getColumnCount())
						col.setWidth(col.getWidth());
					else
					 col.setWidth(compositeArea.width/tree.getColumnCount()); 
				
				tree.setSize(compositeArea.width, compositeArea.height);
			}
			else{
				// composite is getting bigger so make the table 
				// bigger first and then make the columns wider
				// to match the client area width	
				tree.setSize(compositeArea.width, compositeArea.height);
				for(TreeColumn col:tree.getColumns()){
					if (col.getWidth() > compositeArea.width/tree.getColumnCount())
						col.setWidth(col.getWidth());
					else
						col.setWidth(compositeArea.width/tree.getColumnCount()); 
					}
			
			
			}
		});
	
	}

}

