package org.ica.utilityClasses;

import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.ica.uiElements.MainWindowTabs;

public class SaveMenuItem implements SelectionListener {
	
	@Override
	public void widgetSelected(SelectionEvent e) {
		MainWindowTabs mwt = ObjectFactory.getMainWindowTabs(new FactoryTools(MainWindowTabs.class.getName()));	
		 if ( mwt.saveProject()) {
			 mwt.updateUserAfterSave();
			
		}
	}

	@Override
	public void widgetDefaultSelected(SelectionEvent e) {
		MainWindowTabs mwt = ObjectFactory.getMainWindowTabs(new FactoryTools(MainWindowTabs.class.getName()));
		
		 if (mwt.saveProject()) {
			 mwt.updateUserAfterSave();
			
		}

	}

}
