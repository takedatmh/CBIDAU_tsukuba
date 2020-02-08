package sample.functionA;

import sample.common.SharedVarX;
import sample.common.SharedVarY;

public class CallerFromA {
	
	public int delete(int i) {
		if(i <10) {
			SharedVarX.publicStr = null;
		} else {
			i++;
		}
		return i;
	}
	
	public int update(int a){
		a = 1;
		if(a == 1){
			SharedVarY.staticInt = 200;
		} else {
			a = 0;
		}
		return a;
	}
}
