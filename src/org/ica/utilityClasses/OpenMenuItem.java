package org.ica.utilityClasses;


import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.ica.uiElements.MainWindowTabs;

public class OpenMenuItem implements SelectionListener{
	private final Shell shell;
	public OpenMenuItem(Shell shell){
		this.shell = shell;
	}
	@Override
	public void widgetSelected(SelectionEvent e) {
		FileDialog fileDialog = new FileDialog(shell, SWT.OPEN);
		fileDialog.setFilterExtensions(new String[]{"*.xml"});
		MainWindowTabs mwt = ObjectFactory.getMainWindowTabs(new FactoryTools(MainWindowTabs.class.getName()));
		String savepath = fileDialog.open();
		String fileName = fileDialog.getFileName();
		
		if(savepath == null) return;
		//call the guy responsible for reading the received file and putting it on the composite.
		Map<String, String> data = new HashMap<String, String>();
		data.put("fileName", fileName);
		data.put("saveLocation", savepath);
		mwt.openNewProject(data);
	}

	@Override
	public void widgetDefaultSelected(SelectionEvent e) {

		FileDialog fileDialog = new FileDialog(shell, SWT.OPEN);
		fileDialog.setFilterExtensions(new String[]{"*.xml"});
		MainWindowTabs mwt = ObjectFactory.getMainWindowTabs(new FactoryTools(MainWindowTabs.class.getName()));
		String savepath = fileDialog.open();
		String fileName = fileDialog.getFileName();
		
		if(savepath == null) return;
		//call the guy responsible for reading the received file and putting it on the composite.
		Map<String, String> data = new HashMap<String, String>();
		data.put("fileName", fileName);
		data.put("saveLocation", savepath);
		mwt.openNewProject(data);
	}

}
