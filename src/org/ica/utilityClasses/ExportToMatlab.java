package org.ica.utilityClasses;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.eclipse.swt.widgets.Display;
import org.ica.briquePackage.CompositionBriques;
import org.ica.briquePackage.Equation;
import org.ica.briquePackage.MyAbstractModelObject;
import org.ica.briquePackage.Parametre;
import org.ica.enumeration.TypeParametre;
import org.ica.uiElements.CompilationLog;
import org.ica.uiElements.MainApplicationWindowContents;
import org.ica.uiElements.MainApplicationWindowList;


/**
 * This class is responsible for the main calculation, makes all the calls to the external 
 * Matlab generated .exe functions. Code well documented, <b><i>refer to source code</i></b>.<br>
 * Order of calls :<br>
	  1 - sortCompostionContents<br>
	  2 - produceMainMatrix<br>
	  3 - produceNODM(call matching.exe to produce matchin_out.txt)<br>
	  4 - replaceNODM(produces singularity_in.txt and calls singularity.exe to produce singularity_out.txt)<br>
	  5 - readAndInterpretSingularity_Out<br>
 * @author Salim AHMED
 *
 */
public class ExportToMatlab extends MyAbstractModelObject implements Comparator<Equation> {
	/**Holds the main matrix generated from all the parameters and equations with no filtering*/
	private volatile int[][] mainMatrix = null;
	/**Holds the NODM*/
	private volatile int[][] NODM = null;
	/**Holds the filtered and sorted Model object*/
	private volatile CompositionBriques orderedCompositionBriques = null; 
	private final CompositionBriques compositionBriques;
	
	/**Reports the progress of the compilation process.*/
	private int progress;
	private final ImageHolder imageHolder = ImageHolder.getImageHolder();
	/**Keeps a log of the output of the program*/
	private final StringBuilder report = new StringBuilder();
	/**Keeps the error log of the processes. */
	private final StringBuilder errorLog = new StringBuilder();
	
	/*Order of calls :
	 * 1 - sortCompostionContents
	 * 2 - produceMainMatrix
	 * 3 - produceNODM(call matching.exe to produce matchin_out.txt)
	 * 4 - replaceNODM(produces singularity_in.txt and calls singularity.exe to produce singularity_out.txt)
	 * 5 - readAndInterpretSingularity_Out
	 * */
	/**
	 * This is the default constructor which compiles the  {@code CompositionBriques} received as parameter.
	 * @param composition : The {@code CompositionBriques} to be processed.
	 */
	public ExportToMatlab(CompositionBriques composition) {
		this.compositionBriques = composition;

		ExecutorService executor = Executors.newSingleThreadExecutor();
		Runnable runnable = new Runnable() {
			MainApplicationWindowContents contents = ObjectFactory.getMainApplicationWindowContents(
					MainApplicationWindowContents.class.getName());
			public void run() {
				try {
					List<Equation> equationList = compositionBriques.getEquation();
					MatchIdenticalParameters.matchIdenticalBriqueParameters(equationList);
					Set<Parametre> parametreSet = new HashSet<>();
					for (Equation equation : equationList) {
						for (Parametre parametre : equation.getListeDeParametresEqn()) {
							parametreSet.add(parametre);
							
						}
						
					}
					compositionBriques.setParametre(new ArrayList<>(parametreSet));
					
							DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
							Date date = new Date();
							
					long starttime = System.currentTimeMillis();
				//	Enter some details about the compilation process 
					
					setReport("Compilation log for "+ compositionBriques.getNomComposition()+"\t"
					+df.format(date)+"\n");
					
					Display.getDefault().asyncExec(()->{
						CompilationLog log = new CompilationLog(MainApplicationWindowList.getshellForShellViewingContents(), ExportToMatlab.this);
					
						log.open();
					});
					
					
					produceMainMatrix(compositionBriques);
					if(contents != null)
						contents.setProgress(getProgress());
					
					produceNODM(compositionBriques);
					if(contents != null)
						contents.setProgress(getProgress());
					
					replaceNODM();
					if(contents != null)
						contents.setProgress(getProgress());
					Thread.sleep(1000);
					
					interpretSingularity_Out();
					if(contents != null)
						contents.setProgress(getProgress());
					Thread.sleep(1000);
					//If error log is not empty, open a window and display the errors to user
					if(!getErrorLog().isEmpty()){
						Display.getDefault().asyncExec(()->{
							CompilationLog log = new CompilationLog(MainApplicationWindowList.getshellForShellViewingContents(), ExportToMatlab.this, true);		
							log.open();
						});
					}
					
			
					if(contents != null)
						contents.setProgress(100);	
					Thread.sleep(1000);
					
					Display.getDefault().syncExec(()->{
						if(contents != null){
						contents.hideProgressBar();
						contents.enableCompileToolItem();
						
						if(getErrorLog().isEmpty())
							contents.updateUser("Terminé avec succès.", imageHolder.getOk16());
						else
							contents.updateUser("Terminé avec erreur(s).", imageHolder.getError16());
							
						}
					});
					System.out.printf("\n****************** Time elapsed ****************** :%d%n",System.currentTimeMillis() - starttime );
				} catch (Exception e) {
					e.printStackTrace();
					if(contents != null)
						contents.updateUser("Une erreur est survenue lors de la compilation, veuillez recommencer.", imageHolder.getError16());
				
					if(!errorLog.toString().isEmpty()){
						Display.getDefault().asyncExec(()->{
							CompilationLog log = new CompilationLog(MainApplicationWindowList.getshellForShellViewingContents(), ExportToMatlab.this, true);		
							log.open();
						});
					}
						
				}
				finally{
					cleanUp();				
					Display.getDefault().syncExec(()->{
						if(contents != null){
						contents.hideProgressBar();
						contents.enableCompileToolItem();
						
						}
					});
				}
			
			}
		};
		
		executor.execute(runnable);
		/**Important to shut the executor once it's done */
		executor.shutdown();
		
		}



