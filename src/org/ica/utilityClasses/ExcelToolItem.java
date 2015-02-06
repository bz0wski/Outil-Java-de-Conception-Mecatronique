package org.ica.utilityClasses;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.ica.briquePackage.CompositionBriques;
import org.ica.uiElements.MainApplicationWindowContents;
import org.ica.uiElements.MainWindowTabs;

public class ExcelToolItem implements SelectionListener {

	@Override
	public void widgetSelected(SelectionEvent e) {

		MainWindowTabs mwt = ObjectFactory.getMainWindowTabs(new FactoryTools(MainWindowTabs.class.getName()));
		if (mwt == null) return;
		CompositionBriques compositionBriques = mwt.organiseProjectforProcessing();
		
		if (compositionBriques != null) {

			System.out.println("printing...");
			ExecutorService executor = Executors.newSingleThreadExecutor();
			Runnable runnable = new Runnable() {
				public void run() {
					MainApplicationWindowContents contents = ObjectFactory.getMainApplicationWindowContents(
							MainApplicationWindowContents.class.getName());
					String location = mwt.getSaveLocation();
				
					location = location.replaceAll("(.+)(.xml)", "$1.xlsx");

					PrintResultsToExcel print = new PrintResultsToExcel(compositionBriques, location);
					if(contents != null){
						if(print.isPrintSuccess())
							contents.updateUser("Fichier excel generé avec succès.", ImageHolder.getImageHolder().getOk16());
						else
							contents.updateUser(print.getPrintError(), ImageHolder.getImageHolder().getError16());
					}
						
				}
			};
			executor.execute(runnable);
			executor.shutdown();
		}

	}
	
	@Override
	public void widgetDefaultSelected(SelectionEvent e) {
		// TODO Auto-generated method stub

	}

}
