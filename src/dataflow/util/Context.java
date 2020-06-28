package dataflow.util;

import java.io.File;

/**
 * File path, file separator and OS name Context.
 * When you write or read any data from your file system, you should use these string to dispatch file path description to each OS environment.
 * 
 * @author takedatmh
 *
 */
public class Context {
	
	public static String TMP_Folder = "tmp";
	
	public static String SEPARATOR = File.separator;
	
	public static String OS_NAME = System.getProperty("os.name");

}
