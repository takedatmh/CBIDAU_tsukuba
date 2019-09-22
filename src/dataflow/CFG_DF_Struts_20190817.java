package dataflow;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import soot.jimple.*;
import soot.jimple.toolkits.callgraph.CHATransformer;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.UnitGraph;
import soot.util.dot.DotGraph;
import soot.util.queue.QueueReader;
import dataflow.tutorial.GuaranteedDefs;

/**
 * In case of existing statements more than one branch and one external call
 * method.
 * 
 * @author takedatmh
 *
 */
public class CFG_DF_Struts_20190817 {

	// Method Name
	private static String methodName = "deploy";
	private static String mainClass = "org.apache.catalina.manager.TestManagerServlet";
	private static String targetClass = "org.apache.catalina.manager.ManagerServlet";
	private static String resultFolder = "./result/";
	
//	private static String methodName = "create";
//	private static String mainClass = "org.apache.catalina.manager.TestManagerServlet";
//	private static String targetClass = "org.apache.catalina.manager.ManagerServlet";

	// CRUD Map
	private static Map<String, String> crudMap = new LinkedHashMap<String, String>();

	// Data Flow LinkedHashMap
	private static LinkedHashMap<String, String> dfMap = new LinkedHashMap<String, String>();

	// Data Flaw Value LinkedHashMap
	private static LinkedHashMap<String, String> dfVMap = new LinkedHashMap<String, String>();
	
//	//PathFile
//	private static PrintWriter pathFile = null;

