import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.ArrayList;


public class Combine {

	static final String OUTPUT_DIR = "combined";
	static final String[] PREFIXES = {
	    //"CollapsedGraph",
		"CompressedGraph",
		//"OriginalGraph",
		"resultslongestpath",
                "Maps",
                "predlongpath",
		"truepredlongpath",
		"Pred",
		
	};
	
	ArrayList<String> fileNames;
	
	
	public Combine(){
		fileNames = new ArrayList<>();
		File output = new File(OUTPUT_DIR);
		output.mkdir();
	}
	
	public void scanDirs(){
		File root = new File(".");
		for(File entry : root.listFiles()){
			if(entry.isDirectory() && entry.getName().matches("label1000+")){
				File[] files = entry.listFiles(new FilenameFilter() {
					
					@Override
					public boolean accept(File parent, String name) {
						for(String prefix : PREFIXES){
							if( name.toLowerCase().startsWith(prefix.toLowerCase()))
								return true;
						}
						return false;//name.toLowerCase().endsWith(".csv") || name.toLowerCase().endsWith(".txt");
					}
				});
				
				for(File f : files){
					System.out.println("Found file " + f.getAbsolutePath());
					fileNames.add(f.getAbsolutePath());
				}
			}
		}
	}
	
	public void combineFiles(){
		for(String f : fileNames){
			File src = new File(f);
			String dir = src.getParentFile().getName();
			String name = src.getName();
			String combined = name.substring(0, name.lastIndexOf('.')) + "-" + dir + name.substring(name.lastIndexOf('.'));
			File dest = new File("./" + OUTPUT_DIR + "/" + combined);
			
			try {
				System.out.println("Moving to " + dest.getAbsolutePath());
				copyFile(src, dest);
			} catch (IOException e) {
				System.err.println("Failed to create " + combined);
			}
		}
	}
	
	public static void copyFile(File sourceFile, File destFile) throws IOException {
	     if(!destFile.exists()) {
	      destFile.createNewFile();
	     }

	     FileChannel source = null;
	     FileChannel destination = null;
	     try {
	      source = new RandomAccessFile(sourceFile,"rw").getChannel();
	      destination = new RandomAccessFile(destFile,"rw").getChannel();

	      long position = 0;
	      long count    = source.size();

	      source.transferTo(position, count, destination);
	     }
	     finally {
	      if(source != null) {
	       source.close();
	      }
	      if(destination != null) {
	       destination.close();
	      }
	    }
	 }
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Combine c = new Combine();
		c.scanDirs();
		c.combineFiles();
	}

}
