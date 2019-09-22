package dataflow;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import soot.Body;
import soot.MethodOrMethodContext;
import soot.PackManager;
import soot.Scene;
import soot.SceneTransformer;
import soot.SootClass;
import soot.SootMethod;
import soot.Transform;
import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.jimple.InvokeExpr;
import soot.jimple.toolkits.callgraph.CHATransformer;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;
import soot.toolkits.graph.BriefUnitGraph;
import soot.toolkits.graph.CompleteBlockGraph;
import soot.toolkits.graph.CompleteUnitGraph;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.UnitGraph;
import soot.util.dot.DotGraph;
import soot.util.queue.QueueReader;
import dataflow.tutorial.GuaranteedDefs;
import dataflow.tutorial.GuaranteedDefsAnalysis;

/**
 * In case of existing statements more than one branch and one external call method.
 * @author takedatmh
 *
 */
public class CFG_DF_20190803{
	
	//Method Name
	private static String methodName = "read";
	
	//CFG
	public static void SerializeControlFlowGraph(UnitGraph graph, String fileName){
		if(fileName == null){
			fileName = soot.SourceLocator.v().getOutputDir();
			if(fileName.length() > 0){
				fileName = fileName + java.io.File.separator;
			}
			fileName = fileName + "call-graph" + DotGraph.DOT_EXTENSION;
		}
		System.out.println("file name "+ fileName);
		DotGraph canvas = new DotGraph("call-graph");
		
		///////////////////
		Iterator<Unit> iteratorUnit = graph.iterator();
		while(iteratorUnit.hasNext()){
			Unit start = iteratorUnit.next();
			List<Unit> ends = graph.getSuccsOf(start);
			for(int i =0; i < ends.size(); i++){
				if(
						(!start.toString().startsWith("<java.") && !start.toString().startsWith("<sun.") && !start.toString().startsWith("<org.") &&
								!start.toString().startsWith("<com.") && !start.toString().startsWith("<jdk.") && !start.toString().startsWith("<javax."))
								||
							(!ends.get(i).toString().startsWith("<java.") && !ends.get(i).toString().startsWith("<sun.") && !ends.get(i).toString().startsWith("<org.") &&
									!ends.get(i).toString().startsWith("<com.") && !ends.get(i).toString().startsWith("<jdk.") && !ends.get(i).toString().startsWith("<javax."))
				){
					canvas.drawNode(start.toString());
					canvas.drawNode(ends.get(i).toString());
					canvas.drawEdge(start.toString(), ends.get(i).toString());					
				}

			}
		}
		canvas.plot(fileName);
		return;
	}
	
	//CG
	public static void SerializeCallGraph(CallGraph graph, String fileName){
		if(fileName == null){
			fileName = soot.SourceLocator.v().getOutputDir();
			if(fileName.length() > 0){
				fileName = fileName + java.io.File.separator;
			}
			fileName = fileName + "call-graph" + DotGraph.DOT_EXTENSION;
		}
		System.out.println("file name "+ fileName);
		DotGraph canvas = new DotGraph("call-graph");
		QueueReader<Edge> listener= graph.listener();
		
		while(listener.hasNext()){
			Edge next = listener.next();
			MethodOrMethodContext src = next.getSrc();
			MethodOrMethodContext tgt = next.getTgt();
			String srcString = src.toString();
			String tgtString = tgt.toString();
			if( (!srcString.startsWith("<java.") && !srcString.startsWith("<sun.") && !srcString.startsWith("<org.") &&
					!srcString.startsWith("<com.") && !srcString.startsWith("<jdk.") && !srcString.startsWith("<javax."))
					||
				(!tgtString.startsWith("<java.") && !tgtString.startsWith("<sun.") && !tgtString.startsWith("<org.") &&
						!tgtString.startsWith("<com.") && !tgtString.startsWith("<jdk.") && !tgtString.startsWith("<javax."))){
				canvas.drawNode(src.toString());
				canvas.drawNode(tgt.toString());
				canvas.drawEdge(src.toString(), tgt.toString());
				System.out.println(" src = " + srcString);
				System.out.println(" tgt = " + tgtString);
			}
		}
		canvas.plot(fileName);
		return;
	}
	
