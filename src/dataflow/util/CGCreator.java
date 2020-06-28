package dataflow.util;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import soot.MethodOrMethodContext;
import soot.PackManager;
import soot.Scene;
import soot.SceneTransformer;
import soot.SootClass;
import soot.SootMethod;
import soot.Transform;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;
import soot.util.dot.DotGraph;
import soot.util.queue.QueueReader;

/**
 * In case of existing statements more than one branch and one external call
 * method.
 * 
 * @author takedatmh
 * 
 */
public class CGCreator {
	static int a = 0;
	static int counter = 0;
	// Method Name
	private static String methodName = null;
	private static String mainClass = null;
	private static String targetClass = null;

	public static Logger logger = LogUtil.createLogger(".\\SampleA.log",
			CGCreator.class);
	
	public static List<Edge> EdgeListEdge= new ArrayList<Edge>();
	
	/**
	 * Call Graph Creation
	 * 
	 * @param graph
	 * @param fileName
	 */
	public static void SerializeCallGraph(CallGraph graph, String fileName) {
		
		List<Edge> EdgeList = new ArrayList<Edge>();
		
		if (fileName == null) {
			fileName = soot.SourceLocator.v().getOutputDir();
			if (fileName.length() > 0) {
				fileName = fileName + java.io.File.separator;
			}
			fileName = fileName + "call-graph" + DotGraph.DOT_EXTENSION;
		}
		//System.out.println("file name " + fileName);
		DotGraph canvas = new DotGraph("Call_Graph");
		QueueReader<Edge> listener = graph.listener();

		int index = 0;
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
					&& !srcString.startsWith("<simple.") 
					&& !srcString.startsWith("<javax."))
				&& (!tgtString.startsWith("<java.")
							&& !tgtString.startsWith("<sun.")
							//&& !tgtString.startsWith("<org.")
							&& !tgtString.startsWith("<com.")
							&& !tgtString.startsWith("<jdk.") 
							&& !srcString.startsWith("<simple.") 
							&& !tgtString.startsWith("<javax."))) {
				
				// Drawing CG excepted designated java packages.
				canvas.drawNode(srcString);
				canvas.drawNode(tgtString);
				canvas.drawEdge(srcString, tgtString);
				
				//EdgeList
				EdgeList.add(index, next);
			}
		}
		//Write .dot file.
		canvas.plot(fileName);
		//Put edge List into class field to be refered by other methods.
		EdgeListEdge = EdgeList;

