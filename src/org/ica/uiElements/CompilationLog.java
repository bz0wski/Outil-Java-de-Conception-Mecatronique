package org.ica.uiElements;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.SWTResourceManager;
import org.ica.utilityClasses.ExportToMatlab;
/**
 * This class is responsible for creating dialog windows that display a log of 
 * the compilation process. A dialog window is created to report any errors encountered
 * too. The user has the option to save the logs as plain text files for future reference. 
 * @author Salim AHMED
 *
 */
public class CompilationLog extends Dialog {

	protected Object result;
	protected Shell shell;
	private Text text;

	private Button btnEnregistrer;
	/**Holds a reference to the {@code ExportToMatlab} conducting the compilation process,
	 * it serves to live report log and error log entries. */
	private ExportToMatlab export = null;
	/**This attribute, if true indicates that the second constructor(error log) was called. */
	private boolean errorLog = false;
	/**
	 * Creates the dialog which updates the user with the current compilation log.
	  *@param parent: parent shell.
	 * @param exportToMatlab: object to observe
	 */
	
	public CompilationLog(Shell parent, ExportToMatlab exportToMatlab) {
		super(parent);
		setText("Log - "+exportToMatlab.getCompositionBriques().getNomComposition());		
		export = (ExportToMatlab) exportToMatlab;
	}
	
	/**
	 * Creates the dialog which presents the log of errors encountered during the compilation process.
	 * @param parent: parent shell.
	 * @param exportToMatlab: object to observe
	 * @param error: Boolean value to check if to open error log or compilation log
	 */
	
	public CompilationLog(Shell parent, ExportToMatlab exportToMatlab,boolean error) {
		super(parent);
		setText("Error Log - "+exportToMatlab.getCompositionBriques().getNomComposition());	
		export = (ExportToMatlab) exportToMatlab;
		errorLog = error;
	}
	/**
	 * Open the dialog.
	 * @return the result
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
		shell = new Shell(getParent(), SWT.DIALOG_TRIM | SWT.RESIZE);
		shell.setSize(268, 400);
		shell.setText(getText());
		shell.setLayout(new FormLayout());
	
		Composite composite = new Composite(shell, SWT.NONE);
		//	composite.setBackground(SWTResourceManager.getColor(SWT.COLOR_BLUE));
		composite.setLayout(new FormLayout());
		FormData fd_composite = new FormData();
		fd_composite.left = new FormAttachment(0);
		fd_composite.bottom = new FormAttachment(100);
		fd_composite.top = new FormAttachment(0);
		fd_composite.right = new FormAttachment(100);

		composite.setLayoutData(fd_composite);

		text = new Text(composite, SWT.BORDER | SWT.READ_ONLY | SWT.WRAP | SWT.V_SCROLL | SWT.MULTI);
		text.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		FormData fd_text = new FormData();
		fd_text.bottom = new FormAttachment(100, -50);
		fd_text.right = new FormAttachment(100);
		fd_text.top = new FormAttachment(0);
		fd_text.left = new FormAttachment(0);

		text.setLayoutData(fd_text);

		btnEnregistrer = new Button(composite, SWT.NONE);
		FormData fd_btnEnregistrer = new FormData();
		fd_btnEnregistrer.left = new FormAttachment(text, 0, SWT.CENTER);
		fd_btnEnregistrer.top = new FormAttachment(text, 5, SWT.BOTTOM);
		btnEnregistrer.setLayoutData(fd_btnEnregistrer);
		btnEnregistrer.setBounds(0, 10, 75, 25);
		btnEnregistrer.setText("Enregistrer");
		btnEnregistrer.addListener(SWT.Selection, listener->{
			FileDialog fileDialog = new FileDialog(composite.getShell(), SWT.SAVE);
			fileDialog.setText("Enregistrer");
			fileDialog.setOverwrite(true);
			fileDialog.setFilterExtensions(new String []{"*.txt"});
			String 	saveLocation = fileDialog.open();
			if (saveLocation == null)
				return;

			File file = new File(saveLocation);
			
		
			try(PrintWriter writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "utf-8")))){
				writer.write(text.getText());
				
			}
			catch (Exception e) {

				e.printStackTrace();
			}
		});
		
		DataBindingContext context = new DataBindingContext();
		
		
	
		if(errorLog){
			
			Color red = shell.getDisplay().getSystemColor(SWT.COLOR_RED);
			
			
			text.setForeground(red);

			text.setFont(shell.getDisplay().getSystemFont());
			
			IObservableValue widget = WidgetProperties.text(SWT.Modify).observe(text);
			IObservableValue modelValue = BeanProperties.value("errorLog").observe(export);
			context.bindValue(widget, modelValue);
			text.setText(export.getErrorLog());
		}
		else {
			IObservableValue widget = WidgetProperties.text(SWT.Modify).observe(text);
			IObservableValue modelValue = BeanProperties.value("report").observe(export);
			context.bindValue(widget, modelValue);
		}
		
		
		
	}

	
}
