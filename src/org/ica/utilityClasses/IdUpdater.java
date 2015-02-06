package org.ica.utilityClasses;

import org.ica.briquePackage.Brique;
import org.ica.briquePackage.Equation;
import org.ica.briquePackage.Parametre;
import org.simpleframework.xml.strategy.Type;
import org.simpleframework.xml.strategy.Visitor;
import org.simpleframework.xml.stream.InputNode;
import org.simpleframework.xml.stream.NodeMap;
import org.simpleframework.xml.stream.OutputNode;

public class IdUpdater implements Visitor{		
	
	@Override
	public void read(Type type, NodeMap<InputNode> node) throws Exception {	
		InputNode element = node.getNode();

		if (element.getName().equals("parametreId")) {
			long uId = Long.valueOf(element.getValue());	
			Parametre.setParamId( Math.max(Parametre.getParamId(), uId));
		}
			if (element.getName().equals("equationId")) {
				long uId = Long.valueOf(element.getValue());		
				Equation.setId(Math.max(Equation.getId(), uId));
			}
			if (element.getName().equals("briqueId")) {
				long uId = Long.valueOf(element.getValue());				
				Brique.setUniqueBriqueId(Math.max(Brique.getUniqueBriqueId(), uId));
			}
	}

	@Override
	public void write(Type type, NodeMap<OutputNode> node) throws Exception {
		
	}
	
}