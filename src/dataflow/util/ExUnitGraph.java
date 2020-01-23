package dataflow.util;

import java.util.ArrayList;
import java.util.List;

import soot.Body;
import soot.Unit;
import soot.toolkits.graph.UnitGraph;

public class ExUnitGraph extends UnitGraph{
	
	UnitGraph orgUnitGraph = null;
	
	Unit newTailUnit = null; 

	public ExUnitGraph(Body body) {
		super(body);
		// TODO Auto-generated constructor stub
	}
	
	public void addNewTailUnit()  {
		
		tails.add(newTailUnit);
		
		unitToPreds.put(newTailUnit, tails);
		
		List<Unit> newTailUnitList = new ArrayList<Unit>();
		newTailUnitList.add(newTailUnit);
		for(Unit tail : tails)
			unitToSuccs.put(tail, newTailUnitList);
		
	}
	
	/**
	 * Setter for new Tail Unit node.
	 * @param newTailUnit
	 */
	public void setUnit(Unit newTailUnit) {
			this.newTailUnit = newTailUnit;
	}

}

//class ExBody extends Body {
//
//	@Override
//	public Object clone() {
//		// TODO Auto-generated method stub
//		return null;
//	}
//	
//	
//}
