package org.ica.utilityClasses;

import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.ica.briquePackage.CompositionBriques;
import org.ica.uiElements.AfficherEtModifierBriqueContents.VerifyBeforeValidation;
import org.ica.uiElements.MainApplicationWindowContents;
import org.ica.uiElements.MainWindowTabs;

/**
 * Listener for when the compile button is pressed*/
public class CompileToolItem implements SelectionListener{

	@Override
	public void widgetSelected(SelectionEvent e) {
		//first of all check that the canvas is not empty and that there's actually a project on it.
		//second, if first test doesn't pass, indicate to the user, else proceed to compile the project.

		MainWindowTabs mwt = ObjectFactory.getMainWindowTabs(new FactoryTools(MainWindowTabs.class.getName()));
		if (mwt == null) return;
		CompositionBriques compositionBriques = mwt.organiseProjectforProcessing();
		if (compositionBriques != null) {

			//Before compiling, verify that there're no errors in the project
			VerifyBeforeValidation.verifyBeforeValidate(compositionBriques.getEquation());
			if(VerifyBeforeValidation.isValid()){
				MainApplicationWindowContents contents = ObjectFactory.getMainApplicationWindowContents(
						MainApplicationWindowContents.class.getName());
				if(contents != null)	{	
					contents.showProgressBar();
					contents.disableCompileToolItem();
				}
				//Replace all matching Parameters in the composition
				MatchIdenticalParameters.matchIdenticalBriqueParameters(compositionBriques.getEquation());
				new ExportToMatlab(compositionBriques);
				//System.err.println("Composition briques after produceMainMatrix "+compositionBriques.getEquation());
			}
			else {

			}


		}
	}

	@Override
	public void widgetDefaultSelected(SelectionEvent e) {
		
	}

}
