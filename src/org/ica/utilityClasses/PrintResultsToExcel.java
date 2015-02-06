package org.ica.utilityClasses;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.poi.ss.formula.FormulaParseException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.ica.briquePackage.CompositionBriques;
import org.ica.briquePackage.Equation;
import org.ica.briquePackage.Parametre;
import org.ica.enumeration.SousTypeParametre;
import org.ica.enumeration.TypeParametre;


public class PrintResultsToExcel {

	private static String[] inputConstantColumns = {"Valeur", "Unité", "Nom" ,"Description", "Propriété"};
	private static String[] inputVariableColumns = {"Valeur", "Range", "Nom" ,"Description", "Propriété"};
	
	private static String[] outputColumns = {"Valeur", "Unité", "Nom" ,"Description", "Propriété"};
	
	private static Map<Parametre, Cell> parameterToCellMapping = new HashMap<Parametre, Cell>();
	private static Map<Equation, Cell> outputParameterToCellMapping = new HashMap<Equation, Cell>();
	private final CompositionBriques compositionBriques;

	private static String[] equationColumns = {"Equation","Parametre de sortie", "Expression de l'équation", "Description", "Propriétés" };
	private static 	Map<String, CellStyle> styles = new HashMap<String, CellStyle>();
	
	private final String printLocation;
	private String printError = "";
	private boolean printSuccess = true;
	
	private CellReference fromReference	 = null;
	private CellReference toReference = null;
	public PrintResultsToExcel(CompositionBriques compositionBriques, String printLocation) {
		this.compositionBriques = compositionBriques;
		this.printLocation = printLocation;
		
		printProjectResults(compositionBriques);
	}


