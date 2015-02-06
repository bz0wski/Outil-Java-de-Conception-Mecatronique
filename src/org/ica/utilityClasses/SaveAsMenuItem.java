package org.ica.utilityClasses;

import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.ica.uiElements.MainWindowTabs;

public class SaveAsMenuItem implements SelectionListener {

	/*final Composite composite;
	public SaveAsMenuItem(Composite composite){
		this.composite = composite;
	}
	*/
	@Override
	public void widgetSelected(SelectionEvent e) {
		MainWindowTabs mwt = ObjectFactory.getMainWindowTabs(new FactoryTools(MainWindowTabs.class.getName()));	
		
		if(mwt.saveProjectAs())
			mwt.updateUserAfterSave();
	
		
	}

	@Override
	public void widgetDefaultSelected(SelectionEvent e) {
		MainWindowTabs mwt = ObjectFactory.getMainWindowTabs(new FactoryTools(MainWindowTabs.class.getName()));	
		
		if(mwt.saveProjectAs())
			mwt.updateUserAfterSave();

	}

}
