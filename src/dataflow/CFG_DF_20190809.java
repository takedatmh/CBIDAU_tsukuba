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
import java.util.Set;
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
import soot.toolkits.graph.BriefUnitGraph;
import soot.toolkits.graph.CompleteBlockGraph;
import soot.toolkits.graph.CompleteUnitGraph;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.graph.pdg.EnhancedUnitGraph;
import soot.util.dot.DotGraph;
import soot.util.queue.QueueReader;
import dataflow.tutorial.GuaranteedDefs;
import dataflow.tutorial.GuaranteedDefsAnalysis;

/**
 * In case of existing statements more than one branch and one external call
 * method.
 * 
 * @author takedatmh
 *
 */
public class CFG_DF_20190809 {

	// Method Name
	private static String methodName = "create";

	// CRUD Map
	private static Map<String, String> crudMap = new LinkedHashMap<String, String>();

	// Data Flow LinkedHashMap
	private static LinkedHashMap<String, String> dfMap = new LinkedHashMap<String, String>();

	// Data Flaw Value LinkedHashMap
	private static LinkedHashMap<String, String> dfVMap = new LinkedHashMap<String, String>();

	/**
	 * Control Flow Graph Creation
	 * 
	 * @param graph
	 * @param fileName
	 * @throws FileNotFoundException
	 */
	@SuppressWarnings("resource")
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

		System.out.println("CFG file name " + fileName);

		// Create GFG Edge List file for iGraph.
		PrintWriter pwEdge = new PrintWriter("CFG_igraph_Edge_" + methodName
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
		System.out.println(graph.getHeads().get(0));
		System.out.println(graph.getTails().get(0));
		List<Unit> unitPathE2E = graph.getExtendedBasicBlockPathBetween(graph
				.getHeads().get(0), graph.getTails().get(0));
		System.out.println(unitPathE2E);

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
			fileName = fileName + "call-graph" + DotGraph.DOT_EXTENSION;
		}
		System.out.println("file name " + fileName);
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
	 * Main Method.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		// .sfinal List<String> workflowsNames = new ArrayList<String>();

		List<String> argsList = new ArrayList<String>(Arrays.asList(args));
		argsList.addAll(Arrays.asList(new String[] { "-w", "-main-class",
				"simple.client.Client", "simple.client.Client",
				"simple.logic.Logic" }));
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
						SootClass a = Scene.v().getSootClass(
								"simple.logic.Logic");

						// Analyze class structure.//
						// get Method
						a.setApplicationClass();
						// SootMethod has a data of jimple code of designated
						// method.
						SootMethod method = a.getMethodByName(methodName);
						// Body has same jimple code to SootMethod's activeBody.
						Body body = method.retrieveActiveBody();
						// search body
						List<ValueBox> list = body.getUseBoxes();
						System.out.println("########Box########");
						for (ValueBox box : list) {
							System.out.println("Value = " + box.getValue()
									+ "," + "Tag = " + box.getTag("jtp"));
						}

