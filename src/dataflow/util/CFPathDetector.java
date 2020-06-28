package dataflow.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import soot.Body;
import soot.MethodOrMethodContext;
import soot.PackManager;
import soot.Scene;
import soot.SceneTransformer;
import soot.SootClass;
import soot.SootMethod;
import soot.Transform;
import soot.Unit;
import soot.jimple.toolkits.callgraph.CHATransformer;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.UnitGraph;
import soot.util.dot.DotGraph;
import soot.util.queue.QueueReader;

/**
 * Retrieve impact path from the target method to leaf method.
 * @author s1930149
 */
public class CFPathDetector {
	
	//Target Class, Method, TestCalss name.
	private static String methodName = null;
	private static String mainClass = null;
	private static String targetClass = null;

	//Logger Declaration.
	private static Logger logger = LogUtil.createLogger(".\\Sample.log", CFPathDetector.class);

	/**
	 * Main Method.
	 * 
	 * @param followsing java command args are puted into String[];
	 * -whole-program -xml-attributes -keep-line-number -f jimple -p cg.cha enabled:true -app simple.logic.Logic -p cg verbose:true,all-reachable:true,,safe-forname:true,safe-newinstance:true
	 */
	public static void main(String[] args) {
		
		//Obtain soot main arguments from system properties.
		methodName = System.getProperty("method");
		mainClass = System.getProperty("main");
		targetClass = System.getProperty("target");
		
		/* Set arguments for Soot main method. */
		String[] args2 = Utility4Soot.setMainArgs(args, mainClass, targetClass);
		
//		/*
//		 * Delete previous result files under the output directories as follows:
//		 */
//	     File CFG_PathList_Dir = new File("CFG_PathList" + Context.SEPARATOR);
//	     deleteFile(CFG_PathList_Dir);
//	     File ControlFlowGraphPDF_Dir = new File("ControlFlowGraphPDF" + Context.SEPARATOR);
//	     deleteFile(ControlFlowGraphPDF_Dir);

		/**
		 * Soot PackManager
		 * 
		 * WJTP(whole jimple transformation pack) phase's implementation. Whole
		 * of Analysis for the jimple transfer from java to jimple.
		 */
		PackManager.v().getPack("wjtp")
				.add(new Transform("wjtp.myTrans", new SceneTransformer() {
					protected void internalTransform(String phaseName,
							@SuppressWarnings("rawtypes") Map options) {
						
						/////////////Control Flow Graph//////////////////////
						CHATransformer.v().transform();

						//Get a Soot Class of target class. : "org.apache.struts.action.ActionServlet");
						SootClass sootClass = Scene.v().getSootClass(targetClass);
						// Analyze class structure.
						sootClass.setApplicationClass();
						// method.
						SootMethod method = sootClass.getMethodByName(methodName);
						// Body has same jimple code to SootMethod's activeBody.
						Body body = method.retrieveActiveBody();
						// Create Control flow Graph as UnitGraph
						UnitGraph unitGraph = new ExceptionalUnitGraph(body);  // UnitGraph unitGraph = new EnhancedUnitGraph(body);
						////Retrieve each test Path in the CFG.////
						//Get Head and Tail node from whole of CFG.
						Unit head = unitGraph.getHeads().get(0);
						//Prepare
						//PathList Objects.
						ArrayList<Unit> path = new ArrayList<>();
						List<List<Unit>> pathList = new ArrayList<List<Unit>>();
						//Get children of Top node.
						Unit top= unitGraph.getSuccsOf(head).get(0);
						path.add(top);
						
						//Invoke Path Search method and then Return List of test coverage path
						List<List<Unit>> listOfPath = searchPathFromCFG(top, unitGraph, path, pathList);
////Debug						
//logger.log(Level.INFO, "Start writing!");
						
								//Write the list of path.
								FileWriter in = null;
								PrintWriter out = null;
								/*20200506 Amend file path from ¥¥ to /. For Linux  */
								//String filePath = ".\\CFG_PathList\\" + targetClass + "_"+methodName +".txt";
								String filePath = "CFG_PathList/" + targetClass + "_"+methodName +".txt";
								try {
									//postscript version
									in = new FileWriter(filePath, true);
									out = new PrintWriter(in);
									out.println(pathList.toString());
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
						logger.log(Level.INFO, "Finish writting!");

					}
				}));
		
		/* Execute Soot main method with your designated argument values */
		soot.Main.main(args2);
	}
	
	/**
	 * Search coverage path of CFG from head to tail
	 * @param head
	 * @param tail
	 * @param unitGraph
	 * @return List : List of coverage path
	 */
	public static List<List<Unit>> searchPathFromCFG(Unit node, UnitGraph unitGraph, ArrayList<Unit> path, List<List<Unit>> pathList){
		
		path.add(node);
		
		List<Unit> children = unitGraph.getSuccsOf(node);
		int size = children.size();
		
		if(size != 0){
			for(Unit nextNode : children) {
				//path.add(nextNode);
				if(!isLoop(nextNode, path)) {
					//Create new Unit List object for recursive algorithm.
					ArrayList<Unit> newPath = new ArrayList<Unit>();					
					for(Unit u : path)
						newPath.add(u);
					//Recursive Call for searchPathFromCFG method.
					//searchPathFromCFG(nextNode, unitGraph, (ArrayList<Unit>)path.clone(), pathList);
					searchPathFromCFG(nextNode, unitGraph, newPath, pathList);
				} else {
					continue;
				}
			}
		//Modified by takeda in 20200511
		//} else if (size == 0) {
		} else {			
			ArrayList<Unit> newPath = new ArrayList<Unit>();
			for(Unit u : path)
				newPath.add(u);
			pathList.add(newPath);
			//log : Display selected path.
			logger.log(Level.INFO, "##"+newPath);
		} 
		
		return pathList;
	}
	
	private static boolean isLoop(Unit nextNode, List<Unit> path){
		boolean ret = false;
		
		for(Unit node : path){
			if(node.equals(nextNode))
				ret = true;
		}
		
		return ret;
	}

	/**
	 * Select paths which use static variable.
	 * 
	 * @param varList
	 * @param listOfPath
	 * @return List<String> selected path.
	 */
	public static List<String> selectPathWithStaticVar(List<String> varList, List<List<Unit>> listOfPath){
		//return
		List<String> ret = new ArrayList<String>();
		
		//delete unnecessary chars from static var list.
		Map<String, List<String>> extractrdVarMap = new HashMap<String, List<String>>();
		for(int i = 0; i < varList.size(); i++) {
			String var = varList.get(i);
			//get rid off [< and >] from variable char.
			String removedVar = var.replace("[<", "").replace(">]", "").replace(":", "");
			List<String> tempList = Arrays.asList(removedVar.split(" "));
			String fqcn = tempList.get(0) + "." +tempList.get(2);
			extractrdVarMap.put(fqcn, tempList);
			
////log
//logger.log(Level.INFO, "FQCN: " + fqcn);

		}
		
		for(List<Unit> units : listOfPath){
			//node
			for(Unit unit : units){
				String node = unit.toString();
				Set<String> keys = extractrdVarMap.keySet();
				for(String key :keys )
				if(node.contains(extractrdVarMap.get(key).get(2))) {
					ret.add(units.toString());
				}
			}
		}
		
		for(String s : ret)
			logger.log(Level.INFO, "##2##"+s);
		return ret;
	}
	
	/**
	 * Call Graph Creation from each CG path node.
	 * Before invoking this method, designate one node(method) CG from an impact path.
	 * 
	 * @param graph : path node's CFG object.
	 * @param fileName : Output file name.
	 */
	public static void SerializeCallGraph(CallGraph graph, String fileName) {
		if (fileName == null) {
			fileName = soot.SourceLocator.v().getOutputDir();
			if (fileName.length() > 0) {
				fileName = fileName + java.io.File.separator;
			}
			fileName = fileName + "call-graph" + DotGraph.DOT_EXTENSION;
		}
		DotGraph canvas = new DotGraph("Call_Graph_"+ methodName);
		QueueReader<Edge> listener = graph.listener();

		while (listener.hasNext()) {
			Edge next = listener.next();
			MethodOrMethodContext src = next.getSrc();
			MethodOrMethodContext tgt = next.getTgt();
			String srcString = src.toString();
			String tgtString = tgt.toString();

			// Excepted java packages.
			if ((!srcString.startsWith("<java.")
					&& !srcString.startsWith("<sun.")
					//&& !srcString.startsWith("<org.")
					&& !srcString.startsWith("<com.")
					&& !srcString.startsWith("<jdk.") 
					&& !srcString.startsWith("<javax."))
					&& (!tgtString.startsWith("<java.")
							&& !tgtString.startsWith("<sun.")
							//&& !tgtString.startsWith("<org.")
							&& !tgtString.startsWith("<com.")
							&& !tgtString.startsWith("<jdk.") && !tgtString
								.startsWith("<javax."))) {
				
				// Drawing CG excepted designated java packages.
				canvas.drawNode(src.toString());
				canvas.drawNode(tgt.toString());
				canvas.drawEdge(src.toString(), tgt.toString());
			}

		}
		canvas.plot(fileName);
		return;
	}
	
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
	static private void deleteFile(File dir){
        //Delete files under your designated directory.
        if(dir.exists()) {
            
            if(dir.isFile()) {
                if(dir.delete()) {
                    System.out.println("Delete File.");
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
                        System.out.println("ファイル削除2");
                    }        
                }
            }
        } else {
            System.out.println("ディレクトリが存在しない");
        }
    }

}