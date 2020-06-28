package dataflow;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import soot.Body;
import soot.PackManager;
import soot.Scene;
import soot.SceneTransformer;
import soot.SootClass;
import soot.SootMethod;
import soot.Transform;
import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.jimple.AnyNewExpr;
import soot.jimple.AssignStmt;
import soot.jimple.BreakpointStmt;
import soot.jimple.GotoStmt;
import soot.jimple.IdentityStmt;
import soot.jimple.IfStmt;
import soot.jimple.InvokeStmt;
import soot.jimple.MonitorStmt;
import soot.jimple.NewArrayExpr;
import soot.jimple.NewExpr;
import soot.jimple.NewMultiArrayExpr;
import soot.jimple.NopStmt;
import soot.jimple.NullConstant;
import soot.jimple.ReturnStmt;
import soot.jimple.ThrowStmt;
import soot.jimple.toolkits.callgraph.CHATransformer;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.UnitGraph;
import soot.util.dot.DotGraph;
import dataflow.tutorial.GuaranteedDefs;
import dataflow.util.CRUD_Judgement;
import dataflow.util.Context;
import dataflow.util.Utility4Soot;

/**
 * This class provides the following functions:
 * - Create Control Flow Graph. :: SerializaeControlFlowGraph method
 * - Create '.dot' graph file into 'ControlFlowGraphPDF' directory. :: SerializeControlFlowGraph
 * - Create graph data as R igraph csv format and annotate IDAU test priority as 0 or 1.::SerializePath, SerializeControlFlowGraph
 * - 'igraph' data is generated the following two different types:
 *   - 'CFG_igraph_Edge_CLASS_MEHTOD.csv' is has start, end node and CRUD Test Case flag.
 *   - 'CFG_igraph_Node_CLASS_MEHTOD.csv' is has node, CRUF and Data Flow Value.
 * - Create data flow analysis data and output txt data into igraph directory as '.txt' format.
 * - Search all E2E path in Control Flow Graph and output this data into 'CFG_PathList' directory as '.txt' format.
 * 
 * This class is executed by the following java command:
 * <p>
 * -whole-program -xml-attributes -keep-line-number -f jimple -p cg.cha enabled:true -p cg verbose:true,all-reachable:true,safe-forname:true,safe-newinstance:true
 * -Xss1000m -Xmx5000M -Dmethod=main -Dmain=sample.functionA.MainA -Dtarget=sample.functionA.MainA
 * </P>
 * @author takedatmh
 *
 */
public class CFG_DF_20200524 {
	
	//Debug
	static int count = 0;
	
	//Search Loop limit Number
	static int limitNum = 0;

	// Method Name
	public static String methodName = null;
	public static String mainClass = null;
	public static String targetClass = null;

	// CRUD Map
	private static Map<String, String> crudMap = new LinkedHashMap<String, String>();

	// Data Flow LinkedHashMap
	private static LinkedHashMap<String, String> dfMap = new LinkedHashMap<String, String>();


	// Data Flaw Value LinkedHashMap
	//Modified by takeda in 20200510.
	//private static LinkedHashMap<String, String> dfVMap = new LinkedHashMap<String, String>();
 
	// Key value for PathMap
	static Integer index = 0;
	
	//newBranch
	static boolean newBranchFlag = false;
	
	//ExcepationlUnitGraph
	static ExceptionalUnitGraph unitGraph = null;
	
	//BriefUnitGraph is used for 'getExtendedBasicBlockPathBetween(briefUnitGraph.getHeads().get(0), graph.getTails().get(0))'.
	//static BriefUnitGraph briefUnitGraph = null; 
	
