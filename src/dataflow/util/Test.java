package dataflow.util;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

public class Test {

	public static void main(String[] args) {
		writter();
		
	}
	
	private static boolean writter(){
		
		//Write
		FileWriter in = null;
		PrintWriter CGPathWriter = null;
		String filePath = ".\\CallGraphPathList\\"+"testCalsss" + "TestMethod" + ".txt";
		try {
			//postscript version
			in = new FileWriter(filePath, true);
			CGPathWriter = new PrintWriter(in);
			CGPathWriter.println("test");
			CGPathWriter.flush();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				//close
				in.close();
				CGPathWriter.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}	
		
		return true;
		
	}	

}