package org.ica.uiElements;

import org.eclipse.jface.viewers.ColumnViewerEditor;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationEvent;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationStrategy;
import org.eclipse.jface.viewers.FocusCellOwnerDrawHighlighter;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerEditor;
import org.eclipse.jface.viewers.TreeViewerFocusCellManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Tree;
import org.ica.briquePackage.Equation;
import org.ica.utilityClasses.ObjectFactory;

/**
 * This class is responsible for creating the UI components for
 *  modifying {@code Equation } contents.
 * 
 * @author Salim Ahmed
 *
 */

public class AddParameter{
	private Tree parameterTree;
	private TreeViewer parameterTreeViewer;	 	
	private final AddParameterContent addParameterContent ;

	private boolean isValidatedAtLeastOnce = false;
	/**
	 * Constructor for creating the controls for modifying an {@code Eqaution} object.
	 * @param parent : The parent {@link Composite} on which the controls will be built.
	 * @throws Exception {@code Exception}
	 */
	public AddParameter(Composite parent) throws Exception {
		
		
		parameterTreeViewer = new TreeViewer(parent, SWT.FULL_SELECTION|SWT.BORDER|SWT.SINGLE);

		parameterTree = parameterTreeViewer.getTree();
		
		FormData fd_eqns_Parmas_Table = new FormData();
		fd_eqns_Parmas_Table.right = new FormAttachment(100, 0);
		fd_eqns_Parmas_Table.top = new FormAttachment(0, 0);
		fd_eqns_Parmas_Table.left = new FormAttachment(0, 0);
		fd_eqns_Parmas_Table.bottom = new FormAttachment(100, -60);
	
		parameterTree.setLayoutData(fd_eqns_Parmas_Table);
		parameterTree.setHeaderVisible(true);
		parameterTree.setLinesVisible(true);

		//This class is responsible to provide the concept of cells for Tree. This concept is needed to provide features like editor activation with the keyboard
		TreeViewerFocusCellManager focusCellManager =  new TreeViewerFocusCellManager(parameterTreeViewer,new FocusCellOwnerDrawHighlighter(parameterTreeViewer)); 
		
		ColumnViewerEditorActivationStrategy actSupport = new ColumnViewerEditorActivationStrategy(parameterTreeViewer){
			@Override
			protected boolean isEditorActivationEvent(
					ColumnViewerEditorActivationEvent event) {
					return event.eventType == ColumnViewerEditorActivationEvent.TRAVERSAL
						|| event.eventType == ColumnViewerEditorActivationEvent.MOUSE_DOUBLE_CLICK_SELECTION
						|| (event.eventType == ColumnViewerEditorActivationEvent.KEY_PRESSED && event.keyCode == SWT.CR)
						||  event.eventType == ColumnViewerEditorActivationEvent.PROGRAMMATIC;
			}
		};
		
		TreeViewerEditor.create(parameterTreeViewer, focusCellManager, actSupport,
				ColumnViewerEditor.TABBING_HORIZONTAL
				|ColumnViewerEditor.TABBING_MOVE_TO_ROW_NEIGHBOR
				|ColumnViewerEditor.TABBING_VERTICAL
				|ColumnViewerEditor.KEEP_EDITOR_ON_DOUBLE_CLICK);
		

		
		//This here class is responsible for drawing all the tree contents
		 addParameterContent = new AddParameterContent(this,parent);
		ObjectFactory.putAddParameterContent(addParameterContent);
		
		parameterTree.addListener(SWT.KeyUp, e->{
			if( ((e.stateMask & SWT.CTRL) == SWT.CTRL) && (e.keyCode == 'r')){	
			for (Equation eqn : addParameterContent.getEquationList()) {
				parameterTreeViewer.refresh(eqn);
			}	
				
			}			
		});
		
		/**I'm adding a modify listener to all the cell editors which gets fired once the Equation has been
		 * validated at least once. After it is fired, every time an edit is made to any of the cells of the
		 * table, it replaces the value of parameterValidationOk in the validationRegistry Map with false*/
		Listener listener = (event ->{
			if(isValidatedAtLeastOnce()){
				System.out.println("editor activated after atleast one validation call. TREE DIRTY");
				 ObjectFactory.validationRegistry().replace("parameterValidationOk", false);
			}
		}); 
		
				
		parameterTreeViewer.getTree().addListener(SWT.Selection, listener);
	}

	/**
	 * 
	 * @return The parameterTree
	 */
	public Tree getTree() {
		return parameterTree;
	}
	/**
	 * 
	 * @return The parameterTreeViewer
	 */
	public TreeViewer getTreeViewer() {
		return parameterTreeViewer;
	}

	/**
	 * @return the addParameterContent
	 */
	public AddParameterContent getAddParameterContent() {
		return addParameterContent;
	}


	/**
	 * @return the isValidatedAtLeastOnce
	 */
	public boolean isValidatedAtLeastOnce() {
		return isValidatedAtLeastOnce;
	}


	/**
	 * @param isValidatedAtLeastOnce the isValidatedAtLeastOnce to set
	 */
	public void setValidatedAtLeastOnce(boolean isValidatedAtLeastOnce) {
		this.isValidatedAtLeastOnce = isValidatedAtLeastOnce;
	}

}
