package org.ica.utilityClasses;

import java.io.InputStream;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
/**
 * This class initialises and holds all the image resources used by the application.
 * 
@author Salim Ahmed

  */	

public class ImageHolder {
	private static Image lvl2packageIcon16;
	private static Image lvl1packageIcon16;
	private static Image compositionIcon16;
	private static Image brique16;
	
	private static Image lvl2packageIcon32;
	private static Image lvl1packageIcon32;
	private static Image compositionIcon32;
	private static Image brique32;
	
	private static Image lvl2packageIcon64;
	private static Image lvl1packageIcon64;
	private static Image compositionIcon64;
	
	private static Image compiler32;
	private static Image excelIcon32;
	private static Image ajouter32;
	private static Image supprimmer32;
	
	private static Image ok16;
	private static Image error16;
	private Image function32;
	
	private Image hoverClose;
	private Image properClose;
	public static ImageHolder imageHolder = null;
	
	/**Returns the Singleton instance of the ImageHolder class.
	 * @return
	 * 	 Imaholder instance*/
	public static ImageHolder getImageHolder() {
		if (imageHolder == null) {
			imageHolder = new ImageHolder();
		}
			return imageHolder;
		
	}

	
	private ImageHolder() {
try {		
			lvl2packageIcon16 = null;
			lvl1packageIcon16 = null;
			compositionIcon16 = null;
			setBrique16(null);
			
			lvl2packageIcon32 = createImage32("lvlTwo32.png");
			lvl1packageIcon32 = createImage32("lvlOne32.png");
			compositionIcon32 = createImage32("composition32.png");
			
			brique32 = null;
			
			lvl2packageIcon64 = null;
			lvl1packageIcon64 = null;
			compositionIcon64 = null;
			
			compiler32 = null;
			excelIcon32 = null;
			ajouter32 = null;
			supprimmer32 = null;
			
			hoverClose = null;
			properClose = null;
			
			ok16 = null;
			error16 = null;
			setFunction32(null);
			
		} catch (Exception e) {
			if (e instanceof NullPointerException) {
				System.err.println("null pointer");
				
			}
		}
	
	}
	/**
	 * Create the 16 pixel versions of the images.
	 * @param imagePath :  The name of the image resource.
	 * @return A new image object.
	 */
	private Image createImage16(String imagePath){
		Image image = null;
		InputStream stream = ClassLoader.getSystemResourceAsStream("org/ica/icons/"+imagePath);
		
		if ( imagePath == null ||stream == null) {
			
			System.err.println("Image couldn't be loaded "+imagePath);
			image = new Image(Display.getCurrent(), 16, 16);
		
			GC gc = new GC(image);
			gc.setAlpha(128);
			gc.drawRectangle(0, 0, 16, 16);
			
			gc.dispose();
		}
		else {
			image = new Image(Display.getCurrent(), ClassLoader.getSystemResourceAsStream("org/ica/icons/"+imagePath));
		}
	
		return image;
	}
	
	
	/**
	 * Create the 32 pixel versions of the images
	 * @param imagePath :  The name of the image resource.
	 * @return A new image object.
	 */
	private Image createImage32(String imagePath){
		Image image = null;
		InputStream stream = ClassLoader.getSystemResourceAsStream("org/ica/icons/"+imagePath);
		
		if ( imagePath == null ||stream == null) {
			
			System.err.println("Image couldn't be loaded "+imagePath);
			
			image = new Image(Display.getCurrent(), 32, 32);
		
			GC gc = new GC(image);
			gc.setAlpha(128);
			gc.drawRectangle(0, 0, 32, 32);
			
			gc.dispose();
			
		}
		else {
			image = new Image(Display.getCurrent(), ClassLoader.getSystemResourceAsStream("org/ica/icons/"+imagePath));
		}
	
		return image;
	}
	
	/**
	 * Create the 64 pixel versions of the images
	 * @param imagePath :  The name of the image resource.
	 * @return A new image object.
	 */
		private Image createImage64(String imagePath){
			Image image = null;
			InputStream stream = ClassLoader.getSystemResourceAsStream("org/ica/icons/"+imagePath);
			
			if ( imagePath == null ||stream == null) {
				
				System.err.println("Image couldn't be loaded "+imagePath);
				image = new Image(Display.getCurrent(), 64, 64);
			
				GC gc = new GC(image);
				gc.setAlpha(128);
				gc.drawRectangle(0, 0, 64, 64);
				
				gc.dispose();
			}
			else {
				image = new Image(Display.getCurrent(), ClassLoader.getSystemResourceAsStream("org/ica/icons/"+imagePath));
			}
		
			return image;
		}
	
