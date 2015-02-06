package org.ica.uiElements;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import org.eclipse.swt.dnd.ByteArrayTransfer;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.TransferData;
import org.ica.briquePackage.Brique;

public class CustomTransferType_BRIQUE extends ByteArrayTransfer {


	private static final String BASIC_BRIQUE = "Basic Brique";
	private static final int BASIC_BRIQUE_ID = registerType(BASIC_BRIQUE);
	private static CustomTransferType_BRIQUE _instance = new CustomTransferType_BRIQUE();

	/**
	 * @return the only CustomTransferType_BRIQUE Instance
	 */
	public static CustomTransferType_BRIQUE getInstance() {
		return _instance;
	}
	
	@Override
	protected void javaToNative(Object object, TransferData transferData) {
		if (!checkMyType(object) || !isSupportedType(transferData)) {
			DND.error(DND.ERROR_INVALID_DATA);
		}
		Brique brique = (Brique)object;
		try(ByteArrayOutputStream outputStream  = new ByteArrayOutputStream();
				ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream)) {			

				objectOutputStream.writeObject(brique);
			
			byte[] buffer = outputStream.toByteArray();
			super.javaToNative(buffer, transferData);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	/*	Brique [] briques = (Brique[])object;
		try(ByteArrayOutputStream outputStream  = new ByteArrayOutputStream();
				ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream)) {			
			
			for (int i = 0, length = briques.length; i < length; i++) {
				objectOutputStream.writeObject(briques[i]);
			}
			byte[] buffer = outputStream.toByteArray();
			super.javaToNative(buffer, transferData);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		*/
		
	}
	@Override
	protected Object nativeToJava(TransferData transferData) {
		
		if (isSupportedType(transferData)) {
			byte [] buffer = (byte[])super.nativeToJava(transferData);
			if (buffer == null) {
				return null;
			}
		
			Brique brique = null;
			try(ByteArrayInputStream inputStream = new ByteArrayInputStream(buffer);
					ObjectInputStream objectInputStream = new ObjectInputStream(inputStream)) {
				
				brique = (Brique) objectInputStream.readObject();
		/*	//	while (objectInputStream.readObject() != null) {
					
				//	Brique [] tempB = new Brique[briques.length+1];
				//	System.arraycopy(briques, 0, tempB, 0, briques.length);
				//	tempB[briques.length] = brique;
				//	briques = tempB;
			*/	
				
			} catch (Exception e) {
				e.printStackTrace();
			}
			return brique;
		}
		
		return null;
		
	}
	
	
	@Override
	protected int[] getTypeIds() {
		return new int [] {BASIC_BRIQUE_ID};
	}

	@Override
	protected String[] getTypeNames() {
		
		return new String [] {BASIC_BRIQUE};
	}

	boolean checkMyType(Object object){
		if (object == null || !(object instanceof Brique) ) {
			return false;
		}
		Brique brique = (Brique)object;
			
		if (brique == null ||
					brique.getNomBrique().isEmpty() || brique.getListParametres().isEmpty()) {
					return false;
				}					
	//	Brique[] briques = (Brique[])object;
	//	for (int i = 0; i < briques.length; i++) {
	//		if (briques[i] == null ||
				//	briques[i].getNomBrique().isEmpty() || briques[i].getListParametres().isEmpty()) {
		//		return false;
		//	}
	//	}
		return true;
	}

	@Override
	protected boolean validate(Object object) {
		
		return checkMyType(object);
	}
}
