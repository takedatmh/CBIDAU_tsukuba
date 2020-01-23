package dataflow;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import soot.Body;
import soot.MethodOrMethodContext;
import soot.PackManager;
import soot.Scene;
import soot.SceneTransformer;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Transform;
import soot.Unit;
import soot.jimple.toolkits.callgraph.CHATransformer;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.UnitGraph;
import soot.util.Chain;
import soot.util.dot.DotGraph;
import soot.util.queue.QueueReader;
import dataflow.util.Utility4Soot;

/**
 * In case of existing statements more than one branch and one external call
 * method.
 * 
 * @author takedatmh
 *
 */
public class CFG_CG_20200113_v2 {

	// Method Name
	public static String methodName = "create";
	public static String mainClass = "simple.client.Client";
	public static String targetClass = "simple.logic.Logic_static";
	
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

// Console Output
// System.out.println(" src = " + srcString);
// System.out.println(" tgt = " + tgtString);

		}
		canvas.plot(fileName);
		return;
	}

	/**
	 * Main Method.
	 * 
	 * @param followsing java command args are puted into String[];
	 * -whole-program -xml-attributes -keep-line-number -f jimple -p cg.cha enabled:true -app simple.logic.Logic -p cg verbose:true,all-reachable:true,,safe-forname:true,safe-newinstance:true
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
						/////////////Call Flow///////////////////////////////
						CallGraph cg = Scene.v().getCallGraph();
						SerializeCallGraph(cg, "CallGraph_" + methodName
								+ DotGraph.DOT_EXTENSION);
						
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
						//Get each unit from UnitGraph.
						Iterator<Unit> units = unitGraph.iterator();
						//Retrieve each node element.
						Unit start = null;
						Unit endNode = null;
						while(units.hasNext()){
							start = units.next();
							List<Unit> ends = null;
							if(unitGraph.getSuccsOf(start) != null){
								ends = unitGraph.getSuccsOf(start);
							} else {
								endNode = start;
							}
							//Judge whether each node has static variables references.
							for (int i = 0; i < ends.size(); i++) {
								Unit childUnit = ends.get(i);
								System.out.println("##Each nodes : " + childUnit.toString());
							}
						}
						
//						//Path extraction
//						if(endNode != null)
//							System.out.println(unitGraph.getExtendedBasicBlockPathBetween(start, endNode));
						
						//Field Analysis
						Chain<SootField> sootFields = sootClass.getFields();
						Iterator<SootField> iteratorFields = sootFields.iterator();
						List<SootField> staticFieldList = new ArrayList<SootField>();
						while(iteratorFields.hasNext()){
							SootField field = iteratorFields.next();
							if(field.isStatic() == true){
								staticFieldList.add(field);
							}
						}
						System.out.println("-#-" + staticFieldList.toString());
						//-##-public static java.lang.String staticVariable
						System.out.println("-##-" + staticFieldList.get(0).getDeclaration());
						//Display Declaring class name with package from SootField instance.
						System.out.println("-###-" + staticFieldList.get(0).getDeclaringClass());

					}

				}));
		
		/* Execute Soot main method with your designated argument values */
		soot.Main.main(args2);

	}

}