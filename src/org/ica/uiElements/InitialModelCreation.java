package org.ica.uiElements;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

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
import org.ica.utilityClasses.FactoryTools;
import org.ica.utilityClasses.ObjectFactory;

public class InitialModelCreation extends Dialog {

	protected Object result;
	protected Shell shell;
	private Text text;
	private Button btnAjouter; 
	
	private String briqueName;
	private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
	private IStatus status;
	/**
	 * Create the dialog.
	 * @param parent : The Shell of the calling client
	 * @param style : SWT style bits
	 */
	public InitialModelCreation(Shell parent, int style) {
		super(parent, style);
		setText("Nom du Model");
		
			
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
	
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////	
		WritableValue value  = new WritableValue();

		// create the binding
		DataBindingContext ctx = new DataBindingContext();
		IObservableValue target = WidgetProperties.
		  text(SWT.Modify).observe(text);
		IObservableValue model = BeanProperties.value("briqueName").
		    observeDetail(value);

		//Adding a validator so that only a noun can be used as a name
		 IValidator validator = new IValidator() {
		      @Override
		      public IStatus validate(Object value) {
		        if (value instanceof String) {
		          String s = String.valueOf(value);
		          if (s.matches("\\w+")) {
		        	  status = ValidationStatus.ok();
		        	  setBriqueName(s);
		        	  btnAjouter.setEnabled(true);
		            return ValidationStatus.ok();
		          }
		        }
		       
		        if (!btnAjouter.isDisposed()) {
		        	 btnAjouter.setEnabled(false);
				}
		       status = ValidationStatus.error("Nom de brique Invalide");
		        return ValidationStatus.error("Nom de brique Invalide");
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
		
		shell.setDefaultButton(btnAnnuler);
		btnAnnuler.addListener(SWT.Selection, listener->{			
			shell.dispose();
		});
		
		btnAjouter = new Button(shell, SWT.NONE);
		FormData fd_btnAjouter = new FormData();
		fd_btnAjouter.top = new FormAttachment(btnAnnuler, 0, SWT.TOP);
		fd_btnAjouter.right = new FormAttachment(btnAnnuler, -6);
		btnAjouter.setLayoutData(fd_btnAjouter);
		btnAjouter.setText("Ajouter");
		btnAjouter.setEnabled(false);
		 
		btnAjouter.addListener(SWT.Selection, listener->{
		if (status.equals(ValidationStatus.ok())) {
			FactoryTools tools = new FactoryTools(getBriqueName());
			final Brique brique = ObjectFactory.getBrique(tools);
			brique.setNomBrique(briqueName);
			
			 AddModel diag = ObjectFactory.getAddModel(new FactoryTools(AddModel.class.getName(), shell));
			
			try {
				
				diag.open();
				shell.dispose();
			} catch (Exception e) {
				
				e.printStackTrace();
			}
		
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
	
	 public void addPropertyChangeListener(String propertyName,
		      PropertyChangeListener listener) {
		    propertyChangeSupport.addPropertyChangeListener(propertyName, listener);
		  }

		  public void removePropertyChangeListener(PropertyChangeListener listener) {
		    propertyChangeSupport.removePropertyChangeListener(listener);
		  }

}
