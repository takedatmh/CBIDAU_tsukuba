package dataflow.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import dataflow.util.Context;

/**
 * Retrieve impact path from the target method to leaf method.
 * @author s1930149
 */
public class Writter {
	
	public static void main(String[] args) {

	//Write the list of path.
	FileWriter in = null;
	PrintWriter out = null;
	String filePath = null;
	
	filePath = Context.TMP_Folder+ Context.SEPARATOR +"writter.txt";
	
	
	try {
		//postscript version
		in = new FileWriter(filePath, true);
		out = new PrintWriter(in);
		out.println("testtesttesttest");
		out.flush();
	} catch (Exception e) {
		e.printStackTrace();
	} finally {
		try {
			//close
			in.close();
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	}
	
}