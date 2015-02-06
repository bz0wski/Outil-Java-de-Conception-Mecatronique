package org.ica.uiElements;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


import java.util.Set;


import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.ica.briquePackage.Equation;
import org.ica.briquePackage.Parametre;
import org.ica.enumeration.SousTypeParametre;
import org.ica.enumeration.TypeParametre;
import org.ica.utilityClasses.FactoryTools;
import org.ica.utilityClasses.MatchIdenticalParameters;
import org.ica.utilityClasses.ObjectFactory;


public class EquationComposite extends Composite {
	
	private AddEquation equationTableWrapper;
	private Table equationTable;
	private final static int NUMBEROFEQUATIONCOMPONENTS = 6;
	private boolean equationValidationOk;
	
	public EquationComposite(Composite parent, int style) throws Exception {
		super(parent, style);
		
		
		
		FormLayout this_layout = new FormLayout();
		this_layout.marginHeight = 10;
		this_layout.marginWidth = 0;
		this.setLayout(this_layout);	
		
		
		this.equationTableWrapper = ObjectFactory.getEquation(new FactoryTools(AddEquation.class.toString(),this));
		this.equationTable = equationTableWrapper.getTable();
		this.createComponents(parent);
		
		this.addControlListener(new EquationCompositeResizeListener());
	
		
		
		this.addDisposeListener(new DisposeListener() {
			
			@Override
			public void widgetDisposed(DisposeEvent e) {
				EquationComposite.this.widgetDisposed(e);
				
			}
		});
	
	}

	
	public void widgetDisposed(DisposeEvent e){
		
	}
	
	
	private void createComponents(Composite parent){

		Button btnAnnuler = new Button(this, SWT.PUSH);
		btnAnnuler.setToolTipText("Annuler Modifications");
		
		//Lambda Expression SelectionListener
		btnAnnuler.addListener(SWT.Selection, event-> {
				if (!equationTable.isDisposed()) {
					/*
					//The Annuler button is to remove the currently selected line in the equation table
					// this also removes the validation status associated with the current line and also 
					// the equation parameters associated with the equation from the Map that serves
					// the cellcomboboxEditor.
					// * I clear out the parameter table, and replace the contents of the table, best solution i could come up with for
					 * 		now.
					*/
					if (!equationTableWrapper.getEquationTableViewer().getSelection().isEmpty()) {
						
						int tableSelectionIndex = equationTableWrapper.getEquationTableViewer().getTable().getSelectionIndex();
						IStructuredSelection selection = (IStructuredSelection) equationTableWrapper.getEquationTableViewer().getSelection();
						if(!selection.isEmpty()){
							Equation eqn = (Equation) selection.getFirstElement();
							//equationTableWrapper.getWritableList().remove(eqn);
							equationTableWrapper.getInputList().remove(eqn);
							equationTableWrapper.getEquationTableViewer().remove(eqn);//
						}
						if (equationTableWrapper.getTableEquationValidationStatus().containsKey(tableSelectionIndex)) {
							equationTableWrapper.getTableEquationValidationStatus().remove(Integer.valueOf(tableSelectionIndex));
						}
						if (ParametreDeSortieEditingSupport.getEquationList().containsKey(Integer.valueOf(tableSelectionIndex))) {
							ParametreDeSortieEditingSupport.getEquationList().remove(Integer.valueOf(tableSelectionIndex));
						}
					}
					//Like here
					AddParameter addParameter = ObjectFactory.getParameter((AddParameter.class.getName()));
					try {
						addParameter.getAddParameterContent().clearEquationList();
						addParameter.getAddParameterContent().reloadData(equationTableWrapper.getInputList());
					} catch (Exception e) {						
						e.printStackTrace();
					}

				}
			});
		
		FormData fd_btnAnnuler = new FormData();
		fd_btnAnnuler.top = new FormAttachment(equationTable, 19);
		fd_btnAnnuler.right = new FormAttachment(equationTable, -5, SWT.RIGHT);
	
		btnAnnuler.setLayoutData(fd_btnAnnuler);
		btnAnnuler.setText("Annuler");
		btnAnnuler.setToolTipText("Supprimer Equation");
		
		
		Button btnValider = new Button(this, SWT.PUSH);
		//Lambda Expression SelectionListener
		btnValider.addListener(SWT.Selection, event -> {
			Set<Equation> eqnSet = new HashSet<>();
			equationValidationOk = true;
			ObjectFactory.validationRegistry().put("equationValidationOk", true);
			/**Setting this value to true makes any subsequent changes to any of the fields
			 * of a parameter require that the user validates this section again. 
			 *  */	
			equationTableWrapper.setValidatedAtLeastOnce(true);
			
			AddParameter addParameter = ObjectFactory.getParameter((AddParameter.class.getName()));
			AddParameterContent parameterContent =  addParameter.getAddParameterContent();
			parameterContent.clearEquationList();
			
			
			
			List<Equation> equationTableList = equationTableWrapper.getInputList();
			
			//Replace all matching parameters in the equation list.			
			MatchIdenticalParameters.matchIdenticalBriqueParameters(equationTableList);
			
			if (!equationTableList.isEmpty()) {

				for (Object equation : equationTableList) {
					Equation eqn = ((Equation)equation);
					if(!eqnSet.add(eqn)){
						equationValidationOk = false;
						String error = "Duplication d'équation";
						showError(parent, error);
						ObjectFactory.validationRegistry().replace("equationValidationOk", false);
						break;
					}
					if(eqn.isOriented()){
						if(!eqn.getListeDeParametresEqn().contains(eqn.getParametreDeSortie()) || eqn.getParametreDeSortie() == null){
							equationValidationOk = false;
							String error = "Erreur sur l'équation "+eqn.getContenuEqn();
							showError(parent, error);
							ObjectFactory.validationRegistry().replace("equationValidationOk", false);
							break;

					}
				}
				else {
					//eqn.setParametreDeSortie(null);				
					//Just some cleanup
					for (Parametre par : eqn.getListeDeParametresEqn()) {
						if (par.getTypeP().equals(TypeParametre.OUTPUT)) {					
							try {
								par.setTypeP(TypeParametre.UNDETERMINED);
								par.setSousTypeP(SousTypeParametre.FREE);
							} catch (Exception e1) {							
								e1.printStackTrace();
							}
						}
					}
				}
			}
			
			Map<Integer, IStatus> map = equationTableWrapper.getTableEquationValidationStatus();
			for (Iterator<Map.Entry<Integer, IStatus>> iterator =  map.entrySet().iterator(); iterator.hasNext();) {
				Map.Entry<Integer, IStatus> entry = iterator.next();
					if (!entry.getValue().equals(ValidationStatus.ok())) {
						equationValidationOk = false;
						int index = entry.getKey()+1;					
						String error = "Erreur sur la ligne " +index;
						showError(parent, error);
						ObjectFactory.validationRegistry().replace("equationValidationOk", false);
						break;
					}
				}
			
					if (equationValidationOk) {
						//Clear the parameter tree to replace it's content.
					
			
						parameterContent.clearEquationList();
						
						//Set the validation status for the equation table to true in the Validation register. 
						ObjectFactory.validationRegistry().replace("equationValidationOk", true);				
							try {
								//NOTE TO SELF
								parameterContent.reloadData(equationTableList);
								//switch from equation Tab to the Parameter tab.
								
								AddModel addModel = ObjectFactory.getAddModel(new FactoryTools(AddModel.class.getName()));
								if (addModel != null) {
									if(addModel.getTabFolder().isDisposed())return;
									int tabsCount = addModel.getTabFolder().getItemCount();
									if (tabsCount >= 1) {
										addModel.getTabFolder().setSelection(1);
									}
									
								}
							} catch (Exception e) {
								
								e.printStackTrace();
							}
	
					}
			
			}
			}
				);
		btnValider.setToolTipText("Valider Equation(s)");
		
		FormData fd_btnValider = new FormData();
		fd_btnValider.top = new FormAttachment(btnAnnuler,0,SWT.TOP);
		
		fd_btnValider.left = new FormAttachment(btnAnnuler, -60, SWT.LEFT);
		btnValider.setLayoutData(fd_btnValider);
		btnValider.setText("Valider");
		
		
		Button btnAjouterEquationButton =  new Button(this, SWT.PUSH);
		btnAjouterEquationButton.setToolTipText("Ajouter Nouvelle Equation");
		
	
		
		btnAjouterEquationButton.addListener(SWT.Selection, listener->{
			try {
				//equationTableWrapper.getWritableList().add(new Equation(false, "a+b=c",  false,null,"Propriété","Insérer commentaire"));
				 Equation equation = new Equation(false, "a+b=c",  false,null,"Propriété","Insérer commentaire");		
				equationTableWrapper.getInputList().add(equation);
				equationTableWrapper.getEquationTableViewer().insert(equation, -1);;
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
		
		
		
		FormData fd_btnAjouterEquation = new FormData();
		fd_btnAjouterEquation.top = new FormAttachment(equationTable, 19);
		fd_btnAjouterEquation.left = new FormAttachment(equationTable, 5, SWT.LEFT);
		
		btnAjouterEquationButton.setLayoutData(fd_btnAjouterEquation);
		btnAjouterEquationButton.setText("Ajouter Equation");
		
		
	}
	
	
	
	private void showError(Composite parent, String errorMessage){
		MessageBox dialog =  new MessageBox(parent.getShell(), SWT.ICON_ERROR | SWT.OK);
		dialog.equals("");
		dialog.setText("Attention");
		dialog.setMessage(errorMessage);
		// open dialog and await user selection
		dialog.open(); 
	}
	
	public boolean getValidationOk(){
		return this.equationValidationOk;
	};
	
	
	private class EquationCompositeResizeListener implements ControlListener{
		@Override
		public void controlResized(ControlEvent e) {
			Rectangle compositeArea = getClientArea();
			Point tableSize = equationTable.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		//	ScrollBar vBar = eqns_Table.getVerticalBar();
		//	int width = compositeArea.width - eqns_Table.computeTrim(0, 0, 0, 0).width - vBar.getSize().x;
			 
			if(tableSize.y > compositeArea.height + equationTable.getHeaderHeight()){
				// Subtract the scrollbar width from the total column width
				// if a vertical scrollbar will be required
			//	Point vBarSize = vBar.getSize();
			//	width -= vBarSize.x;	
			}
			
			Point oldSize = equationTable.getSize();
			if(oldSize.x > compositeArea.width){
				// table is getting smaller so make the columns 
				// smaller first and then resize the table to
				// match the client area width
				
				for(TableColumn col:equationTable.getColumns())
					if (col.getWidth() < compositeArea.width/NUMBEROFEQUATIONCOMPONENTS)
						col.setWidth(col.getWidth());
					else
					{  col.setWidth(compositeArea.width/NUMBEROFEQUATIONCOMPONENTS); } 
				
				equationTable.setSize(compositeArea.width, compositeArea.height);
			}
			else{
				// table is getting bigger so make the table 
				// bigger first and then make the columns wider
				// to match the client area width
				equationTable.setSize(compositeArea.width, compositeArea.height);
				
				for(TableColumn col:equationTable.getColumns()){
					if (col.getWidth() > compositeArea.width/NUMBEROFEQUATIONCOMPONENTS)
						col.setWidth(col.getWidth());
					else
						col.setWidth(compositeArea.width/NUMBEROFEQUATIONCOMPONENTS); 
					}
			}
		}

		@Override
		public void controlMoved(ControlEvent e) {			
		}
	}
	
}
	