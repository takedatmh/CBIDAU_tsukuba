package dataflow.util;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dataflow.tutorial.GuaranteedDefs;
import soot.Body;
import soot.Unit;
import soot.Value;
import soot.javaToJimple.JimpleBodyBuilder;
import soot.javaToJimple.JimpleBodyBuilderFactory;
import soot.jimple.AnyNewExpr;
import soot.jimple.AssignStmt;
import soot.jimple.BreakpointStmt;
import soot.jimple.GotoStmt;
import soot.jimple.IdentityStmt;
import soot.jimple.IfStmt;
import soot.jimple.InvokeStmt;
import soot.jimple.JimpleBody;
import soot.jimple.MonitorStmt;
import soot.jimple.NewArrayExpr;
import soot.jimple.NewExpr;
import soot.jimple.NewMultiArrayExpr;
import soot.jimple.NopStmt;
import soot.jimple.NullConstant;
import soot.jimple.ReturnStmt;
import soot.jimple.StmtBody;
import soot.jimple.ThrowStmt;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.UnitGraph;

public class Utility4Soot {

	/**
	 * setMainArgs Sets Soot main method following arguments values: - Java main
	 * argument from main method arguments value. - Main Class - Target Class
	 * 
	 * @param args
	 * @param mainClass
	 * @param targetClass
	 * @return String[] arguments values as String array.
	 * */
	public static String[] setMainArgs(String[] args, String mainClass,
			String targetClass) {
		// Setter for main argument
		List<String> argsList = new ArrayList<String>(Arrays.asList(args));
		argsList.addAll(Arrays.asList(new String[] { "-w", "-main-class",
				mainClass, mainClass, targetClass }));
		String[] args2 = new String[argsList.size()];
		args2 = argsList.toArray(args2);

		return args2;
	}

	/**
	 * create_crudMap
	 * 
	 * @param crudMap
	 * @param u
	 * @param v
	 * @return crudMap
	 */
	public static Map<String, String> create_crudMap(
			Map<String, String> crudMap, Unit u, Value v) {

		// Check Expr of each statements and detect CRUD information.
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

		return crudMap;
	}

	/**
	 * complement_crudMap
	 * In case of only variable reference jimple statement, we can't detect CRUD information based on Jimple Expression.
	 * Therefore, we retrieve the specific description when the target statement only refer to variable like "(r01)" from unit description.
	 * If we find out these description in the statement, we update crudMap's key as R.
	 * @param u
	 * @param unitGraph
	 * @param crudMap
	 * @return updated crudMap
	 */
	public static Map<String, String> complement_crudMap(Unit u, UnitGraph unitGraph,
			Map<String, String> crudMap) {

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
				// System.out
				// .printf("[%s] がマッチしました。 Pattern:[%s] input:[%s]\n",
				// matched, pattern1,
				// u.toString());
				tmp = "$" + matched;
				break;
			}
			Pattern pattern2 = Pattern.compile("r[0-9]{1,}");
			Matcher matcher2 = pattern2.matcher(u.toString());
			while (matcher2.find()) {
				String matched = matcher2.group();
// System.out
// .printf("2:"
// + "[%s] がマッチしました。 Pattern:[%s] input:[%s]\n",
// matched, pattern1,
// u.toString());
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

		return crudMap;
	}
	
	/**
	 * Data Flow Analysis using Soot default API. 
	 * In this project, we have implemented as GuaranteeDefs class.
     * This class getGuaranteedDefs() method returns each live data list.
     * Following disposal is to get data flow information and put them into dfMap each statement.
	 * @param unitGraph
	 * @param methodName
	 * @param dfMap
	 * @return dfMap
	 */
public static LinkedHashMap<String, String> create_dfMap(UnitGraph unitGraph, String methodName, LinkedHashMap<String, String> dfMap) {
		
		GuaranteedDefs gf = new GuaranteedDefs(unitGraph);
		Iterator<Unit> units2 = unitGraph.iterator();
		PrintWriter pw = null;
		try {
			pw = new PrintWriter(".\\igraph\\"+"DF" + "_" + "Simple_" + methodName + ".txt");
			int count = 1;
			while (units2.hasNext()) {
				Unit u = units2.next();
				List<?> dfList = gf.getGuaranteedDefs(u);
				System.out
						.println(count
								+ " : ####################DataFlow########################");
				System.out.println(dfList + "	" + u.toString());
				
				//Write DF data on a csv file.
				pw.println(dfList + "	" + u.toString());

				// put DF info into dfMap<Statement, DF variable
				// List>
				dfMap.put(u.toString(), dfList.toString());

				count++;
			}
		//PrintWriter close
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			pw.close();
		}
		
		return dfMap;
	}


	public static boolean multiEndGraphFlag(List<Unit> tails)  {
		
		boolean multiTailFlag = false;
		
		if (tails.size() > 1)
			multiTailFlag = true;
		
		return multiTailFlag;
	}
	
	public static UnitGraph rebuildCFG4MultiTailCFG(UnitGraph u) {
		 
		ExceptionalUnitGraph eu = (ExceptionalUnitGraph)u;
		
		
		 List<Unit> tails = u.getTails();
		 
		 Unit newTail = (Unit)tails.get(0).clone();
		 
		 Body body = u.getBody();
		 body.getMethod();
		 
		 
		 for(Unit tail : tails)  {
			 tail.clone();
		 }
		
		return null;
	}

	
	
	
	
	
	
	

}



