package org.ica.utilityClasses;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.ica.briquePackage.CompositionBriques;
import org.ica.briquePackage.Equation;
import org.ica.briquePackage.Parametre;
import org.ica.exceptions.MainApplicationException;
import org.ica.uiElements.AddNewGlobalEquation;
import org.ica.uiElements.MainWindowTabs;
import org.ica.uiElements.TempCanvas;


public class AddGlobalEquationToolItem implements SelectionListener {
	final private Shell shell;
	
	public AddGlobalEquationToolItem(Shell shell){
		this.shell = shell;
	}
	
	@Override
	public void widgetSelected(SelectionEvent e) {
		AddGlobalEquation.addEquation(shell);
	}

	@Override
	public void widgetDefaultSelected(SelectionEvent e) {
		widgetSelected(e);
	}

	public static class AddGlobalEquation{

		/**
		 * Replaces all matching parameters in the whole project
		 * @param mwt : The {@code MainWindowTabs} object which holds the project.
		 */
		public static void replaceMatchingParameters(MainWindowTabs mwt ) {
			Display.getDefault().asyncExec(()->{
				if(mwt == null) return;
				mwt.pack();
			
				CompositionBriques composition = mwt.getCompositionBriques();
				
				List<Equation> eqn = composition.getEquation();	
				MatchIdenticalParameters.matchIdenticalBriqueParameters(eqn);
			});

		}
		
		
		public static void addEquation(Shell shell){
			MainWindowTabs mwt = ObjectFactory.getMainWindowTabs(new FactoryTools(MainWindowTabs.class.getName()));
			if(mwt == null) return;
			if (mwt.canAddNewEquation()) {
				try {
					mwt.pack();
					List<Parametre> parametres = mwt.getParametreList();
					
					 List<String> paramNames = parametres.stream().map(p->{
						return p.getNomP();	
					}).collect(Collectors.toList());
					AddNewGlobalEquation globalEquation = new AddNewGlobalEquation(shell, paramNames);
					Object result = globalEquation.open();
					
					if (result == null) return;
					
					//if all conditions verified, create a new Equation component on the canvas
					if (result.equals(Boolean.TRUE)) {
						
						Equation equation =  globalEquation.getEquation();
						String toolTip = equation.getContenuEqn();
						TabFolder tabFolder = mwt.getMainTabFolder();
						
						//replace all matching params
						replaceMatchingParameters(mwt);
						
						if(tabFolder.isDisposed()) return;
						
						if(tabFolder.getItemCount() >0){
							Composite composite = (Composite) tabFolder.getItem(0).getControl();
							if (composite == null) return;						
					
						Rectangle rectangle = composite.getClientArea();
						//construct a smaller rectangle and place the UI representation of the equation into it
						Rectangle petitRectangle = new Rectangle(rectangle.x, rectangle.y, 
								(int) (rectangle.width/1.5), (int) (rectangle.height/1.5));
						Random random = new Random();
						
							int x = 0, y = 0, counter = 0;		
							do {
								if(counter>10) break;
								x =	random.nextInt(petitRectangle.width);
								y = random.nextInt(petitRectangle.height);
								counter++;
							} while (!petitRectangle.contains(x, y));			

							Point point = new Point(x, y);
							
						new TempCanvas(composite,equation, toolTip, point, false);
						
						}
					}
					
				} catch (MainApplicationException e1) {
					e1.printStackTrace();
				}
			}
		}
	}

}