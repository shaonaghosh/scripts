import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.Map.Entry;


public class Csv2Dot {
	
	class DotGraph {
		
		LinkedHashMap<Integer, Set<Integer>> edges;
		LinkedHashMap<Integer, ArrayList<Integer>> edgeweights;
		
		public DotGraph(){
			edges = new LinkedHashMap<Integer, Set<Integer>>();
			edgeweights = new LinkedHashMap<Integer, ArrayList<Integer>>();
		}
		
		public void addEdge(int first, int second, int wt){
			int one, two, weight;
			/*if( first <= second){
				one = first;
				two = second;
			}
			else{
				one = second;
				two = first;
			}*/
			//for di graph
			one = first;
			two = second;
			weight = wt;
			
			Set<Integer> neigh = null;
			ArrayList<Integer> weights = null;
			if( edges.containsKey(one) && edgeweights.containsKey(one)){
				neigh = edges.get(one);
				weights = edgeweights.get(one);
			}else{
				neigh = new LinkedHashSet<>();
				weights = new ArrayList<Integer>();
				edges.put(one, neigh);
				edgeweights.put(one,weights);
			}
			
			neigh.add(two);
			weights.add(weight);
		}
		
		public void clear(){
			edges.clear();
			edgeweights.clear();
		}

		@Override
		public String toString() {
			//StringBuilder sb = new StringBuilder("graph {\n");
			StringBuilder sb = new StringBuilder("digraph {\n");
			/*for(Entry<Integer, Set<Integer>> entry : edges.entrySet()){
				for(Integer n : entry.getValue()){
					sb.append(entry.getKey()).append(" -> ").append(n).append(";\n");
				}
				
			}*/
			Iterator<Entry<Integer, Set<Integer>>> it1 = edges.entrySet().iterator();
			Iterator<Entry<Integer, ArrayList<Integer>>> it2 = edgeweights.entrySet().iterator();
			while (it1.hasNext() && it2.hasNext()) {
				Entry<Integer, Set<Integer>>  entry =  it1.next();
				Entry<Integer, ArrayList<Integer>>  entry2 =  it2.next();
				Integer key2 = entry2.getKey();
			//for(Entry<Integer, Set<Integer>> entry : edges.entrySet()){
				Iterator<Integer> itset1 = entry.getValue().iterator();
				Iterator<Integer> itset2 = entry2.getValue().iterator();
				while(itset1.hasNext() && itset2.hasNext()){
				//for(Integer n : entry.getValue()){
					Integer n = itset1.next(); 
					Integer n2 = itset2.next();
					//sb.append(entry.getKey()).append(" -- ").append(n).append(";\n");
					sb.append(entry.getKey()).append(" -> ").append(n).append(" [label=").append(String.valueOf(n2)).append("]").append(";\n");
				}
			}
			sb.append("}");
			return sb.toString();
		}
		
		
	}
	
	DotGraph graph;
	
	public Csv2Dot(){
		graph = new DotGraph();
	}
	
	public void parseCSV(File file){
		//File file = new File(path);
		if( file.exists()){
			BufferedReader reader;
			try {
				reader = new BufferedReader(new FileReader(file));
				
				String row;
				int r = 0;
				graph.clear();
				
				while((row = reader.readLine()) != null){
					String[] values = row.split(",|\\W");
					if( values != null){
						int c = 0;
						for(String value : values){
							if( !value.isEmpty()){
								if( Integer.valueOf(value) != 0 ){
									
									//System.out.println("Adding edge " + (int)(r+1) + " -- " + (int)(c+1));
									graph.addEdge(r + 1, c + 1,Integer.valueOf(value));
								}
								c++;
							}
						}
					}
					r++;
				}
				reader.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	}
	
	
	
	public void createDOT(String path){
		File file = new File(path);
		
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(file));
			writer.write(graph.toString());
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public void convertFiles(){
		File root = new File(".");
		File[] files = root.listFiles(new FilenameFilter() {
			
			@Override
			public boolean accept(File dir, String name) {
				return name.toLowerCase().endsWith(".csv");
			}
		});
		
		for(File f : files){
			String csvName = f.getName();
			String outName = csvName.substring(0, csvName.lastIndexOf('.')) + ".dot";
			System.out.println("Converting " + csvName + " to " + outName);
			parseCSV(f);
			createDOT(outName);
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Csv2Dot c2d = new Csv2Dot();
		c2d.convertFiles();

	}

}
