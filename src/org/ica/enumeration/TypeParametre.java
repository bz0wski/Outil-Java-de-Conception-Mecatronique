package org.ica.enumeration;

import java.util.ArrayList;
import java.util.List;

/**Parameter types <br>{@code INPUT,
		UNDETERMINED,
		OUTPUT}*/
public enum TypeParametre {
	
		INPUT,
		UNDETERMINED,
		OUTPUT;
	

private final List<SousTypeParametre> inputSubtypes = new ArrayList<SousTypeParametre>();
private final List<SousTypeParametre> undeterminedSubtypes =  new ArrayList<SousTypeParametre>();
private final List<SousTypeParametre> outputSubtypes =  new ArrayList<SousTypeParametre>(); 

private TypeParametre() {

	inputSubtypes.add(SousTypeParametre.CONSTANT);
	inputSubtypes.add(SousTypeParametre.RANGE);
	inputSubtypes.add(SousTypeParametre.SET);
	
	outputSubtypes.add(SousTypeParametre.OUTPUT);
	
	undeterminedSubtypes.add(SousTypeParametre.INPUT_UNDETERMINED);
	undeterminedSubtypes.add(SousTypeParametre.FREE);
	undeterminedSubtypes.add(SousTypeParametre.OUTPUT_UNDETERMINED);
	
}

/**
 * This method returns all the subtypes for this type object
 * @return The {@code List} of {@code SousTypeParametre}  for the {@code TypeParametre} object
 */
public List<SousTypeParametre> getSubtype(){
	List<SousTypeParametre> sstype = null;
	
	switch (this) {
	case INPUT:
	 sstype = inputSubtypes;
		break;
		
	case OUTPUT:
		sstype = outputSubtypes;
		break;
		
	case UNDETERMINED:
		sstype = undeterminedSubtypes;
		break;
		
	default:
		break;
	}
	
	return sstype;
	
}

}