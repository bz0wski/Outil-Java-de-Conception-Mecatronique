package org.ica.main;

public interface MainCanvasSubject {
	public void notifyObserver(boolean empty);
	public void registerObserver(MainCanvasObserver o);
	public void unregisterObserver(MainCanvasObserver o);
}
