package org.ica.uiElements;

import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.ica.utilityClasses.FactoryTools;
import org.ica.utilityClasses.ObjectFactory;

public class MainApplicationWindow  {

	
	
	private static MainApplicationWindow mainApplicationWindow;
	/**
	 * Launch the application.
	 * @param args
	 */

	private MainApplicationWindow()  {
		Display display = Display.getDefault();
		final Shell shell = new Shell();
		shell.setText("Application");
		FormLayout layout = new FormLayout();
		layout.marginWidth = 1;
		layout.marginHeight = 1;
		shell.setLayout(layout);
		
		FactoryTools _tools = new FactoryTools(MainApplicationWindowContents.class.getName(), shell);
		
		//Creating the contents of the main shell
		@SuppressWarnings("unused")
		MainApplicationWindowContents contents = ObjectFactory.getMainApplicationWindowContents(_tools);
		
		
		shell.layout();
		shell.open();
		
		
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}
	
	
	/**
	 * Get singleton instance of MainApplicationWindow - Thread Safe
	 * @return the {@code MainApplicationWindow} object.
	 */
	public static synchronized MainApplicationWindow getMainApplicationWindowInstance(){
		
		if (mainApplicationWindow == null) {
			mainApplicationWindow = new MainApplicationWindow();
		}					
			return mainApplicationWindow;		
	}
	
	
	
}
