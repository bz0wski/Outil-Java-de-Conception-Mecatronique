package org.ica.uiElements;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.core.databinding.beans.BeansObservables;
import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.core.databinding.observable.map.IMapChangeListener;
import org.eclipse.core.databinding.observable.map.IObservableMap;
import org.eclipse.core.databinding.observable.map.MapChangeEvent;
import org.eclipse.core.databinding.observable.masterdetail.IObservableFactory;
import org.eclipse.core.databinding.observable.set.IObservableSet;
import org.eclipse.core.internal.databinding.observable.EmptyObservableList;
import org.eclipse.jface.databinding.viewers.ObservableListTreeContentProvider;
import org.eclipse.jface.databinding.viewers.TreeStructureAdvisor;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.ica.briquePackage.Brique;
import org.ica.briquePackage.CompositionBriques;
import org.ica.briquePackage.Equation;
import org.ica.briquePackage.Parametre;
import org.ica.utilityClasses.ObjectFactory;

public class ViewModelContents {
	private static final String[] parameterColumnNames = {"Brique","Equation","Nom Paramètre","Type Paramètre", "Sous Type","Valeur","Unité","Propriété","Description"} ;
	
	private final TreeViewer compositionTreeViewer;
	private final Tree compositionTree;
	private final List<Brique> backingList = new ArrayList<Brique>();
	private final WritableList writableListEquations;
	
	
	/**@param parent
	 * 		Parent composite on which the viewing window will be created
	 * @param object
	 * 		The Composition brique object which will be received for viewing.
	 * <br><br>
	 * This class will permit the user to view the contents of the currently selected 
	 * Brique, either by double clicking or by accessing it through the context menu.
	 * */
	public ViewModelContents(Composite parent, CompositionBriques object){
	
		ObjectFactory.addToOpenCompositionBriques(object);
		
		compositionTreeViewer = new TreeViewer(parent, SWT.FULL_SELECTION|SWT.SINGLE);
		compositionTree = compositionTreeViewer.getTree();
		
	
		compositionTree.setLinesVisible(false);
		compositionTree.setHeaderVisible(true);
		
		 
		 compositionTree.setLayout(new FormLayout());
		 //Setting the LayoutData
		
		compositionTree.setLayoutData(parent.getLayoutData());
		
		 //content provider must have been set when input is set.
		 ObservableListTreeContentProvider contentProvider = new ObservableListTreeContentProvider(new MyTreeListFactory(), new MyTreeStructureAdvisor());
		compositionTreeViewer.setContentProvider(contentProvider);
		
		
		writableListEquations = new WritableList(backingList,Brique.class);
		 //Setting contents
		compositionTreeViewer.setInput(writableListEquations);
								
		
		
		createContents(object, contentProvider, compositionTreeViewer);
		compositionTree.pack();
		writableListEquations.addAll(((CompositionBriques)object).getBrique());
		
		parent.addDisposeListener(d->{
			ObjectFactory.removeFromOpenCompositionBriques(object);
		});
	}
	
	public Tree getCompositionTree() {
		return compositionTreeViewer.getTree();
	}
	
