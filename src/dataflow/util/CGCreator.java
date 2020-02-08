package dataflow.util;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
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
	// Method Name
	public static String methodName = "main";
	public static String mainClass = "sample.functionA.MainA";
	public static String targetClass = "sample.functionA.MainA";

//	public static String methodName = "create";
//	public static String mainClass = "simple.client.Client";
//	public static String targetClass = "simple.logic.Logic_static";
	
//	private static String methodName = "deploy";
//	private static String mainClass = "org.apache.catalina.manager.TestManagerServlet";
//	private static String targetClass = "org.apache.catalina.manager.ManagerServlet";

	public static Logger logger = LogUtil.createLogger(".\\SampleA.log",
			CGCreator.class);
	
//	public static List<List<String>> EdgeListString= new ArrayList<List<String>>();
	public static List<Edge> EdgeListEdge= new ArrayList<Edge>();
	
	/**
	 * Call Graph Creation
	 * 
	 * @param graph
	 * @param fileName
	 */
	public static void SerializeCallGraph(CallGraph graph, String fileName) {
		
		List<String> edge = null;
		List<List<String>> allEdgeStringList = new ArrayList<List<String>>();
		List<Edge> EdgeList = new ArrayList<Edge>();
		
		if (fileName == null) {
			fileName = soot.SourceLocator.v().getOutputDir();
			if (fileName.length() > 0) {
				fileName = fileName + java.io.File.separator;
			}
			fileName = fileName + "call-graph" + DotGraph.DOT_EXTENSION;
		}
		System.out.println("file name " + fileName);
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
					&& !srcString.startsWith("<org.")
					&& !srcString.startsWith("<com.")
					&& !srcString.startsWith("<jdk.") && !srcString
						.startsWith("<javax."))
				&& (!tgtString.startsWith("<java.")
							&& !tgtString.startsWith("<sun.")
							&& !tgtString.startsWith("<org.")
							&& !tgtString.startsWith("<com.")
							&& !tgtString.startsWith("<jdk.") && !tgtString
								.startsWith("<javax."))) {
				
				// Drawing CG excepted designated java packages.
				canvas.drawNode(srcString);
				canvas.drawNode(tgtString);
				canvas.drawEdge(srcString, tgtString);
				
//				//Create Edge List.
//				edge = new ArrayList<String>();
//				//Store as "package.class.method". The last element is method name which is devided by dot.
//				edge.add(0, src.getClass().getName()+"."+src.method().getName()); 
//				edge.add(1, tgt.getClass().getName()+"."+tgt.method().getName());
//				allEdgeStringList.add(index, edge);
				
				//EdgeList
				EdgeList.add(index, next);
			}
		}
		//Write .dot file.
		canvas.plot(fileName);
		//Put edge List into class field to be refered by other methods.
//		EdgeListString = allEdgeStringList;
		EdgeListEdge = EdgeList;
		
for(Edge e : EdgeListEdge)
	System.out.println("ALL Edge : " + e.getSrc() +" -> "+ e.getTgt());
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

		/* Set arguments for Soot main method. */
		String[] args2 = Utility4Soot.setMainArgs(args, mainClass, targetClass);

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
						SerializeCallGraph(cg, ".\\CallGraph\\" + "CallGraph_"
								+ targetClass+ "_" +methodName + DotGraph.DOT_EXTENSION);				
 
						//Get SootClass.
						SootClass sootClass = Scene.v().getSootClass(
								targetClass);
						// Analyze class structure.
						sootClass.setApplicationClass();
						//Get soot method.
						SootMethod method = sootClass
								.getMethodByName(methodName);
						
						//Get first edge.
						//Get Method Name from each edge src node and compare with start method name.
						List<Edge> startEdges = new ArrayList<Edge>();
						Iterator<Edge> iter = EdgeListEdge.iterator();
						while(iter.hasNext()){
							Edge edge = iter.next();
							SootMethod srcMethod = edge.getSrc().method();
							if(srcMethod.equals(method)){
								startEdges.add(edge);
							}
						}
						
for(Edge ed : startEdges){
	System.out.println("startEdges : " + ed.getSrc() +" -> "+ ed.getTgt());
}
						
						//Search CG Path.
						List<List<Edge>> ret = new ArrayList<List<Edge>>();
						ArrayList<Edge> path = new ArrayList<Edge>();
						//List<List<Edge>> ListOfPath = search(startEdges, path, ret);
						List<List<Edge>> ListOfPath = search2(startEdges.get(0), path, ret);
						
						//Write
						FileWriter in = null;
						PrintWriter CGPathWriter = null;
						String filePath = ".\\CallGraphPathList\\"+ targetClass + "_" + methodName + ".txt";
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
						
						//Path List writter
						//boolean pathListWriteResult = PathWritter.writeEdgePath(".¥¥CallGraphPathList¥¥", targetClass, methodName, ListOfPath);
						//logger.log(Level.INFO, String.valueOf(pathListWriteResult));
						