	/**
	 * Control Flow Graph Creation
	 * 
	 * Change UnitGraph to EnhancedUnitGraph for adding termination final
	 * node when target source multiple return statements.
	 * graph = new EnhancedUnitGraph(graph.getBody());
	 * 
	 * @param graph
	 * @param fileName
	 * @throws FileNotFoundException
	 */
	public static void SerializeControlFlowGraph(UnitGraph graph,
			String fileName) throws FileNotFoundException {

		// Create CFG output file.
		if (fileName == null) {
			fileName = soot.SourceLocator.v().getOutputDir();
			if (fileName.length() > 0) {
				fileName = fileName + java.io.File.separator;
			}
			fileName = fileName + "call-graph" + DotGraph.DOT_EXTENSION;
		}

////Debug
//System.out.println("CFG file name " + fileName);

		// Create GFG Edge List file for iGraph.
		PrintWriter pwEdge = new PrintWriter("igraph"+ Context.SEPARATOR +"CFG_igraph_Edge_" + targetClass +"_" + methodName + ".csv");
		pwEdge.println("start" + "	" + "end" + "	" + "CRUD_Test");

		// Create GFG Node List file for iGraph.
		PrintWriter pwNode = new PrintWriter("igraph"+ Context.SEPARATOR + "CFG_igraph_Node_" + targetClass +"_" + methodName
				+ "_" + ".csv");
		pwNode.println("Node" + "	" + "CRUD" + "	" + "DataFlowValue");
		
		//Describe Node csv file data.
		for (String node : crudMap.keySet()) {
			// Annotate Node's CRUD info and DF-Value
			pwNode.println(node + "	" + crudMap.get(node) + "	"
					//Modified by takeda in 20200510.
					//+ dfVMap.get(node));
					+ dfMap.get(node));
		}

		//// Create CFG/////////////////
		DotGraph canvas = new DotGraph("Control_Flow_Graph");
		Iterator<Unit> iteratorUnit = graph.iterator();

		while (iteratorUnit.hasNext()) {
			Unit start = iteratorUnit.next();
			List<Unit> ends = graph.getSuccsOf(start);
			for (int i = 0; i < ends.size(); i++) {
				/**
				 * For IDAU Analysis Annotate CRUD & DF information to each
				 * start&End Node name and attribute(id::node_name_CRUD ||
				 * node_name_DF , attribute::CRUD_value || node_name_DF_values)
				 */
				String start_name = crudMap.get(start.toString()) + "_"
						+ start.toString() + "_" + dfMap.get(start.toString());
				// Create ends_name
				String ends_name = crudMap.get(ends.get(i).toString()) + "_"
						+ ends.get(i).toString() + "_"
						+ dfMap.get(ends.get(i).toString());

				// Create canvas object to draw graph by .dot format.
				canvas.drawNode(start_name);
				canvas.drawNode(ends_name);
				canvas.drawEdge(start_name, ends_name);

				// Annotate IDAU Test Y(1)/N(0) information on each edge
				String crudFlag = "0";
				if ("C".equals(crudMap.get(start.toString()))
						&& "R".equals(crudMap.get(ends.get(i).toString()))) {
					crudFlag = "1";
				} else if ("C".equals(crudMap.get(start.toString()))
						&& "U".equals(crudMap.get(ends.get(i).toString()))) {
					crudFlag = "1";
				} else if ("C".equals(crudMap.get(start.toString()))
						&& "D".equals(crudMap.get(ends.get(i).toString()))) {
					crudFlag = "1";
				} else if ("U".equals(crudMap.get(start.toString()))
						&& "R".equals(crudMap.get(ends.get(i).toString()))) {
					crudFlag = "1";
				} else if ("U".equals(crudMap.get(start.toString()))
						&& "U".equals(crudMap.get(ends.get(i).toString()))) {
					crudFlag = "1";
				} else if ("U".equals(crudMap.get(start.toString()))
						&& "D".equals(crudMap.get(ends.get(i).toString()))) {
					crudFlag = "1";
				} else if ("D".equals(crudMap.get(start.toString()))
						&& "C".equals(crudMap.get(ends.get(i).toString()))) {
					crudFlag = "1";
				}
				// Annotate IDAU Test needs under same variable Y(2)
				// For iGraph, create a file consisted of src-tar combination
				// file deliminated tab.
				pwEdge.println(start.toString() + "	" + ends.get(i).toString()
						+ "	" + crudFlag);

			}

		}
		// Close PrintWriter for igraph.
		pwEdge.close();
		pwNode.close();

		// Output Graph data on designated file.
		canvas.plot(fileName);

////Debug
//System.out.println(graph.getHeads().get(0));
//System.out.println(graph.getTails().get(0));
		
		
//////Path detection/////////////
//List<Unit> unitPathE2E = graph.getExtendedBasicBlockPathBetween(briefUnitGraph
//		.getHeads().get(0), graph.getTails().get(0));
		
////Debug
//System.out.println("E2E Path : "+unitPathE2E);

		//Debug
		return;
	}
	

