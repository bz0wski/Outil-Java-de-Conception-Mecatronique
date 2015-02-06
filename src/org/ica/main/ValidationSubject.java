package org.ica.main;

public interface ValidationSubject {

	public void register(ValidationObserver o);
	public void unregister(ValidationObserver o);
	public void notifyObserver();
	
}
