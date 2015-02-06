package org.ica.uiElements;



import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.ica.briquePackage.Brique;
import org.ica.briquePackage.Equation;
import org.ica.briquePackage.Parametre;
import org.ica.enumeration.TypeParametre;
import org.ica.utilityClasses.FactoryTools;
import org.ica.utilityClasses.ObjectFactory;
import org.ica.utilityClasses.VerificationValeurParametre;

public class ParameterComposite extends Composite {

	
	private final static int NUMBEROFPARAMETERCOMPONENTS = 8;
	
	
	private AddParameter addParameter;
	private Tree addParameterTree;
	private String errorMessage = "";
	//private final ImageHolder imageHolder = ImageHolder.getImageHolder();
	
	public ParameterComposite(Composite parent, int style) throws Exception {
		super(parent, style);

		
		FormLayout fl_parameter_composite = new FormLayout();
		fl_parameter_composite.marginWidth = 0;
		fl_parameter_composite.marginHeight = 10;
		this.setLayout(fl_parameter_composite);
		
		//Add table
		FactoryTools tools = new FactoryTools(AddParameter.class.getName(), this);
		
		addParameter = ObjectFactory.getParameter(tools);
		addParameterTree = addParameter.getTree();
		
		this.createComponents(parent);
		
		this.addControlListener(new ParameterCompositeResizeListener());
	}
	
	
	
	
	private void createComponents(Composite parent) {
		
		//Adding Buttons
		
		
		Button btnAnnuler_Final = new Button(this, SWT.PUSH);
	
		FormData fd_btnAnnuler_Final = new FormData();
		fd_btnAnnuler_Final.top = new FormAttachment(addParameterTree, 19);
		fd_btnAnnuler_Final.right = new FormAttachment(addParameterTree, 0, SWT.RIGHT);

		btnAnnuler_Final.setLayoutData(fd_btnAnnuler_Final);
		btnAnnuler_Final.setText("Annuler");
		
		
		Button btnValider_Final = new Button(this, SWT.PUSH);
	
		//Lambda Expression SelectionListener
		btnValider_Final.addListener(SWT.Selection, event -> {
			AddParameterContent addParameterContent = addParameter.getAddParameterContent();
			
			List<Equation> eqnList = addParameterContent.getEquationList();			
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////						
			/**Setting this value to true makes any subsequent changes to any of the fields
			 * of a parameter require that the user validates this section again. 
			 *  */	
			addParameter.setValidatedAtLeastOnce(true);
			/**Verify type and sub-type and verify that every oriented equation with 
					 * parametre de sortie corresponds to the parameter typed as OUTPUT. Also
					 * that if the type is input, the value field must not be empty and vice versa.
					 * Finally a parameter cannot be typed output  if
					 * i)the equation isn't oriented
					 * ii)if it differs from the output parameter of an oriented equation
					 * 
					 * */
					Supplier<Boolean> verifyB4Validation = ()->{
						boolean isIt = true;
						for (Equation equation : eqnList) {
							//First of all verifying that every oriented equation with 
							//parametre de sortie has the corresponding parameter typed as OUTPUT
							if (equation.isOriented()) {
								if(equation.getParametreDeSortie() != null){							
									Parametre p = equation.getParametreDeSortie();
									for (Parametre parametre : equation.getListeDeParametresEqn()) {
										if (parametre.equals(p)) {
											if (!parametre.getTypeP().equals(TypeParametre.OUTPUT)) {
												errorMessage = "Erreur de type de paramètre de sortie. Equation( "+equation.getContenuEqn()+" )";
												return !isIt;
												
											}
										}
									}
								}
								else if (equation.getParametreDeSortie() == null) {
									errorMessage = "Vérifier le paramètre de sortie";
									return !isIt;
								}
								
							}
							
							//Secondly scan through all the parameters of the equations 
							//to check the Type/SubType pairing
							for (Parametre parametre : equation.getListeDeParametresEqn()){
								if (!parametre.getTypeP().getSubtype().contains(parametre.getSousTypeP())) {
									errorMessage = "Erreur de sous type du paramètre";
									return !isIt;
									
									}
								//Verify that if a parameter is typed OUTPUT, its the same as the local output of the equation
								if (parametre.getTypeP().equals(TypeParametre.OUTPUT)) {
									if (!equation.isOriented()) {
										errorMessage = "Une équation non orientée ne peut pas avoir un paramètre de type OUTPUT.";
										return !isIt;
									}
									else if (equation.isOriented()) {
										if (!equation.getParametreDeSortie().equals(parametre)) {
											errorMessage = "Vous pouvez pas avoir plusieurs sorties pour une même équation.";
											return !isIt;
										}
									}
								}
								//Verify that if the parameter is an input, it contains a value
								if(parametre.getTypeP().equals(TypeParametre.INPUT) && parametre.getValeurP().isEmpty()){
									errorMessage = "Paramètre de type INPUT doit prendre une valeur.";
									return !isIt;
								
								}
								//If the parameter is not typed Input it can't have a value
								if(!parametre.getTypeP().equals(TypeParametre.INPUT) && !parametre.getValeurP().isEmpty()){
								//	errorMessage = "Seulement les paramètres de type INPUT peuvent prendre une valeur.";
									parametre.setValeurP("");
									return isIt;
								//	return errorMessage;
								}
								//Verifying that the value of a parameter corresponds with the input type
								if (parametre.getTypeP().equals(TypeParametre.INPUT)) {
									try {
										VerificationValeurParametre.parameterValues(parametre);
									} catch (Exception e) {
										errorMessage = e.toString()+" (Paramètre: "+parametre.getNomP()+" )";
									
										return !isIt;
										
										
									}
								}
							}
						}
						return isIt;					
					};
					
					/**Functional Interface to extract the parameter name concerned with the 
					Invalid value.
					Function<Long, String> findParam = id -> {
						for(Iterator<?> it = eqnList.iterator(); it.hasNext();){	
									Equation eqn = (Equation)it.next();
										for(Iterator<Parametre> it2 = eqn.getListeDeParametresEqn().iterator();  it2.hasNext();){
											Parametre parametre =  it2.next();	
											if(parametre.getUniqueParamId() == id){
												 return parametre.getNomP();	
											}
										}
									
						}
									return "";
					};*/
				
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////			
		
			
			if(!addParameterContent.getEquationList().isEmpty()){
				
				boolean parameterValidationOk = true;	
				ObjectFactory.validationRegistry().put("parameterValidationOk", true);
				/*
				Map<Long, IStatus> map = addParameter.getAddParameterContent().getTreeParameterValidationStatus();
				if (!map.isEmpty()){
					for(Iterator<Map.Entry<Long, IStatus>> iterator = map.entrySet().iterator(); iterator.hasNext();){
						Map.Entry<Long, IStatus> entry  = iterator.next();
						if(!entry.getValue().equals(ValidationStatus.ok())){
							parameterValidationOk = false;
							ObjectFactory.validationRegistry().replace("parameterValidationOk", false);							
							
							String errorMessage = findParam.apply(entry.getKey());
							if(!errorMessage.equals("") )
								showErrorMessage(parent, "Erreur au parametre "+errorMessage);
							else
								showErrorMessage(parent,"Erreur sur la valeur d'un parametre.");
						
						
						}
					}
				}*/	
			parameterValidationOk = verifyB4Validation.get();
			System.err.println("VALIDATION STATUS "+verifyB4Validation.get()+" ."+errorMessage);
				
			if(parameterValidationOk){
				ObjectFactory.validationRegistry().replace("parameterValidationOk", parameterValidationOk);
				
					//if validation ok, replace all like parameters
				//	MatchIdenticalParameters.matchIdenticalListOfEquationParameters(eqnList);
					for (Equation equation : eqnList) {
						System.err.println(equation.getListeDeParametresEqn());
					}
					//retrieve the Brique object and set its components 
					Brique brique = ObjectFactory.getBrique(Brique.class.getName());	
					long idB = brique.getBriqueId();
					
					List<Parametre> paramList = new ArrayList<>();
					List<Equation> equationList = new ArrayList<>();
					
					
					for (Equation equation : eqnList) {
						//setting brique reference Before Adding Equation
						equation.setReferenceBrique(idB);
						equationList.add(equation);
						
						paramList.addAll(equation.getListeDeParametresEqn());
					}
					if (!paramList.isEmpty() ) {
						brique.setListEquations(equationList);
						brique.setListParametres(paramList);
					
					}
					
		
					
				
					/**If everything is ok, switch from parameter tab to InfoBrique Tab
					 * */
					AddModel addModel = ObjectFactory.getAddModel(new FactoryTools(AddModel.class.getName()));
					if (addModel != null) {
						int tabsCount = addModel.getTabFolder().getItemCount();
						if (tabsCount >= 2) {
							addModel.getTabFolder().setSelection(2);
						}
					}
				}
				/*If !validationOk showErrorMessage*/
				else {
					ObjectFactory.validationRegistry().put("parameterValidationOk", parameterValidationOk);
					showErrorMessage(parent, errorMessage);
				}				
			}
			});
		
		FormData fd_btnValider_Final = new FormData();
		fd_btnValider_Final.left = new FormAttachment(btnAnnuler_Final, -60, SWT.LEFT);
		fd_btnValider_Final.top = new FormAttachment(btnAnnuler_Final, 0, SWT.TOP);
		//fd_btnValider_Final.left = new FormAttachment(addParameterTree, 0, SWT.LEFT);
		btnValider_Final.setLayoutData(fd_btnValider_Final);
		btnValider_Final.setText("Valider");
		btnValider_Final.setToolTipText("Valider");
		
	}
	