	/**
	 * @return the compositionBriques
	 */
	public synchronized CompositionBriques getCompositionBriques() {
		return compositionBriques;
	}



	/**This sub routine sorts out the Composition's Equations and Parameters before making the matrix.
	 * @param compositionBriques : the compositionBriques whose contents are to be ordered.
	 * */
	public void orderCompositionContents(CompositionBriques compositionBriques) {

		if (!compositionBriques.equals(null) && compositionBriques.getBrique() != null) {
			List<Equation> listEqns = compositionBriques.getEquation(); 
			List<Parametre> listParametres = compositionBriques.getParametre();

			//Ordonner la liste de toutes les equations par ORIENTED/NON-ORIENTED 
			Collections.sort(listEqns, (e1,e2)-> Boolean.valueOf(e1.getIsOriented()).compareTo(Boolean.valueOf(e2.getIsOriented())));
			
			//Ordonner la liste de tous les parametres par les types INPUT/UNDETERMINED/OUTPUT 	
			Collections.sort(listParametres,(p1,p2)-> p1.getTypeP().compareTo(p2.getTypeP()));
			orderedCompositionBriques = compositionBriques;		
			setProgress(10);
		}

	}

	
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/*First of all sort the matrix contents 
	 * Equations : Non Oriented, then Oriented
	 * Parameters: Undetermined(Input*, Free, Output*) and Output 
	 * Secondly generate the main by filtering the sorted Equation and parameter list.
	 * Third, write the main matrix to the disk.
	 * */
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////	
	
