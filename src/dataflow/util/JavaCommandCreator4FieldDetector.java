package dataflow.util;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class JavaCommandCreator4FieldDetector {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
	
	public boolean readWrite() {
		
		boolean ret = false;
		
		List<String> javaCmdLlist = new ArrayList<String>();
		
		//Read CFPath files
//		//Read Static Variable List from file.
		Path file = Paths.get(".\\CallGraphPathList\\org.apache.catalina.manager.ManagerServlet_list_Simple.txt");
		List<String> callGraphPathList =null;
		try {
			callGraphPathList = Files.readAllLines(file, Charset.forName("MS932"));		
		} catch (IOException e1) { 
			e1.printStackTrace();
		}
		
		for(String path : callGraphPathList){
			String[] tmpArray = path.split("==>");
			List<String> cfPathList = Arrays.asList(tmpArray);
			for(String node : cfPathList){
				List<String> tmpNodeList = Arrays.asList(node.split(" "));
				//Create Java Command
				String classFQCN = tmpNodeList.get(1);
				String mName = tmpNodeList.get(3);
				
				String classPath = classFQCN.replace(".", "\\");
				
				String[] classEle = classPath.split(".");
				String cName = classEle[classEle.length-1];
				
				//java classPath(/dfa/adfad/fdf.class) ClassName TestClassName MethodName -option 
				String javaCmd = "java " + ".\\" + classPath +" "+ cName +" "+ "Test"+cName + " " + mName + " " + "-whole-program -xml-attributes -keep-line-number -f jimple -p cg.cha enabled:true -p cg verbose:true,all-reachable:true,,safe-forname:true,safe-newinstance:true -Xss1500m -Xmx4000M" ;
				callGraphPathList.add(javaCmd);
			}
			
		}
		
		return ret;
	}

}