	public void createContents(CompositionBriques compositionBriques,ObservableListTreeContentProvider contentProvider, TreeViewer viewer) {
		
					
					final TreeViewerColumn nomBrique = new TreeViewerColumn(viewer, SWT.NONE);
					nomBrique.getColumn().setWidth(100);
					nomBrique.getColumn().setMoveable(true);
					nomBrique.getColumn().setResizable(true);
					nomBrique.getColumn().setText(parameterColumnNames[0]);
					nomBrique.getColumn().pack();
					IObservableMap mapNomBrique = BeansObservables.observeMap(contentProvider.getKnownElements(), "nomBrique");
					nomBrique.setLabelProvider(new MyCustomLabelProvider(mapNomBrique) {
		
						@Override
						public void update(ViewerCell cell) {
							super.update(cell);
							if (cell.getElement() instanceof Brique) {
								Brique brique = (Brique)cell.getElement(); 
								StyledString styledString = new StyledString((String)brique.getNomBrique()
																!= null ? (String) brique.getNomBrique() : "");
								
								cell.setText(styledString.getString());
								cell.setStyleRanges(styledString.getStyleRanges());
							}
						}
					});
///////////////////////////////////////	nomEquation  column//////////////////////////////////////////////////////////////////////////////////////////////////////////////////			
					final TreeViewerColumn nomEquation = new TreeViewerColumn(viewer, SWT.NONE);
					nomEquation.getColumn().setWidth(100);
					nomEquation.getColumn().setMoveable(true);
					nomEquation.getColumn().setResizable(true);
					nomEquation.getColumn().setText(parameterColumnNames[1]);
					nomEquation.getColumn().pack();
					IObservableMap mapNomE = BeansObservables.observeMap(contentProvider.getKnownElements(), "contenuEqn");
					nomEquation.setLabelProvider(new MyCustomLabelProvider(mapNomE) {

						@Override
						public void update(ViewerCell cell) {
							super.update(cell);
							if (cell.getElement() instanceof Equation) {
								Equation equation = (Equation)cell.getElement(); 
								StyledString styledString = new StyledString(equation.getContenuEqn() 
																!= null ? (String) equation.getContenuEqn() : "");
								
								cell.setText(styledString.getString());
								cell.setStyleRanges(styledString.getStyleRanges());
							}
						}
					});
					

////////////////////////////////////Column nomParametre /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////		
					final TreeViewerColumn nomParametre = new TreeViewerColumn(viewer, SWT.NONE);
					nomParametre.getColumn().setMoveable(true);
					nomParametre.getColumn().setResizable(true);
					
					nomParametre.getColumn().setWidth(100);
					nomParametre.getColumn().setText(parameterColumnNames[2]);
					nomParametre.getColumn().pack();
					
					IObservableMap mapNom = BeansObservables.observeMap(contentProvider.getKnownElements(), "nomP");
					
					nomParametre.setLabelProvider(new MyCustomLabelProvider(mapNom) {

						@Override
						public void update(ViewerCell cell) {
							super.update(cell);
							if (cell.getElement() instanceof Parametre) {
								Parametre parametre = (Parametre)cell.getElement(); 
								StyledString styledString = new StyledString(parametre.getNomP() 
																!= null ? (String) parametre.getNomP() : "");
								
								cell.setText(styledString.getString());
								cell.setStyleRanges(styledString.getStyleRanges());
							}
						}
					});
					
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////// TYPE PARAMETRE ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////		

				final TreeViewerColumn typeParametre = new TreeViewerColumn(viewer, SWT.NONE);
				
				typeParametre.getColumn().setMoveable(true);
				typeParametre.getColumn().setResizable(true);
				
				typeParametre.getColumn().setWidth(100);
				typeParametre.getColumn().setText(parameterColumnNames[3]);
				typeParametre.getColumn().pack();
				IObservableMap mapType = BeansObservables.observeMap(contentProvider.getKnownElements(),"typeP");
				typeParametre.setLabelProvider(new MyCustomLabelProvider(mapType) {
				
				@Override
				public void update(ViewerCell cell) {
				super.update(cell);
				if (cell.getElement() instanceof Parametre) {
				Parametre parametre = (Parametre)cell.getElement(); 
				StyledString styledString = new StyledString(parametre.getTypeP().toString() 
				!= null ? (String) parametre.getTypeP().toString() : "");
				
				cell.setText(styledString.getString());
				cell.setStyleRanges(styledString.getStyleRanges());
				}
				}
				});
				
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

/////////////////////////////////////////////// Column sousTypeParametre ///////////////////////////////////////////////////////////////////////////////////////////////	
				
				final TreeViewerColumn sousTypeParametre = new TreeViewerColumn(viewer, SWT.NONE);
				
				sousTypeParametre.getColumn().setMoveable(true);
				sousTypeParametre.getColumn().setResizable(true);
				sousTypeParametre.getColumn().setWidth(100);
				sousTypeParametre.getColumn().setText(parameterColumnNames[4]);
				sousTypeParametre.getColumn().pack();
				
				IObservableMap mapSousType = BeansObservables.observeMap(contentProvider.getKnownElements(),"sousTypeP");
				sousTypeParametre.setLabelProvider(new MyCustomLabelProvider(mapSousType) {
				
				@Override
				public void update(ViewerCell cell) {
				super.update(cell);
				if (cell.getElement() instanceof Parametre) {
				Parametre parametre = (Parametre)cell.getElement(); 
				StyledString styledString = new StyledString(parametre.getSousTypeP().toString() 
				!= null ? (String) parametre.getSousTypeP().toString() : "");
				
				cell.setText(styledString.getString());
				cell.setStyleRanges(styledString.getStyleRanges());
				}
				}
				});
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

////////////////////////////////Column valeurParametre ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////	

				final TreeViewerColumn valeurParametre = new TreeViewerColumn(viewer, SWT.NONE);
				
				valeurParametre.getColumn().setMoveable(true);
				valeurParametre.getColumn().setResizable(true);
				valeurParametre.getColumn().setMoveable(true);
				valeurParametre.getColumn().setWidth(100);
				valeurParametre.getColumn().setText(parameterColumnNames[5]);
				valeurParametre.getColumn().pack();
				
				IObservableMap mapValeur = BeansObservables.observeMap(contentProvider.getKnownElements(),"valeurP");
				valeurParametre.setLabelProvider(new MyCustomLabelProvider(mapValeur) {
				
				@Override
				public void update(ViewerCell cell) {
				super.update(cell);
				if (cell.getElement() instanceof Parametre) {
				Parametre parametre = (Parametre)cell.getElement(); 
				StyledString styledString = new StyledString(parametre.getValeurP()
				!= null ? (String) parametre.getValeurP() : "");
				
				cell.setText(styledString.getString());
				cell.setStyleRanges(styledString.getStyleRanges());
				}
				}
				});

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
				
////////////////////////////////////////////////////////Column uniteParametre////////////////////////////////////////////////////////////////////////////////

				final TreeViewerColumn uniteParametre = new TreeViewerColumn(viewer, SWT.NONE);
				
				uniteParametre.getColumn().setMoveable(true);
				uniteParametre.getColumn().setResizable(true);
				uniteParametre.getColumn().setText(parameterColumnNames[6]);
				uniteParametre.getColumn().pack();
				
				IObservableMap mapUnite = BeansObservables.observeMap(contentProvider.getKnownElements(),"uniteP");
				uniteParametre.setLabelProvider(new MyCustomLabelProvider(mapUnite) {
				
				@Override
				public void update(ViewerCell cell) {
				super.update(cell);
				if (cell.getElement() instanceof Parametre) {
				Parametre parametre = (Parametre)cell.getElement(); 
				StyledString styledString = new StyledString(parametre.getUniteP()
				!= null ? (String) parametre.getUniteP() : "");
				
				cell.setText(styledString.getString());
				cell.setStyleRanges(styledString.getStyleRanges());
				}
				}
				});
////////////////////////////////////////////////////////Column descriptionParametre////////////////////////////////////////////////////////////////////////////////

			final TreeViewerColumn descriptionParametre = new TreeViewerColumn(viewer, SWT.NONE);
			
			descriptionParametre.getColumn().setMoveable(true);
			descriptionParametre.getColumn().setResizable(true);
			descriptionParametre.getColumn().setText(parameterColumnNames[8]);
			descriptionParametre.getColumn().pack();
			
			IObservableMap mapDescription = BeansObservables.observeMap(contentProvider.getKnownElements(),"uniteP");
			descriptionParametre.setLabelProvider(new MyCustomLabelProvider(mapDescription) {
			
			@Override
			public void update(ViewerCell cell) {
			super.update(cell);
			if (cell.getElement() instanceof Parametre) {
			Parametre parametre = (Parametre)cell.getElement(); 
			StyledString styledString = new StyledString(parametre.getDescP()
			!= null ? (String) parametre.getDescP() : "");
			
			cell.setText(styledString.getString());
			cell.setStyleRanges(styledString.getStyleRanges());
			}
			}
			});
			
////////////////////////////////////////////////////////Column proprieteParametre////////////////////////////////////////////////////////////////////////////////

		final TreeViewerColumn proprieteParametre = new TreeViewerColumn(viewer, SWT.NONE);
		
		proprieteParametre.getColumn().setMoveable(true);
		proprieteParametre.getColumn().setResizable(true);
		proprieteParametre.getColumn().setText(parameterColumnNames[7]);
		proprieteParametre.getColumn().pack();
		
		IObservableMap mapPropriete = BeansObservables.observeMap(contentProvider.getKnownElements(),"uniteP");
		proprieteParametre.setLabelProvider(new MyCustomLabelProvider(mapPropriete) {
		
		@Override
		public void update(ViewerCell cell) {
		super.update(cell);
		if (cell.getElement() instanceof Parametre) {
		Parametre parametre = (Parametre)cell.getElement(); 
		StyledString styledString = new StyledString(parametre.getProprieteP()
		!= null ? (String) parametre.getProprieteP() : "");
		
		cell.setText(styledString.getString());
		cell.setStyleRanges(styledString.getStyleRanges());
		}
		}
		});
		
		
	}	