	public void printProjectResults(CompositionBriques compositionBriques){
		int rownum = 0;
		int colIndex = 0;
		int rowIndex = 0;
		//Input constant
		String nom = compositionBriques.getNomComposition();
		
		

		//blank workbook
		XSSFWorkbook workbook = new XSSFWorkbook();

		//blank sheet
		XSSFSheet sheet = workbook.createSheet(nom);

		sheet.setFitToPage(true);
		sheet.setHorizontallyCenter(true);
		styles = createStyles(workbook);
	
		Row row = sheet.createRow(rownum++);
		Cell cell = row.createCell(0, Cell.CELL_TYPE_STRING);
		
		List<Parametre> inputConstant = compositionBriques.getParametre().parallelStream()
				.filter(p->p.getTypeP().equals(TypeParametre.INPUT) && p.getSousTypeP().equals(SousTypeParametre.CONSTANT))
				.collect(Collectors.toList()); 	
		
		if (!inputConstant.isEmpty()) {
			//Put "title" in this row
			
			row.setHeightInPoints(50);

			

			cell.setCellValue("Input Constant");
			cell.setCellStyle(styles.get("title"));
			sheet.addMergedRegion(CellRangeAddress.valueOf("A1:E1"));
			row = sheet.createRow(rownum++);

			for (int i = 0; i < inputConstantColumns.length; i++) {
				cell = row.createCell(i,Cell.CELL_TYPE_STRING);
				cell.setCellStyle(styles.get("header"));
				cell.setCellValue(inputConstantColumns[i]);		
			}

			for (Parametre parametre : inputConstant) {
				row = sheet.createRow(rownum++);
				cell = null;
				for (int i = 0; i < inputConstantColumns.length; i++) {
					cell = row.createCell(i);
					createParameterCells(cell, parametre);				
				}
			}

		}


		//Input Variable
		List<Parametre> inputVariable = compositionBriques.getParametre().parallelStream()
				.filter(p->p.getTypeP().equals(TypeParametre.INPUT) && !p.getSousTypeP().equals(SousTypeParametre.CONSTANT))
				.collect(Collectors.toList()); 	
		if (!inputVariable.isEmpty()) {


			rownum+=1;
			row = sheet.createRow(rownum++);
			row.setHeightInPoints(50);
			//Put "title in this row"
			cell = row.createCell(0, Cell.CELL_TYPE_STRING);	
			cell.setCellValue("Input Variable");
			cell.setCellStyle(styles.get("title"));
			 fromReference = new CellReference(cell);

			 colIndex = fromReference.getCol() +  + inputVariableColumns.length;
			 rowIndex = fromReference.getRow();

			 toReference = new CellReference(rowIndex, colIndex-1);
			sheet.addMergedRegion(CellRangeAddress.valueOf(fromReference.formatAsString()+":"+toReference.formatAsString()));

			row = sheet.createRow(rownum++);
			for (int i = 0; i < inputVariableColumns.length; i++) {
				cell = row.createCell(i,Cell.CELL_TYPE_STRING);
				cell.setCellStyle(styles.get("header"));
				cell.setCellValue(inputVariableColumns[i]);

			}


			for (Parametre parametre : inputVariable) {
				row = sheet.createRow(rownum++);		
				for (int i = 0; i < inputConstantColumns.length; i++) {
					cell = row.createCell(i);
					createParameterCells(cell, parametre);			
				}
			}
		}
		List<Equation> nonContrainte = compositionBriques.getEquation().parallelStream()
				.filter(e->e.isConstraint() == false)
				.collect(Collectors.toList()); 
		
		if(!nonContrainte.isEmpty()){
			//chainage du calcul
			rownum+=1;
			row = sheet.createRow(rownum++);
			row.setHeightInPoints(50);
			cell = row.createCell(0, Cell.CELL_TYPE_STRING);
			cell.setCellValue("Chainage du calcul");
			cell.setCellStyle(styles.get("title"));

			fromReference = new CellReference(cell);
			rowIndex = fromReference.getRow();
			colIndex = fromReference.getCol()+ equationColumns.length;

			toReference = new CellReference(rowIndex, colIndex-1);

			sheet.addMergedRegion(CellRangeAddress.valueOf(fromReference.formatAsString()+":"+toReference.formatAsString()));
			row = sheet.createRow(rownum++);
			for (int i = 0; i < equationColumns.length; i++) {
				cell = row.createCell(i, Cell.CELL_TYPE_STRING);
				cell.setCellStyle(styles.get("header"));
				cell.setCellValue(equationColumns[i]);

			}




			for (Equation equation : nonContrainte) {
				row = sheet.createRow(rownum++);
				for (int j = 0; j < equationColumns.length; j++) {
					cell = row.createCell(j);
					createEquationCells(cell, equation);



				}



			}

		}
		//output
		List<Parametre> outputParameterList = compositionBriques.getParametre().parallelStream()
				.filter(p->p.getTypeP().equals(TypeParametre.OUTPUT)).collect(Collectors.toList());
		if (!outputParameterList.isEmpty()) {


			rownum+=1;
			row = sheet.createRow(rownum++);
			row.setHeightInPoints(50);
			cell = row.createCell(0, Cell.CELL_TYPE_STRING);
			cell.setCellValue("Output");
			cell.setCellStyle(styles.get("title"));

			fromReference = new CellReference(cell);
			rowIndex = fromReference.getRow();
			colIndex = fromReference.getCol()+ outputColumns.length;

			toReference = new CellReference(rowIndex, colIndex-1);

			sheet.addMergedRegion(CellRangeAddress.valueOf(fromReference.formatAsString()+":"+toReference.formatAsString()));
			row = sheet.createRow(rownum++);
			for (int i = 0; i < outputColumns.length; i++) {
				cell = row.createCell(i, Cell.CELL_TYPE_STRING);
				cell.setCellStyle(styles.get("header"));
				cell.setCellValue(outputColumns[i]);

			}
			for (Parametre parametre : outputParameterList) {
				row = sheet.createRow(rownum++);
				for (int i = 0; i < outputColumns.length; i++) {
					cell = row.createCell(i);
					createParameterCells(cell, parametre);
				}
			}
		}
	/*	List<Equation> nonOriented = compositionBriques.getEquation().parallelStream()
				.filter(e->e.isOriented() == false).collect(Collectors.toList());
		for (Equation equation : nonOriented) {
			sheet.createRow(rownum++);

			List<Parametre> params = equation.getListeDeParametresEqn();
		}

		List<Equation> oriented = compositionBriques.getEquation().parallelStream()
				.filter(e->e.isOriented() == true).collect(Collectors.toList());
		
	 */
		//contraintes
		List<Equation> equationsContrainte = compositionBriques.getEquation().parallelStream()
				.filter(e->e.isConstraint() == true)
				.collect(Collectors.toList());
		if (!equationsContrainte.isEmpty()) {


			rownum+=1;
			row = sheet.createRow(rownum++);
			row.setHeightInPoints(50);
			cell = row.createCell(0, Cell.CELL_TYPE_STRING);
			cell.setCellValue("Contraintes");
			cell.setCellStyle(styles.get("title"));

			fromReference = new CellReference(cell);
			rowIndex = fromReference.getRow();
			colIndex = fromReference.getCol()+ outputColumns.length;

			toReference = new CellReference(rowIndex, colIndex-1);

			sheet.addMergedRegion(CellRangeAddress.valueOf(fromReference.formatAsString()+":"+toReference.formatAsString()));
			row = sheet.createRow(rownum++);
			for (int i = 0; i < equationColumns.length; i++) {
				cell = row.createCell(i, Cell.CELL_TYPE_STRING);
				cell.setCellStyle(styles.get("header"));
				cell.setCellValue(equationColumns[i]);

			}



			for (Equation equation : equationsContrainte) {
				row = sheet.createRow(rownum++);
				for (int j = 0; j < equationColumns.length; j++) {
					cell = row.createCell(j);
					createEquationCells(cell, equation);
				}
			}
		}

        //finally set column widths, the width is measured in units of 1/256th of a character width
        sheet.setColumnWidth(0, 10*256); //15 characters wide
        sheet.setColumnWidth(1, 25*256); //25 characters wide
        sheet.setColumnWidth(2, 30*256); //30 characters wide
        sheet.setColumnWidth(3, 35*256); //35 characters wide
        sheet.setColumnWidth(4, 35*256); //35 characters wide
		
        //writeout the to file
		try (FileOutputStream fileOut = new FileOutputStream(
				new File(printLocation))){

			workbook.write(fileOut);

		} catch (Exception e) {
			e.printStackTrace();

			if(e instanceof FileNotFoundException){
				System.err.println("Fichier perdu ou utilisé par un autre programme." );
				setPrintSuccess(false);
				setPrintError("Fichier perdu ou utilisé par un autre programme.");
				Display.getDefault().asyncExec(()->{
					MessageDialog.openInformation(Display.getDefault().getActiveShell(), "Erreur", "Fichier perdu ou utilisé par un autre programme.");
					
				});
			}
		}
	}