////Debug
//for(Edge e : EdgeListEdge)
//	System.out.println("ALL Edge : " + e.getSrc().method().getName() +" -> "+ e.getTgt().method().getName());
		
		return;
	}

	/**
	 * Main Method.
	 * 
	 * @param followsing
	 *            java command args are puted into String[]; -whole-program
	 *            -xml-attributes -keep-line-number -f jimple -p cg.cha
	 *            enabled:true -app simple.logic.Logic -p cg
	 *            verbose:true,all-reachable
	 *            :true,,safe-forname:true,safe-newinstance:true
	 */
	public static void main(String[] args) {
		
		methodName = System.getProperty("method");
		mainClass = System.getProperty("main");
		targetClass = System.getProperty("target");

		/* Set arguments for Soot main method. */
		String[] args2 = Utility4Soot.setMainArgs(args, mainClass, targetClass);

//		/*
//		 * Delete previous result files under the output directories as follows:
//		 */
//	     File CallGraph_Dir = new File("CallGraph" + Context.SEPARATOR);
//	     deleteFile(CallGraph_Dir);
//	     File CallGraphPathList_Dir = new File("CallGraphPathList" + Context.SEPARATOR);
//	     deleteFile(CallGraphPathList_Dir);
//	     File CFG_PathList_Dir = new File("CFG_PathList" + Context.SEPARATOR);
//	     deleteFile(CFG_PathList_Dir);
		
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
						// ///////////Create Call Flow
						// Graph///////////////////////////////
						CallGraph cg = Scene.v().getCallGraph();
						SerializeCallGraph(cg, "CallGraph" + Context.SEPARATOR + "CallGraph_"
								+ targetClass+ "_" +methodName + DotGraph.DOT_EXTENSION);				
						
						//Get SootClass.
						SootClass sootClass = Scene.v().getSootClass(
								targetClass);
						// Analyze class structure.
						sootClass.setApplicationClass();
						//Get soot method.
						SootMethod method = sootClass.getMethodByName(methodName);
						
						//Get first edge. Get Method Name from each edge src node and compare with start method name.
						List<Edge> startEdges = new ArrayList<Edge>();
						Iterator<Edge> iter = EdgeListEdge.iterator();
						while(iter.hasNext()){
							Edge edge = iter.next();
							SootMethod srcMethod = edge.getSrc().method();
							if(srcMethod.equals(method)){
								startEdges.add(edge);
							}
						}

////Debug
//for(Edge ed : startEdges){
//	System.out.println("startEdges : " + ed.getSrc() +" -> "+ ed.getTgt());
//}
						
						//Search CG Path.
						List<List<Edge>> ret = new ArrayList<List<Edge>>();
						ArrayList<Edge> path = new ArrayList<Edge>();
						//List<List<Edge>> ListOfPath = search(startEdges, path, ret);
						List<List<Edge>> ListOfPath = search2(startEdges.get(0), path, ret);
						
						//Write Detail Edges List
						FileWriter in = null;
						PrintWriter CGPathWriter = null;
						String filePath = "CallGraphPathList"+ Context.SEPARATOR +targetClass + "_" + methodName + ".txt";
						try {
							//postscript version
							in = new FileWriter(filePath, true);
							CGPathWriter = new PrintWriter(in);
							for(List<Edge> p : ListOfPath)
								CGPathWriter.println(p.toString());
							CGPathWriter.flush();
						} catch (Exception e) {
							e.printStackTrace();
						} finally {
							try {
								//close
								in.close();
								CGPathWriter.close();
								logger.log(Level.INFO, "Written!");
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
						
						//Write Simple Edges List
						String filePathSimple = "CallGraphPathList"+ Context.SEPARATOR + targetClass + "_" + methodName + "_Simple" +".txt";
						//String filePathSimple = CallGraphPathListPath;
						try {
							//postscript version
							in = new FileWriter(filePathSimple, true);
							CGPathWriter = new PrintWriter(in);
							for(List<Edge> p : ListOfPath){
								for(Edge e : p){
									CGPathWriter.print(
											e.getSrc().getClass().getPackage().toString().replace("pakage ", "") + "." 
												+ e.getSrc().getClass().getClass() + " " 
													+ e.getSrc().method().getName() + 
											" ==> " + 
											e.getTgt().getClass().getPackage().toString().replace("pakage ", "") + "." 
												+ e.getTgt().getClass().getClass() + " " 
													+ e.getTgt().method().getName() + " , ");
								}
								CGPathWriter.println();
							}
							CGPathWriter.flush();
						} catch (Exception e) {
							e.printStackTrace();
						} finally {
							try {
								//close
								in.close();
								CGPathWriter.close();
////Debug								
//logger.log(Level.INFO, "Written! Simple version.");
							} catch (IOException e) {
								e.printStackTrace();
							}
						}	
////Debug												
//for(List<Edge> p : ListOfPath){
//	System.out.println("PathList:: "+p);
//}

					}

				}));

		/* Execute Soot main method with your designated argument values */
		soot.Main.main(args2);

	}
	
	private static List<List<Edge>> search2(Edge edge, ArrayList<Edge> path, List<List<Edge>> ret){

			MethodOrMethodContext tgt = edge.getTgt();
			List<Edge> children = detectChildren(tgt);
		
			if(children.size() != 0 && ! isLoop(path, tgt)){
				for(Edge e : children){
					//breaker
					if(counter++ > 30) break;
					//ArrayList<Edge> newPath = (ArrayList<Edge>)path.clone();
                    path.add(edge);
					ArrayList<Edge> newPath = new ArrayList<Edge>();
					for(Edge copyEdge : path) {
						newPath.add(copyEdge);
					}
////Debug					
//logger.log(Level.INFO, "search2:     if1 : newPath: " + newPath.toString());
                   search2(e, newPath, ret);
				}
			} else if(children.size() != 0 && isLoop(path, tgt) ){
                path.add(edge);
				ArrayList<Edge> newPath = new ArrayList<Edge>();
				for(Edge copyEdge : path) {
					newPath.add(copyEdge);
				}
////Debug
//logger.log(Level.INFO, "search2:     if2 : newPath:" + newPath.toString());
				ret.add(newPath);
			} else if(children.size() == 0) {
                path.add(edge);
				ArrayList<Edge> newPath = new ArrayList<Edge>();
				for(Edge copyEdge : path) {
					newPath.add(copyEdge);
				}
////Debug				
//logger.log(Level.INFO, "search2:     if3 : newPath:" + newPath.toString());
				ret.add(newPath);
			}
		
//Debug
////logger.log(Level.INFO, "search2:   return: " + ret.toString());
			
		return ret;
	}
	
	private static List<Edge> detectChildren(MethodOrMethodContext tgt){
		List<Edge> children = new ArrayList<Edge>();
		
		for(Edge edge : EdgeListEdge){
			if(tgt.method().getName().equals( edge.getSrc().method().getName())){
				children.add(edge);
//Debug
////logger.log(Level.INFO, "detectChildren: children::"+children.toString());
			}
		}
		
		return children;
	}
	
	
	private static boolean isLoop(List<Edge> path, MethodOrMethodContext tgt){

		boolean ret = false;
		
		for(Edge e : path){
			if(e.getSrc().method().getName().equals(tgt.method().getName()) 
					|| e.getTgt().method().getName().equals(tgt.method().getName())){
				ret = true;
			}
		}
		logger.log(Level.INFO, "    ret flag: " + ret);
		return ret;
	}
	
//	/**
//	 * Utility to delete result files under output directories before invoke soot main method.
//	 * 
//	 * Example of invocation of this method.
//	 * <p>
//	 *  FileClass fc = new FileClass();
//     *  File dir = new File("/Users/Shared/java/");
//     *  FileClass.fileClass(dir);
//	 * </p>
//	 * @param dir
//	 */
//	static private void deleteFile(File dir){
//        //Delete files under your designated directory.
//        if(dir.exists()) {
//            
//            if(dir.isFile()) {
//                if(dir.delete()) {
//                    System.out.println("Delete File.");
//                }
//            } else if(dir.isDirectory()) {
//                File[] files = dir.listFiles();
//                
//                if(files == null) {
//                    System.out.println("Not existing any files under the directory.");
//                }
//                //Loop for the number of the existing files.
//                for(int i=0; i<files.length; i++) {
//                    
//                    //Confirm existing any files or not.
//                    if(files[i].exists() == false) {
//                        continue;
//                    //Recursive deletion.
//                    } else if(files[i].isFile()) {
//                        deleteFile(files[i]);
//                        System.out.println("ファイル削除2");
//                    }        
//                }
//            }
//        } else {
//            System.out.println("ディレクトリが存在しない");
//        }
//    }
	
}
