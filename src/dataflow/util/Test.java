package dataflow.util;

import java.util.Arrays;
import java.util.List;

public class Test {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String s = "$r3 = <simple.logic.Logic_static: java.lang.String staticVariable>";
		String d1 = "simple.logic.Logic_static";
		String d2 = "staticVariable";
		
		if(s.contains(d1) && s.contains(d2))
			System.out.println(d1);
		
		if(s.contains(d2))
			System.out.println(d2);
		
	}

}
