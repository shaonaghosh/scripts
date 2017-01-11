import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;


public class Txt2Csv {
	static final String[] PREFIXES = {
		//"ResultsDataset",
		//"resultsp1offline"
		"resultslongestpath"
		//"resultsisingapp"
	};
	public void convertToCsv(File file){
		if(file.exists()){
			String txtName = file.getName();
			String csvName = txtName.substring(0, txtName.lastIndexOf('.')) + ".csv";
			String namepart = txtName.substring(0, txtName.lastIndexOf('.'));
			String[] parts = namepart.split("-");
			String part1 = parts[0]; // 004
			String part2 = parts[1]; // 
			//String csvName = "Ising" + part1 + "-" + part2 + ".csv";
			System.out.println("Converting " + txtName + " to " + csvName);
			try {
				BufferedReader reader = new BufferedReader(new FileReader(file));
				ArrayList<String> rows = new ArrayList<>();
				String row;
				while((row = reader.readLine()) != null){
					String trimmed = row.trim();
					if(!trimmed.isEmpty())
						rows.add(trimmed);
				}				
				reader.close();
				
				if( !rows.isEmpty()){
					StringBuilder sb = new StringBuilder();
					for(int i = 0; i < rows.size(); i++){
						sb.append(rows.get(i));
						if( i < rows.size() - 1)
							sb.append(",");
					}
					
					BufferedWriter writer = new BufferedWriter(new FileWriter(new File(csvName)));
					writer.write(sb.toString());
					writer.newLine();
					writer.close();
				}
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public void convertFiles(){
		File root = new File(".");
		File[] files = root.listFiles(new FilenameFilter() {
			
			@Override
			public boolean accept(File dir, String name) {
				for(String prefix : PREFIXES){
					if( name.toLowerCase().startsWith(prefix.toLowerCase()) && name.toLowerCase().endsWith(".txt"))
						return true;
				}
				return false;
			}
		});
		
		for(File f : files){
			convertToCsv(f);
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Txt2Csv t2c = new Txt2Csv();
		t2c.convertFiles();

	}

}
