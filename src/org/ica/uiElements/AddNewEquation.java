package org.ica.uiElements;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.eclipse.core.databinding.Binding;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.databinding.fieldassist.ControlDecorationSupport;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.ica.briquePackage.Equation;
import org.ica.briquePackage.Parametre;
import org.ica.enumeration.SousTypeParametre;
import org.ica.enumeration.TypeParametre;
import org.ica.exceptions.EquationVide;
import org.ica.exceptions.ErreurDeSyntaxe;
import org.ica.exceptions.InvalidParameter;
import org.ica.exceptions.MainApplicationException;
import org.ica.utilityClasses.ExtractionParametre;
import org.ica.utilityClasses.VerificationSyntaxeEquation;

/**
 * This class creates a dialog for manipulating  {@link Equation}  objects, 
 * The first constructor creates a new  {@code Equation}  object, the second 
 * constructor allows the user to modify an already existent  {@code Equation} object. 
 * @author Salim Ahmed
 */
public class AddNewEquation extends Dialog {

	protected Object result;
	protected Shell shlAjouterNouvelleEquation;
	private Text contenuEquation;
	private Text description;
	private Text properties;
	private final Equation equation;
	private IStatus status = ValidationStatus.error("");
	private Button btnTerminer;
	private Button orientationButton;
	private Button contrainteButton;
	private Combo parametreDeSortieCombo;

	private final boolean modification;
	
