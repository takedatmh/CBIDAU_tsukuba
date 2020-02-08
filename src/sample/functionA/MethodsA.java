package sample.functionA;

public class MethodsA {
	
	public CallerFromA caller = null;
	
	public MethodsA(){
		this.caller = new CallerFromA();
	}
	
	public int method01(int a, int b) throws Exception {
		
		int ret = 0;
		
		if(a + b > 10){
			ret = caller.delete(a + b);
		} else if(a -b < 0){
			ret = caller.update(a);
		} else {
			throw new Exception();
		}
		
		return ret;
		
	}

}