	/**
	 * This method generates a matrix from the {@code CompositionBriques} received as a parameter.
	 * Source code well documented, refer to source code.
	 * @param compositionBriques : The  {@code CompositionBriques} from which to generate the main matrix.
	 */
	public void produceMainMatrix(CompositionBriques compositionBriques){
		
		File main_Matrix = new File(ClassLoader.getSystemResource("org/ica/resources/").getFile(),"main_Matrix.txt");
		/**If main_Matrix already exists, delete it before proceeding.*/
		if (main_Matrix.exists() && main_Matrix.canRead()) {
			if (main_Matrix.delete()) {
				setReport("main_Matrix.txt out deleted");
			}
		}
	
		//Deleting all the input files.
		File matching_in = new File(ClassLoader.getSystemResource("org/ica/resources/").getFile(),"matching_in.txt");
		if (matching_in.exists()) {
			matching_in.delete();
			setReport("\nmatching_in.txt out deleted");
		}
			
		File singularity_in = new File(ClassLoader.getSystemResource("org/ica/resources/").getFile(),"singularity_in.txt");
		if (singularity_in.exists()) {
			singularity_in.delete();
			setReport("\nsingularity_in.txt out deleted");
		}
			
		
		//sorting model contents.
		orderCompositionContents(compositionBriques);
		
		//Filtrage de la liste de paramètres pour sortir avec les parametres de type UNDETERMINED et OUTPUT
		List<Parametre> params_OutUnkn = compositionBriques.getParametre().parallelStream()
						.filter(p->p.getTypeP().equals(TypeParametre.UNDETERMINED) || 
								   p.getTypeP().equals(TypeParametre.OUTPUT))
								   .collect(Collectors.toCollection(ArrayList::new));
				
		//Filtrage de la liste d'équations pour sortir avec les équations non orientée et non contrainte.
		List<Equation> nonContrainteEquations = compositionBriques.getEquation().parallelStream()
						.filter(e-> e.isConstraint() == false).collect(Collectors.toCollection(ArrayList::new));
	
		int matrix[][] = new int[nonContrainteEquations.size()][params_OutUnkn.size()];
		
		
		setReport("\n\nMain matrix");
		
		
		for (int i = 0; i < nonContrainteEquations.size(); i++) {
			Equation equation = nonContrainteEquations.get(i);

				for (int p = 0; p < params_OutUnkn.size(); p++){
					Parametre parametre = params_OutUnkn.get(p);
					if (equation.getListeDeParametresEqn().contains(parametre)) {
							matrix[i][p] = 1;
						} else {							
							matrix[i][p] = 0;
						}
					mainMatrix = matrix;
					
				}
		}
		
		setReport("\nTotal Number of equations: "+nonContrainteEquations.size()+
				"\nTotal Number of parameters: "+params_OutUnkn.size());
	
		StringBuilder sb = new StringBuilder();
		sb.append("[");
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[0].length; j++) {
				sb.append((matrix[i][j]));
			}
			sb.append(";");
		}
		sb.append("]");
		String string = new String(sb);		
	
		string = string.replaceAll(";]", "]");
	//	setReport("\n"+string);
		printMatrix(string, "main_Matrix.txt");
	}
	
	/**Produire une NODM qui a pour lignes  les équations non orientées et non contraintes
	 * et pour colonnes  les parametres de type UNDETERMINED(Input*, Free, Output*). A la sortie imprime 
	 * le fichier matching_in.txt qui contient la NODM, pui appel à matching.exe.
	 * @param compositionBriques : a CompositionBriques instance
	 * */
	public void produceNODM(CompositionBriques compositionBriques)  {
	
		//Filtrage de la liste de paramètres pour sortir avec les parametres de type UNDETERMINED
		List<Parametre> params = compositionBriques.getParametre().stream().parallel()
				.filter(p->p.getTypeP().equals(TypeParametre.UNDETERMINED)).collect(Collectors.toCollection(ArrayList::new));
		
		//Filtrage de la liste d'équations pour sortir avec les équations non orientée et non contrainte.
		List<Equation> nonOrientedEquations = compositionBriques.getEquation().stream().parallel()
				.filter(e->e.isOriented() == false && e.isConstraint() == false).collect(Collectors.toCollection(ArrayList::new));
		
		
		int matrix[][] = new int[nonOrientedEquations.size()][params.size()];
		
		for (int i = 0; i < nonOrientedEquations.size(); i++) {
			Equation equation = nonOrientedEquations.get(i);

				for (int p = 0; p < params.size(); p++){
					Parametre parametre = params.get(p);
				//	System.out.println("Equation Params "+equation.getListeDeParametresEqn()+"\n Parameter "+parametre);
					if (equation.getListeDeParametresEqn().contains(parametre)) {
							matrix[i][p] = 1;
						} else {							
							matrix[i][p] = 0;
						}
					
					
				}
		}
		NODM = matrix;
		setReport("\n\nNODM Matrix\nNumber of equations (NODM): "+nonOrientedEquations.size()
				+ "\nNumber of parameters (NODM) : "+params.size());

	
		StringBuilder sb = new StringBuilder();
		sb.append("NODM = [");
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[0].length; j++) {
				sb.append((matrix[i][j]));
				sb.append(',');
			}
			sb.append(";");
		}
		sb.append("]");
		 String t_string = new String(sb);
		
		t_string = t_string.replaceAll(",;",";").replaceAll(";]", "]");
		
		setProgress(20);
	//	setReport("\n"+t_string);
		printMatrix(t_string,"matching_in.txt"); 
		callMatchingFunction();	
		
	}

	/**This method reads the matching_out.txt file, copies the ODM matrix generated after 
	 * executing the produceNODM() and callingMatchingFunction() subroutines,
	 * and inserts it into the main matrix in the place of the initial NODM matrix. 
	 * 
	 * */
	public void replaceNODM(){
		File matching_out = new File(ClassLoader.getSystemResource("org/ica/resources/").getFile(),"matching_out.txt");
		
			if (matching_out.exists() && matching_out.canRead()) {
			//	System.out.println("Reading matching_out.txt");
				
				setReport("\nReading matching_out.txt");
				try(BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(matching_out),"utf-8"))) {
					String matrix = reader.readLine();
					if (matrix.isEmpty()) return;
					if (matrix.startsWith("ODM", 0)) {
								
						String [] parts = matrix.replaceAll("ODM=\\[", "").replaceAll("([0|1|2]{1})\\];", "$1").split(";");
						int [][] replaceNODM = new int[parts.length][parts[0].split(",").length];
						
						//Problem to rectify here/	
						/*To verify the validity of the matrix received, I compare the number of lines with that of the NODM generated earlier
						 * If there's a difference, then there's a problem. */	
						if (parts.length != NODM.length) {
							setErrorLog("Erreur sur la Matrice ODM passée.");
							throw new IllegalArgumentException("Erreur sur la Matrice ODM passée.");
						}
						
						for (int i = 0; i < parts.length; i++) {
							String[] subParts = parts[i].split(",");
							
							for (int j = 0; j < subParts.length; j++) {
								replaceNODM[i][j] = Integer.parseInt(subParts[j]);
							}
						}
					
						/**To insert the recovered ODM into the main matrix I recover the smaller Matrix, go through it 
						 * line by line and replace the contents of the bigger matrix with those of the smaller in  the same position.*/
					if (mainMatrix == null) {
						throw new NullPointerException("Main matrix is null");
					}
						if (mainMatrix != null) {
							//System.out.println("replacing NODM in main Matrix");
							setReport("\nReplacing NODM in main Matrix");
							int[][] temp = mainMatrix;
							for (int i = 0; i < replaceNODM.length; i++) {
								for (int j = 0; j < replaceNODM[0].length; j++) {
									temp[i][j] = replaceNODM[i][j];
								}
							}
							mainMatrix = temp;
							StringBuilder builder = new StringBuilder();
							builder.append("ODM=[");
							for (int i = 0; i < mainMatrix.length; i++) {
								for (int j = 0; j < mainMatrix[0].length; j++) {								
									builder.append(mainMatrix[i][j]+",");
								}
								builder.append(";");
							}
							builder.append("]");
							String string = builder.toString().replaceAll(",;",";").replaceAll(";]", "]");
							//System.out.println(string);
							//setReport("\n"+string);
							printMatrix(string, "singularity_in.txt");
						}
					}
		
				
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			else {
				System.err.println("matching_out.txt missing");
				setErrorLog("matching_out.txt missing");
			}
			setProgress(60);
			/*After replacing the NODM with the generated ODM, the new ODM is printed into 
			 * singularity_in.txt and a call is made to singularity.exe, which will read 
			 * singularity_in.txt and generate singularity_out.txt*/
			callingSingularityFunction();
		}
	/**
	 * Lire le fichier singularity_out.txt, genéré après l'appel à la fonction singularity.exe.
	 * Last function to be called, reads the results generated by singularity.exe in the singularity_out.txt
		and provides information to the user.

	 * @return A <code> Map</code> which contains a mapping of the matrix names to the corresponding
	 * matrices. <br> NODM = [2,1,0,0,;1,0,2,1].
	 * */
	
	
	public Map<String, Integer[][]> readSingularity_Out(){
		File singularity_out = new File(ClassLoader.getSystemResource("org/ica/resources/").getFile(),"singularity_out.txt");
		
		String matrix = null;
		String patternString = "(\\w{3})=\\[(.*)\\]\\;{1}";
		Map<String,String> matricesMap = new HashMap<>();
		Map<String, Integer[][]> realMatricesMap = new HashMap<>();
		Integer[][] innerMatrix = null;
		
		if (singularity_out.exists() && singularity_out.canRead()) {			
			Matcher matcher = Pattern.compile(patternString).matcher("");
			try(Scanner scanner = new Scanner(new FileInputStream(singularity_out))) {
				while(scanner.hasNextLine()){	
					if ((matrix = scanner.nextLine()) != null) {					
						//Change the input sequence for the matrix;
						matcher.reset(matrix);
						if (matcher.matches()) {
							matricesMap.put((String)matcher.group(1), (String)matcher.group(2));
							if (!matcher.group(2).contains(";")) {
								String[] parts = matcher.group(2).split(",");
								innerMatrix = new Integer[1][parts.length];
								for (int i = 0; i < 1; i++) {
									for (int j = 0; j < parts.length; j++) {
										innerMatrix[i][j] = Integer.parseInt(parts[j]);
									}
									
								}
								realMatricesMap.put(matcher.group(1), innerMatrix);
							}
							else if (matcher.group(2).contains(";")) {
								String [] parts = matcher.group(2).split(";");
								if (parts.length>0) {
									innerMatrix = new Integer[parts.length][parts[0].split(",").length];
									for (int i = 0; i < parts.length; i++) {
										String[] subParts = parts[i].split(",");									
										for (int j = 0; j < subParts.length; j++) {
											innerMatrix[i][j] = Integer.parseInt(subParts[j]);
										}
									}
								}
								realMatricesMap.put(matcher.group(1), innerMatrix);					
							}
						}
					}
				}
		
				for (Iterator<Entry<String, Integer[][]>> iterator = realMatricesMap.entrySet().iterator(); iterator.hasNext();) {
					Map.Entry<String, Integer[][]> entry = iterator.next();
					
					
					StringBuilder bd = new StringBuilder();
					bd.append(entry.getKey());
					Integer[][] m = entry.getValue();
					bd.append("[");
					for (int i = 0; i < m.length; i++) {
						for (int j = 0; j < m[0].length; j++) {
							bd.append(m[i][j]+",");
						}
						bd.append(";");
					}
					bd.append("]");
					System.out.println(bd.toString().replaceAll(",;",";").replaceAll(";]", "]"));
					}
				
			} catch (IOException e) {
			
				e.printStackTrace();
			}
		
		}
		return realMatricesMap;
	
	}
	/**Dans cette fonction, je récupère le résultat de lecture de la matrice Singularity_Out
	 * Et je présente de l'information à l'utilisateur 
	 * */
	public void interpretSingularity_Out(){
		Map <String, Integer[][]> realMatricesMap = readSingularity_Out();
		List<Equation> UCRList = new ArrayList<>();
		List<Parametre> UCCList = new ArrayList<>();
		
		List<Equation> NCRList = new ArrayList<>();
		List<Parametre> NCCList = new ArrayList<>();
		
		List<Equation> OCRList = new ArrayList<>();
		List<Parametre> OCCList = new ArrayList<>();
		
		Integer [][] temp = null;
		if (orderedCompositionBriques == null) {
			setErrorLog("\nNull Error Encountered please restart compilation");
			throw new NullPointerException("OrderedCompositionBriques is null");
		
		}
		
		List<Equation> orderedEqnList = orderedCompositionBriques.getEquation();
		List<Parametre> orderedParamList = orderedCompositionBriques.getParametre();
		StringBuilder builder = new StringBuilder();
		
		for (Iterator<Entry<String, Integer[][]>> iterator = realMatricesMap.entrySet().iterator(); iterator.hasNext();) {
			Map.Entry<String, Integer[][]> entry = iterator.next();
			switch (entry.getKey()) {
			case "UCR":
				int index = -1;				
					temp = realMatricesMap.get("UCR");
					System.out.println("Reordered Equation list");
					setReport("\nReordered Equation list");
					for (int i = 0; i < temp.length; i++) {
						for (int j = 0; j < temp[0].length; j++) {
							index = ((Integer)(temp[i][j]))-1;
							//System.out.println(orderedEqnList.get(index));
							UCRList.add(orderedEqnList.get(index));
						}
						
					}
					temp = realMatricesMap.get("UCC");
					System.out.println("\nReordered Parameter list");
					setReport("\nReordered Parameter list");
					for (int i = 0; i < temp.length; i++) {
						for (int j = 0; j < temp[0].length; j++) {
							index = ((Integer)(temp[i][j])-1);
							System.out.println(orderedParamList.get(index));
							UCCList.add(orderedParamList.get(index));
						}
						
					}	
									
				break;
			case "NCR":
				if (realMatricesMap.containsKey("NCR")) {
					temp = realMatricesMap.get("NCR");
					System.out.println("NCR Reordered Equation list");
					setReport("\nNCR Reordered Equation list");
					for (int i = 0; i < temp.length; i++) {
						for (int j = 0; j < temp[0].length; j++) {
							index = ((Integer)(temp[i][j]))-1;
							System.out.println(orderedEqnList.get(index));
							NCRList.add(orderedEqnList.get(index));
						}
						
					}
					temp = realMatricesMap.get("UCC");
					System.out.println("NCC Reordered Parameter list");
					setReport("\nNCC Reordered Parameter list");
					for (int i = 0; i < temp.length; i++) {
						for (int j = 0; j < temp[0].length; j++) {
							index = ((Integer)(temp[i][j])-1);
							System.out.println(orderedParamList.get(index));
							NCCList.add(orderedParamList.get(index));
						}
						
					}	
				}
				break;
			case "OCR":
				if (realMatricesMap.containsKey("OCR")) {
					temp = realMatricesMap.get("NCR");
					System.out.println("OCR Reordered Equation list");
					setReport("\nOCR Reordered Equation list");
					for (int i = 0; i < temp.length; i++) {
						for (int j = 0; j < temp[0].length; j++) {
							index = ((Integer)(temp[i][j]))-1;
							System.out.println(orderedEqnList.get(index));
							OCRList.add(orderedEqnList.get(index));
						}
						
					}
					temp = realMatricesMap.get("UCC");
					System.out.println("OCC Reordered Parameter list");
					for (int i = 0; i < temp.length; i++) {
						for (int j = 0; j < temp[0].length; j++) {
							index = ((Integer)(temp[i][j])-1);
							System.out.println(orderedParamList.get(index));
							OCCList.add(orderedParamList.get(index));
						}
						
					}	
				}
				break;
			default:
				break;
			}
			
			System.out.println(entry.getKey());
			Integer[][] m = entry.getValue();
			System.out.print("[");
			builder.append("\n[");
			for (int i = 0; i < m.length; i++) {
				for (int j = 0; j < m[0].length; j++) {
					System.out.print(m[i][j]+",");
					builder.append(m[i][j]+",");
				}
				System.out.print(";");
				builder.append(";");
			}
			System.out.print("]\n");
			builder.append("]\n");
			setReport(builder.toString());
			builder.delete(0, builder.length());
			}
		
	}
	/**Given a matrix and a fileName, this subroutine writes the matrix to the specified file.
	 * */
	private static void printMatrix(String matrix,String fileName){
		String resourcePackage = ClassLoader.getSystemResource("org/ica/resources").getFile();
		if (resourcePackage != null) {			
			
				
			File matrixFile = new File(resourcePackage,fileName);
			
			try( PrintWriter writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(matrixFile), "utf-8"))) ) {
				
				if(!writer.checkError()){
					writer.printf("%s", matrix);
					writer.close();
				
				}
				else {
					throw new IOException();
				}
			} catch (IOException e) {			
				e.printStackTrace();
			} 				
		}
	}
	
	/**This method calls the <b><i>matching.exe</i></b> function after the matchig_in.txt file has been generated
	 * and then generates the matching_out.txt file which contains the NODM matrix.
	 * */
	private void callMatchingFunction(){
		/**If matching_out already exists, delete it before proceeding.*/
		File matching_out = new File(ClassLoader.getSystemResource("org/ica/resources/").getFile(),"matching_out.txt");
		if (matching_out.exists() && matching_out.canRead()) {
			if (matching_out.delete()) {
				//System.out.println("Matching_out.txt out deleted");
				setReport("\nmatching_out.txt deleted");
			}
		}
		
		File matching_in = new File(ClassLoader.getSystemResource("org/ica/resources/").getFile(),"matching_in.txt");

		if (matching_in.exists() && matching_in.canRead()) {
			/**Testing a call to matching.exe*/
		//	System.out.println("Reading matching_in.txt");
			setReport("\nReading matching_in.txt");
			String matching_exe = ClassLoader.getSystemResource("org/ica/resources/matching.exe").getFile();
			
			/*Get the Directory which contains the matching.exe and set it as the process default directory*/
			File current = new File(ClassLoader.getSystemResource("org/ica/resources/").getFile());
		
			
				
				try {
					
					ProcessBuilder processBuilder = new ProcessBuilder(matching_exe);
					//Setting the working directory to the current working directory;
					processBuilder.directory(current);
					Process process = processBuilder.start();
				
					process.waitFor();
				
					InputStream errorStream = process.getErrorStream();
					InputStream inputStream = process.getInputStream();

					
					InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
					InputStreamReader errorStreamReader = new InputStreamReader(errorStream);
					BufferedReader reader = new BufferedReader(inputStreamReader);
					BufferedReader erroReader = new BufferedReader(errorStreamReader);
				
					
					String line,e_line = "";
					while ((line = reader.readLine()) != null) {
						//Write to compilation report
						//System.out.println(line);			
						setReport(line+"\n");					}
					
					while ((e_line = erroReader.readLine()) != null) {
						//Write to error log
					//	System.err.println(e_line);
						setErrorLog(e_line+"\n");
						
					}
				}
				catch (IOException |SecurityException |IllegalArgumentException |InterruptedException e) {
				e.printStackTrace();
			}
				
	

			
		}
		else {
			System.err.println("matching_in.txt missing");
			setErrorLog("matching_in.txt missing");
		}
		setProgress(40);
	}
	
	
	/**This method calls the <b><i>singularity.exe</i></b> function after the singularity_in.txt file has been generated
	 * and then generates the singularity_out.txt file which contains a series of matrices : [OCR,OCC,NCC,NCR...].
	 * */
	private void callingSingularityFunction(){
		/**If singularity_out already exists, delete it before proceeding.*/
		File singularity_out = new File(ClassLoader.getSystemResource("org/ica/resources/").getFile(),"singularity_out.txt");
		if (singularity_out.exists() && singularity_out.canRead()) {
			if (singularity_out.delete()) {
			
				setReport("\nsingularity_out.txt out deleted");
			}
		}
		File singularity_in = new File(ClassLoader.getSystemResource("org/ica/resources/").getFile(),"singularity_in.txt");

		if (singularity_in.exists() && singularity_in.canRead()) {
			/**Testing a call to singularity.exe*/
		//	System.out.println("Reading singularity_in.txt");
			setReport("\nReading singularity_in.txt");
			String singularity_exe = ClassLoader.getSystemResource("org/ica/resources/singularity.exe").getFile();
			
			/*Get the Directory which contains the matching.exe and set it as the process default directory*/
			File current = new File(ClassLoader.getSystemResource("org/ica/resources/").getFile());
			
				
				try {
					
					ProcessBuilder processBuilder = new ProcessBuilder(singularity_exe);
					//Setting the working directory to the current working directory;
					processBuilder.directory(current);
					Process process = processBuilder.start();
	
					InputStream errorStream = process.getErrorStream();
					InputStream inputStream = process.getInputStream();

					
					InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
					InputStreamReader errorStreamReader = new InputStreamReader(errorStream);
					BufferedReader reader = new BufferedReader(inputStreamReader);
					BufferedReader erroReader = new BufferedReader(errorStreamReader);
				
					
					String line,e_line = "";
					while ((line = reader.readLine()) != null) {
						System.out.println(line);
						setReport(line+"\n");
					}
					while ((e_line = erroReader.readLine()) != null) {
						System.err.println(e_line);
						setErrorLog(e_line+"\n");
						
					}
				}
				catch (IOException |SecurityException |IllegalArgumentException  e) {
				e.printStackTrace();
			}
					
		
		}
		setProgress(80);
	}