	/**
	 * SerializePath
	 * @param resultPathMap
	 * @throws FileNotFoundException
	 */
	public static void SerializePath(Map<Integer, List<String>> resultPathMap) throws FileNotFoundException{
		if(resultPathMap == null)
			return;
		
		// Create GFG Node List file for iGraph.
		PrintWriter pwPath = new PrintWriter("igraph"+ Context.SEPARATOR + "CFG_igraph_Path_" +targetClass+"_"+ methodName + ".csv");
		
		int columNum = resultPathMap.size();
		String columStr = "";
		for(int i  = 0; i < columNum; i++){
			columStr += "	" + "path" + String.valueOf(i);
		}
		pwPath.println("Node" + "	" + "CRUD" + "	" + "DataFlowValue" + " " + columStr);
		
		for (String node : crudMap.keySet()) {
			//Add Column Str
			String colStr = "";
			//flag
			boolean flag = false;
			//for loop
			for(int i = 0; i < resultPathMap.size(); i++){
				List<String> pathNodeList = resultPathMap.get(i);
				for(String pathNode : pathNodeList){
					if(pathNode.equals(node)){
						colStr += "	" + "1";
						flag = true;
					}
				}
				if(flag == false)
					colStr += "	" + "0";
			}
			
			// Annotate Node's CRUD info and DF-Value
			//Modified by takeda in 20200510.
			//pwPath.println(node + "	" + crudMap.get(node) + "	" + dfVMap.get(node) + colStr);
			pwPath.println(node + "	" + crudMap.get(node) + "	" + dfMap.get(node) + colStr);
		}
		//close
		pwPath.close();
	}
	
	/**
	 * SerializePathEdge
	 * @param resultPathMap
	 * @throws FileNotFoundException
	 */
	public static void SerializePathEdge(Map<Integer, List<String>> resultPathMap, UnitGraph graph) throws FileNotFoundException{
		if(resultPathMap == null)
			return;
		
		//start-end List-List
		List<List<String>> startEndList = new LinkedList<List<String>>();
		
		// Create GFG Node List file for iGraph.
		/* Amend from .¥¥igraph to igraph/. */
		PrintWriter pwPathEdge = new PrintWriter("igraph"+ Context.SEPARATOR +"CFG_igraph_Path_Edge_" +targetClass+"_"+ methodName + ".csv");
		
		int columNum = resultPathMap.size();
		String columStr = "";
		for(int i  = 0; i < columNum; i++){
			columStr += "	" + "path" + String.valueOf(i);
		}
		pwPathEdge.println("start"+ "	" + "end" + "	" + "CRUD_Test" + "	" + columStr);
		
		Iterator<Unit> iteratorUnit = graph.iterator();

		while (iteratorUnit.hasNext()) {
			Unit start = iteratorUnit.next();
			List<Unit> ends = graph.getSuccsOf(start);
			for (int i = 0; i < ends.size(); i++) {
				
				// Annotate IDAU Test Y(1)/N(0) information on each edge
				String crudFlag = "0";
				if ("C".equals(crudMap.get(start.toString()))
						&& "R".equals(crudMap.get(ends.get(i).toString()))) {
					crudFlag = "1";
				} else if ("C".equals(crudMap.get(start.toString()))
						&& "U".equals(crudMap.get(ends.get(i).toString()))) {
					crudFlag = "1";
				} else if ("C".equals(crudMap.get(start.toString()))
						&& "D".equals(crudMap.get(ends.get(i).toString()))) {
					crudFlag = "1";
				} else if ("U".equals(crudMap.get(start.toString()))
						&& "R".equals(crudMap.get(ends.get(i).toString()))) {
					crudFlag = "1";
				} else if ("U".equals(crudMap.get(start.toString()))
						&& "U".equals(crudMap.get(ends.get(i).toString()))) {
					crudFlag = "1";
				} else if ("U".equals(crudMap.get(start.toString()))
						&& "D".equals(crudMap.get(ends.get(i).toString()))) {
					crudFlag = "1";
				} else if ("D".equals(crudMap.get(start.toString()))
						&& "C".equals(crudMap.get(ends.get(i).toString()))) {
					crudFlag = "1";
				}
							
				List<String> st = new LinkedList<String>();
				st.add(start.toString());
				st.add(ends.get(i).toString());
				st.add(crudFlag);
				startEndList.add(st);
////Debug
//System.out.println(startEndList);
			}

		}
		
		//write
		for(List<String> list : startEndList){
			String rowStr = "";
			for(String el : list){
				rowStr += el + "	";
			}
			
			//for loop
			for(int x = 0; x < resultPathMap.size(); x++){
				//flag
				boolean flag = false;
				List<String> pathNodeList = resultPathMap.get(x);
				for(int y = 0; y < pathNodeList.size() - 1; y++){
					if(pathNodeList.get(y).equals(list.get(0)) & pathNodeList.get(y+1).equals(list.get(1))){
						if(x != resultPathMap.size()-1)
							rowStr += "1" + "	";
						else
							rowStr += "1";
						flag = true;
					}
				}
				if(flag == false)
					if(x != resultPathMap.size()-1){
						rowStr += "0" + "	";
					} else {
						rowStr += "0";
					}				
			}		
			//Annotate Node's CRUD info.
			pwPathEdge.println(rowStr);
		}
		//close
		pwPathEdge.close();
		
	}
	
	

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
//	     File igraph_Dir = new File("igraph" + Context.SEPARATOR);
//	     deleteFile(igraph_Dir);
//	     File ControlFlowGraphPDF_Dir = new File("ControlFlowGraphPDF" + Context.SEPARATOR);
//	     deleteFile(ControlFlowGraphPDF_Dir);
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

////Debug
//System.out.println("########Box########");
//for (ValueBox box : list) {
//	System.out.println("Value = " + box.getValue()
//			+ "," + "Tag = " + box.getTag("jtp"));
//}