for(List<Edge> p : ListOfPath){
	System.out.println("PathList:: "+p);
}

					}

				}));

		/* Execute Soot main method with your designated argument values */
		soot.Main.main(args2);

	}
	
	private static List<List<Edge>> search2(Edge edge, ArrayList<Edge> path, List<List<Edge>> ret){
			//for(Edge e : edges){  
		   logger.log(Level.INFO, "search2: start method --------------");
		   logger.log(Level.INFO, "    edge: " + edge.toString());
		   logger.log(Level.INFO, "    path: " + path.toString());
		   logger.log(Level.INFO, "    ret: " + ret.toString());
			MethodOrMethodContext tgt = edge.getTgt();
			List<Edge> children = detectChildren(tgt);
logger.log(Level.INFO, "Find  Children:::" + children);			
			if(children.size() != 0 && ! isLoop(path, tgt)){
				for(Edge e : children){
					//ArrayList<Edge> newPath = (ArrayList<Edge>)path.clone();
                    path.add(edge);
					ArrayList<Edge> newPath = new ArrayList<Edge>();
					for(Edge copyEdge : path) {
						newPath.add(copyEdge);
					}
					logger.log(Level.INFO, "search2:     if1 : newPath: " + newPath.toString());
                   search2(e, newPath, ret);
				}
			} else if(children.size() != 0 && isLoop(path, tgt) ){
                path.add(edge);
				ArrayList<Edge> newPath = new ArrayList<Edge>();
				for(Edge copyEdge : path) {
					newPath.add(copyEdge);
				}
				logger.log(Level.INFO, "search2:     if2 : newPath:" + newPath.toString());
				ret.add(newPath);
			} else if(children.size() == 0) {
                path.add(edge);
				ArrayList<Edge> newPath = new ArrayList<Edge>();
				for(Edge copyEdge : path) {
					newPath.add(copyEdge);
				}
				logger.log(Level.INFO, "search2:     if3 : newPath:" + newPath.toString());
				ret.add(newPath);
			}
		
		//List<List<Edge>> rt = new ArrayList<List<Edge>>(new HashSet<>(ret));
			logger.log(Level.INFO, "search2:   return: " + ret.toString());
		return ret;
	}
	
	/**
	 * This method have a function which searches C1 level coverage path information from the designated start method.
	 * If this method finds any loop edges in this path search disposal, force to stop the path search.
	 * @param edges
	 * @param path
	 * @param ret
	 * @return path list
	 */
	private static List<List<Edge>> search( List<Edge> edges, ArrayList<Edge> path, List<List<Edge>> ret){
		Iterator<Edge> iter = edges.iterator();
		while(iter.hasNext()) {
			Edge e = iter.next();
			//for(Edge e : edges){
			path.add(e);
			logger.log(Level.INFO, String.valueOf(path.size()));
			MethodOrMethodContext tgt = e.getTgt();
			List<Edge> children = detectChildren(tgt, e);
			logger.log(Level.INFO, "Find  Children:::" + children);
			if(children.size() != 0 && !isLoop(path, tgt)){
				logger.log(Level.INFO,"Recursive!");
				//List<Edge> newPath = new ArrayList<Edge>();
				//newPath.addAll(path);
				search(children, (ArrayList<Edge>)path.clone(), ret);
			} else if(children.size() != 0 && isLoop(path, tgt) ){
				ret.add((List<Edge>)path.clone());
				logger.log(Level.INFO,"FindLoop!!" + e);
			} else if(children.size() == 0) {
				logger.log(Level.INFO,"No Children!");
				//path.add(e);
				ret.add((List<Edge>)path.clone());
				path = new ArrayList<Edge>();
			}
		}
		
		List<List<Edge>> rt = new ArrayList<List<Edge>>(new HashSet<>(ret));
		return rt;
	}
	
		
	private static List<Edge> detectChildren(MethodOrMethodContext tgt, Edge e){
		
		List<Edge> children = new ArrayList<Edge>();
		
		for(Edge edge : EdgeListEdge){
			if(tgt.method().equals( edge.getSrc().method())){
//				if(isRedundant(edge, e))
//					continue;
				children.add(edge);
			}
		}
		
		return children;
	}
	
	private static List<Edge> detectChildren(MethodOrMethodContext tgt){
		logger.log(Level.INFO, "detectChildren: start method -----------------------");
		logger.log(Level.INFO, "    tgt: " + tgt.toString());
		List<Edge> children = new ArrayList<Edge>();
		
		for(Edge edge : EdgeListEdge){
logger.log(Level.INFO, "tgt::"+tgt.method().getName());
logger.log(Level.INFO, "src::"+edge.getSrc().method().getName());
			if(tgt.method().getName().equals( edge.getSrc().method().getName())){
//				if(isRedundant(edge, e))
//					continue;
				children.add(edge);
logger.log(Level.INFO, "detectChildren: children::"+children.toString());
			}
		}
		
		return children;
	}
	
	
	private static boolean isLoop(List<Edge> path, MethodOrMethodContext tgt){
		logger.log(Level.INFO, "isLoop start method --------------------------");
		logger.log(Level.INFO, "    tgt: " + tgt.toString());
		boolean ret = false;
		
		for(Edge e : path){
			if(e.getSrc().method().equals(tgt.method()) || e.getTgt().method().equals(tgt.method())){
				ret = true;
			}
		}
		logger.log(Level.INFO, "    ret flag: " + ret);
		return ret;
	}
	
	private static boolean isRedundant(Edge currentEdge, Edge e){
		boolean ret = false;
		
		if(e != null){
			if(e.equals(currentEdge))
				ret = true;
		}
		
		return ret;
	}

}