/*
	public static void main(String [] args) throws Exception {
		List<Equation> list = new ArrayList<>();
		list.add(new Equation(false,"a+b+a=c", false,null, "", "Insérer commentaire"));
		list.add(new Equation(false,"ax+by=cz", false, "", "Insérer commentaire"));
		list.add(new Equation(false,"at+bh=ck", false, "", "Insérer commentaire"));
		list.add(new Equation(false,"aP8_k + o_uiOO0o + byyyh=", false, "", "Insérer commentaire"));
		
		Map<Equation, List<Parametre>> mapEqnsParams = new HashMap<Equation, List<Parametre>>();
		 Equation eqn1 = new Equation(false,"cz+a+b+ax=a+by", true,null, "", "Insérer commentaire");
		 Equation eqn2 = new Equation(false,"ax+by=cz - vcz", false,null, "", "Insérer commentaire");
		 Equation eqn3 = new Equation(false,"zed+arya=cz", true,null, "", "Insérer commentaire");
		 Equation eqn4 = new Equation(false,"abc+blk=jk", false,null, "", "Insérer commentaire");
		 Equation eqn5 = new Equation(false,"false+order=cz_8", false,null, "", "Insérer commentaire");
		
		 mapEqnsParams.put(eqn1, ExtractionParametre.listEquationParameters(eqn1));
		 mapEqnsParams.put(eqn2, ExtractionParametre.listEquationParameters(eqn2));
		 mapEqnsParams.put(eqn3, ExtractionParametre.listEquationParameters(eqn3));
		 mapEqnsParams.put(eqn4, ExtractionParametre.listEquationParameters(eqn4));
		 mapEqnsParams.put(eqn5, ExtractionParametre.listEquationParameters(eqn5));
		 Timer timer = new Timer();
		 TimerTask task =  new TimerTask() {
				
				@Override
				public void run() {
					System.out.println("Thread MNGMT COUNT:"+ ManagementFactory.getThreadMXBean().getThreadCount() );
					System.out.println("Thread  COUNT:"+ Thread.activeCount());
					
					Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
					for (Thread t : threadSet) {
						System.out.println(t.getName());
					}
				//	for (int i = 0; i< Thread.enumerate(new Thread[Thread.activeCount()]) ; i++) {
						
					
					
				}
			};
		 timer.scheduleAtFixedRate(task, 0, 1000);
	
		*/