						/* 
						 * Create Control flow Graph as UnitGraph.
						 * 'unitGraph' is an instance created by ExceptionalUnitGraph which includes Exceptional relation.
						 * 'briefUnitGraph' is an instance created by BriefUnitGraph which dose not include Exceptional relation.
						 * */
						unitGraph = new ExceptionalUnitGraph(body);
						
						//Get unit from UnitGraph.
						Iterator<Unit> units = unitGraph.iterator();
//Loop1 Graph->unit
						while (units.hasNext()) {
//ここから無分離、引数にはUnitとcrudMapを渡して、指定したUnitがCRUDのどれに相当するか判定し、crudMapに格納して返却。							
							Unit u = units.next();
							//Get soot value object from Unit.
							Iterator<ValueBox> iValueBox = u.getUseBoxes().iterator();
//Loop2 Unit->iValueBox
							while (iValueBox.hasNext()) {
								ValueBox valueBox = iValueBox.next();
								Value v = valueBox.getValue();

								/* 
								 * Check Expressions of each soot value and detect CRUD information from each soot value.
								 */
								if (v instanceof NewExpr || v instanceof NewArrayExpr
										|| v instanceof NewMultiArrayExpr || v instanceof AnyNewExpr) {
									crudMap.put(u.toString(), "C");
								} else if (u instanceof NullConstant
										|| u.toString().contains("nullConstant ")
										|| u.toString().contains("delete ")
										|| u.toString().contains("Delete ")) {
									crudMap.put(u.toString(), "D");
								// Treat each Statement.
								} else if (u instanceof AssignStmt) {
									crudMap.put(u.toString(), "U");
								} else if (u instanceof IdentityStmt) {
									crudMap.put(u.toString(), "R");
								} else if (u instanceof GotoStmt) {
									crudMap.put(u.toString(), "R");
								} else if (u instanceof IfStmt) {
									crudMap.put(u.toString(), "R");
								} else if (u instanceof InvokeStmt) {
									crudMap.put(u.toString(), "R");
								} else if (u instanceof MonitorStmt) {
									crudMap.put(u.toString(), "R");
								} else if (u instanceof ReturnStmt) {
									crudMap.put(u.toString(), "R");
								} else if (u instanceof ThrowStmt) {
									crudMap.put(u.toString(), "R");
								} else if (u instanceof BreakpointStmt) {
									crudMap.put(u.toString(), "R");
								} else if (u instanceof NopStmt) {
									crudMap.put(u.toString(), "R");
									// Any other cases are fallen into R.
								} else {
									crudMap.put(u.toString(), "R");
								}
								
								/*
								 * Complement crudMap creation:: detect only refer variable statement and update crudMap key information as R.
								 * Check current Unit object's description like a "r12" by matcher.
								 */
								int sindex = 0;
								sindex = u.getDefBoxes().toString().lastIndexOf("(");

								@SuppressWarnings("unused")
								String tmp = null;
								// Detect Defined Data Flow Value.
								if (sindex != -1) {
									tmp = u.getDefBoxes()
											.toString()
											.substring(sindex + 1,
													u.getDefBoxes().toString().length() - 2);
								}
								// Detect invoked Data Flow Value.
								else if (sindex == -1) {
									tmp = "none";
									Pattern pattern1 = Pattern.compile("¥¥$r[0-9]{1,}");
									Matcher matcher1 = pattern1.matcher(u.toString());
									while (matcher1.find()) {
										String matched = matcher1.group();
										tmp = "$" + matched;
										break;
									}
									Pattern pattern2 = Pattern.compile("r[0-9]{1,}");
									Matcher matcher2 = pattern2.matcher(u.toString());
									while (matcher2.find()) {
										String matched = matcher2.group();
										tmp = "$" + matched;
										break;
									}
								}

								// check crudMap
								Map<String, String> tmap = new HashMap<String, String>();
								Iterator<Unit> units3 = unitGraph.iterator();
								int ct = 0;
								while (units3.hasNext()) {
									Unit unit3 = units3.next();
									List<Unit> childUnits = unitGraph.getSuccsOf(unit3);
									for (int i = 0; i < childUnits.size(); i++) {
										tmap.put(childUnits.get(i).toString(), String.valueOf(ct++));
									}
								}
								// comparison between crudMap and childUnitMap
								for (Map.Entry<String, String> entry : tmap.entrySet()) {
									String key = entry.getKey();
									if (crudMap.get(key) == null) {
										crudMap.put(key, "R");
									}
								}
							}
//END Loop2							
						}
//END Loop1 ここまで
						///////DFA//////////////////////
					    /*
					     * Data Flow Analysis using Soot default API. 
					     * In this class, we have implemented as GuaranteeDefs class.
					     * This getGuaranteedDefs() method returns each live data list.
					     * Following disposal is to get data flow information and put them into dfMap each statement.
					     */
						GuaranteedDefs gf = new GuaranteedDefs(unitGraph);
						Iterator<Unit> units2 = unitGraph.iterator();
						PrintWriter pw = null;
						try {
							/* Amend from .¥¥igraph to igraph/. */
							pw = new PrintWriter("igraph"+ Context.SEPARATOR +"DF" + "_" + "Simple_" + methodName + ".txt");
							//int count = 1;
							while (units2.hasNext()) {
								Unit u = units2.next();
								List<?> dfList = gf.getGuaranteedDefs(u);
//////Debug
//System.out.println(count++ + " : ####################DataFlow########################");
//System.out.println(dfList + "	" + u.toString());
								
								//Write DF data on data flow csv file.
								pw.println(dfList + "	" + u.toString());

								// put DF info into dfMap<Statement, DF variable
								dfMap.put(u.toString(), dfList.toString());

								//count++;
							}
						//PrintWriter close
						} catch (IOException ex) {
							ex.printStackTrace();
						} finally {
							pw.close();
						}