	private void showErrorMessage(Composite parent, String errorMessage){
		MessageBox dialog =  new MessageBox(parent.getShell(), SWT.ICON_ERROR | SWT.OK);
		
		dialog.setText("Attention");
		dialog.setMessage(errorMessage);	
		// open dialog and await user selection
		dialog.open(); 
	}
	
	private class ParameterCompositeResizeListener implements ControlListener{

		@Override
		public void controlMoved(ControlEvent e) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void controlResized(ControlEvent e) {
			Rectangle compositeArea = getClientArea();
			Point tableSize = computeSize(SWT.DEFAULT, SWT.DEFAULT);
	//		ScrollBar vBar = addParameterTree.getVerticalBar();
			//int width = compositeArea.width - addParameterTree.computeTrim(0, 0, 0, 0).width - vBar.getSize().x;
			 
			if(tableSize.y > compositeArea.height + addParameterTree.getHeaderHeight()){
				// Subtract the scrollbar width from the total column width
				// if a vertical scrollbar will be required
			//	Point vBarSize = vBar.getSize();
				//width -= vBarSize.x;	
			}
			
			Point oldSize = addParameterTree.getSize();
			if(oldSize.x > compositeArea.width){
				//System.out.println("Getting smaller, oldSize :"+(oldSize.x) +" compositeArea.width: "+compositeArea.width );
				// table is getting smaller so make the columns 
				// smaller first and then resize the table to
				// match the client area width
				
				for(TreeColumn col:addParameterTree.getColumns())
					if (col.getWidth() < compositeArea.width/NUMBEROFPARAMETERCOMPONENTS)
						col.setWidth(col.getWidth());
					else
					 col.setWidth(compositeArea.width/NUMBEROFPARAMETERCOMPONENTS); 
				
				addParameterTree.setSize(compositeArea.width, compositeArea.height);
			}
			else{
				//System.out.println("Getting BIGGER, oldSize :"+(oldSize.x+20) +" compositeArea.width: "+compositeArea.width );
				// table is getting bigger so make the table 
				// bigger first and then make the columns wider
				// to match the client area width
				addParameterTree.setSize(compositeArea.width, compositeArea.height);
				
				for(TreeColumn col:addParameterTree.getColumns()){
					if (col.getWidth() > compositeArea.width/NUMBEROFPARAMETERCOMPONENTS)
						col.setWidth(col.getWidth());
					else
						col.setWidth(compositeArea.width/NUMBEROFPARAMETERCOMPONENTS); 
					}
			}

		}

			
		}
		
	}


