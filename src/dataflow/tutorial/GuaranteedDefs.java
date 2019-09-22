package dataflow.tutorial;

import java.util.*;

import soot.*;
import soot.options.*;
import soot.toolkits.graph.*;
import soot.toolkits.scalar.FlowSet;

/**
 * Find all locals guaranteed to be defined at (just before) a given
 * program point.
 *
 * @author Navindra Umanee
 **/
public class GuaranteedDefs
{
    protected Map<Unit, List> unitToGuaranteedDefs;

    public GuaranteedDefs(UnitGraph graph)
    {
        if(Options.v().verbose())
            G.v().out.println("[" + graph.getBody().getMethod().getName() +
                               "]     Constructing GuaranteedDefs...");

        GuaranteedDefsAnalysis analysis = new GuaranteedDefsAnalysis(graph);

        // build map
        {
            unitToGuaranteedDefs = new HashMap<Unit, List>(graph.size() * 2 + 1, 0.7f);
            Iterator unitIt = graph.iterator();

            while(unitIt.hasNext()){
                Unit s = (Unit) unitIt.next();
                FlowSet set = (FlowSet) analysis.getFlowBefore(s);
                unitToGuaranteedDefs.put
                    (s, Collections.unmodifiableList(set.toList()));
            }
        }
    }

    /**
     * Returns a list of locals guaranteed to be defined at (just
     * before) program point <tt>s</tt>.
     **/
    public List getGuaranteedDefs(Unit s)
    {
        return unitToGuaranteedDefs.get(s);
    }
    
    /**
     * getmap
     */
    public Map<Unit, List> getUnitToGuaranteedDefs(Unit s){
    	return unitToGuaranteedDefs;
    }
    
}