						///////Draw CGF into ControlFlowGraphPDF directory as '.dot' file format./////////////////////
						try {
							SerializeControlFlowGraph(unitGraph, "ControlFlowGraphPDF"+ Context.SEPARATOR +"CFG_" + targetClass
									+ methodName + DotGraph.DOT_EXTENSION);
						} catch (FileNotFoundException e) {
							e.printStackTrace();
						}
						
////Debug						
//System.out.println("serializeCallGraph completed for CFG.");

//////Debug /////SYSOUT CRUD Map//////////////
//System.out.println("##############CRUD##################");
//for (String key : crudMap.keySet()) {
//	System.out.println(crudMap.get(key) + "	" + key);
//}
//
//////Debug//////SYSOUT df Value Map////////
//System.out.println("##############dfVMap##################");
////Modified by takeda in 20200510 from dfVMap to dfMap.
//for (String key : dfMap.keySet()) {
//	System.out.println(dfMap.get(key) + "	" + key);
//}

						////Path Analysis////////////////////////////////////////////////////////////////////
						/*
						 * 再帰処理でUnitグラフの全パスを検索する処理。
						 * Retrieve each test Path in the CFG.
						 * Get Head and Tail node from whole of CFG.
						 * And obtain all CFG Paths from Head to Tail node.
						 */
						//Get Start Node which is called as Head.
						Unit head = unitGraph.getHeads().get(0);
						
