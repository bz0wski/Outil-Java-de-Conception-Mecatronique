package org.ica.uiElements;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Text;
import org.ica.briquePackage.Brique;
import org.ica.briquePackage.CompositionBriques;
import org.ica.utilityClasses.FactoryTools;
import org.ica.utilityClasses.ObjectFactory;

//In this class the Brique will get it's final validation
//Replacement of all alike parameters will be made in this class
//I will check that the equations and the parameters have been validated 
//before the brique will be actually validated.
public class InfoBriqueComposite extends Composite {
	private Text text;
	private String textString;
	
	public InfoBriqueComposite(Composite parent, int style) {
		super(parent, style);
		

		FormLayout InfoBrique_layout = new FormLayout();
		InfoBrique_layout.marginHeight = 10;
		InfoBrique_layout.marginWidth = 0;
		this.setLayout(InfoBrique_layout);
		
		
		createComponents(parent);
	}

	public void createComponents(Composite parent) {
		
		Label lblInfoBrique = new Label(this, SWT.NONE);
		FormData fd_lblInformationsSurLa = new FormData();
		fd_lblInformationsSurLa.top = new FormAttachment(0, 5);
		fd_lblInformationsSurLa.left = new FormAttachment(0, 10);
		lblInfoBrique.setLayoutData(fd_lblInformationsSurLa);
		lblInfoBrique.setText("Informations sur la Brique");
		
		text = new Text(this, SWT.BORDER | SWT.WRAP | SWT.H_SCROLL | SWT.V_SCROLL | SWT.CANCEL | SWT.MULTI);
		
		FormData fd_text = new FormData();
		fd_text.top = new FormAttachment(0, 30);
		fd_text.bottom = new FormAttachment(100,-40);
		fd_text.left = new FormAttachment(0, 10);
		fd_text.right = new FormAttachment(100, -10);
		text.setLayoutData(fd_text);
		
		
		
		Button btnTerminer = new Button(this, SWT.NONE);
		
		FormData fd_btnTerminer = new FormData();
	
		fd_btnTerminer.top = new FormAttachment(text,10,SWT.BOTTOM);
		fd_btnTerminer.bottom = new FormAttachment(100, -10);
		fd_btnTerminer.right = new FormAttachment(text, 0, SWT.CENTER);
		btnTerminer.setLayoutData(fd_btnTerminer);
		btnTerminer.setText("Terminer");
		btnTerminer.setToolTipText("Enregistrer Nouvelle Brique");
		
		btnTerminer.addListener(SWT.Selection,listener->{
			setTextString(text.getText());
			
			boolean doValidate = true;
			
			if(ObjectFactory.validationRegistry().size() == 2){
			
			Map<String, Boolean> validationMap = ObjectFactory.validationRegistry();
			for (Iterator<Map.Entry<String, Boolean>> iterator = validationMap.entrySet().iterator(); iterator.hasNext();) {
				Map.Entry<String, Boolean> entry = iterator.next();
				if (entry.getValue().equals(false)) {
					doValidate = false;
					StringBuilder errorMessage = new StringBuilder();
					errorMessage.append("Veuillez valider les ");
					errorMessage.append(entry.getKey().equals("parameterValidationOk")? "parametres.":"équations.");
					
					showError(parent,errorMessage.toString());
					break;
				}
			}
		
			if(doValidate){
			ObjectFactory.clearRegistry();
			Brique brique = ObjectFactory.getBrique(Brique.class.getName());
			brique.setInfoBrique(text.getText());
			
	
			
			MainApplicationWindowList mn = ObjectFactory.getMainApplicationWindowList(new FactoryTools(MainApplicationWindowList.class.getName()));
			ITreeSelection selection = (ITreeSelection)mn.getTreeViewer().getSelection();
		
			if (selection.getFirstElement() instanceof CompositionBriques) {
					System.out.println("selection is composition.");
					}
			
			
				CompositionBriques compo = ObjectFactory.getCompositionBriques
						(CompositionBriques.class.getName());
				if (compo != null) {
					System.out.println("Compo Recovered "+compo.hashCode());
					
					List<Brique> holder = compo.getBrique();
					if (!holder.contains(brique)) {
						holder.add(brique);	
					}					
					compo.setBrique(holder);
				}
				else {
					System.err.println("Error, La composition n'a pas été pu récupéré.");
				}
			
				parent.getShell().dispose();
				
			}
		}
			else {
				showError(parent, "Vous n'avez pas validé toutes les sections.");
			}
		
		});
	}

	private void showError(Composite parent, String errorMessage){
		MessageBox dialog =  new MessageBox(parent.getShell(), SWT.ICON_ERROR | SWT.OK);
		dialog.setText("Attention");
		dialog.setMessage(errorMessage);	
		// open dialog and await user selection
		dialog.open();
	}
	/**
	 * @return the textString
	 */
	public String getTextString() {
		return textString;
	}

	/**
	 * @param textString the textString to set
	 */
	public void setTextString(String textString) {
		this.textString = textString;
	}
}
