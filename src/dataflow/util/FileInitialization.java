package dataflow.util;

import java.io.File;

public class FileInitialization {
	
	/**
	 * Utility to delete result files under output directories before invoke soot main method.
	 * 
	 * Example of invocation of this method.
	 * <p>
	 *  FileClass fc = new FileClass();
     *  File dir = new File("/Users/Shared/java/");
     *  FileClass.fileClass(dir);
	 * </p>
	 * @param dir
	 */
	static public void deleteFile(File dir){
        //Delete files under your designated directory.
        if(dir.exists()) {
            
            if(dir.isFile()) {
                if(dir.delete()) {
                    System.out.println("Delete File 1");
                }
            } else if(dir.isDirectory()) {
                File[] files = dir.listFiles();
                
                if(files == null) {
                    System.out.println("Not existing any files under the directory.");
                }
                //Loop for the number of the existing files.
                for(int i=0; i<files.length; i++) {
                    
                    //Confirm existing any files or not.
                    if(files[i].exists() == false) {
                        continue;
                    //Recursive deletion.
                    } else if(files[i].isFile()) {
                        deleteFile(files[i]);
                        System.out.println("Delete File 2");
                    }        
                }
            }
        } else {
            System.out.println("Not Existing Any File.");
        }
    }


}