	private static Map<String, CellStyle> createStyles(XSSFWorkbook wb){
		Map<String, CellStyle> styles = new HashMap<String, CellStyle>();

		CellStyle style;
		XSSFFont titleFont = wb.createFont();
		titleFont.setFontHeightInPoints((short)17);
		titleFont.setFontName("Trebuchet MS");
		style = wb.createCellStyle();
		style.setFont(titleFont);
	//	style.setBorderBottom(CellStyle.BORDER_THICK);
	//	style.setBottomBorderColor(IndexedColors.GREY_80_PERCENT.getIndex());       
		style.setAlignment(CellStyle.ALIGN_CENTER);
		style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
		style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
		style.setFillPattern(CellStyle.SOLID_FOREGROUND);
		styles.put("title", style);

		XSSFFont headerFont = wb.createFont();
		headerFont.setFontHeightInPoints((short)12);
		headerFont.setFontName("Trebuchet MS");
		style = wb.createCellStyle();
		// style.setAlignment(CellStyle.ALIGN_LEFT);
		style.setBorderTop(CellStyle.BORDER_THICK);		
		style.setTopBorderColor(IndexedColors.GREY_80_PERCENT.getIndex());  
		style.setFont(headerFont);
		style.setFillPattern(CellStyle.SOLID_FOREGROUND);
		style.setFillForegroundColor(IndexedColors.PALE_BLUE.getIndex());
		
		styles.put("header", style);
		
		XSSFFont textFont = wb.createFont();
		textFont.setFontHeightInPoints((short)10);
		style = wb.createCellStyle();
		style.setFont(textFont);
		style.setWrapText(false);
		
		styles.put("text", style);
		return styles;
	}
	
	
	
