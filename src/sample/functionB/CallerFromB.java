package sample.functionB;

import sample.common.SharedVarX;
import sample.common.SharedVarY;

public class CallerFromB {

	public String read(int i) {
		
		String str = null;
		
		if(i <10) {
			str = SharedVarX.publicStr;
		} else {
			i++;
		}
		return str;
	}
	
	public String create(){
		
		SharedVarX.publicStr = new String("create");
		
		return "create";
	}
	
	public int update(int a){
		a = 1;
		
		if(a == 1){
			SharedVarY.staticInt = 300;
		} else {
			a = 0;
		}
		
		return a;
	
	}
	
}
