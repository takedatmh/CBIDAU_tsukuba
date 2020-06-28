package dataflow.util;

public class Dot2iGrapConverter {
	
	/**
	 * We store CG graph data as dot file format for graphviz.
	 * But, in our study, we use igraph for R when we analyze any graph topology and graph mining.
	 * Therefore, this method has a function to convert igraph data.
	 * 
	 * <format> csv
	 * |start node|end node|
	 * 
	 * @param String: Call Graph Dot file path 
	 * @return boolean : whether this convert deal is successful or not?
	 */
	public boolean convertDot2iGraph(String dotFilePath){
		boolean ret = false;
		
		//Read dot file.
		
		
		//Create column definition at the top of this file description.
		
		//Read line.
		
		//Ignore first line of dot file.
		
		//Split recode data by "->" and add start and end node data into a list.
				
		//Write csv file like "start" and "end".
		
		return ret;
	}

}