	private void createEquationCells(Cell cell, Equation equation){
		int index = cell.getColumnIndex();
		
		switch (index) {
		case 0:
			//this row will contain the equation rewritten, referencing the parameter cells.
			cell.setCellType(Cell.CELL_TYPE_FORMULA);
			outputParameterToCellMapping.put(equation, cell);
			
			Map<Integer, Object> map = function(equation);
			StringBuilder builder = new StringBuilder();
			for(Iterator<Map.Entry<Integer, Object>> entry = map.entrySet().iterator(); entry.hasNext();){
				Object object = entry.next().getValue();
			
				if (object instanceof Parametre){
					Parametre p = (Parametre)object;
					if(parameterToCellMapping.containsKey(p))	{					
						CellReference reference = new CellReference(parameterToCellMapping.get(p));
						builder.append(reference.formatAsString());
					}

				}
				else if (object instanceof String) {
					String string = (String)object;
					if(!string.contains("="))
						builder.append(string);

				}
			}
		
			try {
				cell.setCellFormula(builder.toString());
			} catch (Exception e) {
				if (e instanceof FormulaParseException) {
					setPrintSuccess(false);
					setPrintError("Erreur(s) lors de la generation du fichier.");
					Display.getDefault().asyncExec(()->{
						MessageDialog.openError(Display.getDefault().getActiveShell(), "Erreur", "Formula parse error, Vérifier l'entrée. "+equation.getContenuEqn());
					});
					

				}
			}
			
			break;
		case 1:
			cell.setCellType(Cell.CELL_TYPE_STRING);
			if (equation.getParametreDeSortie() != null) {
				cell.setCellValue(equation.getParametreDeSortie().getNomP());
			}
			else {
				cell.setCellValue("null");
			}
			break;
		case 2:
			cell.setCellType(Cell.CELL_TYPE_STRING);
			cell.setCellValue(equation.getContenuEqn());
			break;
		case 3:
			cell.setCellType(Cell.CELL_TYPE_STRING);
			cell.setCellValue(equation.getDescEqn());
			cell.setCellStyle(styles.get("text"));
			break;
		case 4:
			cell.setCellType(Cell.CELL_TYPE_STRING);
			cell.setCellValue(equation.getProprieteEqn());
			cell.setCellStyle(styles.get("text"));
			break;
		default:
			break;
		}
	}

	private static void createParameterCells(Cell cell, Parametre content){
		int index = cell.getColumnIndex();

		switch (index) {

		case 0:
			if (content.getTypeP().equals(TypeParametre.INPUT)) {


				if (content.getSousTypeP().equals(SousTypeParametre.CONSTANT)) {
					cell.setCellType(Cell.CELL_TYPE_NUMERIC); 
					cell.setCellValue(Float.parseFloat((String)content.getValeurP()));
					parameterToCellMapping.put(content, cell);
				}
				else {
					if (content.getSousTypeP().equals(SousTypeParametre.RANGE)) {
						String regex = "[\\[|\\]]";
						String[] parts = content.getValeurP().replaceAll(regex, "").split(";", 0);
						float moyenne = 0;
						for (int i = 0; i < parts.length; i++) {
							moyenne += Float.parseFloat(parts[0]);
						}
						cell.setCellType(Cell.CELL_TYPE_NUMERIC);
						cell.setCellValue(moyenne/parts.length);
						parameterToCellMapping.put(content, cell);
					}
					else if (content.getSousTypeP().equals(SousTypeParametre.SET)) {
						String regex = "[\\{|\\}]";
						String[] parts = content.getValeurP().replaceAll(regex, "").split(";", 0);
						float moyenne = 0;
						for (int i = 0; i < parts.length; i++) {
							moyenne += Float.parseFloat(parts[0]);
						}
						cell.setCellType(Cell.CELL_TYPE_NUMERIC);
						cell.setCellValue(moyenne/parts.length);
						parameterToCellMapping.put(content, cell);
					}
				}
			
			}
			else if (content.getTypeP().equals(TypeParametre.OUTPUT)) {
				cell.setCellType(Cell.CELL_TYPE_FORMULA);
				Set<Equation> keySet = outputParameterToCellMapping.keySet();
				for (Equation equation : keySet) {
					if (equation.getParametreDeSortie().equals(content)) {
						Cell cellRef = outputParameterToCellMapping.get(equation);
						if(cellRef != null){
							CellReference reference = new CellReference(cellRef);
							cell.setCellFormula(reference.formatAsString());
						}
						
					}
				}
				
			
				
			}
			break;
		case 1:
			if (content.getTypeP().equals(TypeParametre.INPUT)) {		
				if (content.getSousTypeP().equals(SousTypeParametre.CONSTANT)) {
					cell.setCellType(Cell.CELL_TYPE_STRING); 
					cell.setCellValue((String)content.getUniteP());
				}
				else {
					cell.setCellType(Cell.CELL_TYPE_STRING); 
					cell.setCellValue((String)content.getValeurP());
				}
			}
			else if (content.getTypeP().equals(TypeParametre.OUTPUT)) {
				cell.setCellType(Cell.CELL_TYPE_STRING); 
				cell.setCellValue((String)content.getUniteP());
			}
			
			break;
		case 2:
			cell.setCellType(Cell.CELL_TYPE_STRING); 
			cell.setCellValue((String)content.getNomP());	
			break;

		case 3:
			cell.setCellType(Cell.CELL_TYPE_STRING); 
			cell.setCellValue((String)content.getDescP());	
			cell.setCellStyle(styles.get("text"));
			break;
		case 4:
			cell.setCellType(Cell.CELL_TYPE_STRING); 
			cell.setCellValue((String)content.getProprieteP());
			cell.setCellStyle(styles.get("text"));
			break;
		
		default:
			break;
		}
	}
	private final static String PARAMETER_PATTERN =  "([a-z]+\\w*)";//(\\s*[a-z]+\\w*\\s*{1,20})";
	//private final static String AROUND_PARAMETER_PATTERN = "(?!"+PARAMETER_PATTERN+")*(\\W?+)(?!"+PARAMETER_PATTERN+")*";
	private final static String AROUND_PARAMETER_PATTERN = "(?!"+PARAMETER_PATTERN+")*([\\W?+|\\d+])(?!"+PARAMETER_PATTERN+")*";

