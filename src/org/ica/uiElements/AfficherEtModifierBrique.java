package org.ica.uiElements;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.ica.utilityClasses.ObjectFactory;

public class AfficherEtModifierBrique {
	private final TreeViewer briqueTreeViewer;
	private final Tree briqueTree;
	private final AfficherEtModifierBriqueContents contents;
	
	/**@param parent
	 * 	Composite object on which the contents will be placed
	 * @param object
	 * 	Represents either a Brique or CompositionBriques object whose content will be displayed
	 * <br>
	 * Creates the AfficherEtModifierBrique window which will hold data to be displayed and modified by
	 * the user.
	 * */
	public AfficherEtModifierBrique(Composite parent,Object object) {
		
		briqueTreeViewer = new TreeViewer(parent, SWT.FULL_SELECTION|SWT.SINGLE);
		briqueTree = briqueTreeViewer.getTree();
		briqueTree.setLinesVisible(false);
		briqueTree.setHeaderVisible(true);
		
		 
		 briqueTree.setLayout(new FormLayout());
		 //Inheriting the parent LayoutData
		 briqueTree.setLayoutData(parent.getLayoutData());
		
	
		
		 //Setting contents
	
		 contents = new AfficherEtModifierBriqueContents(this, object);
		ObjectFactory.putAfficherEtModifierBriqueContents(contents);
		
		briqueTree.pack();
	}

	public TreeViewer getBriqueTreeViewer() {
		return briqueTreeViewer;
	}

	/**
	 * @return the contents of AfficherEtModifierBrique
	 */
	public AfficherEtModifierBriqueContents getContents() {
		return contents;
	}
}