/*		
		@SuppressWarnings("deprecation")
		Brique brique = new Brique("Test Brique", mapEqnsParams, "");
		CompositionBriques compositionBriques = new CompositionBriques("Test Composition", Arrays.asList(brique));
	long starttime = System.currentTimeMillis();
			
		File file = new File(ClassLoader.getSystemResource("org/ica/resources/").getFile(),"composave.ser");
		CompositionBriques compositionBriques = new CompositionBriques();
		if (file.exists() && file.canRead()) {
			try(FileInputStream fileInputStream = new FileInputStream(file);
					ObjectInputStream inputStream = new ObjectInputStream(fileInputStream)) {
					compositionBriques = (CompositionBriques)inputStream.readObject();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		else {
			System.err.println("database file missing or courrupted");
		}
	
		
		Equation equation =  new Equation(false,"abc+blk+jokj+tuf+iug=jk", false,null, "", "Insérer commentaire");
		Equation equation1 =  new Equation(false,"abceza+blk=jk", false,null, "", "Insérer commentaire");
		Equation equation2 =  new Equation(false,"abczaedfgghgh+blk=jk", false,null, "", "Insérer commentaire");
		System.out.println(Parametre.getParamId());
		
		VisitorStrategy strategy = new VisitorStrategy(new IdUpdater());
		Serializer serializer = new Persister(strategy);
		serializer.write(equation, new File("D:\\file.xml"));

		VisitorStrategy strategy = new VisitorStrategy(new IdUpdater());
		Serializer serializer = new Persister(strategy);	
		
		Equation eqnList = new Equation();
		serializer.read(eqnList,  new File("D:\\file.xml"));
		
		System.out.println(Parametre.getParamId());
	*/
		/*
		ExportToMatlab.produceMainMatrix(compositionBriques);
		ExportToMatlab.produceNODM(compositionBriques);
		ExportToMatlab.replaceNODM();
		orderCompositionContents(compositionBriques);
		interpretSingularity_Out();	
		//System.out.println(Locale.getDefault());
		 //System.out.println(System.getProperty("user.language"));
		 //System.out.println(System.getProperty("user.country"));
		
		 System.err.printf("\nTime elapsed %d%n",System.currentTimeMillis() - starttime );
		// timer.cancel();
		/*
		MatchIdenticalParameters.matchIdenticalListOfEquationParameters(brique.getListEquations());
		MatchIdenticalParameters.MatchIdenticalBriqueParameters(brique);
		
		sortCompositionContents(compositionBriques);
		produceNODM(compositionBriques);
		
		System.err.println("Meanwhile another thread is running "+System.currentTimeMillis());
		*/
		//callMatchingFunction();
		
	/*	
		Parametre p1 = new Parametre();
		Parametre p2 = p1;
		Parametre p3 = (Parametre) p1.clone();
		Parametre p4 = new Parametre();
		p4 = p1;
		p1.setNomP("raaad");
		p1.setTypeP(TypeParametre.OUTPUT);
		//System.out.println(p1+"\np2"+p2+"\np3"+p3+"\np4"+p4);
		//new ExportToMatlab(list);
	//	MatchIdenticalParameters.matchIdenticalEquationParameters(new Equation(false,"c+a+b+b+a=c", false,null, "", "Insérer commentaire"));
	
	}
*/
	@Override
	public int compare(Equation o1, Equation o2) {
		
		return o1.isOriented() == o2.isOriented() ? -1:1;
	}