						// Control flow Graph by UnitGraph
						UnitGraph unitGraph = new ExceptionalUnitGraph(body);
						// UnitGraph unitGraph = new EnhancedUnitGraph(body);
						Iterator<Unit> units = unitGraph.iterator();
						while (units.hasNext()) {
							Unit u = units.next();
							Iterator<ValueBox> iValueBox = u.getUseBoxes()
									.iterator();
							int counter = 0;
							while (iValueBox.hasNext()) {
								ValueBox valueBox = iValueBox.next();
								Value v = valueBox.getValue();

								/* Check each value statement. */
								System.out
										.println(counter
												+ "############Check satement or Expr###############");
								// Treat each detail of Expr disposal for
								// detection of new as create and delete as
								// delete.)
								if (v instanceof NewExpr
										|| v instanceof NewArrayExpr
										|| v instanceof NewMultiArrayExpr
										|| v instanceof AnyNewExpr) {
									System.out.println(u.toString() + ":::"
											+ "NewExpr");
									crudMap.put(u.toString(), "C");
								} else if (u instanceof NullConstant
										|| u.toString().contains(
												"nullConstant ")
										|| u.toString().contains("delete ")
										|| u.toString().contains("Delete ")) {
									System.out.println("Null Costant");
									crudMap.put(u.toString(), "D");
									// Treat each Statement.
								} else if (u instanceof AssignStmt) {
									System.out.println(u.toString() + ":::"
											+ "AssignStmt");
									crudMap.put(u.toString(), "U");
								} else if (u instanceof IdentityStmt) {
									System.out.println(u.toString() + ":::"
											+ "IdentityStmt");
									crudMap.put(u.toString(), "R");
								} else if (u instanceof GotoStmt) {
									System.out.println(u.toString() + ":::"
											+ "GotoStmt");
									crudMap.put(u.toString(), "R");
								} else if (u instanceof IfStmt) {
									System.out.println(u.toString() + ":::"
											+ "IfStmt");
									crudMap.put(u.toString(), "R");
								} else if (u instanceof InvokeStmt) {
									System.out.println(u.toString() + ":::"
											+ "InvokeStmt");
									crudMap.put(u.toString(), "R");
								} else if (u instanceof MonitorStmt) {
									System.out.println(u.toString() + ":::"
											+ "MonitorStmt");
									crudMap.put(u.toString(), "R");
								} else if (u instanceof ReturnStmt) {
									System.out.println(u.toString() + ":::"
											+ "ReturnStmt");
									crudMap.put(u.toString(), "R");
								} else if (u instanceof ThrowStmt) {
									System.out.println(u.toString() + ":::"
											+ "ThrowStmt");
									crudMap.put(u.toString(), "R");
								} else if (u instanceof BreakpointStmt) {
									System.out.println(u.toString() + ":::"
											+ "BreakpointStmt");
									crudMap.put(u.toString(), "R");
								} else if (u instanceof NopStmt) {
									System.out.println(u.toString() + ":::"
											+ "NopStmt");
									crudMap.put(u.toString(), "R");
									// Any other cases are fallen into R.
								} else {
									System.out.println("other Read cases.");
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
											.compile("¥¥$r[0-9]{1,}");
									Matcher matcher1 = pattern1.matcher(u
											.toString());
									while (matcher1.find()) {
										String matched = matcher1.group();
										System.out
												.printf("[%s] がマッチしました。 Pattern:[%s] input:[%s]\n",
														matched, pattern1,
														u.toString());
										tmp = "$" + matched;
										break;
									}
									Pattern pattern2 = Pattern
											.compile("r[0-9]{1,}");
									Matcher matcher2 = pattern2.matcher(u
											.toString());
									while (matcher2.find()) {
										String matched = matcher2.group();
										System.out
												.printf("2:"
														+ "[%s] がマッチしました。 Pattern:[%s] input:[%s]\n",
														matched, pattern1,
														u.toString());
										tmp = "$" + matched;
										break;
									}
								}

								// Put these valuses into dfVMap.
								dfVMap.put(u.toString(), tmp);

								System.out
										.println("//////////////////////////////////////"
												+ u.getDefBoxes().toString());
								System.out
										.println("//////////////////////////////////////"
												+ sindex);
								System.out
										.println("//////////////////////////////////////"
												+ tmp);
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
							pw = new PrintWriter("DF" + "_" + "Simple_"
									+ methodName + ".txt");
							int count = 1;
							while (units2.hasNext()) {
								Unit u = units2.next();
								List<?> dfList = gf.getGuaranteedDefs(u);
								System.out
										.println(count
												+ " : ####################DataFlow########################");
								System.out.println(dfList + "	" + u.toString());
								pw.println(dfList + "	" + u.toString());

								// put DF info into dfMap<Statement, DF variable
								// List>
								dfMap.put(u.toString(), dfList.toString());

								count++;
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
						// SootMethod src =
						// Scene.v().getMainClass().getMethodByName("main");
						CallGraph cg = Scene.v().getCallGraph();
						SerializeCallGraph(cg, "CG_Simple2"
								+ DotGraph.DOT_EXTENSION);

						// /////Draw CGF/////////////////////
						try {
							SerializeControlFlowGraph(unitGraph, "CFG_Simple_"
									+ methodName + DotGraph.DOT_EXTENSION);
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

						// //////Path Analysis/////////////
						Map<Integer, List<String>> pathListMap = searchPath(unitGraph);
						for (Integer key : pathListMap.keySet()) {
							System.out.println("=_=; " + pathListMap.get(key));
						}

					}

				}));

