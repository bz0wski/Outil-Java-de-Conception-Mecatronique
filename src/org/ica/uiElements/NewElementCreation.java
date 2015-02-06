package org.ica.uiElements;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.List;

import org.eclipse.core.databinding.Binding;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.databinding.validation.IValidator;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.databinding.fieldassist.ControlDecorationSupport;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.widgets.Button;
import org.ica.briquePackage.Brique;
import org.ica.briquePackage.CompositionBriques;
import org.ica.briquePackage.LevelOnePackage;
import org.ica.briquePackage.LevelTwoPackage;
import org.ica.utilityClasses.FactoryTools;
import org.ica.utilityClasses.ObjectFactory;
import org.ica.utilityClasses.RenameContents;


/**
 * Creates a dialog which enables the user to create and rename components.
 * @author Salim Ahmed
 *
 */
public class NewElementCreation extends Dialog {

	protected Object result;
	protected Shell shell;
	private Text text;
	private Button btnAjouter; 

	private String briqueName;
	private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
	private IStatus status;
	private Object object;

	private List<String> names = null;
	/**
	 * 	 * Create the dialog.
	 * @param parent : The Shell of the calling client
	 * @param style : SWT style bits
	 * @param object : The {@code Object} which will be manipulated by this widget.
	 */
	public NewElementCreation(Shell parent, int style, Object object) {
		super(parent, style);
		if (object instanceof LevelOnePackage) {
			setText("Nom du Package");
			this.object = (LevelOnePackage)object;
		}
		if (object instanceof LevelTwoPackage) {
			setText("Nom du Sous Package");	
			this.object = (LevelTwoPackage)object;
		}
		if (object instanceof CompositionBriques) {
			setText("Nom du Model");
			this.object = (CompositionBriques)object;
		}
		if (object instanceof TempCanvas) {
			setText("Renommer la Brique");
			this.object = (TempCanvas)object;
			MainWindowTabs tabs = ObjectFactory.getMainWindowTabs(new FactoryTools(MainWindowTabs.class.getName()));
			names = tabs.getCanvasNamesList(((TempCanvas) object).getDataObject());
		}
	}