/** 
 * Init all parameters to initial values.
 */
	public void cleanUp(){
		NODM = null;
		mainMatrix = null;
		orderedCompositionBriques = null;
		errorLog.delete(0, errorLog.toString().length());
		report.delete(0, report.toString().length());
	}



	/**
	 * @return the progress
	 */
	public int getProgress() {
		return progress;
	}
	/**
	 * 
	 * @return the error log
	 */
	public String getErrorLog(){
		return errorLog.toString();
	}
	/**
	 * This method records a log of all the errors encountered during the current compilation process.
	 * @param s : A {@code String }, appended to the endof the log.
	 */
	public void setErrorLog(String s){
		StringBuilder oldValue = this.errorLog;
		errorLog.append(s);
		firePropertyChange("errorLog", oldValue, errorLog);
	}
	
	/**
	 * 
	 * @return The report(compilation log)
	 */
	public String getReport(){
		return report.toString();
	}
	/**
	 * This method records a log of the current compilation process.
	 * @param s : A {@code String}, appended to the end of the log.
	 */
	public void setReport(String s){
		StringBuilder oldValue = this.errorLog;
		report.append(s);
		firePropertyChange("report", oldValue, report);
	}
	
	/**
	 * The value received as parameter is  represented as the current progress of the compilation
	 * process on the progressBar
	 * @param progress <br>Updates the user on the progress of the compilation process
	 */
	public void setProgress(int progress) {
		this.progress = progress;
	}
}





