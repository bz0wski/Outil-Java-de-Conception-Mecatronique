package org.ica.main;


import java.util.Map;

public interface Observer {
	public void update(int tableItemCount,Map<Integer, String> equations);
}
