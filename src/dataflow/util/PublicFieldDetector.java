package dataflow.util;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import soot.Body;
import soot.PackManager;
import soot.Scene;
import soot.SceneTransformer;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Transform;
import soot.Unit;
import soot.ValueBox;
import soot.jimple.toolkits.callgraph.CHATransformer;
import soot.tagkit.Tag;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.util.Chain;

/**
 * This class has a function to detect Public Field from designated classes' field.
 * @author takeda
 * 
 */
public class PublicFieldDetector {
	
	// Method Name
	public static String methodName = null;
	public static String mainClass = null;
	public static String targetClass = null;

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
		
		//Obtain soot main arguments from system properties.
		methodName = System.getProperty("method");
		mainClass = System.getProperty("main");
		targetClass = System.getProperty("target");

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

						// Get a Soot Class of target class. :
						SootClass sootClass = Scene.v().getSootClass(targetClass);
						// Analyze class structure.
						sootClass.setApplicationClass();
						// method.
						SootMethod method = sootClass.getMethodByName(methodName);
						// Body has same jimple code to SootMethod's activeBody.
						Body body = method.retrieveActiveBody();

						/* 
						 * Create Control flow Graph as UnitGraph.
						 * 'unitGraph' is an instance created by ExceptionalUnitGraph which includes Exceptional relation.
						 * 'briefUnitGraph' is an instance created by BriefUnitGraph which dose not include Exceptional relation.
						 * */
						ExceptionalUnitGraph unitGraph = new ExceptionalUnitGraph(body);  // UnitGraph unitGraph = new EnhancedUnitGraph(body);
						//briefUnitGraph = new BriefUnitGraph(body);

						//Unit
						Unit u = null;
						
						//Get unit from UnitGraph.
						Iterator<Unit> units = unitGraph.iterator();
						while (units.hasNext()) {
							u = units.next();
							System.out.println("Unit: " + u.toString());
							System.out.println("getClass(): "+u.getClass());
							System.out.println("getBoxesPointingToThis: "+u.getBoxesPointingToThis());
							System.out.println("getDefBoxes(): "+u.getDefBoxes());
							System.out.println("getTags(): "+ u.getTags().toString());
							System.out.println("getUnitBoxes(): "+ u.getUnitBoxes());
							System.out.println("getUseAndDefBoxes(): "+ u.getUseAndDefBoxes());
							System.out.println("getUseBoxes(): "+ u.getUseBoxes());
							
							//Get soot value object from Unit.
							Iterator<ValueBox> iUseBox = u.getUseBoxes().iterator();
							
							while (iUseBox.hasNext()) {
								ValueBox valueBox = iUseBox.next();
								System.out.println("valueBox: " + valueBox.toString());
								
							}
						}
												

						/* Field Analysis */
						Chain<SootField> sootFields = sootClass.getFields();
						Iterator<SootField> iteratorFields = sootFields.iterator();
						List<SootField> publicFieldList = new ArrayList<SootField>();
						while (iteratorFields.hasNext()) {
							SootField field = iteratorFields.next();
							//* This "isPublic" method judges public modification field or not. *//
							if (field.isPublic() == true) {
								publicFieldList.add(field);
							}
						}
						// Write the list of public field on the public field list file.
						FileWriter in = null;
						PrintWriter staticFieldWriter = null;
						String path = "PublicFieldList" + Context.SEPARATOR 
								+ "ListOfPublicField" + ".txt";
						try {
							// postscript version
							in = new FileWriter(path, true);
							staticFieldWriter = new PrintWriter(in);
							staticFieldWriter.println(publicFieldList.toString());
							staticFieldWriter.flush();
						} catch (Exception e) {
							e.printStackTrace();
						} finally {
							try {
								// close
								in.close();
								staticFieldWriter.close();
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					}
				}));

		/* Execute Soot main method with your designated argument values */
		soot.Main.main(args2);

	}

}
