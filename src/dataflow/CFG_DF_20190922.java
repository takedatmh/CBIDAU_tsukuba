package dataflow;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayDeque;
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
import soot.jimple.toolkits.callgraph.CHATransformer;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.UnitGraph;
import soot.util.dot.DotGraph;
import soot.util.queue.QueueReader;
import dataflow.tutorial.GuaranteedDefs;
import dataflow.util.Utility4Soot;

/**
 * In case of existing statements more than one branch and one external call
 * method.
 * 
 * @author takedatmh
 *
 */
public class CFG_DF_20190922 {

	// Method Name
	private static String methodName = "read";
	private static String mainClass = "simple.client.Client";
	private static String targetClass = "simple.logic.Logic";

	// CRUD Map
	private static Map<String, String> crudMap = new LinkedHashMap<String, String>();

	// Data Flow LinkedHashMap
	private static LinkedHashMap<String, String> dfMap = new LinkedHashMap<String, String>();

	// Data Flaw Value LinkedHashMap
	private static LinkedHashMap<String, String> dfVMap = new LinkedHashMap<String, String>();

	// Key value for PathMap
	static Integer index = 0;
	
	//newBranch
	static boolean newBranchFlag = false;
	
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
	 * SerializePath
	 * @param resultPathMap
	 * @throws FileNotFoundException
	 */
	public static void SerializePath(Map<Integer, List<String>> resultPathMap) throws FileNotFoundException{
		if(resultPathMap == null)
			return;
		
		// Create GFG Node List file for iGraph.
		PrintWriter pwPath = new PrintWriter("CFG_igraph_Path_" + methodName + "_" + ".csv");
		
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
		PrintWriter pwPathEdge = new PrintWriter("CFG_igraph_Path_Edge_" + methodName + "_" + ".csv");
		
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
System.out.println(startEndList);
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
						// search body
						List<ValueBox> list = body.getUseBoxes();
						
System.out.println("########Box########");
for (ValueBox box : list) {
	System.out.println("Value = " + box.getValue()
			+ "," + "Tag = " + box.getTag("jtp"));
}

						// Create Control flow Graph as UnitGraph
						UnitGraph unitGraph = new ExceptionalUnitGraph(body);  // UnitGraph unitGraph = new EnhancedUnitGraph(body);
						//Get unit from UnitGraph.
						Iterator<Unit> units = unitGraph.iterator();
						while (units.hasNext()) {
							Unit u = units.next();
							//Get soot value object from Unit.
							Iterator<ValueBox> iValueBox = u.getUseBoxes()
									.iterator();
							int counter = 0;
							while (iValueBox.hasNext()) {
								ValueBox valueBox = iValueBox.next();
								Value v = valueBox.getValue();

/* Check each value statement. */
System.out.println(counter+ "############Check satement or Expr###############");

								// Check Expressions of each soot value and detect CRUD information from each soot value.
								crudMap = Utility4Soot.create_crudMap(crudMap, u, v);

								/*
								 * Complement crudMap creation:: detect only refer variable statement and update crudMap key information as R.
								 * Check current Unit object's description like a "r12" by matcher.
								 */
								crudMap = Utility4Soot.complement_crudMap(u, unitGraph, dfMap);

							}
						}

						// /////DFA//////////////////////
					    /*
					     * Data Flow Analysis using Soot default API. 
					     * In this project, we have implemented as GuaranteeDefs class.
					     * This class getGuaranteedDefs() method returns each live data list.
					     * Following disposal is to get data flow information and put them into dfMap each statement.
					     */
						dfMap = Utility4Soot.create_dfMap(unitGraph, methodName, dfMap);

						// /////CG///////////////////////
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
						
System.out.println("serializeCallGraph completed for CFG.");

// /////SYSOUT CRUD Map//////////////
System.out.println("##############CRUD##################");
for (String key : crudMap.keySet()) {
	System.out.println(crudMap.get(key) + "	" + key);
}

// //////SYSOUT df Value Map////////
System.out.println("##############dfVMap##################");
for (String key : dfVMap.keySet()) {
	System.out.println(dfVMap.get(key) + "	" + key);
}

						////////Path Analysis////////////////////////////////////////////////////////////////////
						analyzePath(unitGraph);

					}

				}));
		
		/* Execute Soot main method with your designated argument values */
		soot.Main.main(args2);

	}
	
	/**
	 * analyzePath
	 * 
	 *  Retrieve all CFG path from top to tail.
	 *  In this method, defined top node, tail node, seen list, todo stack and visit node list.
	 *  And then, we call searchPath method implemented all available paths search algorithm by DFS in this method.
	 *  Moreover, searched node and edge information are written as csv file respective.
	 * @param unitGraph
	 */
	public static void analyzePath(UnitGraph unitGraph) {
	
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
		
		//If  this graph is not multiple termination graph.
		if(Utility4Soot.multiEndGraphFlag(tails) == true) {
			//TODO: Rebuild Control Flow graph as single tail graph.
			unitGraph = Utility4Soot.rebuildCFG4MultiTailCFG(unitGraph);
		}

		//Search Paths on the UnitGraph
		Map<Integer, List<String>> resultPathMap = searchPath(unitGraph, top, tails.get(0), todo, seen, visitNodeList, pathListMap, strUnitMap);
		
for (Integer key : resultPathMap.keySet()) {
System.out.println("=_=; " + resultPathMap.get(key));
}
		
		//Create Path and Edge.
		try {
			//PathFile
			SerializePath(resultPathMap);
			//PathEdgeFile
			SerializePathEdge(resultPathMap, unitGraph);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

	}

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
		 * １currentNodeに対してseenの該当Nodeをtrueにする 0.引数からnodeを取り出し終端ノードでないことを確認する。
		 * 　0.1.終端ノード & todoにtrueでないnodeがある場合：終端ノードをvisitNodeListに加えて、MapにvisitNodeListを格納
		 *    0.1.1.visitNodeListの最終インデックスから左にインデックスを下げて行きながらseenの中でfalseになっているノードをchildに持つノードを逆探索する 　
		 *    0.1.2.逆探索してseen falseのノードを子供として持つノードをcurrentノードとして引数指定で４に戻る。
		 *           その際、visitNodeListをコピーして、開始インデックスからvisitNodeListを上書きして行く。 　
		 * 0.2.終端ノード & todoがすべてtrueの場合：終端ノードに対してsennをtrue, NodeListに加えて、MapにnodeListを格納し、終了。
		 * 01.CurrentNodeの子供たちを検索 
		 * ２visitNodeListにnodeを格納する。 
		 * ３todoに子供たちを入れる。
		 * ４todoから子供を一人取り出して、todoから消す。 
		 * ５取り出した子供がseenの中でtrueになってないか調べる
		 * 　 5.1.   trueだったら４に戻って別な子供を取り出す 
		 *     5.1.1 こどもがいなくなったら終わり 　
		 *     5.2.   falseだったら
		 *     5.2.1.todoから取り出したノードを指定して０1に戻る
		 */
		// １nodeに対してseenのNodeをtrueにする
		seen.put(currentNode.toString(), true);
		
		if(currentNode.equals(tail)){
			// seenの中でtrueでないnodeがあるかないかのresearchFlagの取得処理
			// todoに残っていて且つseenの中でfalseのノードを格納する変数restartNode
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
				
			// 0.1 終端ノード & todoにseenのなかでtrueでないnodeがある場合：
			if (researchFlag == true) {
				//終端ノードをseenでtrueに変更
				//seen.put(currentNode.toString(), true);
				// 終端ノードをvisitNodeListに加え
				visitNodeList.add(currentNode.toString());
				//MapにvisitNodeListを格納
				pathListMap.put(index++, visitNodeList);
				
				// 0.1.1.visitNodeListの最終インデックスから左にインデックスを下げて行きながら、seenの中でfalseになっているノード(restartNode)をchildに持つノードを逆探索する
				int indexVNL = visitNodeList.size();
				//for (int i = indexVNL; i < 0; i--) {
				for(int i = 0; i < indexVNL; i++){
					String node = visitNodeList.get(indexVNL - 1 - i);
					Unit unit = strUnitMap.get(node);
					List<Unit> childUnits = graph.getSuccsOf(unit);
					// seenの中でfalseになっているノード(restartNode)をchildに持つノードを発見
					if (childUnits != null && childUnits.contains(strUnitMap.get(restartNode))) {
						// 0.1.2.逆探索してseen
						// falseのノードを子供として持つノードをcurrentノードとして引数指定で４に戻る。
						// その際、visitNodeListをコピーして、開始インデックスからvisitNodeListを上書きして行く。
						List<String> tmpVisitNodeList = new LinkedList<String>();
						for (int x = 0; x < indexVNL - i; x++) {
							tmpVisitNodeList.add(x, visitNodeList.get(x));
						}
						//CurrentNodeを新しいvisitNodeListの最終ノードに変更する。
						currentNode = strUnitMap.get(tmpVisitNodeList.get(tmpVisitNodeList.size() - 1));
						//NewBranch Flag を　true
						newBranchFlag = true;
						// 04から実行
						methodNo4(graph, tail, todo, seen,
								tmpVisitNodeList, pathListMap, strUnitMap);
					} 
				}
			}
			//0.2.終端ノード & todoがすべてtrueの場合：終端ノードに対してsennをtrue,NodeListに加えて、MapにnodeListを格納し、終了。
			else if(researchFlag == false){
				//seen.put(currentNode.toString(), true);
				visitNodeList.add(currentNode.toString());
				pathListMap.put(index++, visitNodeList);
				//初期化
				newBranchFlag = false;
				index = 0;			
				//終わり
				return pathListMap;
			}
			
		}
			
		// 1.CurrentNodeの子供たちを検索 
		List<Unit> children = graph.getSuccsOf(currentNode);
		
		//1.1. In case that Child Node  is null and Current Node is not tail Node, finish this disposal.
		if(children == null && !currentNode.equals(tail)) {
			return pathListMap;
		}

		// 2 nodeListにnodeを格納する。
		visitNodeList.add(currentNode.toString());
		
		// ３todoに子供たちを入れる
		for (Unit child : children)
			todo.push(child);

		// 04 - 05
		methodNo4(graph, tail, todo, seen, visitNodeList,
				pathListMap, strUnitMap);

		//初期化
		//index = 0;
		//newBranchFlag = false;
		
		return pathListMap;
	}

	/**
	 * 04からの繰り返し用
	 * @return Map
	 */
	public static Map<Integer, List<String>> methodNo4(UnitGraph graph,
			Unit tail, Deque<Unit> todo, Map<String, Boolean> seen,
			List<String> visitNodeList, Map<Integer, List<String>> pathListMap,
			Map<String, Unit> strUnitMap) {
		// ４todoから子供を一人取り出す
		if(todo.equals(null))
			return pathListMap;
		Unit child = todo.poll();
		//Unit child = todo.pop();

		// ５取り出した子供がseenの中でtrueになってないか調べる when child node is not a tail node.
		if(child != null ){
			if (seen.get(child.toString()) == true) {
				// ５．１trueだったら４に戻って別な子供を取り出す
				methodNo4(graph, tail, todo, seen, visitNodeList,
						pathListMap, strUnitMap);
			}
		//When the current node does not have any child, return.
		} else {
			return pathListMap;
		}
		// ５．２falseだったら
		// 5.2.1.todoから取り出したノードを指定して
		Unit nextNode = child;
		
		// 5.2.1.2. 空でないならNextNodeを引数指定して０に戻る。
		if(nextNode != null)
			pathListMap = searchPath(graph, nextNode, tail, todo, seen, visitNodeList,
				pathListMap, strUnitMap);
		
		return pathListMap;
	}

}