	/**
	 * Open the dialog.
	 * @return the result of creating the Dialog
	 */
	public Object open() {
		createContents();
		shell.open();
		shell.layout();
		Display display = getParent().getDisplay();
		while (!shell.isDisposed()) {
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
		shell = new Shell(getParent(), SWT.DIALOG_TRIM | SWT.PRIMARY_MODAL);
		shell.setSize(382, 138);
		shell.setText(getText());
		shell.setLocation(shell.getParent().getLocation());
		FormLayout fl_shell = new FormLayout();
		fl_shell.marginWidth = 5;
		fl_shell.marginHeight = 5;
		shell.setLayout(fl_shell);

		text = new Text(shell, SWT.BORDER|SWT.SINGLE);
		FormData fd_text = new FormData();
		fd_text.top = new FormAttachment(0, 10);
		fd_text.left = new FormAttachment(0, 10);
		fd_text.right = new FormAttachment(100, -10);
		text.setLayoutData(fd_text);

		////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////	
		WritableValue value  = new WritableValue();

		// create the binding
		DataBindingContext ctx = new DataBindingContext();
		IObservableValue target = WidgetProperties.
				text(SWT.Modify).observe(text);
		IObservableValue model = BeanProperties.value("briqueName").
				observeDetail(value);

		//Adding a validator so that only a proper word can be used as a name
		IValidator validator = new IValidator() {
			@Override
			public IStatus validate(Object value) {
				if (value instanceof String) {
					String s = String.valueOf(value);
					if (s.matches("\\w+")) {			        	 
						if(names != null){			        		  
							if (names.contains(s)) {
								btnAjouter.setEnabled(false);
								status = ValidationStatus.error("Nom  déjà existant.");
								return status;	}					
						}
						status = ValidationStatus.ok();
						setBriqueName(s);
						btnAjouter.setEnabled(true);
						return ValidationStatus.ok();
					}
				}

				if (!btnAjouter.isDisposed()) {
					btnAjouter.setEnabled(false);
				}
				status = ValidationStatus.error("Nom  Invalide");
				return ValidationStatus.error("Nom Invalide");
			}
		};

		UpdateValueStrategy strategy = new UpdateValueStrategy();
		strategy.setBeforeSetValidator(validator);
		// make the binding valid for this new object
		value.setValue(briqueName); 

		//Binding the validator to the value and update strategy
		Binding binding = ctx.bindValue(target, model,strategy,null);

		//Adding some decoration
		ControlDecorationSupport.create(binding, SWT.RIGHT|SWT.TOP);

		//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


		Button btnAnnuler = new Button(shell, SWT.NONE);
		FormData fd_btnAnnuler = new FormData();
		fd_btnAnnuler.top = new FormAttachment(text, 20);
		fd_btnAnnuler.right = new FormAttachment(text, 0, SWT.RIGHT);
		btnAnnuler.setLayoutData(fd_btnAnnuler);
		btnAnnuler.setText("Annuler");


		btnAnnuler.addListener(SWT.Selection, listener->{
			shell.dispose();
		});

		btnAjouter = new Button(shell, SWT.NONE);
		FormData fd_btnAjouter = new FormData();
		fd_btnAjouter.top = new FormAttachment(btnAnnuler, 0, SWT.TOP);
		fd_btnAjouter.right = new FormAttachment(btnAnnuler, -6);
		btnAjouter.setLayoutData(fd_btnAjouter);
		if(object instanceof TempCanvas)
			btnAjouter.setText("Renommer");
		else 
			btnAjouter.setText("Ajouter");
		btnAjouter.setEnabled(false);

		btnAjouter.addListener(SWT.Selection, listener->{
			if (status.equals(ValidationStatus.ok())) {

				if (object instanceof LevelOnePackage) {
					((LevelOnePackage)object).setPackageName(getBriqueName());
				}
				if (object instanceof LevelTwoPackage) {
					((LevelTwoPackage)object).setSubPackageName(getBriqueName());
				}
				if (object instanceof CompositionBriques) {

					((CompositionBriques)object).setNomComposition(getBriqueName());				
				}
				if (object instanceof TempCanvas) {
					object = ((TempCanvas)object).getDataObject();
					if (object instanceof CompositionBriques) {
						CompositionBriques compositionBriques = ((CompositionBriques)object);
						String oldName = compositionBriques.getNomComposition();
						compositionBriques.setNomComposition(getBriqueName());
						try {
							//After setting the name of the Composition, go through it and change all names 
							//of it's contained contents.
							RenameContents.execute(oldName,compositionBriques);
						} catch (Exception e) {
							status =  ValidationStatus.error(e.toString());
							e.printStackTrace();
						}



					}
					else {
						Brique brique = ((Brique)object);
						String oldName = brique.getNomBrique();
						brique.setNomBrique(getBriqueName());
						try {
							//After setting the name of the Brique, go through it and change all names 
							//of it's contained contents.
							RenameContents.execute(oldName, brique);
						} catch (Exception e2) {
							status =  ValidationStatus.error(e2.toString());
						}
					}
				}
				shell.dispose();					
			}
		});

	}

	/**
	 * @return the briqueName
	 */
	public String getBriqueName() {
		return briqueName;
	}


	/**
	 * @param briqueName the briqueName to set
	 */
	public void setBriqueName(String briqueName) {
		String oldValue = this.briqueName;
		this.briqueName = briqueName;
		propertyChangeSupport.firePropertyChange("briqueName", oldValue, briqueName);
	}
/**
 * Adds basic JavaBeans support  
 * @param propertyName : The name of the property to which to add the {@code PropertyChangeListener}.
 * @param listener : The {@code PropertyChangeListener} to add
 */
	public void addPropertyChangeListener(String propertyName,
			PropertyChangeListener listener) {
		propertyChangeSupport.addPropertyChangeListener(propertyName, listener);
	}
/**
 * Removes basic JavaBeans support  
 * @param listener : The {@code PropertyChangeListener} to remove.
 */
	public void removePropertyChangeListener(PropertyChangeListener listener) {
		propertyChangeSupport.removePropertyChangeListener(listener);
	}

}