		args = argsList.toArray(new String[0]);
		soot.Main.main(args2);
	}

	// ////////////////////////////////////////////////////

	public static Map<Integer, List<String>> searchPath(UnitGraph graph) {

		// /////////////////////////
		// seen
		Map<String, Boolean> seen = new LinkedHashMap<String, Boolean>();
		List<String> vList = new LinkedList<String>();
		for (String key : crudMap.keySet()) {
			seen.put(key, false);
		}

		// Map<StringNode, Unit>
		Map<String, Unit> strUnitMap = new LinkedHashMap<String, Unit>();
		Iterator<Unit> unit = graph.iterator();
		while (unit.hasNext()) {
			Unit node = unit.next();
			strUnitMap.put(node.toString(), node);
		}

		// stack
		Deque<Unit> todo = new ArrayDeque<Unit>();

		// PathListMap
		Map<Integer, List<String>> pathListMap = new LinkedHashMap<Integer, List<String>>();

		// Get Head and Tails.
		Unit top = graph.getHeads().get(0);
		List<Unit> tails = graph.getTails();

		// VistNodeList
		List<String> visitNodeList = new LinkedList<String>();

		// VisitIndex
		int visitIndex = 0;

		// Check whether this graph has multiple end nodes or not.
		boolean multiTailFlag = false;
		if (tails.size() > 1)
			multiTailFlag = true;

		if (multiTailFlag != true) {
			if (graph.getSuccsOf(top) != null) {
				List<Unit> children = graph.getSuccsOf(top);
			}
		}

		// search
		Map<Integer, List<String>> map = searchPath(graph, top, tails.get(0),
				todo, seen, visitNodeList, pathListMap, strUnitMap, index);
		// System.out.println(map);

		// return PathListList
		return map;
	}

	// PathMapのキー用
	static Integer index = 0;

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
			Map<String, Unit> strUnitMap, Integer index) {
		/*
		 * １currentNodeに対してseenの該当Nodeをtrueにする 0.引数からnodeを取り出し終端ノードでないことを確認する。
		 * 　0.1.終端ノード & todoにtrueでないnodeがある場合：終端ノードをvisitNodeListに加えて、MapにvisitNodeListを格納
		 *    0.1.1.visitNodeListの最終インデックスから左にインデックスを下げて行きながらseenの中でfalseになっているノードをchildに持つノードを逆探索する 　
		 *    0.1.2.逆探索してseen falseのノードを子供として持つノードをcurrentノードとして引数指定で４に戻る。
		 *           その際、visitNodeListをコピーして、開始インデックスからvisitNodeListを上書きして行く。 　
		 * 0.2.終端ノード & todoがすべてtrueの場合：終端ノードに対してsennをtrue, NodeListに加えて、MapにnodeListを格納し、終了。
		 * 01.CurrentNodeの子供たちを検索 ２visitNodeListにnodeを格納する。 
		 * ３todoに子供たちを入れる。
		 * ４todoから子供を一人取り出して、todoから消す。 
		 * ５取り出した子供がseenの中でtrueになってないか調べる
		 * 　５．１trueだったら４に戻って別な子供を取り出す 
		 *    ５．１．１こどもがいなくなったら終わり 　
		 *   ５．２falseだったら
		 *     5.2.1.todoから取り出したノードを指定して０1に戻る
		 */
		// １nodeに対してseenのNodeをtrueにする
		seen.put(currentNode.toString(), true);

		// 0.引数からcrrentNodeを取り出し終端ノードでないことを確認する。
		if (currentNode.equals(tail)) {
			// 0.1 終端ノード & todoにseenのなかでtrueでないnodeがある場合：
			// todoに残っていて且つseenの中でfalseのノード
			String restartNode = null;
			// todo(deque)から残りを一個づつ取り出して、
			Iterator<Unit> todoUnits = todo.iterator();
			boolean researchFlag = false;
			// todoの中のノードを取り出して、seenでfalseのものが見つかったresearchFlagがtrueになるまでループ
			Loop1: while (todoUnits.hasNext() & researchFlag == false) {
				Unit todoUnit = todoUnits.next();
				// seenにfalseとして登録されているか否かチェック
				for (String key : seen.keySet()) {
					// 終端ノード & todoにseenのなかでtrueでないnodeがある場合　の条件判定
					if (key.equals(todoUnit.toString())
							& seen.get(key) == false) {
						researchFlag = true;
						restartNode = key;
						break Loop1;
					}
				}
			}
			// 終端ノードをvisitNodeListに加えて、MapにvisitNodeListを格納
			seen.put(currentNode.toString(), true);
			visitNodeList.add(currentNode.toString());
			pathListMap.put(index++, visitNodeList);

			// 0.2.終端ノード & todoがすべてtrueの場合：終端ノードに対してsennをtrue,
			// NodeListに加えて、MapにnodeListを格納し、終了。
			if (researchFlag == false)
				return pathListMap;

			// 0.1.1.visitNodeListの最終インデックスから左にインデックスを下げて行きながら、seenの中でfalseになっているノード(restartNode)をchildに持つノードを逆探索する
			int indexVNL = visitNodeList.size();
			for (int i = indexVNL; i < 0; i--) {
				String node = visitNodeList.get(i);
				Unit unit = strUnitMap.get(node);
				List<Unit> childUnits = graph.getSuccsOf(unit);
				// seenの中でfalseになっているノード(restartNode)をchildに持つノードを発見
				if (childUnits.contains(strUnitMap.get(restartNode))) {
					// 0.1.2.逆探索してseen
					// falseのノードを子供として持つノードをcurrentノードとして引数指定で４に戻る。
					// -
					// その際、visitNodeListをコピーして、開始インデックスからvisitNodeListを上書きして行く。
					List<String> tmpVisitNodeList = new LinkedList<String>();
					for (int x = 0; x < i; x++) {
						tmpVisitNodeList.add(x, visitNodeList.get(x));
					}
					// 04から実行
					pathListMap = methodNo4(graph, tail, todo, seen,
							visitNodeList, pathListMap, strUnitMap, index);
					return pathListMap;
				} else {
					// TODO:
				}
			}

		}
		// 01子供検索
		List<Unit> children = graph.getSuccsOf(currentNode);

		// 2 nodeListにnodeを格納する。
		visitNodeList.add(currentNode.toString());
		// ３todoに子供たちを入れる
		for (Unit child : children)
			todo.push(child);

		// //４todoから子供を一人取り出す
		// Unit child = todo.poll();
		//
		// //５取り出した子供がseenの中でtrueになってないか調べる
		// if(seen.get(child.toString()) == true){
		// //５．１trueだったら４に戻って別な子供を取り出す
		// }
		// //５．２falseだったら
		// //5.2.1.todoから取り出したノードを指定して
		// Unit nextNode = todo.poll();
		// //5.2.1.1. 空っぽならPathListMapに格納して終わり
		// if(nextNode == null){
		// pathListMap.put(index++, visitNodeList);
		// return pathListMap;
		// }
		//
		// //5.2.1.2. 空でないならNextNodeを引数指定して０に戻る。
		// searchPath(graph, nextNode, tail, todo, seen, visitNodeList,
		// pathListMap, strUnitMap, index);
		//
		// return null;

		// 04
		pathListMap = methodNo4(graph, tail, todo, seen, visitNodeList,
				pathListMap, strUnitMap, index);

		index = 0;
		return pathListMap;
	}

	/**
	 * 04からの繰り返し用
	 * 
	 * @return
	 */
	public static Map<Integer, List<String>> methodNo4(UnitGraph graph,
			Unit tail, Deque<Unit> todo, Map<String, Boolean> seen,
			List<String> visitNodeList, Map<Integer, List<String>> pathListMap,
			Map<String, Unit> strUnitMap, Integer intex) {
		// ４todoから子供を一人取り出す
		Unit child = todo.poll();

		// ５取り出した子供がseenの中でtrueになってないか調べる
		if (seen.get(child.toString()) == true) {
			// ５．１trueだったら４に戻って別な子供を取り出す
			pathListMap = methodNo4(graph, tail, todo, seen, visitNodeList,
					pathListMap, strUnitMap, index);
			return pathListMap;
		}
		// ５．２falseだったら
		// 5.2.1.todoから取り出したノードを指定して
		Unit nextNode = todo.poll();
		// 5.2.1.1. 空っぽならPathListMapに格納して終わり
		if (nextNode == null) {
			pathListMap.put(index++, visitNodeList);
			return pathListMap;
		}

		// 5.2.1.2. 空でないならNextNodeを引数指定して０に戻る。
		searchPath(graph, nextNode, tail, todo, seen, visitNodeList,
				pathListMap, strUnitMap, index);

		return null;
	}

}