	/**Functional Interface for creating the items in the dropdown list of the combo box */
	private Function<List<Parametre>, String []> makeItems = list->{
		List<String> names =  new ArrayList<String>();
		for (Parametre parametre : list) {
			names.add(parametre.getNomP());
		}
		return names.toArray(new String[0]);

	};
	/**
	 * First constructor creates the dialog for adding a new {@link Equation} .
	 * @param parent : The parent {@link Composite } on which to attach this dialog.
	 * @param style : Style bits
	 * @param equation : The {@link Equation} object to be added.
	 */
	public AddNewEquation(Shell parent, int style, Equation equation) {
		super(parent, style);
		this.equation = equation;
		setText("Ajouter Nouvelle Equation");
		//A new equation is created
		this.modification = false;
	}
	/**
	 * Second constructor for when an already existent equation is to be modified. 
	 * @param parent : The parent {@link Composite } on which to attach this dialog.
	 * @param style : Style bits
	 * @param equation : The {@link Equation} object to be modified.
	 * @param modification : This parameter tell whether the equation is to be modified if its set to true.
	 */
	public AddNewEquation(Shell parent, int style, Equation equation, Boolean modification) {
		super(parent, style);
		this.equation = equation;
		setText("Modifier Equation");
		System.err.println("second");
		//An already existent equation is been modified
		this.modification = modification;
	}
	/**
	 * Open the dialog.
	 * @return the result
	 */
	public Object open() {
		createContents();
		shlAjouterNouvelleEquation.open();
		shlAjouterNouvelleEquation.layout();

		Display display = getParent().getDisplay();
		while (!shlAjouterNouvelleEquation.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		return result;
	}

	/**
	 * Create contents of the dialog.
	 */
	private void createContents() {
		shlAjouterNouvelleEquation = new Shell(getParent(), SWT.DIALOG_TRIM | SWT.RESIZE | SWT.APPLICATION_MODAL);
		shlAjouterNouvelleEquation.setSize(363, 334);
		if(modification)
			shlAjouterNouvelleEquation.setText("Modifier Equation");
		else
			shlAjouterNouvelleEquation.setText("Ajouter Nouvelle Equation");
		shlAjouterNouvelleEquation.setLayout(new FormLayout());


		Label lblContenuEquation = new Label(shlAjouterNouvelleEquation, SWT.NONE);
		FormData fd_lblContenuEquation = new FormData();
		fd_lblContenuEquation.top = new FormAttachment(0, 5);
		fd_lblContenuEquation.left = new FormAttachment(0, 5);
		lblContenuEquation.setLayoutData(fd_lblContenuEquation);
		lblContenuEquation.setText("Contenu Equation");


		contrainteButton = new Button(shlAjouterNouvelleEquation, SWT.CHECK);
		FormData fd_btnContrainte = new FormData();
		fd_btnContrainte.top = new FormAttachment(0, 27);
		fd_btnContrainte.right = new FormAttachment(100, -10);
		contrainteButton.setLayoutData(fd_btnContrainte);
		contrainteButton.setText("Contrainte");

		orientationButton = new Button(shlAjouterNouvelleEquation, SWT.CHECK);
		FormData fd_btnOrinet = new FormData();
		fd_btnOrinet.top = new FormAttachment(0, 27);
		fd_btnOrinet.right = new FormAttachment(contrainteButton, -10);

		orientationButton.setLayoutData(fd_btnOrinet);
		
		orientationButton.setText("Orient\u00E9");

		contenuEquation = new Text(shlAjouterNouvelleEquation, SWT.BORDER|SWT.SINGLE);
		FormData fd_text = new FormData();
		fd_text.right = new FormAttachment(orientationButton, -10, SWT.LEFT);
		fd_text.top = new FormAttachment(0, 25);
		fd_text.left = new FormAttachment(0, 5);
		contenuEquation.setLayoutData(fd_text);

		contenuEquation.addListener(SWT.FocusOut, out->{
			System.out.println("Making list...");
			try {
				equation.getListeDeParametresEqn_DYNAMIC();
				if(!btnTerminer.isDisposed()){
					if(!btnTerminer.getEnabled())
						 btnTerminer.setEnabled(true);
				}
			} catch (Exception e1) {
				this.showError(shlAjouterNouvelleEquation, e1.toString());
				 btnTerminer.setEnabled(false);
				e1.printStackTrace();
			}	
		});

		Label lblNewLabel = new Label(shlAjouterNouvelleEquation, SWT.NONE);
		FormData fd_lblNewLabel = new FormData();
		fd_lblNewLabel.top = new FormAttachment(0, 51);
		fd_lblNewLabel.left = new FormAttachment(0, 5);
		lblNewLabel.setLayoutData(fd_lblNewLabel);
		lblNewLabel.setText("Param\u00E8tre de Sortie");

		parametreDeSortieCombo = new Combo(shlAjouterNouvelleEquation, SWT.DROP_DOWN |SWT.READ_ONLY);
		parametreDeSortieCombo.setEnabled(false);

		FormData fd_combo = new FormData();
		fd_combo.top = new FormAttachment(0, 71);
		fd_combo.left = new FormAttachment(0, 5);
		parametreDeSortieCombo.setLayoutData(fd_combo);
		parametreDeSortieCombo.addListener(SWT.FocusIn, in->{
			parametreDeSortieCombo.setItems(makeItems.apply(equation.getListeDeParametresEqn()));
			parametreDeSortieCombo.pack();
			parametreDeSortieCombo.update();
		});

		SashForm sashForm = new SashForm(shlAjouterNouvelleEquation, SWT.NONE);
		FormData fd_sashForm = new FormData();
		fd_sashForm.top = new FormAttachment(parametreDeSortieCombo, 6);
		fd_sashForm.bottom = new FormAttachment(100, -50);
		fd_sashForm.right = new FormAttachment(100);
		fd_sashForm.left = new FormAttachment(0, 5);
		sashForm.setLayoutData(fd_sashForm);




		Group propertiesGroup = new Group(sashForm, SWT.NONE);
		propertiesGroup.setLayout(new FormLayout());

		propertiesGroup.setText("Propri\u00E9t\u00E9s");
		FormData fd_propertiesGroup = new FormData();
		fd_propertiesGroup.top = new FormAttachment(0);
		fd_propertiesGroup.left = new FormAttachment(0);
		fd_propertiesGroup.bottom = new FormAttachment(100, 0);
		fd_propertiesGroup.right = new FormAttachment(100, 0);
		propertiesGroup.setLayoutData(fd_propertiesGroup);





		properties = new Text(propertiesGroup, SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
		FormData fd_grouptext = new FormData();
		fd_grouptext.top = new FormAttachment(0,0);
		fd_grouptext.left = new FormAttachment(0, 0);
		fd_grouptext.bottom = new FormAttachment(100, 0);
		fd_grouptext.right = new FormAttachment(100, 0);
		properties.setLayoutData(fd_grouptext);



		Group grpDescription = new Group(sashForm, SWT.NONE);
		grpDescription.setText("Description");
		grpDescription.setLayout(new FormLayout());
		FormData fd_grpDescription = new FormData();
		fd_grpDescription.bottom = new FormAttachment(100, 0);
		fd_grpDescription.right = new FormAttachment(100,0);
		fd_grpDescription.top = new FormAttachment(0);
		fd_grpDescription.left = new FormAttachment(0);
		grpDescription.setLayoutData(fd_grpDescription);

		description = new Text(grpDescription, SWT.BORDER | SWT.WRAP | SWT.V_SCROLL | SWT.MULTI);
		description.setParent(grpDescription);

		FormData fd_description = new FormData();
		fd_description.top = new FormAttachment(0,0);
		fd_description.left = new FormAttachment(0, 0);
		fd_description.bottom = new FormAttachment(100, 0);
		fd_description.right = new FormAttachment(100, 0);

		description.setLayoutData(fd_description);


		sashForm.setWeights(new int[] {50,50});

		btnTerminer = new Button(shlAjouterNouvelleEquation, SWT.NONE);
		btnTerminer.setEnabled(false);
		FormData fd_btnTerminer = new FormData();
		fd_btnTerminer.top = new FormAttachment(sashForm, 6);
		fd_btnTerminer.left = new FormAttachment(sashForm,0, SWT.CENTER);
		btnTerminer.setLayoutData(fd_btnTerminer);
		btnTerminer.setText("Terminer");
		btnTerminer.addListener(SWT.Selection, e->{
			
			boolean go = true;
			result = null;
			if (status.equals(ValidationStatus.ok())) {
				//perform all the neccessary tests before validating the equation					
				if(equation.isOriented()){
					if(!equation.getListeDeParametresEqn().contains(equation.getParametreDeSortie()) || equation.getParametreDeSortie() == null){						
						String error = "Erreur sur l'équation "+equation.getContenuEqn();
						go = false;
						showError(shlAjouterNouvelleEquation, error);					
					}
					if (go) {
						result = Boolean.TRUE;
						shlAjouterNouvelleEquation.dispose();
					}

				}
				else {
					equation.setParametreDeSortie(null);
					result = Boolean.TRUE;
					//Just some cleanup
					for (Parametre par : equation.getListeDeParametresEqn()) {
						if (par.getTypeP().equals(TypeParametre.OUTPUT)) {					
							try {
								par.setTypeP(TypeParametre.UNDETERMINED);
								par.setSousTypeP(SousTypeParametre.FREE);
							} catch (Exception e1) {							
								e1.printStackTrace();
							}
						}
					}
					shlAjouterNouvelleEquation.dispose();
				}

			}
		//	System.out.println(equation.getContenuEqn() +"\n"+equation.isConstraint()+"\n"+equation.isOriented()+"\n"+equation.getParametreDeSortie());
		});

		//In this sub routine I bind the values to the respective controls in order to observe changes 
		//and verify the data insertion
		bindValues();
	}

	/**
	 * @return the status
	 */
	public IStatus getStatus() {
		return status;
	}

	/**
	 * @param status :the status to set
	 */
	public void setStatus(IStatus status) {
		this.status = status;
	}

	/**
	 * Binds a validator to the widget and verifies the inserted text
	 * @return The validation status*/

	private void bindValues(){
		if (contenuEquation.isDisposed())return;
		if(equation == null)return;
		//the DatabindingContext object will manage the databindings
		//In this first part I monitor the the equation expression and check its
		//conformity to the correct syntaxe of an equation.
		DataBindingContext ctx = new DataBindingContext();


		IObservableValue contenuEqnWidget = WidgetProperties.text(SWT.Modify).observe(contenuEquation);
		IObservableValue contenuEqnValue = BeanProperties.value(Equation.class, "contenuEqn").observe(equation);

		//Defining the update strategy
		UpdateValueStrategy strategy = new UpdateValueStrategy();

		//Defining the Update Strategy that the field validator should use, this validation is passed before the value can be set.
		strategy.setBeforeSetValidator(value->{
			String eqnString = String.valueOf(value);
			try {
				new VerificationSyntaxeEquation(eqnString);
				ExtractionParametre.listEquationParameters(eqnString);
			} catch (Exception e) {
				if(!btnTerminer.isDisposed()) btnTerminer.setEnabled(false);
				if (e instanceof ErreurDeSyntaxe){						
					setStatus(ValidationStatus.error(e.toString()));
					return ValidationStatus.error(e.toString());
				}
				else if (e instanceof EquationVide) {
					setStatus(ValidationStatus.error(e.toString()));
					return ValidationStatus.error(e.toString());
				}
				else if (e instanceof InvalidParameter) {
					setStatus(ValidationStatus.error(e.toString()));
					return ValidationStatus.error(e.toString());
				}
			}	
			if(!btnTerminer.isDisposed()) btnTerminer.setEnabled(true);
			setStatus(ValidationStatus.ok());
			return  ValidationStatus.ok();
		});
		//Binding the control and the data with the update strategy to use		
		Binding bindValue = ctx.bindValue(contenuEqnWidget, contenuEqnValue,strategy,null);

		//Adding some decoration to indicate to the user the validation status of
		//of the currently typed equation expression
		ControlDecorationSupport.create(bindValue, SWT.TOP|SWT.RIGHT);
		UpdateValueStrategy convertAndUpdate = new UpdateValueStrategy();

		convertAndUpdate.setBeforeSetValidator(value->{				
			boolean val = (Boolean)value;			

			if ( val) {
				if(!parametreDeSortieCombo.isDisposed())
					if(getStatus().equals(ValidationStatus.ok())){
						parametreDeSortieCombo.setEnabled(true);

						parametreDeSortieCombo.setItems(makeItems.apply(equation.getListeDeParametresEqn()));
						parametreDeSortieCombo.pack();
						parametreDeSortieCombo.update();

					}		
			}
			if (val == false) {
				if(!parametreDeSortieCombo.isDisposed()){

					parametreDeSortieCombo.setEnabled(false);
					parametreDeSortieCombo.setItems(new String[]{});
					parametreDeSortieCombo.pack();
					parametreDeSortieCombo.update();
				}
			}
			return ValidationStatus.ok();

		});		

		IObservableValue contrainteWidget = WidgetProperties.selection().observe(contrainteButton);
		IObservableValue contrainteValue = BeanProperties.value(Equation.class, "isConstraint").observe(equation);
		ctx.bindValue(contrainteWidget, contrainteValue);


		IObservableValue orientationWidget = WidgetProperties.selection().observe(orientationButton);
		IObservableValue orientationValue = BeanProperties.value(Equation.class, "isOriented").observe(equation);

		ctx.bindValue(orientationWidget, orientationValue, convertAndUpdate, null);

		IObservableValue parametreDeSortieWidget = WidgetProperties.selection().observe(parametreDeSortieCombo);
		IObservableValue parametreDeSortieValue = BeanProperties.value(Equation.class, "parametreDeSortie.nomP").observe(equation);
		UpdateValueStrategy updateParametreDeSortie = new UpdateValueStrategy();

		updateParametreDeSortie.setAfterGetValidator(updateOutput->{
			//System.out.println(updateOutput);
			Parametre parametre = equation.getParametreByName((String)updateOutput);
			if (parametre == null) {
				return ValidationStatus.error("Parameter not found.");
			}
			try {
				parametre.setTypeP(TypeParametre.OUTPUT);
				parametre.setSousTypeP(SousTypeParametre.OUTPUT);
				equation.setParametreDeSortie(parametre);
			} catch (MainApplicationException e) {
				e.printStackTrace();
			}

		//	System.err.println(parametre);
			return ValidationStatus.ok();

		});
		ctx.bindValue(parametreDeSortieWidget, parametreDeSortieValue, updateParametreDeSortie, null);

		IObservableValue proprieteEqnWidget = WidgetProperties.text(SWT.Modify).observe(properties);
		IObservableValue proprieteEqnValue = BeanProperties.value(Equation.class, "proprieteEqn").observe(equation);

		ctx.bindValue(proprieteEqnWidget, proprieteEqnValue);

		IObservableValue descEqnWidget = WidgetProperties.text(SWT.Modify).observe(description);
		IObservableValue descEqnEqnValue = BeanProperties.value(Equation.class, "descEqn").observe(equation);

		ctx.bindValue(descEqnWidget, descEqnEqnValue);



		//In this second part I aim to monitor the the equation expression and check its parameter names'
		//conformity to the correct syntaxe of a parameter name before showing it in the combobox.

	}
	/**
	 * Creates an error dialog.
	 * @param parent : Parent {@link Composite}
	 * @param errorMessage : Error message to display.
	 */
	private void showError(Shell parent, String errorMessage){
		MessageBox dialog =  new MessageBox(parent, SWT.ICON_ERROR | SWT.OK);
		dialog.equals("");
		dialog.setText("Attention");
		dialog.setMessage(errorMessage);
		// open dialog and await user selection
		dialog.open(); 
	}

}