	private class  MyTreeStructureAdvisor extends TreeStructureAdvisor{
		@Override
		public Object getParent(Object element) {

			return super.getParent(element);
		}
		
		@Override
		public Boolean hasChildren(Object element) {
			
			if (element instanceof IObservableList) {
				return ((IObservableList)element).size() > 0;
			}
			if (element instanceof IObservableSet) {
				return ((IObservableSet)element).size() > 0;
			}
			if (element instanceof Brique) {
				return ((Brique)element).getListEquations().size() > 0;
			}
			if (element instanceof Equation) {					
					return ((Equation)element).getListeDeParametresEqn().size() > 0;				
			}
			if (element instanceof List<?>) {
				return ((List<?>)element).size()>0;
				
			}
		
			return false;
		}
		
	}

	
	private class MyTreeListFactory implements IObservableFactory{
		
		@Override
		public IObservable createObservable(Object target) {
		
			if (target instanceof Equation) {
				 return BeansObservables.observeList(Realm.getDefault(), target, "listeDeParametresEqn");
			}
			if (target instanceof Brique) {
				return BeansObservables.observeList(Realm.getDefault(), target, "listEquations");
			}
			if (target instanceof IObservableList) {
				return (IObservableList)target;
			}	
			if (target instanceof WritableList) {				
				return (WritableList)target;
				}
			return new EmptyObservableList(Realm.getDefault(),null);
		
		}	
			
	}
	
	private class MyCustomLabelProvider extends StyledCellLabelProvider {
/*
		@Override
		public void update(ViewerCell cell) {
			super.update(cell);
		}
	*/	
		private IMapChangeListener mapChangeListener = 
				new IMapChangeListener() {
		
					@Override
					public void handleMapChange(MapChangeEvent event) {
						Set<?> affectedElements = event.diff.getChangedKeys();
						
						if (!affectedElements.isEmpty()) {
							LabelProviderChangedEvent newEvent = 
									new LabelProviderChangedEvent(MyCustomLabelProvider.this, affectedElements.toArray());
							fireLabelProviderChanged(newEvent);
							
						}
						
					}
				};
				
		public MyCustomLabelProvider(IObservableMap attributeMaps){					
			attributeMaps.addMapChangeListener(mapChangeListener);										
			}	
	/*
		public MyCustomLabelProvider(IObservableMap ... attributeMaps){
			for (int i = 0; i < attributeMaps.length; i++) {
				attributeMaps[i].addMapChangeListener(mapChangeListener);
			}
		}
		*/		
		
	}
}
