package org.ica.utilityClasses;


public final class FactoryTools{
		private String className; 
		private Object object;

		public FactoryTools(String index, Object object ){
			this.className = index;
			this.object = object;
			
		}
		
		public FactoryTools(String object ){
			this.className = (String)object;
			
		}
		public Object get(){
			return object;
		}
		public String getClassName(){
			return className;
		}

}