	/**
	 * Control Flow Graph Creation
	 * 
	 * @param graph
	 * @param fileName
	 * @throws FileNotFoundException
	 */
	public static void SerializeControlFlowGraph(UnitGraph graph,
			String fileName) throws FileNotFoundException {
		// Change UnitGraph to EnhancedUnitGraph for adding termination final
		// node when target source multiple return statements.
		// graph = new EnhancedUnitGraph(graph.getBody());

		// Create CFG output file.
		if (fileName == null) {
			fileName = soot.SourceLocator.v().getOutputDir();
			if (fileName.length() > 0) {
				fileName = fileName + java.io.File.separator;
			}
			fileName = fileName + "call-graph" + DotGraph.DOT_EXTENSION;
		}

		//System.out.println("CFG file name " + fileName);

		// Create GFG Edge List file for iGraph.
		PrintWriter pwEdge = new PrintWriter(resultFolder+"CFG_igraph_Edge_" + methodName
				+ "_" + ".csv");
		pwEdge.println("start" + "	" + "end" + "	" + "CRUD_Test");

		// Create GFG Node List file for iGraph.
		PrintWriter pwNode = new PrintWriter("CFG_igraph_Node_" + methodName
				+ "_" + ".csv");
		pwNode.println("Node" + "	" + "CRUD" + "	" + "DataFlowValue");
		for (String node : crudMap.keySet()) {
			// Annotate Node's CRUD info and DF-Value
			pwNode.println(node + "	" + crudMap.get(node) + "	"
					+ dfVMap.get(node));
		}

		// Create CFG/////////////////
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
				// Create start_name
				// String start_name = start.toString() + "_" +
				// crudMap.get(start.toString()) + "_" +
				// dfMap.get(start.toString()) + "_" +
				// start.getUseAndDefBoxes().toString();
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

		// //////////Path detection/////////////
		//System.out.println(graph.getHeads().get(0));
		//System.out.println(graph.getTails().get(0));
//		List<Unit> unitPathE2E = graph.getExtendedBasicBlockPathBetween(graph
//				.getHeads().get(0), graph.getTails().get(0));
		//System.out.println(unitPathE2E);

		return;
	}

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
			fileName = "CG_" + fileName + DotGraph.DOT_EXTENSION;
		}
		//System.out.println("file name " + fileName);
		DotGraph canvas = new DotGraph("Call_Graph");
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
	 * SerializePath
	 * @param resultPathMap
	 * @throws FileNotFoundException
	 */
	public static void SerializePath(Map<Integer, List<String>> resultPathMap) throws FileNotFoundException{
		if(resultPathMap == null)
			return;
		
		// Create GFG Node List file for iGraph.
		PrintWriter pwPath = new PrintWriter(resultFolder + "CFG_igraph_Path_" + methodName + "_" + ".csv");
		
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
			pwPath.println(node + "	" + crudMap.get(node) + "	" + dfVMap.get(node) + colStr);
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
		PrintWriter pwPathEdge = new PrintWriter(resultFolder + "CFG_igraph_Path_Edge_" + methodName + "_" + ".csv");
		
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
			//Annotate Node's CRUD info and DF-Value
			pwPathEdge.println(rowStr);
		}
		//close
		pwPathEdge.close();
		
	}
	
	

	/**
	 * Main Method.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {

		List<String> argsList = new ArrayList<String>(Arrays.asList(args));
		argsList.addAll(Arrays.asList(new String[] { "-w", "-main-class",
	    		mainClass,
	    		mainClass,
	    		targetClass }));
		String[] args2 = new String[argsList.size()];
		args2 = argsList.toArray(args2);

		/**
		 * WJTP(whole jimple transformation pack) phase's implementation. Whole
		 * of Analysis for the jimple transfer from java to jimple.
		 */
		PackManager.v().getPack("wjtp")
				.add(new Transform("wjtp.myTrans", new SceneTransformer() {
					protected void internalTransform(String phaseName,
							@SuppressWarnings("rawtypes") Map options) {
						// ///////////Control Flow Graph//////////////////////
						CHATransformer.v().transform();

						// SootClass a =
						// Scene.v().getSootClass("org.apache.struts.action.ActionServlet");
						SootClass a = Scene.v().getSootClass(targetClass);

						// Analyze class structure.//
						// get Method
						a.setApplicationClass();
						// SootMethod has a data of jimple code of designated
						// method.
						SootMethod method = a.getMethodByName(methodName);
						// Body has same jimple code to SootMethod's activeBody.
						Body body = method.retrieveActiveBody();
						// search body
//						List<ValueBox> list = body.getUseBoxes();
//						System.out.println("########Box########");
//						for (ValueBox box : list) {
//							System.out.println("Value = " + box.getValue()
//									+ "," + "Tag = " + box.getTag("jtp"));
//						}

						// Control flow Graph by UnitGraph
						UnitGraph unitGraph = new ExceptionalUnitGraph(body);
						// UnitGraph unitGraph = new EnhancedUnitGraph(body);
						Iterator<Unit> units = unitGraph.iterator();
						while (units.hasNext()) {
							Unit u = units.next();
							Iterator<ValueBox> iValueBox = u.getUseBoxes()
									.iterator();
//							int counter = 0;
							while (iValueBox.hasNext()) {
								ValueBox valueBox = iValueBox.next();
								Value v = valueBox.getValue();

								/* Check each value statement. */
//								System.out
//										.println(counter
//												+ "############Check satement or Expr###############");
								// Treat each detail of Expr disposal for
								// detection of new as create and delete as
								// delete.)
								if (v instanceof NewExpr
										|| v instanceof NewArrayExpr
										|| v instanceof NewMultiArrayExpr
										|| v instanceof AnyNewExpr) {
//									System.out.println(u.toString() + ":::"
//											+ "NewExpr");
									crudMap.put(u.toString(), "C");
								} else if (u instanceof NullConstant
										|| u.toString().contains(
												"nullConstant ")
										|| u.toString().contains("delete ")
										|| u.toString().contains("Delete ")) {
//									System.out.println("Null Costant");
									crudMap.put(u.toString(), "D");
									// Treat each Statement.
								} else if (u instanceof AssignStmt) {
//									System.out.println(u.toString() + ":::"
//											+ "AssignStmt");
									crudMap.put(u.toString(), "U");
								} else if (u instanceof IdentityStmt) {
//									System.out.println(u.toString() + ":::"
//											+ "IdentityStmt");
									crudMap.put(u.toString(), "R");
								} else if (u instanceof GotoStmt) {
//									System.out.println(u.toString() + ":::"
//											+ "GotoStmt");
									crudMap.put(u.toString(), "R");
								} else if (u instanceof IfStmt) {
//									System.out.println(u.toString() + ":::"
//											+ "IfStmt");
									crudMap.put(u.toString(), "R");
								} else if (u instanceof InvokeStmt) {
//									System.out.println(u.toString() + ":::"
//											+ "InvokeStmt");
									crudMap.put(u.toString(), "R");
								} else if (u instanceof MonitorStmt) {
//									System.out.println(u.toString() + ":::"
//											+ "MonitorStmt");
									crudMap.put(u.toString(), "R");
								} else if (u instanceof ReturnStmt) {
//									System.out.println(u.toString() + ":::"
//											+ "ReturnStmt");
									crudMap.put(u.toString(), "R");
								} else if (u instanceof ThrowStmt) {
//									System.out.println(u.toString() + ":::"
//											+ "ThrowStmt");
									crudMap.put(u.toString(), "R");
								} else if (u instanceof BreakpointStmt) {
//									System.out.println(u.toString() + ":::"
//											+ "BreakpointStmt");
									crudMap.put(u.toString(), "R");
								} else if (u instanceof NopStmt) {
//									System.out.println(u.toString() + ":::"
//											+ "NopStmt");
									crudMap.put(u.toString(), "R");
									// Any other cases are fallen into R.
								} else {
//									System.out.println("other Read cases.");
									crudMap.put(u.toString(), "R");
								}

								/*
								 * Data Flow Value Detection and put these
								 * values into the dfVMap.
								 */
								int sindex = 0;
								sindex = u.getDefBoxes().toString()
										.lastIndexOf("(");

								String tmp = null;
								// Detect Defined Data Flow Value.
								if (sindex != -1) {
									tmp = u.getDefBoxes()
											.toString()
											.substring(
													sindex + 1,
													u.getDefBoxes().toString()
															.length() - 2);
								}
								// Detect invoked Data Flow Value.
								else if (sindex == -1) {
									tmp = "none";
									Pattern pattern1 = Pattern
											.compile("ﾂ･ﾂ･$r[0-9]{1,}");
									Matcher matcher1 = pattern1.matcher(u
											.toString());
									while (matcher1.find()) {
										String matched = matcher1.group();
//										System.out
//												.printf("[%s] 縺後�槭ャ繝√＠縺ｾ縺励◆縲� Pattern:[%s] input:[%s]\n",
//														matched, pattern1,
//														u.toString());
										tmp = "$" + matched;
										break;
									}
									Pattern pattern2 = Pattern
											.compile("r[0-9]{1,}");
									Matcher matcher2 = pattern2.matcher(u
											.toString());
									while (matcher2.find()) {
										String matched = matcher2.group();
//										System.out
//												.printf("2:"
//														+ "[%s] 縺後�槭ャ繝√＠縺ｾ縺励◆縲� Pattern:[%s] input:[%s]\n",
//														matched, pattern1,
//														u.toString());
										tmp = "$" + matched;
										break;
									}
								}

								// Put these valuses into dfVMap.
								dfVMap.put(u.toString(), tmp);

//								System.out
//										.println("//////////////////////////////////////"
//												+ u.getDefBoxes().toString());
//								System.out
//										.println("//////////////////////////////////////"
//												+ sindex);
//								System.out
//										.println("//////////////////////////////////////"
//												+ tmp);
								// System.out.println("//////////////////////////////////////"
								// + tmp.replace(")", ""));
								// System.out.println("//////////////////////////////////////"
								// + tmp.replace("]", ""));

								// check crudMap
								Map<String, String> tmap = new HashMap<String, String>();
								Iterator<Unit> units3 = unitGraph.iterator();
								int ct = 0;
								while (units3.hasNext()) {
									Unit unit3 = units3.next();
									List<Unit> childUnits = unitGraph
											.getSuccsOf(unit3);
									for (int i = 0; i < childUnits.size(); i++) {
										tmap.put(childUnits.get(i).toString(),
												String.valueOf(ct++));
									}
								}
								// comparison between crudMap and childUnitMap
								for (Map.Entry<String, String> entry : tmap
										.entrySet()) {
									String key = entry.getKey();
									if (crudMap.get(key) == null) {
										crudMap.put(key, "R");
									}
								}

								// //Add an original disposal for each
								// statement.
								// if(v instanceof InvokeExpr){
								// InvokeExpr m = (InvokeExpr)v;
								// SootMethod sootMethod = m.getMethod();
								// String methodName = sootMethod.getName();
								// String className =
								// sootMethod.getClass().toString();
								// String packageName =
								// sootMethod.getClass().getPackage().toString();
								// workflowsNames.add(++counter+":"+packageName+"."+className+"."+methodName);
								// }
							}
						}

						// /////DFA//////////////////////
						GuaranteedDefs gf = new GuaranteedDefs(unitGraph);
						Iterator<Unit> units2 = unitGraph.iterator();
						PrintWriter pw = null;
						try {
							pw = new PrintWriter(resultFolder + "DF" + "_" + "Simple_"+ methodName + ".txt");
//							int count = 1;
							while (units2.hasNext()) {
								Unit u = units2.next();
								List<?> dfList = gf.getGuaranteedDefs(u);
//								System.out
//										.println(count
//												+ " : ####################DataFlow########################");
//								System.out.println(dfList + "	" + u.toString());
								pw.println(dfList + "	" + u.toString());

								// put DF info into dfMap<Statement, DF variable
								// List>
								dfMap.put(u.toString(), dfList.toString());
//								count++;
							}
						} catch (IOException ex) {
							ex.printStackTrace();
						} finally {
							pw.close();
						}

						// /////Method Call Flow////////
						// for(String e : workflowsNames)
						// System.out.println("WORKFLOW: "+ e);

						// /////CG///////////////////////
						//SootMethod src =
						//Scene.v().getMainClass().getMethodByName("main");
//						CallGraph cg = Scene.v().getCallGraph();
//						SerializeCallGraph(cg, resultFolder + targetClass + DotGraph.DOT_EXTENSION);

						// /////Draw CGF/////////////////////
						try {
							SerializeControlFlowGraph(unitGraph, resultFolder+"CFG_"+ methodName + DotGraph.DOT_EXTENSION);
						} catch (FileNotFoundException e) {
							e.printStackTrace();
						}
						System.out
								.println("serializeCallGraph completed for CFG.");

						// /////SYSOUT CRUD Map//////////////
						System.out
								.println("##############CRUD##################");
						for (String key : crudMap.keySet()) {
							System.out.println(crudMap.get(key) + "	" + key);
						}

						// //////SYSOUT df Value Map////////
						System.out
								.println("##############dfVMap##################");
						for (String key : dfVMap.keySet()) {
							System.out.println(dfVMap.get(key) + "	" + key);
						}

						// //////Path Analysis////////////////////////////////////////////////////////////////////
						
						// seen
						Map<String, Boolean> seen = new LinkedHashMap<String, Boolean>();
						//List<String> vList = new LinkedList<String>();
						for (String key : crudMap.keySet()) {
							seen.put(key, false);
						}

						// Map<StringNode, Unit>
						Map<String, Unit> strUnitMap = new LinkedHashMap<String, Unit>();
						Iterator<Unit> unit = unitGraph.iterator();
						while (unit.hasNext()) {
							Unit node = unit.next();
							strUnitMap.put(node.toString(), node);
						}

						// stack
						Deque<Unit> todo = new ArrayDeque<Unit>();

						// PathListMap
						Map<Integer, List<String>> pathListMap = new LinkedHashMap<Integer, List<String>>();

						// Get Head and Tails.
						Unit top = unitGraph.getHeads().get(0);
						List<Unit> tails = unitGraph.getTails();

						// VistNodeList
						List<String> visitNodeList = new LinkedList<String>();

						// Check whether this graph has multiple end nodes or not.
						@SuppressWarnings("unused")
						boolean multiTailFlag = false;
						if (tails.size() > 1)
							multiTailFlag = true;
						
						//繝槭Ν繝√お繝ｳ繝峨げ繝ｩ繝輔〒縺ｪ縺代ｌ縺ｰ
						Map<Integer, List<String>> resultPathMap = searchPath(unitGraph, top, tails.get(0), todo, seen, visitNodeList, pathListMap, strUnitMap);

//						for (Integer key : resultPathMap.keySet()) {
//							System.out.println("=_=; " + resultPathMap.get(key));
//						}

						//PathFile
						try {
							SerializePath(resultPathMap);
						} catch (FileNotFoundException e) {
							e.printStackTrace();
						}
						
						//PathEdgeFile
						try {
							SerializePathEdge(resultPathMap, unitGraph);
						} catch (FileNotFoundException e) {
							e.printStackTrace();
						}
						
					}
					
					

				}));

		args = argsList.toArray(new String[0]);
		soot.Main.main(args2);
	}

	
	// PathMap縺ｮ繧ｭ繝ｼ逕ｨ
	static Integer index = 0;
	
	//newBranch
	static boolean newBranchFlag = false;

	/**
	 * Search all test case pathes on UnitGraph.
	 * 
	 * @param graph
	 * @param currentNode
	 * @param tail
	 * @param todo
	 * @param seen
	 * @param visitNodeList
	 * @param pathListMap
	 * @param strUnitMap
	 * @param index
	 * @return
	 */
	public static Map<Integer, List<String>> searchPath(UnitGraph graph,
			Unit currentNode, Unit tail, Deque<Unit> todo,
			Map<String, Boolean> seen, List<String> visitNodeList,
			Map<Integer, List<String>> pathListMap,
			Map<String, Unit> strUnitMap) {
		/*
		 * �ｼ祖urrentNode縺ｫ蟇ｾ縺励※seen縺ｮ隧ｲ蠖哲ode繧稚rue縺ｫ縺吶ｋ 0.蠑墓焚縺九ｉnode繧貞叙繧雁�ｺ縺礼ｵらｫｯ繝弱�ｼ繝峨〒縺ｪ縺�縺薙→繧堤｢ｺ隱阪☆繧九��
		 * 縲�0.1.邨らｫｯ繝弱�ｼ繝� & todo縺ｫtrue縺ｧ縺ｪ縺�node縺後≠繧句�ｴ蜷茨ｼ夂ｵらｫｯ繝弱�ｼ繝峨ｒvisitNodeList縺ｫ蜉�縺医※縲｀ap縺ｫvisitNodeList繧呈�ｼ邏�
		 *    0.1.1.visitNodeList縺ｮ譛�邨ゅう繝ｳ繝�繝�繧ｯ繧ｹ縺九ｉ蟾ｦ縺ｫ繧､繝ｳ繝�繝�繧ｯ繧ｹ繧剃ｸ九￡縺ｦ陦後″縺ｪ縺後ｉseen縺ｮ荳ｭ縺ｧfalse縺ｫ縺ｪ縺｣縺ｦ縺�繧九ヮ繝ｼ繝峨ｒchild縺ｫ謖√▽繝弱�ｼ繝峨ｒ騾�謗｢邏｢縺吶ｋ 縲�
		 *    0.1.2.騾�謗｢邏｢縺励※seen false縺ｮ繝弱�ｼ繝峨ｒ蟄蝉ｾ帙→縺励※謖√▽繝弱�ｼ繝峨ｒcurrent繝弱�ｼ繝峨→縺励※蠑墓焚謖�螳壹〒�ｼ斐↓謌ｻ繧九��
		 *           縺昴�ｮ髫帙�」isitNodeList繧偵さ繝斐�ｼ縺励※縲�髢句ｧ九う繝ｳ繝�繝�繧ｯ繧ｹ縺九ｉvisitNodeList繧剃ｸ頑嶌縺阪＠縺ｦ陦後￥縲� 縲�
		 * 0.2.邨らｫｯ繝弱�ｼ繝� & todo縺後☆縺ｹ縺ｦtrue縺ｮ蝣ｴ蜷茨ｼ夂ｵらｫｯ繝弱�ｼ繝峨↓蟇ｾ縺励※senn繧稚rue, NodeList縺ｫ蜉�縺医※縲｀ap縺ｫnodeList繧呈�ｼ邏阪＠縲∫ｵゆｺ�縲�
		 * 01.CurrentNode縺ｮ蟄蝉ｾ帙◆縺｡繧呈､懃ｴ｢ 
		 * �ｼ致isitNodeList縺ｫnode繧呈�ｼ邏阪☆繧九�� 
		 * �ｼ鍍odo縺ｫ蟄蝉ｾ帙◆縺｡繧貞�･繧後ｋ縲�
		 * �ｼ杯odo縺九ｉ蟄蝉ｾ帙ｒ荳�莠ｺ蜿悶ｊ蜃ｺ縺励※縲》odo縺九ｉ豸医☆縲� 
		 * �ｼ募叙繧雁�ｺ縺励◆蟄蝉ｾ帙′seen縺ｮ荳ｭ縺ｧtrue縺ｫ縺ｪ縺｣縺ｦ縺ｪ縺�縺玖ｪｿ縺ｹ繧�
		 * 縲��ｼ包ｼ趣ｼ奏rue縺�縺｣縺溘ｉ�ｼ斐↓謌ｻ縺｣縺ｦ蛻･縺ｪ蟄蝉ｾ帙ｒ蜿悶ｊ蜃ｺ縺� 
		 *    �ｼ包ｼ趣ｼ托ｼ趣ｼ代％縺ｩ繧ゅ′縺�縺ｪ縺上↑縺｣縺溘ｉ邨ゅｏ繧� 縲�
		 *   �ｼ包ｼ趣ｼ断alse縺�縺｣縺溘ｉ
		 *     5.2.1.todo縺九ｉ蜿悶ｊ蜃ｺ縺励◆繝弱�ｼ繝峨ｒ謖�螳壹＠縺ｦ�ｼ�1縺ｫ謌ｻ繧�
		 */
		// �ｼ創ode縺ｫ蟇ｾ縺励※seen縺ｮNode繧稚rue縺ｫ縺吶ｋ
		seen.put(currentNode.toString(), true);
		
		if(currentNode.equals(tail)){
			// seen縺ｮ荳ｭ縺ｧtrue縺ｧ縺ｪ縺�node縺後≠繧九°縺ｪ縺�縺九�ｮresearchFlag縺ｮ蜿門ｾ怜�ｦ逅�
			// todo縺ｫ谿九▲縺ｦ縺�縺ｦ荳斐▽seen縺ｮ荳ｭ縺ｧfalse縺ｮ繝弱�ｼ繝�
			String restartNode = null;
			// todo(deque)縺九ｉ谿九ｊ繧剃ｸ�蛟九▼縺､蜿悶ｊ蜃ｺ縺励※縲�
			Iterator<Unit> todoUnits = todo.iterator();
			boolean researchFlag = false;
			// todo縺ｮ荳ｭ縺ｮ繝弱�ｼ繝峨ｒ蜿悶ｊ蜃ｺ縺励※縲《een縺ｧfalse縺ｮ繧ゅ�ｮ縺瑚ｦ九▽縺九▲縺殲esearchFlag縺荊rue縺ｫ縺ｪ繧九∪縺ｧ繝ｫ繝ｼ繝�
			Loop1: while (todoUnits.hasNext() & researchFlag == false) {
				Unit todoUnit = todoUnits.next();
				// seen縺ｫfalse縺ｨ縺励※逋ｻ骭ｲ縺輔ｌ縺ｦ縺�繧九°蜷ｦ縺九メ繧ｧ繝�繧ｯ
				for (String key : seen.keySet()) {
					// 邨らｫｯ繝弱�ｼ繝� & todo縺ｫseen縺ｮ縺ｪ縺九〒true縺ｧ縺ｪ縺�node縺後≠繧句�ｴ蜷医��縺ｮ譚｡莉ｶ蛻､螳�
					if (key.equals(todoUnit.toString())
							& seen.get(key) == false) {
						researchFlag = true;
						restartNode = key;
						break Loop1;
					}
				}
			}
				
			// 0.1 邨らｫｯ繝弱�ｼ繝� & todo縺ｫseen縺ｮ縺ｪ縺九〒true縺ｧ縺ｪ縺�node縺後≠繧句�ｴ蜷茨ｼ�
			if (researchFlag == true) {
				// 邨らｫｯ繝弱�ｼ繝峨ｒvisitNodeList縺ｫ蜉�縺医※縲｀ap縺ｫvisitNodeList繧呈�ｼ邏�
				seen.put(currentNode.toString(), true);
				//visitNodeList.add(currentNode.toString());
				pathListMap.put(index++, visitNodeList);
				
				// 0.1.1.visitNodeList縺ｮ譛�邨ゅう繝ｳ繝�繝�繧ｯ繧ｹ縺九ｉ蟾ｦ縺ｫ繧､繝ｳ繝�繝�繧ｯ繧ｹ繧剃ｸ九￡縺ｦ陦後″縺ｪ縺後ｉ縲《een縺ｮ荳ｭ縺ｧfalse縺ｫ縺ｪ縺｣縺ｦ縺�繧九ヮ繝ｼ繝�(restartNode)繧団hild縺ｫ謖√▽繝弱�ｼ繝峨ｒ騾�謗｢邏｢縺吶ｋ
				int indexVNL = visitNodeList.size();
				//for (int i = indexVNL; i < 0; i--) {
				for(int i = 0; i < indexVNL; i++){
					String node = visitNodeList.get(indexVNL - 1 - i);
					Unit unit = strUnitMap.get(node);
					List<Unit> childUnits = graph.getSuccsOf(unit);
					// seen縺ｮ荳ｭ縺ｧfalse縺ｫ縺ｪ縺｣縺ｦ縺�繧九ヮ繝ｼ繝�(restartNode)繧団hild縺ｫ謖√▽繝弱�ｼ繝峨ｒ逋ｺ隕�
					if (childUnits.contains(strUnitMap.get(restartNode))) {
						// 0.1.2.騾�謗｢邏｢縺励※seen
						// false縺ｮ繝弱�ｼ繝峨ｒ蟄蝉ｾ帙→縺励※謖√▽繝弱�ｼ繝峨ｒcurrent繝弱�ｼ繝峨→縺励※蠑墓焚謖�螳壹〒�ｼ斐↓謌ｻ繧九��
						// 縺昴�ｮ髫帙�」isitNodeList繧偵さ繝斐�ｼ縺励※縲�髢句ｧ九う繝ｳ繝�繝�繧ｯ繧ｹ縺九ｉvisitNodeList繧剃ｸ頑嶌縺阪＠縺ｦ陦後￥縲�
						List<String> tmpVisitNodeList = new LinkedList<String>();
						for (int x = 0; x < indexVNL - i; x++) {
							tmpVisitNodeList.add(x, visitNodeList.get(x));
						}
						//NewBranch Flag 繧偵��true
						newBranchFlag = true;
						// 04縺九ｉ螳溯｡�
						methodNo4(graph, tail, todo, seen,
								tmpVisitNodeList, pathListMap, strUnitMap);
					} 
				}
			}
			//0.2.邨らｫｯ繝弱�ｼ繝� & todo縺後☆縺ｹ縺ｦtrue縺ｮ蝣ｴ蜷茨ｼ夂ｵらｫｯ繝弱�ｼ繝峨↓蟇ｾ縺励※senn繧稚rue,NodeList縺ｫ蜉�縺医※縲｀ap縺ｫnodeList繧呈�ｼ邏阪＠縲∫ｵゆｺ�縲�
			else if(researchFlag == false){
				seen.put(currentNode.toString(), true);
//				visitNodeList.add(currentNode.toString());
				pathListMap.put(index++, visitNodeList);
				//蛻晄悄蛹�
				newBranchFlag = false;
				index = 0;			
				//邨ゅｏ繧�
				return pathListMap;
			}
			
		}
			
		// 1.CurrentNode縺ｮ蟄蝉ｾ帙◆縺｡繧呈､懃ｴ｢ 
		List<Unit> children = graph.getSuccsOf(currentNode);

		// 2 nodeList縺ｫnode繧呈�ｼ邏阪☆繧九��
		visitNodeList.add(currentNode.toString());
		
		// �ｼ鍍odo縺ｫ蟄蝉ｾ帙◆縺｡繧貞�･繧後ｋ
		for (Unit child : children)
			todo.push(child);

		// 04 - 05
		methodNo4(graph, tail, todo, seen, visitNodeList,
				pathListMap, strUnitMap);

		//蛻晄悄蛹�
		index = 0;
		newBranchFlag = false;
		
		return pathListMap;
	}

	/**
	 * 04縺九ｉ縺ｮ郢ｰ繧願ｿ斐＠逕ｨ
	 * @return
	 */
	public static Map<Integer, List<String>> methodNo4(UnitGraph graph,
			Unit tail, Deque<Unit> todo, Map<String, Boolean> seen,
			List<String> visitNodeList, Map<Integer, List<String>> pathListMap,
			Map<String, Unit> strUnitMap) {
		// �ｼ杯odo縺九ｉ蟄蝉ｾ帙ｒ荳�莠ｺ蜿悶ｊ蜃ｺ縺�
		Unit child = todo.poll();

		// �ｼ募叙繧雁�ｺ縺励◆蟄蝉ｾ帙′seen縺ｮ荳ｭ縺ｧtrue縺ｫ縺ｪ縺｣縺ｦ縺ｪ縺�縺玖ｪｿ縺ｹ繧�
		if(child != null)
		if (seen.get(child.toString()) == true & newBranchFlag == false) {
			// �ｼ包ｼ趣ｼ奏rue縺�縺｣縺溘ｉ�ｼ斐↓謌ｻ縺｣縺ｦ蛻･縺ｪ蟄蝉ｾ帙ｒ蜿悶ｊ蜃ｺ縺�
			methodNo4(graph, tail, todo, seen, visitNodeList,
					pathListMap, strUnitMap);
		} 
		// �ｼ包ｼ趣ｼ断alse縺�縺｣縺溘ｉ
		// 5.2.1.todo縺九ｉ蜿悶ｊ蜃ｺ縺励◆繝弱�ｼ繝峨ｒ謖�螳壹＠縺ｦ
		Unit nextNode = child;
		
		// 5.2.1.2. 遨ｺ縺ｧ縺ｪ縺�縺ｪ繧丑extNode繧貞ｼ墓焚謖�螳壹＠縺ｦ�ｼ舌↓謌ｻ繧九��
		if(nextNode != null)
			pathListMap = searchPath(graph, nextNode, tail, todo, seen, visitNodeList,
				pathListMap, strUnitMap);
		
		return pathListMap;
	}

}