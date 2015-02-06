package org.ica.uiElements;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Shell;

public abstract class EmulatedNativeCheckBoxLabelProvider 
  extends ColumnLabelProvider 
{
  private static final String CHECKED_KEY = "CHECKED";
  private static final String UNCHECK_KEY = "UNCHECKED";
 
  public EmulatedNativeCheckBoxLabelProvider(ColumnViewer viewer) {
	  
    if( JFaceResources.getImageRegistry().getDescriptor(CHECKED_KEY) == null ) {
      JFaceResources.getImageRegistry().put(UNCHECK_KEY, 
        makeShot(viewer.getControl().getShell(),false));
      JFaceResources.getImageRegistry().put(CHECKED_KEY,
        makeShot(viewer.getControl().getShell(),true));
    }
  }

  private Image makeShot(Shell shell, boolean type) {
    Shell s = new Shell(shell,SWT.NO_TRIM);
    Button b = new Button(s,SWT.CHECK);
    b.setSelection(type);
    Point bsize = b.computeSize(SWT.DEFAULT, SWT.DEFAULT);
    b.setSize(bsize);
    b.setLocation(0, 0);
    s.setSize(bsize);
    s.open();

    GC gc = new GC(b);
    Image image = new Image(shell.getDisplay(), bsize.x, bsize.y);
    gc.copyArea(image, 0, 0);
    gc.dispose();
    
    s.close();
    
    return image;
  }

  public Image getImage(Object element) {
    if( isChecked(element) ) {
      return JFaceResources.getImageRegistry().
               getDescriptor(CHECKED_KEY).createImage();
    } else {
      return JFaceResources.getImageRegistry().
               getDescriptor(UNCHECK_KEY).createImage();
    }
  }
  

  protected abstract boolean isChecked(Object element);
}
