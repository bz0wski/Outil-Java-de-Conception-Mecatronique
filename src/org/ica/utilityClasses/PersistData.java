package org.ica.utilityClasses;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.ica.briquePackage.LevelOnePackage;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;
import org.simpleframework.xml.strategy.VisitorStrategy;



public class PersistData {

	private static String OS = null;
	
	private static String getAppDataDirectory(){
		if (isWindows()) {
			return System.getenv("APPDATA");
		}
		return null;
		
	}

	public static void saveMainList(List<LevelOnePackage> list) {
	//	File file = new File(ClassLoader.getSystemResource("org/ica/resources/").getFile(),"MainSave.ser");
	//	File backupFile = new File(ClassLoader.getSystemResource("org/ica/resources/").getFile(),"MainSaveBackup.xml");
		
		File db = new File(getAppDataDirectory(),"org.ica.Composition Modeles/bdd");
		if (!db.exists()) {
			try {
				db.mkdirs();
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		File file = new File(db.getAbsolutePath(),"MainSave.ser" );
		File backupFile = new File(db.getAbsolutePath(),"MainSaveBackup.xml" );
		
		if (list instanceof List<?>) {
			try(FileOutputStream fileout = new FileOutputStream(file);
					ObjectOutputStream outputStream = new ObjectOutputStream(fileout)) {
					
						outputStream.writeObject(list);
				//		Only do a backup if the list is not Empty 
						if (!list.isEmpty()) {
							VisitorStrategy strategy = new VisitorStrategy(new IdUpdater());
							Serializer serializer = new Persister(strategy);
							
							//Unhide the file before writing to it
							if(isWindows()){
								String cmdUnhide[] = {"attrib","-h",backupFile.getAbsolutePath()};
								Process process = new ProcessBuilder(cmdUnhide).start();
								//Very important to wait for the process to finish before attempting a write operation
								process.waitFor();
								serializer.write(new MainDataHouse(list), backupFile);
								//Hide the backup after writing to it.
								String cmdHide[] = {"attrib","+h",backupFile.getAbsolutePath()};
								 new ProcessBuilder(cmdHide).start();
								}
							
						}
														
			} catch (Exception e) {
				if (e instanceof NullPointerException) {
					System.err.println("Erreur fatale, relancer l'application.");
				}
				e.printStackTrace();
			}
		}
		
	}


	@SuppressWarnings("unchecked")
	public static List<LevelOnePackage> loadFromDataBase() {
		
	//	File file = new File(ClassLoader.getSystemResource("org/ica/resources/").getFile(),"MainSave.ser");
		
		File db = new File(getAppDataDirectory(),"org.ica.Composition Modeles/bdd");
		if (!db.exists()) {
			try {
				db.mkdirs();
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		File file = new File(db.getAbsolutePath(),"MainSave.ser" );
		File backupFile = new File(db.getAbsolutePath(),"MainSaveBackup.xml" );
		
		List<LevelOnePackage> list = new ArrayList<>();
		if (file.exists() && file.canRead()) {
			try(FileInputStream fileInputStream = new FileInputStream(file);
					ObjectInputStream inputStream = new ObjectInputStream(fileInputStream)) {
					list = (List<LevelOnePackage>)inputStream.readObject();
					if (list.isEmpty()) {
						throw new IllegalArgumentException("Corrupted Database File, couldn't recover any useful data");
					}
			} catch (Exception e) {
				e.printStackTrace();
				if (e instanceof IllegalArgumentException) {
					//File backupFile = new File(ClassLoader.getSystemResource("org/ica/resources/").getFile(),"MainSaveBackup.xml");
					
					if (backupFile.exists() && backupFile.canRead()) {
						List<LevelOnePackage> lvlOneList = new ArrayList<>();
						MainDataHouse data = new MainDataHouse(lvlOneList);
						VisitorStrategy strategy = new VisitorStrategy(new IdUpdater());
						Serializer serializer = new Persister(strategy);
						try {
							serializer.read(data, backupFile);
							list = data.getLevelOnePackages();
						} catch (Exception exp) {
							
							exp.printStackTrace();
						}
					}
					else {
						System.err.println("Backup failed");
					}
				}
			}
		}
		else {
			System.err.println("database file missing or courrupted, loading backup file now...");
		//	File backupFile = new File(ClassLoader.getSystemResource("org/ica/resources/").getFile(),"MainSaveBackup.xml");
			
			if (backupFile.exists() && backupFile.canRead()) {
				List<LevelOnePackage> lvlOneList = new ArrayList<>();
				MainDataHouse data = new MainDataHouse(lvlOneList);
				VisitorStrategy strategy = new VisitorStrategy(new IdUpdater());
				Serializer serializer = new Persister(strategy);
				try {
					serializer.read(data, backupFile);
					list = data.getLevelOnePackages();
					System.err.println(list);
				} catch (Exception exp) {
					
					exp.printStackTrace();
				}
			}
			else {
				System.err.println("Backup failed");
			}
		}
		
		return list;
	}
	
	 public static String getOSName()
	   {
	      if(OS == null) { OS = System.getProperty("os.name"); }    
	      return OS;
	   }
	 
	public static boolean isWindows(){
		 return getOSName().startsWith("Windows");	
	}

}
