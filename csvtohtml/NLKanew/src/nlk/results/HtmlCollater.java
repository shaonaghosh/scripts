package nlk.results;



import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.TreeSet;
import java.util.List;


public class HtmlCollater {
	
	class AlgoResult {
		int algoIndex;
		int n, l, k, nse;
		double mean;
		double sd;
	}
	
	/*static final HashMap<String, Integer> ALGO_PREFIX = new HashMap<>();
	static {
		ALGO_PREFIX.put("results", 1);
		ALGO_PREFIX.put("ResultsDataset", 2);
	}*/
	static final String[] ALGO_PREFIX = {
		//"results",
		"ResultsDatasetclass1Vs2",
		//"resultsisapp",
		"resultslongestpath"
		//"resultsisingapp"
		//"resultsp1offline"
		
		//"resultslongestpath1586"
	};
	
	static final String[] ALGO_NAME = {
		//"p2",
		"treeopt",
		//"isingapprox",
		"shortestpath"
		//"isingapp"
		//"p1offline"
		//"five"
	};
	static final String DEFAULT_RESULTS_DIR = "results";
	static final String COLLATED_DIR = "collated2";
	String reportDir;
	
	HashMap<Integer, HashMap<Integer, HashMap<Integer, HashMap <Integer, AlgoResult[]>>>> resultMap;
	TreeSet<Integer> setN, setL, setK, setnse;