	/**
		 * @return the brique16
		 */
		public Image getBrique16() {
			if (brique16 == null) {
				brique16 = createImage16("brique16.png");
			}
			return brique16;
		}


		/**
		 * @param brique16 the brique16 to set
		 */
		public static void setBrique16(Image brique16) {
			ImageHolder.brique16 = brique16;
		}


	/**
		 * @return the brique32
		 */
		public Image getBrique32() {
			if (brique32 == null) {
				brique32 = createImage32("brique32.png");
			}
			return brique32;
		}


	/**
	 * @return the lvl2packageIcon16
	 */
	public Image getLvl2packageIcon16() {
		if(lvl2packageIcon16 == null)
			lvl2packageIcon16 = createImage16("lvlTwo16.png");
		return lvl2packageIcon16;
	}

	/**
	 * @return the lvl1packageIcon16
	 */
	public Image getLvl1packageIcon16() {
		if(lvl1packageIcon16 == null)
			lvl1packageIcon16 = createImage16("lvlOne16.png");
		return lvl1packageIcon16;
	}

	/**
	 * @return the compositionIcon16
	 */
	public Image getCompositionIcon16() {
		if(compositionIcon16 == null)
			compositionIcon16 =  createImage16("composition16.png");
		return compositionIcon16;
	}

	/**
	 * @return the lvl2packageIcon32
	 */
	public Image getLvl2packageIcon32() {
		return lvl2packageIcon32;
	}

	/**
	 * @return the lvl1packageIcon32
	 */
	public Image getLvl1packageIcon32() {
		return lvl1packageIcon32;
	}

	/**
	 * @return the compositionIcon32
	 */
	public Image getCompositionIcon32() {
		return compositionIcon32;
	}


	/**
	 * @return the lvl2packageIcon64
	 */
	public Image getLvl2packageIcon64() {
		if (lvl2packageIcon64 == null) {
			lvl2packageIcon64 = createImage64(null);
		}
		return lvl2packageIcon64;
	}


	/**
	 * @return the lvl1packageIcon64
	 */
	public Image getLvl1packageIcon64() {
		if (lvl1packageIcon64 == null) {
			lvl1packageIcon64 = createImage64(null);
		}
		return lvl1packageIcon64;
	}


	/**
	 * @return the compositionIcon64
	 */
	public Image getCompositionIcon64() {
		if (compositionIcon64 == null) {
			compositionIcon64 = createImage64("composition64.png");
		}
		return compositionIcon64;
	}


	/**
	 * @return the compiler32
	 */
	public Image getCompiler32() {
		if (compiler32 == null) {
			compiler32 = createImage32("compiler32.png");
		}
		return compiler32;
	}


	/**
	 * @return the excelIcon32
	 */
	public Image getExcelIcon32() {
		if (excelIcon32 == null) {
			excelIcon32 = createImage32("excel-icon32.png");
		}
		return excelIcon32;
	}


	/**
	 * @return the ajouter32
	 */
	public Image getAjouter32() {
		if (ajouter32 == null) {
			ajouter32 = createImage32("ajouter24.png");
		}
		return ajouter32;
	}


	/**
	 * @return the supprimmer32
	 */
	public Image getSupprimmer32() {
		if (supprimmer32 == null) {
			supprimmer32 = createImage32("minus32.png");
		}
		return supprimmer32;
	}


	/**
	 * @return the close
	 */
	public Image getHoverClose() {
		if (hoverClose == null) {
			hoverClose = createImage16("circle_close_delete16.png");
		}
		return hoverClose;
	}

	public Image getProperClose() {
		if (properClose == null) {
			properClose = createImage16("close-circled16.png");
		}
		return properClose;
	}
	/**@param image
	 * Disposes the image passed as a parameter*/
	public void freeResource(Image image) {
		if (image !=null) {
			image.dispose();
		}
		
	}
	
	public Image getOk16() {
		if (ok16 == null) {
			ok16 = createImage16("ok16.png");
		}
		return ok16;
	}


	/**
	 * @return the error16
	 */
	public Image getError16() {
		if(error16 == null)
			error16 = createImage16("error16.png");
		return error16;
	}


	/**
	 * @return the function24
	 */
	public Image getFunction32() {
		if(function32 == null)
			function32 = createImage16("function32.png");
		return function32;
	}


	/**
	 * @param function24 the refresh24 to set
	 */
	public void setFunction32(Image function24) {
		this.function32 = function24;
	}

}
