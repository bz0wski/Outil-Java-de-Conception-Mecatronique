package org.ica.uiElements;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.OwnerDrawLabelProvider;
import org.eclipse.jface.viewers.ViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;

public abstract class CenteredCheckboxLabelProvider extends OwnerDrawLabelProvider {
    private static final String CHECKED_KEY = "CHECKED";
    private static final String UNCHECK_KEY = "UNCHECKED";

    public CenteredCheckboxLabelProvider(ColumnViewer viewer) {
        if (JFaceResources.getImageRegistry().getDescriptor(CHECKED_KEY) == null) {
            JFaceResources.getImageRegistry().put(UNCHECK_KEY, makeShot(viewer.getControl().getShell(), false));
            JFaceResources.getImageRegistry().put(CHECKED_KEY, makeShot(viewer.getControl().getShell(), true));
        }
    }

    private Image makeShot(Shell shell, boolean type) {
        Shell s = new Shell(shell, SWT.NO_TRIM);
        Button b = new Button(s, SWT.CHECK);
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
        if (isChecked(element)) {
            return JFaceResources.getImageRegistry().get(CHECKED_KEY);
        } else {
            return JFaceResources.getImageRegistry().get(UNCHECK_KEY);
        }
    }

    @Override
    protected void setOwnerDrawEnabled(ColumnViewer viewer,
    		ViewerColumn column, boolean enabled) {
    	// TODO Auto-generated method stub
    	super.setOwnerDrawEnabled(viewer, column, enabled);
    }
    @Override
    protected void measure(Event event, Object element) {
    }

    @Override
    protected void paint(Event event, Object element) {
        Image img = getImage(element);

        if (img != null) {
            Rectangle bounds = ((TableItem) event.item).getBounds(event.index);
            Rectangle imgBounds = img.getBounds();
            bounds.width /= 2;
            bounds.width -= imgBounds.width / 2;
            bounds.height /= 2;
            bounds.height -= imgBounds.height / 2;

            int x = bounds.width > 0 ? bounds.x + bounds.width : bounds.x;
            int y = bounds.height > 0 ? bounds.y + bounds.height : bounds.y;

            event.gc.drawImage(img, x, y);
        }
    }

    protected abstract boolean isChecked(Object element);
}