	public HtmlCollater(String dir){
		if(dir != null)
			reportDir = dir;
		else{
			File file = new File(".");
			try {
				reportDir = file.getCanonicalPath() + "/";// + DEFAULT_RESULTS_DIR;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		File outDir = new File(reportDir, COLLATED_DIR);
		outDir.mkdir();
		
		resultMap = new HashMap<>();

		setN = new TreeSet<>();
		setL = new TreeSet<>();
		setK = new TreeSet<>();
		setnse = new TreeSet<>();
		parseResults();
	}
	
	private void parseResults(){
		System.out.println("Parsing report directory " + reportDir + "..");
		File dir = new File(reportDir);
		if(dir.exists() && dir.isDirectory()){
			File[] files = dir.listFiles(new FilenameFilter() {
				
				@Override
				public boolean accept(File dir, String name) {
					// TODO Auto-generated method stub
					return name.toLowerCase().endsWith(".csv");
				}
			});
			
			if(files != null){
				for(int i = 0; i < files.length; i++){
					String fullname = files[i].getName();
					
					String[] splitName = fullname.split("-|\\.");
					if( splitName.length == 3 ){
					
						for(int a = 0; a < ALGO_PREFIX.length; a++){
							if( splitName[0].matches(ALGO_PREFIX[a] + "[0-9]*")){// + "[ising]*")){
								AlgoResult algoResult = new AlgoResult();
								algoResult.algoIndex = a;
								
								String name = splitName[1];
								
								String[] params = name.split("n|l|k|rst");
								if(params.length >= 4){
									algoResult.n = Integer.valueOf(params[1]);
									algoResult.l = Integer.valueOf(params[2]);
									algoResult.k = Integer.valueOf(params[3]);
									algoResult.nse = Integer.valueOf(params[4]);
									
									if(readResults(files[i], algoResult)){
										addResult(algoResult);
										
										
										System.out.println("Algorithm " + algoResult.algoIndex + "(" + ALGO_NAME[a] + ")" +
												": n = " + algoResult.n + 
												", l = " + algoResult.l +
												", k = " + algoResult.k +
												", mean = " + algoResult.mean +
												", sd = " + algoResult.sd);
									}
								}
							}
						}
						
						
					}
				}
			}
		}
	}
	
	private void addResult(AlgoResult result){
		setN.add(result.n);
		setL.add(result.l);
		setK.add(result.k);
		setnse.add(result.nse);
		
		HashMap<Integer, HashMap<Integer, HashMap<Integer, AlgoResult[]>>> mapL;
		HashMap<Integer, HashMap<Integer, AlgoResult[]>> mapK;
		HashMap<Integer, AlgoResult[]> mapNse;
		
		if(!resultMap.containsKey(result.n)){
			mapL = new HashMap<Integer, HashMap<Integer, HashMap<Integer, AlgoResult[]>>>();
			resultMap.put(result.n, mapL);
		}else{
			mapL = resultMap.get(result.n);
		}
		
		if( !mapL.containsKey(result.l)){
			mapK = new HashMap<Integer, HashMap<Integer, AlgoResult[]>>();
			mapL.put(result.l, mapK);
		}else{
			mapK = mapL.get(result.l);
		}
		
		if(!mapK.containsKey(result.k)){
			mapNse = new HashMap<Integer, AlgoResult[]>();
			mapK.put(result.k, mapNse);
			//mapK.put(result.k, new AlgoResult[ALGO_PREFIX.length]);
		}else{
			mapNse = mapK.get(result.k);
		}
		
		if(!mapNse.containsKey(result.nse)){
			mapNse.put(result.nse, new AlgoResult[ALGO_PREFIX.length]);
		}
		
		mapNse.get(result.nse)[result.algoIndex] = result;
		
		
	}
	
	private boolean readResults(File file, AlgoResult result){
		boolean ret = false;
		try {
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String line = reader.readLine();
			if( line != null){
				String[] values = line.split(",");
				if(values.length >= 2){
					String strMean = values[0].replace("\"", "");
					String strSd = values[1].replace("\"", "");
					
					if( values.length == 3){ // hack to fix improper csv
						String exponent = "-" + values[2].replace("\"", "");
						if( strSd.toUpperCase().endsWith("E"))
							strSd += exponent;
					}
					
					result.mean = Double.valueOf(strMean);
					result.sd = Double.valueOf(strSd);
					
					ret = true;
				}
			}
			reader.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NumberFormatException e){
			System.err.println("Error parsing " + file.getName());
			e.printStackTrace();
		}
		
		return ret;
	}
	
	public void collateHtml(){
		System.out.println("Collating results into HTML..");
		File outFile = new File(reportDir + "/" + COLLATED_DIR, "collated.html");
		File outFileNR = new File(reportDir + "/" + COLLATED_DIR, "collatedNotRounded.html");
		System.out.println("Writing to " + outFile.getName());
		NumberFormat formatter = new DecimalFormat("0.###");
		
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(outFile));
			BufferedWriter writerNR = new BufferedWriter(new FileWriter(outFileNR));
			openHtml(writer);
			addTimeStamp(writer);
			openHtml(writerNR);
			addTimeStamp(writerNR);

			for(Integer n : setN){
			for(Integer l : setL){
			  for(Integer k : setK){
				
				if(resultMap.containsKey(n) && resultMap.get(n).containsKey(l)){
					addLineBreak(writer);
					addLineBreak(writerNR);
					addHeading(String.format("l = %d, n = %d, k = %d", l, n, k), writer);
					addHeading(String.format("l = %d, n = %d, k = %d", l, n, k), writerNR);
					openTable(writer);
					openTable(writerNR);

					String[] nseVals = new String[setnse.size()];
					int i = 0;
					for (Integer ns1 : setnse) {
						//nseVals[i++] = "nse = " + ns1;
						nseVals[i++] = "rst = " + ns1;
					}

					addTableHeader(nseVals, 1, writer);
					addTableHeader(nseVals, 1, writerNR);

					for (int a = 0; a < ALGO_NAME.length; a++) {
						
						openRow(ALGO_NAME[a], writer);
						openRow(ALGO_NAME[a], writerNR);
						for (Integer ns1 : setnse) {
							if (resultMap.get(n).get(l).get(k).containsKey(ns1)) {
								
								AlgoResult[] results = resultMap.get(n).get(l).get(k).get(ns1);
								
								if (results != null && results[a] != null) {
									addItem(formatter.format(results[a].mean) + " (sd = " + formatter.format(results[a].sd) + ")", writer);
									//addItem(Double.toString(results[a].mean), Double.toString(results[a].sd), writerNR);
									addItem(Double.toString(results[a].mean) + " (" + Double.toString(results[a].sd) + ")", writerNR);
								}else{
									//addItem("", "", writer);
									addItem("", writer);
									addItem("", writerNR);
								}
							}
						}
						closeRow(writer);
						closeRow(writerNR);
					}						
					
					closeTable(writer);
					closeTable(writerNR);
					addLineBreak(writer);
					addLineBreak(writerNR);
				}
			}
		}
		}	
			
			
			
			/*for(Integer n : setN){
				for(Integer l : setL){
					
					if(resultMap.containsKey(n) && resultMap.get(n).containsKey(l)){
						addLineBreak(writer);
						addLineBreak(writerNR);
						addHeading(String.format("l = %d, n = %d", l, n), writer);
						addHeading(String.format("l = %d, n = %d", l, n), writerNR);
						openTable(writer);
						openTable(writerNR);

						String[] kVals = new String[setK.size()];
						int i = 0;
						for (Integer k : setK) {
							kVals[i++] = "k = " + k;
						}

						addTableHeader(kVals, 1, writer);
						addTableHeader(kVals, 1, writerNR);

						for (int a = 0; a < ALGO_NAME.length; a++) {
							
							openRow(ALGO_NAME[a], writer);
							openRow(ALGO_NAME[a], writerNR);
							for (Integer k : setK) {
								if (resultMap.get(n).get(l).containsKey(k)) {
									
									AlgoResult[] results = resultMap.get(n).get(l).get(k);
									
									if (results != null && results[a] != null) {
										addItem(formatter.format(results[a].mean) + " (sd = " + formatter.format(results[a].sd) + ")", writer);
										//addItem(Double.toString(results[a].mean), Double.toString(results[a].sd), writerNR);
										addItem(Double.toString(results[a].mean) + " (" + Double.toString(results[a].sd) + ")", writerNR);
									}else{
										//addItem("", "", writer);
										addItem("", writer);
										addItem("", writerNR);
									}
								}
							}
							closeRow(writer);
							closeRow(writerNR);
						}						
						
						closeTable(writer);
						closeTable(writerNR);
						addLineBreak(writer);
						addLineBreak(writerNR);
					}
				}
			}*/
			
			
			closeHtml(writer);
			closeHtml(writerNR);
			writer.close();
			writerNR.close();
		}catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	private void openHtml(BufferedWriter html) throws IOException{
		html.write(
				"<!DOCTYPE html>\n" + 
				"<html>\n" + 
				"\n" + 
				"<head>\n" + 
				"<style>\n" + 
				"table, th, td\n" + 
				"{\n" + 
				"border-collapse:collapse;\n" + 
				"border:1px solid black;\n" + 
				"}\n" + 
				"th, td\n" + 
				"{\n" + 
				"padding:5px;\n" + 
				"}\n" + 
				"html *\r\n" + 
				"{\n" + 
				"   color: #1f1f1f !important;\n" + 
				"   font-family: sans-serif !important;\n" + 
				"}\n" +
				"</style>\n" + 
				"</head>\n" + 
				"\n" + 
				"<body>\n" + 
				"<h1>1 Vs. 2 RSTs with shortest path</h1>\n"
				);
	}
	
	private void addTimeStamp(BufferedWriter html)throws IOException{
		Date dNow = new Date( );
	    SimpleDateFormat ft = new SimpleDateFormat ("EEEE dd MMMM yyyy 'at' hh:mm:ss a zzz");
	    String user = System.getenv("USERNAME");
	    html.write("<I>Generated by " + user + " on " + ft.format(dNow) + "</I>");
	    addLineBreak(html);

	}
	
	private void closeHtml(BufferedWriter html)throws IOException{
		html.write("</body>\n</html>");
	}
	
	private void addLineBreak(BufferedWriter html)throws IOException{
		html.write("<br>\n");
	}
	
	@SuppressWarnings("unused")
	private void addParagraph(BufferedWriter html)throws IOException{
		html.write("<p>\n");
	}
	
	private void openTable(BufferedWriter html)throws IOException{
		html.write("<table>\n");
	}
	
	private void closeTable(BufferedWriter html)throws IOException{
		html.write("</table>\n");
	}
	
	private void addHeading(String header, BufferedWriter html) throws IOException {
		html.write("<h3>" + header + "</h3>\n");
		
	}
	
	private void addTableHeader(String[] header, int span, BufferedWriter html) throws IOException {
		html.write(
				"<tr>\n" + 
				"  <th> </th>\n"
				);
		for(String h: header){
			if( span > 1){
				html.write("  <th colspan=\"" + span + "\">" + h + "</th>\n");
			}else{
				html.write("  <th >" + h + "</th>\n");
			}
		}
		html.write("</tr>\n"); 
		
	}
	
	private void openRow(String header, BufferedWriter html) throws IOException {
		html.write(
				"<tr>\n" + 
				"  <th style=\"text-align:left\">" + header + "</th>\n"
				);
		
	}
	
	@SuppressWarnings("unused")
	private void addItem(String item1, String item2, BufferedWriter html) throws IOException {
		if( !item1.isEmpty()){
			html.write(	"  <td>" + item1 + "</td>\n");
		}else{
			html.write(	"  <td style=\"text-align:center\">-</td>\n");
		}
		
		if( !item2.isEmpty()){
			html.write(	"  <td>" + item2 + "</td>\n");
		}else{
			html.write(	"  <td style=\"text-align:center\">-</td>\n");
		}
	}
	
	private void addItem(String item, BufferedWriter html) throws IOException {
		if( !item.isEmpty()){
			html.write(	"  <td>" + item + "</td>\n");
		}else{
			html.write(	"  <td style=\"text-align:center\">-</td>\n");
		}

	}
	
	private void closeRow(BufferedWriter html) throws IOException {
		html.write("</tr>\n"); 
		
	}

	/*
	public void collateResults(){
		System.out.println("Collating results..");
		System.out.println("Fixed n & l parameters:");
		//Keep n & l fixed
		for(Integer n : setN){
			for(Integer l : setL){
				
				String filename = String.format("collated-n%dl%dkx.csv", n, l);
				StringBuffer header = new StringBuffer("n,l,k,");
				for(int a = 0; a < ALGO_NAME.length; a++){
	//				int id = a + 1;
					header.append(String.format("%s.mean,%s.sd,", ALGO_NAME[a], ALGO_NAME[a]));
				}
				ArrayList<String> rows = new ArrayList<>();
				for(Integer k : setK){
					if(resultMap.containsKey(n) && 
					   resultMap.get(n).containsKey(l) &&
					   resultMap.get(n).get(l).containsKey(k)){
						AlgoResult[] results = resultMap.get(n).get(l).get(k);
						if( results != null){
							
							
							StringBuffer sb = new StringBuffer();
							sb.append(String.format("%d,%d,%d,", n, l, k));
							for(int a = 0; a < results.length; a++){
								if( results[a] == null){
									sb.append("NA,NA,");
									
								}else{						
								
									sb.append(String.format("%f,%f,",										
										results[a].mean, results[a].sd));
								}
							}
							if(sb != null)
								rows.add(sb.toString());
						}
					}
				}
				
				if(!rows.isEmpty()){
					createCollated(filename, header.toString(), rows);
				}
				
			}
		}
		
		System.out.println("Fixed n & k parameters:");
		//Keep n & k fixed
		for(Integer n : setN){
			for(Integer k : setK){
				String filename = String.format("collated-n%dlxk%d.csv", n, k);
				
				StringBuffer header = new StringBuffer("n,l,k,");
				for(int a = 0; a < ALGO_NAME.length; a++){
	//				int id = a + 1;
					header.append(String.format("%s.mean,%s.sd,", ALGO_NAME[a], ALGO_NAME[a]));
				}
				ArrayList<String> rows = new ArrayList<>();
				for(Integer l : setL){
					if(resultMap.containsKey(n) && 
					   resultMap.get(n).containsKey(l) &&
					   resultMap.get(n).get(l).containsKey(k)){
						AlgoResult[] results = resultMap.get(n).get(l).get(k);
						if( results != null){
							
							
							StringBuffer sb = new StringBuffer();
							sb.append(String.format("%d,%d,%d,", n, l, k));
							for(int a = 0; a < results.length; a++){
								if( results[a] == null){
									sb.append("NA,NA,");
									
								}else
								sb.append(String.format("%f,%f,",										
										results[a].mean, results[a].sd));
							}
							if(sb != null)
								rows.add(sb.toString());
						}
					}
				}
				
				if(!rows.isEmpty()){
					createCollated(filename, header.toString(), rows);
					//createCollated(filenameNR,header.toString(),rows);
				}
				
			}
		}
		
		System.out.println("Fixed l & k parameters:");
		//Keep l & k fixed
		for(Integer l : setL){
			for(Integer k : setK){
				String filename = String.format("collated-nxl%dk%d.csv", l, k);
				StringBuffer header = new StringBuffer("n,l,k,");
				for(int a = 0; a < ALGO_NAME.length; a++){
//					int id = a + 1;
					header.append(String.format("%s.mean,%s.sd,", ALGO_NAME[a], ALGO_NAME[a]));
				}
				ArrayList<String> rows = new ArrayList<>();
				for(Integer n : setN){
					if(resultMap.containsKey(n) && 
					   resultMap.get(n).containsKey(l) &&
					   resultMap.get(n).get(l).containsKey(k)){
						AlgoResult[] results = resultMap.get(n).get(l).get(k);
						if( results != null){
							
							
							StringBuffer sb = new StringBuffer();
							sb.append(String.format("%d,%d,%d,", n, l, k));
							for(int a = 0; a < results.length; a++){
								if( results[a] == null){
									sb.append("NA,NA,");
									
								}else
								sb.append(String.format("%f,%f,",										
										results[a].mean, results[a].sd));
							}
							if(sb != null)
								rows.add(sb.toString());
						}
					}
				}
				
				if(!rows.isEmpty()){
					createCollated(filename, header.toString(), rows);
				}
				
			}
		}
	}
	*/

	
	private void createCollated(String name, String header, List<String> rows){
		File outFile = new File(reportDir + "/" + COLLATED_DIR, name);
		System.out.println("Writing to " + outFile.getName());
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(outFile));
			writer.write(header);
			writer.newLine();
			
			for(String row : rows){
				writer.write(row);
				writer.newLine();
			}
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		HtmlCollater collater;
		if(args.length < 2)
			collater = new HtmlCollater(null);
		else
			collater = new HtmlCollater(args[0]);

		//collater.collateResults();
		collater.collateHtml();
	}

}
