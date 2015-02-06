package org.ica.uiElements;

import org.eclipse.core.runtime.AssertionFailedException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

public class AddModel extends Dialog {

	protected Object result;
	protected Shell shlAjoutEquations;
	private TabFolder tabFolder;
	


	/**
	 * Create the dialog.
	 * @param parent : The parent {@code Shell } object
	 * @param style : Style bits.
	 */
	public AddModel(Shell parent, int style ) {
		super(parent, style);
		setText("Ajouter Modèle");
		
		
	}

	/**
	 * Open the dialog.
	 * @return the result
	 * @throws Exception {@code Exception}
	 */
	public Object open() throws Exception {
		createContents();
		try{
		shlAjoutEquations.open();
		shlAjoutEquations.layout();
		Display display = getParent().getDisplay();
		while (!shlAjoutEquations.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		}
		catch(Exception e)
		{
			
			if (e instanceof AssertionFailedException) {
				System.err.println("caught it");
				e.printStackTrace();
			}
		}
		return result;
	}

	/**
	 * Create contents of the dialog.
	 * @throws Exception {@code Exception}
	 */
	private void createContents() throws Exception {
		shlAjoutEquations = new Shell(getParent(), SWT.DIALOG_TRIM | SWT.MIN | SWT.RESIZE | SWT.APPLICATION_MODAL);
		
		shlAjoutEquations.setLocation(shlAjoutEquations.getParent().getLocation());
	
	
		//Lambda Expression DisposeListener
		shlAjoutEquations.addDisposeListener(event -> System.out.println("Widget Disposed"));
		
		
		
		shlAjoutEquations.setSize(780, 400);
		shlAjoutEquations.setText("Ajout Equations");
		FormLayout fl_shlAjoutEquations = new FormLayout();
		fl_shlAjoutEquations.marginWidth = 0;
		fl_shlAjoutEquations.marginHeight = 0;
		shlAjoutEquations.setLayout(fl_shlAjoutEquations);
		
		tabFolder = new TabFolder(shlAjoutEquations, SWT.NONE);
		
		FormData fd_tabFolder = new FormData();
		fd_tabFolder.top = new FormAttachment(0);
		fd_tabFolder.right = new FormAttachment(100);
		fd_tabFolder.bottom = new FormAttachment(100);
		fd_tabFolder.left = new FormAttachment(0);
		tabFolder.setLayoutData(fd_tabFolder);
		
		tabFolder.addSelectionListener(new SelectionAdapter() {		
		
		});
	
		
	//STARTING HERE	
		TabItem tabEquations = new TabItem(tabFolder, SWT.NONE);
		tabEquations.setText("Equations");
		EquationComposite equation_composite = new EquationComposite(tabFolder, SWT.NONE);
		tabEquations.setControl(equation_composite);
		
		TabItem tabParametres = new TabItem(tabFolder, SWT.NONE);
		ParameterComposite param_composite = new ParameterComposite(tabFolder, SWT.NONE);
		tabParametres.setText("Parametres");
		tabParametres.setControl(param_composite);
		
		TabItem tabInfoBrique = new TabItem(tabFolder, SWT.NONE);
		InfoBriqueComposite tabInfoBriqueComposite =  new InfoBriqueComposite(tabFolder,SWT.NONE);
		tabInfoBrique.setControl(tabInfoBriqueComposite);
		tabInfoBrique.setText("Informations Surface la brique");
		

		
		
		

		param_composite.pack();
		equation_composite.pack();
		tabInfoBriqueComposite.pack();
	
	}
	/**
	 * @return the tabFolder
	 */
	public TabFolder getTabFolder() {
		return tabFolder;
	}

	/**
	 * @param tabFolder the tabFolder to set
	 */
	public void setTabFolder(TabFolder tabFolder) {
		this.tabFolder = tabFolder;
	}
}
