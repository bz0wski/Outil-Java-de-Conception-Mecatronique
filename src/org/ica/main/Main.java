package org.ica.main;

import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.swt.widgets.Display;
import org.ica.uiElements.MainApplicationWindow;


public class Main {   
	public static void main(String[] args) throws Exception {
		
		final Display display = Display.getDefault();
		Realm.runWithDefault(SWTObservables.getRealm(display), new Runnable() {
			@Override
			public void run() {
				try {
					 MainApplicationWindow.getMainApplicationWindowInstance();
				} catch (Exception e) {
					e.printStackTrace();
				}
					

			}
		});

	}

}
