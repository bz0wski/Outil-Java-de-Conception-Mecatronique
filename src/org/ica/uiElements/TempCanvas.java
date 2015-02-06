package org.ica.uiElements;




import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.ica.briquePackage.Brique;
import org.ica.briquePackage.CompositionBriques;
import org.ica.briquePackage.Equation;
import org.ica.briquePackage.Parametre;
import org.ica.utilityClasses.FactoryTools;
import org.ica.utilityClasses.ImageHolder;
import org.ica.utilityClasses.ObjectFactory;


/***This class is responsible for drawing the visual representation of the project components.
 * @author Salim Ahmed*/
public class TempCanvas extends Canvas{
	/**@attribute dataObject : Holds a reference to the actual data contained, could be an {@code Equation}, {@code Brique } or {@code CompositionBriques}*/
	private final Object dataObject;
	/**
	 * This attribute allows to access the images.
	 */
	private final ImageHolder imageHolder;
	/**@attribute myPoint : the point at which the UI representation will be drawn.*/
	private Point myPoint;

	/**
	 * Constructor for drawing a visual representation of an object
	 * @param composite : Parent composite onto which to draw this UI representation
	 * @param data : Holds a reference to the actual data contained by the UI representation, (Brique, CompositionBriques,... )
	 * @param toolTip : Tooltip text to display
	 * @param point :  the point at which the UI representation will be drawn
	 * @param convertPoint : boolean value which indicates whether the point attribute will be converted to the parent composite's coordinates.
	 */
	public TempCanvas(Composite composite, Object data, String toolTip, Point point, boolean convertPoint) {
		super(composite, SWT.NONE);


		setLayout(new FormLayout());
		imageHolder = ImageHolder.getImageHolder();
		dataObject = data;

		setLayoutData(new FormData(SWT.DEFAULT, SWT.DEFAULT));
		if (convertPoint) 
			myPoint = toControl(point);			
		else 
			myPoint = point;



		setBackground(getParent().getBackground());

		setParent(composite);

		setBounds(myPoint.x ,myPoint.y, 50, 60);

		setToolTipText(toolTip);

		createDragSupport();		
		createContextMenu();

		addListener(SWT.MouseEnter, listener->{
			setFocus();
			setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_LIST_SELECTION));
		});

		addListener(SWT.MouseExit, listener->{

			setBackground(getParent().getBackground());
		});

		addListener(SWT.MouseDoubleClick, listener->{
			MainWindowTabs tabs = ObjectFactory.getMainWindowTabs(new FactoryTools(MainWindowTabs.class.getName()));
			if(tabs != null)
				tabs.createNewTabItem(getDataObject());
		});

		addListener(SWT.Paint, e-> {							
			Rectangle rectangle = getClientArea();


			if (this.getDataObject() instanceof CompositionBriques) {
				String string = ((CompositionBriques)this.getDataObject()).getNomComposition();

				Rectangle imageRectangle =   imageHolder.getCompositionIcon32().getBounds();
				int xPosition = (rectangle.width-imageRectangle.width)/2;
				e.gc.drawImage(imageHolder.getCompositionIcon32(), xPosition, 0);

				Point stringPoint = e.gc.stringExtent(string);
				xPosition = (rectangle.width - stringPoint.x) / 2;

				if(stringPoint.x >rectangle.width){
					string.substring(0, 0);							
					e.gc.drawString(string, 0, imageRectangle.height+2, true);
					computeSize(stringPoint.x,SWT.DEFAULT, true );
					setSize(stringPoint.x, rectangle.height);
					getParent().layout(true, true);

					update();

				}

				if ( (xPosition+stringPoint.x) < rectangle.width) {

					e.gc.drawString(string, xPosition, imageRectangle.height+2, true);
				}
				else {
					e.gc.drawString(string, 0, imageRectangle.height+2, true);
				}


				e.gc.dispose();
			}
			else if (getDataObject() instanceof Equation) {
				String string = ((Equation)this.getDataObject()).getContenuEqn();
				Rectangle imageRectangle =   imageHolder.getFunction32().getBounds();
				int xPosition = (rectangle.width-imageRectangle.width)/2;
				e.gc.drawImage(imageHolder.getFunction32(), xPosition, 0);
				Point stringPoint = e.gc.stringExtent(string);
				xPosition = (rectangle.width - stringPoint.x) /2;

				if(stringPoint.x>rectangle.width){

					e.gc.drawString(string, 0, imageRectangle.height+5, true);
					computeSize(stringPoint.x,SWT.DEFAULT, true );
					setSize(stringPoint.x, rectangle.height);
					getParent().layout(true, true);
					update();						

				}

				if ( (xPosition+stringPoint.x) < rectangle.width) {

					e.gc.drawString(string, xPosition, imageRectangle.height+2, true);
				}
				else {
					e.gc.drawString(string, 0, imageRectangle.height+2, true);
				}


				e.gc.dispose();


			}
			else {
				String string = ((Brique)this.getDataObject()).getNomBrique();


				Rectangle imageRectangle =   imageHolder.getBrique32().getBounds();
				int xPosition = (rectangle.width-imageRectangle.width)/2;
				e.gc.drawImage(imageHolder.getBrique32(), xPosition, 0);

				Point stringPoint = e.gc.stringExtent(string);
				xPosition = (rectangle.width - stringPoint.x) /2;



				if(stringPoint.x>rectangle.width){

					e.gc.drawString(string, 0, imageRectangle.height+5, true);
					computeSize(stringPoint.x,SWT.DEFAULT, true );
					setSize(stringPoint.x, rectangle.height);
					getParent().layout(true, true);
					update();						

				}

				if ( (xPosition+stringPoint.x) < rectangle.width) {

					e.gc.drawString(string, xPosition, imageRectangle.height+2, true);
				}
				else {
					e.gc.drawString(string, 0, imageRectangle.height+2, true);
				}


				e.gc.dispose();

			}      

		});
	}

	public Object getDataObject(){

		return dataObject;
	}


	private void createContextMenu(){
		MainWindowTabs mwt = ObjectFactory.getMainWindowTabs(new FactoryTools(MainWindowTabs.class.getName()));

		final Action renommer = new Action() {
			@Override
			public void run() {					
				super.run();
				StructuredSelection selection = new StructuredSelection(TempCanvas.this);
				TempCanvas tp =  (TempCanvas) selection.getFirstElement(); 
				NewElementCreation changeName = new NewElementCreation(TempCanvas.this.getShell(),
						SWT.DIALOG_TRIM | SWT.PRIMARY_MODAL, tp);
				try {
					changeName.open();
					TempCanvas.this.redraw();

					if (mwt != null) {
						mwt.modified();
						mwt.refreshSecondTab();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		};


		final Action supprimmer = new Action() {
			@Override
			public void run() {					
				super.run();		

				StructuredSelection selection = new StructuredSelection(TempCanvas.this);
				TempCanvas tp =  (TempCanvas) selection.getFirstElement();
				if (mwt != null) {
					mwt.closeTabItem(tp.getDataObject());
					mwt.modified();
				}
				tp.dispose();
				tp = null;				
				mwt.pack();
			}
		};


		final Action modifier = new Action() {
			@Override
			public void run() {					
				super.run();		

				StructuredSelection selection = new StructuredSelection(TempCanvas.this);
				TempCanvas tp =  (TempCanvas) selection.getFirstElement();
				if (mwt != null) {
					Object object = tp.getDataObject();
					if (object instanceof Equation) {
						Equation equation = (Equation)object;
						mwt.pack();
						List<Parametre> parametres = mwt.getParametreList();

						List<String> suggestions = parametres.stream().map(p->{
							return p.getNomP();	
						}).collect(Collectors.toList());

						AddNewGlobalEquation globalEquation = new AddNewGlobalEquation(TempCanvas.this.getShell(), suggestions, equation, true);
						Object result = globalEquation.open();
						//System.out.println("List before"+equation.getListeDeParametresEqn());
						if(result != Boolean.TRUE) return;
						TempCanvas.this.redraw();
						mwt.pack();						
						mwt.modified();
						mwt.refreshSecondTab();
					AfficherEtModifierEquation modifierEquation = 	ObjectFactory.getAfficherEtModifierEquation();
					if (modifierEquation != null)					
						//reload table
						modifierEquation.refreshTableContent(equation.getListeDeParametresEqn());
					
					}

				}


			}
		};
		addMenuDetectListener(listener->{

			MenuManager menuManager = new MenuManager();
			menuManager.setRemoveAllWhenShown(true);
			menuManager.addMenuListener(manager->{
				if(getDataObject() instanceof Equation){
					menuManager.add(modifier);
					modifier.setText("Modifier \tCtrl+R");
					modifier.setAccelerator(SWT.CTRL|'R');
				}else {
					menuManager.add(renommer);
					renommer.setText("Renommer \tCtrl+R");
					renommer.setAccelerator(SWT.CTRL|'R');
				}


				menuManager.add(supprimmer);
				supprimmer.setText("Supprimmer \tCtrl+DEL");
				supprimmer.setAccelerator(SWT.CTRL|SWT.DEL);
			});
			setMenu(menuManager.createContextMenu(this));
		});

	}

	private void createDragSupport(){
		int operations = DND.DROP_DEFAULT|DND.DROP_COPY | DND.DROP_MOVE;
		DragSource dragSource = new DragSource(this,operations);

		final CustomTransferType_BRIQUE customType = CustomTransferType_BRIQUE.getInstance();
		final TextTransfer textTransfer = TextTransfer.getInstance();
		final CustomTransferType_COMPOSITION customTypeTwo = CustomTransferType_COMPOSITION.getInstance();


		final LocalSelectionTransfer selection = LocalSelectionTransfer.getTransfer();

		Transfer [] transferAgents = new Transfer[]{customType,customTypeTwo, textTransfer, selection};
		dragSource.setTransfer(transferAgents);

		dragSource.addDragListener(new DragSourceListener() {
			StructuredSelection xtx = new StructuredSelection(TempCanvas.this);
			@Override
			public void dragStart(DragSourceEvent event) {
				//	System.out.println("Inner drag start x: "+event.x+" y: "+event.y+xtx.getFirstElement().getClass().getName());
				if ( ((TempCanvas)xtx.getFirstElement()).getDataObject() instanceof CompositionBriques) {
					event.image = imageHolder.getCompositionIcon32();

					event.offsetX = -10;
				}
				else {
					event.image = imageHolder.getBrique32();
					event.offsetX = -10;
				}

			}


			@Override
			public void dragSetData(DragSourceEvent event) {
				final LocalSelectionTransfer selection = LocalSelectionTransfer.getTransfer();


				selection.setSelection(xtx);
				if (xtx.getFirstElement() != null) {
					// event.data = (TempCanvas)xtx.getFirstElement();				
				}


			}

			@Override
			public void dragFinished(DragSourceEvent event) {
				//	System.out.println(event.detail);
				TempCanvas.this.dispose();
				xtx = null;
			}
		});


	}

}