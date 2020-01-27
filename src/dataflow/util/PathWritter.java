package dataflow.util;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import soot.MethodOrMethodContext;
import soot.jimple.toolkits.callgraph.ReachableMethods;

public class PathWritter {
	
	public static boolean writePath(String path, String targetClass, String methodName, List<String> pathList ) {
		boolean ret = false;
		
		//Write the list of path.
		FileWriter in = null;
		PrintWriter out = null;
		String filePath = path + targetClass + methodName +".txt";
		try {
			//postscript versionu
			in = new FileWriter(filePath, true);
			out = new PrintWriter(in);
			for(String p : pathList)
				out.println(p.toString());
			out.flush();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				//close
				in.close();
				out.close();
				//Change flag from false to true.
				ret = true;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		//return value.
		return ret;
		
	}
	
	
	public static boolean writePath(String path, String targetClass, String methodName,  ReachableMethods reachMethods) {
		boolean ret = false;
		
		//Write the list of path.
		FileWriter in = null;
		PrintWriter out = null;
		String filePath = path + targetClass + methodName +".txt";
		try {
			//postscript version
			in = new FileWriter(filePath, true);
			out = new PrintWriter(in);
			while(reachMethods.listener().hasNext()){
				MethodOrMethodContext mc = reachMethods.listener().next();
				out.println(mc.toString());
			}
			out.flush();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				//close
				in.close();
				out.close();
				//Change flag from false to true.
				ret = true;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		//return value.
		return ret;
		
	}

}
