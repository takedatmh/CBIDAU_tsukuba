package dataflow.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
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

	// Method Name
//	public static String methodName = "create";
//	public static String mainClass = "simple.client.Client";
//	public static String targetClass = "simple.logic.Logic_static";
	
	private static String methodName = "deploy";
	private static String mainClass = "org.apache.catalina.manager.TestManagerServlet";
	private static String targetClass = "org.apache.catalina.manager.ManagerServlet";

	public static Logger logger = LogUtil.createLogger(".\\Sample.log",
			CGCreator.class);
	
	public static List<List<String>> EdgeListString= new ArrayList<List<String>>();
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
				|| (!tgtString.startsWith("<java.")
							&& !tgtString.startsWith("<sun.")
							&& !tgtString.startsWith("<org.")
							&& !tgtString.startsWith("<com.")
							&& !tgtString.startsWith("<jdk.") && !tgtString
								.startsWith("<javax."))) {
				
				// Drawing CG excepted designated java packages.
				canvas.drawNode(srcString);
				canvas.drawNode(tgtString);
				canvas.drawEdge(srcString, tgtString);
				
				//Create Edge List.
				edge = new ArrayList<String>();
				//Store as "package.class.method". The last element is method name which is devided by dot.
				edge.add(0, src.getClass().getName()+"."+src.method().getName()); 
				edge.add(1, tgt.getClass().getName()+"."+tgt.method().getName());
				allEdgeStringList.add(index, edge);
				
				//EdgeList
				EdgeList.add(index, next);
			}
		}
		//Write .dot file.
		canvas.plot(fileName);
		//Put edge List into class field to be refered by other methods.
		EdgeListString = allEdgeStringList;
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
 
						// ///////// Get Unit
						// ///////////////////////////////////////////////
						// Get a Soot Class of target class. :
						// "org.apache.struts.action.ActionServlet");
						SootClass sootClass = Scene.v().getSootClass(
								targetClass);
						// Analyze class structure.
						sootClass.setApplicationClass();
						// method.
						SootMethod method = sootClass
								.getMethodByName(methodName);
						
						//Get start edge.
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
//for(Edge e : startEdges){
//	System.out.print("srcEdge: " + e.getSrc().method() + "  ->  ");
//	System.out.println("tgtEdge: " + e.getTgt().method());
//}
						//Get path list.
						List<List<Edge>> ret = new ArrayList<List<Edge>>();
						List<Edge> path = new ArrayList<Edge>();
for(Edge ed : startEdges){
	System.out.println("stargEdges : " + ed);
}
						List<List<Edge>> ListOfPath = search(startEdges, path, ret);
for(List<Edge> p : ListOfPath)
	System.out.println("ListOfPath : " + p );
					}

				}));

		/* Execute Soot main method with your designated argument values */
		soot.Main.main(args2);

	}
	
	private static List<List<Edge>> search( List<Edge> edges, List<Edge> path, List<List<Edge>> ret){
		
		for(Edge e : edges){
			path.add(e);
			MethodOrMethodContext tgt = e.getTgt();
			List<Edge> children = detectChildren(tgt);
System.out.println("Children:::" + children);
			if(children.size() != 0 && !isLoop(path, tgt)){
				search(children, path, ret);
			} else if(children.size() != 0 && isLoop(path, tgt) ){
				ret.add(path);
				path = new ArrayList<Edge>();
			} else if(children.size() == 0) {
				path.add(e);
				ret.add(path);
				path = new ArrayList<Edge>();
			}
		}
		
		return ret;
	}
		
	private static List<Edge> detectChildren(MethodOrMethodContext tgt){
		
		List<Edge> children = new ArrayList<Edge>();
		
		for(Edge edge : EdgeListEdge){
			if(tgt.equals( edge.getSrc())){
				if(isRedundant(tgt, edge.getTgt()))
					continue;
				children.add(edge);
			}
		}
		
		return children;
	}
	
	private static boolean isLoop(List<Edge> path, MethodOrMethodContext tgt){
		boolean ret = false;
		
		for(Edge e : path){
			if(e.getSrc().method().equals(tgt.method()) || e.getTgt().method().equals(tgt.method())){
				ret = true;
			}
		}
		
		return ret;
	}
	
	private static boolean isRedundant(MethodOrMethodContext in, MethodOrMethodContext tgt){
		boolean ret = false;
		
		if(in != null){
			if(in.method().equals(tgt.method()))
				ret = true;
		}
		
		return ret;
	}

	/**
	 * This method have a function which searches C1 level coverage path information from the designated start method.
	 * If this method finds any loop edges in this path search disposal, force to stop the path search.
	 * @param startMethod : Start point method
	 * @param cg : Call Graph Object
	 * @param edgeList : having each path information
	 * @param ret : return object
	 * @return List<List<Edge>> Return a list contains each path list.
	 */
	private static List<List<Edge>> searchPath(SootMethod startMethod, CallGraph cg, List<Edge> edgeList, List<List<Edge>> ret) {
		
//		QueueReader<Edge> listener = cg.listener();
//
//		while (listener.hasNext()) {
//			Edge next = listener.next();
//			MethodOrMethodContext src = next.getSrc();
//			MethodOrMethodContext tgt = next.getTgt();
//			String srcString = src.toString();
//			String tgtString = tgt.toString();
//		}
		
		///////////////////////////////
		
		//Obtain out edges from the designated method node.
		Iterator<Edge> edges = cg.edgesOutOf(startMethod);
System.out.println("///:" + edges);
		//In case of existing children, continue to search path for each child node.
		top: if(edges.hasNext()){
			
			//Get an out of edge from target method.
			Edge edge  = edges.next();
			
			//Add an edge into list.
			edgeList.add(edge);
			
			//Get next "method".
			SootMethod targetMethod = edge.tgt();
			
			//Loop Check
			if(checkLoop(targetMethod, edgeList)) {
				//When a loop is found, add the current edgeList into return and return return value.
				ret.add(edgeList);
				
				//exit this iteration and goto next iteration loop.
				break top;
				
				//return.
				//return ret;
			}
			
			//recursive calling
			searchPath(targetMethod, cg, edgeList, ret);
		
		//In case of not existing children, finishing path search and storing this path into return object.
		} else {
			
			//Add this path information into return object.
			ret.add(edgeList);
			
		}
		
		//return.
		return ret;
	}
	
	private static boolean checkLoop(SootMethod targetMethod, List<Edge> edgeList){
		//return value
		boolean ret = false;
		
		//check whether this target method is loop node or not. 
		//Obtain an edge from edgeList.
		for(Edge edge : edgeList){
			//Compare targetMethod with this edge's source method or target method.  
			if( edge.equals(edge.src()) || edge.equals(edge.tgt()) ){
				return ret = true;
			}
		}
		
		//return result.
		return ret;
	}
}
