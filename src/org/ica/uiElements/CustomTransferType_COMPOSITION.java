package org.ica.uiElements;




import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.eclipse.swt.dnd.ByteArrayTransfer;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.TransferData;
import org.ica.briquePackage.CompositionBriques;

public class CustomTransferType_COMPOSITION extends ByteArrayTransfer{
	
	private static final String COMPOSITION = "Composition";
	private static final int COMPOSITION_ID = registerType(COMPOSITION);
	private static CustomTransferType_COMPOSITION _instance = new CustomTransferType_COMPOSITION();

	/**
	 * @return the only CustomTransferType_COMPOSITION Instance
	 */
	public static CustomTransferType_COMPOSITION getInstance() {
		return _instance;
	}

	@Override
	protected void javaToNative(Object object, TransferData transferData) {
		if (!checkMyType(object) || !isSupportedType(transferData)) {
			DND.error(DND.ERROR_INVALID_DATA);
		}
		
		CompositionBriques compositionBriques = (CompositionBriques)object;
		try(ByteArrayOutputStream compoOutputStream  = new ByteArrayOutputStream();
				ObjectOutputStream objectOutputStream_Compo = new ObjectOutputStream(compoOutputStream)) {			

			objectOutputStream_Compo.writeObject(compositionBriques);

			byte[] buffer = compoOutputStream.toByteArray();
			super.javaToNative(buffer, transferData);

		} catch (Exception e) {
			e.printStackTrace();
		}


	}
	@Override
	protected Object nativeToJava(TransferData transferData) {
		if (isSupportedType(transferData)) {
			byte [] buffer = (byte[])super.nativeToJava(transferData);
			if (buffer == null) {
				return null;
			}
			
			CompositionBriques compositionBriques = null;
			try(ByteArrayInputStream inputStream = new ByteArrayInputStream(buffer);
					ObjectInputStream objectInputStream = new ObjectInputStream(inputStream)) {

				compositionBriques = (CompositionBriques) objectInputStream.readObject();
				

			} catch (Exception e) {
				e.printStackTrace();
			}
			return compositionBriques;
		}

		return null;

	}


	@Override
	protected int[] getTypeIds() {
		return new int [] {COMPOSITION_ID};
	}

	@Override
	protected String[] getTypeNames() {

		return new String [] {COMPOSITION};
	}

	boolean checkMyType(Object object){
		if (object == null || !(object instanceof CompositionBriques) ) {
			return false;
		}
		

							
	
		return true;
	}

	@Override
	protected boolean validate(Object object) {

		return checkMyType(object);
	}
}
