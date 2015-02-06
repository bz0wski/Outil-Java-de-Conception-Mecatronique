package org.ica.uiElements;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.ica.briquePackage.Equation;
import org.ica.briquePackage.Parametre;
import org.ica.enumeration.SousTypeParametre;
import org.ica.enumeration.TypeParametre;
import org.ica.exceptions.MainApplicationException;
import org.ica.main.Observer;



public class ParametreDeSortieEditingSupport extends EditingSupport implements Observer{

	final TableViewer viewer;

	final String [] parameterStrings = {""};
	private String [] parameters = {""};
	private ComboBoxCellEditor editor;
	static ContenuEquationEditingSupport eqn;
	static int tableItemCount;
	
	static private Map<Integer, String> equationsList;

	
	public ParametreDeSortieEditingSupport(ColumnViewer viewer) {
		super(viewer);
		this.viewer = (TableViewer) viewer;	
		this.editor = new ComboBoxCellEditor(((TableViewer)viewer).getTable(),parameterStrings,SWT.SINGLE|SWT.READ_ONLY);
		equationsList = new HashMap<Integer, String>();
		
	}

	@Override
	protected CellEditor getCellEditor(Object element) {
		if (element instanceof Equation) {
			if(((Equation)element).getContenuEqn().length()>0){
			final List<Parametre> paramsSet = ((Equation)element).getListeDeParametresEqn();
			
			parameters = new String[paramsSet.size()]; 
			int i = 0;
			
			Iterator<Parametre> iterator = paramsSet.iterator();
			while(iterator.hasNext() && i<paramsSet.size()){
				Parametre param = (Parametre)iterator.next();
				parameters[i] = param.getNomP();
				i++;	}
			editor =  new ComboBoxCellEditor(viewer.getTable(),parameters,SWT.SINGLE|SWT.READ_ONLY);
			viewer.update(element, null);
			return editor;
			}
			else {
				return new ComboBoxCellEditor(viewer.getTable(),parameters,SWT.SINGLE|SWT.READ_ONLY);
			}
		}
		else
			{
				return new ComboBoxCellEditor(viewer.getTable(),parameters,SWT.SINGLE|SWT.READ_ONLY);
			}
		}
	
	@Override
	protected boolean canEdit(Object element) {
		if (element instanceof Equation) {
			if(((Equation)element).isOriented())
				return true;
		}
		return false;
	}

	@Override
	protected Object getValue(Object element) {
		
		return 0;
	}


	@Override
	protected void setValue(Object element, Object value) {
		if (parameters != null && parameters.length>0) {
			String paramString = parameters[Integer.valueOf(String.valueOf(value))];
			Parametre parametre = ((Equation)element).getParametreByName(paramString);
			if (parametre != null) {
				 
				 try {
					 parametre.setTypeP(TypeParametre.OUTPUT);
					parametre.setSousTypeP(SousTypeParametre.OUTPUT);
					((Equation) element).setParametreDeSortie(parametre);
				} catch (MainApplicationException e) {
					e.printStackTrace();
				}
				
			}
			
		}

		getViewer().update(element, null);
		
	}


	@Override
	public void update(int ItemCount, Map<Integer, String> equations) {
		equationsList = equations;
		tableItemCount = ItemCount;
	}
	
	public static Map<Integer, String> getEquationList() {
		return equationsList;
	}
}