	public static Map<Integer, Object> function(Equation equation){

		Matcher parameterMatcher = Pattern.compile(PARAMETER_PATTERN).matcher("");
		Matcher matcher = Pattern.compile(AROUND_PARAMETER_PATTERN).matcher("");

		//Insert all the characters in the list
		String content = equation.getContenuEqn();	
		Map<Integer, Object> locations = new TreeMap<>();


		matcher.reset(content);
		parameterMatcher.reset(content);

		while (parameterMatcher.find()) {				
			if (!parameterMatcher.group().isEmpty()) {

			}
			Parametre p = equation.getParametreByName(parameterMatcher.group());
			if (p != null) {
			
				int start = parameterMatcher.start();

				locations.put(start, p);					
			}

		}
		while (matcher.find()) {				
			if (!matcher.group().isEmpty()) {				
				int start = matcher.start();

				locations.put(start,matcher.group(2));
			}
		}
		
		return locations;
	}
	
	
	public static void orderCompositionContents(CompositionBriques compositionBriques) {

		if (!compositionBriques.equals(null) && compositionBriques.getBrique() != null) {
			List<Equation> listEqns = compositionBriques.getEquation(); 
			List<Parametre> listParametres = compositionBriques.getParametre();

			Collections.sort(listEqns, (e1,e2)-> Boolean.valueOf(e1.getIsOriented()).compareTo(Boolean.valueOf(e2.getIsOriented())));

			//Ordonner la liste de tous les parametres par les types INPUT/UNDETERMINED/OUTPUT 	
			Collections.sort(listParametres,(p1,p2)-> p1.getTypeP().compareTo(p2.getTypeP()));	
		}

	}
/*
	public static void main(String[] args) throws MainApplicationException{

		//	for(Iterator<Map.Entry<String, String>> entry = System.getenv().entrySet().iterator(); entry.hasNext();)
		//System.err.println("Key: "+entry.next().getKey() + " Value: "+ entry.next().getValue());
		//	System.out.println(entry.next());



		File file = new File(ClassLoader.getSystemResource("org/ica/resources/").getFile(),"reality.xml");

		Serializer serializer = new Persister();		
		CompositionBriques composition= new CompositionBriques("", new ArrayList<>());
		try{			

			serializer.read(composition, file);			
		} catch (Exception e) {
			System.err.println("Impossible d'ouvrir, Fichier Corrompu");
			e.printStackTrace();
		}
		List<Equation> equationList = composition.getEquation();
	//	MatchIdenticalParameters.matchIdenticalBriqueParameters(equationList);
		Set<Parametre> parametreSet = new HashSet<>();
		for (Equation equation : equationList) {
			for (Parametre parametre : equation.getListeDeParametresEqn()) {
				parametreSet.add(parametre);
			}

		}
		composition.setParametre(new ArrayList<>(parametreSet));
		PrintResultsToExcel.orderCompositionContents(composition);
		



	new PrintResultsToExcel(composition, "D:\\file.xlsx");

		
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

	}
*/	

	/**
	 * @return the compositionBriques
	 */
	public CompositionBriques getCompositionBriques() {
		return compositionBriques;
	}


	/**
	 * @return the printError
	 */
	public String getPrintError() {
		return printError;
	}


	/**
	 * @param printError the printError to set
	 */
	public void setPrintError(String printError) {
		this.printError = printError;
	}


	/**
	 * @return the printSuccess
	 */
	public boolean isPrintSuccess() {
		return printSuccess;
	}


	/**
	 * @param printSuccess the printSuccess to set
	 */
	public void setPrintSuccess(boolean printSuccess) {
		this.printSuccess = printSuccess;
	}
}

