package dataflow.util;

import java.util.ArrayList;
import java.util.Iterator;
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
	public static String methodName = "create";
	public static String mainClass = "simple.client.Client";
	public static String targetClass = "simple.logic.Logic_static";

	public static Logger logger = LogUtil.createLogger(".\\Sample.log",
			CGCreator.class);

	/**
	 * Call Graph Creation
	 * 
	 * @param graph
	 * @param fileName
	 */
	public static void SerializeCallGraph(CallGraph graph, String fileName) {
		if (fileName == null) {
			fileName = soot.SourceLocator.v().getOutputDir();
			if (fileName.length() > 0) {
				fileName = fileName + java.io.File.separator;
			}
			fileName = fileName + "call-graph" + DotGraph.DOT_EXTENSION;
		}
		System.out.println("file name " + fileName);
		DotGraph canvas = new DotGraph("Call_Graph_" + methodName);
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
				canvas.drawNode(src.toString());
				canvas.drawNode(tgt.toString());
				canvas.drawEdge(src.toString(), tgt.toString());
			}

		}
		canvas.plot(fileName);
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
								+ methodName + DotGraph.DOT_EXTENSION);
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

						// Detect influenced paths and nodes.
						List<List<Edge>> pathList = new ArrayList<List<Edge>>();
						List<Edge> edgeList = new ArrayList<Edge>();
						pathList = searchPath(method, cg, edgeList, pathList);

					}

				}));

		/* Execute Soot main method with your designated argument values */
		soot.Main.main(args2);

	}

	private static List<List<Edge>> searchPath(SootMethod startMethod, CallGraph cg, List<Edge> edgeList, List<List<Edge>> ret) {
		
		Iterator<Edge> edges = cg.edgesOutOf(startMethod);
		
		
		if(edges.hasNext()){
			
			//Get an out of edge from target method.
			Edge edge  = edges.next();
			
			//Add an edge into list.
			edgeList.add(edge);
			
			//Get next "method".
			SootMethod targetMethod = edge.tgt();
			
			//Loop Check
			if(checkLoop(targetMethod, edgeList)) {
				//When a loop is found, add the current edgeList into ret and return ret value.
				ret.add(edgeList);
				return ret;
			}
			
			//recursive calling
			searchPath(targetMethod, cg, edgeList, ret);
			
		} else {
			
			ret.add(edgeList);
			
			return ret;
			
		}
		
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