	public static void main(String[] args){
		final List<String> workflowsNames = new ArrayList<String>();

	    List<String> argsList = new ArrayList<String>(Arrays.asList(args));
//	    argsList.addAll(Arrays.asList(new String[] {"-w", "-main-class",
//	    		"org.apache.struts.action.TestActionServlet","org.apache.struts.action.TestActionServlet",
//	    		"org.apache.struts.action.ActionServlet"}));
	    argsList.addAll(Arrays.asList(new String[] {"-w", "-main-class",
	    		"simple.client.Client",
	    		"simple.client.Client",
	    		"simple.logic.Logic"
	    		}));
	    String[] args2 = new String[argsList.size()];
	    args2 = argsList.toArray(args2);
	    
	    /**
	     * WJTP(whole jimple transformation pack) phase's implementation.
	     * Whole of Analysis for the jimple transfer from java to jimple.
	     */
	    PackManager.v().getPack("wjtp").add(new Transform("wjtp.myTrans", new SceneTransformer() {
			protected void internalTransform(String phaseName, @SuppressWarnings("rawtypes") Map options) {
				/////////////Control Flow Graph//////////////////////
		       CHATransformer.v().transform();
	
		       // SootClass a = Scene.v().getSootClass("org.apache.struts.action.ActionServlet");
		       SootClass a = Scene.v().getSootClass("simple.logic.Logic");
	           //Analyze class structure.
	           //get Method
	           a.setApplicationClass();
	           //SootMethod has a data of jimple code of designated method.
	           //SootMethod method = a.getMethodByName("processActionConfigClass");
	           SootMethod method = a.getMethodByName(methodName);
	           //Body has same jimple code to SootMethod's activeBody.
	           Body body = method.retrieveActiveBody();
	           //search body
	           List<ValueBox> list = body.getUseBoxes();
	           System.out.println("Box:"+list);
	           
	           //Control flow Graph by UnitGraph
	           //UnitGraph  unitGraph = new BriefUnitGraph(body);
	           UnitGraph unitGraph = new ExceptionalUnitGraph(body);
	           //UnitGraph  unitGraph = new CompleteUnitGraph(body);
	           //CompleteBlockGraph unitGraph = new soot.toolkits.graph.CompleteBlockGraph(body);
               Iterator<Unit> units = unitGraph.iterator(); 
               while(units.hasNext()){
	               Unit u = units.next();
	               Iterator<ValueBox> iValueBox = u.getUseBoxes().iterator(); 
	               int counter = 0;
	               while(iValueBox.hasNext()){ 
		                ValueBox valueBox = iValueBox.next(); 
		                Value v = valueBox.getValue(); 
		                if(v instanceof InvokeExpr){ 
		                 InvokeExpr m = (InvokeExpr)v; 
		                 SootMethod sootMethod = m.getMethod(); 
		                 String methodName = sootMethod.getName(); 
		                 String className = sootMethod.getClass().toString();
		                 String packageName = sootMethod.getClass().getPackage().toString();
		                 workflowsNames.add(++counter+":"+packageName+"."+className+"."+methodName); 
		                }
	               } 
               }
	           
	           ///////DFA//////////////////////
	           //GuaranteedDefsAnalysis gf = new GuaranteedDefsAnalysis(unitGraph);
	           GuaranteedDefs gf = new GuaranteedDefs(unitGraph);
	           Iterator<Unit> units2 = unitGraph.iterator(); 
	           PrintWriter pw = null;
	           try {
	               pw = new PrintWriter("DF"+"_"+"Simple_"+ methodName +".txt");
	           
	               while(units2.hasNext()){
	            	   List<?> dfList = gf.getGuaranteedDefs(units2.next());
	            	   System.out.println("####################DataFlow########################");
	            	   System.out.println(dfList);
	                   pw.println(dfList);
	               }
	           
	           } catch (IOException ex) {
	               ex.printStackTrace();
	           } finally{
	               pw.close();
	           }
	           
	           ///////Method Call Flow////////
	           for(String e : workflowsNames)
	        	   System.out.println("WORKFLOW: "+ e);
	           
	           ///////CG///////////////////////
		       //SootMethod src = Scene.v().getMainClass().getMethodByName("main");
		       CallGraph cg = Scene.v().getCallGraph();
	           SerializeCallGraph(cg, "CG_Simple2" + DotGraph.DOT_EXTENSION);
		       
		       //draw CGF/////////////////////
		       SerializeControlFlowGraph(unitGraph, "CFG_Simple_"+ methodName + DotGraph.DOT_EXTENSION);
		       System.out.println("serializeCallGraph completed for CFG.");
		       
		       //draw CG
		       //SerializeCallGraph(cg, "CG_Struts" + DotGraph.DOT_EXTENSION);
		       //System.out.println("serializeCallGraph completed for CG.");
			 }
			
		   }
	    ));

	    args = argsList.toArray(new String[0]);
	    soot.Main.main(args2);
	}
	
}