						//Preparation of PathList Objects.
						ArrayList<Unit> path = new ArrayList<>();
						List<List<Unit>> pathList = new ArrayList<List<Unit>>();
						
						//Get children of Top node.
						Unit top= unitGraph.getSuccsOf(head).get(0);
						path.add(top);

						/* Search all of CFG Paths from whole CFG by the following searchPathFromCFG method. */
						List<List<Unit>> listOfPath = searchPathFromCFG(top, unitGraph, path, pathList);
						
//Debug
//for(List<Unit> list: listOfPath)
//	System.out.println("E2E:" + list.toString());

						//Write all paths into CFG_PathList directory as '.txt' format file.
						FileWriter in = null;
						PrintWriter out = null;
						String filePath = "CFG_PathList" + Context.SEPARATOR + targetClass + "_"+methodName +".txt";
						try {
							//postscript version 追記形式
							in = new FileWriter(filePath, true);
							out = new PrintWriter(in);
//Modified by takeda in 20200524. Before write Unit string data, retrieve CRUD for each Unit expression and add CRUD info.
							for(List<Unit> pathListUnits : listOfPath){
								String tmpPath = "[";
								
								//Unitを取り出してiValueからCRUD判定を行い、UnitのString情報の頭にC_, R_, U_ and D_として追加する。
								for(Unit unit: pathListUnits){
									tmpPath = tmpPath + CRUD_Judgement.judgeCRUD(unit) + ", ";
								}
								tmpPath = tmpPath.substring(0, tmpPath.length()-1) + "]";
System.out.println("tmpPath: "+tmpPath);
								//パス情報の書き込み
								//Modified by takeda in 20200524 form 'pathListUnits.toString' to 'tmpUnit'.
								//out.println(pathListUnits.toString());
								out.println(tmpPath);
							}
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
					}

				}));
		
		/* Execute Soot main method with your designated argument values */
		soot.Main.main(args2);

	}
	
	/**
	 * This method search all paths from Head to Tail node in CFG by recursive algorithm of DFS.
	 * As a circuit breaker, we set limit number of loop :: 100,000 count.
	 * @param node
	 * @param unitGraph
	 * @param path
	 * @param pathList
	 * @return List of E2E CFG Paths
	 */
	@SuppressWarnings("unchecked")
	public static List<List<Unit>> searchPathFromCFG(Unit node, UnitGraph unitGraph, ArrayList<Unit> path, List<List<Unit>> pathList){
		
		//If children != 0, add the child node into path.
		path.add(node);
////Debug
//System.out.println(count++ + ": PathB:" + path.size());		
		List<Unit> children = unitGraph.getSuccsOf(node);
		int size = children.size();

		if(size != 0){
			for(Unit nextNode : children) {
				//limit counter as breaker.
				limitNum++;
				if(limitNum > 1000000) break;
////Debug				
//System.out.println(count++ + ": PathF:" + path.size());
				if(!isLoop(nextNode, path)) {
					/* Regression Method Call of searchPathfromCFG method. */
					searchPathFromCFG(nextNode, unitGraph, (ArrayList<Unit>)path.clone(), pathList);
					//searchPathFromCFG(nextNode, unitGraph, deepPath, pathList);
				} else {
					continue;
				}
			}
		//Modified takedatmh in 20200510.			
		//} else if (size == 0) {
		} else {
////Debug			
//System.out.println(count++ + "PathList:" + path.toString()); 
			pathList.add(path);
		}
////Debug		
//System.out.println(count++ + "PathList E2E:" + path.toString());

		return pathList;
	}
	
	/**
	 * Private Method for check whether NextNode is contained into already obtained nodes on paths.
	 * If NextNode is included, we skip searchPathFromCFG method and go back to For-Loop again.
	 * @param nextNode
	 * @param path
	 * @return
	 */
	private static boolean isLoop(Unit nextNode, List<Unit> path){
		boolean ret = false;
		
		for(Unit node : path){
			if(node.equals(nextNode))
				ret = true;
		}
		
		return ret;
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
	static public void deleteFile(File dir){
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