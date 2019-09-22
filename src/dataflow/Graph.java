package dataflow;

import java.util.ArrayList;
import java.util.List;

import soot.Unit;
import soot.toolkits.graph.UnitGraph;

public class Graph {
	
	private UnitGraph unitGraph;
	
	private List<List<String>> listOfPath;

	public Graph(UnitGraph unitGraph) {
		this.unitGraph = unitGraph;
		this.listOfPath = new ArrayList<List<String>>();
	}
	
	/**
	 * Searh all Pathes
	 * @return
	 */
	public List<List<String>> detectListofPath(){
		return null;
	}
	
}


class Node {
	
	public String nodeName;
	
	public String crudInfo;
	
	public String leftSideData;
	
	public String leftDatas;
	
	public String liveDatas;
	
	public boolean visitFlag = false;
		
	public boolean childrenFlag = false;
	
	public int numOfChildren;
	
	public boolean hasChildren(UnitGraph unitGraph, Unit unit){
		boolean ret = false;
		
		List<Unit> children = unitGraph.getSuccsOf(unit);
		
		return ret;
	}
	
}


class Edge {
	
	public String currentNode;
	
	public String nextNode;
	
}
