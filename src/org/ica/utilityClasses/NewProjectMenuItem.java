package org.ica.utilityClasses;

import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.ica.uiElements.MainWindowTabs;

public class NewProjectMenuItem implements SelectionListener {

	@Override
	public void widgetSelected(SelectionEvent e) {
		MainWindowTabs mwt = ObjectFactory.getMainWindowTabs(new FactoryTools(MainWindowTabs.class.getName()));
		mwt.createNewProject();

	}

	@Override
	public void widgetDefaultSelected(SelectionEvent e) {
		MainWindowTabs mwt = ObjectFactory.getMainWindowTabs(new FactoryTools(MainWindowTabs.class.getName()));
		mwt.createNewProject();
	}

}
